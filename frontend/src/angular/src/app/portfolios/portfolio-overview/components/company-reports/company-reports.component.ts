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
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from "@angular/core";
import { CompanyReportService } from "../../service/company-report.service";
import { CompanyReport } from "../../model/company-report";
import {
  MatCard,
  MatCardHeader,
  MatCardTitleGroup,
  MatCardTitle,
  MatCardSubtitle,
  MatCardContent,
} from "@angular/material/card";
import { DatePipe } from "@angular/common";

@Component({
  selector: "app-company-reports",
  templateUrl: "./company-reports.component.html",
  styleUrl: "./company-reports.component.scss",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    MatCard,
    MatCardHeader,
    MatCardTitleGroup,
    MatCardTitle,
    MatCardSubtitle,
    MatCardContent,
    DatePipe,
  ],
})
export class CompanyReportsComponent implements OnInit {
  private companyReportService = inject(CompanyReportService);
  protected companyReports = signal<CompanyReport[]>([]);

  ngOnInit(): void {
    this.companyReportService
      .getCompanyReports()
      .subscribe((reports: CompanyReport[]) => {
        this.companyReports.set(reports);
      });
  }
}
