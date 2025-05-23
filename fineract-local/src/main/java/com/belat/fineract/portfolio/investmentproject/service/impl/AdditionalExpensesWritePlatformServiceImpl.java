package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.api.commissions.AdditionalExpensesConstants;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpensesRepository;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectEnum;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesReadPlatformService;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesWritePlatformService;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdditionalExpensesWritePlatformServiceImpl implements AdditionalExpensesWritePlatformService {


    private final FromJsonHelper fromApiJsonHelper;
    private final AdditionalExpensesRepository additionalExpensesRepository;
    private final StatusHistoryProjectRepository statusHistoryProjectRepository;
    private final AdditionalExpensesReadPlatformService additionalExpensesReadPlatformService;
    private final InvestmentProjectReadPlatformServiceImpl investmentProjectReadPlatformService;

    @Override
    @Transactional
    public CommandProcessingResult addAdditionalExpenses(JsonCommand jsonCommand) {
        this.validateForCreate(jsonCommand.json());
        final Long projectId = jsonCommand.longValueOfParameterNamed(AdditionalExpensesConstants.projectIdParamName);
        final InvestmentProject investmentProject = investmentProjectReadPlatformService.retrieveInvestmentById(projectId);
        final String name = jsonCommand.stringValueOfParameterNamed(AdditionalExpensesConstants.nameParamName);
        final BigDecimal vat = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.vatParamName);
        final BigDecimal netAmount = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.netAmountParamName);

        AdditionalExpenses additionalExpenses = null;
        if (investmentProject != null) {
            additionalExpenses = AdditionalExpenses.createAdditionalExpenses(investmentProject, name, vat, netAmount);
            additionalExpensesRepository.save(additionalExpenses);
        } else {
            throw new PlatformApiDataValidationException("err.msj.validation", "no project found with id", "id", projectId);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(additionalExpenses.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateAdditionalExpenses(JsonCommand jsonCommand) {

        this.validateForUpdate(jsonCommand.json());

        final Long projectId = jsonCommand.longValueOfParameterNamed(AdditionalExpensesConstants.projectIdParamName);
        final Long expenseId = jsonCommand.longValueOfParameterNamed(AdditionalExpensesConstants.expenseIdParamName);
        final String name = jsonCommand.stringValueOfParameterNamed(AdditionalExpensesConstants.nameParamName);
        final BigDecimal vat = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.vatParamName);
        final BigDecimal netAmount = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.netAmountParamName);


        AdditionalExpenses additionalExpenses = additionalExpensesReadPlatformService.getAdditionalExpensesById(expenseId);
        validateStatusInvestmentProject(projectId);

        additionalExpenses.setName(name);
        additionalExpenses.setVat(vat);
        additionalExpenses.setNetAmount(netAmount);

        additionalExpensesRepository.save(additionalExpenses);

        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(additionalExpenses.getId()) //
                .build();
    }


    private void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, AdditionalExpensesConstants.ADDITIONAL_EXPENSES_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(AdditionalExpensesConstants.projectIdParamName);
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonElement.getAsJsonObject());

        final String name = fromApiJsonHelper.extractStringNamed(AdditionalExpensesConstants.nameParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.nameParamName).value(name).notBlank().notNull();

        final BigDecimal vat = fromApiJsonHelper.extractBigDecimalNamed(AdditionalExpensesConstants.vatParamName, jsonElement, locale);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.vatParamName).value(vat).notNull().positiveAmount();

        final BigDecimal netAmount = fromApiJsonHelper.extractBigDecimalNamed(AdditionalExpensesConstants.netAmountParamName, jsonElement, locale);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.vatParamName).value(netAmount).notNull().positiveAmount();

        final Long investmentProjectId = fromApiJsonHelper.extractLongNamed(AdditionalExpensesConstants.projectIdParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.projectIdParamName).value(investmentProjectId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    private void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, AdditionalExpensesConstants.ADDITIONAL_EXPENSES_PARAMETERS_FOR_UPDATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(AdditionalExpensesConstants.projectIdParamName);
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonElement.getAsJsonObject());

        final String name = fromApiJsonHelper.extractStringNamed(AdditionalExpensesConstants.nameParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.nameParamName).value(name).notBlank().notNull();

        final BigDecimal vat = fromApiJsonHelper.extractBigDecimalNamed(AdditionalExpensesConstants.vatParamName, jsonElement, locale);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.vatParamName).value(vat).notNull().positiveAmount();

        final BigDecimal netAmount = fromApiJsonHelper.extractBigDecimalNamed(AdditionalExpensesConstants.netAmountParamName, jsonElement, locale);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.vatParamName).value(netAmount).notNull().positiveAmount();

        final Long expenseId = fromApiJsonHelper.extractLongNamed(AdditionalExpensesConstants.expenseIdParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.expenseIdParamName).value(expenseId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    private void validateStatusInvestmentProject(Long id) {
        StatusHistoryProject historyProject = statusHistoryProjectRepository.getLastStatusByInvestmentProjectId(id);
        if (historyProject == null) {
            throw new PlatformApiDataValidationException("err.msj.validation", "no status history project found with id", "id", id);
        } else if (!StatusHistoryProjectEnum.DRAFT.getValue().toString().trim().equals(historyProject.getStatusValue().getLabel().trim())) {
            throw new PlatformApiDataValidationException("err.msj.validation", "Expenses are editable only if the status is in draft form.", null);
        }
    }
}
