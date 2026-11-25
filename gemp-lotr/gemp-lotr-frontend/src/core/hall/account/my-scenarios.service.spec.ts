import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, failAccount, setUpAccount } from '../../../testing/account-server';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { parseMyScenarios } from './account-parsers';
import { OwnScenario } from './account-models';
import { HallReadyOffers } from '../ready/hall-ready-offers.service';
import { MyScenarios } from './my-scenarios.service';
import { Subject } from 'rxjs';
import { HallChannel } from '../../platform/hall-tab.service';

const MINE = JSON.parse(readServerResponse('scenarios-mine.json')) as { scenarios: unknown[]; authoring: unknown };
const [inReview, sentBack] = parseMyScenarios(MINE).scenarios as OwnScenario[];

describe('MyScenarios', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let navigation: ReturnType<typeof setUpAccount>['navigation'];
  let shelf: MyScenarios;

  beforeEach(() => {
    ({ backend, navigation } = setUpAccount());
    shelf = TestBed.inject(MyScenarios);
  });

  afterEach(() => backend.verify());

  async function shown(answer: unknown = MINE): Promise<void> {
    shelf.start();
    accountRequest(backend, '/scenarios/mine').flush(answer as object);
    await settle();
  }

  it('shows the scenarios by kind and campaign, and the draft', async () => {
    await shown();
    shelf.start();
    expect(shelf.groups().map((group) => group.heading)).toEqual(['Tutorials', 'Puzzles', 'Unreadable']);
    expect(shelf.draft()?.title).toBe('Second steps');
    expect(shelf.limit()).toBe(100);
    expect(shelf.empty()).toBe(false);
  });

  it('fetches the list again when another tab sends the hall somewhere, once it has been shown', async () => {
    const goes = (TestBed.inject(HallChannel) as unknown as { goes: Subject<string> }).goes;
    goes.next('#account/scenarios');
    backend.expectNone(() => true);
    await shown();
    goes.next('#account/scenarios');
    accountRequest(backend, '/scenarios/mine').flush({ ...MINE, limit: 7 });
    await settle();
    expect(shelf.limit()).toBe(7);
  });

  it('says when there are none, or nowhere to keep them, or the list failed', async () => {
    await shown({ scenarios: [], ownFolder: true, authoring: null });
    expect(shelf.empty()).toBe(true);
    void shelf.refresh();
    accountRequest(backend, '/scenarios/mine').flush({ scenarios: [], ownFolder: false });
    await settle();
    expect([shelf.ownFolder(), shelf.empty()]).toEqual([false, false]);
    void shelf.refresh();
    failAccount(backend, '/scenarios/mine', 502);
    await settle();
    expect(shelf.saying()).toEqual({ text: 'Your scenarios could not be loaded (HTTP 502).', kind: 'error' });
  });

  it('plays a scenario in the tab reserved on the click', async () => {
    await shown();
    const played = shelf.play(inReview!);
    expect(navigation.tabs).toHaveLength(1);
    expect(shelf.saying()).toEqual({ text: 'Setting up the table...', kind: 'pending' });
    const request = accountRequest(backend, '/scenarios/start', 'POST');
    expect(String(request.request.body)).toBe('id=alice%2Ftutorial-1');
    request.flush({ gameId: 'g1', resumedAt: null });
    await played;
    expect(navigation.tabs[0]?.url).toBe('/gemp-lotr/ng/game?gameId=g1');
    expect(shelf.saying()).toEqual({ text: 'Started in a new tab.', kind: 'success' });

    const failed = shelf.play(inReview!);
    failAccount(backend, '/scenarios/start', 404, 'POST', '{"error":"There is no scenario called \'x\'."}');
    await failed;
    expect(navigation.tabs[1]?.closed).toBe(true);
    expect(shelf.saying()).toEqual({ text: "There is no scenario called 'x'.", kind: 'error' });
  });

  it('offers the game in the "Your game is ready" prompt when the browser gives no tab', async () => {
    await shown();
    navigation.blockTabs = true;
    const played = shelf.play(inReview!);
    accountRequest(backend, '/scenarios/start', 'POST').flush({ gameId: 'g2' });
    await played;
    expect(TestBed.inject(HallReadyOffers).offers()).toEqual([
      expect.objectContaining({ key: 'game:g2', kind: 'game', url: '/gemp-lotr/ng/game?gameId=g2' }),
    ]);
  });

  it('asks before giving up a draft with unsaved changes, then opens the scenario for writing', async () => {
    await shown();
    let opened = shelf.editScenario(inReview!);
    expect(shelf.question()).toMatchObject({ kind: 'confirm', title: 'You have a draft open with unsaved changes' });
    expect(navigation.tabs).toHaveLength(0);
    shelf.answer(false);
    expect(await opened).toBe(false);

    opened = shelf.editScenario(inReview!);
    shelf.answer(true);
    expect(navigation.tabs).toHaveLength(1);
    expect(shelf.saying().text).toBe('Opening the authoring tool...');
    const request = accountRequest(backend, '/scenarios/author/open', 'POST');
    expect(String(request.request.body)).toBe('id=alice%2Ftutorial-1');
    request.flush({ ok: true, gameId: 9, events: false, rebuilt: true, state: {} });
    await settle();
    accountRequest(backend, '/scenarios/mine').flush(MINE);
    expect(await opened).toBe(true);
    expect(navigation.tabs[0]?.url).toBe('/gemp-lotr/ng/game?gameId=9');
    expect(shelf.saying()).toEqual({ text: 'Opened in a new tab.', kind: 'success' });
  });

  it('creates a new one by its id, or one made from its title', async () => {
    await shown({ scenarios: [], authoring: null });
    expect(await shelf.create('  ', '')).toBe(false);
    expect(shelf.saying()).toEqual({ text: 'Give the new scenario a title (or an id).', kind: 'error' });
    const created = shelf.create(' My Puzzle! ', '');
    const request = accountRequest(backend, '/scenarios/author/open', 'POST');
    expect(String(request.request.body)).toBe('id=my-puzzle&new=true&title=My%20Puzzle!&canonical=false');
    request.flush('{"error":"You have 100 scenarios."}', { status: 400, statusText: 'Error' });
    expect(await created).toBe(false);
    expect(navigation.tabs[0]?.closed).toBe(true);
    expect(shelf.saying().text).toBe('You have 100 scenarios.');
  });

  it('copies, submits once asked and deletes once asked, picking the copy out', async () => {
    await shown();
    const copied = shelf.copy(inReview!);
    answerAccount(backend, '/scenarios/mine/copy', 'scenario-copy.json', 'POST');
    await settle();
    const withCopy = {
      ...MINE,
      scenarios: [...MINE.scenarios, { path: 'copy-of-first-steps.hjson', kind: 'tutorial' }],
    };
    accountRequest(backend, '/scenarios/mine').flush(withCopy);
    await copied;
    expect(shelf.saying()).toEqual({ text: 'Copied.', kind: 'success' });
    const rows = shelf.groups().flatMap((group) => group.campaigns.flatMap((campaign) => campaign.items));
    expect(rows.filter((row) => row.picked).map((row) => row.scenario.path)).toEqual(['copy-of-first-steps.hjson']);

    shelf.submit(sentBack!);
    expect(shelf.question()).toEqual({ kind: 'submit', title: 'Second steps' });
    shelf.answer(true);
    accountRequest(backend, '/scenarios/mine/submit', 'POST').flush({ ok: true });
    await settle();
    accountRequest(backend, '/scenarios/mine').flush(MINE);
    await settle();
    expect(shelf.saying().text).toBe('Submitted for review. It is locked until an admin has approved or rejected it.');
    expect(
      shelf
        .groups()
        .flatMap((g) => g.campaigns.flatMap((c) => c.items))
        .some((row) => row.picked),
    ).toBe(false);

    shelf.delete(sentBack!);
    expect(shelf.question()).toMatchObject({ title: 'Delete “Second steps”?', yes: 'Delete', danger: true });
    shelf.answer(true);
    failAccount(backend, '/scenarios/mine/delete', 400, 'POST', '{"error":"It is in review."}');
    await settle();
    accountRequest(backend, '/scenarios/mine').flush(MINE);
    await settle();
    expect(shelf.saying()).toEqual({ text: 'It is in review.', kind: 'error' });
  });

  it('continues or closes the draft', async () => {
    await shown();
    const resumed = shelf.continueDraft();
    accountRequest(backend, '/scenarios/author/resume', 'POST').flush({ ok: true, gameId: '7' });
    await resumed;
    expect(navigation.tabs[0]?.url).toBe('/gemp-lotr/ng/game?gameId=7');

    shelf.closeDraft();
    expect(shelf.question()).toMatchObject({ title: 'Close the draft?', yes: 'Close it' });
    shelf.answer(true);
    accountRequest(backend, '/scenarios/author/close', 'POST').flush({ ok: true });
    await settle();
    accountRequest(backend, '/scenarios/mine').flush({ ...MINE, authoring: { gameId: '7', dirty: false } });
    await settle();
    shelf.closeDraft();
    expect(shelf.question()).toBeNull();
    accountRequest(backend, '/scenarios/author/close', 'POST').flush('', { status: 500, statusText: 'Error' });
    await settle();
    accountRequest(backend, '/scenarios/mine').flush({ ...MINE, authoring: null });
    await settle();
    expect(shelf.draft()).toBeNull();
  });

  it('copies a public scenario from the Copy Public dialog', async () => {
    await shown();
    void shelf.openCopyPublic();
    expect(shelf.copyPublicDialog()).toEqual({ state: 'loading' });
    answerAccount(backend, '/scenarios/public', 'scenarios-public.json');
    await settle();
    const dialog = shelf.copyPublicDialog();
    expect(dialog?.state).toBe('loaded');
    const first = dialog?.state === 'loaded' ? dialog.groups[0]?.campaigns[0]?.items[0] : undefined;
    expect(first?.notes).toBe('first · by Ketura · Your first game');

    const copied = shelf.copyPublic(first!.scenario);
    expect(shelf.copyPublicDialog()).toBeNull();
    answerAccount(backend, '/scenarios/mine/copyPublic', 'scenario-copy.json', 'POST');
    await settle();
    accountRequest(backend, '/scenarios/mine').flush(MINE);
    await copied;
    expect(shelf.saying().text).toBe('Copied as copy-of-first-steps.hjson. Edit it from the list below.');

    void shelf.openCopyPublic();
    failAccount(backend, '/scenarios/public', 0);
    await settle();
    expect(shelf.copyPublicDialog()).toEqual({ state: 'failed', message: 'Could not reach the server.' });
    shelf.closeCopyPublic();
    expect(shelf.copyPublicDialog()).toBeNull();
  });
});
