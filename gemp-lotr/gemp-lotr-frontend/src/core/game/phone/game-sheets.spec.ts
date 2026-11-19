import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { GameViewState } from '../session/game-view-state';
import { chatLines, GameSheets } from './game-sheets';
import { readSheet, SheetEntry } from './sheet';

describe('readSheet', () => {
  it('reads back each kind of sheet kept before a reload', () => {
    const scroll = { top: 5, rows: [1, 2] };
    for (const sheet of [
      { kind: 'menu' },
      { kind: 'piles', player: 'bob', focus: 'adventureDeck' },
      { kind: 'card', cardId: '7', blueprintId: '1_7' },
      { kind: 'card', cardId: null, blueprintId: '1_7' },
      { kind: 'chat' },
      { kind: 'settings', tab: 'display' },
      { kind: 'info' },
      { kind: 'confirm', what: 'cancel' },
    ]) {
      expect(readSheet({ sheet, scroll })).toEqual({ sheet: sheet as never, scroll });
    }
  });

  it('drops what is not a sheet and repairs what it can', () => {
    expect(readSheet(null)).toBeNull();
    expect(readSheet({ sheet: { kind: 'nothing' } })).toBeNull();
    expect(readSheet({ sheet: { kind: 'piles' } })).toBeNull();
    expect(readSheet({ sheet: { kind: 'card', cardId: 3 } })).toBeNull();
    expect(
      readSheet({ sheet: { kind: 'piles', player: 'bob', focus: 'pocket' }, scroll: { top: 'x', rows: [1, 'y'] } }),
    ).toEqual({
      sheet: { kind: 'piles', player: 'bob', focus: null },
      scroll: { top: 0, rows: [1] },
    });
  });
});

describe('GameSheets', () => {
  let sheets: GameSheets;
  let view: { sheets: GameViewState['sheets'] };

  beforeEach(() => {
    // The stack lives in the view's state (kept with the game); a stand-in holds it here
    view = { sheets: signal<readonly SheetEntry[]>([]) };
    TestBed.configureTestingModule({ providers: [GameSheets, { provide: GameViewState, useValue: view }] });
    sheets = TestBed.inject(GameSheets);
  });

  it('stacks sheets: closing the top one shows the one below as it was scrolled', () => {
    sheets.open({ kind: 'menu' });
    sheets.keepScroll({ top: 30, rows: [] });
    sheets.open({ kind: 'piles', player: 'bob', focus: null });
    sheets.keepScroll({ top: 80, rows: [12] });
    sheets.open({ kind: 'card', cardId: '7', blueprintId: '1_7' });
    expect(sheets.canGoBack()).toBe(true);
    sheets.back();
    expect(sheets.top()).toEqual({
      sheet: { kind: 'piles', player: 'bob', focus: null },
      scroll: { top: 80, rows: [12] },
    });
    sheets.back();
    expect(sheets.top()?.scroll.top).toBe(30);
    expect(sheets.canGoBack()).toBe(false);
    sheets.back();
    expect(sheets.top()).toBeNull();
  });

  it('draws a sheet as another without a new level, and closes them all at once', () => {
    sheets.open({ kind: 'menu' });
    sheets.open({ kind: 'settings', tab: 'game' });
    sheets.replace({ kind: 'settings', tab: 'display' });
    expect(view.sheets().length).toBe(2);
    sheets.closeAll();
    expect(view.sheets()).toEqual([]);
  });

  it("keeps the same stack when the scroll didn't change (no needless save)", () => {
    sheets.open({ kind: 'menu' });
    const before = view.sheets();
    sheets.keepScroll({ top: 0, rows: [] });
    expect(view.sheets()).toBe(before);
  });
});

describe('chatLines', () => {
  it("counts what players said, not the room's own notices or the game's messages", () => {
    const said = (from: string, system = false) => ({
      kind: 'chat' as const,
      message: { from, html: 'hi', date: null, system },
    });
    expect(chatLines([said('bob'), said('System', true), { kind: 'message', html: 'x' }, said('alice')])).toBe(2);
  });
});
