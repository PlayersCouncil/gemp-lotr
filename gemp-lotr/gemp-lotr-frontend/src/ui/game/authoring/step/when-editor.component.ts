import { Component, computed, inject } from '@angular/core';
import { isOpeningWork } from '../../../../core/game/authoring/step-model';
import { PHASES, pickersOf, putTerm, SEATS, TYPES, WhenPicker } from '../../../../core/game/authoring/terms';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { RepeatRowComponent } from './repeat-row.component';
import { StepEditor } from './step-editor';
import { StepFieldDirective } from './step-field.directive';

const WHEN_HINT =
  'seat, kind of decision, phase(...), turn(...), skirmish(n), inSkirmish(card), after(card), offers(card), text(part of what the decision says). ' +
  "Or written out in full, as the file has it, in HJSON: { seat: freeps, require: { ... } }. What is copied out of a file can be pasted here, with or without 'when:' in front.";
const REQUIRE_HINT =
  'Requirements as cards write them, in HJSON as the file has them (one { ... } or a [ list ], a line to each part), which have to hold as well as the When. ' +
  'For instance, type: canSpot and filter: or(helm,shield,armor), attachedTo(inSkirmishAgainst(alias(balrog))). Names in the scenario are alias(name).';

/**
 * What a step waits for (legacy buildFields' first section): its heading by what kind of step it is; for the
 * opening, only its name; otherwise the composer (seat, kind of decision, phase, turn: each puts its term into the
 * When, keeping the others, in the order a person says them; hidden for a When written out in full), the When, its
 * requirements (Only while), the step's flags (pause, once, checkpoint, its name), "repeat until", and If / Unless
 * when the draft's pauses have choices.
 *
 * DOM contract: `gd-author-when` holding `.gd-author-step__section` with `.gd-author-step__heading`,
 * `.gd-author-when__pickers` (`gd-author-select[data-picker]` seat, kind, phase; `input[data-picker=turn]`), the
 * fields (`data-field` when, whenRequire, id, if, unless), `.gd-author-when__flags` (`input[data-flag]` pause, once,
 * checkpoint) and `gd-author-repeat`.
 */
@Component({
  selector: 'gd-author-when',
  styleUrl: './when-editor.component.css',
  imports: [AuthorFieldComponent, AuthorSelectComponent, HjsonAreaDirective, StepFieldDirective, RepeatRowComponent],
  template: `
    @if (view(); as view) {
      <div class="gd-author-step__section">
        <div class="gd-author-step__heading" [title]="view.heading[1]">{{ view.heading[0] }}</div>
        @if (view.kind === 'block') {
          <div
            class="gd-author-note"
            title="Every decision that fits this is put to the steps below, from where the script comes to the block until it ends. Empty: every decision there is."
          >
            Empty: every decision.
          </div>
        }
        @if (view.opening) {
          <div
            class="gd-author-note"
            title="Read before the first turn begins, on the board as the scenario lays it out: nobody has been asked anything, and nothing has been drawn. Its messages are read one after another; then what it does is done; then the first turn begins. Only a scenario's first step can be this."
          >
            Read before the first turn.
          </div>
          <gd-author-field label="name" hint="Only to make reports easier to read">
            <input class="gd-author-input gd-author-step__short" gdStepField="id" />
          </gd-author-field>
        } @else {
          @if (view.pickers; as pickers) {
            <div class="gd-author-when__pickers">
              <gd-author-select
                data-picker="seat"
                label="Seat"
                [options]="seats"
                [value]="pickers.seat"
                (changed)="pick('seat', $event)"
              />
              <gd-author-select
                data-picker="kind"
                label="Kind of decision"
                [options]="kinds"
                [value]="pickers.kind"
                (changed)="pick('kind', $event)"
              />
              <gd-author-select
                data-picker="phase"
                label="Phase"
                [options]="phases"
                [value]="pickers.phase"
                (changed)="pick('phase', $event)"
              />
              <input
                type="number"
                min="1"
                class="gd-author-input gd-author-step__short"
                data-picker="turn"
                placeholder="turn"
                aria-label="Turn"
                [value]="pickers.turn"
                (change)="pick('turn', $any($event.target).value)"
              />
            </div>
          }
          <gd-author-field label="When" [hint]="whenHint">
            <textarea gdHjsonArea gdStepField="when" rows="1" [text]="view.when"></textarea>
          </gd-author-field>
          @if (view.require) {
            <gd-author-field label="Only while" [hint]="requireHint">
              <textarea
                gdHjsonArea
                gdStepField="whenRequire"
                rows="1"
                placeholder="{ type: canSpot, filter: alias(smith), minVitality(2) }"
                [text]="view.whenRequire"
              ></textarea>
            </gd-author-field>
          }
          <div class="gd-author-when__flags">
            @if (view.kind !== 'block') {
              <label
                class="gd-author-check"
                [title]="
                  view.kind === 'child'
                    ? 'A step that is only read: it has messages, and no answer. It is read once, and the decision then goes to whichever of the block’s other steps fits it.'
                    : 'A step that is only read: it has messages, and no answer. The decision goes to the step after it.'
                "
              >
                <input type="checkbox" data-flag="pause" [checked]="view.pause" (change)="tick('pause', $event)" />
                pause
              </label>
            }
            @if (view.kind === 'child' && !view.pause) {
              <label
                class="gd-author-check"
                title="It applies to one decision, and is then done with. Without this it applies to every decision that fits it, for as long as the block lasts (and what it says is said the first time)."
              >
                <input type="checkbox" data-flag="once" [checked]="view.once" (change)="tick('once', $event)" />
                once
              </label>
            }
            @if (view.kind !== 'child') {
              <label class="gd-author-check" title="A player who fails later can come back to here">
                <input
                  type="checkbox"
                  data-flag="checkpoint"
                  [checked]="view.checkpoint"
                  (change)="tick('checkpoint', $event)"
                />
                checkpoint
              </label>
            }
            <gd-author-field label="name" hint="Only to make reports easier to read">
              <input class="gd-author-input gd-author-step__short" gdStepField="id" />
            </gd-author-field>
          </div>
          @if (view.kind === 'step' && !view.pause) {
            <gd-author-repeat />
          }
          @if (view.flags !== '') {
            <gd-author-field
              label="If"
              [hint]="'Only part of the game if these were chosen at a pause’s choice: ' + view.flags"
            >
              <input class="gd-author-input" gdStepField="if" />
            </gd-author-field>
            <gd-author-field label="Unless" hint="Not part of the game if any of these was chosen">
              <input class="gd-author-input" gdStepField="unless" />
            </gd-author-field>
          }
        }
      </div>
    }
  `,
})
export class WhenEditorComponent {
  protected readonly editor = inject(StepEditor);
  protected readonly whenHint = WHEN_HINT;
  protected readonly requireHint = REQUIRE_HINT;
  protected readonly seats: readonly AuthorOption[] = [['', 'either seat'], ...SEATS];
  protected readonly kinds: readonly AuthorOption[] = [['', 'any kind'], ...TYPES];
  protected readonly phases: readonly AuthorOption[] = [
    ['', 'any phase'],
    ...PHASES,
    ['between_turns', 'between turns'],
  ];

  protected readonly view = computed(() => {
    const work = this.editor.work();
    const place = this.editor.place();
    const draft = this.editor.authoring.draft();
    if (!work || !place || !draft) return null;
    const kind = place.kind;
    const pause = work.pause === true;
    const heading: readonly [string, string] = isOpeningWork(work)
      ? ['The opening', '']
      : kind === 'block'
        ? ['Covers the decisions that are', '']
        : pause
          ? kind === 'child'
            ? ['Read once, over the first decision that is', "Read once, over the player's first decision that is"]
            : ['Read over the decision that is', "Read over the player's decision when it is"]
          : kind === 'child'
            ? work.once
              ? ['Applies to one decision that is', '']
              : ['Applies to each decision that is', '']
            : ['Applies to the decision that is', ''];
    return {
      kind,
      heading,
      opening: isOpeningWork(work),
      pickers: pickersOf(work.when),
      when: work.when ?? '',
      whenRequire: work.whenRequire ?? '',
      require: !(work.when ?? '').trim().startsWith('{'),
      pause,
      once: work.once === true,
      checkpoint: work.checkpoint === true,
      flags: draft.flags.join(', '),
    };
  });

  /** A picker changed: its term is put into the When, in its place. */
  protected pick(picker: WhenPicker, value: string): void {
    this.editor.put('when', putTerm(this.editor.work()?.when, picker, value.trim()));
    this.editor.now();
  }

  protected tick(flag: 'pause' | 'once' | 'checkpoint', event: Event): void {
    this.editor.put(flag, (event.target as HTMLInputElement).checked);
    this.editor.now();
  }
}
