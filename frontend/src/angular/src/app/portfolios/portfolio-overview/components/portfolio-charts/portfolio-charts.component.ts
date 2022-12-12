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
import { Component, Input, OnInit, OnDestroy } from "@angular/core";
import { Portfolio } from "src/app/model/portfolio";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { Subscription } from "rxjs";
import { switchMap, tap, filter } from "rxjs/operators";
import { PortfolioService } from "../../../../service/portfolio.service";

@Component({
  selector: "app-portfolio-charts",
  templateUrl: "./portfolio-charts.component.html",
  styleUrls: ["./portfolio-charts.component.scss"],
})
export class PortfolioChartsComponent implements OnInit, OnDestroy {
  selPortfolio: Portfolio;
  private dataSubscription: Subscription;
  reloadData = false;

  constructor(
    private route: ActivatedRoute,
    private portfolioService: PortfolioService
  ) {}

  ngOnInit(): void {
    this.dataSubscription = this.route.paramMap
      .pipe(
        filter((params: ParamMap) => parseInt(params.get("portfolioId")) >= 0),
        tap(() => (this.reloadData = true)),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId"))
          )
        ),
        tap(() => (this.reloadData = false))
      )
      .subscribe((myData) => (this.selPortfolio = myData));
  }

  ngOnDestroy(): void {
    this.dataSubscription.unsubscribe();
  }
}
