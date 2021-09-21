import { inject as service } from '@ember/service';

import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
import FirebaseService from 'ember-firebase-service/services/firebase';
import firebase from 'firebase/app';

interface AuthenticateCallback {
  (auth: firebase.auth.Auth): Promise<firebase.auth.UserCredential>;
}

export default class FirebaseAuthenticator extends BaseAuthenticator {
  @service
  private firebase!: FirebaseService;

  public async authenticate(
    callback: AuthenticateCallback,
  ): Promise<{ user: firebase.User | null }> {
    console.log(...arguments);
    // authenticate
    //     this.firebase.auth().signInWithEmailAndPassword('foo', 'bar');

    const auth = this.firebase.auth();
    const credential = await callback(auth);

    return { user: credential.user };
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
