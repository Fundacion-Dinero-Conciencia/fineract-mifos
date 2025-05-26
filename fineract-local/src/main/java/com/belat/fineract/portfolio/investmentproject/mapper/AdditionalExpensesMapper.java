package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class, uses = {CodeValueMapper.class})
public interface AdditionalExpensesMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "commissionTypeId", source = "commissionType.id")
    AdditionalExpensesData map(AdditionalExpenses additionalExpenses);

    List<AdditionalExpensesData> map(List<AdditionalExpenses> additionalExpensesList);
}
