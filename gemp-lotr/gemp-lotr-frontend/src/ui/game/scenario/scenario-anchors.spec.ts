import { AnchorContext, cardElement, rectAround, rectOf, resolveAnchor } from './scenario-anchors';

/**
 * A piece of the game page by its DOM contract, each element on the screen where its `data-box` says
 * ("left,top,width,height"); one without takes no room, as an element not laid out.
 */
const PAGE = `
  <div class="gd-game__path" data-box="0,0,50,150">
    <button data-card-id="30" data-box="0,0,50,70"></button>
    <button data-card-id="31" data-box="0,80,50,70"></button>
  </div>
  <div class="gd-game__row" data-row="hand" data-box="100,600,800,150">
    <button data-card-id="4" data-box="110,610,60,90"></button>
    <button data-card-id="extra9" data-box="180,610,60,90"></button>
  </div>
  <div class="gd-game__row" data-row="characters-player" data-box="100,400,800,150">
    <button data-card-id="7" data-box="110,410,100,140">
      <img class="gd-game-token" src="/images/tokens/wound.png" data-box="120,420,10,10" />
      <img class="gd-game-token" src="/images/tokens/wound.png" data-box="140,420,10,10" />
      <img class="gd-game-token" src="/images/tokens/burden.png" data-box="160,420,10,10" />
      <span class="gd-game-number" data-number="strength" data-box="190,520,15,15"></span>
    </button>
    <button data-card-id="8" data-box="220,410,100,140"></button>
  </div>
  <div class="gd-game__row" data-row="support-opponent" data-box="100,10,800,100"></div>
  <div class="gd-game__fight" data-fight="assignment" data-box="300,200,200,150">
    <div data-card-id="20" data-box="300,200,80,100"></div>
    <div data-card-id="7b" data-box="400,200,80,100"></div>
  </div>
  <div class="gd-game__fight" data-fight="skirmish" data-box="500,200,300,150">
    <div class="gd-game__side" data-side="free-peoples"><div data-card-id="8s" data-box="500,200,80,100"></div></div>
    <div class="gd-game__side" data-side="shadow"><div data-card-id="21" data-box="700,200,80,100"></div></div>
    <span class="gd-game__strength" data-side="free-peoples" data-box="590,300,20,20"></span>
    <span class="gd-game__strength" data-side="shadow" data-box="670,300,20,20"></span>
    <span class="gd-game__damage" data-side="free-peoples" data-none data-box="590,330,20,20"></span>
    <span class="gd-game__damage" data-side="shadow" data-box="670,330,20,20"></span>
  </div>
  <div data-game="twilight" data-box="950,10,40,40"></div>
  <div data-game="phase" data-box="950,60,40,20"></div>
  <div data-game="fp-archery" data-box="900,700,20,20"></div>
  <div data-game="shadow-archery" data-box="940,700,20,20"></div>
  <div data-game="move" data-box="980,700,20,20"></div>
  <gd-game-stats data-box="900,690,100,40"></gd-game-stats>
  <span data-stat="deck0" data-box="10,700,30,20"></span>
  <span data-stat="discard1" data-box="10,20,30,20"></span>
  <span data-stat="hand1" data-box="50,20,30,20"></span>
  <gd-game-alert data-box="900,500,100,80">
    <button data-decision-button="Pass" data-box="900,560,40,20"></button>
    <button data-decision-button="Reset choice" data-box="950,560,40,20"></button>
  </gd-game-alert>
  <dialog class="gd-dialog" open data-box="200,100,300,300">
    <button data-card-id="4" data-box="210,110,60,90"></button>
  </dialog>
  <dialog class="gd-dialog" open data-box="250,150,300,300"></dialog>
  <dialog class="gd-dialog" data-box="0,0,10,10"></dialog>
`;

describe('scenario anchors', () => {
  let root: HTMLElement;

  const context = (extra: Partial<AnchorContext> = {}): AnchorContext => ({
    root,
    cards: { frodo: '7', sam: '8', hidden: '99', gimli: '4', orc: '20', aragorn: '9' },
    parts: { 'frodo:name': [10, 5, 80, 10], 'sam:strength': [5, 60, 12, 10] },
    sites: { 1: '30', 2: '31', 3: '77' },
    self: 0,
    opponent: 1,
    humanSeat: 'freeps',
    ...extra,
  });
  const at = (anchor: string, onCards = false, extra: Partial<AnchorContext> = {}) =>
    resolveAnchor(context(extra), anchor, onCards);
  const box = (left: number, top: number, width: number, height: number) => ({ left, top, width, height });

  beforeEach(() => {
    root = document.createElement('div');
    root.innerHTML = PAGE;
    document.body.appendChild(root);
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
      const [left = 0, top = 0, width = 0, height = 0] = (this.getAttribute('data-box') ?? '').split(',').map(Number);
      return { left, top, width, height, right: left + width, bottom: top + height, x: left, y: top } as DOMRect;
    });
  });

  afterEach(() => {
    root.remove();
    vi.restoreAllMocks();
  });

  it('measures an element on the screen, and none that takes no room', () => {
    expect(rectOf(root.querySelector('[data-card-id="7"]'))).toEqual(box(110, 410, 100, 140));
    expect(rectOf(root.querySelector('dialog:not([open])'))).toEqual(box(0, 0, 10, 10));
    expect(rectOf(root.querySelector('.gd-game__side'))).toBeNull();
    expect(rectOf(null)).toBeNull();
    expect(rectOf(undefined)).toBeNull();
    expect(rectAround(root.querySelectorAll('[data-game="twilight"], [data-game="phase"], .gd-game__side'))).toEqual(
      box(950, 10, 40, 70),
    );
    expect(rectAround([])).toBeNull();
  });

  it('finds a card by its id: the one in a dialog in front, or its stand-in off the table', () => {
    expect(cardElement(root, '4')?.closest('dialog')).not.toBeNull();
    expect(cardElement(root, '9')?.getAttribute('data-card-id')).toBe('extra9');
    expect(cardElement(root, '99')).toBeNull();
    expect(at('card:gimli')).toEqual(box(210, 110, 60, 90));
    expect(at('card:aragorn')).toEqual(box(180, 610, 60, 90));
    expect(at('card:nobody')).toBeNull();
    expect(at('card:hidden')).toBeNull();
  });

  it('finds what the game draws on a card (its wounds, its numbers) and what is printed on it', () => {
    expect(at('card:frodo:wounds')).toEqual(box(120, 420, 30, 10));
    expect(at('card:frodo:Strength')).toEqual(box(190, 520, 15, 15));
    // No wounds, or no number drawn: the card itself, or where the server says that part is printed
    expect(at('card:sam:wounds')).toEqual(box(220, 410, 100, 140));
    expect(at('card:sam:strength')).toEqual(box(225, 494, 12, 14));
    expect(at('card:frodo:name')).toEqual(box(120, 417, 80, 14));
    expect(at('card:frodo:vitality')).toEqual(box(110, 410, 100, 140));
    expect(at('card:frodo:culture')).toEqual(box(110, 410, 100, 140));
    expect(at('card:hidden:name')).toBeNull();
    expect(at('card:nobody:name')).toBeNull();
  });

  it('finds a row of the table: where its cards are for the bubble, the whole row for an outline', () => {
    expect(at('zone:self.hand', true)).toEqual(box(110, 610, 130, 90));
    expect(at('zone:self.hand')).toEqual(box(100, 600, 800, 150));
    expect(at('zone:SELF.CHARACTERS', true)).toEqual(box(110, 410, 210, 140));
    // An empty row is the whole row either way
    expect(at('zone:opp.support', true)).toEqual(box(100, 10, 800, 100));
    expect(at('zone:minions')).toBeNull();
    expect(at('zone:graveyard')).toBeNull();
  });

  it("finds a pile's counter, the page's player's or the opponent's", () => {
    expect(at('pile:self.deck')).toEqual(box(10, 700, 30, 20));
    expect(at('pile:opp.discard')).toEqual(box(10, 20, 30, 20));
    expect(at('pile:opp.hand')).toEqual(box(50, 20, 30, 20));
    expect(at('pile:opp.deadPile')).toBeNull();
    expect(at('pile:self.library')).toBeNull();
    expect(at('pile:third.deck')).toBeNull();
    expect(at('pile:self.deck', false, { self: null })).toBeNull();
  });

  it('finds the pieces of the page', () => {
    expect(at('ui:twilightPool')).toEqual(box(950, 10, 40, 40));
    expect(at('ui:phase')).toEqual(box(950, 60, 40, 20));
    expect(at('ui:alertBox')).toEqual(box(900, 500, 100, 80));
    expect(at('ui:advPath')).toEqual(box(0, 0, 50, 150));
    expect(at('ui:stats')).toEqual(box(900, 690, 100, 40));
    expect(at('ui:stats:archery')).toEqual(box(900, 700, 60, 20));
    expect(at('ui:stats:move')).toEqual(box(980, 700, 20, 20));
    expect(at('ui:stats:other')).toEqual(box(900, 690, 100, 40));
    // The dialog in front: the one opened last
    expect(at('ui:dialog')).toEqual(box(250, 150, 300, 300));
    // Not on the page, or not a piece the format knows
    expect(at('ui:cardInfo')).toBeNull();
    expect(at('ui:clock')).toBeNull();
    expect(at('ui:nothing')).toBeNull();
  });

  it('finds a site of the adventure path by its number', () => {
    expect(at('ui:site:2')).toEqual(box(0, 80, 50, 70));
    expect(at('ui:site:3')).toBeNull();
    expect(at('ui:site:9')).toBeNull();
  });

  it("finds the decision's buttons by what they do", () => {
    expect(at('ui:button:Pass')).toEqual(box(900, 560, 40, 20));
    expect(at('ui:button:reset')).toEqual(box(950, 560, 40, 20));
    expect(at('ui:button:done')).toBeNull();
    expect(at('ui:button:jump')).toBeNull();
    expect(at('ui:button')).toBeNull();
  });

  it('finds the assignment a card is in', () => {
    expect(at('ui:assignment:orc')).toEqual(box(300, 200, 200, 150));
    expect(at('ui:assignment:sam')).toBeNull();
    expect(at('ui:assignment')).toBeNull();
  });

  it("finds the skirmish and its parts, the player's own side by the seat they play", () => {
    expect(at('ui:skirmish')).toEqual(box(500, 200, 300, 150));
    expect(at('ui:skirmish:freeps')).toEqual(box(500, 200, 80, 100));
    expect(at('ui:skirmish:shadow')).toEqual(box(700, 200, 80, 100));
    expect(at('ui:skirmish:fpStrength')).toEqual(box(590, 300, 20, 20));
    expect(at('ui:skirmish:shadowStrength')).toEqual(box(670, 300, 20, 20));
    expect(at('ui:skirmish:selfStrength')).toEqual(box(590, 300, 20, 20));
    expect(at('ui:skirmish:oppStrength')).toEqual(box(670, 300, 20, 20));
    expect(at('ui:skirmish:selfStrength', false, { humanSeat: 'shadow' })).toEqual(box(670, 300, 20, 20));
    expect(at('ui:skirmish:oppStrength', false, { humanSeat: 'shadow' })).toEqual(box(590, 300, 20, 20));
    // A damage bonus of none isn't there to point at
    expect(at('ui:skirmish:selfDamage')).toBeNull();
    expect(at('ui:skirmish:oppDamage')).toEqual(box(670, 330, 20, 20));
    expect(at('ui:skirmish:selfDamage', false, { humanSeat: 'shadow' })).toEqual(box(670, 330, 20, 20));
    expect(at('ui:skirmish:oppDamage', false, { humanSeat: 'shadow' })).toBeNull();
    expect(at('ui:skirmish:winner')).toBeNull();
    root.querySelector('[data-fight="skirmish"]')?.remove();
    expect(at('ui:skirmish')).toBeNull();
  });

  it('finds nothing for a kind of anchor it does not know', () => {
    expect(at('player:self')).toBeNull();
    expect(at('')).toBeNull();
  });
});
