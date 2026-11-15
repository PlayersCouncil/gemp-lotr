/**
 * Test-only: games made up from a seed (G5 of trymarz/gemp-lotr-private#27), in the shape of a recorded game
 * (recording.ts), so both pages can be played through them in jsdom as through the recorded ones. The server's side is
 * generated: its first state and the answers to the page's polls carry random events that are consistent with each
 * other (a card is removed, moved, assigned or attached only while it is in play, a site has its place on the path),
 * and decisions of every kind for the player, each followed by an answer the player can give with the page's gestures.
 *
 * The steps carry no model: the legacy page's, taken while it plays the game, is what the Angular page is compared
 * with (src/parity/game/generated-games.parity.spec.ts).
 */
import type { LegacyGameModel } from '../../../visual-check/game/controls.mjs';
import { Choices, choices } from '../parity/choices';
import { GameRecording, RecordedExchange, RecordedStep } from './recording';

const PLAYER = 'GamePlayer1';
const FOE = 'GameFoe1';
const PLAYERS = [PLAYER, FOE];

/** Blueprints of the recorded games, by what they can be. */
const SITES = ['1_319', '1_327', '1_337', '1_343', '1_349', '1_350', '1_353', '1_356', '1_360'];
const COMPANIONS = ['1_7', '1_12', '1_50', '1_290', '1_311', '4_311'];
const MINIONS = ['1_154', '1_178', '1_184', '1_185', '1_191', '1_261', '1_181'];
const SUPPORTS = ['1_20', '3_44', '101_49', '1_183'];
const ATTACHMENTS = ['1_2', '1_9', '1_180'];
const HAND = ['1_3', '1_41', '1_116', '1_176', '1_181', '1_191', '1_20', '103_107', '1_261'];
const PILES = ['DISCARD', 'DEAD', 'REMOVED', 'DECK', 'ADVENTURE_DECK'];
const PHASES = [
  'Play starting fellowship',
  'Fellowship',
  'Shadow',
  'Maneuver',
  'Archery',
  'Assignment',
  'Skirmish',
  'Regroup',
  'Between turns',
];
const TEXTS = [
  'Choose a number of burdens to bid',
  'Play Fellowship action or Pass',
  "Choose card to <div class='cardHint' value='1_9'>Dwarven Axe</div> on",
  'Assign minions to companions or allies at sites',
  'Choose an action',
  'Do you wish to mulligan? (Shuffle cards back and draw 6)',
  'Fellowship & Shadow < 3',
];
const MESSAGES = [
  'GamePlayer1 bid 0',
  "GamePlayer1 plays <div class='cardHint' value='1_12'>·Gimli, Dwarf of Erebor</div> from hand",
  'GameFoe1 has chosen to go second',
  'Skirmish between <b>two</b> & more',
];

interface Card {
  id: string;
  blueprintId: string;
  zone: string;
  owner: string;
  target?: string;
}

const IN_PLAY = ['FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT', 'ATTACHED', 'STACKED'];

/** Escapes a value for an XML attribute. */
const attr = (value: string) =>
  value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

function element(type: string, attributes: Record<string, string>, children = ''): string {
  const list = Object.entries({ ...attributes, timestamp: '2026-11-15 12:00:00.0000', type })
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([name, value]) => ` ${name}="${attr(value)}"`)
    .join('');
  return children ? `<ge${list}>${children}</ge>` : `<ge${list}/>`;
}

const parameters = (values: Record<string, string[]>) =>
  Object.entries(values)
    .flatMap(([name, list]) => list.map((value) => `<parameter name="${name}" value="${attr(value)}"/>`))
    .join('');

const CLOCKS =
  `<clocks><clock participantId="${PLAYER}">2700</clock><clock participantId="${FOE}">2700</clock>` +
  '<clock participantId="decisionClock">0</clock><clock participantId="decisionLimit">360</clock></clocks>';

function xml(root: 'gameState' | 'update', events: string[]): string {
  return `<?xml version="1.0" encoding="UTF-8" standalone="no"?><${root} cn="2">${events.join('')}${CLOCKS}</${root}>`;
}

/** A decision for the player, the answer they give, and what the server sends first after that answer. */
interface Decision {
  event: string;
  value: string;
  id: string;
  confirm?: () => string[];
}

/** The game the generator keeps, so each event it makes fits what came before. */
class Game {
  readonly cards = new Map<string, Card>();
  /** Minion → the character it is assigned to. */
  readonly assigned = new Map<string, string>();
  skirmish = false;
  /** The cards flipped in the answer being made. */
  readonly flipped = new Set<string>();
  nextId = 100;
  decisionId = 0;
  sites = 0;

  constructor(readonly pick: Choices) {}

  list(test: (card: Card) => boolean): Card[] {
    return [...this.cards.values()].filter(test);
  }

  inZone(...zones: string[]): Card[] {
    return this.list((card) => zones.includes(card.zone));
  }

  /** Cards nothing is attached to or stacked on. */
  leaves(test: (card: Card) => boolean): Card[] {
    const targets = new Set(this.list((card) => card.target !== undefined).map((card) => card.target));
    return this.list((card) => test(card) && !targets.has(card.id));
  }

  /** A card put into play somewhere it can be, or null. */
  putInPlay(): string | null {
    const id = String(this.nextId++);
    const owner = this.pick.of(PLAYERS);
    const own = owner === PLAYER;
    const where = this.pick.of(['character', 'minion', 'support', 'attached', 'stacked', 'hand', 'pile', 'site']);
    let card: Card;
    switch (where) {
      case 'character':
        card = { id, blueprintId: this.pick.of(COMPANIONS), zone: 'FREE_CHARACTERS', owner };
        break;
      case 'minion':
        card = { id, blueprintId: this.pick.of(MINIONS), zone: 'SHADOW_CHARACTERS', owner };
        break;
      case 'support':
        card = { id, blueprintId: this.pick.of(SUPPORTS), zone: 'SUPPORT', owner };
        break;
      case 'attached':
      case 'stacked': {
        // Stacked only on the player's own cards: the legacy page left an opponent's stacked card where its flying in
        // ended, off its place, until it laid the row out again (trymarz/gemp-lotr-private#27, decisions to review)
        const targets = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT').filter(
          (target) => where === 'attached' || target.owner === PLAYER,
        );
        if (targets.length === 0) return null;
        const target = this.pick.of(targets);
        const zone = where === 'attached' ? 'ATTACHED' : 'STACKED';
        card = { id, blueprintId: this.pick.of(ATTACHMENTS), zone, owner: target.owner, target: target.id };
        break;
      }
      case 'hand':
        if (!own) return null;
        card = { id, blueprintId: this.pick.of(HAND), zone: 'HAND', owner };
        break;
      case 'pile':
        if (!own) return null;
        card = { id, blueprintId: this.pick.of(HAND), zone: this.pick.of(PILES), owner };
        break;
      default:
        return this.site(owner);
    }
    this.cards.set(id, card);
    return this.placed('PCIP', card);
  }

  /** The next site of the path. */
  site(owner = this.pick.of(PLAYERS)): string | null {
    if (this.sites >= 9) return null;
    const index = ++this.sites;
    const card = { id: String(this.nextId++), blueprintId: SITES[index - 1] ?? '1_319', zone: 'ADVENTURE_PATH', owner };
    this.cards.set(card.id, card);
    return this.placed('PCIP', card, { index: String(index) });
  }

  placed(type: string, card: Card, extra: Record<string, string> = {}): string {
    return element(type, {
      blueprintId: card.blueprintId,
      cardId: card.id,
      hindered: 'false',
      participantId: card.owner,
      zone: card.zone,
      ...(card.target
        ? { targetCardId: card.target, targetType: card.zone === 'ATTACHED' ? 'attached' : 'stacked' }
        : {}),
      ...extra,
    });
  }

  remove(): string | null {
    const removable = this.leaves((card) => card.zone !== 'ADVENTURE_PATH' && !this.isAssigned(card.id));
    if (removable.length === 0) return null;
    const removed = [this.pick.of(removable)];
    if (removable.length > 1 && this.pick.chance(0.3)) {
      const other = this.pick.of(removable);
      if (!removed.includes(other)) removed.push(other);
    }
    for (const card of removed) this.cards.delete(card.id);
    const ids = removed.map((card) => card.id).join(',');
    return element('RCFP', { otherCardIds: ids, ...(this.pick.chance(0.5) ? { participantId: PLAYER } : {}) });
  }

  isAssigned(id: string): boolean {
    return this.assigned.has(id) || [...this.assigned.values()].includes(id);
  }

  move(): string | null {
    const movable = this.leaves((card) => IN_PLAY.includes(card.zone) && !this.isAssigned(card.id));
    if (movable.length === 0) return null;
    const card = this.pick.of(movable);
    if (card.target) {
      // To another bearer
      const targets = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT').filter((t) => t.id !== card.id);
      if (targets.length === 0) return null;
      const target = this.pick.of(targets);
      Object.assign(card, { target: target.id, owner: target.owner });
    } else {
      const zone = this.pick.of(['FREE_CHARACTERS', 'SUPPORT', 'SHADOW_CHARACTERS'].filter((z) => z !== card.zone));
      card.zone = zone;
    }
    return this.placed('MCIP', card);
  }

  assign(): string | null {
    const minions = this.inZone('SHADOW_CHARACTERS').filter((card) => !this.assigned.has(card.id));
    const characters = this.inZone('FREE_CHARACTERS');
    if (minions.length === 0 || characters.length === 0 || this.skirmish) return null;
    const character = this.pick.of(characters);
    const minion = this.pick.of(minions);
    this.assigned.set(minion.id, character.id);
    return element('AA', { cardId: character.id, otherCardIds: minion.id });
  }

  unassign(): string | null {
    const characters = [...new Set(this.assigned.values())];
    if (characters.length === 0 || this.skirmish) return null;
    const character = this.pick.of(characters);
    for (const [minion, to] of [...this.assigned]) if (to === character) this.assigned.delete(minion);
    return element('RA', { cardId: character });
  }

  startSkirmish(): string | null {
    const characters = [...new Set(this.assigned.values())];
    if (characters.length === 0 || this.skirmish) return null;
    const character = this.pick.of(characters);
    const minions = [...this.assigned].filter(([, to]) => to === character).map(([minion]) => minion);
    this.skirmish = true;
    return element('SS', { cardId: character, otherCardIds: minions.join(',') });
  }

  endSkirmish(): string | null {
    if (!this.skirmish) return null;
    this.skirmish = false;
    // The server takes the assignments back as the skirmish ends
    const events = [...new Set(this.assigned.values())].map((character) => element('RA', { cardId: character }));
    this.assigned.clear();
    return element('ES', {}) + events.join('');
  }

  tokens(): string | null {
    // Not on a card flipped in this answer: the legacy page laid the tokens out at the width its flip was animating
    // and left them there (trymarz/gemp-lotr-private#27, decisions to review)
    const cards = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT').filter(
      (card) => !this.flipped.has(card.id),
    );
    if (cards.length === 0) return null;
    const card = this.pick.of(cards);
    return element(this.pick.of(['AT', 'AT', 'RT']), {
      blueprintId: card.blueprintId,
      cardId: card.id,
      count: String(1 + this.pick.below(3)),
      hindered: 'false',
      participantId: card.owner,
      token: this.pick.of(['WOUND', 'BURDEN']),
      zone: card.zone,
    });
  }

  stats(): string {
    const characters = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS');
    const charStats = characters
      .filter(() => this.pick.chance(0.5))
      .map((card) => `${card.id}=${this.pick.below(9)}|${this.pick.below(5)}|RF${this.pick.below(12)}`)
      .join(',');
    const zones = PLAYERS.map(
      (name) =>
        `<playerZones ADVENTURE_DECK="${this.pick.below(10)}" DEAD="${this.pick.below(4)}" DECK="${this.pick.below(61)}" ` +
        `DISCARD="${this.pick.below(8)}" HAND="${this.pick.below(9)}" REMOVED="${this.pick.below(3)}" name="${name}"/>`,
    ).join('');
    const threats = PLAYERS.map(
      (name, i) =>
        `<threats name="${name}" value="${this.pick.below(4)}"/>` +
        `<threatTotals name="${name}" value="${i === 0 ? this.pick.below(4) : -1}"/>`,
    ).join('');
    return element(
      'GS',
      {
        ...(charStats ? { charStats } : {}),
        fellowshipArchery: this.pick.of(['null', '0', '2']),
        fellowshipDamageBonus: '0',
        fellowshipStrength: String(this.pick.below(12)),
        fpOverwhelmed: 'false',
        hindered: '',
        initiative: this.pick.of(['SHADOW', 'FREE_PEOPLE']),
        moveCount: String(this.pick.below(3)),
        moveLimit: '2',
        ...(this.pick.chance(0.3) ? { ruleof4: String(this.pick.below(5)) } : {}),
        shadowArchery: 'null',
        shadowDamageBonus: '0',
        shadowStrength: String(this.pick.below(12)),
      },
      zones + threats,
    );
  }

  /** One event the server could send now, or null when the one picked doesn't fit. */
  event(): string | null {
    const pick = this.pick;
    const kinds: (() => string | null)[] = [
      () => this.putInPlay(),
      () => this.putInPlay(),
      () => this.remove(),
      () => this.move(),
      () => this.assign(),
      () => this.unassign(),
      () => this.startSkirmish(),
      () => this.endSkirmish(),
      () => this.tokens(),
      () => this.stats(),
      () => element('TP', { count: String(pick.below(10)) }),
      () => element('GPC', { phase: pick.of(PHASES) }),
      () => element('M', { message: pick.of(MESSAGES) }),
      () => element('W', { message: "You can't cancel this game" }),
      () => element('PP', { index: String(1 + pick.below(this.sites || 1)), participantId: pick.of(PLAYERS) }),
      () => {
        const inactive = this.list(() => pick.chance(0.3)).map((card) => card.id);
        return element('TC', { otherCardIds: inactive.join(','), participantId: pick.of(PLAYERS) });
      },
      () => {
        const cards = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT');
        if (cards.length === 0) return null;
        return element('CA', { cardId: pick.of(cards).id });
      },
      () => {
        const cards = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS');
        if (cards.length === 0) return null;
        const blueprintId = pick.of(HAND);
        const played = { blueprintId, hindered: 'false', participantId: pick.of(PLAYERS), zone: 'VOID' };
        const id = String(this.nextId++);
        return pick.chance(0.5)
          ? element('EP', { ...played, cardId: id, zone: 'VOID_FROM_HAND' })
          : element('CAC', { ...played, cardId: id, otherCardIds: pick.of(cards).id });
      },
      () => {
        // A card flipped once per answer: the legacy page left a card flipped twice at once squeezed to a sliver (its
        // second flip took the width the first was still animating; trymarz/gemp-lotr-private#27, decisions to review)
        const cards = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT').filter(
          (card) => !this.flipped.has(card.id),
        );
        if (cards.length === 0) return null;
        const card = pick.of(cards);
        this.flipped.add(card.id);
        return element('FCIP', { hindered: String(pick.chance(0.5)), otherCardIds: card.id });
      },
    ];
    return pick.of(kinds)();
  }

  events(count: number): string[] {
    this.flipped.clear();
    const events: string[] = [];
    for (let tries = 0; events.length < count && tries < count * 5; tries++) {
      const event = this.event();
      if (event) events.push(event);
    }
    return events;
  }

  /** A decision for the player and the answer they give, or null when the one picked doesn't fit. */
  decision(): Decision | null {
    const found = this.decisionOf();
    return found && { ...found, id: String(this.decisionId) };
  }

  decisionOf(): Omit<Decision, 'id'> | null {
    const pick = this.pick;
    const id = String(++this.decisionId);
    const text = pick.of(TEXTS);
    const make = (type: string, values: Record<string, string[]>) =>
      element('D', { decisionType: type, id, participantId: PLAYER, text }, parameters(values));
    const some = <T>(items: T[], min: number, max: number): T[] => {
      const left = [...items];
      const count = min + pick.below(Math.min(max, items.length) - min + 1);
      const chosen: T[] = [];
      while (chosen.length < count && left.length > 0) chosen.push(left.splice(pick.below(left.length), 1)[0]!);
      return chosen;
    };
    switch (
      pick.of([
        'INTEGER',
        'MULTIPLE_CHOICE',
        'CARD_ACTION_CHOICE',
        'CARD_SELECTION',
        'ARBITRARY_CARDS',
        'ACTION_CHOICE',
        'ASSIGN_MINIONS',
      ])
    ) {
      case 'INTEGER': {
        const min = pick.below(2);
        return { event: make('INTEGER', { min: [String(min)] }), value: String(min + pick.below(4)) };
      }
      case 'MULTIPLE_CHOICE': {
        const results = ['No', 'Yes', 'Maybe & later', 'Go <first>', 'Second'].slice(0, 2 + pick.below(4));
        return { event: make('MULTIPLE_CHOICE', { results }), value: String(pick.below(results.length)) };
      }
      case 'CARD_ACTION_CHOICE': {
        const inPlay = this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT', 'HAND', 'ADVENTURE_PATH');
        const offered = some(inPlay, 0, 4);
        const values: Record<string, string[]> = { actionId: [], actionText: [], blueprintId: [], cardId: [] };
        offered.forEach((card, i) => {
          values['actionId']!.push(String(i));
          values['actionText']!.push(`Use ${card.blueprintId}`);
          values['blueprintId']!.push('inPlay');
          values['cardId']!.push(card.id);
        });
        const value = offered.length > 0 && pick.chance(0.7) ? String(pick.below(offered.length)) : '';
        return { event: make('CARD_ACTION_CHOICE', offered.length ? values : {}), value };
      }
      case 'CARD_SELECTION': {
        const cards = some(this.inZone('FREE_CHARACTERS', 'SHADOW_CHARACTERS', 'SUPPORT'), 1, 4);
        if (cards.length === 0) return null;
        const min = 1;
        const max = 1;
        const chosen = some(cards, min, max);
        return {
          event: make('CARD_SELECTION', { cardId: cards.map((c) => c.id), max: [String(max)], min: [String(min)] }),
          value: chosen.map((c) => c.id).join(','),
        };
      }
      case 'ARBITRARY_CARDS': {
        const count = 1 + pick.below(6);
        const blueprints = Array.from({ length: count }, () => pick.of([...COMPANIONS, ...HAND]));
        const selectable = blueprints.map(() => pick.chance(0.8));
        const temps = blueprints.map((_, i) => `temp${i}`);
        const max = 1;
        const chosen = some(
          temps.filter((_, i) => selectable[i]),
          0,
          max,
        );
        return {
          event: make('ARBITRARY_CARDS', {
            blueprintId: blueprints,
            cardId: temps,
            max: [String(max)],
            min: ['0'],
            selectable: selectable.map(String),
          }),
          value: chosen.join(','),
        };
      }
      case 'ACTION_CHOICE': {
        const count = 1 + pick.below(3);
        const blueprints = Array.from({ length: count }, (_, i) => (i === 0 ? 'rules' : pick.of(ATTACHMENTS)));
        return {
          event: make('ACTION_CHOICE', {
            actionId: blueprints.map((_, i) => String(i)),
            actionText: blueprints.map((b) =>
              b === 'rules'
                ? 'Resolve skirmish damage'
                : `Required trigger from <div class='cardHint' value='${b}'>a card</div>`,
            ),
            blueprintId: blueprints,
          }),
          value: String(pick.below(count)),
        };
      }
      default: {
        const characters = this.inZone('FREE_CHARACTERS');
        const minions = this.inZone('SHADOW_CHARACTERS');
        if (characters.length === 0 || minions.length === 0 || this.skirmish) return null;
        const groups = new Map<string, string[]>(characters.map((c) => [c.id, []]));
        // A minion the server assigned already stays where it is: the page answers with it there
        for (const minion of minions) {
          const to = this.assigned.get(minion.id) ?? (pick.chance(0.6) ? pick.of(characters).id : null);
          if (to) groups.get(to)!.push(minion.id);
        }
        // The server then assigns them as the player did (as the real one does, before anything else)
        const confirm = () =>
          [...groups]
            .filter(([, assigned]) => assigned.length > 0)
            .map(([character, assigned]) => {
              for (const minion of assigned) this.assigned.set(minion, character);
              return element('AA', { cardId: character, otherCardIds: assigned.join(',') });
            });
        return {
          confirm,
          event: make('ASSIGN_MINIONS', {
            freeCharacters: characters.map((c) => c.id),
            minions: minions.map((c) => c.id),
          }),
          value: [...groups].map(([character, assigned]) => [character, ...assigned].join(' ')).join(','),
        };
      }
    }
  }
}

const exchange = (params: Record<string, string>, body: string): RecordedExchange => ({
  method: Object.keys(params).length > 0 || body.includes('<update') ? 'POST' : 'GET',
  path: '/game/<id>',
  params,
  status: 200,
  contentType: 'application/xml; charset=UTF-8',
  body,
});

/** A made-up game of `rounds` answers from the server, the same for the same seed. */
export function generatedGame(seed: number, rounds = 12): GameRecording {
  const pick = choices(seed);
  const game = new Game(pick);
  const exchanges: RecordedExchange[] = [
    {
      method: 'GET',
      path: '/player',
      params: {},
      status: 200,
      contentType: 'application/json; charset=UTF-8',
      body: JSON.stringify({ hasTrophies: false, name: PLAYER, type: 'aut' }),
    },
  ];
  const steps: RecordedStep[] = [];
  const step = () =>
    steps.push({
      name: `exchange ${exchanges.length}`,
      exchanges: exchanges.length,
      gestures: [],
      model: null as unknown as LegacyGameModel,
    });

  // The first state: the game's summary, the players, the path so far and some cards
  const first = [
    element('M', { message: 'You are starting a game of Fellowship Block' }),
    element('PGS', {
      allParticipantIds: PLAYERS.join(','),
      maps: '',
      notes: 'Deck used: <b>Generated</b>',
      participantId: PLAYER,
      summary: 'A generated game',
    }),
    element('P', {
      allParticipantIds: PLAYERS.join(','),
      discardPublic: String(pick.chance(0.5)),
      participantId: PLAYER,
    }),
    game.stats(),
  ];
  for (let i = 1 + pick.below(3); i > 0; i--) first.push(game.site() ?? '');
  first.push(...game.events(4 + pick.below(8)));
  let pending = pick.chance(0.5) ? game.decision() : null;
  if (pending) first.push(pending.event);
  exchanges.push({ ...exchange({}, xml('gameState', first)), method: 'GET' });
  step();

  for (let round = 0; round < rounds; round++) {
    const events = [...(pending?.confirm?.() ?? []), ...game.events(pick.below(6))];
    let next = null;
    for (let tries = 0; tries < 5 && !next && pick.chance(0.6); tries++) next = game.decision();
    if (next) events.push(next.event);
    if (round === rounds - 1 && !next && pick.chance(0.2)) events.push(element('EG', {}));
    const params: Record<string, string> = pending ? { decisionId: pending.id, decisionValue: pending.value } : {};
    exchanges.push(exchange(params, xml('update', events)));
    step();
    pending = next;
  }
  return {
    scenario: `generated ${seed}`,
    note: 'A generated game',
    participant: PLAYER,
    opponent: FOE,
    size: '1366x768',
    exchanges,
    steps,
  };
}
