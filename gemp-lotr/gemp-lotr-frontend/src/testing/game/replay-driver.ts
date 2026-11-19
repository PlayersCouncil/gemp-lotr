/**
 * Test-only: makes the gestures on the legacy game page (in jsdom) that send the recorded requests: the answer to each
 * decision, card information, concede. The gestures are the browser check's (visual-check/game/run.mjs), made with
 * jQuery clicks, since jsdom has no layout: the bid's field and OK, a choice's button or list, cards, Pass and Done,
 * the action menu, a minion and then its character. Which gesture follows from the decision and the recorded answer.
 */
import { LegacyJQuery } from '../parity/legacy-page';
import { FakeGameServer } from './fake-game-server';
import { LegacyGamePage } from './legacy-game-page';
import { LocalGesture, RecordedExchange } from './recording';

interface Decision {
  type: string;
  values: Record<string, string[]>;
}

/** The last decision for the participant in the answers given so far. */
export function decisionBefore(server: FakeGameServer, index: number, participant: string): Decision | null {
  let found: Decision | null = null;
  server.recording.exchanges.slice(0, index).forEach((exchange) => {
    if (!exchange.contentType.includes('xml')) return;
    const xml = new DOMParser().parseFromString(exchange.body, 'application/xml');
    for (const event of Array.from(xml.getElementsByTagName('ge'))) {
      if (event.getAttribute('type') === 'D' && event.getAttribute('participantId') === participant) {
        const values: Record<string, string[]> = {};
        for (const param of Array.from(event.getElementsByTagName('parameter'))) {
          (values[param.getAttribute('name') ?? ''] ??= []).push(param.getAttribute('value') ?? '');
        }
        found = { type: event.getAttribute('decisionType') ?? '', values };
      }
    }
  });
  return found;
}

/** Whether the exchange is one the player starts (rather than the page itself: its first state and its polls). */
export function isGesture(exchange: RecordedExchange): boolean {
  return (
    'decisionId' in exchange.params || exchange.path.endsWith('/cardInfo') || /\/(concede|cancel)$/.test(exchange.path)
  );
}

/** The open jQuery UI dialogs (jsdom has no layout, so ":visible" can't tell). */
function openDialogs(page: LegacyGamePage): LegacyJQuery {
  return page.$('.ui-dialog').filter(function (this: HTMLElement) {
    return this.style.display !== 'none';
  } as unknown as string);
}

function button(page: LegacyGamePage, label: string): LegacyJQuery | null {
  const inDialog = openDialogs(page).find('.ui-dialog-buttonpane button');
  for (let i = 0; i < inDialog.length; i++) {
    if (inDialog.eq(i).text().trim() === label) return inDialog.eq(i);
  }
  const alert = page.$(`.alertButtons button`);
  for (let i = 0; i < alert.length; i++) {
    if (alert.eq(i).text().trim() === label) return alert.eq(i);
  }
  return null;
}

function click(page: LegacyGamePage, label: string): void {
  const found = button(page, label);
  if (!found) throw new Error(`no "${label}" button`);
  found.click();
}

function clickCard(page: LegacyGamePage, cardId: string, extra: Record<string, unknown> = {}): void {
  const area = page.$(`.card:cardId(${cardId})`).eq(-1).find('.actionArea');
  if (area.length === 0) throw new Error(`no card ${cardId}`);
  const $ = (page.window as unknown as { $: { Event: (type: string, props: object) => object } }).$;
  area.trigger($.Event('click', { which: 1, ...extra }));
}

function assign(page: LegacyGamePage, minion: string, character: string): void {
  clickCard(page, minion);
  clickCard(page, character);
}

/** Makes a recorded gesture that sends nothing (recording.ts LocalGesture). */
function makeLocalGesture(page: LegacyGamePage, gesture: LocalGesture): void {
  if (gesture.assign) assign(page, ...gesture.assign);
  if (gesture.unassign) {
    clickCard(page, gesture.unassign);
    page.advance(300);
    clickCard(page, gesture.unassign);
  }
  page.advance(1000);
}

/** Makes the gesture that sends `exchange`, the next recorded one the player starts. */
export function makeGesture(
  page: LegacyGamePage,
  server: FakeGameServer,
  index: number,
  exchange: RecordedExchange,
): void {
  if (exchange.path.endsWith('/cardInfo')) {
    // A click anywhere first closes card information that is open (the browser check's scenario clicks the page)
    const ui = (page.window as unknown as { ui: { cardInfoDialog: { isOpen(): boolean } } }).ui;
    if (ui.cardInfoDialog.isOpen()) page.$('body').click();
    clickCard(page, exchange.params['cardId'] ?? '', { shiftKey: true });
    return;
  }
  if (exchange.path.endsWith('/concede')) {
    page.$('#concedeGame').click();
    return;
  }
  if (exchange.path.endsWith('/cancel')) {
    page.$('#cancelGame').click();
    return;
  }
  const decision = decisionBefore(server, index, server.recording.participant);
  if (!decision) throw new Error('an answer with no decision before it');
  const value = exchange.params['decisionValue'] ?? '';
  const ids = (name: string) => decision.values[name] ?? [];
  switch (decision.type) {
    case 'INTEGER':
      page.$('#integerDecision').val(value);
      click(page, 'OK');
      return;
    case 'MULTIPLE_CHOICE': {
      const list = page.$('#multipleChoiceDecision');
      if (list.length > 0) {
        list.val(value);
        click(page, 'OK');
      } else {
        openDialogs(page).find('.ui-dialog-content button').eq(Number(value)).click();
      }
      return;
    }
    case 'CARD_ACTION_CHOICE': {
      if (value === '') {
        click(page, 'Pass');
        return;
      }
      const cardId = ids('cardId')[ids('actionId').indexOf(value)] ?? '';
      clickCard(page, cardId);
      page.advance(200);
      // A card with several actions shows a menu of them
      const item = page.$(`.contextMenu a[href="#${value}"]`);
      if (item.length > 0) item.click();
      return;
    }
    case 'ASSIGN_MINIONS': {
      // "character minion minion,character minion": each minion not assigned yet, then its character
      for (const group of value.split(',').filter(Boolean)) {
        const [character = '', ...minions] = group.split(' ');
        for (const minion of minions) {
          const card = page.$(`.card:cardId(${minion})`).eq(-1).data('card') as { assign?: string } | undefined;
          if (card?.assign !== character) assign(page, minion, character);
        }
      }
      page.answers.push(true);
      click(page, 'Done');
      return;
    }
    default: {
      // Cards to pick (CARD_SELECTION, ARBITRARY_CARDS, ACTION_CHOICE): each, then Done unless that answered it
      const before = server.progress;
      // A choice of actions answers with action ids; its dialog shows each action as a card, temp0, temp1, ...
      const cardIds =
        decision.type === 'ACTION_CHOICE'
          ? value
              .split(',')
              .filter(Boolean)
              .map((actionId) => `temp${ids('actionId').indexOf(actionId)}`)
          : value.split(',').filter(Boolean);
      for (const cardId of cardIds) {
        clickCard(page, cardId);
        page.advance(50);
      }
      page.advance(50);
      if (server.progress === before && button(page, 'Done')) click(page, 'Done');
    }
  }
}

/**
 * Plays the recording on the page up to `exchanges` answered exchanges: lets the page's time pass, and when the next
 * recorded request is one the player starts, makes its gesture. Throws when the page stops short.
 */
export function playUntil(
  page: LegacyGamePage,
  server: FakeGameServer,
  exchanges: number,
  local: LocalGesture[] = [],
): void {
  server.limit = exchanges;
  const pending = [...local];
  const makeDueGestures = () => {
    while (pending.length > 0 && (pending[0]?.after ?? 0) <= server.progress) {
      const gesture = pending.shift();
      if (gesture) makeLocalGesture(page, gesture);
    }
  };
  let gestured = -1;
  for (let round = 0; round < 400 && server.progress < exchanges; round++) {
    page.advance(250);
    makeDueGestures();
    const next = server.next();
    if (
      next &&
      next.index < exchanges &&
      isGesture(next.exchange) &&
      gestured !== next.index &&
      server.problems.length === 0
    ) {
      // The page has taken in everything before it
      page.advance(1000);
      gestured = next.index;
      makeGesture(page, server, next.index, next.exchange);
    }
  }
  if (server.progress < exchanges) {
    const next = server.next();
    throw new Error(
      `stopped after ${server.progress} of ${exchanges} exchanges${next ? `, waiting for ${next.exchange.method} ${next.exchange.path} ${JSON.stringify(next.exchange.params)}` : ''}${server.problems.length ? `: ${server.problems.join('; ')}` : ''}`,
    );
  }
  // Animations and layout finish, then what was done after the step's last exchange
  page.advance(5000);
  page.advance(1000);
  makeDueGestures();
}
