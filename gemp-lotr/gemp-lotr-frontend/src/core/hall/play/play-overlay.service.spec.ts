import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallDialogs } from '../hall-dialogs.service';
import { HallSession } from '../hall-session.service';
import { CASUAL_DEFAULT_FLOW_COOKIE, HallTablesService } from '../tables/hall-tables.service';
import { PlayOverlay } from './play-overlay.service';

describe('PlayOverlay', () => {
  let hall: HallTestBed;
  let overlay: PlayOverlay;

  beforeEach(() => {
    hall = setUpHall();
    overlay = TestBed.inject(PlayOverlay);
  });

  afterEach(() => hall.finish());

  /** What opening asks for: formats, decks, and the first time the tournament formats. */
  function answerOpen(first = true): void {
    hall.request('/deck/formats').flush(readServerResponse('hall-formats.json'));
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    if (first) {
      hall.request('/tournament/tournamentFormats', 'GET').flush(readServerResponse('tournament-formats.json'));
    }
  }

  it('opens on the choice, goes to a flow and back, and closes from the choice', () => {
    expect(overlay.open()).toBe(true);
    answerOpen();
    expect(overlay.title()).toBe('Create Table');
    expect(overlay.choices.map((c) => c.title)).toEqual([
      'Play Scenario or Tutorial',
      'Play Against Bots (Beta)',
      'Open Casual Table',
      'Open League Table',
      'Create Tournament',
    ]);
    overlay.choose('bot');
    expect(overlay.title()).toBe('Create Table ▸ Bot');
    overlay.back();
    expect(overlay.screen()).toBe('choice');
    overlay.back();
    expect(overlay.isOpen()).toBe(false);
  });

  it('goes straight to the Casual form when that is the default, and invites from the chat', () => {
    hall.cookies.write(CASUAL_DEFAULT_FLOW_COOKIE, 'true');
    overlay.open();
    answerOpen();
    expect(overlay.title()).toBe('Create Table ▸ Casual');
    overlay.inviteToTable('* bob');
    answerOpen(false);
    expect(TestBed.inject(HallTablesService).invitee()).toBe('bob');
  });

  it('fetches what a flow shows when it is chosen', () => {
    overlay.open();
    answerOpen();
    overlay.choose('league');
    hall.request('/league', 'GET').flush('<leagues/>');
    overlay.choose('scenario');
    hall.request('/scenarios', 'GET').flush('{}');
    expect(overlay.title()).toBe('Create Table ▸ Scenarios & Tutorials');
  });

  it('keeps the bot on Fellowship Block until unlocked, with the warning', () => {
    expect(overlay.botFormat('movie')).toBe('fotr_block');
    overlay.unlockBotFormats();
    expect(overlay.botFormat('movie')).toBe('movie');
    expect(TestBed.inject(HallDialogs).open()[0].title).toBe('Here There Be Dragons');
  });

  it('asks a visitor to log in instead', () => {
    TestBed.inject(HallSession).requireLogin(false);
    expect(overlay.open()).toBe(false);
    expect(overlay.isOpen()).toBe(false);
  });
});
