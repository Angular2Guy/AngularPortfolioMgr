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
import { Component, OnInit, OnDestroy } from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import { FormGroup, FormArray, FormBuilder, AbstractControlOptions, Validators, ValidationErrors } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { FinancialsDataUtils, ItemType } from '../../model/financials-data-utils';
import { SymbolFinancials } from '../../model/symbol-financials';
import { SymbolFinancialsQueryParams, FinancialElementParams, FilterNumber, FilterString } from '../../model/symbol-financials-query-params';
import { Subscription, Observable } from 'rxjs';
import { switchMap,debounceTime } from 'rxjs/operators';
import { SymbolService } from 'src/app/service/symbol.service';
import { ConfigService } from 'src/app/service/config.service';
import {FinancialDataService} from '../../service/financial-data.service';
import {QueryFormFields} from '../query/query.component';
import {Symbol} from 'src/app/model/symbol';

export interface MyItem {	
	queryItemType: ItemType;
	title: string;
}

export interface ItemParams {
	showType: boolean;
	formArray: FormArray;
	formArrayIndex: number;
}

enum FormFields {
	YearOperator = 'yearOperator',
	Year = 'year',
	SymbolOperator = 'symbolOperator',
	Symbol = 'symbol',
	QuarterOperator = 'quarterOperator',
	Quarter = 'quarter',
	QueryItems = 'queryItems'
}

@Component({
  selector: 'app-create-query',
  templateUrl: './create-query.component.html',
  styleUrls: ['./create-query.component.scss']
})
export class CreateQueryComponent implements OnInit, OnDestroy {
  private readonly availableInit: MyItem[] = [{queryItemType: ItemType.Query, title: 'Query'}, 
     {queryItemType: ItemType.TermStart, title: 'Term Start'}, {queryItemType: ItemType.TermEnd, title: 'Term End'}];
  private symbolSubscription: Subscription;
  private subscriptions: Subscription[] = [];
  protected readonly availableItemParams = {showType: true, formArray: null, formArrayIndex: -1 } as ItemParams;
  protected readonly queryItemParams = {showType: false, formArray: {}, formArrayIndex: -1 } as ItemParams;
  protected queryForm: FormGroup; 
  protected availableItems: MyItem[] = [];
  protected queryItems: MyItem[] = [{queryItemType: ItemType.Query, title: 'Query'}];
  protected numberQueryItems: string[] =  [];
  protected quarterQueryItems: string[] = [];
  protected symbols:Symbol[] = [];
  protected FormFields = FormFields;

  constructor(private fb: FormBuilder, private symbolService: SymbolService, private configService: ConfigService, 
     private financialDataService: FinancialDataService) { 
			this.queryForm = fb.group({
				[FormFields.YearOperator]: '',
				[FormFields.Year]: [0, Validators.pattern('^\\d*$')],
				[FormFields.Symbol]: '',
				[FormFields.Quarter]: [''],
				[FormFields.QueryItems]: fb.array([])
			}
			/*
			, {
				validators: [this.validate]
			} 
			as AbstractControlOptions
			*/
			);
			this.queryItemParams.formArray = this.queryForm.controls[FormFields.QueryItems] as FormArray;
	}

  ngOnInit(): void {
	this.availableInit.forEach(myItem => this.availableItems.push(myItem));
	this.subscriptions.push(this.queryForm.controls[FormFields.Symbol].valueChanges
	   .pipe(debounceTime(200),switchMap(myValue => this.symbolService.getSymbolBySymbol(myValue)))
	   .subscribe(myValue => this.symbols = myValue));	
	this.subscriptions.push(this.configService.getNumberOperators().subscribe(values => {
		this.numberQueryItems = values;
		this.queryForm.controls[FormFields.YearOperator].patchValue(values.filter(myValue => myValue === '=')[0]);
	}));	
	this.subscriptions.push(this.financialDataService.getQuarters().subscribe(values => 
	   this.quarterQueryItems = (values.map(myValue => myValue.quarter))));
  }

  ngOnDestroy(): void {
	this.subscriptions.forEach(value => value.unsubscribe());
	this.subscriptions = null;
  }

  drop(event: CdkDragDrop<MyItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      //console.log(event.container.data === this.todo);
      while(this.availableItems.length > 0) {
	     this.availableItems.pop();
      }
      this.availableInit.forEach(myItem => this.availableItems.push(myItem));
    }
  }
  
  public search(): void {		
	console.log(this.queryForm.controls[FormFields.QueryItems].value);
	const symbolFinancials = {
		yearFilter: {
			operation: this.queryForm.controls[FormFields.YearOperator].value,
			value: !this.queryForm.controls[FormFields.Year].value ? 0 : parseInt(this.queryForm.controls[FormFields.Year].value)
		} as FilterNumber,
		quarters: !this.queryForm.controls[FormFields.Quarter].value ? [] : this.queryForm.controls[FormFields.Quarter].value,
		symbol: this.queryForm.controls[FormFields.Symbol].value,
		financialElementParams: !!this.queryForm.controls[FormFields.QueryItems]?.value?.length ? 
		    this.queryForm.controls[FormFields.QueryItems].value.map(myFormGroup => this.createFinancialElementParam(myFormGroup)) : []
	} as SymbolFinancialsQueryParams;
	//console.log(symbolFinancials);
	this.financialDataService.postSymbolFinancialsParam(symbolFinancials).subscribe(result => console.log(result));
  }
  
  private createFinancialElementParam(formGroup: FormGroup): FinancialElementParams {
		//console.log(formGroup);
		return {
			conceptFilter: {
				operation: formGroup[QueryFormFields.ConceptOperator],
				value: formGroup[QueryFormFields.Concept]
			},
			valueFilter: {
				operation: formGroup[QueryFormFields.NumberOperator],
				value: formGroup[QueryFormFields.NumberValue]
			},
			operation: formGroup[QueryFormFields.QueryOperator],
			termType: formGroup[QueryFormFields.ItemType]
		} as FinancialElementParams;
  }
  
  private validate(): void {
	
  }
}
