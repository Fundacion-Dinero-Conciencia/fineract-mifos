package com.belat.fineract.portfolio.projectparticipation.data;

import lombok.Data;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRepaymentScheduleInstallmentSummaryData {

    private Integer installmentNumber;
    private LocalDate fromDate;
    private LocalDate dueDate;
    private BigDecimal principal;
    private BigDecimal principalCompleted;
    private BigDecimal principalWrittenOff;
    private BigDecimal interestCharged;
    private BigDecimal interestPaid;
    private BigDecimal interestWaived;
    private BigDecimal interestWrittenOff;
    private BigDecimal interestAccrued;
    private BigDecimal rescheduleInterestPortion;
    private BigDecimal feeChargesCharged;
    private BigDecimal feeChargesPaid;
    private BigDecimal feeChargesWrittenOff;
    private BigDecimal feeChargesWaived;
    private BigDecimal feeAccrued;
    private BigDecimal penaltyCharges;
    private BigDecimal penaltyChargesPaid;
    private BigDecimal penaltyChargesWrittenOff;
    private BigDecimal penaltyChargesWaived;
    private BigDecimal penaltyAccrued;
    private BigDecimal totalPaidInAdvance;
    private BigDecimal totalPaidLate;
    private boolean obligationsMet;
    private LocalDate obligationsMetOnDate;
    private boolean recalculatedInterestComponent;
    private boolean additional;
    private BigDecimal creditedPrincipal;
    private BigDecimal creditedInterest;
    private BigDecimal creditedFee;
    private BigDecimal creditedPenalty;
    private boolean isDownPayment;
    private boolean isReAged;

    public LoanRepaymentScheduleInstallmentSummaryData(LoanRepaymentScheduleInstallment installment) {
        this.installmentNumber = installment.getInstallmentNumber();
        this.fromDate = installment.getFromDate();
        this.dueDate = installment.getDueDate();
        this.principal = installment.getPrincipal();
        this.principalCompleted = installment.getPrincipalCompleted();
        this.principalWrittenOff = installment.getPrincipalWrittenOff();
        this.interestCharged = installment.getInterestCharged();
        this.interestPaid = installment.getInterestPaid();
        this.interestWaived = installment.getInterestWaived();
        this.interestWrittenOff = installment.getInterestWrittenOff();
        this.interestAccrued = installment.getInterestAccrued();
        this.rescheduleInterestPortion = installment.getRescheduleInterestPortion();
        this.feeChargesCharged = installment.getFeeChargesCharged();
        this.feeChargesPaid = installment.getFeeChargesPaid();
        this.feeChargesWrittenOff = installment.getFeeChargesWrittenOff();
        this.feeChargesWaived = installment.getFeeChargesWaived();
        this.feeAccrued = installment.getFeeAccrued();
        this.penaltyCharges = installment.getPenaltyCharges();
        this.penaltyChargesPaid = installment.getPenaltyChargesPaid();
        this.penaltyChargesWrittenOff = installment.getPenaltyChargesWrittenOff();
        this.penaltyChargesWaived = installment.getPenaltyChargesWaived();
        this.penaltyAccrued = installment.getPenaltyAccrued();
        this.totalPaidInAdvance = installment.getTotalPaidInAdvance();
        this.totalPaidLate = installment.getTotalPaidLate();
        this.obligationsMet = installment.isObligationsMet();
        this.obligationsMetOnDate = installment.getObligationsMetOnDate();
        this.recalculatedInterestComponent = installment.isRecalculatedInterestComponent();
        this.additional = installment.isAdditional();
        this.creditedPrincipal = installment.getCreditedPrincipal();
        this.creditedInterest = installment.getCreditedInterest();
        this.creditedFee = installment.getCreditedFee();
        this.creditedPenalty = installment.getCreditedPenalty();
        this.isDownPayment = installment.isDownPayment();
        this.isReAged = installment.isReAged();
    }

}
