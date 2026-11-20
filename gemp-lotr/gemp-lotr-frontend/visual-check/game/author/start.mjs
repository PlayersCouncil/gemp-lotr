// Authoring checks: where the story starts, the markers and site control, and paths of sites without numbers (see
// ../author.mjs). Every check starts from a new draft with Aragorn and two Goblin Runners on the table (Frodo and the
// Ring for each seat, the stock path), makes its changes through the Board tab (or the Scenario tab) and looks at the
// table, the server's draft and the file's text afterwards, and again once the page has loaded again.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

const start = `${ui.board} gd-author-board-start`;
const at = {
  submit: `${ui.board} button[data-action=submit]`,
  lines: `${ui.board} .gd-author-board-gathered__text`,
  where: (where) => `${ui.board} [data-where=${where}] input`,
  pick: (part) => `${start} .gd-author-board-start__row[data-part=${part}] select`,
  number: (part) => `${start} .gd-author-board-start__row[data-part=${part}] input`,
  fight: (character) => `${start} [data-character="${character}"] select[data-control=fight]`,
  problem: `${start} [data-problem]`,
  laid: (number) => `${ui.board} .gd-author-board-laid__site[data-site="${number}"]`,
  marker: (number, seat) =>
    `${ui.board} .gd-author-board-laid__site[data-site="${number}"] button[data-action=marker][data-seat=${seat}]`,
  control: (number) => `${ui.board} .gd-author-board-laid__site[data-site="${number}"] [data-control=control] select`,
  pathSite: (number) => `${ui.board} .gd-author-board-path__site[data-seat=freeps][data-site="${number}"]`,
};

/** The draft every check starts from: a character and two minions on the table besides the Ring-bearers. */
const TOP = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', aragorn: '1_89' },
    shadow: { oppFrodo: '10_121', oppRing: '1_2', runner: '1_191', runner2: '1_191' },
  },
  setup: { freeps: { characters: 'aragorn' }, shadow: { minions: 'runner, runner2' } },
};

/** A new draft, its game's page on the Board tab. */
async function onBoard(seat, top = TOP) {
  await newDraft(seat, { top });
  await seat.showTab('board');
  await seat.page.locator(start).waitFor({ timeout: 10000 });
}

async function gathered(page) {
  return (await page.locator(at.lines).allInnerTexts()).map((line) => line.trim());
}

/** Waits for a fresh game (the page loads again on it), and for the panel to be back on the Board tab. */
async function freshGame(seat, from, what) {
  await until(() => seat.gameId() !== from, `a fresh game after ${what}`, 25000);
  await seat.settled();
  await seat.page.locator(start).waitFor({ timeout: 10000 });
}

/** Submits what is gathered and waits for the fresh game it brings. */
async function submitFresh(seat, what) {
  const from = seat.gameId();
  await seat.page.locator(at.submit).click();
  await freshGame(seat, from, what);
}

/** Types a number into a field of the start, and gathers it (Enter). */
async function typeNumber(page, part, value) {
  await page.locator(at.number(part)).fill(String(value));
  await page.locator(at.number(part)).press('Enter');
}

/** The file's text as the server would write it. */
async function source(seat) {
  return (await (await seat.who.get('/scenarios/author/source')).json()).source ?? '';
}

/** Loads the page again; the panel comes back on the Board tab. */
async function reload(seat) {
  await seat.page.reload();
  await seat.settled();
  await seat.page.locator(start).waitFor({ timeout: 10000 });
}

export const checks = {
  /**
   * A story that starts at site 3 in the maneuver phase with twilight, made through the UI: the fresh game has three
   * sites on the path, both markers, the pool and the phase; the file says it; the page loaded again shows it.
   */
  async 'start-late'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(at.pick('site')).selectOption('3');
    await page.locator(at.pick('phase')).selectOption('maneuver');
    await typeNumber(page, 'twilight', 4);
    const lines = await gathered(page);
    const wanted = ['start at site 3', 'start in the maneuver phase', 'twilight 4 at the start'];
    if (JSON.stringify(lines) !== JSON.stringify(wanted)) problems.push(`gathered: ${JSON.stringify(lines)}`);
    if ((await page.locator(`${start} .gd-author-board-start__late`).count()) !== 1)
      problems.push('no late start said');
    await page.locator(start).scrollIntoViewIfNeeded();
    await seat.shot('gathered');
    await submitFresh(seat, 'submitting the start');
    const state = await seat.state();
    const path = await page.locator('[data-zone="ADVENTURE_PATH"]').count();
    if (path !== 3) problems.push(`the table has ${path} sites on the path, not 3`);
    if (JSON.stringify(state.board.markers) !== JSON.stringify({ freeps: 3, shadow: 1 }))
      problems.push(`markers: ${JSON.stringify(state.board.markers)}`);
    const pool = (await page.locator('[data-game="twilight"]').first().innerText()).trim();
    if (pool !== '4') problems.push(`the twilight pool shows ${pool}`);
    if (state.position.phase !== 'maneuver') problems.push(`the game is in the ${state.position.phase} phase`);
    if (!/maneuver/i.test(await page.locator('[data-game="phase"]').first().innerText()))
      problems.push('the table does not show the maneuver phase');
    if (JSON.stringify(state.draft.setup?.startAt) !== JSON.stringify({ site: 3, phase: 'maneuver', twilight: 4 }))
      problems.push(`setup.startAt: ${JSON.stringify(state.draft.setup?.startAt)}`);
    const text = await source(seat);
    if (!/startAt: \{\s*site: 3\s*phase: maneuver\s*twilight: 4\s*\}/.test(text))
      problems.push("the file's text does not say startAt site 3, maneuver, twilight 4");
    if ((await gathered(page)).length !== 0) problems.push('the changes are still gathered after the submit');
    await page.locator(start).scrollIntoViewIfNeeded();
    await seat.shot('started');
    await reload(seat);
    if ((await page.locator(at.pick('site')).inputValue()) !== '3')
      problems.push('after loading again, the site is not 3');
    if ((await page.locator(at.pick('phase')).inputValue()) !== 'maneuver')
      problems.push('after loading again, the phase is not maneuver');
    await closeDraft(seat);
  },

  /** Skirmishes assigned at a Skirmish start: the game asks which to fight first; the file says them. */
  async 'start-skirmishes'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(at.pick('site')).selectOption('2');
    await page.locator(at.pick('phase')).selectOption('skirmish');
    await page.locator(at.fight('frodo')).selectOption('runner');
    await page.locator(at.fight('aragorn')).selectOption('runner2');
    const lines = await gathered(page);
    if (!lines.includes('skirmishes: frodo: runner; aragorn: runner2'))
      problems.push(`gathered: ${JSON.stringify(lines)}`);
    await page.locator(start).scrollIntoViewIfNeeded();
    await seat.shot('assigned');
    await submitFresh(seat, 'submitting the skirmishes');
    const state = await seat.state();
    if (JSON.stringify(state.board.skirmishes) !== JSON.stringify({ frodo: ['runner'], aragorn: ['runner2'] }))
      problems.push(`the game's skirmishes: ${JSON.stringify(state.board.skirmishes)}`);
    const asked = state.waiting.human;
    if (!asked || !/skirmish/i.test(asked.text)) problems.push(`the game asks: ${asked ? asked.text : 'nothing'}`);
    if (!/skirmishes: \{\s*frodo: runner\s*aragorn: runner2\s*\}/.test(await source(seat)))
      problems.push("the file's text does not have the skirmishes");
    await seat.shot('asked');
    await reload(seat);
    if ((await page.locator(`${start} [data-character="frodo"] [data-minion="runner"]`).count()) !== 1)
      problems.push('after loading again, runner is not in frodo’s skirmish');
    await closeDraft(seat);
  },

  /**
   * Markers and site control: put and controlled on the board as it stands (the same game), then to the setup (a
   * fresh game, setup.controlled in the file).
   */
  async 'start-markers'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(at.marker(2, 'freeps')).click();
    await page.locator(at.marker(3, 'freeps')).click();
    await page.locator(at.marker(3, 'shadow')).click();
    await page.locator(at.control(1)).selectOption('shadow');
    let lines = await gathered(page);
    if (JSON.stringify(lines) !== JSON.stringify(['marker(freeps, 3)', 'marker(shadow, 3)', 'control(1, shadow)']))
      problems.push(`gathered now: ${JSON.stringify(lines)}`);
    await page.locator(`${ui.board} gd-author-board-laid`).scrollIntoViewIfNeeded();
    await seat.shot('gathered-now');
    const from = seat.gameId();
    await page.locator(at.submit).click();
    await until(async () => (await seat.state()).board.path?.[0]?.controlledBy === 'shadow', 'site 1 controlled');
    await seat.settled();
    if (seat.gameId() !== from) problems.push('a change to the board as it stands brought a fresh game');
    let state = await seat.state();
    if (JSON.stringify(state.board.markers) !== JSON.stringify({ freeps: 3, shadow: 3 }))
      problems.push(`markers now: ${JSON.stringify(state.board.markers)}`);
    // (site 1, controlled, is in the Shadow seat's support area: sites 2 and 3 are on the path)
    const laid = await until(
      async () =>
        (await page.locator('[data-zone="ADVENTURE_PATH"]').count()) === 2 &&
        (await page.locator('[data-blueprint-id="1_323"][data-zone="SUPPORT"]').count()) === 1,
      'sites 2 and 3 on the path, site 1 controlled',
    ).catch(() => false);
    if (!laid) problems.push('the table does not show sites 2 and 3 on the path and site 1 controlled');
    if ((await page.locator(at.control(1)).inputValue()) !== 'shadow')
      problems.push('the tab does not show site 1 controlled');
    await seat.shot('made-now');
    // To the setup: a fresh game, the file says it
    await page.locator(at.where('setup')).check();
    await page.locator(at.marker(2, 'freeps')).click();
    await page.locator(at.marker(3, 'freeps')).click();
    await page.locator(at.marker(3, 'shadow')).click();
    await page.locator(at.control(2)).selectOption('shadow');
    lines = await gathered(page);
    if (JSON.stringify(lines) !== JSON.stringify(['marker(freeps, 3)', 'marker(shadow, 3)', 'control(2, shadow)']))
      problems.push(`gathered for the setup: ${JSON.stringify(lines)}`);
    await submitFresh(seat, 'submitting markers to the setup');
    state = await seat.state();
    if (JSON.stringify(state.draft.setup?.controlled) !== JSON.stringify({ shadow: '2' }))
      problems.push(`setup.controlled: ${JSON.stringify(state.draft.setup?.controlled)}`);
    if (state.start.site !== 3 || state.start.opponentSite !== 3)
      problems.push(`the start: site ${state.start.site}, Shadow marker ${state.start.opponentSite}`);
    if (!/controlled: \{\s*shadow: 2\s*\}/.test(await source(seat))) problems.push("the file's text has no controlled");
    if (state.board.path?.[1]?.controlledBy !== 'shadow')
      problems.push('the fresh game does not have site 2 controlled');
    await reload(seat);
    const tick = page.locator(`${start} [data-seat=shadow] gd-author-check[data-site="2"] input`);
    if (!(await tick.isChecked())) problems.push('after loading again, site 2 is not ticked controlled');
    await page.locator(start).scrollIntoViewIfNeeded();
    await seat.shot('made-setup');
    await closeDraft(seat);
  },

  /** A start the server refuses: its words where the start is changed, what was typed kept. */
  async 'start-refused'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    seat.refusalsExpected.push(/\/scenarios\/author\/edit$/);
    await page.locator(at.pick('site')).selectOption('3');
    await page.locator(at.pick('phase')).selectOption('maneuver');
    await typeNumber(page, 'moves', 5);
    const from = seat.gameId();
    await page.locator(at.submit).click();
    const said = await until(async () => {
      const shown = page.locator(at.problem);
      return (await shown.count()) === 1 ? (await shown.innerText()).trim() : null;
    }, 'the refusal said in the start');
    if (!/move/.test(said)) problems.push(`the start says: ${said}`);
    if (seat.gameId() !== from) problems.push('a refused start brought a fresh game');
    if ((await page.locator(at.number('moves')).inputValue()) !== '5') problems.push('the moves typed are gone');
    if (!(await gathered(page)).includes('moves this turn: 5')) problems.push('the moves are no longer gathered');
    await page.locator(start).scrollIntoViewIfNeeded();
    await seat.shot('refused');
    await closeDraft(seat);
  },

  /** A card in a skirmish already assigned taken out of the scenario: the draft is still read, without it. */
  async 'start-drop'({ seat }, problems) {
    await onBoard(seat, {
      ...TOP,
      setup: { ...TOP.setup, startAt: { site: 2, phase: 'assignment' }, skirmishes: { frodo: 'runner + runner2' } },
    });
    const page = seat.page;
    await page.locator(`${ui.board} .gd-author-board-card[data-alias="runner"] button[data-action=drop]`).click();
    await submitFresh(seat, 'taking a card out');
    const state = await seat.state();
    if (state.draft.cards?.shadow?.runner !== undefined) problems.push('runner is still one of the cards');
    if (JSON.stringify(state.draft.setup?.skirmishes) !== JSON.stringify({ frodo: 'runner2' }))
      problems.push(`setup.skirmishes: ${JSON.stringify(state.draft.setup?.skirmishes)}`);
    if ((state.leftOut ?? []).length > 0) problems.push(`left out: ${JSON.stringify(state.leftOut)}`);
    if (JSON.stringify(state.board.skirmishes) !== JSON.stringify({ frodo: ['runner2'] }))
      problems.push(`the game's skirmishes: ${JSON.stringify(state.board.skirmishes)}`);
    await closeDraft(seat);
  },

  /**
   * The format changed to scenario_shadows in the Scenario tab, and a path of sites without numbers built in the
   * Board tab: a site added from the search (those without a number first), another put first, the one laid first in
   * the fresh game.
   */
  async 'start-shadows'({ seat }, problems) {
    await onBoard(seat, { ...TOP, sites: { freeps: '11_236, 11_261', shadow: '11_236, 11_261' } });
    const page = seat.page;
    await seat.showTab('scenario');
    await page.locator(`${ui.scenario} gd-author-select[data-part=format] select`).selectOption('scenario_shadows');
    await until(async () => (await seat.state()).draft.format === 'scenario_shadows', 'the format changed');
    // (a format is a change that brings a fresh game: settled waits for the page to be on it)
    await seat.settled();
    if (!/format: scenario_shadows/.test(await source(seat))) problems.push("the file's text has not the format");
    await seat.showTab('board');
    await page.locator(start).waitFor({ timeout: 10000 });
    await until(async () => (await page.locator(`${at.pathSite(1)}[data-unnumbered]`).count()) === 1, 'a listed path');
    if ((await page.locator(`${at.pathSite(2)} .gd-author-board-path__number`).innerText()).trim() !== '#2')
      problems.push('the second site is not shown as listed #2');
    // A site found by name: those without a number first
    await page.locator(`${ui.board} gd-author-board-path > button[data-action=add-site]`).click();
    await page.locator(`${ui.board} gd-author-board-path gd-author-card-search input`).fill('guardroom');
    const result = page.locator(`${ui.board} gd-author-board-path .gd-author-search__result`).first();
    await result.waitFor({ timeout: 5000 });
    if (!/no number/.test(await result.innerText())) problems.push(`the site found says: ${await result.innerText()}`);
    let from = seat.gameId();
    await result.click();
    await freshGame(seat, from, 'adding a site');
    let state = await seat.state();
    if (state.board.sites.freeps.length !== 3) problems.push(`the path has ${state.board.sites.freeps.length} sites`);
    // The third put first, twice up
    for (const place of [3, 2]) {
      from = seat.gameId();
      await page.locator(`${at.pathSite(place)} button[data-action=raise]`).click();
      await freshGame(seat, from, 'putting a site first');
    }
    state = await seat.state();
    if (state.board.sites.freeps[0]?.id !== '11_247')
      problems.push(`the first listed is ${state.board.sites.freeps[0]?.id}`);
    if (state.board.path?.[0]?.blueprintId !== '11_247')
      problems.push(`the game laid ${state.board.path?.[0]?.blueprintId} first`);
    if (!/site3: 11_247/.test(await source(seat))) problems.push("the file's text does not keep the site's name");
    await page.locator(`${ui.board} gd-author-board-path`).scrollIntoViewIfNeeded();
    await sleep(200);
    await seat.shot('listed');
    await reload(seat);
    if ((await page.locator(`${at.pathSite(1)} .gd-author-board-path__first`).count()) !== 1)
      problems.push('after loading again, no site is laid first');
    await closeDraft(seat);
  },
};
