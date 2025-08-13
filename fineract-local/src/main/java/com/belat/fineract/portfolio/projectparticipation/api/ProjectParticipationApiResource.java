package com.belat.fineract.portfolio.projectparticipation.api;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.service.ProjectParticipationReadPlatformService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
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

@Path("/v1/projectparticipation")
@Component
@Tag(name = "projectparticipation", description = "projectparticipation")
@RequiredArgsConstructor
public class ProjectParticipationApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProjectParticipationData> apiJsonSerializerService;
    private final ProjectParticipationReadPlatformService projectParticipationReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.PostAddProjectParticipationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.PostAddProjectParticipationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String addProjectParticipation(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).createProjectParticipation()
                .build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("/all")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.GetProjectParticipationResponse.class))) })
    public String getAllProjectsParticipation() {
        platformUserRightsContext.isAuthenticated();
        final List<ProjectParticipationData> projectParticipationData = projectParticipationReadPlatformService.retrieveAll();
        return apiJsonSerializerService.serialize(projectParticipationData);
    }

    @GET
    @Path("/search")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.GetProjectParticipationResponse.class))) })
    public String getProjectParticipation(
            @QueryParam("id") final Long id,
            @QueryParam("participantId") final Long participantId,
            @QueryParam("projectId") final Long projectId,
            @QueryParam("statusCode") final Integer statusCode,
            @QueryParam("page") final Integer page,
            @QueryParam("size") final Integer size) {

        platformUserRightsContext.isAuthenticated();

        if (id != null) {
            final ProjectParticipationData projectParticipationData = projectParticipationReadPlatformService.retrieveById(id);
            return apiJsonSerializerService.serialize(projectParticipationData);
        } else if (participantId != null) {
            final List<ProjectParticipationData> projectParticipationData = projectParticipationReadPlatformService
                    .retrieveByClientId(participantId, statusCode, page, size);
            return apiJsonSerializerService.serialize(projectParticipationData);
        } else if (projectId != null) {
            final List<ProjectParticipationData> projectParticipationData = projectParticipationReadPlatformService
                    .retrieveByProjectId(projectId, statusCode, page, size);
            return apiJsonSerializerService.serialize(projectParticipationData);
        } else {
            throw new IllegalArgumentException("Not supported parameter");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.PutProjectParticipationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectParticipationApiResourceSwagger.PutProjectParticipationResponse.class))) })
    public String update(@PathParam("id") @Parameter(description = "id") final Long id,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateProjectParticipation(id)
                .build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

}
