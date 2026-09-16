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
  DestroyRef,
  ChangeDetectionStrategy,
  signal,
  inject,
  effect,
} from "@angular/core";
import {
  MatTableDataSource,
  MatTable,
  MatColumnDef,
  MatHeaderCellDef,
  MatHeaderCell,
  MatCellDef,
  MatCell,
  MatHeaderRowDef,
  MatHeaderRow,
  MatRowDef,
  MatRow,
} from "@angular/material/table";
import { Portfolio, CommonValues } from "../../../model/portfolio";
import { Router, ActivatedRoute, ParamMap } from "@angular/router";
import { switchMap, tap, filter, mergeMap } from "rxjs/operators";
import { PortfolioService } from "../../../service/portfolio.service";
import { MatDialog } from "@angular/material/dialog";
import { ChangeSymbolComponent } from "../change-symbol/change-symbol.component";
import { PortfolioElement } from "../../../model/portfolio-element";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatIcon } from "@angular/material/icon";
import { DecimalPipe } from "@angular/common";

@Component({
  selector: "app-portfolio-table",
  templateUrl: "./portfolio-table.component.html",
  styleUrls: ["./portfolio-table.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTable,
    MatColumnDef,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    MatIcon,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRowDef,
    MatRow,
    DecimalPipe,
  ],
})
export class PortfolioTableComponent {
  private myLocalPortfolio!: Portfolio;
  portfolioElements = new MatTableDataSource<CommonValues>([]);
  displayedColumns = [
    "name",
    "stocks",
    "month1",
    "month6",
    "year1",
    "year2",
    "year5",
    "year10",
  ];
  reloadData = signal(false);
  localPortfolio = signal<Portfolio>({} as Portfolio);

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private portfolioService = inject(PortfolioService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  constructor() {
    this.route.paramMap
      .pipe(
        filter(
          (params: ParamMap) =>
            parseInt(params.get("portfolioId") ?? "-1") >= 0,
        ),
        tap(() => this.reloadData.set(true)),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId") ?? "-1"),
          ),
        ),
        tap(() => this.reloadData.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((myData: Portfolio) => this.setLocalPortfolio(myData));

    effect(() => {
      const portfolio = this.localPortfolio();
      this.myLocalPortfolio = portfolio;
      const myPortfolioElements: CommonValues[] = [];
      if (!!portfolio?.portfolioElements) {
        myPortfolioElements.push(portfolio);
        myPortfolioElements.push(...portfolio?.portfolioElements);
      }
      this.portfolioElements.connect().next(myPortfolioElements);
    });
  }

  updateStock(event: MouseEvent, element: CommonValues) {
    if (!!(element as Portfolio).symbols) {
      return;
    }
    event.stopPropagation();
    const dialogRef = this.dialog.open(ChangeSymbolComponent, {
      width: "500px",
      data: element,
    });
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: PortfolioElement) => {
        const myPortfolio = {
          createdAt: this.myLocalPortfolio?.createdAt ?? "",
          currencyKey: this.myLocalPortfolio?.currencyKey ?? "",
          id: this.myLocalPortfolio?.id ?? -1,
          name: this.myLocalPortfolio?.name ?? "",
          portfolioElements: [],
          symbols: [],
          userId: this.myLocalPortfolio?.userId ?? "",
        } as unknown as Portfolio;
        if (!!result && result.weight > 0) {
          const mySymbol = this.myLocalPortfolio.symbols.filter(
            (mySymbol) => mySymbol.symbol === result.symbol,
          )[0];
          this.portfolioService
            .putSymbolToPortfolio(
              myPortfolio,
              mySymbol.id,
              result.weight,
              result.changedAt ?? "",
            )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(
              (myResult: Portfolio) => this.setLocalPortfolio(myResult),
            );
        } else if (!!result && result.weight <= 0) {
          const mySymbol = this.myLocalPortfolio.symbols.filter(
            (mySymbol) => mySymbol.symbol === result.symbol,
          )[0];
          this.portfolioService
            .deleteSymbolFromPortfolio(
              myPortfolio,
              mySymbol.id,
              result.changedAt ?? "",
            )
            .pipe(
              mergeMap(() =>
                this.portfolioService.getPortfolioById(myPortfolio.id),
              ),
              takeUntilDestroyed(this.destroyRef),
            )
            .subscribe(
              (myResult: Portfolio) => this.setLocalPortfolio(myResult),
            );
        }
      });
  }

  selPortfolio(commonValues: CommonValues) {
    console.log(commonValues.id);
    this.router.navigate([
      "/portfolios/portfolio-detail/portfolio",
      this.myLocalPortfolio.id,
    ]);
  }

  private setLocalPortfolio(portfolio: Portfolio) {
    this.localPortfolio.set(portfolio);
  }
}
