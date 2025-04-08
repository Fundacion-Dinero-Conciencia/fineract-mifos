package com.belat.fineract.portfolio.questionsanswers.service.impl;

import com.belat.fineract.portfolio.questionsanswers.api.QuestionAnswerConstants;
import com.belat.fineract.portfolio.questionsanswers.domain.answer.BelatAnswer;
import com.belat.fineract.portfolio.questionsanswers.domain.answer.BelatAnswerRepository;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestion;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestionRepository;
import com.belat.fineract.portfolio.questionsanswers.exception.QuestionNotFoundException;
import com.belat.fineract.portfolio.questionsanswers.service.QuestionAnswerWritePlatformService;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class QuestionAnswerWritePlatformServiceImpl implements QuestionAnswerWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final BelatQuestionRepository belatQuestionRepository;
    private final BelatAnswerRepository belatAnswerRepository;
    private final ClientRepository clientRepository;
    
    @Override
    public CommandProcessingResult createQuestion(JsonCommand command) {
        this.validateForCreateQuestion(command.json());
        BelatQuestion question = new BelatQuestion();

        final String userIdParam = command.stringValueOfParameterNamed(QuestionAnswerConstants.userIdParamName);
        Client user = clientRepository.findById(Long.valueOf(userIdParam)).orElseThrow( () -> new ClientNotFoundException(Long.valueOf(userIdParam)));
        if (!user.isActive()) {
            throw new ClientNotActiveException(user.getId());
        }

        question.setTitle(command.stringValueOfParameterNamed(QuestionAnswerConstants.questionTitleParamName));
        question.setQuestion(command.stringValueOfParameterNamed(QuestionAnswerConstants.questionParamName));
        question.setUser(user);

        belatQuestionRepository.saveAndFlush(question);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(question.getId()).build();
    }

    @Override
    public CommandProcessingResult createAnswer(Long questionId, JsonCommand command) {
        this.validateForCreateAnswer(command.json());
        BelatAnswer belatAnswer = new BelatAnswer();

        final String userIdParam = command.stringValueOfParameterNamed(QuestionAnswerConstants.userIdParamName);
        Client user = clientRepository.findById(Long.valueOf(userIdParam)).orElseThrow( () -> new ClientNotFoundException(Long.valueOf(userIdParam)));
        if (!user.isActive()) {
            throw new ClientNotActiveException(user.getId());
        }

        BelatQuestion question = belatQuestionRepository.findById(questionId).orElseThrow( () -> new QuestionNotFoundException(questionId));

        belatAnswer.setQuestion(question);
        belatAnswer.setAnswer(command.stringValueOfParameterNamed(QuestionAnswerConstants.answerParamName));
        belatAnswer.setUser(user);

        belatAnswerRepository.saveAndFlush(belatAnswer);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(question.getId()).build();
    }

    private void validateForCreateQuestion (final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                QuestionAnswerConstants.QUESTION_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("questionAnswers");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String questionTitleParam = fromApiJsonHelper.extractStringNamed(QuestionAnswerConstants.questionTitleParamName, jsonElement);
        baseDataValidator.reset().parameter(QuestionAnswerConstants.questionTitleParamName).value(questionTitleParam).notBlank().notNull();

        final String questionParam = fromApiJsonHelper.extractStringNamed(QuestionAnswerConstants.questionParamName, jsonElement);
        baseDataValidator.reset().parameter(QuestionAnswerConstants.questionParamName).value(questionParam).notBlank().notNull();

        final String userIdParam = fromApiJsonHelper.extractStringNamed(QuestionAnswerConstants.userIdParamName, jsonElement);
        baseDataValidator.reset().parameter(QuestionAnswerConstants.userIdParamName).value(userIdParam).notBlank().notNull();
        
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    private void validateForCreateAnswer (final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                QuestionAnswerConstants.ANSWER_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("questionAnswers");
        final JsonElement jsonElement = fromApiJsonHelper.parse(json);

        final String answerParam = fromApiJsonHelper.extractStringNamed(QuestionAnswerConstants.answerParamName, jsonElement);
        baseDataValidator.reset().parameter(QuestionAnswerConstants.answerParamName).value(answerParam).notBlank().notNull();

        final String userIdParam = fromApiJsonHelper.extractStringNamed(QuestionAnswerConstants.userIdParamName, jsonElement);
        baseDataValidator.reset().parameter(QuestionAnswerConstants.userIdParamName).value(userIdParam).notBlank().notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }
    
    
}
