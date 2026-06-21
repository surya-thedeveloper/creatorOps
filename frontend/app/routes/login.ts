import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type SessionService from '../services/session';
import type RouterService from '@ember/routing/router-service';

export default class LoginRoute extends Route {
  @service declare session: SessionService;
  @service declare router: RouterService;

  beforeModel() {
    if (this.session.isAuthenticated) {
      if (!this.session.orgId || this.session.orgId === 'null') {
        this.router.transitionTo('setup.organization');
      } else if (!this.session.brandId || this.session.brandId === 'null') {
        this.router.transitionTo('setup.brand');
      } else {
        this.router.transitionTo('authenticated.org.brand.content', this.session.orgId, this.session.brandId);
      }
    }
  }
}
