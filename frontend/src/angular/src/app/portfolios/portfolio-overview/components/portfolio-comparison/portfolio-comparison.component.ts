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
import { Component, DestroyRef, Input, OnInit } from "@angular/core";
import { DateTime, Duration } from "luxon";
import { ChartBars, ChartBar } from "ngx-simple-charts/bar";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";
import { Portfolio } from "src/app/model/portfolio";
import { PortfolioBars } from "src/app/model/portfolio-bars";
import { PortfolioService } from "src/app/service/portfolio.service";
import { ComparisonIndex } from "src/app/service/quote.service";

const enum ChartPeriodKey {
  Month,
  Months3,
  Months6,
  Year,
  Year3,
  Year5,
  Year10,
}

interface ChartPeriod {
  periodText: string;
  periodDuration: any;
  chartPeriodKey: ChartPeriodKey;
}

@Component({
    selector: "app-portfolio-comparison",
    templateUrl: "./portfolio-comparison.component.html",
    styleUrls: ["./portfolio-comparison.component.scss"],
    standalone: false
})
export class PortfolioComparisonComponent implements OnInit {
  localSelPortfolio: Portfolio;
  startDate: Date;
  chartPeriods: ChartPeriod[] = [];
  chartsLoading = true;
  readonly ComparisonIndex = ComparisonIndex;
  showSP500 = false;
  showMsciCH = false;
  showES50 = false;
  selChartPeriod: ChartPeriod = null;
  chartBars!: ChartBars;
  selCompIndexes: ComparisonIndex[] = [];

  constructor(
    private portfolioService: PortfolioService,
    private destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.chartPeriods = [
      {
        chartPeriodKey: ChartPeriodKey.Month,
        periodText: $localize`:@@month1:1 Month`,
        periodDuration: { months: 1 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Months3,
        periodText: $localize`:@@month3:3 Months`,
        periodDuration: { months: 3 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Months6,
        periodText: $localize`:@@month6:6 Months`,
        periodDuration: { months: 6 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Year,
        periodText: $localize`:@@year1:1 Year`,
        periodDuration: { years: 1 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Year3,
        periodText: $localize`:@@year3:3 Years`,
        periodDuration: { years: 3 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Year5,
        periodText: $localize`:@@year5:5 Years`,
        periodDuration: { years: 5 },
      },
      {
        chartPeriodKey: ChartPeriodKey.Year10,
        periodText: $localize`:@@year10:10 Years`,
        periodDuration: { years: 10 },
      },
    ];
    this.selChartPeriod = this.chartPeriods[0];
    this.startDate = DateTime.now()
      .minus(this.selChartPeriod.periodDuration)
      .toJSDate();
    this.chartPeriodChanged();
  }

  get selPortfolio(): Portfolio {
    return this.localSelPortfolio;
  }

  @Input()
  set selPortfolio(myPortfolio: Portfolio) {
    this.localSelPortfolio = myPortfolio;
    this.chartPeriodChanged();
  }

  chartPeriodChanged(): void {
    if (!!this.selPortfolio?.id && !!this.selChartPeriod?.periodDuration) {
      this.chartsLoading = true;
      this.startDate = DateTime.now()
        .minus(this.selChartPeriod.periodDuration)
        .toJSDate();
      this.portfolioService
        .getPortfolioBarsByIdAndStart(
          this.selPortfolio.id,
          this.startDate,
          this.selCompIndexes,
        )
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((result) => this.updateChartData(result));
    }
  }

  private updateChartData(portfolioBars: PortfolioBars) {
    this.chartsLoading = false;
    //console.log(portfolioBars);
    const chartBars = portfolioBars.portfolioBars.map(
      (value) => ({ x: value.name, y: value.value }) as ChartBar,
    );
    this.chartBars = {
      title: portfolioBars.title,
      from: this.startDate.toLocaleDateString(),
      yScaleWidth: 50,
      xScaleHeight: 50,
      chartBars: chartBars,
    } as ChartBars;
    //console.log(this.chartBars);
  }

  compIndexUpdate(value: boolean, comparisonIndex: ComparisonIndex): void {
    this.selCompIndexes = !value
      ? this.selCompIndexes.filter((ci) => comparisonIndex !== ci)
      : this.selCompIndexes
          .filter((ci) => comparisonIndex !== ci)
          .concat(comparisonIndex);
    this.chartPeriodChanged();
  }
}
