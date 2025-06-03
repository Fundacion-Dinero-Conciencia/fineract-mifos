package com.belat.fineract.portfolio.loanrelations.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRelationshipsRepository extends JpaRepository<LoanRelationships, Long> {

    @Query("SELECT lr FROM LoanRelationships lr WHERE lr.loanSimulationId = :loanSimulationId")
    LoanRelationships getLoanRelationshipsByLoanSimulationId(@Param("loanSimulationId") Long loanSimulationId);

    @Query("SELECT lr FROM LoanRelationships lr WHERE lr.subLoanId = :subLoanId")
    LoanRelationships getLoanRelationshipsBySubLoanId(@Param("subLoanId") Long subLoanId);
}
