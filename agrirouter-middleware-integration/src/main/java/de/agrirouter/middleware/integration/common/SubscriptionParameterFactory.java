package de.agrirouter.middleware.integration.common;

import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import de.agrirouter.middleware.domain.Application;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Factory to create subscription parameters.
 */
public final class SubscriptionParameterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionParameterFactory.class);

    private SubscriptionParameterFactory() {
    }

    /**
     * Create the list of subscription parameters for the given application.
     *
     * @param application The application.
     * @return List of subscriptions to set.
     */
    public static List<SetSubscriptionParameters.Subscription> create(Application application) {
        List<SetSubscriptionParameters.Subscription> subscriptions = new ArrayList<>();
        application.getSupportedTechnicalMessageTypes().forEach(supportedTechnicalMessageType -> {
            SetSubscriptionParameters.Subscription subscription = new SetSubscriptionParameters.Subscription();
            subscription.setTechnicalMessageType(supportedTechnicalMessageType.getTechnicalMessageType());
            if (null == application.getApplicationSettings() || application.getApplicationSettings().getDdiCombinationsToSubscribeFor().isEmpty()) {
                LOGGER.debug("The application did not define any DDIs, therefore the whole range from DDI 0 to 600 is subscribed.");
                subscription.setDdis(IntStream.rangeClosed(0, 600).boxed().collect(Collectors.toList()));
            } else {
                LOGGER.debug("The application did define (multiple) ranges of DDIs.");
                List<Integer> ddis = new ArrayList<>();
                application.getApplicationSettings().getDdiCombinationsToSubscribeFor()
                        .forEach(ddiCombinationToSubscribeFor -> ddis.addAll(IntStream
                                .rangeClosed(ddiCombinationToSubscribeFor.getStart(), ddiCombinationToSubscribeFor.getEnd())
                                .boxed()
                                .collect(Collectors.toList())));
                LOGGER.trace("Adding the following DDIs as subscription.");
                LOGGER.trace(ArrayUtils.toString(ddis));
                subscription.setDdis(ddis);
            }
            subscriptions.add(subscription);
        });
        return subscriptions;
    }

}
