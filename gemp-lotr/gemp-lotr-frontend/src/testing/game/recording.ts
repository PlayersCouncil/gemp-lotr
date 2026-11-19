/**
 * Test-only: the recorded games of the game check (visual-check/game, `npm run check:game:record`), kept in
 * src/testing/game-recordings: what the legacy game page and a real, seeded server said to each other in each scenario,
 * and the game the page held after each step.
 */
import { readdirSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { LegacyGameModel } from '../../../visual-check/game/controls.mjs';

/** One request of the page and the server's answer. The game's id is `<id>` in the path. */
export interface RecordedExchange {
  method: string;
  path: string;
  /** Without the participant, the channel number and jQuery's cache buster, sorted by name. */
  params: Record<string, string>;
  status: number;
  contentType: string;
  body: string;
}

export interface LocalGesture {
  after: number;
  /** A minion, then the character it goes to. */
  assign?: [string, string];
  /** A minion clicked twice. */
  unassign?: string;
}

/** A step of the scenario, after `exchanges` exchanges: the game as the page held it. */
export interface RecordedStep {
  name: string;
  exchanges: number;
  /**
   * Gestures made since the step before that sent nothing, e.g. a minion assigned (sent only with Done), each after
   * `after` exchanges: card ids.
   */
  gestures: LocalGesture[];
  model: LegacyGameModel;
}

export interface GameRecording {
  scenario: string;
  note: string;
  /** The player in the browser, and the opponent who played over HTTP. */
  participant: string;
  opponent: string;
  size: string;
  exchanges: RecordedExchange[];
  steps: RecordedStep[];
}

const DIR = resolve(process.cwd(), 'src/testing/game-recordings');

export function gameRecordingNames(): string[] {
  return readdirSync(DIR)
    .filter((file) => file.endsWith('.json'))
    .map((file) => file.replace(/\.json$/, ''))
    .sort();
}

export function readGameRecording(name: string): GameRecording {
  return JSON.parse(readFileSync(resolve(DIR, `${name}.json`), 'utf8')) as GameRecording;
}

/** The request a page sent, as the recording writes it (for comparing). */
export function describeExchange({
  method,
  path,
  params,
}: Pick<RecordedExchange, 'method' | 'path' | 'params'>): string {
  const kept = Object.entries(params).map(([name, value]) => `${name}=${value}`);
  return `${method} ${path}${kept.length ? ` ${kept.join('&')}` : ''}`;
}
