import type AuthTokenService from 'brn/services/auth-token';
import type { Handler, NextFn } from '@warp-drive/core/request';
import type { RequestContext } from '@warp-drive/core/types/request';

/**
 * Request handler that adds Authorization headers to all requests.
 * Replaces the auth logic previously in ApplicationAdapter.headers.
 */
export class AuthHandler implements Handler {
  authToken: AuthTokenService;

  constructor(authToken: AuthTokenService) {
    this.authToken = authToken;
  }

  async request<T>(context: RequestContext, next: NextFn<T>) {
    const headers = new Headers(context.request.headers as HeadersInit | undefined);
    headers.set('Content-Type', 'application/json');
    const authHeaders = this.authToken.headers;
    for (const [key, value] of Object.entries(authHeaders)) {
      headers.set(key, value);
    }
    return next({ ...context.request, headers });
  }
}
