import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';
import type ApiService from '../../services/api';
import type SessionService from '../../services/session';
import type ToastService from '../../services/toast';

export default class SetupOrganizationController extends Controller {
  @service declare api: ApiService;
  @service declare session: SessionService;
  @service declare toast: ToastService;
  @service declare router: RouterService;

  @tracked name = '';
  @tracked logoUrl = '';
  @tracked errorMessage = '';
  @tracked isLoading = false;

  @action
  async createOrg(event: Event) {
    event.preventDefault();

    if (!this.name) {
      this.errorMessage = 'Organization name is required.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    try {
      const response = await this.api.post<{
        id: number;
        name: string;
        logoUrl: string | null;
      }>('organizations', {
        name: this.name,
        logoUrl: this.logoUrl || null,
      });

      this.session.selectOrg(String(response.id), response.name);
      this.toast.success(
        `Organization "${response.name}" created successfully!`,
      );
      this.router.transitionTo('setup.brand');
    } catch (error: any) {
      this.errorMessage =
        error.message || 'Failed to create organization. Please try again.';
      this.toast.error(this.errorMessage);
    } finally {
      this.isLoading = false;
    }
  }
}
