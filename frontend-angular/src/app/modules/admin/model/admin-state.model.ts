import { Group } from './group';

export interface AdminStateModel {
  folders?: Array<string>;
  groups?: Array<Group>;
}
