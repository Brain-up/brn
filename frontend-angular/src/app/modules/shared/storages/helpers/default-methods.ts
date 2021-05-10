import {IDefaultMethods} from '../interfaces/default-methods.interface';

export function defaultMethods(key: string): IDefaultMethods {
  return {
    get key(): string {
      return key;
    },

    get(): string | null {
      return localStorage.getItem(this.key);
    },

    set(value: string): void {
      localStorage.setItem(this.key, value);
    },

    remove(): void {
      localStorage.removeItem(this.key);
    },
  };
}
