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
  OnInit,
  HostListener,
  inject,
  signal,
  ChangeDetectionStrategy,
} from "@angular/core";
import { TokenService } from "ngx-simple-charts/base-service";
import { Router, RouterOutlet } from "@angular/router";
import { PortfolioService } from "../../../service/portfolio.service";
import { Portfolio } from "../../../model/portfolio";
import { MatDialog } from "@angular/material/dialog";
import { NewPortfolioComponent } from "../new-portfolio/new-portfolio.component";
import { PortfolioData } from "../../../model/portfolio-data";
import { SymbolImportService } from "../../../service/symbol-import.service";
import { forkJoin } from "rxjs";
import { AddSymbolComponent } from "../add-symbol/add-symbol.component";
import { Symbol } from "../../../model/symbol";
import { QuoteImportService } from "../../../service/quote-import.service";
import { ConfigService } from "../../../service/config.service";
import { ProdConfigComponent } from "../prod-config/prod-config.component";
import { DevConfigComponent } from "../dev-config/dev-config.component";
import {
  SpinnerData,
  DialogSpinnerComponent,
} from "../../../base/components/dialog-spinner/dialog-spinner.component";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { DestroyRef } from "@angular/core";
import { MatToolbar } from "@angular/material/toolbar";
import { MatButton } from "@angular/material/button";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import {
  MatSidenavContainer,
  MatSidenav,
  MatSidenavContent,
} from "@angular/material/sidenav";
import { NgStyle } from "@angular/common";
import { MatActionList, MatListItem } from "@angular/material/list";
import { MatIcon } from "@angular/material/icon";

@Component({
  selector: "app-overview",
  templateUrl: "./overview.component.html",
  styleUrls: ["./overview.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatToolbar,
    MatButton,
    MatProgressSpinner,
    MatSidenavContainer,
    NgStyle,
    MatSidenav,
    MatActionList,
    MatListItem,
    MatIcon,
    MatSidenavContent,
    RouterOutlet,
  ],
})
export class OverviewComponent implements OnInit {
  protected windowHeight = signal(0);
  portfolios = signal<Portfolio[]>([]);
  myPortfolio = signal<Portfolio>({} as Portfolio);
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
  importingSymbols = signal(false);
  countPortfolioSymbolsByUserId = signal(1000000);
  private timeoutId = -1;
  protected profiles = signal("");
  private showPortfolioTable = true;

  private tokenService = inject(TokenService);
  private configService = inject(ConfigService);
  private router = inject(Router);
  private portfolioService = inject(PortfolioService);
  private symbolImportService = inject(SymbolImportService);
  private quoteImportService = inject(QuoteImportService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.windowHeight.set(window.innerHeight - 84);
    this.refreshPortfolios();
    this.configService
      .getProfiles()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string) => {
        this.profiles.set(!value ? "dev" : value.trim().toLowerCase());
        console.log(this.profiles());
      });
  }

  @HostListener("window:resize", ["$event"])
  onResize(event: any) {
    this.windowHeight.set(event.target.innerHeight - 84);
  }

  newPortfolio() {
    const portfolio: Portfolio = {
      id: null,
      createdAt: new Date().toISOString(),
      month1: null,
      month6: null,
      name: null,
      symbols: [],
      currencyKey: null,
      portfolioElements: [],
      userId: this.tokenService.userId as number,
      year1: null,
      year10: null,
      year2: null,
      year5: null,
    } as unknown as Portfolio;
    const newPortfolioData: PortfolioData = { portfolio: portfolio };
    const dialogRef = this.dialog.open(NewPortfolioComponent, {
      width: "500px",
      data: newPortfolioData,
    });
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (result) {
          this.portfolioService
            .postPortfolio(result)
            .subscribe((myPortfolio) => {
              this.portfolios.set([...this.portfolios(), myPortfolio]);
              this.myPortfolio.set(myPortfolio);
              this.selPortfolio(myPortfolio, true);
            });
        }
      });
  }

  selPortfolio(portfolio: Portfolio, showPortTab = false) {
    this.myPortfolio.set(portfolio);
    this.showPortfolioTable = showPortTab ? true : !this.showPortfolioTable;
    const myPath = !this.showPortfolioTable
      ? "portfolio-overview/portfolio-charts"
      : "table";
    if (showPortTab) {
      this.router
        .navigate([`/portfolios/overview/${myPath}`, -1])
        .then(() =>
          this.router.navigate([
            `/portfolios/overview/${myPath}`,
            portfolio.id,
          ]),
        );
    } else {
      this.router.navigate([`/portfolios/overview/${myPath}`, portfolio.id]);
    }
  }

  showFinancialData() {
    this.router.navigate([`/financialdata`]);
  }

  updateQuotes() {
    this.quoteImportService
      .updateAllDailyIntraDayQuotes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((xxx) => console.log("updateQuotes() called."));
  }

  private refreshPortfolios() {
    this.portfolioService
      .getPortfolioByUserId(this.tokenService.userId as number)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((myPortfolios) => {
        myPortfolios.forEach(
          (port) => (port.symbols = !port.symbols ? [] : port.symbols),
        );
        this.portfolios.set(myPortfolios);
        this.myPortfolio.set(
          myPortfolios.length > 0 ? myPortfolios[0] : this.myPortfolio(),
        );
        if (!!this.myPortfolio()) {
          this.selPortfolio(this.myPortfolio(), true);
        }
      });
    this.portfolioService
      .countPortfolioSymbolsByUserId(this.tokenService.userId as number)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => this.countPortfolioSymbolsByUserId.set(result));
  }

  addSymbol(portfolio: Portfolio) {
    const portfolioData: PortfolioData = { portfolio: portfolio };
    const dialogRef = this.dialog.open(AddSymbolComponent, {
      width: "500px",
      disableClose: true,
      hasBackdrop: true,
      data: portfolioData,
    });
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((symbol: Symbol) => {
        if (symbol) {
          const dialogSpinnerRef = this.dialog.open(DialogSpinnerComponent, {
            width: "500px",
            disableClose: true,
            hasBackdrop: true,
            enterAnimationDuration: "500ms",
            exitAnimationDuration: "500ms",
            data: {
              title: $localize`:@@overviewPortfolioCalc:Portfolio Calculation`,
            } as SpinnerData,
          });
          symbol.weight = !symbol.weight ? 0 : symbol.weight;
          this.portfolioService
            .postSymbolToPortfolio(
              portfolio,
              symbol.id,
              symbol.weight,
              symbol.changedAt ?? "",
            )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((result: Portfolio) => {
              if (result) {
                const filteredPortfolios = this.portfolios().filter(
                  (port) => port.id !== result.id,
                );
                this.portfolios.set([...filteredPortfolios, result]);
                this.myPortfolio.set(result);
                this.selPortfolio(result, true);
                dialogSpinnerRef.close();
              }
            });
        }
      });
  }

  importSymbols(): void {
    this.importingSymbols.set(true);
    if (this.timeoutId !== -1) {
      clearTimeout(this.timeoutId);
    }
    forkJoin(
      this.symbolImportService.getSymbolImportUs(),
      this.symbolImportService.getSymbolImportHk(),
      this.symbolImportService.getSymbolImportDe(),
      this.quoteImportService.importFxDailyQuotes("USD"),
      this.quoteImportService.importFxDailyQuotes("HKD"),
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(
        ([resultUs, resultHk, resultDe, resultUSD, resultHKD]: [
          string,
          string,
          string,
          string,
          string,
        ]) => {
          console.log(
            `Us symbols: ${resultUs}, Hk symbols: ${resultHk}, De symbols: ${resultDe}, Usd quotes: ${resultUSD}, Hkd quotes: ${resultHKD}`,
          );
          this.timeoutId = setTimeout(
            () =>
              this.symbolImportService
                .getIndexSymbols()
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe((resultIndex: string) => {
                  console.log(`Index Symbols: ${resultIndex}`);
                  this.importingSymbols.set(false);
                }),
            60000,
          ) as unknown as number;
        },
      );
  }

  logout(): void {
    this.tokenService.logout();
  }

  showConfig(): void {
    if (this.profiles()) {
      const myOptions = { width: "700px" };
      let dialogRef = this.profiles().toLowerCase().includes("prod")
        ? this.dialog.open(ProdConfigComponent, myOptions)
        : this.dialog.open(DevConfigComponent, myOptions);
      dialogRef
        .afterClosed()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe();
    }
  }
}
