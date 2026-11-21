import { signal } from '@angular/core';

/**
 * The (i) toggles of a Play form (TableFlow.bindInfoToggles): each shows or hides its help text below its row. All
 * start hidden.
 */
export class HelpToggles {
  private readonly shown_ = signal<ReadonlySet<string>>(new Set());

  shown(id: string): boolean {
    return this.shown_().has(id);
  }

  toggle(id: string): void {
    this.shown_.update((shown) => {
      const next = new Set(shown);
      if (!next.delete(id)) {
        next.add(id);
      }
      return next;
    });
  }

  hideAll(): void {
    this.shown_.set(new Set());
  }
}

/** The value of a `<select>` or text field from its event. */
export function fieldValue(event: Event): string {
  return (event.target as HTMLInputElement | HTMLSelectElement).value;
}

/** Whether a checkbox is checked, from its event. */
export function checked(event: Event): boolean {
  return (event.target as HTMLInputElement).checked;
}
