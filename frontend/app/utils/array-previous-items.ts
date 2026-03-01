export default function arrayPreviousItems<T>(targetItem: T, array: T[]): T[] {
  return array.slice(0, array.indexOf(targetItem));
}
