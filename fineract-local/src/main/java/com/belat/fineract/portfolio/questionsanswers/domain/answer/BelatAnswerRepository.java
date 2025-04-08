package com.belat.fineract.portfolio.questionsanswers.domain.answer;

import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BelatAnswerRepository extends JpaRepository<BelatAnswer, Long> {

    @Query("SELECT pp FROM ProjectParticipation pp WHERE pp.client.id = :clientId")
    List<ProjectParticipation> retrieveByClientId(@Param("clientId") Long clientId);

}
