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
import { Component, OnInit, Inject, DestroyRef } from "@angular/core";
import {
  FormGroup,
  FormBuilder,
  AbstractControlOptions,
  Validators,
} from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { OverviewComponent } from "../overview/overview.component";
import { ConfigService } from "src/app/service/config.service";
import { takeUntilDestroyed } from "src/app/base/utils/funtions";
import { ImportData, ImportDataType } from "src/app/model/import-data";

enum FormFields {
  Filename = "filename",
}

@Component({
  selector: "app-import-financials",
  templateUrl: "./import-financials.component.html",
  styleUrls: ["./import-financials.component.scss"],
})
export class ImportFinancialsComponent implements OnInit {
  protected financialsForm: FormGroup;
  protected FormFields = FormFields;
  protected filepath: string = null;
  protected filename: string = null;
  protected ImportDataType = ImportDataType;

  constructor(
    private dialogRef: MatDialogRef<OverviewComponent>,
    private configService: ConfigService,
    private destroyRef: DestroyRef,
    @Inject(MAT_DIALOG_DATA) public data: ImportData,
    private fb: FormBuilder,
  ) {
    this.financialsForm = fb.group({
      [FormFields.Filename]: [
        "",
        [Validators.required, Validators.minLength(5)],
      ],
    } as AbstractControlOptions);
  }

  ngOnInit(): void {
    this.configService
      .getImportPath()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => (this.filepath = result));
  }

  okClick(): void {
    this.dialogRef.close({
      filename: this.financialsForm.controls[FormFields.Filename].value,
      path: this.filepath,
    } as ImportData);
  }

  cancelClick(): void {
    this.dialogRef.close();
  }
}
