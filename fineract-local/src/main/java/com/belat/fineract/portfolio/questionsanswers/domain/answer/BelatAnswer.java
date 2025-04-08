package com.belat.fineract.portfolio.questionsanswers.domain.answer;

import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_answer")
public class BelatAnswer extends AbstractAuditableWithUTCDateTimeCustom<Long> {


    @Column(name = "answer")
    private String answer;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "question_id", nullable = false, referencedColumnName = "id")
    private BelatQuestion question;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private Client user;

}
