import { Component, OnInit } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";
import { Observable } from "rxjs";
import { DevAppInfoService } from "../../service/dev-app-info.service";
import { OverviewComponent } from "../overview/overview.component";

@Component({
  selector: "app-dev-config",
  templateUrl: "./dev-config.component.html",
  styleUrls: ["./dev-config.component.scss"],
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
