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
import { ImportFinancialsData } from '../model/import-financials-data';

@Injectable({providedIn: 'root'})
export class SymbolImportService {

  constructor(private http: HttpClient) { }

  getSymbolImportUs(): Observable<string> {
	return this.http.get('/rest/symbol/importus/all', {responseType: 'text'});
  }

  getSymbolImportHk(): Observable<string> {
	return this.http.get('/rest/symbol/importhk/all', {responseType: 'text'});
  }

  getSymbolImportDe(): Observable<string> {
	return this.http.get('/rest/symbol/importde/all', {responseType: 'text'});
  }
  
  getIndexSymbols(): Observable<string> {
	return this.http.get('/rest/symbol/importindex/all', {responseType: 'text'});
  }
  
  putImportFinancialsData(importFinancialsData: ImportFinancialsData): Observable<string> {
	return this.http.put<string>('/rest/symbol/importus/financialdata', importFinancialsData);
  }
}
