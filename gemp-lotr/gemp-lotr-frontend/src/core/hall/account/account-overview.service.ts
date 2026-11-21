import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { ChatApi } from '../../chat/chat-api.service';
import { IgnoreResult } from '../../chat/chat-models';
import { ChatService } from '../../chat/chat.service';
import { Notifier } from '../../notifier/notifier';
import { PageNavigation } from '../../platform/page-navigation';
import { PlayerInfo } from '../../session/player-info.service';
import { price } from '../events/events-parsers';
import { Price } from '../events/events-models';
import { HallComms } from '../hall-comms.service';
import { HallSession } from '../hall-session.service';
import { AccountApi } from './account-api.service';
import {
  IGNORE_NAME_MISSING,
  IGNORES_FAILED,
  IGNORES_NOT_LOGGED_IN,
  IGNORES_UNREACHABLE,
  MIGRATE_QUESTION,
  serverErrorText,
} from './account-messages';

/**
 * My Account > My Account (account.html): the player's name, the pocket, the trophies migration, and the ignore list.
 *
 * The ignore list is the chat's (`ChatService.ignored`), so the user list's menu and this list show each other's
 * changes, as the legacy "gemp:ignores-changed" event made them. Its calls are the chat's (`ChatApi`). The "Ignore a
 * player" field is the hall's player picker (the Casual table's invite field); its value is what `add` is given.
 */
@Injectable()
export class AccountOverview {
  private readonly api = inject(AccountApi);
  private readonly chatApi = inject(ChatApi);
  private readonly chat = inject(ChatService);
  private readonly player = inject(PlayerInfo);
  private readonly comms = inject(HallComms);
  private readonly session = inject(HallSession);
  private readonly notifier = inject(Notifier);
  private readonly navigation = inject(PageNavigation);
  private readonly status_ = signal('');

  /** "My Account: <name>". */
  readonly name = computed(() => this.player.name() ?? '');
  /** The player's money from the hall's latest answer (drawn with the gold and silver images); null before it. */
  readonly pocket = computed<Price | null>(() => {
    const currency = this.comms.currency();
    return currency === null ? null : price(currency);
  });
  /** "Migrate Trophies to My Cards" shows for a player who still has a Trophies collection. */
  readonly migrateShown = computed(() => this.player.player()?.hasTrophies === true);
  /** The ignored players (the server's order, sorted); null until the list has come ("You are not ignoring anyone." waits for it). */
  readonly ignored = computed(() => this.chat.ignored());
  /** The status line under the field (a live region in the view). */
  readonly status = this.status_.asReadonly();

  /** AccountIgnores.load: when the tab opens, and when the viewer comes back to it after a while. */
  async loadIgnores(): Promise<void> {
    try {
      this.chat.ignored.set(await firstValueFrom(this.chatApi.ignoredPlayers()));
    } catch (error) {
      this.failed(error);
    }
  }

  /**
   * "Ignore" (AccountIgnores.add): the field's name, trimmed. True when the field is to be emptied: the server took
   * the name (added, or already on the list); false when it was refused or the call failed.
   */
  async add(name: string): Promise<boolean> {
    const trimmed = name.trim();
    if (trimmed === '') {
      this.status_.set(IGNORE_NAME_MISSING);
      return false;
    }
    try {
      const result = await firstValueFrom(this.chatApi.setIgnored(trimmed, true));
      this.changed(result);
      return !result.error;
    } catch (error) {
      this.failed(error);
      return false;
    }
  }

  /** A row's "Remove" (AccountIgnores.remove). */
  async remove(name: string): Promise<void> {
    try {
      this.changed(await firstValueFrom(this.chatApi.setIgnored(name, false)));
    } catch (error) {
      this.failed(error);
    }
  }

  /**
   * "Migrate Trophies to My Cards" (migrate, GempLotrHallUI.MigrateTrophies): asked first in the browser's own
   * confirm; done, the page is loaded again.
   */
  async migrateTrophies(): Promise<void> {
    if (!(await this.notifier.confirm(MIGRATE_QUESTION))) {
      return;
    }
    try {
      await firstValueFrom(this.api.migrateTrophies());
    } catch (error) {
      this.requestFailed(ApiError.from(error));
      return;
    }
    this.navigation.reload();
  }

  /** AccountIgnores.changed */
  private changed(result: IgnoreResult): void {
    if (result.ignored) {
      this.chat.ignored.set(result.ignored);
    }
    if (result.message) {
      this.status_.set(result.message);
    }
  }

  /** AccountIgnores.errorMap; any other status is the hall's (requestFailed). */
  private failed(error: unknown): void {
    const failure = ApiError.from(error);
    const known: Readonly<Record<number, string>> = {
      0: IGNORES_UNREACHABLE,
      401: IGNORES_NOT_LOGGED_IN,
      500: IGNORES_FAILED,
    };
    const message = known[failure.status];
    if (message !== undefined) {
      this.status_.set(message);
    } else {
      this.requestFailed(failure);
    }
  }

  /**
   * hallUi.js requestFailed: 401 is the "Not logged in" dialog; no answer or 504 nothing (the readout says it); an
   * answer that could not be read nothing (the legacy page's jQuery took it for a 200); any other the "Server error"
   * dialog.
   */
  private requestFailed(error: ApiError): void {
    if (error.is(401)) {
      this.session.requireLogin();
    } else if (!error.is(0) && !error.is(504) && !error.is(INVALID_RESPONSE_STATUS)) {
      this.notifier.tell(serverErrorText(error.status));
    }
  }
}
