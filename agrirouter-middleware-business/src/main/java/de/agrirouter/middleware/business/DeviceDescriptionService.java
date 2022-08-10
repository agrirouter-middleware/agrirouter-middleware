package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ActivateDeviceEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.registration.TransientMachineRegistrationCache;
import de.agrirouter.middleware.business.parameters.CreateDeviceDescriptionParameters;
import de.agrirouter.middleware.business.parameters.RegisterMachineParameters;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.Device;
import de.agrirouter.middleware.domain.DeviceDescription;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.DeviceDescriptionRepository;
import de.agrirouter.middleware.persistence.DeviceRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.saschadoemer.iso11783.clientname.ClientName;
import de.saschadoemer.iso11783.clientname.ClientNameDecoder;
import efdi.GrpcEfdi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to handle business operations round about the device descriptions.
 */
@Slf4j
@Service
public class DeviceDescriptionService {

    private static final ConcurrentHashMap<String, Instant> lastTimeTheDeviceDescriptionHasBeenSent = new ConcurrentHashMap<>();

    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final EndpointRepository endpointRepository;
    private final DeviceRepository deviceRepository;
    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;

    private final TransientMachineRegistrationCache machineRegistrationCache;

    public DeviceDescriptionService(DeviceDescriptionRepository deviceDescriptionRepository,
                                    EndpointRepository endpointRepository,
                                    DeviceRepository deviceRepository,
                                    SendMessageIntegrationService sendMessageIntegrationService,
                                    BusinessOperationLogService businessOperationLogService,
                                    TransientMachineRegistrationCache machineRegistrationCache) {
        this.deviceDescriptionRepository = deviceDescriptionRepository;
        this.endpointRepository = endpointRepository;
        this.deviceRepository = deviceRepository;
        this.sendMessageIntegrationService = sendMessageIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
        this.machineRegistrationCache = machineRegistrationCache;
    }

    /**
     * Save a device description received from the AR.
     *
     * @param contentMessage -
     */
    public void saveReceivedDeviceDescription(ContentMessage contentMessage) {
        log.debug("Received a device description for the following team set '{}'.", contentMessage.getContentMessageMetadata().getTeamSetContextId());
        final var optionalDocument = convert(contentMessage.getMessageContent());
        if (optionalDocument.isPresent()) {
            final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                DeviceDescription deviceDescription = new DeviceDescription();
                deviceDescription.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
                deviceDescription.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                deviceDescription.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                deviceDescription.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                deviceDescription.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                deviceDescription.setExternalEndpointId(endpoint.getExternalEndpointId());
                deviceDescription.setTeamSetContextId(contentMessage.getContentMessageMetadata().getTeamSetContextId());
                deviceDescription.setDeactivated(false);
                deviceDescription.setDocument(optionalDocument.get());
                deviceDescriptionRepository.save(deviceDescription);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Device description received and saved to the database.");
                createOrFindDevices(endpoint, contentMessage.getMessageContent(), deviceDescription);
            } else {
                log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotParseDeviceDescription().asLogMessage());
        }
    }

    /**
     * Save a device description received from the AR.
     *
     * @param createDeviceDescriptionParameters -
     */
    public void saveCreatedDeviceDescription(CreateDeviceDescriptionParameters createDeviceDescriptionParameters) {
        log.debug("Create a new device description for the following team set '{}'.", createDeviceDescriptionParameters.getTeamSetContextId());
        final var optionalISO11783TaskData = parse(createDeviceDescriptionParameters.getBase64EncodedDeviceDescription());
        if (optionalISO11783TaskData.isPresent()) {
            final var optionalDocument = convert(optionalISO11783TaskData.get());
            if (optionalDocument.isPresent()) {
                final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(createDeviceDescriptionParameters.getEndpoint().getAgrirouterEndpointId());
                if (optionalEndpoint.isPresent()) {
                    final var endpoint = optionalEndpoint.get();
                    DeviceDescription deviceDescription = new DeviceDescription();
                    deviceDescription.setAgrirouterEndpointId(createDeviceDescriptionParameters.getEndpoint().getAgrirouterEndpointId());
                    deviceDescription.setExternalEndpointId(createDeviceDescriptionParameters.getEndpoint().getExternalEndpointId());
                    deviceDescription.setTeamSetContextId(createDeviceDescriptionParameters.getTeamSetContextId());
                    deviceDescription.setDocument(optionalDocument.get());
                    deviceDescription.setDeactivated(true);
                    deviceDescription.setBase64EncodedDeviceDescription(createDeviceDescriptionParameters.getBase64EncodedDeviceDescription());
                    deviceDescriptionRepository.save(deviceDescription);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Device description created and saved to the database.");
                    createOrFindDevices(optionalEndpoint.get(), Base64.getDecoder().decode(createDeviceDescriptionParameters.getBase64EncodedDeviceDescription()), deviceDescription);
                } else {
                    log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
                }
            } else {
                log.error(ErrorMessageFactory.couldNotParseDeviceDescription().asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotParseDeviceDescription().asLogMessage());
        }
    }

    private void createOrFindDevices(Endpoint endpoint, byte[] rawDeviceDescription, DeviceDescription deviceDescription) {
        final var optionalISO11783_taskData = parse(rawDeviceDescription);
        if (optionalISO11783_taskData.isPresent()) {
            final var iso11783_taskData = optionalISO11783_taskData.get();
            if (!iso11783_taskData.getDeviceList().isEmpty()) {
                iso11783_taskData.getDeviceList().forEach(d -> {
                    final var clientName = decodeSafely(d);
                    clientName.ifPresentOrElse(cn -> {
                        if (StringUtils.isBlank(d.getDeviceSerialNumber())) {
                            log.warn("The device serial number is empty, this could lead to problems with the identification of the machines.");
                        }
                        final var optionalDevice = deviceRepository.findByClientName_ManufacturerCodeAndSerialNumber(cn.getManufacturerCode(), d.getDeviceSerialNumber());
                        optionalDevice.ifPresentOrElse(device -> {
                            log.debug("The device has been found, using the already existing device.");
                            if (StringUtils.isBlank(device.getInternalDeviceId())) {
                                device.setInternalDeviceId(IdFactory.deviceId());
                            }
                            device.getDeviceDescriptions().add(deviceDescription);
                            deviceRepository.save(device);
                        }, () -> {
                            log.debug("There has been no device found, creating new device.");
                            final var device = new Device();
                            device.setInternalDeviceId(IdFactory.deviceId());
                            device.setClientName(cn);
                            device.setExternalEndpointId(endpoint.getExternalEndpointId());
                            device.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
                            device.setSerialNumber(d.getDeviceSerialNumber());
                            device.getDeviceDescriptions().add(deviceDescription);
                            deviceRepository.save(device);
                        });
                    }, () -> log.error("Could not decode client name. Device will not be created."));
                });
            } else {
                log.warn("There are no devices within the device description. Skipping the device description.");
            }
        }
    }

    private Optional<ClientName> decodeSafely(GrpcEfdi.Device d) {
        try {
            return Optional.of(ClientNameDecoder.decode(new String(Hex.encodeHex(d.getClientName().toByteArray()))));
        } catch (IllegalArgumentException e) {
            log.error("Could not decode client name.", e);
            return Optional.empty();
        }
    }

    /**
     * Parse and return device description.
     *
     * @param base64EncodedDeviceDescription -
     * @return -
     */
    public Optional<GrpcEfdi.ISO11783_TaskData> parse(String base64EncodedDeviceDescription) {
        final var bytesString = Base64.getDecoder().decode(base64EncodedDeviceDescription);
        try {
            return Optional.ofNullable(GrpcEfdi.ISO11783_TaskData.parseFrom(ByteString.copyFrom(bytesString)));
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse device description.", e);
            return Optional.empty();
        }
    }

    /**
     * Parse and return device description.
     *
     * @param deviceDescription -
     * @return -
     */
    public Optional<GrpcEfdi.ISO11783_TaskData> parse(byte[] deviceDescription) {
        try {
            return Optional.ofNullable(GrpcEfdi.ISO11783_TaskData.parseFrom(ByteString.copyFrom(deviceDescription)));
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse device description.", e);
            return Optional.empty();
        }
    }

    /**
     * Read a device description as ByteString to send it to the AR.
     *
     * @param base64EncodedDeviceDescription -
     * @return -
     */
    public ByteString asByteString(String base64EncodedDeviceDescription) {
        final var bytes = Base64.getDecoder().decode(base64EncodedDeviceDescription);
        return ByteString.copyFrom(bytes);
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param deviceDescription -
     * @return -
     */
    public Optional<Document> convert(byte[] deviceDescription) {
        try {
            return convert(GrpcEfdi.ISO11783_TaskData.parseFrom(ByteString.copyFrom(deviceDescription)));
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse device description. Creating document without the original device description.", e);
            return Optional.empty();
        }
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param deviceDescription -
     * @return -
     */
    public Optional<Document> convert(GrpcEfdi.ISO11783_TaskData deviceDescription) {
        try {
            String json = JsonFormat.printer().print(deviceDescription);
            log.debug("The original protobuf has been transformed to JSON.");
            log.trace("{}", json);
            Document document = Document.parse(json);
            log.debug("Converting the JSON to a BSON document.");
            log.trace("{}", document);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse device description. Creating document without the original device description.", e);
            return Optional.empty();
        }
    }


    /**
     * Register a machine and return the dedicated team set context ID.
     *
     * @param registerMachineParameters The parameters for the registration.
     * @return -
     */
    public String registerMachine(RegisterMachineParameters registerMachineParameters) {
        String teamSetContextId;
        if (StringUtils.isBlank(registerMachineParameters.getCustomTeamSetContextId())) {
            log.debug("The custom team set context ID is empty, using the new one.");
            teamSetContextId = IdFactory.teamSetContextId();
        } else {
            teamSetContextId = registerMachineParameters.getCustomTeamSetContextId();
        }
        return registerMachine(teamSetContextId, registerMachineParameters);
    }

    /**
     * Register a machine and return the dedicated team set context ID.
     *
     * @param teamSetContextId          The team set context ID.
     * @param registerMachineParameters The parameters for the registration.
     * @return -
     */
    private String registerMachine(String teamSetContextId, RegisterMachineParameters registerMachineParameters) {
        log.debug("Register machine and return a team set context ID for the device description.");
        final var optionalISO11783TaskData = parse(registerMachineParameters.getBase64EncodedDeviceDescription());
        if (optionalISO11783TaskData.isPresent()) {
            final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(registerMachineParameters.getExternalEndpointId());
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                final var createDeviceDescriptionParameters = new CreateDeviceDescriptionParameters();
                createDeviceDescriptionParameters.setBase64EncodedDeviceDescription(registerMachineParameters.getBase64EncodedDeviceDescription());
                createDeviceDescriptionParameters.setEndpoint(endpoint);
                createDeviceDescriptionParameters.setTeamSetContextId(teamSetContextId);
                saveCreatedDeviceDescription(createDeviceDescriptionParameters);
                final var messagingIntegrationParameters = new MessagingIntegrationParameters();
                messagingIntegrationParameters.setMessage(asByteString(registerMachineParameters.getBase64EncodedDeviceDescription()));
                messagingIntegrationParameters.setExternalEndpointId(endpoint.getExternalEndpointId());
                messagingIntegrationParameters.setTeamSetContextId(teamSetContextId);
                messagingIntegrationParameters.setTechnicalMessageType(ContentMessageType.ISO_11783_DEVICE_DESCRIPTION);
                sendMessageIntegrationService.publish(messagingIntegrationParameters);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Device description for machine has been registered.");
                return teamSetContextId;
            } else {
                log.debug("No endpoint found for the given external endpoint ID. Caching the device description. This could be the case if the virtual endpoint is not yet created.");
                machineRegistrationCache.put(registerMachineParameters.getExternalEndpointId(), teamSetContextId, registerMachineParameters);
                return teamSetContextId;
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotParseDeviceDescription());
        }
    }

    /**
     * Activate a device when the corresponding device description was accepted by the AR.
     *
     * @param activateDeviceEvent -
     */
    @EventListener
    public void activateDevice(ActivateDeviceEvent activateDeviceEvent) {
        log.debug("Activating device for the team set '{}'.", activateDeviceEvent.getTeamSetContextId());
        final var optionalDevice = deviceDescriptionRepository.findByTeamSetContextId(activateDeviceEvent.getTeamSetContextId());
        if (optionalDevice.isPresent()) {
            final var device = optionalDevice.get();
            activateDevice(activateDeviceEvent.getTeamSetContextId());
            businessOperationLogService.log(new EndpointLogInformation(device.getExternalEndpointId(), device.getAgrirouterEndpointId()), "Device has been activated.");
        } else {
            log.error(ErrorMessageFactory.couldNotFindDevice().asLogMessage());
        }
    }


    /**
     * Activate a specific device when the device description was in example accepted by the AR.
     *
     * @param teamSetContextId -
     */
    private void activateDevice(String teamSetContextId) {
        log.debug("Activate the device description for the following team set '{}'.", teamSetContextId);
        final var optionalDeviceDescription = deviceDescriptionRepository.findByTeamSetContextId(teamSetContextId);
        if (optionalDeviceDescription.isPresent()) {
            final var deviceDescription = optionalDeviceDescription.get();
            deviceDescription.setDeactivated(false);
            deviceDescriptionRepository.save(deviceDescription);
        } else {
            log.error(ErrorMessageFactory.couldNotFindDevice().asLogMessage());
        }
    }

    /**
     * Look up and resend the device description if needed.
     *
     * @param teamSetContextId -
     */
    public void resendDeviceDescriptionIfNecessary(String teamSetContextId) {
        final var optionalDeviceDescription = deviceDescriptionRepository.findByTeamSetContextId(teamSetContextId);
        if (optionalDeviceDescription.isPresent()) {
            final var deviceDescription = optionalDeviceDescription.get();
            final var theLastTimeTheDeviceDescriptionHasBeenSent = lastTimeTheDeviceDescriptionHasBeenSent.get(teamSetContextId);
            if (null == theLastTimeTheDeviceDescriptionHasBeenSent || theLastTimeTheDeviceDescriptionHasBeenSent.plus(1, ChronoUnit.HOURS).isBefore(Instant.now())) {
                log.debug("Sending the device for the team set '{}' since it has not been sent before or the last time the device description has been sent was more than 1 hour ago.", teamSetContextId);
                final var messagingIntegrationParameters = new MessagingIntegrationParameters();
                messagingIntegrationParameters.setMessage(asByteString(deviceDescription.getBase64EncodedDeviceDescription()));
                messagingIntegrationParameters.setExternalEndpointId(deviceDescription.getExternalEndpointId());
                messagingIntegrationParameters.setTeamSetContextId(teamSetContextId);
                messagingIntegrationParameters.setTechnicalMessageType(ContentMessageType.ISO_11783_DEVICE_DESCRIPTION);
                sendMessageIntegrationService.publish(messagingIntegrationParameters);
                businessOperationLogService.log(new EndpointLogInformation(deviceDescription.getExternalEndpointId(), deviceDescription.getAgrirouterEndpointId()), "Device description has been resent.");
                lastTimeTheDeviceDescriptionHasBeenSent.put(teamSetContextId, Instant.now());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldnotFindTeamSet(teamSetContextId));
        }
    }

    /**
     * Checks if there is a cached device description for the given external endpoint ID and sends it to the agrirouter. Could be the case if sending the device description was faster than the virtual endpoint was created.
     *
     * @param externalEndpointId The external endpoint ID of the virtual endpoint.
     */
    public void checkAndSendCachedDeviceDescription(String externalEndpointId) {
        machineRegistrationCache.pop(externalEndpointId).ifPresent(ce -> {
            log.debug("Sending the cached device description for the virtual endpoint '{}' to the agrirouter.", externalEndpointId);
            registerMachine(ce.teamSetContextId(), ce.registerMachineParameters());
        });
    }
}
