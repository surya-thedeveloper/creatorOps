import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type Store from '@ember/data/store';

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
}
