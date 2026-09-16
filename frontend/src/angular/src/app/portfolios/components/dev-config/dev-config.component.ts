import { Component, inject, ChangeDetectionStrategy } from "@angular/core";
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
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CdkScrollable, MatDialogContent, MatLabel, MatButton, AsyncPipe],
})
export class DevConfigComponent {
  classNameObs: Observable<string>;

  private dialogRef = inject(MatDialogRef<OverviewComponent>);
  private devAppInfoService = inject(DevAppInfoService);

  constructor() {
    this.classNameObs = this.devAppInfoService.getClassName();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
