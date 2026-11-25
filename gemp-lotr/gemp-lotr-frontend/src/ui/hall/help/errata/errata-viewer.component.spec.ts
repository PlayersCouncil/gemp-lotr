import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { ERRATA_SERVICES } from '../../../../core/hall/help/errata/errata-services';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { ShareLinks } from '../../../../core/hall/share-links';
import { ClipboardWriter } from '../../../../core/platform/clipboard.service';
import { PAGE_ORIGIN } from '../../../../core/platform/page-location';
import { installFakeResizeObserver } from '../../../../testing/fake-resize-observer';
import { readServerResponse } from '../../../../testing/files';
import { ErrataViewerComponent } from './errata-viewer.component';

describe('ErrataViewerComponent', () => {
  let copied: string[];
  let copyWorks: boolean;

  beforeEach(() => (copyWorks = true));
  afterEach(() => vi.restoreAllMocks());

  async function render(answer = true) {
    copied = [];
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ...ERRATA_SERVICES,
        ShareLinks,
        CardInfoService,
        { provide: PAGE_ORIGIN, useValue: 'https://gemp.test' },
        {
          provide: ClipboardWriter,
          useValue: {
            copy: (text: string) => {
              copied.push(text);
              return Promise.resolve(copyWorks);
            },
          },
        },
      ],
    });
    const errata = TestBed.inject(PcErrataService);
    const fixture = TestBed.createComponent(ErrataViewerComponent);
    const done = errata.start();
    if (answer) {
      TestBed.inject(HttpTestingController)
        .expectOne((req) => req.url === '/gemp-lotr-server/hall/errata/json')
        .flush(readServerResponse('errata.json'));
      await done;
    }
    fixture.detectChanges();
    await fixture.whenStable();
    return { fixture, errata, viewer: fixture.nativeElement as HTMLElement };
  }

  const texts = (root: Element, selector: string) =>
    Array.from(root.querySelectorAll(selector)).map((element) => element.textContent?.trim());

  it('draws the bar and the card on show (pc-errata.html .pc-errata-viewer)', async () => {
    const { viewer } = await render();
    expect([
      viewer.getAttribute('tabindex'),
      viewer.getAttribute('aria-roledescription'),
      viewer.getAttribute('aria-label'),
    ]).toEqual(['0', 'card viewer', 'Errata viewer']);
    const steps = Array.from(viewer.querySelectorAll<HTMLButtonElement>('.gd-errata-viewer__step'));
    expect(
      steps.map((step) => [step.textContent?.trim(), step.getAttribute('aria-label'), step.title, step.disabled]),
    ).toEqual([
      ['▲', 'Previous card', 'Previous card (↑ or k)', true],
      ['▼', 'Next card', 'Next card (↓ or j)', false],
    ]);
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['1 of 7']);
    expect(texts(viewer, '.gd-errata-viewer__flip-switch button')).toEqual(['Original', 'PC errata']);
    expect(texts(viewer, '.gd-errata-viewer__enlarge')).toEqual(['Enlarge']);
    expect(viewer.querySelector('.gd-errata-viewer__share')?.getAttribute('aria-label')).toBe(
      'Copy a share link to the errata on show',
    );
    expect(viewer.querySelectorAll('gd-errata-frame')).toHaveLength(1);
    expect((viewer.querySelector('#gd-errata-viewer-keys') as HTMLElement).hidden).toBe(true);
  });

  it('steps with the arrows and flips with the switch', async () => {
    const { fixture, errata, viewer } = await render();
    (viewer.querySelector('[data-step=next]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['2 of 7']);
    const [original, pc] = Array.from(
      viewer.querySelectorAll<HTMLButtonElement>('.gd-errata-viewer__flip-switch button'),
    );
    expect([original?.getAttribute('aria-pressed'), pc?.getAttribute('aria-pressed')]).toEqual(['false', 'true']);
    original?.click();
    fixture.detectChanges();
    expect(errata.showOriginal()).toBe(true);
    expect(original?.getAttribute('aria-pressed')).toBe('true');
    (viewer.querySelector('.gd-errata-viewer__enlarge') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(viewer.hasAttribute('data-enlarged')).toBe(true);
    expect(texts(viewer, '.gd-errata-viewer__enlarge')).toEqual(['Close']);
    expect(errata.showOriginal()).toBe(false);
  });

  it('copies the share link of the card on show and says so', async () => {
    const { fixture, viewer } = await render();
    (viewer.querySelector('.gd-errata-viewer__share') as HTMLButtonElement).click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(copied).toEqual(['https://gemp.test/gemp-lotr/share/errata/51_45']);
    expect(texts(viewer, '.gd-errata-viewer__note')).toEqual(['Link copied']);
  });

  it('steps a card per notch of the wheel', async () => {
    const { fixture, errata, viewer } = await render();
    viewer.dispatchEvent(new WheelEvent('wheel', { deltaY: 30, cancelable: true }));
    expect(errata.index()).toBe(0);
    const event = new WheelEvent('wheel', { deltaY: 30, cancelable: true });
    viewer.dispatchEvent(event);
    fixture.detectChanges();
    expect(event.defaultPrevented).toBe(true);
    expect(errata.index()).toBe(1);
  });

  it('turns the card over on a tap, and steps on a swipe (bindSwipe)', async () => {
    const { fixture, errata, viewer } = await render();
    const reel = viewer.querySelector('.gd-errata-viewer__reel') as HTMLElement;
    const box = viewer.querySelector('.gd-errata-frame__cardbox') as HTMLElement;
    box.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 10, clientY: 100 }));
    reel.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, clientX: 10, clientY: 101 }));
    expect(errata.showOriginal()).toBe(true);
    reel.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 10, clientY: 100 }));
    reel.dispatchEvent(new MouseEvent('pointermove', { bubbles: true, clientX: 10, clientY: 60 }));
    reel.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, clientX: 10, clientY: 50 }));
    fixture.detectChanges();
    expect(errata.index()).toBe(1);
  });

  it('opens the card information on a right-click, with the side’s image', async () => {
    const { viewer } = await render();
    const info = TestBed.inject(CardInfoService);
    const card = viewer.querySelector('gd-errata-card[data-side=before]') as HTMLElement;
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    card.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(info.card()?.blueprintId).toBe('1_45');
    expect(info.card()?.imageUrl).toBe('https://i.lotrtcgpc.net/decipher/LOTR01045.jpg');
    expect(info.title()).toBe('Gandalf, The Grey Wizard (original)');
  });

  it('opens the keys’ help with its (i)', async () => {
    const { fixture, viewer } = await render();
    (viewer.querySelector('[data-info-toggle]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect((viewer.querySelector('#gd-errata-viewer-keys') as HTMLElement).hidden).toBe(false);
  });

  it('is empty until the errata come', async () => {
    const { viewer } = await render(false);
    expect(viewer.querySelectorAll('gd-errata-frame, .gd-errata-viewer__empty')).toHaveLength(0);
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['0 of 0']);
    TestBed.inject(HttpTestingController).match(() => true);
  });

  const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

  it('steps back with ▲, and shows the PC version again with its switch', async () => {
    const { fixture, errata, viewer } = await render();
    errata.last();
    fixture.detectChanges();
    (viewer.querySelector('[data-step=previous]') as HTMLButtonElement).click();
    expect(errata.index()).toBe(5);
    errata.setShowOriginal(true);
    (viewer.querySelectorAll('.gd-errata-viewer__flip-switch button')[1] as HTMLButtonElement).click();
    expect(errata.showOriginal()).toBe(false);
  });

  it('shows the link to copy by hand when copying failed (GempShareLinks.say)', async () => {
    copyWorks = false;
    const { fixture, viewer } = await render();
    (viewer.querySelector('.gd-errata-viewer__share') as HTMLButtonElement).click();
    await fixture.whenStable();
    fixture.detectChanges();
    const field = viewer.querySelector('.gd-errata-viewer__note input') as HTMLInputElement;
    expect([field.value, field.getAttribute('aria-label'), field.readOnly]).toEqual([
      'https://gemp.test/gemp-lotr/share/errata/51_45',
      'Share link',
      true,
    ]);
  });

  it('leaves Ctrl+wheel to the browser, and steps nothing before the errata come', async () => {
    const { errata, viewer } = await render();
    const zoom = new WheelEvent('wheel', { deltaY: 100, ctrlKey: true, cancelable: true });
    viewer.dispatchEvent(zoom);
    expect(zoom.defaultPrevented).toBe(false);
    expect(errata.index()).toBe(0);
    errata.load.set({ kind: 'loading' });
    const early = new WheelEvent('wheel', { deltaY: 100, cancelable: true });
    viewer.dispatchEvent(early);
    expect(early.defaultPrevented).toBe(true);
    errata.load.set({
      kind: 'ready',
      catalog: errata.catalog() ?? {
        entries: [],
        formats: [],
        counts: { total: 0, errata: 0, playtest: 0, revision: 0, recent: 0 },
      },
    });
  });

  it('does nothing for a swipe the browser cancelled', async () => {
    const { errata, viewer } = await render();
    const reel = viewer.querySelector('.gd-errata-viewer__reel') as HTMLElement;
    reel.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 10, clientY: 100 }));
    reel.dispatchEvent(new MouseEvent('pointercancel', { bubbles: true, clientX: 10, clientY: 20 }));
    expect(errata.index()).toBe(0);
  });

  it('slides the next card in from below while the last one slides out above (show)', async () => {
    const animate = vi.fn(() => ({}) as Animation);
    HTMLElement.prototype.animate = animate as unknown as HTMLElement['animate'];
    const { fixture, errata, viewer } = await render();
    errata.step(1);
    fixture.detectChanges();
    await fixture.whenStable();
    const frames = Array.from(viewer.querySelectorAll('gd-errata-frame'));
    expect(frames.map((frame) => [frame.getAttribute('data-id'), frame.hasAttribute('data-leaving')])).toEqual([
      ['51_45', true],
      ['51_100', false],
    ]);
    expect(animate.mock.calls.map((call) => (call as unknown[])[0])).toEqual([
      [{ transform: 'none' }, { transform: 'translateY(-100%)' }],
      [{ transform: 'translateY(100%)' }, { transform: 'none' }],
    ]);
    // a step while it slides: the timer starts again
    errata.step(1);
    fixture.detectChanges();
    await wait(320);
    fixture.detectChanges();
    expect(
      Array.from(viewer.querySelectorAll('gd-errata-frame')).map((frame) => frame.getAttribute('data-id')),
    ).toEqual(['71_100']);
    delete (HTMLElement.prototype as Partial<HTMLElement>).animate;
  });

  it('turns the card over: a quick squeeze, then the other side (setShowOriginal)', async () => {
    const { fixture, errata, viewer } = await render();
    const box = () => viewer.querySelector('.gd-errata-frame__cardbox') as HTMLElement;
    errata.toggleOriginal();
    fixture.detectChanges();
    expect(box().hasAttribute('data-turning')).toBe(true);
    expect(box().dataset['showing']).toBe('after');
    // turned back before it is over: it turns to where it is asked to end
    errata.toggleOriginal();
    errata.toggleOriginal();
    await wait(120);
    fixture.detectChanges();
    expect(box().hasAttribute('data-turning')).toBe(false);
    expect(box().dataset['showing']).toBe('before');
  });

  it('nudges the reel at an end, and settles back (bump)', async () => {
    const { fixture, errata, viewer } = await render();
    errata.step(-1);
    await wait(40);
    fixture.detectChanges();
    expect(viewer.dataset['bump']).toBe('start');
    await wait(320);
    fixture.detectChanges();
    expect(viewer.hasAttribute('data-bump')).toBe(false);
  });

  it('with reduced motion: no slide, no squeeze, no nudge', async () => {
    window.matchMedia = vi.fn(() => ({ matches: true }) as MediaQueryList);
    try {
      const { fixture, errata, viewer } = await render();
      errata.step(1);
      errata.toggleOriginal();
      errata.step(-1);
      errata.step(-1);
      fixture.detectChanges();
      expect(viewer.querySelectorAll('gd-errata-frame')).toHaveLength(1);
      errata.toggleOriginal();
      fixture.detectChanges();
      expect((viewer.querySelector('.gd-errata-frame__cardbox') as HTMLElement).dataset['showing']).toBe('before');
      await wait(40);
      fixture.detectChanges();
      expect(viewer.hasAttribute('data-bump')).toBe(false);
    } finally {
      delete (window as Partial<Window>).matchMedia;
    }
  });

  it('gives Share less room as the bar narrows: only its icon, then none (fitShare)', async () => {
    const observers = installFakeResizeObserver();
    try {
      const { fixture, viewer } = await render();
      const bar = viewer.querySelector('.gd-errata-viewer__bar') as HTMLElement;
      let room = 600;
      Object.defineProperty(bar, 'clientWidth', { get: () => room });
      Object.defineProperty(bar, 'scrollWidth', { get: () => (bar.dataset['share'] === 'full' ? 640 : 600) });
      observers.resize();
      fixture.detectChanges();
      expect(bar.dataset['share']).toBe('icon');
      room = 595;
      observers.resize();
      fixture.detectChanges();
      expect(bar.dataset['share']).toBe('hidden');
      room = 700;
      observers.resize();
      fixture.detectChanges();
      expect(bar.dataset['share']).toBe('full');
    } finally {
      observers.restore();
    }
  });

  it('leaves the browser menu for a side without a picture', async () => {
    const { fixture, errata, viewer } = await render();
    errata.select(5);
    fixture.detectChanges();
    expect(errata.shown()?.entry.id).toBe('101_5');
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    (viewer.querySelector('gd-errata-card[data-side=before]') as HTMLElement).dispatchEvent(event);
    expect(event.defaultPrevented).toBe(false);
  });

  it('opens the card information only once the right button is let go (a press outside would close it)', async () => {
    const { viewer } = await render();
    const info = TestBed.inject(CardInfoService);
    const card = viewer.querySelector('gd-errata-card[data-side=after]') as HTMLElement;
    card.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true, buttons: 2 }));
    await wait(5);
    expect(info.card()).toBeNull();
    document.dispatchEvent(new MouseEvent('mouseup'));
    await wait(5);
    expect(info.title()).toBe('Gandalf, The Grey Wizard (PC errata)');
  });
});
