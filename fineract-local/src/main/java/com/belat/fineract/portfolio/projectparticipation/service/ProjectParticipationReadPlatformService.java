package com.belat.fineract.portfolio.projectparticipation.service;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import java.util.List;

public interface ProjectParticipationReadPlatformService {

    List<ProjectParticipationData> retrieveAll();

    ProjectParticipationData retrieveById(Long id);

    List<ProjectParticipationData> retrieveByClientId(Long clientId, int page, int size);

    List<ProjectParticipationData> retrieveByProjectId(Long categoryId, int page, int size);

}
