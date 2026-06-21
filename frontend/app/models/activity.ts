import Model, { attr } from '@ember-data/model';

export default class ActivityModel extends Model {
  @attr('date') declare occurredAt: Date;
  @attr('string') declare eventType: string;
  @attr('string') declare entityType: string;
  @attr('string') declare entityId: string;
  @attr('string') declare description: string;
  @attr('string') declare metadataJson: string | null;

  get parsedMetadata() {
    if (!this.metadataJson) return null;
    try {
      return JSON.parse(this.metadataJson);
    } catch {
      return null;
    }
  }
}
