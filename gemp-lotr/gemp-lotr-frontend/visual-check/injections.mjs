// Deliberate bugs put into the Angular page (`--inject <name>`), to prove the check would catch that kind of bug. Run by
// `npm run check:selftest`: each must be reported, in the way `expect` says, on a scenario that is otherwise clean.

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
};
