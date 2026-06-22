import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';
import type ApiService from '../services/api';
import type SessionService from '../services/session';
import type ToastService from '../services/toast';

export default class LoginController extends Controller {
  @service declare api: ApiService;
  @service declare session: SessionService;
  @service declare toast: ToastService;
  @service declare router: RouterService;

  @tracked email = '';
  @tracked password = '';
  @tracked errorMessage = '';
  @tracked isLoading = false;

  @action
  async login(event: Event) {
    event.preventDefault();
    if (!this.email || !this.password) {
      this.errorMessage = 'Email and password are required.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    try {
      // API-01: Backend returns { token, user } — field is `token`, not `accessToken`
      const response = await this.api.post<{
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

      this.session.saveSession({
        token: response.token,
        name: response.user.name,
        email: response.user.email,
        role: response.user.role,
        id: response.user.id,
        organizationId: response.user.organizationId || undefined,
      });

      this.toast.success('Successfully signed in!');

      const orgId = response.user.organizationId;
      if (!orgId) {
        this.router.transitionTo('setup.organization');
      } else {
        // Find if they have brands; default to setup.brand if none exist
        try {
          const brands = await this.api.get<any>('brands');
          const brandList = Array.isArray(brands)
            ? brands
            : brands?.content || [];
          if (brandList.length > 0) {
            const firstBrand = brandList[0];
            this.session.selectBrand(String(firstBrand.id));
            this.router.transitionTo(
              'authenticated.org.brand.content',
              String(orgId),
              String(firstBrand.id),
            );
          } else {
            this.router.transitionTo('setup.brand');
          }
        } catch {
          this.router.transitionTo('setup.brand');
        }
      }
    } catch (error: any) {
      this.errorMessage = error.message || 'Invalid email or password.';
      this.toast.error(this.errorMessage);
    } finally {
      this.isLoading = false;
    }
  }
}
