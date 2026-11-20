import { Component, computed, inject, input } from '@angular/core';
import { GameSheets } from '../../../../core/game/phone/game-sheets';
import { CARD_SIZE_MAX, CARD_SIZE_MIN, ChoicesScroll, PhoneSettings } from '../../../../core/game/phone/phone-settings';
import { Sheet } from '../../../../core/game/phone/sheet';
import { AUTO_PASS_PHASES, GameSettings, UnderButton } from '../../../../core/game/settings/game-settings';
import { GameUndo } from '../../../../core/game/undo/game-undo.service';
import { phaseLabel } from '../phone-status.component';

/**
 * The phone layout's settings: one column, finger-sized rows, in two tabs. The game's (passing automatically, playing,
 * alerts), kept in the legacy page's cookies or this device's; and the display's (card size, animation speed, stacks,
 * offered cards' scrolling, the button under a card holding others).
 *
 * DOM contract: `.gd-phone-settings` holding `button.gd-phone-tab[data-settings-tab]` (`aria-selected`), and each
 * setting's `input[data-setting]` (among them skipConfirmations; and undoAutoDecline under "This game", where anybody
 * would be asked for a revert).
 */
@Component({
  selector: 'gd-phone-settings-sheet',
  styleUrl: './phone-settings-sheet.component.css',
  template: `
    <div class="gd-phone-settings">
      <div class="gd-phone-tabs" role="tablist">
        @for (tab of settingTabs; track tab.key) {
          <button
            type="button"
            role="tab"
            class="gd-phone-tab"
            [attr.data-settings-tab]="tab.key"
            [attr.aria-selected]="sheet().tab === tab.key"
            (click)="sheets.replace({ kind: 'settings', tab: tab.key })"
          >
            {{ tab.label }}
          </button>
        }
      </div>
      @if (sheet().tab === 'display') {
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
        <h3>A deck or other cards to choose, in the hand's place</h3>
        @for (choice of choicesScrollChoices; track choice.value) {
          <label class="gd-phone-toggle">
            <input
              type="radio"
              name="gd-phone-choices-scroll"
              data-setting="choicesScroll"
              [value]="choice.value"
              [checked]="phone.choicesScroll() === choice.value"
              (change)="phone.setChoicesScroll(choice.value)"
            />
            <span
              >{{ choice.label }}<small>{{ choice.note }}</small></span
            >
          </label>
        }
        <h3>Chat and game log</h3>
        @for (choice of chatOrderChoices; track choice.value) {
          <label class="gd-phone-toggle">
            <input
              type="radio"
              name="gd-phone-chat-order"
              data-setting="chatNewestFirst"
              [value]="choice.value"
              [checked]="settings.chatNewestFirst() === choice.value"
              (change)="settings.setChatNewestFirst(choice.value)"
            />
            <span
              >{{ choice.label }}<small>{{ choice.note }}</small></span
            >
          </label>
        }
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
        <label class="gd-phone-toggle">
          <input
            type="checkbox"
            data-setting="skipConfirmations"
            [checked]="settings.skipConfirmations()"
            (change)="settings.setSkipConfirmations(checked($event))"
          />
          <span
            >Skip extra confirmations<small
              >Answers a question that only asks you to confirm, such as DONE on a full starting fellowship. You can no
              longer undo the choice before it.</small
            ></span
          >
        </label>
        @if (autoDeclineShown()) {
          <h3>This game</h3>
          <label class="gd-phone-toggle">
            <input
              type="checkbox"
              data-setting="undoAutoDecline"
              [checked]="undo.autoDeclining()"
              (change)="undo.setAutoDecline(checked($event))"
            />
            <span>Auto-decline revert requests<small>Only for this game.</small></span>
          </label>
        }
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
  `,
})
export class PhoneSettingsSheetComponent {
  readonly sheet = input.required<Extract<Sheet, { kind: 'settings' }>>();

  protected readonly sheets = inject(GameSheets);
  protected readonly settings = inject(GameSettings);
  protected readonly phone = inject(PhoneSettings);
  protected readonly undo = inject(GameUndo);
  /** "Auto-decline revert requests": for a player, where something can be taken back and anybody would be asked. */
  protected readonly autoDeclineShown = computed(
    () => this.undo.playing() && this.undo.allowed() && !!this.undo.policy()?.asks,
  );

  protected readonly sizeMin = CARD_SIZE_MIN;
  protected readonly sizeMax = CARD_SIZE_MAX;
  protected readonly phases = AUTO_PASS_PHASES;
  /** How offered cards scroll in the hand's place (PhoneSettings.choicesScroll). */
  protected readonly choicesScrollChoices: readonly { value: ChoicesScroll; label: string; note: string }[] = [
    { value: 'sideways', label: 'Sideways', note: 'One row, swiped left and right, as your hand.' },
    { value: 'down', label: 'Down, in rows', note: 'As many as fit in a row, the next ones below; swipe up and down.' },
  ];
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
  /** The chat's order (GameSettings.chatNewestFirst). */
  protected readonly chatOrderChoices: readonly { value: boolean; label: string; note: string }[] = [
    { value: true, label: 'Newest first', note: 'New lines at the top; the chat jumps there with each one.' },
    { value: false, label: 'Oldest first', note: 'New lines at the bottom, as in the classic look.' },
  ];
  protected readonly settingTabs = [
    { key: 'game', label: 'Game' },
    { key: 'display', label: 'Display' },
  ] as const;

  protected phaseName(phase: string): string {
    return phaseLabel(phase);
  }

  protected checked(event: Event): boolean {
    return (event.target as HTMLInputElement).checked;
  }

  protected number(event: Event): number {
    return Number((event.target as HTMLInputElement).value);
  }
}
