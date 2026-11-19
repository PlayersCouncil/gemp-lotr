import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { PageCues } from './page-cues';

describe('PageCues', () => {
  afterEach(() => vi.restoreAllMocks());

  it("waits for the page's fonts, or not at all where the browser can't tell", async () => {
    const cues = TestBed.inject(PageCues);
    const fonts = Object.getOwnPropertyDescriptor(document, 'fonts');
    let loaded!: () => void;
    Object.defineProperty(document, 'fonts', {
      configurable: true,
      value: { ready: new Promise<void>((resolve) => (loaded = resolve)) },
    });
    let done = false;
    const waiting = cues.fontsReady().then(() => (done = true));
    await Promise.resolve();
    expect(done).toBe(false);
    loaded();
    await waiting;
    expect(done).toBe(true);
    Object.defineProperty(document, 'fonts', { configurable: true, value: undefined });
    await expect(cues.fontsReady()).resolves.toBeUndefined();
    if (fonts) Object.defineProperty(document, 'fonts', fonts);
    else delete (document as { fonts?: unknown }).fonts;
  });

  it("sets the tab's title and tells whether the page is in the background", () => {
    const cues = TestBed.inject(PageCues);
    cues.setTitle('Hello');
    expect(document.title).toBe('Hello');
    vi.spyOn(document, 'hasFocus').mockReturnValue(false);
    expect(cues.away()).toBe(true);
  });

  it('plays a sound from its start, and keeps quiet when the browser refuses', async () => {
    const play = vi.fn(() => Promise.reject(new Error('not allowed')));
    const made: string[] = [];
    class FakeAudio {
      currentTime = 5;
      constructor(src: string) {
        made.push(src);
      }
      play = play;
    }
    vi.stubGlobal('Audio', FakeAudio);
    const cues = TestBed.inject(PageCues);
    cues.play('timerAlert');
    cues.play('timerAlert');
    await Promise.resolve();
    expect(made).toEqual(['/gemp-lotr/src/assets/timer_alert.mp3']);
    expect(play).toHaveBeenCalledTimes(2);
    vi.unstubAllGlobals();
  });

  /** PageCues on a stand-in document: its full screen, the window's location and navigator, as a test sets them. */
  function onFakeDocument() {
    const listeners: Record<string, () => void> = {};
    const location = { reload: vi.fn(), replace: vi.fn(), href: '' };
    const navigator: { vibrate?: ReturnType<typeof vi.fn> } = { vibrate: vi.fn() };
    const fake = {
      fullscreenElement: null as object | null,
      fullscreenEnabled: true,
      exitFullscreen: vi.fn(() => Promise.resolve()),
      documentElement: { requestFullscreen: vi.fn(() => Promise.reject(new Error('no user gesture'))) },
      addEventListener: (type: string, listener: () => void) => (listeners[type] = listener),
      defaultView: { location, navigator },
    };
    TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: fake }] });
    return { cues: TestBed.inject(PageCues), fake, listeners, location, navigator };
  }

  it('fills the screen and leaves it, following the browser; a refusal is no error', async () => {
    const { cues, fake, listeners } = onFakeDocument();
    expect(cues.fullscreenAvailable()).toBe(true);
    expect(cues.fullscreen()).toBe(false);
    cues.toggleFullscreen();
    expect(fake.documentElement.requestFullscreen).toHaveBeenCalled();
    await Promise.resolve();
    // The browser filled the screen
    fake.fullscreenElement = {};
    listeners['fullscreenchange']?.();
    expect(cues.fullscreen()).toBe(true);
    cues.toggleFullscreen();
    expect(fake.exitFullscreen).toHaveBeenCalled();
  });

  it('buzzes where the device can, and nowhere else', () => {
    const { cues, navigator } = onFakeDocument();
    cues.vibrate(50);
    expect(navigator.vibrate).toHaveBeenCalledWith(50);
    navigator.vibrate = vi.fn(() => {
      throw new Error('not before the player has used the page');
    });
    expect(() => cues.vibrate()).not.toThrow();
    delete navigator.vibrate;
    expect(() => cues.vibrate()).not.toThrow();
  });

  it('reloads the page, replaces it, or goes to another address', () => {
    const { cues, location } = onFakeDocument();
    cues.reload();
    expect(location.reload).toHaveBeenCalled();
    cues.replace('/gemp-lotr/hall.html');
    expect(location.replace).toHaveBeenCalledWith('/gemp-lotr/hall.html');
    cues.go('/gemp-lotr/deckBuild.html');
    expect(location.href).toBe('/gemp-lotr/deckBuild.html');
  });
});
