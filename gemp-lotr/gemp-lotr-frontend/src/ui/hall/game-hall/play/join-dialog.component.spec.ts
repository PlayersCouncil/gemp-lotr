import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HallBoard } from '../../../../core/hall/board/hall-board.service';
import { injectGameHall } from '../../../../core/hall/hall-services';
import { JoinDialog } from '../../../../core/hall/play/join-dialog.service';
import { readServerResponse } from '../../../../testing/files';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { JoinDialogComponent } from './join-dialog.component';

describe('JoinDialogComponent (the Join dialog, SelectDeck.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    vi.useFakeTimers();
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  async function render() {
    TestBed.runInInjectionContext(() => injectGameHall());
    const fixture = TestBed.createComponent(JoinDialogComponent);
    const el = fixture.nativeElement as HTMLElement;
    const update = async () => {
      fixture.detectChanges();
      await vi.runOnlyPendingTimersAsync();
      fixture.detectChanges();
    };
    return { fixture, el, update };
  }

  it("joins a Constructed queue with a library deck picked by hand, under the queue's name and start", async () => {
    const { el, update } = await render();
    hall.connect();
    void TestBed.inject(JoinDialog).joinQueue(TestBed.inject(HallBoard).events().queue('daily')!);
    hall.request('/deck/list', 'GET').flush(readServerResponse('deck-list.xml'));
    hall
      .request('/deck/libraryList', 'GET')
      .flush('<decks><deck targetFormat="Fellowship Block">Starter</deck></decks>');
    await update();
    expect(el.querySelector('.gd-join__heading')?.textContent).toBe('Join Queue: Daily FotR');
    expect(el.querySelector('.gd-join__subtitle')?.textContent).toBe(
      'Format: Fellowship Block · Starts: at 2026-11-20 19:00',
    );
    const library = el.querySelector<HTMLSelectElement>('[data-deck=library]')!;
    library.value = 'Starter';
    library.dispatchEvent(new Event('change'));
    await update();
    el.querySelector<HTMLButtonElement>('.gd-join__submit')!.click();
    const req = hall.request('/hall/queue/daily');
    expect(hall.body(req)).toMatchObject({ deckName: 'Starter', deckSource: 'library' });
    req.flush('<result>OK</result>');
    await update();
    expect(TestBed.inject(JoinDialog).state()).toBeNull();
  });

  it('closes from its close button, and opens again from the flow’s memory', async () => {
    const { el, update } = await render();
    TestBed.inject(JoinDialog).openTable('13');
    hall.request('/deck/list', 'GET').flush(readServerResponse('deck-list.xml'));
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    await update();
    expect(el.querySelector('.gd-join__subtitle')).toBeNull();
    el.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    await update();
    expect(TestBed.inject(JoinDialog).state()).toBeNull();
  });
});
