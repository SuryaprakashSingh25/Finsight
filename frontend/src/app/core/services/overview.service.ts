import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { OverviewStats } from '../models/overview.model';

@Injectable({
  providedIn: 'root'
})
export class OverviewService {
  private readonly http = inject(HttpClient);

  getStats(): Observable<OverviewStats> {
    return this.http.get<OverviewStats>('/api/overview/stats');
  }
}