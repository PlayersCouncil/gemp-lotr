/**
 * Test-only: the legacy deck builder itself (deckBuild.html and its scripts, unchanged), running in a browser-like
 * window of its own (jsdom) against a FakeGempServer, with a clock the test moves. For comparing what it does with
 * what the Angular page does (src/parity).
 */
import { JSDOM, VirtualConsole } from 'jsdom';
import { readLegacyWebFile } from '../files';
import { FakeGempServer, ServerAnswer } from './fake-gemp-server';
import { VirtualClock } from './virtual-clock';

const SERVER = '/gemp-lotr-server';
/** How long the fake server takes to answer. */
export const SERVER_DELAY_MS = 10;

/** Answers to the browser's confirm (true = OK) and prompt (text, or null = Cancel), in the order they are asked. */
export type DialogAnswer = boolean | string | null;

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

function paramsOf(encoded: string): Record<string, string> {
  return Object.fromEntries(new URLSearchParams(encoded));
}

/** The XMLHttpRequest jQuery 1.6 uses, answered by the fake server after SERVER_DELAY_MS of the page's time. */
function fakeRequestClass(window: JSDOM['window'], server: FakeGempServer, clock: VirtualClock) {
  return class FakeXMLHttpRequest {
    readyState = 0;
    status = 0;
    statusText = '';
    responseText = '';
    responseXML: Document | null = null;
    onreadystatechange: (() => void) | null = null;
    private method = 'GET';
    private url = '';
    private async = true;
    private answer: ServerAnswer | null = null;
    private timer: number | undefined;

    open(method: string, url: string, async = true): void {
      this.method = method.toUpperCase();
      this.url = url;
      this.async = async;
    }

    setRequestHeader(): void {
      // nothing the fake server reads
    }

    getAllResponseHeaders(): string {
      return this.answer ? `Content-Type: ${this.answer.contentType}\r\n` : '';
    }

    getResponseHeader(name: string): string | null {
      return this.answer && name.toLowerCase() === 'content-type' ? this.answer.contentType : null;
    }

    abort(): void {
      clock.clear(this.timer);
    }

    send(body?: string | null): void {
      const [address = '', query = ''] = this.url.split('?');
      const path = address.replace(/^https?:\/\/[^/]+/, '').replace(SERVER, '');
      const params = { ...paramsOf(query), ...paramsOf(body ?? '') };
      const arrive = () => {
        const answer = server.answer({ method: this.method, path, params });
        this.answer = answer;
        this.readyState = 4;
        this.status = answer.status;
        this.statusText = answer.status === 200 ? 'OK' : 'Error';
        this.responseText = answer.body;
        if (answer.contentType.includes('xml')) {
          this.responseXML = new window.DOMParser().parseFromString(answer.body, 'application/xml');
        }
        this.onreadystatechange?.();
      };
      // A synchronous request (the legacy page converts a deck with one) has its answer when send() returns
      if (this.async) {
        this.timer = clock.after(SERVER_DELAY_MS, arrive);
      } else {
        arrive();
      }
    }
  };
}

/** The scripts deckBuild.html names, in its order. */
function pageScripts(html: string): string[] {
  return [...html.matchAll(/<script[^>]*\ssrc="([^"]+)"/g)].map((found) => found[1] ?? '');
}

/**
 * Opens deckBuild.html: its scripts run as on the real page (jQuery, jQuery UI, the deck builder), and the page has
 * started (the formats, the first collection page and the stats have arrived) when this returns.
 */
export async function openLegacyPage(server: FakeGempServer, query = ''): Promise<LegacyPage> {
  const html = readLegacyWebFile('deckBuild.html');
  const clock = new VirtualClock();
  const dialogs: string[] = [];
  const answers: DialogAnswer[] = [];
  const errors: string[] = [];
  const console = new VirtualConsole();
  console.on('jsdomError', (error) => errors.push(String(error.message)));

  const dom = new JSDOM(html.replace(/<script[^>]*\ssrc="[^"]+"[^>]*>\s*<\/script>/g, ''), {
    url: `http://gemp.test/gemp-lotr/deckBuild.html${query}`,
    runScripts: 'dangerously',
    virtualConsole: console,
    beforeParse(window) {
      const RealDate = window.Date;
      window.Date = class extends RealDate {
        constructor(...args: unknown[]) {
          if (args.length === 0) {
            super(clock.now());
          } else {
            super(...(args as [number]));
          }
        }
        static override now(): number {
          return clock.now();
        }
      } as DateConstructor;
      const call = (handler: unknown, args: unknown[]) => () => {
        if (typeof handler === 'function') {
          (handler as (...a: unknown[]) => void)(...args);
        } else {
          window.eval(String(handler));
        }
      };
      Object.assign(window, {
        setTimeout: (handler: unknown, delay = 0, ...args: unknown[]) => clock.after(delay, call(handler, args)),
        setInterval: (handler: unknown, delay = 0, ...args: unknown[]) => clock.every(delay, call(handler, args)),
        clearTimeout: (id?: number) => clock.clear(id),
        clearInterval: (id?: number) => clock.clear(id),
        alert: (message: unknown) => void dialogs.push(`alert: ${String(message)}`),
        confirm: (message: unknown) => {
          dialogs.push(`confirm: ${String(message)}`);
          return answers.shift() === true;
        },
        prompt: (message: unknown) => {
          dialogs.push(`prompt: ${String(message)}`);
          const answer = answers.shift();
          return typeof answer === 'string' ? answer : null;
        },
        open: (url: unknown) => {
          dialogs.push(`new tab: ${String(url)}`);
          return null;
        },
        XMLHttpRequest: fakeRequestClass(window, server, clock),
      });
    },
  });

  const { window } = dom;
  // The page's stylesheets aren't loaded here. One of them hides this unused shell of a dialog (src/DeckBuilder/index.css)
  window.document.getElementById('cardInfoDiv')?.style.setProperty('display', 'none');
  for (const src of pageScripts(html)) {
    const script = window.document.createElement('script');
    // The one change to the legacy code: its deck builder object is kept where a test can reach it
    script.textContent = readLegacyWebFile(src).replace(
      'var ui = new GempLotrDeckBuildingUI();',
      'var ui = window.legacyDeckBuilder = new GempLotrDeckBuildingUI();',
    );
    window.document.head.appendChild(script);
  }

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
  // The window tells the page it has loaded in its own time (not the page's clock), which starts jQuery's ready
  for (let i = 0; i < 200 && window.document.readyState !== 'complete'; i++) {
    await new Promise((resolve) => setTimeout(resolve, 1));
  }
  // The ready handlers, the first requests and their answers, the stats' first refresh
  page.advance(2000);
  return page;
}
