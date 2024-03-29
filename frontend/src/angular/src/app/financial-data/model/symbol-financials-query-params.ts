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
export interface FilterBase {
  operation: string;
}
export interface FilterNumber extends FilterBase {
  value: number;
}

export interface FilterString extends FilterBase {
  value: string;
}

export interface FinancialElementParams {
  conceptFilter?: FilterString;
  valueFilter?: FilterNumber;
  operation?: string;
  termType: string;
}

export interface SymbolFinancialsQueryParams {
  yearFilter: FilterNumber;
  quarters: string[];
  symbol: string;
  country: string;
  name: string;
  financialElementParams: FinancialElementParams[];
}
