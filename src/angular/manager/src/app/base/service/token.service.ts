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
import { HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private myToken: string;

  constructor() { }

    public createTokenHeader(): HttpHeaders {
        let reqOptions = new HttpHeaders().set( 'Content-Type', 'application/json' )
        if(this.token) {
            reqOptions = new HttpHeaders().set( 'Content-Type', 'application/json' ).set('Authorization', `Bearer ${this.token}`);
        }
        return reqOptions;
    }

  get token(): string {
	return this.myToken;
  }
 
  set token(token: string) {
	this.myToken = token;
  }
}
