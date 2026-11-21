/**
 * Hands out the places of the hall's rows. The legacy hall appended every row to its section's <table> as it came,
 * so a section that holds several kinds of rows (Waiting Tables: queues and tables; Playing Tables: tournaments and
 * tables) shows them in the order they arrived, whatever their kind. The row states take their places from one
 * counter so their rows can be merged in that order.
 */
export class RowOrder {
  constructor(private next = 1) {}

  take(): number {
    return this.next++;
  }
}

/** Merges rows of several kinds by their places. */
export function byOrder<T extends { readonly order: number }>(...lists: readonly (readonly T[])[]): T[] {
  return lists.flat().sort((a, b) => a.order - b.order);
}
