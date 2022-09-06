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
package ch.xxx.manager.usecase.service;

import java.util.Optional;
import java.util.Random;

import ch.xxx.manager.domain.model.entity.Symbol;

public class ServiceUtils {
	public final static String PORTFOLIO_MARKER = "V8yXhrg";
	public static final String SECTOR_PORTFOLIO = "Portfolio";

	private final static int SYMBOL_LENGTH = 18;

	public static String generateRandomPortfolioSymbol() {
		return generateRandomString(SYMBOL_LENGTH - PORTFOLIO_MARKER.length()) + PORTFOLIO_MARKER;
	}

	public static String generateRandomString(long length) {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		Random random = new Random();
		return random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public static String findSectorName(Symbol entity) {
		return Optional.ofNullable(entity.getSector()).stream().map(myEntity -> {
			String result = myEntity.getYahooName() != null && !myEntity.getYahooName().isBlank()
					? myEntity.getYahooName()
					: switch (entity.getQuoteSource()) {
					case ALPHAVANTAGE -> myEntity.getAlphavantageName();
					case YAHOO -> myEntity.getYahooName();
					default -> SECTOR_PORTFOLIO;
					};
			return result;
		}).findFirst().orElse(SECTOR_PORTFOLIO);
	}

	public static String findSectorName(Optional<Symbol> entityOpt) {
		return entityOpt.stream().map(ServiceUtils::findSectorName).findFirst().orElse(SECTOR_PORTFOLIO);
	}
}