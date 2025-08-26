package com.belat.fineract.portfolio.projectparticipation.service.impl;

import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationOdsAreaData;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationStatusEnum;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationDetailData;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import com.belat.fineract.portfolio.projectparticipation.mapper.ProjectParticipationMapper;
import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationReadPlatformService;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.service.PromissoryNoteReadPlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectParticipationReadPlatformServiceImpl implements ProjectParticipationReadPlatformService {

    private static final Logger log = LoggerFactory.getLogger(ProjectParticipationReadPlatformServiceImpl.class);
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectParticipationMapper projectParticipationMapper;
    private final InvestmentProjectReadPlatformService investmentProjectReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final LoanRepository loanRepository;
    private final ApplicationContext applicationContext;
    private final PromissoryNoteReadPlatformService promissoryNoteReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final LoanReadPlatformServiceCommon loanReadPlatformServiceCommon;

    @Override
    public List<ProjectParticipationData> retrieveAll() {
        List<ProjectParticipation> projectParticipations = projectParticipationRepository.findAll();
        List<ProjectParticipationData> projectParticipationData = new ArrayList<>();
        projectParticipations.forEach(projectParticipation -> {
            if (projectParticipation != null) {
                ProjectParticipationData projectData = projectParticipationMapper.map(projectParticipation);
                projectParticipationData.add(projectData);
                projectData.setStatus(new ProjectParticipationData.StatusEnum(
                        ProjectParticipationStatusEnum.fromInt(projectParticipation.getStatusEnum()).toEnumOptionData().getCode(),
                        ProjectParticipationStatusEnum.fromInt(projectParticipation.getStatusEnum()).getValue()));
                projectData.setParticipantId(projectParticipation.getClient().getId());
                projectData.setProject(investmentProjectReadPlatformService.retrieveById(projectParticipation.getInvestmentProject().getId()));
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
    public List<ProjectParticipationData> retrieveByClientId(Long clientId, Integer statusCode, Integer page, Integer size) {
        Pageable pageable = (size != null)
                ? PageRequest.of(page, size)
                : Pageable.unpaged();
        Page<ProjectParticipation> projectsParticipation = projectParticipationRepository.retrieveByClientId(clientId, statusCode, pageable);
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
    public List<ProjectParticipationData> retrieveByProjectId(Long projectId, Integer statusCode, Integer page, Integer size) {
        Pageable pageable = (size != null)
                ? PageRequest.of(page, size)
                : Pageable.unpaged();
        Page<ProjectParticipation> projectsParticipation = projectParticipationRepository.retrieveByProjectId(projectId, statusCode, pageable);
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
    public Page<ProjectParticipationDetailData> retrieveByFiltersPageable(Long clientId, Long projectId, Integer statusCode, Integer page, Integer size) {
        Pageable pageable = (size != null)
                ? PageRequest.of(page, size)
                : Pageable.unpaged();

        Page<ProjectParticipationDetailData> projectsParticipation = null;
        if (clientId != null) {
            projectsParticipation = projectParticipationRepository.findProjectParticipationByClientId(clientId, statusCode, pageable);
        } else if (projectId != null) {
            projectsParticipation = projectParticipationRepository.findProjectParticipationByProjectId(clientId, statusCode, pageable);
        }

        if (projectsParticipation == null) {
            return Page.empty();
        }

        // get loan for each project participation
        projectsParticipation.forEach(pp -> {
            String loanAccount = pp.getNotePromissoryNoteNumber().split("_")[0];
            Loan loan = loanRepository.findLoanAccountByAccountNumber(loanAccount);
            if (loan != null) {
                pp.setLoan(new ProjectParticipationDetailData.LoanData(
                        loan.getId(), loan.getAccountNumber(), loan.getSummary(), loan.getRepaymentScheduleInstallments()));
            }
        });

        return projectsParticipation;
    }


    @Override
    public List<ProjectParticipationOdsAreaData> retrieveOdsAndAreaByClientId(Long clientId, Integer statusCode) {
        List<ProjectParticipation> projectsParticipation = projectParticipationRepository.retrieveWithDetailsByClientId(clientId, statusCode);
        return mapProjectParticipationToPPOdsAreaData(projectsParticipation);
    }

    private List<ProjectParticipationOdsAreaData> mapProjectParticipationToPPOdsAreaData(List<ProjectParticipation> projectsParticipation) {
        return projectsParticipation.stream()
                .map(this::mapSingleProjectParticipation)
                .toList();
    }

    private ProjectParticipationOdsAreaData mapSingleProjectParticipation(ProjectParticipation pp) {
        var clientId = pp.getClient() != null ? pp.getClient().getId() : null;

        var projectOdsAreaData = Optional.ofNullable(pp.getInvestmentProject())
                .map(p -> new ProjectParticipationOdsAreaData.ProjectOdsAreaData(
                        p.getId(), p.getName(), p.getArea(), p.getCategory(), p.getSubCategories(), p.getObjectives()))
                .orElse(null);

        return new ProjectParticipationOdsAreaData(
                pp.getId(),
                clientId,
                pp.getStatusEnum(),
                pp.getType(),
                projectOdsAreaData
        );
    }

    private void factoryData(ProjectParticipationData projectData, ProjectParticipation project,
            List<ProjectParticipationData> projectsData) {
         projectData.setParticipantId(project.getClient().getId());
        projectData.setProject(investmentProjectReadPlatformService.retrieveById(project.getInvestmentProject().getId()));
        ProjectParticipationData.StatusEnum statusEnum = new ProjectParticipationData.StatusEnum(
                ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).toEnumOptionData().getCode(),
                ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).getValue());
        projectData.setConfirmedParticipants(
                projectParticipationRepository.countParticipationByProjectIdWithStatus100And400(project.getInvestmentProject().getId()));
        projectData.setStatus(statusEnum);

        // Get investor saving account
        Optional<SavingsAccount> account = savingsAccountRepositoryWrapper.findSavingAccountByClientId(project.getClient().getId()).stream()
                .findFirst();
        if (account.isPresent()) {

            //Get promissoryNotes by investor saving account
            List<PromissoryNote> promissoryNotes = promissoryNoteReadPlatformService.retrieveByInvestorAccountId(account.get().getId());

            //Get project owner savingsAccounts funds
            List<SavingsAccount> savingsAccounts = savingsAccountRepositoryWrapper.findFundSavingAccountByClientId(
                    applicationContext.getEnvironment().getProperty("fineract.fund.client.name"),
                    project.getInvestmentProject().getOwner().getId());

            List<PromissoryNote> filteredPromissoryNotes = new ArrayList<>();

            //Filter by savingsAccounts and promissoryNotes
            for (PromissoryNote promissoryNote : promissoryNotes) {
                for (SavingsAccount savingsAccount : savingsAccounts) {
                    if (promissoryNote.getFundSavingsAccount().getId().equals(savingsAccount.getId())) {
                        filteredPromissoryNotes.add(promissoryNote);
                    }
                }
            }

            if (!filteredPromissoryNotes.isEmpty()) {
                //Delete duplicates
                //TODO -> no filtrarlos si no obtener el porcentaje por cada monto
                List<PromissoryNote> filtered = filteredPromissoryNotes.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(PromissoryNote::getInvestorSavingsAccount, Function.identity(), (e1, e2) -> e1),
                            map -> new ArrayList<>(map.values())
                    ));

                BigDecimal principalEarned = BigDecimal.ZERO;
                BigDecimal interestEarned = BigDecimal.ZERO;
                BigDecimal commissionEarned = BigDecimal.ZERO;
                BigDecimal pendingAmount = BigDecimal.ZERO;

                //Get total earned
                for (PromissoryNote promissoryNote : filtered) {
                    // Get transactions to investor saving account
                    List<AccountTransferData> accountTransferData = accountTransfersReadPlatformService
                            .retrieveToSavingsAccountTransactionsDependsOnFromSavingsAccount(promissoryNote.getFundSavingsAccount().getId(), account.get().getId());
                    if (!accountTransferData.isEmpty()) {
                        //
                        for (var item : accountTransferData) {
                            if (item.getTransferType() != null) {
                                if (SavingsAccountTransactionType.CURRENT_INTEREST.getValue().equals(item.getTransferType())) {
                                    interestEarned = interestEarned.add(item.getTransferAmount());
                                } else if (SavingsAccountTransactionType.ARREARS_INTEREST.getValue().equals(item.getTransferType()) ||
                                        SavingsAccountTransactionType.INVESTMENT_FEE.getValue().equals(item.getTransferType())) {
                                    commissionEarned = commissionEarned.add(item.getTransferAmount());
                                } else {
                                    principalEarned = principalEarned.add(item.getTransferAmount());
                                }
                            } else {
                                principalEarned = principalEarned.add(item.getTransferAmount());
                            }
                        }
                    }
                    PortfolioAccountData accountData = savingsAccountReadPlatformService.retriveSavingsLinkedAssociation(promissoryNote.getFundSavingsAccount().getId());
                    if (accountData != null) {
                        Optional <Loan> loan = loanRepository.findById(accountData.getId()).stream().findFirst();
                        if (loan.isPresent()) {
                            BigDecimal totalOutstanding = loanReadPlatformServiceCommon.getLoanTotalExpectedRepaymentDerived(loan.get().getId());

                            BigDecimal participationValue = totalOutstanding
                                    .multiply(promissoryNote.getPercentageShare())
                                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

                            pendingAmount = pendingAmount.add(participationValue);

                        }
                    }
                }

                projectData.setPrincipalEarned(principalEarned);
                projectData.setInterestsEarned(interestEarned);
                projectData.setCommissionEarned(commissionEarned);
                projectData.setPendingAmount(pendingAmount);
            }
        }
        projectsData.add(projectData);
    }
}
