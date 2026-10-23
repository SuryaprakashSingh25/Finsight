import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CompanyService } from '../../core/services/company.service';
import { ComparisonService } from '../../core/services/comparison.service';
import { CompanySummary } from '../../core/models/company.model';
import { MarkdownPipe } from '../../core/pipes/markdown.pipe';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-comparisons',
  standalone: true,
  imports: [FormsModule, MarkdownPipe, DatePipe],
  templateUrl: './comparisons.component.html',
  styleUrl: './comparisons.component.scss'
})
export class ComparisonsComponent implements OnInit {
  today = new Date();
  private readonly companyService = inject(CompanyService);
  private readonly comparisonService = inject(ComparisonService);

  companies = signal<CompanySummary[]>([]);
  loadingCompanies = signal(true);

  companyA = signal('');
  yearA = signal<number | null>(null);

  companyB = signal('');
  yearB = signal<number | null>(null);

  metricFocus = signal('Revenue growth, operating margin, and profitability');

  comparing = signal(false);
  analysisResult = signal<string | null>(null);
  analysisProvider = signal<string | null>(null);
  errorMessage = signal('');

  ngOnInit(): void {
    this.companyService.getCompanies().subscribe({
      next: (data) => {
        this.companies.set(data);
        if (data.length > 0) {
          this.companyA.set(data[0].company);
          this.yearA.set(data[0].reports[0]?.year ?? null);
        }
        if (data.length > 1) {
          this.companyB.set(data[1].company);
          this.yearB.set(data[1].reports[0]?.year ?? null);
        } else if (data.length === 1) {
          this.companyB.set(data[0].company);
          this.yearB.set(data[0].reports[1]?.year ?? data[0].reports[0]?.year ?? null);
        }
        this.loadingCompanies.set(false);
      },
      error: () => {
        this.loadingCompanies.set(false);
      }
    });
  }

  getYearsFor(companyName: string): number[] {
    const comp = this.companies().find((c) => c.company === companyName);
    if (!comp) return [];
    return comp.reports
      .map((r) => r.year)
      .filter((y): y is number => y !== null);
  }

  executeComparison(): void {
    if (!this.companyA() || !this.companyB()) {
      this.errorMessage.set('Select both entities to run a comparison.');
      return;
    }

    this.comparing.set(true);
    this.errorMessage.set('');
    this.analysisResult.set(null);

    this.comparisonService.runComparison({
      companyA: this.companyA(),
      yearA: this.yearA(),
      companyB: this.companyB(),
      yearB: this.yearB(),
      metricFocus: this.metricFocus()
    }).subscribe({
      next: (res) => {
        this.analysisResult.set(res.analysis);
        this.analysisProvider.set(res.provider);
        this.comparing.set(false);
      },
      error: (err) => {
        console.error('Comparison execution failed:', err);
        this.errorMessage.set('Failed to generate comparative analysis. Ensure MCP server is reachable.');
        this.comparing.set(false);
      }
    });
  }

  exportPdf(): void{
    window.print();
  }
}