import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';
import { readServerResponse } from '../../../testing/files';
import { HallReadyOffers } from '../../../core/hall/ready/hall-ready-offers.service';
import { sentForm } from '../../../testing/sent-form';

describe('MyScenariosComponent', () => {
  async function shown() {
    const setup = await setUpAccountPages();
    await setup.harness.navigateByUrl('/account/scenarios');
    answerAccount(setup.backend, '/scenarios/mine', 'scenarios-mine.json');
    await drawn(setup.harness);
    const host = (setup.harness.fixture.nativeElement as HTMLElement).querySelector(
      'gd-account-scenarios',
    ) as HTMLElement;
    return { ...setup, host };
  }

  it('draws the intro, the draft, the tools and the rows by kind and campaign', async () => {
    const { backend, host } = await shown();
    const intro = host.querySelectorAll('.gd-account-scenarios__intro');
    expect(intro[0]?.querySelector('b')?.textContent).toBe('Play > Play Scenario or Tutorial');
    expect(intro[0]?.textContent).toMatch(/Everybody plays them from Play > Play Scenario or Tutorial\.$/);
    expect(host.querySelector('.gd-account-scenarios__draft')?.textContent).toContain(
      'You have a draft open in the authoring tool: Second steps (being played as a player would), with changes',
    );
    expect(Array.from(host.querySelectorAll('.gd-account-scenarios__kind')).map((h) => h.textContent)).toEqual([
      'Tutorials',
      'Puzzles',
      'Unreadable',
    ]);
    const rows = Array.from(host.querySelectorAll<HTMLElement>('.gd-account-scenarios__row'));
    expect(rows.map((row) => row.dataset['path'])).toEqual([
      'tutorial-1.hjson',
      'tutorial-2.hjson',
      'puzzle.hjson',
      'broken.hjson',
    ]);
    const tag = rows[0]?.querySelector('.gd-account-scenarios__tag') as HTMLElement;
    expect([tag.textContent, tag.dataset['kind']]).toEqual(['In review', 'review']);
    expect(Array.from(rows[0]!.querySelectorAll('button')).map((b) => [b.dataset['action'], b.disabled])).toEqual([
      ['edit', true],
      ['play', false],
      ['copy', false],
      ['submit', true],
      ['delete', true],
    ]);
    expect(rows[1]?.querySelector('.gd-account-scenarios__feedback')?.textContent).toBe(
      'Changes requested: Shorter messages, please.',
    );
    backend.match(() => true);
  });

  it('suggests the id from the title, asks before deleting, and copies a public scenario', async () => {
    const { backend, harness, host } = await shown();
    const title = host.querySelector('input[name=title]') as HTMLInputElement;
    title.value = 'My Puzzle';
    title.dispatchEvent(new Event('input'));
    await drawn(harness);
    expect((host.querySelector('input[name=id]') as HTMLInputElement).placeholder).toBe('my-puzzle');

    (host.querySelectorAll<HTMLButtonElement>('[data-action=delete]')[1] as HTMLButtonElement).click();
    await drawn(harness);
    const box = host.querySelector('dialog.gd-shelf-question') as HTMLDialogElement;
    expect(box.querySelector('h2')?.textContent).toBe('Delete “Second steps”?');
    (box.querySelector('[data-action=yes]') as HTMLButtonElement).click();
    accountRequest(backend, '/scenarios/mine/delete', 'POST').flush({ ok: true });
    await drawn(harness);
    answerAccount(backend, '/scenarios/mine', 'scenarios-mine.json');
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__status')?.textContent?.trim()).toBe('Deleted.');
    expect(host.querySelector('.gd-account-scenarios__status')?.getAttribute('data-kind')).toBe('success');

    (host.querySelector('[data-action=copy-public]') as HTMLButtonElement).click();
    answerAccount(backend, '/scenarios/public', 'scenarios-public.json');
    await drawn(harness);
    const dialog = host.querySelector('dialog[data-dialog=copy-public]') as HTMLElement;
    expect(dialog.querySelectorAll('.gd-account-scenarios__row')).toHaveLength(2);
    (dialog.querySelector('[data-action=copy]') as HTMLButtonElement).click();
    answerAccount(backend, '/scenarios/mine/copyPublic', 'scenario-copy.json', 'POST');
    await drawn(harness);
    backend.match(() => true);
  });

  it('plays a scenario in a tab reserved on the click, or offers it in the hall', async () => {
    const { backend, harness, host, navigation } = await shown();
    navigation.blockTabs = true;
    (host.querySelector('[data-action=play]') as HTMLButtonElement).click();
    accountRequest(backend, '/scenarios/start', 'POST').flush({ gameId: 'g9' });
    await drawn(harness);
    expect(
      TestBed.inject(HallReadyOffers)
        .offers()
        .map((offer) => offer.key),
    ).toEqual(['game:g9']);
    backend.match(() => true);
  });

  it('goes back to the draft, and closes it once asked', async () => {
    const { backend, harness, host, navigation } = await shown();
    (host.querySelector('[data-action=resume]') as HTMLButtonElement).click();
    accountRequest(backend, '/scenarios/author/resume', 'POST').flush({ ok: true, gameId: '7' });
    await drawn(harness);
    expect(navigation.tabs.map((tab) => tab.url)).toEqual(['/gemp-lotr/ng/game?gameId=7']);

    (host.querySelector('[data-action=close-draft]') as HTMLButtonElement).click();
    await drawn(harness);
    const box = host.querySelector('dialog.gd-shelf-question') as HTMLDialogElement;
    expect(box.querySelector('h2')?.textContent).toBe('Close the draft?');
    (box.querySelector('[data-action=yes]') as HTMLButtonElement).click();
    accountRequest(backend, '/scenarios/author/close', 'POST').flush({ ok: true });
    await drawn(harness);
    accountRequest(backend, '/scenarios/mine').flush({ scenarios: [], ownFolder: true, authoring: null });
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__draft')).toBeNull();
    expect(host.querySelector('.gd-account-scenarios__help:not(.gd-account-scenarios__intro)')?.textContent).toBe(
      'You have no scenarios yet. Create one above, or copy a public one to change.',
    );
    backend.match(() => true);
  });

  it('creates a new one from its title with Enter, emptying the fields, after the draft question', async () => {
    const { backend, harness, host, navigation } = await shown();
    const title = host.querySelector('input[name=title]') as HTMLInputElement;
    title.value = 'My Puzzle';
    title.dispatchEvent(new Event('input'));
    await drawn(harness);
    title.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await drawn(harness);
    const box = host.querySelector('dialog.gd-shelf-question') as HTMLDialogElement;
    expect(box.querySelector('h2')?.textContent).toBe('You have a draft open with unsaved changes');
    (box.querySelector('[data-action=yes]') as HTMLButtonElement).click();
    const open = accountRequest(backend, '/scenarios/author/open', 'POST');
    expect(sentForm(open.request).toString()).toContain('id=my-puzzle&new=true');
    open.flush({ ok: true, gameId: 12 });
    await drawn(harness);
    answerAccount(backend, '/scenarios/mine', 'scenarios-mine.json');
    await drawn(harness);
    await drawn(harness);
    expect(navigation.tabs[0]?.url).toBe('/gemp-lotr/ng/game?gameId=12');
    expect(title.value).toBe('');
    backend.match(() => true);
  });

  it('copies a row, asks before submitting it, and closes the Copy Public dialog', async () => {
    const { backend, harness, host } = await shown();
    (host.querySelectorAll<HTMLButtonElement>('[data-action=copy]')[0] as HTMLButtonElement).click();
    answerAccount(backend, '/scenarios/mine/copy', 'scenario-copy.json', 'POST');
    await drawn(harness);
    answerAccount(backend, '/scenarios/mine', 'scenarios-mine.json');
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__status')?.textContent).toBe('Copied.');

    const submit = host.querySelectorAll<HTMLButtonElement>('[data-action=submit]');
    const enabled = Array.from(submit).find((button) => !button.disabled);
    expect(enabled).toBeUndefined();

    (host.querySelector('[data-action=copy-public]') as HTMLButtonElement).click();
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__popup')?.textContent?.trim()).toBe('Loading...');
    accountRequest(backend, '/scenarios/public').flush('', { status: 500, statusText: 'Error' });
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__popup')?.textContent?.trim()).toBe(
      'The public scenarios could not be loaded (HTTP 500).',
    );
    (host.querySelector('dialog[data-dialog=copy-public] .gd-dialog__close') as HTMLButtonElement).click();
    await drawn(harness);
    expect(host.querySelector('.gd-account-scenarios__popup')).toBeNull();
    backend.match(() => true);
  });

  it('submits a scenario that plays through, after the submit box', async () => {
    const mine = JSON.parse(readServerResponse('scenarios-mine.json')) as { scenarios: Record<string, unknown>[] };
    mine.scenarios[1] = { ...mine.scenarios[1], verified: true, status: null };
    const setup = await setUpAccountPages();
    await setup.harness.navigateByUrl('/account/scenarios');
    accountRequest(setup.backend, '/scenarios/mine').flush(mine);
    await drawn(setup.harness);
    const host = (setup.harness.fixture.nativeElement as HTMLElement).querySelector(
      'gd-account-scenarios',
    ) as HTMLElement;
    const submit = Array.from(host.querySelectorAll<HTMLButtonElement>('[data-action=submit]')).find(
      (b) => !b.disabled,
    )!;
    submit.click();
    await drawn(setup.harness);
    const box = host.querySelector('dialog.gd-shelf-question') as HTMLDialogElement;
    expect(box.dataset['kind']).toBe('submit');
    (box.querySelector('[data-action=yes]') as HTMLButtonElement).click();
    accountRequest(setup.backend, '/scenarios/mine/submit', 'POST').flush({ ok: true });
    await drawn(setup.harness);
    accountRequest(setup.backend, '/scenarios/mine').flush(mine);
    await drawn(setup.harness);
    expect(host.querySelector('.gd-account-scenarios__status')?.textContent).toBe(
      'Submitted for review. It is locked until an admin has approved or rejected it.',
    );
    setup.backend.match(() => true);
  });

  it('edits a row in the authoring tool once the draft question is answered', async () => {
    const { backend, harness, host, navigation } = await shown();
    (host.querySelectorAll<HTMLButtonElement>('[data-action=edit]')[1] as HTMLButtonElement).click();
    await drawn(harness);
    (host.querySelector('dialog.gd-shelf-question [data-action=yes]') as HTMLButtonElement).click();
    accountRequest(backend, '/scenarios/author/open', 'POST').flush({ ok: true, gameId: 3 });
    await drawn(harness);
    expect(navigation.tabs[0]?.url).toBe('/gemp-lotr/ng/game?gameId=3');
    backend.match(() => true);
  });

  it('creates a new one by the id typed, from Create, and scrolls a copy just made into view', async () => {
    const mine = JSON.parse(readServerResponse('scenarios-mine.json')) as Record<string, unknown>;
    const setup = await setUpAccountPages();
    await setup.harness.navigateByUrl('/account/scenarios');
    accountRequest(setup.backend, '/scenarios/mine').flush({ ...mine, authoring: null });
    await drawn(setup.harness);
    const host = (setup.harness.fixture.nativeElement as HTMLElement).querySelector(
      'gd-account-scenarios',
    ) as HTMLElement;
    const id = host.querySelector('input[name=id]') as HTMLInputElement;
    id.value = 'puzzle-2';
    id.dispatchEvent(new Event('input'));
    await drawn(setup.harness);
    (host.querySelector('[data-action=create]') as HTMLButtonElement).click();
    const open = accountRequest(setup.backend, '/scenarios/author/open', 'POST');
    expect(sentForm(open.request).toString()).toContain('id=puzzle-2&new=true');
    open.flush({ ok: true, gameId: 4 });
    await drawn(setup.harness);
    accountRequest(setup.backend, '/scenarios/mine').flush({ ...mine, authoring: null });
    await drawn(setup.harness);
    await drawn(setup.harness);
    expect(id.value).toBe('');

    // (jsdom has no scrollIntoView: one stands in for it)
    const scrolled: string[] = [];
    Object.defineProperty(Element.prototype, 'scrollIntoView', {
      configurable: true,
      value(this: Element) {
        scrolled.push(this.getAttribute('data-path') ?? '');
      },
    });
    try {
      (host.querySelectorAll<HTMLButtonElement>('[data-action=copy]')[0] as HTMLButtonElement).click();
      answerAccount(setup.backend, '/scenarios/mine/copy', 'scenario-copy.json', 'POST');
      await drawn(setup.harness);
      const scenarios = [...(mine['scenarios'] as object[]), { path: 'copy-of-first-steps.hjson', kind: 'tutorial' }];
      accountRequest(setup.backend, '/scenarios/mine').flush({ ...mine, scenarios, authoring: null });
      await drawn(setup.harness);
      await drawn(setup.harness);
    } finally {
      delete (Element.prototype as { scrollIntoView?: unknown }).scrollIntoView;
    }
    expect(host.querySelector('[data-picked]')?.getAttribute('data-path')).toBe('copy-of-first-steps.hjson');
    expect(scrolled).toContain('copy-of-first-steps.hjson');
    setup.backend.match(() => true);
  });
});
