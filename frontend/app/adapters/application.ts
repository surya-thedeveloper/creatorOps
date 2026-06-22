import RESTAdapter from '@ember-data/adapter/rest';
import { service } from '@ember/service';
import type SessionService from '../services/session';

export default class ApplicationAdapter extends RESTAdapter {
  @service declare session: SessionService;

  host = 'http://localhost:8080';
  namespace = 'api/v1';

  get headers() {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-Correlation-Id': crypto.randomUUID(),
    };

    const token = this.session.token;
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  pathForType(modelName: string): string {
    if (modelName === 'research-item') {
      return 'research';
    }
    return super.pathForType(modelName);
  }
}
