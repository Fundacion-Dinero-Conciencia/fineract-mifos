package com.belat.fineract.portfolio.loanrelations.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRelationshipsRepository extends JpaRepository<LoanRelationships, Long> {

    @Query(value = "SELECT * FROM e_loan_relationships WHERE loan_simulation_id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    LoanRelationships getLoanRelationshipsByLoanSimulationId(@Param("loanSimulationId") Long loanSimulationId);

    @Query(value = "SELECT * FROM e_loan_relationships WHERE sub_loan_id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    LoanRelationships getLoanRelationshipsBySubLoanId(@Param("subLoanId") Long subLoanId);
}
