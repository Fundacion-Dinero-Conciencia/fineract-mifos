package com.belat.fineract.portfolio.investmentproject.api.commissions;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

public class AdditionalExpensesApiResourceSwagger {

    @Schema(description = "GetAdditionalExpenseResponse")
    static final class GetAdditionalExpenseResponse {

        private GetAdditionalExpenseResponse() {}

        @Schema(example = "1")
        public Long id;

        @Schema(example = "Consulting fee")
        public String name;

        @Schema(example = "1500.00")
        public BigDecimal netAmount;

        @Schema(example = "285.00", description = "Value Added Tax (VAT)")
        public BigDecimal vat;
    }

    @Schema(description = "PostAdditionalExpenseRequest")
    static final class PostAdditionalExpenseRequest {

        private PostAdditionalExpenseRequest() {}

        @Schema(example = "1", description = "ID of the related Investment Project")
        public Long projectId;

        @Schema(example = "Consulting fee")
        public String name;

        @Schema(example = "1500.00")
        public BigDecimal netAmount;

        @Schema(example = "285.00", description = "Value Added Tax (VAT)")
        public BigDecimal vat;
    }

    @Schema(description = "GetAdditionalExpenseByIdResponse")
    static final class GetAdditionalExpenseByIdResponse {

        private GetAdditionalExpenseByIdResponse() {}

        @Schema(example = "1")
        public Long id;

        @Schema(example = "Consulting fee")
        public String name;

        @Schema(example = "1500.00")
        public BigDecimal netAmount;

        @Schema(example = "285.00", description = "Value Added Tax (VAT)")
        public BigDecimal vat;
    }

    @Schema(description = "GetAdditionalExpensesByProjectIdResponse")
    static final class GetAdditionalExpensesByProjectIdResponse {

        private GetAdditionalExpensesByProjectIdResponse() {}

        @Schema(description = "List of expenses")
        public List<GetAdditionalExpenseByIdResponse> expenses;
    }



    @Schema(description = "PostAdditionalExpenseResponse")
    static final class PostAdditionalExpenseResponse {

        private PostAdditionalExpenseResponse() {}

        @Schema(example = "10")
        public Long resourceId;
    }

}
