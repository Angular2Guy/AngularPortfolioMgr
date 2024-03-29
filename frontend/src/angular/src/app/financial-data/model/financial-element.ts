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
export interface FinancialElement {
  id: number;
  label: string;
  concept: string;
  financialElementType: string;
  currency: string;
  value: number;
  info?: string;
}

export class FinancialElementExt implements FinancialElement {
  constructor(financialElement: FinancialElement) {
    this.id = financialElement.id;
    this.label = financialElement.label;
    this.concept = financialElement.concept;
    this.financialElementType = financialElement.financialElementType;
    this.currency = financialElement.currency;
    this.value = financialElement.value;
    this.info = !financialElement?.info ? this.info : financialElement.info;
  }

  id: number = -1;
  label: string = "";
  concept: string = "";
  financialElementType: string = "";
  currency: string = "";
  value: number = 0;
  year: number = 0;
  quarter: string = "";
  symbol: string = "";
  info: string = "";
}
