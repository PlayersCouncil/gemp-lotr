import { Component, computed, effect, inject, output, signal, untracked } from '@angular/core';
import { PlayerSearch } from '../../../../core/hall/play/player-picker';
import * as words from '../../../../core/hall/play/play-messages';
import { pageMarkup } from '../../../../core/hall/hall-dialogs.service';
import { HallDeckChoice } from '../../../../core/hall/tables/hall-table-models';
import { TIMER_HELP } from '../../../../core/hall/tables/hall-tables-messages';
import { timerLabel } from '../../../../core/hall/tables/hall-table-parsers';
import { CASUAL_DECK_FLOW, HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { MarkupComponent } from '../../../shared/chat/markup.component';
import { NameSuggestComponent, NameSuggestion } from '../../shared/name-suggest.component';
import { DeckPickerComponent } from './deck-picker.component';
import { FormatInfoComponent } from './format-info.component';
import { checked, fieldValue, HelpToggles } from './play-parts';
import { ResultBoxComponent } from './result-box.component';

/**
 * Play ▸ Open Casual Table (CreateUnrankedTable, #create-unranked-table): the blurb, the deck, the description, the
 * format (picking a deck follows its format; picking a format brings back the deck last used in it unless the deck was
 * picked by hand), the timer, invite-only with the invitee picker, private, keep open, open by default, Create Table
 * and the result. The last format, timer and deck come back (HallTablesService); "Invite to table" arrives through
 * `HallTablesService.invitee`. `created` reports a table created that closes the overlay.
 *
 * DOM contract: `gd-casual-form.gd-play-form` holding `h1`, `div.gd-play-form__blurb` (`gd-markup`), `gd-deck-picker`,
 * rows (`div.gd-play-form__row` with `div.gd-play-form__label`, `button.gd-play-form__info` and the field:
 * `input[data-field=description]`, `select[data-field=format]`, `select[data-field=timer]`,
 * `input[type=checkbox][data-field=invite-only|private|keep-open|default-flow]`, `gd-name-suggest`) each followed by its
 * `div.gd-play-form__info-text` while shown, `button.gd-play-form__create` and `gd-result-box`.
 */
@Component({
  selector: 'gd-casual-form',
  imports: [DeckPickerComponent, FormatInfoComponent, MarkupComponent, NameSuggestComponent, ResultBoxComponent],
  host: { class: 'gd-play-form' },
  template: `
    <h1>{{ w.CASUAL_HEADING }}</h1>
    <div class="gd-play-form__blurb"><gd-markup [nodes]="blurb" /></div>
    <gd-deck-picker
      [playerDecks]="tables.playerDecks()"
      [libraryDecks]="tables.libraryDecks()"
      [value]="deck()"
      (deckChange)="deckPicked($event)"
    />
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.DESCRIPTION }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('desc')">
          i
        </button>
      </div>
      <input
        class="gd-play-form__field"
        data-field="description"
        type="text"
        [attr.maxlength]="w.DESCRIPTION_MAX_LENGTH"
        [disabled]="inviteOnly()"
        [placeholder]="inviteOnly() ? w.DESCRIPTION_INVITE_PLACEHOLDER : ''"
        [value]="description()"
        (input)="description.set(value($event))"
      />
    </div>
    @if (help.shown('desc')) {
      <div class="gd-play-form__info-text">{{ w.HELP_DESCRIPTION }}</div>
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.TABLE_FORMAT }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('format')">
          i
        </button>
      </div>
      <select class="gd-play-form__field" data-field="format" (change)="formatPicked(value($event))">
        @for (format of tables.formats(); track format.code) {
          <option [value]="format.code" [selected]="format.code === format_()">{{ format.name }}</option>
        }
      </select>
    </div>
    @if (help.shown('format')) {
      <gd-format-info [info]="tables.formatInfo(format_() || null)" (helpLink)="helpLink.emit($event)" />
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.GAME_TIMER }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('timer')">
          i
        </button>
      </div>
      <select class="gd-play-form__field" data-field="timer" (change)="timer.set(value($event))">
        @for (option of tables.timersOffered(); track option.code) {
          <option [value]="option.code" [selected]="option.code === timer()">{{ timerLabel(option) }}</option>
        }
      </select>
    </div>
    @if (help.shown('timer')) {
      <div class="gd-play-form__info-text">{{ tables.timerDescription(timer()) ?? timerHelp }}</div>
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.INVITE_ONLY }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('invite')">
          i
        </button>
      </div>
      <input
        type="checkbox"
        data-field="invite-only"
        [checked]="inviteOnly()"
        (change)="inviteOnly.set(isChecked($event))"
      />
      <gd-name-suggest
        [value]="invitee()"
        [suggest]="suggest"
        [disabled]="!inviteOnly()"
        [placeholder]="w.INVITEE_PLACEHOLDER"
        [label]="w.INVITEE_LABEL"
        [toggleLabel]="w.PICKER_TOGGLE"
        [maxLength]="w.DESCRIPTION_MAX_LENGTH"
        (valueChange)="invitee.set($event)"
      />
    </div>
    @if (help.shown('invite')) {
      <div class="gd-play-form__info-text">{{ w.HELP_INVITE }}</div>
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.PRIVATE_MODE }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('private')">
          i
        </button>
      </div>
      <input type="checkbox" data-field="private" [checked]="isPrivate()" (change)="isPrivate.set(isChecked($event))" />
    </div>
    @if (help.shown('private')) {
      <div class="gd-play-form__info-text">{{ w.HELP_PRIVATE }}</div>
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.KEEP_OPEN }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('keep')">
          i
        </button>
      </div>
      <input type="checkbox" data-field="keep-open" [checked]="keepOpen()" (change)="keepOpen.set(isChecked($event))" />
    </div>
    @if (help.shown('keep')) {
      <div class="gd-play-form__info-text">{{ w.HELP_KEEP_OPEN }}</div>
    }
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.DEFAULT_FLOW }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('default')">
          i
        </button>
      </div>
      <input
        type="checkbox"
        data-field="default-flow"
        [checked]="openByDefault()"
        (change)="setDefault(isChecked($event))"
      />
    </div>
    @if (help.shown('default')) {
      <div class="gd-play-form__info-text">{{ w.HELP_DEFAULT_FLOW }}</div>
    }
    <button type="button" class="gd-play-form__create" [disabled]="tables.busy()" (click)="create()">
      {{ w.CREATE_TABLE_BUTTON }}
    </button>
    <gd-result-box [result]="tables.results().casual" />
  `,
})
export class CasualFormComponent {
  protected readonly tables = inject(HallTablesService);
  private readonly players = inject(PlayerSearch);
  readonly created = output();
  readonly helpLink = output<string>();

  protected readonly w = words;
  protected readonly blurb = pageMarkup(words.CASUAL_BLURB);
  protected readonly help = new HelpToggles();
  protected readonly timerLabel = timerLabel;
  protected readonly timerHelp = TIMER_HELP;
  protected readonly value = fieldValue;
  protected readonly isChecked = checked;

  private readonly defaults = this.tables.casualDefaults();
  protected readonly format_ = signal('');
  protected readonly timer = signal(this.defaults.timer);
  protected readonly deck = signal<HallDeckChoice | null>(null);
  private deckByHand = false;
  protected readonly description = signal('');
  protected readonly inviteOnly = signal(false);
  protected readonly invitee = signal('');
  protected readonly isPrivate = signal(false);
  protected readonly keepOpen = signal(false);
  protected readonly openByDefault = signal(this.defaults.openByDefault);

  protected readonly suggest = async (term: string): Promise<NameSuggestion[]> =>
    (await this.players.suggest(term)).map((item) =>
      item.kind === 'player' ? { name: item.name, heading: item.heading } : { note: item.text },
    );

  private readonly formatCodes = computed(() => this.tables.formats().map((f) => f.code));

  constructor() {
    // the remembered format, timer and deck, once the lists have arrived (TableFlow.want / SelectDeck.requestRestore)
    effect(() => {
      const codes = this.formatCodes();
      const timers = this.tables.timersOffered();
      this.tables.playerDecks();
      this.tables.libraryDecks();
      untracked(() => {
        if (!codes.includes(this.format_())) {
          const wanted = this.defaults.format;
          this.format_.set(wanted !== null && codes.includes(wanted) ? wanted : (codes[0] ?? ''));
        }
        if (!timers.some((t) => t.code === this.timer())) {
          this.timer.set(timers.some((t) => t.code === 'default') ? 'default' : (timers[0]?.code ?? 'default'));
        }
        if (!this.deckByHand && this.deck() === null) {
          const deck = this.tables.initialDeck(CASUAL_DECK_FLOW, this.format_() || null);
          this.deck.set(deck);
          // selecting it triggers the deck's change, so the format follows it (followDeck)
          const format = deck === null ? null : this.tables.formatOfDeck(deck);
          if (format !== null) {
            this.format_.set(format);
          }
        }
      });
    });
    // "Invite to table" (contract C1): invite-only for that player
    effect(() => {
      const invitee = this.tables.invitee();
      if (invitee !== null) {
        untracked(() => {
          this.inviteOnly.set(true);
          this.invitee.set(invitee);
        });
      }
    });
  }

  /** A deck picked by hand: the format follows the deck's when the hall offers it. */
  protected deckPicked(deck: HallDeckChoice | null): void {
    this.deck.set(deck);
    this.deckByHand = deck !== null;
    const format = deck === null ? null : this.tables.formatOfDeck(deck);
    if (format !== null) {
      this.format_.set(format);
    }
  }

  /** A format picked by hand: the deck last used in it comes back, unless the deck was picked by hand. */
  protected formatPicked(code: string): void {
    this.format_.set(code);
    if (!this.deckByHand) {
      const remembered = this.tables.rememberedDeck(CASUAL_DECK_FLOW, code);
      if (remembered !== null) {
        this.deck.set(remembered);
      }
    }
  }

  protected setDefault(value: boolean): void {
    this.openByDefault.set(value);
    this.tables.setCasualOpenByDefault(value);
  }

  protected async create(): Promise<void> {
    const close = await this.tables.createCasual({
      format: this.format_(),
      deck: this.deck(),
      timer: this.timer(),
      description: this.description(),
      isPrivate: this.isPrivate(),
      isInviteOnly: this.inviteOnly(),
      invitee: this.invitee(),
      keepOpen: this.keepOpen(),
    });
    if (close) {
      this.created.emit();
    }
  }
}
