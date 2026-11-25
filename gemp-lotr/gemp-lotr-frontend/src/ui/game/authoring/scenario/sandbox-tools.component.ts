import { Component, computed, inject, input } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  optionsWithOtherSeat,
  optionsWithTool,
  otherSeatOptions,
  toolTicks,
} from '../../../../core/game/authoring/puzzle-settings';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { sendTop } from './top';

/** What each sandbox tool lets the player do (its tick's tooltip); a name the panel does not know says itself. */
const TOOL_HINTS: Readonly<Record<string, string>> = {
  add: 'Put a card into any zone',
  copy: 'Put a second copy of a card into its zone',
  delete: 'Take a card out of the game (not a discard)',
  move: 'Send a card to another zone',
  attach: 'Attach a card to another',
  stack: 'Stack a card on another',
  wounds: 'Add or heal wounds',
  tokens: 'Add or take away culture tokens',
  modify: 'Modify a stat or keyword for a time',
  counters: 'Set the twilight, the burdens and the threats',
  markers: "Move a seat's marker, control and liberate sites",
  sites: 'Put a site on the path',
  piles: 'Look in the hidden piles (hands, draw decks)',
};

/**
 * The Scenario tab's tools of a sandbox and who plays its other seat (es-notes.md 4: `options.tools`), under its Kind: a tick per tool the server
 * names, all ticked when the file does not say (every tool: what ticking the last one comes back to); one the server
 * has not built is shown and cannot be ticked. Each change sends the whole of `options`.
 *
 * DOM contract: `.gd-author-scenario__ticks[data-part=tools]` with `.gd-author-label`, a `gd-author-check[data-tool]`
 * per tool, and `.gd-author-note[data-part=all-tools]` while the file does not say; then (a sandbox's) who plays its
 * other seat (ef-notes.md A: `options.otherSeat`), `.gd-author-scenario__ticks[data-part=other-seat]` with
 * `gd-author-select[data-part=other-seat]` (script, bot, you: the server's choices) and
 * `.gd-author-note[data-part=seat-unsaid]` while the file does not say.
 */
@Component({
  selector: 'gd-game-author-sandbox-tools',
  imports: [AuthorCheckComponent, AuthorSelectComponent],
  template: `
    @if (state().sandboxTools; as tools) {
      <div class="gd-author-scenario__ticks" data-part="tools">
        <span class="gd-author-label" title="What the sandbox's player may change (the server refuses the rest)"
          >Tools</span
        >
        @for (tick of ticks(); track tick.name) {
          <gd-author-check
            [attr.data-tool]="tick.name"
            [label]="tick.name"
            [hint]="tick.notBuilt ? 'Not built yet' : (hints[tick.name] ?? tick.name)"
            [checked]="tick.on"
            [disabled]="tick.notBuilt"
            (changed)="tool(tick.name, $event)"
          />
        }
        @if (!tools.said) {
          <span class="gd-author-note" data-part="all-tools" title="The file does not say: every tool">(all)</span>
        }
      </div>
    }
    @if (state().sandboxOtherSeat; as seat) {
      @if (state().draft.kind === 'sandbox') {
        <div class="gd-author-scenario__ticks" data-part="other-seat">
          <span class="gd-author-label" title="Who plays the seat the person does not sit in">Other seat</span>
          <gd-author-select
            data-part="other-seat"
            label="Other seat"
            hint="Who plays the seat the person does not sit in"
            [options]="seatOptions()"
            [value]="seat.value"
            (changed)="otherSeat($event)"
          />
          @if (!seat.said) {
            <span class="gd-author-note" data-part="seat-unsaid" title="The file does not say: the script"
              >(script)</span
            >
          }
        </div>
      }
    }
  `,
})
export class SandboxToolsComponent {
  private readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly hints = TOOL_HINTS;
  protected readonly ticks = computed(() => {
    const tools = this.state().sandboxTools;
    return tools ? toolTicks(tools) : [];
  });

  protected readonly seatOptions = computed(() => otherSeatOptions(this.state().sandboxOtherSeat?.choices ?? []));

  /** Who plays the other seat: the whole of `options` sent. */
  protected otherSeat(choice: string): void {
    void sendTop(this.authoring, 'options', optionsWithOtherSeat(this.state().draft, choice));
  }

  protected tool(name: string, on: boolean): void {
    const state = this.state();
    if (!state.sandboxTools) return;
    void sendTop(this.authoring, 'options', optionsWithTool(state.draft, state.sandboxTools, name, on));
  }
}
