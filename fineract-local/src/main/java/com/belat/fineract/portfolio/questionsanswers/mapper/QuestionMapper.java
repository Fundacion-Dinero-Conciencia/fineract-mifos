package com.belat.fineract.portfolio.questionsanswers.mapper;

import com.belat.fineract.portfolio.questionsanswers.data.QuestionData;
import com.belat.fineract.portfolio.questionsanswers.domain.question.BelatQuestion;
import java.util.List;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface QuestionMapper {

    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "user", ignore = true)
    QuestionData map(BelatQuestion source);

    List<QuestionData> map(List<BelatQuestion> sources);

}
