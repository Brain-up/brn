import type AuthTokenService from 'brn/services/auth-token';

/**
 * Request handler that adds Authorization headers to all requests.
 * Replaces the auth logic previously in ApplicationAdapter.headers.
 */
export class AuthHandler {
  authToken: AuthTokenService;

  constructor(authToken: AuthTokenService) {
    this.authToken = authToken;
  }

  async request<T>(context: any, next: (req: any) => Promise<T>): Promise<T> {
    const headers = new Headers(context.request.headers);
    headers.set('Content-Type', 'application/json');
    const authHeaders = this.authToken.headers;
    for (const [key, value] of Object.entries(authHeaders)) {
      headers.set(key, value);
    }
    context.request.headers = headers;
    return next(context.request);
  }
}
