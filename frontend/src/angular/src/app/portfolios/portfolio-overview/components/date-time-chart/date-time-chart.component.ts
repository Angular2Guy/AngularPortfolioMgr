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
  AfterViewInit,
  Component,
  ElementRef,
  Inject,
  Input,
  LOCALE_ID,
  OnInit,
  ViewChild,
} from "@angular/core";
import { DateTime, Interval } from "luxon";
import { Item } from "../../model/item";
import { DateTimeChartBase } from "./date-time-chart-base";

@Component({
  selector: "app-date-time-chart",
  templateUrl: "./date-time-chart.component.html",
  styleUrls: ["./date-time-chart.component.scss"],
})
export class DateTimeChartComponent extends DateTimeChartBase implements OnInit, AfterViewInit {  
  protected dayPx = -10;
  protected anchoreIdIndex = 0;
  protected nextAnchorId = "";
  protected timeChartHeight = 0;
  protected readonly CURRENT_TIME = "currentTime";

  @ViewChild("timeChart")
  private timeChartRef: ElementRef;

  constructor(    
    @Inject(LOCALE_ID) locale: string
  ) {
	  super(locale);
  }

  ngAfterViewInit(): void {    
	this.calcTimeChartHeight();
    setTimeout(() => {
		//console.log('afterViewInit');
		let myPeriods = !this.showDays ? this.periodYears : this.periodMonths;
		myPeriods = myPeriods.filter(myPeriod => myPeriod.diffNow().seconds <= 0);
		const myPeriodIndex = myPeriods.length === 0 ? -1 : myPeriods.length-1;
		if(myPeriodIndex >= 0) { 
			this.scrollToAnchorId(!this.showDays ? this.yearHeaderAnchorIds[myPeriodIndex] : this.monthHeaderAnchorIds[myPeriodIndex]);
		}		
	}, 1000);
    //console.log(this.timeChartHeight);
  }

  ngOnInit(): void {
    this.calcChartTime();
  }

  protected calcTimeChartHeight(): void {
	setTimeout(() => {
      this.timeChartHeight = this.timeChartRef.nativeElement.offsetHeight;      
    });
  }

  protected scrollContainer(event: Event): void {
    //console.log((event.target as Element).scrollLeft);
    //console.log((event.target as Element).scrollWidth);
    //console.log((event.target as Element).clientWidth);
    const myScrollWidth = (event.target as Element).scrollWidth;
    const myClientWidth = (event.target as Element).clientWidth;
    const myScrollRight =
      myScrollWidth - myClientWidth - (event.target as Element).scrollLeft;
    let myScrollDayPosition = 0;
    const today = DateTime.now()
      .setLocale(this.locale)
      .setZone(Intl.DateTimeFormat().resolvedOptions().timeZone)
      .toJSDate();
    myScrollDayPosition = myScrollWidth - this.calcStartPx(today);
    const leftDayContainerBoundary =
      myScrollDayPosition + myClientWidth < myScrollWidth
        ? myScrollWidth
        : myScrollDayPosition + myScrollWidth;
    //const rightDayContainerBoundary = myScrollDayPosition - myClientWidth  < 0 ? 0 : myScrollDayPosition - myClientWidth;
    this.dayPx =
      myScrollWidth -
      leftDayContainerBoundary -
      myScrollRight +
      myScrollDayPosition;
    //console.log(leftDayContainerBoundary);
    //console.log(rightDayContainerBoundary);
    //console.log(this.dayPx);
    //console.log(myScrollRight);
    //console.log(myScrollDayPosition);
  }

  protected calcStartPx(start: Date): number {
    const chartStart = DateTime.fromObject({
      year: this.start.getFullYear(),
      month: !this.showDays ? 1 : this.start.getMonth() + 1,
      day: 1,
    });
    const itemInterval = Interval.fromDateTimes(
      chartStart,
      !!start ? DateTime.fromJSDate(start) : chartStart
    );
    const itemPeriods = !this.showDays
      ? itemInterval.length("months")
      : itemInterval.length("days");
    const result =
      itemPeriods * ((!this.showDays ? this.MONTH_WIDTH : this.DAY_WIDTH) + 2);
    return result;
  }

  protected calcEndPx(end: Date): number {
    const chartEnd = DateTime.fromJSDate(this.end);
    const itemInterval = Interval.fromDateTimes(
      DateTime.fromJSDate(end),
      chartEnd
    );
    const itemPeriods = !this.showDays
      ? itemInterval.length("months")
      : itemInterval.length("days");
    const result =
      itemPeriods * ((!this.showDays ? this.MONTH_WIDTH : this.DAY_WIDTH) + 2);
    return result;
  }

  protected calcStartPxItem(item: Item<Event>): number {
    return this.calcStartPx(item.start);
  }

  protected calcEndPxItem(item: Item<Event>): number {
    if (!item?.end) {
      return 0;
    }
    return this.calcEndPx(item.end);
  }

  protected calcWidthPxItem(item: Item<Event>): number {
    const chartStart = DateTime.fromObject({
      year: this.start.getFullYear(),
      month: !this.showDays ? 1 : this.start.getMonth() + 1,
      day: 1,
    });
    const chartEnd = DateTime.fromJSDate(this.end);
    const itemInterval = Interval.fromDateTimes(chartStart, chartEnd);
    const itemPeriods = !this.showDays
      ? itemInterval.length("months")
      : Math.ceil(itemInterval.length("days")); //Math.ceil() for full days
    //console.log(itemDays * (this.DAY_WIDTH + 2));
    //console.log(itemDays);
    const result =
      itemPeriods * ((!this.showDays ? this.MONTH_WIDTH : this.DAY_WIDTH) + 2) -
      2 -
      (this.calcStartPxItem(item) + this.calcEndPxItem(item));
    return result;
  }

  protected scrollToTime(timeDiff: number): void {
    const anchorIds = !this.showDays
      ? this.yearHeaderAnchorIds
      : this.monthHeaderAnchorIds;
    this.anchoreIdIndex =
      this.anchoreIdIndex + timeDiff < 0
        ? 0
        : this.anchoreIdIndex + timeDiff >= anchorIds.length
        ? anchorIds.length - 1
        : this.anchoreIdIndex + timeDiff;
    this.scrollToAnchorId(anchorIds[this.anchoreIdIndex]);
  }

  protected scrollToAnchorId(anchorId: string): void {
    const element = document.getElementById(anchorId);
    element.scrollIntoView({
      block: "start",
      behavior: "smooth",
      inline: "nearest",
    });
  }

  get items(): Item<Event>[] {
    return this.localItems;
  }

  @Input({ required: true })
  set items(items: Item<Event>[]) {		
    this.localItems = items;
    this.calcChartTime();
    this.calcTimeChartHeight();
  }

  get start(): Date {
    return this.localStart;
  }

  @Input()
  set start(start: Date) {
    this.localStart = DateTime.fromJSDate(start)
      .setLocale(this.locale)
      .setZone(Intl.DateTimeFormat().resolvedOptions().timeZone)
      .toJSDate();
    this.calcChartTime();
    this.calcTimeChartHeight();
  }

  get showDays(): boolean {
    return this.localShowDays;
  }

  @Input({ required: true })
  set showDays(showDays: boolean) {
    this.localShowDays = showDays;
    this.calcChartTime();
    this.calcTimeChartHeight();
  }
}
