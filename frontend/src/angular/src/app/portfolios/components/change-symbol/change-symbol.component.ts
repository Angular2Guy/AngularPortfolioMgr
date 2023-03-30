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
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, AbstractControlOptions, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { filter } from 'rxjs';
import { PortfolioElement } from 'src/app/model/portfolio-element';
import { PortfolioTableComponent } from '../portfolio-table/portfolio-table.component';

enum FormFields {
  SymbolWeight = "symbolWeight"
}

@Component({
  selector: 'app-change-symbol',
  templateUrl: './change-symbol.component.html',
  styleUrls: ['./change-symbol.component.scss']
})
export class ChangeSymbolComponent implements OnInit {
	protected FormFields = FormFields;
    protected symbolForm: FormGroup;
    protected updatingQuotes = false;
    private newWeight = -1;
    
    constructor(public dialogRef: MatDialogRef<PortfolioTableComponent>,
      @Inject(MAT_DIALOG_DATA) public data: PortfolioElement,
      private fb: FormBuilder) {
		this.symbolForm = this.fb.group({
	      [FormFields.SymbolWeight]: [data.weight , [Validators.required, this.validateWeight]]
		});
	  }
    
    ngOnInit(): void {		
		this.symbolForm.controls[FormFields.SymbolWeight].valueChanges
		   .pipe(filter((value: number) => !!(''+value).match(/^[\d]+$/g))).subscribe((value: number) => this.newWeight = value);        
    }	
	
	updateClick() {		
		this.data.weight = this.newWeight >= 0 ? this.newWeight : this.data.weight; 
		this.dialogRef.close(this.data);
	}
	
	cancelClick() {
		this.dialogRef.close();
	}
	
	private validateWeight(control: AbstractControl): ValidationErrors {
		const myValue = control.value?.toString();
		const myResult = !myValue || !myValue.match(/^[\d]+$/g) ? { xxx: true } as ValidationErrors : {}  as ValidationErrors;
		return myResult;
	}
}
