package ch.xxx.manager.domain.model.entity.dto;

import ch.xxx.manager.domain.model.dto.QuoteDto;
import ch.xxx.manager.domain.model.entity.DailyQuote;

public record DailyQuoteEntityDto(DailyQuote entity, QuoteDto dto) {

}
