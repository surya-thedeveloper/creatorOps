import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type Store from '@ember-data/store';
import type ApiService from '../../../../../services/api';
import RSVP from 'rsvp';

export default class ContentDetailRoute extends Route {
  @service declare store: Store;
  @service declare api: ApiService;

  model(params: { content_id: string }) {
    const id = params.content_id;
    return RSVP.hash({
      // Content card loaded via Ember Data (adapter handles /api/v1/contents/:id)
      content: this.store.findRecord('content', id),

      // API-07: Research items live at /api/v1/contents/{id}/research (nested path)
      // store.query('research-item', { contentId }) hits the wrong flat path — use api.get directly
      researchItems: this.api
        .get<any[]>(`contents/${id}/research`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),

      // API-08: Scripts live at /api/v1/contents/{id}/scripts (nested path)
      scripts: this.api
        .get<any[]>(`contents/${id}/scripts`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),

      // API-09: Assignments live at /api/v1/contents/{id}/assignments (nested path)
      assignments: this.api
        .get<any[]>(`contents/${id}/assignments`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),

      // API-10: Assets live at /api/v1/contents/{id}/assets (nested path)
      assets: this.api
        .get<any[]>(`contents/${id}/assets`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),

      // Activity timeline — correct nested path
      activities: this.api
        .get<any>(`contents/${id}/activities`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),
    });
  }

  setupController(controller: any, model: any) {
    super.setupController(controller, model);
    controller.initializeOverviewFields();
  }

  // STATE-01: Reset controller state when leaving the route to prevent stale data
  resetController(controller: any, isExiting: boolean) {
    if (isExiting) {
      controller.activeTab = 'overview';
      controller.isSaving = false;
      controller.isGeneratingAi = false;
      controller.editTitle = '';
      controller.editDescription = '';
      controller.editStage = '';
      controller.editPriority = '';
      controller.editDueDate = '';
      controller.newNoteTitle = '';
      controller.newNoteText = '';
      controller.newLinkTitle = '';
      controller.newLinkUrl = '';
      controller.assignUserId = '';
      controller.assignNotes = '';
      controller.tasksList = [];
      controller.usersList = [];
    }
  }
}
