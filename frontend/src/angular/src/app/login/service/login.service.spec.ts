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
import { TestBed } from "@angular/core/testing";
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { provideHttpClient } from "@angular/common/http";
import { Login } from "../model/login";
import { LoginService } from "./login.service";
import { TokenService } from "ngx-simple-charts/base-service";

describe("LoginService", () => {
  const login = {
    emailAddress: "email@email.com",
    password: "password",
    username: "username",
    token: "token",
  } as Login;
  let service!: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: TokenService,
          useValue: jasmine.createSpyObj(
            "TokenService",
            ["createTokenHeader"],
            { secUntilNextLogin: 60 },
          ),
        },
      ],
    });
    service = TestBed.inject(LoginService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
