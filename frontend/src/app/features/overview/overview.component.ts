import { Component, EventEmitter, Output } from '@angular/core';
import { StatItem } from '../../core/models/navigation.model';

@Component({
  selector: 'app-overview',
  standalone: true,
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent {
  @Output() navigate = new EventEmitter<string>();

  readonly stats: StatItem[] = [
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
}