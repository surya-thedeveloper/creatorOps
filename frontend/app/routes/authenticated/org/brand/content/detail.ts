import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type Store from '@ember/data/store';
import type ApiService from '../../../../services/api';
import RSVP from 'rsvp';

export default class ContentDetailRoute extends Route {
  @service declare store: Store;
  @service declare api: ApiService;

  model(params: { content_id: string }) {
    return RSVP.hash({
      content: this.store.findRecord('content', params.content_id),
      researchItems: this.store.query('research-item', { contentId: params.content_id }).catch(() => []),
      scripts: this.store.query('script', { contentId: params.content_id }).catch(() => []),
      assignments: this.store.query('assignment', { contentId: params.content_id }).catch(() => []),
      assets: this.store.query('asset', { contentId: params.content_id }).catch(() => []),
      activities: this.api.get<any>(`contents/${params.content_id}/activities`).catch(() => ({ content: [] })),
    });
  }

  setupController(controller: any, model: any) {
    super.setupController(controller, model);
    controller.initializeOverviewFields();
  }
}
