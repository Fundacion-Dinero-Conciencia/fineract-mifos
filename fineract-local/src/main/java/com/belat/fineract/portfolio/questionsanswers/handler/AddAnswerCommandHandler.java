package com.belat.fineract.portfolio.questionsanswers.handler;

import com.belat.fineract.portfolio.questionsanswers.service.QuestionAnswerWritePlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CommandType(entity = "ANSWER", action = "CREATE")
public class AddAnswerCommandHandler implements NewCommandSourceHandler {

    private final QuestionAnswerWritePlatformService questionAnswerWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return questionAnswerWritePlatformService.createAnswer(command.entityId(), command);
    }
}
