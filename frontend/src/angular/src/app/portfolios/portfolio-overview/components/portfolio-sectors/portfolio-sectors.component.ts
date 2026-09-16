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
  ElementRef,
  ChangeDetectionStrategy,
  signal,
  viewChild,
  input,
  effect,
  DestroyRef,
  inject,
} from "@angular/core";
import {
  trigger,
  state,
  style,
  animate,
  transition,
  query,
  group,
} from "@angular/animations";
import {
  ChartSlices,
  ChartSlice,
  NgxDonutChartsModule,
} from "ngx-simple-charts/donut";
import { Portfolio } from "../../../../model/portfolio";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatIcon } from "@angular/material/icon";
import { NgStyle, DecimalPipe } from "@angular/common";

interface CalcPortfolioElement {
  name: string;
  sector: string;
  value: number;
}

@Component({
  selector: "app-portfolio-sectors",
  templateUrl: "./portfolio-sectors.component.html",
  styleUrls: ["./portfolio-sectors.component.scss"],
  animations: [
    trigger("fadeInGrow", [
      transition("* => ready", [
        style({ opacity: 0, transform: "scale(0.1)" }),
        group([
          animate("300ms linear", style({ opacity: 1 })),
          animate("1000ms linear", style({ transform: "scale(1)" })),
        ]),
      ]),
    ]),
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatProgressSpinner,
    MatIcon,
    NgStyle,
    NgxDonutChartsModule,
    DecimalPipe,
  ],
})
export class PortfolioSectorsComponent {
  selPortfolio = input.required<Portfolio>();
  divHideMe = viewChild<ElementRef>("hideMe");
  chartSlices = signal<ChartSlices>({
    title: "",
    from: "",
    xScaleHeight: 0,
    yScaleWidth: 0,
    chartSlices: [],
  });
  chartsLoading = signal(true);
  chartState = signal<"ready" | "not-ready">("not-ready");
  slicesSum = signal(1);

  private readonly colorKeys = [
    "--red",
    "--purple",
    "--blue",
    "-cyan",
    "--green",
    "--lime",
    "--orange",
    "--gray",
  ];

  private destroyRef = inject(DestroyRef);

  constructor() {
    effect(() => {
      const portfolio = this.selPortfolio();
      const divHideMe = this.divHideMe();
      if (portfolio?.id) {
        this.chartSlices.update((s) => ({ ...s, title: portfolio.name, chartSlices: [] }));
        this.chartsLoading.set(false);
        this.chartState.set("not-ready");
        if (divHideMe) {
          this.drawDonut();
        }
      }
    });
  }

  private drawDonut(): void {
    const divHideMe = this.divHideMe();
    if (!divHideMe || !this.selPortfolio()?.id) {
      return;
    }
    const sliceColors = window
      .getComputedStyle(divHideMe.nativeElement, ":before")
      ["content"].replace('"', "")
      .replace('"', "")
      .split(",");
    const valueMap = this.selPortfolio().portfolioElements
      .map(
        (pe) =>
          ({
            name: pe.name,
            sector: pe.sector,
            value: pe.lastClose * pe.weight,
          }) as CalcPortfolioElement,
      )
      .reduce((myMap, cpe) => {
        let myValue = myMap.get(cpe.sector);
        myValue = !myValue ? 0 : myValue;
        myMap.set(cpe.sector, myValue + cpe.value);
        return myMap;
      }, new Map<string, number>());
    let calcColors = [] as string[];
    while (calcColors.length < valueMap.size) {
      calcColors = calcColors.concat(sliceColors);
    }
    const newChartSlices: ChartSlice[] = [];
    let i = 0;
    valueMap.forEach((myValue, myKey) => {
      i = i + 1;
      newChartSlices.push({
        name: myKey,
        value: myValue,
        color: calcColors[i],
      } as ChartSlice);
    });
    const sum = newChartSlices.reduce(
      (acc, mySlice) => (acc = acc + mySlice.value),
      0,
    );
    this.slicesSum.set(sum);
    newChartSlices.sort((a, b) => b.value - a.value);
    this.chartSlices.update((s) => ({ ...s, chartSlices: newChartSlices }));
    setTimeout(() => {
      this.chartState.set("ready");
    });
  }
}
