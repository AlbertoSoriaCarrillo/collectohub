import { Component, effect, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorMessageService } from '../../../core/http/error-message.service';
import { LanguageService } from '../../../core/i18n/language.service';
import type { SupportedLanguage } from '../../../core/i18n/language.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPasswordControl = control.get('confirmPassword');
  const confirmPassword = confirmPasswordControl?.value;

  if (!confirmPasswordControl) {
    return null;
  }

  const currentErrors = confirmPasswordControl.errors ?? {};

  if (password && confirmPassword && password !== confirmPassword) {
    confirmPasswordControl.setErrors({ ...currentErrors, passwordMismatch: true });
    return { passwordMismatch: true };
  }

  if (currentErrors['passwordMismatch']) {
    const remainingErrors = { ...currentErrors };
    delete remainingErrors['passwordMismatch'];
    confirmPasswordControl.setErrors(
      Object.keys(remainingErrors).length > 0 ? remainingErrors : null
    );
  }

  return null;
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly errorMessageService = inject(ErrorMessageService);
  private readonly languageService = inject(LanguageService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group(
    {
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      displayName: ['', [Validators.required, Validators.maxLength(120)]],
      preferredInterfaceLanguage: this.fb.control<SupportedLanguage>(
        this.languageService.currentLanguage(),
        [Validators.required]
      )
    },
    { validators: passwordsMatchValidator }
  );

  constructor() {
    effect(() => {
      const currentLanguage = this.languageService.currentLanguage();
      if (this.form.controls.preferredInterfaceLanguage.value !== currentLanguage) {
        this.form.controls.preferredInterfaceLanguage.setValue(currentLanguage, { emitEvent: false });
      }
    });
  }

  changeLanguage(language: string): void {
    if (language === 'es' || language === 'en') {
      this.languageService.setLanguage(language);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    this.authService
      .register({
        email: value.email,
        password: value.password,
        displayName: value.displayName,
        preferredInterfaceLanguage: value.preferredInterfaceLanguage
      })
      .pipe(
        finalize(() => {
          this.loading.set(false);
          this.form.controls.password.reset('');
          this.form.controls.confirmPassword.reset('');
        })
      )
      .subscribe({
        next: () => void this.router.navigateByUrl('/collections'),
        error: (error) => this.errorMessage.set(this.errorMessageService.toMessage(error))
      });
  }
}
