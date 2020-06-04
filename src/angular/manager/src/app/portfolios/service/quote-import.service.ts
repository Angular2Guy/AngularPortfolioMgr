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

@Injectable()
export class QuoteImportService {

  constructor(private http: HttpClient) { }

  importDailyQuotes(symbol: string): Observable<number> {
	return this.http.get<number>(`/rest/quote/import/daily/symbol/${symbol}`);
  }  

  importIntraDayQuotes(symbol: string): Observable<number> {
	return this.http.get<number>(`/rest/quote/import/intraday/symbol/${symbol}`);
  }

  importFxDailyQuotes(to_curr: string): Observable<number> {
	return this.http.get<number>(`/rest/quote/import/daily/currency/${to_curr}`)
  }
}
