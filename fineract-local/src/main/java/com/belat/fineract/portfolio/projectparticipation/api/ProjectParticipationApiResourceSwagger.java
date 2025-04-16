package com.belat.fineract.portfolio.projectparticipation.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public class ProjectParticipationApiResourceSwagger {

    private ProjectParticipationApiResourceSwagger() {}

    @Schema(description = "PostAddProjectParticipationRequest")
    static final class PostAddProjectParticipationRequest {

        private PostAddProjectParticipationRequest() {}

        @Schema(example = "1")
        public Long participantId;

        @Schema(example = "1")
        public Long projectId;

        @Schema(example = "1000.00")
        public BigDecimal amount;

        @Schema(example = "USD")
        public Integer status;

        @Schema(example = "Type")
        public String type;

    }

    @Schema(description = "PostAddProjectParticipationResponse")
    static final class PostAddProjectParticipationResponse {

        private PostAddProjectParticipationResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetProjectParticipationResponse")
    static final class GetProjectParticipationResponse {

        private GetProjectParticipationResponse() {}

        @Schema(example = "1")
        public Long id;

        @Schema(example = "1")
        public Long participantId;

        @Schema(example = "Name")
        public Long projectId;

        @Schema(example = "1000.00")
        public BigDecimal amount;

        private static final class GetStatusEnum {

            private GetStatusEnum() {}

            @Schema(example = "Code")
            public BigDecimal code;

            @Schema(example = "Value")
            public Integer value;

        }

        public GetStatusEnum status;

        @Schema(example = "Type")
        public String type;

    }

    @Schema(description = "PutProjectParticipationRequest")
    public static final class PutProjectParticipationRequest {

        private PutProjectParticipationRequest() {}

        @Schema(example = "1000.11")
        public BigDecimal amount;

        @Schema(example = "100")
        public Integer status;

    }

    @Schema(description = "PutProjectParticipationResponse")
    public static final class PutProjectParticipationResponse {

        private PutProjectParticipationResponse() {}

        static final class PutProjectParticipationChanges {

            private PutProjectParticipationChanges() {}

            @Schema(example = "1000.11")
            public String amount;

            @Schema(example = "Description")
            public Integer status;

        }

        @Schema(example = "2")
        public PutProjectParticipationChanges changes;
    }

}
