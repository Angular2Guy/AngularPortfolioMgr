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
import { Component, Input, Output, EventEmitter, OnInit, Inject } from '@angular/core';
import { Symbol } from '../../model/symbol';
import { QuoteService } from '../../service/quote.service';
import { Quote } from '../../model/quote';
import { tap } from 'rxjs/operators';
import { DOCUMENT } from '@angular/common';

const enum QuotePeriodKeys {Day, Month, Months3, Months6, Year, Year3, Year5, Year10}

interface QuotePeriod {
	periodText: string;
	quotePeriodKey: QuotePeriodKeys;
}

@Component({
  selector: 'app-symbol',
  templateUrl: './symbol.component.html',
  styleUrls: ['./symbol.component.scss']
})
export class SymbolComponent implements OnInit {
  quotePeriods: QuotePeriod[] = [];
  selQuotePeriod: QuotePeriod = null;
  private localSymbol: Symbol;
  quotes: Quote[] = [];  
  @Output()
  loadingData = new EventEmitter<boolean>();

  constructor(private quoteService: QuoteService, @Inject( DOCUMENT ) private document: Document) { }

  ngOnInit(): void { 
  	this.quotePeriods = [{quotePeriodKey: QuotePeriodKeys.Day, periodText: this.document.getElementById( 'intraDay' ).textContent},
						{quotePeriodKey: QuotePeriodKeys.Month, periodText: this.document.getElementById( 'oneMonth').textContent},
						{quotePeriodKey: QuotePeriodKeys.Months3, periodText: this.document.getElementById( 'threeMonths').textContent},
						{quotePeriodKey: QuotePeriodKeys.Months6, periodText: this.document.getElementById( 'sixMonths').textContent},
						{quotePeriodKey: QuotePeriodKeys.Year, periodText: this.document.getElementById( 'oneYear').textContent},
						{quotePeriodKey: QuotePeriodKeys.Year3, periodText: this.document.getElementById( 'threeYears').textContent},
						{quotePeriodKey: QuotePeriodKeys.Year5, periodText: this.document.getElementById( 'fiveYears').textContent},
						{quotePeriodKey: QuotePeriodKeys.Year10, periodText: this.document.getElementById( 'tenYears').textContent}];
	this.selQuotePeriod = this.quotePeriods[0];
  }

  private updateQuotes(selPeriod: QuotePeriodKeys): void {	
	if(selPeriod === QuotePeriodKeys.Day) {
		this.loadingData.emit(true);
		this.quoteService.getIntraDayQuotes(this.symbol.symbol)		  
 			.subscribe(myQuotes => { 
				this.quotes = myQuotes; 
				this.loadingData.emit(false);
			});
	}
  }  

  @Input()
  set symbol(mySymbol: Symbol) {
	if(mySymbol) {
	  this.localSymbol = mySymbol;
	  this.updateQuotes(!this.selQuotePeriod ? QuotePeriodKeys.Day : this.selQuotePeriod.quotePeriodKey);
	}
  }

  get symbol(): Symbol {
	return this.localSymbol;
  }
}
