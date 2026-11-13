import { ALL_CULTURES } from './filter-cultures';
import { FilterFields, NumericFilter } from './filter-fields';

export interface FilterContext {
  /** Format code of the deck (format:...); null before the formats are loaded. */
  format: string | null;
  /** The deck builder's predefined filter (e.g. "cardType:-THE_ONE_RING"), appended last. */
  override: string;
  /** RTMD meta-site allowing other sites: sent with site searches. */
  siteOverride: boolean;
  /** Bare blueprint IDs of the cards in the deck, for "Special in Deck". */
  cardsInDeck: () => readonly string[];
}

/**
 * The filter string for the server. A field the predefined filter already sets is left out. Multi-word game text has
 * every space turned into "_".
 */
export function buildFilterString(fields: FilterFields, context: FilterContext): string {
  const override = context.override;
  const unlessOverridden = (key: string, value: string, render = (v: string) => ` ${key}:${v}`) =>
    value !== '' && !override.includes(`${key}:`) ? render(value) : '';

  const culture =
    fields.cultures.length > 0 && !override.includes('culture:')
      ? ` culture:${ALL_CULTURES.filter((code) => fields.cultures.includes(code)).join(',')}`
      : '';
  const product = unlessOverridden('product', fields.product);
  const rarity = unlessOverridden('rarity', fields.rarity);
  const side = unlessOverridden('side', fields.side);
  const sort = unlessOverridden('sort', fields.sort);
  const format = context.format ? ` format:${context.format}` : '';
  const block = fields.block ? ` block:${fields.block}` : '';
  const set = fields.set ? ` set:${fields.set}` : '';
  const cardType = fields.cardType !== '' ? ` cardType:${fields.cardType}` : '';
  const name =
    fields.name.trim() !== '' && !override.includes('name:')
      ? fields.name
          .split(' ')
          .map((word) => ` name:${word}`)
          .join('')
      : '';
  const gametext = unlessOverridden('gametext', fields.gametext, (v) => ` gametext:${v.replaceAll(' ', '_')}`);
  const keyword = unlessOverridden('keyword', fields.keyword);
  const phase = unlessOverridden('phase', fields.phase);
  const race = unlessOverridden('race', fields.race);
  const itemClass = unlessOverridden('itemClass', fields.itemClass);
  const numeric = (stat: NumericFilter) => {
    const compare = fields[`${stat}Compare`];
    const value = fields[`${stat}Value`];
    return compare !== '' && value !== '' && !override.includes(`${stat}:`)
      ? ` ${stat}Compare:${compare} ${stat}:${value}`
      : '';
  };
  const signet = fields.signet !== '' ? ` signet:${fields.signet}` : '';
  const siteOverride = context.siteOverride && cardType.includes('SITE') ? ' siteOverride:true' : '';
  const blueprints = product === ' product:specialForDeck' ? ` blueprints:${context.cardsInDeck().join(',')}` : '';

  return (
    side +
    format +
    block +
    set +
    cardType +
    rarity +
    sort +
    product +
    culture +
    name +
    gametext +
    keyword +
    phase +
    race +
    itemClass +
    numeric('twilight') +
    numeric('strength') +
    numeric('vitality') +
    numeric('resistance') +
    numeric('siteNumber') +
    signet +
    siteOverride +
    blueprints +
    ' ' +
    override
  ).trim();
}
