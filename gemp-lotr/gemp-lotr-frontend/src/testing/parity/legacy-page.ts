/**
 * Test-only: the legacy deck builder itself (deckBuild.html and its scripts, unchanged), running in a browser-like
 * window of its own (jsdom, legacy-window.ts) against a FakeGempServer, with a clock the test moves. For comparing
 * what it does with what the Angular page does (src/parity).
 */
import { JSDOM } from 'jsdom';
import { FakeGempServer } from './fake-gemp-server';
import { DialogAnswer, openLegacyWindow } from './legacy-window';

export { SERVER_DELAY_MS } from './legacy-window';
export type { DialogAnswer } from './legacy-window';

export interface LegacyPage {
  readonly window: JSDOM['window'];
  /** "alert: ...", "confirm: ...", "prompt: ..." and "new tab: <address>" in the order the page showed them. */
  readonly dialogs: string[];
  readonly answers: DialogAnswer[];
  /** What the page's scripts threw. */
  readonly errors: string[];
  /** Lets `ms` of the page's time pass: its timers run and the server's answers arrive. */
  advance(ms: number): void;
  /**
   * The legacy deck builder object (GempLotrDeckBuildingUI, js/gemp-022/deckBuildingUi.js), for calling its functions
   * on generated inputs.
   */
  readonly deckBuilder: Record<string, unknown> & { cardFilter: Record<string, unknown> };
  /** The page's jQuery. */
  $(selector: string): LegacyJQuery;
  close(): void;
}

/** The little of jQuery 1.6 the tests use. */
export interface LegacyJQuery {
  length: number;
  eq(index: number): LegacyJQuery;
  get(index: number): HTMLElement | undefined;
  val(): string;
  val(value: string): LegacyJQuery;
  text(): string;
  trigger(event: string | object): LegacyJQuery;
  click(): LegacyJQuery;
  change(): LegacyJQuery;
  find(selector: string): LegacyJQuery;
  filter(selector: string): LegacyJQuery;
  is(selector: string): boolean;
  attr(name: string): string | undefined;
  data(name: string): unknown;
}

/**
 * Opens deckBuild.html: its scripts run as on the real page (jQuery, jQuery UI, the deck builder), and the page has
 * started (the formats, the first collection page and the stats have arrived) when this returns.
 */
export async function openLegacyPage(server: FakeGempServer, query = ''): Promise<LegacyPage> {
  const { window, clock, dialogs, answers, errors } = await openLegacyWindow({
    page: 'deckBuild.html',
    query,
    server,
    // The page's stylesheets aren't loaded here. One of them hides this unused shell of a dialog (src/DeckBuilder/index.css)
    prepare: (window) => window.document.getElementById('cardInfoDiv')?.style.setProperty('display', 'none'),
    // The one change to the legacy code: its deck builder object is kept where a test can reach it
    script: (_src, text) =>
      text.replace(
        'var ui = new GempLotrDeckBuildingUI();',
        'var ui = window.legacyDeckBuilder = new GempLotrDeckBuildingUI();',
      ),
  });

  const page: LegacyPage = {
    window,
    dialogs,
    answers,
    errors,
    get deckBuilder() {
      return (window as unknown as { legacyDeckBuilder: LegacyPage['deckBuilder'] }).legacyDeckBuilder;
    },
    advance: (ms) => clock.advance(ms),
    $: (selector) => (window as unknown as { $: (s: string) => LegacyJQuery }).$(selector),
    close: () => window.close(),
  };
  // The ready handlers, the first requests and their answers, the stats' first refresh
  page.advance(2000);
  return page;
}
