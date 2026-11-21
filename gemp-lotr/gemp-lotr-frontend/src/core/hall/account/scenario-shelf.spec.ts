import { HttpErrorResponse } from '@angular/common/http';
import { readServerResponse } from '../../../testing/files';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { parseMyScenarios, parsePublicScenarios } from './account-parsers';
import {
  groupShelf,
  kindHeading,
  ownScenarioRow,
  publicScenarioRow,
  scenarioSlug,
  shelfError,
  shelfWhen,
} from './scenario-shelf';

const mine = parseMyScenarios(JSON.parse(readServerResponse('scenarios-mine.json'))).scenarios;
const [inReview, sentBack, published, broken] = mine;

describe('the scenario shelf', () => {
  it('heads kinds and campaigns as the legacy shelf', () => {
    expect(kindHeading('tutorial')).toBe('Tutorials');
    expect(kindHeading('other')).toBe('other');
    expect(kindHeading(null)).toBe('Unreadable');
    const groups = groupShelf(mine, (s) => s, true);
    expect(groups.map((g) => g.heading)).toEqual(['Tutorials', 'Puzzles', 'Unreadable']);
    expect(groups[0]?.campaigns.map((c) => [c.heading, c.items.length])).toEqual([
      ['Basics', 1],
      ['Not in a campaign', 1],
    ]);
    expect(groups[1]?.campaigns[0]?.heading).toBeNull();
    expect(groupShelf(mine, (s) => s, false)[0]?.campaigns[1]?.heading).toBeNull();
  });

  it('makes a new id from a title, and says when', () => {
    expect(scenarioSlug('  The Pit: Part 2! ')).toBe('the-pit-part-2');
    expect(scenarioSlug('x'.repeat(70))).toHaveLength(60);
    expect(shelfWhen(null)).toBe('');
    expect(shelfWhen(1763640000000)).not.toBe('');
  });

  it('says what a failed shelf call said, or what went wrong', () => {
    const failed = (status: number, body: unknown = null) =>
      ApiError.from(new HttpErrorResponse({ status, error: body }));
    expect(shelfError(failed(400, '{"error":"You may keep 100."}'), 'That could not be done')).toBe(
      'You may keep 100.',
    );
    expect(shelfError(failed(0), 'x')).toBe('Could not reach the server.');
    expect(shelfError(failed(403), 'x')).toBe('You may not do that.');
    expect(shelfError(failed(500), 'That could not be done')).toBe('That could not be done (HTTP 500).');
    expect(shelfError(new ApiError(INVALID_RESPONSE_STATUS, 'bad'), 'X')).toBe('X (HTTP 200).');
  });

  it('shows a scenario in review: locked, tagged, its place in the campaign', () => {
    const row = ownScenarioRow(inReview!, false);
    expect(row.title).toBe('First steps');
    expect(row.position).toBe(' #2');
    expect(row.tags.map((t) => [t.text, t.kind])).toEqual([['In review', 'review']]);
    expect(row.tags[0]?.hint).toMatch(
      /^Submitted .+\. It cannot be changed until an admin has approved or rejected it\.$/,
    );
    expect(row.notes).toBe('tutorial-1.hjson');
    expect(row.edit).toEqual({
      enabled: false,
      hint: 'It is in review, and cannot be changed. Copy it to go on working on it.',
    });
    expect(row.submit).toEqual({ enabled: false, hint: 'It has been submitted' });
    expect(row.delete).toEqual({ enabled: false, hint: 'It is in review, and cannot be deleted' });
    expect(row.copy).toEqual({ enabled: true, hint: 'Make a copy of it, “Copy of First steps”, to change' });
  });

  it('shows one sent back, one published and one unreadable', () => {
    const back = ownScenarioRow(sentBack!, true);
    expect(back.picked).toBe(true);
    expect(back.tags.map((t) => t.kind)).toEqual(['rejected', 'unverified']);
    expect(back.tags[0]?.hint).toMatch(/^Reviewed by admin /);
    expect(back.notes).toBe(
      'A version of the public “Second steps (public)”: approved, it replaces that one · tutorial-2.hjson',
    );
    expect(back.feedback).toEqual({ label: 'Changes requested: ', text: 'Shorter messages, please.' });
    expect(back.submit).toEqual({ enabled: false, hint: 'It has to play through before it can be submitted' });
    expect(ownScenarioRow({ ...sentBack!, feedback: null }, false).feedback).toEqual({
      label: null,
      text: 'It was sent back without a note.',
    });

    const pub = ownScenarioRow(published!, false);
    expect(pub.tags.map((t) => t.text)).toEqual(['★ Published']);
    expect(pub.tags[0]?.hint).toMatch(/: this is the public version of The Pit\.$/);
    expect(pub.notes).toBe('puzzle.hjson');
    expect(pub.submit.hint).toBe('It is the public version as it stands: change it to submit it again');
    expect(ownScenarioRow({ ...published!, published: 'changed' }, false).tags[0]?.text).toBe(
      '★ Published; changed since',
    );

    const bad = ownScenarioRow(broken!, false);
    expect(bad.title).toBe('broken');
    expect(bad.tags).toEqual([{ text: 'Cannot be played', kind: 'broken', hint: 'Line 3: a } is missing\nNo steps' }]);
    expect(bad.notes).toBe('Was a version of the public gone, which is no longer public · broken.hjson');
    expect([bad.edit.enabled, bad.play.enabled, bad.copy.enabled, bad.delete.enabled]).toEqual([
      false,
      false,
      false,
      true,
    ]);
    expect(ownScenarioRow({ ...inReview!, locked: false, status: null }, false).submit.hint).toBe(
      'Submit it for review, to become a public scenario',
    );
  });

  it('shows the public scenarios to copy', () => {
    const [first, sandbox] = parsePublicScenarios(JSON.parse(readServerResponse('scenarios-public.json'))).map(
      publicScenarioRow,
    );
    expect(first).toMatchObject({ title: 'The First Game', notes: 'first · by Ketura · Your first game' });
    expect(sandbox).toMatchObject({ title: 'sandbox', notes: 'sandbox · disabled' });
  });
});
