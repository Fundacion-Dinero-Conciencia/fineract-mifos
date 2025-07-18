package com.belat.fineract.email.service;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sendgrid")
@Data
public class EmailProperties {

    private boolean enabled;

    @NotBlank
    @Pattern(regexp = "^SG[0-9a-zA-Z._]{67}$")
    private String apiKey;

    @Email
    @NotBlank
    private String from;

    private String fromName;

    @NotBlank
    private String to;

    public boolean isValid() {
        return !enabled || (
                apiKey != null && apiKey.matches("^SG[0-9a-zA-Z._]{67}$") &&
                        from != null && !from.isBlank() && isEmailValid(from) &&
                        to != null && !to.isBlank()
        );
    }

    private boolean isEmailValid(String email) {
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }

}
