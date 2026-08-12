import { OnInit, ChangeDetectionStrategy } from "@angular/core";
import { Component } from "@angular/core";
import { MatDialogRef, MatDialogContent } from "@angular/material/dialog";
import { Observable } from "rxjs";
import { ProdAppInfoService } from "../../service/prod-app-info.service";
import { OverviewComponent } from "../overview/overview.component";
import { CdkScrollable } from "@angular/cdk/scrolling";
import { MatLabel } from "@angular/material/form-field";
import { MatButton } from "@angular/material/button";
import { AsyncPipe } from "@angular/common";

@Component({
  selector: "app-prod-config",
  templateUrl: "./prod-config.component.html",
  styleUrls: ["./prod-config.component.scss"],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CdkScrollable, MatDialogContent, MatLabel, MatButton, AsyncPipe],
})
export class ProdConfigComponent implements OnInit {
  classNameObs: Observable<string>;

  constructor(
    private dialogRef: MatDialogRef<OverviewComponent>,
    private prodAppInfoService: ProdAppInfoService,
  ) {}

  ngOnInit(): void {
    this.classNameObs = this.prodAppInfoService.getClassName();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
