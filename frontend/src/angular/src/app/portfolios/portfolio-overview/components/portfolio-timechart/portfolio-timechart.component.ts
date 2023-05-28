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
import { Component, Input, OnInit } from '@angular/core';
import { DateTime } from 'luxon';
import { Portfolio } from 'src/app/model/portfolio';
import { Item } from '../../model/item';

@Component({
  selector: 'app-portfolio-timechart',
  templateUrl: './portfolio-timechart.component.html',
  styleUrls: ['./portfolio-timechart.component.scss']
})
export class PortfolioTimechartComponent implements OnInit {
  private localSelPortfolio: Portfolio;
  protected start = new Date();
  protected items: Item<Event>[] = [];

  ngOnInit(): void {		  
	this.start = DateTime.now().minus({year: 2}).toJSDate();
	const myItem = new Item<Event>();
	myItem.id = 1;
	myItem.name = 'MyName';
	myItem.details = 'MyDetails';
	myItem.start = this.start;
	myItem.end = new Date();
	this.items.push(myItem);
  }
	
  get selPortfolio(): Portfolio {
    return this.localSelPortfolio;
  }

  @Input()
  set selPortfolio(myPortfolio: Portfolio) {
    this.localSelPortfolio = myPortfolio;
  }
}
