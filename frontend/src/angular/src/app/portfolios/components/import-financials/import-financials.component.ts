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
import { FormGroup, FormBuilder, AbstractControlOptions, Validators, ValidationErrors } from '@angular/forms';

enum FormFields {
	Filename = 'filename',
}

@Component({
  selector: 'app-import-financials',
  templateUrl: './import-financials.component.html',
  styleUrls: ['./import-financials.component.scss']
})
export class ImportFinancialsComponent implements OnInit {
  protected financialsForm: FormGroup;  
  protected FormFields = FormFields;
  protected filePath: string = null;
  
  constructor(private fb: FormBuilder) {
	this.financialsForm = fb.group({
		[FormFields.Filename]: ['', Validators.required]
	  } as AbstractControlOptions);
  }

  ngOnInit(): void {
	
  }

  okClick(): void {
	console.log('okClick()');
  }
  
  cancelClick(): void {
	console.log('cancelClick()');
  }
}
