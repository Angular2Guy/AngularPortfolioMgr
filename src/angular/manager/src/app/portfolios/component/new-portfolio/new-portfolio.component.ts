import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, Validators, FormBuilder } from '@angular/forms';
import { OverviewComponent } from '../overview/overview.component';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Portfolio } from '../../model/portfolio';
import { PortfolioData } from '../../model/portfolio-data';
import { TokenService } from '../../../service/token.service';

@Component({
  selector: 'app-new-portfolio',
  templateUrl: './new-portfolio.component.html',
  styleUrls: ['./new-portfolio.component.scss']
})
export class NewPortfolioComponent implements OnInit {
  portfolioForm: FormGroup;
  formValid = true;

  constructor(public dialogRef: MatDialogRef<OverviewComponent>,
		@Inject(MAT_DIALOG_DATA) public data: PortfolioData,
		private tokenService: TokenService,
		private fb: FormBuilder) { 
	this.portfolioForm = fb.group({
		portfolioName: ['', Validators.required]
	}, {
		validator: this.validate.bind(this)
	});
  }

  ngOnInit() {
  }

  onAddClick(): void {
		const portfolio: Portfolio = {id: null, month1: null, months6: null, name: this.portfolioForm.get('portfolioName').value, 
			symbols: [], userId: this.tokenService.userId, year1: null, year10: null, year2: null, year5: null }; 		
		this.dialogRef.close(portfolio);		
  }

  onCancelClick(): void {
	this.dialogRef.close();
  }

  validate(formGroup: FormGroup) {
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
  }
}
