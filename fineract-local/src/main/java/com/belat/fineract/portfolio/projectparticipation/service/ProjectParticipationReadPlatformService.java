package com.belat.fineract.portfolio.projectparticipation.service;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import java.util.List;

public interface ProjectParticipationReadPlatformService {

    List<ProjectParticipationData> retrieveAll();

    ProjectParticipationData retrieveById(Long id);

    List<ProjectParticipationData> retrieveByClientId(Long clientId);

    List<ProjectParticipationData> retrieveByProjectId(Long categoryId);

}
