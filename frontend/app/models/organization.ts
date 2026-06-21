import Model, { attr } from '@ember-data/model';

export default class OrganizationModel extends Model {
  @attr('string') declare name: string;
  @attr('string') declare logoUrl: string | null;
  @attr('boolean') declare isDeleted: boolean;
  @attr('date') declare createdAt: Date;
}
