import { inject as service } from '@ember/service';

import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
import FirebaseService from 'ember-firebase-service/services/firebase';

interface SerializedUser  {
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
  }
};

export default class FirebaseAuthenticator extends BaseAuthenticator {
  @service
  private firebase!: FirebaseService;

  public async authenticate(login: string, password: string): Promise<{ user: SerializedUser }> {
    // authenticate

    try {
      const result = await this.firebase.auth().signInWithEmailAndPassword(login, password);
      if (result.user === null) {
        throw new Error('No user');
      }
      return { user: result.user.toJSON() as SerializedUser };
    } catch(e) {
      if (e.code === 'auth/internal-error') {
        const { error }: any = JSON.parse(e.message);
        const errorObj: any = new Error(error.message);
        errorObj.errors = error.errors;
        errorObj.code = error.code;
        errorObj.status = error.status;
        errorObj.message = error.message;
        throw errorObj;
      } else if (e.code === 'auth/user-not-found') {
        try {
          const newUser = await this.firebase.auth().createUserWithEmailAndPassword(login, password);
          if (newUser.user === null) {
            throw new Error('No user');
          }
          return {
            user: newUser.user.toJSON() as SerializedUser
          };
        } catch(e) {
          const { error }: any = e.message;
          const errorObj: any = new Error(error);
          errorObj.errors = e.errors;
          errorObj.code = e.code;
          errorObj.status = e.status;
          errorObj.message = e.message;
          throw errorObj;
        }
      }
    }

  }

  public invalidate(): Promise<void> {
    return this.firebase.auth().signOut();
  }

  public restore(): Promise<{ user: SerializedUser }> {
    return new Promise((resolve, reject) => {
      const auth = this.firebase.auth();

      const unsubscribe = auth.onAuthStateChanged(async (user) => {
        unsubscribe();

        if (user) {
          resolve({ user: user.toJSON() as SerializedUser });
        } else {
          auth.getRedirectResult().then((credential) => {
            if (credential) {
              resolve({ user: credential.user?.toJSON() as SerializedUser });
            } else {
              reject();
            }
          }).catch(() => {
            reject();
          });
        }
      }, () => {
        reject();
        unsubscribe();
      });
    });
  }
}
