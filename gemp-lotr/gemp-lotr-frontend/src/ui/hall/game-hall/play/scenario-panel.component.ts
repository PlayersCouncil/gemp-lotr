import { Component, inject, input, output } from '@angular/core';
import * as words from '../../../../core/hall/play/play-messages';
import { MY_SCENARIOS_ROUTE, PlayScenario } from '../../../../core/hall/play/scenarios/play-scenario.service';
import { ScenarioEntry } from '../../../../core/hall/play/scenarios/scenario-list';
import { APP_URL } from '../../../../core/platform/app-paths';
import { ResultBoxComponent } from './result-box.component';

/**
 * Play ▸ Scenarios & Tutorials (PlayScenario, #create-scenario): the blurb, the small-window note, the game in
 * progress with Continue, the folders and the scenarios of the one chosen, the authors' link to My Scenarios, the files
 * with mistakes (Edit when the authoring tool can open them), and the result. Starting a game (or opening one for
 * writing, or Continue) reports `started`, which closes the overlay; following the My Scenarios link reports
 * `route`.
 *
 * DOM contract: `gd-scenario-panel.gd-play-form` holding `h1`, `div.gd-play-form__blurb`,
 * `div.gd-scenario__note[role=note]` (a small window), `div.gd-scenario__current` (`b`, `button[data-action=continue]`),
 * `div.gd-scenario__browser` with `div.gd-scenario__folders[role=tree]` of `button.gd-scenario__folder` (`role=treeitem`,
 * `aria-selected`, `--gd-scenario-depth`; `span.gd-scenario__folder-name`, `span.gd-scenario__folder-count`) and
 * `div.gd-scenario__list` (`div.gd-scenario__empty`, or `h2.gd-scenario__kind` headings and `div.gd-scenario__row`s
 * (`data-scenario`) with `span.gd-scenario__status[data-status]` (`title` its hint), `span.gd-scenario__title-text`,
 * `div.gd-scenario__summary`, `div.gd-scenario__notes` and `button[data-action=play]`)), `div.gd-scenario__author`
 * (`a`), `div.gd-scenario__unreadable[role=alert]` (`div.gd-scenario__unreadable-file`s with `code`, `ul`, and
 * `button[data-action=edit]` or `div.gd-scenario__unreadable-note`) and `gd-result-box`.
 */
@Component({
  selector: 'gd-scenario-panel',
  imports: [ResultBoxComponent],
  host: { class: 'gd-play-form gd-scenario', 'data-heading': 'outer' },
  template: `
    <h1>{{ w.SCENARIO_HEADING }}</h1>
    <div class="gd-play-form__blurb">{{ w.SCENARIO_BLURB }}</div>
    @if (smallWindow()) {
      <div class="gd-scenario__note" role="note">{{ w.SCENARIO_SMALL_WINDOW }}</div>
    }
    @if (play.current(); as current) {
      <div class="gd-scenario__current">
        <div class="gd-scenario__current-text">
          {{ w.IN_THE_MIDDLE_OF }}<b>{{ current.title }}</b
          >.
        </div>
        <button
          type="button"
          class="gd-play-form__button"
          data-action="continue"
          [disabled]="play.starting()"
          (click)="continueGame()"
        >
          {{ w.CONTINUE }}
        </button>
      </div>
    }
    <div class="gd-scenario__browser">
      @if (hasScenarios()) {
        <div class="gd-scenario__folders" role="tree" [attr.aria-label]="w.SCENARIO_FOLDERS_LABEL">
          @for (folder of play.folders(); track folder.key) {
            <button
              type="button"
              class="gd-scenario__folder"
              role="treeitem"
              [attr.aria-selected]="folder.key === play.selectedFolder()?.key"
              [style.--gd-scenario-depth]="folder.depth"
              (click)="play.selectFolder(folder.key)"
            >
              <span class="gd-scenario__folder-name">{{ folder.name }}</span>
              <span class="gd-scenario__folder-count">{{ folder.count }}</span>
            </button>
          }
        </div>
      }
      <div class="gd-scenario__list">
        @if (play.listing() === null) {
          <div class="gd-scenario__empty">{{ play.listFailed() ? w.SCENARIO_LIST_FAILED : w.SCENARIO_LOADING }}</div>
        } @else if (!hasScenarios()) {
          <div class="gd-scenario__empty">{{ w.NO_SCENARIOS }}</div>
        } @else {
          @for (group of play.groups(); track $index) {
            @if (group.heading !== null) {
              <h2 class="gd-scenario__kind">{{ group.heading }}</h2>
            }
            @for (scenario of group.scenarios; track scenario.id) {
              @let row = play.row(scenario);
              <div class="gd-scenario__row" [attr.data-scenario]="scenario.id">
                <div class="gd-scenario__text">
                  <div class="gd-scenario__title">
                    <span class="gd-scenario__status" [attr.data-status]="row.status.kind" [title]="row.status.hint">{{
                      row.status.text
                    }}</span>
                    <span class="gd-scenario__title-text">{{ scenario.title }}</span>
                  </div>
                  @if (scenario.summary) {
                    <div class="gd-scenario__summary">{{ scenario.summary }}</div>
                  }
                  @if (row.notes.length > 0) {
                    <div class="gd-scenario__notes">{{ row.notes.join(' · ') }}</div>
                  }
                </div>
                <button
                  type="button"
                  class="gd-play-form__button"
                  data-action="play"
                  [disabled]="play.starting()"
                  (click)="start(scenario)"
                >
                  {{ row.button }}
                </button>
              </div>
            }
          }
        }
      </div>
    </div>
    @if (play.listing()?.mayAuthor) {
      <div class="gd-scenario__author">
        <span class="gd-scenario__author-note"
          >{{ w.AUTHOR_NOTE_BEFORE }}<a [href]="myScenarios" (click)="follow($event)">{{ w.AUTHOR_LINK }}</a
          >{{ w.AUTHOR_NOTE_AFTER }}</span
        >
      </div>
    }
    @if (play.listing()?.unreadable; as unreadable) {
      @if (unreadable.length > 0) {
        <div class="gd-scenario__unreadable" role="alert">
          <div class="gd-scenario__unreadable-heading">{{ w.unreadableHeading(unreadable.length) }}</div>
          @for (file of unreadable; track file.path) {
            <div class="gd-scenario__unreadable-file">
              <div class="gd-scenario__unreadable-text">
                <code>{{ file.path }}</code>
                <ul>
                  @for (error of file.errors.length > 0 ? file.errors : [w.UNREADABLE_DEFAULT_ERROR]; track $index) {
                    <li>{{ error }}</li>
                  }
                </ul>
                @if (!(file.openable && file.id !== null) && play.listing()?.mayAuthor) {
                  <div class="gd-scenario__unreadable-note">{{ w.CANNOT_OPEN_NOTE }}</div>
                }
              </div>
              @if (file.openable && file.id !== null) {
                <button
                  type="button"
                  class="gd-play-form__button"
                  data-action="edit"
                  [title]="w.EDIT_TITLE"
                  (click)="edit(file.id)"
                >
                  {{ w.EDIT }}
                </button>
              }
            </div>
          }
        </div>
      }
    }
    <gd-result-box [result]="play.result()" />
  `,
})
export class ScenarioPanelComponent {
  protected readonly play = inject(PlayScenario);
  /** The window is too small for a tutorial's pointers (windowTooSmallForTutorial, worked out by the overlay). */
  readonly smallWindow = input(false);
  readonly started = output();
  readonly route = output<string>();

  protected readonly w = words;
  protected readonly myScenarios = `${APP_URL}/${MY_SCENARIOS_ROUTE}`;

  protected hasScenarios(): boolean {
    return (this.play.listing()?.scenarios.length ?? 0) > 0;
  }

  protected async start(scenario: ScenarioEntry): Promise<void> {
    if (await this.play.start(scenario)) {
      this.started.emit();
    }
  }

  protected continueGame(): void {
    this.play.continueCurrent();
    this.started.emit();
  }

  protected async edit(id: string): Promise<void> {
    if (await this.play.edit(id)) {
      this.started.emit();
    }
  }

  protected follow(event: MouseEvent): void {
    event.preventDefault();
    this.route.emit(MY_SCENARIOS_ROUTE);
  }
}
