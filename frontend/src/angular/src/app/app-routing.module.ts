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
import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { SpinnerComponent } from "./spinner/spinner.component";
import { MainGuard } from "./service/main.guard";

const routes: Routes = [
  { path: "spinner", component: SpinnerComponent },
  {
    path: "portfolios",
    canActivate: [MainGuard],
    loadChildren: () =>
      import("./portfolios/portfolios.module").then((m) => m.PortfoliosModule),
  },
  {
    path: "financialdata",
    canActivate: [MainGuard],
    loadChildren: () =>
      import("./financial-data/financial-data.module").then(
        (m) => m.FinancialDataModule
      ),
  },
  {
    path: "login",
    loadChildren: () =>
      import("./login/login.module").then((m) => m.LoginModule),
  },
  { path: "**", redirectTo: "spinner" },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {})],
  exports: [RouterModule],
})
export class AppRoutingModule {}
