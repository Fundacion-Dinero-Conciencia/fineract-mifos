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
package org.apache.fineract.portfolio.loanaccount.service.fund;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanFundServiceImpl {

    private final ApplicationContext applicationContext;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService;
    private final SavingsAccountRepository savingsAccountRepository;

    public void createFundAccount(final Loan loan, final JsonCommand command) {

        final String savingsName = applicationContext.getEnvironment().getProperty("fineract.fund.client.name").concat(String.valueOf(loan.getAccountNumber()));

        // Build saving data
        JsonObject savingJson = createSavingAccountData(loan.getApprovedPrincipal(), loan.getClientId(),
                savingsName, loan.getCurrencyCode(), command.stringValueOfParameterNamed("approvedOnDate"), command.stringValueOfParameterNamed("locale"), command.stringValueOfParameterNamed("dateFormat"));

        JsonCommand savingCommand = JsonCommand.from(String.valueOf(savingJson), JsonParser.parseString(savingJson.toString()),
                command.getFromApiJsonHelper());

        // Create saving account
        CommandProcessingResult savingResult = savingsApplicationProcessWritePlatformService.submitApplication(savingCommand);

        SavingsAccount account = savingsAccountRepository.findSavingAccountByClientId(loan.getClientId()).stream()
                .filter(saving -> Objects.equals(saving.getId(), savingResult.getSavingsId())).findFirst()
                .orElseThrow(() -> new SavingsAccountNotFoundException("Saving account not found"));

        // Link fund saving account
        AccountAssociations accountAssociations = AccountAssociations.associateSavingsAccount(loan, account,
                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION_FOR_FUND.getValue(), true);
        this.accountAssociationsRepository.save(accountAssociations);
    }

    private JsonObject createFundClientData(final Long officeId, final String accountNo, final DateTimeFormatter formatter) {
        JsonObject clientJson = new JsonObject();
        clientJson.addProperty("officeId", officeId);
        clientJson.addProperty("legalFormId", applicationContext.getEnvironment().getProperty("fineract.client.type.entity"));
        clientJson.addProperty("fullname",
                Objects.requireNonNull(applicationContext.getEnvironment().getProperty("fineract.fund.client.name"))
                        .concat(String.valueOf(accountNo)));
        clientJson.addProperty("dateFormat", DateUtils.DEFAULT_DATE_FORMAT);
        clientJson.addProperty("locale", Locale.ENGLISH.toString());
        clientJson.addProperty("clientTypeId", applicationContext.getEnvironment().getProperty("fineract.fund.client.type"));
        clientJson.addProperty("activationDate", DateUtils.getBusinessLocalDate().format(formatter));
        clientJson.addProperty("active", true);
        return clientJson;
    }

    public JsonObject createSavingAccountData(final BigDecimal amount, final Long clientId, final String accountNo,
                                              final String currencyCode, final String creationDate, final String locale, final String dateFormat) {
        JsonObject accountJson = new JsonObject();
        if (amount != null) {
            accountJson.addProperty("maxAllowedDepositLimit", amount);
        }
        accountJson.addProperty("dateFormat", dateFormat);
        accountJson.addProperty("locale", locale);
        accountJson.addProperty("submittedOnDate", creationDate);
        accountJson.addProperty("productId", applicationContext.getEnvironment().getProperty("fineract.saving.product.id"));
        accountJson.addProperty("clientId", clientId);
        accountJson.addProperty("allowOverdraft", false);
        if (accountNo != null) {
            accountJson.addProperty("accountNo", accountNo);
        }
        accountJson.addProperty("currencyCode", currencyCode);
        return accountJson;
    }

}
