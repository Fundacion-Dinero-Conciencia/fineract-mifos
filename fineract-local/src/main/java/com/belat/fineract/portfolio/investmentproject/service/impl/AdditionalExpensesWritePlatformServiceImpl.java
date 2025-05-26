package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.api.commissions.AdditionalExpensesConstants;
import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpensesRepository;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectEnum;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesReadPlatformService;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesWritePlatformService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
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
import java.util.*;

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
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final CodeRepository codeRepository;

    @Override
    @Transactional
    public CommandProcessingResult addAdditionalExpenses(JsonCommand jsonCommand) {

        Map<String, Object> changes = new HashMap<>();
        Type type = new TypeToken<List<AdditionalExpensesData>>() {}.getType();
        List<AdditionalExpensesData> data = new Gson().fromJson(jsonCommand.json(), type);
        if (data != null) {
            log.info("Data were obtained correctly");
        } else {
            throw new PlatformApiDataValidationException("err.msj.validation", "Error to convert additional expenses to list", null);
        }

        final Long projectId = data.get(0).getProjectId();
        final InvestmentProject investmentProject = investmentProjectReadPlatformService.retrieveInvestmentById(projectId);
        for (AdditionalExpensesData additionalExpensesData : data) {
            String json = fromApiJsonHelper.toJson(additionalExpensesData);
            this.validateForCreate(json);

            final String description = additionalExpensesData.getDescription();
            final BigDecimal vat = additionalExpensesData.getVat();
            final BigDecimal netAmount = additionalExpensesData.getNetAmount();
            final BigDecimal total = additionalExpensesData.getTotal();
            final Long commissionTypeId = Long.valueOf(additionalExpensesData.getCommissionTypeId());
            final CodeValue commissionType = codeValueRepositoryWrapper.findOneWithNotFoundDetection(commissionTypeId);

            AdditionalExpenses additionalExpenses = null;
            if (investmentProject != null) {
                additionalExpenses = AdditionalExpenses.createAdditionalExpenses(investmentProject, commissionType, description, netAmount, vat,  total);
                additionalExpensesRepository.save(additionalExpenses);

                changes.put(commissionType.getLabel(), additionalExpenses.getId());
            } else {
                throw new PlatformApiDataValidationException("err.msj.validation", "no project found with id", "id", projectId);
            }

        }

        updateStatusInvestmentProject(investmentProject);
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updateAdditionalExpenses(JsonCommand jsonCommand) {

        this.validateForUpdate(jsonCommand.json());

        final Long projectId = jsonCommand.longValueOfParameterNamed(AdditionalExpensesConstants.projectIdParamName);
        final Long expenseId = jsonCommand.longValueOfParameterNamed(AdditionalExpensesConstants.expenseIdParamName);
        final String description = jsonCommand.stringValueOfParameterNamed(AdditionalExpensesConstants.descriptionParamName);
        final BigDecimal vat = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.vatParamName);
        final BigDecimal netAmount = jsonCommand.bigDecimalValueOfParameterNamed(AdditionalExpensesConstants.netAmountParamName);


        AdditionalExpenses additionalExpenses = additionalExpensesReadPlatformService.getAdditionalExpensesById(expenseId);
        validateStatusInvestmentProject(projectId);

        additionalExpenses.setDescription(description);
        additionalExpenses.setVat(vat);
        additionalExpenses.setNetAmount(netAmount);

        additionalExpensesRepository.save(additionalExpenses);

        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(additionalExpenses.getId()) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult calculateAdditionalExpenses(JsonCommand jsonCommand) {


        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(null) //
                .build();
    }

    private void updateStatusInvestmentProject(InvestmentProject investmentProject) {
        final Code code = codeRepository.findOneByName("ProjectStatus");
        final CodeValue newStatus = code.getValues().stream().filter(item -> StatusHistoryProjectEnum.FUNDING.getCode().equals(item.getLabel())).findFirst().orElse(null);

        StatusHistoryProject historyProject = new StatusHistoryProject();
        historyProject.setInvestmentProject(investmentProject);
        historyProject.setStatusValue(newStatus);
        statusHistoryProjectRepository.save(historyProject);
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

        final String description = fromApiJsonHelper.extractStringNamed(AdditionalExpensesConstants.descriptionParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.descriptionParamName).value(description).notBlank().notNull();

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

        final String description = fromApiJsonHelper.extractStringNamed(AdditionalExpensesConstants.descriptionParamName, jsonElement);
        baseDataValidator.reset().parameter(AdditionalExpensesConstants.descriptionParamName).value(description).notBlank().notNull();

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
