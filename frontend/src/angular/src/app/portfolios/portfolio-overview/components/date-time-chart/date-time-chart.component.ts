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
import { Component, Input, OnInit } from '@angular/core';
import { DateTime, Duration, Interval } from 'luxon';
import { Item } from '../../model/item';
import { CalendarService } from '../../service/calendar.service';

@Component({
  selector: 'app-date-time-chart',
  templateUrl: './date-time-chart.component.html',
  styleUrls: ['./date-time-chart.component.scss']
})
export class DateTimeChartComponent implements OnInit {
	@Input({required: true})	
	protected items: Item<Event>[];
	@Input({required: true})
	protected start: Date;
	protected end: Date;
	@Input({required: true})
	protected showDays: boolean;
	protected periodDays: DateTime[] = [];
	protected periodMonths: DateTime[] = [];
	protected periodYears: DateTime[] = [];
	protected readonly DAY_WIDTH = CalendarService.DAY_WIDTH;
	protected readonly MONTH_WIDTH = CalendarService.MONTH_WIDTH;
	
	constructor(protected calendarService: CalendarService) {}
	
    ngOnInit(): void {
		const endOfYear = new Date(new Date().getFullYear(),11,31,23,59,59);
		let myItem = new Item<Event>();
		myItem.end = new Date(0, 11,31);
        const lastEndItem = this.items.reduce((acc, newItem) => acc.end.getMilliseconds() < newItem?.end.getMilliseconds() ? newItem: acc, myItem);
        const openEndItems =  this.items.filter(newItem => !newItem?.end);
        this.end = openEndItems.length > 0 || !this.showDays ? endOfYear : lastEndItem.end.getFullYear() < 1 ? endOfYear : lastEndItem.end;               
        for(let myDay = DateTime.fromObject({year: this.start.getFullYear(), month: this.start.getMonth()+1, day: 1});
        	myDay.toMillis() <= DateTime.fromJSDate(this.end).toMillis();
        	myDay = myDay.plus(Duration.fromObject({days: 1}))) {
			this.periodDays.push(myDay);			
		}				
		for(let myMonth = DateTime.fromObject({year: this.start.getFullYear(), month: !!this.showDays ? this.start.getMonth()+1 : 1, day: 1}); 
			myMonth.toMillis() <= DateTime.fromJSDate(this.end).toMillis(); 
			myMonth = myMonth.plus(Duration.fromObject({months: 1}))) {
			this.periodMonths.push(myMonth);			
		}		
		for(let myYear = DateTime.fromObject({year: this.start.getFullYear(), month: 1, day: 1}); 
			myYear.toMillis() <= DateTime.fromJSDate(this.end).toMillis();			 
			myYear = myYear.plus(Duration.fromObject({years: 1}))) {				
			this.periodYears.push(myYear);
		}
    }
    
    protected calcStartPx(item: Item<Event>): number {
		const chartStart = DateTime.fromObject({year: this.start.getFullYear(), month: this.start.getMonth()+1, day: 1});
		const itemInterval = Interval.fromDateTimes(chartStart, !!item.start ? DateTime.fromJSDate(item.start) : chartStart);
		const itemDays = itemInterval.length('days');
		return itemDays * (this.DAY_WIDTH + 2);
	}
	
	protected calcEndPx(item: Item<Event>): number {
		if(!item?.end) {
			return 0;
		}
		const chartEnd = DateTime.fromJSDate(this.end);
		const itemInterval = Interval.fromDateTimes(!!item.end ? DateTime.fromJSDate(item.end) : chartEnd, chartEnd);
		const itemDays = itemInterval.length('days');
		return itemDays * (this.DAY_WIDTH + 2);
	}	
	
	protected calcWidthPx(item: Item<Event>): number {
		const chartStart = DateTime.fromObject({year: this.start.getFullYear(), month: this.start.getMonth()+1, day: 1});		
		const chartEnd = DateTime.fromJSDate(this.end);		
		const itemInterval = Interval.fromDateTimes(chartStart, chartEnd);
		const itemDays = Math.ceil(itemInterval.length('days')); //Math.ceil() for full days 
		//console.log(itemDays * (this.DAY_WIDTH + 2));
		//console.log(itemDays);				
		return (itemDays * (this.DAY_WIDTH + 2) -2) - (this.calcStartPx(item) + this.calcEndPx(item));
	}
}
