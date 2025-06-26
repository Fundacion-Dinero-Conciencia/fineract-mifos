package com.belat.fineract.portfolio.pdfgenerator.service.impl;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.pdfgenerator.api.PdfGeneratorConstants;
import com.belat.fineract.portfolio.pdfgenerator.service.PdfGeneratorWritePlatformService;
import com.belat.fineract.portfolio.projectparticipation.api.ProjectParticipationConstants;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfGeneratorWritePlatformServiceImpl implements PdfGeneratorWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ClientRepository clientRepository;
    private final InvestmentProjectRepository investmentProjectRepository;
    private final ClientIdentifierRepository clientIdentifierRepository;

    @Override
    public String generateFundMandateV1(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        JsonElement jsonElement = fromApiJsonHelper.parse(json);
        Long clientId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.clientIdParamName, jsonElement);
        if (clientId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.id.is.mandatory", "Client id is mandatory");
        }

        Client client = clientRepository.findById(clientId).stream().findFirst().orElseThrow(() ->
                new ClientNotFoundException(clientId));

        BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(PdfGeneratorConstants.amountParamName, jsonElement);
        if (amount == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.amount.is.mandatory", "Amount is mandatory");
        }
        LocalDate date = DateUtils.getBusinessLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String formattedDate = date.format(formatter);

        ClientIdentifier clientIdentifier = clientIdentifierRepository.retrieveByClientId(clientId);

        if (clientIdentifier == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.identifier.not.found", "Client identifier not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("date", formattedDate);
        data.put("inversionist", client.getDisplayName());
        data.put("documentNumber", clientIdentifier.documentKey());
        data.put("amount", amount);

        //Generate html
        String html = generateHtmlFromTemplate("templates/v1/fundmandatev1.html", data);

        return generatePdfBase64(html);
    }

    @Override
    public String generateRetailMandateV1(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        JsonElement jsonElement = fromApiJsonHelper.parse(json);
        Long clientId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.clientIdParamName, jsonElement);
        if (clientId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.id.is.mandatory", "Client id is mandatory");
        }
        Long projectId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.projectIdParamName, jsonElement);
        if (projectId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.project.id.is.mandatory", "Project id is mandatory");
        }

        Client client = clientRepository.findById(clientId).stream().findFirst().orElseThrow(() ->
                new ClientNotFoundException(clientId));

        InvestmentProject investmentProject = investmentProjectRepository.retrieveOneByProjectId(projectId);
        if (investmentProject == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.investment.project.not.found", "Investment project not found");
        }

        BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(PdfGeneratorConstants.amountParamName, jsonElement);
        if (amount == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.amount.is.mandatory", "Amount is mandatory");
        }
        LocalDate date = DateUtils.getBusinessLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String formattedDate = date.format(formatter);

        ClientIdentifier clientIdentifier = clientIdentifierRepository.retrieveByClientId(clientId);

        if (clientIdentifier == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.identifier.not.found", "Client identifier not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("date", formattedDate);
        data.put("inversionist", client.getDisplayName());
        data.put("documentNumber", clientIdentifier.documentKey());
        data.put("enterprise", investmentProject.getName());
        data.put("amount", amount);

        //Generate html
        String html = generateHtmlFromTemplate("templates/v1/retailmandatev1.html", data);

        return generatePdfBase64(html);
    }

    private String generateHtmlFromTemplate (String templateName, Map<String, Object> params) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassLoaderForTemplateLoading(PdfGeneratorWritePlatformServiceImpl.class.getClassLoader(), "/");
        configuration.setDefaultEncoding("UTF-8");

        Template template = null;
        try {
            template = configuration.getTemplate(templateName);
        } catch (Exception e) {
            throw new GeneralPlatformDomainRuleException("error.msg.could.not.found.template", "Could not found template");
        }

        try (StringWriter out = new StringWriter()) {
            template.process(params, out);
            return out.toString();
        } catch (IOException | TemplateException e) {
            throw new GeneralPlatformDomainRuleException("error.msg.could.not.generate.template", "Could not generate template");
        }
    }

    private static String generatePdfBase64(String htmlContent) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);
        } catch (IOException e) {
            throw new GeneralPlatformDomainRuleException("error.msg.could.not.generate.pdf.base64", "Could not generate pdf base64");
        }
    }

    private void validateForGenerate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                PdfGeneratorConstants.PROJECT_PARTICIPATION_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("pdfGenerator");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String projectIdParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.projectIdParamName, jsonElement);
        baseDataValidator.reset().parameter(PdfGeneratorConstants.projectIdParamName).value(projectIdParam).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
