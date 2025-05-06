package com.belat.fineract.portfolio.projectparticipation.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ProjectParticipationStatusEnum {

    ACCEPTED("ProjectParticipationStatusEnum.accepted", 100), //

    PENDING("ProjectParticipationStatusEnum.pending", 200), //
    DECLINED("ProjectParticipationStatusEnum.declined", 300), //
    RESERVED("ProjectParticipationStatusEnum.reserved", 400), // //
    ;

    private static final ProjectParticipationStatusEnum[] VALUES = values();

    private static final Map<Integer, ProjectParticipationStatusEnum> BY_ID = Arrays.stream(VALUES)
            .collect(Collectors.toMap(ProjectParticipationStatusEnum::getValue, v -> v));

    private final String code;

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    ProjectParticipationStatusEnum(String code, Integer value) {
        this.code = code;
        this.value = value;
    }

    public static ProjectParticipationStatusEnum fromInt(final Integer value) {
        return BY_ID.get(value);
    }

    public static EnumOptionData toEnumOptionData(final Integer id) {
        return toEnumOptionData(ProjectParticipationStatusEnum.fromInt(id));
    }

    public static EnumOptionData toEnumOptionData(final ProjectParticipationStatusEnum statusType) {
        return statusType == null ? null : statusType.toEnumOptionData();
    }

    public EnumOptionData toEnumOptionData() {
        return new EnumOptionData(getValue().longValue(), code, name());
    }

}
