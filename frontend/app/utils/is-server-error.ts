interface ErrorLike {
  status?: number;
  code?: number;
  isRequestError?: boolean;
  name?: string;
  message?: string;
  errors?: Array<{ status?: number | string }>;
}

/**
 * Detects whether an error represents a server/network failure
 * (as opposed to a client error like 401/404).
 *
 * Returns true for:
 * - WarpDrive FetchError with status >= 500 or status 0 (network error)
 * - HTTP 5xx errors from error payloads
 * - TypeError from fetch (network failures)
 * - AbortError (request timeout)
 */
export default function isServerError(error: ErrorLike): boolean {
  if (!error) return false;

  // WarpDrive FetchError with status >= 500 or network error (status 0)
  if (error.isRequestError) {
    const status = error.status ?? error.code ?? 0;
    return status === 0 || status >= 500;
  }

  // HTTP 5xx from error payload
  const status = error.status ?? Number(error.errors?.[0]?.status);
  if (status && status >= 500) return true;

  // Network failures (fetch throws TypeError for network errors)
  if (error instanceof TypeError && error.message?.includes('fetch')) return true;

  // AbortError (request timed out)
  if (error.name === 'AbortError') return true;

  return false;
}
