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
});
