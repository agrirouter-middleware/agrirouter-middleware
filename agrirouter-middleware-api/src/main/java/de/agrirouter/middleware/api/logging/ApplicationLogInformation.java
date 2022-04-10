package de.agrirouter.middleware.api.logging;

/**
 * Parameter object for application business actions.
 */
public record ApplicationLogInformation(String internalApplicationId, String agrirouterApplicationId) {
}
