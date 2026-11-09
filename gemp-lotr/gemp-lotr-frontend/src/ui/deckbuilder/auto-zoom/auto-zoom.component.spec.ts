import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardView } from '../../../core/cards/card-view';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CardHover } from '../../shared/card/card-hover';
import { AutoZoomComponent, HOVER_COOLDOWN_MS, HOVER_DELAY_MS } from './auto-zoom.component';

describe('AutoZoomComponent', () => {
  let hover: CardHover;
  let setting: AutoZoomSetting;

  const card = (blueprintId: string): CardView => ({
    ref: { kind: 'shown', blueprintId, zone: 'FREE_PEOPLE', choosable: false },
    role: 'card',
    imageUrl: TestBed.inject(CardImageService).imageUrl(blueprintId),
    horizontal: false,
    errata: false,
    foilImage: null,
    border: true,
    count: null,
    overlayImage: null,
  });
  const box = { left: 100, top: 300, right: 200, bottom: 440 };

  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    hover = TestBed.inject(CardHover);
    setting = TestBed.inject(AutoZoomSetting);
    Object.defineProperty(setting, 'available', { value: true });
    setting.enabled.set(true);
  });

  afterEach(() => vi.useRealTimers());

  function render() {
    const fixture = TestBed.createComponent(AutoZoomComponent);
    fixture.detectChanges();
    const zoom = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.gd-zoom')!;
    // Effects first (they start the timers), then the time, then effects again
    const step = (ms = 0) => {
      TestBed.tick();
      vi.advanceTimersByTime(ms);
      TestBed.tick();
    };
    return { fixture, zoom, step };
  }

  it('shows a card resting under the mouse for half a second, beside it (DOM contract)', () => {
    const { zoom, step } = render();
    expect(zoom.hidden).toBe(true);
    expect(zoom.getAttribute('aria-hidden')).toBe('true');
    const source = {};
    hover.enter(source, card('1_12'), box);
    step(HOVER_DELAY_MS - 1);
    expect(zoom.hidden).toBe(true);
    step(1);
    expect(zoom.hidden).toBe(false);
    expect(zoom.dataset['orientation']).toBe('vertical');
    expect(zoom.style.getPropertyValue('--gd-zoom-left')).toBe('185px');
    expect(zoom.style.getPropertyValue('--gd-zoom-width')).not.toBe('');
    expect(zoom.querySelector<HTMLImageElement>('img.gd-zoom__image')!.src).toContain('LOTR01012');
    hover.leave(source);
    step();
    expect(zoom.hidden).toBe(true);
  });

  it('shows the next card at once soon after, and waits again after the cooldown', () => {
    const { zoom, step } = render();
    const a = {};
    const b = {};
    hover.enter(a, card('1_12'), box);
    step(HOVER_DELAY_MS);
    hover.leave(a);
    step(HOVER_COOLDOWN_MS - 1);
    hover.enter(b, card('1_13'), box);
    step();
    expect(zoom.querySelector<HTMLImageElement>('img')!.src).toContain('LOTR01013');
    hover.leave(b);
    step(HOVER_COOLDOWN_MS);
    hover.enter(a, card('1_12'), box);
    step();
    expect(zoom.hidden).toBe(true);
  });

  it('hides on a mouse press, when turned off and while the card information is open', () => {
    const { zoom, step } = render();
    const source = {};
    hover.enter(source, card('1_12'), box);
    step(HOVER_DELAY_MS);
    hover.press();
    step();
    expect(zoom.hidden).toBe(true);

    hover.enter(source, card('1_12'), box);
    step();
    setting.enabled.set(false);
    step(HOVER_DELAY_MS);
    expect(zoom.hidden).toBe(true);
    setting.enabled.set(true);

    TestBed.inject(CardInfoService).show({ kind: 'shown', blueprintId: '1_1', zone: 'FREE_PEOPLE', choosable: false });
    hover.enter({}, card('1_12'), box);
    step(HOVER_DELAY_MS);
    expect(zoom.hidden).toBe(true);
  });

  it('flips the zoomed card to its back with Shift (every card has the standard back), face up for the next', () => {
    const { zoom, step } = render();
    const shift = () => {
      window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Shift' }));
      step();
    };
    const source = {};
    hover.enter(source, card('1_12'), box);
    step(HOVER_DELAY_MS);
    shift();
    expect(zoom.dataset['flipped']).toBe('back');
    expect(zoom.querySelector<HTMLImageElement>('img.gd-zoom__image')!.src).toBe(
      TestBed.inject(CardImageService).resolve('1_12').backSideImageUrl,
    );
    shift();
    expect(zoom.hasAttribute('data-flipped')).toBe(false);
    shift();
    hover.enter(source, card('1_13'), box);
    step();
    expect(zoom.hasAttribute('data-flipped')).toBe(false);
    // Held down, Shift flips once
    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Shift', repeat: true }));
    step();
    expect(zoom.hasAttribute('data-flipped')).toBe(false);
  });
});
