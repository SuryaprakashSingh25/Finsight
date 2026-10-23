import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ComparisonRequest, ComparisonResponse } from '../models/comparison.model';

@Injectable({
  providedIn: 'root'
})
export class ComparisonService {
  private readonly http = inject(HttpClient);

  runComparison(req: ComparisonRequest): Observable<ComparisonResponse> {
    return this.http.post<ComparisonResponse>('/api/comparison', req);
  }
}