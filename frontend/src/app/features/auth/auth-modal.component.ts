import { Component, EventEmitter, inject, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: "auth-modal.component.html",
  styleUrl: "auth-modal.component.scss"
})
export class AuthModalComponent {
  private readonly authService = inject(AuthService);

  @Output() authenticated = new EventEmitter<void>();

  isSignUp = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  email = '';
  password = '';
  fullName = '';

  toggleMode(): void {
    this.isSignUp.update((v) => !v);
    this.errorMessage.set(null);
  }

  submit(): void {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.errorMessage.set(null);

    const req$ = this.isSignUp()
      ? this.authService.register({ email: this.email, password: this.password, fullName: this.fullName })
      : this.authService.login({ email: this.email, password: this.password });

    req$.subscribe({
      next: () => {
        this.loading.set(false);
        this.authenticated.emit();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(typeof err.error === 'string' ? err.error : 'Authentication failed. Please verify credentials.');
      }
    });
  }
}