package com.belat.fineract.portfolio.loanrelations.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_loan_relationships")
public class LoanRelationships extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "contains_commission")
    private Boolean containsCommission;

    @Column(name = "loan_simulation_id")
    private Long loanSimulationId;

    @Column(name = "sub_loan_id")
    private Long subLoanId;
}
