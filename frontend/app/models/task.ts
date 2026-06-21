import Model, { attr, belongsTo } from '@ember-data/model';
import type UserModel from './user';
import type AssignmentModel from './assignment';

export default class TaskModel extends Model {
  @attr('string') declare title: string;
  @attr('string') declare description: string | null;
  @attr('string') declare priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  @attr('string') declare status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'DONE';
  @attr('date') declare dueDate: Date | null;

  @belongsTo('user', { async: true, inverse: null })
  declare assignedToUser: UserModel | null;

  @belongsTo('assignment', { async: true, inverse: null })
  declare assignment: AssignmentModel;
}
