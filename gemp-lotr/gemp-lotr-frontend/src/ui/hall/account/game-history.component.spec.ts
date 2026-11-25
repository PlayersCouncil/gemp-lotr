import { accountRequest, answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';

describe('GameHistoryComponent', () => {
  it('draws the filters, the pagers, the status and the table, and applies and clears the filters', async () => {
    const { backend, harness } = await setUpAccountPages();
    await harness.navigateByUrl('/account/history');
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    answerAccount(backend, '/gameHistory', 'game-history.xml');
    await drawn(harness);
    const host = harness.fixture.nativeElement.querySelector('gd-account-history') as HTMLElement;
    expect(Array.from(host.querySelectorAll('select option')).map((o) => o.textContent)).toEqual([
      'Any format',
      'Fellowship Block',
      'Movie Block',
    ]);
    expect(host.querySelectorAll('datalist option')).toHaveLength(3);
    expect(host.querySelector('.gd-account-history__status')?.textContent).toBe('Showing 21–23 of 23 games.');
    const pagers = Array.from(host.querySelectorAll<HTMLElement>('.gd-account-history__pager'));
    expect(pagers.map((p) => p.hidden)).toEqual([false, false]);
    expect(pagers[0]?.querySelector('.gd-account-history__page')?.textContent).toBe('Page 2 of 2');
    expect(
      Array.from(pagers[0]!.querySelectorAll('button')).map((b) => [b.getAttribute('aria-label'), b.disabled]),
    ).toEqual([
      ['First page', false],
      ['Previous page', false],
      ['Next page', true],
      ['Last page', true],
    ]);
    const first = host.querySelector('tbody tr') as HTMLElement;
    expect(first.querySelector('[data-result]')?.getAttribute('title')).toBe('Won');
    const replay = first.querySelector('a.gd-account-history__replay') as HTMLAnchorElement;
    expect([replay.getAttribute('href'), replay.target]).toEqual([
      '/gemp-lotr/ng/game?replayId=alice%24rec1',
      '_blank',
    ]);
    expect(host.querySelectorAll('tbody i')[0]?.textContent).toBe('not stored');

    const opponent = host.querySelector('input[name=opponent]') as HTMLInputElement;
    opponent.value = 'carol';
    opponent.dispatchEvent(new Event('input'));
    (host.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    let request = accountRequest(backend, '/gameHistory');
    expect(request.request.params.get('opponent')).toBe('carol');
    request.flush('<gameHistory count="0" total="57" playerId="alice"/>');
    await drawn(harness);
    expect(host.querySelector('.gd-account-history__empty')?.textContent).toBe('No games match these filters.');

    (host.querySelector('[data-action=clear]') as HTMLButtonElement).click();
    await drawn(harness);
    expect(opponent.value).toBe('');
    request = accountRequest(backend, '/gameHistory');
    expect(request.request.params.has('opponent')).toBe(false);
    backend.match(() => true);
  });

  it('sends every filter typed, and pages with the four pager buttons', async () => {
    const { backend, harness } = await setUpAccountPages();
    await harness.navigateByUrl('/account/history');
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    const page = (start: number) =>
      `<gameHistory count="45" total="45" start="${start}" playerId="alice">` +
      '<historyEntry winner="alice" loser="bob"/>'.repeat(start === 40 ? 5 : 20) +
      '</gameHistory>';
    accountRequest(backend, '/gameHistory').flush(page(0));
    await drawn(harness);
    const host = harness.fixture.nativeElement.querySelector('gd-account-history') as HTMLElement;
    const set = (name: string, value: string, event: string) => {
      const field = host.querySelector<HTMLInputElement | HTMLSelectElement>(`[name=${name}]`)!;
      field.value = value;
      field.dispatchEvent(new Event(event));
    };
    set('format', 'Movie Block', 'change');
    set('event', 'casual', 'input');
    set('from', '2026-11-01', 'input');
    set('to', '2026-11-20', 'input');
    await drawn(harness);
    (host.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    let request = accountRequest(backend, '/gameHistory');
    const params = request.request.params;
    expect([params.get('format'), params.get('event'), params.get('from'), params.get('to')]).toEqual([
      'Movie Block',
      'casual',
      '2026-11-01',
      '2026-11-20',
    ]);
    request.flush(page(0));
    await drawn(harness);

    const pager = (which: string) =>
      host.querySelector<HTMLButtonElement>(`.gd-account-history__pager [data-page=${which}]`)!;
    for (const [which, start] of [
      ['next', '20'],
      ['last', '40'],
      ['previous', '20'],
      ['first', '0'],
    ] as const) {
      pager(which).click();
      request = accountRequest(backend, '/gameHistory');
      expect(request.request.params.get('start')).toBe(start);
      request.flush(page(Number(start)));
      await drawn(harness);
    }
    expect(host.querySelector('.gd-account-history__page')?.textContent).toBe('Page 1 of 3');
    backend.match(() => true);
  });
});
