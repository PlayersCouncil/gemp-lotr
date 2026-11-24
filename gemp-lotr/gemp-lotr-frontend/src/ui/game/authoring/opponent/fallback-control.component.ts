import { Component, computed, inject } from '@angular/core';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  fallbackOptions,
  fallbackPatch,
  OpponentPatch,
  playsPatch,
  playsWith,
} from '../../../../core/game/authoring/opponent-orders';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { OrdersEditor } from './orders-editor';

/**
 * What the opponent does with a decision no step and no standing order answers (`opponent.fallback`), and, for the
 * sandbox, which of its cards it may play (`opponent.plays`; none ticked: all of them, in the file's order; ticked,
 * in the order ticked). The same control in the Scenario tab's "How it plays" and in the Opponent tab. Each change
 * is an `opponent` patch, which leaves the standing orders as they are; a fallback other than the sandbox takes the
 * sandbox's cards out with it.
 *
 * DOM contract: `gd-author-select[data-part=fallback]`; with the sandbox, `.gd-author-plays` holding a
 * `gd-author-check[data-card]` per card the opponent has.
 */
@Component({
  selector: 'gd-author-fallback',
  styleUrl: './fallback-control.component.css',
  imports: [AuthorCheckComponent, AuthorSelectComponent],
  template: `
    <gd-author-select
      data-part="fallback"
      label="Fallback"
      hint="fail: that is leaving the script; pass: it does as little as it can; bot: Gemp's bot plays it; sandbox: it plays the minions it can afford, and assigns the minions left to the leftovers character; eager: it takes every action and response it can"
      [options]="options()"
      [value]="fallback()"
      (changed)="choose($event)"
    />
    @if (fallback() === 'sandbox') {
      <div class="gd-author-plays" title="The cards it may play, tried in this order. None ticked: all of them.">
        <span class="gd-author-label">plays</span>
        @for (card of choices(); track card) {
          <gd-author-check
            [attr.data-card]="card"
            [label]="card"
            [checked]="plays().includes(card)"
            (changed)="play(card, $event)"
          />
        }
        @if (plays().length === 0) {
          <span class="gd-author-note">(all)</span>
        }
      </div>
    }
  `,
})
export class FallbackControlComponent {
  private readonly authoring = inject(Authoring);
  /** In the Opponent tab: its changes go through the tab's editor, which keeps the tab as it was. */
  private readonly editor = inject(OrdersEditor, { optional: true });
  private readonly opponent = computed(() => this.authoring.draft()?.opponent);
  protected readonly options = computed(() => fallbackOptions(this.opponent()));
  protected readonly fallback = computed(
    () => this.opponent()?.fallback ?? this.authoring.draft()?.draft.opponent?.fallback ?? 'fail',
  );
  protected readonly plays = computed(() => this.opponent()?.plays ?? []);
  protected readonly choices = computed(() => this.opponent()?.playsChoices ?? []);

  protected choose(fallback: string): void {
    this.send(fallbackPatch(fallback));
  }

  protected play(card: string, on: boolean): void {
    this.send(playsPatch(playsWith(this.plays(), card, on)));
  }

  private send(patch: OpponentPatch): void {
    if (this.editor) void this.editor.patch(patch);
    else void this.authoring.edit({ opponent: patch });
  }
}
