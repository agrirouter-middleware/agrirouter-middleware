package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.business.parameters.MessagesForTimeLogPeriodParameters;
import de.agrirouter.middleware.domain.documents.TimeLog;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriod;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriods;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TimeLogServiceTest {

    @Mock
    private de.agrirouter.middleware.persistence.mongo.TimeLogRepository timeLogRepository;

    @Mock
    private EndpointService endpointService;

    @Mock
    private DeviceDescriptionService deviceDescriptionService;

    @Mock
    private de.agrirouter.middleware.integration.SendMessageIntegrationService sendMessageIntegrationService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private de.agrirouter.middleware.api.logging.BusinessOperationLogService businessOperationLogService;

    @InjectMocks
    private TimeLogService timeLogService;

    @Test
    void getMessagesForTimeLogPeriod_withNoTimeFilter_throwsBusinessException() {
        var params = new MessagesForTimeLogPeriodParameters();
        // sendFrom and sendTo are both null → shouldFilterByTime() == false

        assertThrows(BusinessException.class, () -> timeLogService.getMessagesForTimeLogPeriod(params));
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withEmptyList_returnsEmptyPeriods() {
        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", new ArrayList<TimeLog>());

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).isEmpty();
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withNullList_returnsEmptyPeriods() {
        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", (Object) null);

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).isEmpty();
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withSingleTimelog_returnsOnePeriodWithOnEntry() {
        var timeLog = createTimeLog("msg-1", 1000L);
        List<TimeLog> timeLogs = List.of(timeLog);

        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", timeLogs);

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).hasSize(1);
        TimeLogPeriod period = result.timeLogPeriods().get(0);
        assertThat(period.begin()).isEqualTo(1000L);
        assertThat(period.end()).isEqualTo(1000L);
        assertThat(period.nrOfTimeLogs()).isEqualTo(1);
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withConsecutiveTimelogs_returnsOnePeriod() {
        // Consecutive entries (within TIME_LOG_PERIOD_OFFSET of each other)
        var tl1 = createTimeLog("msg-1", 1000L);
        var tl2 = createTimeLog("msg-2", 1000L + 100_000L); // 100 seconds apart - within 300 second offset
        var tl3 = createTimeLog("msg-3", 1000L + 200_000L);
        List<TimeLog> timeLogs = List.of(tl1, tl2, tl3);

        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", timeLogs);

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).hasSize(1);
        TimeLogPeriod period = result.timeLogPeriods().get(0);
        assertThat(period.nrOfTimeLogs()).isEqualTo(3);
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withGapBetweenLogs_returnsMultiplePeriods() {
        // Two groups separated by more than TIME_LOG_PERIOD_OFFSET (300_000 ms)
        var tl1 = createTimeLog("msg-1", 1_000L);
        var tl2 = createTimeLog("msg-2", 1_100L);
        // Gap > 300_000 ms
        var tl3 = createTimeLog("msg-3", 1_000_000L);
        var tl4 = createTimeLog("msg-4", 1_100_000L);
        List<TimeLog> timeLogs = List.of(tl1, tl2, tl3, tl4);

        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", timeLogs);

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).hasSize(2);
        assertThat(result.timeLogPeriods().get(0).nrOfTimeLogs()).isEqualTo(2);
        assertThat(result.timeLogPeriods().get(1).nrOfTimeLogs()).isEqualTo(2);
    }

    @Test
    void getSegmentedTimeLogsForTimePeriod_withThreeGroups_returnsThreePeriods() {
        var tl1 = createTimeLog("msg-1", 100L);
        var tl2 = createTimeLog("msg-2", 200L);
        // gap
        var tl3 = createTimeLog("msg-3", 1_000_000L);
        // gap
        var tl4 = createTimeLog("msg-4", 5_000_000L);
        List<TimeLog> timeLogs = List.of(tl1, tl2, tl3, tl4);

        TimeLogPeriods result = ReflectionTestUtils.invokeMethod(timeLogService, "getSegmentedTimeLogsForTimePeriod", timeLogs);

        assertThat(result).isNotNull();
        assertThat(result.timeLogPeriods()).hasSize(3);
    }

    @Test
    void convert_withInvalidBytes_returnsEmpty() {
        byte[] invalidBytes = "not-a-protobuf".getBytes();

        var result = timeLogService.convert(invalidBytes);

        assertThat(result).isEmpty();
    }

    private TimeLog createTimeLog(String messageId, long timestamp) {
        var timeLog = new TimeLog();
        timeLog.setMessageId(messageId);
        timeLog.setTimestamp(timestamp);
        return timeLog;
    }
}
