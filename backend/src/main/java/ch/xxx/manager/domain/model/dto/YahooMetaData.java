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
package ch.xxx.manager.domain.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record YahooMetaData(String currency, String symbol, String exchangeName, String instrumentType, Long firstTradeDate, Long regularMarketTime,
		Boolean hasPrePostMarketData, Long gmtoffset, String timezone, String exchangeTimezoneName, BigDecimal regularMarketPrice, BigDecimal fiftyTwoWeekHigh,
		BigDecimal fiftyTwoWeekLow, BigDecimal regularMarketDayHigh, BigDecimal regularMarketDayLow, Long regularMarketVolume, String longName, String shortName,
		BigDecimal chartPreviousClose, Long priceHint, YahooTradingPeriods currentTradingPeriod, String dataGranularity, String range, List<String> validRanges) {

}
