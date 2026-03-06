/**
 * Creates a minimal valid JWT token that Firebase v8 SDK can parse.
 * Firebase's internal JWT parser requires: sub, iss, aud, exp claims.
 */
export function createFakeJwt(claims: Record<string, unknown> = {}): string {
  const header = { alg: 'RS256', typ: 'JWT' };
  const now = Math.floor(Date.now() / 1000);
  const payload = {
    sub: 'test-uid',
    iss: 'https://securetoken.google.com/brainupspb',
    aud: 'brainupspb',
    exp: now + 86400,
    iat: now,
    email: 'admin@admin.com',
    email_verified: true,
    firebase: { sign_in_provider: 'password' },
    ...claims,
  };

  const encode = (obj: unknown) =>
    Buffer.from(JSON.stringify(obj)).toString('base64url');

  return `${encode(header)}.${encode(payload)}.fake-signature`;
}

/** Pre-generated fake JWT for use in mock responses */
export const FAKE_JWT = createFakeJwt();
