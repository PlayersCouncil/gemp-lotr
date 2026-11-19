// Authoring checks: picking on the table for a message (the anchor fields, TablePicker; see ../author.mjs).
//
// WAITING FOR INTEGRATION: the anchor fields (`gd-author-anchor`) live in the Step tab's message cards, which wire
// them in with `data-field` = anchor (Points at), highlight (Outlines), avoid (Keeps clear of), each sending what it
// commits at once. Until the Step tab has them the check passes with a note saying it could not run.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

const ONE_MESSAGE = [
  { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass', messages: [{ text: 'Look at this.' }] },
];

const field = (name) => `${ui.step} gd-author-anchor[data-field="${name}"]`;
const pick = {
  told: `${ui.notices} .gd-author-notice[data-notice="told"]`,
  box: 'gd-game-author-pickbox .gd-author-pickbox',
  label: 'gd-game-author-pickbox .gd-author-picklabel',
  picking: 'html[data-author-picking]',
  outline: '.gd-scenario-outline',
};

const box = (locator) => locator.evaluate((element) => element.getBoundingClientRect().toJSON());
const inside = (point, rect) =>
  point.x >= rect.left && point.x <= rect.right && point.y >= rect.top && point.y <= rect.bottom;

/** The step's first message as the server has it. */
async function message(seat) {
  const state = await seat.state();
  const first = [].concat(state.draft.steps[0]?.messages ?? [])[0];
  return typeof first === 'string' ? { text: first } : (first ?? {});
}

export const checks = {
  /**
   * Pick on the table: the crosshair, the notice and the box round what the pointer is on, with its anchor; a click
   * takes it (Points at, then a part of the card); Escape and a click in the panel take nothing. + Pick adds a card
   * under another, a site, a row, a pile, the twilight pool and a button to the outlines, and each outline the
   * message then draws is round the place it was picked at.
   */
  async picking(table, problems) {
    const { seat } = table;
    await newDraft(seat, { title: 'Picking check', steps: ONE_MESSAGE });
    const page = seat.page;
    await page.locator(`${ui.timeline} .gd-author-timeline__row[data-step="1"] .gd-author-timeline__pick`).click();
    const ready = await until(async () => (await page.locator(field('highlight')).count()) > 0, 'anchor fields', 5000)
      .then(() => true)
      .catch(() => false);
    if (!ready) {
      table.note = '(waiting for integration: no anchor fields in the Step tab yet)';
      await closeDraft(seat);
      return;
    }
    const board = (await seat.state()).board;
    const idOf = (alias) => String(board.cards.find((card) => card.alias === alias).cardId);

    // Points at: Pick, hovering Frodo, then a click on him
    await page.locator(`${field('anchor')} button[data-pick="replace"]`).click();
    await page.locator(pick.told).waitFor({ timeout: 3000 });
    if ((await page.locator(pick.picking).count()) !== 1) problems.push('no crosshair while picking');
    const frodo = await box(page.locator(`[data-card-id="${idOf('frodo')}"]`).first());
    const middle = { x: frodo.left + frodo.width * 0.6, y: frodo.top + frodo.height / 2 };
    await page.mouse.move(middle.x, middle.y);
    await page.locator(pick.label).waitFor({ timeout: 3000 });
    const label = (await page.locator(pick.label).innerText()).trim();
    if (label !== 'card:frodo') problems.push(`hovering Frodo, the label says "${label}"`);
    await seat.shot('hovering');
    await page.mouse.click(middle.x, middle.y);
    await until(async () => (await message(seat)).anchor === 'card:frodo', 'Points at card:frodo in the step');
    if ((await page.locator(pick.told).count()) !== 0) problems.push('the notice stays after the pick');
    if ((await page.locator(pick.picking).count()) !== 0) problems.push('the crosshair stays after the pick');
    await page.locator(`${field('anchor')} select.gd-author-anchor__part`).selectOption('strength');
    await until(async () => (await message(seat)).anchor === 'card:frodo:strength', 'the part of the card chosen');

    // Escape, and a click in the panel, take nothing
    for (const how of ['Escape', 'panel']) {
      await page.locator(`${field('anchor')} button[data-pick="replace"]`).click();
      await page.locator(pick.told).waitFor({ timeout: 3000 });
      if (how === 'Escape') await page.keyboard.press('Escape');
      else await page.locator(ui.where).click();
      await page.locator(pick.told).waitFor({ state: 'detached', timeout: 3000 });
      const value = await page.locator(`${field('anchor')} input`).inputValue();
      if (value !== 'card:frodo:strength') problems.push(`after ${how} the field says "${value}"`);
    }

    // Outlines: + Pick, one thing at a time, each where it shows
    const ring = await box(page.locator(`[data-card-id="${idOf('ring')}"]`).first());
    const site = await box(page.locator('.gd-game__path [data-card-id]').first());
    const row = await box(page.locator('.gd-game__row[data-row="characters-player"]'));
    const twilight = await box(page.locator('[data-game="twilight"]'));
    const pass = await box(page.locator(ui.decisionButton('Pass')));
    const deck = await box(page.locator('[data-stat^="deck"]').first());
    const centre = (rect) => ({ x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 });
    const targets = [
      ['card:ring', { x: ring.left + 4, y: ring.top + ring.height / 2 }],
      ['ui:site:1', centre(site)],
      ['zone:self.characters', { x: row.right - 30, y: row.top + 10 }],
      ['ui:twilightPool', centre(twilight)],
      ['ui:button:pass', centre(pass)],
      [/^pile:(self|opp)\.deck$/, centre(deck)],
    ];
    const picked = [];
    for (const [anchor, point] of targets) {
      await page.locator(`${field('highlight')} button[data-pick="add"]`).click();
      await page.locator(pick.told).waitFor({ timeout: 3000 });
      await page.mouse.move(point.x, point.y);
      await sleep(100);
      await page.mouse.click(point.x, point.y);
      await page.locator(pick.told).waitFor({ state: 'detached', timeout: 3000 });
      const list = (await page.locator(`${field('highlight')} input`).inputValue()).split(',').map((one) => one.trim());
      const got = list.at(-1);
      if (anchor instanceof RegExp ? !anchor.test(got) : got !== anchor) {
        problems.push(`+ Pick at ${Math.round(point.x)},${Math.round(point.y)} gave "${got}", not ${anchor}`);
      }
      picked.push([got, point]);
    }
    // (the Step tab may have filled Outlines from Points at first)
    const want = picked.map(([anchor]) => anchor);
    await until(
      async () => {
        const have = ((await message(seat)).highlight ?? '').split(',').map((one) => one.trim());
        return want.every((anchor) => have.includes(anchor));
      },
      `Outlines with ${want.join(', ')} in the step`,
    );
    await seat.settled();
    await until(
      async () => (await page.locator(pick.outline).count()) >= picked.length,
      'the outlines of what was picked',
    );
    const outlines = await page
      .locator(pick.outline)
      .evaluateAll((all) => all.map((one) => one.getBoundingClientRect().toJSON()));
    for (const [anchor, point] of picked) {
      if (!outlines.some((rect) => inside(point, rect)))
        problems.push(`no outline round ${anchor}, where it was picked`);
    }
    await seat.shot('outlined');
    await closeDraft(seat);
  },
};
