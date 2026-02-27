export default function arrayNext(item, array) {
  const targetArray = Array.isArray(array) ? array : Array.from(array);
  const itemIndex = targetArray.indexOf(item);
  return targetArray[itemIndex + 1];
}
