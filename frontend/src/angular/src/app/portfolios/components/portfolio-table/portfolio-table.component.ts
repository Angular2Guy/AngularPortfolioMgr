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
import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Portfolio, CommonValues } from '../../../model/portfolio';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { Subscription } from 'rxjs';
import { switchMap, tap, filter } from 'rxjs/operators';
import { PortfolioService } from '../../../service/portfolio.service'

@Component({
  selector: 'app-portfolio-table',
  templateUrl: './portfolio-table.component.html',
  styleUrls: ['./portfolio-table.component.scss']
})
export class PortfolioTableComponent implements OnInit,OnDestroy {
  private myLocalPortfolio: Portfolio = null;
  portfolioElements = new MatTableDataSource<CommonValues>([]);
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];  
  private dataSubscription: Subscription;
  reloadData = false;

  constructor(private router: Router, private route: ActivatedRoute, private portfolioService: PortfolioService) { }

  ngOnInit(): void {
	this.dataSubscription = this.route.paramMap.pipe(filter((params: ParamMap) => parseInt(params.get('portfolioId')) >= 0),
		tap(() => this.reloadData = true),
		switchMap((params: ParamMap) => this.portfolioService.getPortfolioById(parseInt(params.get('portfolioId')))),
		tap(() => this.reloadData = false))
		.subscribe(myData => this.localPortfolio = myData);
  }
  
  ngOnDestroy(): void {
	this.dataSubscription.unsubscribe();
  }

  selPortfolio(commonValues: CommonValues) {
	console.log(commonValues.id);
	this.router.navigate(['/portfolios/portfolio-detail/portfolio', this.myLocalPortfolio.id]);	
  }

  set localPortfolio(localPortfolio: Portfolio) {
	this.myLocalPortfolio = localPortfolio;
	const myPortfolioElements: CommonValues[] = [];
	if(!!localPortfolio?.portfolioElements) {
	   myPortfolioElements.push(localPortfolio)
	   myPortfolioElements.push(...localPortfolio?.portfolioElements);
	}
	this.portfolioElements.connect().next(myPortfolioElements);  	
  }

  get localPortfolio(): Portfolio {
	return this.myLocalPortfolio;
  }
}
