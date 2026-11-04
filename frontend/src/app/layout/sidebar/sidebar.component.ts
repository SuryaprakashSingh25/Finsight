import { Component, EventEmitter, HostListener, inject, Input, Output, signal } from '@angular/core';
import { NavItem } from '../../core/models/navigation.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  readonly authService = inject(AuthService);

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

  get userInitials(): string {
    const name = this.authService.currentUser()?.fullName?.trim();
    if (!name) return 'FS';
    const parts = name.split(/\s+/);
    return parts.length > 1
      ? (parts[0][0] + parts[1][0]).toUpperCase()
      : parts[0].substring(0, 2).toUpperCase();
  }

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

  logout(): void {
    this.authService.logout();
    this.isMenuOpen.set(false);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.isMenuOpen()) {
      this.isMenuOpen.set(false);
    }
  }
}