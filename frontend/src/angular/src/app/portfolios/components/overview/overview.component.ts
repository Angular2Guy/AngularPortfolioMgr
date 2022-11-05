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
import { TokenService } from 'ngx-simple-charts/base-service';
import { Router } from '@angular/router';
import { PortfolioService } from '../../../service/portfolio.service';
import { Portfolio } from '../../../model/portfolio';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { NewPortfolioComponent } from '../new-portfolio/new-portfolio.component';
import { PortfolioData } from '../../../model/portfolio-data';
import { SymbolImportService } from '../../../service/symbol-import.service';
import { forkJoin, Subscription } from 'rxjs';
import { AddSymbolComponent } from '../add-symbol/add-symbol.component';
import { Symbol } from '../../../model/symbol';
import { QuoteImportService } from '../../../service/quote-import.service';
import { ConfigService } from 'src/app/service/config.service';
import { ProdConfigComponent } from '../prod-config/prod-config.component';
import { DevConfigComponent } from '../dev-config/dev-config.component';
import { SpinnerData, DialogSpinnerComponent } from 'src/app/base/components/dialog-spinner/dialog-spinner.component';
import { OnDestroy } from '@angular/core';

@Component({
	selector: 'app-overview',
	templateUrl: './overview.component.html',
	styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
	windowHeight: number = null;
	portfolios: Portfolio[] = [];
	myPortfolio!: Portfolio;
	displayedColumns = ['name', 'stocks', 'month1', 'month6', 'year1', 'year2', 'year5', 'year10'];
	importingSymbols = false;
	private timeoutId = -1;
	dialogRef: MatDialogRef<unknown, any> = null;
	financialsDialogRef: MatDialogRef<unknown, any> = null;
	dialogSubscription: Subscription;
	private profiles: string = null;
	private showPortfolioTable = true;

	constructor(private tokenService: TokenService, private configService: ConfigService,
		private router: Router,
		private portfolioService: PortfolioService,
		private symbolImportService: SymbolImportService,
		private quoteImportService: QuoteImportService,
		private dialog: MatDialog) { }

	ngOnInit() {
		this.windowHeight = window.innerHeight - 84;
		this.refreshPortfolios();
		this.configService.getProfiles().subscribe(value => this.profiles = !value ? 'dev' : value);
	}

    ngOnDestroy(): void {		
        this.dialogRef = null;
		if(!!this.dialogSubscription) {
			this.dialogSubscription.unsubscribe();
			this.dialogSubscription = null;
		}
    }

	@HostListener('window:resize', ['$event'])
	onResize(event: any) {
		this.windowHeight = event.target.innerHeight - 84;
	}

	newPortfolio() {
		if (!this.dialogRef) {
			if(!!this.dialogSubscription) {
				this.dialogSubscription.unsubscribe();
			}
			const portfolio: Portfolio = {
				id: null, createdAt: new Date().toISOString(), month1: null, month6: null, name: null, symbols: [], currencyKey: null,
				portfolioElements: [], userId: (this.tokenService.userId as number), year1: null, year10: null, year2: null, year5: null
			};
			const newPortfolioData: PortfolioData = { portfolio: portfolio };
			this.dialogRef = this.dialog.open(NewPortfolioComponent, { width: '500px', data: newPortfolioData });
			this.dialogSubscription = this.dialogRef.afterClosed().subscribe(result => {
				if (result) {
					this.portfolioService.postPortfolio(result)
						.subscribe(myPortfolio => {
							this.portfolios = [...this.portfolios, myPortfolio];
							this.myPortfolio = myPortfolio;
							this.selPortfolio(myPortfolio, true);
						});
				}
				this.dialogRef = null;
			});
		}
	}

	selPortfolio(portfolio: Portfolio, showPortTab = false) {
		this.myPortfolio = portfolio;
		this.showPortfolioTable = showPortTab ? true : !this.showPortfolioTable;
		const myPath = !this.showPortfolioTable ? 'portfolio-overview/portfolio-charts' : 'table';  
		//console.log(this.showPortfolioTable, `/portfolios/overview/${myPath}`);
		//the -1 portfolioId is filtered out and forces a update of the route
		if(showPortTab) {
		   this.router.navigate([`/portfolios/overview/${myPath}`, -1])
		      .then(() => this.router.navigate([`/portfolios/overview/${myPath}`, portfolio.id]));
		   } else {
			  this.router.navigate([`/portfolios/overview/${myPath}`, portfolio.id]);
		}
	}

	private refreshPortfolios() {
		this.portfolioService.getPortfolioByUserId((this.tokenService.userId as number)).subscribe(myPortfolios => {
			myPortfolios.forEach(port => port.symbols = !port.symbols ? [] : port.symbols);
			this.portfolios = myPortfolios;
			this.myPortfolio = myPortfolios.length > 0 ? myPortfolios[0] : this.myPortfolio;
			if(!!this.myPortfolio) {
				this.selPortfolio(this.myPortfolio, true);
			}
		});
	}

	addSymbol(portfolio: Portfolio) {
		const portfolioData: PortfolioData = { portfolio: portfolio };
		if(!!this.dialogSubscription) {
			this.dialogSubscription.unsubscribe();
		}
		const dialogRef = this.dialog.open(AddSymbolComponent, { width: '500px', disableClose: true, hasBackdrop: true, data: portfolioData });
		this.dialogSubscription = dialogRef.afterClosed().subscribe((symbol: Symbol) => {
			if (symbol) {
				const dialogSpinnerRef = this.dialog.open(DialogSpinnerComponent, { width: '500px', disableClose: true, hasBackdrop: true, 
				   enterAnimationDuration: '500ms', exitAnimationDuration: '500ms', data: {title: $localize`:@@overviewPortfolioCalc:Portfolio Calculation`} as SpinnerData  });
				symbol.weight = !symbol.weight ? 0 : symbol.weight;
				this.portfolioService.postSymbolToPortfolio(portfolio, symbol.id, symbol.weight, symbol.changedAt)
					.subscribe(result => {
						if (result) {
							const filteredPortfolios = this.portfolios.filter(port => port.id !== result.id);
							this.portfolios = [...filteredPortfolios, result];
							this.myPortfolio = result;
							this.selPortfolio(result, true);
							dialogSpinnerRef.close();
						}
					});
			}
		});
	}

	importSymbols(): void {
		this.importingSymbols = true;
		if (this.timeoutId >= 0) {
			clearTimeout(this.timeoutId);
		}
		forkJoin(
			this.symbolImportService.getSymbolImportUs(),
			this.symbolImportService.getSymbolImportHk(),
			this.symbolImportService.getSymbolImportDe(),
			this.quoteImportService.importFxDailyQuotes('USD'),
			this.quoteImportService.importFxDailyQuotes('HKD'))
			.subscribe(([resultUs, resultHk, resultDe, resultUSD, resultHKD]) => {
				console.log(`Us symbols: ${resultUs}, Hk symbols: ${resultHk}, De symbols: ${resultDe}, Usd quotes: ${resultUSD}, Hkd quotes: ${resultHKD}`);
				setTimeout(() => this.symbolImportService.getIndexSymbols()
					.subscribe(resultIndex => {
						console.log(`Index Symbols: ${resultIndex}`);
						this.importingSymbols = false;
					}), 60000);
			});
	}

	logout(): void {
		this.tokenService.logout();
	}

	showConfig(): void {
		if (!this.dialogRef && this.profiles) {
			if(!!this.dialogSubscription) {
				this.dialogSubscription.unsubscribe();				
			}
			const myOptions = { width: '700px' };
			this.dialogRef = this.profiles.toLowerCase().includes('prod') ? 
				this.dialog.open(ProdConfigComponent, myOptions) : this.dialog.open(DevConfigComponent, myOptions);
			this.dialogSubscription = this.dialogRef.afterClosed().subscribe(() => this.dialogRef = null);
		}
	}
}
