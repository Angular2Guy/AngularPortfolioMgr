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
import { Portfolio } from '../../../model/portfolio';
import { Router } from '@angular/router';

@Component({
  selector: 'app-portfolio-table',
  templateUrl: './portfolio-table.component.html',
  styleUrls: ['./portfolio-table.component.scss']
})
export class PortfolioTableComponent implements OnInit {
  portfolios = new MatTableDataSource<Portfolio>([]);
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];  

  constructor(private router: Router) { }

  ngOnInit(): void {
	const x = 1 + 1;
  }

  selPortfolio(portfolio: Portfolio) {
	this.router.navigate(['/portfolios/portfolio-detail/portfolio', portfolio.id]);	
  }

  @Input()
  set localPortfolios(localPortfolios: Portfolio[]) {
	this.portfolios.connect().next(localPortfolios);  	
  }

  get localPortfolios(): Portfolio[] {
	return this.portfolios.data;
}
}
