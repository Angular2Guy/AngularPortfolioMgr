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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.xxx.manager.domain.model.dto.YahooChartWrapper;
import ch.xxx.manager.domain.model.dto.YahooDailyQuoteImportDto;
import ch.xxx.manager.domain.model.dto.YahooResultWrapper;

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

	private final ObjectMapper objectMapper;

	public YahooClientMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public YahooChartWrapper convert(String jsonStr) {
		try {
			var mappingIterator = this.objectMapper.readValue(jsonStr, YahooChartWrapper.class);
			return mappingIterator;
		} catch (IOException e) {
			throw new RuntimeException("Json import failed.", e);
		}
	}

	public static Stream<YahooDailyQuoteImportDto> convert(YahooChartWrapper dto) {
		return Optional.ofNullable(dto.chart().result()).orElse(List.of()).stream().flatMap(YahooClientMapper::convert);
	}

	private static Stream<YahooDailyQuoteImportDto> convert(final YahooResultWrapper dto) {
		final var atomicInt = new AtomicInteger(-1);
		return Optional.ofNullable(dto.timestamp()).orElse(List.of()).stream()
				.flatMap(myTimestamp -> Stream
						.of(new DateToDto(myTimestamp, atomicInt.addAndGet(1), new YahooDailyQuoteImportDto())))
				.map(myDto -> addLocalDate(myDto)).map(myDto -> addAdjClose(dto, myDto))
				.map(myDto -> addQuoteProperties(dto, myDto)).map(myDto -> addEventProperties(dto, myDto))
				.map(myDto -> myDto.dto());
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
					.stream().map(myValue -> myValue.amount()).findFirst().orElse(BigDecimal.ZERO));
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
			myDto.dto()
					.setOpen(
							Optional.ofNullable(myIndicators.quote()).orElse(List.of(Map.of())).stream()
									.map(myMap -> myMap.getOrDefault(JsonKey.Open.toString(),
											new ArrayList<BigDecimal>(myDto.index() + 1)).get(myDto.index()))
									.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO)).findFirst()
									.orElse(BigDecimal.ZERO));
			myDto.dto()
					.setHigh(
							Optional.ofNullable(myIndicators.quote()).orElse(List.of(Map.of())).stream()
									.map(myMap -> myMap.getOrDefault(JsonKey.High.toString(),
											new ArrayList<BigDecimal>(myDto.index() + 1)).get(myDto.index()))
									.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO)).findFirst()
									.orElse(BigDecimal.ZERO));
			myDto.dto()
					.setVolume(Optional.ofNullable(myIndicators.quote()).orElse(List.of(Map.of())).stream()
							.map(myMap -> myMap.getOrDefault(JsonKey.Volume.toString(),
									new ArrayList<BigDecimal>(myDto.index() + 1)).get(myDto.index()))
							.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO))
							.map(myValue -> myValue.longValue()).findFirst().orElse(0L));
			myDto.dto().setLow(Optional.ofNullable(myIndicators.quote()).orElse(List.of(Map.of())).stream()
					.map(myMap -> myMap
							.getOrDefault(JsonKey.Low.toString(), new ArrayList<BigDecimal>(myDto.index() + 1))
							.get(myDto.index()))
					.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO)).findFirst()
					.orElse(BigDecimal.ZERO));
			myDto.dto()
					.setClose(Optional.ofNullable(myIndicators.quote()).orElse(List.of(Map.of())).stream()
							.map(myMap -> myMap.getOrDefault(JsonKey.Close.toString(),
									new ArrayList<BigDecimal>(myDto.index() + 1)).get(myDto.index()))
							.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO)).findFirst()
							.orElse(BigDecimal.ZERO));
		});
		return myDto;
	}

	private static DateToDto addAdjClose(final YahooResultWrapper dto, DateToDto myDto) {
		Optional.ofNullable(dto.indicators()).ifPresent(myIndicators -> {
			myDto.dto()
					.setAdjClose(Optional.ofNullable(myIndicators.adjclose()).orElse(List.of(Map.of())).stream()
							.map(myMap -> myMap.getOrDefault(JsonKey.AdjClose.toString(),
									new ArrayList<BigDecimal>(myDto.index() + 1)).get(myDto.index()))
							.map(myValue -> Optional.ofNullable(myValue).orElse(BigDecimal.ZERO)).findFirst()
							.orElse(BigDecimal.ZERO));
		});
		return myDto;
	}
}
