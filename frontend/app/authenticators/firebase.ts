import { inject as service } from '@ember/service';

import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
import FirebaseService from 'ember-firebase-service/services/firebase';
import type firebase from 'firebase';

export default class FirebaseAuthenticator extends BaseAuthenticator {
  @service
  private firebase!: FirebaseService;

  public async authenticate(login: string, password: string): Promise<{ user: firebase.User | null }> {
    // authenticate

    try {
      const result = await this.firebase.auth().signInWithEmailAndPassword(login, password);
      console.log(result);

      // const credential = await callback(auth);

      return { user: result };
    } catch(e) {
      if (e.code === 'auth/internal-error') {
        const { error }: any = JSON.parse(e.message);
        const errorObj: any = new Error(error.message);
        errorObj.errors = error.errors;
        errorObj.code = error.code;
        errorObj.status = error.status;
        errorObj.message = error.message;
        throw errorObj;
      }
    }

  }

  public invalidate(): Promise<void> {
    return this.firebase.auth().signOut();
  }

  public restore(): Promise<{ user: firebase.User | null }> {
    return new Promise((resolve, reject) => {
      const auth = this.firebase.auth();

      const unsubscribe = auth.onAuthStateChanged(async (user) => {
        unsubscribe();

        if (user) {
          resolve({ user });
        } else {
          auth.getRedirectResult().then((credential) => {
            if (credential) {
              resolve({ user: credential.user });
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
