import Controller from '@ember/controller';
import type RouterService from '@ember/routing/router-service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type ApiService from '../services/api';
import type SessionService from '../services/session';
import type ToastService from '../services/toast';

export default class RegisterController extends Controller {
  @service declare api: ApiService;
  @service declare router: RouterService;
  @tracked termsAccepted = false;
  @service declare session: SessionService;
  @service declare toast: ToastService;

  @tracked name = '';
  @tracked email = '';
  @tracked password = '';
  @tracked confirmPassword = '';
  @tracked errorMessage = '';
  @tracked isLoading = false;

  @action
  async register(event: Event) {
    // SEC-01: preventDefault must be called first — before any early returns —
    // to prevent the browser from doing a native form POST that exposes credentials in the URL.
    event.preventDefault();

    if (!this.termsAccepted) {
      this.errorMessage = 'You must accept the Terms of Service.';
      return;
    }

    if (!this.name || !this.email || !this.password || !this.confirmPassword) {
      this.errorMessage = 'All fields are required.';
      return;
    }

    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters long.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    try {
      // 1. Call Register endpoint
      await this.api.post('auth/register', {
        name: this.name,
        email: this.email,
        password: this.password,
      });

      this.toast.success('Registration successful! Logging in...');

      // 2. Perform automatic Login after registration
      // API-01: Backend returns { token, user } — not { accessToken, refreshToken, user }
      const loginResponse = await this.api.post<{
        token: string;
        user: {
          id: number;
          name: string;
          email: string;
          role: string;
          imageUrl: string | null;
          organizationId: number | null;
        };
      }>('auth/login', {
        email: this.email,
        password: this.password,
      });

      // 3. Save session details
      this.session.saveSession({
        token: loginResponse.token,
        email: loginResponse.user.email,
        role: loginResponse.user.role,
        id: loginResponse.user.id,
        organizationId: loginResponse.user.organizationId || undefined,
      });

      this.toast.success('Successfully logged in!');

      // 4. Since new users don't have an organization, transition to organization setup wizard
      this.router.transitionTo('setup.organization');
    } catch (error: any) {
      this.errorMessage =
        error.message || 'Registration failed. Please try again.';
      this.toast.error(this.errorMessage);
    } finally {
      this.isLoading = false;
    }
  }
}
