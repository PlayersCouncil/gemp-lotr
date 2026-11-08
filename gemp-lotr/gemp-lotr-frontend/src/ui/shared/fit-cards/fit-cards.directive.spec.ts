import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FitCard } from './fit-cards';
import { CARD_HEIGHT_PROPERTY, FitCardsDirective } from './fit-cards.directive';

/** A stand-in for the browser's ResizeObserver: the test says when and to what size the box resizes. */
class FakeResizeObserver {
  static instances: FakeResizeObserver[] = [];
  observed: Element[] = [];
  disconnected = false;

  constructor(private readonly callback: ResizeObserverCallback) {
    FakeResizeObserver.instances.push(this);
  }

  observe(element: Element): void {
    this.observed.push(element);
  }

  disconnect(): void {
    this.disconnected = true;
  }

  resize(width: number, height: number): void {
    const entry = { contentRect: { width, height } } as ResizeObserverEntry;
    this.callback([entry], this as unknown as ResizeObserver);
  }
}

@Component({
  imports: [FitCardsDirective],
  template: `<div class="box" [gdFitCards]="cards()"></div>`,
})
class Host {
  readonly cards = signal<readonly FitCard[]>(Array.from({ length: 12 }, () => ({ horizontal: false })));
}

describe('FitCardsDirective', () => {
  let properties: Record<string, string>;

  beforeEach(() => {
    FakeResizeObserver.instances = [];
    vi.stubGlobal('ResizeObserver', FakeResizeObserver);
    properties = {
      '--gd-card-max-height': '200px',
      '--gd-card-gap': '5px',
      '--gd-card-fit': 'contain',
    };
    const real = window.getComputedStyle.bind(window);
    vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => {
      const style = real(element);
      return {
        ...style,
        getPropertyValue: (name: string) => properties[name] ?? '',
      } as CSSStyleDeclaration;
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const box = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.box')!;
    const [observer] = FakeResizeObserver.instances;
    return { fixture, box, observer };
  }

  const cardHeight = (box: HTMLElement) => box.style.getPropertyValue(CARD_HEIGHT_PROPERTY);

  it('observes its box and sets nothing before the first size is known', async () => {
    const { box, observer } = await render();
    expect(observer.observed).toEqual([box]);
    expect(cardHeight(box)).toBe('');
  });

  it("sets --gd-card-height from the box's size and the theme's properties", async () => {
    const { fixture, box, observer } = await render();
    observer.resize(372, 472);
    await fixture.whenStable();
    expect(cardHeight(box)).toBe('125px');

    properties['--gd-card-fit'] = 'rows';
    observer.resize(372, 472);
    await fixture.whenStable();
    expect(cardHeight(box)).toBe('114.25px');

    properties['--gd-card-max-height'] = '90px';
    observer.resize(1000, 472);
    await fixture.whenStable();
    expect(cardHeight(box)).toBe('90px');
  });

  it('sizes again when the cards change', async () => {
    const { fixture, box, observer } = await render();
    observer.resize(372, 472);
    await fixture.whenStable();
    fixture.componentInstance.cards.set([{ horizontal: false }, { horizontal: false }]);
    await fixture.whenStable();
    expect(cardHeight(box)).toBe('200px');
  });

  it('uses the defaults when the theme sets nothing', async () => {
    properties = {};
    const { fixture, box, observer } = await render();
    observer.resize(372, 472);
    await fixture.whenStable();
    expect(cardHeight(box)).toBe('125px');
  });

  it('stops observing when destroyed', async () => {
    const { fixture, observer } = await render();
    fixture.destroy();
    expect(observer.disconnected).toBe(true);
  });
});
