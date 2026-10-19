import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { DocumentService } from '../../core/services/document.service';
import { DocumentUploadResponse } from '../../core/models/document.model';

@Component({
  selector: 'app-documents',
  standalone: true,
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.scss'
})
export class DocumentsComponent {
  private readonly documentService = inject(DocumentService);
  private readonly cdr = inject(ChangeDetectorRef);

  selectedFile: File | null = null;
  company = '';
  documentTitle = '';
  year = '';
  uploading = false;
  uploadError = '';
  uploadResult: DocumentUploadResponse | null = null;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.uploadError = '';
    this.uploadResult = null;

    if (file && !file.name.toLowerCase().endsWith('.pdf')) {
      this.selectedFile = null;
      this.uploadError = 'Please select a PDF file.';
      input.value = '';
      return;
    }
    this.selectedFile = file;
  }

  uploadDocument(): void {
    if (!this.selectedFile) {
      this.uploadError = 'Please select a PDF file.';
      return;
    }

    if (!this.company.trim() || !this.documentTitle.trim()) {
      this.uploadError = 'Company and document title are required.';
      return;
    }

    const parsedYear = this.year.trim() ? Number(this.year) : null;

    if (
      parsedYear !== null &&
      (!Number.isInteger(parsedYear) || parsedYear < 1900 || parsedYear > 2100)
    ) {
      this.uploadError = 'Enter a valid year between 1900 and 2100.';
      return;
    }

    this.uploading = true;
    this.uploadError = '';
    this.uploadResult = null;
    this.cdr.markForCheck();

    this.documentService
      .uploadDocument(
        this.selectedFile,
        this.company.trim(),
        this.documentTitle.trim(),
        parsedYear
      )
      .pipe(
        finalize(() => {
          this.uploading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (result) => {
          this.uploadResult = result;
          this.selectedFile = null;
          this.cdr.markForCheck();
        },
        error: (error: HttpErrorResponse) => {
          console.error('Upload failed:', error);
          this.uploadError =
            error.error?.error ??
            'Upload failed. Check the backend logs and browser console.';
          this.cdr.markForCheck();
        }
      });
  }
}