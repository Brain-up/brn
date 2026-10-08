export default function arrayNext<T>(item: T, array: ArrayLike<T>): T | undefined {
  const targetArray = Array.isArray(array) ? array : Array.from(array);
  const itemIndex = targetArray.indexOf(item);
  return targetArray[itemIndex + 1];
}
