import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type Store from '@ember-data/store';

export default class AuthenticatedOrgBrandContentRoute extends Route {
  @service declare store: Store;

  model() {
    const parentModel = this.modelFor('authenticated.org.brand') as any;
    const brandId = parentModel?.id;
    if (!brandId) {
      return [];
    }

    return this.store.query('content', { brandId });
  }

  // STATE-01: Reset filter/modal state when navigating away from and back to the board
  resetController(controller: any, isExiting: boolean) {
    if (isExiting) {
      controller.isCreating = false;
      controller.newTitle = '';
      controller.newDescription = '';
      controller.newType = 'YOUTUBE_VIDEO';
      controller.newPriority = 'MEDIUM';
      controller.newDueDate = '';
    }
  }
}
