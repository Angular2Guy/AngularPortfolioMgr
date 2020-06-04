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
import { MatDialog } from '@angular/material/dialog';
import { NewPortfolioComponent } from '../new-portfolio/new-portfolio.component';
import { PortfolioData } from '../../model/portfolio-data';
import { SymbolImportService } from '../../service/symbol-import.service';
import { forkJoin } from 'rxjs';
import { AddSymbolComponent } from '../add-symbol/add-symbol.component';
import { Symbol } from '../../model/symbol';
import { QuoteImportService } from '../../service/quote-import.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {
  windowHeight: number = null;
  portfolios: Portfolio[] = [];
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];
  importingSymbols = false;
  showPortfolioTable = true;

  constructor(private tokenService: TokenService,
		private router: Router,
		private portfolioService: PortfolioService,
		private symbolImportService: SymbolImportService,
		private quoteImportService: QuoteImportService,	
		private dialog: MatDialog) { }

  ngOnInit() {
	this.windowHeight = window.innerHeight - 84;
	this.refreshPortfolios();
  }

  @HostListener( 'window:resize', ['$event'] )
  onResize( event: any ) {
    this.windowHeight = event.target.innerHeight - 84;
  }

  newPortfolio() {
	const portfolio: Portfolio = {id: null, createdAt: new Date().toISOString(), month1: null, month6: null, name: null, symbols: [], 
		userId: this.tokenService.userId, year1: null, year10: null, year2: null, year5: null};
	const newPortfolioData: PortfolioData = { portfolio: portfolio };
	const dialogRef = this.dialog.open(NewPortfolioComponent, { width: '500px', data: newPortfolioData});
	dialogRef.afterClosed().subscribe( result => {
		if(result) {
			this.portfolioService.postPortfolio(result)
				.subscribe(myPortfolio => this.portfolios = [...this.portfolios, myPortfolio]);
		}
	});
  }

  selPortfolio(portfolio: Portfolio) {
	this.router.navigate(['/portfolios/portfolio', portfolio.id]);
  }

  private refreshPortfolios() {
	this.portfolioService.getPortfolioByUserId(this.tokenService.userId).subscribe(myPortfolios => {
		myPortfolios.forEach(port => port.symbols = !port.symbols ?  [] : port.symbols);
		this.portfolios = myPortfolios;
	});
  }

  addSymbol(portfolio: Portfolio) {
	const portfolioData: PortfolioData = { portfolio: portfolio };
	const dialogRef = this.dialog.open(AddSymbolComponent, { width: '500px', data: portfolioData});
	dialogRef.afterClosed().subscribe( (symbol: Symbol) => {
		if(symbol) {
			symbol.weight = !symbol.weight ? 0 : symbol.weight;
			this.portfolioService.postSymbolToPortfolio(portfolio, symbol.id, symbol.weight, symbol.changedAt)
				.subscribe(result => {
					if(result) {
						const filteredPortfolios = this.portfolios.filter(port => port.id !== result.id);
						this.portfolios = [...filteredPortfolios, result];
						//this.refreshPortfolios();
					}
				});
		}
	});
  }

  importSymbols():void {
	this.importingSymbols = true;
	forkJoin(
		this.symbolImportService.getSymbolImportUs(),
		this.symbolImportService.getSymbolImportHk(),
		this.symbolImportService.getSymbolImportDe(),
		this.quoteImportService.importFxDailyQuotes('USD'),
		this.quoteImportService.importFxDailyQuotes('HKD'))
		.subscribe(([resultUs, resultHk, resultDe, resultUSD, resultHKD]) => { 
			console.log(`Us symbols: ${resultUs}, Hk symbols: ${resultHk}, De symbols: ${resultDe}, Usd quotes: ${resultUSD}, Hkd quotes: ${resultHKD}`);
			this.importingSymbols = false;
		});
  }

  logout():void {
	this.tokenService.clear();	
  }
}
