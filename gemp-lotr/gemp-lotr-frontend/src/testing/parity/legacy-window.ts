/**
 * Test-only: a legacy page (one of the web root's .html files and its scripts, unchanged) running in a browser-like
 * window of its own (jsdom) against a fake server, with a clock the test moves. Shared by the legacy deck builder
 * (legacy-page.ts) and the legacy game page (../game/legacy-game-page.ts).
 */
import { JSDOM, VirtualConsole } from 'jsdom';
import { readLegacyWebFile } from '../files';
import { ServerAnswer, ServerRequest } from './fake-gemp-server';
import { VirtualClock } from './virtual-clock';

const SERVER = '/gemp-lotr-server';
/** How long a fake server takes to answer. */
export const SERVER_DELAY_MS = 10;
const RETRY_MS = 50;

/** Answers to the browser's confirm (true = OK) and prompt (text, or null = Cancel), in the order they are asked. */
export type DialogAnswer = boolean | string | null;

/**
 * A fake server: an answer for each request, or null for none yet (a long poll that stays open). A request without an
 * answer asks again every RETRY_MS of the page's time, so a server can answer it later.
 */
export interface AnswersRequests {
  answer(request: ServerRequest): ServerAnswer | null;
}

function paramsOf(encoded: string): Record<string, string> {
  return Object.fromEntries(new URLSearchParams(encoded));
}

/** The XMLHttpRequest jQuery 1.6 uses, answered by the fake server after SERVER_DELAY_MS of the page's time. */
function fakeRequestClass(window: JSDOM['window'], server: AnswersRequests, clock: VirtualClock) {
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
        if (!answer) {
          // No answer yet: the request stays open, as a long poll does until something happens
          this.timer = clock.after(RETRY_MS, arrive);
          return;
        }
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
      // A synchronous request (the legacy deck builder converts a deck with one) has its answer when send() returns
      if (this.async) {
        this.timer = clock.after(SERVER_DELAY_MS, arrive);
      } else {
        arrive();
      }
    }
  };
}

/** The scripts a page names, in its order. */
function pageScripts(html: string): string[] {
  return [...html.matchAll(/<script[^>]*\ssrc="([^"]+)"/g)].map((found) => found[1] ?? '');
}

export interface LegacyWindowOptions {
  /** The page, by its path under the web root (e.g. "game.html"). */
  page: string;
  /** What follows the page's address: "?gameId=1". */
  query?: string;
  server: AnswersRequests;
  /** Runs before the page's scripts, in its window (e.g. cookies, what jsdom lacks). */
  prepare?: (window: JSDOM['window']) => void;
  /** Changes a script's text before it runs (the one allowed change: keeping the page's object where a test sees it). */
  script?: (src: string, text: string) => string;
}

export interface LegacyWindow {
  readonly window: JSDOM['window'];
  readonly clock: VirtualClock;
  /** "alert: ...", "confirm: ...", "prompt: ..." and "new tab: <address>" in the order the page showed them. */
  readonly dialogs: string[];
  readonly answers: DialogAnswer[];
  /** What the page's scripts threw. */
  readonly errors: string[];
}

/** Opens a legacy page: its scripts run as on the real page, and its document has loaded when this returns. */
export async function openLegacyWindow(options: LegacyWindowOptions): Promise<LegacyWindow> {
  const html = readLegacyWebFile(options.page);
  const clock = new VirtualClock();
  const dialogs: string[] = [];
  const answers: DialogAnswer[] = [];
  const errors: string[] = [];
  const console = new VirtualConsole();
  console.on('jsdomError', (error) => errors.push(String(error.message)));

  const dom = new JSDOM(html.replace(/<script[^>]*\ssrc="[^"]+"[^>]*>\s*<\/script>/g, ''), {
    url: `http://gemp.test/gemp-lotr/${options.page}${options.query ?? ''}`,
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
        XMLHttpRequest: fakeRequestClass(window, options.server, clock),
      });
    },
  });

  const { window } = dom;
  options.prepare?.(window);
  for (const src of pageScripts(html)) {
    const script = window.document.createElement('script');
    const text = readLegacyWebFile(src);
    script.textContent = options.script ? options.script(src, text) : text;
    window.document.head.appendChild(script);
  }
  // The window tells the page it has loaded in its own time (not the page's clock), which starts jQuery's ready
  for (let i = 0; i < 200 && window.document.readyState !== 'complete'; i++) {
    await new Promise((resolve) => setTimeout(resolve, 1));
  }
  return { window, clock, dialogs, answers, errors };
}
