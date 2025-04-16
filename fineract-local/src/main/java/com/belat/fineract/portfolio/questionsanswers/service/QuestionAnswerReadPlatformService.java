package com.belat.fineract.portfolio.questionsanswers.service;

import com.belat.fineract.portfolio.questionsanswers.data.QuestionData;
import java.util.List;

public interface QuestionAnswerReadPlatformService {

    List<QuestionData> retrieveAllQuestions();

    QuestionData retrieveQuestionById(Long id);

    List<QuestionData> retrieveQuestionByUserId(Long id);
}
