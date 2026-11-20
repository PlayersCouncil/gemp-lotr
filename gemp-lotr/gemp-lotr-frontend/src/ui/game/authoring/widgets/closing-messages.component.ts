import { Component, computed, input, linkedSignal, output } from '@angular/core';
import { DraftMessage } from '../../../../core/game/authoring/authoring-state';
import { AuthorButtonDirective } from './author-button.directive';
import { AuthorKeepDirective } from './author-keep.directive';
import { AuthorSendDirective } from './author-send.directive';
import { AuthorFieldComponent } from './author-field.component';
import { HjsonAreaDirective } from './hjson-area.directive';
import { SAY_IF_HINT, SAY_IF_PLACEHOLDER } from './say-if';

/**
 * A list of closing messages (said together on the panel that ends the scenario): each a heading, a text and what it
 * is said only if (its `require`, typed as the file has it: `conditions` are the server's text of each, by its place),
 * sent when a field is left; ↑ ↓ to reorder, ✕ to take one out, + Message to write another (it goes with its text).
 * Any other part a message has (a note) is kept as it is. The list says each change whole (`changed`); a
 * fresh list from the server is taken only when it says something else, so a field being typed in is left alone.
 * `name` names its fields across a fresh game (gdAuthorKeep).
 *
 * DOM contract: `gd-author-closing-messages` holding `ol.gd-author-closing` (`data-count`) with an
 * `li.gd-author-closing__message` per message (`data-index`): `input[data-part=header]`, `textarea[data-part=text]`,
 * `textarea[data-part=require]` (Say only if), `button[data-author]` up, down, delete; then
 * `button[data-author=add-message]`.
 */
@Component({
  selector: 'gd-author-closing-messages',
  styleUrl: './closing-messages.component.css',
  imports: [AuthorButtonDirective, AuthorFieldComponent, AuthorKeepDirective, AuthorSendDirective, HjsonAreaDirective],
  template: `
    <ol class="gd-author-closing" [attr.data-count]="rows().length">
      @for (row of rows(); track $index; let i = $index, last = $last) {
        <li class="gd-author-closing__message" [attr.data-index]="i">
          <div class="gd-author-closing__head">
            <input
              type="text"
              class="gd-author-input"
              data-part="header"
              placeholder="heading"
              aria-label="Heading"
              [value]="row.header ?? ''"
              [gdAuthorKeep]="name() + '.' + i + '.header'"
              gdAuthorSend
              (sent)="put(i, 'header', $event)"
            />
            <button
              gdAuthorButton
              data-author="up"
              title="Said before the one above"
              [disabled]="i === 0"
              (click)="move(i, -1)"
            >
              ↑
            </button>
            <button
              gdAuthorButton
              data-author="down"
              title="Said after the one below"
              [disabled]="last"
              (click)="move(i, 1)"
            >
              ↓
            </button>
            <button gdAuthorButton="danger" data-author="delete" title="Take it out" (click)="remove(i)">✕</button>
          </div>
          <textarea
            class="gd-author-input gd-author-closing__text"
            rows="2"
            data-part="text"
            placeholder="what is said"
            aria-label="What is said"
            [value]="row.text"
            [gdAuthorKeep]="name() + '.' + i + '.text'"
            gdAuthorSend
            (sent)="put(i, 'text', $event)"
          ></textarea>
          @let condition = conditionOf(row, i);
          <gd-author-field label="Say only if" [hint]="sayIfHint">
            <textarea
              gdHjsonArea
              rows="1"
              data-part="require"
              aria-label="Say only if"
              [placeholder]="sayIfPlaceholder"
              [text]="condition"
              [value]="condition"
              [gdAuthorKeep]="name() + '.' + i + '.require'"
              gdAuthorSend
              (sent)="put(i, 'require', $event)"
            ></textarea>
          </gd-author-field>
        </li>
      }
    </ol>
    <button
      gdAuthorButton
      data-author="add-message"
      title="Another message, after these; it goes into the file with its text"
      (click)="add()"
    >
      + Message
    </button>
  `,
})
export class ClosingMessagesComponent {
  readonly messages = input.required<readonly DraftMessage[]>();
  /** Names the fields across a fresh game. */
  readonly name = input.required<string>();
  readonly changed = output<DraftMessage[]>();
  /** The server's text of each message's condition, as the file has it (by its place); JSON where it has none. */
  readonly conditions = input<readonly (string | undefined)[]>([]);
  protected readonly sayIfHint = SAY_IF_HINT;
  protected readonly sayIfPlaceholder = SAY_IF_PLACEHOLDER;

  /** What the server says (the list is made afresh only when that changes). */
  private readonly said = computed(() => JSON.stringify(this.messages()));
  protected readonly rows = linkedSignal<string, DraftMessage[]>({
    source: this.said,
    computation: (said) => JSON.parse(said) as DraftMessage[],
  });

  protected add(): void {
    this.rows.update((rows) => [...rows, { text: '' }]);
  }

  /** A message's condition as the file has it: what was typed, the server's text of it, or JSON. */
  protected conditionOf(row: DraftMessage, index: number): string {
    const require: unknown = row.require;
    if (require === undefined || require === null) return '';
    if (typeof require === 'string') return require;
    return this.conditions()[index] ?? JSON.stringify(require, null, '\t');
  }

  protected put(index: number, part: 'header' | 'text' | 'require', typed: string): void {
    this.rows.update((rows) =>
      rows.map((row, at) => {
        if (at !== index) return row;
        const next: Record<string, unknown> = { ...row, [part]: typed };
        if ((part === 'header' || part === 'require') && typed.trim() === '') Reflect.deleteProperty(next, part);
        return next as unknown as DraftMessage;
      }),
    );
    this.say();
  }

  protected move(index: number, by: -1 | 1): void {
    this.rows.update((rows) => {
      const next = [...rows];
      next.splice(index + by, 0, ...next.splice(index, 1));
      return next;
    });
    this.say();
  }

  protected remove(index: number): void {
    this.rows.update((rows) => rows.filter((_, at) => at !== index));
    this.say();
  }

  /** Says the list as it now is (unless that is what the server has): a message with no text stays here until it has one. */
  private say(): void {
    const said = this.rows().filter((row) => row.text.trim() !== '');
    if (JSON.stringify(said) !== this.said()) this.changed.emit(said);
  }
}
