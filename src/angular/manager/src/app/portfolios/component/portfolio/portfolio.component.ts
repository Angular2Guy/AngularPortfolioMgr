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
import { Component, OnInit, OnDestroy, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { switchMap, tap } from 'rxjs/operators';
import { Symbol } from '../../model/symbol';
import { TokenService } from 'src/app/service/token.service';
import { PortfolioService } from '../../service/portfolio.service';
import { Subscription, Subject } from 'rxjs';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.scss']
})
export class PortfolioComponent implements OnInit, OnDestroy { 
  symbols: Symbol[] = [];
  reloadData = false;
  windowHeight = 0;
  portfolioName = '';
  selSymbol: Symbol = null;
  private routeParamSubscription: Subscription;

  constructor(private route: ActivatedRoute, private tokenService: TokenService, 
              private portfolioService: PortfolioService, private changeDetectorRef: ChangeDetectorRef) { }

  ngOnInit(): void {
	this.windowHeight = window.innerHeight - 84;
	this.routeParamSubscription = this.route.paramMap.pipe(tap(() => this.reloadData = true),
		switchMap((params: ParamMap) => this.portfolioService.getPortfolioById(parseInt(params.get('portfolioId')))),
		tap(() => this.reloadData = false))		
		.subscribe(myPortfolio => {
			this.symbols = myPortfolio.symbols;
			this.portfolioName = myPortfolio.name;});
  }

  ngOnDestroy(): void {
    this.routeParamSubscription.unsubscribe();	
  }   

  updateReloadData(state: boolean) {
	this.reloadData = state;
	this.changeDetectorRef.detectChanges();
	//console.log('loading:'+state);
  }

  selectSymbol(symbol: Symbol): void {
	this.selSymbol = symbol;
	//console.log(symbol);
  }

  logout():void {
	this.tokenService.clear();	
  }
}
