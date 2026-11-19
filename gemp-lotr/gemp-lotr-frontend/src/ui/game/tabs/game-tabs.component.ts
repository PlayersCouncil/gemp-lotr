import { Component, computed, inject } from '@angular/core';
import { ChatRoom } from '../../../core/chat/chat-room.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { AUTO_PASS_PHASES, GameSettings } from '../../../core/game/settings/game-settings';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { GameChatComponent } from './game-chat.component';
import { KeepScrollDirective } from './keep-scroll.directive';

export type GameTab = 'chat' | 'settings' | 'options' | 'players';

const PHASE_LABELS: Readonly<Record<string, string>> = {
  FELLOWSHIP: 'Fellowship',
  SHADOW: 'Shadow',
  MANEUVER: 'Maneuver',
  ARCHERY: 'Archery',
  ASSIGNMENT: 'Assignment',
  SKIRMISH: 'Skirmish',
  REGROUP: 'Regroup',
};

/**
 * The tab box at the bottom left: the chat, the settings, the options (concede, ask to cancel) and the players in the
 * room; beside the tabs, the hover zoom's switch and the timer sounds' switch (not for a spectator). A replay has only
 * the chat and the settings, and no timer switch.
 *
 * DOM contract: `gd-game-tabs` with `data-extras` (how many switches), holding `.gd-game-tabs__strip` with an `a[data-tab]` (a link, as the legacy tab) per tab (`aria-selected`) and
 * `[data-toggle]` auto-zoom and timer-mute; then `.gd-game-tabs__panel` (`data-panel`). Settings: `[data-setting]`
 * named as the legacy ids (foilPresentation, animation-slider, autoAccept, autoDismissDeckReveal,
 * use-old-stack-checkbox, alwaysDropDown, skipConfirmations, autoPassFELLOWSHIP ...), `[data-timer-alert]`
 * timerAlertBelow and timerAlertEvery; options: `[data-button]` undoGame ("Revert...", there for a player in a game
 * where something can be taken back; disabled while a request waits), concedeGame and cancelGame, and beside Revert
 * `[data-setting=undoAutoDecline]` where anybody would be asked.
 */
@Component({
  selector: 'gd-game-tabs',
  styleUrl: './game-tabs.component.css',
  imports: [GameChatComponent, KeepScrollDirective],
  host: { '[attr.data-extras]': 'extras()' },
  template: `
    <div class="gd-game-tabs__strip" role="tablist">
      @for (tab of tabs(); track tab.name) {
        <!-- A link, as the legacy tab: the browser shows its focus and takes its keys as it did there -->
        <a
          href="#"
          role="tab"
          class="gd-game-tabs__tab"
          [attr.data-tab]="tab.name"
          [attr.aria-selected]="tab.name === selected()"
          (click)="$event.preventDefault(); open(tab.name)"
        >
          {{ tab.label }}
        </a>
      }
      <div class="gd-game-tabs__extras">
        @if (zoom.available) {
          <div class="gd-game-tabs__extra">
            <span>Auto-zoom: </span>
            <button
              type="button"
              class="gd-button gd-game-tabs__switch"
              data-toggle="auto-zoom"
              [attr.data-icon]="zoom.enabled() ? 'zoom' : 'no-zoom'"
              title="Auto-zoom cards on hover"
              aria-label="Auto-zoom cards on hover"
              [attr.aria-pressed]="zoom.enabled()"
              (click)="zoom.toggle()"
            ></button>
          </div>
        }
        @if (timer() === 'ready') {
          <div class="gd-game-tabs__extra">
            <span>Timer: </span>
            <button
              type="button"
              class="gd-button gd-game-tabs__switch"
              data-toggle="timer-mute"
              [attr.data-icon]="settings.timerAudioMuted() ? 'muted' : 'sound'"
              [title]="settings.timerAudioMuted() ? 'Timer sounds are muted' : 'Mute timer sounds'"
              aria-label="Mute timer sounds"
              [attr.aria-pressed]="settings.timerAudioMuted()"
              (click)="settings.setTimerAudioMuted(!settings.timerAudioMuted())"
            ></button>
          </div>
        } @else if (timer() === 'waiting') {
          <!-- Until the players are known the legacy page shows its switch unfinished: a plain button that does nothing -->
          <div class="gd-game-tabs__extra">
            <span>Timer: </span>
            <button type="button" class="gd-game-tabs__switch" title="Mute timer sounds">Mute timer sounds</button>
          </div>
        }
      </div>
    </div>
    <div class="gd-game-tabs__panel" role="tabpanel" data-panel="chat" [hidden]="selected() !== 'chat'">
      <gd-game-chat />
    </div>
    @if (selected() === 'settings') {
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="settings" gdKeepScroll>
        <div class="gd-game-setting">
          <label for="gd-game-foil">Foil presentation:</label>
          <select id="gd-game-foil" data-setting="foilPresentation">
            <option value="static">Static layer</option>
            <option value="animated">Animated layer</option>
            <option value="none">None</option>
          </select>
        </div>
        <div class="gd-game-setting">
          <label for="gd-game-speed">Animation speed</label>
          <span class="gd-game-slider" [style.--gd-slider-share]="(settings.animationSpeed() + 4) / 8">
            <input
              id="gd-game-speed"
              type="range"
              class="gd-slider"
              data-setting="animation-slider"
              min="-4"
              max="4"
              [value]="settings.animationSpeed()"
              (input)="settings.setAnimationSpeed(number($event))"
            />
          </span>
        </div>
        <div class="gd-game-setting">
          <input
            id="gd-game-auto-accept"
            type="checkbox"
            data-setting="autoAccept"
            [checked]="settings.autoAccept()"
            (change)="settings.setAutoAccept(checked($event))"
          /><label for="gd-game-auto-accept">Auto-accept after selecting action or card</label>
        </div>
        <div class="gd-game-setting">
          <input
            id="gd-game-dismiss"
            type="checkbox"
            data-setting="autoDismissDeckReveal"
            [checked]="settings.autoDismissDeckReveal()"
            (change)="settings.setAutoDismissDeckReveal(checked($event))"
          /><label
            for="gd-game-dismiss"
            title="When you search your own draw deck, skip the popup that shows its full contents and go straight to the choice"
            >Auto-dismiss deck reveal when searching your own deck</label
          >
        </div>
        <div class="gd-game-setting">
          <input
            id="gd-game-old-stack"
            type="checkbox"
            data-setting="use-old-stack-checkbox"
            [checked]="settings.oldStackVisuals()"
            (change)="settings.setOldStackVisuals(checked($event))"
          /><label for="gd-game-old-stack">Use old visuals for stacked cards</label>
        </div>
        <div class="gd-game-setting">
          <input
            id="gd-game-drop-down"
            type="checkbox"
            data-setting="alwaysDropDown"
            [checked]="settings.alwaysDropDown()"
            (change)="settings.setAlwaysDropDown(checked($event))"
          /><label for="gd-game-drop-down">Always display drop-down in answer selection</label>
        </div>
        <div class="gd-game-setting">
          <input
            id="gd-game-skip-confirmations"
            type="checkbox"
            data-setting="skipConfirmations"
            [checked]="settings.skipConfirmations()"
            (change)="settings.setSkipConfirmations(checked($event))"
          /><label
            for="gd-game-skip-confirmations"
            title="Answer for you any question that only asks you to confirm, such as pressing DONE on a starting fellowship that is already full. You can no longer undo the choice before it."
            >Skip extra confirmations</label
          >
        </div>
        <div class="gd-game-setting" data-group>
          <div class="gd-game-setting__title">
            Phases when game auto-passes for you, if you have no phase actions to play
          </div>
          <div class="gd-game-setting__choices">
            @for (phase of phases; track phase.name) {
              <span
                ><input
                  [id]="'gd-game-pass-' + phase.name"
                  type="checkbox"
                  [attr.data-setting]="'autoPass' + phase.name"
                  [checked]="settings.autoPassPhases().includes(phase.name)"
                  (change)="settings.setAutoPass(phase.name, checked($event))"
                /><label [for]="'gd-game-pass-' + phase.name">{{ phase.label }}</label></span
              >
            }
          </div>
        </div>
        @if (timer() === 'ready') {
          <div #rules class="gd-game-setting" data-group data-timer-alerts>
            <div class="gd-game-setting__title">Timer alert sound intervals</div>
            @for (rule of timerRows; track $index) {
              <div class="gd-game-setting__rule">
                Below
                <input
                  type="number"
                  min="0"
                  max="999"
                  data-timer-alert="timerAlertBelow"
                  aria-label="Below, minutes"
                  [value]="rule.belowMinutes"
                  (change)="timerRulesChanged(rules)"
                />
                min, every
                <input
                  type="number"
                  min="0"
                  max="600"
                  data-timer-alert="timerAlertEvery"
                  aria-label="Every, seconds"
                  [value]="rule.everySeconds"
                  (change)="timerRulesChanged(rules)"
                />
                s
              </div>
            }
            <div class="gd-game-setting__hint">(0 seconds disables a row)</div>
          </div>
        }
      </div>
    }
    @if (selected() === 'options') {
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="options" gdKeepScroll>
        @if (undoRow()) {
          <div class="gd-game-setting gd-game-tabs__undo">
            <button
              type="button"
              class="gd-button gd-game-tabs__action"
              data-button="undoGame"
              [disabled]="undo.frozen()"
              (click)="undo.openList()"
            >
              <span>Revert...</span>
            </button>
            @if (undo.policy()?.asks) {
              <input
                id="gd-game-undo-auto-decline"
                type="checkbox"
                data-setting="undoAutoDecline"
                [checked]="undo.autoDeclining()"
                (change)="undo.setAutoDecline(checked($event))"
              /><label
                for="gd-game-undo-auto-decline"
                title="Decline every revert request in this game without being asked. Only for this game."
                >Auto-decline revert requests</label
              >
            }
          </div>
        }
        <div class="gd-game-setting">
          <button type="button" class="gd-button gd-game-tabs__action" data-button="concedeGame" (click)="concede()">
            <span>Concede game</span>
          </button>
        </div>
        <div class="gd-game-setting">
          <button type="button" class="gd-button gd-game-tabs__action" data-button="cancelGame" (click)="cancel()">
            <span>Request game cancel</span>
          </button>
        </div>
      </div>
    }
    @if (selected() === 'players') {
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="players" gdKeepScroll>
        @for (user of chat.users(); track user.name) {
          {{ userLine(user) }}<br />
        }
      </div>
    }
  `,
})
export class GameTabsComponent {
  private readonly session = inject(GameSession);
  protected readonly settings = inject(GameSettings);
  protected readonly zoom = inject(AutoZoomSetting);
  protected readonly chat = inject(ChatRoom);
  protected readonly undo = inject(GameUndo);
  /** Revert... and its switch: for a player, in a game where something can be taken back (as undo.js). */
  protected readonly undoRow = computed(() => this.undo.allowed() && this.undo.playing());

  /** The open tab: kept with the game, so a reload opens it again. */
  private readonly viewState = inject(GameViewState);
  protected readonly selected = this.viewState.tab;
  protected readonly phases = AUTO_PASS_PHASES.map((name) => ({ name, label: PHASE_LABELS[name] ?? name }));

  /**
   * The timer sounds' switch: there for a player once the players are known ('ready'), and before that as the legacy
   * page shows it ('waiting'); a spectator has none.
   */
  protected readonly timer = computed<'ready' | 'waiting' | null>(() => {
    const model = this.session.model();
    if (!model.started) return 'waiting';
    // A replay has no timer: its switch goes once the replayed game starts
    return model.spectator || this.session.replaying() ? null : 'ready';
  });

  /** How many switches stand beside the tabs: two make the strip higher. */
  protected readonly extras = computed(() => (this.zoom.available ? 1 : 0) + (this.timer() !== null ? 1 : 0));

  /**
   * A spectator keeps the Options tab, as on the legacy page (which meant to take it away, but decides before it
   * knows who watches).
   */
  protected readonly tabs = computed<{ name: GameTab; label: string }[]>(() => {
    const all: { name: GameTab; label: string }[] = [
      { name: 'chat', label: 'Chat' },
      { name: 'settings', label: 'Settings' },
      { name: 'options', label: 'Options' },
      { name: 'players', label: this.chat.joined() ? `Players(${this.chat.users().length})` : 'Players' },
    ];
    // A replay has no game to concede and no room to be in
    return this.session.replaying() ? all.filter((tab) => tab.name === 'chat' || tab.name === 'settings') : all;
  });

  /**
   * The three rows of timer alert rules as the tab first shows them (a row without a rule: zeros). The fields keep
   * what the player types; a row counts once both its numbers are above zero.
   */
  protected readonly timerRows = [0, 1, 2].map((row) => {
    const rule = this.settings.timerAlertRules()[row];
    return { belowMinutes: Math.round((rule?.belowSeconds ?? 0) / 60), everySeconds: rule?.everySeconds ?? 0 };
  });

  protected timerRulesChanged(rows: HTMLElement): void {
    const numbers = (name: string) =>
      Array.from(rows.querySelectorAll<HTMLInputElement>(`[data-timer-alert="${name}"]`), (field) =>
        parseInt(field.value, 10),
      );
    const every = numbers('timerAlertEvery');
    this.settings.setTimerAlertRules(
      numbers('timerAlertBelow')
        .map((minutes, row) => ({ belowSeconds: minutes * 60, everySeconds: every[row] ?? NaN }))
        .filter((rule) => rule.belowSeconds > 0 && rule.everySeconds > 0),
    );
  }

  /** Another tab: its panel starts at its top. */
  protected open(tab: GameTab): void {
    if (tab !== this.selected()) this.viewState.panelScroll.set(0);
    this.selected.set(tab);
  }

  protected userLine(user: { name: string; role: string | null }): string {
    const mark = user.role === 'admin' ? '* ' : user.role === 'leagueAdmin' ? '+ ' : '';
    return `${mark}${user.name}`;
  }

  protected checked(event: Event): boolean {
    return (event.target as HTMLInputElement).checked;
  }

  protected number(event: Event): number {
    return Number((event.target as HTMLInputElement).value);
  }

  protected concede(): void {
    this.session.concede();
  }

  protected cancel(): void {
    this.session.cancel();
  }
}
