package com.belat.fineract.portfolio.projectparticipation.service.impl;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.exception.InvestmentProjectNotFoundException;
import com.belat.fineract.portfolio.projectparticipation.api.ProjectParticipationConstants;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationStatusEnum;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import com.belat.fineract.portfolio.projectparticipation.exception.ProjectParticipationNotFoundException;
import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationWritePlatformService;
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
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ProjectParticipationWritePlatformServiceImpl implements ProjectParticipationWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ClientRepository clientRepository;
    private final InvestmentProjectRepository investmentProjectRepository;
    private final ProjectParticipationRepository projectParticipationRepository;

    @Override
    public CommandProcessingResult createProjectParticipation(JsonCommand command) {
        this.validateForCreate(command.json());

        ProjectParticipation projectParticipation = new ProjectParticipation();

        final Long participantId = command.longValueOfParameterNamed(ProjectParticipationConstants.participantIdParamName);
        Client participant = clientRepository.findById(participantId).orElseThrow(() -> new ClientNotFoundException(participantId));

        final Long projectId = command.longValueOfParameterNamed(ProjectParticipationConstants.projectIdParamName);
        InvestmentProject investmentProject = investmentProjectRepository.findById(projectId)
                .orElseThrow(() -> new InvestmentProjectNotFoundException(projectId, false));


        BigDecimal clientAmount = command.bigDecimalValueOfParameterNamed(ProjectParticipationConstants.amountParamName);
        if (investmentProject.getMaxAmount() == null || investmentProject.getMinAmount() == null) {
            throw new GeneralPlatformDomainRuleException("err.msg.project.should.have.max.and.min.amount", "Project should have max and min amount");
        }
        if (clientAmount.compareTo(investmentProject.getMinAmount()) < 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.amount.is.lower.than.project.min.amount", "Amount is lower than project min amount");
        }
        if (clientAmount.compareTo(investmentProject.getMaxAmount()) > 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.amount.is.higher.than.project.max.amount", "Amount is higher than project max amount");
        }

        BigDecimal projectAmount = investmentProject.getAmount();
        BigDecimal projectParticipationAmount = projectParticipationRepository.retrieveTotalParticipationAmountByProjectId(projectId);
        BigDecimal availableAmount = projectAmount;

        if (projectParticipationAmount.doubleValue() > 0.0) {
            availableAmount = availableAmount.subtract(projectParticipationAmount);
        }

        if (availableAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.not.available.to.participate", "Project amount has been reached");
        }
        if (availableAmount.compareTo(clientAmount) < 0) {
            throw new GeneralPlatformDomainRuleException("err.msg.not.amount.exceed.available.amount",
                    "Amount exceeds available project amount");
        }

        projectParticipation.setClient(participant);
        projectParticipation.setInvestmentProject(investmentProject);
        projectParticipation.setAmount(command.bigDecimalValueOfParameterNamed(ProjectParticipationConstants.amountParamName));
        projectParticipation.setCommission(command.bigDecimalValueOfParameterNamed(ProjectParticipationConstants.commissionParamName));

        final Integer status = command.integerValueSansLocaleOfParameterNamed(ProjectParticipationConstants.statusParamName);
        if (ProjectParticipationStatusEnum.fromInt(status) == null) {
            throw new GeneralPlatformDomainRuleException("msg.err.not.valid.status", "Status is not valid");
        }

        projectParticipation.setStatusEnum(status);

        projectParticipation.setType(command.stringValueOfParameterNamed(ProjectParticipationConstants.typeParamName));

        projectParticipationRepository.saveAndFlush(projectParticipation);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(projectParticipation.getId()).build();
    }

    @Override
    public CommandProcessingResult updateProjectParticipation(Long id, JsonCommand command) {
        this.validateForUpdate(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>(20);

        ProjectParticipation projectParticipation = projectParticipationRepository.findById(id)
                .orElseThrow(() -> new ProjectParticipationNotFoundException(id));

        BigDecimal projectAmount = projectParticipation.getInvestmentProject().getAmount();
        BigDecimal projectParticipationAmount = projectParticipationRepository
                .retrieveTotalParticipationAmountByProjectId(projectParticipation.getInvestmentProject().getId());
        BigDecimal availableAmount = projectAmount.subtract(projectParticipationAmount);
        BigDecimal clientAmount = command.bigDecimalValueOfParameterNamed(ProjectParticipationConstants.amountParamName);

        if (clientAmount.compareTo(projectParticipation.getAmount()) > 0) {
            BigDecimal extraParticipantClientAmount = clientAmount.subtract(projectParticipation.getAmount());

            if (availableAmount.compareTo(BigDecimal.ZERO) == 0) {
                throw new GeneralPlatformDomainRuleException("err.msg.not.available.to.participate", "Project amount has been reached");
            }
            if (availableAmount.compareTo(extraParticipantClientAmount) < 0) {
                throw new GeneralPlatformDomainRuleException("err.msg.not.amount.exceed.available.amount",
                        "Amount exceeds available project amount");
            }
        }

        final Integer status = command.integerValueSansLocaleOfParameterNamed(ProjectParticipationConstants.statusParamName);
        if (ProjectParticipationStatusEnum.fromInt(status) == null) {
            throw new GeneralPlatformDomainRuleException("msg.err.not.valid.status", "Status is not valid");

        }

        projectParticipation.modifyApplication(command, changes);

        if (!changes.isEmpty()) {
            this.projectParticipationRepository.save(projectParticipation);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(id) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteProjectParticipation(Long id, JsonCommand command) {
        return null;
    }

    private void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ProjectParticipationConstants.PROJECT_PARTICIPATION_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("projectParticipation");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String participantIdParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.participantIdParamName,
                jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.participantIdParamName).value(participantIdParam).notBlank()
                .notNull();

        final String projectIdParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.projectIdParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.projectIdParamName).value(projectIdParam).notBlank().notNull();

        final String amountParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.amountParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.amountParamName).value(amountParam).notBlank().notNull();

        final String commissionParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.commissionParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.commissionParamName).value(commissionParam).notBlank().notNull();

        final String statusParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.statusParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.statusParamName).value(statusParam).notBlank().notNull();

        final String typeParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.typeParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.typeParamName).value(typeParam).notBlank().notNull();

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
                ProjectParticipationConstants.PROJECT_PARTICIPATION_PARAMETERS_FOR_UPDATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("projectParticipation");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String amountParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.amountParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.amountParamName).value(amountParam).notBlank().notNull();

        final String statusParam = fromApiJsonHelper.extractStringNamed(ProjectParticipationConstants.statusParamName, jsonElement);
        baseDataValidator.reset().parameter(ProjectParticipationConstants.statusParamName).value(statusParam).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
