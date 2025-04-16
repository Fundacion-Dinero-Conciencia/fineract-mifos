package com.belat.fineract.portfolio.questionsanswers.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class QuestionAnswerApiResourceSwagger {

    private QuestionAnswerApiResourceSwagger() {}

    @Schema(description = "PostAddQuestionRequest")
    static final class PostAddQuestionRequest {

        private PostAddQuestionRequest() {}

        @Schema(example = "Title")
        public String title;

        @Schema(example = "Example")
        public String question;

        @Schema(example = "1")
        public Long userId;
    }

    @Schema(description = "PostAddQuestionResponse")
    static final class PostAddQuestionResponse {

        private PostAddQuestionResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PostAddAnswerRequest")
    static final class PostAddAnswerRequest {

        private PostAddAnswerRequest() {}

        @Schema(example = "Example")
        public Long questionId;

        @Schema(example = "Example")
        public String answer;

        @Schema(example = "1")
        public Long userId;
    }

    @Schema(description = "PostAddAnswerResponse")
    static final class PostAddAnswerResponse {

        private PostAddAnswerResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetQuestionResponse")
    static final class GetQuestionResponse {

        private GetQuestionResponse() {}

        @Schema(example = "1")
        public Long id;

        @Schema(example = "Title")
        public String title;

        @Schema(example = "Example")
        public String question;

        private static class User {

            private User() {}

            @Schema(example = "1")
            private Long id;
            @Schema(example = "username")
            private Long username;
        }

        public User user;

        private static class Answer {

            @Schema(example = "1")
            private Long id;
            @Schema(example = "answer")
            private String answer;
            private User user;
        }

        public List<Answer> answers;
    }

}
