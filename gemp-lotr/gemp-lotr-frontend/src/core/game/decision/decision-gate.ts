import { Injectable } from '@angular/core';
import { DecisionButton } from './decision-view';

/**
 * Something the player does to answer the waiting decision, as GameDecisions is about to carry it out: a card
 * clicked, an action taken (a card's only one, or one of its menu), a button, a number or a choice given, Escape.
 */
export type DecisionGesture =
  | { kind: 'card'; cardId: string }
  | { kind: 'action'; actionId: string; cardId: string }
  | { kind: 'button'; button: DecisionButton }
  | { kind: 'number'; value: string }
  | { kind: 'choice'; index: number }
  | { kind: 'escape' };

/**
 * Somebody with a say in what the player may do about the waiting decision: a scripted game that takes only the
 * answers its step allows, a request (to take the game back, say) that freezes the decision until it is settled.
 */
export interface DecisionGate {
  /**
   * Whether the gesture may go ahead. A gate that refuses says so to the player itself (a hint, a shake of what
   * has to be read first); nothing happens on the page otherwise.
   */
  allows(gesture: DecisionGesture): boolean;
  /** The gesture went ahead and the page has acted on it (the player's choices, or the answer sent, are as it left them). */
  after?(gesture: DecisionGesture): void;
}

/**
 * The gates of this page's game. GameDecisions asks them before every gesture and tells them after it; whoever
 * needs a say adds a gate (and takes it away again with what `add` returns). A gesture goes ahead only if every gate
 * allows it; the first one that refuses is the one that tells the player. Page-scoped.
 */
@Injectable()
export class DecisionGates {
  private gates: readonly DecisionGate[] = [];

  /** Adds a gate; the function returned removes it. */
  add(gate: DecisionGate): () => void {
    this.gates = [...this.gates, gate];
    return () => {
      this.gates = this.gates.filter((other) => other !== gate);
    };
  }

  allows(gesture: DecisionGesture): boolean {
    return this.gates.every((gate) => gate.allows(gesture));
  }

  after(gesture: DecisionGesture): void {
    for (const gate of this.gates) gate.after?.(gesture);
  }
}
