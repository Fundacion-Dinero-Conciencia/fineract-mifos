package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategoryRepository;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescription;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescriptionRepository;
import com.belat.fineract.portfolio.investmentproject.domain.objective.InvestmentProjectObjective;
import com.belat.fineract.portfolio.investmentproject.domain.objective.InvestmentProjectObjectiveRepository;
import com.belat.fineract.portfolio.investmentproject.domain.socioenvironmentaldescription.SocioEnvironmentalDescription;
import com.belat.fineract.portfolio.investmentproject.domain.socioenvironmentaldescription.SocioEnvironmentalDescriptionRepository;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import com.belat.fineract.portfolio.investmentproject.exception.InvestmentProjectNotFoundException;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectWritePlatformService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.IdempotencyKeyGenerator;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanApplicationWritePlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants.SHORT_NAME_FACTORING;

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
    private final InvestmentProjectObjectiveRepository investmentProjectObjectiveRepository;
    private final StatusHistoryProjectRepository statusHistoryProjectRepository;
    private final SocioEnvironmentalDescriptionRepository socioEnvironmentalDescriptionRepository;
    private final LoanApplicationWritePlatformService loanApplicationWritePlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final CommandSourceService commandSourceService;
    private final PlatformSecurityContext context;

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

        final String littleSocioEnvironmentalDescription = command.stringValueOfParameterNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName);
        final String detailedSocioEnvironmentalDescription = command.stringValueOfParameterNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName);

        SocioEnvironmentalDescription socioEnvironmentalDescription = new SocioEnvironmentalDescription(littleSocioEnvironmentalDescription, detailedSocioEnvironmentalDescription);

        socioEnvironmentalDescription = socioEnvironmentalDescriptionRepository.save(socioEnvironmentalDescription);

        InvestmentProjectDescription description = new InvestmentProjectDescription(impactDescription, institutionDescription,
                teamDescription, financingDescription, socioEnvironmentalDescription);
        description = investmentProjectDescriptionRepository.saveAndFlush(description);
        investmentProject.setDescription(description);

        investmentProject.setActive(command.booleanPrimitiveValueOfParameterNamed(InvestmentProjectConstants.isActiveParamName));

        investmentProject.setCurrencyCode(currencyRepositoryWrapper.findOneWithNotFoundDetection(currencyCode).getCode());

        final Long categoryId = command.longValueOfParameterNamed(InvestmentProjectConstants.categoryParamName);
        if (categoryId != null) {
            investmentProject.setCategory(codeValueRepositoryWrapper.findOneWithNotFoundDetection(categoryId));
        }


        final Long areaId = command.longValueOfParameterNamed(InvestmentProjectConstants.areaParamName);
        if (areaId != null) {
            investmentProject.setArea(codeValueRepositoryWrapper.findOneWithNotFoundDetection(areaId));
        }


        final BigDecimal maxAmount = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.maxAmountParamName);

        final BigDecimal minAmount = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.minAmountParamName);

        if (maxAmount.compareTo(investmentProject.getAmount()) > 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.max.amount.is.higher.than.project.amount", "Max amount is higher than project amount");
        }
        if (minAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.min.amount.should.be.higher.than.zero", "Min amount should be higher than zero");
        }

        investmentProject.setMaxAmount(maxAmount);

        investmentProject.setMinAmount(minAmount);

        final String mnemonic = command.stringValueOfParameterNamed(InvestmentProjectConstants.mnemonicParamName);

        InvestmentProject investmentProjectByMnemonic = investmentProjectRepository.retrieveOneByMnemonic(mnemonic);

        if (investmentProjectByMnemonic != null) {
            throw new GeneralPlatformDomainRuleException("msg.err.mnemonic.already.in.use", "Mnemonic already in use");
        }

        investmentProject.setMnemonic(mnemonic);

        final Integer position = command.integerValueSansLocaleOfParameterNamed(InvestmentProjectConstants.positionParamName);

        if (position <= 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.position.should.be.higher.than.zero", "Position should be higher than zero");
        }

        investmentProject.setPosition(position);

        BigDecimal amountToBeDelivered = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeDeliveredParamName);
        if (amountToBeDelivered != null) {
            investmentProject.setAmountToBeDelivered(amountToBeDelivered);
        }

        BigDecimal amountToBeFinanced = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeFinancedParamName);
        if (amountToBeFinanced != null) {
            investmentProject.setAmountToBeFinanced(amountToBeFinanced);
        }

        investmentProject = investmentProjectRepository.saveAndFlush(investmentProject);

        final String subcategories = command.stringValueOfParameterNamed(InvestmentProjectConstants.subCategoriesParamName);
        InvestmentProject finalInvestmentProject = investmentProject;
        if (subcategories != null && !subcategories.isEmpty()) {
            List<CodeValue> codeSubCategories = getCodevaluesDataFromArray(subcategories);
            codeSubCategories
                    .forEach(item -> investmentProjectCategoryRepository.save(new InvestmentProjectCategory(item, finalInvestmentProject)));

        }
        final String objectives = command.stringValueOfParameterNamed(InvestmentProjectConstants.objectivesParamName);
        if (objectives != null && !objectives.isEmpty()) {
            List<CodeValue> codeObjectives = getCodevaluesDataFromArray(objectives);
            codeObjectives.forEach(item -> investmentProjectObjectiveRepository.save(new InvestmentProjectObjective(item, finalInvestmentProject)));

        }

        final Long statusCodeId = command.longValueOfParameterNamed(InvestmentProjectConstants.statusIdParamName);
        final Long creditTypeId = command.longValueOfParameterNamed(InvestmentProjectConstants.creditTypeIdParamName);

        if (creditTypeId != null) {
            CodeValue creditTypeCodeValue = codeValueRepositoryWrapper.findOneWithNotFoundDetection(creditTypeId);
            investmentProject.setCreditType(creditTypeCodeValue);
            investmentProject = investmentProjectRepository.saveAndFlush(investmentProject);
        }
        if (statusCodeId != null) {

            final CodeValue newStatus = codeValueRepositoryWrapper.findOneWithNotFoundDetection(statusCodeId);
            StatusHistoryProject historyProject = new StatusHistoryProject();
            historyProject.setInvestmentProject(investmentProject);
            historyProject.setStatusValue(newStatus);
            statusHistoryProjectRepository.save(historyProject);
        }
        Long basedInLoanProductId = command.longValueOfParameterNamed(InvestmentProjectConstants.basedInLoanProductIdParamName);

        final Long loanPurposeId = command.longValueOfParameterNamed(InvestmentProjectConstants.loanPurposeIdParamName);

        // Build loan data
        JsonObject loanJson = createLoanAccountData(investmentProject.getOwner().getId(), basedInLoanProductId, investmentProject.getAmount(),
                investmentProject.getRate(), investmentProject.getPeriod(), mnemonic, loanPurposeId);

        JsonCommand loanCommand = JsonCommand.from(String.valueOf(loanJson), JsonParser.parseString(loanJson.toString()),
                command.getFromApiJsonHelper());

        // Create saving account
        CommandProcessingResult loanResult = loanApplicationWritePlatformService.submitApplication(loanCommand);

        investmentProject.setLoan(loanRepositoryWrapper.findOneWithNotFoundDetection(loanResult.getResourceId()));
        investmentProjectRepository.saveAndFlush(investmentProject);
        CommandSource commandSource = commandSourceService.saveInitialNewTransaction(new CommandWrapperBuilder().createLoanApplication().build(), loanCommand, context.authenticatedUser(), new IdempotencyKeyGenerator().create());
        commandSource.setResourceExternalId(loanResult.getResourceExternalId());
        commandSource.setLoanId(loanResult.getResourceId());
        commandSource.setClientId(owner.getId());
        commandSourceService.saveResultNewTransaction(commandSource);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(investmentProject.getId()).build();
    }

    @Override
    public CommandProcessingResult updateInvestmentProject(Long projectId, JsonCommand command) {

        this.validateForUpdate(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>(20);

        InvestmentProject investmentProject = investmentProjectRepository.findById(projectId)
                .orElseThrow(() -> new InvestmentProjectNotFoundException(projectId, false));


        investmentProject.modifyApplication(command, changes);

        SocioEnvironmentalDescription socioEnvironmentalDescription;

        if (investmentProject.getDescription().getSocioEnvironmentalDescription() == null) {
            socioEnvironmentalDescription = new SocioEnvironmentalDescription();
        } else {
            socioEnvironmentalDescription = socioEnvironmentalDescriptionRepository.getReferenceById(investmentProject.getDescription().getSocioEnvironmentalDescription().getId());
        }

        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, socioEnvironmentalDescription.getLittleDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName);
            changes.put(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, newValue);
            socioEnvironmentalDescription.setLittleDescription(newValue);
        }

        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, socioEnvironmentalDescription.getDetailedDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName);
            changes.put(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, newValue);
            socioEnvironmentalDescription.setDetailedDescription(newValue);
        }

        if (investmentProject.getDescription().getSocioEnvironmentalDescription() == null) {
            investmentProject.getDescription().setSocioEnvironmentalDescription(socioEnvironmentalDescription);
        } else {
            socioEnvironmentalDescriptionRepository.save(socioEnvironmentalDescription);
        }

        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.mnemonicParamName, investmentProject.getMnemonic())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.mnemonicParamName);
            InvestmentProject investmentProjectByMnemonic = investmentProjectRepository.retrieveOneByMnemonic(newValue);

            if (investmentProjectByMnemonic != null) {
                throw new GeneralPlatformDomainRuleException("msg.err.mnemonic.already.in.use", "Mnemonic already in use");
            }
            
            changes.put(InvestmentProjectConstants.mnemonicParamName, newValue);
            investmentProject.setMnemonic(StringUtils.defaultIfEmpty(newValue, null));
        }

        final Long categoryId = command.longValueOfParameterNamed(InvestmentProjectConstants.categoryParamName);
        investmentProject.setCategory(codeValueRepositoryWrapper.findOneWithNotFoundDetection(categoryId));
        changes.put(InvestmentProjectConstants.categoryParamName, categoryId);

        final Long areaId = command.longValueOfParameterNamed(InvestmentProjectConstants.areaParamName);
        investmentProject.setArea(codeValueRepositoryWrapper.findOneWithNotFoundDetection(areaId));
        changes.put(InvestmentProjectConstants.areaParamName, areaId);

        List<InvestmentProjectCategory> subCategoriesList = investmentProjectCategoryRepository.retrieveByProjectId(investmentProject.getId());
        subCategoriesList.forEach(investmentProjectCategoryRepository::delete);
        final String subCategoriesString = command.stringValueOfParameterNamed(InvestmentProjectConstants.subCategoriesParamName);
        List<CodeValue> codeCategories = getCodevaluesDataFromArray(subCategoriesString);
        InvestmentProject finalInvestmentProject = investmentProject;
        codeCategories.forEach(item -> investmentProjectCategoryRepository.save(new InvestmentProjectCategory(item, finalInvestmentProject)));

        List<InvestmentProjectObjective> objectivesList = investmentProjectObjectiveRepository.retrieveByProjectId(investmentProject.getId());
        objectivesList.forEach(investmentProjectObjectiveRepository::delete);
        final String objectivesString = command.stringValueOfParameterNamed(InvestmentProjectConstants.objectivesParamName);
        List<CodeValue> codeObjectives = getCodevaluesDataFromArray(objectivesString);
        InvestmentProject finalInvestmentProject1 = investmentProject;
        codeObjectives.forEach(item -> investmentProjectObjectiveRepository.save(new InvestmentProjectObjective(item, finalInvestmentProject1)));

        final Long statusCodeId = command.longValueOfParameterNamed(InvestmentProjectConstants.statusIdParamName);
        final CodeValue newStatus = codeValueRepositoryWrapper.findOneWithNotFoundDetection(statusCodeId);
        final Long creditTypeId = command.longValueOfParameterNamed(InvestmentProjectConstants.creditTypeIdParamName);
        final CodeValue newCreditType = codeValueRepositoryWrapper.findOneWithNotFoundDetection(creditTypeId);

        if (!changes.isEmpty() || newStatus != null) {

            if (newCreditType != null) {
                investmentProject.setCreditType(newCreditType);
                changes.put(InvestmentProjectConstants.creditTypeIdParamName, newCreditType.getLabel());
            }
            investmentProject = this.investmentProjectRepository.saveAndFlush(investmentProject);

            StatusHistoryProject historyProject = statusHistoryProjectRepository.getLastStatusByInvestmentProjectId(investmentProject.getId());
            if (historyProject == null || !Objects.equals(historyProject.getStatusValue().getId(), newStatus.getId())) {
                historyProject = new StatusHistoryProject();
                historyProject.setInvestmentProject(investmentProject);
                historyProject.setStatusValue(newStatus);
                statusHistoryProjectRepository.save(historyProject);
                changes.put(InvestmentProjectConstants.statusIdParamName, historyProject.getStatusValue().getId());

            }
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

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, InvestmentProjectConstants.INVESTMENT_PROJECT_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("investmentProject");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String projectNameParam = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectNameParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectNameParamName).value(projectNameParam).notBlank().notNull();

//        final String subtitleParam = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subtitleParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.subtitleParamName).value(subtitleParam).notBlank().notNull();

        final String ownerId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectOwnerIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectOwnerIdParamName).value(ownerId).notBlank().notNull();

        final String amount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.amountParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.amountParamName).value(amount).notBlank().notNull();

//        final String currencyCode = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.currencyCodeParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.currencyCodeParamName).value(currencyCode).notBlank().notNull();

        final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();

        final String period = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.periodParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.periodParamName).value(period).notBlank().notNull();

        final String countryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.countryIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.countryIdParamName).value(countryId).notBlank().notNull();

//        final String impactDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.impactDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.impactDescriptionParamName).value(impactDescription).notBlank()
//                .notNull();

//        final String institutionDescription = fromApiJsonHelper
//                .extractStringNamed(InvestmentProjectConstants.institutionDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.institutionDescriptionParamName).value(institutionDescription)
//                .notBlank().notNull();
//
//        final String teamDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.teamDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.teamDescriptionParamName).value(teamDescription).notBlank()
//                .notNull();

//        final String financingDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.financingDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.financingDescriptionParamName).value(financingDescription).notBlank()
//                .notNull();

//        final String isActive = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.isActiveParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.isActiveParamName).value(isActive).notBlank().notNull()
//                .validateForBooleanValue();
//
//        final String categoryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.categoryParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.categoryParamName).value(categoryId).notBlank().notNull();
//
//        final String subCategories = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subCategoriesParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.subCategoriesParamName).value(subCategories).notBlank().notNull();
//
//        final String objectives = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.objectivesParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.objectivesParamName).value(objectives).notBlank().notNull();
//
//        final String areaId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.areaParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.areaParamName).value(areaId).notBlank().notNull();
//
//        final String maxAmount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.maxAmountParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.maxAmountParamName).value(maxAmount).notBlank().notNull();
//
//        final String minAmount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.minAmountParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.minAmountParamName).value(minAmount).notBlank().notNull();

//        final Long statusId = fromApiJsonHelper.extractLongNamed(InvestmentProjectConstants.statusIdParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.statusIdParamName).value(statusId).notNull();
//
        final String mnemonic = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.mnemonicParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.mnemonicParamName).value(mnemonic).notBlank().notNull();

//        final String littleSocioEnvironmentalDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName).value(littleSocioEnvironmentalDescription).notBlank().notNull();
//
//        final String detailedSocioEnvironmentalDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName).value(detailedSocioEnvironmentalDescription).notBlank().notNull();
//
//        final String position = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.positionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.positionParamName).value(position).notBlank().notNull();

        final String basedInLoanProductId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.basedInLoanProductIdParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.basedInLoanProductIdParamName).value(basedInLoanProductId).notBlank().notNull();

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

//        final String subtitle = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subtitleParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.subtitleParamName).value(subtitle).notBlank().notNull();

        final String rate = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.projectRateParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.projectRateParamName).value(rate).notBlank().notNull();

//        final String impactDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.impactDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.impactDescriptionParamName).value(impactDescription).notBlank()
//                .notNull();
//
//        final String institutionDescription = fromApiJsonHelper
//                .extractStringNamed(InvestmentProjectConstants.institutionDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.institutionDescriptionParamName).value(institutionDescription)
//                .notBlank().notNull();
//
//        final String teamDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.teamDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.teamDescriptionParamName).value(teamDescription).notBlank()
//                .notNull();
//
//        final String financingDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.financingDescriptionParamName,
//                jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.financingDescriptionParamName).value(financingDescription).notBlank()
//                .notNull();
//
//        final Boolean isActive = fromApiJsonHelper.extractBooleanNamed(InvestmentProjectConstants.isActiveParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.isActiveParamName).value(isActive).notBlank().notNull();
//
//        final String categoryId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.categoryParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.categoryParamName).value(categoryId).notBlank().notNull();
//
//        final String areaId = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.areaParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.areaParamName).value(areaId).notBlank().notNull();
//
//        final String subCategories = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.subCategoriesParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.subCategoriesParamName).value(subCategories).notBlank().notNull();
//
//        final String objectives = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.objectivesParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.objectivesParamName).value(objectives).notBlank().notNull();
//
//        final String minAmount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.minAmountParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.minAmountParamName).value(minAmount).notBlank().notNull();
//
//        final String maxAmount = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.maxAmountParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.maxAmountParamName).value(maxAmount).notBlank().notNull();

        final String mnemonic = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.mnemonicParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.mnemonicParamName).value(mnemonic).notBlank().notNull();

//        final String littleSocioEnvironmentalDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName).value(littleSocioEnvironmentalDescription).notBlank().notNull();
//
//        final String detailedSocioEnvironmentalDescription = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, jsonElement);
//        baseDataValidator.reset().parameter(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName).value(detailedSocioEnvironmentalDescription).notBlank().notNull();

        final String position = fromApiJsonHelper.extractStringNamed(InvestmentProjectConstants.positionParamName, jsonElement);
        baseDataValidator.reset().parameter(InvestmentProjectConstants.positionParamName).value(position).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private List<CodeValue> getCodevaluesDataFromArray(String array) {
        List<CodeValue> codeValues = new ArrayList<>();
        List<Long> categoriesId = new ArrayList<>();
        if (array != null && !array.isEmpty()) {
            categoriesId = getIdsFromJsonString(array);
        }
        if (!categoriesId.isEmpty()) {
            categoriesId.forEach(item -> {
                if (item != null) {
                    codeValues.add(codeValueRepositoryWrapper.findOneWithNotFoundDetection(item));
                }
            });
        }
        return codeValues;
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

    private JsonObject createLoanAccountData(final Long clientId, final Long loanProductId, final BigDecimal amount,
                                             final BigDecimal interest, final Integer periods, final String mnemonic,
                                             final Long loanPurposeId) {
        JsonObject accountJson = new JsonObject();
        // To format date in specific format
        DateTimeFormatter formatter = DateUtils.DEFAULT_DATE_FORMATTER;

        LoanProductData loanProductData = loanProductReadPlatformService.retrieveLoanProduct(loanProductId);
        Integer loanTermFrequencyType = PeriodFrequencyType.MONTHS.getValue();
        if (SHORT_NAME_FACTORING.equals(loanProductData.getShortName())) {
            loanTermFrequencyType = PeriodFrequencyType.DAYS.getValue();
        }
        accountJson.addProperty("productId", loanProductData.getId());
        accountJson.addProperty("loanOfficerId", "");
        accountJson.addProperty("loanPurposeId", "");
        accountJson.addProperty("fundId", "");
        accountJson.addProperty("submittedOnDate", DateUtils.getBusinessLocalDate().format(formatter));
        accountJson.addProperty("expectedDisbursementDate", DateUtils.getBusinessLocalDate().format(formatter));
        accountJson.addProperty("externalId", "");
        accountJson.addProperty("linkAccountId", "");
        accountJson.addProperty("createStandingInstructionAtDisbursement", "");
        accountJson.addProperty("loanTermFrequency", periods);
        accountJson.addProperty("loanTermFrequencyType", loanTermFrequencyType);
        accountJson.addProperty("numberOfRepayments", periods);
        accountJson.addProperty("repaymentEvery", loanProductData.getRepaymentEvery());
        accountJson.addProperty("repaymentFrequencyType", loanProductData.getRepaymentFrequencyType().getId());
        accountJson.addProperty("repaymentFrequencyNthDayType", "");
        accountJson.addProperty("repaymentFrequencyDayOfWeekType", "");
        accountJson.add("repaymentsStartingFromDate", JsonNull.INSTANCE);
        accountJson.add("interestChargedFromDate", JsonNull.INSTANCE);
        accountJson.addProperty("interestType", loanProductData.getInterestType().getId());
        accountJson.addProperty("isEqualAmortization", loanProductData.isEqualAmortization());
        accountJson.addProperty("amortizationType", loanProductData.getAmortizationType().getId());
        accountJson.addProperty("interestCalculationPeriodType", loanProductData.getInterestCalculationPeriodType().getId());
        accountJson.addProperty("graceOnPrincipalPayment", loanProductData.getGraceOnPrincipalPayment());
        accountJson.addProperty("graceOnArrearsAgeing", loanProductData.getGraceOnArrearsAgeing());
        accountJson.addProperty("loanIdToClose", "");
        accountJson.addProperty("isTopup", "");
        accountJson.addProperty("transactionProcessingStrategyCode", loanProductData.getTransactionProcessingStrategyCode());
        accountJson.addProperty("interestRateFrequencyType", loanProductData.getInterestRateFrequencyType().getId());
        accountJson.addProperty("interestRatePerPeriod", interest);
        accountJson.add("charges", new JsonArray());
        accountJson.add("collateral", new JsonArray());
        accountJson.add("disbursementData", new JsonArray());
        accountJson.addProperty("dateFormat", DateUtils.DEFAULT_DATE_FORMAT);
        accountJson.addProperty("locale", Locale.ENGLISH.toString());
        accountJson.addProperty("clientId", clientId);
        accountJson.addProperty("loanType", "individual");
        accountJson.addProperty("principal", amount);
        accountJson.addProperty("allowPartialPeriodInterestCalcualtion", loanProductData.getAllowPartialPeriodInterestCalculation());
        accountJson.addProperty("accountNo", mnemonic);
        accountJson.addProperty("loanPurposeId", loanPurposeId);
        return accountJson;
    }
}
