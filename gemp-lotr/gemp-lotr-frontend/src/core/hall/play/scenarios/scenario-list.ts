import { listOf, stringsOf } from '../../../api/json';
import { asText, asTextOrNull, orIfEmpty } from '../../json-values';

/** GET /scenarios as the Play overlay's scenario list reads it, and the pure rules it is drawn by (PlayScenario). */

export interface ScenarioProgress {
  /** "complete", or another status while in progress. */
  readonly status: string;
  readonly attempts: number;
  /** The version last played or, once completed, the version completed. */
  readonly version: number | null;
}

export interface ScenarioCampaign {
  readonly id: string | null;
  readonly title: string | null;
  readonly position: number;
}

export interface ScenarioEntry {
  readonly id: string;
  /** "tutorial", "puzzle", "sandbox". */
  readonly kind: string;
  readonly title: string;
  readonly summary: string | null;
  readonly author: string | null;
  readonly version: number | null;
  readonly canonical: boolean;
  readonly verified: boolean;
  /** Where the file is within its folder ("tutorials/basics/01-into-the-wild.hjson"). */
  readonly path: string;
  /** A player's own scenario: whose (null for the game's own). */
  readonly owner: string | null;
  readonly mine: boolean;
  readonly campaign: ScenarioCampaign | null;
  readonly progress: ScenarioProgress | null;
}

/** A scenario file with mistakes in it, in no list. */
export interface UnreadableScenario {
  readonly path: string;
  readonly id: string | null;
  readonly title: string | null;
  readonly errors: readonly string[];
  /** The authoring tool can open it all the same, with the mistaken parts left out. */
  readonly openable: boolean;
}

/** The author's open draft (ScenarioAuthoring.currentForHall). */
export interface ScenarioDraft {
  readonly gameId: string | null;
  readonly id: string | null;
  readonly title: string | null;
  /** Something in it is not saved (false: it is given up without a word). */
  readonly dirty: boolean | null;
}

export interface ScenarioListing {
  readonly scenarios: readonly ScenarioEntry[];
  /** The scenario game the player is in the middle of. */
  readonly current: { readonly gameId: string; readonly id: string } | null;
  readonly mayAuthor: boolean;
  readonly authoring: ScenarioDraft | null;
  readonly unreadable: readonly UnreadableScenario[];
}

const str = (value: unknown): string | null => (typeof value === 'string' ? value : null);
const num = (value: unknown): number | null => (typeof value === 'number' ? value : null);
const obj = (value: unknown): Record<string, unknown> | null =>
  value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Record<string, unknown>) : null;

export function parseScenarioListing(json: unknown): ScenarioListing {
  const data = obj(json) ?? {};
  const list = (value: unknown) =>
    listOf(value)
      .map(obj)
      .filter((e) => e !== null);
  const scenarios = list(data['scenarios']).map((s): ScenarioEntry => {
    const campaign = obj(s['campaign']);
    const progress = obj(s['progress']);
    return {
      id: asText(s['id']),
      kind: str(s['kind']) ?? '',
      title: str(s['title']) ?? asText(s['id']),
      summary: str(s['summary']),
      author: str(s['author']),
      version: num(s['version']),
      canonical: s['canonical'] === true,
      verified: s['verified'] === true,
      path: str(s['path']) ?? '',
      owner: str(s['owner']),
      mine: s['mine'] === true,
      campaign:
        campaign === null
          ? null
          : {
              id: asTextOrNull(campaign['id']),
              title: str(campaign['title']),
              position: num(campaign['position']) ?? 0,
            },
      progress:
        progress === null
          ? null
          : {
              status: str(progress['status']) ?? '',
              attempts: num(progress['attempts']) ?? 0,
              version: num(progress['version']),
            },
    };
  });
  const current = obj(data['current']);
  const authoring = obj(data['authoring']);
  return {
    scenarios,
    current: current === null ? null : { gameId: asText(current['gameId']), id: asText(current['id']) },
    mayAuthor: data['mayAuthor'] === true,
    authoring:
      authoring === null
        ? null
        : {
            gameId: asTextOrNull(authoring['gameId']),
            id: str(authoring['id']),
            title: str(authoring['title']),
            dirty: typeof authoring['dirty'] === 'boolean' ? authoring['dirty'] : null,
          },
    unreadable: list(data['unreadable']).map((u) => ({
      path: str(u['path']) ?? '',
      id: str(u['id']),
      title: str(u['title']),
      errors: stringsOf(u['errors']),
      openable: u['openable'] === true,
    })),
  };
}

/** The top-level divisions by kind, in their order. */
export const SCENARIO_KINDS: readonly { readonly kind: string; readonly heading: string }[] = [
  { kind: 'tutorial', heading: 'Tutorials' },
  { kind: 'puzzle', heading: 'Puzzles' },
  { kind: 'sandbox', heading: 'Sandboxes' },
];
export const MY_SCENARIOS = 'My scenarios';
export const OTHER_PLAYERS = 'Other players';

/** "01-first-steps" -> "First steps": a folder's name as a person would write it. */
export function prettyFolder(name: string): string {
  const text = name
    .replace(/^\d+[-_ .]+/, '')
    .replace(/[-_]+/g, ' ')
    .trim();
  return text.length === 0 ? name : text.charAt(0).toUpperCase() + text.slice(1);
}

/** The folder a scenario is shown in, from the top down: its kind, then the folders its file is in. */
export function folderOf(scenario: ScenarioEntry): string[] {
  let dirs = scenario.path.split('/').slice(0, -1);
  const heading = SCENARIO_KINDS.find((known) => known.kind === scenario.kind)?.heading ?? scenario.kind;
  // files are kept under tutorials/, puzzles/ or sandboxes/: that folder is the division itself
  const first = dirs[0];
  if (first !== undefined && [scenario.kind, `${scenario.kind}s`, `${scenario.kind}es`].includes(first.toLowerCase())) {
    dirs = dirs.slice(1);
  }
  const within = [prettyFolder(heading), ...dirs.map(prettyFolder)];
  if (scenario.owner === null) {
    return within;
  }
  return scenario.mine ? [MY_SCENARIOS, ...within] : [OTHER_PLAYERS, scenario.owner, ...within];
}

export interface ScenarioFolder {
  /** null for the root. */
  readonly name: string | null;
  /** Its names from the top joined with "/" ("" for the root). */
  readonly key: string;
  readonly children: ScenarioFolder[];
  readonly scenarios: ScenarioEntry[];
}

/** Every folder that has a scenario in or under it (buildTree): kinds first, then the player's own, then others'. */
export function buildScenarioTree(scenarios: readonly ScenarioEntry[]): ScenarioFolder {
  const root: ScenarioFolder = { name: null, key: '', children: [], scenarios: [] };
  const sorted = [...scenarios].sort((a, b) => a.path.localeCompare(b.path));
  for (const scenario of sorted) {
    let node = root;
    for (const name of folderOf(scenario)) {
      let child = node.children.find((candidate) => candidate.name === name);
      if (child === undefined) {
        child = { name, key: node.key === '' ? name : `${node.key}/${name}`, children: [], scenarios: [] };
        node.children.push(child);
      }
      node = child;
    }
    node.scenarios.push(scenario);
  }
  const order = SCENARIO_KINDS.map((known) => known.heading);
  const rank = (node: ScenarioFolder) => {
    const index = order.indexOf(node.name ?? '');
    if (index > -1) {
      return index;
    }
    return node.name === MY_SCENARIOS ? 100 : node.name === OTHER_PLAYERS ? 101 : 50;
  };
  const sort = (node: ScenarioFolder) => {
    node.children.sort((a, b) => rank(a) - rank(b));
    node.children.forEach(sort);
  };
  sort(root);
  return root;
}

export function countIn(node: ScenarioFolder): number {
  return node.scenarios.length + node.children.reduce((sum, child) => sum + countIn(child), 0);
}

export function findFolder(node: ScenarioFolder, key: string): ScenarioFolder | null {
  if (node.key === key) {
    return node;
  }
  for (const child of node.children) {
    const found = findFolder(child, key);
    if (found !== null) {
      return found;
    }
  }
  return null;
}

/** The folder column: every folder with its depth and count, in order (renderFolders). */
export interface ScenarioFolderLine {
  readonly key: string;
  readonly name: string;
  readonly depth: number;
  readonly count: number;
}

export function folderLines(root: ScenarioFolder): ScenarioFolderLine[] {
  const lines: ScenarioFolderLine[] = [];
  const walk = (node: ScenarioFolder, depth: number) => {
    for (const child of node.children) {
      lines.push({ key: child.key, name: child.name ?? '', depth, count: countIn(child) });
      walk(child, depth + 1);
    }
  };
  walk(root, 0);
  return lines;
}

/** The list for the folder selected: groups under headings "A › B" (renderFolder). */
export interface ScenarioGroup {
  /** The heading, or null when the group carries none. */
  readonly heading: string | null;
  readonly scenarios: readonly ScenarioEntry[];
}

export function folderGroups(top: ScenarioFolder): ScenarioGroup[] {
  const groups: ScenarioGroup[] = [];
  const walk = (node: ScenarioFolder, first: boolean) => {
    let heading: string | null = null;
    if (node !== top && node.scenarios.length > 0) {
      heading = node.key
        .substring(top.key.length + 1)
        .split('/')
        .join(' › ');
    } else if (first) {
      heading = top.key.split('/').join(' › ');
    }
    if (heading !== null || node.scenarios.length > 0) {
      groups.push({ heading, scenarios: node.scenarios });
    }
    node.children.forEach((child) => walk(child, false));
  };
  walk(top, true);
  return groups;
}

export type ScenarioStatusKind = 'new' | 'next' | 'progress' | 'complete' | 'updated';

export interface ScenarioStatus {
  readonly kind: ScenarioStatusKind;
  readonly text: string;
  readonly hint: string;
}

/** New, In Progress, Complete, or Updated once revised since it was completed (PlayScenario.progressOf). */
export function progressOf(scenario: ScenarioEntry, current: ScenarioListing['current']): ScenarioStatus {
  const progress = scenario.progress;
  if (progress !== null && progress.status === 'complete') {
    if (progress.version !== null && scenario.version !== null && progress.version < scenario.version) {
      return { kind: 'updated', text: 'Updated', hint: 'You have completed it, and it has been revised since' };
    }
    return { kind: 'complete', text: 'Complete', hint: 'You have played it through' };
  }
  if (progress !== null || (current !== null && current.id === scenario.id)) {
    return { kind: 'progress', text: 'In Progress', hint: 'You have started it, and not finished it' };
  }
  return { kind: 'new', text: 'New', hint: 'You have not played it' };
}

/** In each campaign, the first scenario not played that comes after one finished (nextIds). */
export function nextIds(scenarios: readonly ScenarioEntry[], current: ScenarioListing['current']): Set<string> {
  const campaigns = new Map<string, ScenarioEntry[]>();
  for (const scenario of scenarios) {
    const id = scenario.campaign?.id;
    if (id !== null && id !== undefined) {
      campaigns.set(id, [...(campaigns.get(id) ?? []), scenario]);
    }
  }
  const next = new Set<string>();
  for (const list of campaigns.values()) {
    const inOrder = [...list].sort((a, b) => (a.campaign?.position ?? 0) - (b.campaign?.position ?? 0));
    let finished = false;
    for (const scenario of inOrder) {
      const kind = progressOf(scenario, current).kind;
      if (kind === 'complete' || kind === 'updated') {
        finished = true;
      } else if (kind === 'new' && finished) {
        next.add(scenario.id);
        break;
      }
    }
  }
  return next;
}

/** The tag in front of a title: progressOf, or Next for the one a campaign has the player on. */
export function statusOf(scenario: ScenarioEntry, current: ScenarioListing['current'], next: ReadonlySet<string>) {
  const status = progressOf(scenario, current);
  if (status.kind === 'new' && next.has(scenario.id)) {
    return {
      kind: 'next' as const,
      text: 'Next',
      hint: `The next one in ${orIfEmpty(scenario.campaign?.title, 'its campaign')}`,
    };
  }
  return status;
}

/** The tutorial window is too small below this (the game page's scenario panels: MIN_WIDTH / MIN_HEIGHT). */
export const SCENARIO_MIN_WIDTH = 1000;
export const SCENARIO_MIN_HEIGHT = 600;

export function windowTooSmallForTutorial(width: number, height: number): boolean {
  return width < SCENARIO_MIN_WIDTH || height < SCENARIO_MIN_HEIGHT;
}
