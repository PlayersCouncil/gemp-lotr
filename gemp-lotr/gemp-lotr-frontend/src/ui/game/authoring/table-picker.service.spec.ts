import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { AuthoringBoard, BoardCard, DraftState } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { PICK_TOLD } from '../../../core/game/authoring/picking';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { emptyGame, GameCard, GameModel, NO_SKIRMISH_NUMBERS } from '../../../core/game/state/game-model';
import { draftState } from '../../../testing/authoring';
import { pageAnchorContext, resolveAnchor } from '../scenario/scenario-anchors';
import { TablePicker } from './table-picker.service';

/**
 * A game page by its DOM contract, each element where its `data-box` says ("left,top,width,height"); later elements
 * lie over earlier ones.
 */
const PAGE = `
  <div class="gd-game__path" data-box="0,0,50,150">
    <button data-card-id="30" data-zone="ADVENTURE_PATH" data-box="0,0,50,70"></button>
    <button data-card-id="31" data-zone="ADVENTURE_PATH" data-box="0,80,50,70"></button>
  </div>
  <section class="gd-game__row" data-row="characters-player" data-box="100,400,800,150">
    <button data-card-id="26" data-under="attached" data-box="100,410,100,140"></button>
    <button data-card-id="25" data-box="120,410,100,140"><img data-box="130,420,20,20" /></button>
    <button data-card-id="40" data-box="300,410,100,140"></button>
  </section>
  <section class="gd-game__row" data-row="shadow" data-box="100,250,800,140">
    <button data-card-id="33" data-box="110,260,90,120"></button>
  </section>
  <section class="gd-game__row" data-row="hand" data-box="100,600,800,150"></section>
  <section class="gd-game__row" data-row="support-player" data-box="100,560,800,30"></section>
  <section class="gd-game__row" data-row="characters-opponent" data-box="100,150,800,90"></section>
  <section class="gd-game__row" data-row="support-opponent" data-box="100,100,800,40"></section>
  <section class="gd-game__fight" data-fight="assignment" data-box="920,250,200,150">
    <div class="gd-game__side" data-side="shadow"><button data-card-id="34" data-box="930,260,80,100"></button></div>
    <div class="gd-game__side" data-side="free-peoples"><button data-card-id="32" data-box="1020,260,80,100"></button></div>
  </section>
  <section class="gd-game__fight" data-fight="skirmish" data-box="920,420,300,150">
    <div class="gd-game__side" data-side="shadow">
      <button data-card-id="35" data-box="930,430,80,100"></button>
      <button data-card-id="37" data-box="1015,430,60,100"></button>
    </div>
    <div class="gd-game__side" data-side="free-peoples"><button data-card-id="36" data-box="1100,430,80,100"></button></div>
    <span class="gd-game__strength" data-side="free-peoples" data-box="1050,540,20,20"></span>
    <span class="gd-game__strength" data-side="shadow" data-box="1020,540,20,20"></span>
    <span class="gd-game__damage" data-side="free-peoples" data-box="1080,540,20,20"></span>
    <span class="gd-game__damage" data-side="shadow" data-box="990,540,20,20"></span>
  </section>
  <gd-game-state data-box="60,0,40,300">
    <span data-stat="deck0" data-box="60,10,30,20"></span>
    <span data-stat="discard0" data-box="60,40,30,20"></span>
    <span data-stat="deadPile0" data-box="60,70,30,20"></span>
    <span data-stat="adventureDeck0" data-box="60,100,30,20"></span>
    <span data-stat="removedPile0" data-box="60,130,30,20"></span>
    <span data-stat="hand0" data-box="60,160,30,20"></span>
    <span data-stat="deck1" data-box="60,190,30,20"></span>
    <span data-stat="discard1" data-box="60,220,30,20"></span>
    <span data-stat="deadPile1" data-box="60,250,30,20"></span>
    <span data-stat="adventureDeck1" data-box="60,280,30,8"></span>
    <span data-stat="removedPile1" data-box="80,280,10,8"></span>
    <span data-stat="hand1" data-box="90,280,10,8"></span>
    <span data-game="twilight" data-box="60,300,30,30"></span>
  </gd-game-state>
  <span data-game="phase" data-box="1300,0,50,20"></span>
  <span data-game="initiative" data-box="1300,30,50,20"></span>
  <span data-game="ruleof4" data-box="1300,60,50,20"></span>
  <span class="gd-game-state__decision-clock" data-box="1300,90,50,20"></span>
  <gd-game-alert data-box="1300,120,200,100">
    <button data-decision-button="Pass" data-box="1310,190,40,20"></button>
    <button data-decision-button="Done" data-box="1360,190,40,20"></button>
    <button data-decision-button="Clear selection" data-box="1410,190,40,20"></button>
  </gd-game-alert>
  <gd-game-tabs data-box="1300,240,200,200">
    <a data-tab="settings" data-box="1310,245,40,15"></a>
  </gd-game-tabs>
  <gd-game-stats data-box="1300,460,200,40">
    <span data-game="fp-archery" data-box="1310,465,20,20"></span>
    <span data-game="shadow-archery" data-box="1340,465,20,20"></span>
    <span data-game="move" data-box="1400,465,20,20"></span>
  </gd-game-stats>
  <dialog class="gd-dialog" open data-box="1300,520,200,200">
    <button data-card-id="extra38" data-box="1310,530,60,80"></button>
  </dialog>
  <dialog class="gd-card-info" open data-box="1300,760,150,150"></dialog>
  <gd-game-author data-box="1600,0,400,1000"><button class="in-panel" data-box="1610,10,50,20"></button></gd-game-author>
`;

const boardCard = (alias: string, cardId: number, extra: Partial<BoardCard> = {}): BoardCard => ({
  alias,
  blueprintId: '1_1',
  name: alias,
  type: 'COMPANION',
  owner: 'freeps',
  cardId,
  zone: 'characters',
  at: null,
  wounds: 0,
  tokens: 0,
  on: null,
  ...extra,
});

const BOARD: AuthoringBoard = {
  cards: [
    boardCard('frodo', 25),
    boardCard('ring', 26, { zone: 'attached', on: 'frodo' }),
    boardCard('sam', 32),
    boardCard('orc', 33, { owner: 'shadow', zone: 'minions' }),
    boardCard('troll', 34, { owner: 'shadow', zone: 'minions' }),
    boardCard('goblin', 35, { owner: 'shadow', zone: 'minions' }),
    boardCard('merry', 36),
    boardCard('gimli', 38, { zone: 'hand' }),
  ],
  twilight: 0,
  burdens: 0,
  threats: 0,
  sites: { freeps: [], shadow: [] },
};

const gameCard = (id: string, zone: string, index: number | null = null): GameCard => ({
  id,
  blueprintId: '1_1',
  zone,
  owner: 'asdf',
  hindered: false,
  numbersHidden: false,
  index,
  attachedTo: null,
  stackedOn: null,
  attachedAt: 0,
  tokens: {},
  inactive: false,
  assignedTo: null,
  inSkirmish: false,
  testingText: null,
  backSideTestingText: null,
});

function boxOf(element: Element): DOMRect {
  const [left = 0, top = 0, width = 0, height = 0] = (element.getAttribute('data-box') ?? '').split(',').map(Number);
  return { left, top, width, height, right: left + width, bottom: top + height, x: left, y: top } as DOMRect;
}

describe('TablePicker', () => {
  let picker: TablePicker;
  let root: HTMLElement;
  let state: ReturnType<typeof signal<DraftState | null>>;
  let model: ReturnType<typeof signal<GameModel>>;
  let authoring: { draft: typeof state; startPicking: ReturnType<typeof vi.fn>; stopPicking: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    root = document.createElement('div');
    root.innerHTML = PAGE;
    document.body.appendChild(root);
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
      return boxOf(this);
    });
    // What is at a point: the elements whose box holds it, the last drawn (and the innermost) first
    (document as { elementsFromPoint: (x: number, y: number) => Element[] }).elementsFromPoint = (x, y) =>
      [...document.querySelectorAll('[data-box]')]
        .filter((element) => {
          const box = boxOf(element);
          return box.width > 0 && x >= box.left && x <= box.right && y >= box.top && y <= box.bottom;
        })
        .reverse();
    state = signal<DraftState | null>(draftState({ board: BOARD }));
    model = signal<GameModel>({
      ...emptyGame(),
      participantId: 'asdf',
      players: ['asdf', '~Scenario'],
      cards: {
        '30': gameCard('30', 'ADVENTURE_PATH', 1),
        '31': gameCard('31', 'ADVENTURE_PATH', 2),
        '40': gameCard('40', 'FREE_CHARACTERS'),
      },
      skirmish: { characterId: '36', minionIds: ['35'], shown: NO_SKIRMISH_NUMBERS },
    });
    authoring = { draft: state, startPicking: vi.fn(), stopPicking: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        TablePicker,
        { provide: Authoring, useValue: authoring },
        { provide: GameSession, useValue: { model } },
        { provide: ScenarioGuide, useValue: { info: signal({ humanSeat: 'freeps' }), role: signal('freeps') } },
      ],
    });
    picker = TestBed.inject(TablePicker);
  });

  afterEach(() => {
    root.remove();
    vi.restoreAllMocks();
    delete (document as { elementsFromPoint?: unknown }).elementsFromPoint;
  });

  const fire = (type: string, x: number, y: number, target: Element = document.body) => {
    const event = new MouseEvent(type, { clientX: x, clientY: y, bubbles: true, cancelable: true });
    target.dispatchEvent(event);
    return event;
  };

  /** What each point picks; each picked anchor must then point back at that point (scenario-anchors.ts). */
  const ROUND_TRIP: readonly [x: number, y: number, anchor: string][] = [
    [160, 500, 'card:frodo'],
    [140, 425, 'card:frodo'],
    [105, 500, 'card:ring'],
    [150, 300, 'card:orc'],
    [25, 30, 'ui:site:1'],
    [25, 120, 'ui:site:2'],
    [1060, 300, 'card:sam'],
    [1110, 380, 'ui:assignment:sam'],
    [600, 500, 'zone:self.characters'],
    [600, 300, 'zone:minions'],
    [600, 700, 'zone:self.hand'],
    [600, 570, 'zone:self.support'],
    [600, 200, 'zone:opp.characters'],
    [600, 120, 'zone:opp.support'],
    [70, 20, 'pile:self.deck'],
    [70, 50, 'pile:self.discard'],
    [70, 80, 'pile:self.deadPile'],
    [70, 110, 'pile:self.adventureDeck'],
    [70, 140, 'pile:self.removed'],
    [70, 170, 'pile:self.hand'],
    [70, 200, 'pile:opp.deck'],
    [70, 230, 'pile:opp.discard'],
    [70, 260, 'pile:opp.deadPile'],
    [70, 284, 'pile:opp.adventureDeck'],
    [85, 284, 'pile:opp.removed'],
    [95, 284, 'pile:opp.hand'],
    [70, 310, 'ui:twilightPool'],
    [1320, 10, 'ui:phase'],
    [1320, 40, 'ui:initiative'],
    [1320, 70, 'ui:ruleof4'],
    [1320, 100, 'ui:clock'],
    [30, 75, 'ui:advPath'],
    [1450, 150, 'ui:alertBox'],
    [1450, 300, 'ui:gameLog'],
    [1320, 250, 'ui:settings'],
    [1200, 450, 'ui:skirmish'],
    [1012, 450, 'ui:skirmish:shadow'],
    [960, 450, 'card:goblin'],
    [1140, 450, 'card:merry'],
    [1060, 550, 'ui:skirmish:selfStrength'],
    [1090, 550, 'ui:skirmish:selfDamage'],
    [1030, 550, 'ui:skirmish:oppStrength'],
    [1000, 550, 'ui:skirmish:oppDamage'],
    [1450, 490, 'ui:stats'],
    [1320, 475, 'ui:stats:archery'],
    [1410, 475, 'ui:stats:move'],
    [1450, 700, 'ui:dialog'],
    [1340, 570, 'card:gimli'],
    [1400, 850, 'ui:cardInfo'],
    [1320, 200, 'ui:button:pass'],
    [1370, 200, 'ui:button:done'],
    [1420, 200, 'ui:button:reset'],
  ];

  it('picks the smallest thing under the pointer, and each points back where it was clicked', () => {
    const context = pageAnchorContext(document, model(), {
      cards: { frodo: '25', ring: '26', sam: '32', orc: '33', troll: '34', goblin: '35', merry: '36', gimli: '38' },
      parts: {},
      humanSeat: 'freeps',
      humanRole: 'freeps',
    });
    for (const [x, y, anchor] of ROUND_TRIP) {
      expect(picker.thingAt(x, y)?.anchor, `${x},${y}`).toBe(anchor);
      const rect = resolveAnchor(context, anchor);
      expect(rect, anchor).not.toBeNull();
      expect(
        x >= rect!.left && x <= rect!.left + rect!.width && y >= rect!.top && y <= rect!.top + rect!.height,
        anchor,
      ).toBe(true);
    }
  });

  it('draws the part of a card under another that shows; leaves out cards it has no name for', () => {
    expect(picker.thingAt(105, 500)).toEqual({
      anchor: 'card:ring',
      box: { left: 100, top: 410, width: 20, height: 140 },
    });
    // (a card of no name of the scenario's: what is round it is picked)
    expect(picker.thingAt(350, 500)?.anchor).toBe('zone:self.characters');
    // Nothing there
    expect(picker.thingAt(1550, 990)).toBeNull();
    // With no board, cards are not picked
    state.set(draftState({ board: null }));
    expect(picker.thingAt(160, 500)?.anchor).toBe('zone:self.characters');
    state.set(null);
    expect(picker.thingAt(160, 500)?.anchor).toBe('zone:self.characters');
  });

  it('takes no card behind what lies over it', () => {
    const cover = document.createElement('div');
    cover.setAttribute('data-box', '150,450,40,40');
    root.appendChild(cover);
    expect(picker.thingAt(160, 460)?.anchor).toBe('zone:self.characters');
  });

  it('picks with a click on the table: told, outlined while hovering, the anchor resolved', async () => {
    const picked = picker.pick();
    expect(picker.picking()).toBe(true);
    expect(authoring.startPicking).toHaveBeenCalledWith(PICK_TOLD);
    fire('mousemove', 160, 500);
    expect(picker.hover()).toEqual({
      anchor: 'card:frodo',
      box: { left: 120, top: 410, width: 100, height: 140 },
      label: { left: 120, top: 388 },
    });
    fire('mousemove', 1550, 990);
    expect(picker.hover()).toBeNull();
    // Presses never reach the table
    for (const type of ['mousedown', 'mouseup', 'pointerdown', 'contextmenu']) {
      expect(fire(type, 160, 500).defaultPrevented, type).toBe(true);
    }
    const heard = vi.fn();
    document.body.addEventListener('click', heard);
    const click = fire('click', 160, 500);
    expect(await picked).toBe('card:frodo');
    expect(click.defaultPrevented).toBe(true);
    expect(heard).not.toHaveBeenCalled();
    expect(picker.picking()).toBe(false);
    expect(picker.hover()).toBeNull();
    expect(authoring.stopPicking).toHaveBeenCalledTimes(1);
    // Over: the page is its own again
    expect(fire('mousedown', 160, 500).defaultPrevented).toBe(false);
    document.body.removeEventListener('click', heard);
  });

  it('ends with nothing on a click in the panel, on nothing, on Escape, on cancel or another pick', async () => {
    const panelButton = root.querySelector('.in-panel')!;
    let picked = picker.pick({ told: 'Click a card' });
    expect(authoring.startPicking).toHaveBeenCalledWith('Click a card');
    fire('mousemove', 1620, 15, panelButton);
    expect(picker.hover()).toBeNull();
    fire('click', 1620, 15, panelButton);
    expect(await picked).toBeNull();

    picked = picker.pick();
    fire('click', 1550, 990);
    expect(await picked).toBeNull();

    picked = picker.pick();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'a', bubbles: true }));
    expect(picker.picking()).toBe(true);
    const escape = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true });
    document.body.dispatchEvent(escape);
    expect(escape.defaultPrevented).toBe(true);
    expect(await picked).toBeNull();

    picked = picker.pick();
    picker.cancel();
    expect(await picked).toBeNull();
    picker.cancel();

    picked = picker.pick();
    const second = picker.pick();
    expect(await picked).toBeNull();
    expect(picker.picking()).toBe(true);
    TestBed.resetTestingModule();
    expect(await second).toBeNull();
  });

  it('says which side of the skirmish being fought a card is on', () => {
    expect(picker.fightingSide('card:goblin')).toBe('shadow');
    expect(picker.fightingSide('card:merry')).toBe('freeps');
    expect(picker.fightingSide('card:frodo')).toBeNull();
    state.set(null);
    expect(picker.fightingSide('card:goblin')).toBeNull();
  });
});
