/**
 * Sort an array of objects by a given key in ascending order.
 * Uses `<` / `>` comparison (works for numbers and lexicographic strings).
 * Returns a new sorted array (does not mutate the input).
 */
export function sortByKey<T>(items: T[], key: string & keyof T): T[] {
  return items.slice().sort((a, b) => {
    const aVal = a[key];
    const bVal = b[key];
    if (aVal < bVal) return -1;
    if (aVal > bVal) return 1;
    return 0;
  });
}
