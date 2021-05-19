import { defaultMethods } from './helpers/default-methods';

export abstract class ALocaleStorage {
  public static readonly LANG = defaultMethods('lang');
  public static readonly AUTH_TOKEN = defaultMethods('authToken');
}
