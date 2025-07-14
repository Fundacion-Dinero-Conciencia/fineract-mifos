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

    @Query(value = "SELECT * FROM e_investment_project WHERE mnemonic = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    InvestmentProject retrieveOneByMnemonic(String mnemonic);

    @Query(value = "SELECT * FROM e_investment_project WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    InvestmentProject retrieveOneByProjectId(Long projectId);

    @Query(value = "SELECT * FROM e_investment_project WHERE loan_id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    InvestmentProject retrieveOneByLoanId(Long loanId);

    @Query("SELECT ip FROM InvestmentProject ip WHERE LOWER(ip.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<InvestmentProject> retrieveByName(@Param("name") String name);

    @Query(value = "SELECT ip.* FROM e_investment_project ip " +
            "JOIN ( " +
            "  SELECT DISTINCT ON (investment_project_id) investment_project_id, status_value_id " +
            "  FROM e_project_status_history " +
            "  ORDER BY investment_project_id, created_on_utc DESC " +
            ") ps ON ps.investment_project_id = ip.id " +
            "JOIN m_code_value cv ON cv.id = ps.status_value_id " +
            "WHERE ip.is_active = true " +
            "ORDER BY " +
            "  CASE WHEN LOWER(cv.code_value) = LOWER('En Financiamiento') THEN 0 ELSE 1 END, " +
            "  ip.position",
            nativeQuery = true)
    List<InvestmentProject> retrieveByPositionActiveAndStatus();


}
