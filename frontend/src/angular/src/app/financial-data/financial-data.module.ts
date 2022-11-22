import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FinancialDataRoutingModule } from './financial-data-routing.module';
import { OverviewComponent } from './components/overview/overview.component';


@NgModule({
  declarations: [
    OverviewComponent
  ],
  imports: [
    CommonModule,
    FinancialDataRoutingModule
  ]
})
export class FinancialDataModule { }
