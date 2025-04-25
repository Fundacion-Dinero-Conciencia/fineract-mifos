package com.belat.fineract.portfolio.investmentproject.api;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.StatusHistoryProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.springframework.stereotype.Component;

@Path("/v1/investmentproject")
@Component
@Tag(name = "investmentproject", description = "investmentproject")
@RequiredArgsConstructor
public class InvestmentProjectApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<InvestmentProjectData> apiJsonSerializerService;
    private final InvestmentProjectReadPlatformService investmentProjectReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.PostAddInvestmentProjectRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.PostAddInvestmentProjectResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String addInvestmentProject(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).createInvestmentProject().build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("/all")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.GetInvestmentProjectResponse.class))) })
    public String getAllInvestmentProjects() {
        platformUserRightsContext.isAuthenticated();
        final List<InvestmentProjectData> projects = investmentProjectReadPlatformService.retrieveAll();
        return apiJsonSerializerService.serialize(projects);
    }


    @GET
    @Path("/search")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.GetInvestmentProjectResponse.class))) })
    public String getInvestmentProjects(@QueryParam("id") final Long id, @QueryParam("ownerId") final Long ownerId,
            @QueryParam("categoryId") final Long categoryId) {
        platformUserRightsContext.isAuthenticated();

        if (id != null) {
            final InvestmentProjectData projectData = investmentProjectReadPlatformService.retrieveById(id);
            return apiJsonSerializerService.serialize(projectData);
        } else if (ownerId != null) {
            final List<InvestmentProjectData> projectsData = investmentProjectReadPlatformService.retrieveByClientId(ownerId);
            return apiJsonSerializerService.serialize(projectsData);
        } else if (categoryId != null) {
            final List<InvestmentProjectData> projectsData = investmentProjectReadPlatformService.retrieveByCategoryId(categoryId);
            return apiJsonSerializerService.serialize(projectsData);
        } else {
            throw new IllegalArgumentException("Not supported parameter");
        }
    }

    @GET
    @Path("/historyStatus")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.GetInvestmentProjectResponse.class))) })
    public String getHistoryStatusInvestmentProjects(@QueryParam("id") final Long id) {
        platformUserRightsContext.isAuthenticated();

        if (id != null) {
            final List<StatusHistoryProjectData> history = this.investmentProjectReadPlatformService.getAllStatusHistoryByInvestmentProjectId(id);
            return apiJsonSerializerService.serialize(history);
        } else {
            throw new IllegalArgumentException("Not supported parameter");
        }
    }

    @PUT
    @Path("{projectId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.PutInvestmentProjectRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InvestmentProjectApiResourceSwagger.PutInvestmentProjectResponse.class))) })
    public String update(@PathParam("projectId") @Parameter(description = "projectId") final Long projectId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateInvestmentProject(projectId)
                .build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

}
