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
import { CommonModule } from "@angular/common";
import { BaseModule } from "../base/base.module";
import { MatDialogModule } from "@angular/material/dialog";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSortModule } from "@angular/material/sort";
import { MatSelectModule } from "@angular/material/select";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatTreeModule } from "@angular/material/tree";
import { DragDropModule } from "@angular/cdk/drag-drop";
import {MatBottomSheetModule} from '@angular/material/bottom-sheet';
import { FinancialDataRoutingModule } from "./financial-data-routing.module";
import { OverviewComponent } from "./components/overview/overview.component";
import { ImportFinancialsComponent } from "./components/import-financials/import-financials.component";
import { CreateQueryComponent } from "./components/create-query/create-query.component";
import { QueryResultsComponent } from "./components/query-results/query-results.component";
import { QueryComponent } from "./components/query/query.component";
import { FinancialDataService } from "./service/financial-data.service";
import { TokenInterceptor } from "ngx-simple-charts/base-service";
import { HTTP_INTERCEPTORS } from "@angular/common/http";
import { ResultTreeComponent } from "./components/result-tree/result-tree.component";

@NgModule({
  declarations: [
    OverviewComponent,
    ImportFinancialsComponent,
    CreateQueryComponent,
    QueryResultsComponent,
    QueryComponent,
    ResultTreeComponent,
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatToolbarModule,
    MatTooltipModule,
    MatSortModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatTableModule,
    MatTreeModule,
    MatAutocompleteModule,
    DragDropModule,
    MatBottomSheetModule,
    BaseModule,
    FinancialDataRoutingModule,
  ],
  providers: [
    FinancialDataService,
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
  ],
})
export class FinancialDataModule {}
