import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type Store from '@ember-data/store';
import type ToastService from '../../../../services/toast';
import { getOwner } from '@ember/application';

export default class AuthenticatedOrgBrandContentController extends Controller {
  declare model: any;
  @service declare store: Store;
  @service declare toast: ToastService;

  @tracked searchTitle = '';
  @tracked filterType = '';
  @tracked filterPriority = '';

  @tracked isCreating = false;
  @tracked newTitle = '';
  @tracked newType = 'YOUTUBE_VIDEO';
  @tracked newPriority = 'MEDIUM';
  @tracked newDescription = '';
  @tracked newDueDate = '';

  stages = [
    'IDEA',
    'RESEARCH',
    'SCRIPT',
    'PRODUCTION',
    'EDITING',
    'REVIEW',
    'SCHEDULED',
    'PUBLISHED',
  ];
  contentTypes = [
    'YOUTUBE_VIDEO',
    'REEL',
    'SHORT',
    'BLOG',
    'LINKEDIN_POST',
    'PODCAST',
    'OTHER',
  ];
  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  get filteredContents() {
    let contents = (this.model as any[]) || [];

    if (this.searchTitle) {
      const query = this.searchTitle.toLowerCase();
      contents = contents.filter(
        (c) => c.title && c.title.toLowerCase().includes(query),
      );
    }

    if (this.filterType) {
      contents = contents.filter((c) => c.type === this.filterType);
    }

    if (this.filterPriority) {
      contents = contents.filter((c) => c.priority === this.filterPriority);
    }

    return contents;
  }

  // Get items grouped by stage
  get contentsByStage() {
    const groups: Record<string, any[]> = {};
    this.stages.forEach((stage) => {
      groups[stage] = [];
    });

    this.filteredContents.forEach((content) => {
      const stage = content.stage || 'IDEA';
      if (groups[stage]) {
        groups[stage].push(content);
      } else {
        // Fallback for custom/unrecognized stages
        groups['IDEA']?.push(content);
      }
    });

    return groups;
  }

  @action
  openCreateModal() {
    this.newTitle = '';
    this.newDescription = '';
    this.newType = 'YOUTUBE_VIDEO';
    this.newPriority = 'MEDIUM';
    this.newDueDate = '';
    this.isCreating = true;
  }

  @action
  closeCreateModal() {
    this.isCreating = false;
  }

  @action
  async createContent(event: Event) {
    event.preventDefault();
    if (!this.newTitle) {
      this.toast.error('Title is required');
      return;
    }

    const brand = this.modelForBrand;
    if (!brand) {
      this.toast.error('Active brand context not found.');
      return;
    }

    try {
      const newCard = this.store.createRecord('content', {
        title: this.newTitle,
        description: this.newDescription || null,
        type: this.newType,
        priority: this.newPriority,
        stage: 'IDEA',
        dueDate: this.newDueDate ? new Date(this.newDueDate) : null,
        brand,
      });

      await newCard.save();
      this.toast.success(`Content "${this.newTitle}" created successfully!`);
      this.isCreating = false;
    } catch (err: any) {
      this.toast.error(err.message || 'Failed to create content card.');
    }
  }

  @action
  async moveCard(content: any, direction: 'prev' | 'next') {
    const currentIndex = this.stages.indexOf(content.stage);
    if (currentIndex === -1) return;

    let newIndex = currentIndex;
    if (direction === 'prev' && currentIndex > 0) {
      newIndex = currentIndex - 1;
    } else if (direction === 'next' && currentIndex < this.stages.length - 1) {
      newIndex = currentIndex + 1;
    }

    if (newIndex !== currentIndex) {
      const oldStage = content.stage;
      const newStage = this.stages[newIndex];
      content.stage = newStage;

      try {
        await content.save();
        this.toast.success(`Moved to ${newStage}`);
      } catch (err: any) {
        content.stage = oldStage; // rollback
        this.toast.error(err.message || 'Failed to save stage change.');
      }
    }
  }

  private get modelForBrand() {
    return this.modelForRoute('authenticated.org.brand');
  }

  private modelForRoute(routeName: string) {
    return (getOwner(this) as any)
      .lookup(`route:${routeName}`)
      .modelFor(routeName);
  }
}
