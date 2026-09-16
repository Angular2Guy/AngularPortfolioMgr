import { Component, inject, ChangeDetectionStrategy } from "@angular/core";
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
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CdkScrollable, MatDialogContent, MatLabel, MatButton, AsyncPipe],
})
export class ProdConfigComponent {
  classNameObs: Observable<string>;

  private dialogRef = inject(MatDialogRef<OverviewComponent>);
  private prodAppInfoService = inject(ProdAppInfoService);

  constructor() {
    this.classNameObs = this.prodAppInfoService.getClassName();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
