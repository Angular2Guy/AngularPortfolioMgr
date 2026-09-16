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
  input,
  signal,
  inject,
  ChangeDetectionStrategy,
  effect,
} from "@angular/core";
import { ServiceUtils } from "../../../../model/service-utils";
import { Symbol } from "../../../../model/symbol";
import { Portfolio, CommonValues } from "../../../../model/portfolio";
import { DecimalPipe, DatePipe } from "@angular/common";

@Component({
  selector: "app-symbol-overview",
  templateUrl: "./symbol-overview.component.html",
  styleUrls: ["./symbol-overview.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, DatePipe],
})
export class SymbolOverviewComponent {
  portfolio = input.required<Portfolio>();
  symbol = input.required<Symbol>();

  private localSymbol = signal<Symbol>({} as Symbol);
  serviceUtils = ServiceUtils;

  constructor() {
    effect(() => {
      const mySymbol = this.symbol();
      if (mySymbol) {
        this.localSymbol.set(mySymbol);
      }
    });
  }

  getPortfolioElement(): CommonValues {
    return ServiceUtils.isPortfolioSymbol(this.symbol())
      ? this.portfolio()
      : this.portfolio().portfolioElements.filter(
          (value) => value.symbol === this.symbol().symbol,
        )[0];
  }
}
