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
import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder, AbstractControlOptions, FormControl, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { OverviewComponent } from '../overview/overview.component';
import { PortfolioData } from '../../model/portfolio-data';
import { Portfolio } from '../../model/portfolio';
import { Symbol } from '../../model/symbol';
import { SymbolService } from '../../service/symbol.service';
import { Observable, of, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap, switchMap, map } from 'rxjs/operators';
import { QuoteImportService } from '../../service/quote-import.service';

@Component({
  selector: 'app-add-symbol',
  templateUrl: './add-symbol.component.html',
  styleUrls: ['./add-symbol.component.scss']
})
export class AddSymbolComponent implements OnInit {
  private portfolio: Portfolio = null;
  symbolForm: FormGroup;  
  selSymbol: Symbol = null;
  symbolsName: Observable<Symbol[]> = of([]);
  symbolsSymbol: Observable<Symbol[]> = of([]);
  loading = false;
  importingQuotes = false;
  formValid = true;  
  symbolNameFormControl = new FormControl();
  symbolSymbolFormControl = new FormControl();

  constructor(public dialogRef: MatDialogRef<OverviewComponent>,
		@Inject(MAT_DIALOG_DATA) public data: PortfolioData,
		private symbolService: SymbolService,
		private quoteImportService: QuoteImportService,
		private fb: FormBuilder) { 
			this.symbolForm = fb.group({
				symbolSymbol: '',	
				symbolName: '',
				symbolWeight: 0,
				createdAt: [new Date(), Validators.required]
			}, {
				validators: [this.validate]
			} as AbstractControlOptions);
			this.portfolio = data.portfolio;
  }

  ngOnInit() {	
	this.symbolsName = this.symbolNameFormControl.valueChanges.pipe(
		debounceTime( 400 ),
        distinctUntilChanged(),
        tap(() => this.loading = true ),
        switchMap( name => name && name.length > 2 ? this.symbolService.getSymbolByName( name )
			.pipe(map(localSymbols => this.filterPortfolioSymbols(localSymbols))) : this.clearSymbol()),
        tap(() => this.loading = false )
	);
	this.symbolsSymbol = this.symbolSymbolFormControl.valueChanges.pipe(
		debounceTime( 400 ),
        distinctUntilChanged(),
        tap(() => this.loading = true ),
        switchMap( name => name && name.length > 2 ? this.symbolService.getSymbolBySymbol( name )
			.pipe(map(localSymbols => this.filterPortfolioSymbols(localSymbols))) : this.clearSymbol()),
        tap(() => this.loading = false )
	);
  }

  private filterPortfolioSymbols(symbols: Symbol[]): Symbol[] {
	return symbols.filter(symbol => this.portfolio.symbols.filter(mySymbol => symbol.symbol === mySymbol.symbol));
  }

  private clearSymbol(): Observable<Symbol[]> {
	this.selSymbol = null;
	return of([]) as Observable<Symbol[]>;
  }

  findSymbolByName() {
	const symbolNameStr = this.symbolNameFormControl.value;
	this.symbolService.getSymbolByName(symbolNameStr)
		.subscribe(mySymbols => {
			this.selSymbol = mySymbols.length > 0 ? mySymbols.filter(sym => sym.name === symbolNameStr)[0] : null;
			this.updateSymbolWeight();
		});
  }

  findSymbolBySymbol() {
	const symbolStr = this.symbolSymbolFormControl.value;
	this.symbolService.getSymbolBySymbol(symbolStr)
		.subscribe(mySymbols => {
			this.selSymbol = mySymbols.length > 0 ? mySymbols.filter(sym => sym.symbol === symbolStr)[0] : null;
			this.updateSymbolWeight();
		});
  }

  updateSymbolWeight() {
	if(this.selSymbol) {
		this.selSymbol.weight = this.symbolForm.controls['symbolWeight'].value;
	}
  }

  onAddClick(): void {
	if(this.selSymbol) {
		this.importingQuotes = true;
		this.selSymbol.weight = this.symbolForm.controls['symbolWeight'].value;
		const changedAt = this.symbolForm.controls['createdAt'].value as Date;
		this.selSymbol.changedAt = changedAt.toISOString();
		forkJoin(
			this.quoteImportService.importDailyQuotes(this.selSymbol.symbol),
			this.quoteImportService.importIntraDayQuotes(this.selSymbol.symbol))
		.subscribe(([resultDaily, resultIntraDay]) => {
			console.log(`Daily quotes: ${resultDaily}, Intraday quotes: ${resultIntraDay}`);
			this.importingQuotes = false;
			this.dialogRef.close(this.selSymbol);
		});
	}
  }

  onCancelClick(): void {
	this.dialogRef.close();
  }

  validate(formGroup: FormGroup) {
	/*
	if (formGroup.get('portfolioName').touched) {
		const myValue: string = formGroup.get('portfolioName').value;
		if(myValue && myValue.trim().length > 4) {
			formGroup.get('portfolioName').setErrors(null);
			this.formValid = true;
		} else {
			formGroup.get('portfolioName').setErrors({ MatchPassword: true });
			this.formValid = false;			
		}
	}
	return { MatchPassword: true } as ValidationErrors;
	return null;
	*/
  }

}
