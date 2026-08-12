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
import {
  provideZoneChangeDetection,
} from "@angular/core";
import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
  withXhr,
} from "@angular/common/http";
import {
  MODULE_CONFIG,
  TokenInterceptor,
  TokenService,
} from "ngx-simple-charts/base-service";
import { provideLuxonDateAdapter } from "@angular/material-luxon-adapter";
import { provideAnimations } from "@angular/platform-browser/animations";
import { provideRouter } from "@angular/router";
import { routes } from "./app.routes";

export const appConfig = {
  providers: [
    provideZoneChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    provideAnimations(),
    provideLuxonDateAdapter(),
    TokenService,
    {
      provide: MODULE_CONFIG,
      useValue: {
        tokenRefreshPath: "/rest/auth/refreshToken",
        logoutPath: "/rest/auth/logout",
        loginRoute: "/login",
      },
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,
    },
  ],
};