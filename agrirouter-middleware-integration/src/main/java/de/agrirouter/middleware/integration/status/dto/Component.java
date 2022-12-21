package de.agrirouter.middleware.integration.status.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * A component in the status page.
 * <p>
 * Example for the fields within the component:
 * <p>
 * "created_at": "2014-05-03T01:22:07.274Z",
 * "description": null,
 * "group": false,
 * "group_id": null,
 * "id": "b13yz5g2cw10",
 * "name": "API",
 * "only_show_if_degraded": false,
 * "page_id": "5ksblyvxp84q",
 * "position": 1,
 * "showcase": true,
 * "start_date": null,
 * "status": "partial_outage",
 * "updated_at": "2014-05-14T20:34:43.340Z"
 */
public class Component {

    @Setter
    @Getter
    private String name;

    @Setter
    private String status;

    public ComponentStatus getComponentStatus() {
        return ComponentStatus.parse(status);
    }

}
