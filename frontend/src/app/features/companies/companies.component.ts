import { Component, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { CompanyService } from '../../core/services/company.service';
import { CompanySummary } from '../../core/models/company.model';

@Component({
  selector: 'app-companies',
  standalone: true,
  templateUrl: './companies.component.html',
  styleUrl: './companies.component.scss'
})
export class CompaniesComponent implements OnInit {
  private readonly companyService = inject(CompanyService);

  @Output() navigate = new EventEmitter<string>();

  loading = signal(true);
  companies = signal<CompanySummary[]>([]);
  errorMessage = signal('');

  ngOnInit(): void {
    this.fetchCompanies();
  }

  fetchCompanies(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.companyService.getCompanies().subscribe({
      next: (data) => {
        this.companies.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load companies:', err);
        this.errorMessage.set('Could not load tracked companies. Ensure backend is running.');
        this.loading.set(false);
      }
    });
  }
}