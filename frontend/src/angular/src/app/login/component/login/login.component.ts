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
import { Component, OnInit, Inject } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MainComponent } from '../main/main.component';
import { LoginService } from '../../service/login.service';
import { TokenService } from '../../../service/token.service';
import { Login } from '../../model/login';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
	signinForm: UntypedFormGroup;
	loginForm: UntypedFormGroup;
	loginFailed = false;
	signinFailed = false;
	pwMatching = true;

	constructor(public dialogRef: MatDialogRef<MainComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private loginService: LoginService,
		private tokenService: TokenService,
		fb: UntypedFormBuilder) {
		this.signinForm = fb.group({
			username: ['', Validators.required],
			password: ['', Validators.required],
			password2: ['', Validators.required],
			email: ['', Validators.required]
		}, {
				validator: this.validate.bind(this)
			});
		this.loginForm = fb.group({
			username: ['', Validators.required],
			password: ['', Validators.required]
		});
	}

	ngOnInit() {
		console.log(this.data);
	}

	validate(group: UntypedFormGroup) {
		if (group.get('password').touched || group.get('password2').touched) {
			this.pwMatching = group.get('password').value === group.get('password2').value && group.get('password').value !== '';
			if (!this.pwMatching) {
				group.get('password').setErrors({ MatchPassword: true });
				group.get('password2').setErrors({ MatchPassword: true });
			} else {
				group.get('password').setErrors(null);
				group.get('password2').setErrors(null);
			}
		}
		return this.pwMatching;
	}

	onSigninClick(): void {
		const login: Login = { emailAddress: null, token: null, password: null, username: null };
		login.username = this.signinForm.get('username').value;
		login.password = this.signinForm.get('password').value;
		login.emailAddress = this.signinForm.get('email').value;
		this.loginService.postSignin(login).subscribe(res => this.signin(res), err => console.log(err));
	}

	onLoginClick(): void {
		const login: Login = {emailAddress: null, token: null, password: null, username: null}; 
		login.username = this.loginForm.get('username').value;
		login.password = this.loginForm.get('password').value;
		this.loginService.postLogin(login).subscribe(res => this.login(res), err => console.log(err));		
	}

	private signin(login: boolean): void {
		this.data.login = null;
		if (login) {
			this.signinFailed = false;
			this.dialogRef.close();
		} else {
			this.signinFailed = true;
		}
	}

	private login(login: Login): void {
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
