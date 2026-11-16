import { TestBed } from '@angular/core/testing';
import { PAGE_PATH, PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { PageCues } from '../../../core/platform/page-cues';
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
    const replace = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        { provide: PAGE_PATH, useValue: '/gemp-lotr/ng/game' },
        { provide: PAGE_QUERY_STRING, useValue: '?gameId=7' },
        { provide: PageCues, useValue: { replace } },
      ],
    });
    TestBed.inject(ThemeService).init();
    const fixture = TestBed.createComponent(GameThemeButtonComponent);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-game-theme');
    // The game starts on the modern look (no choice made)
    expect(document.documentElement.dataset['theme']).toBe('modern');
    expect(button?.title).toBe('Switch to the Classic theme');
    button?.click();
    fixture.detectChanges();
    // Classic is remembered and the page opened anew (with no viewport tag, as game.html)
    expect(written.some((cookie) => cookie.startsWith('gempGameTheme=classic'))).toBe(true);
    expect(String(replace.mock.calls[0]?.[0])).toContain('theme=classic');
    document.head.querySelector('meta[name="viewport"]')?.remove();
  });
});
