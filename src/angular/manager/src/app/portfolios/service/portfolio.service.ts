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
import { TokenService } from '../../service/token.service';
import { Observable, of } from 'rxjs';
import { Portfolio } from '../model/portfolio'
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class PortfolioService {

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  	getPortfolio(userId: number): Observable<Portfolio[]> {
		return this.http.get<Portfolio[]>(`/rest/portfolio/userid/${userId}`, { headers: this.tokenService.createTokenHeader() })
			.pipe(map(res => res, err => this.handleError('getPortfolio', err)));
	}
	
	postPortfolio(portfolio: Portfolio): Observable<Portfolio> {
		return this.http.post<Portfolio>('/rest/portfolio', portfolio, { headers: this.tokenService.createTokenHeader() })
			.pipe(map(res => res, err => this.handleError('postPortfolio', err)));
	}
	
	private handleError<T>(operation = 'operation', result?: T) {
		return (error: any): Observable<T> => {

			console.error(error); // log to console instead

			this.log(`${operation} failed: ${error.message}`);

			return of(result as T);
		};
	}
	
	private log(message: string) {
		console.log(message);
	}
}
