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
import { DateTime } from "luxon";
import { Item } from "../../model/item";
import { CalendarService } from "../../service/calendar.service";

export class DateTimeChartBase {
  protected localStart: Date = new Date();
  protected localShowDays: boolean;
  protected end: Date;
  protected localItems: Item<Event>[] = [];
  protected periodDays: DateTime[] = [];
  protected periodMonths: DateTime[] = [];
  protected periodYears: DateTime[] = [];
  protected monthHeaderAnchorIds: string[] = [];
  protected yearHeaderAnchorIds: string[] = [];
  protected readonly DAY_WIDTH = CalendarService.DAY_WIDTH;
  protected readonly MONTH_WIDTH = CalendarService.MONTH_WIDTH;
  
  constructor(protected locale: string) {}	 
  
  protected calcChartTime(): void {
    const myEndOfYear = new Date(new Date().getFullYear(), 11, 31, 23, 59, 59);
    const endOfYear = DateTime.fromJSDate(myEndOfYear)
      .setLocale(this.locale)
      .setZone(Intl.DateTimeFormat().resolvedOptions().timeZone)
      .toJSDate();
    let myItem = new Item<Event>();
    myItem.end = DateTime.fromJSDate(new Date(0, 11, 31))
      .setLocale(this.locale)
      .setZone(Intl.DateTimeFormat().resolvedOptions().timeZone)
      .toJSDate();
    const lastEndItem = this.localItems.reduce(
      (acc, newItem) =>
        acc?.end?.getMilliseconds() < newItem?.end?.getMilliseconds()
          ? newItem
          : acc,
      myItem
    );
    const openEndItems = this.localItems.filter((newItem) => !newItem?.end);
    this.end =
      openEndItems.length > 0 || !this.localShowDays
        ? endOfYear
        : lastEndItem.end.getFullYear() < 1
        ? endOfYear
        : lastEndItem.end;
    this.periodDays = [];
    for (
      let myDay = DateTime.fromObject({
        year: this.localStart.getFullYear(),
        month: this.localStart.getMonth() + 1,
        day: 1,
      });
      myDay.toMillis() <= DateTime.fromJSDate(this.end).toMillis();
      myDay = myDay.plus({ days: 1 })
    ) {
      this.periodDays.push(myDay);
    }
    this.periodMonths = [];
    this.monthHeaderAnchorIds = [];
    for (
      let myMonth = DateTime.fromObject({
        year: this.localStart.getFullYear(),
        month: !!this.localShowDays ? this.localStart.getMonth() + 1 : 1,
        day: 1,
      });
      myMonth.toMillis() <= DateTime.fromJSDate(this.end).toMillis();
      myMonth = myMonth.plus({ months: 1 })
    ) {
      this.periodMonths.push(myMonth);
      this.monthHeaderAnchorIds.push(
        "M_" + this.generateHeaderAnchorId(myMonth)
      );
    }
    this.periodYears = [];
    this.yearHeaderAnchorIds = [];
    for (
      let myYear = DateTime.fromObject({
        year: this.localStart.getFullYear(),
        month: 1,
        day: 1,
      });
      myYear.toMillis() <= DateTime.fromJSDate(this.end).toMillis();
      myYear = myYear.plus({ years: 1 })
    ) {
      this.periodYears.push(myYear);
      this.yearHeaderAnchorIds.push("Y_" + this.generateHeaderAnchorId(myYear));
    }
    //console.log('onInit');
  }
  
  protected generateHeaderAnchorId(dateTime: DateTime): string {
    const headerAnchorId =
      "" +
      dateTime.year +
      "_" +
      dateTime.month +
      "_" +
      new Date().getMilliseconds().toString(16);
    return headerAnchorId;
  }   
}