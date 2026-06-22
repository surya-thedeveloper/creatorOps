import Model, { attr, belongsTo } from '@ember-data/model';
import type ContentModel from './content';

export default class AssetModel extends Model {
  @attr('string') declare name: string;
  @attr('string') declare description: string | null;
  @attr('string') declare assetType:
    | 'RAW_VIDEO'
    | 'EDITED_VIDEO'
    | 'THUMBNAIL'
    | 'AUDIO_SFX'
    | 'DOCUMENT'
    | 'IMAGE'
    | 'OTHER';
  @attr('string') declare assetSource:
    | 'GOOGLE_DRIVE'
    | 'ONEDRIVE'
    | 'LOCAL_UPLOAD'
    | 'FRAME_IO'
    | 'DROPBOX'
    | 'OTHER';
  @attr('string') declare fileUrl: string;
  @attr('number') declare fileSize: number | null;
  @attr('string') declare mimeType: string | null;
  @attr('number') declare version: number;
  @attr('date') declare createdAt: Date;

  @belongsTo('content', { async: true, inverse: null })
  declare content: ContentModel;
}
