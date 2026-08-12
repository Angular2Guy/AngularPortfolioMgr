import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { MatDialogRef, MatDialogContent } from "@angular/material/dialog";
import { Observable } from "rxjs";
import { DevAppInfoService } from "../../service/dev-app-info.service";
import { OverviewComponent } from "../overview/overview.component";
import { CdkScrollable } from "@angular/cdk/scrolling";
import { MatLabel } from "@angular/material/form-field";
import { MatButton } from "@angular/material/button";
import { AsyncPipe } from "@angular/common";

@Component({
  selector: "app-dev-config",
  templateUrl: "./dev-config.component.html",
  styleUrls: ["./dev-config.component.scss"],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CdkScrollable, MatDialogContent, MatLabel, MatButton, AsyncPipe],
})
export class DevConfigComponent implements OnInit {
  classNameObs: Observable<string>;

  constructor(
    private dialogRef: MatDialogRef<OverviewComponent>,
    private devAppInfoService: DevAppInfoService,
  ) {}

  ngOnInit(): void {
    this.classNameObs = this.devAppInfoService.getClassName();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
