import { OnInit } from "@angular/core";
import { Component } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";
import { Observable } from "rxjs";
import { ProdAppInfoService } from "../../service/prod-app-info.service";
import { OverviewComponent } from "../overview/overview.component";

@Component({
    selector: "app-prod-config",
    templateUrl: "./prod-config.component.html",
    styleUrls: ["./prod-config.component.scss"],
    standalone: false
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
