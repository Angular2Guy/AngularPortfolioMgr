import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NewsItem } from '../model/news-item';

@Injectable()
export class NewsService {

  constructor(private httpClient: HttpClient) { }
  
  getYahooNews(): Observable<NewsItem[]> {
	return this.httpClient.get<NewsItem[]>('/rest/newsfeed/yahoo-finance');
  }
  
  getCnnFinanceNews(): Observable<NewsItem[]> {
	return this.httpClient.get<NewsItem[]>('/rest/newsfeed/cnn-finance');
  }
}
