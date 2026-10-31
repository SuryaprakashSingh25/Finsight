import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TearSheetResponse } from '../models/tearsheet.model';

@Injectable({
  providedIn: 'root'
})
export class TearSheetService {
  private readonly http = inject(HttpClient);

  generateTearSheet(company: string, year?: number | null, ticker?: string): Observable<TearSheetResponse> {
    let params = new HttpParams().set('company', company);
    if (year) params = params.set('year', year.toString());
    if (ticker) params = params.set('ticker', ticker);

    return this.http.get<TearSheetResponse>('/api/tearsheet', { params });
  }
}