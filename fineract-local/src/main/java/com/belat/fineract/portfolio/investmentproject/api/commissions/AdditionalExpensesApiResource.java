package com.belat.fineract.portfolio.investmentproject.api.commissions;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesReadPlatformService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Path("/v1/additionalExpenses")
@Component
@Tag(name = "additionalExpenses", description = "additionalExpenses")
@RequiredArgsConstructor
public class AdditionalExpensesApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<InvestmentProjectData> apiJsonSerializerService;
    private final AdditionalExpensesReadPlatformService additionalExpensesReadPlatformService;

//    @POST
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class)))
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class))),
//            @ApiResponse(responseCode = "403", description = "Project can not be created") })
//    public String addAdditionalExpenses(@Parameter(hidden = true) final AdditionalExpensesData data) {
//        platformUserRightsContext.isAuthenticated();
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiJsonSerializerService.serializeResult(data)).createAdditionalExpenses().build();
//        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//        return apiJsonSerializerService.serialize(result);
//    }


    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String updateAdditionalExpenses(@Parameter(hidden = true) final AdditionalExpensesData data) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiJsonSerializerService.serializeResult(data)).updateAdditionalExpenses().build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.GetAdditionalExpenseByIdResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public String getAdditionalExpenseById(@PathParam("id") Long id) {
        platformUserRightsContext.isAuthenticated();
        AdditionalExpensesData data = additionalExpensesReadPlatformService.getAdditionalExpensesDataById(id);
        return apiJsonSerializerService.serialize(data);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.GetAdditionalExpensesByProjectIdResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing projectId parameter")
    })
    public String getAdditionalExpensesByProjectId(@QueryParam("projectId") Long projectId) {
        platformUserRightsContext.isAuthenticated();
        if (projectId == null) {
            throw new IllegalArgumentException("projectId query param is required");
        }
        List<AdditionalExpensesData> list = this.additionalExpensesReadPlatformService.getAdditionalExpenses(projectId);
        return apiJsonSerializerService.serialize(list);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AdditionalExpensesApiResourceSwagger.PostAdditionalExpenseResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String addAdditionalExpenses(@Parameter(hidden = true) final List<AdditionalExpensesData> data) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiJsonSerializerService.serializeResult(data)).createAdditionalExpenses().build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

}
