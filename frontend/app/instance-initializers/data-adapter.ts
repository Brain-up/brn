import type ApplicationInstance from '@ember/application/instance';
import WarpDriveDataAdapter from 'brn/data-adapters/main';

export function initialize(appInstance: ApplicationInstance): void {
  appInstance.register('data-adapter:main', WarpDriveDataAdapter);
}

export default {
  name: 'warp-drive-data-adapter',
  initialize,
};
