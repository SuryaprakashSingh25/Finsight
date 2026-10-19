import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DocumentUploadResponse } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly http = inject(HttpClient);

  uploadDocument(
    file: File,
    company: string,
    documentTitle: string,
    year: number | null
  ): Observable<DocumentUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('company', company);
    formData.append('documentTitle', documentTitle);

    if (year !== null) {
      formData.append('year', year.toString());
    }

    return this.http.post<DocumentUploadResponse>('/api/documents', formData);
  }
}