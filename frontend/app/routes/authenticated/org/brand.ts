import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type SessionService from '../../../services/session';
import type Store from '@ember-data/store';

export default class AuthenticatedOrgBrandRoute extends Route {
  @service declare session: SessionService;
  @service declare store: Store;

  async model(params: { org_id: string; brand_id: string }) {
    this.session.selectOrg(params.org_id);
    this.session.selectBrand(params.brand_id);
    
    try {
      const brand = await this.store.findRecord('brand', params.brand_id);
      return brand;
    } catch {
      return null;
    }
  }
}
