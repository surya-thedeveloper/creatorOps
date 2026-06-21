import Model, { attr, belongsTo } from '@ember-data/model';
import type ContentModel from './content';

export default class ResearchItemModel extends Model {
  @attr('string') declare type: 'NOTE' | 'LINK' | 'AI_BRAINSTORM';
  @attr('string') declare title: string;
  @attr('string') declare contentText: string | null;
  @attr('string') declare externalUrl: string | null;
  @attr('date') declare createdAt: Date;

  @belongsTo('content', { async: true, inverse: null })
  declare content: ContentModel;
}
