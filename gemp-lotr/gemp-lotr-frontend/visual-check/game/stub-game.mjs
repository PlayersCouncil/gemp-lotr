// The recorded games served to a page without a server (the table check and the phone check): the game's answers up to
// a step, one by one as the recording's server gave them, and the room's chat.
import { STUB_ORIGIN } from '../stub-server.mjs';

export const GAME_ID = '77';
/** How long the stand-in server holds a question for news before answering that there is none. */
const HELD_MS = 8000;

/**
 * The game's answers of the first `count` exchanges, one by one as the server gave them, without their decisions
 * (but the last one's, which is the decision waiting at that step): the first as the page opens the game, each of the others to the page's next question for news.
 */
export function gameUpTo(recording, count, { decisions = true } = {}) {
  const answers = recording.exchanges
    .slice(0, count)
    .filter((exchange) => exchange.path === '/game/<id>' && exchange.status === 200);
  return answers.map((exchange, i) =>
    // The last answer keeps its decision, unless only the table is compared
    i === answers.length - 1 && decisions
      ? exchange.body
      : exchange.body.replace(/<ge\b[^>]*?(?:\/>|>[\s\S]*?<\/ge>)/g, (event) =>
          /\stype="D"/.test(event.slice(0, event.indexOf('>'))) ? '' : event,
        ),
  );
}

/**
 * Answers the game's calls from the recording: the game as it opened, then one recorded answer to each of the page's
 * questions for news, and the room's first answer; later questions are held, then answered with no news.
 */
export async function routeGame(context, recording, count, log, { decisions = true } = {}) {
  const player = recording.exchanges.find((exchange) => exchange.path === '/player');
  const [opening, ...rest] = gameUpTo(recording, count, { decisions });
  // Who is in the room: in every answer of the chat
  const users = `<user>${recording.participant}</user><user>${recording.opponent ?? 'GameFoe1'}</user>`;
  // The legacy chat marks a message naming the player only once it knows the player (GET /player): as the game check
  // does on a real server, the game's first answer waits for that (at most 5s), on both pages
  let playerKnown;
  const known = new Promise((resolve) => (playerKnown = resolve));
  setTimeout(() => playerKnown(), 5000);
  await context.route(`${STUB_ORIGIN}/gemp-lotr-server/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const call = `${request.method()} ${url.pathname.replace('/gemp-lotr-server', '')}`;
    if (call === 'GET /player') {
      await route.fulfill({ contentType: 'application/json; charset=UTF-8', body: player?.body ?? '{}' });
      playerKnown();
      return;
    }
    if (call === `GET /game/${GAME_ID}`) {
      await known;
      return route.fulfill({ contentType: 'application/xml; charset=UTF-8', body: opening });
    }
    if (call === `POST /game/${GAME_ID}` && rest.length > 0) {
      return route.fulfill({ contentType: 'application/xml; charset=UTF-8', body: rest.shift() });
    }
    if (call === `GET /chat/Game${GAME_ID}`) {
      return route.fulfill({
        contentType: 'application/xml; charset=UTF-8',
        body:
          '<?xml version="1.0" encoding="UTF-8"?><chat roomName="Game#">' +
          '<message date="1794571436000" from="System">Welcome to room: Game#</message>' +
          `${users}</chat>`,
      });
    }
    // The long poll and the chat's next question: the server holds them a while, then says nothing happened
    if (call === `POST /game/${GAME_ID}` || call === `POST /chat/Game${GAME_ID}`) {
      const nothing = call.includes('/chat/') ? `<chat>${users}</chat>` : '<update cn="1"/>';
      await new Promise((resolve) => setTimeout(resolve, HELD_MS));
      return route
        .fulfill({
          contentType: 'application/xml; charset=UTF-8',
          body: `<?xml version="1.0" encoding="UTF-8"?>${nothing}`,
        })
        .catch(() => undefined);
    }
    log.push(`unanswered ${call}`);
    return route.fulfill({ status: 404, body: '' });
  });
  return () => rest.length;
}

export function inPage(page, fn, arg) {
  return page.evaluate(([source, arg]) => new Function(`return (${source})`)()(arg), [fn.toString(), arg]);
}

/** Waits until the page has drawn what it has and stands still. */
export async function settled(page, waiting = () => 0) {
  // Every picture has loaded, nothing moves, no answer is still to come, and nothing has changed for a while
  let last = '';
  let same = 0;
  for (let i = 0; i < 200 && same < 4; i++) {
    await page.waitForTimeout(150);
    const now = await page.evaluate(() => {
      const loaded =
        Array.from(document.images).every((img) => img.complete) &&
        document.getAnimations().length === 0 &&
        !document.querySelector('[data-animating]');
      return loaded ? document.body.innerHTML.length + ':' + document.querySelectorAll('img').length : '';
    });
    same = now !== '' && now === last && waiting() === 0 ? same + 1 : 0;
    last = now;
  }
}

/**
 * A recording up to `count` exchanges with the game's answers made one: the opening state with every later answer's
 * events after its own (a decision only from the last, as gameUpTo keeps it), as the server sends a page that opens
 * the game again.
 */
export function asOneState(recording, count) {
  const exchanges = recording.exchanges.slice(0, count);
  const games = exchanges.flatMap((exchange, i) =>
    exchange.path === '/game/<id>' && exchange.status === 200 ? [i] : [],
  );
  const last = games.at(-1);
  const withoutDecisions = (body) =>
    body.replace(/<ge\b[^>]*?(?:\/>|>[\s\S]*?<\/ge>)/g, (event) =>
      /\stype="D"/.test(event.slice(0, event.indexOf('>'))) ? '' : event,
    );
  const inner = (body) =>
    body.replace(/^[\s\S]*?<(?:gameState|update)\b[^>]*>/, '').replace(/<\/(?:gameState|update)>\s*$/, '');
  const [first, ...rest] = games;
  const events = rest.map((i) => (i === last ? inner(exchanges[i].body) : withoutDecisions(inner(exchanges[i].body))));
  const opening = first === last ? exchanges[first].body : withoutDecisions(exchanges[first].body);
  const merged = opening.replace(/<\/gameState>\s*$/, `${events.join('')}</gameState>`);
  return {
    ...recording,
    exchanges: exchanges
      .filter((_, i) => !rest.includes(i))
      .map((exchange, i) => (i === first ? { ...exchange, body: merged } : exchange)),
  };
}
