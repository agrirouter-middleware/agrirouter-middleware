package de.agrirouter.middleware.controller.secured;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApplicationControllerTest {

    @Test
    void givenValidPublicKeyWhenDecodingBase64StringTheDecoderShouldNotThrowException() {
        var publicKey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF1dXYvdnZpV0IzRXN4QkQ0VlI1Kwo5dks4N3VkQVpZdHpreGZtaEk4bDVxYjZ0NmdwY2wwUE1Qa3VTMGUvbFB3aWw5ekVkQkUvdDg1ZE5QaWJwTzNRCkwrQnUxL0RlYWVmdnZJNzJlNXlONlFxcXhwQ3NEUGloSUJUWEgxbXloWG5FSStzQUM5Tjk5d0xwRmYvdytxQ0EKbS9vTnUzc2JNbWFyUDhsVE5mQ2ZhMzNkOXFZSXRWWVZhTTdDQzBNS3JHaTdUR2FQVWFvL2pqalpoUWdOVHU5MgpVcW1BMmJOS3V1ekYzTWM2bURHbjU3eFMyYXY1Ym4xVVNTUVk1WUlpaVQ1V3loRHRLYXVBeXpDR2EvRTN0S2NWCnZPUldBd1BIUGptL1Q0bTE4RmFVSmJNcGRzRzF1VWtSekprUXovaXowRHVkSjErUVNhZTY2RGdhNDVQN2ZHMEUKM3dJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t=";
        byte[] decodedBytes = Base64.decodeBase64(publicKey);
        var publicKeyAsString = new String(decodedBytes);

        String expectedPublicKey = """
                -----BEGIN PUBLIC KEY-----
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuuv/vviWB3EsxBD4VR5+
                9vK87udAZYtzkxfmhI8l5qb6t6gpcl0PMPkuS0e/lPwil9zEdBE/t85dNPibpO3Q
                L+Bu1/DeaefvvI72e5yN6QqqxpCsDPihIBTXH1myhXnEI+sAC9N99wLpFf/w+qCA
                m/oNu3sbMmarP8lTNfCfa33d9qYItVYVaM7CC0MKrGi7TGaPUao/jjjZhQgNTu92
                UqmA2bNKuuzF3Mc6mDGn57xS2av5bn1USSQY5YIiiT5WyhDtKauAyzCGa/E3tKcV
                vORWAwPHPjm/T4m18FaUJbMpdsG1uUkRzJkQz/iz0DudJ1+QSae66Dga45P7fG0E
                3wIDAQAB
                -----END PUBLIC KEY-----""";

        Assertions.assertEquals(expectedPublicKey, publicKeyAsString);
    }


}
