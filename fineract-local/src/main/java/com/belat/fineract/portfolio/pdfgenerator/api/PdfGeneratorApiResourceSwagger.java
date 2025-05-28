package com.belat.fineract.portfolio.pdfgenerator.api;

import io.swagger.v3.oas.annotations.media.Schema;

public class PdfGeneratorApiResourceSwagger {

    private PdfGeneratorApiResourceSwagger() {}

    @Schema(description = "PostGeneratePdfRequest")
    static final class PostGeneratePdfRequest {

        private PostGeneratePdfRequest() {}

        @Schema(example = "1")
        public Long projectId;

    }

    @Schema(description = "PostGeneratePdfResponse")
    static final class PostGeneratePdfResponse {

        private PostGeneratePdfResponse() {}

        @Schema(example = "g64")
        public String base64;
    }

}
