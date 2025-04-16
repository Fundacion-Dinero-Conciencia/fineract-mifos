package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategoryRepository;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescription;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescriptionRepository;
import com.belat.fineract.portfolio.investmentproject.exception.InvestmentProjectNotFoundException;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectWritePlatformService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class InvestmentProjectWritePlatformServiceImpl implements InvestmentProjectWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final InvestmentProjectRepository investmentProjectRepository;
    private final ClientRepository clientRepository;
    private final ApplicationCurrencyRepositoryWrapper currencyRepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final InvestmentProjectDescriptionRepository investmentProjectDescriptionRepository;
    private final InvestmentProjectCategoryRepository investmentProjectCategoryRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Override
    public CommandProcessingResult createInvestmentProject(JsonCommand command) {

        this.validateForCreate(command.json());

        InvestmentProject investmentProject = new InvestmentProject();

        investmentProject.setName(command.stringValueOfParameterNamed(InvestmentProjectConstants.projectNameParamName));
        investmentProject.setSubtitle(command.stringValueOfParameterNamed(InvestmentProjectConstants.subtitleParamName));

        final String ownerIdParam = command.stringValueOfParameterNamed(InvestmentProjectConstants.projectOwnerIdParamName);
        Client owner = clientRepository.findById(Long.valueOf(ownerIdParam))
                .orElseThrow(() -> new ClientNotFoundException(Long.valueOf(ownerIdParam)));
        if (!owner.isActive()) {
            throw new ClientNotActiveException(owner.getId());
        }
        investmentProject.setOwner(owner);

        investmentProject.setAmount(command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountParamName));

        final String currencyCode = command.stringValueOfParameterNamed(InvestmentProjectConstants.currencyCodeParamName);
        investmentProject.setCurrencyCode(currencyRepositoryWrapper.findOneWithNotFoundDetection(currencyCode).getCode());

        investmentProject.setRate(command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.projectRateParamName));
        investmentProject.setPeriod(command.integerValueSansLocaleOfParameterNamed(InvestmentProjectConstants.periodParamName));

        final Long countryId = command.longValueOfParameterNamed(InvestmentProjectConstants.countryIdParamName);
        CodeValue countryValue = codeValueRepositoryWrapper.findOneWithNotFoundDetection(countryId);
        investmentProject.setCountry(countryValue);

        final String impactDescription = command.stringValueOfParameterNamed(InvestmentProjectConstants.impactDescriptionParamName);
        final String institutionDescription = command
                .stringValueOfParameterNamed(InvestmentProjectConstants.institutionDescriptionParamName);
        final String teamDescription = command.stringValueOfParameterNamed(InvestmentProjectConstants.teamDescriptionParamName);
        final String financingDescription = command.stringValueOfParameterNamed(InvestmentProjectConstants.financingDescriptionParamName);
        InvestmentProjectDescription description = new InvestmentProjectDescription(impactDescription, institutionDescription,
                teamDescription, financingDescription);
        description = investmentProjectDescriptionRepository.save(description);
        investmentProject.setDescription(description);

        investmentProject.setActive(command.booleanPrimitiveValueOfParameterNamed(InvestmentProjectConstants.isActiveParamName));

        investmentProject.setCurrencyCode(currencyRepositoryWrapper.findOneWithNotFoundDetection(currencyCode).getCode());

        final Long categoryId = command.longValueOfParameterNamed(InvestmentProjectConstants.categoryParamName);
        investmentProject.setCategory(codeValueRepositoryWrapper.findOneWithNotFoundDetection(categoryId));

        final Long areaId = command.longValueOfParameterNamed(InvestmentProjectConstants.areaParamName);
        investmentProject.setArea(codeValueRepositoryWrapper.findOneWithNotFoundDetection(areaId));

        investmentProject = investmentProjectRepository.saveAndFlush(investmentProject);

        final String subcategories = command.stringValueOfParameterNamed(InvestmentProjectConstants.subCategoriesParamName);
        List<CodeValue> codeSubCategories = getInvestmentProjectCategoryData(subcategories);
        InvestmentProject finalInvestmentProject = investmentProject;
        codeSubCategories
                .forEach(item -> investmentProjectCategoryRepository.save(new InvestmentProjectCategory(item, finalInvestmentProject)));

        final Long loanId = command.longValueOfParameterNamed(InvestmentProjectConstants.loanIdParamName);
        investmentProject.setLoan(loanRepositoryWrapper.findOneWithNotFoundDetection(loanId));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(investmentProject.getId()).build();
    }

    @Override
    public CommandProcessingResult updateInvestmentProject(Long projectId, JsonCommand command) {

        this.validateForUpdate(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>(20);

        InvestmentProject investmentProject = investmentProjectRepository.findById(projectId)
                .orElseThrow(() -> new InvestmentProjectNotFoundException(projectId, false));
        investmentProject.modifyApplication(command, changes);

        List<InvestmentProjectCategory> categoriesList = investmentProjectCategoryRepository.retrieveByProjectId(investmentProject.getId());
        categoriesList.forEach(investmentProjectCategoryRepository::delete);

        final Long categoryId = command.longValueOfParameterNamed(InvestmentProjectConstants.categoryParamName);
        investmentProject.setCategory(codeValueRepositoryWrapper.findOneWithNotFoundDetection(categoryId));
        changes.put(InvestmentProjectConstants.categoryParamName, categoryId);

        final Long areaId = command.longValueOfParameterNamed(InvestmentProjectConstants.areaParamName);
        investmentProject.setArea(codeValueRepositoryWrapper.findOneWithNotFoundDetection(areaId));
        changes.put(InvestmentProjectConstants.areaParamName, areaId);

        final String subCategoriesString = command.stringValueOfParameterNamed(InvestmentProjectConstants.subCategoriesParamName);
        List<CodeValue> codeCategories = getInvestmentProjectCategoryData(subCategoriesString);
        codeCategories.forEach(item -> investmentProjectCategoryRepository.save(new InvestmentProjectCategory(item, investmentProject)));

        if (!changes.isEmpty()) {
            this.investmentProjectRepository.save(investmentProject);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(projectId) //
                .with(changes) //
                .build();

    }

    private void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, InvestmentProjectConstants.INVESTMENT_PROJECT_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("investmentProject");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String projectNameParam = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectNameParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectNameParamName).value(projectNameParam).notBlank().notNull();

        final String subtitleParam = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subtitleParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.subtitleParamName).value(subtitleParam).notBlank().notNull();

        final String ownerId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectOwnerIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectOwnerIdParamName).value(ownerId).notBlank().notNull();

        final String amount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.amountParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.amountParamName).value(amount).notBlank().notNull();

        final String currencyCode = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.currencyCodeParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.currencyCodeParamName).value(currencyCode).notBlank().notNull();

        final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();

        final String period = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.periodParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.periodParamName).value(period).notBlank().notNull();

        final String countryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.countryIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.countryIdParamName).value(countryId).notBlank().notNull();

        final String impactDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.impactDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.impactDescriptionParamName).value(impactDescription).notBlank()
                .notNull();

        final String institutionDescription = fromApiJsonHelper
                .extractStringNamed(InvestmentProjectConstants.institutionDescriptionParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.institutionDescriptionParamName).value(institutionDescription)
                .notBlank().notNull();

        final String teamDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.teamDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.teamDescriptionParamName).value(teamDescription).notBlank()
                .notNull();

        final String financingDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.financingDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.financingDescriptionParamName).value(financingDescription).notBlank()
                .notNull();

        final String isActive = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.isActiveParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.isActiveParamName).value(isActive).notBlank().notNull()
                .validateForBooleanValue();

        final String categoryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.categoryParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.categoryParamName).value(categoryId).notBlank().notNull();

        final String subCategories = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subCategoriesParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.subCategoriesParamName).value(subCategories).notBlank().notNull();

        final String areaId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.areaParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.areaParamName).value(areaId).notBlank().notNull();

        final String loanId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.loanIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.loanIdParamName).value(loanId).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    private void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                InvestmentProjectConstants.INVESTMENT_PROJECT_PARAMETERS_FOR_UPDATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("investmentProject");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectNameParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectNameParamName).value(name).notBlank().notNull();

        final String subtitle = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subtitleParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.subtitleParamName).value(subtitle).notBlank().notNull();

        final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();

        final String impactDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.impactDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.impactDescriptionParamName).value(impactDescription).notBlank()
                .notNull();

        final String institutionDescription = fromApiJsonHelper
                .extractStringNamed(InvestmentProjectConstants.institutionDescriptionParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.institutionDescriptionParamName).value(institutionDescription)
                .notBlank().notNull();

        final String teamDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.teamDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.teamDescriptionParamName).value(teamDescription).notBlank()
                .notNull();

        final String financingDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.financingDescriptionParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.financingDescriptionParamName).value(financingDescription).notBlank()
                .notNull();

        final Boolean isActive = fromApiJsonHelper.extractBooleanNamed(InvestmentProjectConstants.isActiveParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.isActiveParamName).value(isActive).notBlank().notNull();

        final String categoryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.categoryParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.categoryParamName).value(categoryId).notBlank().notNull();

        final String areaId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.areaParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.areaParamName).value(areaId).notBlank().notNull();

        final String subCategories = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subCategoriesParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.subCategoriesParamName).value(subCategories).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private List<CodeValue> getInvestmentProjectCategoryData(String categories) {
        List<CodeValue> codeCategories = new ArrayList<>();
        List<Long> categoriesId = getIdsFromJsonString(categories);
        if (!categoriesId.isEmpty()) {
            categoriesId.forEach(item -> codeCategories.add(codeValueRepositoryWrapper.findOneWithNotFoundDetection(item)));
        }
        return codeCategories;
    }

    private List<Long> getIdsFromJsonString(String list) {
        List<Long> categoriesId = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            categoriesId = objectMapper.readValue(list, new TypeReference<List<Long>>() {});
        } catch (IOException e) {
            throw new GeneralPlatformDomainRuleException("err.msg.obtain.long.list.from.string", "Error when get long list from string");
        }
        return categoriesId;
    }
}
