import { computed, inject, Injectable, signal } from '@angular/core';
import { ChatService } from '../chat/chat.service';
import { PlayerInfo } from '../session/player-info.service';
import { hasPlayerType, PlayerType } from '../session/player';
import { HallComms } from './hall-comms.service';

/**
 * Logged in or not (hallLinks.js GempHallSession). The hall's live parts (the poll and the chat) start at once when
 * the hall opens on a tab that needs a logged-in player. On a public tab (Help, Server Info) the player is asked for
 * first, quietly: a logged-in player's hall then starts as usual, while a visitor reads on with no "not logged in"
 * dialog (the readout and the chat say so). The hall starts once the visitor opens a tab that needs logging in.
 */
@Injectable()
export class HallSession {
  private readonly comms = inject(HallComms);
  private readonly chat = inject(ChatService);
  private readonly player = inject(PlayerInfo);

  readonly started = signal(false);
  private readonly visitor = signal(false);
  /** A request found the viewer not logged in. */
  readonly loggedOut = computed(() => this.visitor() || this.comms.loggedOut());
  /** The "Not logged in" dialog is open (only one at a time). */
  readonly loginRequired = signal(false);
  /** The Admin tab shows for admins and league admins. */
  readonly adminTabShown = computed(() => {
    const player = this.player.player();
    return hasPlayerType(player, PlayerType.admin) || hasPlayerType(player, PlayerType.leagueAdmin);
  });

  /** The hall opened on a tab that needs logging in (`tabNeedsLogin`) or on a public one. */
  async begin(tabNeedsLogin: boolean): Promise<void> {
    if (tabNeedsLogin) {
      this.start();
      return;
    }
    let player;
    try {
      player = await this.player.whoAmI();
    } catch {
      // any answer but "not logged in" starts the hall, whose own error handling takes over
      this.start();
      return;
    }
    if (player) {
      this.start();
    } else {
      this.markVisitor();
    }
  }

  /** The poll, the chat and the player info (for the Admin tab) go live; once. */
  start(): void {
    if (this.started()) {
      return;
    }
    this.started.set(true);
    this.chat.start();
    this.comms.start();
    this.player.whoAmI().catch(() => undefined);
  }

  /**
   * The page is left: the poll and the chat stop (the hall's services belong to its route, whose injector outlives
   * the page). Showing the page again starts them afresh (begin()).
   */
  stop(): void {
    if (!this.started()) {
      return;
    }
    this.started.set(false);
    this.comms.stop();
    this.chat.stop();
  }

  /** The viewer opened the Game Hall, Events, My Account or Admin. */
  loginTabOpened(): void {
    if (!this.started()) {
      this.start();
    } else if (this.loggedOut()) {
      this.requireLogin();
    }
  }

  /**
   * A request needed a logged-in player: the dialog shows when `show` (Play was pressed, or a tab that needs logging
   * in is showing); on Help or Server Info it waits until such a tab is opened (loginTabOpened).
   */
  requireLogin(show = true): void {
    this.visitor.set(true);
    if (show) {
      this.loginRequired.set(true);
    }
  }

  dismissLoginRequired(): void {
    this.loginRequired.set(false);
  }

  private markVisitor(): void {
    this.visitor.set(true);
    if (this.started()) {
      return;
    }
    this.comms.markLoggedOut();
    this.chat.showLoggedOut();
  }
}
