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
  OnDestroy,
  EventEmitter,
  ChangeDetectorRef,
  DestroyRef,
} from "@angular/core";
import { ActivatedRoute, ParamMap, Router } from "@angular/router";
import { switchMap, tap } from "rxjs/operators";
import { Symbol } from "../../../../model/symbol";
import { Portfolio } from "../../../../model/portfolio";
import { TokenService } from "ngx-simple-charts/base-service";
import { PortfolioService } from "../../../../service/portfolio.service";
import { Subscription, Subject } from "rxjs";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";

@Component({
    selector: "app-portfolio",
    templateUrl: "./portfolio.component.html",
    styleUrls: ["./portfolio.component.scss"],
    standalone: false
})
export class PortfolioComponent implements OnInit {
  symbols: Symbol[] = [];
  reloadData = false;
  windowHeight = 0;
  portfolio: Portfolio;
  selSymbol: Symbol = null;
  showSymbol = true;

  constructor(
    private route: ActivatedRoute,
    private tokenService: TokenService,
    private portfolioService: PortfolioService,
    private router: Router,
    private changeDetectorRef: ChangeDetectorRef,
    private destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.windowHeight = window.innerHeight - 84;
    this.route.paramMap
      .pipe(
        tap(() => (this.reloadData = true)),
        //tap((params: ParamMap) => this.portfolioId = parseInt(params.get('portfolioId'))),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId")),
          ),
        ),
        tap(() => (this.reloadData = false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((myPortfolio) => {
        this.symbols = myPortfolio.symbols;
        this.selSymbol =
          myPortfolio?.symbols.length > 0
            ? myPortfolio.symbols[0]
            : this.selSymbol;
        this.portfolio = myPortfolio;
      });
  }

  updateReloadData(state: boolean) {
    this.reloadData = state;
    this.changeDetectorRef.detectChanges();
    //console.log('loading:'+state);
  }

  selectSymbol(symbol: Symbol): void {
    this.showSymbol =
      this?.selSymbol?.symbol === symbol?.symbol
        ? !this.showSymbol
        : this.showSymbol;
    this.selSymbol = symbol;
  }

  back(): void {
    this.router.navigate(["/portfolios/overview"]);
  }

  logout(): void {
    this.tokenService.logout();
  }
}
