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
package ch.xxx.manager.stocks.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record YahooMetaData(String currency,
        String symbol,
        String exchangeName,
        String fullExchangeName,
        String instrumentType,
        Long firstTradeDate,
        Long regularMarketTime,
        Boolean hasPrePostMarketData,
        Integer gmtoffset,
        String timezone,
        String exchangeTimezoneName,
        Double regularMarketPrice,
        Double fiftyTwoWeekHigh,
        Double fiftyTwoWeekLow,
        Double regularMarketDayHigh,
        Double regularMarketDayLow,
        Long regularMarketVolume,
        String longName,
        String shortName,
        Double chartPreviousClose,
        Integer priceHint,
        YahooCurrentTradingPeriod currentTradingPeriod,
        String dataGranularity,
        String range,
        List<String> validRanges) {

}
