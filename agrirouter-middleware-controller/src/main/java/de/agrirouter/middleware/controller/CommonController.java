package de.agrirouter.middleware.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

import javax.ws.rs.ApplicationPath;

/**
 * Common controller interface to hold the complete configuration.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Agrirouter© Middleware",
                description = "Middleware to access the agrirouter© using a 'smaller' API. The middleware will handle the connection for all endpoints and fetches / stores messages from the agrirouter© within a custom database.",
                version = "1.0.0",
                license = @License(
                        name = "Apache License Version 2.0",
                        url = "https://github.com/agrirouter-middleware/LICENSE"
                ),
                contact = @Contact(
                        name = "Agrirouter© Middleware",
                        url = "https://github.com/agrirouter-middleware"
                )
        )
)
@ApplicationPath("/")
public interface CommonController {
}
