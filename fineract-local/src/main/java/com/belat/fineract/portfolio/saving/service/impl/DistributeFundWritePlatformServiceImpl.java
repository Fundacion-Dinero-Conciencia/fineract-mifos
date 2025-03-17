package com.belat.fineract.portfolio.saving.service.impl;

import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteStatus;
import com.belat.fineract.portfolio.promissorynote.service.impl.PromissoryNoteReadPlatformServiceImpl;
import com.belat.fineract.portfolio.saving.service.DistributeFundWritePlatformService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistributeFundWritePlatformServiceImpl implements DistributeFundWritePlatformService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsAccountTransactionRepository transactionRepository;
    private final ApplicationContext context;
    private final PromissoryNoteReadPlatformServiceImpl promissoryNoteReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final String PAYMENT_INVERSION = "Payment inversion";


    @Override
    public Map<String, Object> distributeFunds(Long accountId) {

        final Map<String, Object> changes = new LinkedHashMap<>(7);

        SavingsAccount savingsAccountFund = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new SavingsAccountNotFoundException(accountId));

        validateFund(savingsAccountFund);

        List<PromissoryNote> accountsToDistribute = promissoryNoteReadPlatformService
                .retrieveByFundAccountId(savingsAccountFund.getId())
                .stream()
                .filter(sa -> sa.getStatus().equals(PromissoryNoteStatus.ACTIVE) && sa.getFundSavingsAccount().getId() != null &&  sa.getFundSavingsAccount().getId().equals(savingsAccountFund.getId()))
                .toList();


        final List<SavingsAccountTransaction> transactions = savingsAccountFund.getTransactions()
                .stream()
                .filter(tr -> tr.getTransactionType().isDeposit()
                        && !tr.isReversed()
                        && (tr.getWasDistribute() == null || !tr.getWasDistribute())
                        && tr.getTransactionDate().isAfter(tr.getSavingsAccount().getActivationDate()))
                .toList();

        if (transactions.isEmpty()) {
            throw new PlatformApiDataValidationException("error.msg.transaction", "No balance to distribute to investors", null);
        }

        List<Long> transactionsList = new ArrayList<>(100);
        for (SavingsAccountTransaction tr : transactions) {
            BigDecimal transactionAmount = tr.getAmount();

            for (PromissoryNote item : accountsToDistribute) {

                // amount = amountTransaction * percentage
                BigDecimal amountToSend = transactionAmount.multiply(item.getPercentageShare().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

                Long transactionId = sendTransaction(savingsAccountFund, item.getInvestorSavingsAccount(), amountToSend);
                transactionsList.add(transactionId);
            }
            tr.setWasDistribute(true);
            tr.setDistributeDate(DateUtils.getBusinessLocalDate());
            transactionRepository.saveAndFlush(tr);
        }

        changes.put("transactions", transactionsList);
        return changes;
    }

    private Long sendTransaction(SavingsAccount accountFund, SavingsAccount accountInvestor, BigDecimal amount) {

        Map<String, Object> transferData = new HashMap<>();

        transferData.put("toAccountId", accountInvestor.getId());
        transferData.put("toClientId", accountInvestor.getClient().getId());
        transferData.put("toAccountType", 2);
        transferData.put("toOfficeId", accountInvestor.officeId());

        transferData.put("transferAmount", amount);
        transferData.put("transferDate", DateUtils.getBusinessLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("es"))));
        transferData.put("transferDescription", PAYMENT_INVERSION);
        transferData.put("dateFormat", "dd MMMM yyyy");
        transferData.put("locale", "es");

        transferData.put("fromAccountId", accountFund.getId());
        transferData.put("fromClientId", accountFund.getClient().getId());
        transferData.put("fromAccountType", "2");
        transferData.put("fromOfficeId", accountFund.officeId());



        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(new Gson().toJson(transferData));
        final CommandWrapper commandRequest = builder.createAccountTransfer().build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result.getResourceId();

    }

    private void validateFund(SavingsAccount savingsAccount) {

        String defaultUserMessage = "";
        ApiParameterError error = null;
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final Long clientTypeFund = context.getEnvironment().getProperty("fineract.fund.client.type", Long.class);

        if (savingsAccount.isNotActive() ) {

            defaultUserMessage = "Transaction is not allowed. Account is not active.";
            error = ApiParameterError.parameterError(
                    "error.msg." + ".transaction.account.is.not.active", defaultUserMessage, "accountId", savingsAccount.getId());
            dataValidationErrors.add(error);

        } else if (!Objects.equals(savingsAccount.getClient().getClientType().getId(), clientTypeFund)) {
            defaultUserMessage = "Transaction is not allowed. the account does not belong to a fund.";
            error = ApiParameterError.parameterError(
                    "error.msg." + ".account.does.not.belong.to.fund", defaultUserMessage, "accountId", savingsAccount.getId());
            dataValidationErrors.add(error);

        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
