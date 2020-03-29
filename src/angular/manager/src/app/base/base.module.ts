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
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTreeModule } from '@angular/material/tree';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRippleModule } from '@angular/material/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { HttpClientModule }    from '@angular/common/http';

@NgModule({
	declarations: [],
	imports: [
		CommonModule,
		HttpClientModule,
		ReactiveFormsModule,
		MatAutocompleteModule,
		MatCheckboxModule,
		MatDatepickerModule,
		MatFormFieldModule,
		MatInputModule,
		MatRadioModule,
		MatSelectModule,
		MatSliderModule,
		MatSlideToggleModule,
		MatMenuModule,
		MatSidenavModule,
		MatToolbarModule,
		MatCardModule,
		MatDividerModule,
		MatExpansionModule,
		MatGridListModule,
		MatListModule,
		MatStepperModule,
		MatTabsModule,
		MatTreeModule,
		MatButtonModule,
		MatButtonToggleModule,
		MatBadgeModule,
		MatChipsModule,
		MatProgressSpinnerModule,
		MatProgressBarModule,
		MatRippleModule,
		MatBottomSheetModule,
		MatDialogModule,
		MatSnackBarModule,
		MatTooltipModule,
		MatPaginatorModule,
		MatSortModule,
		MatTableModule,
		MatIconModule
	],
	exports: [		
		CommonModule,
		HttpClientModule,
		ReactiveFormsModule,
		MatAutocompleteModule,
		MatCheckboxModule,
		MatDatepickerModule,
		MatFormFieldModule,
		MatInputModule,
		MatRadioModule,
		MatSelectModule,
		MatSliderModule,
		MatSlideToggleModule,
		MatMenuModule,
		MatSidenavModule,
		MatToolbarModule,
		MatCardModule,
		MatDividerModule,
		MatExpansionModule,
		MatGridListModule,
		MatListModule,
		MatStepperModule,
		MatTabsModule,
		MatTreeModule,
		MatButtonModule,
		MatButtonToggleModule,
		MatBadgeModule,
		MatChipsModule,
		MatProgressSpinnerModule,
		MatProgressBarModule,
		MatRippleModule,
		MatBottomSheetModule,
		MatDialogModule,
		MatSnackBarModule,
		MatTooltipModule,
		MatPaginatorModule,
		MatSortModule,
		MatTableModule,
		MatIconModule
		]
})
export class BaseModule { }
