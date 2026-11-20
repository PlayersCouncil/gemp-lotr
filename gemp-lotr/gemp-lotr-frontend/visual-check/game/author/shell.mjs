// Authoring checks: the panel itself and its Timeline (see ../author.mjs).

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

const TWO_STEPS = [
  { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass', messages: [{ text: 'First, pass.' }] },
  { when: 'freeps, action, phase(fellowship), turn(2)', allow: 'pass' },
];

const box = (locator) => locator.evaluate((element) => element.getBoundingClientRect().toJSON());

export const checks = {
  /** Docked beside the table, which keeps to the rest of the window; folded, the table takes the room back. */
  async dock({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    const view = page.viewportSize();
    const panel = await box(page.locator(ui.panel));
    const game = await box(page.locator(ui.game));
    if (Math.round(panel.right) !== view.width)
      problems.push(`the panel ends at ${panel.right}, not the window's right`);
    if (Math.round(game.right) > Math.round(panel.left)) {
      problems.push(`the table runs to ${game.right}, under the panel (from ${panel.left})`);
    }
    const bubble = await box(page.locator(ui.bubble));
    if (bubble.right > panel.left) problems.push(`the message bubble runs to ${bubble.right}, under the panel`);
    await seat.shot('docked');

    await page.locator(ui.fold).click();
    await page.locator(`${ui.panel}[data-folded]`).waitFor({ timeout: 5000 });
    await sleep(300);
    const folded = await box(page.locator(ui.panel));
    const wide = await box(page.locator(ui.game));
    if (folded.width > 40) problems.push(`folded, the panel is still ${folded.width}px wide`);
    if (Math.abs(wide.right - folded.left) > 1) problems.push('folded, the table does not take the room back');
    await seat.shot('folded');
    await page.locator(ui.fold).click();
    await page.locator(`${ui.panel}:not([data-folded])`).waitFor({ timeout: 5000 });

    for (const tab of ['step', 'opponent', 'board', 'scenario', 'timeline']) await seat.showTab(tab);
    await closeDraft(seat);
  },

  /** The Timeline lists the script; ▶ on a row plays the script to it in a fresh game, the panel as it was. */
  async timeline({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    const rows = page.locator(`${ui.timeline} .gd-author-timeline__row`);
    if ((await rows.count()) !== 2) problems.push(`${await rows.count()} rows for a script of two steps`);
    const first = seat.gameId();
    await rows.nth(1).locator('button.gd-author-timeline__go').click();
    await until(() => seat.gameId() !== first, 'a fresh game after ▶ on the second row', 20000);
    await seat.settled();
    const where = await seat.whereLine();
    if (!/Step 2 of 2/.test(where)) problems.push(`after ▶ on step 2 the panel says "${where}"`);
    if ((await page.locator(`${ui.tab('timeline')}[aria-selected="true"]`).count()) !== 1) {
      problems.push('the fresh game did not come back on the Timeline tab');
    }
    await seat.shot('at-step-2');
    await closeDraft(seat);
  },

  /** Leaving with changes in no file asks first; Stay stays, Discard closes the draft. */
  async leave({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    await page.locator(ui.menuButton('leave')).click();
    await page.locator(ui.leave).waitFor({ timeout: 5000 });
    await seat.shot('asked');
    await page.locator(ui.leaveButton('stay')).click();
    await page
      .locator(ui.leave)
      .waitFor({ state: 'detached', timeout: 5000 })
      .catch(() => undefined);
    if ((await page.locator(ui.leave).count()) !== 0) problems.push('Stay left the question up');
    await page.locator(ui.menuButton('leave')).click();
    await page.locator(ui.leaveButton('discard')).click();
    await until(async () => !(await seat.state()).draft, 'the draft closed after Discard');
  },
};
