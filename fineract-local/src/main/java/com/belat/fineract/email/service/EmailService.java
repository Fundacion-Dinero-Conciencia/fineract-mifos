package com.belat.fineract.email.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final SendGrid sendGrid;
    private final Email fromEmail;
    private final EmailTemplateService emailTemplateService;

    public void sendSimpleEmail(String toEmail, String subject, String body) throws IOException {
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);

        Mail mail = new Mail(fromEmail, subject, to, content);

        sendMailRequest(mail);
    }

    public void sendEmailWithTemplate(String toEmail, String subject, String templateId, Map<String,String> templateParams) throws IOException {
        Personalization personalization = new Personalization();
        templateParams.forEach(personalization::addDynamicTemplateData);
        Email to = new Email(toEmail);
        personalization.addTo(to);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);
        mail.setTemplateId(templateId);
        mail.addPersonalization(personalization);

        sendMailRequest(mail);
    }

    public void sendEmailWithTemplateThymeleaf(String toEmail, String subject, String templateFileName, Map<String,String> templateParams) throws IOException {
        Email to = new Email(toEmail);
        Content content = new Content("text/html", emailTemplateService.buildEmail(templateFileName, templateParams));

        Mail mail = new Mail(fromEmail, subject, to, content);

        sendMailRequest(mail);
    }

    private void sendMailRequest(Mail mail) throws IOException {
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sendGrid.api(request);
    }
}

