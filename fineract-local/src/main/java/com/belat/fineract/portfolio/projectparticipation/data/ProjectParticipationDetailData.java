package com.belat.fineract.portfolio.projectparticipation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ProjectParticipationDetailData {

    private Long participationId;
    private Long participantId;
    private BigDecimal participationAmount;
    private Integer participationStatus;
    private String participationType;
    private BigDecimal participationCommission;

    private Long projectId;
    private String projectName;
    private BigDecimal projectAmount;
    private String currencyCode;
    private BigDecimal rate;

    private Long noteId;
    private Long noteFundSavingsAccountId;
    private Long noteInvestorSavingsAccountId;
    private BigDecimal noteInvestmentAmount;
    private String notePromissoryNoteNumber;
    private BigDecimal notePercentageShare;

    private BigDecimal commissionEarned;
    private BigDecimal interestsEarned;
    private BigDecimal interestRate;
    private LocalDate promissoryNoteDate;

    private LoanData loan;

    public ProjectParticipationDetailData(Long participationId, Long participantId, BigDecimal participationAmount, Integer participationStatus, String participationType, BigDecimal participationCommission, Long projectId, String projectName, BigDecimal projectAmount, String currencyCode, BigDecimal rate, Long noteId, Long noteFundSavingsAccountId, Long noteInvestorSavingsAccountId, BigDecimal noteInvestmentAmount, String notePromissoryNoteNumber, BigDecimal notePercentageShare) {
        this.participationId = participationId;
        this.participantId = participantId;
        this.participationAmount = participationAmount;
        this.participationStatus = participationStatus;
        this.participationType = participationType;
        this.participationCommission = participationCommission;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectAmount = projectAmount;
        this.currencyCode = currencyCode;
        this.rate = rate;
        this.noteId = noteId;
        this.noteFundSavingsAccountId = noteFundSavingsAccountId;
        this.noteInvestorSavingsAccountId = noteInvestorSavingsAccountId;
        this.noteInvestmentAmount = noteInvestmentAmount;
        this.notePromissoryNoteNumber = notePromissoryNoteNumber;
        this.notePercentageShare = notePercentageShare;
    }

    @Data
    public static class LoanData {
        private Long id;
        private String accountNumber;
        private LoanSummary summary;
        private List<LoanRepaymentScheduleInstallmentSummaryData> repaymentScheduleInstallments;

        public LoanData(Long id, String accountNumber, LoanSummary summary, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
           this.id = id;
           this.accountNumber = accountNumber;
           this.summary = summary;
           this.repaymentScheduleInstallments = repaymentScheduleInstallments.stream().map(LoanRepaymentScheduleInstallmentSummaryData::new).collect(Collectors.toList());
        }

    }

}
