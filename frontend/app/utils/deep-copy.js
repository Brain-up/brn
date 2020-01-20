export default function deepCopy(item) {
  return JSON.parse(JSON.stringify(item));
}
