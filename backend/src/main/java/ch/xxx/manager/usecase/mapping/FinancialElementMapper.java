package ch.xxx.manager.usecase.mapping;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.dto.FinancialElementImportDto;
import ch.xxx.manager.domain.utils.DataHelper;

@Component
public class FinancialElementMapper {
	public FinancialElementImportDto toDto(FinancialElement financialElement) {
		FinancialElementImportDto dto = new FinancialElementImportDto();
		dto.setConcept(financialElement.getConcept());
		dto.setCurrency(financialElement.getCurrency().toString());
		dto.setLabel(financialElement.getLabel());
		dto.setValue(financialElement.getValue().toPlainString());
		dto.setId(financialElement.getId());
		return dto;
	}

	public FinancialElement toEntity(FinancialElementImportDto financialElementDto) {
		FinancialElement entity = new FinancialElement();
		entity.setConcept(financialElementDto.getConcept());
		entity.setCurrency(List.of(DataHelper.CurrencyKey.values()).stream().filter(
				myCurrencyKey -> financialElementDto.getCurrency().toUpperCase().contains(myCurrencyKey.toString()))
				.findFirst().orElse(DataHelper.CurrencyKey.USD));
		entity.setId(financialElementDto.getId());
		entity.setLabel(financialElementDto.getLabel());
		try {
			entity.setValue(new BigDecimal(financialElementDto.getValue()).round(MathContext.DECIMAL64));
		} catch (Exception e) {
			entity.setValue(BigDecimal.ZERO);
		}
		return entity;
	}
}
