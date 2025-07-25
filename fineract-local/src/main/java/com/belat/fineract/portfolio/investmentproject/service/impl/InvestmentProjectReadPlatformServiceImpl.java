package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData.ImageDocument;
import com.belat.fineract.portfolio.investmentproject.data.StatusHistoryProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategoryRepository;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import com.belat.fineract.portfolio.investmentproject.mapper.InvestmentProjectMapper;
import com.belat.fineract.portfolio.investmentproject.mapper.StatusHistoryProjectMapper;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import com.belat.fineract.useradministration.domain.AppUserRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentProjectReadPlatformServiceImpl implements InvestmentProjectReadPlatformService {

    private final InvestmentProjectRepository investmentProjectRepository;
    private final InvestmentProjectMapper investmentProjectMapper;
    private final StatusHistoryProjectMapper historyProjectMapper;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final InvestmentProjectCategoryRepository investmentProjectCategoryRepository;
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ApplicationContext applicationContext;
    private final StatusHistoryProjectRepository historyProjectRepository;
    private final AppUserRepositoryV2 appUserRepository;

    @Override
    public List<InvestmentProjectData> retrieveAll() {
        List<InvestmentProject> projects = investmentProjectRepository.findAll();
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        projects.forEach(project -> {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                factoryData(projectData, project, projectsData);
            }
        });
        return projectsData;
    }

    @Transactional
    protected void setImageData(InvestmentProjectData projectData) {
        List<ImageDocument> images = new ArrayList<>();
        List<DocumentData> documents = documentReadPlatformService.retrieveAllDocumentsByDescriptionOrder("projects", projectData.getId());
        documents.forEach(document -> {
            if (document != null) {
                ImageDocument imageDocument = new ImageDocument(document.getFileName(),
                        applicationContext.getEnvironment().getProperty("fineract.s3.images.url").concat(document.getLocation()),
                        document.getDescription());
                if ("Cover".equals(document.getDescription())) {
                    projectData.setCover(imageDocument);
                } else {
                    images.add(imageDocument);
                }
            }
        });
        projectData.setImages(images);
    }

    @Transactional
    protected List<ImageDocument> retrieveImageList(Long id) {
        List<ImageDocument> images = new ArrayList<>();
        List<DocumentData> documents = documentReadPlatformService.retrieveAllDocumentsByDescriptionOrder("projects", id);
        documents.forEach(document -> {
            if (document != null) {
                images.add(new ImageDocument(document.getFileName(),
                        applicationContext.getEnvironment().getProperty("fineract.s3.images.url").concat(document.getLocation()),
                        document.getDescription()));
            }
        });
        return images;
    }

    @Override
    public List<InvestmentProject> retrieveAllInvestmentProject() {
        return investmentProjectRepository.findAll();
    }

    @Override
    public InvestmentProjectData retrieveById(Long id) {
        InvestmentProject project = investmentProjectRepository.retrieveOneByProjectId(id);
        InvestmentProjectData projectData = investmentProjectMapper.map(project);
        if (project != null) {
            factoryData(projectData, project, new ArrayList<>());
        }
        return projectData;
    }

    @Override
    public List<InvestmentProjectData> retrieveByClientId(Long clientId) {
        List<InvestmentProject> projects = investmentProjectRepository.retrieveByClientId(clientId);
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        projects.forEach(project -> {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                factoryData(projectData, project, projectsData);
            }
        });
        return projectsData;
    }

    @Override
    public List<InvestmentProjectData> retrieveByCategoryId(Long categoryId) {
        List<InvestmentProjectCategory> categories = investmentProjectCategoryRepository.retrieveByCategoryId(categoryId);
        List<InvestmentProject> projects = new ArrayList<>();
        categories.forEach(item -> projects.add(item.getInvestmentProject()));
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        projects.forEach(project -> {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                factoryData(projectData, project, projectsData);
            }
        });
        return projectsData;
    }

    @Override
    public List<InvestmentProjectData> retrieveByName(String name) {
        List<InvestmentProject> projects = investmentProjectRepository.retrieveByName(name);
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        projects.forEach(project -> {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                factoryData(projectData, project, projectsData);
            }
        });
        return projectsData;
    }

    @Override
    public InvestmentProjectData retrieveByLinkedLoan(Long loanId) {
        InvestmentProject project = investmentProjectRepository.retrieveOneByLoanId(loanId);
        InvestmentProjectData projectData = null;
        if (project != null) {
            projectData = investmentProjectMapper.map(project);
            factoryData(projectData, project, new ArrayList<>());
            if (project.getAmountToBeFinanced() != null && project.getAmountToBeDelivered() != null) {
                projectData.setAvailableTotalAmount(project.getAmountToBeFinanced().subtract(project.getAmountToBeDelivered(), MoneyHelper.getMathContext()));
            }
        }
        return projectData;
    }

    @Override
    public List<StatusHistoryProjectData> getAllStatusHistoryByInvestmentProjectId(Long investmentId) {
        List<StatusHistoryProject> history = historyProjectRepository.getAllStatusHistoryByInvestmentProjectId(investmentId);

        return history.stream().map(historyItem -> {
            StatusHistoryProjectData dto = historyProjectMapper.map(historyItem);
            Long userId = historyItem.getCreatedBy().orElse(null);
            AppUser user = appUserRepository.findAppUserById(userId);
            if (user != null) {
                dto.setPersonInCharge(user.getFirstname().concat(" ").concat(user.getLastname()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public InvestmentProject retrieveInvestmentById(Long id) {
        return investmentProjectRepository.retrieveOneByProjectId(id);
    }

    @Override
    public List<InvestmentProjectData> retrieveFiltered() {
        List<InvestmentProject> projects = investmentProjectRepository.retrieveByPositionActiveAndStatus();
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        projects.forEach(project -> {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                factoryData(projectData, project, projectsData);
            }
        });
        return projectsData;
    }


    private void factoryData(InvestmentProjectData projectData, InvestmentProject project, List<InvestmentProjectData> projectsData) {
        setImageData(projectData);
        projectsData.add(projectData);

        BigDecimal projectParticipation = projectParticipationRepository.retrieveTotalParticipationAmountByProjectId(project.getId());
        BigDecimal projectAmount = project.getAmount();
        BigDecimal occupancyPercentage = projectParticipation.multiply(BigDecimal.valueOf(100)).divide(projectAmount, 2,
                RoundingMode.HALF_UP);

        projectData.setOccupancyPercentage(occupancyPercentage);

        BigDecimal remainingPercentage = BigDecimal.valueOf(100).subtract(occupancyPercentage);
        BigDecimal remainingAmount = projectAmount.multiply(remainingPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        projectData.setAvailableTotalAmount(remainingAmount);

    }

}
