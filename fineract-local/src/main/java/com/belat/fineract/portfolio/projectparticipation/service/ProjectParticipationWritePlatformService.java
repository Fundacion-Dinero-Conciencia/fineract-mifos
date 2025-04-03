package com.belat.fineract.portfolio.projectparticipation.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface ProjectParticipationWritePlatformService {

    CommandProcessingResult createProjectParticipation(JsonCommand command);

    CommandProcessingResult updateProjectParticipation(Long id, JsonCommand command);

    CommandProcessingResult deleteProjectParticipation(Long id, JsonCommand command);

}
