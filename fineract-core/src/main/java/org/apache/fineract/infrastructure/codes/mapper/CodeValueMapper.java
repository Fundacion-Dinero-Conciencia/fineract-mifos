package org.apache.fineract.infrastructure.codes.mapper;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class)
public interface CodeValueMapper {

    @Mapping(source = "label", target = "name")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "mandatory", target = "mandatory")
    @Mapping(source = "codeScore", target = "score")
    CodeValueData map(CodeValue entity);

    @Mapping(source = "name", target = "label")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "mandatory", target = "mandatory")
    @Mapping(source = "score", target = "codeScore")
    @Mapping(target = "code", ignore = true)
    CodeValue map(CodeValueData data);

    List<CodeValueData> toDataList(List<CodeValue> entities);
    List<CodeValue> toEntityList(List<CodeValueData> data);

}
