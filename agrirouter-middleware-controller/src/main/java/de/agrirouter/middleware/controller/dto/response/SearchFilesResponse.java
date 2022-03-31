package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.FileHeaderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Search file headers response.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response containing all search results for the file header query.")
public class SearchFilesResponse extends Response {

    @Schema(description = "The number of files found.")
    int nrOfFilesFound;

    /**
     * The file headers that can be used to fetch the messages afterwards.
     */
    @Schema(description = "The file headers (can be used for further filtering.")
    List<FileHeaderDto> fileHeaders;

}
