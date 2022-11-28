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
import { FormGroup, FormBuilder, AbstractControlOptions, Validators, ValidationErrors } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';

export interface MyItem {
	id: number;
	title: string;
}

enum FormFields {
	TermOperator = 'termOperator',
	ConceptOperator = 'conceptOperator',
	Concept = 'concept',
	NumberOperator = 'numberOperator',
	NumberValue = 'numberValue',
	QueryItems = 'queryItems'
}

@Component({
  selector: 'app-create-query',
  templateUrl: './create-query.component.html',
  styleUrls: ['./create-query.component.scss']
})
export class CreateQueryComponent implements OnInit {
  private readonly availableInit: MyItem[] = [{id: 1, title: 'Query'}, {id: 2, title: 'And Term'}, {id: 3, title: 'And Not Term'}, 
     {id: 4, title: 'Or Term'}, {id: 5, title: 'Or Not Term'}];
  protected queryForm: FormGroup; 
  protected availableItems: MyItem[] = [];
  protected queryItems: MyItem[] = [{id: 1, title: 'Query'}];
  protected termQueryItems = ['And', 'AndNot', 'Or', 'OrNot'];
  protected stringQueryItems: string[] =  ['=', '=*', '*=', '*=*'];
  protected numberQueryItems: string[] =  ['=', '>=', '<='];
  protected readonly conceptsInit: string[] = ['AAA','BBB','CCC']; 
  protected concepts: string[] = [];
  protected FormFields = FormFields;

  constructor(private fb: FormBuilder) { 
			this.queryForm = fb.group({
				[FormFields.TermOperator]: this.termQueryItems[0],
				[FormFields.ConceptOperator]: this.stringQueryItems[0],
				[FormFields.Concept]: [this.conceptsInit[0], [Validators.required]],
				[FormFields.NumberOperator]: this.numberQueryItems[0],
				[FormFields.NumberValue]: [0, [Validators.required, Validators.pattern('^[+-]?(\\d+[\\,\\.])*\\d+$')]],
				[FormFields.QueryItems]: fb.array([])
			}
			/*
			, {
				validators: [this.validate]
			} 
			as AbstractControlOptions
			*/
			);
	}

  ngOnInit(): void {
	this.availableInit.forEach(myItem => this.availableItems.push(myItem));
	this.conceptsInit.forEach(myConcept => this.concepts.push(myConcept));
	this.queryForm.controls[FormFields.Concept].valueChanges.subscribe(myValue => 
	   this.concepts = this.conceptsInit.filter(myConcept => myConcept.includes(myValue)));	
  }

  conceptSelected(event: MatAutocompleteSelectedEvent): void {
	console.log(event.option.value);
	console.log(this.queryForm.controls[FormFields.Concept].value);
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
