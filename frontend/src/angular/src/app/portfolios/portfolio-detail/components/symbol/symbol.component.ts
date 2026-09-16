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
  input,
  output,
  signal,
  inject,
  LOCALE_ID,
  DestroyRef,
  DOCUMENT,
  ChangeDetectionStrategy,
  effect,
} from "@angular/core";
import { Symbol } from "../../../../model/symbol";
import {
  QuoteService,
  ComparisonIndex,
} from "../../../../service/quote.service";
import { Quote } from "../../../../model/quote";
import { ServiceUtils } from "../../../../model/service-utils";
import { forkJoin } from "rxjs";
import {
  ChartPoint,
  ChartPoints,
  NgxLineChartsModule,
} from "ngx-simple-charts/line";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatRadioGroup, MatRadioButton } from "@angular/material/radio";
import { FormsModule } from "@angular/forms";
import { MatCheckbox } from "@angular/material/checkbox";
import { DecimalPipe, DatePipe } from "@angular/common";

const enum QuotePeriodKey {
  Month,
  Months3,
  Months6,
  Year,
  Year3,
  Year5,
  Year10,
}

interface QuotePeriod {
  periodText: string;
  quotePeriodKey: QuotePeriodKey;
}

interface SymbolData {
  start: Date;
  end: Date;
  open: number;
  high: number;
  low: number;
  close: number;
  accDividend: number;
  avgVolume: number;
  avgClose: number;
  medianClose: number;
  volatilityClose: number;
}

@Component({
  selector: "app-symbol",
  templateUrl: "./symbol.component.html",
  styleUrls: ["./symbol.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatRadioGroup,
    FormsModule,
    MatRadioButton,
    MatCheckbox,
    NgxLineChartsModule,
    DecimalPipe,
    DatePipe,
  ],
})
export class SymbolComponent {
  private readonly dayInMs = 24 * 60 * 60 * 1000;
  private readonly hourInMs = 1 * 60 * 60 * 1000;

  portfolioId = input.required<number>();
  symbol = input.required<Symbol>();
  showSymbol = input<boolean>(false);
  loadingData = output<boolean>();

  readonly ComparisonIndex = ComparisonIndex;
  serviceUtils = ServiceUtils;

  quotes = signal<Quote[]>([]);
  compIndexes = signal<Map<string, Quote[]>>(
    new Map([
      [ComparisonIndex.SP500, []],
      [ComparisonIndex.EUROSTOXX50, []],
      [ComparisonIndex.MSCI_CHINA, []],
    ]),
  );
  quotesLoading = signal(true);
  symbolData = signal<SymbolData>({
    avgVolume: 0,
    close: 0,
    end: new Date(),
    high: 0,
    low: 0,
    open: 0,
    accDividend: 0,
    start: new Date(),
    avgClose: 0,
    medianClose: 0,
    volatilityClose: 0,
  });
  chartPoints = signal<ChartPoints[]>([
    {
      chartPointList: [],
      name: "",
      xScaleHeight: 20,
      yScaleWidth: 50,
    } as ChartPoints,
  ]);
  showSP500 = signal(false);
  showMsciCH = signal(false);
  showES50 = signal(false);
  portfolioName = signal("");
  portfolioSymbol = signal("");
  quotePeriods: QuotePeriod[] = [];
  selQuotePeriod!: QuotePeriod;

  private quoteService = inject(QuoteService);
  private destroyRef = inject(DestroyRef);
  private document = inject(DOCUMENT);
  private locale = inject(LOCALE_ID);

  constructor() {
    this.quotePeriods = [
      {
        quotePeriodKey: QuotePeriodKey.Month,
        periodText: $localize`:@@month1:1 Month`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Months3,
        periodText: $localize`:@@month3:3 Months`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Months6,
        periodText: $localize`:@@month6:6 Months`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Year,
        periodText: $localize`:@@year1:1 Year`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Year3,
        periodText: $localize`:@@year3:3 Years`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Year5,
        periodText: $localize`:@@year5:5 Years`,
      },
      {
        quotePeriodKey: QuotePeriodKey.Year10,
        periodText: $localize`:@@year10:10 Years`,
      },
    ];

    effect(() => {
      const mySymbol = this.symbol();
      if (mySymbol) {
        this.selQuotePeriod = !ServiceUtils.isIntraDayDataAvailiable(mySymbol)
          ? this.quotePeriods[1]
          : this.quotePeriods[0];
        this.portfolioName.set(
          ServiceUtils.isPortfolioSymbol(mySymbol) ? mySymbol.name : "",
        );
        this.portfolioSymbol.set(
          ServiceUtils.isPortfolioSymbol(mySymbol) ? mySymbol.symbol : "",
        );
        this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
      }
    });

    effect(() => {
      const show = this.showSymbol();
      const loading = this.quotesLoading();
      if (!loading && show) {
        this.selQuotePeriod = !ServiceUtils.isIntraDayDataAvailiable(
          this.symbol(),
        )
          ? this.quotePeriods[1]
          : this.quotePeriods[0];
        this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
      }
    });
  }

  quotePeriodChanged() {
    this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
  }

  isIntraDayDataAvailiable(mySymbol: Symbol): boolean {
    return ServiceUtils.isIntraDayDataAvailiable(mySymbol);
  }

  compIndexUpdate(value: boolean, comparisonIndex: ComparisonIndex): void {
    if (value) {
      const currentPoints = this.chartPoints();
      const existing = currentPoints.filter(
        (myChartPoints) => myChartPoints.name === comparisonIndex,
      );
      if (existing.length > 0) {
        this.chartPoints.update((points) =>
          points.map((p) =>
            p.name === comparisonIndex
              ? { ...p, chartPointList: this.createChartPoints(comparisonIndex) }
              : p,
          ),
        );
      } else {
        this.chartPoints.update((points) => [
          ...points,
          {
            name: comparisonIndex,
            xScaleHeight: 20,
            yScaleWidth: 50,
            chartPointList: this.createChartPoints(comparisonIndex),
          } as ChartPoints,
        ]);
      }
    } else {
      this.chartPoints.update((points) =>
        points.filter((myChartPoints) => myChartPoints.name !== comparisonIndex),
      );
    }
  }

  private createChartPoints(comparisonIndex: ComparisonIndex): ChartPoint[] {
    return (
      this.compIndexes().get(comparisonIndex)?.map(
        (myQuote) =>
          ({
            x: new Date(Date.parse(myQuote.timestamp)),
            y: myQuote.close,
          }) as ChartPoint,
      ) || []
    );
  }

  private updateSymbolData(): void {
    const localQuotes = this.quotes();
    const safeQuotes =
      localQuotes && localQuotes.length > 0 ? localQuotes : null;
    this.symbolData.update((data) => ({
      ...data,
      start:
        safeQuotes && safeQuotes.length > 0
          ? new Date(safeQuotes[0].timestamp)
          : new Date(),
      end:
        safeQuotes && safeQuotes.length > 0
          ? new Date(safeQuotes[safeQuotes.length - 1].timestamp)
          : new Date(),
      open: safeQuotes && safeQuotes.length > 0 ? safeQuotes[0].open : 0,
      close:
        safeQuotes && safeQuotes.length > 0
          ? safeQuotes[safeQuotes.length - 1].close
          : 0,
      high:
        safeQuotes && safeQuotes.length > 0
          ? Math.max(...safeQuotes.map((quote) => quote.high))
          : 0,
      low:
        safeQuotes && safeQuotes.length > 0
          ? Math.min(...safeQuotes.map((quote) => quote.low))
          : 0,
      avgVolume:
        safeQuotes && safeQuotes.length > 0
          ? safeQuotes
              .map((quote) => quote.volume)
              .reduce((result, volume) => result + volume, 0) /
            safeQuotes.length
          : 0,
      accDividend:
        safeQuotes && safeQuotes.length > 0
          ? safeQuotes
              .map((quote) => quote.dividend)
              .reduce((result, dividend) => result + dividend, 0)
          : 0,
      avgClose:
        safeQuotes && safeQuotes.length > 0
          ? safeQuotes
              .map((quote) => quote.close)
              .reduce((result, close) => result + close, 0) /
            safeQuotes.length
          : 0,
      medianClose:
        safeQuotes && safeQuotes.length > 0
          ? safeQuotes.map((quote) => quote.close).sort((a, b) => a - b)[
              Math.round(safeQuotes.length / 2)
            ]
          : 0,
      volatilityClose: this.calcVolatility(safeQuotes ?? []),
    }));
  }

  private calcVolatility(localQuotes: Quote[]): number {
    if (!localQuotes || localQuotes.length < 1) {
      return 0;
    }
    const variances = [] as number[];
    for (let i = 1; i < localQuotes.length; i++) {
      const myVariance =
        Math.log(localQuotes[i].close) - Math.log(localQuotes[i - 1].close);
      variances.push(myVariance);
    }
    const realizedVariance = variances
      .map((localVar) => localVar * localVar)
      .reduce((acc, value) => acc + value, 0);
    return Math.sqrt(realizedVariance);
  }

  private updateChartData(): void {
    this.chartPoints.set([
      {
        name: this.symbol().symbol,
        chartPointList: this.createChartValues(),
        xScaleHeight: 20,
        yScaleWidth: 50,
      } as ChartPoints,
    ]);
    this.compIndexUpdate(this.showMsciCH(), ComparisonIndex.MSCI_CHINA);
    this.compIndexUpdate(this.showES50(), ComparisonIndex.EUROSTOXX50);
    this.compIndexUpdate(this.showSP500(), ComparisonIndex.SP500);
  }

  private createChartValues(): ChartPoint[] {
    return this.quotes().map(
      (quote) =>
        ({
          x: new Date(Date.parse(quote.timestamp)),
          y: quote.close,
        }) as ChartPoint,
    );
  }

  private updateQuotes(selPeriod: QuotePeriodKey): void {
    if (!this.symbol()) {
      return;
    }
    this.loadingData.emit(true);
    this.quotesLoading.set(true);
    const startDate = this.createStartDate(selPeriod);
    const endDate = new Date();
    this.quoteService
      .getDailyQuotesFromStartToEnd(this.symbol().symbol, startDate, endDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((myQuotes: Quote[]) => {
        this.quotes.set(myQuotes);
        this.updateSymbolData();
        if (ServiceUtils.isPortfolioSymbol(this.symbol().symbol)) {
          console.log("add comparison index quotes.");
          forkJoin([
            this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
              this.portfolioId(),
              ComparisonIndex.EUROSTOXX50,
              startDate,
              endDate,
            ),
            this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
              this.portfolioId(),
              ComparisonIndex.MSCI_CHINA,
              startDate,
              endDate,
            ),
            this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
              this.portfolioId(),
              ComparisonIndex.SP500,
              startDate,
              endDate,
            ),
          ])
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(
              ([myQuotesES50, myQuotesMsciCh, myQuotesSP500]: [
                Quote[],
                Quote[],
                Quote[],
              ]) => {
                const newMap = new Map(this.compIndexes());
                newMap.set(ComparisonIndex.EUROSTOXX50, myQuotesES50);
                newMap.set(ComparisonIndex.MSCI_CHINA, myQuotesMsciCh);
                newMap.set(ComparisonIndex.SP500, myQuotesSP500);
                this.compIndexes.set(newMap);
                this.updateChartData();
                this.loadingData.emit(false);
                this.quotesLoading.set(false);
              },
            );
        } else {
          this.showES50.set(false);
          this.showMsciCH.set(false);
          this.showSP500.set(false);
          this.updateChartData();
          this.loadingData.emit(false);
          this.quotesLoading.set(false);
        }
      });
  }

  private createStartDate(selPeriod: QuotePeriodKey): Date {
    const startDate = new Date();
    if (QuotePeriodKey.Month === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 1);
    } else if (QuotePeriodKey.Months3 === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 3);
    } else if (QuotePeriodKey.Months6 === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 6);
    } else if (QuotePeriodKey.Year === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 12);
    } else if (QuotePeriodKey.Year3 === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 36);
    } else if (QuotePeriodKey.Year5 === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 60);
    } else if (QuotePeriodKey.Year10 === selPeriod) {
      startDate.setMonth(startDate.getMonth() - 120);
    }
    return startDate;
  }
}
