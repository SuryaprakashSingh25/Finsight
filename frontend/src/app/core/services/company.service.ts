import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CompanySummary } from '../models/company.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly http = inject(HttpClient);

  getCompanies(): Observable<CompanySummary[]> {
    return this.http.get<CompanySummary[]>('/api/companies');
  }

  deleteCompany(company: string): Observable<void> {
    return this.http.delete<void>(`/api/companies/${encodeURIComponent(company)}`);
  }
}