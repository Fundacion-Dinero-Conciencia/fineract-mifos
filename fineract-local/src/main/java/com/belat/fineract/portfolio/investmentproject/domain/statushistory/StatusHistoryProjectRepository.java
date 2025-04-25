package com.belat.fineract.portfolio.investmentproject.domain.statushistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryProjectRepository extends JpaRepository<StatusHistoryProject, Long> {

    @Query(value = "SELECT * FROM public.e_project_status_history WHERE investment_project_id = ?1 ORDER BY created_on_utc DESC LIMIT 1", nativeQuery = true)
    StatusHistoryProject getLastStatusByInvestmentProjectId(Long investmentProjectId);

    @Query(value = "SELECT * FROM public.e_project_status_history WHERE investment_project_id = ?1", nativeQuery = true)
    List<StatusHistoryProject> getAllStatusHistoryByInvestmentProjectId(Long investmentProjectId);

}
