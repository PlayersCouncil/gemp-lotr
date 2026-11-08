import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { flushLegacyCardFiles } from '../../testing/legacy-cards';
import { CardDisplayData } from '../cards/card-display.component';
import { CardRegistry } from '../cards/card-registry';
import { LegacyCardDataService } from '../../core/cards/legacy-card-data.service';
import {
  AUTO_ZOOM_COOKIE,
  AutoZoomService,
  HOVER_COOLDOWN_MS,
  HOVER_DELAY_MS,
  previewPlacement,
} from './auto-zoom.component';
import { CardInfoService } from '../../core/cards/card-info.service';
import { provideFakeNotifier } from '../../testing/fake-notifier';

const UPRIGHT: CardDisplayData = { image: 'x', back: null, horizontal: false, foil: false, overlayImage: null };
const rect = (left: number, top: number, right: number, bottom: number) => ({ left, top, right, bottom });

describe('previewPlacement (positions measured on the legacy deck builder)', () => {
  it('puts the zoom left of a card near the right edge, centred on it', () => {
    const p = previewPlacement(UPRIGHT, rect(975, 579.25, 1061, 699.25), 1280, 800);
    expect([p.left, p.top]).toEqual([605, 269]);
  });

  it('keeps the zoom inside the window', () => {
    expect(previewPlacement(UPRIGHT, rect(884, 454.25, 970, 574.25), 1280, 800).top).toBe(256.25);
  });

  it('uses the window width as the limit on a tall window', () => {
    const p = previewPlacement(UPRIGHT, rect(712, 454.25, 811, 592.25), 900, 1000);
    expect([p.left, p.top]).toEqual([296, 233.25]);
  });
});

describe('AutoZoomService', () => {
  let zoom: AutoZoomService;
  let card: HTMLElement;

  beforeEach(async () => {
    vi.useFakeTimers();
    document.cookie = `${AUTO_ZOOM_COOKIE}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/gemp-lotr`;
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideFakeNotifier()],
    });
    const loading = TestBed.inject(LegacyCardDataService).load();
    flushLegacyCardFiles(TestBed.inject(HttpTestingController));
    await loading;
    zoom = TestBed.inject(AutoZoomService);
    // jsdom reports touch support; the zoom is for mouse users
    Object.defineProperty(zoom, 'touchDevice', { value: false });
    TestBed.runInInjectionContext(() => zoom.listen());
    const element = document.createElement('div');
    element.className = 'card';
    card = document.createElement('div');
    card.className = 'borderOverlay actionArea';
    element.append(card);
    document.body.append(element);
    TestBed.inject(CardRegistry).set(element, { kind: 'shown', blueprintId: '1_2', zone: 'player', role: '' });
  });

  afterEach(() => {
    vi.useRealTimers();
    document.body.replaceChildren();
  });

  const over = (target: Element) => target.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
  const out = (target: Element) => target.dispatchEvent(new MouseEvent('mouseout', { bubbles: true }));

  it('shows the zoom after resting on a card, and at once on the next card soon after', () => {
    over(card);
    expect(zoom.preview()).toBeNull();
    vi.advanceTimersByTime(HOVER_DELAY_MS);
    expect(zoom.preview()?.data.image).toContain('LOTR01002');
    out(card);
    over(document.body);
    expect(zoom.preview()).toBeNull();
    over(card);
    expect(zoom.preview()).not.toBeNull();
    out(card);
    over(document.body);
    vi.advanceTimersByTime(HOVER_COOLDOWN_MS);
    // Too late: the next card waits for the delay again
    over(card);
    expect(zoom.preview()).toBeNull();
    vi.advanceTimersByTime(HOVER_DELAY_MS);
    expect(zoom.preview()).not.toBeNull();
  });

  it('hides on a mouse press, and not at all when switched off or while card details are shown', () => {
    over(card);
    vi.advanceTimersByTime(HOVER_DELAY_MS);
    card.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    expect(zoom.preview()).toBeNull();

    TestBed.inject(CardInfoService).show({ kind: 'shown', blueprintId: '1_2', zone: 'player', role: '' });
    vi.advanceTimersByTime(HOVER_COOLDOWN_MS);
    over(card);
    vi.advanceTimersByTime(HOVER_DELAY_MS);
    expect(zoom.preview()).toBeNull();
    TestBed.inject(CardInfoService).close();

    const cookies = vi.spyOn(document, 'cookie', 'set');
    zoom.toggle();
    expect(zoom.enabled()).toBe(false);
    expect(cookies.mock.calls[0][0]).toMatch(new RegExp(`^${AUTO_ZOOM_COOKIE}=false; expires=.*; path=/gemp-lotr$`));
    over(card);
    vi.advanceTimersByTime(HOVER_DELAY_MS);
    expect(zoom.preview()).toBeNull();
  });
});
