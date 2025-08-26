package com.belat.fineract.portfolio.projectparticipation.domain;

import java.math.BigDecimal;
import java.util.List;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationDetailData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.client.id = :clientId AND pp.statusEnum = COALESCE(:statusCode, pp.statusEnum)")
    Page<ProjectParticipation> retrieveByClientId(@Param("clientId") Long clientId, @Param("statusCode") Integer statusCode, Pageable pageable);

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.investmentProject.id = :projectId AND pp.statusEnum = COALESCE(:statusCode, pp.statusEnum)")
    Page<ProjectParticipation> retrieveByProjectId(@Param("projectId") Long projectId, @Param("statusCode") Integer statusCode, Pageable pageable);

    @Query("SELECT DISTINCT pp FROM ProjectParticipation pp JOIN FETCH pp.investmentProject p LEFT JOIN FETCH p.subCategories LEFT JOIN FETCH p.objectives WHERE pp.client.id = :clientId AND pp.statusEnum = COALESCE(:statusCode, pp.statusEnum)")
    List<ProjectParticipation> retrieveWithDetailsByClientId(@Param("clientId") Long clientId, @Param("statusCode") Integer statusCode);

    @Query(value = "SELECT * FROM e_project_participation WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    ProjectParticipation retrieveOneById(Long id);

    @Query("""
    SELECT new com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationDetailData(
        pp.id, pp.client.id, pp.amount, pp.statusEnum, pp.type, pp.commission,
        p.id, p.name, p.amount, p.currencyCode, p.rate,
        n.id, n.fundSavingsAccount.id, n.investorSavingsAccount.id, n.investmentAmount, n.promissoryNoteNumber, n.percentageShare
    )
    FROM ProjectParticipation pp LEFT JOIN pp.investmentProject p LEFT JOIN PromissoryNote n ON n.projectParticipationId = pp.id
    WHERE pp.client.id = :clientId AND pp.statusEnum = COALESCE(:statusCode, pp.statusEnum)
    """)
    Page<ProjectParticipationDetailData> findProjectParticipationByClientId(@Param("clientId") Long clientId, @Param("statusCode") Integer statusCode, Pageable pageable);

    @Query("""
    SELECT new com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationDetailData(
        pp.id, pp.client.id, pp.amount, pp.statusEnum, pp.type, pp.commission,
        p.id, p.name, p.amount, p.currencyCode, p.rate,
        n.id, n.fundSavingsAccount.id, n.investorSavingsAccount.id, n.investmentAmount, n.promissoryNoteNumber, n.percentageShare
    )
    FROM ProjectParticipation pp LEFT JOIN pp.investmentProject p LEFT JOIN PromissoryNote n ON n.projectParticipationId = pp.id
    WHERE pp.investmentProject.id = :projectId AND pp.statusEnum = COALESCE(:statusCode, pp.statusEnum)
    """)
    Page<ProjectParticipationDetailData> findProjectParticipationByProjectId(@Param("projectId") Long projectId, @Param("statusCode") Integer statusCode, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE investment_project_id = ?1 AND (status_enum = 100 OR status_enum = 400)", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByProjectId(Long projectId);

    @Query(value = "SELECT COALESCE(SUM(amount), 0.0) FROM e_project_participation WHERE client_id = ?1 AND investment_project_id = ?2 AND status_enum = 100", nativeQuery = true)
    BigDecimal retrieveTotalParticipationAmountByClientIdAndProjectId(Long clientId, Long projectId);

    @Query(value = "SELECT COUNT(*) FROM e_project_participation WHERE investment_project_id = ?1 AND (status_enum = 100 OR status_enum = 400)", nativeQuery = true)
    Long countParticipationByProjectIdWithStatus100And400(Long projectId);

}
