import { DOCUMENT } from '@angular/common';
import { Component, computed, DestroyRef, effect, inject, ViewEncapsulation } from '@angular/core';
import { AuthoringTab } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { whereLine } from '../../../core/game/authoring/notices';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';
import { PAGE_RIGHT_PROPERTY } from '../../shared/page-room';
import { AuthoringLeaveComponent } from './authoring-leave.component';
import { AuthoringNoticesComponent } from './authoring-notices.component';
import { BoardTabComponent } from './board-tab.component';
import { OpponentTabComponent } from './opponent-tab.component';
import { ScenarioTabComponent } from './scenario-tab.component';
import { StepTabComponent } from './step-tab.component';
import { TimelineTabComponent } from './timeline-tab.component';
import { AuthorButtonDirective } from './widgets/author-button.directive';
import { PickBoxComponent } from './pick-box.component';
import { TablePicker } from './table-picker.service';
import { TablePick } from '../table-edit/table-pick';

/** The panel's tabs, in order, with what their buttons say. */
export const AUTHOR_TABS: readonly (readonly [AuthoringTab, string])[] = [
  ['timeline', 'Timeline'],
  ['step', 'Step'],
  ['opponent', 'Opponent'],
  ['board', 'Board'],
  ['scenario', 'Scenario'],
];

/**
 * The scenario authoring panel (the legacy scenarioAuthor.js): docked down the window's right while a scenario is
 * written as it is played, the game table leaving it the room (ScenarioRoom, and `--gd-page-right` on the document
 * element for what is laid out against the window). Its head (fold, the draft's title with a star while it has
 * changes in no file, a mark while calls are on their way, Save), where the game is in the script, the notices, and
 * the tabs. In a player's game of the draft (a test) it floats small in a corner and offers only the way back; with
 * the draft gone, or moved on to another game, it says so. Keys typed in it are its own: neither the page's
 * shortcuts nor the scenario's hear them. Everything it shows and does is the Authoring service's.
 *
 * Its stylesheet is not scoped: it also draws the widgets the tabs share (widgets/, `.gd-author-button`,
 * `.gd-author-input`, `.gd-author-code`, `.gd-author-field`, `.gd-author-check`, `.gd-author-note`, `.gd-author-bar`);
 * every rule starts at `gd-game-author`.
 *
 * DOM contract: `gd-game-author` holding `section.gd-author` (`data-own-keys`; `data-view`: none, elsewhere, test,
 * author; `data-folded`, `data-floating`, `data-unsaved`, `data-wanted` (the opponent is held for the author)) with
 * `header.gd-author__head` (`button.gd-author__fold`, `.gd-author__title`, `.gd-author__busy` while calls are on their
 * way, `button.gd-author__edit-table[data-author=edit-table]` with `aria-pressed` (the table's own edit controls on:
 * AuthorBackend), `button.gd-author__save` with `data-dirty`), `.gd-author__where`, `gd-game-author-notices`, then for the tabs
 * `nav.gd-author__tabs` with a `button.gd-author__tab` per tab (`data-tab`, `aria-selected`, `data-attention`) and
 * `.gd-author__body` holding the tab shown (`gd-game-author-timeline`, `-step`, `-opponent`, `-board`, `-scenario`);
 * for a test `.gd-author__body` with `.gd-author-note` and `button[data-author=resume]`, `button[data-author=test-again]`;
 * elsewhere `.gd-author__body` with `.gd-author-note` and `button[data-author=go]` (the draft's game). The leave dialog
 * is `gd-game-author-leave`. After the section, `gd-game-author-pickbox` (picking on the table, TablePicker).
 */
@Component({
  selector: 'gd-game-author',
  styleUrl: './authoring-panel.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [
    AuthorButtonDirective,
    AuthoringNoticesComponent,
    AuthoringLeaveComponent,
    TimelineTabComponent,
    StepTabComponent,
    OpponentTabComponent,
    BoardTabComponent,
    ScenarioTabComponent,
    PickBoxComponent,
  ],
  providers: [TablePicker, { provide: TablePick, useExisting: TablePicker }],
  host: {
    style: 'display: contents',
    // Typing in the panel is not for the page's shortcuts to hear (the scenario's skip [data-own-keys])
    '(keydown)': '$event.stopPropagation()',
    '(keyup)': '$event.stopPropagation()',
    '(keypress)': '$event.stopPropagation()',
  },
  template: `
    <section
      class="gd-author"
      aria-label="Scenario authoring"
      data-own-keys
      [attr.data-view]="authoring.view()"
      [attr.data-folded]="dock().folded ? '' : null"
      [attr.data-floating]="dock().floating ? '' : null"
      [attr.data-unsaved]="unsaved() ? '' : null"
      [attr.data-wanted]="authoring.wanted() ? '' : null"
    >
      <header class="gd-author__head">
        @if (!dock().floating) {
          <button
            gdAuthorButton
            class="gd-author__fold"
            [title]="dock().folded ? 'Open the authoring panel' : 'Fold the panel away'"
            [attr.aria-expanded]="!dock().folded"
            (click)="authoring.fold()"
          >
            {{ dock().folded ? '«' : '»' }}
          </button>
        }
        <span class="gd-author__title" [title]="unsaved() ? 'Changed since it was saved' : ''">{{ title() }}</span>
        @if (authoring.busy() > 0) {
          <span class="gd-author__busy" role="status" aria-label="Working">…</span>
        }
        @if (authoring.view() === 'author') {
          <button
            [gdAuthorButton]="author.editing() ? 'primary' : ''"
            class="gd-author__edit-table"
            data-author="edit-table"
            [attr.aria-pressed]="author.editing()"
            [title]="
              author.editing()
                ? 'Back to playing: the table answers decisions again'
                : 'Change the board on the table itself (right-click a card, the + spots): nothing is answered meanwhile'
            "
            (click)="author.toggle()"
          >
            Edit table
          </button>
        }
        @if (authoring.view() === 'author') {
          <button
            gdAuthorButton
            class="gd-author__save"
            title="Write the draft to its file"
            [attr.data-dirty]="unsaved() ? '' : null"
            (click)="authoring.save()"
          >
            Save
          </button>
        }
      </header>
      <div class="gd-author__where">{{ where() }}</div>
      <gd-game-author-notices />
      @switch (authoring.view()) {
        @case ('author') {
          <nav class="gd-author__tabs" aria-label="Authoring">
            @for (tab of tabs; track tab[0]) {
              <button
                type="button"
                class="gd-author__tab"
                [attr.data-tab]="tab[0]"
                [attr.aria-selected]="authoring.ui().tab === tab[0]"
                [attr.data-attention]="authoring.attention().has(tab[0]) ? '' : null"
                (click)="authoring.showTab(tab[0])"
              >
                {{ tab[1] }}
              </button>
            }
          </nav>
          <div class="gd-author__body">
            @switch (authoring.ui().tab) {
              @case ('timeline') {
                <gd-game-author-timeline />
              }
              @case ('step') {
                <gd-game-author-step />
              }
              @case ('opponent') {
                <gd-game-author-opponent />
              }
              @case ('board') {
                <gd-game-author-board />
              }
              @case ('scenario') {
                <gd-game-author-scenario />
              }
            }
          </div>
        }
        @case ('test') {
          <div class="gd-author__body">
            <p
              class="gd-author-note"
              title="Nothing is written down, the opponent does what the script and its fallback say, and leaving the script ends the game the way the scenario says it should."
            >
              What a player sees.
            </p>
            <div class="gd-author-bar">
              <button
                gdAuthorButton="primary"
                data-author="resume"
                title="Start a game for writing again, at the step this test began"
                (click)="authoring.resume()"
              >
                Back to writing
              </button>
              <button
                gdAuthorButton
                data-author="test-again"
                title="The same test from the same step"
                (click)="authoring.startTest(testFrom())"
              >
                Test again
              </button>
            </div>
          </div>
        }
        @case ('elsewhere') {
          <div class="gd-author__body">
            @if (authoring.draft(); as draft) {
              <p class="gd-author-note">The draft has moved on to a fresh game.</p>
              <div class="gd-author-bar">
                <button gdAuthorButton="primary" data-author="go" (click)="authoring.go(gameIdOf(draft.gameId))">
                  Go to it
                </button>
              </div>
            } @else {
              <p
                class="gd-author-note"
                title="This game was a draft's, and the draft has been closed (or the server has been restarted)."
              >
                No draft is open. Open one for editing from the Game Hall.
              </p>
            }
          </div>
        }
      }
      <gd-game-author-leave />
    </section>
    <gd-game-author-pickbox />
  `,
})
export class AuthoringPanelComponent {
  protected readonly authoring = inject(Authoring);
  /** Edit table: the on-table controls for the author. */
  protected readonly author = inject(AuthorBackend);
  private readonly room = inject(ScenarioRoom);
  private readonly document = inject(DOCUMENT);
  protected readonly tabs = AUTHOR_TABS;
  protected readonly dock = this.authoring.dock;

  protected readonly unsaved = computed(
    () => this.authoring.view() === 'author' && this.authoring.draft()?.dirty === true,
  );
  protected readonly title = computed(() => {
    const draft = this.authoring.draft();
    const view = this.authoring.view();
    if (!draft || (view !== 'author' && view !== 'test')) return 'Scenario authoring';
    const name = draft.draft.title ?? draft.draft.id;
    return this.unsaved() ? `* ${name}` : name;
  });
  protected readonly where = computed(() => {
    const draft = this.authoring.draft();
    if (!draft) return '';
    if (this.authoring.view() === 'test') return `A player's game of the draft, from step ${draft.testFrom ?? 1}`;
    return this.authoring.view() === 'author' ? whereLine(draft) : '';
  });
  protected readonly testFrom = computed(() => this.authoring.draft()?.testFrom ?? 1);

  constructor() {
    // The table, and what is laid out against the window, leave the panel its room
    const root = this.document.documentElement;
    effect(() => {
      const width = this.dock().width;
      this.room.reservedRight.set(width);
      if (width > 0) root.style.setProperty(PAGE_RIGHT_PROPERTY, `${width}px`);
      else root.style.removeProperty(PAGE_RIGHT_PROPERTY);
    });
    inject(DestroyRef).onDestroy(() => {
      this.room.reservedRight.set(0);
      root.style.removeProperty(PAGE_RIGHT_PROPERTY);
    });
  }

  protected gameIdOf(gameId: string | number): string {
    return String(gameId);
  }
}
