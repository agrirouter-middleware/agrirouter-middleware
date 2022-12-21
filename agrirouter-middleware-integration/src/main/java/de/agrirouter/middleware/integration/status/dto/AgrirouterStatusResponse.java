package de.agrirouter.middleware.integration.status.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The response for the status page.
 */
@Getter
@Setter
public class AgrirouterStatusResponse {

    /**
     * The components.
     */
    private List<Component> components;

}
