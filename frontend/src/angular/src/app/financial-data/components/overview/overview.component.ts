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
import { Component, OnInit, HostListener, OnDestroy } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { switchMap} from "rxjs/operators";
import { Subscription } from "rxjs";
import { ImportFinancialsComponent } from "../import-financials/import-financials.component";
import { FinancialDataService } from "../../service/financial-data.service";
import { ImportFinancialsData } from "../../model/import-financials-data";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialElementExt } from "../../model/financial-element";
import { TokenService } from "ngx-simple-charts/base-service";
import { ConfigService } from "src/app/service/config.service";
import {
  DialogSpinnerComponent,
  SpinnerData,
} from "src/app/base/components/dialog-spinner/dialog-spinner.component";

@Component({
  selector: "app-overview",
  templateUrl: "./overview.component.html",
  styleUrls: ["./overview.component.scss"],
})
export class OverviewComponent implements OnInit, OnDestroy {
  protected windowHeight: number = null;
  private dialogSubscription: Subscription;
  protected symbolFinancials: SymbolFinancials[] = [];
  protected financialElements: FinancialElementExt[] = [];
  private spinnerDialogRef: MatDialogRef<DialogSpinnerComponent, any> = null;

  constructor(
    private financialDataService: FinancialDataService,
    private tokenService: TokenService,
    private dialog: MatDialog,
    private configService: ConfigService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.windowHeight = window.innerHeight - 84;
  }

  ngOnDestroy(): void {
    this.cleanupDialogSubcription();
  }

  private cleanupDialogSubcription(): void {
    if (!!this.dialogSubscription) {
      this.dialogSubscription.unsubscribe();
      this.dialogSubscription = null;
    }
  }

  @HostListener("window:resize", ["$event"])
  onResize(event: any) {
    this.windowHeight = event.target.innerHeight - 84;
  }

  showSpinner(show: boolean): void {
    if (!this.spinnerDialogRef && show) {
      const fetchDatai18n = $localize`:@@overviewFetchingData:Fetching Data`;
      this.spinnerDialogRef = this.dialog.open(DialogSpinnerComponent, {
        width: "500px",
        disableClose: true,
        hasBackdrop: true,
        data: { title: fetchDatai18n } as SpinnerData,
      });
    }
    if (!!this.spinnerDialogRef && !show) {
      this.spinnerDialogRef.close();
      this.spinnerDialogRef = null;
    }
  }

  updateSymbolFinancials(event: SymbolFinancials[]): void {
    this.symbolFinancials = event;
  }

  updateFinancialElements(event: FinancialElementExt[]): void {
    this.financialElements = event;
  }

  showFinancialsImport(): void {
    this.cleanupDialogSubcription();
    this.configService.getImportPath().subscribe((result) => {
      const dialogRef = this.dialog.open(ImportFinancialsComponent, {
        width: "500px",
        disableClose: true,
        hasBackdrop: true,
        data: { filename: "", path: result } as ImportFinancialsData,
      });
      this.dialogSubscription = dialogRef
        .afterClosed()
        .pipe(
          switchMap((result: ImportFinancialsData) =>
            this.financialDataService.putImportFinancialsData(result)
          )
        )
        .subscribe((result) => console.log(result));
    });
    //console.log('showFinancialsConfig()');
  }

  back(): void {
    this.router.navigate(["/portfolios/overview"]);
  }

  logout(): void {
    this.tokenService.logout();
  }
}
