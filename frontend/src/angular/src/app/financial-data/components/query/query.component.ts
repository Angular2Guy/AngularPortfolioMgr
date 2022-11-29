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
import { ConfigService } from 'src/app/service/config.service';

enum FormFields {
	QueryOperator = 'queryOperator',
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
  protected termQueryItems: string[] = [];
  protected stringQueryItems: string[] = [];
  protected numberQueryItems: string[] = [];
  protected readonly conceptsInit: string[] = ['AAA','BBB','CCC'];   
  protected concepts: string[] = [];
  protected FormFields = FormFields;
  protected itemFormGroup: FormGroup;
  protected ItemType = ItemType;
  private subscriptions: Subscription[] = [];
	
  constructor(private fb: FormBuilder, private configService: ConfigService) { 
			this.itemFormGroup = fb.group({
				[FormFields.QueryOperator]: '',
				[FormFields.ConceptOperator]: '',
				[FormFields.Concept]: [this.conceptsInit[0], [Validators.required]],
				[FormFields.NumberOperator]: '',
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
	//make service caching work
	if(this.formArrayIndex === 0) {
		this.getOperators(0);	
	} else {
		this.getOperators(400);
	}
	}
  
  private getOperators(delayMillis: number): void {
	setTimeout(() => {
		this.subscriptions.push(this.itemFormGroup.controls[FormFields.Concept].valueChanges.subscribe(myValue => 
	   this.concepts = this.conceptsInit.filter(myConcept => myConcept.includes(myValue))));	
	this.subscriptions.push(this.configService.getNumberOperators().subscribe(values => {
		this.numberQueryItems = values;
		this.itemFormGroup.controls[FormFields.ConceptOperator].patchValue(values.filter(myValue => '=' === myValue)[0]);
	}));
	this.subscriptions.push(this.configService.getStringOperators().subscribe(values => {
		this.stringQueryItems = values;
		this.itemFormGroup.controls[FormFields.NumberOperator].patchValue(values.filter(myValue => '=' === myValue)[0]);
	}));
	this.subscriptions.push(this.configService.getQueryOperators().subscribe(values => {
		this.termQueryItems = values;
		this.itemFormGroup.controls[FormFields.QueryOperator].patchValue(values.filter(myValue => 'And' === myValue)[0]);
	}));	
	}, delayMillis);	
  }
  
  ngOnDestroy(): void {
	if(!this.showType) {
	   this.baseFormArray.removeAt(this.formArrayIndex);
	}
	this.subscriptions.forEach(value => value.unsubscribe());	
	this.subscriptions = null;
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
