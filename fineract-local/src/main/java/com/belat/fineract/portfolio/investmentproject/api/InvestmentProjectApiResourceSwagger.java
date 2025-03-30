package com.belat.fineract.portfolio.investmentproject.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class InvestmentProjectApiResourceSwagger {

    private InvestmentProjectApiResourceSwagger() {}

    @Schema(description = "PostAddInvestmentProjectRequest")
    static final class PostAddInvestmentProjectRequest {

        private PostAddInvestmentProjectRequest() {}

        @Schema(example = "Name")
        public String name;

        @Schema(example = "1")
        public String ownerId;

        @Schema(example = "1000.00")
        public BigDecimal amount;

        @Schema(example = "USD")
        public String currencyCode;

        @Schema(example = "Example description")
        public String description;

        @Schema(example = "11.1")
        public BigDecimal rate;

    }

    @Schema(description = "PostAddInvestmentProjectResponse")
    static final class PostAddInvestmentProjectResponse {

        private PostAddInvestmentProjectResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetInvestmentProjectResponse")
    static final class GetInvestmentProjectResponse {

        private GetInvestmentProjectResponse() {}

        @Schema(example = "1")
        public Long id;

        @Schema(example = "1")
        public String ownerId;

        @Schema(example = "Name")
        public String name;

        @Schema(example = "1000.00")
        public BigDecimal amount;

        @Schema(example = "USD")
        public String currencyCode;

        @Schema(example = "Example description")
        public String description;

        @Schema(example = "11.1")
        public BigDecimal rate;

    }

}
