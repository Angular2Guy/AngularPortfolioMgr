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
import { Symbol } from "./symbol";
import { PortfolioElement } from "./portfolio-element";

export interface Portfolio extends CommonValues {
  userId: number;
  symbols: Symbol[];
  portfolioElements: PortfolioElement[];
}

export interface CommonValues {
  id: number;
  currencyKey: string;
  name: string;
  createdAt: string;
  month1: number;
  month6: number;
  year1: number;
  year2: number;
  year5: number;
  year10: number;
}
