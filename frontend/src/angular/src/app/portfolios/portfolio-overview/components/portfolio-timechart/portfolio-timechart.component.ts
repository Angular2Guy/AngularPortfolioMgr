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
import { Component, Input, OnInit } from "@angular/core";
import { DateTime } from "luxon";
import { Portfolio } from "src/app/model/portfolio";
import { ServiceUtils } from "src/app/model/service-utils";
import { PortfolioService } from "src/app/service/portfolio.service";
import { Item } from "../../model/item";

@Component({
  selector: "app-portfolio-timechart",
  templateUrl: "./portfolio-timechart.component.html",
  styleUrls: ["./portfolio-timechart.component.scss"],
})
export class PortfolioTimechartComponent implements OnInit {
  private localSelPortfolio: Portfolio;
  protected start = new Date();
  protected items: Item<Event>[] = [];
  protected showDays = false;
  
  constructor(private portfolioService: PortfolioService) {}

  ngOnInit(): void {
	this.portfolioService.getPortfolioByIdWithHistory(this.localSelPortfolio.id).subscribe(result => {
		//console.log(result);
		const myItems = result.symbols.filter(mySymbol => !mySymbol.symbol.includes(ServiceUtils.PORTFOLIO_MARKER)).map((mySymbol, index) => {
			let myItem = new Item<Event>();
			myItem.id = index;
			myItem.details = mySymbol.description;
			myItem.name = mySymbol.name;
			myItem.start = new Date(mySymbol.changedAt);
			myItem.end = !mySymbol?.removedAt ? null : new Date(mySymbol.removedAt);
			//console.log(myItem);
			return myItem;
		});		
		this.items = myItems;		    
	});	
    /*
    this.start = DateTime.now().minus({ year: 2 }).toJSDate();
    let myItem = new Item<Event>();
    myItem.id = 1;
    myItem.name = "MyName1";
    myItem.details = "MyDetails1";
    myItem.start = this.start;
    myItem.end = new Date();
    this.items.push(myItem);
    myItem = new Item<Event>();
    myItem.id = 2;
    myItem.name = "MyName2";
    myItem.details = "MyDetails2";
    myItem.start = DateTime.now().minus({ year: 1 }).toJSDate();
    myItem.end = null;
    this.items.push(myItem);
    */
  }

  get selPortfolio(): Portfolio {
    return this.localSelPortfolio;
  }

  @Input({required: true})
  set selPortfolio(myPortfolio: Portfolio) {
    this.localSelPortfolio = myPortfolio;
  }
}
