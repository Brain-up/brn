import { inject as service } from '@ember/service';

import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
import FirebaseService from 'ember-firebase-service/services/firebase';
import { getOwner } from '@ember/application';

export interface SerializedUser {
  uid: string;
  displayName: null | string;
  email: string;
  emailVerified: boolean;
  photoURL: null | string;
  stsTokenManager: {
    accessToken: string;
    apiKey: string;
    expirationTime: number;
    refreshToken: string;
  };
}

export default class FirebaseAuthenticator extends BaseAuthenticator {
  @service
  private firebase!: FirebaseService;

  public async registerUser(email: string, password: string) {
    return this.firebase.auth().createUserWithEmailAndPassword(email, password);
  }

  public async resetPassword(email: string) {
    return this.firebase.auth().sendPasswordResetEmail(email);
  }

  public async confirmPasswordReset(code: string, newPassword: string) {
    return this.firebase.auth().confirmPasswordReset(code, newPassword);
  }

  public async authenticate(
    login: string,
    password: string,
  ): Promise<{ user: SerializedUser }> {
    // authenticate

    try {
      const result = await this.firebase
        .auth()
        .signInWithEmailAndPassword(login, password);
      if (result.user === null) {
        throw new Error('No user');
      }
      return {
        user: this.applyTimersToUser(result.user.toJSON() as SerializedUser),
      };
    } catch (e) {
      const firebaseError = e as { code?: string; message: string; errors?: unknown[]; status?: string };
      // https://firebase.google.com/docs/reference/js/v8/firebase.auth.Auth#signinandretrievedatawithcredential
      if (firebaseError.code === 'auth/internal-error') {
        const { error }: any = JSON.parse(firebaseError.message);
        const errorObj: any = new Error(error.message);
        errorObj.errors = error.errors;
        errorObj.code = error.code;
        errorObj.status = error.status;
        errorObj.message = error.message;
        throw errorObj;
      } else if (firebaseError.code === 'auth/user-not-found') {
        const { error }: any = firebaseError.message;
        const errorObj: any = new Error(error);
        errorObj.errors = firebaseError.errors;
        errorObj.code = firebaseError.code;
        errorObj.status = firebaseError.status;
        errorObj.message = firebaseError.message;
        throw errorObj;
      } else if (firebaseError.code === 'auth/wrong-password') {
        const { error }: any = firebaseError.message;
        const errorObj: any = new Error(error);
        errorObj.errors = firebaseError.errors;
        errorObj.code = firebaseError.code;
        errorObj.status = firebaseError.status;
        errorObj.message = firebaseError.message;
        throw errorObj;
      } else if (firebaseError.code === 'auth/invalid-credential') {
        const { error }: any = firebaseError.message;
        const errorObj: any = new Error(error);
        errorObj.errors = firebaseError.errors;
        errorObj.code = firebaseError.code;
        errorObj.status = firebaseError.status;
        errorObj.message = firebaseError.message;
        throw errorObj;
      } else if (firebaseError.code === 'auth/user-disabled') {
        const { error }: any = firebaseError.message;
        const errorObj: any = new Error(error);
        errorObj.errors = firebaseError.errors;
        errorObj.code = firebaseError.code;
        errorObj.status = firebaseError.status;
        errorObj.message = firebaseError.message;
        throw errorObj;
      }
      throw e;
    }
  }

  public invalidate(): Promise<void> {
    return this.firebase.auth().signOut();
  }

  tokenRefreshTimeout: any = null;

  private async refreshToken() {
    const auth = await this.firebase.auth();
    await auth.currentUser?.getIdToken(true);
    const userSnapshot: SerializedUser =
      getOwner(this).lookup('service:session').data?.authenticated.user;
    const user = auth.currentUser?.toJSON() as SerializedUser;
    userSnapshot.stsTokenManager = user.stsTokenManager;
    this.applyTimersToUser(user);
  }

  private applyTimersToUser(user: SerializedUser) {
    this.scheduleTokenRefresh(user.stsTokenManager.expirationTime - Date.now());
    return user;
  }

  public scheduleTokenRefresh(interval: number) {
    clearTimeout(this.tokenRefreshTimeout);
    if (interval < 0) {
      this.refreshToken();
    } else {
      setTimeout(() => this.refreshToken(), interval);
    }
  }

  public restore(): Promise<{ user: SerializedUser }> {
    return new Promise((resolve, reject) => {
      const auth = this.firebase.auth();

      const unsubscribe = auth.onAuthStateChanged(
        async (user) => {
          unsubscribe();

          if (user) {
            const serializedUser: SerializedUser =
              user.toJSON() as SerializedUser;
            resolve({ user: this.applyTimersToUser(serializedUser) });
          } else {
            auth
              .getRedirectResult()
              .then((credential) => {
                if (credential) {
                  const serializedUser: SerializedUser =
                    credential.user?.toJSON() as SerializedUser;
                  resolve({ user: this.applyTimersToUser(serializedUser) });
                } else {
                  reject();
                }
              })
              .catch(() => {
                reject();
              });
          }
        },
        () => {
          reject();
          unsubscribe();
        },
      );
    });
  }
}
