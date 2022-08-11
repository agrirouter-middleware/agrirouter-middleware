package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.DeviceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Component
public class OneTimeFixForDuplicateTeamSetContextIds {

    private static final Logger LOGGER = LoggerFactory.getLogger(OneTimeFixForDuplicateTeamSetContextIds.class);

    private final DeviceDescriptionRepository deviceDescriptionRepository;

    public OneTimeFixForDuplicateTeamSetContextIds(DeviceDescriptionRepository deviceDescriptionRepository) {
        this.deviceDescriptionRepository = deviceDescriptionRepository;
    }

    @PostConstruct
    @Transactional
    public void runOnce() {
        LOGGER.info("Running one time fix for duplicate team set context IDs");
        List<DeviceDescription> deviceDescriptions = deviceDescriptionRepository.findAll();
        final var groupedDeviceDescriptions = deviceDescriptions.stream().collect(
                groupingBy(DeviceDescription::getTeamSetContextId, counting()));
        final var duplicateTeamSetContextIds = groupedDeviceDescriptions.entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey).toList();
        LOGGER.info("Found {} duplicate team set context IDs", duplicateTeamSetContextIds.size());
        deviceDescriptionRepository.deleteAllByTeamSetContextIdIn(duplicateTeamSetContextIds);
    }

}
