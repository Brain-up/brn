export function isNotEmptyString(value: unknown): boolean {
  return typeof value === 'string' && value.trim().length > 0;
}

export function isBornYearValid(bYear: string): boolean {
  const maxDate = new Date().getFullYear();
  const minDate = new Date().getFullYear() - 100;
  const year = parseInt(bYear, 10);
  if (bYear.length === 4) {
    return year <= maxDate && year >= minDate;
  }

  return false;
}
