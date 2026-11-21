import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HALL_SETTINGS_COOKIE } from '../../../core/hall/board/hall-section-settings.service';
import { injectGameHall } from '../../../core/hall/hall-services';
import { JoinDialog } from '../../../core/hall/play/join-dialog.service';
import { HallDialogs } from '../../../core/hall/hall-dialogs.service';
import { Notifier } from '../../../core/notifier/notifier';
import { FakeNotifier, settle } from '../../../testing/fake-notifier';
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
  const button = (el: HTMLElement, key: string, action: string) =>
    el.querySelector<HTMLButtonElement>(`[data-key="${key}"] [data-action=${action}]`)!;

  it("sends a queue's buttons: Leave Queue, Start Now, READY CHECK; Show Prizes shows them", async () => {
    const { el } = await render();
    for (const [action, path] of [
      ['leave-queue', '/hall/queue/pm1/leave'],
      ['start', '/hall/queue/pm1/start'],
      ['ready-check', '/hall/queue/pm1/ready'],
    ]) {
      button(el, 'queue-waiting:pm1', action).click();
      hall.request(path).flush('<result>OK</result>');
      await settle();
    }
    button(el, 'queue-waiting:pm1', 'prizes').click();
    expect(
      TestBed.inject(HallDialogs)
        .open()
        .map((d) => d.title),
    ).toEqual(['Prizes details']);
  });

  it("opens a table draft's format page from its name, in a new tab", async () => {
    const { el } = await render();
    el.querySelector<HTMLButtonElement>('[data-key="queue-waiting:pm1"] .gd-hall-sections__hint')!.click();
    expect(hall.navigation.opened).toContain('/gemp-lotr-server/deck/draftHtml?draftCode=fotr_table_draft');
  });

  it("sends a tournament's buttons, and a long player list opens from its count", async () => {
    const { fixture, el } = await render();
    const notifier = TestBed.inject(Notifier) as FakeNotifier;
    const titles = () =>
      TestBed.inject(HallDialogs)
        .open()
        .map((d) => d.title);
    const join = TestBed.inject(JoinDialog);

    el.querySelector<HTMLButtonElement>('[data-key="tournament:t2"] .gd-hall-sections__hint')!.click();
    expect(titles()).toEqual(['Competing Players']);

    button(el, 'tournament:t2', 'go-to-draft').click();
    expect(hall.navigation.opened.at(-1)).toBe('/gemp-lotr/soloDraft.html?eventId=t2');

    button(el, 'tournament:t2', 'register-deck').click();
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    expect(join.state()?.title).toContain('Casual FotR Solo Draft');
    join.close();

    notifier.answers.push(true);
    button(el, 'tournament:t1', 'abandon').click();
    await settle();
    hall.request('/hall/tournament/t1/leave').flush('<result>OK</result>');
    await settle();
    expect(notifier.dialogs.at(-1)).toContain('Casual Fellowship Block');

    button(el, 'tournament:t3', 'late-join').click();
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    expect(join.state()?.title).toContain('Casual Movie Block');
    fixture.detectChanges();
  });

  it('joins a queue that takes no deck at once, leaves your own table, and shows finished tables with their winner', async () => {
    const { fixture, el } = await render();
    hall.poll(
      '<hall channelNumber="8">' +
        '<queue action="add" id="q2" cost="0" collection="Sealed" format="Fellowship Block" type="SEALED" queue="Casual Sealed" playerCount="1" playerList="bob" prizes="" system="Swiss" start="When 4 players join" signedUp="false" joinable="true" startable="false" readyCheckSecsRemaining="-1" confirmedReadyCheck="false" wc="false" recurring="false" scheduled="false" displayInWaitingTables="true"/>' +
        '<table action="add" id="13" status="WAITING" statusDescription="Waiting" format="Fellowship Block" tournament="Casual" userDescription="" players="alice" playing="true" watchable="false" isPrivate="false" isInviteOnly="false"/>' +
        '<table action="add" id="14" status="FINISHED" statusDescription="Finished" format="Movie Block" tournament="Casual" userDescription="" players="bob,carol" winner="carol" playing="false" watchable="false" isPrivate="false" isInviteOnly="false"/>' +
        '<table action="add" id="15" gameId="g-15" status="PLAYING" statusDescription="Playing" format="Movie Block" tournament="Casual" userDescription="no proxies" players="bob,carol" playing="false" watchable="true" isPrivate="false" isInviteOnly="false"/>' +
        '</hall>',
    );
    fixture.detectChanges();

    const watched = el.querySelector('[data-key="table:15"]')!;
    expect(text(watched.querySelector('td:nth-child(2)'))).toBe('Casual - [no proxies]');
    expect(watched.querySelector('a[data-action=watch]')?.getAttribute('href')).toContain('gameId=g-15');

    button(el, 'queue-waiting:q2', 'join-queue').click();
    hall.request('/hall/queue/q2').flush('<result>OK</result>');
    await settle();

    button(el, 'table:13', 'leave').click();
    hall.request('/hall/13/leave').flush('<result>OK</result>');
    await settle();

    el.querySelector<HTMLButtonElement>('.gd-hall-sections__header[data-section=finished]')!.click();
    fixture.detectChanges();
    const finished = el.querySelector('table[data-section=finished] [data-key="table:14"]')!;
    expect(text(finished.querySelector('td:last-child'))).toBe('carol');
  });

  it("draws an event's prizes as text when there is no breakdown, and a table draft's name as its format link", async () => {
    const { fixture, el } = await render();
    hall.poll(
      '<hall channelNumber="8"><queue action="add" id="rd" cost="0" collection="Sealed" format="Fellowship Block" type="TABLE_DRAFT" queue="Daily Draft" draftCode="fotr_table_draft" playerCount="0" playerList="" prizes="" system="Swiss" start="at 2026-11-20 20:00" signedUp="false" joinable="true" startable="false" readyCheckSecsRemaining="-1" confirmedReadyCheck="false" wc="false" recurring="true" scheduled="false" displayInWaitingTables="false"/></hall>',
    );
    for (const section of ['wc', 'recurring']) {
      el.querySelector<HTMLButtonElement>(`.gd-hall-sections__header[data-section=${section}]`)!.click();
    }
    fixture.detectChanges();
    const wc = el.querySelector('table[data-section=wc] [data-key="queue:wc_queue"]')!;
    expect(text(wc.querySelector('td:last-child'))).toBe('No prizes');
    expect(wc.querySelector('td:last-child button')).toBeNull();
    el.querySelector<HTMLButtonElement>(
      'table[data-section=recurring] [data-key="queue:rd"] .gd-hall-sections__hint',
    )!.click();
    expect(hall.navigation.opened.at(-1)).toBe('/gemp-lotr-server/deck/draftHtml?draftCode=fotr_table_draft');
  });
  it('joins a running tournament that takes no deck at once', async () => {
    const { fixture, el } = await render();
    hall.poll(
      '<hall channelNumber="8"><tournament action="add" id="t4" collection="Sealed" format="Fellowship Block" name="Casual Sealed" system="Swiss" type="SEALED" stage="Playing Games" round="1" playerCount="4" playerList="bob, carol, dave, erin" signedUp="false" abandoned="false" joinable="true" wc="false"/></hall>',
    );
    fixture.detectChanges();
    button(el, 'tournament:t4', 'late-join').click();
    const join = hall.request('/hall/tournament/t4/join');
    expect(hall.body(join)).toMatchObject({ deckName: '', deckSource: '' });
    join.flush('<response message="Joined."/>');
    await settle();
  });
});
