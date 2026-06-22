import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type ApiService from '../../services/api';
import type Store from '@ember-data/store';
import RSVP from 'rsvp';

export default class DashboardRoute extends Route {
  @service declare api: ApiService;
  @service declare store: Store;

  model() {
    return RSVP.hash({
      summary: this.api.get<any>('analytics/dashboard').catch(() => ({
        totalContent: 0,
        scheduledContent: 0,
        publishedContent: 0,
        overdueContent: 0,
        totalAssignments: 0,
        activeAssignments: 0,
        totalTasks: 0,
        completedTasks: 0,
        overdueTasks: 0,
        totalAssets: 0,
      })),
      brands: this.store.findAll('brand').catch(() => []),
    });
  }
}
