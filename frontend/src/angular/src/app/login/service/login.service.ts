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
import { Observable, of } from "rxjs";
import { Login } from "../model/login";
import { catchError, map, tap } from "rxjs/operators";
import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { TokenService } from "ngx-simple-charts/base-service";

@Injectable()
export class LoginService {
  constructor(private http: HttpClient, private tokenService: TokenService) {}

  postLogin(login: Login): Observable<Login> {
    return this.http
      .post<Login>("/rest/auth/login", login, {
        headers: this.tokenService.createTokenHeader(),
      })
      .pipe(
        map((res) => res, this.handleError("postLogin")),
        tap(
          (res) =>
            (this.tokenService.secUntilNextLogin = !!res?.secUntilNexLogin
              ? res?.secUntilNexLogin
              : 24 * 60 * 60)
        )
      );
  }

  postSignin(login: Login): Observable<boolean> {
    return this.http
      .post<boolean>("/rest/auth/signin", login, {
        headers: this.tokenService.createTokenHeader(),
      })
      .pipe(map((res) => res, this.handleError("postSignin")));
  }

  private handleError<T>(operation = "operation", result?: T) {
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
