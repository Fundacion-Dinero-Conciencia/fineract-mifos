package com.belat.fineract.portfolio.projectparticipation.service;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationOdsAreaData;

import java.util.List;

public interface ProjectParticipationReadPlatformService {

    List<ProjectParticipationData> retrieveAll();

    ProjectParticipationData retrieveById(Long id);

    List<ProjectParticipationData> retrieveByClientId(Long clientId, Integer statusCode, Integer page, Integer size);

    List<ProjectParticipationOdsAreaData> retrieveOdsAndAreaByClientId(Long clientId, Integer statusCode);

    List<ProjectParticipationData> retrieveByProjectId(Long categoryId, Integer statusCode, Integer page, Integer size);

}
