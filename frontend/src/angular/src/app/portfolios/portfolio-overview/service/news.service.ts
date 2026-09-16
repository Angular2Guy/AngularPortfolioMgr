import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { NewsItem } from "../model/news-item";

@Injectable({ providedIn: "root" })
export class NewsService {
  private httpClient = inject(HttpClient);

  getSeekingAlphaNews(): Observable<NewsItem[]> {
    return this.httpClient.get<NewsItem[]>("/rest/newsfeed/seeking-alpha");
  }

  getCnbcFinanceNews(): Observable<NewsItem[]> {
    return this.httpClient.get<NewsItem[]>("/rest/newsfeed/cnbc-finance");
  }
}
