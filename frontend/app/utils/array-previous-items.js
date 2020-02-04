export default function arrayPreviousItems(targetItem, array) {
  return array.slice(0, array.indexOf(targetItem));
}
