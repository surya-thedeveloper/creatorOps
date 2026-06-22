import Model, { attr, belongsTo } from '@ember-data/model';
import type UserModel from './user';
import type ContentModel from './content';

export default class AssignmentModel extends Model {
  @attr('string') declare assignmentType:
    | 'RESEARCH'
    | 'SCRIPT'
    | 'PRODUCTION'
    | 'EDITING'
    | 'REVIEW'
    | 'PUBLISHING'
    | 'OTHER';
  @attr('string') declare status:
    | 'PENDING'
    | 'IN_PROGRESS'
    | 'COMPLETED'
    | 'BLOCKED';
  @attr('string') declare notes: string | null;
  @attr('date') declare dueDate: Date | null;
  @attr('date') declare startedAt: Date | null;
  @attr('date') declare completedAt: Date | null;

  @belongsTo('user', { async: true, inverse: null })
  declare assignedToUser: UserModel;

  @belongsTo('user', { async: true, inverse: null })
  declare assignedByUser: UserModel;

  @belongsTo('content', { async: true, inverse: null })
  declare content: ContentModel;
}
