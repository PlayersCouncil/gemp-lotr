/**
 * How each server event changes the game a page holds: a pure function per event (the legacy page's
 * gameAnimations.js and gameUi.js, without the drawing). Events of only visual meaning (a card flashed, an event
 * shown, a card affecting another) change nothing here.
 */
import { CardRef, GameEvent, ownerOf } from '../protocol/game-events';
import { GameCard, GameModel, NO_CHOICES } from './game-model';

type Cards = Record<string, GameCard>;

function newCard(ref: CardRef): GameCard {
  return {
    id: ref.cardId,
    blueprintId: ref.blueprintId,
    zone: ref.zone ?? '',
    owner: ownerOf(ref),
    hindered: ref.hindered,
    index: ref.zone === 'ADVENTURE_PATH' ? ref.index : null,
    attachedTo: ref.targetCardId !== null && ref.targetType !== 'stacked' ? ref.targetCardId : null,
    stackedOn: ref.targetCardId !== null && ref.targetType === 'stacked' ? ref.targetCardId : null,
    tokens: {},
    inactive: false,
    assignedTo: null,
    inSkirmish: false,
    testingText: ref.testingText,
    backSideTestingText: ref.backSideTestingText,
  };
}

/** The cards with one changed (if it is there). */
function withCard(cards: Readonly<Cards>, id: string, change: (card: GameCard) => GameCard): Readonly<Cards> {
  const card = cards[id];
  return card ? { ...cards, [id]: change(card) } : cards;
}

function mapCards(cards: Readonly<Cards>, change: (card: GameCard) => GameCard): Readonly<Cards> {
  const result: Cards = {};
  for (const [id, card] of Object.entries(cards)) result[id] = change(card);
  return result;
}

/** A minion assigned to a character; assigning it again first takes it from where it was (legacy assignMinion). */
function assign(cards: Readonly<Cards>, minionId: string, characterId: string): Readonly<Cards> {
  return withCard(cards, minionId, (card) => ({ ...card, assignedTo: characterId }));
}

function tokensChanged(card: GameCard, token: string, by: number): GameCard {
  return { ...card, tokens: { ...card.tokens, [token]: (card.tokens[token] ?? 0) + by } };
}

/** The page's state after one more event. */
export function applyEvent(model: GameModel, event: GameEvent): GameModel {
  const next = { ...model, eventCount: model.eventCount + 1 };
  switch (event.type) {
    case 'P': {
      // A participant who isn't a player watches, from the second player's side
      const spectator = !event.allParticipantIds.includes(event.participantId);
      return {
        ...next,
        participantId: spectator ? (event.allParticipantIds[1] ?? null) : event.participantId,
        spectator,
        players: event.allParticipantIds,
        discardPublic: event.discardPublic,
        started: true,
      };
    }
    case 'PGS':
      return {
        ...next,
        preGame: { summary: event.summary, notes: event.notes, maps: event.maps, metaSites: event.metaSites },
      };
    case 'GPC':
      return { ...next, phase: event.phase };
    case 'TP':
      return { ...next, twilight: event.count };
    case 'TC':
      return {
        ...next,
        currentPlayer: event.participantId,
        cards: mapCards(model.cards, (card) => {
          const inactive = event.inactiveCardIds.includes(card.id);
          return card.inactive === inactive ? card : { ...card, inactive };
        }),
      };
    case 'PP':
      return { ...next, positions: { ...model.positions, [event.participantId]: event.index } };
    case 'PCIP':
      return {
        ...next,
        cards: { ...model.cards, [event.cardId]: newCard(event) },
        cardOrder: model.cards[event.cardId] ? model.cardOrder : [...model.cardOrder, event.cardId],
      };
    case 'MCIP': {
      const moved = newCard(event);
      return {
        ...next,
        cards: withCard(model.cards, event.cardId, (card) => ({
          ...card,
          zone: moved.zone,
          owner: moved.owner,
          attachedTo: moved.attachedTo,
          stackedOn: moved.stackedOn,
        })),
      };
    }
    case 'FCIP': {
      let cards = model.cards;
      for (const id of event.cardIds) cards = withCard(cards, id, (card) => ({ ...card, hindered: event.hindered }));
      return { ...next, cards };
    }
    case 'RCFP':
      return {
        ...next,
        cards: Object.fromEntries(Object.entries(model.cards).filter(([id]) => !event.cardIds.includes(id))),
        cardOrder: model.cardOrder.filter((id) => !event.cardIds.includes(id)),
      };
    case 'AA': {
      let cards = model.cards;
      let cardOrder = model.cardOrder;
      // A negative id is a skirmish split into several waiting ones: its minions go to a see-through copy of the
      // character (legacy createVirtualCard)
      if (Number(event.cardId) < 0) {
        const virtualId = `extra${event.cardId}`;
        const real = cards[String(-Number(event.cardId))];
        if (!cards[virtualId] && real) {
          cards = {
            ...cards,
            [virtualId]: { ...newVirtual(virtualId, real.blueprintId), zone: 'EXTRA' },
          };
          cardOrder = [...cardOrder, virtualId];
        }
      }
      for (const minionId of event.opposingCardIds) {
        if (cards[minionId]?.assignedTo !== event.cardId) cards = assign(cards, minionId, event.cardId);
      }
      return { ...next, cards, cardOrder };
    }
    case 'RA': {
      const negative = Number(event.cardId) < 0;
      return {
        ...next,
        cards: mapCards(model.cards, (card) =>
          card.assignedTo === event.cardId && (!negative || event.opposingCardIds.includes(card.id))
            ? { ...card, assignedTo: null }
            : card,
        ),
      };
    }
    case 'SS': {
      const ids = [...event.opposingCardIds, ...(event.cardId !== null ? [event.cardId] : [])];
      return {
        ...next,
        skirmish: { characterId: event.cardId, minionIds: event.opposingCardIds },
        cards: mapCards(model.cards, (card) => (ids.includes(card.id) ? { ...card, inSkirmish: true } : card)),
      };
    }
    case 'ATS':
      return { ...next, cards: withCard(model.cards, event.cardId, (card) => ({ ...card, inSkirmish: true })) };
    case 'RFS':
      return { ...next, cards: withCard(model.cards, event.cardId, (card) => ({ ...card, inSkirmish: false })) };
    case 'ES':
      return {
        ...next,
        skirmish: null,
        cards: mapCards(model.cards, (card) => (card.inSkirmish ? { ...card, inSkirmish: false } : card)),
      };
    case 'AT':
      return {
        ...next,
        cards: withCard(model.cards, event.cardId, (card) => tokensChanged(card, event.token, event.count)),
      };
    case 'RT':
      return {
        ...next,
        cards: withCard(model.cards, event.cardId, (card) => tokensChanged(card, event.token, -event.count)),
      };
    case 'M':
      return { ...next, messages: [...model.messages, { kind: 'message', text: event.message }] };
    case 'W':
      return { ...next, messages: [...model.messages, { kind: 'warning', text: event.message }] };
    case 'GS': {
      const zones = Object.entries(event.stats.playerZones);
      return {
        ...next,
        stats: event.stats,
        // Replays recorded before the server sent the flag have no player zones: the revealed hands stay as they were
        revealedHands:
          zones.length > 0 ? zones.filter(([, z]) => z.handRevealed).map(([name]) => name) : model.revealedHands,
      };
    }
    case 'EG':
      // A decision still open can't be answered any more
      return { ...next, ended: true, decision: null, local: NO_CHOICES };
    case 'D':
      return { ...next, decision: { decision: event.decision, arrivedAt: model.eventCount }, local: NO_CHOICES };
    case 'CAC':
    case 'EP':
    case 'CA':
      return next;
  }
}

function newVirtual(id: string, blueprintId: string): GameCard {
  return {
    id,
    blueprintId,
    zone: 'EXTRA',
    owner: null,
    hindered: false,
    index: null,
    attachedTo: null,
    stackedOn: null,
    tokens: {},
    inactive: false,
    assignedTo: null,
    inSkirmish: false,
    testingText: null,
    backSideTestingText: null,
  };
}

/** The page's state after several events, in order. */
export function applyEvents(model: GameModel, events: readonly GameEvent[]): GameModel {
  return events.reduce(applyEvent, model);
}
