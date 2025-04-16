package com.belat.fineract.portfolio.questionsanswers.domain.question;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BelatQuestionRepository extends JpaRepository<BelatQuestion, Long> {

    @Query(value = "SELECT * FROM e_question WHERE id = ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    BelatQuestion retrieveOneByQuestionId(Long questionId);

    @Query("SELECT bq FROM BelatQuestion bq WHERE bq.user.id = :userId")
    List<BelatQuestion> retrieveByUserId(@Param("userId") Long userId);

}
