package com.belat.fineract.portfolio.investmentproject.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvestmentProjectCategoryRepository extends JpaRepository<InvestmentProjectCategory, Long> {

    @Query("SELECT ipc FROM InvestmentProjectCategory ipc WHERE ipc.investmentProject.id = :projectId")
    List<InvestmentProjectCategory> retrieveByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT ipc FROM InvestmentProjectCategory ipc WHERE ipc.category.id = :categoryId")
    List<InvestmentProjectCategory> retrieveByCategoryId(@Param("categoryId") Long categoryId);
}
