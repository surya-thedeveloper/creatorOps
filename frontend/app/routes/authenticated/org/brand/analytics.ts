import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type ApiService from '../../../../services/api';
import RSVP from 'rsvp';

export default class AnalyticsRoute extends Route {
  @service declare api: ApiService;

  model() {
    return RSVP.hash({
      content: this.api.get<any>('analytics/content').catch(() => ({})),
      assignments: this.api.get<any>('analytics/assignments').catch(() => ({})),
      tasks: this.api.get<any>('analytics/tasks').catch(() => ({})),
      publishing: this.api.get<any>('analytics/publishing').catch(() => ({})),
    });
  }
}
