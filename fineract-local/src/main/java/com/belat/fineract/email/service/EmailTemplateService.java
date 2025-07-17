package com.belat.fineract.email.service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public String buildEmail(String templateFileName, Map<String, String> templateParams) {
        Context context = new Context();
        templateParams.forEach(context::setVariable);
        return templateEngine.process(templateFileName, context);
    }
}
