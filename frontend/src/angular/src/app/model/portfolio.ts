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
  
  year1CorrelationSp500?: number;
  year1CorrelationMsciChina?: number;
  year1CorrelationEuroStoxx50?: number;
  year1LinRegReturnSp500?: number;
  year1LinRegReturnMsciChina?: number;
  year1LinRegReturnEuroStoxx50?: number;
  year1SigmaSp500?: number;
  year1SigmaMsciChina?: number;
  year1SigmaEuroStoxx50?: number;
  year1SigmaPortfolio?: number;
  year2CorrelationSp500?: number;
  year2CorrelationMsciChina?: number;
  year2CorrelationEuroStoxx50?: number;
  year2LinRegReturnSp500?: number;
  year2LinRegReturnMsciChina?: number;
  year2LinRegReturnEuroStoxx50?: number;
  year2SigmaSp500?: number;
  year2SigmaMsciChina?: number;
  year2SigmaEuroStoxx50?: number;
  year2SigmaPortfolio?: number;
  year5CorrelationSp500?: number;
  year5CorrelationMsciChina?: number;
  year5CorrelationEuroStoxx50?: number;
  year5LinRegReturnSp500?: number;
  year5LinRegReturnMsciChina?: number;
  year5LinRegReturnEuroStoxx50?: number;
  year5SigmaSp500?: number;
  year5SigmaMsciChina?: number;
  year5SigmaEuroStoxx50?: number;
  year5SigmaPortfolio?: number;
  year10CorrelationSp500?: number;
  year10CorrelationMsciChina?: number;
  year10CorrelationEuroStoxx50?: number;
  year10LinRegReturnSp500?: number;
  year10LinRegReturnMsciChina?: number;
  year10LinRegReturnEuroStoxx50?: number;
  year10SigmaSp500?: number;
  year10SigmaMsciChina?: number;
  year10SigmaEuroStoxx50?: number;
  year10SigmaPortfolio?: number;
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
