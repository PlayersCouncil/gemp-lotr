// Who may see a player's discard pile depends on the format (LotroFormat.discardPileIsPublic, lotrFormats.hjson): the
// server tells the page at the start of a game (the P event's discardPublic) and sends an opponent's discarded cards
// only when it is public. The page shows an opponent's discard pile only then (game/phone.mjs --checks piles, and the
// page's specs). This plays the start of a game in formats of both kinds on a real server and checks what it says.
//
//   npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
//   node game/discard-formats.mjs
//   npm run check:game:down
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { endGame, foeAnswer, parseGameXml, pendingDecision, startGame } from './protocol.mjs';
import { pairOf } from './players.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const { server, password } = JSON.parse(
  fs.readFileSync(path.resolve(HERE, '../.local-server/game/login.json'), 'utf8'),
);

/**
 * Formats the test deck (Fellowship Block, set 1) may be played in, and whether their discard pile is public. Movie
 * Block and Expanded (false; their PC versions true, in lotrFormats.hjson) don't take its Fellowship Block sites.
 */
const FORMATS = [
  ['fotr_block', false],
  ['pc_fotr_block', true],
];

/**
 * What the server says about the discard pile to one player: the game starts (its P event) once both have answered
 * the first decision (the bid), so each answers whatever waits for it, in turn, until the event comes.
 */
async function discardFlags(game, pair) {
  const sides = [
    { who: game.player, name: pair.player, cn: null, flag: null },
    { who: game.foe, name: pair.foe, cn: null, flag: null },
  ];
  const url = `/game/${game.gameId}`;
  for (const side of sides) {
    const answer = parseGameXml(await (await side.who.get(url)).text());
    side.cn = answer.cn;
    side.events = answer.events;
  }
  for (let round = 0; round < 6 && sides.some((side) => side.flag === null); round++) {
    for (const side of sides) {
      const p = side.events.find((event) => event.type === 'P');
      if (p) side.flag = p.discardPublic;
      if (side.flag !== null) continue;
      const decision = pendingDecision(side.events, side.name);
      const fields = decision
        ? { channelNumber: side.cn, decisionId: decision.id, decisionValue: foeAnswer(decision) }
        : { channelNumber: side.cn };
      const answer = parseGameXml(await (await side.who.post(url, fields)).text());
      side.cn = answer.cn ?? side.cn;
      side.events = answer.events;
    }
  }
  return sides.map((side) => side.flag);
}

let ok = 0;
for (const [format, expected] of FORMATS) {
  const game = await startGame(server, pairOf(0), password, 'main', format);
  try {
    const flags = await discardFlags(game, pairOf(0));
    const right = flags.every((flag) => flag === String(expected));
    if (right) ok++;
    console.log(`${right ? '  ✓' : '  ✗'} ${format}: discardPublic ${flags.join(', ')} (expected ${expected})`);
  } finally {
    await endGame(game);
  }
}
console.log(`${ok} of ${FORMATS.length} formats tell the page whether the discard pile is public as configured`);
process.exitCode = ok === FORMATS.length ? 0 : 1;
