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
package ch.xxx.manager.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServiceUtils {
	public final static String PORTFOLIO_MARKER = "äüè";

	public enum RefMarker {
		US_REF_MARKER("öüà"), EUROPE_REF_MARKER("üäé"), CHINA_REF_MARKER("àöü");

		private String marker;

		private RefMarker(String marker) {
			this.marker = marker;
		}

		public String getMarker() {
			return this.marker;
		}
	};

	private final static int SYMBOL_LENGTH = 15;

	public static String generateRandomRefMarkerSymbol(RefMarker refMarker) {
		return generateRandomString(SYMBOL_LENGTH - refMarker.getMarker().length()) + refMarker.getMarker();
	}

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

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
