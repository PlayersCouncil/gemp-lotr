import { afterRenderEffect, Component, computed, ElementRef, inject, input, signal } from '@angular/core';
import { valueAt, WorkMessage } from '../../../../core/game/authoring/step-model';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { MessageCardComponent } from './message-card.component';
import { StepEditor } from './step-editor';

/**
 * A step's messages, a card each, and "+ Message" after them: its own messages, or the ones that close the scenario
 * (`closing`). A message put in (Insert, + Message) is empty, and goes to the server with what is written in it; its
 * text has the focus.
 *
 * DOM contract: `gd-author-messages` holding `.gd-author-messages` (`data-closing`) with a `gd-author-message` per
 * message and `button[data-act=add-message]`.
 */
@Component({
  selector: 'gd-author-messages',
  styleUrl: './message-list.component.css',
  imports: [AuthorButtonDirective, MessageCardComponent],
  template: `
    <div class="gd-author-messages" [attr.data-closing]="closing() ? '' : null">
      @for (message of messages(); track $index) {
        <gd-author-message
          [index]="$index"
          [path]="path()"
          [count]="messages().length"
          [closing]="closing()"
          (inserted)="insert($index)"
        />
      }
      <button gdAuthorButton="primary" data-act="add-message" title="Another message on this step" (click)="insert(-1)">
        + Message
      </button>
    </div>
  `,
})
export class MessageListComponent {
  /** Where the messages are in the working copy: `messages`, or `end.messages`. */
  readonly path = input.required<string>();
  readonly closing = input(false);

  protected readonly editor = inject(StepEditor);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  /** A message just put in, whose text is to have the focus once it is on the page. */
  private readonly focusDue = signal<number | null>(null);

  protected readonly messages = computed(
    () => (valueAt(this.editor.work(), this.path()) as readonly WorkMessage[] | undefined) ?? [],
  );

  constructor() {
    afterRenderEffect(() => {
      const due = this.focusDue();
      if (due === null) return;
      this.focusDue.set(null);
      this.element.querySelector<HTMLTextAreaElement>(`.gd-author-message[data-index="${due}"] textarea`)?.focus();
    });
  }

  /** An empty message before the one at `at` (-1: after the last). */
  protected insert(at: number): void {
    const messages = [...this.messages()];
    const place = at < 0 ? messages.length : at;
    messages.splice(place, 0, { text: '' });
    this.editor.put(this.path(), messages);
    this.focusDue.set(place);
  }
}
