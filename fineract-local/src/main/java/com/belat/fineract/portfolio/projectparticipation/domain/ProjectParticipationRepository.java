package com.belat.fineract.portfolio.projectparticipation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.client.id = :clientId")
    List<ProjectParticipation> retrieveByClientId(@Param("clientId") Long clientId);

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.investmentProject.id = :projectId")
    List<ProjectParticipation> retrieveByProjectId(@Param("projectId") Long projectId);

    @Query(value = "SELECT * FROM e_project_participation WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    ProjectParticipation retrieveOneById(Long id);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE investment_project_id = ?1 AND status_enum = 100", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByProjectId(Long projectId);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE client_id = ?1 AND investment_project_id = ?2 AND status_enum = 100", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByClientIdAndProjectId(Long clientId, Long projectId);

    @Query(value = "SELECT COUNT(*) FROM e_project_participation WHERE investment_project_id = ?1 AND status_enum = 100", nativeQuery = true)
    Long countParticipationByProjectIdWithStatus100(Long projectId);

}
