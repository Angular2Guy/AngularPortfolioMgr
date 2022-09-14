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
import { Component, OnInit, Input } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Portfolio, CommonValues } from '../../../model/portfolio';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-portfolio-table',
  templateUrl: './portfolio-table.component.html',
  styleUrls: ['./portfolio-table.component.scss']
})
export class PortfolioTableComponent implements OnInit {
  private myLocalPortfolio: Portfolio = null;
  portfolioElements = new MatTableDataSource<CommonValues>([]);
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];  

  constructor(private router: Router, private route: ActivatedRoute) { }

  ngOnInit(): void {
	this.route.data.subscribe(myData => this.localPortfolio = myData as Portfolio);
  }

  selPortfolio(commonValues: CommonValues) {
	console.log(commonValues.id);
	this.router.navigate(['/portfolios/portfolio-detail/portfolio', this.myLocalPortfolio.id]);	
  }

  set localPortfolio(localPortfolio: Portfolio) {
	this.myLocalPortfolio = localPortfolio;
	const myPortfolioElements: CommonValues[] = [];
	if(localPortfolio) {
	   myPortfolioElements.push(localPortfolio)
	   myPortfolioElements.push(...localPortfolio?.portfolioElements);
	}
	this.portfolioElements.connect().next(myPortfolioElements);  	
  }

  get localPortfolio(): Portfolio {
	return this.myLocalPortfolio;
  }
}
