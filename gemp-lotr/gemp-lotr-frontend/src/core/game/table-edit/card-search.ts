import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GempHttp } from '../../api/gemp-http.service';
import { listOf, stringsOf } from '../../api/json';
import { BlockFilter, GempFormat } from '../../api/models';
import { CardFilterModel, CardFilterPager, FilterFieldName } from '../../card-filter/card-filter-model';
import { blockOptions, setOptions } from '../../card-filter/filter-block-sets';
import { hiddenCultureGroups } from '../../card-filter/filter-cultures';
import { EMPTY_FILTER, FilterFields, resetAdvanced, resetAll } from '../../card-filter/filter-fields';
import { SelectOption } from '../../card-filter/filter-options';
import { buildFilterString } from '../../card-filter/filter-string';
import { FormatsService } from '../../formats/formats.service';
import { LocalStore } from '../../platform/local-store';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { storedList } from '../../platform/session-store';

/** A card found (`GET scenarios/search`). */
export interface FoundCard {
  blueprintId: string;
  name: string;
  /** The card type in capitals, as the server gives it (and the state): COMPANION, MINION, SITE... */
  type: string;
  culture: string | null;
  site: boolean;
}

export interface SearchPage {
  total: number;
  start: number;
  cards: readonly FoundCard[];
}

/**
 * A search held inside a bound (a place's card types; a puzzle slot's filter and its puzzle's pool): shown, never
 * changed. The server searches with the bound and the pool each by itself and gives only the cards that the person's
 * own filter finds too (`GET scenarios/search` with `bound=` and `pool=`), so nothing the person sets can widen it.
 */
export interface SearchBound {
  /** The bound, in the deck builder's filter language (`bound=`; empty: none). */
  filter: string;
  label: string;
  /** A puzzle's pool, searched with by itself as well (`pool=`); absent: none. */
  pool?: string | null;
  /** Always part of the person's own filter (a puzzle slot's kinds of card and site number: the place's, not its bound). */
  with?: string;
}

/** The bound for its title, a line each: the pool, the bound and what is always searched with, as filter text. */
export function boundText(bound: SearchBound): string {
  const lines: [string, string | null | undefined][] = [
    ['Pool', bound.pool],
    ['Bound', bound.filter],
    ['Place', bound.with],
  ];
  return lines
    .filter(([, text]) => (text ?? '').trim() !== '')
    .map(([name, text]) => `${name}: ${(text ?? '').trim()}`)
    .join('\n');
}

/** Where the search's last filters are remembered, on this device (a convenience: the page works without it). */
export const SEARCH_KEY = 'gemp-edit-search';
/** The fields the search has no use for (every card of the library once: no products, no rarities). */
export const SEARCH_OMITS: readonly FilterFieldName[] = ['product', 'rarity'];
/** How many cards a page of results holds: at first, at least, at most. */
export const SEARCH_PAGE_SIZES = { min: 6, max: 48, start: 18 } as const;

type Json = Record<string, unknown>;
const object = (value: unknown): Json =>
  typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as Json) : {};

/** Every block of every format once (its sets joined), for the Block and Set lists of a search over every card. */
export function allBlocks(formats: Readonly<Record<string, GempFormat>>): BlockFilter[] {
  const blocks = new Map<string, BlockFilter>();
  for (const format of Object.values(formats)) {
    for (const block of format.blockFilters ?? []) {
      const known = blocks.get(block.filter);
      blocks.set(block.filter, {
        filter: block.filter,
        name: known?.name ?? block.name,
        setFilters: { ...known?.setFilters, ...block.setFilters },
      });
    }
  }
  return [...blocks.values()];
}

/** The filter fields remembered, each checked against the empty filter's (anything else is left as it starts). */
export function readFields(value: unknown): FilterFields {
  const json = object(value);
  // (every block and set at first: the search is over the whole library)
  const fields: Record<string, unknown> = { ...EMPTY_FILTER, block: '', set: '' };
  for (const [name, start] of Object.entries(EMPTY_FILTER) as [string, unknown][]) {
    const stored = json[name];
    if (typeof start === 'string' && typeof stored === 'string') fields[name] = stored;
    const codes = storedList(stored);
    if (Array.isArray(start) && codes !== null) fields[name] = stringsOf(codes);
  }
  for (const name of SEARCH_OMITS) fields[name] = '';
  return fields as unknown as FilterFields;
}

/** The keys a filter text says, in lower case (as the deck builder's search reads them). */
function keysOf(text: string | null | undefined): string[] {
  return (text ?? '').split(/\s+/).map((part) => part.split(':')[0]?.toLowerCase() ?? '');
}

/**
 * The fields of the person's filter that are not theirs under a bound: every field that what is always searched with
 * (`with`) names, since the deck builder's search reads a key given twice as either of the two, or only the first, so
 * the person's own value must never be sent beside it; and the card type (and a site's number) where the bound or the
 * pool fixes it, where a select offering others would only confuse. Anything else the person may set: the server
 * keeps the search inside the bound whatever it is.
 */
export function boundFields(bound: SearchBound | null): FilterFieldName[] {
  const keys = keysOf(bound?.with);
  const fixed = [...keysOf(bound?.filter), ...keysOf(bound?.pool)];
  const names: FilterFieldName[] = [];
  if (keys.includes('cardtype') || fixed.includes('cardtype')) names.push('cardType');
  if (keys.includes('side')) names.push('side');
  if (keys.includes('set')) names.push('set', 'block');
  if (keys.includes('block')) names.push('block', 'set');
  for (const [key, fields] of BOUND_KEYS) if (keys.includes(key)) names.push(...fields);
  if (fixed.includes('sitenumber') && !names.includes('siteNumberCompare')) names.push(...SITE_NUMBER);
  return names;
}

const SITE_NUMBER: readonly FilterFieldName[] = ['siteNumberCompare', 'siteNumberValue'];

/** The fields of the filter each other key of the deck builder's search sets (keys in lower case, as the server reads them). */
const BOUND_KEYS: readonly (readonly [string, readonly FilterFieldName[]])[] = [
  ['name', ['name']],
  ['gametext', ['gametext']],
  ['keyword', ['keyword']],
  ['phase', ['phase']],
  ['race', ['race']],
  ['itemclass', ['itemClass']],
  ['signet', ['signet']],
  ...(['twilight', 'strength', 'vitality', 'resistance', 'siteNumber'] as const).flatMap((stat) =>
    [stat.toLowerCase(), `${stat.toLowerCase()}compare`].map(
      (key) => [key, [`${stat}Compare`, `${stat}Value`] as FilterFieldName[]] as const,
    ),
  ),
];

/** Whether what is always searched with names cultures (the person's culture buttons are then not sent, nor shown). */
function boundCultures(bound: SearchBound | null): boolean {
  return /(^|\s)culture:/i.test(bound?.with ?? '');
}

/** What the filter panel leaves out for a bound (by its fields' `data-field` names): every field the bound sets. */
export function boundOmits(bound: SearchBound | null): string[] {
  const fields = boundFields(bound);
  const stats = fields.filter((name) => name.endsWith('Compare')).map((name) => name.replace(/Compare$/, ''));
  return [...new Set([...fields, ...stats, ...(boundCultures(bound) ? ['cultures'] : [])])];
}

/** The person's own filter, as the deck builder's filter string (with what the bound always searches with). */
export function searchFilter(fields: FilterFields, bound: SearchBound | null): string {
  const cleared: Partial<Record<FilterFieldName, string>> = {};
  for (const name of [...SEARCH_OMITS, ...boundFields(bound)]) cleared[name] = '';
  return buildFilterString(
    { ...fields, ...cleared, ...(boundCultures(bound) ? { cultures: [] } : {}) },
    { format: null, override: bound?.with ?? '', siteOverride: false, cardsInDeck: () => [] },
  );
}

/** A page of cards found, read defensively. */
export function readSearchPage(value: unknown): SearchPage {
  const json = object(value);
  const cards = listOf(json['cards']);
  return {
    total: typeof json['total'] === 'number' ? json['total'] : 0,
    start: typeof json['start'] === 'number' ? json['start'] : 0,
    cards: cards.flatMap((item: unknown) => {
      const card = object(item);
      const blueprintId = card['blueprintId'];
      if (typeof blueprintId !== 'string') return [];
      return [
        {
          blueprintId,
          name: typeof card['name'] === 'string' ? card['name'] : blueprintId,
          type: typeof card['type'] === 'string' ? card['type'] : '',
          culture: typeof card['culture'] === 'string' ? card['culture'] : null,
          site: card['site'] === true,
        },
      ];
    }),
  };
}

/** `GET scenarios/search`: cards of the whole library found with a deck builder's filter. */
@Injectable()
export class CardSearchApi {
  private readonly http = inject(GempHttp);

  /** The cards the filter finds, within the bound and the pool if given (each searched with by itself). */
  search(filter: string, start: number, count: number, bound: SearchBound | null = null): Observable<SearchPage> {
    // (an empty bound or pool is not sent: the server would read it as one that finds nothing)
    const within = {
      ...(bound?.filter.trim() ? { bound: bound.filter.trim() } : {}),
      ...(bound?.pool?.trim() ? { pool: bound.pool.trim() } : {}),
    };
    return this.http
      .getJson<unknown>('/scenarios/search', { filter, start: String(start), count: String(count), ...within })
      .pipe(map(readSearchPage));
  }
}

/**
 * The card search of the on-table edit controls (the search window): the deck builder's filter fields over every card
 * of the library (`GET scenarios/search`), page by page. It is the shared filter panel's model and pager
 * (CardFilterModel, CardFilterPager), so the panel is the deck builder's own. The last filters are remembered for this
 * viewer (LocalStore); a bound (a place's card types, a puzzle slot's bound) narrows every search and is not one of
 * them. The Block and Set lists hold every block of every format. Page-scoped.
 */
@Injectable()
export class CardSearch implements CardFilterModel, CardFilterPager {
  private readonly api = inject(CardSearchApi);
  private readonly store = inject(LocalStore);
  private readonly formats = inject(FormatsService);
  private readonly lifetime = inject(PageLifetime);

  readonly fields = signal<FilterFields>(readFields(this.store.read(SEARCH_KEY)));
  private readonly blocks = signal<readonly BlockFilter[] | null>(null);
  readonly blockOptions = computed<readonly SelectOption[]>(() => blockOptions(this.blocks() ?? []).options);
  readonly setOptions = computed<readonly SelectOption[]>(
    () => setOptions(this.blocks() ?? [], this.fields().block).options,
  );
  readonly hiddenCultureGroups = computed(() =>
    this.blocks() === null ? new Set<never>() : hiddenCultureGroups(this.blocks()),
  );
  readonly isPackProduct = signal(false).asReadonly();

  /** The bound of the search now open, or null. */
  readonly bound = signal<SearchBound | null>(null);
  readonly page = signal<SearchPage | null>(null);
  readonly loading = signal(false);
  private readonly start = signal(0);
  private readonly count = signal<number>(SEARCH_PAGE_SIZES.start);
  readonly pageSizes = SEARCH_PAGE_SIZES;
  readonly previousDisabled = computed(() => this.loading() || this.start() === 0);
  readonly nextDisabled = computed(() => this.loading() || this.start() + this.count() >= (this.page()?.total ?? 0));
  readonly sliderDisabled = computed(() => this.page() === null);
  /** The filter string searched with. */
  readonly filter = computed(() => searchFilter(this.fields(), this.bound()));
  /** The fields the panel leaves out: those the search has no use for, and those the bound sets. */
  readonly omitted = computed<readonly string[]>(() => [...SEARCH_OMITS, ...boundOmits(this.bound())]);

  private asked = 0;

  /** Opens a search (within a bound, if any): the remembered filters, from the first page. */
  open(bound: SearchBound | null = null): void {
    this.bound.set(bound);
    this.start.set(0);
    if (this.blocks() === null) {
      this.lifetime.read(this.formats.formats$, {
        next: (formats) => this.blocks.set(allBlocks(formats)),
        error: () => this.blocks.set([]),
      });
    }
    this.load();
  }

  setField(field: FilterFieldName, value: string): void {
    this.fields.update((fields) => {
      const changed = { ...fields, [field]: value };
      // A block chosen starts its sets from "Any"; a comparison back to "Any" clears its number
      if (field === 'block') changed.set = '';
      if (field.endsWith('Compare') && value === '')
        changed[field.replace(/Compare$/, 'Value') as FilterFieldName] = '';
      return changed;
    });
    this.changed();
  }

  toggleCulture(code: string): void {
    this.fields.update((fields) => ({
      ...fields,
      cultures: fields.cultures.includes(code) ? fields.cultures.filter((c) => c !== code) : [...fields.cultures, code],
    }));
    this.changed();
  }

  resetAdvanced(): void {
    this.fields.update(resetAdvanced);
    this.changed();
  }

  /** "Clear filters": every field as it starts. */
  resetAll(): void {
    this.fields.set({ ...resetAll(this.fields()), block: '', set: '' });
    this.changed();
  }

  previousPage(): void {
    if (this.previousDisabled()) return;
    this.start.set(Math.max(0, this.start() - this.count()));
    this.load();
  }

  nextPage(): void {
    if (this.nextDisabled()) return;
    this.start.set(this.start() + this.count());
    this.load();
  }

  setPageSize(count: number): void {
    this.count.set(Math.min(SEARCH_PAGE_SIZES.max, Math.max(SEARCH_PAGE_SIZES.min, Math.round(count))));
    this.load();
  }

  private changed(): void {
    this.store.write(SEARCH_KEY, this.fields());
    this.start.set(0);
    this.load();
  }

  private load(): void {
    const asked = ++this.asked;
    this.loading.set(true);
    this.lifetime.read(this.api.search(this.filter(), this.start(), this.count(), this.bound()), {
      next: (page) => {
        if (asked !== this.asked) return;
        this.loading.set(false);
        this.page.set(page);
      },
      error: () => {
        if (asked !== this.asked) return;
        this.loading.set(false);
        this.page.set({ total: 0, start: 0, cards: [] });
      },
    });
  }
}
