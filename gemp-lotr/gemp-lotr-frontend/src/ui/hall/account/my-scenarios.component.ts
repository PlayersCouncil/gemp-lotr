import { afterRenderEffect, Component, ElementRef, inject, signal } from '@angular/core';
import { firstText } from '../../../core/hall/first-text';
import {
  CLOSE_DRAFT,
  CLOSE_DRAFT_HINT,
  CONTINUE_WRITING,
  CONTINUE_WRITING_HINT,
  COPY,
  COPY_PUBLIC,
  COPY_PUBLIC_HELP,
  COPY_PUBLIC_HINT,
  COPY_PUBLIC_LOADING,
  COPY_PUBLIC_ROW_HINT,
  COPY_PUBLIC_TITLE,
  CREATE,
  CREATE_HINT,
  DELETE,
  DRAFT_OPEN_BEFORE,
  draftOpenAfter,
  EDIT,
  NEW_ID_HINT,
  NEW_ID_LABEL,
  NEW_ID_MAX,
  NEW_ID_PLACEHOLDER,
  NEW_SCENARIO_LABEL,
  NEW_TITLE_LABEL,
  NEW_TITLE_MAX,
  NEW_TITLE_PLACEHOLDER,
  NO_OWN_FOLDER,
  NO_SCENARIOS_YET,
  PLAY,
  SCENARIOS_HEADING,
  SCENARIOS_INTRO,
  SUBMIT,
} from '../../../core/hall/account/account-messages';
import { MyScenarios } from '../../../core/hall/account/my-scenarios.service';
import { scenarioSlug } from '../../../core/hall/account/scenario-shelf';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { ShelfQuestionComponent } from './shelf-question.component';

/**
 * My Account > My Scenarios (myScenarios.html, scenarioShelf.js MyScenariosUI; scenarioShelf.css): the intro, the
 * draft open in the authoring tool, the new scenario's fields and Copy Public..., the status line, and the player's
 * scenarios by kind and campaign, each with its tags, notes and buttons; the questions in the shelf's own box and the
 * Copy Public dialog. A copy just made is picked out and scrolled to.
 *
 * DOM contract: `gd-account-scenarios.gd-account-scenarios` holding `h1`, two `p.gd-account-scenarios__intro` (bold
 * parts in `b`), `div.gd-account-scenarios__draft` (when one is open: the title in `b`, `button[data-action=resume]`,
 * `button[data-action=close-draft]`), `div.gd-account-scenarios__tools` (`input[name=title]`, `input[name=id]`,
 * `button[data-action=create]`, `button[data-action=copy-public]`), `div.gd-account-scenarios__status` (`role=status`,
 * `data-kind` = pending | success | error), `div.gd-account-scenarios__list` holding `p.gd-account-scenarios__help`
 * (none yet, or no folder) or `h2.gd-account-scenarios__kind`, `h3.gd-account-scenarios__campaign` and a
 * `div.gd-account-scenarios__row` per scenario (`data-path`, `data-picked`; `span.gd-account-scenarios__title`,
 * `span.gd-account-scenarios__dim`, `span.gd-account-scenarios__tag[data-kind]` with `title`,
 * `div.gd-account-scenarios__notes`, `div.gd-account-scenarios__feedback`, and buttons `[data-action=edit|play|copy|
 * submit|delete]` with `title`); `gd-shelf-question`; and `gd-dialog[data-dialog=copy-public]`.
 */
@Component({
  selector: 'gd-account-scenarios',
  styleUrl: './my-scenarios.component.css',
  imports: [DialogComponent, ShelfQuestionComponent],
  host: { class: 'gd-account-scenarios' },
  template: `
    <h1>{{ heading }}</h1>
    @for (paragraph of intro; track $index) {
      <!-- prettier-ignore -->
      <p class="gd-account-scenarios__help gd-account-scenarios__intro">@for (part of paragraph; track $index) {@if (part.bold) {<b>{{ part.text }}</b>} @else {{{ part.text }}}}</p>
    }
    @if (shelf.draft(); as draft) {
      <div class="gd-account-scenarios__draft">
        <span
          >{{ draftBefore }}<b>{{ draftTitle(draft) }}</b
          >{{ draftAfter(draft.test, draft.dirty) }}</span
        >
        <button
          type="button"
          class="gd-button gd-account-scenarios__button"
          data-action="resume"
          [title]="continueHint"
          (click)="shelf.continueDraft()"
        >
          {{ continueLabel }}
        </button>
        <button
          type="button"
          class="gd-button gd-account-scenarios__button"
          data-action="close-draft"
          [title]="closeHint"
          (click)="shelf.closeDraft()"
        >
          {{ closeLabel }}
        </button>
      </div>
    }
    <div class="gd-account-scenarios__tools">
      <span class="gd-account-scenarios__label">{{ newLabel }}</span>
      <input
        type="text"
        name="title"
        [attr.maxlength]="titleMax"
        [placeholder]="titlePlaceholder"
        [attr.aria-label]="titleLabel"
        [value]="newTitle()"
        (input)="newTitle.set(value($event))"
        (keydown.enter)="create()"
      />
      <input
        type="text"
        name="id"
        [attr.maxlength]="idMax"
        [placeholder]="idPlaceholder()"
        [attr.aria-label]="idLabel"
        [title]="idHint"
        [value]="newId()"
        (input)="newId.set(value($event))"
        (keydown.enter)="create()"
      />
      <button
        type="button"
        class="gd-button gd-account-scenarios__button"
        data-action="create"
        [title]="createHint"
        (click)="create()"
      >
        {{ createLabel }}
      </button>
      <span class="gd-account-scenarios__gap"></span>
      <button
        type="button"
        class="gd-button gd-account-scenarios__button"
        data-action="copy-public"
        [title]="copyPublicHint"
        (click)="shelf.openCopyPublic()"
      >
        {{ copyPublicLabel }}
      </button>
    </div>
    <!-- prettier-ignore -->
    <div class="gd-account-scenarios__status" role="status" aria-live="polite" [attr.data-kind]="shelf.saying().kind || null">{{ shelf.saying().text }}</div>
    <div class="gd-account-scenarios__list">
      @if (shelf.loaded()) {
        @if (!shelf.ownFolder()) {
          <p class="gd-account-scenarios__help">{{ noFolder }}</p>
        } @else if (shelf.empty()) {
          <p class="gd-account-scenarios__help">{{ noneYet }}</p>
        } @else {
          @for (group of shelf.groups(); track group.kind) {
            <h2 class="gd-account-scenarios__kind">{{ group.heading }}</h2>
            @for (campaign of group.campaigns; track campaign.title) {
              @if (campaign.heading) {
                <h3 class="gd-account-scenarios__campaign">{{ campaign.heading }}</h3>
              }
              @for (row of campaign.items; track row.scenario.path) {
                <div
                  class="gd-account-scenarios__row"
                  [attr.data-path]="row.scenario.path"
                  [attr.data-picked]="row.picked ? '' : null"
                >
                  <div class="gd-account-scenarios__text">
                    <div>
                      <span class="gd-account-scenarios__title">{{ row.title }}</span>
                      @if (row.position) {
                        <span class="gd-account-scenarios__dim">{{ row.position }}</span>
                      }
                    </div>
                    @if (row.tags.length > 0) {
                      <div class="gd-account-scenarios__tags">
                        @for (tag of row.tags; track tag.kind) {
                          <span class="gd-account-scenarios__tag" [attr.data-kind]="tag.kind" [title]="tag.hint">{{
                            tag.text
                          }}</span>
                        }
                      </div>
                    }
                    <div class="gd-account-scenarios__notes">{{ row.notes }}</div>
                    @if (row.feedback; as feedback) {
                      <!-- prettier-ignore -->
                      <div class="gd-account-scenarios__feedback">@if (feedback.label) {<b>{{ feedback.label }}</b>}{{ feedback.text }}</div>
                    }
                  </div>
                  <div class="gd-account-scenarios__buttons">
                    <button
                      type="button"
                      class="gd-button gd-account-scenarios__button"
                      data-action="edit"
                      [title]="row.edit.hint"
                      [disabled]="!row.edit.enabled"
                      (click)="shelf.editScenario(row.scenario)"
                    >
                      {{ editLabel }}
                    </button>
                    <button
                      type="button"
                      class="gd-button gd-account-scenarios__button"
                      data-action="play"
                      [title]="row.play.hint"
                      [disabled]="!row.play.enabled"
                      (click)="shelf.play(row.scenario)"
                    >
                      {{ playLabel }}
                    </button>
                    <button
                      type="button"
                      class="gd-button gd-account-scenarios__button"
                      data-action="copy"
                      [title]="row.copy.hint"
                      [disabled]="!row.copy.enabled"
                      (click)="shelf.copy(row.scenario)"
                    >
                      {{ copyLabel }}
                    </button>
                    <button
                      type="button"
                      class="gd-button gd-account-scenarios__button"
                      data-action="submit"
                      [title]="row.submit.hint"
                      [disabled]="!row.submit.enabled"
                      (click)="shelf.submit(row.scenario)"
                    >
                      {{ submitLabel }}
                    </button>
                    <button
                      type="button"
                      class="gd-button gd-account-scenarios__button"
                      data-action="delete"
                      data-danger
                      [title]="row.delete.hint"
                      [disabled]="!row.delete.enabled"
                      (click)="shelf.delete(row.scenario)"
                    >
                      {{ deleteLabel }}
                    </button>
                  </div>
                </div>
              }
            }
          }
        }
      }
    </div>
    <gd-shelf-question [question]="shelf.question()" (answered)="shelf.answer($event)" />
    <gd-dialog
      name="copy-public"
      [heading]="copyPublicTitle"
      [open]="shelf.copyPublicDialog() !== null"
      (closed)="shelf.closeCopyPublic()"
    >
      @if (shelf.copyPublicDialog(); as dialog) {
        <div class="gd-account-scenarios__popup">
          @switch (dialog.state) {
            @case ('loading') {
              {{ loadingLabel }}
            }
            @case ('failed') {
              {{ dialog.message }}
            }
            @case ('loaded') {
              <p class="gd-account-scenarios__help">{{ copyPublicHelp }}</p>
              <div class="gd-account-scenarios__list">
                @for (group of dialog.groups; track group.kind) {
                  <h2 class="gd-account-scenarios__kind">{{ group.heading }}</h2>
                  @for (campaign of group.campaigns; track campaign.title) {
                    @if (campaign.heading) {
                      <h3 class="gd-account-scenarios__campaign">{{ campaign.heading }}</h3>
                    }
                    @for (row of campaign.items; track row.scenario.path) {
                      <div class="gd-account-scenarios__row" [attr.data-path]="row.scenario.path">
                        <div class="gd-account-scenarios__text">
                          <div class="gd-account-scenarios__title-plain">{{ row.title }}</div>
                          <div class="gd-account-scenarios__notes">{{ row.notes }}</div>
                        </div>
                        <div class="gd-account-scenarios__buttons">
                          <button
                            type="button"
                            class="gd-button gd-account-scenarios__button"
                            data-action="copy"
                            [title]="copyPublicRowHint"
                            (click)="shelf.copyPublic(row.scenario)"
                          >
                            {{ copyLabel }}
                          </button>
                        </div>
                      </div>
                    }
                  }
                }
              </div>
            }
          }
        </div>
      }
    </gd-dialog>
  `,
})
export class MyScenariosComponent {
  protected readonly shelf = inject(MyScenarios);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly newTitle = signal('');
  protected readonly newId = signal('');

  protected readonly heading = SCENARIOS_HEADING;
  protected readonly intro = SCENARIOS_INTRO;
  protected readonly draftBefore = DRAFT_OPEN_BEFORE;
  protected readonly draftAfter = draftOpenAfter;
  protected readonly continueLabel = CONTINUE_WRITING;
  protected readonly continueHint = CONTINUE_WRITING_HINT;
  protected readonly closeLabel = CLOSE_DRAFT;
  protected readonly closeHint = CLOSE_DRAFT_HINT;
  protected readonly newLabel = NEW_SCENARIO_LABEL;
  protected readonly titlePlaceholder = NEW_TITLE_PLACEHOLDER;
  protected readonly titleLabel = NEW_TITLE_LABEL;
  protected readonly titleMax = NEW_TITLE_MAX;
  protected readonly idLabel = NEW_ID_LABEL;
  protected readonly idHint = NEW_ID_HINT;
  protected readonly idMax = NEW_ID_MAX;
  protected readonly createLabel = CREATE;
  protected readonly createHint = CREATE_HINT;
  protected readonly copyPublicLabel = COPY_PUBLIC;
  protected readonly copyPublicHint = COPY_PUBLIC_HINT;
  protected readonly noFolder = NO_OWN_FOLDER;
  protected readonly noneYet = NO_SCENARIOS_YET;
  protected readonly editLabel = EDIT;
  protected readonly playLabel = PLAY;
  protected readonly copyLabel = COPY;
  protected readonly submitLabel = SUBMIT;
  protected readonly deleteLabel = DELETE;
  protected readonly copyPublicTitle = COPY_PUBLIC_TITLE;
  protected readonly loadingLabel = COPY_PUBLIC_LOADING;
  protected readonly copyPublicHelp = COPY_PUBLIC_HELP;
  protected readonly copyPublicRowHint = COPY_PUBLIC_ROW_HINT;

  constructor() {
    // a copy just made is scrolled to (MyScenariosUI.render: centred, a moment after the list is drawn)
    afterRenderEffect(() => {
      if (this.shelf.groups().some((group) => group.campaigns.some((c) => c.items.some((row) => row.picked)))) {
        const picked = this.host.nativeElement.querySelector('[data-picked]');
        // (jsdom has no scrollIntoView)
        if (picked && typeof picked.scrollIntoView === 'function') {
          picked.scrollIntoView({ block: 'center' });
        }
      }
    });
  }

  /** The id field's placeholder: the id the title makes, else "id (optional)". */
  protected idPlaceholder(): string {
    return firstText(scenarioSlug(this.newTitle()), NEW_ID_PLACEHOLDER);
  }

  protected draftTitle(draft: { title: string | null; id: string | null }): string {
    return firstText(draft.title, draft.id);
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  protected async create(): Promise<void> {
    if (await this.shelf.create(this.newTitle(), this.newId())) {
      this.newTitle.set('');
      this.newId.set('');
    }
  }
}
