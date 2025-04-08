package com.belat.fineract.portfolio.questionsanswers.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionData {

    private Long id;
    private String title;
    private String question;
    private User user;
    private List<Answer> answers;

    @Data
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String username;
    }

    @Data
    @AllArgsConstructor
    public static class Answer {
        private Long id;
        private String answer;
        private User user;
    }

}
