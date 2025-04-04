package com.belat.fineract.portfolio.investmentproject.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public class InvestmentProjectApiResourceSwagger {

    private InvestmentProjectApiResourceSwagger() {}

    @Schema(description = "PostAddInvestmentProjectRequest")
    static final class PostAddInvestmentProjectRequest {

        private PostAddInvestmentProjectRequest() {}

        @Schema(example = "Name")
        public String name;

        @Schema(example = "Example")
        public String subtitle;

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

        private static final class GetCategory {

            private GetCategory() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "201")
            public String code;
            @Schema(example = "Example")
            public String description;
        }

        public List<GetCategory> categories;

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

        @Schema(example = "Example")
        public String subtitle;

        @Schema(example = "1000.00")
        public BigDecimal amount;

        @Schema(example = "USD")
        public String currencyCode;

        @Schema(example = "11.1")
        public BigDecimal rate;

        @Schema(example = "11.1")
        public Integer period;

        @Schema(example = "Description")
        public String impactDescription;

        @Schema(example = "Description")
        public String institutionDescription;

        @Schema(example = "Description")
        public String teamDescription;

        @Schema(example = "Description")
        public String financingDescription;

        @Schema(example = "true")
        public Boolean isActive;

        @Schema(example = "5.9999999999")
        public BigDecimal occupancyPercentage;

        static final class GetCountry {

            private GetCountry() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "201")
            public String code;
            @Schema(example = "Example")
            public String description;
        }
        public GetCountry country;

        static final class GetImage {

            private GetImage() {}

            @Schema(example = "1")
            public Integer name;
            @Schema(example = "201")
            public String url;
        }

        public List<GetImage> images;

        private static final class GetCategory {

            private GetCategory() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "201")
            public String code;
            @Schema(example = "Example")
            public String description;
        }

        public List<GetCategory> categories;

    }

    @Schema(description = "PutInvestmentProjectRequest")
    public static final class PutInvestmentProjectRequest {

        private PutInvestmentProjectRequest() {}
        @Schema(example = "Name")
        public String name;

        @Schema(example = "Example")
        public String subtitle;

        @Schema(example = "Description")
        public String impactDescription;

        @Schema(example = "Description")
        public String institutionDescription;

        @Schema(example = "Description")
        public String teamDescription;

        @Schema(example = "Description")
        public String financingDescription;

        @Schema(example = "Description")
        public Boolean isActive;

        @Schema(example = "1")
        public Integer period;

        @Schema(example = "5.9999999999")
        public BigDecimal rate;

        private static final class GetCategory {

            private GetCategory() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "201")
            public String code;
            @Schema(example = "Example")
            public String description;
        }

        public List<GetCategory> categories;
    }

    @Schema(description = "PutInvestmentProjectResponse")
    public static final class PutInvestmentProjectResponse {

        private PutInvestmentProjectResponse() {}

        static final class PutInvestmentProjectChanges {

            private PutInvestmentProjectChanges() {}

            @Schema(example = "Name")
            public String name;

            @Schema(example = "Description")
            public String description;

            @Schema(example = "5.9999999999")
            public BigDecimal rate;
        }

        @Schema(example = "2")
        public PutInvestmentProjectChanges changes;
    }


}
