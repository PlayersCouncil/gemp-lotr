/**
 * How each server event changes the game a page holds: a pure function per event (the legacy page's
 * gameAnimations.js and gameUi.js, without the drawing). Events of only visual meaning (a card flashed, an event
 * shown, a card affecting another) change nothing here.
 */
import { CardRef, GameEvent, GameStats, ownerOf } from '../protocol/game-events';
import { ALERT_DECISIONS, GameCard, GameModel, NO_CHOICES, NO_SKIRMISH_NUMBERS, SkirmishNumbers } from './game-model';

type Cards = Record<string, GameCard>;

function newCard(ref: CardRef, at: number): GameCard {
  return {
    id: ref.cardId,
    blueprintId: ref.blueprintId,
    zone: ref.zone ?? '',
    owner: ownerOf(ref),
    hindered: ref.hindered,
    numbersHidden: false,
    index: ref.zone === 'ADVENTURE_PATH' ? ref.index : null,
    attachedTo: ref.targetCardId !== null && ref.targetType !== 'stacked' ? ref.targetCardId : null,
    stackedOn: ref.targetCardId !== null && ref.targetType === 'stacked' ? ref.targetCardId : null,
    attachedAt: at,
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

/** The skirmish's boxes after a GS: the strengths as sent, what the stats leave out as it was. */
function skirmishNumbers(shown: SkirmishNumbers, stats: GameStats): SkirmishNumbers {
  return {
    fellowshipStrength: stats.fellowshipStrength,
    shadowStrength: stats.shadowStrength,
    fellowshipDamageBonus: stats.fellowshipDamageBonus ?? shown.fellowshipDamageBonus,
    shadowDamageBonus: stats.shadowDamageBonus ?? shown.shadowDamageBonus,
    fpOverwhelmed: stats.fpOverwhelmed ?? shown.fpOverwhelmed,
    shadowOverwhelmed: stats.shadowOverwhelmed ?? shown.shadowOverwhelmed,
  };
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
        preGame: {
          participantId: event.participantId,
          players: event.allParticipantIds,
          summary: event.summary,
          notes: event.notes,
          maps: event.maps,
          metaSites: event.metaSites,
        },
      };
    case 'GPC':
      return { ...next, phase: event.phase };
    case 'TP':
      return { ...next, twilight: event.count };
    case 'TC':
      // No inactive cards named: the cards stay as they were (the legacy page changed them only for a list)
      if (event.inactiveCardIds.length === 0) return { ...next, currentPlayer: event.participantId };
      return {
        ...next,
        currentPlayer: event.participantId,
        inactiveCardIds: event.inactiveCardIds,
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
        cards: {
          ...model.cards,
          [event.cardId]: {
            ...newCard(event, model.eventCount),
            inactive: model.inactiveCardIds.includes(event.cardId),
          },
        },
        cardOrder: model.cards[event.cardId] ? model.cardOrder : [...model.cardOrder, event.cardId],
      };
    case 'MCIP': {
      const moved = newCard(event, model.eventCount);
      return {
        ...next,
        cards: withCard(model.cards, event.cardId, (card) => ({
          ...card,
          zone: moved.zone,
          owner: moved.owner,
          attachedTo: moved.attachedTo,
          stackedOn: moved.stackedOn,
          attachedAt: moved.attachedAt,
        })),
      };
    }
    case 'FCIP': {
      let cards = model.cards;
      for (const id of event.cardIds) {
        cards = withCard(cards, id, (card) => ({
          ...card,
          hindered: event.hindered,
          numbersHidden: card.numbersHidden || event.hindered,
        }));
      }
      return { ...next, cards };
    }
    case 'RCFP':
      return {
        ...next,
        stats: model.stats && {
          ...model.stats,
          charStats: Object.fromEntries(
            Object.entries(model.stats.charStats).filter(([id]) => !event.cardIds.includes(id)),
          ),
        },
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
        skirmish: { characterId: event.cardId, minionIds: event.opposingCardIds, shown: NO_SKIRMISH_NUMBERS },
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
        // A character the server leaves out keeps the numbers it had (the legacy page left them on the card)
        stats: { ...event.stats, charStats: { ...model.stats?.charStats, ...event.stats.charStats } },
        // A card the stats name shows its numbers again, unless it lies face down
        cards: mapCards(model.cards, (card) =>
          card.numbersHidden && !card.hindered && event.stats.charStats[card.id]
            ? { ...card, numbersHidden: false }
            : card,
        ),
        skirmish: model.skirmish && { ...model.skirmish, shown: skirmishNumbers(model.skirmish.shown, event.stats) },
        lastRuleOf4:
          event.stats.ruleOf4 !== null && event.stats.ruleOf4 !== '-1' ? event.stats.ruleOf4 : model.lastRuleOf4,
        // Replays recorded before the server sent the flag have no player zones: the revealed hands stay as they were
        revealedHands:
          zones.length > 0 ? zones.filter(([, z]) => z.handRevealed).map(([name]) => name) : model.revealedHands,
      };
    }
    case 'EG':
      // A decision still open can't be answered any more
      return {
        ...next,
        ended: true,
        closedByEnd: model.decision?.decision.type ?? model.closedByEnd,
        decision: null,
        local: NO_CHOICES,
      };
    case 'D':
      return {
        ...next,
        decision: { decision: event.decision, arrivedAt: model.eventCount },
        local: NO_CHOICES,
        alertAsked: model.alertAsked || ALERT_DECISIONS.includes(event.decision.type),
      };
    // The game being taken back (GR) changes nothing here: the table was sent again before it (GameSession says it)
    case 'CAC':
    case 'EP':
    case 'CA':
    case 'GR':
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
    numbersHidden: false,
    index: null,
    attachedTo: null,
    stackedOn: null,
    attachedAt: 0,
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
