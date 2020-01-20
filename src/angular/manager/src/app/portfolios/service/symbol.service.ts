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
import { TokenService } from '../../service/token.service';
import { map } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class SymbolService {

	constructor(private http: HttpClient, private tokenService: TokenService) { }

	getSymbolBySymbol(symbol: string): Observable<Symbol> {
		return this.http.get<Symbol>(`/rest/symbol/symbol/${symbol}`, { headers: this.tokenService.createTokenHeader() })
			.pipe(map(res => res, err => this.handleError('getSymbolBySymbol', err)));
	}

	getSymbolByName(name: string): Observable<Symbol[]> {
		return this.http.get<Symbol[]>(`/rest/symbol/name/${name}`, { headers: this.tokenService.createTokenHeader() })
			.pipe(map(res => res, err => this.handleError('getSymbolByName', err)));
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
