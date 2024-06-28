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
import { Component, DestroyRef, Inject, OnInit } from "@angular/core";
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { DateTime, Duration } from "luxon";
import { filter } from "rxjs";
import { PortfolioElement } from "src/app/model/portfolio-element";
import { PortfolioTableComponent } from "../portfolio-table/portfolio-table.component";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";

enum FormFields {
  SymbolWeight = "symbolWeight",
  ChangedAt = "changedAt",
}

@Component({
  selector: "app-change-symbol",
  templateUrl: "./change-symbol.component.html",
  styleUrls: ["./change-symbol.component.scss"],
})
export class ChangeSymbolComponent implements OnInit {
  protected FormFields = FormFields;
  protected symbolForm: FormGroup;
  protected updatingQuotes = false;
  protected deleteSymbol = false;
  private newWeight = -1;
  private changedAt = DateTime.now();

  constructor(
    public dialogRef: MatDialogRef<PortfolioTableComponent>,
    private destroyRef: DestroyRef,
    @Inject(MAT_DIALOG_DATA) public data: PortfolioElement,
    private fb: FormBuilder,
  ) {
    this.symbolForm = this.fb.group({
      [FormFields.SymbolWeight]: [
        data.weight,
        [Validators.required, this.validateWeight],
      ],
      [FormFields.ChangedAt]: [this.changedAt, [Validators.required]],
    });
  }

  ngOnInit(): void {
    const startChangedAt = this.changedAt;
    this.symbolForm.controls[FormFields.ChangedAt].valueChanges
      .pipe(
        filter(
          (myChangedAt: DateTime) =>
            !!myChangedAt && myChangedAt.toMillis() > startChangedAt.toMillis(),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((value: DateTime) => (this.changedAt = value));
    this.symbolForm.controls[FormFields.SymbolWeight].valueChanges
      .pipe(
        filter((value: number) => !!("" + value).match(/^[\d]+$/g)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((value: number) => (this.newWeight = value));
  }

  updateClick() {
    if (!this.deleteSymbol) {
      const startChangedAt = DateTime.now().minus(
        Duration.fromObject({ years: 100 }),
      );
      if (
        this.newWeight >= 0 &&
        this.changedAt.toMillis() > startChangedAt.toMillis()
      ) {
        this.data.weight = this.newWeight;
        this.data.changedAt = this.changedAt.toISO().split("+")[0];
        this.dialogRef.close(this.data);
      }
    } else {
	  this.data.changedAt = this.changedAt.toISO().split("+")[0];      
      this.data.weight = 0;
      //console.log(this.data);
      this.dialogRef.close(this.data);
    }
  }

  cancelClick() {
    this.dialogRef.close();
  }

  deleteClick() {
    this.deleteSymbol = true;
  }

  private validateWeight(control: AbstractControl): ValidationErrors {
    const myValue = control.value?.toString();
    const myResult =
      !myValue || !myValue.match(/^[\d]+$/g)
        ? ({ xxx: true } as ValidationErrors)
        : ({} as ValidationErrors);
    return myResult;
  }
}
