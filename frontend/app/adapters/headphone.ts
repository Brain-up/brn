import ApplicationAdapter from './application';

export default class HeadphoneAdapter extends ApplicationAdapter {
  pathForType(): string {
    return 'users/current/headphones';
  }
}
