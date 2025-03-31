package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.exception.InvestmentProjectNotFoundException;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectWritePlatformService;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class InvestmentProjectWritePlatformServiceImpl implements InvestmentProjectWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final InvestmentProjectRepository investmentProjectRepository;
    private final ClientRepository clientRepository;
    private final ApplicationCurrencyRepositoryWrapper currencyRepositoryWrapper;

    @Override
    public CommandProcessingResult createInvestmentProject(JsonCommand command) {

        this.validateForCreate(command.json());

        InvestmentProject investmentProject = new InvestmentProject();

        investmentProject.setName(command.stringValueOfParameterNamed(InvestmentProjectConstants.projectNameParamName));

        final String ownerIdParam = command.stringValueOfParameterNamed(InvestmentProjectConstants.projectOwnerIdParamName);

        Client owner = clientRepository.findById(Long.valueOf(ownerIdParam)).orElseThrow( () -> new ClientNotFoundException(Long.valueOf(ownerIdParam)));

        if (!owner.isActive()) {
            throw new ClientNotActiveException(owner.getId());
        }

        investmentProject.setOwner(owner);

        investmentProject.setAmount(command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountParamName));

        final String currencyCode = command.stringValueOfParameterNamed(InvestmentProjectConstants.currencyCodeParamName);

        investmentProject.setCurrencyCode(currencyRepositoryWrapper.findOneWithNotFoundDetection(currencyCode).getCode());

        investmentProject.setDescription(command.stringValueOfParameterNamed(InvestmentProjectConstants.descriptionParamName));

        investmentProject.setRate(command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.projectRateParamName));

        investmentProject = investmentProjectRepository.save(investmentProject);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(investmentProject.getId()).build();
    }

    @Override
    public CommandProcessingResult updateInvestmentProject(Long projectId, JsonCommand command) {

        this.validateForUpdate(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>(20);

        InvestmentProject investmentProject = investmentProjectRepository.findById(projectId).orElseThrow(() -> new InvestmentProjectNotFoundException(projectId, false));
        investmentProject.modifyApplication(command, changes);

        if (!changes.isEmpty()) {
            this.investmentProjectRepository.save(investmentProject);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(projectId) //
                .with(changes) //
                .build();

    }

    private void validateForCreate (final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                InvestmentProjectConstants.INVESTMENT_PROJECT_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("investmentProject");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String projectNameParam = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectNameParamName,
                jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectNameParamName).value(projectNameParam).notBlank()
                .notNull();

        final String ownerId = fromApiJsonHelper
                .extractStringNamed(InvestmentProjectConstants.projectOwnerIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectOwnerIdParamName).value(ownerId)
                .notBlank().notNull();

        final String amount = fromApiJsonHelper
                .extractStringNamed(InvestmentProjectConstants.amountParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.amountParamName).value(amount)
                .notBlank().notNull();

        final String currencyCode = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.currencyCodeParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.currencyCodeParamName).value(currencyCode).notBlank().notNull();

        final String description = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.descriptionParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.descriptionParamName).value(description).notBlank().notNull();

        final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    private void validateForUpdate (final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                InvestmentProjectConstants.INVESTMENT_PROJECT_PARAMETERS_FOR_UPDATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("investmentProject");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        if (fromApiJsonHelper.parameterExists(InvestmentProjectConstants.projectNameParamName, jsonElement)) {
            final String name = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectNameParamName, jsonElement);
            baseDataValidator.reset().parameter(InvestmentProjectConstants.projectNameParamName).value(name).notBlank().notNull();
        }
        if (fromApiJsonHelper.parameterExists(InvestmentProjectConstants.descriptionParamName, jsonElement)) {
            final String description = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.descriptionParamName, jsonElement);
            baseDataValidator.reset().parameter(InvestmentProjectConstants.descriptionParamName).value(description).notBlank().notNull();
        }
        if (fromApiJsonHelper.parameterExists(InvestmentProjectConstants.projectRateParamName, jsonElement)) {
            final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
            baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
