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
import { NgModule } from '@angular/core';
import { PortfoliosRoutingModule } from './portfolios-routing.module';
import { BaseModule } from '../base/base.module';
import { OverviewComponent } from './component/overview/overview.component';
import { PortfolioService } from './service/portfolio.service';
import { SymbolService } from './service/symbol.service';
import { QuoteService } from './service/quote.service';
import { NewPortfolioComponent } from './component/new-portfolio/new-portfolio.component';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from '../service/token.interceptor';
import { SymbolImportService } from './service/symbol-import.service';
import { AddSymbolComponent } from './component/add-symbol/add-symbol.component';
import { QuoteImportService } from './service/quote-import.service';
import { PortfolioComponent } from './component/portfolio/portfolio.component';
import { SymbolComponent } from './component/symbol/symbol.component';
import { PortfolioTableComponent } from './component/portfolio-table/portfolio-table.component';
import { PortfolioChartsComponent } from './component/portfolio-charts/portfolio-charts.component';
import { NgxSimpleChartsModule } from 'ngx-simple-charts';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button'; 
import { MatDialogModule } from '@angular/material/dialog';

@NgModule({
  declarations: [OverviewComponent, NewPortfolioComponent, AddSymbolComponent, PortfolioComponent, SymbolComponent, PortfolioTableComponent, PortfolioChartsComponent],
  imports: [
    BaseModule,
	NgxSimpleChartsModule,
	MatCheckboxModule,
	MatRadioModule,
	MatListModule,
	MatProgressSpinnerModule,
	MatSidenavModule,
	MatToolbarModule,
	MatIconModule,
	MatDatepickerModule,
	MatFormFieldModule,
	MatSelectModule,
	MatAutocompleteModule,
	MatTableModule,
	MatButtonModule,
	MatDialogModule,
    PortfoliosRoutingModule
  ],
  entryComponents: [NewPortfolioComponent],
  providers: [PortfolioService, SymbolService, QuoteService, SymbolImportService, QuoteImportService,
	  { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }],
})
export class PortfoliosModule { }
