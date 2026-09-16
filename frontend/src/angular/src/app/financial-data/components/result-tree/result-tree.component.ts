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
  TemplateRef,
  ChangeDetectionStrategy,
  effect,
  signal,
  viewChild,
  input,
  inject,
} from "@angular/core";
import { MatBottomSheet } from "@angular/material/bottom-sheet";
import {
  MatTree,
  MatTreeNestedDataSource,
  MatTreeNodeDef,
  MatNestedTreeNode,
  MatTreeNodeToggle,
  MatTreeNodeOutlet,
} from "@angular/material/tree";
import { FeIdInfo } from "../../model/fe-id-info";
import {
  FinancialElement,
  FinancialElementExt,
} from "../../model/financial-element";
import { FinancialsDataUtils } from "../../model/financials-data-utils";
import { SymbolFinancials } from "../../model/symbol-financials";
import { FinancialDataService } from "../../service/financial-data.service";
import {
  MatTable,
  MatColumnDef,
  MatHeaderCellDef,
  MatHeaderCell,
  MatCellDef,
  MatCell,
  MatHeaderRowDef,
  MatHeaderRow,
  MatRowDef,
  MatRow,
} from "@angular/material/table";
import { MatTooltip } from "@angular/material/tooltip";
import { MatIcon } from "@angular/material/icon";
import { MatIconButton } from "@angular/material/button";
import { MatDivider } from "@angular/material/list";

interface ElementNode {
  name: string;
  visible?: boolean;
  children?: ElementNode[];
}

interface ByElements extends ElementNode {
  finanicalElementExts: FinancialElementExt[];
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
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTree,
    MatTreeNodeDef,
    MatNestedTreeNode,
    MatTreeNodeToggle,
    MatTable,
    MatColumnDef,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    MatTooltip,
    MatIcon,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRowDef,
    MatRow,
    MatIconButton,
    MatTreeNodeOutlet,
    MatDivider,
  ],
})
export class ResultTreeComponent {
  symbolFinancials = input<SymbolFinancials[]>([]);
  bsTemplate = viewChild<TemplateRef<HTMLElement>>("bottomSheet");
  protected dataSource = new MatTreeNestedDataSource<ElementNode>();
  protected displayedColumns: string[] = [
    "concept",
    "value",
    "currency",
    "element-type",
    "quarter",
  ];
  protected financialElement!: FinancialElement;

  private financialDataService = inject(FinancialDataService);
  private bottomSheet = inject(MatBottomSheet);

  constructor() {
    effect(() => {
      const sf = this.symbolFinancials();
      this.dataSource.data = this.createElementNodeTree(sf);
    });
  }

  protected childrenAccessor = (node: ElementNode) => {
    return node.children ?? [];
  };
  protected hasChild = (_: number, node: ElementNode) => {
    return !!node.children && node.children.length > 0;
  };

  protected conceptClick(element: FinancialElement): void {
    this.financialDataService
      .getFeInfo(element.id)
      .subscribe((value: FeIdInfo) => {
        this.financialElement = element;
        this.financialElement.info = value.info;
        const tpl = this.bsTemplate();
        if (tpl) {
          this.bottomSheet.open(tpl);
        }
      });
  }

  protected toggleNode(node: ByElements): void {
    node.children?.forEach((child) => {
      if (child.hasOwnProperty("finanicalElementExts")) {
        if (child.hasOwnProperty("visible")) {
          child.visible = !child.visible;
        } else {
          child.visible = true;
        }
      }
    });
  }

  protected formatFinancialType(type: string): string {
    return type.toLocaleLowerCase() === "BalanceSheet".toLowerCase()
      ? "BS"
      : type.toLocaleLowerCase() === "Cashflow".toLowerCase()
        ? "CF"
        : "IC";
  }

  private createElementNodeTree(
    symbolFinancials: SymbolFinancials[],
  ): BySymbolElements[] {
    const bySymbolElementExtsMap = FinancialsDataUtils.groupByKey<
      FinancialElementExt,
      string
    >(FinancialsDataUtils.toFinancialElementsExt(symbolFinancials), "symbol");
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
          visible: false,
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
    return myBySymbolElements;
  }
}
