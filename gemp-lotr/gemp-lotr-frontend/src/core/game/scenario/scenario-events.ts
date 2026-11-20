/**
 * What a scripted game (a scenario: a tutorial, a puzzle) tells the page beside the game itself: who it is and how it
 * plays (`info`), what the script has to say about the decision at hand and which answers it takes (`step`), that the
 * game has left its script (`offscript`), and how it ended (`end`). The server sends each as JSON in the `scenario`
 * attribute of a game event of type S, to the person playing only (LiveScenario.java builds them). Pure: read from
 * the JSON with every field checked, so the rest of the page can rely on the shapes.
 */
import { legacyRelativeImages } from '../../platform/app-paths';

export type ScenarioKind = 'tutorial' | 'puzzle' | 'sandbox';
export type ScenarioSeat = 'freeps' | 'shadow';

/** Where the scenario stands in the campaign it is part of. */
export interface ScenarioCampaign {
  id: string;
  title: string;
  position: number;
  count: number;
  next: { id: string; title: string } | null;
}

/** The player's own settings a scenario fixes for its game (null: the player's own). */
export interface ScenarioSettings {
  autoAccept: boolean | null;
  autoDismissDeckReveal: boolean | null;
  /** The animation speed slider's step, -4 to 4. */
  animationSpeed: number | null;
}

export interface ScenarioInfo {
  type: 'info';
  id: string;
  kind: ScenarioKind;
  title: string;
  /** The seat of the person playing. */
  humanSeat: ScenarioSeat;
  /** The scripted opponent's name as the game knows it, and the name it is shown under. */
  opponent: string;
  opponentName: string;
  settings: ScenarioSettings;
  /** How far a bubble sits from what it points at where its message doesn't say (see ScenarioMessage.gap). */
  bubbleGap: string | null;
  /** The checkpoint step the game was brought back to, or null for a game from the beginning. */
  resumedAt: number | null;
  /**
   * Both seats take turns: the person is the Free Peoples player on their own turns and a Shadow player on the
   * opponent's (the step says which, `ScenarioStep.role`). False: the person always plays their seat's role.
   */
  twoSided: boolean;
  /** How the game opens: the board laid out by the scenario, or the game's own opening played (the bid, ...). */
  opening: 'board' | 'game';
  campaign: ScenarioCampaign | null;
  /** The person is writing the scenario as they play it (the authoring panel goes with this)... */
  author: boolean;
  /** ...or playing a draft of it as a player would. */
  draftTest: boolean;
}

export type MessageSide = 'auto' | 'above' | 'below' | 'left' | 'right';
export type HighlightStyle = 'gold' | 'red' | 'green' | 'blue';
/** What takes a message off the screen: its button, the decision's answer, a card's information opened, or closed. */
export type MessageAdvance = 'next' | 'decision' | 'inspect' | 'close';
export type MessageMoment = 'start' | 'picked' | 'assigned' | 'ready';

/** A part of a picture, in hundredths of its width and height: left, top, width, height. */
export type PartRect = readonly [number, number, number, number];

/** A card or picture a message shows large, with the parts of it the message is about. */
export interface ScenarioZoom {
  blueprint: string | null;
  image: string | null;
  magnify: number;
  focus: readonly { name: string | null; rect: PartRect }[];
}

export interface ScenarioMessage {
  /**
   * Its place among the messages of its step in the scenario's file, from 0. A message whose condition doesn't hold is
   * left out of what the server sends, so this is not always its place in the list it came in (see messagePlace).
   * Null where the server doesn't say: a closing message, or a game recorded before messages could be left out.
   */
  index: number | null;
  header: string | null;
  /** The message, as the server rendered it (HTML: paragraphs, card links, culture and twilight icons). */
  html: string;
  /** What the bubble points at (see scenario anchors); none: it sits in the middle. */
  anchors: readonly string[];
  /** "3": hundredths of the window's shorter side; "20px": pixels; null: the scenario's, else the stock distance. */
  gap: string | null;
  highlight: readonly string[];
  highlightStyle: HighlightStyle;
  /** What the bubble must not cover. */
  avoid: readonly string[];
  side: MessageSide;
  advance: MessageAdvance;
  /** For `inspect`: the card (by its name in the scenario) whose information has to be opened; null: any. */
  advanceCard: string | null;
  /** Buttons in place of Next; the one pressed is told to the server. */
  choices: readonly { name: string; label: string }[];
  /** Shown at a moment inside the answering instead of when the step starts. */
  on: { moment: MessageMoment; card: string | null } | null;
  zoom: ScenarioZoom | null;
}

/** What a step takes for an answer. */
export interface ScenarioAllow {
  /**
   * The answers, as the game takes them for this decision: an action's id; card ids joined by commas ("" for none);
   * for an assignment "character minion minion" groups joined by commas, in the order they are to be made; a choice's
   * place in its list; a number.
   */
  answers: readonly string[];
  pass: boolean;
  /** What to say of any other click (HTML), or null for the stock line. */
  otherwise: string | null;
  /** What to say of a click on one card (by its name in the scenario; HTML). */
  hints: Readonly<Record<string, string>>;
}

export interface ScenarioStep {
  type: 'step';
  step: number;
  /** Which step of a block (from 1), or 0 for a step of the script itself. */
  sub: number;
  /** Whose decision it is about; the opponent's is only read. */
  seat: 'human' | 'opponent';
  /** A step read before the person's own decision is asked. */
  pause: boolean;
  /** The role the person plays at this moment (in a two-sided game, it changes with the turn); null: their seat's. */
  role: ScenarioSeat | null;
  /** Whose turn it is, and which (the game's own count; 0 in the game's own opening); null when not said. */
  turnOf: 'human' | 'opponent' | null;
  turn: number | null;
  /** The last checkpoint step reached (this one, if it is one), which the game can be taken back to; null: none. */
  lastCheckpoint: number | null;
  messages: readonly ScenarioMessage[];
  /** Where a printed part of a card is on it, by "name:part" in lower case. */
  parts: Readonly<Record<string, PartRect>>;
  /** Null: any answer. */
  allow: ScenarioAllow | null;
  /** The card id each name used by the step has at this moment. */
  cards: Readonly<Record<string, string>>;
}

export interface ScenarioOffScript {
  type: 'offscript';
  message: string | null;
}

export type ScenarioResult = 'complete' | 'failed' | 'off_script' | 'abandoned' | 'error';

export interface ScenarioEnd {
  type: 'end';
  result: ScenarioResult;
  messages: readonly ScenarioMessage[];
  message: string | null;
  /** The last checkpoint step reached, or null. */
  checkpoint: number | null;
  /**
   * The game left its script and the scenario has it played again (outcome.offScript.result: restart, the default),
   * rather than counting it a failure (`fail`). Only ever true with the result `off_script`.
   */
  restart: boolean;
}

export type ScenarioEvent = ScenarioInfo | ScenarioStep | ScenarioOffScript | ScenarioEnd;

type Json = Record<string, unknown>;

const isObject = (value: unknown): value is Json =>
  typeof value === 'object' && value !== null && !Array.isArray(value);
const string = (value: unknown): string | null => (typeof value === 'string' ? value : null);
const number = (value: unknown): number | null => (typeof value === 'number' && Number.isFinite(value) ? value : null);
const boolean = (value: unknown): boolean | null => (typeof value === 'boolean' ? value : null);
const strings = (value: unknown): string[] =>
  Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
const html = (value: unknown): string | null => {
  const text = string(value);
  return text === null ? null : legacyRelativeImages(text);
};

function oneOf<T extends string>(value: unknown, known: readonly T[], fallback: T): T {
  return known.includes(value as T) ? (value as T) : fallback;
}

function partRect(value: unknown): PartRect | null {
  if (!Array.isArray(value) || value.length !== 4) return null;
  const [left, top, width, height] = value.map(number);
  if (left == null || top == null || width == null || height == null) return null;
  return [left, top, width, height];
}

function zoom(value: unknown): ScenarioZoom | null {
  if (!isObject(value)) return null;
  const focus = Array.isArray(value['focus']) ? value['focus'] : [];
  return {
    blueprint: string(value['blueprint']),
    image: string(value['image']),
    magnify: Math.max(1, number(value['magnify']) ?? 1),
    focus: focus.flatMap((part: unknown) => {
      const rect = isObject(part) ? partRect(part['rect']) : null;
      return rect && isObject(part) ? [{ name: string(part['name']), rect }] : [];
    }),
  };
}

function message(value: unknown): ScenarioMessage | null {
  if (!isObject(value)) return null;
  const on = value['on'];
  const anchors = strings(value['anchors']);
  const anchor = string(value['anchor']);
  const choices = Array.isArray(value['choices']) ? value['choices'] : [];
  const index = number(value['index']);
  return {
    index: index !== null && Number.isInteger(index) && index >= 0 ? index : null,
    header: string(value['header']),
    html: html(value['html']) ?? '',
    // (a game recorded before a message could point at several things names the one)
    anchors: anchors.length > 0 ? anchors : anchor !== null ? [anchor] : [],
    gap: string(value['gap']),
    highlight: strings(value['highlight']),
    highlightStyle: oneOf<HighlightStyle>(value['highlightStyle'], ['gold', 'red', 'green', 'blue'], 'gold'),
    avoid: strings(value['avoid']),
    side: oneOf<MessageSide>(value['side'], ['auto', 'above', 'below', 'left', 'right'], 'auto'),
    advance: oneOf<MessageAdvance>(value['advance'], ['next', 'decision', 'inspect', 'close'], 'next'),
    advanceCard: string(value['advanceCard']),
    choices: choices.flatMap((choice: unknown) => {
      const name = isObject(choice) ? string(choice['name']) : null;
      return name !== null && isObject(choice) ? [{ name, label: string(choice['label']) ?? name }] : [];
    }),
    on: isObject(on)
      ? {
          moment: oneOf<MessageMoment>(on['moment'], ['start', 'picked', 'assigned', 'ready'], 'start'),
          card: string(on['card']),
        }
      : null,
    zoom: zoom(value['zoom']),
  };
}

function messages(value: unknown): ScenarioMessage[] {
  return (Array.isArray(value) ? value : []).flatMap((item: unknown) => {
    const parsed = message(item);
    return parsed ? [parsed] : [];
  });
}

function allow(value: unknown): ScenarioAllow | null {
  if (!isObject(value)) return null;
  const hints = isObject(value['hints']) ? value['hints'] : {};
  return {
    answers: strings(value['answers']),
    pass: boolean(value['pass']) ?? false,
    otherwise: html(value['otherwise']),
    hints: Object.fromEntries(
      Object.entries(hints).flatMap(([name, text]) => {
        const hint = html(text);
        return hint === null ? [] : [[name, hint] as const];
      }),
    ),
  };
}

function campaign(value: unknown): ScenarioCampaign | null {
  if (!isObject(value)) return null;
  const id = string(value['id']);
  if (id === null) return null;
  const next = value['next'];
  const nextId = isObject(next) ? string(next['id']) : null;
  return {
    id,
    title: string(value['title']) ?? id,
    position: number(value['position']) ?? 1,
    count: number(value['count']) ?? 1,
    next: nextId !== null && isObject(next) ? { id: nextId, title: string(next['title']) ?? nextId } : null,
  };
}

function info(value: Json): ScenarioInfo | null {
  const id = string(value['id']);
  if (id === null) return null;
  const opponent = string(value['opponent']) ?? '';
  return {
    type: 'info',
    id,
    kind: oneOf<ScenarioKind>(value['kind'], ['tutorial', 'puzzle', 'sandbox'], 'tutorial'),
    title: string(value['title']) ?? id,
    humanSeat: oneOf<ScenarioSeat>(value['humanSeat'], ['freeps', 'shadow'], 'freeps'),
    opponent,
    opponentName: string(value['opponentName']) ?? opponent,
    settings: {
      autoAccept: boolean(value['autoAccept']),
      autoDismissDeckReveal: boolean(value['autoDismissDeckReveal']),
      animationSpeed: number(value['animationSpeed']),
    },
    bubbleGap: string(value['bubbleGap']),
    resumedAt: number(value['resumedAt']),
    twoSided: boolean(value['twoSided']) ?? false,
    opening: value['opening'] === 'game' ? 'game' : 'board',
    campaign: campaign(value['campaign']),
    author: boolean(value['author']) ?? false,
    draftTest: boolean(value['draftTest']) ?? false,
  };
}

function step(value: Json): ScenarioStep | null {
  const stepNumber = number(value['step']);
  if (stepNumber === null) return null;
  const parts = isObject(value['parts']) ? value['parts'] : {};
  const cards = isObject(value['cards']) ? value['cards'] : {};
  return {
    type: 'step',
    step: stepNumber,
    sub: number(value['sub']) ?? 0,
    seat: value['seat'] === 'opponent' ? 'opponent' : 'human',
    pause: boolean(value['pause']) ?? false,
    role: value['role'] === 'freeps' || value['role'] === 'shadow' ? value['role'] : null,
    turnOf: value['turnOf'] === 'human' || value['turnOf'] === 'opponent' ? value['turnOf'] : null,
    turn: number(value['turn']),
    lastCheckpoint: number(value['lastCheckpoint']),
    messages: messages(value['messages']),
    parts: Object.fromEntries(
      Object.entries(parts).flatMap(([name, rect]) => {
        const parsed = partRect(rect);
        return parsed ? [[name.toLowerCase(), parsed] as const] : [];
      }),
    ),
    allow: allow(value['allow']),
    // (the game's card ids are numbers to the server and text to the page)
    cards: Object.fromEntries(
      Object.entries(cards).flatMap(([name, id]) =>
        typeof id === 'number' || typeof id === 'string' ? [[name, String(id)] as const] : [],
      ),
    ),
  };
}

function end(value: Json): ScenarioEnd {
  return {
    type: 'end',
    result: oneOf<ScenarioResult>(value['result'], ['complete', 'failed', 'off_script', 'abandoned', 'error'], 'error'),
    messages: messages(value['messages']),
    message: html(value['message']),
    checkpoint: number(value['checkpoint']),
    restart: boolean(value['restart']) ?? false,
  };
}

/**
 * A message's place among its step's messages in the scenario's file: what it says (`index`), else its place in the
 * list it came in (a game recorded before messages could be left out has every one in its place). -1: not one of them.
 */
export function messagePlace(messages: readonly ScenarioMessage[], message: ScenarioMessage): number {
  const at = messages.indexOf(message);
  return at < 0 ? -1 : (message.index ?? at);
}

/** A scenario event from its JSON; null for anything that isn't one (unreadable, or of a kind the page doesn't know). */
export function parseScenarioEvent(json: string): ScenarioEvent | null {
  let value: unknown;
  try {
    value = JSON.parse(json);
  } catch {
    return null;
  }
  if (!isObject(value)) return null;
  switch (value['type']) {
    case 'info':
      return info(value);
    case 'step':
      return step(value);
    case 'offscript':
      return { type: 'offscript', message: html(value['message']) };
    case 'end':
      return end(value);
    default:
      return null;
  }
}
