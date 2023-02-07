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
import { LoginService } from "./login.service";

interface Obs {
	pipe(fn: (x,y) => void): Observable<boolean>;
}

describe('LoginService', () => {
  const login = {
		emailAddress: 'email@email.com',
		password: 'password',
		username: 'username',
		token: 'token'
  } as Login;
  let service: LoginService = null;
  let obsSpy;
  beforeEach(() => {
	  const httpSpy = jasmine.createSpyObj('http', ['post']);
      obsSpy = jasmine.createSpyObj('obs', ['pipe']);		  
	  httpSpy.post.and.returnValue(obsSpy);
	  service = new LoginService(httpSpy,  
	  	jasmine.createSpyObj('tokenService', ['createTokenHeader'],{'secUntilNextLogin': 60}));	  	  
	  });

  it('should post signin', () => {	 
	obsSpy.pipe.and.returnValue(of(true));
    const result = service.postSignin(login);
    expect(result).toBeTruthy();
    result.subscribe(value => {
		expect(value).toBe(true);
	});
  });
  
  it('should post login', () => {
	  obsSpy.pipe.and.returnValue(of(login));
		const result = service.postLogin(login);
		expect(result).toBeTruthy();	
		result.subscribe(value => {
			expect(value.username).toBe(login.username);
			expect(value.password).toBe(login.password);
			});		  
	  });
  });

