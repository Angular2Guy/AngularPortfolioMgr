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
import { Component, Input, OnInit } from '@angular/core';
import { DateTime, Duration } from 'luxon';
import { Portfolio } from 'src/app/model/portfolio';
import { PortfolioService } from 'src/app/service/portfolio.service';
import { ComparisonIndex } from 'src/app/service/quote.service';

const enum ChartPeriodKey {Month, Months3, Months6, Year, Year3, Year5, Year10 }

interface ChartPeriod {
	periodText: string;
	periodDuration: any;
	chartPeriodKey: ChartPeriodKey;
}

@Component({
  selector: 'app-portfolio-charts',
  templateUrl: './portfolio-charts.component.html',
  styleUrls: ['./portfolio-charts.component.scss']
})
export class PortfolioChartsComponent implements OnInit {
  @Input()
  selPortfolio: Portfolio;
  startDate: Date;
  chartPeriods: ChartPeriod[] = [];
  myChartPeriod: ChartPeriod;
  chartsLoading: true;
  readonly ComparisonIndex = ComparisonIndex;
  readonly compIndexes = new Map<string, number>([[ComparisonIndex.SP500, 0], [ComparisonIndex.EUROSTOXX50, 0], [ComparisonIndex.MSCI_CHINA, 0]]);
  showSP500 = false;
  showMsciCH = false;
  showES50 = false;  
  selChartPeriod: ChartPeriod = null;

  constructor(private portfolioService: PortfolioService) { }

  ngOnInit(): void {
	this.chartPeriods = [{ chartPeriodKey: ChartPeriodKey.Month, periodText: $localize`:@@oneMonth:1 Month`, periodDuration:  {months: 1} },
		{ chartPeriodKey: ChartPeriodKey.Months3, periodText: $localize`:@@threeMonths:3 Months`, periodDuration:  {months: 3} },
		{ chartPeriodKey: ChartPeriodKey.Months6, periodText: $localize`:@@sixMonths:6 Months`, periodDuration:  {months: 6} },
		{ chartPeriodKey: ChartPeriodKey.Year, periodText: $localize`:@@oneYear:1 Year`, periodDuration:  {years: 1} },
		{ chartPeriodKey: ChartPeriodKey.Year3, periodText: $localize`:@@threeYears:3 Years`, periodDuration:  {years: 3} },
		{ chartPeriodKey: ChartPeriodKey.Year5, periodText: $localize`:@@fiveYears:5 Years`, periodDuration:  {years: 5} },
		{ chartPeriodKey: ChartPeriodKey.Year10, periodText: $localize`:@@tenYears:10 Years`, periodDuration:  {years: 10} }];
		this.selChartPeriod = this.chartPeriods[0];	
	this.portfolioService.getPortfolioBarsByIdAndStart(this.selPortfolio.id, DateTime.now().minus(this.selChartPeriod.periodDuration).toJSDate()).subscribe(result => console.log(result));
  }

  chartPeriodChanged(): void {
	this.portfolioService.getPortfolioBarsByIdAndStart(this.selPortfolio.id, DateTime.now().minus(this.selChartPeriod.periodDuration).toJSDate()).subscribe(result => console.log(result));	
  }

  compIndexUpdate(value: boolean, comparisonIndex: ComparisonIndex): void {
	
  }
}