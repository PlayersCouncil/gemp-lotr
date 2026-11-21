import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';
import { ScenarioInfo, ScenarioMessage, ScenarioZoom } from '../../../core/game/scenario/scenario-events';
import { GuideHint, NextClicks, ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { emptyGame, GameModel } from '../../../core/game/state/game-model';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { ScenarioLayerComponent } from './scenario-layer.component';

const INFO = { type: 'info', id: 'basics-01', humanSeat: 'freeps', bubbleGap: null } as ScenarioInfo;

const message = (extra: Partial<ScenarioMessage> = {}): ScenarioMessage => ({
  index: 0,
  header: 'Frodo',
  html: '<p>This is <b>Frodo</b>.</p>',
  anchors: ['card:frodo'],
  gap: null,
  highlight: [],
  highlightStyle: 'gold',
  avoid: [],
  side: 'auto',
  advance: 'next',
  advanceCard: null,
  choices: [],
  on: null,
  zoom: null,
  ...extra,
});

const NO_CLICKS: NextClicks = { cardIds: [], button: null, options: [], numbers: [] };

/**
 * The page the layer draws over, by its DOM contract; each element on the screen where its `data-box` says
 * ("left,top,width,height") in a 1280 by 800 window.
 */
const PAGE = `
  <button data-card-id="7" data-box="500,400,100,140"></button>
  <button data-card-id="8" data-box="620,400,100,140"></button>
  <button data-card-id="9" data-box="500,30,100,140"></button>
  <div class="gd-game__path"><button data-card-id="30" data-box="0,0,50,70"></button></div>
  <div data-game="twilight" data-box="1200,20,40,40"></div>
  <gd-game-alert data-box="1050,560,200,120">
    <button data-decision-button="Done" data-box="1100,600,60,24"></button>
  </gd-game-alert>
  <button data-decision-choice="1" data-box="1100,650,60,24"></button>
  <dialog class="gd-dialog" open data-box="100,100,200,200"></dialog>
  <input class="typing" />
`;

/** How big the layer's own boxes are (jsdom lays nothing out). */
const SIZES: Record<string, [number, number]> = { 'gd-scenario-bubble': [300, 120], 'gd-scenario-hint': [200, 40] };

describe('ScenarioLayerComponent', () => {
  let fixture: ComponentFixture<ScenarioLayerComponent>;
  let el: HTMLElement;
  let page: HTMLElement;
  let frames: Map<number, FrameRequestCallback>;
  let lastFrame = 0;
  const saved = {
    frame: window.requestAnimationFrame,
    cancel: window.cancelAnimationFrame,
    offsetWidth: Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetWidth'),
    offsetHeight: Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetHeight'),
  };

  const guide = {
    message: signal<ScenarioMessage | null>(null),
    hint: signal<GuideHint | null>(null),
    nextClicks: signal<NextClicks>(NO_CLICKS),
    info: signal<ScenarioInfo | null>(INFO),
    role: signal<'freeps' | 'shadow'>('freeps'),
    step: signal<{ cards: Record<string, string>; parts: Record<string, number[]> } | null>(null),
    count: signal<{ at: number; of: number } | null>(null),
    nextLabel: signal<string | null>('Next'),
    nudges: signal(0),
    playing: signal(true),
    picking: signal(false),
    next: vi.fn(),
    choose: vi.fn(),
  };
  const model = signal<GameModel>(emptyGame());
  let room: ScenarioRoom;

  /** The bubble's box: where the layer put it. */
  const placed = (element: HTMLElement): [number, number] => [
    Number.parseFloat(element.style.getPropertyValue('--gd-left')) || 0,
    Number.parseFloat(element.style.getPropertyValue('--gd-top')) || 0,
  ];

  beforeEach(() => {
    frames = new Map();
    window.requestAnimationFrame = (callback) => {
      frames.set(++lastFrame, callback);
      return lastFrame;
    };
    window.cancelAnimationFrame = (id) => frames.delete(id);
    const size = (index: 0 | 1) =>
      function (this: HTMLElement) {
        const known = Object.entries(SIZES).find(([name]) => this.classList.contains(name));
        return known ? known[1][index] : 0;
      };
    Object.defineProperty(HTMLElement.prototype, 'offsetWidth', { configurable: true, get: size(0) });
    Object.defineProperty(HTMLElement.prototype, 'offsetHeight', { configurable: true, get: size(1) });
    Object.defineProperty(document.documentElement, 'clientWidth', { configurable: true, get: () => 1280 });
    Object.defineProperty(document.documentElement, 'clientHeight', { configurable: true, get: () => 800 });
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
      let [left = 0, top = 0, width = 0, height = 0] = (this.getAttribute('data-box') ?? '').split(',').map(Number);
      if (this instanceof HTMLElement) {
        const known = Object.entries(SIZES).find(([name]) => this.classList.contains(name));
        if (known) [left, top, width, height] = [...placed(this), ...known[1]];
      }
      return { left, top, width, height, right: left + width, bottom: top + height, x: left, y: top } as DOMRect;
    });

    page = document.createElement('gemp-game');
    page.innerHTML = PAGE;
    document.body.append(page);

    guide.message.set(null);
    guide.hint.set(null);
    guide.nextClicks.set(NO_CLICKS);
    guide.info.set(INFO);
    guide.role.set('freeps');
    guide.step.set({ cards: { frodo: '7', sam: '8', gandalf: '9' }, parts: {} });
    guide.count.set(null);
    guide.nextLabel.set('Next');
    guide.nudges.set(0);
    guide.playing.set(true);
    guide.next.mockClear();
    guide.choose.mockClear();
    model.set({
      ...emptyGame(),
      participantId: 'asdf',
      players: ['asdf', '~Scenario'],
      cards: { '30': { id: '30', zone: 'ADVENTURE_PATH', index: 1 } } as unknown as GameModel['cards'],
    });

    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        // (the card names in a message show the card as the game does: a meta-site as one)
        GameCardViews,
        ScenarioRoom,
        { provide: ScenarioGuide, useValue: guide },
        { provide: GameSession, useValue: { model } },
      ],
    });
    room = TestBed.inject(ScenarioRoom);
    fixture = TestBed.createComponent(ScenarioLayerComponent);
    el = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    page.remove();
    window.requestAnimationFrame = saved.frame;
    window.cancelAnimationFrame = saved.cancel;
    for (const name of ['offsetWidth', 'offsetHeight'] as const) {
      const descriptor = saved[name];
      if (descriptor) Object.defineProperty(HTMLElement.prototype, name, descriptor);
    }
    delete (document.documentElement as { clientWidth?: number }).clientWidth;
    delete (document.documentElement as { clientHeight?: number }).clientHeight;
    vi.restoreAllMocks();
  });

  /** The next frame: everything measured again, and drawn. */
  function frame() {
    fixture.detectChanges();
    const due = [...frames.values()];
    frames.clear();
    for (const callback of due) callback(0);
    fixture.detectChanges();
  }
  const bubble = () => el.querySelector<HTMLElement>('section.gd-scenario-bubble');
  const outlines = () =>
    [...el.querySelectorAll<HTMLElement>('.gd-scenario-outline')].map(
      (outline) =>
        `${outline.dataset['look']} ${['left', 'top', 'width', 'height'].map((side) => outline.style.getPropertyValue(`--gd-${side}`)).join(' ')}`,
    );
  const press = (key: string, init: KeyboardEventInit = {}, target: EventTarget = document.body) => {
    const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true, ...init });
    target.dispatchEvent(event);
    return event;
  };

  it('is not in the page while nothing is up, and measures nothing', () => {
    expect(el.querySelector('.gd-scenario')).toBeNull();
    expect(frames.size).toBe(0);
  });

  it('puts the message beside what it is about, with an arrow, and outlines what it highlights', () => {
    guide.message.set(message({ highlight: ['card:frodo', 'card:nobody'], avoid: ['ui:twilightPool', 'ui:gone'] }));
    guide.count.set({ at: 1, of: 3 });
    fixture.detectChanges();
    // Kept out of sight until it has been measured
    expect(bubble()?.hasAttribute('data-placed')).toBe(false);
    expect(bubble()?.hasAttribute('data-headed')).toBe(true);
    expect(el.querySelector('.gd-scenario-bubble__title')?.textContent).toBe('Frodo');
    expect(el.querySelector('.gd-scenario-bubble__count')?.textContent).toBe('1 / 3');
    expect(el.querySelector('.gd-scenario-bubble__body')?.innerHTML).toBe('<p>This is <b>Frodo</b>.</p>');
    frame();
    expect(bubble()?.hasAttribute('data-placed')).toBe(true);
    // Above Frodo, level with his middle, the stock gap away
    expect(placed(bubble()!)).toEqual([400, 266]);
    const arrow = el.querySelector<HTMLElement>('.gd-scenario-bubble__arrow');
    expect(arrow?.dataset['edge']).toBe('bottom');
    expect(arrow?.style.getPropertyValue('--gd-along')).toBe('150px');
    expect(el.querySelector('svg.gd-scenario-pointer')).toBeNull();
    expect(outlines()).toEqual(['gold 496px 396px 108px 148px']);
    // Measured again each frame; drawn again only when something moved
    const before = el.querySelector('.gd-scenario-outline');
    frame();
    expect(el.querySelector('.gd-scenario-outline')).toBe(before);
    page.querySelector('[data-card-id="7"]')?.setAttribute('data-box', '500,420,100,140');
    frame();
    expect(outlines()).toEqual(['gold 496px 416px 108px 148px']);
  });

  it("outlines a skirmish's own side by the role the person plays now, not by their seat", () => {
    page.insertAdjacentHTML(
      'beforeend',
      `<section class="gd-game__fight" data-fight="skirmish" data-box="300,200,300,150">
        <span class="gd-game__strength" data-side="free-peoples" data-box="390,300,20,20"></span>
        <span class="gd-game__strength" data-side="shadow" data-box="470,300,20,20"></span>
      </section>`,
    );
    guide.message.set(message({ anchors: [], highlight: ['ui:skirmish:selfStrength'] }));
    frame();
    expect(outlines()).toEqual(['gold 386px 296px 28px 28px']);
    // (the opponent's turn of a two-sided game: the person is the Shadow player, from the same seat)
    guide.role.set('shadow');
    frame();
    expect(outlines()).toEqual(['gold 466px 296px 28px 28px']);
  });

  it("outlines nothing to click while the page's own question is open over the table", () => {
    guide.nextClicks.set({ ...NO_CLICKS, button: 'Done' });
    page.insertAdjacentHTML('beforeend', '<dialog class="gd-dialog" open data-dialog="confirm"></dialog>');
    frame();
    expect(outlines()).toEqual([]);
    page.querySelector('[data-dialog="confirm"]')?.remove();
    frame();
    expect(outlines()).toEqual(['next 1096px 596px 68px 32px']);
  });

  it('points at the way to the number a step takes, then at OK', () => {
    page.insertAdjacentHTML(
      'beforeend',
      `<button data-decision="integer-down" data-box="900,300,20,20"></button>
      <output data-decision="integer" data-box="930,300,20,20">0</output>
      <button data-decision="integer-up" data-box="960,300,20,20"></button>
      <button data-decision-button="OK" data-box="930,340,40,20"></button>`,
    );
    guide.nextClicks.set({ ...NO_CLICKS, numbers: [1] });
    frame();
    expect(outlines()).toEqual(['next 956px 296px 28px 28px']);
    page.querySelector('output')!.textContent = '3';
    frame();
    expect(outlines()).toEqual(['next 896px 296px 28px 28px']);
    page.querySelector('output')!.textContent = '1';
    frame();
    expect(outlines()).toEqual(['next 926px 336px 48px 28px']);
    // (no number asked for on the page: nothing to point at)
    page.querySelector('output')!.remove();
    frame();
    expect(outlines()).toEqual([]);
  });

  it('presses Next, or one of the choices a message ends in', () => {
    guide.message.set(message({ choices: [{ name: 'yes', label: 'Yes please' }] }));
    fixture.detectChanges();
    const choice = el.querySelector<HTMLButtonElement>('[data-choice="yes"]');
    expect(choice?.textContent.trim()).toBe('Yes please');
    choice?.click();
    expect(guide.choose).toHaveBeenCalledWith('yes');
    el.querySelector<HTMLButtonElement>('[data-scenario="next"]')?.click();
    expect(guide.next).toHaveBeenCalled();
    // No button at all: no footer
    guide.message.set(message({ header: null }));
    guide.nextLabel.set(null);
    fixture.detectChanges();
    expect(el.querySelector('.gd-scenario-bubble__foot')).toBeNull();
    expect(bubble()?.hasAttribute('data-headed')).toBe(false);
  });

  it('shakes the buttons each time the player has to read on first', () => {
    guide.message.set(message());
    fixture.detectChanges();
    const foot = () => el.querySelector<HTMLElement>('.gd-scenario-bubble__foot');
    expect(foot()?.hasAttribute('data-nudge')).toBe(false);
    guide.nudges.set(1);
    fixture.detectChanges();
    expect(foot()?.dataset['nudge']).toBe('1');
    guide.nudges.set(2);
    fixture.detectChanges();
    expect(foot()?.dataset['nudge']).toBe('0');
  });

  it('outlines what is to be clicked next: cards, the button, the choice', () => {
    guide.nextClicks.set({ cardIds: ['8', '99'], button: 'Done', options: [1], numbers: [] });
    frame();
    expect(bubble()).toBeNull();
    expect(outlines()).toEqual([
      'next 616px 396px 108px 148px',
      'next 1096px 596px 68px 32px',
      'next 1096px 646px 68px 32px',
    ]);
    // Nothing more to click: gone
    guide.nextClicks.set(NO_CLICKS);
    fixture.detectChanges();
    expect(el.querySelector('.gd-scenario')).toBeNull();
    frame();
    expect(frames.size).toBe(0);
  });

  it('draws a line to each of several things it is about, and none for a message about nothing', () => {
    guide.message.set(message({ anchors: ['card:frodo', 'card:gandalf'] }));
    frame();
    expect(el.querySelectorAll('svg.gd-scenario-pointer .gd-scenario-pointer__line')).toHaveLength(2);
    expect(el.querySelector('.gd-scenario-bubble__arrow')).toBeNull();

    guide.message.set(message({ anchors: [] }));
    frame();
    expect(el.querySelector('svg.gd-scenario-pointer')).toBeNull();
    expect(el.querySelector('.gd-scenario-bubble__arrow')).toBeNull();
    // Near the middle of the window
    expect(placed(bubble()!)[0]).toBe(490);
  });

  it('keeps to the room a panel at the right leaves, at the distance the scenario sets', () => {
    room.reservedRight.set(400);
    guide.info.set({ ...INFO, bubbleGap: '40px' });
    guide.message.set(message({ anchors: [] }));
    frame();
    expect(placed(bubble()!)[0]).toBeLessThanOrEqual(1280 - 400 - 300 - 8);
    guide.message.set(message({ side: 'below' }));
    frame();
    expect(placed(bubble()!)).toEqual([400, 580]);
  });

  it('folds away and opens again, without its arrow while folded; a new message comes whole', () => {
    guide.message.set(message());
    frame();
    const fold = el.querySelector<HTMLButtonElement>('.gd-scenario-bubble__fold');
    expect(fold?.getAttribute('aria-expanded')).toBe('true');
    expect(fold?.title).toBe('Fold this away');
    fold?.click();
    fixture.detectChanges();
    expect(bubble()?.hasAttribute('data-collapsed')).toBe(true);
    expect(fold?.getAttribute('aria-expanded')).toBe('false');
    expect(fold?.title).toBe('Show this again');
    expect(el.querySelector('.gd-scenario-bubble__arrow')).toBeNull();
    // Folded, the keys don't read on
    press(' ');
    expect(guide.next).not.toHaveBeenCalled();
    guide.message.set(message({ header: 'Sam', anchors: ['card:sam'] }));
    fixture.detectChanges();
    expect(bubble()?.hasAttribute('data-collapsed')).toBe(false);
  });

  it('stays where it is dragged by its head (with a line to what it is about), until its message changes', () => {
    guide.message.set(message());
    frame();
    const head = el.querySelector<HTMLElement>('.gd-scenario-bubble__head')!;
    const pointer = (type: string, x: number, y: number, on: Element = head) => {
      const event = new MouseEvent(type, { clientX: x, clientY: y, bubbles: true, cancelable: true, button: 0 });
      Object.defineProperty(event, 'pointerId', { value: 1 });
      on.dispatchEvent(event);
    };
    // Pressing the fold button is no drag
    pointer('pointerdown', 410, 270, el.querySelector('.gd-scenario-bubble__fold')!);
    pointer('pointermove', 100, 100);
    frame();
    expect(bubble()?.hasAttribute('data-moved')).toBe(false);
    pointer('pointerdown', 410, 270);
    pointer('pointermove', 110, 120);
    pointer('pointerup', 160, 170);
    frame();
    expect(bubble()?.hasAttribute('data-moved')).toBe(true);
    expect(placed(bubble()!)).toEqual([150, 166]);
    expect(el.querySelector('.gd-scenario-bubble__arrow')).toBeNull();
    expect(el.querySelectorAll('.gd-scenario-pointer__line')).toHaveLength(1);
    guide.message.set(message({ header: 'Again' }));
    frame();
    expect(bubble()?.hasAttribute('data-moved')).toBe(false);
    expect(placed(bubble()!)).toEqual([400, 266]);
  });

  it('reads on with Space or Escape, unless the key is meant for something else', () => {
    guide.message.set(message());
    fixture.detectChanges();
    const button = el.querySelector<HTMLButtonElement>('[data-scenario="next"]')!;
    button.focus();
    const space = press(' ');
    expect(guide.next).toHaveBeenCalledTimes(1);
    expect(space.defaultPrevented).toBe(true);
    // (the focused button isn't pressed by the key as well)
    expect(document.activeElement).not.toBe(button);
    press('Escape');
    expect(guide.next).toHaveBeenCalledTimes(2);
    press('Enter');
    press(' ', { repeat: true });
    press(' ', { ctrlKey: true });
    press(' ', { altKey: true });
    press(' ', { metaKey: true });
    press(' ', {}, page.querySelector('input.typing')!);
    expect(guide.next).toHaveBeenCalledTimes(2);
    // Escape for the card's information that is open
    TestBed.inject(CardInfoService).show({ ref: null } as CardView);
    press('Escape');
    expect(guide.next).toHaveBeenCalledTimes(2);
    press(' ');
    expect(guide.next).toHaveBeenCalledTimes(3);
    // Nothing to press, or a replay
    guide.nextLabel.set(null);
    press(' ');
    guide.nextLabel.set('Next');
    guide.playing.set(false);
    press(' ');
    expect(guide.next).toHaveBeenCalledTimes(3);
    // An author picking something on the table (the bubble fades meanwhile)
    guide.playing.set(true);
    guide.picking.set(true);
    fixture.detectChanges();
    expect(el.querySelector('.gd-scenario')?.hasAttribute('data-picking')).toBe(true);
    press('Escape');
    expect(guide.next).toHaveBeenCalledTimes(3);
    guide.picking.set(false);
    // Gone with the page
    guide.playing.set(true);
    fixture.destroy();
    press(' ');
    expect(guide.next).toHaveBeenCalledTimes(3);
  });

  describe('a hint', () => {
    const hint = (extra: Partial<GuideHint>): GuideHint => ({
      html: '<p>Not now.</p>',
      cardId: null,
      button: null,
      option: null,
      ...extra,
    });
    const hintAt = () => {
      frame();
      const box = el.querySelector<HTMLElement>('.gd-scenario-hint')!;
      expect(box.hasAttribute('data-placed')).toBe(true);
      return placed(box);
    };

    it('is said above the card clicked, or below it where there is no room above', () => {
      guide.hint.set(hint({ cardId: '8' }));
      fixture.detectChanges();
      expect(el.querySelector('.gd-scenario-hint')?.innerHTML).toBe('<p>Not now.</p>');
      expect(hintAt()).toEqual([570, 350]);
      guide.hint.set(hint({ cardId: '9' }));
      expect(hintAt()).toEqual([450, 180]);
    });

    it('is said at the button or the choice clicked, or near the top when it was neither', () => {
      guide.hint.set(hint({ button: 'Done' }));
      expect(hintAt()).toEqual([1030, 550]);
      guide.hint.set(hint({ option: 1 }));
      expect(hintAt()).toEqual([1030, 600]);
      guide.hint.set(hint({}));
      expect(hintAt()).toEqual([540, 80]);
      // A card no longer there: as though it were neither
      guide.hint.set(hint({ cardId: '99' }));
      expect(hintAt()).toEqual([540, 80]);
    });
  });

  describe('a card shown large', () => {
    const zoom = (extra: Partial<ScenarioZoom> = {}): ScenarioZoom => ({
      blueprint: '1_290',
      image: null,
      magnify: 2,
      focus: [{ name: 'strength', rect: [5, 60, 12, 10] }],
      ...extra,
    });

    it('is drawn with what is in focus outlined, the bubble beside it pointing at that part, the table left out', () => {
      guide.message.set(message({ zoom: zoom(), highlight: ['card:frodo'] }));
      guide.nextClicks.set({ cardIds: ['8'], button: null, options: [], numbers: [] });
      frame();
      const shown = el.querySelector<HTMLElement>('.gd-scenario-zoom');
      // Read before going on: it takes the pointer
      expect(shown?.hasAttribute('data-blocks')).toBe(true);
      const picture = el.querySelector<HTMLImageElement>('img.gd-scenario-zoom__card');
      const card = TestBed.inject(CardViews).create({ ref: null, blueprintId: '1_290', zone: 'SPECIAL' });
      expect(picture?.getAttribute('src')).toBe(card.imageUrl);
      expect(el.querySelector('.gd-scenario-zoom__dim path')?.getAttribute('d')).toMatch(/^M0 0H/);
      expect(el.querySelectorAll('.gd-scenario-zoom__mark')).toHaveLength(1);
      // No outlines over it
      expect(outlines()).toEqual([]);
      const frameBox = el.querySelector<HTMLElement>('.gd-scenario-zoom__frame')!;
      const [frameLeft] = placed(frameBox);
      const frameWidth = Number.parseFloat(frameBox.style.getPropertyValue('--gd-width'));
      // Beside it, on its right
      expect(placed(bubble()!)[0]).toBeGreaterThanOrEqual(Math.round(frameLeft + frameWidth));

      // Its picture's own shape, once loaded
      let natural = 400;
      Object.defineProperties(picture, { naturalWidth: { get: () => natural }, naturalHeight: { value: 400 } });
      picture?.dispatchEvent(new Event('load'));
      frame();
      expect(frameBox.style.getPropertyValue('--gd-width')).toBe(frameBox.style.getPropertyValue('--gd-height'));
      // A picture that didn't load keeps the shape it had
      natural = 0;
      picture?.dispatchEvent(new Event('load'));
      frame();
      expect(frameBox.style.getPropertyValue('--gd-width')).toBe(frameBox.style.getPropertyValue('--gd-height'));
    });

    it('lets clicks through while the decision is answered under it; shows a picture by its address', () => {
      guide.message.set(
        message({ advance: 'decision', zoom: zoom({ blueprint: null, image: '/x/map.png', focus: [] }) }),
      );
      frame();
      expect(el.querySelector('.gd-scenario-zoom')?.hasAttribute('data-blocks')).toBe(false);
      expect(el.querySelector('img.gd-scenario-zoom__card')?.getAttribute('src')).toBe('/x/map.png');
      expect(el.querySelector('.gd-scenario-zoom__dim')).toBeNull();
      expect(el.querySelector('.gd-scenario-pointer, .gd-scenario-bubble__arrow')).toBeNull();
    });

    it('is not drawn without a card or a picture to show', () => {
      guide.message.set(message({ zoom: zoom({ blueprint: null }) }));
      frame();
      expect(el.querySelector('.gd-scenario-zoom')).toBeNull();
      expect(bubble()).not.toBeNull();
    });
  });

  it('stops measuring when it is taken off the page', () => {
    guide.message.set(message());
    frame();
    expect(frames.size).toBe(1);
    fixture.destroy();
    expect(frames.size).toBe(0);
  });
});
