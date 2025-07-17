package com.belat.fineract.email.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sendgrid")
@Data
public class EmailProperties {
    @NotBlank
    @Pattern(regexp = "^SG[0-9a-zA-Z._]{67}$")
    private String apiKey;

    @Email
    @NotBlank
    private String from;

    private String fromName;

    @NotBlank
    private String to;
}
