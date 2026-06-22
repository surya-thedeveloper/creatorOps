import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type SessionService from '../services/session';
import type RouterService from '@ember/routing/router-service';

export default class AuthenticatedRoute extends Route {
  @service declare session: SessionService;
  @service declare router: RouterService;

  beforeModel() {
    if (!this.session.isAuthenticated) {
      this.router.transitionTo('login');
    } else {
      const orgId = this.session.orgId;
      const brandId = this.session.brandId;

      // ROUTE-04: Check for falsy or 'null'/'undefined' strings robustly
      const hasOrg = orgId && orgId !== 'null' && orgId !== 'undefined';
      const hasBrand = brandId && brandId !== 'null' && brandId !== 'undefined';

      if (!hasOrg) {
        this.router.transitionTo('setup.organization');
      } else if (!hasBrand) {
        this.router.transitionTo('setup.brand');
      }
    }
  }
}
