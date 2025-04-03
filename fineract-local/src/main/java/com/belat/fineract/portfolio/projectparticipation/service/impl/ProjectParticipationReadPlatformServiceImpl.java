package com.belat.fineract.portfolio.projectparticipation.service.impl;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationStatusEnum;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipationRepository;
import com.belat.fineract.portfolio.projectparticipation.mapper.ProjectParticipationMapper;
import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationReadPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectParticipationReadPlatformServiceImpl implements ProjectParticipationReadPlatformService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectParticipationMapper projectParticipationMapper;


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
        projectData.setProjectId(project.getInvestmentProject().getId());
        ProjectParticipationData.StatusEnum statusEnum = new ProjectParticipationData.StatusEnum(ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).toEnumOptionData().getCode(), ProjectParticipationStatusEnum.fromInt(project.getStatusEnum()).getValue());
        projectData.setStatus(statusEnum);
        projectsData.add(projectData);
    }
}
