import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftSeat, DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  ruleNumber,
  rulesWithSeat,
  SeatRules,
  seatRulesOf,
  skipWith,
} from '../../../../core/game/authoring/scenario-settings';
import { PHASES } from '../../../../core/game/authoring/terms';
import {
  formatChoices,
  formatOf,
  leftoversLost,
  rulesOneSided,
  rulesWithMarkers,
} from '../../../../core/game/authoring/game-rules';
import { Notifier } from '../../../../core/notifier/notifier';
import { askAuthor } from '../widgets/author-confirm';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { sendTop } from './top';

const SEATS: readonly (readonly [DraftSeat, string])[] = [
  ['freeps', 'Free Peoples'],
  ['shadow', 'Shadow'],
];

/**
 * The Scenario tab's "Rules": the format it is played in (the scenarios' two, and any other the file names), the
 * usual rules a scenario changes (legacy renderRules): whether the Free Peoples take every turn (unticked, a game
 * of two sides: the markers no longer keep together, and leftovers to a character the opponent then cannot give them
 * to go, after the page's own question; and then whether
 * the Shadow seat's marker keeps level with the fellowship's), and per seat its hand size, the phases it skips, The One Ring's text switched off, and its minions'
 * roaming penalty. Each change sends the whole of `rules` (taken out when nothing is left in it); a seat's other
 * effects are kept as they are, and counted.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=rules]` with `gd-author-select[data-part=format]`,
 * `gd-author-check[data-part=one-sided]`, `gd-author-check[data-part=markers]`
 * and a `.gd-author-scenario__seat[data-seat]` per seat (`.gd-author-scenario__seathead`, `input[data-part]` hand,
 * roaming; `.gd-author-scenario__ticks[data-part=skip]` with a `gd-author-check[data-phase]` per phase,
 * `gd-author-check[data-part=ring]`, and `.gd-author-note[data-part=others]` when it has other effects).
 */
@Component({
  selector: 'gd-game-author-scenario-rules',
  imports: [AuthorCheckComponent, AuthorFieldComponent, AuthorSelectComponent, AuthorSendDirective],
  template: `
    <section class="gd-author-scenario__section" data-section="rules">
      <h3 class="gd-author-scenario__heading">Rules</h3>
      <gd-author-field label="Format" hint="The game's format: scenario_shadows is for a path of sites without numbers">
        <gd-author-select
          data-part="format"
          label="Format"
          [options]="formats()"
          [value]="format()"
          (changed)="setFormat($event)"
        />
      </gd-author-field>
      <div class="gd-author-scenario__settings">
        @for (shown of [kept()]; track shown) {
          <gd-author-check
            data-part="one-sided"
            label="one-sided: the Free Peoples take every turn"
            hint="The Shadow player never takes a turn of their own"
            [checked]="state().draft.rules?.oneSided === true"
            (changed)="oneSided($event)"
          />
        }
        <gd-author-check
          data-part="markers"
          label="markers keep together"
          hint="One-sided games: the Shadow seat's marker moves with the fellowship, so sites behind it can be controlled"
          [checked]="state().draft.rules?.markers === 'together'"
          [disabled]="state().draft.rules?.oneSided !== true"
          (changed)="markers($event)"
        />
      </div>
      @for (seat of seats(); track seat.seat) {
        <div class="gd-author-scenario__seat" [attr.data-seat]="seat.seat">
          <div class="gd-author-scenario__seathead">{{ seat.name }}</div>
          <gd-author-field label="Hand size ±" hint="Added to the usual 8: -3 makes it 5">
            <input
              type="number"
              class="gd-author-input gd-author-scenario__short"
              data-part="hand"
              [value]="seat.rules.hand"
              gdAuthorSend
              (sent)="change(seat.seat, { hand: number($event) })"
            />
          </gd-author-field>
          <div class="gd-author-scenario__ticks" data-part="skip">
            <span class="gd-author-label" title="Phases this seat's turns go without">Skips</span>
            @for (phase of phases; track phase) {
              <gd-author-check
                [attr.data-phase]="phase"
                [label]="phase"
                [checked]="seat.rules.skip.includes(phase)"
                (changed)="change(seat.seat, { skip: skip(seat.rules, phase, $event) })"
              />
            }
          </div>
          <div class="gd-author-scenario__settings">
            <gd-author-check
              data-part="ring"
              label="The One Ring's game text is switched off"
              [checked]="seat.rules.ring"
              (changed)="change(seat.seat, { ring: $event })"
            />
          </div>
          <gd-author-field
            label="Roaming ±"
            hint="Added to the roaming penalty of this seat's minions (the twilight for playing one whose site number is above the fellowship's), -2 takes 2 off it"
          >
            <input
              type="number"
              class="gd-author-input gd-author-scenario__short"
              data-part="roaming"
              [value]="seat.rules.roaming"
              gdAuthorSend
              (sent)="change(seat.seat, { roaming: number($event) })"
            />
          </gd-author-field>
          @if (seat.rules.others.length > 0) {
            <p
              class="gd-author-note"
              data-part="others"
              title="Under 'rules' in The rest of the file, below: kept as they are"
            >
              {{ seat.rules.others.length }} other effect{{ seat.rules.others.length === 1 ? '' : 's' }}, kept.
            </p>
          }
        </div>
      }
    </section>
  `,
})
export class RulesSectionComponent {
  protected readonly authoring = inject(Authoring);
  private readonly notifier = inject(Notifier);
  /** Bumped when the author says no to a change of sides: its tick is made afresh, as the draft has it. */
  protected readonly kept = signal(0);
  readonly state = input.required<DraftState>();
  protected readonly phases = PHASES;
  protected readonly seats = computed(() => {
    const rules = this.state().draft.rules;
    return SEATS.map(([seat, name]) => ({ seat, name, rules: seatRulesOf(rules, seat) }));
  });

  protected readonly formats = computed(() => formatChoices(this.state().draft.format));
  protected readonly format = computed(() => formatOf(this.state().draft));

  /**
   * One-sided or two: the markers keeping together go with a game of two sides; leftovers to a character the other
   * way's games cannot give them to go too, after the page's own question.
   */
  protected async oneSided(on: boolean): Promise<void> {
    const draft = this.state().draft;
    const rules = rulesOneSided(draft, on);
    const lost = leftoversLost(draft, on);
    if (lost === null) {
      void sendTop(this.authoring, 'rules', rules);
      return;
    }
    const question = `The opponent's leftovers to ${lost} go: in a ${on ? 'one-sided' : 'two-sided'} game they go to ${
      on ? 'the Free Peoples seat' : "the player's"
    } characters.`;
    if (!(await askAuthor(this.notifier, question, on ? 'One-sided' : 'Two-sided'))) {
      this.kept.update((count) => count + 1);
      return;
    }
    await this.authoring.edit({ top: { rules }, opponent: { unset: ['leftovers'] } });
  }

  protected markers(together: boolean): void {
    void sendTop(this.authoring, 'rules', rulesWithMarkers(this.state().draft, together));
  }

  protected setFormat(format: string): void {
    if (format !== this.format()) void sendTop(this.authoring, 'format', format);
  }

  protected change(seat: DraftSeat, change: Partial<Omit<SeatRules, 'others'>>): void {
    void sendTop(this.authoring, 'rules', rulesWithSeat(this.state().draft, seat, change));
  }

  protected number(typed: string): number {
    return ruleNumber(typed);
  }

  protected skip(rules: SeatRules, phase: string, on: boolean): string[] {
    return skipWith(rules, phase, on);
  }
}
