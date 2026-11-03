import { Component, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { OverviewService } from '../../core/services/overview.service';
import { OverviewStats } from '../../core/models/overview.model';

@Component({
  selector: 'app-overview',
  standalone: true,
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent implements OnInit {
  private readonly overviewService = inject(OverviewService);

  @Output() navigate = new EventEmitter<string>();

  stats = signal<OverviewStats | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.overviewService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load overview stats', err);
        this.loading.set(false);
      }
    });
  }

  formatCount(val: number | undefined): string {
    if (val === undefined || val === null) return '00';
    return val < 10 ? `0${val}` : `${val}`;
  }
}