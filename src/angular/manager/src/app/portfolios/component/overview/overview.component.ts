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
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { NewPortfolioComponent } from '../new-portfolio/new-portfolio.component';
import { PortfolioData } from '../../model/portfolio-data';
import { SymbolImportService } from '../../service/symbol-import.service';
import { forkJoin } from 'rxjs';
import { AddSymbolComponent } from '../add-symbol/add-symbol.component';
import { Symbol } from '../../model/symbol';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {
  windowHeight: number = null;
  portfolios = new MatTableDataSource<Portfolio>([]);
  displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];

  constructor(private tokenService: TokenService,
		private router: Router,
		private portfolioService: PortfolioService,
		private symbolImportService: SymbolImportService,
		private dialog: MatDialog) { }

  ngOnInit() {
	this.windowHeight = window.innerHeight - 84;
	this.portfolioService.getPortfolio(this.tokenService.userId).subscribe(myPortfolios => {
		do{
			this.portfolios.data.pop();
		} while(this.portfolios.data.length > 0);
			myPortfolios.forEach(port => { 
				port.symbols = !port.symbols ?  [] : port.symbols;
			  	this.portfolios.data.push(port);
			});
		});
  }

  @HostListener( 'window:resize', ['$event'] )
  onResize( event: any ) {
    this.windowHeight = event.target.innerHeight - 84;
  }

  newPortfolio() {
	const portfolio: Portfolio = {id: null, month1: null, months6: null, name: null, symbols: [], 
		userId: this.tokenService.userId, year1: null, year10: null, year2: null, year5: null};
	const newPortfolioData: PortfolioData = { portfolio: portfolio };
	const dialogRef = this.dialog.open(NewPortfolioComponent, { width: '500px', data: newPortfolioData});
	dialogRef.afterClosed().subscribe( result => {
		if(result) {
			this.portfolioService.postPortfolio(result).subscribe(myPortfolio => this.portfolios.data.push(myPortfolio));
		}
	});
  }

  selPortfolio(portfolio: Portfolio) {
	console.log(portfolio);
  }

  addSymbol(portfolio: Portfolio) {
	const portfolioData: PortfolioData = { portfolio: portfolio };
	const dialogRef = this.dialog.open(AddSymbolComponent, { width: '500px', data: portfolioData});
	dialogRef.afterClosed().subscribe( (symbol: Symbol) => {
		if(symbol) {
			portfolio.symbols.push(symbol);
			this.portfolioService.postPortfolio(portfolio)
				.subscribe(myPortfolio => this.portfolios.data.map(localPort => localPort.id === portfolio.id ? myPortfolio : localPort));
		}
	});
  }

  importSymbols():void {
	forkJoin(
		this.symbolImportService.getSymbolImportUs(),
		this.symbolImportService.getSymbolImportHk(),
		this.symbolImportService.getSymbolImportDe())
		.subscribe(([resultUs, resultHk, resultDe]) => 
			console.log(`Us symbols: ${resultUs}, Hk symbols: ${resultHk}, De symbols: ${resultDe}`));
  }

  logout():void {
	this.tokenService.clear();
	this.router.navigate(['/login/login']);
  }
}
