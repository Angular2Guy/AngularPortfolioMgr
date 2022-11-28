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
import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { FinancialsDataUtils, ItemType } from '../../model/financials-data-utils';
import { FormArray, FormGroup, FormBuilder, AbstractControl, AbstractControlOptions, Validators, ValidationErrors } from '@angular/forms';
import { Subscription } from 'rxjs';

enum FormFields {
	TermOperator = 'termOperator',
	ConceptOperator = 'conceptOperator',
	Concept = 'concept',
	NumberOperator = 'numberOperator',
	NumberValue = 'numberValue'
}

@Component({
  selector: 'app-query',
  templateUrl: './query.component.html',
  styleUrls: ['./query.component.scss']
})
export class QueryComponent implements OnInit, OnDestroy {
  @Input()
  public baseFormArray: FormArray; 
  @Input()
  public formArrayIndex: number;
  @Input()
  public queryItemType: ItemType;   
  private _showType: boolean;
  protected termQueryItems = ['And', 'AndNot', 'Or', 'OrNot'];
  protected stringQueryItems: string[] =  ['=', '=*', '*=', '*=*'];
  protected numberQueryItems: string[] =  ['=', '>=', '<='];
  protected readonly conceptsInit: string[] = ['AAA','BBB','CCC'];   
  protected concepts: string[] = [];
  protected FormFields = FormFields;
  protected itemFormGroup: FormGroup;
  protected ItemType = ItemType;
  private conceptSubscription: Subscription;
	
  constructor(private fb: FormBuilder) { 
			this.itemFormGroup = fb.group({
				[FormFields.TermOperator]: this.termQueryItems[0],
				[FormFields.ConceptOperator]: this.stringQueryItems[0],
				[FormFields.Concept]: [this.conceptsInit[0], [Validators.required]],
				[FormFields.NumberOperator]: this.numberQueryItems[0],
				[FormFields.NumberValue]: [0, [Validators.required, Validators.pattern('^[+-]?(\\d+[\\,\\.])*\\d+$')]]
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
	if(!this.showType) {
		this.baseFormArray.insert(this.formArrayIndex ,this.itemFormGroup);
	}
	this.conceptsInit.forEach(myConcept => this.concepts.push(myConcept));
	this.conceptSubscription = this.itemFormGroup.controls[FormFields.Concept].valueChanges.subscribe(myValue => 
	   this.concepts = this.conceptsInit.filter(myConcept => myConcept.includes(myValue)));	
  }
  
  ngOnDestroy(): void {
	if(!this.showType) {
	   this.baseFormArray.removeAt(this.formArrayIndex);
	}
	this.conceptSubscription.unsubscribe();
	this.conceptSubscription = null;
  }
  
  get showType(): boolean {
	return this._showType;
  }
  
  @Input()
  set showType(showType: boolean) {
	this._showType = showType;
	if(!this.showType) {
		const formIndex = this?.baseFormArray?.controls?.findIndex(myControl => myControl === this.itemFormGroup) || -1;
		if(formIndex >= 0) {
			this.baseFormArray.insert(this.formArrayIndex ,this.itemFormGroup);
		}
	} else {
		const formIndex = this?.baseFormArray?.controls?.findIndex(myControl => myControl === this.itemFormGroup) || -1;
		if(formIndex >= 0) {
			this.baseFormArray.removeAt(formIndex);
		}
	}
  }
}
