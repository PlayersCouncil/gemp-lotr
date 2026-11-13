// Deliberate bugs for `npm run check:game:selftest` (local.mjs --selftest): each is put into page B of one scenario,
// and the check must report it the way `expect` says, and must not report that without it. That proves the game
// check catches each kind of difference: the screen, the answers sent, the state shown, browser dialogs, page errors.
// `apply` runs before the page loads, `afterSettle` after each step has settled (before its snapshot).

/** A selector for the same thing on either page: the legacy markup or the Angular page's contract (controls.mjs). */
const pick = (kind, legacy, angular) => (kind === 'legacy' ? legacy : angular);

export const GAME_INJECTIONS = {
  'twilight-moved': {
    what: 'the twilight pool drawn 4px to the right',
    scenario: 'opening',
    expect: /screen: \d+ px differ/,
    apply: async (page, kind) => {
      await page.addInitScript(
        (selector) => {
          document.addEventListener('DOMContentLoaded', () => {
            const style = document.createElement('style');
            style.textContent = `${selector} { position: relative; left: 4px; }`;
            document.head.append(style);
          });
        },
        pick(kind, '.twilightPool', '[data-game="twilight"]'),
      );
    },
  },
  'wrong-bid': {
    what: 'the bid sent as one more than chosen',
    scenario: 'opening',
    expect: /requests: A \["decision 1 = 0"\], B \["decision 1 = 1"\]/,
    // Changed in the page, so the request the check reads is the one the page sent
    apply: async (page) => {
      await page.addInitScript(() => {
        const send = XMLHttpRequest.prototype.send;
        let changed = false;
        XMLHttpRequest.prototype.send = function (body) {
          if (!changed && typeof body === 'string' && /(^|&)decisionValue=0(&|$)/.test(body)) {
            changed = true;
            body = body.replace(/decisionValue=0(?=&|$)/, 'decisionValue=1');
          }
          return send.call(this, body);
        };
      });
    },
  },
  'phase-misnamed': {
    what: 'the phase shown under another name',
    scenario: 'opening',
    expect: /phase: A "[^"]*", B "Wrong phase"/,
    afterSettle: async (page, kind) => {
      await page.evaluate(
        (selector) => {
          const phase = document.querySelector(selector);
          if (phase && phase.textContent.trim()) phase.textContent = 'Wrong phase';
        },
        pick(kind, '.phase', '[data-game="phase"]'),
      );
    },
  },
  'card-hidden': {
    what: 'a card of the hand not shown',
    scenario: 'opening',
    expect: /cards: only A \["[^"]+:HAND"\], only B \[\]/,
    afterSettle: async (page, kind) => {
      await page.evaluate((kind) => {
        const hand =
          kind === 'legacy'
            ? window
                .$('.card')
                .filter((_, el) => window.$(el).data('card')?.zone === 'HAND')
                .get(0)
            : document.querySelector('[data-zone="HAND"]');
        if (hand) hand.style.visibility = 'hidden';
      }, kind);
    },
  },
  'extra-alert': {
    what: 'a browser alert when the page opens',
    scenario: 'start',
    expect: /dialogs: A \[\], B \["alert: /,
    apply: async (page) => {
      await page.addInitScript(() => window.addEventListener('load', () => setTimeout(() => alert('Injected'), 0)));
    },
  },
  'page-error': {
    what: 'an error thrown by the page',
    scenario: 'start',
    expect: /page errors: /,
    apply: async (page) => {
      await page.addInitScript(() =>
        window.addEventListener('load', () =>
          setTimeout(() => {
            throw new Error('Injected error');
          }, 0),
        ),
      );
    },
  },
};
