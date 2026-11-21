/** The first of `values` that is not empty or missing, else "" (the legacy page's `a || b || c` on texts). */
export function firstText(...values: (string | null | undefined)[]): string {
  for (const value of values) {
    if (value !== null && value !== undefined && value !== '') {
      return value;
    }
  }
  return '';
}
