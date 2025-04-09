package com.belat.fineract.portfolio.projectparticipation.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProjectParticipationData {

    private Long id;
    private Long participantId;
    private Long projectId;
    private BigDecimal amount;
    private StatusEnum status;
    private String type;

    @Data
    @AllArgsConstructor
    public static class StatusEnum {
        private String code;
        private Integer value;
    }

}
