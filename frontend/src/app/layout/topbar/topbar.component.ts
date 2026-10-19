import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-topbar',
  standalone: true,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  @Input() activePage = 'Overview';
  @Input() searchQuery = '';
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() searchQueryChange = new EventEmitter<string>();

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQueryChange.emit(input.value);
  }
}