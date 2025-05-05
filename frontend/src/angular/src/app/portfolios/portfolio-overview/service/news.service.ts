import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { NewsItem } from "../model/news-item";

@Injectable()
export class NewsService {
  constructor(private httpClient: HttpClient) {}

  getSeekingAlphaNews(): Observable<NewsItem[]> {
    return this.httpClient.get<NewsItem[]>("/rest/newsfeed/seeking-alpha");
  }

  getCnbcFinanceNews(): Observable<NewsItem[]> {
    return this.httpClient.get<NewsItem[]>("/rest/newsfeed/cnbc-finance");
  }
}
