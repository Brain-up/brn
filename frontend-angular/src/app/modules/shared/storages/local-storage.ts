import { defaultMethods } from './helpers/default-methods';

export abstract class ALocaleStorage {
  public static readonly AUTH_TOKEN = defaultMethods('authToken');
  public static readonly LANG = defaultMethods('lang');
  public static readonly SELECTED_USER = defaultMethods('user');
}
