package com.belat.fineract.portfolio.questionsanswers.api;

import com.belat.fineract.portfolio.questionsanswers.domain.answer.BelatAnswer;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestion;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class QuestionAnswerConstants {

    // Questions
    public static final String questionTitleParamName = "questionTitle";
    public static final String questionParamName = "question";

    // Answers
    public static final String questionIdParamName = "questionId";
    public static final String answerParamName = "answer";

    public static final String userIdParamName = "userId";

    /**
     * These parameters will match the class level parameters of {@link BelatQuestion}. Where possible, we try to get
     * response parameters to match those of request parameters.
     */
    public static final Set<String> QUESTION_PARAMETERS = new HashSet<>(
            Arrays.asList(questionTitleParamName, questionParamName, userIdParamName));

    /**
     * These parameters will match the class level parameters of {@link BelatAnswer}. Where possible, we try to get
     * response parameters to match those of request parameters.
     */
    public static final Set<String> ANSWER_PARAMETERS = new HashSet<>(Arrays.asList(questionIdParamName, answerParamName, userIdParamName));

}
