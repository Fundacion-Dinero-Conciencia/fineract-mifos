package com.belat.fineract.portfolio.saving.service.impl;

import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteStatus;
import com.belat.fineract.portfolio.promissorynote.service.impl.PromissoryNoteReadPlatformServiceImpl;
import com.belat.fineract.portfolio.saving.api.DistributeFundConstants;
import com.belat.fineract.portfolio.saving.service.DistributeFundWritePlatformService;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional()
public class DistributeFundWritePlatformServiceImpl implements DistributeFundWritePlatformService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsAccountTransactionRepository transactionRepository;
    private final ApplicationContext context;
    private final PromissoryNoteReadPlatformServiceImpl promissoryNoteReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ConfigurationDomainService configurationService;

    @Override
    @Transactional
    public Map<String, Object> distributeFunds(Long accountId, LocalDate transferDate) {

        final BigDecimal percentageInvestmentReturn = configurationService.retrievePercentageInvestmentFeeReturn();
        final SavingsAccount savingBelat = savingsAccountRepository.findById(configurationService.getDefaultAccountId()).orElseThrow();
        final MathContext mc = MoneyHelper.getMathContext(2);

        final Map<String, Object> changes = new LinkedHashMap<>(7);

        SavingsAccount savingsAccountFund = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new SavingsAccountNotFoundException(accountId));

        validateFund(savingsAccountFund);

        List<PromissoryNote> accountsToDistribute = promissoryNoteReadPlatformService.retrieveByFundAccountId(savingsAccountFund.getId())
                .stream().filter(sa -> sa.getStatus().equals(PromissoryNoteStatus.ACTIVE) && sa.getFundSavingsAccount().getId() != null
                        && sa.getFundSavingsAccount().getId().equals(savingsAccountFund.getId()))
                .toList();

        final List<SavingsAccountTransaction> transactions = savingsAccountFund.getTransactions().stream()
                .filter(tr -> (tr.getTransactionType().isCapitalPayment() || tr.getTransactionType().isCurrentInterest()
                        || tr.getTransactionType().isArrearsInterest() || tr.getTransactionType().isInvestmentFee())
                        && !tr.isReversed() && (tr.getWasDistribute() == null || !tr.getWasDistribute())
                        && (tr.getTransactionDate().isAfter(tr.getSavingsAccount().getActivationDate())
                                || tr.getTransactionDate().equals(tr.getSavingsAccount().getActivationDate())))
                .toList();

        if (transactions.isEmpty()) {
            throw new PlatformApiDataValidationException("error.msg.transaction", "No balance to distribute to investors", null);
        }

        List<Long> transactionsList = new ArrayList<>(100);
        for (SavingsAccountTransaction tr : transactions) {
            String paymentPeriods = MessageFormat.format(", Cuotas: {0}", tr.getInstallments().replace("[\\[\\]]", ""));

            BigDecimal transactionAmount = Money.of(savingBelat.getCurrency(), tr.getAmount()).getAmount();

            // Commission CPD (devolución)
            if (percentageInvestmentReturn.doubleValue() > 0 && tr.getTransactionType().isCurrentInterest()) {
                BigDecimal amountEarned = Money.of(savingBelat.getCurrency(), transactionAmount.multiply(percentageInvestmentReturn.divide(BigDecimal.valueOf(100), mc))).getAmount();
                transactionAmount = Money.of(savingBelat.getCurrency(), transactionAmount.subtract(amountEarned)).getAmount();

                if (amountEarned.doubleValue() > 0) {
                    Long transactionPercentage = sendTransaction(savingsAccountFund, savingBelat, amountEarned,
                            DistributeFundConstants.COMMISSION_CPD.concat("-" + savingsAccountFund.getId()).concat(paymentPeriods), null, transferDate);
                    transactionsList.add(transactionPercentage);
                }

            }

            for (PromissoryNote item : accountsToDistribute) {
                BigDecimal amountToSend = Money.of(item.getInvestorSavingsAccount().getCurrency(), transactionAmount.multiply(item.getPercentageShare().divide(BigDecimal.valueOf(100), mc))).getAmount();
                BigDecimal amountCAI = BigDecimal.ZERO;
                // Commission CAI (agente inversiones)
                if (item.getInvestmentAgent() != null && item.getPercentageInvestmentAgent().doubleValue() > 0
                        && tr.getTransactionType().isCurrentInterest()) {
                    log.info("Send transaction to investment agent {}", item.getInvestmentAgent().displayName());
                    amountCAI = Money.of(item.getInvestorSavingsAccount().getCurrency(), amountToSend.multiply(item.getPercentageInvestmentAgent().divide(BigDecimal.valueOf(100), mc))).getAmount();
                    SavingsAccount savingInvestmentAgent = savingsAccountRepository.findByStaffId(item.getInvestmentAgent().getId());
                    Long transactionId = sendTransaction(savingsAccountFund, savingInvestmentAgent, amountCAI,
                            DistributeFundConstants.COMMISSION_CAI
                                    .concat("-" + item.getInvestorSavingsAccount().getClient().getDisplayName()).concat(paymentPeriods), null, transferDate);
                    transactionsList.add(transactionId);

                }
                amountToSend = Money.of(item.getInvestorSavingsAccount().getCurrency(), amountToSend.subtract(amountCAI)).getAmount();
                if (tr.getId().equals(transactions.get(transactions.size() - 1).getId())
                        && item.getId().equals(accountsToDistribute.get(accountsToDistribute.size() - 1).getId())) {
                    BigDecimal amountToCompare = savingsAccountRepository.findById(savingsAccountFund.getId())
                            .orElseThrow(() -> new SavingsAccountNotFoundException(savingBelat.getId())).getSummary().getAccountBalance();
                    if (amountToCompare.compareTo(amountToSend) < 0) {
                        amountToSend = Money.of(item.getInvestorSavingsAccount().getCurrency(), amountToCompare).getAmount();
                    }
                }
                String description = DistributeFundConstants.PAYMENT_FUND_INVESTMENT.concat("-" + savingsAccountFund.getId()).concat(paymentPeriods);
                Long transactionId = sendTransaction(savingsAccountFund, item.getInvestorSavingsAccount(), amountToSend, description, tr.getTransactionType().getValue(), transferDate);
                transactionsList.add(transactionId);
            }
            tr.setWasDistribute(true);
            tr.setDistributeDate(DateUtils.getBusinessLocalDate());
            transactionRepository.saveAndFlush(tr);
        }

        changes.put("transactions", transactionsList);
        return changes;
    }

    @Transactional
    public Long sendTransaction(SavingsAccount accountFund, SavingsAccount accountInvestor, BigDecimal amount,
                                String observationTransaction, Integer paymentTypeId, LocalDate transferDate) {

        Map<String, Object> transferData = new HashMap<>();
        LocalDate transactionDate = transferDate != null ? transferDate : DateUtils.getBusinessLocalDate();
        String transferDateStr = transactionDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("es")));

        transferData.put("toAccountId", accountInvestor.getId());
        transferData.put("toClientId", accountInvestor.getClient().getId());
        transferData.put("toAccountType", 2);
        transferData.put("toOfficeId", accountInvestor.officeId());

        transferData.put("transferAmount", Money.of(accountFund.getCurrency(), amount).getAmount());
        transferData.put("transferDate", transferDateStr);
        transferData.put("transferDescription", observationTransaction);
        transferData.put("dateFormat", "dd MMMM yyyy");
        transferData.put("locale", "es");

        transferData.put("fromAccountId", accountFund.getId());
        transferData.put("fromClientId", accountFund.getClient().getId());
        transferData.put("fromAccountType", "2");
        transferData.put("fromOfficeId", accountFund.officeId());
        transferData.put("paymentTypeId", paymentTypeId);

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(new Gson().toJson(transferData));
        final CommandWrapper commandRequest = builder.createAccountTransfer().build();
        CommandProcessingResult result = null;
        result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result.getResourceId();

    }

    private void validateFund(SavingsAccount savingsAccount) {

        String defaultUserMessage = "";
        ApiParameterError error = null;
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final Long clientTypeFund = context.getEnvironment().getProperty("fineract.fund.client.type", Long.class);

        if (savingsAccount.isNotActive()) {

            defaultUserMessage = "Transaction is not allowed. Account is not active.";
            error = ApiParameterError.parameterError("error.msg." + ".transaction.account.is.not.active", defaultUserMessage, "accountId",
                    savingsAccount.getId());
            dataValidationErrors.add(error);
        }

//        } else if (!Objects.equals(savingsAccount.getClient().getClientType().getId(), clientTypeFund)) {
//            defaultUserMessage = "Transaction is not allowed. the account does not belong to a fund.";
//            error = ApiParameterError.parameterError("error.msg." + ".account.does.not.belong.to.fund", defaultUserMessage, "accountId",
//                    savingsAccount.getId());
//            dataValidationErrors.add(error);
//
//        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
