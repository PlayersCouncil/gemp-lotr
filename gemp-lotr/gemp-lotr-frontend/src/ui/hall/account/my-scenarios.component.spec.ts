import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';
import { HallReadyOffers } from '../../../core/hall/ready/hall-ready-offers.service';

describe('MyScenariosComponent', () => {
  async function shown() {
    const setup = await setUpAccountPages();
    await setup.harness.navigateByUrl('/account/scenarios');
    answerAccount(setup.backend, '/scenarios/mine', 'scenarios-mine.json');
    await drawn(setup.harness);
    const host = setup.harness.fixture.nativeElement.querySelector('gd-account-scenarios') as HTMLElement;
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
});
