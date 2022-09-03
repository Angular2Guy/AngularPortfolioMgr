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
import { Symbol } from './symbol';

enum QuoteSource { ALPHAVANTAGE = 'ALPHAVANTAGE', YAHOO = 'YAHOO', PORTFOLIO = 'PORTFOLIO' }

export class ServiceUtils {
	public static readonly PORTFOLIO_MARKER = "äüè";
	public static readonly QuoteSource = QuoteSource;
	
	public static isPortfolioSymbol(symbol: string | Symbol): boolean {		
		return (typeof symbol === 'string') ?  symbol.includes(ServiceUtils.PORTFOLIO_MARKER) 
		  : symbol.symbol.includes(ServiceUtils.PORTFOLIO_MARKER);
	}
	
	public static isIntraDayDataAvailiable(symbol: Symbol): boolean {
		return symbol && symbol.source && symbol.source === QuoteSource.ALPHAVANTAGE;
	}
}