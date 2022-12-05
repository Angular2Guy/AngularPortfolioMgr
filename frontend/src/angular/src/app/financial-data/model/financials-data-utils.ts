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
export enum ItemType {Query = 'Query', TermStart = 'TermStart', TermEnd = 'TermEnd'};



export class FinancialsDataUtils {
	public static compareNumbers(valueA: number, valueB: number, operator: string): boolean {
		let result = !!valueA && !!valueB; 
		switch(operator.trim()) {
			case '=': 
			   result = valueA === valueB;
			   break;
			case '<=':
			   result = valueA <= valueB;
			   break;
			case '>=':
			   result = valueA >= valueB;
			   break;
			default:
			   throw 'Missing number operator: ' + operator;
		}
		return result;
	}
	
	public static compareStrings(valueA: string, valueB: string, operator: string): boolean {
		let result = !!valueA && !!valueB;
		switch(operator.trim()) {
			case '=':
			   result = valueA.trim().toLowerCase() === valueB.trim().toLocaleLowerCase();
			   break;
			case '*=':
			   result = valueA.trim().toLowerCase().startsWith(valueB.trim().toLowerCase());
			   break;
			case '=*':
			   result = valueA.trim().toLowerCase().endsWith(valueB.trim().toLowerCase());
			   break;
			case '*=*':
			   result = valueA.trim().toLowerCase().includes(valueB.trim().toLowerCase());
			   break;
			default:
			   throw 'Missing string operator: ' + operator;
		}
		return result;
	}
}