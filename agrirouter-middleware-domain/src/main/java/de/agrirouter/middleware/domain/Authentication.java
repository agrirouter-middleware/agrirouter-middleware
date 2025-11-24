package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.dto.onboard.RouterDevice;
import com.dke.data.agrirouter.api.enums.CertificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Authentication details for a router device.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class Authentication extends BaseEntity {

    /**
     * The type of the certificate.
     */
    private CertificationType type;

    /**
     * The secret for the certificate.
     */
    private String secret;

    /**
     * The certificate.
     */
    private String certificate;

    public RouterDevice.Authentication asAgrirouterAuthentication() {
        var agrirouterAuthentication = new RouterDevice.Authentication();
        agrirouterAuthentication.setType(this.type.getKey());
        agrirouterAuthentication.setSecret(this.secret);
        agrirouterAuthentication.setCertificate(this.certificate);
        return agrirouterAuthentication;
    }
}
