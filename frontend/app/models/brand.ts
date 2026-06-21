import Model, { attr, belongsTo } from '@ember-data/model';
import type OrganizationModel from './organization';

export default class BrandModel extends Model {
  @attr('string') declare name: string;
  @attr('string') declare description: string | null;
  @attr('string') declare logoUrl: string | null;
  @attr('boolean') declare isDeleted: boolean;

  @belongsTo('organization', { async: true, inverse: null })
  declare organization: OrganizationModel;
}
