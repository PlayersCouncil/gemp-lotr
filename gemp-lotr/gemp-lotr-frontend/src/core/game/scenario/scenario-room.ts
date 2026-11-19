import { Injectable, signal } from '@angular/core';

/**
 * The room the game page has: a panel docked at its right edge (the scenario authoring panel) takes some of the
 * window, and what is laid out against the window (the bubbles, the table itself) leaves it that room. Page-scoped.
 */
@Injectable()
export class ScenarioRoom {
  /** How much of the window's right is taken (px). */
  readonly reservedRight = signal(0);
}
