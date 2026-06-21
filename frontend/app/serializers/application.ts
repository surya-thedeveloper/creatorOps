import RESTSerializer from '@ember-data/serializer/rest';
import type Store from '@ember-data/store';
import type Model from '@ember-data/model';

export default class ApplicationSerializer extends RESTSerializer {
  normalizeResponse(
    store: Store,
    primaryModelClass: typeof Model,
    payload: any,
    id: string | null,
    requestType: string
  ) {
    const modelName = primaryModelClass.modelName; // e.g. 'content', 'brand'
    const singularKey = modelName;
    const pluralKey = this.pluralize(modelName);

    let normalizedPayload: any = {};

    if (payload && typeof payload === 'object' && 'success' in payload) {
      const data = payload.data;
      if (Array.isArray(data)) {
        // Simple list: { success: true, data: [...] }
        normalizedPayload[pluralKey] = data;
      } else if (data && typeof data === 'object') {
        if ('content' in data && Array.isArray(data.content)) {
          // Paginated list: { success: true, data: { content: [...], pagination: {...} } }
          normalizedPayload[pluralKey] = data.content;
          normalizedPayload.meta = {
            pagination: data.pagination,
          };
        } else {
          // Single record: { success: true, data: { id: 1, ... } }
          normalizedPayload[singularKey] = data;
        }
      }
    } else {
      normalizedPayload = payload;
    }

    return super.normalizeResponse(store, primaryModelClass, normalizedPayload, id, requestType);
  }

  // Helper to pluralize keys matching path Conventions
  private pluralize(word: string): string {
    if (word === 'research-item') {
      return 'research';
    }
    if (word.endsWith('y')) {
      return word.slice(0, -1) + 'ies';
    }
    return word + 's';
  }
}
