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
package ch.xxx.manager.domain.utils;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonValue;

public class DataHelper {
	public static enum TermType {Query, StartTerm, EndTerm }
	
	public static enum Operation {
		And, AndNot, Or, OrNot
	}

	public static enum CurrencyKey {
		EUR, HKD, USD
	}

	public static enum FinancialElementType {
		CashFlow("cf"), BalanceSheet("bf"), Income("ic");

		public final String value;

		private FinancialElementType(String value) {
			this.value = value;
		}

		public String toString() {
			return this.value;
		}
	}

	public static enum Quarter {
		H1("H1"), H2("H2"), H3("H3"), H4("H4"), T1("T1"), T2("T2"), T3("T3"), T4("T4"), CY("CY"), FY("FY"), Q1("Q1"),
		Q2("Q2"), Q3("Q3"), Q4("Q4");

		public final String value;

		private Quarter(String value) {
			this.value = value;
		}
		@JsonValue
		public String toString() {
			return this.value;
		}
	}

	public static enum Role implements GrantedAuthority {
		USERS, GUEST;

		@Override
		public String getAuthority() {
			return this.name();
		}

	}

}
