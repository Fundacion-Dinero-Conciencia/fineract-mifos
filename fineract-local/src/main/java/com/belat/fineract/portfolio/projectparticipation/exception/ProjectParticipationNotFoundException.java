package com.belat.fineract.portfolio.projectparticipation.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ProjectParticipationNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProjectParticipationNotFoundException(final Long id) {
        super("error.msg.investment.project.not.found", "Investment project with identifier " + id + " does not exist", id);
    }

}
