import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, failAccount, setUpAccount } from '../../../testing/account-server';
import { settle } from '../../../testing/fake-notifier';
import { NO_GAME_HISTORY_FILTERS } from './account-models';
import { GameHistory } from './game-history.service';

function historyXml(count: number, start: number, entries: number, total = count): string {
  const rows = Array.from({ length: entries }, () => '<historyEntry winner="alice" loser="bob"/>').join('');
  return `<gameHistory count="${count}" total="${total}" start="${start}" playerId="alice">${rows}</gameHistory>`;
}

describe('GameHistory', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let history: GameHistory;

  beforeEach(() => {
    ({ backend } = setUpAccount());
    history = TestBed.inject(GameHistory);
  });

  afterEach(() => backend.verify());

  async function started(xml = historyXml(45, 0, 20)): Promise<void> {
    history.start();
    expect(history.status()).toBe('Loading…');
    expect(history.pager().nextDisabled).toBe(true);
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    accountRequest(backend, '/gameHistory').flush(xml);
    await settle();
  }

  it('shows the first page, its count and the pager', async () => {
    await started();
    history.start();
    expect(history.status()).toBe('Showing 1–20 of 45 games.');
    expect(history.rows()).toHaveLength(20);
    expect(history.pager()).toEqual({
      shown: true,
      text: 'Page 1 of 3',
      firstDisabled: true,
      previousDisabled: true,
      nextDisabled: false,
      lastDisabled: false,
    });
    expect(history.formats()).toEqual(['Fellowship Block', 'Movie Block']);
    expect(history.events()[0]).toEqual({ value: 'casual', label: 'Casual games' });
    expect(history.events()[1]).toEqual({ value: 'Weekly League', label: null });
  });

  it('pages, keeping the filters applied', async () => {
    await started();
    history.apply({ ...NO_GAME_HISTORY_FILTERS, opponent: '  bo ', format: 'Movie Block' });
    let request = accountRequest(backend, '/gameHistory');
    expect([request.request.params.get('opponent'), request.request.params.get('start')]).toEqual(['bo', '0']);
    request.flush(historyXml(45, 0, 20, 99));
    await settle();
    expect(history.status()).toBe('Showing 1–20 of 45 games matching the filters (99 in all).');
    history.last();
    request = accountRequest(backend, '/gameHistory');
    expect([request.request.params.get('start'), request.request.params.get('format')]).toEqual(['40', 'Movie Block']);
    request.flush(historyXml(45, 40, 5, 99));
    await settle();
    expect(history.pager().text).toBe('Page 3 of 3');
    expect(history.pager().nextDisabled).toBe(true);
    history.next(); // already on the last page: nothing asked
    history.previous();
    expect(accountRequest(backend, '/gameHistory').request.params.get('start')).toBe('20');
  });

  it('refuses From after To, and says when nothing matches', async () => {
    await started(historyXml(0, 0, 0));
    expect(history.status()).toBe('You have not finished any games yet.');
    expect(history.emptyRow()).toBe('No games yet.');
    expect(history.pager().shown).toBe(false);
    history.apply({ ...NO_GAME_HISTORY_FILTERS, from: '2026-11-02', to: '2026-11-01' });
    expect(history.status()).toBe("The 'From' date is after the 'To' date.");
    history.apply({ ...NO_GAME_HISTORY_FILTERS, event: 'casual' });
    accountRequest(backend, '/gameHistory').flush(historyXml(0, 0, 0));
    await settle();
    expect(history.status()).toBe('No games match these filters.');
    expect(history.emptyRow()).toBe('No games match these filters.');
    history.clear();
    expect(accountRequest(backend, '/gameHistory').request.params.has('event')).toBe(false);
  });

  it("says why a page could not be loaded, with the server's words for bad filters", async () => {
    await started();
    history.apply({ ...NO_GAME_HISTORY_FILTERS, from: 'x' });
    failAccount(backend, '/gameHistory', 400, 'GET', '', { message: 'Dates must be given as yyyy-MM-dd.' });
    await settle();
    expect(history.status()).toBe('Dates must be given as yyyy-MM-dd.');
    expect(history.rows()).toHaveLength(20);
    expect(history.pager().lastDisabled).toBe(false);
    for (const [status, text] of [
      [0, 'Could not reach the server. Check your connection and try again.'],
      [401, 'You are not logged in.'],
      [503, 'Could not load your game history. Please try again later.'],
    ] as const) {
      history.clear();
      failAccount(backend, '/gameHistory', status);
      await settle();
      expect(history.status()).toBe(text);
    }
  });

  it('refreshes quietly, keeping what is shown on a failure, and drops stale answers', async () => {
    await started();
    history.refresh();
    expect(history.status()).toBe('Showing 1–20 of 45 games.');
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    failAccount(backend, '/gameHistory', 500);
    await settle();
    expect(history.status()).toBe('Showing 1–20 of 45 games.');

    history.next();
    history.clear();
    const [stale, fresh] = backend.match((request) => request.url.endsWith('/gameHistory'));
    fresh!.flush(historyXml(3, 0, 3));
    stale!.flush(historyXml(45, 20, 20));
    await settle();
    expect(history.status()).toBe('Showing 1–3 of 3 games.');
  });
});
