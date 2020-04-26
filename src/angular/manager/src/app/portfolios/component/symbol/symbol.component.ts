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
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Symbol } from '../../model/symbol';
import { QuoteService } from '../../service/quote.service';
import { Quote } from '../../model/quote';
import { tap } from 'rxjs/operators';

const enum QuotePeriod {Day, Month, Months3, Months6, Year, Year3, Year5, Year10}

@Component({
  selector: 'app-symbol',
  templateUrl: './symbol.component.html',
  styleUrls: ['./symbol.component.scss']
})
export class SymbolComponent {
  
  private localSymbol: Symbol;
  quotes: Quote[] = [];  
  @Output()
  loadingData = new EventEmitter<boolean>();

  constructor(private quoteService: QuoteService) { }

  private updateQuotes(selPeriod: QuotePeriod): void {
	if(selPeriod === QuotePeriod.Day) {
		this.quoteService.getIntraDayQuotes(this.symbol.symbol)
		  .pipe(tap(() => this.loadingData.emit(true)))
 			.subscribe(myQuotes => { 
				this.quotes = myQuotes; 
				this.loadingData.emit(false);
			});
	}
  }  

  @Input()
  set symbol(symbol: Symbol) {
	this.localSymbol = symbol;
	this.updateQuotes(QuotePeriod.Day);
  }

  get symbol(): Symbol {
	return this.localSymbol;
  }
}
