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
import { Component, Input, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { ChartSlices, ChartSlice } from 'ngx-simple-charts/donut';
import { Portfolio } from 'src/app/model/portfolio';

interface CalcPortfolioElement {
	name: string;
	sector: string; 
	value: number;
}

@Component({
  selector: 'app-portfolio-sectors',
  templateUrl: './portfolio-sectors.component.html',
  styleUrls: ['./portfolio-sectors.component.scss']
})
export class PortfolioSectorsComponent implements OnInit, AfterViewInit {
  localSelPortfolio: Portfolio;
  chartSlices: ChartSlices = {title: '', from: '', xScaleHeight: 0, yScaleWidth: 0, chartSlices: []};
  chartsLoading = true;
  @ViewChild('hideMe') 
  divHideMe: ElementRef;
  afterViewInitDone = false;
  slicesSum = 1;
   
  private readonly colorKeys = ['--red','--purple', '--blue','-cyan','--green','--lime','--orange','--gray'];
  
  constructor() { }

  ngOnInit(): void {	
	this.chartSlices.title = this.selPortfolio.name;
	this.chartSlices.chartSlices = [];
	this.chartsLoading = false;
  }

  ngAfterViewInit(): void {
	this.afterViewInitDone = true;
	this.drawDonut();
  }
  
  private drawDonut(): void {
	if(!this.afterViewInitDone || !this.selPortfolio?.id) {
		return;
	}
	const sliceColors = window.getComputedStyle(this.divHideMe.nativeElement,':before')['content']
	   .replace('"', '').replace('\"','').split(',');
	//console.log(sliceColors);	
	const valueMap = this.selPortfolio.portfolioElements
	  .map(pe => ({name: pe.name, sector: pe.sector, value: (pe.lastClose * pe.weight)} as CalcPortfolioElement))
	  .reduce((myMap, cpe) => {
		let myValue = myMap.get(cpe.sector);
		myValue = !myValue ? 0 : myValue;
		myMap.set(cpe.sector, myValue + cpe.value);
		return myMap;  
	},new Map<string,number>());
	let calcColors = [];
	while(calcColors.length < valueMap.size) {
		calcColors = calcColors.concat(sliceColors);
	}
	let i = 0;
	valueMap.forEach((myValue, myKey) => { 
		i = i + 1;
		this.chartSlices.chartSlices.push({name: myKey, value: myValue, color: calcColors[i]} as ChartSlice);
	});
	this.slicesSum = this.chartSlices.chartSlices.reduce((acc, mySlice) => acc = acc + mySlice.value, 0);
	this.chartSlices.chartSlices = this.chartSlices.chartSlices.sort((chartSliceA, chartSliceB) => chartSliceA.value - chartSliceB.value).reverse();
	//console.log(this.chartSlices.chartSlices);
  }
  
  get selPortfolio(): Portfolio {
	return this.localSelPortfolio;
  }
  
  @Input()
  set selPortfolio(myPortfolio: Portfolio) {
	this.localSelPortfolio = myPortfolio;
	this.drawDonut();
  }
}
