import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HALL_SETTINGS_COOKIE } from '../../../core/hall/board/hall-section-settings.service';
import { injectGameHall } from '../../../core/hall/hall-services';
import { JoinDialog } from '../../../core/hall/play/join-dialog.service';
import { HallDialogs } from '../../../core/hall/hall-dialogs.service';
import { GameHallSectionsComponent } from './game-hall-sections.component';

describe('GameHallSectionsComponent', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    vi.useFakeTimers();
    hall = setUpHall();
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  async function render() {
    TestBed.runInInjectionContext(() => injectGameHall());
    const fixture = TestBed.createComponent(GameHallSectionsComponent);
    hall.connect();
    fixture.detectChanges();
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  const text = (e: Element | null | undefined) => e?.textContent?.replace(/\s+/g, ' ').trim() ?? '';

  it('draws the sections with their counts, open as the cookie says', async () => {
    const { el } = await render();
    const headers = Array.from(el.querySelectorAll('.gd-hall-sections__header'));
    expect(headers.map((h) => [h.getAttribute('data-section'), text(h), h.getAttribute('aria-expanded')])).toEqual([
      ['waiting', 'Waiting Tables(3)', 'true'],
      ['playing', 'Playing Tables(4)', 'true'],
      ['finished', 'Finished Tables(0)', 'false'],
      ['wc', 'World Championship Queues(1)', 'false'],
      ['recurring', 'Recurring Events(1)', 'false'],
    ]);
    const waiting = Array.from(el.querySelectorAll('table[data-section=waiting] .gd-hall-sections__row'));
    expect(waiting.map((r) => r.getAttribute('data-key'))).toEqual([
      'queue-waiting:wc_queue',
      'queue-waiting:pm1',
      'table:11',
    ]);
    expect(waiting[0]?.hasAttribute('data-bold')).toBe(true);
    expect(waiting[0]?.hasAttribute('data-played')).toBe(true);
    expect(Array.from(waiting[1]!.querySelectorAll('button')).map((b) => text(b))).toEqual([
      'Casual FotR Live Draft',
      'Leave Queue',
      'Start Now',
      'READY CHECK - 45 s',
      'Show Prizes',
    ]);
    expect(text(waiting[2]?.querySelector('.gd-hall-sections__age'))).toBe('00:10');
  });

  it('opens and closes a section, keeping it in the cookie', async () => {
    const { fixture, el } = await render();
    el.querySelector<HTMLButtonElement>('[data-section=recurring]')!.click();
    fixture.detectChanges();
    expect(hall.cookies.read(HALL_SETTINGS_COOKIE)).toBe('1|1|0|0|0|0|1|0');
    const row = el.querySelector('table[data-section=recurring] .gd-hall-sections__row');
    expect(Array.from(row!.querySelectorAll('td')).map((td) => text(td))).toEqual([
      'Fellowship Block',
      'All cards',
      'Daily FotR',
      'at 2026-11-20 19:00',
      'Swiss, minimum players: 4',
      '2 50',
      'Prize Breakdown',
    ]);
    row!.querySelector<HTMLButtonElement>('.gd-hall-sections__hint')!.click();
    expect(TestBed.inject(HallDialogs).open()[0]).toMatchObject({ title: 'Prizes details', ok: false });
  });

  it('draws the tournaments and their buttons, and joins a table through the Join dialog', async () => {
    const { el } = await render();
    const playing = Array.from(el.querySelectorAll('table[data-section=playing] .gd-hall-sections__row'));
    expect(playing.map((r) => text(r.querySelector('td:nth-child(2)')))).toEqual([
      'Swiss Tournament - Constructed - Casual Fellowship Block',
      'Swiss Tournament - Solo Draft - Casual FotR Solo Draft',
      'Tournament - Constructed - Casual Movie Block',
      'Casual Fellowship Block',
    ]);
    expect(playing[1]?.querySelector('td:nth-child(4) button')?.textContent?.trim()).toBe('9');
    expect(playing[3]?.querySelector('a[data-action=play]')?.getAttribute('href')).toBe(
      '/gemp-lotr/ng/game?gameId=g-12&participantId=alice',
    );
    el.querySelector<HTMLButtonElement>('[data-key="table:11"] [data-action=join]')!.click();
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    expect(TestBed.inject(JoinDialog).state()?.title).toBe('Join Table');
  });
});
