package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.dto.onboard.RouterDevice;
import com.dke.data.agrirouter.api.enums.CertificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Authentication details for a router device.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Authentication extends BaseEntity {

    /**
     * The type of the certificate.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificationType type;

    /**
     * The secret for the certificate.
     */
    @Column(nullable = false)
    private String secret;

    /**
     * The certificate.
     */
    @Lob
    @Column(nullable = false)
    private String certificate;

    public RouterDevice.Authentication asAgrirouterAuthentication() {
        var agrirouterAuthentication = new RouterDevice.Authentication();
        agrirouterAuthentication.setType(this.type.getKey());
        agrirouterAuthentication.setSecret(this.secret);
        agrirouterAuthentication.setCertificate(this.certificate);
        return agrirouterAuthentication;
    }
}
