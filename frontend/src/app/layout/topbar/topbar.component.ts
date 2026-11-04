import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  OnInit,
  Output,
  signal,
  ViewChild
} from '@angular/core';
import { CompanyService } from '../../core/services/company.service';
import { CompanySummary } from '../../core/models/company.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent implements OnInit {
  private readonly companyService = inject(CompanyService);
  readonly authService = inject(AuthService);

  @Input() activePage = 'Overview';
  @Input() searchQuery = '';
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() searchQueryChange = new EventEmitter<string>();
  @Output() navigate = new EventEmitter<string>();

  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  companies = signal<CompanySummary[]>([]);

  ngOnInit(): void {
    this.companyService.getCompanies().subscribe({
      next: (data) => this.companies.set(data),
      error: () => {}
    });
  }

  get filteredCompanies(): CompanySummary[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return [];
    return this.companies().filter(
      (c) =>
        c.company.toLowerCase().includes(q) ||
        c.reports.some((r) => r.documentTitle.toLowerCase().includes(q))
    );
  }

  @HostListener('window:keydown', ['$event'])
  handleGlobalShortcut(event: KeyboardEvent): void {
    if (
      event.key === '/' &&
      document.activeElement?.tagName !== 'INPUT' &&
      document.activeElement?.tagName !== 'TEXTAREA'
    ) {
      event.preventDefault();
      this.searchInput?.nativeElement.focus();
    }

    if (event.key === 'Escape') {
      if (document.activeElement === this.searchInput?.nativeElement) {
        this.clearSearch();
        this.searchInput.nativeElement.blur();
      }
    }
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQueryChange.emit(input.value);
  }

  clearSearch(): void {
    this.searchQueryChange.emit('');
    if (this.searchInput) {
      this.searchInput.nativeElement.value = '';
    }
  }

  selectCompanyFromSearch(company: string): void {
    this.clearSearch();
    this.navigate.emit('Companies');
  }

  get userInitial(): string {
    const name = this.authService.currentUser()?.fullName;
    return name ? name.trim().charAt(0).toUpperCase() : 'S';
  }
}