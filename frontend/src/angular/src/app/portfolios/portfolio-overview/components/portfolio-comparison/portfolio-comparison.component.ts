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
  inject,
  signal,
  input,
  effect,
} from "@angular/core";
import { DateTime, Duration } from "luxon";
import { ChartBars, ChartBar, NgxBarChartsModule } from "ngx-simple-charts/bar";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Portfolio } from "../../../../model/portfolio";
import { PortfolioBars } from "../../../../model/portfolio-bars";
import { PortfolioService } from "../../../../service/portfolio.service";
import { ComparisonIndex } from "../../../../service/quote.service";
import { MatRadioGroup, MatRadioButton } from "@angular/material/radio";
import { FormsModule } from "@angular/forms";
import { MatCheckbox } from "@angular/material/checkbox";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { DatePipe } from "@angular/common";

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
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatRadioGroup,
    FormsModule,
    MatRadioButton,
    MatCheckbox,
    MatProgressSpinner,
    NgxBarChartsModule,
    DatePipe,
  ],
})
export class PortfolioComparisonComponent {
  selPortfolio = input.required<Portfolio>();
  startDate = signal(new Date());
  chartPeriods = signal<ChartPeriod[]>([]);
  chartsLoading = signal(true);
  readonly ComparisonIndex = ComparisonIndex;
  showSP500 = signal(false);
  showMsciCH = signal(false);
  showES50 = signal(false);
  selChartPeriod = signal<ChartPeriod>({} as ChartPeriod);
  chartBars = signal<ChartBars>({} as ChartBars);
  selCompIndexes = signal<ComparisonIndex[]>([]);

  private portfolioService = inject(PortfolioService);
  private destroyRef = inject(DestroyRef);

  constructor() {
    const periods: ChartPeriod[] = [
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
    this.chartPeriods.set(periods);
    this.selChartPeriod.set(periods[0]);
    this.startDate.set(
      DateTime.now().minus(periods[0].periodDuration).toJSDate(),
    );
    this.chartPeriodChanged();

    effect(() => {
      const portfolio = this.selPortfolio();
      if (portfolio?.id) {
        this.chartPeriodChanged();
      }
    });
  }

  chartPeriodChanged(): void {
    const portfolio = this.selPortfolio();
    const period = this.selChartPeriod();
    if (!!portfolio?.id && !!period?.periodDuration) {
      this.chartsLoading.set(true);
      this.startDate.set(
        DateTime.now().minus(period.periodDuration).toJSDate(),
      );
      this.portfolioService
        .getPortfolioBarsByIdAndStart(
          portfolio.id,
          this.startDate(),
          this.selCompIndexes(),
        )
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((result: PortfolioBars) => this.updateChartData(result));
    }
  }

  private updateChartData(portfolioBars: PortfolioBars) {
    this.chartsLoading.set(false);
    const chartBars = portfolioBars.portfolioBars.map(
      (value) => ({ x: value.name, y: value.value }) as ChartBar,
    );
    this.chartBars.set({
      title: portfolioBars.title,
      from: this.startDate().toLocaleDateString(),
      yScaleWidth: 50,
      xScaleHeight: 50,
      chartBars: chartBars,
    } as ChartBars);
  }

  compIndexUpdate(value: boolean, comparisonIndex: ComparisonIndex): void {
    this.selCompIndexes.update((indexes) =>
      !value
        ? indexes.filter((ci) => comparisonIndex !== ci)
        : indexes.filter((ci) => comparisonIndex !== ci).concat(comparisonIndex),
    );
    this.chartPeriodChanged();
  }
}
