package de.agrirouter.middleware.api.errorhandling.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ErrorKeyTest {

    @Test
    void givenMultipleValuesForErrorKeyWhenCheckingTheErrorCodesThenNoErrorCodeShouldBeDuplicated() {
        List<String> errorCodes = new ArrayList<>();
        Arrays.stream(ErrorKey.values()).forEach(errorKey -> {
            Assertions.assertFalse(errorCodes.contains(errorKey.getKey()), "There was a duplicate. Please check error code " + errorKey.getKey());
            errorCodes.add(errorKey.getKey());
        });
    }

}
