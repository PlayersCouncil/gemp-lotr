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
  // The number's spinner: the value shown and the arrows around it
  integerValue: '[data-decision="integer"]',
  integerDown: '[data-decision="integer-down"]',
  integerUp: '[data-decision="integer-up"]',
  actionMenuItem: (actionId) => `[data-action-id="${actionId}"]`,
  tab: (name) => `[data-tab="${name}"]`,
  pile: (name, playerIndex) => `[data-pile="${name}"][data-player-index="${playerIndex}"]`,
  statsToggle: (playerIndex) => `[data-stats-toggle="${playerIndex}"]`,
  setting: (name) => `[data-setting="${name}"]`,
  sliderHandle: (name) => `[data-setting="${name}"]`,
  timerAlertField: (field) => `[data-timer-alert="${field}"]`,
  toggle: (name) => `[data-toggle="${name}"]`,
  chatInput: '[data-chat-input]',
  replay: (name) => `[data-replay="${name}"]`,
  button: (name) => `[data-button="${name}"]`,
  // The page's own question before an assignment is sent with a minion left unassigned (only while more minions could
  // have been assigned: maxAssignable), where the legacy page asks with the browser's confirm: the dialog (titled
  // unassignedTitle), its message, and its buttons by `data-action` (ok "Proceed", cancel "Go back")
  unassignedQuestion: 'dialog.gd-dialog[open][data-dialog="confirm"]',
  unassignedTitle: 'Unassigned minions',
  questionMessage: '.gd-notifier__message',
  questionAnswer: (action) => `button[data-action="${action}"]`,
  // Changes over time on its own: left out of the screenshots
  masks: ['[data-clock]', '[data-chat-time]'],
  // What the legacy page had nothing like, hidden in the screenshots (the owner put it there, 2026-11-15): the theme
  // button in the table's top right corner; what lies under it is still compared
  screenshotStyle: '.gd-game__theme { visibility: hidden !important; }',
};

/** On the legacy page: the same things through its own markup. */
export const LEGACY = {
  decisionButton: (label) =>
    `.ui-dialog:visible .ui-dialog-buttonpane button:has-text("${label}"), .alertButtons button:has-text("${label}")`,
  integerValue: '.SpinnerControl .ValueDisplay',
  integerDown: '.SpinnerControl .LeftButton',
  integerUp: '.SpinnerControl .RightButton',
  choiceSelect: '#multipleChoiceDecision',
  tab: (name) =>
    ({ chat: '#chatTab a', settings: '#settingsTab a', options: '#gameOptionsTab a', players: '#playersInRoomTab a' })[
      name
    ],
  pile: (name, playerIndex) => `#${name}${playerIndex}`,
  statsToggle: (playerIndex) => `#showStats${playerIndex} .slimButton`,
  setting: (name) => `#${name}`,
  sliderHandle: (name) => `#${name} .ui-slider-handle`,
  timerAlertField: (field) => `#timerAlertSettings .${field}`,
  toggle: (name) => ({ 'auto-zoom': '#auto-zoom-toggle', 'timer-mute': '#timer-mute-toggle' })[name],
  chatInput: '#chatBox .chatTalk',
  replay: (name) => ({ play: '#replayButton', slower: '#slowerButton', faster: '#fasterButton' })[name],
  button: (name) => `#${name}`,
  masks: ['.gameClock', '.decisionClock', '#chatBox .timestamp'],
  screenshotStyle: '',
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
    alertButtons: Array.from(document.querySelectorAll('[data-game="alert-buttons"] [data-decision-button]'), text),
    // The modern theme's game's end (the owner, 2026-11-20) holds the server's feedback form, as game.html's dialog did
    dialogs: Array.from(document.querySelectorAll('[data-dialog-title]'))
      .filter(shown)
      .map((el) =>
        el.closest('[data-dialog="game-ended"]')?.querySelector('[data-ended="feedback"]')
          ? `${text(el)} (the game's end, with the feedback)`
          : text(el),
      ),
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
  // An event holding the page (its animation) is still moving, also between the moves
  return (
    document.querySelector('[data-animating]') !== null ||
    document.getAnimations().some((animation) => animation.playState === 'running')
  );
}

/**
 * The game the legacy page holds, read without layout so that a browser and jsdom read it the same: the texts of the
 * game state panel and the decision, and every card element with its data. For the recordings (run.mjs --record) and
 * the jsdom tests that replay them (src/parity/game).
 */
export function readLegacyModel() {
  const $ = window.$;
  const text = (el) => (el ? el.textContent.replace(/\s+/g, ' ').trim() : null);
  const cards = [];
  $('.card').each(function () {
    const card = $(this).data('card');
    if (card) {
      const parts = [card.cardId, card.blueprintId, card.zone, card.owner ?? ''];
      if (card.assign) parts.push(`assign ${card.assign}`);
      if (card.skirmish) parts.push('skirmish');
      cards.push(parts.join(':'));
    }
  });
  const stats = {};
  document.querySelectorAll('.playerStats > div[id]').forEach((el) => (stats[el.id] = text(el)));
  const ui = window.ui;
  return {
    twilight: text(document.querySelector('.twilightPool')),
    phase: text(document.querySelector('.phase')),
    initiative: text(document.querySelector('#initiative')),
    ruleOf4: text(document.querySelector('#ruleof4')),
    alert: ui?.alertText ? text(ui.alertText.get(0)) : null,
    stats,
    cards: cards.sort(),
  };
}

/**
 * Where the game page's parts are (table.mjs --geometry): every card by its id, and the panels around the table, each
 * as [x, y, width, height]. Runs in the page; the same on both pages, so their results can be compared (compareBoxes).
 */
/**
 * Where each accepted area is on the Angular page (GAME_ACCEPTED_AREAS): the box of the first visible match of its
 * selector, grown by its margin (only its bottom right corner with `corner`); null where nothing matches.
 */
export function readAreaBoxes(entries) {
  return entries.map(({ selector, margin = 0, corner }) => {
    const el = Array.from(document.querySelectorAll(selector)).find((candidate) => {
      const box = candidate.getBoundingClientRect();
      return box.width > 0 && box.height > 0;
    });
    if (!el) return null;
    const box = el.getBoundingClientRect();
    const left = corner ? box.right - corner : box.left;
    const top = corner ? box.bottom - corner : box.top;
    return { left: left - margin, top: top - margin, right: box.right + margin, bottom: box.bottom + margin };
  });
}

export function readGameBoxes({ kind }) {
  const box = (el) => {
    if (!el) return null;
    const b = el.getBoundingClientRect();
    return b.width > 0 && b.height > 0 ? [b.x, b.y, b.width, b.height].map((n) => Math.round(n * 10) / 10) : null;
  };
  const boxes = {};
  const put = (name, el) => {
    const found = box(el);
    if (found) boxes[name] = [found];
  };
  if (kind === 'legacy') {
    const ui = window.ui;
    put('state', ui.gameStateElem?.[0]);
    put('alert', ui.alertBox?.[0]);
    put('stats', ui.statsDiv?.[0]);
    put('tabs', document.querySelector('#bottomLeftTabs'));
    for (const el of document.querySelectorAll('.card')) {
      const id = window.$(el).data('card')?.cardId;
      if (id !== undefined && el.closest('.ui-dialog') === null) put(`card ${id}`, el);
    }
  } else {
    put('state', document.querySelector('.gd-game__state'));
    put('alert', document.querySelector('.gd-game__alert'));
    put('stats', document.querySelector('.gd-game__stats'));
    put('tabs', document.querySelector('.gd-game__tabs'));
    for (const el of document.querySelectorAll('[data-card-id]')) {
      if (el.closest('dialog') === null) put(`card ${el.dataset.cardId}`, el);
    }
  }
  return boxes;
}
