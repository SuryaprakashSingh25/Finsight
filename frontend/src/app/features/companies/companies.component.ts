import { Component, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { CompanyService } from '../../core/services/company.service';
import { TearSheetService } from '../../core/services/tearsheet.service';
import { CompanySummary } from '../../core/models/company.model';
import { MarkdownPipe } from '../../core/pipes/markdown.pipe';

@Component({
  selector: 'app-companies',
  standalone: true,
  imports: [MarkdownPipe, DatePipe],
  templateUrl: './companies.component.html',
  styleUrl: './companies.component.scss'
})
export class CompaniesComponent implements OnInit {
  private readonly companyService = inject(CompanyService);
  private readonly tearSheetService = inject(TearSheetService);

  @Output() navigate = new EventEmitter<string>();

  loading = signal(true);
  companies = signal<CompanySummary[]>([]);
  errorMessage = signal('');

  activeTearSheet = signal<string | null>(null);
  activeCompany = signal<string>('');
  activeYear = signal<number | null>(null);
  isGeneratingTearSheet = signal(false);
  today = new Date();

  deletingCompany = signal<string | null>(null);


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

  generateTearSheet(company: string, year?: number | null): void {
    this.activeCompany.set(company);
    this.activeYear.set(year ?? null);
    this.isGeneratingTearSheet.set(true);
    this.activeTearSheet.set(null);

    this.tearSheetService.generateTearSheet(company, year).subscribe({
      next: (res) => {
        this.activeTearSheet.set(res.tearSheetMarkdown);
        this.isGeneratingTearSheet.set(false);
      },
      error: (err) => {
        console.error('Tear sheet generation failed:', err);
        this.activeTearSheet.set('Failed to synthesize Executive Tear Sheet. Verify that the MCP server and Ollama are reachable.');
        this.isGeneratingTearSheet.set(false);
      }
    });
  }

  deleteCompany(company: string, event: MouseEvent): void {
    event.stopPropagation();

    const confirmed = window.confirm(
      `Are you sure you want to delete all indexed reports and vector chunks for "${company}"?`
    );
    if (!confirmed) return;

    this.deletingCompany.set(company);

    this.companyService.deleteCompany(company).subscribe({
      next: () => {
        // Remove locally from state
        this.companies.update((list) => list.filter((c) => c.company !== company));
        this.deletingCompany.set(null);
      },
      error: (err) => {
        console.error('Failed to delete company:', err);
        alert(`Failed to delete "${company}". Check backend logs.`);
        this.deletingCompany.set(null);
      }
    });
  }

  closeTearSheet(): void {
    this.activeTearSheet.set(null);
    this.isGeneratingTearSheet.set(false);
  }

  printTearSheet(): void {
    window.print();
  }
}