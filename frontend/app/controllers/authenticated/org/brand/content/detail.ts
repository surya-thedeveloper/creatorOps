import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';
import type Store from '@ember-data/store';
import type ApiService from '../../../../../services/api';
import type ToastService from '../../../../../services/toast';
import { getOwner } from '@ember/application';

export default class ContentDetailController extends Controller {
  declare model: any;
  @service declare store: Store;
  @service declare api: ApiService;
  @service declare toast: ToastService;
  @service declare router: RouterService;

  @tracked activeTab = 'overview';
  @tracked isSaving = false;
  @tracked isGeneratingAi = false;

  // Overview Form
  @tracked editTitle = '';
  @tracked editDescription = '';
  @tracked editStage = '';
  @tracked editPriority = '';
  @tracked editDueDate = '';

  // Research Form
  @tracked newNoteTitle = '';
  @tracked newNoteText = '';
  @tracked newLinkTitle = '';
  @tracked newLinkUrl = '';

  // Assignment Form
  @tracked assignUserId = '';
  @tracked assignRole = 'RESEARCH'; // e.g. RESEARCH, SCRIPT, EDITING
  @tracked assignNotes = '';

  // Task Checklist Form
  @tracked selectedAssignmentIdForTask = '';
  @tracked newTaskTitle = '';
  @tracked newTaskPriority = 'MEDIUM';

  // Asset Form
  @tracked newAssetName = '';
  @tracked newAssetSource = 'GOOGLE_DRIVE'; // GOOGLE_DRIVE, YOUTUBE, OTHER
  @tracked newAssetType = 'RAW_VIDEO';
  @tracked newAssetUrl = '';

  // Local lists for tasks
  @tracked tasksList: any[] = [];
  @tracked usersList: any[] = [];

  stages = [
    'IDEA',
    'RESEARCH',
    'SCRIPT',
    'PRODUCTION',
    'EDITING',
    'REVIEW',
    'SCHEDULED',
    'PUBLISHED',
    'ON_HOLD',
    'CANCELLED',
  ];
  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  contentTypes = [
    'YOUTUBE_VIDEO',
    'REEL',
    'SHORT',
    'BLOG',
    'LINKEDIN_POST',
    'PODCAST',
    'OTHER',
  ];

  @action
  changeTab(tabName: string) {
    this.activeTab = tabName;
    if (tabName === 'assignments') {
      this.loadUsers();
    }
    if (tabName === 'tasks') {
      this.loadTasksForAssignments();
    }
  }

  async loadUsers() {
    try {
      // API-06: The backend API spec does not define a /api/v1/users listing endpoint in Phase 1.
      // We attempt to load from the brands' team endpoint if available.
      // Until a proper user-listing endpoint exists, the dropdown will be empty with a message.
      // TODO: Connect to real user listing endpoint in Phase 2.
      this.usersList = [];
    } catch {
      this.usersList = [];
    }
  }

  async loadTasksForAssignments() {
    const assignments = this.model.assignments || [];
    if (assignments.length === 0) {
      this.tasksList = [];
      return;
    }

    // PERF-01: Use Promise.all to fetch all assignment tasks in parallel instead of sequential for-await
    const results = await Promise.all(
      assignments.map((assignment: any) =>
        this.api
          .get<any>(`assignments/${assignment.id}/tasks`)
          .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
          .catch(() => []),
      ),
    );
    this.tasksList = results.flat();
  }

  @action
  initializeOverviewFields() {
    const content = this.model.content;
    if (content) {
      this.editTitle = content.title || '';
      this.editDescription = content.description || '';
      this.editStage = content.stage || 'IDEA';
      this.editPriority = content.priority || 'MEDIUM';
      this.editDueDate = content.dueDate
        ? new Date(content.dueDate).toISOString().split('T')[0] || ''
        : '';
    }
  }

  @action
  async saveOverview(event: Event) {
    event.preventDefault();
    this.isSaving = true;

    try {
      const content = this.model.content;
      content.title = this.editTitle;
      content.description = this.editDescription || null;
      content.stage = this.editStage;
      content.priority = this.editPriority;
      content.dueDate = this.editDueDate ? new Date(this.editDueDate) : null;

      await content.save();
      this.toast.success('Content card updated successfully!');
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to save updates.');
    } finally {
      this.isSaving = false;
    }
  }

  @action
  async deleteCard() {
    if (
      !confirm(
        'Are you sure you want to delete this content card? All details will be permanently archived.',
      )
    ) {
      return;
    }

    try {
      await this.model.content.destroyRecord();
      this.toast.success('Content card deleted successfully.');
      this.closeModal();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to delete card.');
    }
  }

  @action
  async addNote(event: Event) {
    event.preventDefault();
    if (!this.newNoteTitle || !this.newNoteText) {
      this.toast.error('Title and note content are required');
      return;
    }

    try {
      const response = await this.api.post<any>(
        `contents/${this.model.content.id}/research`,
        {
          type: 'NOTE',
          title: this.newNoteTitle,
          contentText: this.newNoteText,
        },
      );

      this.store.pushPayload('research-item', {
        'research-item': {
          id: response.id,
          type: 'NOTE',
          title: response.title,
          contentText: response.contentText,
          content: this.model.content.id,
        },
      });

      this.toast.success('Note added!');
      this.newNoteTitle = '';
      this.newNoteText = '';
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to save note.');
    }
  }

  @action
  async addLink(event: Event) {
    event.preventDefault();
    if (!this.newLinkTitle || !this.newLinkUrl) {
      this.toast.error('Title and URL are required');
      return;
    }

    try {
      const response = await this.api.post<any>(
        `contents/${this.model.content.id}/research`,
        {
          type: 'LINK',
          title: this.newLinkTitle,
          externalUrl: this.newLinkUrl,
        },
      );

      this.store.pushPayload('research-item', {
        'research-item': {
          id: response.id,
          type: 'LINK',
          title: response.title,
          externalUrl: response.externalUrl,
          content: this.model.content.id,
        },
      });

      this.toast.success('Link added!');
      this.newLinkTitle = '';
      this.newLinkUrl = '';
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to save link reference.');
    }
  }

  @action
  async triggerAiBrainstorm() {
    this.isGeneratingAi = true;
    try {
      await this.api.post(`ai/contents/${this.model.content.id}/brainstorm`);
      this.toast.success('AI brainstorming complete! Added hooks to research.');
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'AI generation failed.');
    } finally {
      this.isGeneratingAi = false;
    }
  }

  @action
  async triggerAiScript() {
    this.isGeneratingAi = true;
    try {
      await this.api.post(
        `ai/contents/${this.model.content.id}/generate-script`,
      );
      this.toast.success('AI script generated successfully!');
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'AI script drafting failed.');
    } finally {
      this.isGeneratingAi = false;
    }
  }

  @action
  async saveScriptText(script: any) {
    try {
      await script.save();
      this.toast.success('Script draft updated!');
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to save script.');
    }
  }

  @action
  async createAssignment(event: Event) {
    event.preventDefault();
    if (!this.assignUserId) {
      this.toast.error('Please select a user');
      return;
    }

    try {
      await this.api.post(`contents/${this.model.content.id}/assignments`, {
        assignedToUserId: this.assignUserId,
        assignmentType: this.assignRole,
        notes: this.assignNotes || null,
      });

      this.toast.success('Contributor assigned successfully!');
      this.assignUserId = '';
      this.assignNotes = '';
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to assign user.');
    }
  }

  @action
  async createChecklistItem(event: Event) {
    event.preventDefault();
    if (!this.selectedAssignmentIdForTask || !this.newTaskTitle) {
      this.toast.error('Assignment and title are required');
      return;
    }

    try {
      await this.api.post(
        `assignments/${this.selectedAssignmentIdForTask}/tasks`,
        {
          title: this.newTaskTitle,
          priority: this.newTaskPriority,
          status: 'TODO',
        },
      );

      this.toast.success('Checklist item added!');
      this.newTaskTitle = '';
      this.loadTasksForAssignments();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to add task.');
    }
  }

  @action
  async toggleTaskStatus(task: any) {
    const nextStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
    try {
      await this.api.patch(`tasks/${task.id}/status`, {
        status: nextStatus,
      });
      task.status = nextStatus;
      this.toast.success('Task updated!');
      this.loadTasksForAssignments();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to toggle task.');
    }
  }

  @action
  async addAsset(event: Event) {
    event.preventDefault();
    if (!this.newAssetName || !this.newAssetUrl) {
      this.toast.error('Name and URL link are required');
      return;
    }

    try {
      await this.api.post(`contents/${this.model.content.id}/assets`, {
        name: this.newAssetName,
        assetSource: this.newAssetSource,
        assetType: this.newAssetType,
        fileUrl: this.newAssetUrl,
      });

      this.toast.success('Asset URL registered!');
      this.newAssetName = '';
      this.newAssetUrl = '';
      this.refreshModelRelations();
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to register asset.');
    }
  }

  @action
  closeModal() {
    const route = (getOwner(this) as any).lookup(
      'route:authenticated.org.brand.content',
    );
    route.refresh(); // Refresh Kanban cards
    this.router.transitionTo('authenticated.org.brand.content');
  }

  private refreshModelRelations() {
    const route = (getOwner(this) as any).lookup(
      'route:authenticated.org.brand.content.detail',
    );
    route.refresh();
  }

  private modelForRoute(routeName: string) {
    return (getOwner(this) as any)
      .lookup(`route:${routeName}`)
      .modelFor(routeName);
  }
}
