#!/usr/bin/env node
// The PC account's behaviour in a real browser, without a server (gemp-lotr/PC-ACCOUNT.md): the login page, My
// Account and the hall against account/stubs.mjs's stand-in for the server's PC account addresses. No screenshots:
// each scenario does what a player does and checks what the page shows and what was sent. Writes <out>/results.json;
// exits with 1 when a scenario fails. See README.md.
//
//   npm run check:account
//   npm run check:account -- --only create-taken,link-good --out report-account
//   CHECK_BROWSER_EXECUTABLE=/usr/bin/google-chrome npm run check:account
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { routeToStub, STUB_ORIGIN } from '../stub-server.mjs';
import { FakePcServer, read, sessionWithAliases } from './stubs.mjs';

const { values: options } = parseArgs({
  options: {
    out: { type: 'string', default: 'report-account' },
    only: { type: 'string' },
  },
});

const NG = '/gemp-lotr/ng/';
/** The hall: the app's root, which opens its Game Hall tab. */
const HALL = [NG, `${NG}hall`];
const SIGN_IN = '/gemp-lotr-server/argonauth/login';
const WAIT = 5000;

class Failed extends Error {}

/** What a scenario can do: its page (more with `newPage`), the stand-in server, and checks that stop it at the first failure. */
function tools(context, page, server) {
  const visible = async (selector) => {
    try {
      await page.locator(selector).first().waitFor({ state: 'visible', timeout: WAIT });
    } catch {
      throw new Failed(`${selector} is not shown`);
    }
  };
  /**
   * Reads until what is read is right, or the wait runs out. The page redraws after an answer in its own time, so
   * something read once, straight after the click that changes it, is sometimes read too soon.
   */
  const eventually = async (read, isRight, wrong) => {
    const until = Date.now() + WAIT;
    for (;;) {
      const actual = await read();
      if (isRight(actual)) return;
      if (Date.now() >= until) throw new Failed(wrong(actual));
      await page.waitForTimeout(50);
    }
  };
  const same = (expected) => (actual) => JSON.stringify(actual) === JSON.stringify(expected);
  return {
    page,
    server,
    newPage: () => context.newPage(),
    open: (address) => page.goto(STUB_ORIGIN + address),
    visible,
    absent: async (selector) => {
      // Let the page settle first: a part drawn once the session is known
      await page.waitForTimeout(300);
      const count = await page.locator(selector).count();
      if (count) throw new Failed(`${selector} is shown (${count})`);
    },
    text: async (selector, expected) => {
      await visible(selector);
      await eventually(
        async () => (await page.locator(selector).first().innerText()).replace(/\s+/g, ' ').trim(),
        same(expected),
        (actual) => `${selector} reads "${actual}", not "${expected}"`,
      );
    },
    texts: async (selector, expected) => {
      await visible(selector);
      await eventually(
        async () => (await page.locator(selector).allInnerTexts()).map((t) => t.trim()),
        same(expected),
        (actual) => `${selector}: ${JSON.stringify(actual)}, not ${JSON.stringify(expected)}`,
      );
    },
    attribute: async (selector, name, expected) => {
      await visible(selector);
      await eventually(
        () => page.locator(selector).first().getAttribute(name),
        same(expected),
        (actual) => `${selector} [${name}] is ${JSON.stringify(actual)}, not ${JSON.stringify(expected)}`,
      );
    },
    disabled: async (selector, expected = true) => {
      await visible(selector);
      await eventually(
        () => page.locator(selector).first().isDisabled(),
        same(expected),
        (actual) => `${selector} is ${actual ? '' : 'not '}disabled`,
      );
    },
    click: async (selector) => {
      await visible(selector);
      await page.locator(selector).first().click();
    },
    fill: async (selector, value) => {
      await visible(selector);
      await page.locator(selector).first().fill(value);
    },
    /** An input's value. */
    value: async (selector, expected) => {
      await visible(selector);
      await eventually(
        () => page.locator(selector).first().inputValue(),
        same(expected),
        (actual) => `${selector} holds "${actual}", not "${expected}"`,
      );
    },
    /**
     * The browser arrives within the wait at `where`: a path of the stub's (one of several, given an array), with the
     * query `search` if given, or an outside address it starts with.
     */
    arrives: async (where, search = null, target = page) => {
      const paths = [where].flat();
      const matches = (url) =>
        paths.some((p) =>
          p.startsWith('http')
            ? url.href.startsWith(p)
            : url.origin === STUB_ORIGIN && url.pathname === p && (search === null || url.search === search),
        );
      try {
        await target.waitForURL(matches, { timeout: WAIT, waitUntil: 'commit' });
      } catch {
        throw new Failed(`at ${target.url()}, not ${paths.join(' or ')}${search ?? ''}`);
      }
    },
    /** The POSTs sent so far, as "path body". */
    sent: (expected) => {
      const actual = server.posts.map(({ path: p, body }) => `${p} ${new URLSearchParams(body).toString()}`);
      if (JSON.stringify(actual) !== JSON.stringify(expected)) {
        throw new Failed(`sent ${JSON.stringify(actual)}, not ${JSON.stringify(expected)}`);
      }
    },
  };
}

const ALIAS = (name) => `.gd-alias-list__alias[data-alias="${name}"]`;
const ENTER = (name) => `${ALIAS(name)} [data-action="enter"]`;
const ACTION = (action) => `:is(.gd-alias-list__actions, .gd-alias-list__head) [data-action="${action}"]`;
const FORM = 'form.gd-pc-form__form';
const FORM_ERROR = `${FORM} .gd-pc-form__error`;
const NAMES = '.gd-alias-list__name';

/** Opens the create form and submits `name`. */
async function create(t, name) {
  await t.click(ACTION('create'));
  await t.fill(`${FORM} input[name="name"]`, name);
  await t.click(`${FORM} [data-action="submit"]`);
}

async function link(t, login, password) {
  await t.click(ACTION('link'));
  await t.fill(`${FORM} input[name="login"]`, login);
  await t.fill(`${FORM} input[name="password"]`, password);
  await t.click(`${FORM} [data-action="submit"]`);
}

const SCENARIOS = [
  // ---- test mode: no PC account anywhere
  {
    name: 'test-login',
    note: 'test mode: the login page as it always was',
    server: { session: 'argonauth-session-test.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.visible('.gd-login__form .gd-button[data-action="register"]');
      await t.absent('[data-action="pc-login"]');
      await t.absent('.gd-login__box');
      await t.absent('gd-alias-list *');
      await t.open(`${NG}login?pc=failed`);
      await t.visible('.gd-login__form');
      await t.absent('.gd-login__notice');
    },
  },
  {
    name: 'test-hall-account',
    note: "test mode: nothing on My Account; the hall's Log Out still logs out",
    server: { session: 'argonauth-session-test.json' },
    run: async (t) => {
      await t.open(`${NG}account`);
      await t.visible('.gd-account-overview__ignores');
      await t.absent('gd-pc-account-block *');
      await t.click('.gd-hall__presence .gd-hall__logout');
      await t.arrives(`${NG}login`);
      t.sent(['/logout scope=alias']);
      await t.visible('.gd-login__form .gd-button[data-action="register"]');
    },
  },
  // ---- the login page in optional mode
  {
    name: 'optional-nobody',
    note: 'optional mode, nobody: Legacy Login beside PC SSO (Sign in, Register), no registration form',
    server: { session: 'argonauth-session-nobody.json' },
    run: async (t) => {
      await t.open(`${NG}login?return=%2Fevents%2Fleagues`);
      await t.text('[data-action="pc-login"]', 'Sign in with PC Account');
      await t.attribute('[data-action="pc-login"]', 'href', `${SIGN_IN}?return=%2Fevents%2Fleagues`);
      await t.text('.gd-login__column[data-column="legacy"] .gd-login__heading', 'Legacy Login');
      await t.text('.gd-login__column[data-column="sso"] .gd-login__heading', 'PC SSO');
      await t.text('[data-action="pc-register"]', 'Register PC Account');
      await t.attribute('[data-action="pc-register"]', 'href', `${SIGN_IN}?register=1&return=%2Fevents%2Fleagues`);
      await t.visible('.gd-login__column[data-column="legacy"] .gd-login__forgot');
      await t.absent('.gd-login__intro');
      await t.absent('[data-action="register"]');
      await t.absent('.gd-login__input[name="password-repeated"]');
      await t.visible('.gd-login__input[name="password"]');
      await t.click('[data-action="pc-login"]');
      await t.arrives('/gemp-lotr-server/argonauth/login', '?return=%2Fevents%2Fleagues');
    },
  },
  {
    name: 'optional-columns',
    note: 'the two columns: side by side at 1366 and 1024 (tops level, the rule as tall as both), stacked at 600',
    server: { session: 'argonauth-session-nobody.json' },
    run: async (t) => {
      const box = (selector) => t.page.locator(selector).first().boundingBox();
      for (const width of [1366, 1024]) {
        await t.page.setViewportSize({ width, height: 768 });
        await t.open(`${NG}login`);
        await t.visible('.gd-login__columns');
        const [legacy, rule, sso] = await Promise.all(
          ['[data-column="legacy"]', '.gd-login__rule', '[data-column="sso"]'].map(box),
        );
        if (Math.abs(legacy.y - sso.y) > 1)
          throw new Failed(`${width}: the columns' tops differ (${legacy.y}, ${sso.y})`);
        if (!(legacy.x + legacy.width <= rule.x && rule.x + rule.width <= sso.x)) {
          throw new Failed(`${width}: the columns are not side by side either side of the rule`);
        }
        if (rule.height < Math.max(legacy.height, sso.height) - 1) throw new Failed(`${width}: the rule is too short`);
        if (sso.x + sso.width > width) throw new Failed(`${width}: PC SSO runs off the window`);
      }
      await t.page.setViewportSize({ width: 600, height: 900 });
      await t.open(`${NG}login`);
      await t.visible('.gd-login__columns');
      const [legacy, rule, sso] = await Promise.all(
        ['[data-column="legacy"]', '.gd-login__rule', '[data-column="sso"]'].map(box),
      );
      if (!(legacy.y + legacy.height <= rule.y && rule.y + rule.height <= sso.y)) {
        throw new Failed('600: the columns are not stacked either side of the rule');
      }
      if (rule.height > 2 || rule.width < 50) throw new Failed('600: the rule is not across');
    },
  },
  {
    name: 'login-claimed',
    note: 'a password login to an account a PC account has claimed (423): said where the refusals go',
    server: { session: 'argonauth-session-nobody.json', loginStatus: 423 },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.fill('.gd-login__input[name="login"]', 'Samwise');
      await t.fill('.gd-login__input[name="password"]', 'anything');
      await t.click('.gd-login__form [data-action="submit"]');
      await t.text('.gd-login__error', 'That account is linked to a PC Account, please use that to log in.');
      await t.visible('.gd-login__form');
      t.sent(['/login login=Samwise&password=anything']);
    },
  },
  {
    name: 'optional-alignment',
    note: 'the box: Login and Password flush, each heading centred over its column, the SSO buttons one width under theirs; inside the window down to 360',
    server: { session: 'argonauth-session-nobody.json' },
    run: async (t) => {
      const box = (selector) => t.page.locator(selector).first().boundingBox();
      const centre = (b) => b.x + b.width / 2;
      for (const [width, height] of [
        [1366, 768],
        [1024, 768],
        [390, 800],
        [360, 800],
      ]) {
        await t.page.setViewportSize({ width, height });
        await t.open(`${NG}login`);
        await t.visible('.gd-login__box .gd-login__columns');
        const [login, password] = await Promise.all(
          ['.gd-login__input[name="login"]', '.gd-login__input[name="password"]'].map(box),
        );
        if (Math.abs(login.x - password.x) > 0.5 || Math.abs(login.width - password.width) > 0.5) {
          throw new Failed(
            `${width}: the inputs are not flush (${login.x}/${login.width}, ${password.x}/${password.width})`,
          );
        }
        for (const column of ['legacy', 'sso']) {
          const [heading, body] = await Promise.all([
            box(`[data-column="${column}"] .gd-login__heading`),
            box(column === 'legacy' ? '[data-column="legacy"] .gd-login__form' : '.gd-login__sso'),
          ]);
          if (Math.abs(centre(heading) - centre(body)) > 1) {
            throw new Failed(
              `${width}: ${column}'s heading is not centred over it (${centre(heading)}, ${centre(body)})`,
            );
          }
        }
        const [signIn, register] = await Promise.all(
          ['[data-action="pc-login"]', '[data-action="pc-register"]'].map(box),
        );
        if (Math.abs(signIn.width - register.width) > 0.5 || Math.abs(signIn.x - register.x) > 0.5) {
          throw new Failed(`${width}: the SSO buttons differ in width or place`);
        }
        const panel = await box('.gd-login__box');
        if (panel.x < 0 || panel.x + panel.width > width) throw new Failed(`${width}: the box overflows the window`);
      }
    },
  },
  {
    name: 'optional-notices',
    note: 'where a PC sign-in came back to: cancelled, failed',
    server: { session: 'argonauth-session-nobody.json' },
    run: async (t) => {
      await t.open(`${NG}login?pc=cancelled`);
      await t.text('.gd-login__notice', 'PC Account sign-in was cancelled.');
      await t.open(`${NG}login?pc=failed`);
      await t.text('.gd-login__notice', 'PC Account sign-in failed. Try again.');
    },
  },
  {
    name: 'list',
    note: 'a PC account with several aliases: the list instead of the form',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.text('.gd-alias-list__signed-in', 'Signed in as Frodo Baggins');
      await t.texts(NAMES, ['Frodo', 'Underhill', 'Ringbearer']);
      await t.texts(`${ALIAS('Frodo')} [data-action="enter"]`, ['Enter Gemp']);
      await t.texts('.gd-alias-list__actions .gd-button', ['Create New Gemp Profile', 'Link Existing Gemp Account']);
      await t.text('.gd-alias-list__head [data-action="logout"]', 'Log Out');
      await t.text('.gd-alias-list__head .gd-alias-list__who', 'Frodo Baggins');
      await t.absent('.gd-login__form');
      await t.absent('.gd-alias-list__pager');
    },
  },
  {
    name: 'enter',
    note: 'Enter Gemp: the alias is entered and the hall opens',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.click(ENTER('Underhill'));
      await t.arrives(HALL);
      t.sent(['/enter playerId=900002']);
    },
  },
  {
    name: 'enter-unlinked',
    note: 'Enter Gemp on an alias unlinked at the account service: it leaves the list, and the message stays',
    server: { session: 'argonauth-session-aliases.json', refuse: { enter: [403, 'not-linked'] } },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.click(ENTER('Ringbearer'));
      await t.texts(NAMES, ['Frodo', 'Underhill']);
      await t.text('.gd-alias-list__refusal', 'That profile is no longer linked to your PC Account.');
      await t.attribute('.gd-alias-list__refusal', 'role', 'alert');
      await t.arrives(`${NG}login`);
      t.sent(['/enter playerId=900003']);
    },
  },
  {
    name: 'gate',
    note: "this browser's alias busy: only it can be entered, none created, linking still can",
    server: { session: 'argonauth-session.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.text(
        '.gd-alias-list__blocked',
        'Underhill is in a game, queue, table or tournament: finish or leave it to switch.',
      );
      await t.disabled(ENTER('Frodo'));
      await t.disabled(ENTER('Underhill'), false);
      await t.disabled(ACTION('create'));
      await t.disabled(ACTION('link'), false);
      await t.click(ENTER('Underhill'));
      await t.arrives(HALL);
      t.sent(['/enter playerId=900002']);
    },
  },
  {
    name: 'busy-elsewhere',
    note: 'an alias busy in another browser gates nothing here: it is only tagged',
    server: {
      session: {
        ...read('argonauth-session-aliases.json'),
        aliases: read('argonauth-session-aliases.json').aliases.map((a) => ({ ...a, busy: a.name === 'Underhill' })),
      },
    },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.texts(`${ALIAS('Underhill')} .gd-alias-list__tag`, ['playing']);
      await t.absent('.gd-alias-list__blocked');
      await t.disabled(ENTER('Frodo'), false);
      await t.disabled(ACTION('create'), false);
      await t.click(ENTER('Underhill'));
      await t.arrives(HALL);
      t.sent(['/enter playerId=900002']);
    },
  },
  {
    name: 'paging',
    note: 'eleven aliases: ten to a page',
    server: { session: sessionWithAliases(11) },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.text('.gd-alias-list__page', 'Page 1 of 2');
      const first = await t.page.locator(NAMES).count();
      if (first !== 10) throw new Failed(`${first} aliases on the first page`);
      await t.disabled('[data-action="previous"]');
      await t.click('[data-action="next"]');
      await t.texts(NAMES, ['Alias11']);
      await t.text('.gd-alias-list__page', 'Page 2 of 2');
      await t.disabled('[data-action="next"]');
    },
  },
  {
    name: 'list-layout',
    note: 'the list on the login page: the sentence centred on the box with Log Out on its line, Create and Link centred, a row opened after the last profile; at 1366 and 390',
    server: {
      session: 'argonauth-session-aliases.json',
      refuse: { link: [409, 'linked-elsewhere'] },
    },
    run: async (t) => {
      const box = (selector) => t.page.locator(selector).first().boundingBox();
      const centre = (b) => b.x + b.width / 2;
      for (const [width, height] of [
        [1366, 768],
        [390, 800],
      ]) {
        await t.page.setViewportSize({ width, height });
        await t.open(`${NG}login`);
        await t.text('.gd-alias-list__signed-in', 'Signed in as Frodo Baggins');
        const [panel, sentence, logout, create, link] = await Promise.all(
          [
            '.gd-login__box',
            '.gd-alias-list__signed-in',
            '.gd-alias-list__head [data-action="logout"]',
            ACTION('create'),
            ACTION('link'),
          ].map(box),
        );
        const list = await box('gd-alias-list');
        if (Math.abs(centre(sentence) - centre(list)) > 1.5) {
          throw new Failed(`${width}: the sentence is not centred (${centre(sentence)}, ${centre(list)})`);
        }
        if (
          width > 480 &&
          (Math.abs(centre(sentence) - centre(panel)) > 1.5 ||
            logout.y > sentence.y + sentence.height ||
            logout.y + logout.height < sentence.y)
        ) {
          throw new Failed(`${width}: the sentence is off the box's centre, or Log Out is not on its line`);
        }
        // Side by side, the pair is centred; wrapped, each on its line is
        const lines =
          Math.abs(create.y - link.y) < 1 ? [{ x: create.x, width: link.x + link.width - create.x }] : [create, link];
        if (lines.some((line) => Math.abs(centre(line) - centre(list)) > 1.5)) {
          throw new Failed(`${width}: Create and Link are not centred`);
        }
        const weight = await t.page
          .locator('.gd-alias-list__who')
          .evaluate((b) => Number(getComputedStyle(b).fontWeight));
        if (weight < 600) throw new Failed(`${width}: the name is not bold (${weight})`);
        // A row opened sits after the last profile and before the buttons, as a profile's row
        await t.click(ACTION('create'));
        await t.visible('.gd-alias-list__new');
        const [last, row, field] = await Promise.all(
          [`${ALIAS('Ringbearer')}`, '.gd-alias-list__new', `${FORM} input[name="name"]`].map(box),
        );
        const createAfter = await box(ACTION('create'));
        if (!(last.y + last.height <= row.y + 1 && row.y + row.height <= createAfter.y)) {
          throw new Failed(`${width}: the row is not between the last profile and the buttons`);
        }
        if (row.x < panel.x || row.x + row.width > panel.x + panel.width)
          throw new Failed(`${width}: the row leaves the box`);
        await t.attribute(`${FORM} input[name="name"]`, 'placeholder', 'Profile Name');
        await t.attribute(`${FORM} input[name="name"]`, 'aria-label', 'Profile Name');
        await t.absent(`${FORM} .gd-pc-form__label`);
        if ((await t.page.locator(`${FORM} label`).first().innerText()).trim() !== '') {
          throw new Failed(`${width}: the field has a visible label`);
        }
        if (field.width < 80) throw new Failed(`${width}: the field is too narrow (${field.width})`);
        // Link swaps it; a refusal under it, in the theme's error colour
        await t.click(ACTION('link'));
        await t.attribute(FORM, 'data-form', 'link');
        await t.fill(`${FORM} input[name="login"]`, 'Taken');
        await t.fill(`${FORM} input[name="password"]`, 'secret');
        await t.page.keyboard.press('Enter');
        await t.text(`${FORM} .gd-pc-form__error`, 'That account belongs to another PC Account.');
        const [colour, token] = await t.page.evaluate(() => {
          const error = document.querySelector('.gd-pc-form__error');
          const probe = document.createElement('span');
          probe.style.color = 'var(--theme-error)';
          error.parentElement.append(probe);
          const result = [getComputedStyle(error).color, getComputedStyle(probe).color];
          probe.remove();
          return result;
        });
        if (colour !== token)
          throw new Failed(`${width}: the refusal is ${colour}, not the theme's error colour ${token}`);
        await t.value(`${FORM} input[name="login"]`, 'Taken');
        await t.value(`${FORM} input[name="password"]`, '');
        t.server.refuse.link = [409, 'linked-elsewhere'];
      }
    },
  },
  {
    name: 'landing-no-alias',
    note: 'a PC sign-in with no alias lands in Create, Link plainly beside it, and enters the return place',
    server: { session: 'argonauth-session-no-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login?open=create&return=%2Fevents%2Fleagues`);
      await t.attribute(FORM, 'data-form', 'create');
      await t.text('.gd-alias-list__empty', 'No Gemp profile yet: create one, or link an account you had.');
      await t.disabled(ACTION('link'), false);
      // The row is where the first profile would be, under the empty list's line, Link under it within the window
      await t.visible('.gd-alias-list__aliases > li.gd-alias-list__new:only-child');
      const focused = await t.page.evaluate(() => document.activeElement?.getAttribute('name'));
      if (focused !== 'name') throw new Failed(`the focus is on ${focused}, not the name field`);
      const [empty, row, link] = await Promise.all(
        ['.gd-alias-list__empty', '.gd-alias-list__new', ACTION('link')].map((selector) =>
          t.page.locator(selector).first().boundingBox(),
        ),
      );
      if (!(empty.y + empty.height <= row.y && row.y + row.height <= link.y)) {
        throw new Failed('the empty line, the row and Link are not in that order');
      }
      if (link.y + link.height > 768) throw new Failed('Link Existing Gemp Account is below the window');
      await t.fill(`${FORM} input[name="name"]`, 'Pippin');
      await t.click(`${FORM} [data-action="submit"]`);
      await t.arrives(`${NG}events/leagues`);
      t.sent(['/create name=Pippin']);
    },
  },
  {
    name: 'create-good',
    note: 'Create New Gemp Profile: made, entered, the hall opens',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await create(t, 'Merry');
      await t.arrives(HALL);
      t.sent(['/create name=Merry']);
    },
  },
  {
    name: 'create-bad-name',
    note: "a name outside Gemp's rule is not sent",
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await create(t, 'no spaces!');
      await t.text(FORM_ERROR, 'A name is 2 to 30 letters, digits, _ or -.');
      t.sent([]);
    },
  },
  {
    name: 'create-taken',
    note: 'a name taken: said under the form, the page stays',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await create(t, 'frodo');
      await t.text(FORM_ERROR, 'That name is taken.');
      await t.arrives(`${NG}login`);
      t.sent(['/create name=frodo']);
    },
  },
  {
    name: 'create-reauth',
    note: '401 reauth: through a fresh PC sign-in, back with the form open',
    server: { session: 'argonauth-session-aliases.json', refuse: { create: [401, 'reauth'] } },
    run: async (t) => {
      await t.open(`${NG}login`);
      await create(t, 'Merry');
      await t.arrives('/gemp-lotr-server/argonauth/login', '?then=create');
      // The callback's landing
      await t.open(`${NG}login?open=create`);
      await t.attribute(FORM, 'data-form', 'create');
    },
  },
  {
    name: 'create-stale',
    note: 'a lapsed PC sign-in: Create goes through a sign-in before the form',
    server: { session: 'argonauth-session-stale.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.click(ACTION('create'));
      await t.arrives('/gemp-lotr-server/argonauth/login', '?then=create');
      t.sent([]);
    },
  },
  {
    name: 'link-good',
    note: 'Link Existing Gemp Account: the alias joins the list, nothing entered',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.click(ACTION('link'));
      await t.text(
        `${FORM} .gd-pc-form__note`,
        'Once linked, that account is entered through your PC Account; its password no longer logs in.',
      );
      await t.click(`${FORM} [data-action="cancel"]`);
      await link(t, 'OldSam', 'secret');
      await t.texts(NAMES, ['Frodo', 'Underhill', 'Ringbearer', 'OldSam']);
      await t.absent(FORM);
      await t.arrives(`${NG}login`);
      t.sent(['/link login=OldSam&password=secret']);
    },
  },
  {
    name: 'link-bad',
    note: 'wrong credentials: said under the form',
    server: { session: 'argonauth-session-aliases.json', refuse: { link: [401, 'bad-credentials'] } },
    run: async (t) => {
      await t.open(`${NG}login`);
      await link(t, 'OldSam', 'wrong');
      await t.text(FORM_ERROR, 'Wrong username or password, or that account cannot be linked with a password.');
      await t.value(`${FORM} input[name="password"]`, '');
      await t.value(`${FORM} input[name="login"]`, 'OldSam');
    },
  },
  {
    name: 'link-elsewhere',
    note: 'an account of another PC account: said under the form',
    server: { session: 'argonauth-session-aliases.json', refuse: { link: [409, 'linked-elsewhere'] } },
    run: async (t) => {
      await t.open(`${NG}login`);
      await link(t, 'OldSam', 'secret');
      await t.text(FORM_ERROR, 'That account belongs to another PC Account.');
      await t.texts(NAMES, ['Frodo', 'Underhill', 'Ringbearer']);
    },
  },
  {
    name: 'logout-all',
    note: "the alias list's Log Out: everything, to the PC Account's sign-out",
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.click(ACTION('logout'));
      await t.arrives(read('argonauth-logout-all.json').next.split('?')[0]);
      t.sent(['/logout scope=all']);
    },
  },
  {
    name: 'logout-hall',
    note: "the hall's Log Out: the alias only, to the login page's list",
    server: { session: 'argonauth-session.json' },
    run: async (t) => {
      await t.open(`${NG}hall`);
      await t.text('.gd-hall__presence .gd-hall__logout', 'Log Out');
      await t.click('.gd-hall__logout');
      await t.arrives(`${NG}login`);
      t.sent(['/logout scope=alias']);
      await t.text('.gd-alias-list__signed-in', 'Signed in as Frodo Baggins');
    },
  },
  {
    name: 'password-login',
    note: 'a password login: its one account, Enter Gemp and Log Out',
    server: { session: 'argonauth-session-password.json' },
    run: async (t) => {
      await t.open(`${NG}login`);
      await t.texts(NAMES, ['Samwise']);
      await t.text('.gd-alias-list__signed-in', 'Logged in as Samwise');
      await t.texts('.gd-alias-list__head .gd-button', ['Log Out']);
      await t.absent('.gd-alias-list__actions');
      await t.click(ENTER('Samwise'));
      await t.arrives(HALL);
      t.sent([]);
      await t.click('.gd-hall__logout');
      await t.arrives(`${NG}login`);
      t.sent(['/logout scope=alias']);
      await t.visible('.gd-login__form');
    },
  },
  // ---- My Account
  {
    name: 'account-pc',
    note: 'My Account, a PC account: the alias list and Manage PC Account',
    server: { session: 'argonauth-session.json' },
    run: async (t) => {
      await t.open(`${NG}account`);
      await t.visible('gd-pc-account-block gd-alias-list[data-place="account"]');
      await t.texts(`gd-pc-account-block ${NAMES}`, ['Frodo', 'Underhill']);
      await t.attribute('.gd-pc-account-block__manage', 'href', 'https://auth.lotrtcgpc.net/');
      await t.attribute('.gd-pc-account-block__manage', 'target', '_blank');
      await t.absent('.gd-pc-account-block__connect');
    },
  },
  {
    name: 'account-password',
    note: 'My Account, a password login not linked: Connect PC Account, and the lines a Connect comes back to',
    server: { session: 'argonauth-session-password.json' },
    run: async (t) => {
      await t.open(`${NG}account`);
      await t.visible('.gd-pc-account-block__about');
      await t.text('button.gd-pc-account-block__connect', 'Connect PC Account');
      await t.absent('gd-pc-account-block gd-alias-list *');
      await t.absent('gd-pc-account-block gd-pc-form');
      for (const [pc, line] of [
        ['linked-elsewhere', 'This account already belongs to another PC Account.'],
        ['failed', 'PC Account sign-in failed. Try again.'],
        ['cancelled', 'PC Account sign-in was cancelled.'],
      ]) {
        await t.open(`${NG}account?pc=${pc}`);
        await t.text('.gd-pc-account-block__notice', line);
      }
    },
  },
  {
    name: 'connect-good',
    note: "Connect PC Account: the account's password, then the PC sign-in the server names",
    server: { session: 'argonauth-session-password.json' },
    run: async (t) => {
      await t.open(`${NG}account`);
      await t.click('button.gd-pc-account-block__connect');
      await t.attribute(FORM, 'data-form', 'connect');
      await t.text(
        `${FORM} .gd-pc-form__note`,
        'Once connected, this account is entered through your PC Account; its password no longer logs in.',
      );
      await t.fill(`${FORM} input[name="password"]`, 'secret');
      await t.click(`${FORM} [data-action="submit"]`);
      await t.arrives(read('argonauth-connect.json').next.split('?')[0]);
      t.sent(['/connect password=secret']);
    },
  },
  {
    name: 'connect-bad',
    note: 'Connect with a wrong password: said under the form, nothing else happens; Cancel closes it',
    server: { session: 'argonauth-session-password.json', refuse: { connect: [401, 'bad-credentials'] } },
    run: async (t) => {
      await t.open(`${NG}account`);
      await t.click('button.gd-pc-account-block__connect');
      await t.fill(`${FORM} input[name="password"]`, 'wrong');
      await t.click(`${FORM} [data-action="submit"]`);
      await t.text(FORM_ERROR, 'Wrong password.');
      await t.value(`${FORM} input[name="password"]`, '');
      await t.arrives(`${NG}account`);
      await t.click(`${FORM} [data-action="cancel"]`);
      await t.visible('button.gd-pc-account-block__connect');
      t.sent(['/connect password=wrong']);
    },
  },
  {
    name: 'account-linked',
    note: 'My Account, a password login linked: says so',
    server: { session: 'argonauth-session-password-linked.json' },
    run: async (t) => {
      await t.open(`${NG}account?pc=connected`);
      await t.text('.gd-pc-account-block__notice', 'This account is now connected to your PC Account.');
      await t.text(
        '.gd-pc-account-block__connected',
        'This account is connected to a PC Account. Sign in with the PC Account to see all your Gemp profiles.',
      );
      await t.absent('.gd-pc-account-block__connect');
    },
  },
  // ---- other tabs
  {
    name: 'second-tab',
    note: 'entering an alias in one tab reloads the deck builder open in another',
    server: { session: 'argonauth-session-aliases.json' },
    run: async (t) => {
      const other = await t.newPage();
      await other.goto(`${STUB_ORIGIN}${NG}deckbuilder`);
      await other.locator('.gd-page').waitFor({ state: 'visible', timeout: WAIT });
      await other.evaluate(() => (window.gempMarker = 'before'));
      await t.open(`${NG}login`);
      await t.click(ENTER('Frodo'));
      await t.arrives(HALL);
      try {
        await other.waitForFunction(() => window.gempMarker === undefined, null, { timeout: WAIT });
      } catch {
        throw new Failed('the other tab did not reload');
      }
      await other.locator('.gd-page').waitFor({ state: 'visible', timeout: WAIT });
    },
  },
];

const only = options.only ? options.only.split(',') : null;
const scenarios = SCENARIOS.filter((s) => !only || only.includes(s.name));
if (only && scenarios.length !== only.length) {
  console.error(`Unknown scenario: ${only.filter((name) => !SCENARIOS.some((s) => s.name === name)).join(', ')}`);
  process.exit(2);
}

const outDir = path.resolve(options.out);
fs.mkdirSync(outDir, { recursive: true });
const browser = await launchBrowser();

async function check(scenario) {
  const log = [];
  const server = new FakePcServer(scenario.server);
  const context = await browser.newContext({ viewport: { width: 1366, height: 768 } });
  // The built-in chat, not Discord's embed (a new tab's about:blank has no storage)
  await context.addInitScript(() => {
    try {
      localStorage.setItem('gemp.hallChat', 'builtin');
    } catch {
      // about:blank
    }
  });
  await routeToStub(context, log, { answer: server.answers() });
  // The account service's own pages (Log Out from the alias list ends there): a page that says where it is
  await context.route(/^https:\/\/auth\.lotrtcgpc\.net\//, (route) =>
    route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>PC Account</title>' }),
  );
  const page = await context.newPage();
  const errors = [];
  context.on('page', (opened) =>
    opened.on('pageerror', (error) => errors.push(String(error.message ?? error).slice(0, 200))),
  );
  page.on('pageerror', (error) => errors.push(String(error.message ?? error).slice(0, 200)));
  const problems = [];
  try {
    await scenario.run(tools(context, page, server));
  } catch (error) {
    problems.push(error instanceof Failed ? error.message : `could not run: ${String(error.message).split('\n')[0]}`);
  } finally {
    await context.close();
  }
  for (const error of errors) problems.push(`page error: ${error}`);
  console.log(
    `${problems.length ? 'FAIL' : 'pass'} ${scenario.name}${problems.length ? `: ${problems.join('; ')}` : ''}`,
  );
  return { name: scenario.name, only: scenario.name, note: scenario.note, problems };
}

try {
  const results = [];
  for (const scenario of scenarios) results.push(await check(scenario));
  fs.writeFileSync(path.join(outDir, 'results.json'), JSON.stringify({ checks: results }, null, 1));
  const failed = results.filter((r) => r.problems.length).length;
  console.log(`\n${results.length} scenarios, ${failed} failed. Results: ${path.join(outDir, 'results.json')}`);
  process.exitCode = failed ? 1 : 0;
} finally {
  await browser.close();
}
