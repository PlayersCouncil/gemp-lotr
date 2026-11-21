import { computed, inject, Injectable, signal } from '@angular/core';
import { HallDialogs, pageMarkup } from '../hall-dialogs.service';
import { BOT_DEFAULT_FORMAT, HallTablesService } from '../tables/hall-tables.service';
import { CreateTournament } from './create-tournament.service';
import { LeagueTableFlow } from './league-table.service';
import { PLAY_CHOICES, PLAY_TITLE, PlayFlow, playFlowTitle, DRAGONS_TEXT, DRAGONS_TITLE } from './play-messages';
import { PlayerSearch } from './player-picker';
import { PlayScenario } from './scenarios/play-scenario.service';
import { orIfEmpty } from '../json-values';

/** What the overlay shows: the choice of flows, or one flow's form. */
export type PlayScreen = 'choice' | PlayFlow;

/**
 * The Play overlay (CreateTable): the choice of five flows (Scenarios, Bot, Casual, League, Tournament) and the form
 * behind each, with Back to the choice (or, from the choice, closing it). Opening it refreshes the formats, timers and
 * decks, resets every result box and the tournament form, and with "Open this flow by default" goes straight to the
 * Casual form. A visitor gets the "Not logged in" dialog instead.
 */
@Injectable()
export class PlayOverlay {
  private readonly tables = inject(HallTablesService);
  private readonly tournament = inject(CreateTournament);
  private readonly league = inject(LeagueTableFlow);
  private readonly scenarios = inject(PlayScenario);
  private readonly players = inject(PlayerSearch);
  private readonly dialogs = inject(HallDialogs);

  readonly isOpen = signal(false);
  readonly screen = signal<PlayScreen>('choice');
  /** "Create Table", or "Create Table ▸ Casual" on a form. */
  readonly title = computed(() => {
    const screen = this.screen();
    const choice = PLAY_CHOICES.find((c) => c.flow === screen);
    return choice ? playFlowTitle(choice.flowName) : PLAY_TITLE;
  });
  readonly choices = PLAY_CHOICES;

  /** The bot's format can be changed (Unlock Formats was pressed; until then Fellowship Block). */
  readonly botFormatsUnlocked = signal(false);

  /** Play was pressed. False for a visitor (who gets the "Not logged in" dialog). */
  open(): boolean {
    if (!this.tables.openPlay()) {
      return false;
    }
    this.tournament.resetForm();
    this.scenarios.resetResult();
    this.screen.set('choice');
    this.isOpen.set(true);
    void this.tournament.loadFormats();
    if (this.tables.casualDefaults().openByDefault) {
      this.choose('casual');
    }
    return true;
  }

  /** A flow chosen on the choice screen. */
  choose(flow: PlayFlow): void {
    this.screen.set(flow);
    if (flow === 'league') {
      void this.league.show();
    } else if (flow === 'scenario') {
      void this.scenarios.refresh();
    }
  }

  /** "< Back": to the choice, or from the choice, closed. */
  back(): void {
    if (this.screen() === 'choice') {
      this.close();
    } else {
      this.screen.set('choice');
    }
  }

  close(): void {
    this.isOpen.set(false);
    this.screen.set('choice');
  }

  /** Contract C1, the chat's "Invite to table": the Casual form, invite-only for that player. */
  inviteToTable(name: string): void {
    if (!this.open()) {
      return;
    }
    if (this.screen() !== 'casual') {
      this.choose('casual');
    }
    this.tables.requestInvite(name);
    this.players.pin(name);
  }

  /** The bot's format to start with: Fellowship Block unless unlocked (CreateBotTable.show). */
  botFormat(current: string | null): string {
    return this.botFormatsUnlocked() ? orIfEmpty(current, BOT_DEFAULT_FORMAT) : BOT_DEFAULT_FORMAT;
  }

  /** Unlock Formats: the warning, and the format can be changed. */
  unlockBotFormats(): void {
    this.botFormatsUnlocked.set(true);
    this.dialogs.show(DRAGONS_TITLE, pageMarkup(DRAGONS_TEXT));
  }

  /** The format (i)'s link: the overlay closes and the view goes to the route (GempFormatHelp.show). */
  formatHelpOpened(): void {
    this.close();
  }
}
