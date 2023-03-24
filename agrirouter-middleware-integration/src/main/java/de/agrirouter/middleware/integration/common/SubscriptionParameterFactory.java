package de.agrirouter.middleware.integration.common;

import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import de.agrirouter.middleware.domain.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Factory to create subscription parameters.
 */
@Component
@Slf4j
public class SubscriptionParameterFactory {

    @Value("${app.subscriptions.ddi.range.start}")
    private int ddiRangeStart;

    @Value("${app.subscriptions.ddi.range.end}")
    private int ddiRangeEnd;


    /**
     * Create the list of subscription parameters for the given application.
     *
     * @param application The application.
     * @return List of subscriptions to set.
     */
    public List<SetSubscriptionParameters.Subscription> create(Application application) {
        List<SetSubscriptionParameters.Subscription> subscriptions = new ArrayList<>();
        application.getSupportedTechnicalMessageTypes().forEach(supportedTechnicalMessageType -> {
            if (supportedTechnicalMessageType.getDirection().equals(Capabilities.CapabilitySpecification.Direction.RECEIVE)
                    || supportedTechnicalMessageType.getDirection().equals(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE)) {
                SetSubscriptionParameters.Subscription subscription = new SetSubscriptionParameters.Subscription();
                subscription.setTechnicalMessageType(supportedTechnicalMessageType.getTechnicalMessageType());
                if (ContentMessageType.ISO_11783_TIME_LOG == supportedTechnicalMessageType.getTechnicalMessageType()) {
                    subscription.setPosition(true);
                    if (null == application.getApplicationSettings() || application.getApplicationSettings().getDdiCombinationsToSubscribeFor().isEmpty()) {
                        log.debug("The application did not define any DDIs, therefore the whole range from DDI 0 to 600 is subscribed.");
                        subscription.setDdis(IntStream.rangeClosed(ddiRangeStart, ddiRangeEnd).boxed().collect(Collectors.toList()));
                    } else {
                        log.debug("The application did define (multiple) ranges of DDIs.");
                        List<Integer> ddis = new ArrayList<>();
                        application.getApplicationSettings().getDdiCombinationsToSubscribeFor()
                                .forEach(ddiCombinationToSubscribeFor -> ddis.addAll(IntStream
                                        .rangeClosed(ddiCombinationToSubscribeFor.getStart(), ddiCombinationToSubscribeFor.getEnd())
                                        .boxed().toList()));
                        log.trace("Adding the following DDIs as subscription.");
                        log.trace(ArrayUtils.toString(ddis));
                        subscription.setDdis(ddis);
                    }
                } else {
                    log.trace("Skip DDIs and position information for technical message types that do not require setting DDIs.");
                }
                subscriptions.add(subscription);
            } else {
                log.trace("Skip technical message type {} because it is not a receive or send-receive type.", supportedTechnicalMessageType.getTechnicalMessageType());
            }
        });
        return subscriptions;
    }

}
