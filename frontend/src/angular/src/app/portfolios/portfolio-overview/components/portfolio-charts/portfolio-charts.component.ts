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
import { Component, OnInit, DestroyRef } from "@angular/core";
import { Portfolio } from "src/app/model/portfolio";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { switchMap, tap, filter } from "rxjs/operators";
import { PortfolioService } from "../../../../service/portfolio.service";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";
import { ReplaySubject, Subject } from "rxjs";
import { NewsItem } from "../../model/news-item";
import { NewsService } from "../../service/news.service";

@Component({
    selector: "app-portfolio-charts",
    templateUrl: "./portfolio-charts.component.html",
    styleUrls: ["./portfolio-charts.component.scss"],
    standalone: false
})
export class PortfolioChartsComponent implements OnInit {
  protected cnbcFinanceNews: NewsItem[] = [];
  protected seekingAlphaNews: NewsItem[] = [];
  selPortfolio: Portfolio;
  reloadData = false;

  constructor(
    private route: ActivatedRoute,
    private portfolioService: PortfolioService,
    private destroyRef: DestroyRef,
    private newsService: NewsService,
  ) {}

  ngOnInit(): void {
    this.newsService
      .getCnbcFinanceNews()
      .subscribe((result) => (this.cnbcFinanceNews = result));
    this.newsService
      .getSeekingAlphaNews()
      .subscribe((result) => (this.seekingAlphaNews = result));
    this.route.paramMap
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        filter((params: ParamMap) => parseInt(params.get("portfolioId")) >= 0),
        tap(() => (this.reloadData = true)),
        switchMap((params: ParamMap) =>
          this.portfolioService.getPortfolioById(
            parseInt(params.get("portfolioId")),
          ),
        ),
        tap(() => (this.reloadData = false)),
      )
      .subscribe((myData) => (this.selPortfolio = myData));
  }
}
