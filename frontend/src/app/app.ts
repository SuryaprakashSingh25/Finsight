import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-root',
  imports: [],
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  sidebarCollapsed=false;
  activePage='Overview';
  searchQuery='';

  readonly navigation=[
    { label: 'Overview', icon: '◫' },
    { label: 'Companies', icon: '▤' },
    { label: 'Documents', icon: '▧' },
    { label: 'AI Research', icon: '✳' },
    { label: 'Comparisons', icon: '⌁' }
  ];

  readonly stats=[
    {
      label: 'Companies tracked',
      value: '01',
      detail: 'Your research workspace',
      icon: '▤',
      color: 'teal'
    },
    {
      label: 'Reports indexed',
      value: '01',
      detail: 'Annual report available',
      icon: '▧',
      color: 'blue'
    },
    {
      label: 'AI providers',
      value: '03',
      detail: 'Automatic fallback enabled',
      icon: '✳',
      color: 'purple'
    }
  ];

  selectPage(page: string): void{
    this.activePage=page;
  }

  toggleSidebar(): void{
    this.sidebarCollapsed=!this.sidebarCollapsed;
  }
}
