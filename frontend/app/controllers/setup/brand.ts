import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';
import type ApiService from '../../services/api';
import type SessionService from '../../services/session';
import type ToastService from '../../services/toast';

export default class SetupBrandController extends Controller {
  @service declare api: ApiService;
  @service declare session: SessionService;
  @service declare toast: ToastService;
  @service declare router: RouterService;

  @tracked name = '';
  @tracked description = '';
  @tracked logoUrl = '';
  @tracked errorMessage = '';
  @tracked isLoading = false;

  @action
  updateField(field: 'name' | 'description' | 'logoUrl', event: Event) {
    this[field] = (event.target as HTMLInputElement).value;
  }

  @action
  async createBrand(event: Event) {
    event.preventDefault();

    if (!this.name) {
      this.errorMessage = 'Brand name is required.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    try {
      const response = await this.api.post<{
        id: number;
        name: string;
        description: string;
        logoUrl: string | null;
        organizationId: number;
      }>('brands', {
        name: this.name,
        description: this.description || null,
        logoUrl: this.logoUrl || null,
      });

      this.session.selectBrand(String(response.id));
      this.toast.success(`Brand "${response.name}" created successfully!`);

      const orgId = String(response.organizationId || this.session.orgId);
      this.router.transitionTo(
        'authenticated.org.brand.content',
        orgId,
        String(response.id),
      );
    } catch (error: any) {
      this.errorMessage =
        error.message || 'Failed to create brand. Please try again.';
      this.toast.error(this.errorMessage);
    } finally {
      this.isLoading = false;
    }
  }
}
