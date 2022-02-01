package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriod;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriods;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriodsForTeamSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Caching implementation for search results.
 */
@Slf4j
@Component
public class TimeLogPeriodSearchCache {

    /**
     * Cached entries for search of time logs.
     */
    private final Map<String, List<TimeLogPeriodsForTeamSet>> cachedTimeLogPeriods = new ConcurrentHashMap<>();

    /**
     * Place a cached result within the cache.
     *
     * @param deviceId                  -
     * @param timeLogPeriodsForTeamSets -
     */
    public void put(String deviceId, List<TimeLogPeriodsForTeamSet> timeLogPeriodsForTeamSets) {
        if (StringUtils.isEmpty(deviceId)) {
            log.warn("Could not add time log periods since the device ID was empty.");
        } else {
            if (null != cachedTimeLogPeriods.get(deviceId)) {
                log.debug("Removing former results to avoid duplicates.");
                cachedTimeLogPeriods.remove(deviceId);
            }
            cachedTimeLogPeriods.put(deviceId, timeLogPeriodsForTeamSets);
        }
    }

    /**
     * Get all messages by their time log period ID.
     *
     * @return All messages from teh time log period.
     */
    public Set<String> getMessagesForTimeLogPeriod(String deviceId, String teamSetContextId, String timeLogPeriodId) {
        final var timeLogPeriodsForTeamSets = cachedTimeLogPeriods.get(deviceId);
        if (null != timeLogPeriodsForTeamSets) {
            AtomicReference<Set<String>> messageIds = new AtomicReference<>(Collections.emptySet());
            timeLogPeriodsForTeamSets
                    .stream()
                    .filter(timeLogPeriodsForTeamSet -> StringUtils.equals(timeLogPeriodsForTeamSet.getTeamSetContextId(), teamSetContextId))
                    .map(TimeLogPeriodsForTeamSet::getTimeLogPeriods)
                    .map(TimeLogPeriods::getTimeLogPeriods)
                    .collect(ArrayList<TimeLogPeriod>::new, List::addAll, List::addAll)
                    .stream()
                    .filter(timeLogPeriod -> StringUtils.equals(timeLogPeriod.getId(), timeLogPeriodId))
                    .findFirst()
                    .ifPresentOrElse(timeLogPeriod -> messageIds.set(timeLogPeriod.getMessageIds()), () -> messageIds.set(Collections.emptySet()));
            return messageIds.get();
        } else {
            log.warn("There are no messages for this combination of parameters, therefore the result is an empty set of messages.");
            return Collections.emptySet();
        }
    }

}
