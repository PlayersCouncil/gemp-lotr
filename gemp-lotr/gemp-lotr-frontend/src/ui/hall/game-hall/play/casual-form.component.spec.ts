import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import * as w from '../../../../core/hall/play/play-messages';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { deckMemoryCookie } from '../../../../core/hall/tables/hall-deck-memory';
import { CANNOT_INVITE_SELF } from '../../../../core/hall/tables/hall-tables-messages';
import {
  CASUAL_DECK_FLOW,
  CASUAL_DEFAULT_FLOW_COOKIE,
  HallTablesService,
} from '../../../../core/hall/tables/hall-tables.service';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { openPlayOverlay } from '../../../../testing/play-overlay';

describe('CasualFormComponent (Play ▸ Open Casual Table, CreateUnrankedTable.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  it('shows each (i) text under its row, the chosen timer’s own description', async () => {
    const play = await openPlayOverlay(hall, 'casual');
    const infos = () => Array.from(play.el.querySelectorAll<HTMLButtonElement>('.gd-play-form__info'));
    // the deck picker's own (i) comes first; then Description, Format, Timer, Invite, Private, Keep open, Default
    const rows = infos().slice(-7);
    const expected = [
      w.HELP_DESCRIPTION,
      null,
      'Each player has a total time bank of 45 minutes',
      w.HELP_INVITE,
      w.HELP_PRIVATE,
      w.HELP_KEEP_OPEN,
      w.HELP_DEFAULT_FLOW,
    ];
    for (const [i, help] of expected.entries()) {
      rows[i]!.click();
      await play.update();
      if (help === null) {
        expect(play.el.querySelector('gd-format-info')).not.toBeNull();
      } else {
        expect(play.text('.gd-play-form__info-text')).toContain(help.replace(/\s+/g, ' ').trim().slice(0, 40));
      }
      rows[i]!.click();
      await play.update();
    }
    expect(play.el.querySelector('.gd-play-form__info-text')).toBeNull();
    await play.set('[data-field=timer]', 'blitz');
    rows[2]!.click();
    await play.update();
    expect(play.text('.gd-play-form__info-text')).toContain('25 minutes');
  });

  it('sends a private, kept-open table with its description, format and timer, and stays open', async () => {
    const play = await openPlayOverlay(hall, 'casual');
    await play.set('[data-field=description]', 'Casual fun');
    await play.set('[data-field=format]', 'movie');
    await play.set('[data-field=timer]', 'blitz');
    await play.set('[data-field=private]', 'true');
    await play.set('[data-field=keep-open]', 'true');
    await play.click('.gd-play-form__create');
    const req = hall.request('/hall');
    expect(hall.body(req)).toMatchObject({
      format: 'movie',
      timer: 'blitz',
      desc: 'Casual fun',
      isPrivate: 'true',
      isInviteOnly: 'false',
      deckName: 'Test + deck & co',
    });
    req.flush('<result>OK</result>');
    await play.update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(true);
    expect(play.one('.gd-play-form__result-box').getAttribute('data-kind')).toBe('success');
  });

  it('remembers "open this flow by default" in the legacy cookie', async () => {
    const play = await openPlayOverlay(hall, 'casual');
    await play.set('[data-field=default-flow]', 'true');
    expect(hall.cookies.read(CASUAL_DEFAULT_FLOW_COOKIE)).toBe('true');
    await play.set('[data-field=default-flow]', 'false');
    expect(hall.cookies.read(CASUAL_DEFAULT_FLOW_COOKIE)).toBe('false');
  });

  it('follows a library deck picked by hand with its format, and keeps it when the format changes', async () => {
    const play = await openPlayOverlay(hall, 'casual');
    await play.set('[data-field=format]', 'movie');
    await play.set('[data-deck=library]', 'Starter');
    expect(play.one<HTMLSelectElement>('[data-field=format]').value).toBe('fotr_block');
    await play.set('[data-field=format]', 'pc_fotr_block');
    await play.click('.gd-play-form__create');
    const req = hall.request('/hall');
    expect(hall.body(req)).toMatchObject({ format: 'pc_fotr_block', deckName: 'Starter', deckSource: 'library' });
    req.flush('<result>OK</result>');
    await play.update();
    await play.update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('brings back the deck last used in a format picked by hand', async () => {
    hall.cookies.write(deckMemoryCookie(CASUAL_DECK_FLOW), JSON.stringify({ movie: ['Starter', 'library'] }));
    const play = await openPlayOverlay(hall, 'casual');
    await play.set('[data-field=format]', 'movie');
    expect(play.one<HTMLSelectElement>('[data-deck=library]').value).toBe('Starter');
  });

  it('opens invite-only for the player named by "Invite to table", and refuses inviting oneself', async () => {
    const play = await openPlayOverlay(hall, 'casual');
    TestBed.inject(HallTablesService).requestInvite('+ alice');
    await play.update();
    expect(play.one<HTMLInputElement>('[data-field=invite-only]').checked).toBe(true);
    expect(play.one<HTMLInputElement>('gd-player-picker input').value).toBe('alice');
    await play.click('.gd-play-form__create');
    expect(play.text('.gd-play-form__result-box')).toContain(CANNOT_INVITE_SELF);
  });
});
