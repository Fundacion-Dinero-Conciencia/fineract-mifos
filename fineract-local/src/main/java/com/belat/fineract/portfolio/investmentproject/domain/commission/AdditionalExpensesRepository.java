package com.belat.fineract.portfolio.investmentproject.domain.commission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalExpensesRepository extends JpaRepository<AdditionalExpenses, Long> {

    @Query(value = "SELECT * FROM e_additional_expenses WHERE project_id = ?1", nativeQuery = true)
    List<AdditionalExpenses> findByInvestmentProjectId(Long investmentProjectId);
}
