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
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Quote } from '../model/quote';

export enum ComparisonIndex {
	SP500 = 'IVV',
	EUROSTOXX50 = 'SXRT.DE',
	MSCI_CHINA = 'ICGA.DE'
}

@Injectable()
export class QuoteService {
  constructor(private http: HttpClient) { }

  getAllDailyQuotes(symbol: string): Observable<Quote[]> {
	return this.http.get<Quote[]>(`/rest/quote/daily/all/symbol/${symbol}`);
  }

  getIntraDayQuotes(symbol: string): Observable<Quote[]> {
	return this.http.get<Quote[]>(`/rest/quote/intraday/symbol/${symbol}`);
  }

  getDailyQuotesFromStartToEnd(symbol: string, start: Date, end: Date): Observable<Quote[]> {
	const startStr = start.toISOString().split('T')[0];
	const endStr = end.toISOString().split('T')[0];
	return this.http.get<Quote[]>(`/rest/quote/daily/symbol/${symbol}/start/${startStr}/end/${endStr}`);
  }

  getDailyQuotesForComparisonIndexFromStartToEnd(portfolioId: number, comparisonIndex: ComparisonIndex, start: Date, end: Date): Observable<Quote[]> {
	const startStr = start.toISOString().split('T')[0];
	const endStr = end.toISOString().split('T')[0];
	return this.http.get<Quote[]>(`/rest/quote/daily/portfolio/${portfolioId}/index/${comparisonIndex}/start/${startStr}/end/${endStr}`);
  }

  getDailyQuotesForComparisonIndex(portfolioId: number, comparisonIndex: ComparisonIndex): Observable<Quote[]> {
	return this.http.get<Quote[]>(`/rest/quote/daily/all/portfolio/${portfolioId}/index/${comparisonIndex}`);
  }  
}
