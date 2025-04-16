package com.belat.fineract.portfolio.questionsanswers.domain.question;

import com.belat.fineract.portfolio.questionsanswers.domain.answer.BelatAnswer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "e_question")
public class BelatQuestion extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "title")
    private String title;

    @Column(name = "question")
    private String question;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private Client user;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BelatAnswer> belatAnswers;

}
