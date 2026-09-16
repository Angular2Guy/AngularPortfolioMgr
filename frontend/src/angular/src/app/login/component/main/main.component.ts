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
  Component,
  inject,
  DestroyRef,
  signal,
  ChangeDetectionStrategy,
} from "@angular/core";
import { Login } from "../../model/login";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { LoginComponent } from "../login/login.component";
import { TokenService } from "ngx-simple-charts/base-service";
import { Router } from "@angular/router";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatToolbar } from "@angular/material/toolbar";
import { MatButton } from "@angular/material/button";
import { MatProgressSpinner } from "@angular/material/progress-spinner";

@Component({
  selector: "app-main",
  templateUrl: "./main.component.html",
  styleUrls: ["./main.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatToolbar, MatButton, MatProgressSpinner],
})
export class MainComponent {
  login = signal<Login | null>(null);

  private dialog = inject(MatDialog);
  private tokenService = inject(TokenService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  openLoginDialog(): MatDialogRef<LoginComponent, Login> {
    const dialogRef = this.dialog.open(LoginComponent, {
      width: "600px",
      hasBackdrop: true,
      disableClose: true,
      data: { login: this.login() },
    });
    dialogRef
      .beforeClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: Login) => {
        this.login.set(typeof result == "undefined" ? null : result);
        if (this.login()) {
          this.router.navigate(["/portfolios/overview"]);
        }
      });
    return dialogRef;
  }

  logout(): void {
    this.tokenService.logout();
  }
}
