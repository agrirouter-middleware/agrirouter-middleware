package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.CriticalBusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriod;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriods;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriodsForDevice;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriodsForTeamSet;
import de.agrirouter.middleware.business.parameters.MessagesForTimeLogPeriodParameters;
import de.agrirouter.middleware.business.parameters.PublishTimeLogParameters;
import de.agrirouter.middleware.business.parameters.SearchMachinesParameters;
import de.agrirouter.middleware.business.parameters.SearchTimeLogPeriodsParameters;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.DeviceDescription;
import de.agrirouter.middleware.domain.TimeLog;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.TimeLogRepository;
import efdi.GrpcEfdi;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.agrirouter.middleware.api.logging.BusinessOperationLogService.NA;

/**
 * Service to handle business operations round about the device descriptions.
 */
@Slf4j
@Service
public class TimeLogService {

    public static final int TIME_LOG_PERIOD_OFFSET = 300_000;

    private final TimeLogRepository timeLogRepository;
    private final EndpointRepository endpointRepository;
    private final DeviceDescriptionService deviceDescriptionService;
    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final DeviceService deviceService;
    private final BusinessOperationLogService businessOperationLogService;
    private final MessageCache messageCache;

    public TimeLogService(TimeLogRepository timeLogRepository,
                          EndpointRepository endpointRepository,
                          DeviceDescriptionService deviceDescriptionService,
                          SendMessageIntegrationService sendMessageIntegrationService,
                          DeviceService deviceService,
                          BusinessOperationLogService businessOperationLogService, MessageCache messageCache) {
        this.timeLogRepository = timeLogRepository;
        this.endpointRepository = endpointRepository;
        this.deviceDescriptionService = deviceDescriptionService;
        this.sendMessageIntegrationService = sendMessageIntegrationService;
        this.deviceService = deviceService;
        this.businessOperationLogService = businessOperationLogService;
        this.messageCache = messageCache;
    }

    /**
     * Publish time logs.
     *
     * @param publishTimeLogParameters -
     */
    public void publish(PublishTimeLogParameters publishTimeLogParameters) {
        deviceDescriptionService.resendDeviceDescriptionIfNecessary(publishTimeLogParameters.getTeamSetContextId());
        final var messagingIntegrationParameters = new MessagingIntegrationParameters(publishTimeLogParameters.getExternalEndpointId(),
                ContentMessageType.ISO_11783_TIME_LOG,
                Collections.emptyList(),
                null,
                asByteString(publishTimeLogParameters.getBase64EncodedTimeLog()),
                publishTimeLogParameters.getTeamSetContextId());
        try {
            sendMessageIntegrationService.publish(messagingIntegrationParameters);
            businessOperationLogService.log(new EndpointLogInformation(publishTimeLogParameters.getExternalEndpointId(), NA), "Time log has been published");
        } catch (CriticalBusinessException e) {
            log.debug("Could not publish data. There was a critical business exception. {}", e.getErrorMessage());
            messageCache.put(publishTimeLogParameters.getExternalEndpointId(), messagingIntegrationParameters);
            businessOperationLogService.log(new EndpointLogInformation(publishTimeLogParameters.getExternalEndpointId(), NA), "Non telemetry data not published. Message saved to cache.");
        }
    }

    private ByteString asByteString(String base64EncodedTimeLog) {
        try {
            return GrpcEfdi.TimeLog.parseFrom(Base64.getDecoder().decode(base64EncodedTimeLog)).toByteString();
        } catch (InvalidProtocolBufferException e) {
            throw new BusinessException(ErrorMessageFactory.couldNotParseTimeLog());
        }
    }

    /**
     * Save a time log received from the AR.
     *
     * @param contentMessage -
     */
    public void save(ContentMessage contentMessage) {
        log.debug("Received a time log for the following team set '{}'.", contentMessage.getContentMessageMetadata().getTeamSetContextId());
        final var optionalDocument = convert(contentMessage.getMessageContent());
        if (optionalDocument.isPresent()) {
            final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                final var timeLog = new TimeLog();
                timeLog.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
                timeLog.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                timeLog.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                timeLog.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                timeLog.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                timeLog.setExternalEndpointId(endpoint.getExternalEndpointId());
                timeLog.setTeamSetContextId(contentMessage.getContentMessageMetadata().getTeamSetContextId());
                timeLog.setDocument(optionalDocument.get());
                timeLogRepository.save(timeLog);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Time log has been received and saved.");
            } else {
                log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotParseTimeLog().asLogMessage());
        }
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param timeLog -
     * @return -
     */
    public Optional<Document> convert(byte[] timeLog) {
        try {
            return convert(GrpcEfdi.TimeLog.parseFrom(ByteString.copyFrom(timeLog)));
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse time log. Creating document without the original time log.", e);
            return Optional.empty();
        }
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param timeLog -
     * @return A document to save in the database.
     */
    public Optional<Document> convert(GrpcEfdi.TimeLog timeLog) {
        try {
            String json = JsonFormat.printer().print(timeLog);
            log.debug("The original protobuf has been transformed to JSON.");
            log.trace("{}", json);
            Document document = Document.parse(json);
            log.debug("Converting the JSON to a BSON document.");
            log.trace("{}", document);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse time log. Creating document without the original time log.", e);
            return Optional.empty();
        }
    }

    /**
     * Fetch all messages for a time log period.
     *
     * @param messagesForTimeLogPeriodParameters -
     * @return The list of time logs.
     */
    public List<TimeLog> getMessagesForTimeLogPeriod(MessagesForTimeLogPeriodParameters messagesForTimeLogPeriodParameters) {
        List<TimeLog> timeLogs;
        if (messagesForTimeLogPeriodParameters.shouldFilterByTime()) {
            timeLogs = timeLogRepository.findAllByTimestampBetween(messagesForTimeLogPeriodParameters.getSendFromOrDefault(), messagesForTimeLogPeriodParameters.getSendToOrDefault());
            if (null != messagesForTimeLogPeriodParameters.getDdisToList() && !messagesForTimeLogPeriodParameters.getDdisToList().isEmpty()) {
                timeLogs.forEach(timeLog -> {
                    final var document = timeLog.getDocument();
                    final var timeEntries = document.getList("time", Document.class);
                    timeEntries.forEach(timeEntry -> {
                        final var dataLogValues = timeEntry.getList("dataLogValue", Document.class);
                        final var filteredDataLogValues = dataLogValues.stream().filter(d -> messagesForTimeLogPeriodParameters.getDdisToList().contains(d.getInteger("processDataDdi"))).toList();
                        timeEntry.put("dataLogValue", filteredDataLogValues);
                    });
                    document.put("time", timeEntries);
                });
            }
            return timeLogs;
        } else {
            log.warn("This would have been a search over all time logs, this causes an exception, since there has to be either a filter for period or a filter for time.");
            throw new BusinessException(ErrorMessageFactory.missingFilterCriteriaForTimeLogSearch());
        }
    }

    /**
     * Get the time periods with machine data.
     *
     * @param searchTimeLogPeriodsParameters -
     */
    public List<TimeLogPeriodsForDevice> searchTimeLogPeriods(SearchTimeLogPeriodsParameters searchTimeLogPeriodsParameters) {
        final var searchMachinesParameters = new SearchMachinesParameters();
        searchMachinesParameters.setExternalEndpointId(searchTimeLogPeriodsParameters.getExternalEndpointId());
        searchMachinesParameters.setInternalDeviceIds(searchTimeLogPeriodsParameters.getInternalDeviceIds());
        final var devices = deviceService.search(searchMachinesParameters);
        final var timeLogPeriods = new ArrayList<TimeLogPeriodsForDevice>();
        devices.forEach(device -> {
            final var teamSetContextIds = device.getDeviceDescriptions().stream().map(DeviceDescription::getTeamSetContextId).collect(Collectors.toCollection(HashSet::new));
            if (!teamSetContextIds.isEmpty()) {
                log.debug("Currently there are {} team sets available. Fetching the data for the team sets.", teamSetContextIds.size());
                log.trace(String.join(",", teamSetContextIds));
                final var segmentedTimeLogsForTeamSetContextId = getSegmentedTimeLogsForTeamSetContextId(teamSetContextIds, searchTimeLogPeriodsParameters);
                final var timeLogPeriodsForTeamSets = new ArrayList<TimeLogPeriodsForTeamSet>();
                segmentedTimeLogsForTeamSetContextId.forEach((teamSetContextId, timeLogs) -> {
                    final var segmentedTimeLogsForTimePeriod = getSegmentedTimeLogsForTimePeriod(timeLogs);
                    final var timeLogPeriodsForTeamSet = new TimeLogPeriodsForTeamSet(teamSetContextId, segmentedTimeLogsForTimePeriod);
                    if (searchTimeLogPeriodsParameters.isFilterEmptyEntries()) {
                        if (timeLogs.isEmpty()) {
                            log.debug("Since the filtering of empty entries is enabled and the time logs for the team set '{}' are empty, the entry is not added.", teamSetContextId);
                        } else {
                            timeLogPeriodsForTeamSets.add(timeLogPeriodsForTeamSet);
                        }
                    } else {
                        timeLogPeriodsForTeamSets.add(timeLogPeriodsForTeamSet);
                    }
                });
                if (searchTimeLogPeriodsParameters.isFilterEmptyEntries()) {
                    if (timeLogPeriodsForTeamSets.isEmpty()) {
                        log.debug("Since the filtering of empty entries is enabled and the time logs for the device '{}' are empty, the device is not added.", device.getInternalDeviceId());
                    } else {
                        timeLogPeriods.add(new TimeLogPeriodsForDevice(device, timeLogPeriodsForTeamSets));
                    }
                } else {
                    timeLogPeriods.add(new TimeLogPeriodsForDevice(device, timeLogPeriodsForTeamSets));
                }
            }
        });
        return timeLogPeriods;
    }

    private HashMap<String, List<TimeLog>> getSegmentedTimeLogsForTeamSetContextId(HashSet<String> teamSetContextIds, SearchTimeLogPeriodsParameters searchTimeLogPeriodsParameters) {
        final var segmentedTimeLogs = new HashMap<String, List<TimeLog>>();
        teamSetContextIds.forEach(teamSetContextId -> {
            final List<TimeLog> timeLogs;
            if (searchTimeLogPeriodsParameters.shouldFilterByTime()) {
                timeLogs = timeLogRepository.findForTeamSetContextIdAndTimestampBetween(teamSetContextId, searchTimeLogPeriodsParameters.getSendFromOrDefault(), searchTimeLogPeriodsParameters.getSendToOrDefault());
            } else {
                timeLogs = timeLogRepository.findForTeamSetContextId(teamSetContextId);
            }
            segmentedTimeLogs.put(teamSetContextId, timeLogs);
        });
        if (searchTimeLogPeriodsParameters.isFilterEmptyEntries()) {
            final var teamSetContextIdsToBeRemoved = new ArrayList<String>();
            segmentedTimeLogs.forEach((teamSetContextId, timeLogs) -> {
                if (timeLogs.isEmpty()) {
                    log.debug("Since the filtering of empty entries is enabled and the time logs for the team set '{}' are empty, the entry is removed.", teamSetContextId);
                    teamSetContextIdsToBeRemoved.add(teamSetContextId);
                }
            });
            teamSetContextIdsToBeRemoved.forEach(segmentedTimeLogs::remove);
        }
        return segmentedTimeLogs;
    }

    private TimeLogPeriods getSegmentedTimeLogsForTimePeriod(List<TimeLog> timeLogs) {
        if (null != timeLogs && timeLogs.size() > 0) {
            var lastTimeStamp = -1L;
            var segmentedTimeLogs = new ArrayList<List<TimeLog>>();
            var currentPeriod = new ArrayList<TimeLog>();
            for (TimeLog timeLog : timeLogs) {
                final var currentTimeStamp = timeLog.getTimestamp();
                if (lastTimeStamp != -1 && currentTimeStamp > (lastTimeStamp + TIME_LOG_PERIOD_OFFSET)) {
                    log.debug("This is the beginning of a new period, creating a new segment.");
                    currentPeriod.sort(Comparator.comparingLong(TimeLog::getTimestamp));
                    segmentedTimeLogs.add(currentPeriod);
                    currentPeriod = new ArrayList<>();
                }
                currentPeriod.add(timeLog);
                lastTimeStamp = currentTimeStamp;
            }
            //noinspection ConstantConditions
            if (!currentPeriod.isEmpty()) {
                log.debug("There are some time logs left, creating a new segment.");
                segmentedTimeLogs.add(currentPeriod);
            }
            log.debug("We segmented the whole list of time logs. Now reducing them to a set of periods.");
            final var timeLogPeriods = new ArrayList<TimeLogPeriod>();
            segmentedTimeLogs.forEach(period -> {
                period.sort(Comparator.comparingLong(TimeLog::getTimestamp));
                timeLogPeriods.add(new TimeLogPeriod(period.get(0).getTimestamp(), period.get(period.size() - 1).getTimestamp(), period.size(), period.stream().map(TimeLog::getMessageId).collect(Collectors.toSet())));
            });
            return new TimeLogPeriods(timeLogPeriods);
        } else {
            return new TimeLogPeriods(Collections.emptyList());
        }
    }

}
