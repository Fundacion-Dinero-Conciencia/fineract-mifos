package com.belat.fineract.portfolio.projectparticipation.domain;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.client.id = :clientId")
    Page<ProjectParticipation> retrieveByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.investmentProject.id = :projectId")
    Page<ProjectParticipation> retrieveByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query(value = "SELECT * FROM e_project_participation WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    ProjectParticipation retrieveOneById(Long id);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE investment_project_id = ?1 AND (status_enum = 100 OR status_enum = 400)", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByProjectId(Long projectId);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE client_id = ?1 AND investment_project_id = ?2 AND status_enum = 100", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByClientIdAndProjectId(Long clientId, Long projectId);

    @Query(value = "SELECT COUNT(*) FROM e_project_participation WHERE investment_project_id = ?1 AND (status_enum = 100 OR status_enum = 400)", nativeQuery = true)
    Long countParticipationByProjectIdWithStatus100And400(Long projectId);

}
