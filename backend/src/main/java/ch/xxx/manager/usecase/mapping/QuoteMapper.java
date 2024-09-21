/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.usecase.mapping;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.QuoteDto;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.dto.DailyQuoteEntityDto;

@Component
public class QuoteMapper {
	public QuoteDto convert(DailyQuote entity) {
		return new QuoteDto(entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose(), entity.getAdjClose(), entity.getVolume(),
				entity.getLocalDay().atStartOfDay(), entity.getSymbol().getSymbol(), entity.getSplit(), entity.getDividend());
	}
	
	public QuoteDto convert(DailyQuoteEntityDto myRecord) {
		return myRecord.dto();
	}		
}
