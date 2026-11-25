// Differences from the legacy page that the owner has accepted. Only the owner adds to
// this list: anything not in it fails the check. An entry allows at most `maxPixels` differing pixels inside the area of
// `selector` (the Angular page's, grown by `margin` px); every other pixel still counts towards the normal tolerance.
//
//   scenarios  which scenarios it applies to: names, or patterns for the generated ones of matrix.mjs
//   sizes      which window sizes (default: all)
//   browsers   which browsers (CHECK_BROWSER; default: all)
//   themes     which themes of the Angular page (run.mjs --theme; default: all; behaviours only)
//   corner     only the bottom right corner of the area, this many px square (before `margin`)
//   reason     what differs and why it is accepted

import { BROWSER_NAME } from './browser.mjs';

const SLIDER_RING = 'Slider handle focus ring: Chrome drew the legacy ring itself, CSS can only approximate its shape';

/**
 * Areas of the Angular page left out of every comparison: what the legacy page had nothing like (by the selector, grown
 * by `margin` px). Only the owner adds to it. A pixel there never counts, in any scenario or window size.
 */
export const EXCLUDED_AREAS = [
  {
    selector: '.gd-toolbar__theme',
    margin: 2,
    reason:
      'The theme picker (a toolbar button): not on the legacy page; placed after the deck name, so nothing else moves',
  },
];

// Where two entries cover the same pixel of a scenario, the first one counts it: the narrower entries come first.
export const ACCEPTED = [
  {
    scenarios: [/./],
    browsers: ['firefox'],
    selector: 'dialog[open]',
    corner: 16,
    margin: 2,
    maxPixels: 150,
    reason: 'Firefox draws its own resize grip in the corner of a dialog, which CSS cannot style (accepted 2026-11-11)',
  },
  {
    scenarios: ['open-library-deck', 'save-rename-copy', 'import-decklist'],
    sizes: ['1280x800'],
    selector: '.gd-toolbar',
    margin: 2,
    maxPixels: 3500,
    reason:
      'The theme toggle takes 30px of a toolbar a long deck name already fills: the format list is squeezed that much more',
  },
  {
    scenarios: ['deck-add-remove', 'deck-ring-bearer-ring', 'deck-sites', 'deck-full', 'format-movie', 'notes-escape'],
    sizes: ['1280x800'],
    browsers: ['firefox'],
    selector: '.gd-toolbar',
    margin: 2,
    maxPixels: 3500,
    reason:
      'The same squeeze in Firefox, whose serif is wider: a shorter deck name already fills the toolbar (accepted 2026-11-11)',
  },
  {
    scenarios: ['pane-resized'],
    sizes: ['1280x800', '1366x768'],
    browsers: ['firefox'],
    selector: '.gd-toolbar',
    margin: 20,
    maxPixels: 3500,
    reason:
      'The same squeeze in a narrowed deck pane in Firefox, also at 1366x768; its wider deck name reaches about 20px past the toolbar (accepted 2026-11-11)',
  },
  {
    scenarios: ['selection-pack'],
    browsers: ['firefox'],
    selector: '[data-dialog="selection"]',
    margin: 2,
    maxPixels: 120000,
    reason:
      "The legacy page's choices are 1px smaller in Firefox (210x294 against 211x295): its jQuery 1.6 sizing starts from whole-pixel heights, which Firefox rounds just under (accepted 2026-11-11)",
  },
  {
    scenarios: ['pane-resized'],
    sizes: ['1280x800'],
    selector: '.gd-toolbar',
    margin: 10,
    maxPixels: 3500,
    reason: 'The same squeeze in a narrowed deck pane, where the squeezed lists reach about 10px past the toolbar',
  },
  {
    scenarios: [/^(myCards--item-\d+|selection--selectionChoice)--(rightClick|shiftClick|dragUp)$/],
    selector: '.gd-card-info__zoom',
    margin: 8,
    maxPixels: 1250,
    reason: 'Card Information of a pack: the slider, which the legacy page drew in full only there',
  },
  {
    scenarios: [/--pageSize--arrows$/],
    selector: '.gd-pager__size',
    margin: 8,
    maxPixels: 600,
    reason: `${SLIDER_RING} (moved by the keyboard, the ring differs more)`,
  },
  {
    scenarios: ['zoom', 'zoom-next-card-shift', /--ringBearerCard--(hover|leave|hoverThenShift)$/],
    selector: '.gd-zoom',
    margin: 4,
    maxPixels: 150,
    reason: 'The hover zoom shows above dialogs on the Angular page, which leaves a faint 1px line in its picture',
  },
  {
    scenarios: [/^cardInfo--cardInfoWiki--focus$/],
    selector: '.gd-card-info__wiki',
    margin: 8,
    maxPixels: 2000,
    reason: "The wiki link's keyboard focus ring is as wide as the dialog; the legacy one was as wide as the text",
  },
  {
    scenarios: [
      'card-info',
      'card-info-zoom-flip',
      'card-info-close-drag',
      /^cardInfo--|--(rightClick|shiftClick|dragUp)$/,
    ],
    browsers: ['firefox'],
    selector: '.gd-card-info__zoom',
    margin: 8,
    maxPixels: 350,
    reason: `${SLIDER_RING}; Firefox draws its own ring less like it (accepted 2026-11-11)`,
  },
  {
    scenarios: ['state-slider-focus', /--pageSize--/],
    browsers: ['firefox'],
    selector: '.gd-pager__size',
    margin: 8,
    maxPixels: 350,
    reason: `${SLIDER_RING}; Firefox draws its own ring less like it (accepted 2026-11-11)`,
  },
  {
    scenarios: [
      'card-info',
      'card-info-zoom-flip',
      'card-info-close-drag',
      /^cardInfo--|--(rightClick|shiftClick|dragUp)$/,
    ],
    selector: '.gd-card-info__zoom',
    margin: 8,
    maxPixels: 180,
    reason: SLIDER_RING,
  },
  {
    scenarios: ['state-slider-focus', /--pageSize--/],
    selector: '.gd-pager__size',
    margin: 8,
    maxPixels: 180,
    reason: SLIDER_RING,
  },
  {
    scenarios: ['page-size-slider', /--pageSize--/],
    selector: '.gd-pager',
    margin: 8,
    maxPixels: 480,
    reason: 'Requested by the owner: the page size handle stays inside its bar and never covers the arrow buttons',
  },
  {
    scenarios: ['state-button-focus', /--(focus|enter|space)$/],
    selector: '.gd-toolbar .gd-button:focus',
    margin: 4,
    maxPixels: 250,
    reason: 'Toolbar button keyboard focus ring, 1px shorter',
  },
  {
    scenarios: ['state-dialog-close-hover', 'state-dialog-close-focus', /--(hover|focus|enter|space|press)$/],
    selector: 'dialog[open] .gd-dialog__close:is(:hover, :focus)',
    margin: 4,
    maxPixels: 80,
    reason: 'Corners and icon of the dialog close button',
  },
  {
    scenarios: ['zoom-deck-site'],
    sizes: ['1920x1080'],
    selector: '.gd-zoom',
    margin: 4,
    maxPixels: 100,
    reason: 'Small remainder of the hover zoom',
  },
  {
    scenarios: ['deck-sites'],
    selector: '.gd-slot[data-slot="sites"] .gd-slot__cards',
    margin: 3,
    maxPixels: 60000,
    reason: 'A site 1px off at some window sizes and steps (legacy floating-point rounding)',
  },
  {
    scenarios: [
      /--(newDeck|saveDeck|renameDeck|copyDeck|importDeck|library|deckList|notes|dialogClose)--(enter|space|tap)$/,
    ],
    selector: '.gd-toolbar',
    margin: 2,
    maxPixels: 1500,
    reason: 'After the keyboard or a tap a legacy toolbar button stayed highlighted; the Angular one does not',
  },
  {
    scenarios: [/--(nextPage|previousPage)--tap$/],
    selector: '.gd-pager',
    margin: 2,
    maxPixels: 1000,
    reason: 'After a tap a legacy pager button stayed highlighted; the Angular one does not',
  },
  {
    scenarios: [/--library--/],
    selector: 'dialog[open] h2',
    margin: 3,
    maxPixels: 1400,
    reason: 'Opened after the deck list, the legacy library dialog kept the title "Your Saved Decks"',
  },
];

/**
 * Behaviour the owner has accepted being different, which a limited area can't describe. A scenario listed without
 * `problem` isn't compared at all, so each of those is exactly the gesture that was decided on. `themes`: only in those
 * themes; `devices`: only in those checks ('computer', or 'phone': run.mjs --phone, phone.mjs).
 */
const ACCEPTED_BEHAVIOUR = [
  {
    reason:
      "Phone: the second tap of a double tap is dropped when it lands on another control than the first (SecondTapGuard): a menu item's menu closed (Rename, Save: asked once), or the card tapped left the deck (the slot or the next site under the finger isn't pressed). The legacy page's second click pressed what was there (owner: fix it, 2026-11-12)",
    devices: ['phone'],
    scenarios: [
      /^(empty|deck)--(renameDeck|saveDeck)--doubleTap$/,
      /^deck--(ringBearerCard|ringCard|siteCards)--doubleTap$/,
    ],
  },
  {
    reason:
      'A double tap (or double click) on Next page moves one page: the Angular page disables Next while the page loads; the legacy one only drew it disabled, so its second click moved a second page (owner: keep, 2026-11-12)',
    scenarios: [/^empty--nextPage--double(Tap|Click)$/],
    problem:
      /^step 1: request, (legacy only: GET \/collection\/\w+ \["[^"]*start=24"\]|Angular only: GET \/collection\/\w+ \["[^"]*start=12"\])$/,
  },
  {
    reason:
      'Phone: a double tap on a pack opens one (data-tap-once, SecondTapGuard); the legacy page opened one, two or three depending on timing (owner: one pack, 2026-11-12). The Angular page must ask once',
    devices: ['phone'],
    scenarios: ['myCards--item-1--doubleTap'],
    problem:
      /^step 1: request, (legacy only: (GET \/delivery\/packContents|POST \/collection\/permanent) |Angular only: (GET \/delivery\/packContents \[""\]|POST \/collection\/permanent \["pack=[^"]*"\])$)/,
  },
  {
    reason:
      'Phone: a long press in Card Information (on the card or its close button) leaves it open; a right click there closed the legacy one (accepted 2026-11-12)',
    devices: ['phone'],
    scenarios: [/^cardInfo--(cardInfoCard|cardInfoClose)--longPress$/],
    problem: /(cardInfo\w*|dialogTitles|dialogClose): legacy null, Angular \{/,
  },
  {
    reason:
      "Phone: a long press on a selection pack's choice shows that card's Card Information, to look before choosing; a right click there did nothing on the legacy page (accepted 2026-11-12)",
    devices: ['phone'],
    scenarios: ['selection--selectionChoice--longPress'],
    problem:
      /dialogTitles: legacy \{"disabled":false,"text":"Choose one"\}, Angular \{"disabled":false,"text":"Card Information"\}/,
  },
  {
    reason:
      'Modern: closing a dialog gives the focus back to what opened it (THEME_BEHAVIOUR.dialogsReturnFocus); the legacy page left it nowhere (accepted 2026-11-11)',
    themes: ['modern'],
    scenarios: [/./],
    problem: /focus: legacy null, Angular "/,
  },
  {
    reason:
      "Modern on a computer: in a toolbar short of room (the deck pane narrowed, or a long deck name, at 1280x800) the theme button's 30px, which the legacy page didn't have, can't shrink, so the auto-zoom button and the collection list sit further right and the list is squeezed differently (accepted 2026-11-12)",
    themes: ['modern'],
    scenarios: ['pane-resized', 'open-library-deck'],
    problem:
      /^(step \d+|step \d+, end|end): geometry (autoZoom|collection): legacy [\d.]+,6 [\d.]+x23, Angular [\d.]+,6 [\d.]+x23$/,
  },
  {
    reason:
      'Modern: Card Information is modal (THEME_BEHAVIOUR.cardInfoModal), so a press on the page behind it closes it; the legacy one stayed open (accepted 2026-11-11)',
    themes: ['modern'],
    scenarios: ['card-info-close-drag'],
    problem:
      /(cardInfo\w*|dialogTitles|dialogClose): (1 shown on the legacy page, 0 on the Angular page|legacy \{.*\}, Angular null)|focus: legacy "cardInfo\w*", Angular null/,
  },
  {
    reason: 'The Angular page can reach the slots and the toggle by keyboard, so its Tab order has more stops',
    scenarios: [
      /--keyboard--tabOrder$/,
      /--(ringBearerSlot|ringSlot|sitesSlot|toggleCollection)--(focus|enter|space)$/,
    ],
  },
  {
    reason: "Space on a dialog's close button closes it on the Angular page; on the legacy page it did nothing",
    scenarios: [/--(dialogClose|cardInfoClose)--space$/],
  },
  {
    reason: 'Opened after the deck list, the legacy library dialog kept the title "Your Saved Decks"',
    scenarios: [/--library--/],
    problem: /dialogTitles: legacy .*Your Saved Decks.*Angular .*Library Decks/,
  },
  {
    reason: 'Turning the hover zoom off by keyboard closes an open zoom on the Angular page; the legacy one stayed',
    scenarios: [/--autoZoom--(enter|space)$/],
  },
  {
    reason: 'Dragging the Card Information slider follows the mouse; the legacy one jumped to the smallest size',
    scenarios: [/--cardInfoZoom--dragSideways$/],
  },
  {
    reason: 'Home on the Card Information slider leaves the dialog where it is; the legacy one moved it off centre',
    scenarios: [/^cardInfo--cardInfoZoom--arrows$/],
    problem: /^step 4: \d+ pixels differ/,
  },
  {
    reason: 'Dragging up from a site in the deck opens Card Information without a hover zoom left behind it',
    scenarios: [/--siteCards--dragUp$/],
  },
  {
    reason:
      'Card Information opened by touch: focus is on its close button; on the legacy page it was on the wiki link',
    scenarios: [/^cardInfo--(cardInfoCard|cardInfoClose)--tap$/],
  },
  {
    reason: "A double click on a dialog's close button leaves focus on Game Text; the legacy page left it nowhere",
    scenarios: [/--dialogClose--doubleClick$/],
  },
  {
    reason: 'After renaming a deck in the deck list focus is on nothing; the legacy page kept it on the rename button',
    scenarios: [/^deckList--deckRename--yes$/],
    problem: /focus: legacy "deckRename", Angular null/,
  },
  {
    reason:
      'Card Information opened from a selection pack takes the focus; on the legacy page it stayed on the "Choose one" dialog behind it',
    scenarios: [/^selection--selectionChoice--(rightClick|shiftClick|dragUp)$/],
    problem:
      /^(step 1|end): (focus: legacy "a dialog", Angular "cardInfoZoom"|dialogTitles: legacy .*"Choose one".*Angular .*"Card Information".*)$/,
  },
];

export function acceptedBehaviour(scenario, problem, theme = 'classic', device = 'computer') {
  const applies = (pattern) => (pattern instanceof RegExp ? pattern.test(scenario) : pattern === scenario);
  return ACCEPTED_BEHAVIOUR.some(
    (entry) =>
      (!entry.themes || entry.themes.includes(theme)) &&
      (!entry.devices || entry.devices.includes(device)) &&
      entry.scenarios.some(applies) &&
      (!entry.problem || entry.problem.test(problem)),
  );
}

/**
 * Controls only the Angular page has, which the owner has accepted (by their name in controls.mjs, ANGULAR_ONLY). The
 * coverage check lets them be on the Angular page alone; their area is left out of the comparison (EXCLUDED_AREAS).
 */
export const ACCEPTED_ANGULAR_ONLY = [
  {
    control: 'themePicker',
    reason:
      'Players switch between the classic and the modern theme (decided 2026-11-10); the legacy page had one look',
  },
];

/**
 * Bugs of the legacy page the owner has accepted not copying: what it sent or saved is rewritten to what it should have
 * been, before comparing. Each is limited to exactly that text.
 */
export const LEGACY_BUGS = [
  {
    reason: 'A new deck without notes: the legacy page sent and saved the word "null" as its notes',
    request: [/(^|&)notes=null(?=&|$)/g, '$1notes='],
    saved: [/<notes>null<\/notes>/g, '<notes/>'],
  },
];

/** Links the owner has accepted being different: the legacy address is rewritten before comparing. */
const ACCEPTED_LINKS = [
  {
    reason: 'The wiki links use https on the Angular page',
    from: /^http:\/\/wiki\.lotrtcgpc\.net\//,
    to: 'https://wiki.lotrtcgpc.net/',
  },
];

/** A legacy control's state, with an accepted change of its link applied. */
export function withAcceptedLink(control) {
  return control?.href
    ? { ...control, href: ACCEPTED_LINKS.reduce((href, link) => href.replace(link.from, link.to), control.href) }
    : control;
}

export function withoutLegacyBugs(text, kind) {
  return LEGACY_BUGS.reduce((result, bug) => result.replace(bug[kind][0], bug[kind][1]), text);
}

/** The areas with their limits for one scenario at one size: the excluded areas first (no limit), then ACCEPTED's. */
export function acceptedFor(scenario, sizeLabel) {
  const applies = (pattern) => (pattern instanceof RegExp ? pattern.test(scenario) : pattern === scenario);
  return [
    ...EXCLUDED_AREAS.map((area) => ({ ...area, maxPixels: Infinity })),
    ...ACCEPTED.filter(
      (entry) =>
        entry.scenarios.some(applies) &&
        (!entry.sizes || entry.sizes.includes(sizeLabel)) &&
        (!entry.browsers || entry.browsers.includes(BROWSER_NAME)),
    ),
  ];
}

/**
 * Differences of the game page (visual-check/game, `npm run check:game` and `check:game:matrix`) that the owner has
 * accepted. Only the owner adds to it (the decisions in trymarz/gemp-lotr-private#27, "Decisions to review"): anything
 * not in it fails the check. Two kinds of entry:
 *
 *   problem    a pattern for a problem's text (e.g. /^pile \w+ \d: enter: /): the problems it matches don't count
 *   selector   an area of the Angular page (its first visible match, grown by `margin` px; with `corner`, only the
 *              bottom right corner that many px square): up to `maxPixels` differing pixels in it don't count, in the
 *              screenshots of the steps `steps` matches (default: all). Read again at every screenshot, so it follows
 *              what moves (e.g. ':focus' is wherever the focus is)
 *
 *   scenarios  the scenarios (names, or patterns: the matrix's are matrix-<state>)
 *   sizes      which window sizes (default: all)
 *   browsers   which browsers (CHECK_BROWSER; default: all)
 *   maxPixels  for a screen difference: at most this many pixels
 *   decision   the number of the decision in #27
 *   reason     what differs and why it is accepted
 */
export const GAME_ACCEPTED = [
  {
    decision: 5,
    scenarios: [/^matrix-/],
    problem: /^(counters \+ \d|pile \w+ \d): enter: /,
    reason:
      "The keyboard reaches the counters' + and the pile counters (buttons on the Angular page, divs on the legacy " +
      'page): Enter opens the stats or the pile (accepted 2026-11-15, as for the deck builder)',
  },
  {
    decision: 5,
    scenarios: [/^matrix-/],
    problem: /^bid (up|down): (focus|enter|space|shiftClick|doubleClick|hover|leave): screen: /,
    maxPixels: 600,
    reason: "The bid's arrows are buttons: focused and pressed from the keyboard (accepted 2026-11-15)",
  },
  {
    decision: 6,
    scenarios: [/^matrix-/],
    problem: /^(pile|cardInfo) close: space: /,
    reason: "Space on a dialog's close button closes it (a button; the legacy page's was a link) (accepted 2026-11-15)",
  },
  {
    decision: 7,
    scenarios: [/^matrix-/],
    selector: ':focus',
    margin: 4,
    maxPixels: 700,
    reason:
      'Focus rings drawn differently: the size and animation sliders, the replay buttons, a tab or button focused ' +
      'after a shift click (accepted 2026-11-15)',
  },
  {
    decision: 19,
    scenarios: [/./],
    browsers: ['firefox'],
    selector: 'dialog[open]',
    corner: 16,
    margin: 2,
    maxPixels: 150,
    reason: "Firefox draws its own resize grip in a dialog's corner, which CSS cannot style (accepted 2026-11-15)",
  },
  {
    // Ketura's decision of 2026-11-19 (no number in trymarz/gemp-lotr-private#27 yet): the October game-screen
    // improvement wanted on the Angular page. game.html no longer sizes these windows to their cards (it was put back
    // as it was), so every step where one is open differs on purpose: the dialog of cards a decision shows
    // (ARBITRARY_CARDS, ACTION_CHOICE) and a revealed hand's (no scenario opens one). These are exactly the steps of
    // `check:game:table` (2026-11-19, 18 of 300 at 3 sizes) that differ because of it, and the one more step of
    // `check:game` that shows such a dialog (settings: a card chosen, then cleared; every scenario run on a real
    // server at 1366x768 with --pages legacy,angular). The whole screen may differ: the legacy dialog takes 80% of the
    // window, the Angular one is sized to its cards, so they differ wherever either one is. (Not after a reload, which compares the Angular page with
    // itself: there it must still be the same.)
    decision: null,
    scenarios: ['opening', 'settings', 'effects-skirmish'],
    problem:
      /^(bid|starting fellowship: Gimli|going first, from the list|starting fellowship: a card chosen, then cleared|starting fellowship: chosen again, then Done|two required responses at once|one of them chosen first): screen: \d+ px differ$/,
    reason: 'Ketura, 2026-11-19: card windows sized to their cards, in both themes',
  },
  {
    // Ketura's decision of 2026-11-19 (no number in trymarz/gemp-lotr-private#27 yet), in check:game: the Angular page
    // asks "At least one minion has not been assigned" (in its own dialog, run.mjs answerUnassignedQuestion) only while
    // more minions could have been assigned (the server's maxAssignable); game.html asks (the browser's confirm)
    // about any minion left unassigned. So a step where the legacy page asked and the Angular page had no reason to
    // differs in its dialogs, and only so: the answer sent is the same. Never the other way round. Not seen in the
    // runs of 2026-11-19 (every scenario at 1366x768, on a seeded server started by hand, not through Docker).
    decision: null,
    scenarios: [/./],
    problem: /: dialogs: A \["confirm: At least one minion has not been assigned, do you want to proceed\?"\], B \[\]$/,
    reason: 'Ketura, 2026-11-19: the unassigned minion question only when more minions could have been assigned',
  },
];

/** Whether the owner accepted this problem of a game scenario at this window size. */
export function acceptedGameProblem(scenario, sizeLabel, problem, entries = GAME_ACCEPTED) {
  const pixels = Number(/ screen: (\d+) px differ$/.exec(problem)?.[1] ?? NaN);
  return entries.some(
    (entry) =>
      entry.problem &&
      gameEntryApplies(entry, scenario, sizeLabel) &&
      entry.problem.test(problem) &&
      (entry.maxPixels === undefined || pixels <= entry.maxPixels),
  );
}

/** The area entries of GAME_ACCEPTED (to read on the Angular page at every screenshot, in this order). */
export const GAME_ACCEPTED_AREAS = GAME_ACCEPTED.filter((entry) => entry.selector);

/** Whether a GAME_ACCEPTED entry applies to this scenario, window size and browser (and step, if it names steps). */
export function gameEntryApplies(entry, scenario, sizeLabel, step = null) {
  const applies = (pattern) => (pattern instanceof RegExp ? pattern.test(scenario) : pattern === scenario);
  return (
    entry.scenarios.some(applies) &&
    (!entry.sizes || entry.sizes.includes(sizeLabel)) &&
    (!entry.browsers || entry.browsers.includes(BROWSER_NAME)) &&
    (!entry.steps || step === null || entry.steps.test(step))
  );
}

/**
 * Differences of the hall and the login page (visual-check/hall, `npm run check:hall`) that the owner has accepted.
 * Only the owner adds to it: anything not in it fails the check. An entry is an area of the Angular page (its first
 * visible match of `selector`, grown by `margin` px) where up to `maxPixels` differing pixels don't count (all of them
 * without a `maxPixels`), read again at every screenshot. A pixel belongs to the first entry whose area holds it.
 *
 *   scenarios  the scenarios (names, or patterns)
 *   steps      a pattern for the steps' labels it applies to (default: all; the page as loaded is "loaded")
 *   sizes      which window sizes (default: all)
 *   browsers   which browsers (CHECK_BROWSER; default: all)
 *   reason     what differs and why it is accepted
 *
 * What is here was accepted by Ketura on 2026-11-20, shown what the check still found after the pixel pass (the frame
 * identical, most screens within a few thousand of the screen's 1,049,088 pixels, and the larger ones named): "<1% is
 * more than accurate enough, I'm not concerned. Approved." Each screen is given what it measured then (the most of
 * several runs, and about a sixth more), so the check passes as the pages are and fails when one moves further from
 * its legacy page. results.json has every screenshot's count (`differing`), accepted or not.
 */

/** The whole hall: its frame is 8px inside the window. */
const HALL_SCREEN = { selector: 'gd-hall-shell', margin: 8, sizes: ['1366x768'] };
const exactly = (label) => new RegExp(`^${label.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`);
/** `scenarios` at `step` (a label; null: every step) may differ from the legacy page by `maxPixels`. */
const within = (scenarios, step, maxPixels, reason) => ({
  scenarios,
  ...(step === null ? {} : { steps: exactly(step) }),
  ...HALL_SCREEN,
  maxPixels,
  reason,
});

const SUB_PIXEL = 'Text a fraction of a pixel from the legacy page here and there (under 1% of the screen).';

export const HALL_ACCEPTED = [
  // ---- what the legacy hall has nothing like ----
  {
    scenarios: [/./],
    selector: '.gd-hall__logout',
    margin: 2,
    reason:
      'Log Out, right after the connection readout: the legacy hall has no log out. Asked for by Ketura on ' +
      '2026-11-23 ("in the hall somewhere we need a prominent log out button, probably next to the connection ' +
      'status"). Shown whenever an alias is played; nothing else in the bar moves for it.',
  },

  // ---- where the legacy page's own place depends on timing ----
  {
    scenarios: ['events-tournament-link', 'events-league-link'],
    selector: '.gd-hall__panel',
    sizes: ['1366x768'],
    reason:
      "Where the tab ends up scrolled. The legacy hall's smooth scroll to the row is clamped to the page's height at " +
      'the moment it starts, while the drawer is still loading, so where it stops depends on timing (62,000 to ' +
      '171,000 px between runs); the Angular hall always lands on the row.',
  },

  // ---- over 1% of the screen: named to Ketura with the rest ----
  within(['play-league'], 'league', 48000, 'Play > League: the "Join Leagues" list starts about 10 px higher.'),
  within(['game-hall-prizes'], 'Show Prizes', 34000, 'The prizes dialog sits 2 px from the legacy one.'),
  within(['account-overview'], 'Player picker', 30000, "The player picker's menu sits 2 px from the legacy one."),
  within(['play-bot'], 'bot', 19000, 'Play > Bot: lines a pixel from the legacy form (not measured further).'),
  within(['frame-visitor-hall'], 'loaded', 14400, 'The "Not logged in" dialog: its button bar.'),
  within(['game-hall-queues', 'game-hall-prizes'], 'loaded', 14200, "The queues' and tournaments' rows."),
  within(['events-leagues'], 'Open the first league', 12000, 'A league opened in Events: a pixel here and there.'),
  within(['events-leagues'], 'Your league matches', 12000, 'A league opened in Events: a pixel here and there.'),

  // ---- under 1% ----
  within(['account-history'], null, 9600, `My Game History's rows. ${SUB_PIXEL}`),
  within(['help-errata-filtered'], null, 8800, SUB_PIXEL),
  within(['help-format-link'], null, 8000, SUB_PIXEL),
  within(['help-formats'], null, 7400, SUB_PIXEL),
  within(['play-casual'], 'Create without a deck', 7200, SUB_PIXEL),
  within(['play-casual'], 'casual', 5500, SUB_PIXEL),
  within(['play-back'], 'Casual', 5500, SUB_PIXEL),
  within(['play-scenario'], 'scenario', 5200, SUB_PIXEL),
  within([/^play-/], 'Play', 4100, `The Play popup's choices. ${SUB_PIXEL}`),
  within(['play-tournament'], 'tournament', 4100, SUB_PIXEL),
  within(['play-back'], 'Back', 3900, SUB_PIXEL),
  within(['join-table'], 'Join Table', 3000, SUB_PIXEL),
  within(['account-scenarios'], 'Copy Public...', 5900, SUB_PIXEL),
  within(['account-scenarios'], 'Close', 4200, SUB_PIXEL),
  within(['account-scenarios'], 'loaded', 3400, SUB_PIXEL),
  within(['info-patch-notes'], 'Months', 5900, SUB_PIXEL),
  within(['info-patch-notes'], 'loaded', 2700, SUB_PIXEL),
  within(['help-conduct'], null, 3500, SUB_PIXEL),
  within(['info-about'], null, 3300, SUB_PIXEL),
  within(['info-stats', 'help-errata'], null, 2800, SUB_PIXEL),
  within(['info-contribute', 'help-errata-card'], null, 2700, SUB_PIXEL),
  within(['account-stats', 'events-leagues'], null, 1200, SUB_PIXEL),
  within(['events-calendar'], null, 1100, SUB_PIXEL),
  within(['help-how-to-play', 'frame-visitor-help'], null, 1000, SUB_PIXEL),
  within(['help-leagues', 'account-overview'], null, 800, SUB_PIXEL),

  // ---- every screen ----
  within(
    [/./],
    null,
    200,
    "A table's waiting time or the clock caught a second apart on the two pages (59 px when it happens).",
  ),
];
