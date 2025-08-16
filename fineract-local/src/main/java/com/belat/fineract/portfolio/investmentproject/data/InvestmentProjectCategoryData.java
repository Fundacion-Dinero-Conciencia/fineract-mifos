package com.belat.fineract.portfolio.investmentproject.data;

import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

@Data
public class InvestmentProjectCategoryData {

    private CodeValueData category;
    private InvestmentProjectData investmentProject;
}
