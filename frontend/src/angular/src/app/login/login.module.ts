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
import { NgModule } from '@angular/core';
import { LoginRoutingModule } from './login-routing.module';
import { LoginComponent } from './component/login/login.component';
import { BaseModule } from '../base/base.module';
import { MainComponent } from './component/main/main.component';
import { LoginService } from './service/login.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialogModule } from '@angular/material/dialog'; 
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';

@NgModule({
    declarations: [LoginComponent, MainComponent],
    imports: [
        BaseModule,
        MatToolbarModule,
        MatFormFieldModule,
        MatTabsModule,
        MatDialogModule,
        MatButtonModule,
        MatInputModule,
        LoginRoutingModule,
    ],
    providers: [LoginService]
})
export class LoginModule { }
