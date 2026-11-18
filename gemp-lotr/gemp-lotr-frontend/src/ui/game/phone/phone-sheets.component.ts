import {
  afterNextRender,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  Injector,
  untracked,
  viewChild,
} from '@angular/core';
import { ChatRoom } from '../../../core/chat/chat-room.service';
import { CardViews } from '../../../core/cards/card-views.service';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { noEffectReason } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { chatLines, GameSheets } from '../../../core/game/phone/game-sheets';
import { CARD_SIZE_MAX, CARD_SIZE_MIN, PhoneSettings } from '../../../core/game/phone/phone-settings';
import { PhoneCard } from '../../../core/game/phone/phone-table';
import { Sheet } from '../../../core/game/phone/sheet';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GAME_THEME_PICKER, THEME_LABELS, THEMES, ThemeService } from '../../../core/platform/theme.service';
import { AUTO_PASS_PHASES, GameSettings, UnderButton } from '../../../core/game/settings/game-settings';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { handRevealed, PileName, pileCards } from '../../../core/game/table/game-table';
import { PageCues } from '../../../core/platform/page-cues';
import { clockText } from '../panel/game-clock';
import { GameChatComponent } from '../tabs/game-chat.component';
import { ModalSheetDirective } from './modal-sheet.directive';
import { PhoneCardsComponent } from './phone-cards.component';
import { ScrollSettledDirective } from './scroll-settled.directive';
import { phaseLabel } from './phone-status.component';

interface PileView {
  pile: PileName;
  title: string;
  /** The cards, or null when only their number may be seen. */
  cards: readonly PhoneCard[] | null;
  count: string;
}

const PILE_TITLES: Readonly<Record<PileName, string>> = {
  adventureDeck: 'Adventure deck',
  deadPile: 'Dead pile',
  discard: 'Discard pile',
  removedPile: 'Removed from game',
  deck: 'Draw deck',
  revealedHand: 'Revealed hand',
};

/** The hall the menu goes back to. */
const HALL = '/gemp-lotr/hall.html';

/**
 * The phone layout's sheets (G7 of trymarz/gemp-lotr-private#27), one over another (GameSheets): a card's details,
 * a player's piles, the menu, the chat with the game's messages, the settings (one column, finger-sized rows), the
 * game's information and the confirmations to concede or cancel; and over them all, a card's actions to choose from.
 * Closing (✕, ‹ when it goes back, Escape on the page, the backdrop) shows the sheet below as it was, scrolled to the
 * same place. Upright phones get them from the bottom; tablets and phones held sideways in the middle.
 *
 * DOM contract: `gd-phone-sheets` holding, while one is open, `dialog.gd-phone-sheet` (a modal: `data-sheet` the kind,
 * `data-depth` how many are open, `aria-label` its title) with `.gd-phone-sheet__backdrop`, and `.gd-phone-sheet__panel`
 * holding
 * `.gd-phone-sheet__head` (`h2`, `button[data-phone=sheet-close]` with `data-back` when it goes back) and
 * `.gd-phone-sheet__body`. The card's details: `img.gd-phone-detail__image` (the picture only) and the attached
 * cards' `gd-phone-cards`. Piles: `section[data-pile]` each. The menu: `button[data-menu]`. The actions:
 * `.gd-phone-sheet[data-sheet=actions]` with `button[data-action-id]`.
 */
@Component({
  selector: 'gd-phone-sheets',
  styleUrl: './phone-sheets.component.css',
  imports: [PhoneCardsComponent, GameChatComponent, ModalSheetDirective, ScrollSettledDirective],
  template: `
    @if (top(); as entry) {
      <dialog
        gdModalSheet
        class="gd-phone-sheet"
        [attr.data-sheet]="entry.sheet.kind"
        [attr.data-depth]="sheets.entries().length"
        [attr.aria-label]="title(entry.sheet)"
      >
        <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -->
        <div class="gd-phone-sheet__backdrop" (click)="sheets.back()"></div>
        <div class="gd-phone-sheet__panel">
          <div class="gd-phone-sheet__head">
            <h2>{{ title(entry.sheet) }}</h2>
            <button
              type="button"
              class="gd-phone-sheet__close"
              data-phone="sheet-close"
              [attr.data-back]="sheets.canGoBack() ? '' : null"
              [attr.aria-label]="sheets.canGoBack() ? 'Back' : 'Close'"
              (click)="back($event)"
            >
              {{ sheets.canGoBack() ? '‹' : '✕' }}
            </button>
          </div>
          <div #body class="gd-phone-sheet__body" (gdScrollSettled)="keep()">
            @switch (entry.sheet.kind) {
              @case ('card') {
                @if (detail(); as d) {
                  <img
                    class="gd-phone-detail__image"
                    [attr.data-horizontal]="d.horizontal ? '' : null"
                    [src]="d.image"
                    alt=""
                    draggable="false"
                  />
                  @if (d.under.length > 0) {
                    <h3>Under it ({{ d.under.length }})</h3>
                    <gd-phone-cards class="gd-phone-sheet__cards" [cards]="d.under" />
                  }
                }
              }
              @case ('piles') {
                @if (pilesView(); as p) {
                  <dl class="gd-phone-facts">
                    @for (fact of p.facts; track fact.name) {
                      <dt>{{ fact.name }}</dt>
                      <dd [attr.data-clock]="fact.clock ? '' : null">{{ fact.value }}</dd>
                    }
                  </dl>
                  @for (pile of p.piles; track pile.pile) {
                    <section [attr.data-pile]="pile.pile">
                      <h3>{{ pile.title }} ({{ pile.count }})</h3>
                      @if (pile.cards === null) {
                        <div class="gd-phone-note">Only its owner sees these cards.</div>
                      } @else {
                        <gd-phone-cards class="gd-phone-sheet__cards" [cards]="pile.cards" empty="Empty" />
                      }
                    </section>
                  }
                }
              }
              @case ('menu') {
                <div class="gd-phone-menu">
                  @for (item of menu(); track item.key) {
                    <button
                      type="button"
                      class="gd-phone-menu__item"
                      [class.gd-button]="item.key === 'theme'"
                      [attr.data-icon]="item.key === 'theme' ? 'theme' : null"
                      [attr.data-menu]="item.key"
                      [attr.data-danger]="item.key === 'concede' ? '' : null"
                      (click)="choose(item.key)"
                    >
                      {{ item.label }}
                    </button>
                  }
                </div>
              }
              @case ('chat') {
                <div class="gd-phone-note">In the room: {{ users() }}</div>
                <gd-game-chat class="gd-phone-chat" />
              }
              @case ('settings') {
                <div class="gd-phone-settings">
                  <div class="gd-phone-tabs" role="tablist">
                    @for (tab of settingTabs; track tab.key) {
                      <button
                        type="button"
                        role="tab"
                        class="gd-phone-tab"
                        [attr.data-settings-tab]="tab.key"
                        [attr.aria-selected]="settingsTab() === tab.key"
                        (click)="sheets.replace({ kind: 'settings', tab: tab.key })"
                      >
                        {{ tab.label }}
                      </button>
                    }
                  </div>
                  @if (settingsTab() === 'display') {
                    <h3>Card size on the table</h3>
                    <div class="gd-phone-scale">
                      <input
                        type="range"
                        data-setting="card-size"
                        aria-label="Card size"
                        [min]="sizeMin"
                        [max]="sizeMax"
                        step="5"
                        [value]="phone.cardSize()"
                        (input)="phone.setCardSize(number($event))"
                      />
                      <output>{{ phone.cardSize() }}%</output>
                    </div>
                    <div class="gd-phone-note">
                      Bigger cards show fewer in a row; the table scrolls further. Kept on this device.
                    </div>
                    <h3>Animation speed</h3>
                    <div class="gd-phone-scale">
                      <input
                        type="range"
                        data-setting="animation-slider"
                        aria-label="Animation speed"
                        min="-4"
                        max="4"
                        step="1"
                        [value]="settings.animationSpeed()"
                        (input)="settings.setAnimationSpeed(number($event))"
                      />
                      <output>{{ settings.animationSpeed() }}</output>
                    </div>
                    <h3>Cards</h3>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="use-old-stack-checkbox"
                        [checked]="settings.oldStackVisuals()"
                        (change)="settings.setOldStackVisuals(checked($event))"
                      />
                      <span>Stacked cards upright<small>As the old look drew them.</small></span>
                    </label>
                    <h3>Under a card holding others</h3>
                    @for (choice of underChoices; track choice.value) {
                      <label class="gd-phone-toggle">
                        <input
                          type="radio"
                          name="gd-phone-under-button"
                          data-setting="underButton"
                          [value]="choice.value"
                          [checked]="settings.underButton() === choice.value"
                          (change)="settings.setUnderButton(choice.value)"
                        />
                        <span
                          >{{ choice.label }}<small>{{ choice.note }}</small></span
                        >
                      </label>
                    }
                  } @else {
                    <h3>Pass automatically when you have nothing to play in</h3>
                    @for (phase of phases; track phase) {
                      <label class="gd-phone-toggle">
                        <input
                          type="checkbox"
                          [attr.data-setting]="'autoPass' + phase"
                          [checked]="settings.autoPassPhases().includes(phase)"
                          (change)="settings.setAutoPass(phase, checked($event))"
                        />
                        <span>{{ phaseName(phase) }}</span>
                      </label>
                    }
                    <h3>Playing</h3>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="autoAccept"
                        [checked]="settings.autoAccept()"
                        (change)="settings.setAutoAccept(checked($event))"
                      />
                      <span>Send at once after choosing<small>No Done tap once the last card is chosen.</small></span>
                    </label>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="alwaysDropDown"
                        [checked]="settings.alwaysDropDown()"
                        (change)="settings.setAlwaysDropDown(checked($event))"
                      />
                      <span>Answers in a list<small>Also when there are only two.</small></span>
                    </label>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="autoDismissDeckReveal"
                        [checked]="settings.autoDismissDeckReveal()"
                        (change)="settings.setAutoDismissDeckReveal(checked($event))"
                      />
                      <span>Skip the look at your own deck when searching it</span>
                    </label>
                    <h3>Alerts</h3>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="moveVibrate"
                        [checked]="phone.moveVibrate()"
                        (change)="phone.setMoveVibrate(checked($event))"
                      />
                      <span>Vibrate when it's your move</span>
                    </label>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="moveSound"
                        [checked]="phone.moveSound()"
                        (change)="phone.setMoveSound(checked($event))"
                      />
                      <span>Sound when it's your move</span>
                    </label>
                    <label class="gd-phone-toggle">
                      <input
                        type="checkbox"
                        data-setting="timerSound"
                        [checked]="!settings.timerAudioMuted()"
                        (change)="settings.setTimerAudioMuted(!checked($event))"
                      />
                      <span>Timer warnings<small>A sound as your time bank runs low.</small></span>
                    </label>
                  }
                </div>
              }
              @case ('info') {
                <dl class="gd-phone-facts">
                  @for (fact of info(); track fact.name) {
                    <dt>{{ fact.name }}</dt>
                    @if (fact.html) {
                      <!-- The server's summary is HTML (the format, the timer), shown as the pre-game panel does -->
                      <dd [innerHTML]="fact.value"></dd>
                    } @else {
                      <dd>{{ fact.value }}</dd>
                    }
                  }
                </dl>
              }
              @case ('confirm') {
                @if (entry.sheet.kind === 'confirm') {
                  <p class="gd-phone-confirm">{{ confirmText(entry.sheet.what) }}</p>
                  <div class="gd-phone-confirm__buttons">
                    <button type="button" class="gd-phone-menu__item" data-phone="confirm-no" (click)="sheets.back()">
                      Back
                    </button>
                    <button
                      type="button"
                      class="gd-phone-menu__item"
                      data-danger
                      data-phone="confirm-yes"
                      (click)="confirmed(entry.sheet.what)"
                    >
                      {{ entry.sheet.what === 'concede' ? 'Concede' : 'Request cancel' }}
                    </button>
                  </div>
                }
              }
            }
          </div>
        </div>
      </dialog>
    }
    @if (decisions.menu(); as menu) {
      <dialog gdModalSheet class="gd-phone-sheet" data-sheet="actions" aria-label="Choose an action">
        <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -->
        <div class="gd-phone-sheet__backdrop" (click)="decisions.closeMenu()"></div>
        <div class="gd-phone-sheet__panel">
          <div class="gd-phone-sheet__head">
            <h2>Choose an action</h2>
            <button
              type="button"
              class="gd-phone-sheet__close"
              data-phone="sheet-close"
              aria-label="Close"
              (click)="decisions.closeMenu()"
            >
              ✕
            </button>
          </div>
          <div class="gd-phone-sheet__body">
            <div class="gd-phone-menu">
              @for (action of menu.actions; track action.actionId) {
                <button
                  type="button"
                  class="gd-phone-menu__item"
                  [attr.data-action-id]="action.actionId"
                  [attr.data-no-effect]="action.noEffect"
                  (click)="decisions.chooseAction(action.actionId)"
                >
                  <span [innerHTML]="action.text"></span>
                  <!-- An action that would do nothing: dimmed, with why (game.html's noEffectAction) -->
                  @if (action.noEffect; as why) {
                    <span class="gd-phone-menu__reason">{{ ' (' + noEffectReason(why) + ')' }}</span>
                  }
                </button>
              }
            </div>
          </div>
        </div>
      </dialog>
    }
  `,
})
export class PhoneSheetsComponent {
  protected readonly sheets = inject(GameSheets);
  protected readonly decisions = inject(GameDecisions);
  protected readonly noEffectReason = noEffectReason;
  protected readonly settings = inject(GameSettings);
  protected readonly phone = inject(PhoneSettings);
  private readonly session = inject(GameSession);
  private readonly clocks = inject(GameClocks);
  private readonly chat = inject(ChatRoom);
  private readonly log = inject(GameLog);
  private readonly viewState = inject(GameViewState);
  private readonly gameViews = inject(GameCardViews);
  private readonly cardViews = inject(CardViews);
  private readonly cues = inject(PageCues);
  private readonly theme = inject(ThemeService);
  /** Players may switch themes (GAME_THEME_PICKER): the menu's first item does it. */
  private readonly themePicker = inject(GAME_THEME_PICKER);
  /** The theme the menu's first item switches to: the next one, after the last the first. */
  private readonly nextTheme = computed(
    () => THEMES[(THEMES.indexOf(this.theme.theme()) + 1) % THEMES.length] ?? THEMES[0],
  );
  private readonly body = viewChild<ElementRef<HTMLElement>>('body');

  protected readonly top = this.sheets.top;
  protected readonly sizeMin = CARD_SIZE_MIN;
  protected readonly sizeMax = CARD_SIZE_MAX;
  protected readonly phases = AUTO_PASS_PHASES;
  /** The button under a card others lie under (GameSettings.underButton). */
  protected readonly underChoices: readonly { value: UnderButton; label: string; note: string }[] = [
    { value: 'button', label: 'A ▾ button', note: 'Opens the cards under it below it, big; ▴ puts them back.' },
    {
      value: 'space',
      label: 'An empty space',
      note: 'The button without its look: the space under the card opens them.',
    },
    { value: 'none', label: 'Nothing', note: 'They only peek out on its left.' },
  ];
  protected readonly settingTabs = [
    { key: 'game', label: 'Game' },
    { key: 'display', label: 'Display' },
  ] as const;

  protected readonly settingsTab = computed(() => {
    const sheet = this.top()?.sheet;
    return sheet?.kind === 'settings' ? sheet.tab : 'game';
  });

  constructor() {
    const injector = inject(Injector);
    // A sheet shown (opened, or shown again by going back): scrolled where it was
    let shown: Sheet | null = null;
    let depth = 0;
    effect(() => {
      const entry = this.top();
      const entries = this.sheets.entries().length;
      untracked(() => {
        if (!entry || (entry.sheet === shown && entries === depth)) {
          shown = entry?.sheet ?? null;
          depth = entries;
          return;
        }
        shown = entry.sheet;
        depth = entries;
        const scroll = entry.scroll;
        afterNextRender(
          {
            write: () => {
              const body = this.body()?.nativeElement;
              if (!body) return;
              body.scrollTop = scroll.top;
              this.rowsOf(body).forEach((row, i) => (row.scrollLeft = scroll.rows[i] ?? 0));
              if (entry.sheet.kind === 'piles' && entry.sheet.focus && scroll.top === 0) {
                body.querySelector(`[data-pile="${entry.sheet.focus}"]`)?.scrollIntoView({ block: 'start' });
              }
            },
          },
          { injector },
        );
        if (entry.sheet.kind === 'chat') this.markChatSeen();
      });
    });
    // While the chat is open, what comes is seen
    effect(() => {
      this.log.entries();
      untracked(() => {
        if (this.top()?.sheet.kind === 'chat') this.markChatSeen();
      });
    });
  }

  /** A card's details: its picture, big, and the cards under it (attached, stacked), whose details open over it. */
  protected readonly detail = computed(() => {
    const sheet = this.top()?.sheet;
    if (sheet?.kind !== 'card') return null;
    const model = this.session.model();
    const card = sheet.cardId === null ? undefined : model.cards[sheet.cardId];
    const view = card
      ? this.gameViews.of(card)
      : this.cardViews.create({ ref: { hint: true }, blueprintId: sheet.blueprintId, zone: 'SPECIAL' });
    const under = card
      ? model.cardOrder
          .flatMap((id) => model.cards[id] ?? [])
          .filter((c) => c.attachedTo === card.id || c.stackedOn === card.id)
          .map((c) => ({ card: c, under: [] }))
      : [];
    return { image: view.imageUrl, horizontal: view.horizontal, under };
  });

  protected readonly pilesView = computed(() => {
    const sheet = this.top()?.sheet;
    if (sheet?.kind !== 'piles') return null;
    const model = this.session.model();
    const player = sheet.player;
    const own = !model.spectator && player === model.participantId;
    const zones = model.stats?.playerZones[player];
    const threat = model.stats?.threats[player];
    const seconds = this.clocks.view().banks[player];
    const count = (n: number | null | undefined) => (n === null || n === undefined ? '–' : String(n));
    const cardsOf = (pile: PileName) => pileCards(model, pile, player).map((card) => ({ card, under: [] }));
    const piles: PileView[] = [
      {
        pile: 'adventureDeck',
        title: PILE_TITLES.adventureDeck,
        cards: own ? cardsOf('adventureDeck') : null,
        count: count(zones?.adventureDeck),
      },
      { pile: 'deadPile', title: PILE_TITLES.deadPile, cards: cardsOf('deadPile'), count: count(zones?.dead) },
      {
        pile: 'discard',
        title: PILE_TITLES.discard,
        cards: model.discardPublic || own ? cardsOf('discard') : null,
        count: count(zones?.discard),
      },
      {
        pile: 'removedPile',
        title: PILE_TITLES.removedPile,
        cards: cardsOf('removedPile'),
        count: count(zones?.removed),
      },
    ];
    if (handRevealed(model, player)) {
      const hand = cardsOf('revealedHand');
      piles.push({
        pile: 'revealedHand',
        title: own ? 'Your revealed hand' : PILE_TITLES.revealedHand,
        cards: hand,
        count: String(hand.length),
      });
    }
    if (own && model.ended) {
      piles.push({ pile: 'deck', title: PILE_TITLES.deck, cards: cardsOf('deck'), count: count(zones?.deck) });
    }
    return {
      facts: [
        { name: 'Time bank', value: seconds === undefined ? '–' : clockText(seconds), clock: true },
        { name: 'Site', value: count(model.positions[player]), clock: false },
        { name: 'Hand', value: count(zones?.hand), clock: false },
        { name: 'Draw deck', value: count(zones?.deck), clock: false },
        {
          name: 'Threats',
          value: threat ? (threat.total === '-1' ? threat.value : `${threat.value} / ${threat.total}`) : '–',
          clock: false,
        },
      ],
      piles,
    };
  });

  protected readonly menu = computed(() => {
    const model = this.session.model();
    const playing = !model.spectator && !this.session.replaying();
    const items: { key: string; label: string }[] = [];
    // The theme first, red, as in the deck builder's menu (the owner, 2026-11-18); the piles are on the players' chips
    if (this.themePicker) items.push({ key: 'theme', label: `Switch to the ${THEME_LABELS[this.nextTheme()]} theme` });
    items.push(
      { key: 'chat', label: 'Chat and game log' },
      { key: 'info', label: 'Game info' },
      { key: 'settings', label: 'Settings' },
    );
    if (this.cues.fullscreenAvailable()) {
      items.push({ key: 'fullscreen', label: this.cues.fullscreen() ? 'Leave full screen' : 'Full screen' });
    }
    if (playing && !model.ended) {
      items.push({ key: 'cancel', label: 'Request game cancel' }, { key: 'concede', label: 'Concede game' });
    }
    items.push({ key: 'hall', label: 'Back to the game hall' });
    return items;
  });

  protected readonly users = computed(
    () =>
      this.chat
        .users()
        .map((user) => user.name)
        .join(', ') || '–',
  );

  protected readonly info = computed(() => {
    const model = this.session.model();
    const pre = model.preGame;
    const facts: { name: string; value: string; html?: boolean }[] = [
      { name: 'Players', value: model.players.join(' against ') || '–' },
      { name: 'Phase', value: phaseLabel(model.phase) || '–' },
      { name: 'Turn', value: model.currentPlayer ?? '–' },
      ...(pre?.summary ? [{ name: 'Game', value: pre.summary, html: true }] : []),
      { name: 'Watching', value: model.spectator ? 'Yes' : this.session.replaying() ? 'A replay' : 'No' },
    ];
    return facts;
  });

  protected title(sheet: Sheet): string {
    switch (sheet.kind) {
      case 'card':
        return 'Card';
      case 'piles': {
        const model = this.session.model();
        return !model.spectator && sheet.player === model.participantId ? 'Your piles' : `${sheet.player}'s piles`;
      }
      case 'menu':
        return 'Menu';
      case 'chat':
        return 'Chat and game log';
      case 'log':
        return 'Game log';
      case 'settings':
        return 'Settings';
      case 'info':
        return 'Game info';
      case 'confirm':
        return sheet.what === 'concede' ? 'Concede game?' : 'Request game cancel?';
    }
  }

  /**
   * The close button: one level back. The focus goes to the sheet itself, so the sheet shown again (whose button it is
   * too) doesn't keep it on its button.
   */
  protected back(event: Event): void {
    (event.currentTarget as HTMLElement | null)?.closest('dialog')?.focus({ preventScroll: true });
    this.sheets.back();
  }

  protected choose(key: string): void {
    switch (key) {
      case 'theme':
        this.theme.select(this.nextTheme());
        break;
      case 'chat':
      case 'info':
        this.sheets.open({ kind: key });
        break;
      case 'settings':
        this.sheets.open({ kind: 'settings', tab: 'game' });
        break;
      case 'cancel':
      case 'concede':
        this.sheets.open({ kind: 'confirm', what: key });
        break;
      case 'hall':
        this.cues.go(HALL);
        break;
      case 'fullscreen':
        // The menu closes: the game fills the screen behind it
        this.sheets.closeAll();
        this.cues.toggleFullscreen();
        break;
    }
  }

  protected confirmText(what: 'concede' | 'cancel'): string {
    return what === 'concede'
      ? 'You lose this game. If you have to leave, conceding lets your opponent move on.'
      : 'The game is cancelled, with no winner, if your opponent asks for it too.';
  }

  protected confirmed(what: 'concede' | 'cancel'): void {
    this.sheets.closeAll();
    if (what === 'concede') this.session.concede();
    else this.session.cancel();
  }

  protected phaseName(phase: string): string {
    return phaseLabel(phase);
  }

  protected checked(event: Event): boolean {
    return (event.target as HTMLInputElement).checked;
  }

  protected number(event: Event): number {
    return Number((event.target as HTMLInputElement).value);
  }

  /** Where the sheet shown is scrolled, kept for when it is shown again (and after a reload). */
  protected keep(): void {
    const body = this.body()?.nativeElement;
    if (!body) return;
    this.sheets.keepScroll({
      top: Math.round(body.scrollTop),
      rows: this.rowsOf(body).map((row) => Math.round(row.scrollLeft)),
    });
  }

  private rowsOf(body: HTMLElement): HTMLElement[] {
    return [...body.querySelectorAll<HTMLElement>('.gd-phone-cards__scroll')];
  }

  private markChatSeen(): void {
    const seen = chatLines(this.log.entries());
    this.viewState.phone.update((phone) => (phone.chatSeen === seen ? phone : { ...phone, chatSeen: seen }));
  }
}
