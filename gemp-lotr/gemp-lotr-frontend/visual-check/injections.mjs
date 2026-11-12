// Deliberate bugs put into the Angular page (`--inject <name>`), to prove the check would catch that kind of bug. Run by
// `npm run check:selftest`: each must be reported, in the way `expect` says, on a scenario that is otherwise clean.
// `phone`: a scenario of the phone check (run.mjs --phone, phone.mjs), on a 390x844 phone.

const css = (rules) => (page) =>
  page.addInitScript((text) => {
    addEventListener('DOMContentLoaded', () => {
      const style = document.createElement('style');
      style.textContent = text;
      document.head.appendChild(style);
    });
  }, rules);

const script = (code) => (page) => page.addInitScript(code);

export const INJECTIONS = {
  'visible-change': {
    what: 'A change on the screen at the end of a scenario',
    scenario: 'start',
    expect: /end: \d+ pixels differ/,
    apply: css('.gd-stats { margin-top: 6px; }'),
  },
  'hidden-behind-accepted': {
    what: 'A change in a scenario that has an accepted difference, away from that difference',
    scenario: 'card-info',
    expect: /end: \d+ pixels differ/,
    apply: css('.gd-card-info__wiki { margin-left: 8px; }'),
  },
  'beside-excluded-area': {
    what: 'A change just outside an area left out of the comparison (the theme picker and its 2px margin)',
    scenario: 'start',
    expect: /end: \d+ pixels differ/,
    apply: css('.gd-toolbar__theme { box-shadow: 0 0 0 5px #f00; }'),
  },
  'accepted-area-over-limit': {
    what: 'A bigger change inside an accepted area',
    scenario: 'state-dialog-close-hover',
    expect: /in the accepted area/,
    apply: css('dialog[open] .gd-dialog__close { background: #f00 !important; }'),
  },
  'only-in-a-middle-step': {
    what: 'A change that has gone by the end of the scenario',
    scenario: 'notes-escape',
    expect: /step \d+: \d+ pixels differ/,
    apply: css('dialog[data-dialog="notes"] .gd-dialog__title { margin-left: 12px; }'),
  },
  'extra-request': {
    what: 'A server request the legacy page does not send',
    scenario: 'start',
    expect: /request, Angular only: GET \/deck\/list/,
    apply: script(() => addEventListener('load', () => void fetch('/gemp-lotr-server/deck/list'))),
  },
  'dead-button': {
    what: 'A button that does nothing',
    scenario: 'reset-all',
    expect: /end: (request, legacy only|\d+ pixels differ)/,
    apply: script(() =>
      addEventListener(
        'click',
        (e) => {
          if (e.target.closest?.('[data-action="reset-all"]')) e.stopImmediatePropagation();
        },
        true,
      ),
    ),
  },
  'wrong-question': {
    what: 'A different question in a browser dialog',
    scenario: 'save-rename-copy',
    expect: /browser dialogs:/,
    apply: script(() => {
      const original = window.prompt;
      window.prompt = (message, value) => original.call(window, `${message}!`, value);
    }),
  },
  'saved-differently': {
    what: 'A deck that reaches the server changed',
    scenario: 'save-rename-copy',
    expect: /what the server holds differs.*Changed Visual/,
    apply: (page) =>
      page.route('**/gemp-lotr-server/deck', (route) => {
        const request = route.request();
        if (request.method() !== 'POST') return route.continue();
        return route.continue({ postData: request.postData().replace('deckName=Visual', 'deckName=Changed%20Visual') });
      }),
  },
  'focus-lost': {
    what: 'A control that loses the keyboard focus',
    scenario: 'state-button-focus',
    expect: /focus: legacy "notes", Angular null/,
    apply: script(() => addEventListener('focusin', (e) => e.target.blur(), true)),
  },
  'wrong-cursor': {
    what: 'A different mouse cursor',
    scenario: 'state-button-hover',
    expect: /cursor \(over .*\): legacy/,
    apply: css('.gd-toolbar .gd-button { cursor: progress !important; }'),
  },
  'wrong-tooltip': {
    what: 'A different tooltip',
    scenario: 'state-button-hover',
    expect: /tooltip: legacy/,
    apply: script(() =>
      addEventListener('load', () =>
        setInterval(() => document.querySelectorAll('.gd-toolbar .gd-button').forEach((b) => (b.title = 'Wrong')), 50),
      ),
    ),
  },
  'enabled-when-disabled': {
    what: 'A button enabled when it should be disabled',
    scenario: 'start',
    expect: /previousPage: legacy \{"disabled":true\}, Angular \{"disabled":false\}/,
    apply: script(() =>
      addEventListener('load', () =>
        setInterval(() => {
          const button = document.querySelector('.gd-pager__previous');
          if (button) button.disabled = false;
        }, 50),
      ),
    ),
  },
  'phone-menu-stays-open': {
    what: 'A compact layout menu that never closes',
    phone: true,
    scenario: 'compact-deck-menu',
    expect: /Scenario failed: a tap beside the menu did not close it/,
    apply: script(() => {
      const close = HTMLDialogElement.prototype.close;
      HTMLDialogElement.prototype.close = function (...args) {
        if (!this.classList.contains('gd-drawer')) close.apply(this, args);
      };
    }),
  },
  'phone-switch-viewport-removed': {
    what: 'Switching to classic on a phone takes the viewport away (the phone keeps its width: the collection runs off)',
    phone: true,
    scenario: 'theme-switch--phoneSideways--from-modern',
    expect: /width is 844, a freshly loaded page has 980/,
    apply: script(() => {
      const content = Object.getOwnPropertyDescriptor(HTMLMetaElement.prototype, 'content');
      Object.defineProperty(HTMLMetaElement.prototype, 'content', {
        ...content,
        set(value) {
          if (this.name === 'viewport' && !value.includes('device-width')) this.remove();
          else content.set.call(this, value);
        },
      });
    }),
  },
  'phone-menu-list-ignored': {
    what: "A list in a compact layout menu whose choice isn't used",
    phone: true,
    scenario: 'empty--collection--eachOption',
    expect: /request, legacy only/,
    apply: script(() =>
      addEventListener(
        'change',
        (event) => {
          if (event.target.closest?.('dialog.gd-drawer')) event.stopImmediatePropagation();
        },
        true,
      ),
    ),
  },
  'phone-card-size-ignored': {
    what: "A card size slider whose moves don't reach the cards",
    phone: true,
    scenario: 'compact-card-size',
    expect: /cards [\d.]+px high became/,
    apply: script(() =>
      addEventListener(
        'input',
        (event) => {
          if (event.target.matches?.('[data-sets="card-size"]')) event.stopImmediatePropagation();
        },
        true,
      ),
    ),
  },
};
