import Controller from '@ember/controller';
import { service } from '@ember/service';
import type SessionService from '../services/session';
import type RouterService from '@ember/routing/router-service';

export default class AuthenticatedController extends Controller {
  @service declare session: SessionService;
  @service declare router: RouterService;

  get userInitial(): string {
    const name = this.session.userName || this.session.userEmail || 'U';
    return name.charAt(0).toUpperCase();
  }

  // UX-06: Dynamic page title based on the current route
  get pageTitle(): string {
    const route = this.router.currentRouteName ?? '';

    if (route.includes('content.detail')) return 'Content Details';
    if (route.includes('content')) return 'Content Board';
    if (route.includes('calendar')) return 'Release Calendar';
    if (route.includes('analytics')) return 'Workspace Analytics';
    if (route.includes('team')) return 'Team';
    if (route.includes('settings')) return 'Settings';
    if (route.includes('dashboard')) return 'Dashboard';

    return 'CreatorOps';
  }
}
