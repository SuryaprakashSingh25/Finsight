import { Component } from '@angular/core';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { TopbarComponent } from './layout/topbar/topbar.component';
import { OverviewComponent } from './features/overview/overview.component';
import { DocumentsComponent } from './features/documents/documents.component';
import { PlaceholderComponent } from './features/placeholder/placeholder.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    SidebarComponent,
    TopbarComponent,
    OverviewComponent,
    DocumentsComponent,
    PlaceholderComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  sidebarCollapsed = false;
  activePage = 'Overview';
  searchQuery = '';

  selectPage(page: string): void {
    this.activePage = page;
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  updateSearch(query: string): void {
    this.searchQuery = query;
  }
}