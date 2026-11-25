import { Component, computed, inject, input, signal } from '@angular/core';
import {
  insertAt,
  TEMPLATE_SEATS,
  TemplateChoice,
  templateCards,
  templateChoices,
  templateText,
} from '../../../../core/game/authoring/templates';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { StepEditor } from './step-editor';

/**
 * "Insert a number from the game" beside a message's text (B9): `{…}` opens the list of what the text can quote
 * (state.templates: the totals, the ones for a card, the ones for a seat); one for a card asks which of the scenario's
 * cards, one for a seat which seat; the template then goes into the text at its caret (in place of what is selected),
 * as if it had been typed there: the field keeps the focus and the caret (after it), and the text is sent as typing
 * is. Its buttons do not take the focus from the text.
 *
 * DOM contract: `gd-author-templates` holding `button[data-act=templates]` (`aria-expanded`) and, open,
 * `.gd-author-templates__menu` (`data-asking` = name, card, seat) with a `button[data-template]` per name, then a
 * `button[data-of]` per card or seat, and `button[data-act=back]`.
 */
@Component({
  selector: 'gd-author-templates',
  styleUrl: './template-menu.component.css',
  imports: [AuthorButtonDirective],
  host: { '(mousedown)': '$event.preventDefault()' },
  template: `
    <button
      gdAuthorButton="inline"
      data-act="templates"
      title="Insert a number from the game at the caret"
      [attr.aria-expanded]="open()"
      (click)="toggle()"
    >
      {{ '{…}' }}
    </button>
    @if (open()) {
      <div class="gd-author-templates__menu" [attr.data-asking]="asking()?.needs ?? 'name'">
        @if (asking(); as wanted) {
          <button gdAuthorButton="inline" data-act="back" title="Back to the list" (click)="asking.set(null)">◀</button>
          <span class="gd-author-templates__name">{{ wanted.name }}:</span>
          @for (of of wanted.needs === 'card' ? cards() : seats; track of) {
            <button gdAuthorButton="inline" [attr.data-of]="of" [title]="wanted.title" (click)="put(wanted, of)">
              {{ of }}
            </button>
          }
        } @else {
          @for (choice of choices(); track choice.name + choice.needs) {
            <button
              gdAuthorButton="inline"
              [attr.data-template]="choice.name"
              [attr.data-needs]="choice.needs"
              [title]="choice.title"
              (click)="choose(choice)"
            >
              {{ choice.name }}{{ choice.needs === 'none' ? '' : ':…' }}
            </button>
          }
        }
      </div>
    }
  `,
})
export class TemplateMenuComponent {
  /** The text field it puts the templates into. */
  readonly target = input.required<HTMLTextAreaElement | HTMLInputElement>();

  private readonly editor = inject(StepEditor);
  protected readonly open = signal(false);
  /** A name chosen that asks for a card or a seat. */
  protected readonly asking = signal<TemplateChoice | null>(null);
  protected readonly seats = TEMPLATE_SEATS;
  protected readonly choices = computed(() => templateChoices(this.editor.authoring.draft()?.templates));
  protected readonly cards = computed(() => templateCards(this.editor.authoring.draft()?.draft));

  protected toggle(): void {
    this.open.set(!this.open());
    this.asking.set(null);
  }

  protected choose(choice: TemplateChoice): void {
    if (choice.needs === 'none') this.put(choice, null);
    else this.asking.set(choice);
  }

  /** The template goes in at the caret, as typing would put it there. */
  protected put(choice: TemplateChoice, of: string | null): void {
    const field = this.target();
    const at = insertAt(
      field.value,
      field.selectionStart ?? field.value.length,
      field.selectionEnd ?? field.value.length,
      templateText(choice.name, of),
    );
    field.focus();
    field.value = at.text;
    field.setSelectionRange(at.caret, at.caret);
    // (the field's own input: into the working copy, and sent as its typing is)
    field.dispatchEvent(new Event('input', { bubbles: true }));
    this.open.set(false);
    this.asking.set(null);
  }
}
