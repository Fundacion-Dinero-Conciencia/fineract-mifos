package com.belat.fineract.email.service;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailProperties.class)
class SendGridConfiguration {
    private final EmailProperties emailProperties;

    @Bean
    public SendGrid sendGrid() {
        String apiKey = emailProperties.getApiKey();
        return new SendGrid(apiKey);
    }

    @Bean
    public Email fromEmail() {
        String fromEmail = emailProperties.getFrom();
        String fromName = emailProperties.getFromName();
        return fromName != null ? new Email(fromEmail, fromName) : new Email(fromEmail);
    }
}
