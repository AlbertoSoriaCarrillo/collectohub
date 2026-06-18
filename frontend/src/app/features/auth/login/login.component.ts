import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageSelectorComponent } from '../../../core/i18n/language-selector.component';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    LanguageSelectorComponent,
    TranslatePipe
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.authService
      .login(this.form.getRawValue())
      .pipe(
        finalize(() => {
          this.loading.set(false);
          this.form.controls.password.reset('');
        })
      )
      .subscribe({
        next: () => {
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
          void this.router.navigateByUrl(returnUrl);
        },
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }
}
