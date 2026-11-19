// The game's phone layout sorts each support row Free Peoples first, then Shadow, by src/generated/card-sides.json,
// which scripts/extract-card-data.mjs makes from the card definitions. This compares it with the server's own sides:
// the side the deck builder's Side filter uses (/collection answers each card with `side`, from its blueprint).
//
//   npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
//   node game/card-sides.mjs                                       compares every card of "All cards"
//   npm run check:game:down
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { session } from '../local-server/seed.mjs';
import { pairOf } from './players.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const LOGIN = path.resolve(HERE, '../.local-server/game/login.json');
const SIDES = path.resolve(HERE, '../../src/generated/card-sides.json');

/** As the page strips a blueprint (Card.StripBlueprintId): foils, alternate images and tengwar are the card itself. */
const strip = (id) =>
  id
    .replace('*', '')
    .replace('^', '')
    .replace(/(_\d+)T/, '$1');

const { server, password } = JSON.parse(fs.readFileSync(LOGIN, 'utf8'));
const shadow = new Set(JSON.parse(fs.readFileSync(SIDES, 'utf8')).shadow);
const { player } = pairOf(0);
const who = await session(server, player, password);
const xml = await (await who.get(`/collection/default?participantId=${player}&filter=&start=0&count=100000`)).text();
const cards = [...xml.matchAll(/<card\b([^>]*)\/?>/g)].map(([, text]) => ({
  id: /blueprintId="([^"]+)"/.exec(text)?.[1],
  side: /side="([^"]+)"/.exec(text)?.[1] ?? null,
}));
const wrong = cards.filter(({ id, side }) => (side === 'SHADOW') !== shadow.has(strip(id)));
const sides = cards.reduce((count, { side }) => ({ ...count, [side ?? 'none']: (count[side ?? 'none'] ?? 0) + 1 }), {});
console.log(`${cards.length} cards on the server: ${JSON.stringify(sides)}`);
for (const { id, side } of wrong.slice(0, 40)) {
  console.log(
    `  ✗ ${id}: the server says ${side ?? 'no side'}, card-sides.json ${shadow.has(strip(id)) ? 'Shadow' : 'not Shadow'}`,
  );
}
console.log(`${cards.length - wrong.length} of ${cards.length} the same side`);
process.exitCode = wrong.length === 0 && cards.length > 0 ? 0 : 1;
