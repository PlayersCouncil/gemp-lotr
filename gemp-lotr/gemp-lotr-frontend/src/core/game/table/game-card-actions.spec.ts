import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { THEME_BEHAVIOUR, ThemeName, ThemeService } from '../../platform/theme.service';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameCardActions } from './game-card-actions';

describe('GameCardActions', () => {
  const cardClicked = vi.fn();
  const theme = signal<ThemeName>('classic');

  beforeEach(() => {
    cardClicked.mockReset();
    TestBed.configureTestingModule({
      providers: [
        GameCardActions,
        { provide: GameDecisions, useValue: { cardClicked } },
        { provide: ThemeService, useValue: { behaviour: () => THEME_BEHAVIOUR[theme()] } },
      ],
    });
  });

  // The click's fields the page reads (jsdom's MouseEvent has neither page coordinates nor pointerType)
  const click = (pointerType: string) => ({ pageX: 300, pageY: 200, pointerType }) as unknown as MouseEvent;

  it('opens the menu where the mouse clicked', () => {
    theme.set('classic');
    TestBed.inject(GameCardActions).activate({ cardId: '7' }, click('mouse'));
    expect(cardClicked).toHaveBeenCalledWith('7', { x: 300, y: 200 });
  });

  it("opens a tap's menu at the corner in the classic theme, as game.html does", () => {
    theme.set('classic');
    TestBed.inject(GameCardActions).activate({ cardId: '7' }, click('touch'));
    expect(cardClicked).toHaveBeenCalledWith('7', { x: 0, y: 0 });
  });

  it("opens a tap's menu where tapped in the modern theme", () => {
    theme.set('modern');
    TestBed.inject(GameCardActions).activate({ cardId: '7' }, click('touch'));
    expect(cardClicked).toHaveBeenCalledWith('7', { x: 300, y: 200 });
  });
});
