/**
 * Test-only: a small Gemp server kept in memory, for running the legacy page and the Angular page against the same
 * answers (src/parity). It keeps the saved decks, answers the collection from a fixed list of cards and records every
 * request the way the comparison reads it.
 */
import { readServerResponse } from '../files';

export interface ServerRequest {
  method: string;
  /** The address after /gemp-lotr-server, without the query. */
  path: string;
  params: Record<string, string>;
}

export interface ServerAnswer {
  status: number;
  body: string;
  contentType: string;
}

interface SavedDeck {
  targetFormat: string;
  notes: string;
  contents: string;
}

interface CollectionCard {
  id: string;
  count: number;
  side: 'FREE_PEOPLE' | 'SHADOW' | null;
}

const XML = 'application/xml; charset=UTF-8';
const xml = (body: string): ServerAnswer => ({
  status: 200,
  body: `<?xml version="1.0" encoding="UTF-8"?>${body}`,
  contentType: XML,
});
const failed = (status: number): ServerAnswer => ({ status, body: '', contentType: 'text/plain' });

const escapeXml = (text: string) =>
  text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

/** The first cards of set 1: the Ring, a Ring-bearer, then cards of both sides. */
const CARDS: readonly CollectionCard[] = Array.from({ length: 60 }, (_, i) => ({
  id: `1_${i + 1}`,
  count: (i % 4) + 1,
  side: i % 3 === 0 ? 'SHADOW' : 'FREE_PEOPLE',
}));
/** Set 1's sites, which a search for sites finds. */
const SITES: readonly CollectionCard[] = Array.from({ length: 24 }, (_, i) => ({
  id: `1_${319 + i}`,
  count: 1,
  side: null,
}));

const FORMAT_NAMES: Record<string, string> = { fotr_block: 'Fellowship Block', pc_movie: 'Movie Block (PC)' };

/** What a request asks, without what never matters: the cache-busting `_` and the participant. */
export function describeRequest({ method, path, params }: ServerRequest): string {
  const kept = Object.entries(params)
    .filter(([name]) => name !== '_' && name !== 'participantId')
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([name, value]) => `${name}=${value}`);
  return `${method} ${path}${kept.length ? ` ${kept.join('&')}` : ''}`;
}

/** How a page writes a missing value: applied to a request's parameters and to a saved deck before they are read. */
export type Writing = (values: Record<string, string>) => Record<string, string>;

/** A request the server answers with an error status: by its method and path (`*` for any method). */
export interface Failure {
  method: 'GET' | 'POST' | '*';
  path: string;
  status: number;
}

export class FakeGempServer {
  /** Every request, in the order it arrived. */
  readonly received: ServerRequest[] = [];
  readonly decks = new Map<string, SavedDeck>();
  /** Requests answered with an error, until removed. */
  readonly failures: Failure[] = [];
  /**
   * The packs at the start of every collection: a booster and a selection pack, and how many of each are left.
   * Empty unless a test adds them.
   */
  readonly packs = new Map<string, { count: number; contents?: string }>();
  /** What the next GET /delivery/packContents gives: set by opening a pack, consumed by fetching it. */
  private delivery: string | null = null;

  /** Adds a booster and a selection pack (of three cards) to every collection. */
  withPacks(): this {
    this.packs.set('FotR - Booster', { count: 2 });
    this.packs.set('(S)FotR - Starter', { count: 1, contents: '1_1|1_2|1_3' });
    return this;
  }

  constructor() {
    this.decks.set('Fellowship', {
      targetFormat: 'fotr_block',
      notes: 'Notes with + plus and a "quote"',
      contents: '1_2|1_1|1_319,1_327,1_331|1_7,1_12,1_131,1_131,1_3',
    });
    this.decks.set('Test + deck & co', { targetFormat: 'pc_movie', notes: '', contents: '|||1_5,1_6' });
  }

  answer(request: ServerRequest): ServerAnswer {
    this.received.push(request);
    const { method, path, params } = request;
    const post = method === 'POST';
    const failure = this.failures.find((f) => f.path === path && (f.method === '*' || f.method === method));
    if (failure) {
      return failed(failure.status);
    }
    switch (path) {
      case '/deck/formats':
        return { status: 200, body: readServerResponse('formats.json'), contentType: 'application/json' };
      case '/deck/stats':
        return { status: 200, body: this.stats(params['deckContents'] ?? ''), contentType: 'text/html; charset=UTF-8' };
      case '/deck/list':
        return xml(this.deckList());
      case '/deck/libraryList':
        return xml('<decks><deck targetFormat="Fellowship Block">Starter</deck></decks>');
      case '/deck/library':
        return xml(this.deckXml({ targetFormat: 'fotr_block', notes: '', contents: '1_2|1_1||1_8,1_9' }));
      case '/deck/share':
        return {
          status: 200,
          body: `http://gemp.test/share/deck?id=${encodeURIComponent(params['deckName'] ?? '')}`,
          contentType: 'text/plain',
        };
      case '/deck/rename':
        return this.rename(params['oldDeckName'] ?? '', params['deckName'] ?? '');
      case '/deck/delete':
        this.decks.delete(params['deckName'] ?? '');
        return xml('<ok/>');
      case '/deck/convert':
        return xml(
          this.deckXml({
            targetFormat: params['targetFormat'] ?? '',
            notes: '',
            contents: params['deckContents'] ?? '',
          }),
        );
      case '/deck':
        return post ? this.save(params) : this.open(params['deckName'] ?? '');
      case '/collection':
        return xml(readServerResponse('collection-types.xml').replace(/^<\?xml[^>]*\?>/, ''));
      case '/collection/import/':
        return xml(this.imported(params['decklist'] ?? ''));
      case '/league':
        return xml('<leagues/>');
      case '/delivery/packContents': {
        const delivery = this.delivery;
        this.delivery = null;
        return delivery ? xml(delivery) : failed(404);
      }
      default:
        if (!path.startsWith('/collection/')) {
          return failed(404);
        }
        return post ? this.openPack(path.slice('/collection/'.length), params) : xml(this.collection(params));
    }
  }

  private openPack(collectionType: string, params: Record<string, string>): ServerAnswer {
    const id = params['pack'] ?? '';
    const pack = this.packs.get(id);
    const selection = params['selection'];
    if (!pack || pack.count === 0 || (selection !== undefined && !pack.contents?.split('|').includes(selection))) {
      return failed(404);
    }
    pack.count--;
    const name = collectionType === 'default' ? 'permanent' : collectionType;
    const cards = selection !== undefined ? [selection] : ['1_12', '1_13'];
    this.delivery =
      `<delivery><collectionType name="${name}">` +
      cards.map((card) => `<card blueprintId="${card}" count="1"/>`).join('') +
      '</collectionType></delivery>';
    return xml('<ok/>');
  }

  /** Packs first (not in a search for sites), then the cards, as the real server lists a collection. */
  private collection(params: Record<string, string>): string {
    const start = Number(params['start'] ?? 0);
    const count = Number(params['count'] ?? 18);
    const sites = /cardType:SITE/.test(params['filter'] ?? '');
    const packs = sites
      ? []
      : [...this.packs.entries()]
          .filter(([, pack]) => pack.count > 0)
          .map(([id, pack]) => {
            const contents = pack.contents ? ` contents="${pack.contents}"` : '';
            return `<pack blueprintId="${escapeXml(id)}" count="${pack.count}"${contents}/>`;
          });
    const cards = (sites ? SITES : CARDS).map((card) => {
      const side = card.side ? ` side="${card.side}"` : '';
      return `<card blueprintId="${card.id}" count="${card.count}"${side}/>`;
    });
    const items = [...packs, ...cards];
    return `<collection count="${items.length}">${items.slice(start, start + count).join('')}</collection>`;
  }

  /**
   * The cleaned decklist the pages send: its lines end with "~". A line that starts with a number (after an optional
   * count, e.g. "2x 12") is that card of set 1; anything else isn't recognised.
   */
  private imported(decklist: string): string {
    const cards = decklist
      .split('~')
      .map((line) => /^\s*(?:(\d+)x?\s+)?(\d+)\b/.exec(line))
      .filter((found): found is RegExpExecArray => found !== null)
      .map(([, count = '1', number]) => {
        const n = Number(number);
        const place =
          n === 1
            ? ' group="ring"'
            : n === 2
              ? ' group="ringBearer"'
              : n >= 319
                ? ''
                : ` side="${n % 3 === 1 ? 'SHADOW' : 'FREE_PEOPLE'}"`;
        return `<card blueprintId="1_${n}" count="${count}"${place}/>`;
      });
    return `<collection count="${cards.length}">${cards.join('')}</collection>`;
  }

  private stats(contents: string): string {
    const cards = (contents.split('|')[3] ?? '').split(',').filter(Boolean);
    return `<b>Free People</b>: ${cards.length}, <b>Shadow</b>: 0<br/><b>Format</b>: <font color='green'>valid</font>`;
  }

  private deckList(): string {
    const decks = [...this.decks.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(
        ([name, deck]) =>
          `<deck targetFormat="${FORMAT_NAMES[deck.targetFormat] ?? deck.targetFormat}">${escapeXml(name)}</deck>`,
      );
    return `<decks>${decks.join('')}</decks>`;
  }

  private deckXml(deck: SavedDeck): string {
    const [ringBearer = '', ring = '', sites = '', draw = ''] = deck.contents.split('|');
    const side = (id: string) => CARDS.find((card) => card.id === id)?.side ?? 'FREE_PEOPLE';
    return (
      `<deck><targetFormat formatCode="${deck.targetFormat}" formatName="${FORMAT_NAMES[deck.targetFormat] ?? deck.targetFormat}"/>` +
      `<notes>${escapeXml(deck.notes)}</notes>` +
      (ringBearer ? `<ringBearer blueprintId="${ringBearer}"/>` : '') +
      (ring ? `<ring blueprintId="${ring}"/>` : '') +
      sites
        .split(',')
        .filter(Boolean)
        .map((id) => `<site blueprintId="${id}"/>`)
        .join('') +
      draw
        .split(',')
        .filter(Boolean)
        .map((id) => `<card blueprintId="${id}" side="${side(id)}"/>`)
        .join('') +
      '</deck>'
    );
  }

  private open(name: string): ServerAnswer {
    const deck = this.decks.get(name);
    return deck ? xml(this.deckXml(deck)) : xml('<deck/>');
  }

  private save(params: Record<string, string>): ServerAnswer {
    const deck: SavedDeck = {
      targetFormat: params['targetFormat'] ?? '',
      notes: params['notes'] ?? '',
      contents: params['deckContents'] ?? '',
    };
    this.decks.set(params['deckName'] ?? '', deck);
    return xml(this.deckXml(deck));
  }

  private rename(oldName: string, name: string): ServerAnswer {
    const deck = this.decks.get(oldName);
    if (!deck) {
      return failed(404);
    }
    this.decks.delete(oldName);
    this.decks.set(name, deck);
    return xml(this.deckXml(deck));
  }

  /** Every request, in the order it arrived, as text (describeRequest). */
  requests(writing: Writing = (values) => values): string[] {
    return this.received.map((request) => describeRequest({ ...request, params: writing(request.params) }));
  }

  /** The saved decks, for comparing what two pages left on the server. */
  saved(writing: Writing = (values) => values): string {
    const decks = [...this.decks.entries()].sort(([a], [b]) => a.localeCompare(b));
    return JSON.stringify(decks.map(([name, deck]) => [name, writing({ ...deck })]));
  }
}
