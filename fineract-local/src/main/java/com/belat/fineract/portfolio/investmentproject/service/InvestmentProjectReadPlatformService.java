package com.belat.fineract.portfolio.investmentproject.service;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.StatusHistoryProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import java.util.List;

public interface InvestmentProjectReadPlatformService {

    List<InvestmentProjectData> retrieveAll();

    List<InvestmentProject> retrieveAllInvestmentProject();

    InvestmentProjectData retrieveById(Long id);

    List<InvestmentProjectData> retrieveByClientId(Long clientId);

    List<InvestmentProjectData> retrieveByCategoryId(Long categoryId);

    List<InvestmentProjectData> retrieveByName(String name);

    InvestmentProjectData retrieveByLinkedLoan(Long loanId);

    List<StatusHistoryProjectData> getAllStatusHistoryByInvestmentProjectId(Long investmentId);

    InvestmentProject retrieveInvestmentById(Long id);

    InvestmentProject retrieveByLinkedLoanId(Long loanId);

    List<InvestmentProjectData> retrieveFiltered();
}
