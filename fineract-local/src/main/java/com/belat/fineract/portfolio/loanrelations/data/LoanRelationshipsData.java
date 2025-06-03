package com.belat.fineract.portfolio.loanrelations.data;

import lombok.Data;

@Data
public class LoanRelationshipsData {
    private Long id;
    private Long subLoanId;
    private Boolean containsCommission;
    private Long loanSimulationId;
}
