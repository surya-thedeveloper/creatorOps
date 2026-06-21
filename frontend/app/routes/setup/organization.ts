import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type SessionService from '../../services/session';
import type RouterService from '@ember/routing/router-service';

export default class SetupOrganizationRoute extends Route {
  @service declare session: SessionService;
  @service declare router: RouterService;

  beforeModel() {
    if (!this.session.isAuthenticated) {
      this.router.transitionTo('login');
    } else if (this.session.orgId && this.session.orgId !== 'null') {
      this.router.transitionTo('setup.brand');
    }
  }
}
