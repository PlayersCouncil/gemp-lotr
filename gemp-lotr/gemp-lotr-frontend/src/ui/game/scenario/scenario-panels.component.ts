import { DOCUMENT } from '@angular/common';
import { Component, computed, inject, signal, ViewEncapsulation } from '@angular/core';
import { ScenarioEnd, ScenarioInfo } from '../../../core/game/scenario/scenario-events';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { ScoreLines, scoreLines } from '../../../core/game/scenario/scenario-score';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { EditBarComponent } from '../table-edit/edit-bar.component';
import { PuzzleBarComponent } from '../table-edit/puzzle-bar.component';
import { CardHintsDirective } from '../table/card-hints.directive';

/** Where players say what is wrong with a scenario, or with the game. */
export const BUG_REPORT_URL = 'https://forms.gle/pks6CHMWcpBsTcwv9';

/** A window smaller than this has no room for what a scenario points at (px). */
export const MIN_WIDTH = 1000;
export const MIN_HEIGHT = 600;

interface PanelButton {
  label: string;
  title?: string;
  main: boolean;
  /** Stands apart at the right (the way onward, beside the ways back). */
  onward?: boolean;
  press: () => void;
}

interface Panel {
  kind: 'end' | 'small' | 'phone' | 'hall';
  result: ScenarioEnd['result'] | null;
  heading: string;
  /** HTML. */
  body: string;
  buttons: readonly PanelButton[];
  /** A scored puzzle's score, its best and whether it is a new best. */
  score?: ScoreLines | null;
  /** Solved with cards no solution of the author's lists. */
  unlisted?: boolean;
}

/** Said at a puzzle's end when it was solved with cards its author did not list (Ketura: the solver is told). */
export const UNLISTED_TEXT = 'You found a solution the author did not list.';

const HEADINGS: Readonly<Record<ScenarioEnd['result'], string>> = {
  complete: 'Complete',
  failed: 'Not this time',
  off_script: "That's not how this one goes",
  abandoned: 'This game has been closed',
  error: 'Something went wrong',
  // (a sandbox's game played to its end: see endHeading for who won)
  ended: 'Game over',
};

/** What is said of each ending when the scenario says nothing itself (`<b></b>`: the scenario's title). */
const STOCK: Readonly<Record<ScenarioEnd['result'], string>> = {
  complete: '<p>You have finished <b></b>.</p>',
  failed: "<p>That didn't work out. Have another go.</p>",
  off_script: "<p>The game went somewhere this scenario wasn't written for.</p>",
  abandoned:
    '<p>Your game of <b></b> is no longer running. That happens when a scenario is left alone for too long, or when you start another one.</p>',
  error: '<p>The scenario could not carry on. What happened has been recorded.</p>',
  ended: '',
};

/**
 * The end panel's heading. A game that left its script is "not how this one goes", to be played again; one whose
 * scenario counts leaving the script as failing (outcome.offScript.result: fail, the end event's `restart` false) is
 * a failure like any other.
 */
export function endHeading(end: ScenarioEnd): string {
  if (end.result === 'ended' && end.winner !== null) return end.winner === 'human' ? 'You won' : 'You lost';
  return end.result === 'off_script' && !end.restart ? HEADINGS.failed : HEADINGS[end.result];
}

const escapeHtml = (text: string) => text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

/** What the end panel says: the scenario's own words for its ending, or the stock ones. */
export function endBody(end: ScenarioEnd, info: ScenarioInfo | null): string {
  const said =
    end.messages
      .map((message) => (message.header === null ? '' : `<h3>${escapeHtml(message.header)}</h3>`) + message.html)
      .join('') + (end.message ?? '');
  const title = escapeHtml(info?.title ?? 'this scenario');
  // A game played to its end (a sandbox's) says why it ended, in the game's words
  const why = end.result === 'ended' && end.reason !== null ? `<p>${escapeHtml(end.reason)}</p>` : '';
  return why + (said === '' ? STOCK[end.result] : said).split('<b></b>').join(`<b>${title}</b>`);
}

/**
 * A scripted game's own corner of the page and the panels that stop play: its menu (the scenario's name, Start
 * again, Back to checkpoint, Leave, Report a Bug), the panel it ends with, and the notices that it can't be played here (the phone
 * layout; a window too small for what a scenario points at) or that the Game Hall is open in another tab.
 *
 * A table changed by hand (a sandbox) has its edit bar (`gd-edit-bar`) in the menu in place of Start again; its
 * ending (`ended`: the game played to its end) says who won and why, with Reset and Leave.
 *
 * A puzzle has its bar (`gd-puzzle-bar`: Choosing and Submit, then Start again and Change cards) in its place; its
 * ending says, under the puzzle's own words, the score with its label, the best and "New best" when it is
 * (`.gd-scenario-panel__score`), and that the solution is one the author did not list (`.gd-scenario-panel__unlisted`);
 * and offers Change cards beside Start again.
 *
 * DOM contract: `gd-game-scenario-panels` holding `nav.gd-scenario-menu` (`.gd-scenario-menu__title`,
 * `button[data-scenario-menu]`: restart (not with an edit bar), checkpoint (only while there is one to go back to), leave; `a[data-scenario-menu=report]`; `gd-edit-bar`) and, when one is up,
 * `.gd-scenario-shade` with `section.gd-scenario-panel` (`data-panel`: end, small, phone, hall; an ending's
 * `data-result`) holding `h2`, `.gd-scenario-panel__body`, `.gd-scenario-panel__buttons` with
 * `button.gd-scenario-button[data-panel-button]` and, at an ending with a replay, `.gd-scenario-panel__report`.
 */
@Component({
  selector: 'gd-game-scenario-panels',
  styleUrl: './scenario-panels.component.css',
  // An ending's words are the server's HTML, which a scoped stylesheet wouldn't reach; and the buttons' look is
  // shared with the bubble's (gd-game-scenario). Every rule starts at a .gd-scenario class
  encapsulation: ViewEncapsulation.None,
  imports: [CardHintsDirective, EditBarComponent, PuzzleBarComponent],
  host: {
    style: 'display: contents',
    '(window:resize)': 'measure()',
  },
  template: `
    @if (guide.playing()) {
      @if (!phone()) {
        <nav class="gd-scenario-menu" aria-label="Scenario" [style.--gd-reserved]="room.reservedRight() + 'px'">
          <span class="gd-scenario-menu__title">{{ guide.title() }}</span>
          @if (edit.level() === 'puzzle') {
            <!-- A puzzle: Choosing and Submit while its slots are filled; then Start again and Change cards -->
            <gd-puzzle-bar />
          } @else if (edit.level() !== 'none') {
            <!-- A table changed by hand: its mode, Stop / Start and Reset (in place of Start again) -->
            <gd-edit-bar />
          } @else {
            <button type="button" data-scenario-menu="restart" (click)="guide.askRestart()">Start again</button>
          }
          @if (guide.checkpoint(); as checkpoint) {
            <button
              type="button"
              data-scenario-menu="checkpoint"
              [title]="'Go back to the last checkpoint: step ' + checkpoint"
              (click)="guide.askCheckpoint()"
            >
              Back to checkpoint
            </button>
          }
          <button type="button" data-scenario-menu="leave" (click)="guide.leave()">Leave</button>
          <a
            data-scenario-menu="report"
            target="_blank"
            rel="noopener"
            title="Something wrong with this scenario, or with the game? Tell us (opens in a new tab)."
            [href]="bugReport"
            >Report a Bug</a
          >
        </nav>
      }
      @if (panel(); as panel) {
        <div class="gd-scenario-shade">
          <section
            class="gd-scenario-panel"
            role="dialog"
            aria-modal="true"
            [attr.data-panel]="panel.kind"
            [attr.data-result]="panel.result"
          >
            <h2>{{ panel.heading }}</h2>
            <div class="gd-scenario-panel__body" gdCardHints [innerHTML]="panel.body"></div>
            @if (panel.score; as score) {
              <p class="gd-scenario-panel__score" [title]="score.title">
                <span class="gd-scenario-panel__score-label">{{ score.label }}</span>
                <b class="gd-scenario-panel__score-value">{{ score.value }}</b>
                @if (score.best !== null) {
                  <span class="gd-scenario-panel__best">Best {{ score.best }}</span>
                }
                @if (score.newBest) {
                  <span class="gd-scenario-panel__new-best">New best</span>
                }
              </p>
            }
            @if (panel.unlisted) {
              <p class="gd-scenario-panel__unlisted">{{ unlistedText }}</p>
            }
            <div class="gd-scenario-panel__buttons">
              @for (button of panel.buttons; track button.label) {
                <button
                  type="button"
                  class="gd-scenario-button"
                  [attr.data-main]="button.main ? '' : null"
                  [attr.data-onward]="button.onward ? '' : null"
                  [attr.data-panel-button]="button.label"
                  [title]="button.title ?? ''"
                  (click)="button.press()"
                >
                  {{ button.label }}
                </button>
              }
            </div>
            @if (panel.kind === 'end' && report(); as link) {
              <p class="gd-scenario-panel__report">
                Something not right with this scenario?
                <a target="_blank" rel="noopener" [href]="link">Report it</a>
                (the replay is filled in for you).
              </p>
            }
          </section>
        </div>
      }
    }
  `,
})
export class ScenarioPanelsComponent {
  protected readonly guide = inject(ScenarioGuide);
  protected readonly edit = inject(TableEdit);
  protected readonly room = inject(ScenarioRoom);
  private readonly session = inject(GameSession);
  private readonly log = inject(GameLog);
  private readonly hall = inject(HallTab);
  protected readonly phone = inject(CompactLayout).gameActive;
  protected readonly bugReport = BUG_REPORT_URL;
  protected readonly unlistedText = UNLISTED_TEXT;

  /** The window's size, for the notice that it is too small; and whether the player has said to carry on anyway. */
  private readonly document = inject(DOCUMENT);
  private readonly window = signal(this.windowSize());
  private readonly carryOn = signal(false);

  /** The feedback form's address, with this game's replay filled in: the server names it at the end of the game. */
  protected readonly report = computed(
    () => /<a\b[^>]*\bhref\s*=\s*["']([^"']+)["']/.exec(this.log.feedback() ?? '')?.[1] ?? null,
  );

  protected readonly panel = computed<Panel | null>(() => {
    const back = () => this.guide.backToHall();
    if (this.phone()) {
      return {
        kind: 'phone',
        result: null,
        heading: 'Not on this screen',
        body: '<p>This scenario cannot be played on mobile devices.</p>',
        buttons: [{ label: 'Back to the Game Hall', main: true, press: back }],
      };
    }
    const elsewhere = this.hall.elsewhere();
    if (elsewhere !== null) {
      return {
        kind: 'hall',
        result: null,
        heading: 'The Game Hall is already open',
        body: '<p>It is in another tab of this browser. You can close this tab and go back to it.</p>',
        buttons: [{ label: 'Open it here instead', main: true, press: () => this.hall.openHere(elsewhere) }],
      };
    }
    const end = this.guide.end();
    if (end && !this.guide.leaving()) {
      return {
        kind: 'end',
        result: end.result,
        heading: endHeading(end),
        body: endBody(end, this.guide.info()) + this.lastOfCampaign(end),
        buttons: this.withChangeCards(this.endButtons(end), end),
        score: end.score ? scoreLines(end.score) : null,
        unlisted: end.unlisted === true && end.result === 'complete',
      };
    }
    const { width, height } = this.window();
    if (!end && !this.carryOn() && !this.session.model().ended && (width < MIN_WIDTH || height < MIN_HEIGHT)) {
      return {
        kind: 'small',
        result: null,
        heading: 'This needs a bigger screen',
        body: "<p>Scenarios point at things on the game table, and there isn't room for that here. Try a larger window.</p>",
        buttons: [
          { label: 'Back to the Game Hall', main: true, press: back },
          { label: 'Carry on anyway', main: false, press: () => this.carryOn.set(true) },
        ],
      };
    }
    return null;
  });

  private lastOfCampaign(end: ScenarioEnd): string {
    const campaign = this.guide.info()?.campaign;
    return end.result === 'complete' && campaign && !campaign.next
      ? `<p>That was the last of ${escapeHtml(campaign.title)}.</p>`
      : '';
  }

  private endButtons(end: ScenarioEnd): PanelButton[] {
    const back = () => this.guide.backToHall();
    if (end.result === 'ended') {
      // A sandbox's game played to its end: the table as it opened again, or away
      return [
        ...(end.reset ? [{ label: 'Reset', main: true, press: () => void this.edit.reset(false) }] : []),
        { label: 'Leave', main: !end.reset, press: back },
      ];
    }
    const campaign = this.guide.info()?.campaign ?? null;
    if (campaign && end.result === 'complete') {
      // One of a campaign, finished: the ways back on the left, the way onward on the right
      const next = campaign.next;
      return [
        { label: 'Back to the Game Hall', main: next === null, press: back },
        { label: 'Play it Again', main: false, press: () => this.guide.restart() },
        ...(next
          ? [
              {
                label: 'Next Scenario',
                title: next.title,
                main: true,
                onward: true,
                press: () => this.guide.startNext(),
              },
            ]
          : []),
      ];
    }
    const checkpoint = end.checkpoint !== null && end.result !== 'complete' && end.result !== 'abandoned';
    const buttons: PanelButton[] = [];
    if (checkpoint) {
      buttons.push({ label: 'Back to the last checkpoint', main: true, press: () => this.guide.restart(true) });
    }
    if (end.result !== 'error') {
      buttons.push({
        label: end.result === 'complete' ? 'Play it again' : 'Start again',
        main: end.result !== 'complete' && !checkpoint,
        press: () => this.guide.restart(),
      });
    }
    buttons.push({
      label: 'Back to the Game Hall',
      main: end.result === 'complete' || (end.result === 'error' && !checkpoint),
      press: back,
    });
    return buttons;
  }

  /**
   * A puzzle's ending also offers Change cards (the puzzle again with its slots empty), before the way back to the
   * Game Hall; not for a draft's game, nor an ending that cannot be played again.
   */
  private withChangeCards(buttons: PanelButton[], end: ScenarioEnd): PanelButton[] {
    const info = this.guide.info();
    if (info?.level !== 'puzzle' || info.author || info.draftTest || end.result === 'error') return buttons;
    const change: PanelButton = {
      label: 'Change cards',
      title: 'Start over with the slots empty',
      main: false,
      press: () => void this.guide.changeCards(),
    };
    const hall = buttons.findIndex((button) => button.label === 'Back to the Game Hall');
    return hall < 0 ? [...buttons, change] : [...buttons.slice(0, hall), change, ...buttons.slice(hall)];
  }

  protected measure(): void {
    this.window.set(this.windowSize());
  }

  private windowSize(): { width: number; height: number } {
    const page = this.document.documentElement;
    return { width: page.clientWidth, height: page.clientHeight };
  }
}
