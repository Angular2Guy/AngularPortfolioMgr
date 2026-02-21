/**
 * Copyright 2019 Sven Loesekann
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.xxx.manager.stocks.mapping.open;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import ch.xxx.manager.stocks.dto.YahooEventsDividend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.stocks.dto.YahooChartWrapper;
import ch.xxx.manager.stocks.dto.YahooDailyQuoteImportDto;
import ch.xxx.manager.stocks.dto.YahooResultWrapper;
import tools.jackson.databind.json.JsonMapper;

@Component
public class YahooClientMapper {
    private enum JsonKey {
        AdjClose("adjclose"), Open("open"), High("high"), Volume("volume"), Low("low"), Close("close");

        private final String value;

        JsonKey(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private record DateToDto(Long timestamp, Integer index, YahooDailyQuoteImportDto dto) {
    }

    private final JsonMapper objectMapper;
    private static final Logger LOG = LoggerFactory.getLogger(YahooClientMapper.class);

    public YahooClientMapper(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public YahooChartWrapper convert(String jsonStr) {
        var mappingIterator = this.objectMapper.readValue(jsonStr, YahooChartWrapper.class);
        return mappingIterator;
    }

    public static Stream<YahooDailyQuoteImportDto> convert(YahooChartWrapper dto) {
        return Optional.ofNullable(dto.chart().result()).orElse(List.of()).stream().flatMap(YahooClientMapper::convert);
    }

    private static Stream<YahooDailyQuoteImportDto> convert(final YahooResultWrapper dto) {
        final var atomicInt = new AtomicInteger(-1);
        return Optional.ofNullable(dto.timestamp()).orElse(List.of()).stream()
                .flatMap(myTimestamp -> Stream
                        .of(new DateToDto(myTimestamp, atomicInt.addAndGet(1), new YahooDailyQuoteImportDto())))
                .map(YahooClientMapper::addLocalDate).map(myDto -> addAdjClose(dto, myDto))
                .map(myDto -> addQuoteProperties(dto, myDto)).map(myDto -> addEventProperties(dto, myDto))
                .filter(YahooClientMapper::filterEmptyQuotes)
                .map(DateToDto::dto);
    }

    private static boolean filterEmptyQuotes(final DateToDto dto) {
        return Optional.ofNullable(dto.dto()).stream()
                .anyMatch(myDto -> !BigDecimal.ZERO.equals(myDto.getOpen()) && !BigDecimal.ZERO.equals(myDto.getHigh()) && !BigDecimal.ZERO.equals(myDto.getLow())
                        && !BigDecimal.ZERO.equals(myDto.getClose()) && !Long.valueOf(0L).equals(myDto.getVolume()));
    }

    private static DateToDto addLocalDate(DateToDto myDto) {
        myDto.dto()
                .setDate(Instant.ofEpochMilli(myDto.timestamp() * 1000).atZone(ZoneId.systemDefault()).toLocalDate());
        return myDto;
    }

    private static DateToDto addEventProperties(final YahooResultWrapper dto, DateToDto myDto) {
        Optional.ofNullable(dto.events()).ifPresent(myEvents -> {
            myDto.dto().setDividend(Optional
                    .ofNullable(Optional.ofNullable(myEvents.dividends()).orElse(Map.of()).get(myDto.timestamp()))
                    .stream().map(YahooEventsDividend::amount).findFirst().orElse(BigDecimal.ZERO));
            myDto.dto().setSplit(Optional
                    .ofNullable(Optional.ofNullable(myEvents.splits()).orElse(Map.of()).get(myDto.timestamp())).stream()
                    .map(myValue -> myValue.denominator() != 0
                            ? BigDecimal.valueOf(myValue.numerator()).divide(BigDecimal.valueOf(myValue.denominator()))
                            : BigDecimal.ZERO)
                    .findFirst().orElse(BigDecimal.ZERO));
        });
        return myDto;
    }

    private static DateToDto addQuoteProperties(final YahooResultWrapper dto, DateToDto myDto) {
        Optional.ofNullable(dto.indicators()).ifPresent(myIndicators -> {
            myDto.dto().setOpen(myIndicators.quote().stream()
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.open(), myDto.index())).orElse(BigDecimal.ZERO))
                    .findFirst().orElse(BigDecimal.ZERO));
            myDto.dto().setHigh(myIndicators.quote().stream()
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.high(), myDto.index())).orElse(BigDecimal.ZERO))
                    .findFirst().orElse(BigDecimal.ZERO));
            myDto.dto().setVolume(myIndicators.quote().stream()
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.volume(), myDto.index())).orElse(0L))
                    .findFirst().orElse(0L));
            myDto.dto().setLow(myIndicators.quote().stream()
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.low(), myDto.index())).orElse(BigDecimal.ZERO))
                    .findFirst().orElse(BigDecimal.ZERO));
            myDto.dto().setClose(myIndicators.quote().stream()
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.close(), myDto.index())).orElse(BigDecimal.ZERO))
                    .findFirst().orElse(BigDecimal.ZERO));
        });
        return myDto;
    }

    private static DateToDto addAdjClose(final YahooResultWrapper dto, DateToDto myDto) {
        Optional.ofNullable(dto.indicators()).ifPresent(myIndicators -> {
            myDto.dto().setAdjClose(Optional.ofNullable(myIndicators.adjclose()).stream().flatMap(List::stream)
                    .map(myQuote -> Optional.ofNullable(getListValueAt(myQuote.adjclose(), myDto.index())).orElse(BigDecimal.ZERO))
                    .findFirst().orElse(BigDecimal.ZERO));
        });
        return myDto;
    }

    private static <T> T getListValueAt(List<T> list, int index) {
        return list.size() <= index ? null : list.get(index);
    }
}
