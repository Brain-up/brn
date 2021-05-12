export interface IDefaultMethods {
  readonly key: string;
  get(): string | null;
  set(value: string): void;
  remove(): void;
}
