import { DOCUMENT } from '@angular/common';
import { DestroyRef, inject, signal } from '@angular/core';
import { labelPlace, PickHit } from '../../../core/game/authoring/picking';
import { Rect } from '../../../core/game/scenario/bubble-placement';

export type { PickHit } from '../../../core/game/authoring/picking';

/** What the pointer is on while picking: what a click there would take, the box drawn round it, and its label's place. */
export interface PickHover {
  anchor: string;
  /** What the label says, where it is not the anchor itself (a card's name). */
  text?: string;
  box: Rect;
  label: { left: number; top: number };
}

/** The presses that would do something on the table, kept from it while picking. */
const SWALLOWED = ['mousedown', 'mouseup', 'pointerdown', 'pointerup', 'dblclick', 'contextmenu', 'auxclick'] as const;

/**
 * Picking something on the game table with the pointer (the legacy scenarioAuthor.js pick): while it lasts the pointer
 * is a crosshair, the thing under it is outlined with its name beside it (`gd-game-author-pickbox` draws them from
 * `hover`), and nothing on the table hears a press. A click takes what it is on (`thingAt`); a click on nothing, in
 * the place that `outside` names, Escape, `cancel` or another pick ends it with nothing.
 *
 * Shared by the authoring panel (its TablePicker: anchors for a message) and the on-table edit controls (their
 * CardPicker: a card to attach to or stack on); each says what a point holds and what it does while picking. The
 * page-wide listeners (capture phase, on the document) are there only while picking.
 */
export abstract class TablePick {
  protected readonly document = inject(DOCUMENT);

  private readonly active = signal(false);
  /** A pick is under way. */
  readonly picking = this.active.asReadonly();
  private readonly under = signal<PickHover | null>(null);
  /** What the pointer is on while picking, or null. */
  readonly hover = this.under.asReadonly();

  private done: ((anchor: string | null) => void) | null = null;
  private readonly listeners: readonly [string, (event: Event) => void][] = [
    ['mousemove', (event) => this.moved(event as MouseEvent)],
    ['click', (event) => this.clicked(event as MouseEvent)],
    ['keydown', (event) => this.key(event as KeyboardEvent)],
    ...SWALLOWED.map((name) => [name, (event: Event) => this.swallow(event)] as [string, (event: Event) => void]),
  ];

  constructor() {
    inject(DestroyRef).onDestroy(() => this.finish(null));
  }

  /** What a click at a point of the window would take, and the box round it (and its label); null for nothing. */
  abstract thingAt(x: number, y: number): (PickHit & { text?: string }) | null;

  /** A pick has begun, telling the person `told` (the authoring panel's notice; the edit bar). */
  protected abstract began(told: string): void;

  /** The pick has ended. */
  protected abstract ended(): void;

  /** Where a click takes nothing and ends the pick (the panel picked for), as a selector; null: nowhere. */
  protected outside(): string | null {
    return null;
  }

  /**
   * Lets the person click something on the table; resolves to what it took, or null when nothing was taken (Escape, a
   * click on nothing or outside, `cancel`, another pick).
   */
  protected start(told: string): Promise<string | null> {
    this.finish(null);
    return new Promise((resolve) => {
      this.done = resolve;
      this.active.set(true);
      this.began(told);
      for (const [name, listener] of this.listeners) this.document.addEventListener(name, listener, true);
    });
  }

  /** Ends a pick under way with nothing. */
  cancel(): void {
    this.finish(null);
  }

  private finish(anchor: string | null): void {
    const done = this.done;
    if (!done) return;
    this.done = null;
    for (const [name, listener] of this.listeners) this.document.removeEventListener(name, listener, true);
    this.active.set(false);
    this.under.set(null);
    this.ended();
    done(anchor);
  }

  private isOutside(event: Event): boolean {
    const outside = this.outside();
    return outside !== null && event.target instanceof Element && event.target.closest(outside) !== null;
  }

  private moved(event: MouseEvent): void {
    const hit = this.isOutside(event) ? null : this.thingAt(event.clientX, event.clientY);
    this.under.set(hit ? { ...hit, label: labelPlace(hit.box) } : null);
  }

  private clicked(event: MouseEvent): void {
    this.swallow(event);
    const hit = this.isOutside(event) ? null : this.thingAt(event.clientX, event.clientY);
    this.finish(hit?.anchor ?? null);
  }

  private key(event: KeyboardEvent): void {
    if (event.key !== 'Escape') return;
    this.swallow(event);
    this.finish(null);
  }

  private swallow(event: Event): void {
    event.preventDefault();
    event.stopImmediatePropagation();
  }
}
