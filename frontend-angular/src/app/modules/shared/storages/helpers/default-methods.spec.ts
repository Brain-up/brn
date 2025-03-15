import { defaultMethods } from "./default-methods";

const key = "1234";
const value = "value";

beforeEach(() => {
  let store = {};
  const mockLocalStorage = {
    getItem: (key2: string): string => {
      return key2 in store ? store[key2] : null;
    },
    setItem: (key2: string, value2: string) => {
      store[key2] = `${value2}`;
    },
    removeItem: (key2: string) => {
      delete store[key2];
    },
    clear: () => {
      store = {};
    },
  };

  spyOn(localStorage, "getItem").and.callFake(mockLocalStorage.getItem);
  spyOn(localStorage, "setItem").and.callFake(mockLocalStorage.setItem);
  spyOn(localStorage, "removeItem").and.callFake(mockLocalStorage.removeItem);
  spyOn(localStorage, "clear").and.callFake(mockLocalStorage.clear);
});

describe("defaultMethods", () => {
  it("should call getter", () => {
    // const spyGetter = spyOnProperty(
    //   defaultMethods(key),
    //   'key',
    //   'get',
    // ).and.callThrough();
    expect(defaultMethods(key).key).toBe("1234");
  });

  it("should return stored token from localStorage", () => {
    localStorage.setItem(key, value);
    expect(defaultMethods(key).get()).toEqual("value");
  });

  it("should store the token in localStorage", () => {
    defaultMethods(key).set(value);
    expect(localStorage.getItem(key)).toEqual("value");
  });

  it("should remove value from localStorage", () => {
    defaultMethods(key).set(value);
    defaultMethods(key).remove();
    expect(localStorage.getItem(key)).toEqual(null);
  });
});
