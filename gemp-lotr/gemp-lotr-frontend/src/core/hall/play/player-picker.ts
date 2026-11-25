import { computed, inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { stringsOf } from '../../api/json';
import { ChatService } from '../../chat/chat.service';
import { PlayerInfo } from '../../session/player-info.service';
import { bareName } from '../tables/hall-table-parsers';
import { PICKER_IN_HALL, PICKER_NOBODY, pickerNoMatch, PICKER_OTHERS } from './play-messages';

/** The invitee picker's server search starts at 2 characters and asks for at most 10 names (PlayerPicker). */
export const PICKER_MIN_SEARCH = 2;
export const PICKER_LIMIT = 10;

/** One line of the picker's list: a name under its heading, or a note that is not a choice. */
export type PickerItem =
  | { readonly kind: 'player'; readonly name: string; readonly heading: string }
  | { readonly kind: 'note'; readonly text: string };

export function compareNames(a: string, b: string): number {
  return a.toLowerCase().localeCompare(b.toLowerCase());
}

function contains(list: readonly string[], name: string): boolean {
  const lower = name.toLowerCase();
  return list.some((entry) => entry.toLowerCase() === lower);
}

/**
 * Names matching `term`, ignoring case: exact first, then those starting with it, then those containing it;
 * alphabetical within each. An empty term keeps every name (PlayerPicker.rank).
 */
export function rankNames(names: readonly string[], term: string): string[] {
  const q = term.toLowerCase();
  if (q === '') {
    return [...names].sort(compareNames);
  }
  return names
    .map((name) => {
      const folded = name.toLowerCase();
      const rank = folded === q ? 0 : folded.startsWith(q) ? 1 : folded.includes(q) ? 2 : -1;
      return { name, rank };
    })
    .filter((hit) => hit.rank >= 0)
    .sort((a, b) => a.rank - b.rank || compareNames(a.name, b.name))
    .map((hit) => hit.name);
}

/** The players in the hall, bare, without duplicates (any case), sorted (PlayerPicker.setPlayers). */
export function hallPlayers(names: readonly string[]): string[] {
  const seen = new Set<string>();
  const list: string[] = [];
  for (const raw of names) {
    const name = bareName(raw);
    if (name !== '' && !seen.has(name.toLowerCase())) {
      seen.add(name.toLowerCase());
      list.push(name);
    }
  }
  return list.sort(compareNames);
}

/**
 * The picker's list for what was typed (PlayerPicker.suggest): the players in the hall that match, then "Other
 * players": the server's matches (from 2 characters; `searched` null when it was not asked or failed) or, for a
 * shorter term, the names set from outside (`pinned`, e.g. "Invite to table" for someone not in the hall). Never
 * the viewer. Notes stand in for an empty list.
 */
export function pickerItems(
  term: string,
  online: readonly string[],
  pinned: readonly string[],
  self: string,
  searched: readonly string[] | null,
): PickerItem[] {
  const q = term.trim();
  const isSelf = (name: string) => self !== '' && name.toLowerCase() === self.toLowerCase();
  const inHall = rankNames(
    online.filter((name) => !isSelf(name)),
    q,
  );
  const items: PickerItem[] = inHall.map((name) => ({ kind: 'player', name, heading: PICKER_IN_HALL }));
  const others = (names: readonly string[]) => {
    const list: string[] = [];
    for (const name of names) {
      if (!isSelf(name) && !contains(inHall, name) && !contains(list, name)) {
        list.push(name);
      }
    }
    return list.map((name): PickerItem => ({ kind: 'player', name, heading: PICKER_OTHERS }));
  };
  if (q === '') {
    items.push(...others(pinned));
    if (items.length === 0) {
      items.push({ kind: 'note', text: PICKER_NOBODY });
    }
    return items;
  }
  if (q.length < PICKER_MIN_SEARCH || searched === null) {
    items.push(...others(rankNames(pinned, q)));
    return items;
  }
  items.push(...others(searched));
  if (items.length === 0) {
    items.push({ kind: 'note', text: pickerNoMatch(q) });
  }
  return items;
}

/**
 * The Casual form's invitee picker (playerPicker.js): the players in the hall (the chat's user list) and, from 2
 * characters, every registered player whose name starts with what was typed (GET /hall/players, at most 10, cached
 * per prefix). A failed search still offers the players in the hall.
 */
@Injectable()
export class PlayerSearch {
  private readonly http = inject(GempHttp);
  private readonly chat = inject(ChatService);
  private readonly player = inject(PlayerInfo);
  private readonly cache = new Map<string, readonly string[]>();
  private readonly pinned: string[] = [];

  /** The players in the hall, bare and sorted, without the viewer. */
  readonly online = computed(() => {
    const me = this.self();
    return hallPlayers(this.chat.users().map((user) => user.name)).filter(
      (name) => me === '' || name.toLowerCase() !== me.toLowerCase(),
    );
  });

  self(): string {
    return bareName(this.player.player()?.name);
  }

  /** A name set from outside (val()): offered under "Other players" when it is not in the hall. */
  pin(name: string): void {
    const bare = bareName(name);
    if (bare !== '' && !contains(this.online(), bare) && !contains(this.pinned, bare)) {
      this.pinned.push(bare);
    }
  }

  /** The list for `term`, asking the server when it is long enough. */
  async suggest(term: string): Promise<PickerItem[]> {
    const q = term.trim();
    let searched: readonly string[] | null = null;
    if (q.length >= PICKER_MIN_SEARCH) {
      searched = await this.search(q);
    }
    return pickerItems(q, this.online(), this.pinned, this.self(), searched);
  }

  private async search(prefix: string): Promise<readonly string[] | null> {
    const key = prefix.toLowerCase();
    const cached = this.cache.get(key);
    if (cached) {
      return cached;
    }
    try {
      const json = await firstValueFrom(
        this.http.getJson<{ players?: unknown }>('/hall/players', { prefix, limit: PICKER_LIMIT }),
      );
      const names = stringsOf(json.players).slice(0, PICKER_LIMIT);
      this.cache.set(key, names);
      return names;
    } catch {
      return null;
    }
  }
}
