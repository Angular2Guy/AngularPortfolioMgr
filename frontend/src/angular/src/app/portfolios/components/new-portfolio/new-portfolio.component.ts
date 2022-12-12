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
import { Component, OnInit, Inject } from "@angular/core";
import { FormGroup, Validators, FormBuilder } from "@angular/forms";
import { OverviewComponent } from "../overview/overview.component";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Portfolio } from "../../../model/portfolio";
import { PortfolioData } from "../../../model/portfolio-data";
import { DateTime } from "luxon";
import { TokenService } from "ngx-simple-charts/base-service";

enum FormFields {
  PortfolioName = "portfolioName",
  CreatedAt = "createdAt",
}

@Component({
  selector: "app-new-portfolio",
  templateUrl: "./new-portfolio.component.html",
  styleUrls: ["./new-portfolio.component.scss"],
})
export class NewPortfolioComponent {
  portfolioForm: FormGroup;
  formValid = true;
  FormFields = FormFields;

  constructor(
    public dialogRef: MatDialogRef<OverviewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PortfolioData,
    private tokenService: TokenService,
    private fb: FormBuilder
  ) {
    this.portfolioForm = this.fb.group(
      {
        [FormFields.PortfolioName]: ["", Validators.required],
        [FormFields.CreatedAt]: [
          DateTime.fromISO(this.data.portfolio.createdAt),
          Validators.required,
        ],
      },
      {
        validator: this.validate.bind(this),
      }
    );
  }

  onAddClick(): void {
    const createdAt = this.portfolioForm.get(FormFields.CreatedAt)
      .value as DateTime;
    //createdAt.setMinutes(createdAt.getMinutes() - createdAt.getTimezoneOffset());
    const portfolio: Portfolio = {
      id: null,
      createdAt: new Date(createdAt.toMillis()).toISOString(),
      month1: null,
      month6: null,
      name: this.portfolioForm.get(FormFields.PortfolioName).value,
      currencyKey: null,
      symbols: [],
      portfolioElements: [],
      userId: this.tokenService.userId as number,
      year1: null,
      year10: null,
      year2: null,
      year5: null,
    };
    this.dialogRef.close(portfolio);
    /*
	private BigDecimal month1;
	private BigDecimal month6;
	private BigDecimal year1;
	private BigDecimal year2;
	private BigDecimal year5;
	private BigDecimal year10;	
	private CurrencyKey currencyKey;
	private List<SymbolDto> symbols = new ArrayList<>();
	private List<PortfolioElementDto> portfolioElements = new ArrayList<>();
	*/
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }

  validate(formGroup: FormGroup) {
    if (formGroup.get(FormFields.PortfolioName).touched) {
      const myValue: string = formGroup.get(FormFields.PortfolioName).value;
      if (myValue && myValue.trim().length > 4) {
        formGroup.get(FormFields.PortfolioName).setErrors(null);
        this.formValid = true;
      } else {
        formGroup
          .get(FormFields.PortfolioName)
          .setErrors({ MatchPassword: true });
        this.formValid = false;
      }
    }
  }
}
