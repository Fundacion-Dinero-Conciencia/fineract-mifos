package com.belat.fineract.portfolio.loanrelations.service;

import com.belat.fineract.portfolio.loanrelations.data.LoanRelationshipsData;

public interface LoanRelationshipsWritePlatformService {

    LoanRelationshipsData createLoanRelationships(Long simulationId, Long subLoanId);
}
