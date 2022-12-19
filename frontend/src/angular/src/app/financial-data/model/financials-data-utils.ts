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
import { FinancialElementExt } from "./financial-element";
import { SymbolFinancials } from "./symbol-financials";

export enum ItemType {
  Query = "Query",
  TermStart = "TermStart",
  TermEnd = "TermEnd",
}

export class FinancialsDataUtils {
  public static groupByKey<A, B>(array: A[], key: string): Map<B, A[]> {
    const resultObj = array.reduce((hash, obj) => {
      if (obj[key] === undefined || obj[key] === null) return hash;
      return Object.assign(hash, {
        [obj[key]]: (hash[obj[key]] || []).concat(obj),
      });
    }, {});
    return new Map(Object.entries(resultObj)) as Map<B, A[]>;
  }

  public static toFinancialElementsExt(
    symbolFiancials: SymbolFinancials[]
  ): FinancialElementExt[] {
    const myResult = symbolFiancials.map((mySymbolFinancials) =>
      mySymbolFinancials.financialElements.map((myFinancialElement) => {
        const financialElementExt = new FinancialElementExt(myFinancialElement);
        financialElementExt.year = mySymbolFinancials.year;
        financialElementExt.quarter = mySymbolFinancials.quarter;
        financialElementExt.symbol = mySymbolFinancials.symbol;
        return financialElementExt;
      })
    );
    return [].concat.apply([], myResult);
  }

  public static compareNumbers(
    valueA: number,
    valueB: number,
    operator: string
  ): boolean {
    let result = !!valueA && !!valueB;
    switch (operator.trim()) {
      case "=":
        result = valueA === valueB;
        break;
      case "<=":
        result = valueA <= valueB;
        break;
      case ">=":
        result = valueA >= valueB;
        break;
      default:
        throw "Missing number operator: " + operator;
    }
    return result;
  }

  public static compareStrings(
    valueA: string,
    valueB: string,
    operator: string
  ): boolean {
    let result = !!valueA && !!valueB;
    switch (operator.trim()) {
      case "=":
        result =
          valueA.trim().toLowerCase() === valueB.trim().toLocaleLowerCase();
        break;
      case "*=":
        result = valueA
          .trim()
          .toLowerCase()
          .startsWith(valueB.trim().toLowerCase());
        break;
      case "=*":
        result = valueA
          .trim()
          .toLowerCase()
          .endsWith(valueB.trim().toLowerCase());
        break;
      case "*=*":
        result = valueA
          .trim()
          .toLowerCase()
          .includes(valueB.trim().toLowerCase());
        break;
      default:
        throw "Missing string operator: " + operator;
    }
    return result;
  }
}
