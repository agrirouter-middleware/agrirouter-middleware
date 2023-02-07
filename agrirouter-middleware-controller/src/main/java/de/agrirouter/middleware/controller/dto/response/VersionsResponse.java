package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response schema for the version.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Response schema for the version information.")
public class VersionsResponse extends Response {

    private final String version;

}
