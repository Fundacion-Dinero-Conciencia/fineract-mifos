package com.belat.fineract.portfolio.investmentproject.api;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.StatusHistoryProjectData;
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

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.api.ImagesApiResource;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageResizer;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
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
    private final ImageReadPlatformService imageReadPlatformService;
    private final ImageResizer imageResizer;

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
            @QueryParam("categoryId") final Long categoryId, @QueryParam("name") final String name,  @QueryParam("maxWidth") final Integer maxWidth, @QueryParam("maxHeight") final Integer maxHeight) {
        platformUserRightsContext.isAuthenticated();

        if (id != null) {
            final InvestmentProjectData projectData = investmentProjectReadPlatformService.retrieveById(id);
            try {
                // fetch client image
                final FileData imageData = imageReadPlatformService.retrieveImage(ImagesApiResource.EntityTypeForImages.CLIENTS.toString(), projectData.getOwnerId());
                final FileData resizedImage = imageResizer.resize(imageData, maxWidth, maxHeight);
                // Else return response with Base64 encoded
                // TODO: Need a better way of determining image type
                String imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.JPEG.getValue();
                if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
                    imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.GIF.getValue();
                } else if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
                    imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.PNG.getValue();
                }


                byte[] resizedImageBytes = resizedImage.getByteSource().read();
                if (resizedImageBytes != null) {
                    final String clientImageAsBase64Text = imageDataURISuffix + Base64.getMimeEncoder().encodeToString(resizedImageBytes);
                    projectData.setLogo(clientImageAsBase64Text);
                }
            } catch (Exception e) {
                // do not fail if client image is not found
            }
            return apiJsonSerializerService.serialize(projectData);
        } else if (ownerId != null) {
            final List<InvestmentProjectData> projectsData = investmentProjectReadPlatformService.retrieveByClientId(ownerId);
            return apiJsonSerializerService.serialize(projectsData);
        } else if (categoryId != null) {
            final List<InvestmentProjectData> projectsData = investmentProjectReadPlatformService.retrieveByCategoryId(categoryId);
            return apiJsonSerializerService.serialize(projectsData);
        } else if (name != null) {
            final List<InvestmentProjectData> projectsData = investmentProjectReadPlatformService.retrieveByName(name);
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
