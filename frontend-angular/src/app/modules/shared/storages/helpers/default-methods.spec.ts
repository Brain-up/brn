import { defaultMethods } from './default-methods';

const key = '1234';
const value = 'value';

beforeEach(() => {
  let store = {};
  const mockLocalStorage = {
    getItem: (key: string): string => {
      return key in store ? store[key] : null;
    },
    setItem: (key: string, value: string) => {
      store[key] = `${value}`;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };

  spyOn(localStorage, 'getItem').and.callFake(mockLocalStorage.getItem);
  spyOn(localStorage, 'setItem').and.callFake(mockLocalStorage.setItem);
  spyOn(localStorage, 'removeItem').and.callFake(mockLocalStorage.removeItem);
  spyOn(localStorage, 'clear').and.callFake(mockLocalStorage.clear);
});

describe('defaultMethods', () => {
  it('should call getter', () => {
    const spyGetter = spyOnProperty(
      defaultMethods(key),
      'key',
      'get',
    ).and.callThrough();
    expect(defaultMethods(key).key).toBe('1234');
  });

  it('should return stored token from localStorage', () => {
    localStorage.setItem(key, value);
    expect(defaultMethods(key).get()).toEqual('value');
  });

  it('should store the token in localStorage', () => {
    defaultMethods(key).set(value);
    expect(localStorage.getItem(key)).toEqual('value');
  });

  it('should remove value from localStorage', () => {
    defaultMethods(key).set(value);
    defaultMethods(key).remove();
    expect(localStorage.getItem(key)).toEqual(null);
  });
});
