package de.agrirouter.middleware;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main entry of the application.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Agrirouter Middleware",
                version = "${app.version:unknown}",
                description = """
                        The agrirouter© middleware was developed to have easier access to the functionality of the agrirouter.
                        The agrirouter© middleware is an additional layer of abstraction and provides access to the agrirouter© without deep knowledge of the underlying processes.
                        The agrirouter© middleware manages the connections and fetches messages from the agrirouter, based on the technical messages types registered.
                        In addition, the agrirouter© middleware provides simple data conversion from ISOXML TaskData to EFDI Telemetry Data and provides searching operations for DDIs and other parts of the specification."""
        )
)
@SpringBootApplication
@EnableJpaRepositories(basePackages = "de.agrirouter.middleware.persistence.jpa")
@EnableMongoRepositories(basePackages = "de.agrirouter.middleware.persistence.mongo")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
