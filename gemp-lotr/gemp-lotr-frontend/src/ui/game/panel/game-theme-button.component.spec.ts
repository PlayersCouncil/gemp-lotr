import { TestBed } from '@angular/core/testing';
import { PAGE_PATH, PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { ThemeService } from '../../../core/platform/theme.service';
import { GameThemeButtonComponent } from './game-theme-button.component';

describe('GameThemeButtonComponent', () => {
  // What the page writes (the tests' page isn't under /gemp-lotr, where the cookie goes)
  let written: string[] = [];

  beforeEach(() => {
    written = [];
    vi.spyOn(document, 'cookie', 'get').mockReturnValue('');
    vi.spyOn(document, 'cookie', 'set').mockImplementation((cookie: string) => {
      written.push(cookie);
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    document.documentElement.removeAttribute('data-theme');
  });

  it("switches to the next theme, named in its title, and remembers it as the game's choice", () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: PAGE_PATH, useValue: '/gemp-lotr/ng/game' },
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
      ],
    });
    TestBed.inject(ThemeService).init();
    const fixture = TestBed.createComponent(GameThemeButtonComponent);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-game-theme');
    // The game starts on the classic look
    expect(document.documentElement.dataset['theme']).toBe('classic');
    expect(button?.title).toBe('Switch to the Modern theme');
    button?.click();
    fixture.detectChanges();
    expect(document.documentElement.dataset['theme']).toBe('modern');
    expect(written.some((cookie) => cookie.startsWith('gempGameTheme=modern'))).toBe(true);
    expect(button?.getAttribute('aria-label')).toBe('Switch to the Classic theme');
  });
});
