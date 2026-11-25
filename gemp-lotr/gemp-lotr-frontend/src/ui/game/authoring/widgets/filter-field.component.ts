import { Component, computed, DestroyRef, effect, inject, input, output, signal, untracked } from '@angular/core';
import { BOUND_TYPING_MS, boundVerdict } from '../../../../core/game/authoring/bound-check';
import { AuthoringApi } from '../../../../core/game/authoring/authoring-api.service';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { FilterVerdict, verdictText } from '../../../../core/game/authoring/puzzle-settings';
import { AuthorKeepDirective } from './author-keep.directive';
import { AuthorSendDirective } from './author-send.directive';

/**
 * A deck builder filter typed as text (a puzzle's pool, a slot's bound), with the server's word beside it: how many
 * cards the filter as the draft has it finds, or why it will not do (`GET /scenarios/author/bound`, looked at whenever
 * that changes, and as the author types, once the typing rests; a slot's counted with the pool, `pool`), or, when
 * what was sent was refused, why (`refused`, the server's words, set by the form that sent it; the field keeps what
 * was typed). It sends what is typed when it is left or Enter is pressed, and keeps it across a fresh game (`key`).
 * `count` is what to count, when it is not the field's own text (a bound counted with the pool); `exact` false says
 * that the count is the filter's alone.
 *
 * DOM contract: `.gd-author-filter` with `input.gd-author-input[data-part]` (`part`) and
 * `.gd-author-filter__verdict` (`data-tone`: good, error, quiet; `title`), then `p.gd-author-filter__why` while it
 * was refused.
 */
@Component({
  selector: 'gd-author-filter',
  styleUrl: './filter-field.component.css',
  imports: [AuthorKeepDirective, AuthorSendDirective],
  template: `
    <span class="gd-author-filter">
      <input
        type="text"
        class="gd-author-input gd-author-code"
        spellcheck="false"
        [attr.data-part]="part()"
        [attr.aria-label]="label()"
        [title]="hint()"
        [placeholder]="placeholder()"
        [value]="value()"
        [gdAuthorKeep]="key()"
        gdAuthorSend
        (sent)="sent.emit($event)"
        (input)="typing($any($event.target).value)"
      />
      @if (shown(); as said) {
        <span class="gd-author-filter__verdict" [attr.data-tone]="said.tone" [title]="verdictHint()">{{
          said.text
        }}</span>
      }
    </span>
    @if (why(); as why) {
      <p class="gd-author-filter__why">{{ why }}</p>
    }
  `,
})
export class FilterFieldComponent {
  private readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);

  /** The filter as the draft has it. */
  readonly value = input('');
  /** What to count (default: the value). */
  readonly count = input<string | null>(null);
  readonly exact = input(true);
  readonly part = input.required<string>();
  readonly key = input.required<string>();
  readonly label = input('');
  readonly hint = input('');
  readonly placeholder = input('');
  /** Why what was sent was refused, or null. */
  readonly refused = input<string | null>(null);
  readonly sent = output<string>();

  private readonly verdict = signal<FilterVerdict>({ kind: 'none' });
  protected readonly shown = computed(() => {
    const refused = this.refused();
    return verdictText(refused !== null ? { kind: 'refused', why: refused } : this.verdict());
  });

  protected readonly verdictHint = computed(() => {
    const refused = this.refused();
    if (refused !== null) return refused;
    const verdict = this.verdict();
    if (verdict.kind === 'refused') return verdict.why;
    if (this.pool() !== null) return 'How many cards the server finds with it and the pool, each searched by itself';
    return this.exact()
      ? 'How many cards the server finds with it (alternate printings not counted)'
      : 'How many cards the filter alone finds: the pool says the same key, so the server checks the two apart';
  });

  constructor() {
    effect(() => {
      const filter = (this.count() ?? this.value()).trim();
      this.pool();
      untracked(() => void this.look(filter));
    });
  }

  private serial = 0;

  /** Counts what the filter finds (the last asked wins). */
  private async look(filter: string): Promise<void> {
    const asked = ++this.serial;
    if (filter === '') {
      this.verdict.set({ kind: 'none' });
      return;
    }
    this.verdict.set({ kind: 'busy' });
    const answer = await this.authoring.send(this.api.bound(filter, this.pool()), false);
    if (asked !== this.serial) return;
    this.verdict.set(boundVerdict(answer));
  }

  /**
   * A slot's bound: the puzzle's pool, which the server counts it with (each searched by itself). Null for a field
   * that is checked by itself (the pool's own).
   */
  readonly pool = input<string | null>(null);
  /** Why the filter will not do: what was sent was refused, or the server's check of what is typed says so. */
  protected readonly why = computed(() => {
    const refused = this.refused();
    if (refused !== null) return refused;
    const verdict = this.verdict();
    return verdict.kind === 'refused' ? verdict.why : null;
  });
  private typingTimer: ReturnType<typeof setTimeout> | null = null;

  /** What is being typed is checked once the typing rests (the field's own text stays as typed). */
  protected typing(typed: string): void {
    if (this.typingTimer !== null) clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.typingTimer = null;
      void this.look(typed.trim());
    }, BOUND_TYPING_MS);
  }

  private readonly stopTyping = inject(DestroyRef).onDestroy(() => {
    if (this.typingTimer !== null) clearTimeout(this.typingTimer);
  });
}
