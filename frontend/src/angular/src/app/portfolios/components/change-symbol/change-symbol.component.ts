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
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CommonValues } from 'src/app/model/portfolio';
import { PortfolioTableComponent } from '../portfolio-table/portfolio-table.component';

@Component({
  selector: 'app-change-symbol',
  templateUrl: './change-symbol.component.html',
  styleUrls: ['./change-symbol.component.scss']
})
export class ChangeSymbolComponent implements OnInit {
    protected symbolForm: FormGroup;
    protected updatingQuotes = true;
    
    constructor(public dialogRef: MatDialogRef<PortfolioTableComponent>,
      @Inject(MAT_DIALOG_DATA) public data: CommonValues,
      private fb: FormBuilder) {
		this.symbolForm = this.fb.group({
	      symbolWeight: 0
		});
	  }
    
    ngOnInit(): void {
        console.log(this.data);
    }

}
