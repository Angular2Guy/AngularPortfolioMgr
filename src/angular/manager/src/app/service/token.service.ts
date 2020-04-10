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
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Observable, of, timer, Subscription } from 'rxjs';
import { shareReplay, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private myTokenCache: Observable<string>; 	
  private readonly CACHE_SIZE = 1;
  private readonly REFRESH_INTERVAL = 45000; //45 sec
  private myToken: string;
  private myUserId: number;
  private myTokenSubscription: Subscription;

  constructor(private http: HttpClient) { }

  private refreshToken(): Observable<string> {	
	return this.http.get<string>('/rest/auth/refreshToken', {
		headers: this.createTokenHeader()
	});
  }

	public createTokenHeader(): HttpHeaders {
	    let reqOptions = new HttpHeaders().set( 'Content-Type', 'application/json' )
	    if(this.token) {
	        reqOptions = new HttpHeaders().set( 'Content-Type', 'application/json' ).set('Authorization', `Bearer ${this.token}`);
	    }
	    return reqOptions;
	}
	
  public clear() {
	this.myTokenSubscription.unsubscribe();
	this.myTokenCache = null;
	this.myToken = null;
	this.myUserId = null;
  }

  get tokenStream(): Observable<string> {
	return this.myTokenCache;
  }

  get token(): string {	
	return this.myToken;
  }
 
  set token(token: string) {
	this.myToken = token;
	if(token && !this.myTokenCache) {
		const myTimer = timer(0, this.REFRESH_INTERVAL);
		this.myTokenCache = myTimer.pipe(
			switchMap(() => this.refreshToken()),
			shareReplay(this.CACHE_SIZE));
		this.myTokenSubscription = this.myTokenCache.subscribe(newToken => this.myToken = newToken);
	}
  }

  get userId(): number {
	return this.myUserId;
  }

  set userId(userId: number) {
	this.myUserId = userId;
}
}
