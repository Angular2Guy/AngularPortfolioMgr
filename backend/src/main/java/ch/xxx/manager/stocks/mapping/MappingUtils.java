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
package ch.xxx.manager.stocks.mapping;

import java.util.Optional;
import java.util.function.Predicate;

import ch.xxx.manager.stocks.entity.Symbol;

public class MappingUtils {
	public static final String SECTOR_PORTFOLIO = "Portfolio";
	
	public static String findSectorName(Symbol entity) {
		return Optional.ofNullable(entity.getSector()).stream().map(myEntity -> {
			String result = myEntity.getYahooName() != null && !myEntity.getYahooName().isBlank()
					? myEntity.getYahooName()
					: switch (entity.getQuoteSource()) {
					case ALPHAVANTAGE -> Optional.ofNullable(myEntity.getAlphavantageName()).filter(Predicate.not(String::isBlank)).orElse(SECTOR_PORTFOLIO);
					case YAHOO -> Optional.ofNullable(myEntity.getYahooName()).filter(Predicate.not(String::isBlank)).orElse(SECTOR_PORTFOLIO);
					default -> SECTOR_PORTFOLIO;
					};
			return result;
		}).findFirst().orElse(SECTOR_PORTFOLIO);
	}

	public static String findSectorName(Optional<Symbol> entityOpt) {
		return entityOpt.stream().map(MappingUtils::findSectorName).findFirst().orElse(SECTOR_PORTFOLIO);
	}
}
