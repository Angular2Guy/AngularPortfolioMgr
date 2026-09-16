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
  OnInit,
  Inject,
  inject,
  DestroyRef,
  signal,
  ChangeDetectionStrategy,
} from "@angular/core";
import {
  FormGroup,
  FormBuilder,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogContent,
} from "@angular/material/dialog";
import { MainComponent } from "../main/main.component";
import { LoginService } from "../../service/login.service";
import { Login } from "../../model/login";
import { TokenService } from "ngx-simple-charts/base-service";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { CdkScrollable } from "@angular/cdk/scrolling";
import { MatTabGroup, MatTab } from "@angular/material/tabs";
import { NgTemplateOutlet } from "@angular/common";
import { MatFormField } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatButton } from "@angular/material/button";
import { MatProgressSpinner } from "@angular/material/progress-spinner";

enum FormFields {
  Username = "username",
  Password = "password",
  Password2 = "password2",
  Email = "email",
  RapidApiKey = "rapidApiKey",
  AlphavantageKey = "alphavantageKey",
}

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CdkScrollable,
    MatDialogContent,
    MatTabGroup,
    MatTab,
    NgTemplateOutlet,
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatButton,
    MatProgressSpinner,
  ],
})
export class LoginComponent implements OnInit {
  signinForm: FormGroup;
  loginForm: FormGroup;
  loginFailed = signal(false);
  signinFailed = signal(false);
  pwMatching = signal(true);
  FormFields = FormFields;
  protected waitingForResponse = signal(false);

  private loginService = inject(LoginService);
  private tokenService = inject(TokenService);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);

  constructor(
    public dialogRef: MatDialogRef<MainComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.signinForm = this.fb.group(
      {
        [FormFields.Username]: ["", Validators.required],
        [FormFields.Password]: ["", Validators.required],
        [FormFields.Password2]: ["", Validators.required],
        [FormFields.Email]: ["", Validators.required],
        [FormFields.RapidApiKey]: ["", Validators.required],
        [FormFields.AlphavantageKey]: ["", Validators.required],
      },
      {
        validator: this.validate.bind(this),
      },
    );
    this.loginForm = this.fb.group({
      [FormFields.Username]: ["", Validators.required],
      [FormFields.Password]: ["", Validators.required],
    });
  }

  ngOnInit() {
    console.log(this.data);
  }

  validate(group: FormGroup) {
    if (
      group.get(FormFields.Password)?.touched ||
      group.get(FormFields.Password2)?.touched
    ) {
      this.pwMatching.set(
        group.get(FormFields.Password)?.value ===
          group.get(FormFields.Password2)?.value &&
          group.get(FormFields.Password)?.value !== "",
      );
      if (!this.pwMatching()) {
        group.get(FormFields.Password)?.setErrors({ MatchPassword: true });
        group.get(FormFields.Password2)?.setErrors({ MatchPassword: true });
      } else {
        group.get(FormFields.Password)?.setErrors(null);
        group.get(FormFields.Password2)?.setErrors(null);
      }
    }
    return this.pwMatching();
  }

  onSigninClick(): void {
    const login: Login = {
      emailAddress: "",
      token: "",
      password: "",
      username: "",
      alphavantageKey: "",
      rapidApiKey: "",
    };
    login.username = this.signinForm.get(FormFields.Username)?.value;
    login.password = this.signinForm.get(FormFields.Password)?.value;
    login.emailAddress = this.signinForm.get(FormFields.Email)?.value;
    login.alphavantageKey = this.signinForm.get(
      FormFields.AlphavantageKey,
    )?.value;
    login.rapidApiKey = this.signinForm.get(FormFields.RapidApiKey)?.value;
    this.waitingForResponse.set(true);
    this.loginService
      .postSignin(login)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res: boolean) => this.signin(res),
        error: (err: Error) => console.log(err),
      });
  }

  onLoginClick(): void {
    const login: Login = {
      emailAddress: "",
      token: "",
      password: "",
      username: "",
    };
    login.username = this.loginForm.get(FormFields.Username)?.value;
    login.password = this.loginForm.get(FormFields.Password)?.value;
    this.waitingForResponse.set(true);
    this.loginService
      .postLogin(login)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res: Login) => this.login(res),
        error: (err: Error) => console.log(err),
      });
  }

  private signin(login: boolean): void {
    this.data.login = null;
    this.waitingForResponse.set(false);
    if (login) {
      this.signinFailed.set(false);
      this.dialogRef.close();
    } else {
      this.signinFailed.set(true);
    }
  }

  private login(login: Login): void {
    this.waitingForResponse.set(false);
    if (login && login.token && login.id) {
      this.tokenService.token = login.token;
      this.tokenService.userId = login.id;
      this.data.login = login;
      this.loginFailed.set(false);
      this.dialogRef.close(this.data.login);
    } else {
      this.loginFailed.set(true);
    }
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }
}
