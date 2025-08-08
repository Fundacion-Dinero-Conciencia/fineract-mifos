package com.belat.fineract.portfolio.investmentproject.domain.commission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdditionalExpensesRepository extends JpaRepository<AdditionalExpenses, Long> {

    @Query(value = "SELECT * FROM e_additional_expenses WHERE project_id = ?1", nativeQuery = true)
    List<AdditionalExpenses> findByInvestmentProjectId(Long investmentProjectId);

    @Query(value = "SELECT ae FROM AdditionalExpenses ae WHERE ae.project.id =:projectId AND ae.commissionType.id =:commissionTypeId")
    AdditionalExpenses findByInvestmentProjectIdAndCommissionTypeId(@Param("projectId") final Long projectId, @Param("commissionTypeId") final Long commissionTypeId);


    @Modifying
    @Query(value = "DELETE FROM e_additional_expenses WHERE project_id = ?1", nativeQuery = true)
    void deleteAllByInvestmentProjectId(final Long investmentProjectId);

    @Query("SELECT ae FROM AdditionalExpenses ae JOIN FETCH ae.project WHERE ae.id = :id")
    Optional<AdditionalExpenses> findByIdWithProject(@Param("id") Long id);

}
