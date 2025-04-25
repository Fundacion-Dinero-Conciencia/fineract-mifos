package com.belat.fineract.portfolio.investmentproject.data;

import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class StatusHistoryProjectData {

    private Long id;
    private Long investmentProjectId;
    private CodeValueData statusValue;
    private Long createdBy;
    private OffsetDateTime createdDate;


    public LocalDate getCreatedDate() {
        return this.createdDate.toLocalDate();
    }
}
