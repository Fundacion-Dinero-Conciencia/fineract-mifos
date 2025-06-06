package com.belat.fineract.portfolio.pdfgenerator.api;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PdfGeneratorConstants {

    public static final String projectIdParamName = "projectId";
    public static final String clientIdParamName = "clientId";
    public static final String amountParamName = "amount";
    public static final String fundIdParamName = "fundId";
    public static final String loanIdParamName = "loanId";

    /**
     * These parameters will match the class level parameters of {@link ProjectParticipationData}. Where possible, we
     * try to get response parameters to match those of request parameters.
     */
    public static final Set<String> PROJECT_PARTICIPATION_PARAMETERS = new HashSet<>(Arrays.asList(projectIdParamName,
            clientIdParamName, amountParamName, fundIdParamName));

}
