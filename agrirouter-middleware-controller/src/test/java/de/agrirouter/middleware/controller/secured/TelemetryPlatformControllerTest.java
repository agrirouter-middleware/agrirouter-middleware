package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriod;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.record.RecordModule;

import java.util.Set;

public class TelemetryPlatformControllerTest {

    @Test
    public void givenValidTimeLogPeriod_whenConvertingItToDto_thenTheConversionShouldBeSuccessful() {
        var timeLogPeriod = new TimeLogPeriod(2L, 4L, 8, createRandomSetOfMessageIds());
        var modelMapper = new ModelMapper().registerModule(new RecordModule());
        var dto = modelMapper.map(timeLogPeriod, TimeLogPeriodDto.class);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(2L, dto.getBegin(), "The begin should be the same.");
        Assertions.assertEquals(4L, dto.getEnd(), "The end should be the same.");
        Assertions.assertEquals(8, dto.getNrOfTimeLogs(), "The number of time logs should be the same.");
        Assertions.assertNull(dto.getHumanReadableBegin(), "The human readable begin should be null, since it is not set.");
        Assertions.assertNull(dto.getHumanReadableEnd(), "The human readable end should be null, since it is not set.");
    }

    private Set<String> createRandomSetOfMessageIds() {
        return Set.of("message_id_32", "message_id_64", "message_id_128", "message_id_256",
                "message_id_512", "message_id_1024", "message_id_2048", "message_id_4096",
                "message_id_8192", "message_id_16384", "message_id_32768", "message_id_65536",
                "message_id_131072", "message_id_262144", "message_id_524288", "message_id_1048576");
    }

}
