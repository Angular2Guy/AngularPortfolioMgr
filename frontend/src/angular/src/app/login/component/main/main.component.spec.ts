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
import { ComponentFixture, TestBed, waitForAsync } from "@angular/core/testing";
import { FormBuilder } from "@angular/forms";
import {
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatToolbarModule } from "@angular/material/toolbar";
import { ActivatedRoute, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TokenService } from "ngx-simple-charts/base-service";
import { timeout } from "rxjs";
import { Login } from "../../model/login";
import { LoginService } from "../../service/login.service";

import { MainComponent } from "./main.component";
import { BaseModule } from "src/app/base/base.module";
import { LoginComponent } from "../login/login.component";
import { MatInputModule } from "@angular/material/input";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { MatTabsModule } from "@angular/material/tabs";
import { MatButtonModule } from "@angular/material/button";

describe("MainComponent", () => {
  let component: MainComponent;
  let fixture: ComponentFixture<MainComponent>;
  let tokenService: TokenService;
  let loginService: LoginService;
  let matDialog: MatDialog;
  let router: ActivatedRoute;

  beforeEach(waitForAsync(() => {
    tokenService = jasmine.createSpyObj("tokenService", ["logout"]);
    TestBed.configureTestingModule({
      imports: [
        BaseModule,
        MatDialogModule,
        MatToolbarModule,
        MatInputModule,
        NoopAnimationsModule,
        MatTabsModule,
        MatButtonModule,
        RouterTestingModule.withRoutes([
          { path: "portfolios/overview", redirectTo: "/" },
        ]),
      ],
      declarations: [MainComponent, LoginComponent],
      providers: [
        FormBuilder,
        { provide: TokenService, useValue: tokenService },
        { provide: LoginService, useValue: loginService },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MainComponent);
    component = fixture.componentInstance;
    matDialog = TestBed.inject(MatDialog);
    router = TestBed.inject(ActivatedRoute);
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should logout", () => {
    component.logout();
    expect(tokenService.logout).toHaveBeenCalled();
  });

  it("should open login", waitForAsync(() => {
    spyOn(matDialog, "open").and.callThrough();
    component.openLoginDialog();
    expect(matDialog.open).toHaveBeenCalled();
  }));
  it("should close login", waitForAsync(() => {
    const matDialogRef = component.openLoginDialog();
    const testLogin = {
      emailAddress: "e",
      password: "p",
      username: "u",
      token: "t",
    } as Login;
    matDialogRef.close(testLogin);
    matDialog.afterAllClosed.subscribe(() =>
      expect(component.login).toEqual(testLogin),
    );
  }));
});
