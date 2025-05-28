package com.belat.fineract.portfolio.pdfgenerator.api;

import com.belat.fineract.portfolio.pdfgenerator.service.PdfGeneratorWritePlatformService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.springframework.stereotype.Component;

@Path("/v1/generatepdf")
@Component
@Tag(name = "generatepdf", description = "generatepdf")
@RequiredArgsConstructor
public class PdfGeneratorApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PdfGeneratorWritePlatformService pdfGeneratorWritePlatformService;

    @POST
    @Path("fundmandate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = PdfGeneratorApiResourceSwagger.PostGeneratePdfRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PdfGeneratorApiResourceSwagger.PostGeneratePdfResponse.class))),
            @ApiResponse(responseCode = "403", description = "Pdf could not be created") })
    public String generateFundMandatePdf(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        return pdfGeneratorWritePlatformService.generateFundMandateV1(apiRequestBodyAsJson);
    }

    @POST
    @Path("retailmandate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = PdfGeneratorApiResourceSwagger.PostGeneratePdfRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PdfGeneratorApiResourceSwagger.PostGeneratePdfResponse.class))),
            @ApiResponse(responseCode = "403", description = "Pdf could not be created") })
    public String generateRetailMandatePdf(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        return pdfGeneratorWritePlatformService.generateRetailMandateV1(apiRequestBodyAsJson);
    }

}
