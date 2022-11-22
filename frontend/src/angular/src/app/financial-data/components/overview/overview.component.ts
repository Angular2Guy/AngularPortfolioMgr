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
import { Component, OnInit, HostListener, OnDestroy } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { switchMap,tap } from 'rxjs/operators';
import { forkJoin, Subscription } from 'rxjs';
import { ImportFinancialsComponent } from '../import-financials/import-financials.component';
import { FinancialDataService } from 'src/app/service/financial-data.service';
import { ImportFinancialsData } from '../../../model/import-financials-data';
import { TokenService } from 'ngx-simple-charts/base-service';
import { ConfigService } from 'src/app/service/config.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
  protected windowHeight: number = null;
  private dialogSubscription: Subscription;
  
  constructor(private financialDataService: FinancialDataService, private tokenService: TokenService, private dialog: MatDialog, private configService: ConfigService) { }

  ngOnInit(): void {
	this.windowHeight = window.innerHeight - 84;
  }

    ngOnDestroy(): void {		
		this.cleanupDialogSubcription();
    }

    private cleanupDialogSubcription(): void {
	   if(!!this.dialogSubscription) {
			this.dialogSubscription.unsubscribe();
			this.dialogSubscription = null;
		}
    }

	@HostListener('window:resize', ['$event'])
	onResize(event: any) {
		this.windowHeight = event.target.innerHeight - 84;
	}

    showFinancialsImport(): void {
		this.cleanupDialogSubcription();
		this.configService.getImportPath().subscribe(result => {
			const dialogRef = this.dialog.open(ImportFinancialsComponent, { width: '500px', disableClose: true, hasBackdrop: true, data: {filename: '', path: result} as ImportFinancialsData});
			this.dialogSubscription = dialogRef.afterClosed()
			.pipe(switchMap((result: ImportFinancialsData) => this.financialDataService.putImportFinancialsData(result)))				
			.subscribe(result => console.log(result));
		});
		//console.log('showFinancialsConfig()');
	}
	
	logout(): void {
		this.tokenService.logout();
	}
}
