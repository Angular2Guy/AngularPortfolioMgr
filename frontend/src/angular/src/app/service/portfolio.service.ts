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
import { Observable } from 'rxjs';
import { Portfolio } from '../model/portfolio';
import { PortfolioBars } from '../model/portfolio-bars';
import { HttpClient } from '@angular/common/http';
import { ComparisonIndex } from './quote.service';

@Injectable({providedIn: 'root'})
export class PortfolioService {

  constructor(private http: HttpClient) { }

  	getPortfolioByUserId(userId: number): Observable<Portfolio[]> {
		return this.http.get<Portfolio[]>(`/rest/portfolio/userid/${userId}`);
	}
	
	getPortfolioById(portfolioId: number): Observable<Portfolio> {
		return this.http.get<Portfolio>(`/rest/portfolio/id/${portfolioId}`);
	}
	
	getPortfolioBarsByIdAndStart(portfolioId: number, start: Date, compSyms: ComparisonIndex[]): Observable<PortfolioBars> {
		const compSymStrs = compSyms.length === 0 ? '' : ('?compSymbols=' + compSyms.join(','));
		return this.http.get<PortfolioBars>(`/rest/portfolio/id/${portfolioId}/start/${start.toISOString().split('T')[0]}${compSymStrs}`);
	}	
	
	postPortfolio(portfolio: Portfolio): Observable<Portfolio> {
		return this.http.post<Portfolio>('/rest/portfolio', portfolio);
	}
	
	postSymbolToPortfolio(portfolio: Portfolio, symbolId: number, weight: number, changedAt: string): Observable<Portfolio> {
		return this.http.post<Portfolio>(`/rest/portfolio/symbol/${symbolId}/weight/${weight}?changedAt=${encodeURI(changedAt)}`, portfolio);
	}
	
	putSymbolToPortfolio(portfolio: Portfolio, symbolId: number, weight: number, changedAt: string): Observable<Portfolio> {
		return this.http.put<Portfolio>(`/rest/portfolio/symbol/${symbolId}/weight/${weight}?changedAt=${encodeURI(changedAt)}`, portfolio);
	}
	
	deleteSymbolFromPortfolio(portfolio: Portfolio, symbolId: number, removedAt: string): Observable<Portfolio> {
		const id = portfolio.id;
		return this.http.delete<Portfolio>(`/rest/portfolio/${id}/symbol/${symbolId}?removedAt=${encodeURI(removedAt)}`);
	}
}
