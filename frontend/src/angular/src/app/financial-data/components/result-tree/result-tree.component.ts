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
import { NestedTreeControl } from "@angular/cdk/tree";
import {
  Component,
  DestroyRef,
  inject,
  Input,
  TemplateRef,
  ViewChild,
} from "@angular/core";
import { MatBottomSheet } from "@angular/material/bottom-sheet";
import { MatTreeNestedDataSource } from "@angular/material/tree";
import { MonoTypeOperatorFunction, Subject, takeUntil } from "rxjs";
import { FeIdInfo } from "../../model/fe-id-info";
import {
  FinancialElement,
  FinancialElementExt,
} from "../../model/financial-element";
import { FinancialsDataUtils } from "../../model/financials-data-utils";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialDataService } from "../../service/financial-data.service";

interface ElementNode {
  name: string;
  children?: ElementNode[];
}

interface ByElements extends ElementNode {
  finanicalElementExts: FinancialElementExt[];
  isOpen: boolean;
}

interface ByYearElements extends ElementNode {
  year: number;
  byElements: ByElements[];
}

interface BySymbolElements extends ElementNode {
  symbol: string;
  byYearElements: ByYearElements[];
}

@Component({
  selector: "app-result-tree",
  templateUrl: "./result-tree.component.html",
  styleUrls: ["./result-tree.component.scss"],
})
export class ResultTreeComponent {
  private _symbolFinancials: SymbolFinancials[] = [];
  protected treeControl = new NestedTreeControl<ElementNode>(
    (node) => node.children
  );
  protected dataSource = new MatTreeNestedDataSource<ElementNode>();
  protected displayedColumns: string[] = [
    "concept",
    "value",
    "currency",
    "quarter",
  ];
  protected financialElement: FinancialElement = null;
  protected destroyRef: DestroyRef;

  @ViewChild("bottomSheet") bsTemplate: TemplateRef<HTMLElement>;

  constructor(
    private financialDataService: FinancialDataService,
    private bottomSheet: MatBottomSheet
  ) {
    this.destroyRef = inject(DestroyRef);
  }

  protected hasChild = (_: number, node: ElementNode) =>
    !!node.children && node.children.length > 0;

  toggleNode(node: ElementNode): void {
    this.treeControl.toggle(node);
    node?.children?.forEach((childNode) => {
      if (!childNode || !childNode?.children?.length) {
        const myByElements = childNode as ByElements;
        myByElements.isOpen = this.treeControl.isExpanded(node);
      }
    });
  }

  conceptClick(element: FinancialElement): void {
    //console.log(element);
    this.financialDataService
      .getFeInfo(element.id)
      .pipe(this.takeUntilDestroyed())
      .subscribe((value) => {
        //console.log(value);
        this.financialElement = element;
        this.financialElement.info = (value as FeIdInfo).info;
        this.bottomSheet.open(this.bsTemplate);
      });
  }

  get symbolFinancials(): SymbolFinancials[] {
    return this._symbolFinancials;
  }

  @Input()
  set symbolFinancials(symbolFinancials: SymbolFinancials[]) {
    this._symbolFinancials = symbolFinancials;
    //console.log(symbolFinancials);
    this.dataSource.data = this.createElementNodeTree(symbolFinancials);
  }

  private createElementNodeTree(
    symbolFinancials: SymbolFinancials[]
  ): BySymbolElements[] {
    const bySymbolElementExtsMap = FinancialsDataUtils.groupByKey<
      FinancialElementExt,
      string
    >(FinancialsDataUtils.toFinancialElementsExt(symbolFinancials), "symbol");
    //console.log(bySymbolElementExtsMap);
    const myBySymbolElements: BySymbolElements[] = [];
    bySymbolElementExtsMap.forEach((value, key) => {
      const byYearElementsMap = FinancialsDataUtils.groupByKey<
        FinancialElementExt,
        number
      >(value, "year");
      const byYearElements: ByYearElements[] = [];
      byYearElementsMap.forEach((value, key) => {
        const myByElements = {
          name: "Elements",
          isOpen: false,
          finanicalElementExts: value,
        } as ByElements;
        const myByYearElement = {
          year: key,
          name: key.toString(),
          children: [myByElements],
          byElements: [myByElements],
        } as ByYearElements;
        byYearElements.push(myByYearElement);
      });
      const myBySymbolElement = {
        name: key,
        symbol: key,
        byYearElements: byYearElements,
        children: byYearElements,
      } as BySymbolElements;
      myBySymbolElements.push(myBySymbolElement);
    });
    //console.log(myBySymbolElements);
    return myBySymbolElements;
  }

  private takeUntilDestroyed<T>(): MonoTypeOperatorFunction<T> {
    const subject = new Subject();

    this.destroyRef.onDestroy(() => {
      subject.next(true);
      subject.complete();
    });

    return takeUntil<T>(subject.asObservable());
  }
}
