import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TokenService } from '../../service/token.service';
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
import { Observable, of } from 'rxjs';
import { Login } from '../model/login';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable()
export class LoginService {

	constructor(private http: HttpClient, private tokenService: TokenService) { }

	postLogin(login: Login): Observable<Login> {
		return this.http.post<Login>('/rest/login', login, { headers: this.tokenService.createTokenHeader() }).pipe(map(res => {
			this.tokenService.token = res.jwtToken;
			return res;
		}, this.handleError('postLogin')));
	}

	postSignin(login: Login): Observable<Login> {
		return this.http.post<Login>('/rest/signin', login, { headers: this.tokenService.createTokenHeader() })
			.pipe(map(res => res, this.handleError('postSignin')));
	}

	postLogout(login: Login): Observable<boolean> {
		this.tokenService.token = null;
		return of(true);
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
