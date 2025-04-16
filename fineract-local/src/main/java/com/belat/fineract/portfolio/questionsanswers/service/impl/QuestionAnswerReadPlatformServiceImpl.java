package com.belat.fineract.portfolio.questionsanswers.service.impl;

import com.belat.fineract.portfolio.questionsanswers.data.QuestionData;
import com.belat.fineract.portfolio.questionsanswers.domain.answer.BelatAnswer;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestion;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestionRepository;
import com.belat.fineract.portfolio.questionsanswers.mapper.QuestionMapper;
import com.belat.fineract.portfolio.questionsanswers.service.QuestionAnswerReadPlatformService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.client.domain.Client;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class QuestionAnswerReadPlatformServiceImpl implements QuestionAnswerReadPlatformService {

    private final BelatQuestionRepository belatQuestionRepository;
    private final QuestionMapper questionMapper;

    @Override
    public List<QuestionData> retrieveAllQuestions() {
        List<BelatQuestion> questions = belatQuestionRepository.findAll();
        List<QuestionData> questionsData = new ArrayList<>();

        questions.forEach(question -> {
            if (question != null) {
                QuestionData questionData = questionMapper.map(question);
                factoryData(questionData, question.getBelatAnswers(), question.getUser(), questionsData);
            }
        });
        return questionsData;
    }

    @Override
    public QuestionData retrieveQuestionById(Long id) {
        BelatQuestion question = belatQuestionRepository.retrieveOneByQuestionId(id);
        QuestionData questionData = questionMapper.map(question);
        if (question != null) {
            factoryData(questionData, question.getBelatAnswers(), question.getUser(), new ArrayList<>());
        }
        return questionData;
    }

    @Override
    public List<QuestionData> retrieveQuestionByUserId(Long id) {
        List<BelatQuestion> questions = belatQuestionRepository.retrieveByUserId(id);
        List<QuestionData> questionsData = new ArrayList<>();

        questions.forEach(question -> {
            if (question != null) {
                QuestionData questionData = questionMapper.map(question);
                factoryData(questionData, question.getBelatAnswers(), question.getUser(), questionsData);
            }
        });
        return questionsData;
    }

    private void factoryData(QuestionData questionData, List<BelatAnswer> belatAnswers, Client questionClient,
            List<QuestionData> questionsData) {
        QuestionData.User questionUser = new QuestionData.User(questionClient.getId(), questionClient.getDisplayName());
        questionData.setUser(questionUser);

        List<QuestionData.Answer> answersQuestion = new ArrayList<>();
        belatAnswers.stream().forEach(item -> {
            QuestionData.Answer answer = new QuestionData.Answer(item.getId(), item.getAnswer(),
                    new QuestionData.User(item.getUser().getId(), item.getUser().getDisplayName()));
            answersQuestion.add(answer);
        });
        questionData.setAnswers(answersQuestion);

        questionsData.add(questionData);
    }
}
