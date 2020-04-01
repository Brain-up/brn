export default function arrayNext(item, array) {
  const targetArray = array.toArray();
  const itemIndex = targetArray.indexOf(item);
  return targetArray[itemIndex + 1];
}
