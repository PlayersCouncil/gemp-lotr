// The test data of the throwaway server (../local-server.mjs). Everything but the two players goes in through the
// server's own requests, so it is stored exactly as the server stores it.

import { createHash } from 'node:crypto';

/** The first one is the player the check logs in as; an admin, to be able to add the test data. */
export const TEST_PLAYERS = [
  { name: 'VisualCheck', type: 'aut' },
  // Decks of this player are the library
  { name: 'Librarian', type: 'aut' },
];

/** In "My cards": packs and a selection pack to open, a foil, an errata card and a promised prize. */
const MY_CARDS = [
  '3xFotR - Booster',
  '2x(S)FotR - Starter',
  '4x1_12',
  '4x1_13',
  '2x1_2*',
  '2x51_2',
  '1x404_1',
  '1x1_290',
  '1x1_2',
  '1x1_319',
];
const TROPHIES = ['1x1_13', '1x1_17'];

const BOOT_TIMEOUT_MS = 5 * 60 * 1000;

/** The players, for the database's first start: only the password's hash is written (DbPlayerDAO.encodePassword). */
export function playersSql(password) {
  const hash = createHash('sha256').update(password, 'utf8').digest('hex');
  const rows = TEST_PLAYERS.map(
    ({ name, type }) => `('${name}', '${hash}', '${type}', 20170101, '127.0.0.1', '127.0.0.1')`,
  );
  return `USE gemp_db;\nINSERT INTO player (name, password, type, last_login_reward, last_ip, create_ip) VALUES\n${rows.join(',\n')};\n`;
}

/** Waits until the server answers a login, one attempt at a time (see "One login for all pages" in README.md). */
export async function waitForLogin(server, login, password) {
  const deadline = Date.now() + BOOT_TIMEOUT_MS;
  let last = 'no answer';
  while (Date.now() < deadline) {
    try {
      const response = await fetch(`${server}/gemp-lotr-server/login`, {
        method: 'POST',
        headers: { 'content-type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ login, password }),
      });
      if (response.ok) {
        return;
      }
      last = `HTTP ${response.status}`;
    } catch (error) {
      last = error.cause?.code ?? error.message;
    }
    await new Promise((resolve) => setTimeout(resolve, 2000));
  }
  throw new Error(`The server did not start in time (last answer: ${last})`);
}

const RACE_LEAGUE = 'Visual check race';
const RACE_PATH_LENGTH = 5;

const LIBRARY_DECKS = [
  {
    deckName: 'Library deck one',
    targetFormat: 'fotr_block',
    notes: 'A library deck of the visual check',
    deckContents: '1_290|1_2|1_319,1_327,1_331,1_337,1_343,1_349,1_354,1_356,1_360|1_12,1_12,1_13,1_17',
  },
  {
    deckName: 'Library deck two',
    targetFormat: 'fotr_block',
    notes: '',
    deckContents: '1_290|1_2|1_319,1_327,1_331,1_337,1_343,1_349,1_354,1_356,1_360|1_30,1_45,1_50',
  },
];

function formBody(fields) {
  const body = new URLSearchParams();
  for (const [name, value] of Object.entries(fields)) {
    for (const item of Array.isArray(value) ? value : [value]) {
      body.append(name, item);
    }
  }
  return body;
}

/** Logged in as one player, one request at a time. */
async function session(server, login, password) {
  const post = (url, fields, cookie) =>
    fetch(`${server}/gemp-lotr-server${url}`, {
      method: 'POST',
      headers: { 'content-type': 'application/x-www-form-urlencoded', ...(cookie ? { cookie } : {}) },
      body: formBody(fields),
    });
  const answer = await post('/login', { login, password });
  if (!answer.ok) {
    throw new Error(`Login as ${login} failed: HTTP ${answer.status}`);
  }
  const cookie = answer.headers
    .getSetCookie()
    .map((value) => value.split(';')[0])
    .join('; ');
  const ok = async (response, what) => {
    if (!response.ok) {
      throw new Error(`${what}: HTTP ${response.status} ${(await response.text()).slice(0, 200)}`);
    }
    return response;
  };
  return {
    post: async (url, fields) => ok(await post(url, fields, cookie), `POST ${url}`),
    get: async (url) => ok(await fetch(`${server}/gemp-lotr-server${url}`, { headers: { cookie } }), `GET ${url}`),
  };
}

export async function seedTestData(server, password) {
  const [player, librarian] = TEST_PLAYERS;
  const admin = await session(server, player.name, password);
  const addItems = (collectionType, items) =>
    admin.post('/admin/addItems', { players: player.name, product: items.join('\n'), collectionType });
  await addItems('permanent', MY_CARDS);
  await addItems('trophy', TROPHIES);

  const library = await session(server, librarian.name, password);
  for (const deck of LIBRARY_DECKS) {
    await library.post('/deck', deck);
  }

  await joinRaceLeague(admin);
}

/** A running Race to Mount Doom league with the test player in it, with the same path on every run. */
async function joinRaceLeague(admin) {
  const options = await (await admin.get('/admin/rtmdModifiers')).json();
  const firstIds = (cards) =>
    cards
      .map((card) => card.blueprintId)
      .sort()
      .slice(0, RACE_PATH_LENGTH);
  const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().slice(0, 10).replaceAll('-', '');
  await admin.post('/admin/addRTMDLeague', {
    name: RACE_LEAGUE,
    description: 'The Race to Mount Doom league of the visual check',
    cost: '0',
    start: yesterday,
    maxRepeatMatches: '1',
    'format[]': ['fotr_block'],
    'serieDuration[]': ['30'],
    'maxMatches[]': ['10'],
    'racePath[]': firstIds(options.modifiers),
    'raceVisualPath[]': firstIds(options.visualCards),
    raceCumulative: 'false',
    raceAdvancementMode: 'WIN',
    raceAdvanceFactor: '1',
  });

  const leagueOf = async () => {
    const xml = await (await admin.get('/league')).text();
    return xml.match(/<league\b[^>]*>/g)?.find((tag) => tag.includes(`name="${RACE_LEAGUE}"`));
  };
  const code = (await leagueOf())?.match(/\bcode="([^"]+)"/)?.[1];
  if (!code) {
    throw new Error('The Race to Mount Doom league was not created');
  }
  await admin.post(`/league/${code}`, {});
  const joined = await leagueOf();
  if (!joined?.includes('member="true"') || !joined.includes('type="RTMD"')) {
    throw new Error(`The test player did not join the Race to Mount Doom league: ${joined}`);
  }
}
