import Model, { attr, belongsTo } from '@ember-data/model';
import type BrandModel from './brand';

export default class ContentModel extends Model {
  @attr('string') declare title: string;
  @attr('string') declare description: string | null;
  @attr('string') declare type:
    | 'YOUTUBE_VIDEO'
    | 'REEL'
    | 'SHORT'
    | 'BLOG'
    | 'LINKEDIN_POST'
    | 'PODCAST'
    | 'OTHER';
  @attr('string') declare stage:
    | 'IDEA'
    | 'RESEARCH'
    | 'SCRIPT'
    | 'PRODUCTION'
    | 'EDITING'
    | 'REVIEW'
    | 'SCHEDULED'
    | 'PUBLISHED'
    | 'ON_HOLD'
    | 'CANCELLED';
  @attr('string') declare priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  @attr('date') declare dueDate: Date | null;
  @attr('date') declare publishDate: Date | null;
  @attr('boolean') declare isDeleted: boolean;

  @belongsTo('brand', { async: true, inverse: null })
  declare brand: BrandModel;
}
