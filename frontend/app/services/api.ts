import Service from '@ember/service';
import { service } from '@ember/service';
import type SessionService from './session';

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Array<{ field: string; message: string }>;
}

export default class ApiService extends Service {
  @service declare session: SessionService;

  private host = 'http://localhost:8080';
  private namespace = 'api/v1';

  async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.host}/${this.namespace}/${path.replace(/^\//, '')}`;

    const headers = new Headers(options.headers || {});
    if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
      headers.set('Content-Type', 'application/json');
    }
    headers.set('Accept', 'application/json');

    // Auto-generate Correlation ID
    headers.set('X-Correlation-Id', crypto.randomUUID());

    // Auto-attach JWT Token
    if (this.session.token) {
      headers.set('Authorization', `Bearer ${this.session.token}`);
    }

    // Auto-attach Idempotency-Key for POST requests on AI generation endpoints
    const isAiGenerationEndpoint =
      options.method?.toUpperCase() === 'POST' &&
      path.includes('/ai/contents/') &&
      (path.endsWith('/brainstorm') || path.endsWith('/generate-script'));

    if (isAiGenerationEndpoint && !headers.has('Idempotency-Key')) {
      headers.set('Idempotency-Key', crypto.randomUUID());
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      let errorBody: ApiError;
      try {
        errorBody = await response.json();
      } catch {
        errorBody = {
          timestamp: new Date().toISOString(),
          status: response.status,
          error: response.statusText,
          message: 'An unexpected network error occurred.',
          path,
        };
      }
      throw errorBody;
    }

    // Unpack generic ApiResponse wrapper: { success: boolean, message: string, data: T }
    try {
      const payload = await response.json();
      if (payload && typeof payload === 'object' && 'success' in payload) {
        if (!payload.success) {
          throw {
            timestamp: payload.timestamp || new Date().toISOString(),
            status: response.status,
            error: 'API Error',
            message: payload.message || 'Operation failed',
            path,
          } as ApiError;
        }
        return payload.data as T;
      }
      return payload as T;
    } catch (e) {
      if (response.status === 204) {
        return {} as T;
      }
      throw e;
    }
  }

  get<T>(path: string, options: RequestInit = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'GET' });
  }

  post<T>(path: string, body?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  put<T>(path: string, body?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  patch<T>(path: string, body?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  delete<T>(path: string, options: RequestInit = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'DELETE' });
  }
}

declare module '@ember/service' {
  interface Registry {
    api: ApiService;
  }
}
