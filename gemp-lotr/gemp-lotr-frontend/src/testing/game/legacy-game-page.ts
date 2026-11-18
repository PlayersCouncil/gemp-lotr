/**
 * Test-only: the legacy game page itself (game.html and its scripts, unchanged) in jsdom (../parity/legacy-window.ts),
 * against a FakeGameServer playing a recorded game back, with a clock the test moves. The Angular game page will be
 * compared with it on the same recordings (G1 and later of trymarz/gemp-lotr-private#27).
 */
import { JSDOM } from 'jsdom';
import { LegacyGameModel, readLegacyModel } from '../../../visual-check/game/controls.mjs';
import { LegacyJQuery } from '../parity/legacy-page';
import { DialogAnswer, openLegacyWindow } from '../parity/legacy-window';
import { FakeGameServer } from './fake-game-server';

export interface LegacyGamePage {
  readonly window: JSDOM['window'];
  readonly dialogs: string[];
  readonly answers: DialogAnswer[];
  readonly errors: string[];
  /** Lets `ms` of the page's time pass: its timers and animations run and the server's answers arrive. */
  advance(ms: number): void;
  $(selector: string): LegacyJQuery;
  /** The game the page holds, read as the browser check reads it (visual-check/game/controls.mjs). */
  model(): LegacyGameModel;
  close(): void;
}

/** Opens game.html for the server's game; the page has asked for the game's first state when this returns. */
export async function openLegacyGamePage(server: FakeGameServer): Promise<LegacyGamePage> {
  const { window, clock, dialogs, answers, errors } = await openLegacyWindow({
    page: 'game.html',
    query: `?gameId=${server.gameId}`,
    server,
    prepare: (window) => {
      // As in the browser check: the fastest animations
      window.document.cookie = 'animation-speed=4; path=/';
      // jsdom plays no sound
      window.HTMLMediaElement.prototype.play = () => Promise.resolve();
      window.HTMLMediaElement.prototype.pause = () => undefined;
    },
  });
  const page: LegacyGamePage = {
    window,
    dialogs,
    answers,
    errors,
    advance: (ms) => clock.advance(ms),
    $: (selector) => (window as unknown as { $: (s: string) => LegacyJQuery }).$(selector),
    model: () => window.eval(`(${readLegacyModel.toString()})()`) as LegacyGameModel,
    close: () => window.close(),
  };
  page.advance(1000);
  return page;
}
