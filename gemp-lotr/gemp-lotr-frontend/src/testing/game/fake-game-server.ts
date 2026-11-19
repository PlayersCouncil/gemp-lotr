/**
 * Test-only: a game server that plays a recorded game back (recording.ts). The game's own requests (its first state,
 * the long polls, the answers to decisions, concede and cancel) must come in the recorded order, and each is answered
 * as the real server answered it; the player's information and card information are answered whenever they are asked.
 * The chat is empty. A request the recording doesn't have stays unanswered, like a long poll with nothing new, and a
 * request that differs from the recorded one is noted in `problems`.
 */
import { ServerAnswer, ServerRequest } from '../parity/fake-gemp-server';
import { AnswersRequests } from '../parity/legacy-window';
import { describeExchange, GameRecording, RecordedExchange } from './recording';

/** 'any': answered whenever asked (the player's information, card information); 'page' or 'player': in order. */
function kindOf(path: string, params: Record<string, string>): 'any' | 'page' | 'player' {
  if (path === '/player' || path.endsWith('/cardInfo')) return 'any';
  return path === '/game/<id>' && !('decisionId' in params) ? 'page' : 'player';
}

export class FakeGameServer implements AnswersRequests {
  /** The recorded exchanges answered so far, by their place in the recording. */
  readonly answered = new Set<number>();
  readonly problems: string[] = [];
  /**
   * Requests the recording answers at or past this place are held, as the real server held them while the browser
   * check took a step's snapshot (its long poll answered only after it).
   */
  limit = Infinity;

  constructor(
    readonly recording: GameRecording,
    readonly gameId = '1',
  ) {}

  /** How many recorded exchanges were answered. */
  get progress(): number {
    return this.answered.size;
  }

  /** The first recorded exchange not answered yet, with its place. */
  next(): { index: number; exchange: RecordedExchange } | null {
    const index = this.recording.exchanges.findIndex((_, i) => !this.answered.has(i));
    const exchange = this.recording.exchanges[index];
    return exchange ? { index, exchange } : null;
  }

  answer(request: ServerRequest): ServerAnswer | null {
    const path = request.path.replace(`/game/${this.gameId}`, '/game/<id>');
    if (path.startsWith('/chat/')) {
      // The chat's first answer and a sent message's: no messages; its polls stay open
      return request.method === 'GET' || 'message' in request.params ? chat() : null;
    }
    const params = Object.fromEntries(
      Object.entries(request.params)
        .filter(([name]) => !['participantId', 'channelNumber', '_'].includes(name))
        .sort(([a], [b]) => a.localeCompare(b)),
    );
    const asked = describeExchange({ method: request.method, path, params });
    // The page's own requests (its first state, its polls) and the player's (answers, concede, cancel) each come in
    // the recorded order. A poll can be sent before a gesture and answered after it (a cancel while the page polls):
    // it is answered once everything recorded before its answer has been.
    const kind = kindOf(path, params);
    const index = this.recording.exchanges.findIndex((exchange, i) => {
      if (this.answered.has(i)) return false;
      return kind === 'any' ? describeExchange(exchange) === asked : kindOf(exchange.path, exchange.params) === kind;
    });
    const exchange = this.recording.exchanges[index];
    if (!exchange) {
      // After the recording: a long poll waits; anything else is unexpected
      if (!(request.method === 'POST' && path === '/game/<id>' && !('decisionId' in params))) {
        this.problems.push(`not in the recording: ${asked}`);
      }
      return null;
    }
    if (index >= this.limit) {
      return null;
    }
    if (
      kind === 'page' &&
      exchange &&
      this.recording.exchanges.some(
        (other, i) => i < index && !this.answered.has(i) && kindOf(other.path, other.params) !== 'any',
      )
    ) {
      return null;
    }
    if (describeExchange(exchange) !== asked) {
      this.problems.push(`expected ${describeExchange(exchange)}, the page sent ${asked}`);
      return null;
    }
    this.answered.add(index);
    return { status: exchange.status, body: exchange.body, contentType: exchange.contentType };
  }
}

function chat(): ServerAnswer {
  return {
    status: 200,
    body: '<?xml version="1.0" encoding="UTF-8"?><chat/>',
    contentType: 'application/xml; charset=UTF-8',
  };
}
