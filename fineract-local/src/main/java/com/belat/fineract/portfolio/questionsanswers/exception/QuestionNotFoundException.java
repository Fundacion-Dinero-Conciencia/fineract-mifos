package com.belat.fineract.portfolio.questionsanswers.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class QuestionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public QuestionNotFoundException(final Long questionId) {
        super("error.msg.question.not.found", "BelatQuestion with id " + questionId + " does not exist", questionId);
    }
}
