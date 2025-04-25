package com.belat.fineract.portfolio.investmentproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentProjectRepository extends JpaRepository<InvestmentProject, Long> {

    @Query("SELECT ip FROM InvestmentProject ip WHERE ip.owner.id = :clientId")
    List<InvestmentProject> retrieveByClientId(@Param("clientId") Long clientId);

    @Query(value = "SELECT * FROM e_investment_project WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    InvestmentProject retrieveOneByProjectId(Long projectId);

}
