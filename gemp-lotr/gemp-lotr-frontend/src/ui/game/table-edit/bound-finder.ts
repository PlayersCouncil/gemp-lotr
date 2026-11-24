import { Injectable, signal } from '@angular/core';

/** The search window asked for to find a filter: what it is for, and where the filter found goes. */
export interface FindingBound {
  /** What the filter fills, in the window's title ("bound of ally", "pool"). */
  what: string;
  use: (filter: string) => void;
}

/**
 * The card search opened to find a deck builder filter rather than a card (the authoring panel's Find… beside a
 * slot's bound and the pool): what it is for while it is open. Its window is drawn by the page (gd-bound-finder),
 * outside the panel, so that it opens over the page as the edit controls' does. Page-scoped (ui).
 */
@Injectable()
export class BoundFinder {
  readonly finding = signal<FindingBound | null>(null);

  open(what: string, use: (filter: string) => void): void {
    this.finding.set({ what, use });
  }

  /** "Use as bound": the filter goes where it was asked for, and the window closes. */
  use(filter: string): void {
    const finding = this.finding();
    this.finding.set(null);
    finding?.use(filter);
  }

  close(): void {
    this.finding.set(null);
  }
}
