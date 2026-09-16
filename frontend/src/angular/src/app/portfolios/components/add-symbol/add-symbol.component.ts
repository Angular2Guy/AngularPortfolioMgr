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
  OnInit,
  Inject,
  inject,
  DestroyRef,
  signal,
  ChangeDetectionStrategy,
} from "@angular/core";
import {
  FormGroup,
  FormBuilder,
  AbstractControlOptions,
  Validators,
  ValidationErrors,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogContent,
} from "@angular/material/dialog";
import { OverviewComponent } from "../overview/overview.component";
import { PortfolioData } from "../../../model/portfolio-data";
import { Portfolio } from "../../../model/portfolio";
import { Symbol } from "../../../model/symbol";
import { SymbolService } from "../../../service/symbol.service";
import { Observable, of, forkJoin } from "rxjs";
import {
  debounceTime,
  distinctUntilChanged,
  tap,
  switchMap,
  map,
} from "rxjs/operators";
import { QuoteImportService } from "../../../service/quote-import.service";
import { DateTime } from "luxon";
import {
  MatAutocompleteSelectedEvent,
  MatAutocompleteTrigger,
  MatAutocomplete,
} from "@angular/material/autocomplete";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { CdkScrollable } from "@angular/cdk/scrolling";
import {
  MatFormField,
  MatLabel,
  MatSuffix,
} from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatOption } from "@angular/material/select";
import {
  MatDatepickerInput,
  MatDatepickerToggle,
  MatDatepicker,
} from "@angular/material/datepicker";
import { MatButton } from "@angular/material/button";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { AsyncPipe } from "@angular/common";

enum FormFields {
  SymbolSymbol = "symbolSymbol",
  SymbolName = "symbolName",
  SymbolWeight = "symbolWeight",
  CreatedAt = "createdAt",
}

@Component({
  selector: "app-add-symbol",
  templateUrl: "./add-symbol.component.html",
  styleUrls: ["./add-symbol.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CdkScrollable,
    MatDialogContent,
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatAutocompleteTrigger,
    MatAutocomplete,
    MatOption,
    MatLabel,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatSuffix,
    MatDatepicker,
    MatButton,
    MatProgressSpinner,
    AsyncPipe,
  ],
})
export class AddSymbolComponent implements OnInit {
  private portfolio!: Portfolio;
  symbolForm: FormGroup;
  selSymbol!: Symbol;
  symbolsName: Observable<Symbol[]> = of([]);
  symbolsSymbol: Observable<Symbol[]> = of([]);
  loading = signal(false);
  importingQuotes = signal(false);
  formValid = signal(true);
  FormFields = FormFields;
  private symbolService = inject(SymbolService);
  private quoteImportService = inject(QuoteImportService);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);

  constructor(
    public dialogRef: MatDialogRef<OverviewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PortfolioData,
  ) {
    this.symbolForm = this.fb.group(
      {
        [FormFields.SymbolSymbol]: "",
        [FormFields.SymbolName]: "",
        [FormFields.SymbolWeight]: 0,
        [FormFields.CreatedAt]: [DateTime.now(), Validators.required],
      },
      {
        validators: [this.validate],
      } as AbstractControlOptions,
    );
    this.portfolio = data.portfolio;
  }

  ngOnInit() {
    this.symbolsName = this.symbolForm
      .get(FormFields.SymbolName)
      ?.valueChanges.pipe(
        debounceTime(400),
        distinctUntilChanged(),
        tap(() => this.loading.set(true)),
        switchMap((name: string) =>
          name && name.length > 2
            ? this.symbolService
                .getSymbolByName(name)
                .pipe(
                  map((localSymbols: Symbol[]) =>
                    this.filterPortfolioSymbols(localSymbols),
                  ),
                )
            : this.clearSymbol(),
        ),
        tap(() => this.loading.set(false)),
      );
    this.symbolsSymbol = this.symbolForm
      .get(FormFields.SymbolSymbol)
      ?.valueChanges.pipe(
        debounceTime(400),
        distinctUntilChanged(),
        tap(() => this.loading.set(true)),
        switchMap((name: string) =>
          name && name.length >= 2
            ? this.symbolService
                .getSymbolBySymbol(name)
                .pipe(
                  map((localSymbols: Symbol[]) =>
                    this.filterPortfolioSymbols(localSymbols),
                  ),
                )
            : this.clearSymbol(),
        ),
        tap(() => this.loading.set(false)),
      );
  }

  private filterPortfolioSymbols(symbols: Symbol[]): Symbol[] {
    return symbols.filter((symbol) =>
      this.portfolio.symbols.filter(
        (mySymbol) => symbol.symbol === mySymbol.symbol,
      ),
    );
  }

  private clearSymbol(): Observable<Symbol[]> {
    this.selSymbol = {} as Symbol;
    return of([]) as Observable<Symbol[]>;
  }

  symbolSelected(event: MatAutocompleteSelectedEvent): void {
    this.selSymbol = event.option.value;
    this.symbolForm.controls[FormFields.SymbolSymbol].patchValue(
      this.selSymbol.symbol,
    );
    this.symbolForm.controls[FormFields.SymbolName].patchValue(
      this.selSymbol.name,
    );
    this.updateSymbolWeight();
  }

  updateSymbolWeight() {
    if (this.selSymbol) {
      this.selSymbol.weight =
        this.symbolForm.controls[FormFields.SymbolWeight].value;
    }
  }

  onAddClick(): void {
    if (this.selSymbol) {
      this.importingQuotes.set(true);
      this.selSymbol.weight =
        this.symbolForm.controls[FormFields.SymbolWeight].value;
      const changedAt = this.symbolForm.controls[FormFields.CreatedAt]
        .value as DateTime;
      this.selSymbol.changedAt = new Date(changedAt.toMillis()).toISOString();
      forkJoin(
        this.quoteImportService.importDailyQuotes(this.selSymbol.symbol),
        this.quoteImportService.importIntraDayQuotes(this.selSymbol.symbol),
      )
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(([resultDaily, resultIntraDay]: [string, string]) => {
          console.log(
            `Daily quotes: ${resultDaily}, Intraday quotes: ${resultIntraDay}`,
          );
          this.importingQuotes.set(false);
          this.dialogRef.close(this.selSymbol);
        });
    }
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }

  validate(formGroup: FormGroup): ValidationErrors {
    return { xxx: true } as ValidationErrors;
  }
}
