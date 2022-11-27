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
import { Component, OnInit } from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';

export interface MyItem {
	id: number;
	title: string;
}

@Component({
  selector: 'app-create-query',
  templateUrl: './create-query.component.html',
  styleUrls: ['./create-query.component.scss']
})
export class CreateQueryComponent implements OnInit {
  private readonly availableInit: MyItem[] = [{id: 1, title: 'Query'}, {id: 2, title: 'And Term'}, {id: 3, title: 'And Not Term'}, 
     {id: 4, title: 'Or Term'}, {id: 5, title: 'Or Not Term'}];
  available: MyItem[] = [];

  query: MyItem[] = [{id: 1, title: 'Query'}];

  ngOnInit(): void {
	this.availableInit.forEach(myItem => this.available.push(myItem));
  }

  drop(event: CdkDragDrop<MyItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      //console.log(event.container.data === this.todo);
      while(this.available.length > 0) {
	     this.available.pop();
      }
      this.availableInit.forEach(myItem => this.available.push(myItem));
    }
  }
}
