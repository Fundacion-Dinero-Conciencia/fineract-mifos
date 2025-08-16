package com.belat.fineract.portfolio.projectparticipation.data;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectCategoryData;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectObjectiveData;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.objective.InvestmentProjectObjective;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectParticipationOdsAreaData {

    private Long id;
    private Long participantId;
    private Integer status;
    private String type;
    private ProjectOdsAreaData project;

    @Data
    public static class ProjectOdsAreaData {
        private Long id;
        private String name;
        private CodeValueData area;
        private CodeValueData category;
        private List<InvestmentProjectCategoryData> subCategories;
        private List<InvestmentProjectObjectiveData> objectives;

        public ProjectOdsAreaData(Long id, String name, CodeValue area, CodeValue category, List<InvestmentProjectCategory> subCategories, List<InvestmentProjectObjective> objectives) {
            this.id = id;
            this.name = name;
            this.area = new CodeValueData(area.getId(), area.getLabel(), area.getPosition(), null, area.isActive(), area.isMandatory(), null);
            this.category = new CodeValueData(category.getId(), category.getLabel(), category.getPosition(), null, category.isActive(), category.isMandatory(), null);
            this.subCategories = mapProjectCategoriesToData(subCategories);
            this.objectives = mapProjectObjectivesToData(objectives);
        }

        private List<InvestmentProjectCategoryData> mapProjectCategoriesToData(List<InvestmentProjectCategory> categories) {
            List<InvestmentProjectCategoryData> data = new ArrayList<>();
            for (InvestmentProjectCategory category : categories) {
                var codeValueData = new CodeValueData(category.getCategory().getId(), category.getCategory().getLabel(),
                        category.getCategory().getPosition(), null, category.getCategory().isActive(),
                        category.getCategory().isMandatory(), null);
                InvestmentProjectCategoryData categoryData = new InvestmentProjectCategoryData();
                categoryData.setCategory(codeValueData);
                data.add(categoryData);
            }
            return data;
        }

        private List<InvestmentProjectObjectiveData> mapProjectObjectivesToData(List<InvestmentProjectObjective> objectives) {
            List<InvestmentProjectObjectiveData> data = new ArrayList<>();
            for (InvestmentProjectObjective objective : objectives) {
                var codeValueData = new CodeValueData(objective.getObjective().getId(), objective.getObjective().getLabel(),
                        objective.getObjective().getPosition(), null, objective.getObjective().isActive(),
                        objective.getObjective().isMandatory(), null);
                InvestmentProjectObjectiveData objectiveData = new InvestmentProjectObjectiveData();
                objectiveData.setObjective(codeValueData);
                data.add(objectiveData);
            }
            return data;
        }

    }

}
