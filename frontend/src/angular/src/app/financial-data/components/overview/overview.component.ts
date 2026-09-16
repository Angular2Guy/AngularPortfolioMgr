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
  Component,
  HostListener,
  DestroyRef,
  ChangeDetectionStrategy,
  inject,
  signal,
} from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { switchMap } from "rxjs/operators";
import { ImportFinancialsComponent } from "../import-financials/import-financials.component";
import { FinancialDataService } from "../../service/financial-data.service";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialElementExt } from "../../model/financial-element";
import { TokenService } from "ngx-simple-charts/base-service";
import { ConfigService } from "../../../service/config.service";
import {
  DialogSpinnerComponent,
  SpinnerData,
} from "../../../base/components/dialog-spinner/dialog-spinner.component";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ImportData, ImportDataType } from "../../../model/import-data";
import { QuoteImportService } from "../../../service/quote-import.service";
import { MatToolbar } from "@angular/material/toolbar";
import { MatButton } from "@angular/material/button";
import { CreateQueryComponent } from "../create-query/create-query.component";
import { QueryResultsComponent } from "../query-results/query-results.component";

@Component({
  selector: "app-overview",
  templateUrl: "./overview.component.html",
  styleUrls: ["./overview.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatToolbar, MatButton, CreateQueryComponent, QueryResultsComponent],
})
export class OverviewComponent {
  protected windowHeight = signal(0);
  protected symbolFinancials = signal<SymbolFinancials[]>([]);
  protected financialElements = signal<FinancialElementExt[]>([]);
  private spinnerDialogRef!: MatDialogRef<DialogSpinnerComponent, any> | null;

  private financialDataService = inject(FinancialDataService);
  private quoteImportService = inject(QuoteImportService);
  private tokenService = inject(TokenService);
  private dialog = inject(MatDialog);
  private configService = inject(ConfigService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  constructor() {
    this.windowHeight.set(window.innerHeight - 84);
  }

  @HostListener("window:resize", ["$event"])
  onResize(event: any) {
    this.windowHeight.set(event.target.innerHeight - 84);
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
    this.symbolFinancials.set(event);
  }

  updateFinancialElements(event: FinancialElementExt[]): void {
    this.financialElements.set(event);
  }

  showFinancialsImport(): void {
    this.configService.getImportPath().subscribe((result) => {
      const dialogRef = this.dialog.open(ImportFinancialsComponent, {
        width: "500px",
        disableClose: true,
        hasBackdrop: true,
        data: {
          filename: "",
          path: result,
          dataType: ImportDataType.Sec,
        } as ImportData,
      });
      dialogRef
        .afterClosed()
        .pipe(
          switchMap((result: ImportData) =>
            this.financialDataService.putImportFinancialsData(result),
          ),
        )
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((result) => console.log(result));
    });
  }

  showDailyQuotesImport(): void {
    this.configService.getImportPath().subscribe((result) => {
      const dialogRef = this.dialog.open(ImportFinancialsComponent, {
        width: "500px",
        disableClose: true,
        hasBackdrop: true,
        data: {
          filename: "",
          path: result,
          dataType: ImportDataType.Stocks,
        } as ImportData,
      });
      dialogRef
        .afterClosed()
        .pipe(
          switchMap((result: ImportData) =>
            this.quoteImportService.putDailyQuotesImport(result),
          ),
        )
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((result) => console.log(result));
    });
  }

  back(): void {
    this.router.navigate(["/portfolios/overview"]);
  }

  logout(): void {
    this.tokenService.logout();
  }
}
