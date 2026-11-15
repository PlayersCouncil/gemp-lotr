/**
 * Test-only: the Angular game page, whole, in jsdom against a FakeGameServer playing a recorded game back, on vitest's
 * fake timers; and the gestures that send the recorded requests, made through the page's DOM contract. The counterpart
 * of legacy-game-page.ts and replay-driver.ts, so the two pages can be compared on the same recordings (src/parity/game).
 */
import {
  HttpBackend,
  HttpErrorResponse,
  HttpEvent,
  HttpHeaders,
  HttpParams,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { GameSession } from '../../core/game/session/game-session.service';
import { GameModel } from '../../core/game/state/game-model';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { PlayerInfo } from '../../core/session/player-info.service';
import { GamePageComponent } from '../../ui/game/game-page.component';
import { FakeNotifier, provideFakeNotifier } from '../fake-notifier';
import { SERVER_DELAY_MS } from '../parity/legacy-window';
import { FakeGameServer } from './fake-game-server';
import { LocalGesture, RecordedExchange } from './recording';
import { decisionBefore, isGesture } from './replay-driver';

const SERVER = '/gemp-lotr-server';
/** A request the server has no answer for yet asks again this often (a long poll that stays open). */
const RETRY_MS = 50;

function paramsOf(params: HttpParams | null): Record<string, string> {
  return params ? Object.fromEntries(new URLSearchParams(params.toString())) : {};
}

/** Answers HttpClient from the fake server; a request without an answer yet asks again until it has one. */
class FakeGameBackend implements HttpBackend {
  constructor(private readonly server: FakeGameServer) {}

  handle(request: HttpRequest<unknown>): Observable<HttpEvent<unknown>> {
    return new Observable((subscriber) => {
      let timer: ReturnType<typeof setTimeout>;
      const ask = () => {
        const body = request.body instanceof HttpParams ? request.body : null;
        const answer = this.server.answer({
          method: request.method,
          path: request.url.replace(SERVER, ''),
          params: { ...paramsOf(request.params), ...paramsOf(body) },
        });
        if (!answer) {
          timer = setTimeout(ask, RETRY_MS);
          return;
        }
        const headers = new HttpHeaders({ 'Content-Type': answer.contentType });
        if (answer.status >= 400) {
          subscriber.error(new HttpErrorResponse({ status: answer.status, url: request.url, headers }));
        } else {
          subscriber.next(new HttpResponse({ status: answer.status, body: answer.body, url: request.url, headers }));
          subscriber.complete();
        }
      };
      timer = setTimeout(ask, SERVER_DELAY_MS);
      return () => clearTimeout(timer);
    });
  }
}

export interface AngularGamePage {
  readonly fixture: ComponentFixture<GamePageComponent>;
  readonly element: HTMLElement;
  readonly dialogs: string[];
  readonly answers: (boolean | string | null)[];
  /** The game the page holds. */
  model(): GameModel;
  /** Lets `ms` pass: timers run, the server's answers arrive and the page is drawn again. */
  advance(ms: number): Promise<void>;
  close(): void;
}

/** Opens the page on the server's game. Needs `vi.useFakeTimers()`. */
export async function openAngularGamePage(server: FakeGameServer): Promise<AngularGamePage> {
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      { provide: HttpBackend, useValue: new FakeGameBackend(server) },
      provideFakeNotifier(),
      { provide: PAGE_QUERY_STRING, useValue: `?gameId=${server.gameId}` },
    ],
  });
  // As the login guard does before the page opens
  TestBed.inject(PlayerInfo).load().subscribe();
  const fixture = TestBed.createComponent(GamePageComponent);
  const notifier = TestBed.inject(FakeNotifier);
  const session = fixture.debugElement.injector.get(GameSession);
  const page: AngularGamePage = {
    fixture,
    element: fixture.nativeElement as HTMLElement,
    dialogs: notifier.dialogs,
    answers: notifier.answers,
    model: () => session.model(),
    advance: async (ms) => {
      await vi.advanceTimersByTimeAsync(ms);
      fixture.detectChanges();
      await vi.advanceTimersByTimeAsync(0);
      fixture.detectChanges();
    },
    close: () => fixture.destroy(),
  };
  fixture.detectChanges();
  await page.advance(1000);
  return page;
}

function find(page: AngularGamePage, selector: string): HTMLElement | null {
  return Array.from(page.element.querySelectorAll<HTMLElement>(selector)).at(-1) ?? null;
}

async function click(page: AngularGamePage, selector: string, what = selector): Promise<void> {
  const element = find(page, selector);
  if (!element) throw new Error(`no ${what}`);
  element.click();
  await page.advance(0);
}

const button = (label: string) => `[data-decision-button="${label}"]`;
const card = (id: string) => `[data-card-id="${id}"]`;

async function assign(page: AngularGamePage, minion: string, character: string): Promise<void> {
  await click(page, card(minion));
  await click(page, card(character));
}

async function makeLocalGesture(page: AngularGamePage, gesture: LocalGesture): Promise<void> {
  if (gesture.assign) await assign(page, ...gesture.assign);
  if (gesture.unassign) {
    await click(page, card(gesture.unassign));
    await page.advance(300);
    await click(page, card(gesture.unassign));
  }
  await page.advance(1000);
}

/** Makes the gesture that sends `exchange`, the next recorded one the player starts. */
async function makeGesture(
  page: AngularGamePage,
  server: FakeGameServer,
  index: number,
  exchange: RecordedExchange,
): Promise<void> {
  if (exchange.path.endsWith('/concede') || exchange.path.endsWith('/cancel')) {
    await click(page, '[data-tab="options"]');
    await click(page, `[data-button="${exchange.path.endsWith('/concede') ? 'concedeGame' : 'cancelGame'}"]`);
    return;
  }
  const decision = decisionBefore(server, index, server.recording.participant);
  if (!decision) throw new Error('an answer with no decision before it');
  const value = exchange.params['decisionValue'] ?? '';
  const ids = (name: string) => decision.values[name] ?? [];
  switch (decision.type) {
    case 'INTEGER': {
      const shown = Number(find(page, '[data-decision="integer"]')?.textContent);
      const arrow = `[data-decision="integer-${Number(value) > shown ? 'up' : 'down'}"]`;
      for (let i = 0; i < Math.abs(Number(value) - shown); i++) await click(page, arrow);
      await click(page, button('OK'));
      return;
    }
    case 'MULTIPLE_CHOICE': {
      const list = find(page, 'select[data-decision="choice"]') as HTMLSelectElement | null;
      if (list) {
        list.value = value;
        list.dispatchEvent(new Event('change'));
        await click(page, button('OK'));
      } else {
        await click(page, `[data-decision-choice="${value}"]`);
      }
      return;
    }
    case 'CARD_ACTION_CHOICE': {
      if (value === '') {
        await click(page, button('Pass'));
        return;
      }
      const at = ids('actionId').indexOf(value);
      const cardId =
        ids('blueprintId')[at] === 'inPlay' ? (ids('cardId')[at] ?? '') : `extra${ids('cardId')[at] ?? ''}`;
      await click(page, card(cardId));
      await page.advance(200);
      // A card with several actions shows a menu of them
      if (find(page, `[data-action-id="${value}"]`)) await click(page, `[data-action-id="${value}"]`);
      return;
    }
    case 'ASSIGN_MINIONS': {
      // "character minion minion,character minion": each minion not assigned yet, then its character
      for (const group of value.split(',').filter(Boolean)) {
        const [character = '', ...minions] = group.split(' ');
        for (const minion of minions) {
          if (find(page, card(minion))?.dataset['assign'] !== character) await assign(page, minion, character);
        }
      }
      page.answers.push(true);
      await click(page, button('Done'));
      return;
    }
    default: {
      // Cards to pick (CARD_SELECTION, ARBITRARY_CARDS, ACTION_CHOICE): each, then Done unless that answered it
      const before = server.progress;
      const cardIds =
        decision.type === 'ACTION_CHOICE'
          ? value
              .split(',')
              .filter(Boolean)
              .map((actionId) => `temp${ids('actionId').indexOf(actionId)}`)
          : value.split(',').filter(Boolean);
      for (const cardId of cardIds) {
        await click(page, card(cardId));
        await page.advance(50);
      }
      await page.advance(50);
      if (server.progress === before && find(page, button('Done'))) await click(page, button('Done'));
    }
  }
}

/**
 * Plays the recording on the page up to `exchanges` answered exchanges, as replay-driver.ts does on the legacy page.
 * `skip` names recorded requests the page doesn't make yet (they count as answered).
 */
export async function playAngularUntil(
  page: AngularGamePage,
  server: FakeGameServer,
  exchanges: number,
  local: LocalGesture[] = [],
  skip: (exchange: RecordedExchange) => boolean = () => false,
): Promise<void> {
  server.limit = exchanges;
  const pending = [...local];
  const makeDueGestures = async () => {
    while (pending.length > 0 && (pending[0]?.after ?? 0) <= server.progress) {
      const gesture = pending.shift();
      if (gesture) await makeLocalGesture(page, gesture);
    }
  };
  let gestured = -1;
  for (let round = 0; round < 400 && server.progress < exchanges; round++) {
    await page.advance(250);
    await makeDueGestures();
    const next = server.next();
    if (next && next.index < exchanges && skip(next.exchange)) {
      server.answered.add(next.index);
      continue;
    }
    if (
      next &&
      next.index < exchanges &&
      isGesture(next.exchange) &&
      gestured !== next.index &&
      server.problems.length === 0
    ) {
      await page.advance(1000);
      gestured = next.index;
      await makeGesture(page, server, next.index, next.exchange);
    }
  }
  if (server.progress < exchanges) {
    const next = server.next();
    throw new Error(
      `stopped after ${server.progress} of ${exchanges} exchanges${next ? `, waiting for ${next.exchange.method} ${next.exchange.path} ${JSON.stringify(next.exchange.params)}` : ''}${server.problems.length ? `: ${server.problems.join('; ')}` : ''}`,
    );
  }
  await page.advance(5000);
  await makeDueGestures();
}
