package com.belat.fineract.portfolio.investmentproject.domain.objective;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvestmentProjectObjectiveRepository extends JpaRepository<InvestmentProjectObjective, Long> {

    @Query("SELECT ipo FROM InvestmentProjectObjective ipo WHERE ipo.investmentProject.id = :projectId")
    List<InvestmentProjectObjective> retrieveByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT ipo FROM InvestmentProjectObjective ipo WHERE ipo.objective.id = :objectiveId")
    List<InvestmentProjectObjective> retrieveByCategoryId(@Param("objectiveId") Long objectiveId);

}
