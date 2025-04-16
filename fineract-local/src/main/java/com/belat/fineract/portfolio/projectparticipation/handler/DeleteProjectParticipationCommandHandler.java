package com.belat.fineract.portfolio.projectparticipation.handler;

import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationWritePlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CommandType(entity = "PROJECT_PARTICIPATION", action = "DELETE")
public class DeleteProjectParticipationCommandHandler implements NewCommandSourceHandler {

    private final ProjectParticipationWritePlatformService projectParticipationWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return projectParticipationWritePlatformService.deleteProjectParticipation(command.entityId(), command);
    }

}
