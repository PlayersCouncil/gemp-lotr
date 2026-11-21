import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { readServerResponse } from '../../../../testing/files';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { injectGameHall } from '../../../../core/hall/hall-services';
import { JoinDialog } from '../../../../core/hall/play/join-dialog.service';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { JoinDialogComponent } from './join-dialog.component';
import { PlayOverlayComponent } from './play-overlay.component';

describe('the Play overlay and the Join dialog', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  const text = (e: Element | null | undefined) => e?.textContent?.replace(/\s+/g, ' ').trim() ?? '';

  async function open(screen?: string) {
    const fixture = TestBed.createComponent(PlayOverlayComponent);
    const el = fixture.nativeElement as HTMLElement;
    const update = async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();
    };
    TestBed.inject(PlayOverlay).open();
    hall.request('/deck/formats').flush(readServerResponse('hall-formats.json'));
    hall.request('/deck/list', 'GET').flush(readServerResponse('deck-list.xml'));
    hall
      .request('/deck/libraryList', 'GET')
      .flush('<decks><deck targetFormat="Fellowship Block">Starter</deck></decks>');
    hall.request('/tournament/tournamentFormats', 'GET').flush(readServerResponse('tournament-formats.json'));
    await update();
    if (screen) {
      el.querySelector<HTMLButtonElement>(`.gd-play-overlay__choice[data-flow=${screen}]`)!.click();
      await update();
    }
    return { fixture, el, update };
  }

  it('shows the choice, a flow behind each, and Back', async () => {
    const { el, update } = await open();
    expect(text(el.querySelector('[data-dialog-title]'))).toBe('Create Table');
    expect(Array.from(el.querySelectorAll('.gd-play-overlay__choice')).map((b) => b.getAttribute('data-flow'))).toEqual(
      ['scenario', 'bot', 'casual', 'league', 'tournament'],
    );
    el.querySelector<HTMLButtonElement>('[data-flow=bot]')!.click();
    await update();
    expect(text(el.querySelector('[data-dialog-title]'))).toBe('Create Table ▸ Bot');
    expect(el.querySelector<HTMLSelectElement>('gd-bot-form select[data-field=format]')?.disabled).toBe(true);
    el.querySelector<HTMLButtonElement>('.gd-play-overlay__back')!.click();
    await update();
    expect(el.querySelector('.gd-play-overlay__choices')).not.toBeNull();
    el.querySelector<HTMLButtonElement>('.gd-play-overlay__back')!.click();
    await update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('creates a Casual table with the deck it starts with, and shows a refusal in its result', async () => {
    const { el, update } = await open('casual');
    const field = (name: string) => el.querySelector<HTMLSelectElement>(`[data-field=${name}]`)!;
    expect(field('format').value).toBe('fotr_block');
    expect(el.querySelector<HTMLSelectElement>('[data-deck=player]')!.value).toBe('Test + deck & co');
    el.querySelectorAll<HTMLButtonElement>('.gd-play-form__info')[0]!.click();
    await update();
    expect(text(el.querySelector('.gd-play-form__info-text'))).toContain('The Deck Library contains sample decks');
    const invite = el.querySelector<HTMLInputElement>('[data-field=invite-only]')!;
    invite.click();
    await update();
    expect(field('description').disabled).toBe(true);
    expect(field('description').getAttribute('placeholder')).toBe(
      "An invite-only table's description is the invited player's name",
    );
    el.querySelector<HTMLButtonElement>('.gd-play-form__create')!.click();
    await update();
    expect(text(el.querySelector('.gd-play-form__result-box'))).toContain(
      'If set to invite-only, you must name a player',
    );
    invite.click();
    await update();
    el.querySelector<HTMLButtonElement>('.gd-play-form__create')!.click();
    const req = hall.request('/hall');
    expect(hall.body(req)).toMatchObject({ format: 'fotr_block', deckName: 'Test + deck & co', timer: 'default' });
    req.flush('<error message="Failed to create table. Please try again later."/>');
    await update();
    expect(el.querySelector('.gd-play-form__result-box')?.getAttribute('data-kind')).toBe('error');
  });

  it('shows the tournament fields of the type chosen', async () => {
    const { el, update } = await open('tournament');
    expect(el.querySelector('.gd-play-form__startup')).toBeNull();
    const type = el.querySelector<HTMLSelectElement>('[data-field=type]')!;
    type.value = 'table_draft';
    type.dispatchEvent(new Event('change'));
    await update();
    expect(text(el.querySelector('.gd-play-form__draft-info'))).toBe('FotR Table Draft');
    expect(el.querySelector('gd-deck-picker')).toBeNull();
    el.querySelector<HTMLButtonElement>('.gd-play-form__advanced-toggle')!.click();
    await update();
    expect(el.querySelector<HTMLSelectElement>('[data-field=draft-timer]')?.value).toBe('SLOW');
    expect(el.querySelector<HTMLInputElement>('[data-field=deckbuilding]')?.value).toBe('15');
  });

  it('lists the scenarios and closes when one starts', async () => {
    const { el, update } = await open('scenario');
    hall.request('/scenarios', 'GET').flush(readServerResponse('scenarios.json'));
    await update();
    expect(text(el.querySelector('.gd-scenario__current'))).toContain('You are in the middle of Ring Trouble.');
    expect(Array.from(el.querySelectorAll('.gd-scenario__folder-name')).map((e) => e.textContent)).toEqual([
      'Tutorials',
      'Basics',
      'Puzzles',
      'My scenarios',
      'Puzzles',
    ]);
    expect(el.querySelector('[data-scenario=t2] .gd-scenario__status')?.getAttribute('data-status')).toBe('next');
    el.querySelector<HTMLButtonElement>('[data-scenario=t1] [data-action=play]')!.click();
    hall.request('/scenarios/start').flush({ gameId: 'g-1' });
    await update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it("shows the league form with the Events tab's league list, and goes to Help from a format's (i)", async () => {
    const { el, update } = await open('league');
    for (const req of hall.backend.match((r) => r.url.endsWith('/league'))) {
      req.flush(readServerResponse('leagues.xml'));
    }
    await update();
    expect(
      Array.from(el.querySelectorAll<HTMLOptionElement>('[data-field=league] option')).map((o) =>
        o.textContent?.trim(),
      ),
    ).toEqual(['Constructed League', 'Race to Mount Doom']);
    expect(el.querySelectorAll('.gd-play-form__league-list gd-event-row').length).toBe(3);

    el.querySelector<HTMLButtonElement>('.gd-play-overlay__back')!.click();
    await update();
    el.querySelector<HTMLButtonElement>('[data-flow=casual]')!.click();
    await update();
    el.querySelectorAll<HTMLButtonElement>('.gd-play-form__info')[2]!.click();
    await update();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    el.querySelector<HTMLAnchorElement>('.gd-play-form__format-link')!.click();
    expect(navigate).toHaveBeenCalledWith(['/help/formats/fotr_block'], { queryParams: {} });
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('joins a table from the Join dialog with the deck it starts with', async () => {
    TestBed.runInInjectionContext(() => injectGameHall());
    const fixture = TestBed.createComponent(JoinDialogComponent);
    const el = fixture.nativeElement as HTMLElement;
    TestBed.inject(JoinDialog).openTable('13');
    hall.request('/deck/list', 'GET').flush(readServerResponse('deck-list.xml'));
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(text(el.querySelector('.gd-join__heading'))).toBe('Join Table');
    el.querySelector<HTMLButtonElement>('.gd-join__submit')!.click();
    const req = hall.request('/hall/13');
    expect(hall.body(req)).toMatchObject({ deckName: 'Test + deck & co', deckSource: 'player' });
    req.flush('<result>OK</result>');
    await fixture.whenStable();
    expect(TestBed.inject(JoinDialog).state()).toBeNull();
  });
});
