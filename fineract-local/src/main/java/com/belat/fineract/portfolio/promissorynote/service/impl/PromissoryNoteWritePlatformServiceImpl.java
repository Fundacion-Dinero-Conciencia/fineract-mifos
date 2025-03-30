/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.belat.fineract.portfolio.promissorynote.service.impl;

import com.belat.fineract.organisation.staff.service.StaffReadPlatformServiceLocal;
import com.belat.fineract.portfolio.promissorynote.api.PromissoryNoteConstants;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteRepository;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteStatus;
import com.belat.fineract.portfolio.promissorynote.service.PromissoryNoteWritePlatformService;
import com.google.gson.JsonElement;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PromissoryNoteWritePlatformServiceImpl implements PromissoryNoteWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final PromissoryNoteRepository noteRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final StaffReadPlatformServiceLocal staffReadService;

    @Override
    public CommandProcessingResult addPromissoryNote(JsonCommand command) {

        validatePromissoryNoteRequestBody(command);

        PromissoryNote promissoryNote = createPromissoryNote(fromApiJsonHelper.parse(command.json()));

        String promissoryNumberFund = promissoryNote.getFundSavingsAccount().getClient().getDisplayName();
        promissoryNumberFund = promissoryNumberFund.substring(promissoryNumberFund.indexOf("_") + 1);

        String promissoryNumberInvestor = String.valueOf(promissoryNote.getInvestorSavingsAccount().getClient().getId());

        promissoryNote.setPromissoryNoteNumber(paddingNumberPromissory(promissoryNumberInvestor, promissoryNumberFund));
        BigDecimal acumulatePercentage = noteRepository.retrieveByFundAccountIdAndStatus(promissoryNote.getFundSavingsAccount().getId(), PromissoryNoteStatus.ACTIVE)
                .stream()
                .map(PromissoryNote::getPercentageShare)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal percentageCalculated = calculateParticipationPercentage(promissoryNote.getFundSavingsAccount().getMaxAllowedDepositLimit(), promissoryNote.getInvestmentAmount());

        if (acumulatePercentage.add(percentageCalculated).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new PlatformApiDataValidationException("error.msg.amount.fund", "The fund exceeds the percentage available", "available", BigDecimal.valueOf(100).subtract(acumulatePercentage));
        }
        promissoryNote.setPercentageShare(percentageCalculated);

        promissoryNote = noteRepository.save(promissoryNote);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(promissoryNote.getId()).build();
    }

    @Override
    public void validatePromissoryNoteRequestBody(final JsonCommand command) {
        final String apiRequestBodyAsJson = command.json();
        final Set<String> requestParameters = new HashSet<>(Arrays.asList(PromissoryNoteConstants.fundSavingsAccountIdParamName,
                PromissoryNoteConstants.investorSavingsAccountIdParamName, PromissoryNoteConstants.amountParamName));

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("promissoryNote");
        final JsonElement json = fromApiJsonHelper.parse(apiRequestBodyAsJson);

        final String fundSavingsAccountId = fromApiJsonHelper.extractStringNamed(PromissoryNoteConstants.fundSavingsAccountIdParamName,
                json);
        baseDataValidator.reset().parameter(PromissoryNoteConstants.fundSavingsAccountIdParamName).value(fundSavingsAccountId).notBlank()
                .notNull();

        final String investorSavingsAccountId = fromApiJsonHelper
                .extractStringNamed(PromissoryNoteConstants.investorSavingsAccountIdParamName, json);
        baseDataValidator.reset().parameter(PromissoryNoteConstants.investorSavingsAccountIdParamName).value(investorSavingsAccountId)
                .notBlank().notNull();

        final String currencyCode = fromApiJsonHelper.extractStringNamed(PromissoryNoteConstants.currencyCodeParamName, json);
        baseDataValidator.reset().parameter(PromissoryNoteConstants.currencyCodeParamName).value(currencyCode).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    @Override
    public PromissoryNote createPromissoryNote(JsonElement element) {

        PromissoryNote promissoryNote = new PromissoryNote();

        promissoryNote.setStatus(PromissoryNoteStatus.ACTIVE);
        promissoryNote.setInvestmentAmount(getAmountFromJson(element));
        promissoryNote.setFundSavingsAccount(savingsAccountRepository.findById(getFundSavingAccountIdFromJson(element)).orElseThrow(() -> new SavingsAccountNotFoundException(getFundSavingAccountIdFromJson(element))));
        promissoryNote
                .setInvestorSavingsAccount(savingsAccountRepository.findById(getInvestorSavingAccountIdFromJson(element)).orElseThrow(() -> new SavingsAccountNotFoundException(getInvestorSavingAccountIdFromJson(element))));
        promissoryNote.setCurrencyCode(getCurrencyCodeFromJson(element));
        promissoryNote.setPromissoryNoteNumber(promissoryNote.getFundSavingsAccount().getAccountNumber());
        promissoryNote.setInvestmentAgent(getInvestmentAgentFromJson(element));
        promissoryNote.setPercentageInvestmentAgent(getPercentageInvestmentAgent(element));

        if (!Objects.equals(promissoryNote.getFundSavingsAccount().getCurrency().getCode(),
                promissoryNote.getInvestorSavingsAccount().getCurrency().getCode())) {
            String developerMessage = MessageFormat.format("The currency of the accounts is different code:{0}, code:{1}",
                    promissoryNote.getFundSavingsAccount().getCurrency().getCode(),
                    promissoryNote.getInvestorSavingsAccount().getCurrency().getCode());
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", developerMessage, null);
        }

        return promissoryNote;
    }

    private Long getFundSavingAccountIdFromJson(JsonElement json) {
        return fromApiJsonHelper.extractLongNamed(PromissoryNoteConstants.fundSavingsAccountIdParamName, json);
    }

    private Long getInvestorSavingAccountIdFromJson(JsonElement json) {
        return fromApiJsonHelper.extractLongNamed(PromissoryNoteConstants.investorSavingsAccountIdParamName, json);
    }

    private String getCurrencyCodeFromJson(JsonElement json) {
        return fromApiJsonHelper.extractStringNamed(PromissoryNoteConstants.currencyCodeParamName, json);
    }

    private BigDecimal getAmountFromJson(JsonElement json) {
        final Locale locale = fromApiJsonHelper.extractLocaleParameter(json.getAsJsonObject());
        return fromApiJsonHelper.extractBigDecimalNamed(PromissoryNoteConstants.amountParamName, json, locale);
    }

    private Staff getInvestmentAgentFromJson(JsonElement element) {
        final Long investmentAgentId = fromApiJsonHelper.extractLongNamed(PromissoryNoteConstants.investmentAgentParamName, element);
        if (investmentAgentId == null) {
            return null;
        }
        return staffReadService.getById(investmentAgentId);
    }

    private BigDecimal getPercentageInvestmentAgent(JsonElement element) {
        final Locale locale = fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        return fromApiJsonHelper.extractBigDecimalNamed(PromissoryNoteConstants.percentageInvestmentAgentParamName, element, locale);
    }

    private String paddingNumberPromissory(String clientId, String numberFund) {
        int number = 19 - (clientId.length() + numberFund.length())-3;
        String newNumber = "";
        for (int i = 0; i < number; i++) {
            newNumber = newNumber.concat("0");
        }

        String promissoryNoteNumber = numberFund.concat("_" + newNumber.concat(clientId));
        return promissoryNoteNumber.concat(validateAndGenerateConsecutiveNumber(promissoryNoteNumber));
    }

    private String validateAndGenerateConsecutiveNumber(String number) {
        PromissoryNote promissoryNote = noteRepository.retrieveOneByPromissoryNoteNumber(number.concat("%"));
        String consecutive = "";
        if (promissoryNote != null && promissoryNote.getPromissoryNoteNumber().contains(".")) {
            consecutive = promissoryNote.getPromissoryNoteNumber().substring(promissoryNote.getPromissoryNoteNumber().indexOf(".") + 1);
            int n = Integer.parseInt(consecutive) + 1 ;
            consecutive = n < 10 ? ".0".concat(String.valueOf(n)) : ".".concat(String.valueOf(n));
        } else {
            consecutive = ".00";
        }

        return consecutive;
    }

    private BigDecimal calculateParticipationPercentage(BigDecimal totalFund, BigDecimal totalInvestment) {
        if (totalInvestment == null || totalInvestment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformApiDataValidationException("error.msg.resource.amount", "The amount of the investment should be greater than 0", null);
        }
        if (totalFund == null || totalFund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformApiDataValidationException("error.msg.resource.amount", "The amount of the fund should be greater than 0", null);
        }
        return (totalInvestment.divide(totalFund, 2, MoneyHelper.getRoundingMode()))
                .multiply(BigDecimal.valueOf(100));

    }
}
