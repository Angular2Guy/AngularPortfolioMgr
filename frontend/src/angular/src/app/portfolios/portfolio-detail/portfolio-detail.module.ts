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
import { BaseModule } from "../../base/base.module";
import { PortfolioComponent } from "./components/portfolio/portfolio.component";
import { SymbolComponent } from "./components/symbol/symbol.component";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatRadioModule } from "@angular/material/radio";
import { MatListModule } from "@angular/material/list";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatButtonModule } from "@angular/material/button";
import { NgxLineChartsModule } from "ngx-simple-charts/line";
import { PortfolioDetailRoutingModule } from "./portfolio-detail-routing.module";
import { SymbolOverviewComponent } from "./components/symbol-overview/symbol-overview.component";

@NgModule({
  declarations: [PortfolioComponent, SymbolComponent, SymbolOverviewComponent],
  imports: [
    BaseModule,
    NgxLineChartsModule,
    MatCheckboxModule,
    MatRadioModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatFormFieldModule,
    MatButtonModule,
    PortfolioDetailRoutingModule,
  ],
})
export class PortfolioDetailModule {}
