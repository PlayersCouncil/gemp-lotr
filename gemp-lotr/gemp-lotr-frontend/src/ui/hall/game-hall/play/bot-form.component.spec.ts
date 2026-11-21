import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RANDOM_STARTER } from '../../../../core/hall/tables/hall-tables-messages';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { openPlayOverlay } from '../../../../testing/play-overlay';

describe('BotFormComponent (Play ▸ Play Against Bots, CreateBotTable.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  it('unlocks the formats, and plays the bot with the decks chosen', async () => {
    const play = await openPlayOverlay(hall, 'bot');
    const format = play.one<HTMLSelectElement>('[data-field=format]');
    expect(format.disabled).toBe(true);
    await play.click('.gd-play-form__unlock');
    expect(format.disabled).toBe(false);
    expect(play.el.querySelector('.gd-play-form__unlock')).toBeNull();
    await play.set('[data-field=format]', 'movie');
    const botDeck = play.one<HTMLSelectElement>('[data-deck-for=bot] [data-deck=player]');
    expect(botDeck.options[0]?.textContent?.trim()).toBe(RANDOM_STARTER);
    await play.set('[data-deck-for=bot] [data-deck=player]', 'Test + deck & co');
    await play.set('[data-deck-for=player] [data-deck=library]', 'Starter');
    await play.click('.gd-play-form__create');
    const req = hall.request('/hall/solo');
    expect(hall.body(req)).toMatchObject({
      format: 'movie',
      deckName: 'Starter',
      deckSource: 'library',
      botDeckName: 'Test + deck & co',
    });
    req.flush('<result>OK</result>');
    await play.update();
    await play.update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('asks for a deck of the player’s own, and goes to Help from the format’s (i)', async () => {
    const play = await openPlayOverlay(hall, 'bot');
    await play.set('[data-deck-for=player] [data-deck=player]', '');
    await play.click('.gd-play-form__create');
    expect(play.one('.gd-play-form__result-box').getAttribute('data-kind')).toBe('error');
    await play.click('.gd-play-form__info');
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await play.click('.gd-play-form__format-link');
    expect(navigate).toHaveBeenCalledWith(['/help/formats/fotr_block'], { queryParams: {} });
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });
});
