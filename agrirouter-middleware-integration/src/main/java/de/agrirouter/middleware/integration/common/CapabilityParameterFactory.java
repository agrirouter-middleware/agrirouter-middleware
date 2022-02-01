package de.agrirouter.middleware.integration.common;

import com.dke.data.agrirouter.api.service.parameters.SetCapabilitiesParameters;
import de.agrirouter.middleware.domain.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory to create capability parameters.
 */
public final class CapabilityParameterFactory {

    private CapabilityParameterFactory() {
    }

    /**
     * Create the list of capability parameters for the given application.
     *
     * @param application The application.
     * @return List of capabilties to set.
     */
    public static List<SetCapabilitiesParameters.CapabilityParameters> create(Application application) {
        List<SetCapabilitiesParameters.CapabilityParameters> capabilitiesParameters = new ArrayList<>();
        application.getSupportedTechnicalMessageTypes().forEach(supportedTechnicalMessageType -> {
            SetCapabilitiesParameters.CapabilityParameters capabilitiesParameter = new SetCapabilitiesParameters.CapabilityParameters();
            capabilitiesParameter.setDirection(supportedTechnicalMessageType.getDirection());
            capabilitiesParameter.setTechnicalMessageType(supportedTechnicalMessageType.getTechnicalMessageType());
            capabilitiesParameters.add(capabilitiesParameter);
        });
        return capabilitiesParameters;
    }
}
