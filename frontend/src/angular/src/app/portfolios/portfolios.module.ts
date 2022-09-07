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
import { OverviewComponent } from './components/overview/overview.component';
import { NewPortfolioComponent } from './components/new-portfolio/new-portfolio.component';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from '../service/token.interceptor';
import { AddSymbolComponent } from './components/add-symbol/add-symbol.component';
import { PortfolioTableComponent } from './components/portfolio-table/portfolio-table.component';
import { PortfolioChartsComponent } from './components/portfolio-charts/portfolio-charts.component';
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
import { MatLuxonDateModule } from '@angular/material-luxon-adapter';
import { MatInputModule } from '@angular/material/input';
import { DevAppInfoService } from './service/dev-app-info.service';
import { ProdAppInfoService } from './service/prod-app-info.service';
import { ProdConfigComponent } from './components/prod-config/prod-config.component';
import { DevConfigComponent } from './components/dev-config/dev-config.component';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs'; 
import { NgxBarChartsModule } from 'ngx-simple-charts/bar';
import { PortfolioComparisonComponent } from './components/portfolio-comparison/portfolio-comparison.component';
import { PortfolioSectorsComponent } from './components/portfolio-sectors/portfolio-sectors.component';


@NgModule({
    declarations: [OverviewComponent, NewPortfolioComponent, AddSymbolComponent,
        PortfolioTableComponent, PortfolioChartsComponent, ProdConfigComponent, DevConfigComponent, PortfolioComparisonComponent, PortfolioSectorsComponent],
    imports: [
        BaseModule,
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
        MatLuxonDateModule,
        MatInputModule,
        MatRadioModule,
        MatCheckboxModule,
        MatTabsModule,
        NgxBarChartsModule,
        PortfoliosRoutingModule
    ],
    providers: [DevAppInfoService, ProdAppInfoService, { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }]
})
export class PortfoliosModule { }
