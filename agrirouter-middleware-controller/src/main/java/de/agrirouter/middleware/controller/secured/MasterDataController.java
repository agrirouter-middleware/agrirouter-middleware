package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.business.CustomerService;
import de.agrirouter.middleware.business.FarmService;
import de.agrirouter.middleware.business.FieldService;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.response.CustomersResponse;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.FarmsResponse;
import de.agrirouter.middleware.controller.dto.response.FieldsResponse;
import de.agrirouter.middleware.controller.dto.response.domain.CustomerDto;
import de.agrirouter.middleware.controller.dto.response.domain.FarmDto;
import de.agrirouter.middleware.controller.dto.response.domain.FieldDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for master data management, i.e. farms, customers, devices, etc.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SecuredApiController.API_PREFIX + "/master-data")
@Tag(
        name = "master data | customer, farm and field management",
        description = "Operations for master data management, i.e. farm, customer, device, etc."
)
public class MasterDataController implements SecuredApiController {

    private final CustomerService customerService;
    private final FarmService farmService;
    private final FieldService fieldService;

    @GetMapping("/customer/{externalEndpointId}")
    @Operation(
            operationId = "master-data.customers",
            summary = "Retrieve customers for a given external endpoint ID",
            description = "Retrieve all customers for a given external endpoint ID.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Customers retrieved successfully, even if the list is empty.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = CustomersResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<?> getCustomers(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        var customers = customerService.findByExternalEndpointId(externalEndpointId);
        var dtos = customers.stream().map(customer -> {
            var dto = new CustomerDto();
            dto.setCustomerAsJson(customer.getDocument().toJson());
            return dto;
        }).toList();
        var customersResponse = new CustomersResponse(externalEndpointId, dtos);
        return ResponseEntity.ok(customersResponse);
    }

    @GetMapping("/customer/{externalEndpointId}/{entityId}")
    @Operation(
            summary = "Get customer by external endpoint ID and entity ID",
            description = "Retrieves a customer by its external endpoint ID and entity ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer found", content = @Content(schema = @Schema(implementation = CustomerDto.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public ResponseEntity<?> getCustomer(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The entity ID.", required = true) @PathVariable String entityId) {
        var optionalCustomer = customerService.getCustomer(externalEndpointId, entityId);
        if (optionalCustomer.isPresent()) {
            var customer = optionalCustomer.get();
            var dto = new CustomerDto();
            dto.setCustomerAsJson(customer.getDocument().toJson());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/customer/{externalEndpointId}")
    @Operation(
            operationId = "master-data.customer",
            summary = "Send customer data to the specified external endpoint",
            description = "This operation sends customer data to the specified external endpoint.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customer data sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid customer data provided, please be aware that the customer data must be a valid JSON object and match the ISO 11783 standard.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    public ResponseEntity<?> publishCustomer(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The customer data as JSON string, please be aware that the customer data must be a valid JSON object and match the ISO 11783 standard.") @RequestBody String customerAsJson) {
        customerService.publishCustomer(externalEndpointId, customerAsJson);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/farm/{externalEndpointId}")
    @Operation(
            operationId = "master-data.farms",
            summary = "Retrieve farms for a given external endpoint ID",
            description = "This operation retrieves all farms associated with the specified external endpoint ID.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Farms retrieved successfully, even if the list is empty.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = FarmsResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<?> getFarms(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        var farms = farmService.findByExternalEndpointId(externalEndpointId);
        var dtos = farms.stream().map(farm -> {
            var dto = new FarmDto();
            dto.setFarmAsJson(farm.getDocument().toJson());
            return dto;
        }).toList();
        var farmsResponse = new FarmsResponse(externalEndpointId, dtos);
        return ResponseEntity.ok(farmsResponse);
    }

    @PostMapping("/farm/{externalEndpointId}")
    @Operation(
            operationId = "master-data.farm",
            summary = "Send farm data to the specified external endpoint",
            description = "This operation sends farm data to the specified external endpoint.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Farm data sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid farm data provided, please be aware that the farm data must be a valid JSON object and match the ISO 11783 standard.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    public ResponseEntity<?> publishFarm(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The farm data as JSON string, please be aware that the farm data must be a valid JSON object and match the ISO 11783 standard.") @RequestBody String farmAsJson) {
        farmService.publishFarm(externalEndpointId, farmAsJson);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/field/{externalEndpointId}")
    @Operation(
            operationId = "master-data.fields",
            summary = "Retrieve fields for a given external endpoint ID",
            description = "This operation retrieves all fields associated with the specified external endpoint ID.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Fields retrieved successfully, even if the list is empty.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = FieldsResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<?> getFields(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        var fields = fieldService.findByExternalEndpointId(externalEndpointId);
        var dtos = fields.stream().map(field -> {
            var dto = new FieldDto();
            dto.setFieldAsJson(field.getDocument().toJson());
            return dto;
        }).toList();
        var fieldsResponse = new FieldsResponse(externalEndpointId, dtos);
        return ResponseEntity.ok(fieldsResponse);
    }

    @PostMapping("/field/{externalEndpointId}")
    @Operation(
            operationId = "master-data.field",
            summary = "Send field data to the specified external endpoint",
            description = "This operation sends field data to the specified external endpoint.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Field data sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid field data provided, please be aware that the field data must be a valid JSON object and match the ISO 11783 standard.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    public ResponseEntity<?> publishField(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The field data as JSON string, please be aware that the field data must be a valid JSON object and match the ISO 11783 standard.") @RequestBody String fieldAsJson) {
        fieldService.publishField(externalEndpointId, fieldAsJson);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
