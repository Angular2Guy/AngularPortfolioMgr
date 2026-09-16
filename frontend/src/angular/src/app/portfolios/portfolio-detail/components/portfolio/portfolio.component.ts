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
} from "@angular/core";
import { ActivatedRoute, ParamMap, Router } from "@angular/router";
import { switchMap, tap } from "rxjs/operators";
import { Symbol } from "../../../../model/symbol";
import { Portfolio } from "../../../../model/portfolio";
import { TokenService } from "ngx-simple-charts/base-service";
import { PortfolioService } from "../../../../service/portfolio.service";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatToolbar } from "@angular/material/toolbar";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatButton } from "@angular/material/button";
import {
  MatSidenavContainer,
  MatSidenav,
  MatSidenavContent,
} from "@angular/material/sidenav";
import { NgStyle } from "@angular/common";
import { MatActionList, MatListItem } from "@angular/material/list";
import { SymbolComponent } from "../symbol/symbol.component";
import { SymbolOverviewComponent } from "../symbol-overview/symbol-overview.component";

@Component({
  selector: "app-portfolio",
  templateUrl: "./portfolio.component.html",
  styleUrls: ["./portfolio.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatToolbar,
    MatProgressSpinner,
    MatButton,
    MatSidenavContainer,
    NgStyle,
    MatSidenav,
    MatActionList,
    MatListItem,
    MatSidenavContent,
    SymbolComponent,
    SymbolOverviewComponent,
  ],
})
export class PortfolioComponent {
  symbols = signal<Symbol[]>([]);
  reloadData = signal(false);
  windowHeight = 0;
  portfolio = signal<Portfolio>({} as Portfolio);
  selSymbol = signal<Symbol>({} as Symbol);
  showSymbol = signal(true);

  private route = inject(ActivatedRoute);
  private tokenService = inject(TokenService);
  private portfolioService = inject(PortfolioService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  constructor() {
    this.windowHeight = window.innerHeight - 84;
    this.route.paramMap
      .pipe(
        tap(() => this.reloadData.set(true)),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId") ?? "-1"),
          ),
        ),
        tap(() => this.reloadData.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((myPortfolio: Portfolio) => {
        this.symbols.set(myPortfolio.symbols);
        this.selSymbol.set(
          myPortfolio?.symbols.length > 0
            ? myPortfolio.symbols[0]
            : this.selSymbol(),
        );
        this.portfolio.set(myPortfolio);
      });
  }

  updateReloadData(state: boolean) {
    this.reloadData.set(state);
  }

  selectSymbol(symbol: Symbol): void {
    if (this.selSymbol()?.symbol === symbol?.symbol) {
      this.showSymbol.set(!this.showSymbol());
    }
    this.selSymbol.set(symbol);
  }

  back(): void {
    this.router.navigate(["/portfolios/overview"]);
  }

  logout(): void {
    this.tokenService.logout();
  }
}
