package com.belat.fineract.portfolio.questionsanswers.api;

import com.belat.fineract.portfolio.questionsanswers.data.QuestionData;
import com.belat.fineract.portfolio.questionsanswers.service.QuestionAnswerReadPlatformService;
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

@Path("/v1/question")
@Component
@Tag(name = "questionAnswers", description = "questionAnswers")
@RequiredArgsConstructor
public class QuestionAnswerApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<QuestionData> apiJsonSerializerService;
    private final QuestionAnswerReadPlatformService questionAnswerReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.PostAddQuestionRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.PostAddQuestionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String addQuestion(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).createQuestion().build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

    @POST
    @Path("/{questionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.PostAddAnswerRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.PostAddAnswerResponse.class))),
            @ApiResponse(responseCode = "403", description = "Project can not be created") })
    public String addAnswer(@Parameter(hidden = true) final String apiRequestBodyAsJson, @PathParam("questionId") final Long questionId) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).createAnswer(questionId).build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("/all")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.GetQuestionResponse.class))) })
    public String getAllQuestions() {
        platformUserRightsContext.isAuthenticated();
        final List<QuestionData> questions = questionAnswerReadPlatformService.retrieveAllQuestions();
        return apiJsonSerializerService.serialize(questions);
    }

    @GET
    @Path("/search")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = QuestionAnswerApiResourceSwagger.GetQuestionResponse.class))) })
    public String getQuestionById(@QueryParam("id") final Long id, @QueryParam("userId") final Long userId) {
        platformUserRightsContext.isAuthenticated();

        if (id != null) {
            final QuestionData questionData = questionAnswerReadPlatformService.retrieveQuestionById(id);
            return apiJsonSerializerService.serialize(questionData);
        }
        if (userId != null) {
            final List<QuestionData> questionData = questionAnswerReadPlatformService.retrieveQuestionByUserId(userId);
            return apiJsonSerializerService.serialize(questionData);
        } else {
            throw new IllegalArgumentException("Not supported parameter");
        }
    }

}
