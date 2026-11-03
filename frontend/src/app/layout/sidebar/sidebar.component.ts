import { Component, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
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

  isMenuOpen = signal(false);

  readonly navigation: NavItem[] = [
    { label: 'Overview', icon: '◫' },
    { label: 'Companies', icon: '▤' },
    { label: 'Documents', icon: '▧' },
    { label: 'AI Research', icon: '✳', badge: 'AI' },
    { label: 'Comparisons', icon: '⌁' }
  ];

  select(page: string): void {
    this.pageSelect.emit(page);
    this.isMenuOpen.set(false);
  }

  toggleWorkspaceMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.isMenuOpen.update((open) => !open);
  }

  refreshWorkspace(): void {
    window.location.reload();
  }

  toggleFullScreen(): void {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(() => {});
    } else {
      document.exitFullscreen().catch(() => {});
    }
    this.isMenuOpen.set(false);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.isMenuOpen()) {
      this.isMenuOpen.set(false);
    }
  }
}