package com.belat.fineract.portfolio.projectparticipation.api;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProjectParticipationConstants {



    public static final String participantIdParamName = "participantId";
    public static final String projectIdParamName = "projectId";
    public static final String amountParamName = "amount";
    public static final String commissionParamName = "commission";
    public static final String statusParamName = "status";
    public static final String typeParamName = "type";

    /**
     * These parameters will match the class level parameters of {@link ProjectParticipationData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> PROJECT_PARTICIPATION_PARAMETERS = new HashSet<>(Arrays.asList(participantIdParamName,
            projectIdParamName, amountParamName, commissionParamName, statusParamName, typeParamName));

    /**
     * These parameters will match the class level parameters of {@link ProjectParticipationData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> PROJECT_PARTICIPATION_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(amountParamName, statusParamName));
}
