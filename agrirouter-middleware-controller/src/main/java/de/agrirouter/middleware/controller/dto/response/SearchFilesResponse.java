package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Search file headers response.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response containing all file headers.")
public class SearchFilesResponse extends Response {

}
