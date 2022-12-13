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
  Input,
  Output,
  EventEmitter,
  OnInit,
  Inject,
  LOCALE_ID,
  ViewChild,
  ElementRef,
  AfterViewInit,
} from "@angular/core";
import { Symbol } from "../../../../model/symbol";
import {
  QuoteService,
  ComparisonIndex,
} from "../../../../service/quote.service";
import { Quote } from "../../../../model/quote";
import { DOCUMENT, formatDate } from "@angular/common";
import { ServiceUtils } from "../../../../model/service-utils";
import { forkJoin } from "rxjs";
import { ChartPoint, ChartPoints } from "ngx-simple-charts/line";

const enum QuotePeriodKey {
  Day,
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
  avgVolume: number;
  avgClose: number;
  medianClose: number;
  volatilityClose: number;
}

@Component({
  selector: "app-symbol",
  templateUrl: "./symbol.component.html",
  styleUrls: ["./symbol.component.scss"],
})
export class SymbolComponent implements OnInit {
  private readonly dayInMs = 24 * 60 * 60 * 1000;
  private readonly hourInMs = 1 * 60 * 60 * 1000;
  @Input()
  portfolioId: number;
  private localShowSymbol: boolean;
  quotePeriods: QuotePeriod[] = [];
  selQuotePeriod: QuotePeriod = null;
  private localSymbol: Symbol;
  quotes: Quote[] = [];
  compIndexes = new Map<string, Quote[]>([
    [ComparisonIndex.SP500, []],
    [ComparisonIndex.EUROSTOXX50, []],
    [ComparisonIndex.MSCI_CHINA, []],
  ]);
  quotesLoading = true;
  symbolData = {
    avgVolume: null,
    close: null,
    end: null,
    high: null,
    low: null,
    open: null,
    start: null,
    avgClose: null,
    medianClose: null,
    volatilityClose: null,
  } as SymbolData;
  @Output()
  loadingData = new EventEmitter<boolean>();
  readonly quotePeriodKeyDay = QuotePeriodKey.Day;
  readonly ComparisonIndex = ComparisonIndex;
  showSP500 = false;
  showMsciCH = false;
  showES50 = false;
  chartPoints: ChartPoints[] = [
    {
      chartPointList: [],
      name: "",
      xScaleHeight: 20,
      yScaleWidth: 50,
    } as ChartPoints,
  ];
  portfolioName: string = null;
  portfolioSymbol: string = null;
  serviceUtils = ServiceUtils;

  constructor(
    private quoteService: QuoteService,
    @Inject(DOCUMENT) private document: Document,
    @Inject(LOCALE_ID) private locale: string
  ) {
    this.quotePeriods = [
      {
        quotePeriodKey: QuotePeriodKey.Day,
        periodText: $localize`:@@intraDay:IntraDay`,
      },
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
  }

  ngOnInit(): void {
    console.log(this.selQuotePeriod);
  }

  quotePeriodChanged() {
    this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
    //console.log(this.selQuotePeriod);
  }

  isIntraDayDataAvailiable(mySymbol: Symbol): boolean {
    //console.log(ServiceUtils.isIntraDayDataAvailiable(mySymbol));
    return ServiceUtils.isIntraDayDataAvailiable(mySymbol);
  }

  compIndexUpdate(value: boolean, comparisonIndex: ComparisonIndex): void {
    if (value) {
      if (
        this.chartPoints.filter(
          (myChartPoints) => myChartPoints.name === comparisonIndex
        ).length > 0
      ) {
        this.chartPoints.filter(
          (myChartPoints) => myChartPoints.name === comparisonIndex
        )[0].chartPointList = this.createChartPoints(comparisonIndex);
        this.chartPoints = [...this.chartPoints];
      } else {
        this.chartPoints.push({
          name: comparisonIndex,
          xScaleHeight: 20,
          yScaleWidth: 50,
          chartPointList: this.createChartPoints(comparisonIndex),
        } as ChartPoints);
        this.chartPoints = [...this.chartPoints];
      }
    } else {
      this.chartPoints = this.chartPoints.filter(
        (myChartPoints) => myChartPoints.name !== comparisonIndex
      );
    }
    //console.log(`Value: ${value}, ComparisonIndex: ${comparisonIndex}`);
    //console.log(this.chartPoints);
  }

  private createChartPoints(comparisonIndex: ComparisonIndex): ChartPoint[] {
    return this.compIndexes.get(comparisonIndex).map(
      (myQuote) =>
        ({
          x: new Date(Date.parse(myQuote.timestamp)),
          y: myQuote.close,
        } as ChartPoint)
    );
  }

  private updateSymbolData(): void {
    const localQuotes =
      this.quotes && this.quotes.length > 0
        ? this.quotes.filter(
            (myQuote) =>
              (this.selQuotePeriod.quotePeriodKey === QuotePeriodKey.Day &&
                new Date(myQuote.timestamp).getTime() >
                  new Date(
                    this.quotes[this.quotes.length - 1].timestamp
                  ).getTime() -
                    this.dayInMs +
                    this.hourInMs) ||
              this.selQuotePeriod.quotePeriodKey !== QuotePeriodKey.Day
          )
        : null;
    this.symbolData.start =
      localQuotes && localQuotes.length > 0
        ? new Date(localQuotes[0].timestamp)
        : null;
    this.symbolData.end =
      localQuotes && localQuotes.length > 0
        ? new Date(localQuotes[localQuotes.length - 1].timestamp)
        : null;
    this.symbolData.open =
      localQuotes && localQuotes.length > 0 ? localQuotes[0].open : null;
    this.symbolData.close =
      localQuotes && localQuotes.length > 0
        ? localQuotes[localQuotes.length - 1].close
        : null;
    this.symbolData.high =
      localQuotes && localQuotes.length > 0
        ? Math.max(...localQuotes.map((quote) => quote.high))
        : null;
    this.symbolData.low =
      localQuotes && localQuotes.length > 0
        ? Math.min(...localQuotes.map((quote) => quote.low))
        : null;
    this.symbolData.avgVolume =
      localQuotes && localQuotes.length > 0
        ? localQuotes
            .map((quote) => quote.volume)
            .reduce((result, volume) => result + volume, 0) / localQuotes.length
        : null;
    this.symbolData.avgClose =
      localQuotes && localQuotes.length > 0
        ? localQuotes
            .map((quote) => quote.close)
            .reduce((result, close) => result + close, 0) / localQuotes.length
        : null;
    this.symbolData.medianClose =
      localQuotes && localQuotes.length > 0
        ? localQuotes.map((quote) => quote.close).sort((a, b) => a - b)[
            Math.round(localQuotes.length / 2)
          ]
        : null;
    this.symbolData.volatilityClose = this.calcVolatility(localQuotes);
  }

  private calcVolatility(localQuotes: Quote[]): number {
    if (!localQuotes || localQuotes.length < 1) {
      return 0;
    }
    const variances = [];
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
    this.chartPoints = [
      {
        name: this.symbol.symbol,
        chartPointList: this.createChartValues(),
        xScaleHeight: 20,
        yScaleWidth: 50,
      } as ChartPoints,
    ];
    this.compIndexUpdate(this.showMsciCH, ComparisonIndex.MSCI_CHINA);
    this.compIndexUpdate(this.showES50, ComparisonIndex.EUROSTOXX50);
    this.compIndexUpdate(this.showSP500, ComparisonIndex.SP500);
    this.chartPoints = [...this.chartPoints];
    //console.log(this.chartPoints);
  }

  private createChartValues(): ChartPoint[] {
    const myChartValues = this.quotes
      .filter(
        (myQuote) =>
          (this.selQuotePeriod.quotePeriodKey === QuotePeriodKey.Day &&
            new Date(myQuote.timestamp).getTime() >
              new Date(
                this.quotes[this.quotes.length - 1].timestamp
              ).getTime() -
                this.dayInMs +
                this.hourInMs) ||
          this.selQuotePeriod.quotePeriodKey !== QuotePeriodKey.Day
      )
      .map(
        (quote) =>
          ({
            x: new Date(Date.parse(quote.timestamp)),
            y: quote.close,
          } as ChartPoint)
      );
    return myChartValues;
  }

  private updateQuotes(selPeriod: QuotePeriodKey): void {
    if (!this.symbol) {
      return;
    }
    if (selPeriod === QuotePeriodKey.Day) {
      this.loadingData.emit(true);
      this.quotesLoading = true;
      this.quoteService
        .getIntraDayQuotes(this.symbol.symbol)
        .subscribe((myQuotes) => {
          this.quotes = myQuotes;
          this.updateSymbolData();
          this.updateChartData();
          this.loadingData.emit(false);
          this.quotesLoading = false;
        });
    } else {
      this.loadingData.emit(true);
      this.quotesLoading = true;
      const startDate = this.createStartDate(selPeriod);
      const endDate = new Date();
      this.quoteService
        .getDailyQuotesFromStartToEnd(this.symbol.symbol, startDate, endDate)
        .subscribe((myQuotes) => {
          this.quotes = myQuotes;
          this.updateSymbolData();
          if (ServiceUtils.isPortfolioSymbol(this.symbol.symbol)) {
            console.log("add comparison index quotes.");
            forkJoin([
              this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
                this.portfolioId,
                ComparisonIndex.EUROSTOXX50,
                startDate,
                endDate
              ),
              this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
                this.portfolioId,
                ComparisonIndex.MSCI_CHINA,
                startDate,
                endDate
              ),
              this.quoteService.getDailyQuotesForComparisonIndexFromStartToEnd(
                this.portfolioId,
                ComparisonIndex.SP500,
                startDate,
                endDate
              ),
            ]).subscribe(([myQuotesES50, myQuotesMsciCh, myQuotesSP500]) => {
              this.compIndexes.set(ComparisonIndex.EUROSTOXX50, myQuotesES50);
              this.compIndexes.set(ComparisonIndex.MSCI_CHINA, myQuotesMsciCh);
              this.compIndexes.set(ComparisonIndex.SP500, myQuotesSP500);
              this.updateChartData();
              this.loadingData.emit(false);
              this.quotesLoading = false;
            });
          } else {
            this.showES50 = false;
            this.showMsciCH = false;
            this.showSP500 = false;
            this.updateChartData();
            this.loadingData.emit(false);
            this.quotesLoading = false;
          }
        });
    }
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

  @Input()
  set symbol(mySymbol: Symbol) {
    if (!!mySymbol) {
      this.selQuotePeriod = !ServiceUtils.isIntraDayDataAvailiable(mySymbol)
        ? this.quotePeriods[1]
        : this.quotePeriods[0];
      this.localSymbol = mySymbol;
      this.portfolioName = ServiceUtils.isPortfolioSymbol(mySymbol)
        ? mySymbol.name
        : null;
      this.portfolioSymbol = ServiceUtils.isPortfolioSymbol(mySymbol)
        ? mySymbol.symbol
        : null;
      this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
    }
  }

  get symbol(): Symbol {
    return this.localSymbol;
  }

  @Input()
  set showSymbol(showSymbol: boolean) {
    if (
      !this.quotesLoading &&
      !!showSymbol &&
      this.localShowSymbol !== showSymbol
    ) {
      this.selQuotePeriod = !ServiceUtils.isIntraDayDataAvailiable(this.symbol)
        ? this.quotePeriods[1]
        : this.quotePeriods[0];
      this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
    }
    this.localShowSymbol = showSymbol;
  }

  get showSymbol(): boolean {
    return this.showSymbol;
  }
}
