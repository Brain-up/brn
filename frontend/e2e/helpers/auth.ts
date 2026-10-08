import type { Page } from '@playwright/test';
import { FAKE_JWT } from '../fixtures/fake-jwt';

const FIREBASE_API_KEY = 'AIzaSyCxu7mVxd_waBDUn9VKblBl4zl8MX5WxWY';
const FIREBASE_AUTH_DOMAIN = 'brainupspb.firebaseapp.com';
const FIREBASE_APP_NAME = '[DEFAULT]';

/**
 * Full Firebase user object matching the format produced by
 * firebase.User.toJSON() (see @firebase/auth `User.prototype.w()`).
 * Missing fields cause `qn()` deserialization to return null.
 */
const fakeFirebaseUser = {
  uid: 'test-uid',
  displayName: 'admin',
  email: 'admin@admin.com',
  emailVerified: true,
  photoURL: null,
  phoneNumber: null,
  isAnonymous: false,
  tenantId: null,
  providerData: [],
  apiKey: FIREBASE_API_KEY,
  appName: FIREBASE_APP_NAME,
  authDomain: FIREBASE_AUTH_DOMAIN,
  stsTokenManager: {
    apiKey: FIREBASE_API_KEY,
    accessToken: FAKE_JWT,
    refreshToken: 'fake-refresh-token',
    expirationTime: Date.now() + 86_400_000,
  },
  redirectEventId: null,
  lastLoginAt: '1700000000000',
  createdAt: '1600000000000',
  multiFactor: { enrolledFactors: [] },
};

/**
 * Seeds authentication for the app by pre-populating both
 * ember-simple-auth session (localStorage) and Firebase auth state (indexedDB).
 *
 * Navigates to the app origin first to establish the storage context,
 * seeds the data synchronously (awaiting indexedDB write), then the
 * caller navigates to the actual page.
 */
export async function seedAuth(page: Page): Promise<void> {
  // Navigate to the origin to establish storage context
  await page
    .goto('http://localhost:4200/__playwright_seed__', {
      waitUntil: 'commit',
      timeout: 10_000,
    })
    .catch(() => {
      // 404 is fine — we just need the browser context at this origin
    });

  const emberUser = {
    uid: fakeFirebaseUser.uid,
    displayName: fakeFirebaseUser.displayName,
    email: fakeFirebaseUser.email,
    emailVerified: fakeFirebaseUser.emailVerified,
    photoURL: fakeFirebaseUser.photoURL,
    stsTokenManager: fakeFirebaseUser.stsTokenManager,
  };

  // Seed localStorage
  await page.evaluate(
    ({ user, firebaseUser, apiKey }) => {
      localStorage.setItem(
        'ember_simple_auth-session',
        JSON.stringify({
          authenticated: {
            authenticator: 'authenticator:firebase',
            user,
          },
        }),
      );
      localStorage.setItem(
        `firebase:authUser:${apiKey}:[DEFAULT]`,
        JSON.stringify(firebaseUser),
      );
      localStorage.setItem('locale', 'en-us');
    },
    {
      user: emberUser,
      firebaseUser: fakeFirebaseUser,
      apiKey: FIREBASE_API_KEY,
    },
  );

  // Seed Firebase indexedDB (primary persistence)
  await page.evaluate(
    ({ firebaseUser, apiKey }) => {
      return new Promise<void>((resolve, reject) => {
        const dbReq = indexedDB.open('firebaseLocalStorageDb', 1);
        dbReq.onupgradeneeded = (event) => {
          const db = (event.target as IDBOpenDBRequest).result;
          if (!db.objectStoreNames.contains('firebaseLocalStorage')) {
            db.createObjectStore('firebaseLocalStorage', {
              keyPath: 'fbase_key',
            });
          }
        };
        dbReq.onsuccess = (event) => {
          const db = (event.target as IDBOpenDBRequest).result;
          const tx = db.transaction('firebaseLocalStorage', 'readwrite');
          const store = tx.objectStore('firebaseLocalStorage');
          store.put({
            fbase_key: `firebase:authUser:${apiKey}:[DEFAULT]`,
            value: firebaseUser,
          });
          tx.oncomplete = () => {
            db.close();
            resolve();
          };
          tx.onerror = () => {
            db.close();
            reject(new Error('Failed to seed indexedDB'));
          };
        };
        dbReq.onerror = () => reject(new Error('Failed to open indexedDB'));
      });
    },
    { firebaseUser: fakeFirebaseUser, apiKey: FIREBASE_API_KEY },
  );
}

/**
 * Seeds only the locale (no auth) for public pages.
 */
export async function seedLocale(page: Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem('locale', 'en-us');
  });
}
