import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NavItem } from '../../core/models/navigation.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input() activePage = 'Overview';
  @Output() pageSelect = new EventEmitter<string>();

  readonly navigation: NavItem[] = [
    { label: 'Overview', icon: '◫' },
    { label: 'Companies', icon: '▤' },
    { label: 'Documents', icon: '▧' },
    { label: 'AI Research', icon: '✳', badge: 'AI' },
    { label: 'Comparisons', icon: '⌁' }
  ];

  select(page: string): void {
    this.pageSelect.emit(page);
  }
}