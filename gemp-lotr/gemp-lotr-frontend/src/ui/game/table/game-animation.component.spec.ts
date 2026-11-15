import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardViews } from '../../../core/cards/card-views.service';
import { GameAnimation } from '../../../core/game/session/event-pacing';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameAnimationComponent } from './game-animation.component';

describe('GameAnimationComponent', () => {
  const animation = signal<{ animation: GameAnimation; duration: number } | null>(null);
  let animate: ReturnType<typeof vi.fn>;
  let page: HTMLElement;
  let saved: { animate: unknown; frame: typeof window.requestAnimationFrame };

  /** A card of the page, drawn `width` wide (jsdom draws nothing). */
  const card = (id: string, width = 50) => {
    const element = document.createElement('button');
    element.dataset['cardId'] = id;
    element.getBoundingClientRect = () => ({ left: 10, top: 20, width, height: 70 }) as DOMRect;
    page.append(element);
    return element;
  };

  beforeEach(() => {
    vi.useFakeTimers();
    animate = vi.fn();
    saved = { animate: (Element.prototype as { animate?: unknown }).animate, frame: window.requestAnimationFrame };
    (Element.prototype as { animate?: unknown }).animate = animate;
    window.requestAnimationFrame = (callback) => {
      callback(0);
      return 0;
    };
    page = document.createElement('gemp-game');
    document.body.append(page);
    animation.set(null);
    TestBed.configureTestingModule({
      providers: [{ provide: GameSession, useValue: { animation } }, CardViews],
    });
  });

  afterEach(() => {
    (Element.prototype as { animate?: unknown }).animate = saved.animate;
    window.requestAnimationFrame = saved.frame;
    page.remove();
    vi.useRealTimers();
  });

  const play = (next: GameAnimation, duration = 100) => {
    animation.set({ animation: next, duration });
    TestBed.tick();
  };

  function render() {
    const fixture = TestBed.createComponent(GameAnimationComponent);
    page.append(fixture.nativeElement as HTMLElement);
    fixture.detectChanges();
    return fixture;
  }

  it('flies a card put into play in from the middle of the table, the last one drawn of its copies', () => {
    card('1', 0);
    const shown = card('1');
    render();
    play({ kind: 'put', cardId: '1' });
    expect(animate).toHaveBeenCalledTimes(1);
    expect(animate.mock.contexts[0]).toBe(shown);
    const [frames, options] = animate.mock.calls[0] as [Keyframe[], KeyframeAnimationOptions];
    expect(frames).toHaveLength(4);
    expect(frames[0]).toMatchObject({ opacity: 0, offset: 0, transformOrigin: 'top left' });
    expect(frames[3]).toMatchObject({ transform: 'none', offset: 1 });
    expect(options).toEqual({ duration: 100 });
    // A card not drawn: nothing to move
    play({ kind: 'put', cardId: '9' });
    expect(animate).toHaveBeenCalledTimes(1);
  });

  it('fades removed cards, turns flipped ones over in two halves, flashes an activated one', () => {
    const a = card('1');
    card('2');
    render();
    play({ kind: 'remove', cardIds: ['1', '2', '9'] });
    expect(animate).toHaveBeenCalledTimes(2);
    expect(animate.mock.calls[0]?.[1]).toMatchObject({ fill: 'forwards', duration: 100 });
    play({ kind: 'flip', cardIds: ['1'], phase: 'out' });
    play({ kind: 'flip', cardIds: ['1'], phase: 'in' });
    expect(animate.mock.calls[2]?.[0]).toEqual([{ transform: 'scaleX(1)' }, { transform: 'scaleX(0)' }]);
    expect(animate.mock.calls[3]?.[0]).toEqual([{ transform: 'scaleX(0)' }, { transform: 'scaleX(1)' }]);
    play({ kind: 'activate', cardId: '1' }, 300);
    expect(a.hasAttribute('data-activated')).toBe(true);
    expect(a.style.getPropertyValue('--gd-animation-duration')).toBe('300ms');
    vi.advanceTimersByTime(300);
    expect(a.hasAttribute('data-activated')).toBe(false);
    play({ kind: 'activate', cardId: '9' });
    play({ kind: 'decision' });
    expect(animate).toHaveBeenCalledTimes(4);
  });

  it('shows a played event in the middle of the table, and a card spreading out over each card it affects', () => {
    card('1');
    const fixture = render();
    play({ kind: 'event', blueprintId: '1_7', testingText: null });
    fixture.detectChanges();
    const images = () => (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLElement>('img');
    expect(images()).toHaveLength(1);
    play({ kind: 'event', blueprintId: '1_7', testingText: null });
    expect(animate.mock.contexts.at(-1)).toBe(images()[0]);
    expect(images()[0]?.style.getPropertyValue('--gd-animation-height')).toBe(`${window.innerHeight / 2}px`);

    play({ kind: 'affect', blueprintId: '1_8', cardIds: ['1', '9'] });
    fixture.detectChanges();
    expect(images()).toHaveLength(1);
    play({ kind: 'affect', blueprintId: '1_8', cardIds: ['1', '9'] });
    expect(images()[0]?.style.getPropertyValue('--gd-animation-left')).toBe('10px');
    const [frames] = animate.mock.calls.at(-1) as [Keyframe[]];
    expect(frames[1]).toMatchObject({ opacity: 0, transform: 'translate(-25px, -35px) scale(2, 2)' });
    animation.set(null);
    fixture.detectChanges();
    expect(images()).toHaveLength(0);
  });

  it('plays nothing that is over before the next frame, nor without animations (jsdom)', () => {
    card('1');
    let later: FrameRequestCallback = () => undefined;
    window.requestAnimationFrame = (callback) => {
      later = callback;
      return 0;
    };
    render();
    play({ kind: 'put', cardId: '1' });
    animation.set(null);
    later(0);
    expect(animate).not.toHaveBeenCalled();
    (Element.prototype as { animate?: unknown }).animate = undefined;
    play({ kind: 'put', cardId: '1' });
    later(0);
    expect(animate).not.toHaveBeenCalled();
  });
});
