import { computed, Injectable, signal } from '@angular/core';
import { BlockFilter, CollectionTypeInfo, GempFormat, League, MetaSite } from '../api/models';
import { deckContentsOf, DeckState } from './deck-model';
import { formatUsesMap } from './deck-contents';
import {
  FormatOption,
  hallFormatOptions,
  INITIAL_FORMAT_OPTIONS,
  limitedFormatOptions,
  optionIndexOf,
  rtmdFormatOptions,
} from './format-options';

/** The meta-site shown in the Race slot: the position card, with the modifier's lower part over it. */
export interface RaceCard {
  blueprintId: string;
  overlayBlueprintId: string | null;
}

/** The format list and which entry is selected, with what follows from that selection. Only state, no requests. */
@Injectable()
export class DeckFormatSelection {
  private readonly formats = signal<ReadonlyMap<string, GempFormat>>(new Map());
  private readonly rtmdLeagues = signal<ReadonlyMap<string, League>>(new Map());

  readonly formatOptions = signal<readonly FormatOption[]>(INITIAL_FORMAT_OPTIONS);
  readonly selectedFormatIndex = signal(0);
  readonly selectedFormat = computed(
    () => this.formatOptions()[this.selectedFormatIndex()] ?? INITIAL_FORMAT_OPTIONS[0],
  );

  private readonly selectedLeague = computed(() => {
    const option = this.selectedFormat();
    return option.rtmd ? this.rtmdLeagues().get(option.value) : undefined;
  });

  /** The format code for the server: an RTMD league's own format, otherwise the selected one. */
  readonly effectiveFormat = computed(() => this.selectedLeague()?.formatCode ?? this.selectedFormat().value);
  /** Whether the format has a Map slot. */
  readonly usesMap = computed(() => formatUsesMap(this.effectiveFormat()));
  /** Whether the format is a Race to Mount Doom league, which shows the Race slot. */
  readonly usesRace = computed(() => this.selectedLeague() !== undefined);
  /** The current Race to Mount Doom meta-sites (the last one is shown), or null when there are none. */
  readonly metaSites = computed<readonly MetaSite[] | null>(() => {
    const sites = this.selectedLeague()?.metaSites;
    return sites && sites.length > 0 ? sites : null;
  });
  readonly raceCard = computed<RaceCard | null>(() => {
    const current = this.metaSites()?.at(-1);
    return current
      ? {
          blueprintId: current.visualBlueprintId ?? current.blueprintId,
          overlayBlueprintId: current.visualBlueprintId ? current.blueprintId : null,
        }
      : null;
  });

  /** Lists the hall formats, staying on the selected format. */
  showHallFormats(formats: Readonly<Record<string, GempFormat>>): void {
    const current = this.selectedFormat().value;
    const options = hallFormatOptions({ ...formats });
    this.formats.set(new Map(Object.entries(formats)));
    this.formatOptions.set(options);
    this.selectedFormatIndex.set(optionIndexOf(options, current));
  }

  addLimitedCollections(collections: readonly CollectionTypeInfo[]): void {
    this.formatOptions.update((options) => [...options, ...limitedFormatOptions(collections)]);
  }

  addRtmdLeagues(leagues: readonly League[]): void {
    this.rtmdLeagues.set(new Map(leagues.map((league) => [league.code, league])));
    this.formatOptions.update((options) => [...options, ...rtmdFormatOptions(leagues)]);
  }

  /** The entry a saved deck belongs to: its league while that is running, otherwise its format. */
  indexForDeck(target: { formatCode: string; leagueCode?: string | null }): number {
    const value =
      target.leagueCode && this.rtmdLeagues().has(target.leagueCode) ? target.leagueCode : target.formatCode;
    return optionIndexOf(this.formatOptions(), value);
  }

  blockFilters(format: string): readonly BlockFilter[] | null {
    return this.formats().get(format)?.blockFilters ?? null;
  }

  /** The deckContents string for the server. */
  contentsOf(deck: DeckState): string {
    return deckContentsOf(deck, this.usesMap());
  }
}
