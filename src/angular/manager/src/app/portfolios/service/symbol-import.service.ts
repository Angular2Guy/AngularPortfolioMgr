import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class SymbolImportService {

  constructor(private http: HttpClient) { }

  getSymbolImportUs(): Observable<number> {
	return this.http.get<number>('/rest/symbol/importus/all');
  }

  getSymbolImportHk(): Observable<number> {
	return this.http.get<number>('/rest/symbol/importhk/all');
  }

  getSymbolImportDe(): Observable<number> {
	return this.http.get<number>('/rest/symbol/importde/all');
  }
}
