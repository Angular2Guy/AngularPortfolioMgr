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
import { Component, OnInit, Inject } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MainComponent } from "../main/main.component";
import { LoginService } from "../../service/login.service";
import { Login } from "../../model/login";
import { TokenService } from "ngx-simple-charts/base-service";

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
})
export class LoginComponent implements OnInit {
  signinForm: FormGroup;
  loginForm: FormGroup;
  loginFailed = false;
  signinFailed = false;
  pwMatching = true;
  FormFields = FormFields;
  protected waitingForResponse = false;

  constructor(
    public dialogRef: MatDialogRef<MainComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private loginService: LoginService,
    private tokenService: TokenService,
    fb: FormBuilder
  ) {
    this.signinForm = fb.group(
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
      }
    );
    this.loginForm = fb.group({
      [FormFields.Username]: ["", Validators.required],
      [FormFields.Password]: ["", Validators.required],
    });
  }

  ngOnInit() {
    console.log(this.data);
  }

  validate(group: FormGroup) {
    if (
      group.get(FormFields.Password).touched ||
      group.get(FormFields.Password2).touched
    ) {
      this.pwMatching =
        group.get(FormFields.Password).value ===
          group.get(FormFields.Password2).value &&
        group.get(FormFields.Password).value !== "";
      if (!this.pwMatching) {
        group.get(FormFields.Password).setErrors({ MatchPassword: true });
        group.get(FormFields.Password2).setErrors({ MatchPassword: true });
      } else {
        group.get(FormFields.Password).setErrors(null);
        group.get(FormFields.Password2).setErrors(null);
      }
    }
    return this.pwMatching;
  }

  onSigninClick(): void {
    const login: Login = {
      emailAddress: null,
      token: null,
      password: null,
      username: null,
    };
    login.username = this.signinForm.get(FormFields.Username).value;
    login.password = this.signinForm.get(FormFields.Password).value;
    login.emailAddress = this.signinForm.get(FormFields.Email).value;
    this.waitingForResponse = true;
    this.loginService.postSignin(login).subscribe({
      next: (res) => this.signin(res),
      error: (err) => console.log(err),
    });
  }

  onLoginClick(): void {
    const login: Login = {
      emailAddress: null,
      token: null,
      password: null,
      username: null,
    };
    login.username = this.loginForm.get(FormFields.Username).value;
    login.password = this.loginForm.get(FormFields.Password).value;
    this.waitingForResponse = true;
    this.loginService.postLogin(login).subscribe({
      next: (res) => this.login(res),
      error: (err) => console.log(err),
    });
  }

  private signin(login: boolean): void {
    this.data.login = null;
    this.waitingForResponse = false;
    if (login) {
      this.signinFailed = false;
      this.dialogRef.close();
    } else {
      this.signinFailed = true;
    }
  }

  private login(login: Login): void {
    this.waitingForResponse = false;
    if (login && login.token && login.id) {
      this.tokenService.token = login.token;
      this.tokenService.userId = login.id;
      this.data.login = login;
      this.loginFailed = false;
      this.dialogRef.close(this.data.login);
    } else {
      this.loginFailed = true;
    }
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }
}
