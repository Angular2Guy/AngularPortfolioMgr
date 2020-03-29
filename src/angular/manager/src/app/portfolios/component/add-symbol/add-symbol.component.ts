import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder, AbstractControlOptions, FormControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { OverviewComponent } from '../overview/overview.component';
import { PortfolioData } from '../../model/portfolio-data';
import { Portfolio } from '../../model/portfolio';
import { Symbol } from '../../model/symbol';
import { SymbolService } from '../../service/symbol.service';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap, switchMap, filter } from 'rxjs/operators';

@Component({
  selector: 'app-add-symbol',
  templateUrl: './add-symbol.component.html',
  styleUrls: ['./add-symbol.component.scss']
})
export class AddSymbolComponent implements OnInit {
  private portfolio: Portfolio = null;
  symbolForm: FormGroup;  
  selSymbol: Symbol = null;
  symbols: Observable<Symbol[]> = of([]);
  loading = false;
  formValid = true;
  symbolFormControl = new FormControl();

  constructor(public dialogRef: MatDialogRef<OverviewComponent>,
		@Inject(MAT_DIALOG_DATA) public data: PortfolioData,
		private symbolService: SymbolService,
		private fb: FormBuilder) { 
			this.symbolForm = fb.group({
				symbolSymbol: '',	
				symbolName: ''
			}, {
				validators: [this.validate]
			} as AbstractControlOptions);
			this.portfolio = data.portfolio;
  }

  ngOnInit() {	
	this.symbols = this.symbolFormControl.valueChanges.pipe(
		debounceTime( 400 ),
        distinctUntilChanged(),
        tap(() => this.loading = true ),
        switchMap( name => name && name.length > 2 ? this.symbolService.getSymbolByName( name ) : of([])),
        tap(() => this.loading = false )
	);
  }

  findSymbolByName() {
	this.symbolService.getSymbolByName(this.symbolFormControl.value)
		.subscribe(mySymbols => this.selSymbol = mySymbols.length === 1 ? mySymbols[0] : null);
  }

  findSymbolBySymbol() {
	this.symbolService.getSymbolBySymbol(this.symbolForm.controls.symbolSymbol.value)
	  .subscribe(mySymbol => this.selSymbol = mySymbol);
  }

  onAddClick(): void {
		//const portfolio: Portfolio = {id: null, month1: null, months6: null, name: this.portfolioForm.get('portfolioName').value, 
		//	symbols: [], userId: this.tokenService.userId, year1: null, year10: null, year2: null, year5: null }; 		
		this.dialogRef.close(this.portfolio);		
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
