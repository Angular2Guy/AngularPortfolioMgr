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
import { Component, OnInit, Input, OnDestroy, DestroyRef } from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { Portfolio, CommonValues } from "../../../model/portfolio";
import { Router, ActivatedRoute, ParamMap } from "@angular/router";
import { Subscription } from "rxjs";
import { switchMap, tap, filter, mergeMap } from "rxjs/operators";
import { PortfolioService } from "../../../service/portfolio.service";
import { MatDialog } from "@angular/material/dialog";
import { ChangeSymbolComponent } from "../change-symbol/change-symbol.component";
import { PortfolioElement } from "src/app/model/portfolio-element";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";

@Component({
    selector: "app-portfolio-table",
    templateUrl: "./portfolio-table.component.html",
    styleUrls: ["./portfolio-table.component.scss"],
    standalone: false
})
export class PortfolioTableComponent implements OnInit {
  private myLocalPortfolio: Portfolio = null;
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
  reloadData = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private portfolioService: PortfolioService,
    private dialog: MatDialog,
    private destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        filter((params: ParamMap) => parseInt(params.get("portfolioId")) >= 0),
        tap(() => (this.reloadData = true)),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId")),
          ),
        ),
        tap(() => (this.reloadData = false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((myData) => (this.localPortfolio = myData));
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
        //console.log(result);
        const myPortfolio = {
          createdAt: this.localPortfolio?.createdAt,
          currencyKey: this.localPortfolio?.currencyKey,
          id: this.localPortfolio?.id,
          name: this.localPortfolio.name,
          portfolioElements: [],
          symbols: [],
          userId: this.localPortfolio.userId,
        } as Portfolio;
        if (!!result && result.weight > 0) {
          const mySymbol = this.localPortfolio.symbols.filter(
            (mySymbol) => mySymbol.symbol === result.symbol,
          )[0];
          this.portfolioService
            .putSymbolToPortfolio(
              myPortfolio,
              mySymbol.id,
              result.weight,
              result.changedAt,
            )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((myResult) => (this.localPortfolio = myResult));
        } else if (!!result && result.weight <= 0) {
          const mySymbol = this.localPortfolio.symbols.filter(
            (mySymbol) => mySymbol.symbol === result.symbol,
          )[0];
          this.portfolioService
            .deleteSymbolFromPortfolio(
              myPortfolio,
              mySymbol.id,
              result.changedAt,
            )
            .pipe(
              mergeMap((xyz) =>
                this.portfolioService.getPortfolioById(myPortfolio.id),
              ),
              takeUntilDestroyed(this.destroyRef),
            )
            .subscribe((myResult) => (this.localPortfolio = myResult));
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

  set localPortfolio(localPortfolio: Portfolio) {
    this.myLocalPortfolio = localPortfolio;
    const myPortfolioElements: CommonValues[] = [];
    if (!!localPortfolio?.portfolioElements) {
      myPortfolioElements.push(localPortfolio);
      myPortfolioElements.push(...localPortfolio?.portfolioElements);
    }
    this.portfolioElements.connect().next(myPortfolioElements);
  }

  get localPortfolio(): Portfolio {
    return this.myLocalPortfolio;
  }
}
