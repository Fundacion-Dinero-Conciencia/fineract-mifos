package com.belat.fineract.portfolio.projectparticipation.service.impl;

import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationStatusEnum;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import com.belat.fineract.portfolio.projectparticipation.mapper.ProjectParticipationMapper;
import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationReadPlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectParticipationReadPlatformServiceImpl implements ProjectParticipationReadPlatformService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectParticipationMapper projectParticipationMapper;
    private final InvestmentProjectReadPlatformService investmentProjectReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final LoanRepository loanRepository;
    private final ApplicationContext applicationContext;
    @Override
    public List<ProjectParticipationData> retrieveAll() {
        List<ProjectParticipation> projectParticipations = projectParticipationRepository.findAll();
        List<ProjectParticipationData> projectParticipationData = new ArrayList<>();
        projectParticipations.forEach(projectParticipation -> {
            if (projectParticipation != null) {
                ProjectParticipationData projectData = projectParticipationMapper.map(projectParticipation);
                factoryData(projectData, projectParticipation, projectParticipationData);
            }
        });
        return projectParticipationData;
    }

    @Override
    public ProjectParticipationData retrieveById(Long id) {
        ProjectParticipation projectParticipation = projectParticipationRepository.retrieveOneById(id);
        ProjectParticipationData projectParticipationData = projectParticipationMapper.map(projectParticipation);
        if (projectParticipation != null) {
            factoryData(projectParticipationData, projectParticipation, new ArrayList<>());
        }
        return projectParticipationData;
    }

    @Override
    public List<ProjectParticipationData> retrieveByClientId(Long clientId) {
        List<ProjectParticipation> projectsParticipation = projectParticipationRepository.retrieveByClientId(clientId);
        List<ProjectParticipationData> projectParticipationData = new ArrayList<>();
        projectsParticipation.forEach(projectParticipation -> {
            if (projectParticipation != null) {
                ProjectParticipationData projectsData = projectParticipationMapper.map(projectParticipation);
                factoryData(projectsData, projectParticipation, projectParticipationData);
            }
        });
        return projectParticipationData;
    }

    @Override
    public List<ProjectParticipationData> retrieveByProjectId(Long projectId) {
        List<ProjectParticipation> projectsParticipation = projectParticipationRepository.retrieveByProjectId(projectId);
        List<ProjectParticipationData> projectParticipationData = new ArrayList<>();
        projectsParticipation.forEach(projectParticipation -> {
            if (projectParticipation != null) {
                ProjectParticipationData projectsData = projectParticipationMapper.map(projectParticipation);
                factoryData(projectsData, projectParticipation, projectParticipationData);
            }
        });
        return projectParticipationData;
    }

    private void factoryData (ProjectParticipationData projectData, ProjectParticipation project, List<ProjectParticipationData> projectsData) {
        projectData.setParticipantId(project.getClient().getId());
        projectData.setProject(investmentProjectReadPlatformService.retrieveById(project.getInvestmentProject().getId()));
        ProjectParticipationData.StatusEnum statusEnum = new ProjectParticipationData.StatusEnum(ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).toEnumOptionData().getCode(), ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).getValue());
        projectData.setConfirmedParticipants(projectParticipationRepository.countParticipationByProjectIdWithStatus100(project.getInvestmentProject().getId()));
        projectData.setStatus(statusEnum);

        //Get investor saving account
        Optional<SavingsAccount> account = savingsAccountRepositoryWrapper.findSavingAccountByClientId(project.getClient().getId()).stream().findFirst();
        if (account.isPresent()) {
            //Get project owner credit name
            Loan loan = loanRepository.findLoanByClientIdAmountAndApprovedStatus(project.getInvestmentProject().getOwner().getId(), project.getInvestmentProject().getAmount());

            if (loan != null) {
                //Get transactions to investor saving account
                List<AccountTransferData> accountTransferData = accountTransfersReadPlatformService.retrieveToSavingsAccountTransactionsDependsOnFromSavingsName(account.get().getId(),
                        applicationContext.getEnvironment().getProperty("fineract.fund.client.name") + loan.getAccountNumber());

                BigDecimal principalEarned = BigDecimal.ZERO;
                BigDecimal interestEarned = BigDecimal.ZERO;
                BigDecimal commissionEarned = BigDecimal.ZERO;

                for (var item : accountTransferData) {
                    if (item.getTransferDescription().contains("P")) {
                        principalEarned = principalEarned.add(item.getTransferAmount());
                    }
                    if (item.getTransferDescription().contains("I")) {
                        interestEarned = interestEarned.add(item.getTransferAmount());
                    }
                    if (item.getTransferDescription().contains("C")) {
                        commissionEarned = commissionEarned.add(item.getTransferAmount());
                    }
                }
                projectData.setPrincipalEarned(principalEarned);
                projectData.setInterestsEarned(interestEarned);
                projectData.setCommissionEarned(commissionEarned);
            }
        }
        projectsData.add(projectData);
    }
}
