import { Component, computed, inject, signal } from '@angular/core';
import { DraftSeat } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { behind, controlledNumbers, SKIRMISH_PHASES, StartPart } from '../../../../core/game/authoring/board-start';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';

/** The numbers typed in. */
type Typed = 'turn' | 'twilight' | 'moves';

const SEAT_NAMES: Readonly<Record<DraftSeat, string>> = { freeps: 'Free Peoples', shadow: 'Shadow' };

/**
 * The Board tab's "Where the story starts" (setup.startAt and what goes with it): the fellowship's site, the Shadow
 * seat's marker (level with the fellowship while the markers keep together), the phase, the turn, the twilight pool,
 * the moves made this turn (only where they can be other than the phase's own), in the Assignment and Skirmish
 * phases the skirmishes already assigned (a Free Peoples character of the setup's and the setup's minions), a tick
 * per site behind both markers for who controls it, and under a fold whose site is laid at each place. A change is
 * gathered with the tab's others (a line each in the bar) and made when they are submitted; a part at its default
 * leaves the file. A start other than the very start of a game is said to be a late one. What the author types
 * stays in its field until it is gathered (Enter, or leaving the field), whatever the state that comes meanwhile
 * says. What the server said of a refused start is said here too.
 *
 * DOM contract: `gd-author-board-start` holding `.gd-author-board__heading` (with `.gd-author-board-start__late`
 * for a late start), a `.gd-author-board-start__row` per part (`data-part`: site, opponentSite, phase, turn,
 * twilight, moves; `data-changed`) with `.gd-author-label` and `.gd-author-board-start__pick` holding `select.gd-author-input`, or
 * `input[type=number].gd-author-input`; `.gd-author-board-start__fights` with a `.gd-author-board-start__fight` per
 * character (`data-character`) holding `.gd-author-board-start__minion` (`data-minion`, `button[data-action=unfight]`)
 * and `select[data-control=fight]`; `.gd-author-board-start__held` with a `.gd-author-board-start__seat` per seat
 * (`data-seat`) of `gd-author-check[data-site]`; `details.gd-author-board-start__fold` with a
 * `.gd-author-board-start__row[data-part=path][data-site]` per place; `p.gd-author-board-start__problem` when a
 * submit was refused.
 */
@Component({
  selector: 'gd-author-board-start',
  styleUrl: './board-start.component.css',
  imports: [AuthorButtonDirective, AuthorCheckComponent, AuthorSelectComponent],
  template: `
    @if (changes.startView(); as view) {
      <div class="gd-author-board__heading">
        Where the story starts
        @if (view.late) {
          <span
            class="gd-author-board-start__late"
            title="The game is carried on from here: nothing earlier in the turn happens (no start of turn, no move to this site)"
            >late start</span
          >
        }
      </div>
      <div class="gd-author-board-start__row" data-part="site" [attr.data-changed]="mark('site')">
        <span class="gd-author-label">Site</span>
        <span class="gd-author-board-start__pick">
          <gd-author-select
            label="Site"
            hint="The fellowship's site, whose turn it is"
            [options]="sites()"
            [value]="'' + view.site"
            (changed)="changes.setStart('site', +$event)"
          />
        </span>
      </div>
      <div class="gd-author-board-start__row" data-part="opponentSite" [attr.data-changed]="mark('opponentSite')">
        <span class="gd-author-label">Shadow marker</span>
        <span class="gd-author-board-start__pick">
          <gd-author-select
            label="Shadow marker"
            [hint]="view.markersTogether ? togetherHint : markerHint"
            [options]="sites()"
            [value]="'' + view.opponentSite"
            [disabled]="view.markersTogether"
            (changed)="changes.setStart('opponentSite', +$event)"
          />
        </span>
      </div>
      <div class="gd-author-board-start__row" data-part="phase" [attr.data-changed]="mark('phase')">
        <span class="gd-author-label">Phase</span>
        <span class="gd-author-board-start__pick">
          <gd-author-select
            label="Phase"
            hint="The phase play begins in"
            [options]="view.phases"
            [value]="view.phase"
            (changed)="changes.setStart('phase', $event)"
          />
        </span>
      </div>
      @for (field of numbers(); track field.part) {
        <div class="gd-author-board-start__row" [attr.data-part]="field.part" [attr.data-changed]="mark(field.part)">
          <span class="gd-author-label">{{ field.label }}</span>
          <input
            class="gd-author-input"
            type="number"
            [min]="field.min"
            [attr.aria-label]="field.label"
            [title]="field.hint"
            [value]="typed()[field.part] ?? view[field.part]"
            (input)="type(field.part, $any($event.target).value)"
            (change)="put(field.part)"
            (keydown.enter)="put(field.part)"
          />
        </div>
      }
      @if (fighting()) {
        <div class="gd-author-board-start__fights">
          <span class="gd-author-label" title="Already assigned when play begins">Skirmishes</span>
          @for (character of changes.fightCards().characters; track character) {
            <div class="gd-author-board-start__fight" [attr.data-character]="character">
              <span class="gd-author-board-start__name" [title]="nameOf(character)">{{ character }}</span>
              @for (minion of view.skirmishes[character] ?? []; track minion) {
                <span class="gd-author-board-start__minion" [attr.data-minion]="minion" [title]="nameOf(minion)"
                  >{{ minion }}
                  <button
                    gdAuthorButton="inline"
                    data-action="unfight"
                    title="Out of this skirmish"
                    (click)="changes.setFight(character, minion, false)"
                  >
                    ×
                  </button></span
                >
              }
              @if (free().length > 0) {
                <select
                  class="gd-author-input"
                  data-control="fight"
                  aria-label="Minion"
                  title="A minion the setup puts on the table, into this skirmish"
                  (change)="fight(character, $event)"
                >
                  <option value="" selected>+ minion</option>
                  @for (minion of free(); track minion) {
                    <option [value]="minion">{{ minion }}</option>
                  }
                </select>
              }
            </div>
          } @empty {
            <span class="gd-author-note">The setup puts no character on the table.</span>
          }
        </div>
      }
      @if (past().length > 0) {
        <div class="gd-author-board-start__held">
          <span class="gd-author-label" title="Sites every marker is past can be controlled when play begins"
            >Controlled</span
          >
          @for (row of held(); track row.seat) {
            <div
              class="gd-author-board-start__seat"
              [attr.data-seat]="row.seat"
              [attr.data-changed]="mark('controlled')"
            >
              <span class="gd-author-board-start__name">{{ row.name }}</span>
              @for (site of row.sites; track site.number) {
                <gd-author-check
                  [attr.data-site]="site.number"
                  [label]="'' + site.number"
                  [hint]="'Site ' + site.number + ' is controlled by the ' + row.name + ' seat when play begins'"
                  [checked]="site.on"
                  (changed)="changes.setControl(row.seat, site.number, $event)"
                />
              }
            </div>
          }
        </div>
      }
      <details class="gd-author-board-start__fold" [open]="open()" (toggle)="open.set($any($event.target).open)">
        <summary title="Which seat's site is laid at each place (setup.path)" [attr.data-changed]="mark('path')">
          Whose sites
        </summary>
        @for (place of places(); track place.number) {
          <div class="gd-author-board-start__row" data-part="path" [attr.data-site]="place.number">
            <span class="gd-author-label">Site {{ place.number }}</span>
            <span class="gd-author-board-start__pick">
              <gd-author-select
                [label]="'Site ' + place.number"
                hint="The site laid at this place on the path"
                [options]="place.options"
                [value]="place.value"
                (changed)="changes.setPathSite(place.number, $event)"
              />
            </span>
          </div>
        }
      </details>
      @if (changes.startRefused(); as said) {
        <p class="gd-author-note gd-author-board-start__problem" data-problem>{{ said }}</p>
      }
    }
  `,
})
export class BoardStartComponent {
  protected readonly changes = inject(BoardChanges);
  /** What has been typed in each number field and not gathered yet. */
  protected readonly typed = signal<Partial<Record<Typed, string>>>({});
  protected readonly togetherHint = "The markers keep together (the Scenario tab's rules): level with the fellowship";
  protected readonly markerHint = "Where the Shadow seat's marker is";
  /** The "Whose sites" fold is open. */
  protected readonly open = signal(false);

  /** 1 to the last site any seat's path has (and the site said, if it is further). */
  protected readonly sites = computed<AuthorOption[]>(() => {
    const view = this.changes.startView();
    const last = Math.max(view?.lastSite ?? 1, view?.site ?? 1, view?.opponentSite ?? 1);
    return Array.from({ length: last }, (_, at) => String(at + 1));
  });

  /** The number fields: moves only where they can be other than the phase's own (or the file says them). */
  protected readonly numbers = computed(() => {
    const view = this.changes.startView();
    const fields: { part: Typed; label: string; hint: string; min: number }[] = [
      { part: 'turn', label: 'Turn', hint: 'The game’s turn counter, and what turn(n) means', min: 1 },
      { part: 'twilight', label: 'Twilight', hint: 'The twilight pool when play begins', min: 0 },
    ];
    if (view && (view.movesSet || (view.phase !== 'fellowship' && view.site >= 3))) {
      fields.push({
        part: 'moves',
        label: 'Moves',
        hint: 'Moves the fellowship has made this turn (1 unless it moved on again)',
        min: 1,
      });
    }
    return fields;
  });

  /** Skirmishes can be assigned already. */
  protected readonly fighting = computed(() => SKIRMISH_PHASES.includes(this.changes.startView()?.phase ?? ''));

  /** The minions in no skirmish. */
  protected readonly free = computed(() => {
    const view = this.changes.startView();
    const fighting = new Set(Object.values(view?.skirmishes ?? {}).flat());
    return this.changes.fightCards().minions.filter((minion) => !fighting.has(minion));
  });

  /** The sites behind both markers. */
  protected readonly past = computed(() => {
    const view = this.changes.startView();
    return view ? behind(view) : [];
  });

  /** A row of ticks per seat: the Shadow seat's, and the Free Peoples' in a game of two sides (or if it has some). */
  protected readonly held = computed(() => {
    const view = this.changes.startView();
    if (!view) return [];
    const twoSided = !this.changes.oneSided();
    const seats: DraftSeat[] =
      twoSided || (view.controlled.freeps ?? []).length > 0 ? ['shadow', 'freeps'] : ['shadow'];
    return seats.map((seat) => {
      const on = controlledNumbers(view, seat, this.changes.board());
      return {
        seat,
        name: SEAT_NAMES[seat],
        sites: this.past().map((number) => ({ number, on: on.has(number) })),
      };
    });
  });

  /** The places of the path the start lays, each with the sites it can be. */
  protected readonly places = computed(() => {
    const view = this.changes.startView();
    const board = this.changes.board();
    if (!view || !board) return [];
    const length = Math.max(view.site, view.opponentSite, view.path.length);
    const all = [...board.sites.freeps, ...board.sites.shadow].flatMap((site) =>
      site.alias ? [{ ...site, alias: site.alias }] : [],
    );
    return Array.from({ length }, (_, at) => {
      const own = board.sites.freeps[at];
      const said = view.path[at];
      const value = said === undefined || said === own?.alias ? '' : said;
      const options: AuthorOption[] = [
        ['', own ? `${own.alias} (${own.name ?? own.id})` : 'the Free Peoples seat’s'],
        ...all
          .filter((site) => site.alias !== own?.alias)
          .map((site): AuthorOption => [site.alias, `${site.alias} (${site.name ?? site.id})`]),
      ];
      return { number: at + 1, value, options };
    });
  });

  protected mark(part: StartPart): '' | null {
    return this.changes.startView()?.changed.has(part) ? '' : null;
  }

  protected nameOf(alias: string): string {
    return this.changes.board()?.cards.find((card) => card.alias === alias)?.name ?? alias;
  }

  protected type(part: Typed, value: string): void {
    this.typed.update((typed) => ({ ...typed, [part]: value }));
  }

  /** What was typed in a field, gathered (nothing typed, or no number: nothing). */
  protected put(part: Typed): void {
    const text = this.typed()[part];
    if (text === undefined) return;
    this.typed.update((typed) => ({ ...typed, [part]: undefined }));
    const value = parseInt(text, 10);
    if (Number.isNaN(value)) return;
    this.changes.setStart(part, value);
  }

  protected fight(character: string, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const minion = select.value;
    select.value = '';
    if (minion !== '') this.changes.setFight(character, minion, true);
  }
}
