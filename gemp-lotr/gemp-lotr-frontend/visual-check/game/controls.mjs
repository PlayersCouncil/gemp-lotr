// How the check finds things on each game page. The legacy page is reached through its own objects (the global `ui`,
// GempLotrGameUI, and jQuery's card data); the Angular page through the DOM contract below, which the Angular game
// page must provide (G2 in trymarz/gemp-lotr-private#27). Every function here runs in the page.

/**
 * The Angular game page's DOM contract, as selectors. Cards carry `data-card-id` (the server's card id, or `temp<n>`
 * for the cards shown inside a decision's dialog) and `data-zone`; decision controls carry `data-decision-*`.
 */
export const ANGULAR_CONTRACT = {
  card: (id) => `[data-card-id="${id}"]`,
  decisionButton: (label) => `[data-decision-button="${label}"]`,
  choiceButton: (index) => `[data-decision-choice="${index}"]`,
  choiceSelect: 'select[data-decision="choice"]',
  integerInput: 'input[data-decision="integer"]',
  actionMenuItem: (actionId) => `[data-action-id="${actionId}"]`,
  tab: (name) => `[data-tab="${name}"]`,
  pile: (name, playerIndex) => `[data-pile="${name}"][data-player-index="${playerIndex}"]`,
  statsToggle: (playerIndex) => `[data-stats-toggle="${playerIndex}"]`,
  setting: (name) => `[data-setting="${name}"]`,
  // Changes over time on its own: left out of the screenshots
  masks: ['[data-clock]', '[data-chat-time]'],
};

/** On the legacy page: the same things through its own markup. */
export const LEGACY = {
  decisionButton: (label) =>
    `.ui-dialog:visible .ui-dialog-buttonpane button:has-text("${label}"), .alertButtons button:has-text("${label}")`,
  integerInput: '#integerDecision',
  choiceSelect: '#multipleChoiceDecision',
  tab: (name) =>
    ({ chat: '#chatTab a', settings: '#settingsTab a', options: '#gameOptionsTab a', players: '#playersInRoomTab a' })[
      name
    ],
  pile: (name, playerIndex) => `#${name}${playerIndex}`,
  statsToggle: (playerIndex) => `#showStats${playerIndex} .slimButton`,
  setting: (name) => `#${name}`,
  masks: ['.gameClock', '.decisionClock', '#chatBox .timestamp'],
};

/**
 * Where to click a card so the click lands on it and on nothing laid over it: `{x, y}` in the window; null if there is
 * no such card, `{blocked}` (what covers it) if there is no such place.
 */
export function cardPoint({ kind, id }) {
  let target;
  if (kind === 'legacy') {
    // jQuery's :cardId selector of gameUi.js; the frontmost visible one (cards in a closed dialog stay in the page)
    const cards = window.$(`.card:cardId(${id})`).filter(':visible');
    const card = cards.last();
    target = card.find('.actionArea').get(0) ?? card.get(0);
  } else {
    const all = Array.from(document.querySelectorAll(`[data-card-id="${CSS.escape(id)}"]`));
    target = all.filter((el) => el.getBoundingClientRect().width > 0).at(-1);
  }
  if (!target) {
    return null;
  }
  const box = target.getBoundingClientRect();
  // The middle first, then a grid: an attached card may lie over part of it
  const points = [[0.5, 0.5]];
  for (let fx = 0.15; fx < 1; fx += 0.175) {
    for (let fy = 0.1; fy < 1; fy += 0.2) {
      points.push([fx, fy]);
    }
  }
  for (const [fx, fy] of points) {
    const x = box.left + box.width * fx;
    const y = box.top + box.height * fy;
    const hit = document.elementFromPoint(x, y);
    if (hit && (hit === target || target.contains(hit))) {
      return { x, y };
    }
  }
  // For the error: what lies over the card's middle
  const over = document.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);
  return {
    blocked: over ? `${over.tagName.toLowerCase()}.${String(over.className).trim().replace(/\s+/g, '.')}` : 'nothing',
  };
}

/** Where the menu item of one action is, after a card with several actions was clicked; or null. */
export function actionMenuPoint({ kind, actionId }) {
  const item =
    kind === 'legacy'
      ? document.querySelector(`.contextMenu a[href="#${actionId}"]`)
      : document.querySelector(`[data-action-id="${actionId}"]`);
  if (!item) {
    return null;
  }
  const box = item.getBoundingClientRect();
  return { x: box.left + box.width / 2, y: box.top + box.height / 2 };
}

/**
 * What the page shows that a screenshot can't be compared on alone: the texts of the game state panel, the decision's
 * text, the open dialogs, and every card with its zone. The same shape on both pages, so it can be compared. Not the
 * page's title: the legacy page flashes it on a timer while a decision waits.
 */
export function readGameState({ kind }) {
  const text = (el) => (el ? el.textContent.replace(/\s+/g, ' ').trim() : null);
  const shown = (el) => {
    const box = el.getBoundingClientRect();
    return box.width > 0 && box.height > 0 && getComputedStyle(el).visibility !== 'hidden';
  };
  if (kind === 'legacy') {
    const ui = window.ui;
    const cards = [];
    window.$('.card').each(function () {
      const card = window.$(this).data('card');
      if (card && shown(this)) {
        cards.push(`${card.cardId}:${card.blueprintId}:${card.zone}${card.assign ? `>${card.assign}` : ''}`);
      }
    });
    const stats = {};
    document.querySelectorAll('.playerStats > div[id]').forEach((el) => {
      if (shown(el)) stats[el.id] = text(el);
    });
    return {
      twilight: text(document.querySelector('.twilightPool')),
      phase: text(document.querySelector('.phase')),
      initiative: text(document.querySelector('#initiative')),
      ruleOf4: text(document.querySelector('#ruleof4')),
      alert: ui?.alertText ? text(ui.alertText.get(0)) : null,
      alertButtons: ui?.alertButtons ? Array.from(ui.alertButtons.get(0)?.querySelectorAll('button') ?? [], text) : [],
      dialogs: Array.from(document.querySelectorAll('.ui-dialog'))
        .filter(shown)
        .map((el) => text(el.querySelector('.ui-dialog-title')) ?? ''),
      menu: Array.from(document.querySelectorAll('.contextMenu a'), text),
      stats,
      cards: cards.sort(),
    };
  }
  const read = (name) => text(document.querySelector(`[data-game="${name}"]`));
  const cards = Array.from(document.querySelectorAll('[data-card-id]'))
    .filter(shown)
    .map(
      (el) =>
        `${el.dataset.cardId}:${el.dataset.blueprintId}:${el.dataset.zone}${el.dataset.assign ? `>${el.dataset.assign}` : ''}`,
    );
  const stats = {};
  document.querySelectorAll('[data-stat]').forEach((el) => {
    if (shown(el)) stats[el.dataset.stat] = text(el);
  });
  return {
    twilight: read('twilight'),
    phase: read('phase'),
    initiative: read('initiative'),
    ruleOf4: read('ruleof4'),
    alert: read('alert'),
    alertButtons: Array.from(document.querySelectorAll('[data-decision-button]'), text),
    dialogs: Array.from(document.querySelectorAll('[data-dialog-title]')).filter(shown).map(text),
    menu: Array.from(document.querySelectorAll('[data-action-id]'), text),
    stats,
    cards: cards.sort(),
  };
}

/** True while the page still moves: a jQuery animation or a CSS one, on the legacy page also its animation queue. */
export function animating({ kind }) {
  if (kind === 'legacy') {
    const $ = window.$;
    return $(':animated').length > 0 || ($('#main').queue && $('#main').queue().length > 0);
  }
  return document.getAnimations().some((animation) => animation.playState === 'running');
}
