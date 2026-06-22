import Model, { attr, belongsTo } from '@ember-data/model';
import type ContentModel from './content';

export default class ScriptModel extends Model {
  @attr('string') declare documentType:
    | 'INTERNAL'
    | 'GOOGLE_DOC'
    | 'MS_WORD'
    | 'UPLOADED_FILE';
  @attr('string') declare editorContent: string | null;
  @attr('string') declare externalDocumentUrl: string | null;
  @attr('string') declare uploadedFileReference: string | null;
  @attr('number') declare version: number;
  @attr('date') declare createdAt: Date;

  @belongsTo('content', { async: true, inverse: null })
  declare content: ContentModel;
}
