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
import { Component, OnInit, HostListener } from '@angular/core';
import { TokenService } from '../../../service/token.service';
import { Router } from '@angular/router';
import { PortfolioService } from '../../service/portfolio.service';
import { Portfolio } from '../../model/portfolio';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {
  windowHeight: number = null;
  portfolios: Portfolio[] = [];
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];

  constructor(private tokenService: TokenService,
		private router: Router,
		private portfolioService: PortfolioService) { }

  ngOnInit() {
	this.windowHeight = window.innerHeight - 84;
	this.portfolioService.getPortfolio(this.tokenService.userId).subscribe(myPortfolios => this.portfolios = myPortfolios);
  }

  @HostListener( 'window:resize', ['$event'] )
  onResize( event: any ) {
    this.windowHeight = event.target.innerHeight - 84;
  }

  selPortfolio() {
	console.log('hallo');
  }

  logout():void {
	this.tokenService.clear();
	this.router.navigate(['/login/login']);
  }
}
