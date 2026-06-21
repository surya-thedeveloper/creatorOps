import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type ToastService from '../../../../services/toast';

export default class SettingsController extends Controller {
  @service declare toast: ToastService;

  @tracked isSaving = false;

  @action
  async saveBrand(event: Event) {
    event.preventDefault();
    this.isSaving = true;

    try {
      const brand = this.model as any;
      await brand.save();
      this.toast.success('Brand configuration updated successfully!');
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to update brand configurations.');
    } finally {
      this.isSaving = false;
    }
  }
}
