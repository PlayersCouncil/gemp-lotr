// Authoring checks: the Scenario tab (see ../author.mjs). Every check makes a new draft of its own; the two that save
// write only files of their own new draft (and its copy) and delete them again.

import fs from 'node:fs';
import path from 'node:path';
import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';
import { changed, reloaded, same, source } from './puzzles.mjs';

/** Where the server keeps its scenarios, when it runs on this machine (files saved by a check are deleted there). */
const DATA = process.env.GEMP_SCENARIOS ?? '/etc/gemp-lotr/scenarios';

const TWO_STEPS = [
  { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass', messages: [{ text: 'First, pass.' }] },
  { when: 'freeps, action, phase(fellowship), turn(2)', allow: 'pass' },
];

/** The Scenario tab's parts (its DOM contract: scenario-tab.component.ts and scenario/). */
const tab = {
  section: (name) => `${ui.scenario} section[data-section="${name}"]`,
  part: (name) => `${ui.scenario} [data-part="${name}"]`,
  select: (name) => `${ui.scenario} gd-author-select[data-part="${name}"] select`,
  tick: (name) => `${ui.scenario} gd-author-check[data-part="${name}"] input`,
  button: (name) => `${ui.scenario} button[data-author="${name}"]`,
  report: `${ui.scenario} pre.gd-author-scenario__report`,
  filingPath: `${ui.scenario} input[data-filing-path]`,
  filingPreview: `${ui.scenario} [data-filing-preview]`,
  notice: (kind) => `${ui.notices} .gd-author-notice[data-notice="${kind}"]`,
  submit: 'dialog.gd-dialog[open][data-dialog="author-submit"]',
  setting: (name) => `gd-game-tabs [data-setting="${name}"]`,
};

/** Opens the Scenario tab (a fresh game comes back on it). */
async function openTab(seat) {
  await seat.showTab('scenario');
  await seat.page.locator(tab.section('file')).waitFor({ timeout: 5000 });
}

/** Does `change` (a change that starts a fresh game) and waits for the panel on the new game. */
async function freshGame(seat, what, change) {
  const before = seat.gameId();
  await change();
  await until(() => seat.gameId() !== before, `a fresh game after ${what}`, 20000);
  await seat.settled();
  if ((await seat.page.locator(`${ui.tab('scenario')}[aria-selected="true"]`).count()) !== 1) {
    throw new Error(`the fresh game after ${what} did not come back on the Scenario tab`);
  }
}

/** The draft's own part as the server has it now. */
async function draftPart(seat, name) {
  return (await seat.state()).draft[name];
}

/** Deletes a file the check saved (`shown`: its path as the state's `path` gives it; `file`: in the player's folder). */
async function deleteSaved(seat, { shown, file }) {
  const own = path.join(DATA, shown);
  if (fs.existsSync(own)) {
    fs.rmSync(own);
    return;
  }
  // (a server elsewhere: its own way, which keeps a copy in its archive)
  await seat.who.post('/scenarios/mine/delete', { path: file }).catch(() => undefined);
}

/** Opens the game's Settings tab (the player's own settings, as the table plays by them). */
async function settingsTab(page) {
  const link = page.locator('gd-game-tabs a[data-tab="settings"]');
  if ((await link.getAttribute('aria-selected')) !== 'true') await link.click();
  await page.locator(tab.setting('autoAccept')).waitFor({ timeout: 5000 });
}

export const checks = {
  /** A new title goes into the game in hand: the panel's head and the scenario's menu follow, with no fresh game. */
  async 'scenario-title'({ seat }, problems) {
    await newDraft(seat, { title: 'Before', steps: TWO_STEPS });
    const page = seat.page;
    await openTab(seat);
    const game = seat.gameId();
    await page.locator(tab.part('title')).fill('After the change');
    await page.locator(tab.part('title')).press('Enter');
    await until(async () => (await draftPart(seat, 'title')) === 'After the change', 'the new title in the draft');
    await seat.settled();
    if (seat.gameId() !== game) problems.push('a new title started a fresh game');
    const head = await page.locator(ui.title).innerText();
    if (head !== '* After the change') problems.push(`the panel's head says "${head}"`);
    const menu = await page.locator('.gd-scenario-menu__title').innerText();
    if (menu !== 'After the change') problems.push(`the scenario's menu says "${menu}"`);
    // (a field typed in is not disturbed by a fresh state)
    await page.locator(tab.part('summary')).fill('Half typed');
    await seat.call('settings', { recording: 'false' });
    await page.locator(ui.tab('timeline')).click();
    await page.locator(ui.tab('scenario')).click();
    await seat.shot('titled');
    await closeDraft(seat);
  },

  /** A new draft saved (its file written, the star gone), then saved as a copy through the form, the path tidied. */
  async 'scenario-save'({ seat }, problems) {
    const id = await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    const saved = [];
    try {
      await openTab(seat);
      if (!/not saved yet/.test(await page.locator(`${ui.scenario} .gd-author-scenario__path`).innerText())) {
        problems.push('a new draft is not said to be unsaved');
      }
      await page.locator(tab.button('save')).click();
      await page.locator(tab.notice('saved')).waitFor({ timeout: 20000 });
      const state = await seat.state();
      saved.push({ shown: state.path, file: state.file });
      if (state.dirty || !state.onDisk) problems.push('saved, the draft is still dirty or not on disk');
      if ((await page.locator(`${ui.panel}[data-unsaved]`).count()) !== 0)
        problems.push('saved, the head is still red');
      if ((await page.locator(ui.title).innerText()).startsWith('*')) problems.push('saved, the title keeps its star');
      if (!/Saved to/.test(await page.locator(tab.report).innerText())) problems.push('no save report under Scenario');
      const local = path.join(DATA, state.path);
      if (fs.existsSync(DATA) && !fs.existsSync(local)) problems.push(`no file at ${local}`);
      await seat.shot('saved');

      // Save as: the name typed with a stray character and the ending, tidied
      await page.locator(tab.button('save-as')).click();
      await page.locator(tab.filingPath).fill(`  ${id} copy!.hjson`);
      const preview = await page.locator(tab.filingPreview).innerText();
      const tidy = `${id} copy.hjson`;
      if (!preview.includes(`tutorials/${tidy}`) || !preview.includes(`(id: ${id}-copy)`)) {
        problems.push(`the form says it goes to "${preview}"`);
      }
      // (nothing usable typed: marked, and nothing sent)
      await page.locator(tab.filingPath).fill(' !! ');
      await page.locator(tab.button('file')).click();
      await page
        .locator(`${tab.filingPath}[data-bad]`)
        .waitFor({ timeout: 3000 })
        .catch(() => problems.push('an empty name is not marked'));
      await page.locator(tab.filingPath).fill(`  ${id} copy!.hjson`);
      await seat.shot('filing');
      await page.locator(tab.filingPath).press('Enter');
      const copied = await until(async () => {
        const now = await seat.state();
        return now.file === `tutorials/${tidy}` ? now : null;
      }, 'the draft saved as the copy');
      saved.push({ shown: copied.path, file: copied.file });
      if (copied.draft.id !== `${id}-copy`) problems.push(`the copy's id is ${copied.draft.id}`);
      await page.locator(tab.filingPath).waitFor({ state: 'detached', timeout: 5000 });
      await seat.settled();
      if ((await page.locator(tab.notice('saved')).count()) !== 1) problems.push('no saved notice after the copy');
      if (!(await page.locator(`${ui.scenario} .gd-author-scenario__path`).innerText()).includes(tidy)) {
        problems.push("the path line doesn't say the copy's file");
      }
      await seat.shot('copied');
    } finally {
      await closeDraft(seat);
      for (const file of saved) await deleteSaved(seat, file);
    }
  },

  /** A rule added and taken away again, each a fresh game; with nothing left, `rules` goes. */
  async 'scenario-rules'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    await openTab(seat);
    const before = JSON.stringify((await draftPart(seat, 'rules')) ?? null);
    const skip = `${ui.scenario} [data-seat="shadow"] [data-part="skip"] gd-author-check[data-phase="maneuver"] input`;
    await freshGame(seat, 'a skipped phase', () => page.locator(skip).check());
    const rules = await draftPart(seat, 'rules');
    const effect = JSON.stringify(rules?.shadow ?? []);
    if (!effect.includes('skipPhase') || !effect.includes('maneuver'))
      problems.push(`the rules are ${JSON.stringify(rules)}`);
    if (!(await page.locator(skip).isChecked())) problems.push('the skipped phase is not ticked on the fresh game');
    const hand = `${ui.scenario} [data-seat="freeps"] input[data-part="hand"]`;
    await freshGame(seat, 'a hand size', async () => {
      await page.locator(hand).fill('-2');
      await page.locator(hand).press('Enter');
    });
    if (!JSON.stringify(await draftPart(seat, 'rules')).includes('"amount":-2'))
      problems.push('the hand size is not in the rules');
    await seat.shot('rules');
    await freshGame(seat, 'the hand size taken back', async () => {
      await page.locator(hand).fill('0');
      await page.locator(hand).press('Enter');
    });
    await freshGame(seat, 'the phase unticked', () => page.locator(skip).uncheck());
    // (a new draft is one-sided; with that alone left, the seats' parts are gone)
    const left = JSON.stringify((await draftPart(seat, 'rules')) ?? null);
    if (left !== before) problems.push(`taken back, the rules are ${left}, not ${before}`);
    await freshGame(seat, 'one-sided unticked', () => page.locator(tab.tick('one-sided')).uncheck());
    const none = await draftPart(seat, 'rules');
    if (none !== undefined && none !== null) problems.push(`with nothing left the rules are ${JSON.stringify(none)}`);
    await closeDraft(seat);
  },

  /** How the player loses: also when a companion is killed, and what they are told. */
  async 'scenario-outcome'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    await openTab(seat);
    await freshGame(seat, 'a way to lose', () => page.locator(tab.select('lose')).selectOption('companion'));
    await freshGame(seat, 'what the player is told', async () => {
      await page.locator(tab.part('lose-message')).fill('Every companion counts.');
      await page.locator(tab.part('lose-message')).blur();
    });
    const outcome = await draftPart(seat, 'outcome');
    if (outcome?.lose?.type !== 'hasCardInDeadPile' || outcome?.loseMessage !== 'Every companion counts.') {
      problems.push(`the outcome is ${JSON.stringify(outcome)}`);
    }
    if ((await page.locator(tab.select('lose')).inputValue()) !== 'companion')
      problems.push('the way to lose is not shown');
    await seat.shot('outcome');
    await closeDraft(seat);
  },

  /** The player's settings the draft fixes: an animation speed and the deck reveal, as the table plays by them. */
  async 'scenario-settings'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    await openTab(seat);
    await settingsTab(page);
    // (the check's browser plays at speed 4; a tutorial dismisses deck reveals unless it says otherwise)
    if (!(await page.locator(tab.setting('autoDismissDeckReveal')).isChecked())) {
      problems.push("a tutorial's table doesn't dismiss deck reveals");
    }
    await freshGame(seat, 'an animation speed', () => page.locator(tab.select('animation-speed')).selectOption('-2'));
    await settingsTab(page);
    const slider = await page.locator(tab.setting('animation-slider')).inputValue();
    if (slider !== '-2') problems.push(`with the speed set to -2 the table's slider is at ${slider}`);
    await freshGame(seat, 'the deck reveal kept', () => page.locator(tab.tick('auto-dismiss-deck-reveal')).uncheck());
    await settingsTab(page);
    if (await page.locator(tab.setting('autoDismissDeckReveal')).isChecked()) {
      problems.push('unticked in the draft, the table still dismisses deck reveals');
    }
    await freshGame(seat, 'auto-accept off', () => page.locator(tab.tick('auto-accept')).uncheck());
    await settingsTab(page);
    if (await page.locator(tab.setting('autoAccept')).isChecked())
      problems.push('unticked in the draft, the table still auto-accepts');
    const options = await draftPart(seat, 'options');
    if (options?.animationSpeed !== -2 || options?.autoDismissDeckReveal !== false || options?.autoAccept !== false) {
      problems.push(`the options are ${JSON.stringify(options)}`);
    }
    // Back to the player's own speed: the part goes from the file
    await freshGame(seat, "the player's own speed", () => page.locator(tab.select('animation-speed')).selectOption(''));
    if ((await draftPart(seat, 'options'))?.animationSpeed !== undefined) problems.push('the speed stayed in the file');
    await seat.shot('settings');
    await closeDraft(seat);
  },

  /** The rest of the file: a part changed as HJSON; a mistake in it refused in the server's words, the text kept. */
  async 'scenario-parts'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    seat.refusalsExpected.push(/\/scenarios\/author\/edit$/);
    await openTab(seat);
    const part = `${ui.scenario} details[data-part="options"]`;
    await page.locator(`${part} > summary`).click();
    await freshGame(seat, 'the options applied', async () => {
      await page.locator(`${part} textarea`).fill('{\n\tbubbleGap: "3"\n}');
      await page.locator(`${part} button[data-author="apply"]`).click();
    });
    if ((await draftPart(seat, 'options'))?.bubbleGap !== '3') problems.push('the options part was not applied');
    if ((await page.locator(tab.part('bubble-gap')).inputValue()) !== '3')
      problems.push('How it plays does not show the gap');
    await page.locator(`${part} > summary`).click();
    const broken = '{\n\tbubbleGap: "3"\n\tnoSuchOption: 1\n}';
    const game = seat.gameId();
    await page.locator(`${part} textarea`).fill(broken);
    await page.locator(`${part} button[data-author="apply"]`).click();
    const said = await until(
      async () =>
        (await page.locator(tab.notice('problem')).count()) > 0
          ? page.locator(tab.notice('problem')).innerText()
          : null,
      'the refusal said',
    );
    if (!/noSuchOption/.test(said)) problems.push(`the refusal says "${said.slice(0, 120)}"`);
    if ((await page.locator(`${part} textarea`).inputValue()) !== broken)
      problems.push('the refused text was not kept');
    if (seat.gameId() !== game) problems.push('a refused part started a fresh game');
    await seat.shot('refused');
    await closeDraft(seat);
  },

  /** Check reports; See the file shows it; Submit for review asks first (Cancel submits nothing); Close asks. */
  async 'scenario-check'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    let submitted = false;
    await page.route('**/scenarios/author/submit', (route) => {
      submitted = true;
      return route.abort();
    });
    await openTab(seat);
    await page.locator(tab.button('check')).click();
    const report = await until(
      async () => ((await page.locator(tab.report).count()) > 0 ? page.locator(tab.report).innerText() : null),
      'the check report',
      60000,
    );
    if (!/^(Scenario validated\.|It does not play through\.)/.test(report))
      problems.push(`the report says "${report.slice(0, 80)}"`);
    await seat.shot('checked');
    await page.locator(tab.button('see-file')).click();
    await until(async () => /id: check-/.test(await page.locator(tab.report).innerText()), 'the file shown');
    await page.locator(tab.button('put-away')).click();
    await page
      .locator(tab.report)
      .waitFor({ state: 'detached', timeout: 3000 })
      .catch(() => problems.push('Put that away left the report'));
    if ((await page.locator(tab.button('submit')).count()) === 1) {
      await page.locator(tab.button('submit')).click();
      await page.locator(tab.submit).waitFor({ timeout: 5000 });
      await seat.shot('submit-asked');
      await page.locator(`${tab.submit} button[data-action="cancel"]`).click();
      await page
        .locator(tab.submit)
        .waitFor({ state: 'detached', timeout: 5000 })
        .catch(() => undefined);
      if ((await page.locator(tab.submit).count()) !== 0) problems.push('Cancel left the submit dialog up');
    } else problems.push("no Submit for review on a draft in the author's own folder");
    await sleep(300);
    if (submitted) problems.push('Cancel submitted the draft');
    // Close the draft (it has changes): asked first in the page; Cancel keeps it, OK closes it
    await page.locator(tab.button('close')).click();
    await page.locator(ui.confirm).waitFor({ timeout: 5000 });
    await page.locator(ui.confirmButton('cancel')).click();
    if (!(await seat.state()).draft) problems.push('Cancel closed the draft');
    await page.locator(tab.button('close')).click();
    await page.locator(ui.confirmButton('ok')).click();
    await until(async () => !(await seat.state()).draft, 'the draft closed');
  },

  /**
   * The kind made a sandbox, one of its tools unticked and ticked again (every tool: the field left out), and
   * take-backs off: on the server, in the file, and in the tab after a reload.
   */
  async 'scenario-kind-undo'({ seat }, problems) {
    await newDraft(seat, { steps: TWO_STEPS });
    const page = seat.page;
    await openTab(seat);
    let state = await changed(
      seat,
      'the kind made a sandbox',
      () => page.locator(tab.select('kind')).selectOption('sandbox'),
      (now) => now.draft.kind === 'sandbox',
    );
    const kinds = await page.locator(`${tab.select('kind')} option`).evaluateAll((all) => all.map((one) => one.value));
    if (!same(kinds, state.kinds ?? [])) problems.push(`the kinds offered are ${kinds}`);
    if ((await page.locator(`${ui.scenario} [data-part="all-tools"]`).count()) !== 1) {
      problems.push('a sandbox saying no tools is not shown with all of them');
    }
    state = await changed(
      seat,
      'a tool unticked',
      () => page.locator(`${ui.scenario} gd-author-check[data-tool="copy"] input`).uncheck(),
      (now) => now.sandboxTools?.said === true,
    );
    const wanted = state.sandboxTools.names.filter((name) => name !== 'copy').join(', ');
    if (state.draft.options?.tools !== wanted) problems.push(`the tools are "${state.draft.options?.tools}"`);
    if (!(await source(seat)).includes(`tools: ${wanted}`)) problems.push("the file's text lacks the tools");
    await seat.shot('tools');
    state = await changed(
      seat,
      'take-backs off',
      () => page.locator(tab.select('undo')).selectOption('off'),
      (now) => now.draft.options?.undo === 'off',
    );
    if (state.puzzle?.undo !== false) problems.push('take-backs are not off in the state');
    if (!(await source(seat)).includes('undo: off')) problems.push("the file's text lacks undo: off");
    await reloaded(seat, 'scenario');
    if ((await page.locator(tab.select('undo')).inputValue()) !== 'off')
      problems.push('undo is not off after a reload');
    if (await page.locator(`${ui.scenario} gd-author-check[data-tool="copy"] input`).isChecked()) {
      problems.push('copy is ticked after a reload');
    }
    state = await changed(
      seat,
      'the tool ticked again',
      () => page.locator(`${ui.scenario} gd-author-check[data-tool="copy"] input`).check(),
      (now) => now.sandboxTools?.said === false,
    );
    if (state.draft.options?.tools !== undefined) problems.push('every tool ticked does not leave the field out');
    await closeDraft(seat);
  },
};
