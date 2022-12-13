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
import { Component, Input } from "@angular/core";
import { FinancialElementExt } from "../../model/financial-element";
import { FinancialsDataUtils } from "../../model/financials-data-utils";
import { SymbolFinancials } from "../../model/symbol-financials";

interface ByYearElements {
	year: number;
	finanicalElementExts: FinancialElementExt[];
}

interface BySymbolElements {
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
  private bySymbolElements: BySymbolElements[] = [];
  
  get symbolFinancials(): SymbolFinancials[] {
	  return this._symbolFinancials; 
  }
  
  @Input()
  set symbolFinancials(symbolFinancials: SymbolFinancials[]) {
	  this._symbolFinancials = symbolFinancials;
	  console.log(symbolFinancials);
	  const mySymbolFinancialsExts =  FinancialsDataUtils.toFinancialElementsExt(symbolFinancials);
	  //const byYearElements = groupByKey<FinancialElementExt, ByYearElements>(mySymbolFinancialsExts, 'year');
	  const bySymbolElementExtsMap = FinancialsDataUtils.groupByKey<FinancialElementExt,string>(mySymbolFinancialsExts, 'symbol');
	  console.log(bySymbolElementExtsMap);
	  const myBySymbolElements: BySymbolElements[] = [];
	  bySymbolElementExtsMap.forEach((value, key) => {
		  const byYearElementsMap = FinancialsDataUtils.groupByKey<FinancialElementExt,number>(value, 'year');
		  const byYearElements: ByYearElements[] = [];
		  byYearElementsMap.forEach((value, key) => {
			  const element = {
				  year: key,
				  finanicalElementExts: value
			  } as ByYearElements;
			  byYearElements.push(element);
		  });
		  const myBySymbolElement = {
			  symbol: key,
			  byYearElements: byYearElements
		  } as BySymbolElements;
		  myBySymbolElements.push(myBySymbolElement);
	  });
	  console.log(myBySymbolElements);
	  this.bySymbolElements = myBySymbolElements;
  }
}
