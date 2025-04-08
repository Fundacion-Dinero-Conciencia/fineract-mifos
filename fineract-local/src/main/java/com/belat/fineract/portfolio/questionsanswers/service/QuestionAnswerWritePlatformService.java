package com.belat.fineract.portfolio.questionsanswers.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface QuestionAnswerWritePlatformService {

    CommandProcessingResult createQuestion(JsonCommand command);

    CommandProcessingResult createAnswer(Long questionId, JsonCommand command);

}
