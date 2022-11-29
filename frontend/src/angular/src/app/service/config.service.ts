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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { of } from 'rxjs';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
	private profiles: string = null;
	private importPath: string = null;
	private stringOperators: string[] = [];
	private numberOperators: string[] = [];
	private queryOperators: string[] = [];
	private termOperators: string[] = [];
	
	constructor(private http: HttpClient) { }

	getProfiles(): Observable<string> {		
		if(!this.profiles) {			
			return this.http.get(`/rest/config/profiles`, {responseType: 'text'}).pipe(tap(value => this.profiles = value));
		} else {
			return of(this.profiles);
		}
	}

    getImportPath(): Observable<string> {
		if(!this.importPath) {
			return this.http.get(`/rest/config/importpath`, {responseType: 'text'}).pipe(tap(value => this.importPath = value));
		} else {
			return of(this.importPath);
		}			
    }
    
    getStringOperators(): Observable<string[]> {
		if(this.stringOperators.length > 0) {
			return of(this.stringOperators);
		} else {
			return this.http.get<string[]>('/rest/config/operators/string').pipe(tap(value => this.stringOperators = value));
		}
    }
    
    getNumberOperators(): Observable<string[]> {	
		if(this.numberOperators.length > 0) {
			return of(this.numberOperators);
		} else {
			return this.http.get<string[]>('/rest/config/operators/number').pipe(tap(value => this.numberOperators = value));
		}
    }
    
    getQueryOperators(): Observable<string[]> {
		if(this.queryOperators.length > 0) {
			return of(this.queryOperators);
		} else {
			return this.http.get<string[]>('/rest/config/operators/query').pipe(tap(value => this.queryOperators = value));
		}
    }
}
