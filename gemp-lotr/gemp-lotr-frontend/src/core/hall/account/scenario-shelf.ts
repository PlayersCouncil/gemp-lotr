import { ApiError } from '../../api/api-error';
import { shownStatus } from '../events/events-messages';
import {
  byAuthor,
  CHANGES_REQUESTED_LABEL,
  changesRequestedHint,
  copyHint,
  DELETE_HINT,
  DELETE_LOCKED_HINT,
  DISABLED_NOTE,
  DOES_NOT_PLAY_THROUGH_HINT,
  EDIT_HINT,
  EDIT_LOCKED_HINT,
  inReviewHint,
  NOT_IN_A_CAMPAIGN,
  NOTES_SEPARATOR,
  PLAY_HINT,
  publishedChangedHint,
  publishedHint,
  SCENARIO_KIND_HEADINGS,
  SENT_BACK_WITHOUT_NOTE,
  SHELF_FORBIDDEN,
  SHELF_UNREACHABLE,
  shelfFailed,
  SUBMIT_CURRENT_HINT,
  SUBMIT_HINT,
  SUBMIT_LOCKED_HINT,
  SUBMIT_UNVERIFIED_HINT,
  TAG_CANNOT_BE_PLAYED,
  TAG_CHANGES_REQUESTED,
  TAG_DOES_NOT_PLAY_THROUGH,
  TAG_IN_REVIEW,
  TAG_PUBLISHED,
  TAG_PUBLISHED_CHANGED,
  UNREADABLE_KIND,
  versionOf,
  wasVersionOf,
} from './account-messages';
import { OwnScenario, PublicScenario, ShelfScenario } from './account-models';
import { firstText } from './first-text';

/** The rules of the shelf's rows (scenarioShelf.js ScenarioShelfCommon and MyScenariosUI.row), with no state. */

/** A kind's heading (ScenarioShelfCommon.heading): Tutorials, Puzzles, Sandboxes; an unknown kind as it is sent. */
export function kindHeading(kind: string | null): string {
  if (kind === null || kind === '') {
    return UNREADABLE_KIND;
  }
  return SCENARIO_KIND_HEADINGS[kind] ?? kind;
}

export interface ShelfCampaignGroup<T> {
  readonly title: string;
  /** The heading over its rows: the campaign's title; "Not in a campaign" when it has none and others do. */
  readonly heading: string | null;
  readonly items: readonly T[];
}

export interface ShelfKindGroup<T> {
  readonly kind: string;
  readonly heading: string;
  readonly campaigns: readonly ShelfCampaignGroup<T>[];
}

/**
 * ScenarioShelfCommon.group: what the server sent (already in order) divided by kind, then campaign, in the order they
 * come (`scenarioOf`: the scenario an item stands for). `namelessHeading`: My Scenarios heads the scenarios in no campaign "Not in a campaign" when a kind has
 * campaigns too; the Copy Public dialog does not.
 */
export function groupShelf<T>(
  items: readonly T[],
  scenarioOf: (item: T) => ShelfScenario,
  namelessHeading: boolean,
): ShelfKindGroup<T>[] {
  const kinds: { kind: string; heading: string; campaigns: { title: string; items: T[] }[] }[] = [];
  for (const item of items) {
    const scenario = scenarioOf(item);
    const kind = scenario.kind ?? '';
    let group = kinds.find((entry) => entry.kind === kind);
    if (!group) {
      group = { kind, heading: kindHeading(scenario.kind), campaigns: [] };
      kinds.push(group);
    }
    const title = scenario.campaign?.title ?? '';
    let campaign = group.campaigns.find((entry) => entry.title === title);
    if (!campaign) {
      campaign = { title, items: [] };
      group.campaigns.push(campaign);
    }
    campaign.items.push(item);
  }
  return kinds.map((group) => ({
    kind: group.kind,
    heading: group.heading,
    campaigns: group.campaigns.map((campaign) => ({
      title: campaign.title,
      heading: campaign.title
        ? campaign.title
        : namelessHeading && group.campaigns.length > 1
          ? NOT_IN_A_CAMPAIGN
          : null,
      items: campaign.items,
    })),
  }));
}

/** ScenarioShelfCommon.when: the viewer's own date and time ("" for none). */
export function shelfWhen(ms: number | null): string {
  if (ms === null) {
    return '';
  }
  const date = new Date(ms);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
}

/**
 * MyScenariosUI's new scenario's id from its title (slug): small letters, digits and dashes, at most 60 characters.
 */
export function scenarioSlug(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .substring(0, 60);
}

/** ScenarioShelfCommon.errorOf: the server's `{error}`, else what went wrong, with `otherwise` and the status. */
export function shelfError(error: unknown, otherwise: string): string {
  const failure = ApiError.from(error);
  if (failure.said !== null) {
    return failure.said;
  }
  if (failure.is(0)) {
    return SHELF_UNREACHABLE;
  }
  if (failure.is(403)) {
    return SHELF_FORBIDDEN;
  }
  return shelfFailed(otherwise, shownStatus(failure));
}

/** A row's tag: its words, its kind (drawn by it: review, rejected, published, ...) and its tooltip. */
export interface ShelfTag {
  readonly text: string;
  readonly kind: 'review' | 'rejected' | 'published' | 'published-changed' | 'broken' | 'unverified';
  readonly hint: string;
}

export interface ShelfButton {
  readonly enabled: boolean;
  readonly hint: string;
}

/** One of the player's scenarios as its row shows it (MyScenariosUI.row). */
export interface OwnScenarioRow {
  readonly scenario: OwnScenario;
  readonly title: string;
  /** " #n": its place in its campaign; "" when none. */
  readonly position: string;
  readonly tags: readonly ShelfTag[];
  /** The notes line: what it is a version of, and its file. */
  readonly notes: string;
  /** The reviewer's note, after "Changes requested: " (`label`), or "It was sent back without a note." alone. */
  readonly feedback: { readonly label: string | null; readonly text: string } | null;
  readonly edit: ShelfButton;
  readonly play: ShelfButton;
  readonly copy: ShelfButton;
  readonly submit: ShelfButton;
  readonly delete: ShelfButton;
  /** It was just copied, and is picked out (and scrolled to: the view's). */
  readonly picked: boolean;
}

function tags(scenario: OwnScenario): ShelfTag[] {
  const list: ShelfTag[] = [];
  if (scenario.status === 'submitted') {
    list.push({ text: TAG_IN_REVIEW, kind: 'review', hint: inReviewHint(shelfWhen(scenario.submittedAt)) });
  } else if (scenario.status === 'rejected') {
    list.push({
      text: TAG_CHANGES_REQUESTED,
      kind: 'rejected',
      hint: changesRequestedHint(scenario.reviewedBy, shelfWhen(scenario.reviewedAt)),
    });
  }
  if (scenario.published === 'current') {
    list.push({
      text: TAG_PUBLISHED,
      kind: 'published',
      hint: publishedHint(shelfWhen(scenario.reviewedAt), scenario.origin?.title ?? null),
    });
  } else if (scenario.published === 'changed') {
    list.push({
      text: TAG_PUBLISHED_CHANGED,
      kind: 'published-changed',
      hint: publishedChangedHint(shelfWhen(scenario.reviewedAt)),
    });
  }
  if (!scenario.playable) {
    list.push({ text: TAG_CANNOT_BE_PLAYED, kind: 'broken', hint: scenario.errors.join('\n') });
  } else if (!scenario.verified) {
    list.push({ text: TAG_DOES_NOT_PLAY_THROUGH, kind: 'unverified', hint: DOES_NOT_PLAY_THROUGH_HINT });
  }
  return list;
}

function notes(scenario: OwnScenario): string {
  const list: string[] = [];
  const origin = scenario.origin;
  if (origin && scenario.published === null) {
    list.push(origin.exists ? versionOf(firstText(origin.title, origin.id)) : wasVersionOf(origin.id));
  }
  list.push(scenario.path);
  return list.join(NOTES_SEPARATOR);
}

export function ownScenarioRow(scenario: OwnScenario, picked: boolean): OwnScenarioRow {
  const locked = scenario.locked;
  const current = scenario.published === 'current';
  let feedback: OwnScenarioRow['feedback'] = null;
  if (scenario.status === 'rejected') {
    feedback = scenario.feedback
      ? { label: CHANGES_REQUESTED_LABEL, text: scenario.feedback }
      : { label: null, text: SENT_BACK_WITHOUT_NOTE };
  }
  return {
    scenario,
    title: firstText(scenario.title, scenario.id, scenario.path),
    position: scenario.campaign?.position ? ` #${scenario.campaign.position}` : '',
    tags: tags(scenario),
    notes: notes(scenario),
    feedback,
    edit: { enabled: !locked && scenario.openable, hint: locked ? EDIT_LOCKED_HINT : EDIT_HINT },
    play: { enabled: scenario.playable, hint: PLAY_HINT },
    copy: { enabled: scenario.fullId !== null, hint: copyHint(firstText(scenario.title, scenario.id)) },
    submit: {
      enabled: !locked && !current && scenario.verified,
      hint: locked
        ? SUBMIT_LOCKED_HINT
        : current
          ? SUBMIT_CURRENT_HINT
          : !scenario.verified
            ? SUBMIT_UNVERIFIED_HINT
            : SUBMIT_HINT,
    },
    delete: { enabled: !locked, hint: locked ? DELETE_LOCKED_HINT : DELETE_HINT },
    picked,
  };
}

/** A public scenario as the Copy Public dialog shows it: title, and its id, author, "disabled" and summary. */
export interface PublicScenarioRow {
  readonly scenario: PublicScenario;
  readonly title: string;
  readonly notes: string;
}

export function publicScenarioRow(scenario: PublicScenario): PublicScenarioRow {
  const list: string[] = [scenario.fullId ?? ''];
  if (scenario.author) {
    list.push(byAuthor(scenario.author));
  }
  if (scenario.disabled) {
    list.push(DISABLED_NOTE);
  }
  if (scenario.summary) {
    list.push(scenario.summary);
  }
  return { scenario, title: firstText(scenario.title, scenario.id), notes: list.join(NOTES_SEPARATOR) };
}
