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
import {
  Component,
  DestroyRef,
  inject,
  ChangeDetectionStrategy,
  signal,
  effect,
  input,
  output,
  OnInit,
} from "@angular/core";
import {
  FinancialsDataUtils,
  ItemType,
} from "../../model/financials-data-utils";
import {
  FormArray,
  FormGroup,
  FormBuilder,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import { debounceTime } from "rxjs/operators";
import { ConfigService } from "../../../service/config.service";
import { FinancialDataService } from "../../service/financial-data.service";
import { FeConcept } from "../../model/fe-concept";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatFormField, MatLabel } from "@angular/material/form-field";
import { MatSelect, MatOption } from "@angular/material/select";
import { MatInput } from "@angular/material/input";
import {
  MatAutocompleteTrigger,
  MatAutocomplete,
} from "@angular/material/autocomplete";
import { MatIconButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";

export enum QueryFormFields {
  QueryOperator = "queryOperator",
  ConceptOperator = "conceptOperator",
  Concept = "concept",
  NumberOperator = "numberOperator",
  NumberValue = "numberValue",
  ItemType = "itemType",
}

@Component({
  selector: "app-query",
  templateUrl: "./query.component.html",
  styleUrls: ["./query.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatSelect,
    MatOption,
    MatInput,
    MatAutocompleteTrigger,
    MatAutocomplete,
    MatIconButton,
    MatIcon,
  ],
})
export class QueryComponent implements OnInit {
  protected readonly containsOperator = "*=*";
  baseFormArray = input.required<FormArray>();
  formArrayIndex = input.required<number>();
  queryItemType = input.required<ItemType>();
  showType = input<boolean>(true);
  removeItem = output<number>();
  private timeoutRef = null as any;
  protected termQueryItems = signal<string[]>([]);
  protected stringQueryItems = signal<string[]>([]);
  protected numberQueryItems = signal<string[]>([]);
  protected concepts = signal<FeConcept[]>([]);
  protected QueryFormFields = QueryFormFields;
  protected itemFormGroup: FormGroup;
  protected ItemType = ItemType;

  private fb = inject(FormBuilder);
  private configService = inject(ConfigService);
  private financialDataService = inject(FinancialDataService);
  private destroyRef = inject(DestroyRef);

  constructor() {
    this.destroyRef.onDestroy(() => {
      if (!this.showType()) {
        this.baseFormArray().removeAt(this.formArrayIndex());
      }
    });
    this.itemFormGroup = this.fb.group({
      [QueryFormFields.QueryOperator]: "",
      [QueryFormFields.ConceptOperator]: "",
      [QueryFormFields.Concept]: ["", [Validators.required]],
      [QueryFormFields.NumberOperator]: "",
      [QueryFormFields.NumberValue]: [
        0,
        [Validators.required, Validators.pattern("^[+-]?(\\d+[\\,\\.])*\\d+$")],
      ],
      [QueryFormFields.ItemType]: ItemType.Query,
    });

    effect(() => {
      const show = this.showType();
      const baseArr = this.baseFormArray();
      const idx = this.formArrayIndex();
      if (!show) {
        const formIndex =
          baseArr?.controls?.findIndex(
            (myControl) => myControl === this.itemFormGroup,
          ) || -1;
        if (formIndex < 0) {
          baseArr.insert(idx, this.itemFormGroup);
        }
      } else {
        const formIndex =
          baseArr?.controls?.findIndex(
            (myControl) => myControl === this.itemFormGroup,
          ) || -1;
        if (formIndex >= 0) {
          baseArr.removeAt(formIndex);
        }
      }
    });
  }

  ngOnInit(): void {
    this.itemFormGroup.controls[QueryFormFields.Concept].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef), debounceTime(200))
      .subscribe((myValue: string) =>
        this.financialDataService
          .getConcepts()
          .subscribe(
            (myConceptList: FeConcept[]) =>
              this.concepts.set(
                myConceptList.filter((myConcept) =>
                  FinancialsDataUtils.compareStrings(
                    myConcept.concept,
                    myValue,
                    this.itemFormGroup.controls[QueryFormFields.ConceptOperator]
                      .value,
                  ),
                ),
              ),
          ),
      );
    this.itemFormGroup.controls[QueryFormFields.ItemType].patchValue(
      this.queryItemType(),
    );
    if (
      this.queryItemType() === ItemType.TermStart ||
      this.queryItemType() === ItemType.TermEnd
    ) {
      this.itemFormGroup.controls[QueryFormFields.ConceptOperator].patchValue(
        this.containsOperator,
      );
      this.itemFormGroup.controls[QueryFormFields.Concept].patchValue("xxx");
      this.itemFormGroup.controls[QueryFormFields.NumberOperator].patchValue(
        "=",
      );
      this.itemFormGroup.controls[QueryFormFields.NumberValue].patchValue(0);
    }
    if (this.formArrayIndex() === 0) {
      this.getOperators(0);
    } else {
      this.getOperators(400);
    }
  }

  private getOperators(delayMillis: number): void {
    if (!!this.timeoutRef) {
      clearTimeout(this.timeoutRef);
    }
    this.timeoutRef = setTimeout(() => {
      this.financialDataService
        .getConcepts()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe();
      this.configService
        .getNumberOperators()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((values: string[]) => {
          this.numberQueryItems.set(values);
          this.itemFormGroup.controls[
            QueryFormFields.NumberOperator
          ].patchValue(values.filter((myValue) => "=" === myValue)[0]);
        });
      this.configService
        .getStringOperators()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((values: string[]) => {
          this.stringQueryItems.set(values);
          this.itemFormGroup.controls[
            QueryFormFields.ConceptOperator
          ].patchValue(
            values.filter((myValue) => this.containsOperator === myValue)[0],
          );
        });
      if (ItemType.TermEnd === this.queryItemType()) {
        this.itemFormGroup.controls[QueryFormFields.QueryOperator].patchValue(
          "End",
        );
        this.itemFormGroup.controls[QueryFormFields.QueryOperator].disable();
      } else {
        this.configService
          .getQueryOperators()
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe((values: string[]) => {
            this.termQueryItems.set(values);
            this.itemFormGroup.controls[
              QueryFormFields.QueryOperator
            ].patchValue(values.filter((myValue) => "And" === myValue)[0]);
          });
      }
    }, delayMillis);
  }

  itemRemove(): void {
    this.removeItem.emit(this.formArrayIndex());
  }
}
