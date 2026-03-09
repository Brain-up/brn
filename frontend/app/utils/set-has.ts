export function setHas(set: Set<string> | undefined, value: string): boolean {
  return set?.has(value) ?? false;
}
