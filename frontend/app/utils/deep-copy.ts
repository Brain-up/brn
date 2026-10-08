export default function deepCopy<T>(item: T): T {
  return JSON.parse(JSON.stringify(item));
}
