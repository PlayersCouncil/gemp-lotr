import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HallDialogs } from '../../../../core/hall/hall-dialogs.service';
import * as w from '../../../../core/hall/play/play-messages';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { openPlayOverlay } from '../../../../testing/play-overlay';

describe('TournamentFormComponent (Play ▸ Create Tournament, CreateTournament.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  it('shows each field’s (i) text under it, and sends a Sealed tournament with the advanced settings', async () => {
    const play = await openPlayOverlay(hall, 'tournament');
    expect(play.text('h1')).toBe(w.TOURNAMENT_HEADING);
    await play.set('[data-field=type]', 'sealed');
    expect(play.one<HTMLSelectElement>('[data-field=format]').value).toBe('single_fotr_block_sealed');
    expect(play.el.querySelector('gd-deck-picker')).toBeNull();

    // every (i) in turn: its text shows under its row, and goes again on a second press
    await play.click('.gd-play-form__advanced-toggle');
    expect(play.text('.gd-play-form__advanced-toggle')).toBe(w.HIDE_ADVANCED);
    const infos = () => Array.from(play.el.querySelectorAll<HTMLButtonElement>('.gd-play-form__info'));
    expect(infos().map((b) => b.title)).toEqual(Array(7).fill(w.INFO_TOGGLE_TITLE));
    const helps = [
      w.HELP_PLAYERS,
      w.HELP_PAIRING,
      w.HELP_DECKBUILDING,
      w.HELP_COMPETITIVE,
      w.HELP_EARLY_START,
      w.HELP_READY_CHECK,
    ];
    for (const [i, help] of helps.entries()) {
      infos()[i + 1]!.click();
      await play.update();
      expect(play.text('.gd-play-form__info-text')).toBe(help.replace(/\s+/g, ' ').trim());
      infos()[i + 1]!.click();
      await play.update();
      expect(play.el.querySelector('.gd-play-form__info-text')).toBeNull();
    }
    infos()[0]!.click();
    await play.update();
    expect(play.el.querySelector('gd-format-info')).not.toBeNull();

    await play.set('[data-field=players]', '6');
    await play.set('[data-field=pairing]', 'SINGLE_ELIMINATION');
    await play.set('[data-field=deckbuilding]', '45');
    await play.set('[data-field=competitive]', 'true');
    await play.set('[data-field=early-start]', 'false');
    await play.set('[data-field=ready-check]', '120');
    await play.click('.gd-play-form__create');
    const req = hall.request('/tournament/create');
    expect(hall.body(req)).toMatchObject({
      type: 'sealed',
      formatCode: 'single_fotr_block_sealed',
      maxPlayers: '6',
      playoff: 'SINGLE_ELIMINATION',
      deckbuildingDuration: '45',
      competitive: 'true',
      startable: 'false',
      readyCheck: '120',
      deckName: '',
    });
    req.flush({ response: 'OK' });
    await play.update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
    expect(
      TestBed.inject(HallDialogs)
        .open()
        .map((d) => d.title),
    ).toEqual([w.TOURNAMENT_CREATED_TITLE]);
  });

  it("opens a live draft's format page, and keeps the draft timer chosen", async () => {
    const play = await openPlayOverlay(hall, 'tournament');
    await play.set('[data-field=type]', 'table_draft');
    await play.click('.gd-play-form__draft-info');
    expect(hall.navigation.opened).toEqual(['/gemp-lotr-server/deck/draftHtml?draftCode=fotr_table_draft']);
    await play.click('.gd-play-form__advanced-toggle');
    const infos = Array.from(play.el.querySelectorAll<HTMLButtonElement>('.gd-play-form__info'));
    infos[infos.length - 1]!.click();
    await play.update();
    expect(play.text('.gd-play-form__info-text')).toBe(w.HELP_DRAFT_TIMER);
    expect(
      Array.from(play.el.querySelectorAll('[data-field=draft-timer] option')).map((o) => o.textContent?.trim()),
    ).toEqual(['Classic', 'Slow', 'No Timer']);
    await play.set('[data-field=draft-timer]', 'NO_TIMER');
    await play.click('.gd-play-form__create');
    const req = hall.request('/tournament/create');
    expect(hall.body(req)).toMatchObject({ type: 'table_draft', tableDraftTimer: 'NO_TIMER' });
    req.flush({ error: 'You are already in a tournament.' });
    await play.update();
    expect(play.one('.gd-play-form__result-box').getAttribute('data-kind')).toBe('error');
    expect(play.text('.gd-play-form__result-box')).toContain('You are already in a tournament.');
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(true);
  });

  it('sends a Constructed tournament with the deck picked by hand, which a change of format leaves alone', async () => {
    const play = await openPlayOverlay(hall, 'tournament');
    await play.set('[data-field=type]', 'constructed');
    const decks = play.el.querySelectorAll<HTMLSelectElement>('gd-deck-picker select');
    expect(decks.length).toBe(2);
    await play.set('[data-deck=library]', 'Starter');
    await play.set('[data-field=format]', 'movie');
    expect(play.one<HTMLSelectElement>('[data-deck=library]').value).toBe('Starter');
    await play.set('[data-field=players]', '0');
    await play.click('.gd-play-form__create');
    expect(play.text('.gd-play-form__result-box')).toContain(w.ENTER_PLAYER_COUNT);
    await play.set('[data-field=players]', '2');
    await play.click('.gd-play-form__create');
    const req = hall.request('/tournament/create');
    expect(hall.body(req)).toMatchObject({
      type: 'constructed',
      formatCode: 'movie',
      deckName: 'Starter',
      deckSource: 'library',
      maxPlayers: '2',
    });
    req.flush({ response: 'OK' });
    await play.update();
  });

  it('says when the tournament formats could not be loaded, before any type is chosen', async () => {
    const play = await openPlayOverlay(hall, 'tournament', { tournamentFormatsStatus: 500 });
    expect(play.el.querySelector('.gd-play-form__startup')).toBeNull();
    expect(play.one('gd-result-box .gd-play-form__result-box').getAttribute('data-kind')).toBe('error');
    expect(play.text('.gd-play-form__result-box')).toContain('Could not load the tournament formats');
  });
});
