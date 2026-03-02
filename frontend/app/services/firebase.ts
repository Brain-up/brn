import Service from '@ember/service';
import { getOwner } from '@ember/application';
import firebase from 'firebase/app';
import 'firebase/auth';

export default class FirebaseService extends Service {
  private _app: firebase.app.App | null = null;

  private get config(): { apiKey: string; authDomain: string; projectId: string } {
    const owner = getOwner(this) as unknown as { resolveRegistration(name: string): { firebase: { apiKey: string; authDomain: string; projectId: string } } };
    return owner.resolveRegistration('config:environment').firebase;
  }

  private get app(): firebase.app.App {
    if (!this._app) {
      if (firebase.apps.length === 0) {
        this._app = firebase.initializeApp(this.config);
      } else {
        this._app = firebase.apps[0]!;
      }
    }
    return this._app;
  }

  auth(): firebase.auth.Auth {
    return this.app.auth();
  }
}
