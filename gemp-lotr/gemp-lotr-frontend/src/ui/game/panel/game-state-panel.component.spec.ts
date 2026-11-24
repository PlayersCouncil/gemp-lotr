import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { GameStats } from '../../../core/game/protocol/game-events';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { emptyGame, GameModel } from '../../../core/game/state/game-model';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { EditTool } from '../../../core/game/table-edit/edit-ops';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { fakeEditSeating, SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { CounterPopups } from '../table-edit/counter-popups';
import { GameStatePanelComponent } from './game-state-panel.component';

const zones = { deck: 2, hand: 1, discard: 1, dead: 0, adventureDeck: 8, removed: 0 };

/** The game state panel in a sandbox: every pile opens, and its counters change where they are shown. */
describe('GameStatePanelComponent in a sandbox', () => {
  let edit: FakeTableEdit;
  const model = signal<GameModel>(emptyGame());
  const openPile = vi.fn();

  function render(level: 'sandbox' | 'none', mode: 'edit' | 'play', tools?: EditTool[]) {
    edit = fakeTableEdit({ level, mode, ...(tools ? { tools } : {}) });
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    model.set({
      ...emptyGame(),
      participantId: 'alice',
      players: ['alice', '~Scenario'],
      currentPlayer: 'alice',
      twilight: 3,
      stats: {
        playerZones: { alice: zones, '~Scenario': zones },
        threats: { alice: { value: '0', total: '1' }, '~Scenario': { value: '0', total: '-1' } },
      } as unknown as GameStats,
    });
    TestBed.configureTestingModule({
      providers: [
        { provide: GameSession, useValue: { model } },
        {
          provide: GameClocks,
          useValue: {
            view: signal({
              banks: {},
              ownBand: null,
              expired: [],
              decision: null,
              decisionBand: null,
              decisionExpired: false,
            }),
            bankPulse: signal(0),
            decisionPulse: signal(0),
          },
        },
        { provide: GameViewState, useValue: { openPiles: signal([0, 1]), openPile, togglePiles: vi.fn() } },
        { provide: TableEdit, useValue: edit },
        { provide: EditSeating, useValue: fakeEditSeating() },
        CounterPopups,
      ],
    });
    const fixture = TestBed.createComponent(GameStatePanelComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const stat = (name: string) => el.querySelector<HTMLElement>(`[data-stat="${name}"]`)!;
    const links = () => [...el.querySelectorAll<HTMLElement>('[data-link]')].map((counter) => counter.dataset['stat']);
    return { fixture, el, stat, links };
  }

  beforeEach(() => openPile.mockClear());

  it("opens every pile of both players with the piles tool, each count a link; the hand's opens the hand", () => {
    const { stat, links } = render('sandbox', 'play');
    expect(links()).toEqual([
      'deck0',
      'hand0',
      'discard0',
      'deadPile0',
      'adventureDeck0',
      'removedPile0',
      'deck1',
      'hand1',
      'discard1',
      'deadPile1',
      'adventureDeck1',
      'removedPile1',
    ]);
    stat('hand1').click();
    expect(openPile).toHaveBeenLastCalledWith('hand', '~Scenario');
    stat('deck0').click();
    expect(openPile).toHaveBeenLastCalledWith('deck', 'alice');
    expect(stat('hand0').dataset['pile']).toBe('hand');
  });

  it("opens no hidden pile without the piles tool: the draw decks, the other's hand and adventure deck", () => {
    const { stat, links } = render('sandbox', 'play', ['add']);
    expect(links()).toEqual([
      'hand0',
      'discard0',
      'deadPile0',
      'adventureDeck0',
      'removedPile0',
      'discard1',
      'deadPile1',
      'removedPile1',
    ]);
    stat('hand1').click();
    expect(openPile).not.toHaveBeenCalled();
  });

  it('outside a sandbox opens the piles as always, and no count is a link', () => {
    const { stat, links, el } = render('none', 'play');
    expect(links()).toEqual([]);
    expect(stat('hand1').hasAttribute('data-clickable')).toBe(false);
    expect(stat('discard1').hasAttribute('data-clickable')).toBe(false);
    expect(stat('deck0').hasAttribute('data-clickable')).toBe(false);
    expect(el.querySelector('gd-edit-counter')).toBeNull();
    expect(el.querySelector('[data-game="twilight"]')?.textContent).toBe('3');
    expect(stat('threats0').textContent).toBe('0/1');
    expect(el.querySelector('[data-game="burdens"]')).toBeNull();
  });

  it('changes the twilight pool, the burdens and the threats of the player whose turn it is, while edited', () => {
    const { fixture, el, stat } = render('sandbox', 'edit');
    const counters = [...el.querySelectorAll<HTMLElement>('button.gd-edit-counter')];
    expect(counters.map((counter) => [counter.dataset['editCounter'], counter.textContent.trim()])).toEqual([
      ['threats', '0/1'],
      ['twilight', '3'],
      ['burdens', '1'],
    ]);
    expect(stat('threats0').querySelector('gd-edit-counter')).not.toBeNull();
    expect(stat('threats1').textContent).toBe('0');
    expect(el.querySelector('[data-game="burdens"]')?.textContent).toContain('Burdens:');
    // (the threats as the table has them: the game's stats are sent only as it moves)
    edit.board.update((board) => board && { ...board, threats: 1 });
    fixture.detectChanges();
    expect(stat('threats0').textContent.trim()).toBe('1/1');
    model.update((now) => ({
      ...now,
      stats: { ...now.stats!, threats: { alice: { value: '1', total: '-1' } } },
    }));
    fixture.detectChanges();
    expect(stat('threats0').textContent.trim()).toBe('1');
    model.update((now) => ({ ...now, stats: { ...now.stats!, threats: {} } }));
    fixture.detectChanges();
    expect(stat('threats0').textContent.trim()).toBe('1');
  });

  it("counts the piles as the sandbox's table has them while edited, as the game's stats while played", () => {
    const counts = (el: HTMLElement) =>
      ['deck0', 'hand0', 'discard0', 'adventureDeck1', 'removedPile1'].map((name) =>
        el.querySelector(`[data-stat="${name}"]`)?.textContent.trim(),
      );
    expect(counts(render('sandbox', 'edit').el)).toEqual(['3', '0', '0', '2', '0']);
    TestBed.resetTestingModule();
    expect(counts(render('sandbox', 'play').el)).toEqual(['2', '1', '1', '8', '0']);
  });

  it('keeps the counters as they are while played, and without the counters tool', () => {
    const { el } = render('sandbox', 'play');
    expect(el.querySelector('gd-edit-counter')).toBeNull();
    TestBed.resetTestingModule();
    const without = render('sandbox', 'edit', ['add', 'piles']);
    expect(without.el.querySelector('gd-edit-counter')).toBeNull();
  });
});
