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
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";

export interface SpinnerData {
  title: string;
}

@Component({
    selector: "app-dialog-spinner",
    templateUrl: "./dialog-spinner.component.html",
    styleUrls: ["./dialog-spinner.component.scss"],
    standalone: false
})
export class DialogSpinnerComponent implements OnInit {
  constructor(@Inject(MAT_DIALOG_DATA) public data: SpinnerData) {}

  ngOnInit(): void {
    console.log("title: " + this.data.title);
  }
}
