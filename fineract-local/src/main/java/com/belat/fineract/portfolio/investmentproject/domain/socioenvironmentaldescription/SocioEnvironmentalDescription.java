package com.belat.fineract.portfolio.investmentproject.domain.socioenvironmentaldescription;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_project_socio_environmental_description")
public class SocioEnvironmentalDescription extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "little_description", nullable = false)
    private String littleDescription;

    @Column(name = "detailed_description", nullable = false)
    private String detailedDescription;

    public SocioEnvironmentalDescription(final String littleDescription, final String detailedDescription) {
        this.littleDescription = littleDescription;
        this.detailedDescription = detailedDescription;
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, littleDescription)) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.littleSocioEnvironmentalDescriptionParamName, newValue);
            this.littleDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, detailedDescription)) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.detailedSocioEnvironmentalDescriptionParamName, newValue);
            this.detailedDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
    }

}
