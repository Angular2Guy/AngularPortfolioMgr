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
import { Component, Input, Output, EventEmitter, OnInit, Inject, LOCALE_ID, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { Symbol } from '../../model/symbol';
import { QuoteService } from '../../service/quote.service';
import { Quote } from '../../model/quote';
import { MyChartData, MyChartValue } from '../../model/my-chart-data';
import { DOCUMENT, formatDate } from '@angular/common';
import { ServiceUtils } from '../../model/service-utils';

const enum QuotePeriodKey { Day, Month, Months3, Months6, Year, Year3, Year5, Year10 }

interface QuotePeriod {
	periodText: string;
	quotePeriodKey: QuotePeriodKey;
}

interface SymbolData {
	start: Date,
	end: Date,
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
	selector: 'app-symbol',
	templateUrl: './symbol.component.html',
	styleUrls: ['./symbol.component.scss']
})
export class SymbolComponent implements OnInit {	
	private readonly dayInMs = 24 * 60 * 60 * 1000;
	private readonly hourInMs = 1 * 60 * 60 * 1000;
	quotePeriods: QuotePeriod[] = [];
	selQuotePeriod: QuotePeriod = null;
	private localSymbol: Symbol;
	quotes: Quote[] = [];
	symbolData = { avgVolume: null, close: null, end: null, high: null, low: null, 
		open: null, start: null, avgClose: null, medianClose: null, volatilityClose: null } as SymbolData;
	@Output()
	loadingData = new EventEmitter<boolean>();
	readonly quotePeriodKeyDay = QuotePeriodKey.Day;

	multi: MyChartData[] = [{ name: 'none', series: [] }];	
	legend = false;
	showLabels = true;
	animations = true;
	xAxis = true;
	yAxis = true;
	showYAxisLabel = true;
	showXAxisLabel = true;
	xAxisLabel: string = 'Time';
	yAxisLabel: string = 'Value';
	timeline = true;
	autoScale = true;

	constructor(private quoteService: QuoteService, @Inject(DOCUMENT) private document: Document, 
		@Inject(LOCALE_ID) private locale: string) { }

	ngOnInit(): void {
		this.quotePeriods = [{ quotePeriodKey: QuotePeriodKey.Day, periodText: this.document.getElementById('intraDay').textContent },
		{ quotePeriodKey: QuotePeriodKey.Month, periodText: this.document.getElementById('oneMonth').textContent },
		{ quotePeriodKey: QuotePeriodKey.Months3, periodText: this.document.getElementById('threeMonths').textContent },
		{ quotePeriodKey: QuotePeriodKey.Months6, periodText: this.document.getElementById('sixMonths').textContent },
		{ quotePeriodKey: QuotePeriodKey.Year, periodText: this.document.getElementById('oneYear').textContent },
		{ quotePeriodKey: QuotePeriodKey.Year3, periodText: this.document.getElementById('threeYears').textContent },
		{ quotePeriodKey: QuotePeriodKey.Year5, periodText: this.document.getElementById('fiveYears').textContent },
		{ quotePeriodKey: QuotePeriodKey.Year10, periodText: this.document.getElementById('tenYears').textContent }];
		this.selQuotePeriod = this.quotePeriods[0];
	}	

	replacePortfolioSymbol(symbolStr: string): string {
		return ServiceUtils.isPortfolioSymbol(symbolStr) ? 'SymbolId' : symbolStr;
	}

	quotePeriodChanged() {
		this.updateQuotes(this.selQuotePeriod.quotePeriodKey);
		console.log(this.selQuotePeriod);
	}

	isIntraDayDataAvailiable(mySymbol: Symbol): boolean {
		console.log(ServiceUtils.isIntraDayDataAvailiable(mySymbol));
		return ServiceUtils.isIntraDayDataAvailiable(mySymbol);
	}

	private updateSymbolData() {
		const localQuotes = this.quotes && this.quotes.length > 0 ? this.quotes.
			filter(myQuote => (this.selQuotePeriod.quotePeriodKey === QuotePeriodKey.Day && new Date(myQuote.timestamp).getTime() > new Date(this.quotes[this.quotes.length -1].timestamp).getTime() -this.dayInMs + this.hourInMs) 
			|| this.selQuotePeriod.quotePeriodKey !== QuotePeriodKey.Day) : null;
		this.symbolData.start = localQuotes && localQuotes.length > 0 ? new Date(localQuotes[0].timestamp) : null;
		this.symbolData.end = localQuotes && localQuotes.length > 0 ? new Date(localQuotes[localQuotes.length -1].timestamp) : null;			
		this.symbolData.open = localQuotes && localQuotes.length > 0 ? localQuotes[0].open : null;
		this.symbolData.close = localQuotes && localQuotes.length > 0 ? localQuotes[localQuotes.length - 1].close : null;
		this.symbolData.high = localQuotes && localQuotes.length > 0 ? Math.max(...localQuotes.map(quote => quote.high)) : null;
		this.symbolData.low = localQuotes && localQuotes.length > 0 ? Math.min(...localQuotes.map(quote => quote.low)) : null;
		this.symbolData.avgVolume = localQuotes && localQuotes.length > 0 ?
			(localQuotes.map(quote => quote.volume).reduce((result, volume) => result + volume, 0) / localQuotes.length) : null;
		this.symbolData.avgClose = localQuotes && localQuotes.length > 0 ?
			(localQuotes.map(quote => quote.close).reduce((result, close) => result + close, 0) / localQuotes.length) : null;
		this.symbolData.medianClose = localQuotes && localQuotes.length > 0 ? (localQuotes.map(quote => quote.close).sort((a,b) => a-b)[Math.round(localQuotes.length / 2)]) : null;
		this.symbolData.volatilityClose = this.calcVolatility(localQuotes); 
		this.updateChartData();
	}

	private calcVolatility(localQuotes: Quote[]): number {
		if(!localQuotes || localQuotes.length < 1) {
			return 0;
		}
		const variances = [];
		for(let i = 1; i < localQuotes.length;i++) {
			const myVariance = Math.log(localQuotes[i].close) - Math.log(localQuotes[i-1].close);
			variances.push(myVariance);
		}
		const realizedVariance = variances.map(localVar => localVar*localVar).reduce((acc, value) => acc + value, 0);
		return Math.sqrt(realizedVariance);
	}

	private updateChartData(): void {
		const myChartData = { name: this.symbol.symbol, series: this.createChartValues() } as MyChartData;
		this.multi = [myChartData];
		//console.log(this.multi);
	}

	private createChartValues(): MyChartValue[] {
		const dateFormatStr = this.selQuotePeriod.quotePeriodKey === QuotePeriodKey.Day ? 'mediumTime' : 'shortDate';		
		const myChartValues = this.quotes.
			filter(myQuote => (this.selQuotePeriod.quotePeriodKey === QuotePeriodKey.Day && new Date(myQuote.timestamp).getTime() > new Date(this.quotes[this.quotes.length -1].timestamp).getTime() -this.dayInMs + this.hourInMs) 
			|| this.selQuotePeriod.quotePeriodKey !== QuotePeriodKey.Day)
			.map(quote => ({ name: formatDate(quote.timestamp, dateFormatStr, this.locale), value: quote.close } as MyChartValue));
		return myChartValues;
	}

	private updateQuotes(selPeriod: QuotePeriodKey): void {
		if (!this.symbol) { return; }
		if (selPeriod === QuotePeriodKey.Day) {
			this.loadingData.emit(true);
			this.quoteService.getIntraDayQuotes(this.symbol.symbol)
				.subscribe(myQuotes => {										
					this.quotes = myQuotes;
					this.updateSymbolData();
					this.loadingData.emit(false);
				});
		} else {
			this.loadingData.emit(true);
			const startDate = this.createStartDate(selPeriod);
			const endDate = new Date();
			this.quoteService.getDailyQuotesFromStartToEnd(this.symbol.symbol, startDate, endDate)
				.subscribe(myQuotes => {
					this.quotes = myQuotes;
					this.updateSymbolData();
					this.loadingData.emit(false);
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
		if (mySymbol) {
			this.selQuotePeriod = !ServiceUtils.isIntraDayDataAvailiable(mySymbol) && this.selQuotePeriod === this.quotePeriods[0] ? this.quotePeriods[1] : this.selQuotePeriod;
			this.localSymbol = mySymbol;
			this.updateQuotes(!this.selQuotePeriod ? QuotePeriodKey.Day : this.selQuotePeriod.quotePeriodKey);
		}
	}

	get symbol(): Symbol {
		return this.localSymbol;
	}
}
