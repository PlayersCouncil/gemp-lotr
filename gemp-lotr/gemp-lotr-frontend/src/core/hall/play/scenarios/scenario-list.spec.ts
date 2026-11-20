import { readServerResponse } from '../../../../testing/files';
import {
  buildScenarioTree,
  folderGroups,
  folderLines,
  folderOf,
  nextIds,
  parseScenarioListing,
  prettyFolder,
  progressOf,
  statusOf,
  windowTooSmallForTutorial,
} from './scenario-list';

const listing = parseScenarioListing(JSON.parse(readServerResponse('scenarios.json')));
const byId = (id: string) => listing.scenarios.find((s) => s.id === id)!;

describe('the scenario list', () => {
  it('reads GET /scenarios', () => {
    expect(listing.current).toEqual({ gameId: 'g-77', id: 'p1' });
    expect(listing.mayAuthor).toBe(true);
    expect(listing.unreadable[0]).toMatchObject({
      id: 'broken',
      openable: true,
      errors: ["Unknown card 'the'", 'No such format'],
    });
    expect(byId('t1').progress).toEqual({ status: 'complete', attempts: 3, version: 2 });
    expect(parseScenarioListing(null).scenarios).toEqual([]);
  });

  it('puts each scenario in its folder: its kind, then its directories; the player own ones apart', () => {
    expect(prettyFolder('01-first-steps')).toBe('First steps');
    expect(prettyFolder('42')).toBe('42');
    expect(folderOf(byId('t1'))).toEqual(['Tutorials', 'Basics']);
    expect(folderOf(byId('p1'))).toEqual(['Puzzles']);
    expect(folderOf(byId('m1'))).toEqual(['My scenarios', 'Puzzles']);
    expect(folderOf({ ...byId('m1'), mine: false, owner: 'bob' })).toEqual(['Other players', 'bob', 'Puzzles']);
  });

  it('builds the folder column and the groups of the selected folder', () => {
    const tree = buildScenarioTree(listing.scenarios);
    expect(folderLines(tree)).toEqual([
      { key: 'Tutorials', name: 'Tutorials', depth: 0, count: 2 },
      { key: 'Tutorials/Basics', name: 'Basics', depth: 1, count: 2 },
      { key: 'Puzzles', name: 'Puzzles', depth: 0, count: 1 },
      { key: 'My scenarios', name: 'My scenarios', depth: 0, count: 1 },
      { key: 'My scenarios/Puzzles', name: 'Puzzles', depth: 1, count: 1 },
    ]);
    const groups = folderGroups(tree.children[0]);
    expect(groups.map((g) => [g.heading, g.scenarios.map((s) => s.id)])).toEqual([
      ['Tutorials', []],
      ['Basics', ['t1', 't2']],
    ]);
  });

  it('tags the progress, and the next one of a campaign', () => {
    expect(progressOf(byId('t1'), listing.current).kind).toBe('complete');
    expect(progressOf({ ...byId('t1'), version: 3 }, listing.current).kind).toBe('updated');
    expect(progressOf(byId('p1'), null).kind).toBe('progress');
    const next = nextIds(listing.scenarios, listing.current);
    expect([...next]).toEqual(['t2']);
    expect(statusOf(byId('t2'), listing.current, next)).toEqual({
      kind: 'next',
      text: 'Next',
      hint: 'The next one in The Basics',
    });
    expect(windowTooSmallForTutorial(999, 800)).toBe(true);
    expect(windowTooSmallForTutorial(1200, 600)).toBe(false);
  });
});
