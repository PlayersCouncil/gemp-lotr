import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { emptyGame, GameCard, GameModel } from '../../../core/game/state/game-model';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { PileName } from '../../../core/game/table/game-table';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { fakeEditSeating, SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { EditPopups } from '../table-edit/edit-popups';
import { PILE_READ_MS, PileDialogsComponent } from './pile-dialogs.component';

const card = (id: string, zone: string, owner: string, blueprintId = '1_50'): GameCard => ({
  id,
  blueprintId,
  zone,
  owner,
  hindered: false,
  numbersHidden: false,
  index: null,
  attachedTo: null,
  stackedOn: null,
  attachedAt: 0,
  tokens: {},
  inactive: false,
  assignedTo: null,
  inSkirmish: false,
  testingText: null,
  backSideTestingText: null,
});

describe('PileDialogsComponent', () => {
  let edit: FakeTableEdit;
  const model = signal<GameModel>(emptyGame());
  const pileDialogs = signal<readonly { pile: PileName; player: string }[]>([]);
  const closePile = vi.fn();

  function render(level: 'sandbox' | 'none' = 'sandbox', mode: 'edit' | 'play' = 'edit') {
    vi.useFakeTimers();
    edit = fakeTableEdit({ level, mode });
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    model.set({
      ...emptyGame(),
      participantId: 'alice',
      players: ['alice', '~Scenario'],
      cards: { '7': card('7', 'DISCARD', 'alice', '1_92'), '8': card('8', 'HAND', 'alice') },
      cardOrder: ['7', '8'],
    });
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        PageLifetime,
        GameCardViews,
        EditPopups,
        { provide: GameSession, useValue: { model } },
        { provide: GameViewState, useValue: { pileDialogs, closePile } },
        { provide: TableEdit, useValue: edit },
        { provide: EditSeating, useValue: fakeEditSeating() },
        { provide: GameDecisions, useValue: { view: signal({ marks: {}, noEffect: {} }) } },
        { provide: ScenarioGuide, useValue: { cardMarks: signal({}) } },
      ],
    });
    const fixture = TestBed.createComponent(PileDialogsComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const pile = (name: string) => el.querySelector<HTMLElement>(`.gd-game-pile[data-pile="${name}"]`)!;
    const ids = (name: string) =>
      [...pile(name).querySelectorAll<HTMLElement>('button.gd-card')].map((button) => button.dataset['cardId']);
    const spots = (name: string) =>
      [...pile(name).querySelectorAll<HTMLElement>(':scope > *')]
        .filter((child) => child.tagName === 'GD-OPEN-SPOT')
        .map((spot) => spot.querySelector<HTMLElement>('button')!.dataset['zone']);
    return { fixture, el, pile, ids, spots };
  }

  afterEach(() => {
    vi.useRealTimers();
    pileDialogs.set([]);
  });

  it("shows a hidden pile from the sandbox's board, top first, with a draw deck's top and bottom spots", () => {
    pileDialogs.set([{ pile: 'deck', player: 'alice' }]);
    const { el, pile, ids, spots } = render();
    expect(el.querySelector('dialog[data-dialog="pile"]')?.textContent).toContain('Draw Deck - alice');
    expect(pile('deck').dataset).toMatchObject({ seat: 'freeps', source: 'board' });
    expect(ids('deck')).toEqual(['40', '41']);
    const children = [...pile('deck').children].map((child) => child.tagName.toLowerCase());
    expect(children).toEqual(['gd-open-spot', 'button', 'button', 'gd-open-spot']);
    expect(spots('deck')).toEqual(['deck.top', 'deck.bottom']);
  });

  it("shows the other seat's hand and discard pile from the board, one's own from the page, each with its spot", () => {
    pileDialogs.set([
      { pile: 'hand', player: '~Scenario' },
      { pile: 'discard', player: '~Scenario' },
      { pile: 'hand', player: 'alice' },
      { pile: 'adventureDeck', player: '~Scenario' },
    ]);
    const { pile, ids, spots } = render();
    expect(ids('hand')).toEqual(['32']);
    expect(pile('hand').dataset['source']).toBe('board');
    expect(spots('hand')).toEqual(['hand']);
    expect(ids('discard')).toEqual(['42']);
    expect(spots('discard')).toEqual(['discard']);
    const own = [...document.querySelectorAll<HTMLElement>('.gd-game-pile[data-pile="hand"]')];
    expect(own.map((box) => box.dataset['source'])).toEqual(['board', 'page']);
    expect([...own[1]!.querySelectorAll<HTMLElement>('button.gd-card')].map((b) => b.dataset['cardId'])).toEqual(['8']);
    // (nothing is added to an adventure deck)
    expect(ids('adventureDeck')).toEqual(['33', '34']);
    expect(spots('adventureDeck')).toEqual([]);
  });

  it('has no open spots in play mode, and reads the board again when a hidden pile opens and as the game moves', () => {
    pileDialogs.set([{ pile: 'discard', player: 'alice' }]);
    const { fixture, ids, spots } = render('sandbox', 'play');
    expect(ids('discard')).toEqual(['7']);
    expect(spots('discard')).toEqual([]);
    vi.advanceTimersByTime(PILE_READ_MS);
    expect(edit.refresh).not.toHaveBeenCalled();
    pileDialogs.set([...pileDialogs(), { pile: 'deck', player: '~Scenario' }]);
    fixture.detectChanges();
    vi.advanceTimersByTime(PILE_READ_MS);
    expect(edit.refresh).toHaveBeenCalledTimes(1);
    // The game moves twice in a moment: read once, after it
    model.update((now) => ({ ...now, twilight: 1 }));
    fixture.detectChanges();
    vi.advanceTimersByTime(PILE_READ_MS / 2);
    model.update((now) => ({ ...now, twilight: 2 }));
    fixture.detectChanges();
    vi.advanceTimersByTime(PILE_READ_MS);
    expect(edit.refresh).toHaveBeenCalledTimes(2);
  });

  it('outside a sandbox shows the piles as always, from the page, and the window closes', () => {
    pileDialogs.set([{ pile: 'discard', player: 'alice' }]);
    const { el } = render('none', 'play');
    const box = el.querySelector<HTMLElement>('.gd-game-pile')!;
    expect(box.dataset['pile']).toBeUndefined();
    expect(box.dataset['source']).toBeUndefined();
    expect(box.querySelectorAll('button.gd-card')).toHaveLength(1);
    el.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    expect(closePile).toHaveBeenCalledWith('discard', 'alice');
  });
});
