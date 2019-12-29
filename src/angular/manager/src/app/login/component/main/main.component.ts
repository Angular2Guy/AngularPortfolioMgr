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
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Login } from '../../model/login';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import { LoginService } from '../../service/login.service';
import { TokenService } from '../../../service/token.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {	
  login: Login = null;

  constructor(private dialog: MatDialog, 
		private loginService: LoginService, 
		private tokenService: TokenService,
		private router: Router) { }

  ngOnInit() {
  }

  openLoginDialog():void {
	const dialogRef = this.dialog.open(LoginComponent, { width: '500px', data: {login: this.login}});
	dialogRef.afterClosed().subscribe( result => {
		this.login = typeof result == 'undefined' ? null : result;	
		this.router.navigate(['/portfolios/overview']);
	});
  }

  logout():void {
	this.tokenService.token = null;
  }
}
