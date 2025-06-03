package com.belat.fineract.portfolio.loanrelations.service;

import com.belat.fineract.portfolio.loanrelations.data.LoanRelationshipsData;

import java.util.List;

public interface LoanRelationshipsReadPlatformService {

    List<LoanRelationshipsData> getLoanRelationshipsData();

    LoanRelationshipsData getLoanRelationshipsDataBySubLoanId(Long loanId);

    LoanRelationshipsData getLoanRelationshipsDataByLoanSimulationId(Long loanId);
}
