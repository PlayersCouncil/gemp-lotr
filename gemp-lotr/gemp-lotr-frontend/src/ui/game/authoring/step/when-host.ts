import { WhenFacts } from '../../../../core/game/authoring/terms';

/**
 * What the When composer's pickers (gd-author-when-pickers, gd-author-asked, gd-author-role) work on: the text of a
 * When, an Until or a Skip when by its path, putting a picker's change into it, what is done once a picker has changed
 * it, and what the draft is (both seats taking turns, the game's own opening). The Step tab's form is one
 * (StepEditor: a change is sent at once); an opponent's order is another (its form is sent whole by Apply). Provide
 * it as `{ provide: WhenHost, useExisting: ... }`.
 */
export abstract class WhenHost {
  /** The text at a path ("when", "until", "skipWhen"), '' for none; null when nothing is being edited. Reads signals. */
  abstract whenText(path: string): string | null;
  /** A picker's change, put in at the path. */
  abstract putWhen(path: string, value: string): void;
  /** Once a picker has changed it: sent now, or nothing (sent with the rest). */
  abstract pickedWhen(): void;
  /** What the draft is. Reads signals. */
  abstract whenFacts(): WhenFacts;
}
