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
  output,
  OnInit,
} from "@angular/core";
import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
  CdkDropListGroup,
  CdkDropList,
  CdkDrag,
} from "@angular/cdk/drag-drop";
import {
  FormGroup,
  FormArray,
  FormBuilder,
  Validators,
  ValidationErrors,
  ValidatorFn,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import {
  FinancialsDataUtils,
  ItemType,
} from "../../model/financials-data-utils";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialElementExt } from "../../model/financial-element";
import {
  SymbolFinancialsQueryParams,
  FinancialElementParams,
  FilterNumber,
} from "../../model/symbol-financials-query-params";
import { switchMap, debounceTime, delay, filter } from "rxjs/operators";
import { SymbolService } from "../../../service/symbol.service";
import { ConfigService } from "../../../service/config.service";
import { FinancialDataService } from "../../service/financial-data.service";
import { QueryFormFields, QueryComponent } from "../query/query.component";
import { Symbol } from "../../../model/symbol";
import { SfSymbolName } from "../../model/sf-symbol-name";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { QuarterData } from "../../model/quarter-data";
import { FeCountry } from "../../model/fe-country";
import { MatButton } from "@angular/material/button";
import { MatFormField, MatLabel } from "@angular/material/form-field";
import { MatSelect, MatOption } from "@angular/material/select";
import { MatInput } from "@angular/material/input";
import {
  MatAutocompleteTrigger,
  MatAutocomplete,
} from "@angular/material/autocomplete";

export interface MyItem {
  queryItemType: ItemType;
  title: string;
}

export interface ItemParams {
  showType: boolean;
  formArray: FormArray;
  formArrayIndex: number;
}

enum FormFields {
  YearOperator = "yearOperator",
  Year = "year",
  SymbolOperator = "symbolOperator",
  Symbol = "symbol",
  Name = "name",
  Country = "country",
  QuarterOperator = "quarterOperator",
  Quarter = "quarter",
  QueryItems = "queryItems",
}

@Component({
  selector: "app-create-query",
  templateUrl: "./create-query.component.html",
  styleUrls: ["./create-query.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatButton,
    MatFormField,
    MatLabel,
    MatSelect,
    MatOption,
    MatInput,
    MatAutocompleteTrigger,
    MatAutocomplete,
    CdkDropListGroup,
    CdkDropList,
    QueryComponent,
    CdkDrag,
  ],
})
export class CreateQueryComponent implements OnInit {
  private readonly availableInit: MyItem[] = [
    { queryItemType: ItemType.Query, title: "Query" },
    { queryItemType: ItemType.TermStart, title: "Term Start" },
    { queryItemType: ItemType.TermEnd, title: "Term End" },
  ];
  protected availableItems = signal<MyItem[]>([]);
  protected queryItems = signal<MyItem[]>([
    { queryItemType: ItemType.Query, title: "Query" },
  ]);
  protected readonly availableItemParams = {
    showType: true,
    formArray: new FormArray([] as any[]),
    formArrayIndex: -1,
  } as ItemParams;
  protected readonly queryItemParams = {
    showType: false,
    formArray: new FormArray([] as any[]),
    formArrayIndex: -1,
  } as ItemParams;
  protected queryForm: FormGroup;
  protected yearOperators: string[] = [];
  protected quarterQueryItems: string[] = [];
  protected countryQueryItems: string[] = [];
  protected symbols = signal<Symbol[]>([]);
  protected sfSymbolNames = signal<SfSymbolName[]>([]);
  protected FormFields = FormFields;
  protected formStatus = signal("");
  symbolFinancials = output<SymbolFinancials[]>();
  financialElements = output<FinancialElementExt[]>();
  showSpinner = output<boolean>();

  private fb = inject(FormBuilder);
  private symbolService = inject(SymbolService);
  private configService = inject(ConfigService);
  private financialDataService = inject(FinancialDataService);
  private destroyRef = inject(DestroyRef);

  constructor() {
    this.queryForm = this.fb.group(
      {
        [FormFields.YearOperator]: "",
        [FormFields.Year]: [0, Validators.pattern("^\\d*$")],
        [FormFields.Symbol]: "",
        [FormFields.Quarter]: [""],
        [FormFields.Name]: "",
        [FormFields.Country]: "",
        [FormFields.QueryItems]: this.fb.array([]),
      },
      {
        validators: [this.validateItemTypes()],
      },
    );
    this.queryItemParams.formArray = this.queryForm.controls[
      FormFields.QueryItems
    ] as FormArray;
    this.queryForm.statusChanges
      .pipe(delay(0))
      .subscribe((result: string) => this.formStatus.set(result));
  }

  ngOnInit(): void {
    this.symbolFinancials.emit([]);
    this.financialElements.emit([]);
    this.availableItems.set([...this.availableInit]);
    this.queryForm.controls[FormFields.Symbol].valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        debounceTime(200),
        filter((myValue: string) => !!myValue),
        switchMap((myValue: string) =>
          this.symbolService.getSymbolBySymbol(myValue),
        ),
      )
      .subscribe((myValue: Symbol[]) => this.symbols.set(myValue));
    this.queryForm.controls[FormFields.Name].valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        debounceTime(200),
        filter((myValue: string) => !!myValue),
        switchMap((myValue: string) =>
          this.financialDataService.getSymbolNamesByCompanyName(myValue),
        ),
      )
      .subscribe((myValue: SfSymbolName[]) =>
        this.sfSymbolNames.set(myValue),
      );

    this.configService.getNumberOperators().subscribe((values: string[]) => {
      this.yearOperators = values;
      this.queryForm.controls[FormFields.YearOperator].patchValue(
        values.filter((myValue: string) => myValue === "=")[0],
      );
    });
    this.financialDataService
      .getQuarters()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(
        (values: QuarterData[]) =>
          (this.quarterQueryItems = values.map(
            (myValue: QuarterData) => myValue.quarter,
          )),
      );
    this.financialDataService
      .getCountries()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(
        (values: FeCountry[]) =>
          (this.countryQueryItems = values.map(
            (myValue: FeCountry) => myValue.country,
          )),
      );
  }

  drop(event: CdkDragDrop<MyItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      const queryItemsArray = this.queryForm.controls[
        FormFields.QueryItems
      ] as FormArray;
      const myFormArrayItem = queryItemsArray.value.splice(
        event.previousIndex,
        1,
      )[0];
      queryItemsArray.value.splice(event.currentIndex, 0, myFormArrayItem);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      this.availableItems.set([...this.availableInit]);
    }
  }

  public removeItem(index: number): void {
    this.queryItems.update((items) => {
      const newItems = [...items];
      newItems.splice(index, 1);
      return newItems;
    });
  }

  public search(): void {
    const symbolFinancialsParams = {
      yearFilter: {
        operation: this.queryForm.controls[FormFields.YearOperator].value,
        value: !this.queryForm.controls[FormFields.Year].value
          ? 0
          : parseInt(this.queryForm.controls[FormFields.Year].value),
      } as FilterNumber,
      quarters: !this.queryForm.controls[FormFields.Quarter].value
        ? []
        : this.queryForm.controls[FormFields.Quarter].value,
      symbol: this.queryForm.controls[FormFields.Symbol].value,
      country: this.queryForm.controls[FormFields.Country].value,
      name: this.queryForm.controls[FormFields.Name].value,
      financialElementParams: !!this.queryForm.controls[FormFields.QueryItems]
        ?.value?.length
        ? this.queryForm.controls[FormFields.QueryItems].value.map(
            (myFormGroup: FormGroup) =>
              this.createFinancialElementParam(myFormGroup),
          )
        : [],
    } as SymbolFinancialsQueryParams;
    this.showSpinner.emit(true);
    this.financialDataService
      .postSymbolFinancialsParam(symbolFinancialsParams)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: SymbolFinancials[]) => {
        this.processQueryResult(result, symbolFinancialsParams);
        this.showSpinner.emit(false);
      });
  }

  private processQueryResult(
    result: SymbolFinancials[],
    symbolFinancialsParams: SymbolFinancialsQueryParams,
  ): void {
    const logResults = false;
    const symbolFinancialsFilter =
      !!symbolFinancialsParams?.yearFilter?.value ||
      !!symbolFinancialsParams?.quarters?.length ||
      !!symbolFinancialsParams?.symbol ||
      !!symbolFinancialsParams?.country ||
      !!symbolFinancialsParams?.name;
    if (result.length > 0 && symbolFinancialsFilter) {
      if (!!logResults) {
        console.log(result.length);
      }
      this.symbolFinancials.emit(result);
      this.financialElements.emit([]);
    } else if (result.length > 0 && !symbolFinancialsFilter) {
      if (!!logResults) {
        console.log(result.length);
      }
      this.symbolFinancials.emit([]);
      this.financialElements.emit(
        FinancialsDataUtils.toFinancialElementsExt(result),
      );
    } else {
      if (!!logResults) {
        console.log(result);
      }
      this.symbolFinancials.emit([]);
      this.financialElements.emit([]);
    }
  }

  private createFinancialElementParam(
    formGroup: FormGroup,
  ): FinancialElementParams {
    return {
      conceptFilter: {
        operation: formGroup.get(QueryFormFields.ConceptOperator)?.value,
        value: formGroup.get(QueryFormFields.Concept)?.value,
      },
      valueFilter: {
        operation: formGroup.get(QueryFormFields.NumberOperator)?.value,
        value: formGroup.get(QueryFormFields.NumberValue)?.value,
      },
      operation: formGroup.get(QueryFormFields.QueryOperator)?.value,
      termType: formGroup.get(QueryFormFields.ItemType)?.value,
    } as FinancialElementParams;
  }

  private validateItemTypes(): ValidatorFn {
    const validateItemTypesFn = (form: FormGroup): ValidationErrors | null => {
      let termStartCount = this.queryItems().filter(
        (myTerm) => myTerm.queryItemType === ItemType.TermStart,
      ).length;
      let termEndCount = this.queryItems().filter(
        (myTerm) => myTerm.queryItemType === ItemType.TermEnd,
      ).length;
      return termStartCount != termEndCount ? { termItemsValid: false } : null;
    };
    return validateItemTypesFn as ValidatorFn;
  }
}
