import Model, { attr, belongsTo } from '@ember-data/model';
import type OrganizationModel from './organization';

export default class UserModel extends Model {
  @attr('string') declare fullName: string;
  @attr('string') declare email: string;
  @attr('string') declare role: 'ADMIN' | 'MANAGER' | 'CONTRIBUTOR';
  @attr('string') declare imageUrl: string | null;

  @belongsTo('organization', { async: true, inverse: null })
  declare organization: OrganizationModel;
}
