import { Component, computed, inject } from '@angular/core';
import { ChatRoom } from '../../../core/chat/chat-room.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { AUTO_PASS_PHASES, GameSettings } from '../../../core/game/settings/game-settings';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { GameChatComponent } from './game-chat.component';

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
 * room; beside the tabs, the hover zoom's switch and the timer sounds' switch (not for a spectator).
 *
 * DOM contract: `gd-game-tabs` with `data-extras` (how many switches), holding `.gd-game-tabs__strip` with a `button[data-tab]` per tab (`aria-selected`) and
 * `[data-toggle]` auto-zoom and timer-mute; then `.gd-game-tabs__panel` (`data-panel`). Settings: `[data-setting]`
 * named as the legacy ids (foilPresentation, animation-slider, autoAccept, autoDismissDeckReveal,
 * use-old-stack-checkbox, alwaysDropDown, autoPassFELLOWSHIP ...), `[data-timer-alert]` timerAlertBelow and
 * timerAlertEvery; options: `[data-button]` concedeGame and cancelGame.
 */
@Component({
  selector: 'gd-game-tabs',
  styleUrl: './game-tabs.component.css',
  imports: [GameChatComponent],
  host: { '[attr.data-extras]': 'extras()' },
  template: `
    <div class="gd-game-tabs__strip" role="tablist">
      @for (tab of tabs(); track tab.name) {
        <button
          type="button"
          role="tab"
          class="gd-game-tabs__tab"
          [attr.data-tab]="tab.name"
          [attr.aria-selected]="tab.name === selected()"
          (click)="selected.set(tab.name)"
        >
          {{ tab.label }}
        </button>
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
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="settings">
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
      </div>
    }
    @if (selected() === 'options') {
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="options">
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
      <div class="gd-game-tabs__panel" role="tabpanel" data-panel="players">
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

  /** The open tab: kept with the game, so a reload opens it again. */
  protected readonly selected = inject(GameViewState).tab;
  protected readonly phases = AUTO_PASS_PHASES.map((name) => ({ name, label: PHASE_LABELS[name] ?? name }));

  /**
   * The timer sounds' switch: there for a player once the players are known ('ready'), and before that as the legacy
   * page shows it ('waiting'); a spectator has none.
   */
  protected readonly timer = computed<'ready' | 'waiting' | null>(() => {
    const model = this.session.model();
    return !model.started ? 'waiting' : model.spectator ? null : 'ready';
  });

  /** How many switches stand beside the tabs: two make the strip higher. */
  protected readonly extras = computed(() => (this.zoom.available ? 1 : 0) + (this.timer() !== null ? 1 : 0));

  /**
   * A spectator keeps the Options tab, as on the legacy page (which meant to take it away, but decides before it
   * knows who watches).
   */
  protected readonly tabs = computed<{ name: GameTab; label: string }[]>(() => [
    { name: 'chat', label: 'Chat' },
    { name: 'settings', label: 'Settings' },
    { name: 'options', label: 'Options' },
    { name: 'players', label: this.chat.joined() ? `Players(${this.chat.users().length})` : 'Players' },
  ]);

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
