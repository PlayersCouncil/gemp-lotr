import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CARD_WINDOW_ATTRIBUTE, CardWindowDirective } from './card-window.directive';
import { FitCard } from './fit-cards';

/** A stand-in for the browser's ResizeObserver: the test says when the box resized. */
class FakeResizeObserver {
  static instances: FakeResizeObserver[] = [];
  disconnected = false;

  constructor(private readonly callback: ResizeObserverCallback) {
    FakeResizeObserver.instances.push(this);
  }

  observe(): void {
    // (the test calls resize itself)
  }

  disconnect(): void {
    this.disconnected = true;
  }

  resize(): void {
    this.callback([], this as unknown as ResizeObserver);
  }
}

@Component({
  imports: [CardWindowDirective],
  template: `<div class="layout">
    <dialog open>
      <div class="box" [gdCardWindow]="cards()" [gdCardWindowLabel]="label()" [gdCardWindowActions]="actions()"></div>
      @if (buttons()) {
        <div gdDialogActions></div>
      }
    </dialog>
  </div>`,
})
class Host {
  readonly cards = signal<readonly FitCard[] | null>(upright(3));
  readonly label = signal(0);
  readonly actions = signal(0);
  readonly buttons = signal(false);
}

function upright(count: number): FitCard[] {
  return Array.from({ length: count }, () => ({ horizontal: false }));
}

describe('CardWindowDirective', () => {
  /** The box's size as laid out (0 x 0: its dialog closed); the dialog is 120px larger each way. */
  let box: { width: number; height: number };
  let pageRight: string;
  let frames: FrameRequestCallback[];

  beforeEach(() => {
    FakeResizeObserver.instances = [];
    frames = [];
    vi.stubGlobal('ResizeObserver', FakeResizeObserver);
    vi.spyOn(window, 'requestAnimationFrame').mockImplementation((callback) => frames.push(callback));
    vi.spyOn(window, 'cancelAnimationFrame').mockImplementation(() => undefined);
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 1366 });
    Object.defineProperty(window, 'innerHeight', { configurable: true, value: 768 });
    box = { width: 700, height: 400 };
    pageRight = '';
    vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockImplementation(function (this: HTMLElement) {
      const extra = this.tagName === 'DIALOG' ? 120 : 0;
      return { width: box.width + extra, height: box.height + extra } as DOMRect;
    });
    const real = window.getComputedStyle.bind(window);
    vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => {
      const style = real(element);
      const own: Record<string, string> = {
        '--gd-card-gap': '5px',
        '--gd-page-right': element === document.documentElement ? pageRight : '',
      };
      return {
        ...style,
        width: element.tagName === 'DIALOG' ? '800px' : style.width,
        height: element.tagName === 'DIALOG' ? '500px' : style.height,
        getPropertyValue: (name: string) => own[name] ?? '',
      } as CSSStyleDeclaration;
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  async function render(setUp: (host: Host) => void = () => undefined) {
    const fixture = TestBed.createComponent(Host);
    setUp(fixture.componentInstance);
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    return {
      fixture,
      host: fixture.componentInstance,
      dialog: root.querySelector('dialog')!,
      cards: root.querySelector<HTMLElement>('.box')!,
    };
  }

  it('sizes its dialog to its cards when it opens, and lays the cards out at that height', async () => {
    const { dialog, cards } = await render();
    // Three cards of 500 (357 wide) in one row: 1081 x 500, a pixel more each way, from a box of 700 x 400
    expect(dialog.style.getPropertyValue('--dialog-width')).toBe(`${800 + 1082 - 700}px`);
    expect(dialog.style.getPropertyValue('--dialog-height')).toBe(`${500 + 501 - 400}px`);
    expect(dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(true);
    expect(cards.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(true);
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('500px');
    expect(cards.style.getPropertyValue('--gd-card-fit')).toBe('contain');
    expect(cards.style.getPropertyValue('--gd-card-label')).toBe('0px');
  });

  it('keeps room for labels, and for the buttons the dialog has none of yet', async () => {
    const { dialog, cards, host, fixture } = await render((h) => {
      h.label.set(36);
      h.actions.set(63);
      h.cards.set(upright(2));
    });
    // 768 * 0.9 - 120 - 63 = 508 down: 460 + 36 (480 + 36 is too high)
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('460px');
    expect(dialog.style.getPropertyValue('--dialog-height')).toBe(`${500 + 497 - 400 + 63}px`);
    // With its buttons there, a new choice has no room kept for them
    host.buttons.set(true);
    host.cards.set(upright(3));
    await fixture.whenStable();
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('500px');
    expect(dialog.style.getPropertyValue('--dialog-height')).toBe(`${500 + 537 - 400}px`);
  });

  it('is placed in the room left of a panel at the right', async () => {
    pageRight = '600px';
    const { cards } = await render((h) => h.cards.set(upright(4)));
    // (766 * 0.9 - 120 = 569 across: four cards in two rows of two at 280, 200 wide)
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('280px');
  });

  it('sizes it again for other cards, unless the player moved or resized it', async () => {
    const { dialog, cards, host, fixture } = await render();
    host.cards.set(upright(8));
    await fixture.whenStable();
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('280px');
    // The same cards again (a new array): nothing changes
    dialog.style.setProperty('--dialog-width', 'kept');
    host.cards.set(upright(8));
    await fixture.whenStable();
    expect(dialog.style.getPropertyValue('--dialog-width')).toBe('kept');

    dialog.setAttribute('data-placed', '');
    host.cards.set(upright(2));
    await fixture.whenStable();
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('280px');
    dialog.removeAttribute('data-placed');
    dialog.style.width = '300px';
    host.cards.set(upright(1));
    await fixture.whenStable();
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('280px');
  });

  it('opened again, it is sized afresh, without the size the player gave it', async () => {
    const { dialog, cards } = await render();
    dialog.style.width = '300px';
    dialog.style.height = '200px';
    // Closed: the box has no size; the observer's news is taken in the next frame
    box = { width: 0, height: 0 };
    dialog.removeAttribute('open');
    const [observer] = FakeResizeObserver.instances;
    const before = frames.length;
    observer.resize();
    observer.resize();
    // (one frame however often it is told)
    expect(frames.length - before).toBe(1);
    frames.splice(0).forEach((frame) => frame(0));
    box = { width: 700, height: 400 };
    dialog.setAttribute('open', '');
    cards.style.removeProperty('--gd-card-max-height');
    observer.resize();
    frames.splice(0).forEach((frame) => frame(0));
    expect(dialog.style.width).toBe('');
    expect(dialog.style.height).toBe('');
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('500px');
  });

  it('leaves the cards to the box where none fit at the least height', async () => {
    Object.defineProperty(window, 'innerHeight', { configurable: true, value: 200 });
    const { dialog, cards } = await render();
    expect(cards.style.getPropertyValue('--gd-card-max-height')).toBe('');
    expect(cards.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(false);
    expect(dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(true);
  });

  it('gives the dialog back its own size when it has no cards to size it to, or in the compact layout', async () => {
    const { dialog, cards, host, fixture } = await render();
    host.cards.set(null);
    await fixture.whenStable();
    expect(dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(false);
    expect(dialog.style.getPropertyValue('--dialog-width')).toBe('');
    expect(cards.style.getPropertyValue('--gd-card-fit')).toBe('');
    // (and again: nothing more to give back)
    host.label.set(1);
    await fixture.whenStable();

    host.cards.set(upright(3));
    await fixture.whenStable();
    expect(dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(true);
    (fixture.nativeElement as HTMLElement).querySelector('.layout')!.setAttribute('data-layout', 'compact');
    host.cards.set(upright(4));
    await fixture.whenStable();
    expect(dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)).toBe(false);
  });

  it('stops watching the box when it goes', async () => {
    const { fixture } = await render();
    const [observer] = FakeResizeObserver.instances;
    observer.resize();
    fixture.destroy();
    expect(observer.disconnected).toBe(true);
    expect(window.cancelAnimationFrame).toHaveBeenCalled();
  });
});
