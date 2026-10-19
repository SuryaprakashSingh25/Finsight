import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  templateUrl: './placeholder.component.html',
  styleUrl: './placeholder.component.scss'
})
export class PlaceholderComponent {
  @Input() activePage = '';
  @Output() navigate = new EventEmitter<string>();
}