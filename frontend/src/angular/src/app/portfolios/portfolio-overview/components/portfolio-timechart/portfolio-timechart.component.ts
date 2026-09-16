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
  ChangeDetectionStrategy,
  inject,
  signal,
  input,
  effect,
} from "@angular/core";
import { DateTime } from "luxon";
import { Portfolio } from "../../../../model/portfolio";
import { Symbol } from "../../../../model/symbol";
import { ServiceUtils } from "../../../../model/service-utils";
import { PortfolioService } from "../../../../service/portfolio.service";
import {
  ChartItem,
  NgxDateTimeChartsModule,
} from "ngx-simple-charts/date-time";
import { Item } from "../../model/item";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Component({
  selector: "app-portfolio-timechart",
  templateUrl: "./portfolio-timechart.component.html",
  styleUrls: ["./portfolio-timechart.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxDateTimeChartsModule],
})
export class PortfolioTimechartComponent {
  selPortfolio = input.required<Portfolio>();
  protected start = signal(new Date());
  protected items = signal<ChartItem<Event>[]>([]);
  protected showDays = signal(false);

  private portfolioService = inject(PortfolioService);
  private destroyRef = inject(DestroyRef);

  constructor() {
    effect(() => {
      const portfolio = this.selPortfolio();
      if (portfolio?.id) {
        this.portfolioService
          .getPortfolioByIdWithHistory(portfolio.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe((result: Portfolio) => {
            const myMap = result.symbols
              .filter(
                (mySymbol) =>
                  !mySymbol.symbol.includes(ServiceUtils.PORTFOLIO_MARKER),
              )
              .reduce((acc, mySymbol) => {
                const myValue = !acc.get(mySymbol.symbol)
                  ? []
                  : (acc.get(mySymbol.symbol) as Symbol[]);
                myValue.push(mySymbol);
                acc.set(mySymbol.symbol, myValue);
                return acc;
              }, new Map<string, Symbol[]>());
            const myItems: ChartItem<Event>[] = [];
            let myIndex = 0;
            myMap.forEach((myValue, myKey) => {
              const myStart = myValue
                .map((mySym) => new Date(mySym.changedAt))
                .reduce((acc, value) =>
                  acc.valueOf() < value.valueOf() ? value : acc,
                );
              const myEndItem = myValue.reduce((acc, value) =>
                acc.changedAt.valueOf() < value.changedAt.valueOf() ? value : acc,
              );
              const myEnd = !myEndItem?.removedAt
                ? null
                : new Date(myEndItem.removedAt);
              let myItem = new ChartItem<Event>();
              myItem.id = myIndex;
              myItem.lineId = myKey;
              myItem.details = myValue[0].description ?? "";
              myItem.name = myValue[0].name;
              myItem.start = myStart;
              myItem.end = myEnd;
              myItem.id = myIndex;
              myIndex = myIndex++;
              myItems.push(myItem);
            });
            this.items.set(myItems);
          });
      }
    });
  }
}
