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
import { AfterViewInit, Component, Input, OnInit, ViewChild } from "@angular/core";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialElementExt } from "../../model/financial-element";
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";

@Component({
  selector: "app-query-results",
  templateUrl: "./query-results.component.html",
  styleUrls: ["./query-results.component.scss"],
})
export class QueryResultsComponent implements AfterViewInit {
  @Input()
  symbolFinancials: SymbolFinancials[] = [];
  @ViewChild(MatSort) sort: MatSort;
  protected displayedColumns: string[] = ['concept', 'value', 'currency', 'year', 'quarter', 'symbol'];
  protected dataSource = new MatTableDataSource<FinancialElementExt>([]);
  private _financialElements: FinancialElementExt[] = [];
  
  ngAfterViewInit(): void {
    this.dataSource.filterPredicate = (data: FinancialElementExt, filter: string) => data?.symbol?.trim().toLowerCase().includes(filter);
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
  
  get financialElements(): FinancialElementExt[] {
	  return this._financialElements;
  }
  
  @Input()
  set financialElements(financialElementExt: FinancialElementExt[]) {
	  this.dataSource.data = financialElementExt;	  
	  this._financialElements = financialElementExt;
  }
}
