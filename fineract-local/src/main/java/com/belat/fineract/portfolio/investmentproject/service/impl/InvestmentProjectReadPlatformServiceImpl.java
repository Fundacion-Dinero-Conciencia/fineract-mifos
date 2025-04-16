package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData.DataCode;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData.ImageDocument;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategoryRepository;
import com.belat.fineract.portfolio.investmentproject.mapper.InvestmentProjectMapper;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentProjectReadPlatformServiceImpl implements InvestmentProjectReadPlatformService {

    private final InvestmentProjectRepository investmentProjectRepository;
    private final InvestmentProjectMapper investmentProjectMapper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final InvestmentProjectCategoryRepository investmentProjectCategoryRepository;
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ApplicationContext applicationContext;

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
    private List<ImageDocument> retrieveList(Long id) {
        List<ImageDocument> images = new ArrayList<>();
        List<DocumentData> documents = documentReadPlatformService.retrieveAllDocuments("projects", id);
        documents.forEach(document -> {
            if (document != null) {
                images.add(new ImageDocument(document.getFileName(),
                        applicationContext.getEnvironment().getProperty("fineract.s3.images.url").concat(document.getLocation())));
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
            projectData.setImpactDescription(project.getDescription().getImpactDescription());
            projectData.setInstitutionDescription(project.getDescription().getInstitutionDescription());
            projectData.setTeamDescription(project.getDescription().getTeamDescription());
            projectData.setFinancingDescription(project.getDescription().getFinancingDescription());
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
                projectData.setImpactDescription(project.getDescription().getImpactDescription());
                projectData.setInstitutionDescription(project.getDescription().getInstitutionDescription());
                projectData.setTeamDescription(project.getDescription().getTeamDescription());
                projectData.setFinancingDescription(project.getDescription().getFinancingDescription());
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
                projectData.setImpactDescription(project.getDescription().getImpactDescription());
                projectData.setInstitutionDescription(project.getDescription().getInstitutionDescription());
                projectData.setTeamDescription(project.getDescription().getTeamDescription());
                projectData.setFinancingDescription(project.getDescription().getFinancingDescription());
            }
        });
        return projectsData;
    }

    private void factoryData(InvestmentProjectData projectData, InvestmentProject project, List<InvestmentProjectData> projectsData) {
        projectData.setOwnerId(project.getOwner().getId());
        projectData.setOwnerName(project.getOwner().getDisplayName());
        projectData.setIsActive(project.isActive());
        projectData.setPeriod(project.getPeriod());
        DataCode country = new DataCode(project.getCountry().getId(), project.getCountry().getLabel(),
                project.getCountry().getDescription());
        projectData.setCountry(country);
        projectData.setImages(retrieveList(project.getId()));
        projectsData.add(projectData);

        BigDecimal projectParticipation = projectParticipationRepository.retrieveTotalParticipationAmountByProjectId(project.getId());
        BigDecimal projectAmount = project.getAmount();
        BigDecimal occupancyPercentage = projectParticipation.multiply(BigDecimal.valueOf(100)).divide(projectAmount, 2,
                RoundingMode.HALF_UP);

        projectData.setOccupancyPercentage(occupancyPercentage);

        BigDecimal remainingPercentage = BigDecimal.valueOf(100).subtract(occupancyPercentage);
        BigDecimal remainingAmount = projectAmount.multiply(remainingPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        projectData.setMaxAmount(remainingAmount);

        List<DataCode> categories = new ArrayList<>();
        project.getSubCategories().forEach(item -> {
            if (item != null) {
                categories
                        .add(new DataCode(item.getCategory().getId(), item.getCategory().getLabel(), item.getCategory().getDescription()));
            }
        });
        projectData.setSubCategories(categories);
        if (project.getLoan() != null) {
            projectData.setLoanId(project.getLoan().getId());
        }
        if (project.getCategory() != null) {
            projectData.setCategory(
                    new DataCode(project.getCategory().getId(), project.getCategory().getLabel(), project.getCategory().getDescription()));
        }
        if (project.getArea() != null) {
            projectData.setArea(new DataCode(project.getArea().getId(), project.getArea().getLabel(), project.getArea().getDescription()));
        }
    }

}
