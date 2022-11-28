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
import { Component, OnInit } from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import { FormGroup, FormArray, FormBuilder, AbstractControlOptions, Validators, ValidationErrors } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { FinancialsDataUtils, ItemType } from '../../model/financials-data-utils';
import { Subscription } from 'rxjs';

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
export class CreateQueryComponent implements OnInit {
  private readonly availableInit: MyItem[] = [{queryItemType: ItemType.Query, title: 'Query'}, 
     {queryItemType: ItemType.TermStart, title: 'Term Start'}, {queryItemType: ItemType.TermEnd, title: 'Term End'}];
  private symbolSubscription: Subscription;
  protected readonly availableItemParams = {showType: true, formArray: null, formArrayIndex: -1 } as ItemParams;
  protected readonly queryItemParams = {showType: false, formArray: {}, formArrayIndex: -1 } as ItemParams;
  protected queryForm: FormGroup; 
  protected availableItems: MyItem[] = [];
  protected queryItems: MyItem[] = [{queryItemType: ItemType.Query, title: 'Query'}];
  protected termQueryItems = ['And', 'AndNot', 'Or', 'OrNot'];
  protected stringQueryItems: string[] =  ['=', '=*', '*=', '*=*'];
  protected numberQueryItems: string[] =  ['=', '>=', '<='];
  protected quarterQueryItems: string[] = ['FY', 'CY', 'Q1', 'Q2', 'Q3', 'Q4'];
  protected readonly symbolsInit = ['IBM', 'JNJ', 'MSFT', 'AMZN'];
  protected symbols = [];
  protected FormFields = FormFields;

  constructor(private fb: FormBuilder) { 
			this.queryForm = fb.group({
				[FormFields.YearOperator]: this.termQueryItems[0],
				[FormFields.Year]: [0, Validators.pattern('^\\d*$')],
				[FormFields.SymbolOperator]: this.stringQueryItems[0],
				[FormFields.Symbol]: '',
				[FormFields.QuarterOperator]: this.numberQueryItems[0],
				[FormFields.Quarter]: this.quarterQueryItems[0],
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
	this.symbolsInit.forEach(mySymbol => this.symbols.push(mySymbol));
	this.availableInit.forEach(myItem => this.availableItems.push(myItem));
	this.symbolSubscription = this.queryForm.controls[FormFields.Symbol].valueChanges.subscribe(myValue => 
	   this.symbols = this.symbolsInit.filter(myConcept => myConcept.includes(myValue)));	
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
  
  private validate(): void {
	
  }
}
