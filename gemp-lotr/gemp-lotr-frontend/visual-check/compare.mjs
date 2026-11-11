// What the check compares after each step of a scenario, on both pages: the screen, the requests, the browser's own
// dialogs and the state of the page that a screenshot doesn't show.
import { PNG } from 'pngjs';
import { withAcceptedLink } from './accepted.mjs';
import { CONTROLS, GEOMETRY } from './controls.mjs';

/** The controls with one selector each, for the page's state (the ones that take arguments are compared by scenarios). */
export function fixedControls(which) {
  return Object.fromEntries(
    Object.entries(CONTROLS)
      .filter(([, control]) => typeof control !== 'function')
      .map(([name, control]) => [name, control[which]]),
  );
}

/** Keeps where the mouse is, for reading what is under it. Runs in the page. */
export const MOUSE_TRACKER = () => {
  window.__mouse = null;
  // Pointer events: after a press whose default is prevented (dragging the pane bar) there are no mouse events
  window.addEventListener('pointermove', (e) => (window.__mouse = { x: e.clientX, y: e.clientY }), true);
};

/** The state of the page a screenshot doesn't show. Runs in the page; the same on both, so its results can be compared. */
export function readPageState(controls) {
  function visible(el) {
    const box = el.getBoundingClientRect();
    return box.width > 0 && box.height > 0 && getComputedStyle(el).visibility !== 'hidden';
  }
  /** The frontmost visible match: legacy dialogs stay in the page while closed. */
  const select = (selector) => {
    const matches = Array.from(document.querySelectorAll(selector.replace(/:visible/g, '')));
    return matches.filter(visible).at(-1) ?? null;
  };
  const text = (el) => (el ? el.textContent.replace(/\s+/g, ' ').trim() : null);

  const state = { controls: {}, focus: null, mouse: false, cursor: null, tooltip: null };
  for (const [name, selector] of Object.entries(controls)) {
    const el = select(selector);
    if (!el) {
      continue;
    }
    // The bar between the panes has no disabled state of its own on the Angular page: dragging it just does nothing
    const entry = {
      disabled:
        (name !== 'separator' && el.disabled === true) ||
        (name !== 'separator' &&
          (el.getAttribute('aria-disabled') === 'true' ||
            el.matches('.ui-state-disabled, .ui-button-disabled, .ui-slider-disabled'))),
    };
    if (el.matches('select, input[type=text], input[type=number], textarea')) {
      entry.value = el.value;
    }
    if (el.tagName === 'SELECT') {
      entry.options = Array.from(el.options, (option) => `${option.value}=${option.text.trim()}`).join('|');
    }
    // Only for real links: the legacy dialogs' close button is an <a href="#">
    if (el.tagName === 'A' && el.getAttribute('href') !== '#') {
      entry.href = el.href;
    }
    if (name === 'deckName' || name === 'stats' || name === 'dialogTitles') {
      entry.text = text(el);
    }
    state.controls[name] = entry;
  }

  // Controls that hold others: being inside one says nothing about which control has the focus
  const CONTAINERS = ['collectionPane', 'stats', 'deckName', 'dialogTitles'];
  const active = document.activeElement;
  if (active && active !== document.body) {
    const named = Object.entries(controls).filter(([name]) => !CONTAINERS.includes(name));
    const plain = (selector) => selector.replace(/:visible/g, '');
    state.focus =
      named.find(([, selector]) => active.matches(plain(selector)))?.[0] ??
      named.find(([, selector]) => active.closest(plain(selector)) !== null)?.[0] ??
      (active.closest('.gd-card, .card')
        ? 'a card'
        : active.matches('dialog, .ui-dialog')
          ? 'a dialog'
          : 'something else');
  }

  if (window.__mouse) {
    state.mouse = true;
    const under = document.elementFromPoint(window.__mouse.x, window.__mouse.y);
    if (under) {
      const cursor = getComputedStyle(under).cursor;
      state.cursor = cursor === 'auto' ? 'default' : cursor;
      state.under = `${under.tagName.toLowerCase()}.${under.className}`;
      state.tooltip = under.closest('[title]')?.getAttribute('title') || null;
    }
  }
  return state;
}

/** What differs between the two pages' state, one line per thing. */
export function compareStates(legacy, angular, last) {
  const differences = [];
  const note = (what, a, b) => {
    if (JSON.stringify(a ?? null) !== JSON.stringify(b ?? null)) {
      const show = (value) => JSON.stringify(value ?? null).slice(0, 260);
      differences.push(`${what}: legacy ${show(a)}, Angular ${show(b)}`);
    }
  };
  note('focus', legacy.focus, angular.focus);
  // The browser reports the mouse's first position at a moment of its own
  if (legacy.mouse && angular.mouse) {
    note(`cursor (over ${legacy.under} / ${angular.under})`, legacy.cursor, angular.cursor);
    note('tooltip', legacy.tooltip, angular.tooltip);
  }
  for (const name of new Set([...Object.keys(legacy.controls), ...Object.keys(angular.controls)])) {
    // The stats come from a request sent on a timer, so they can be a moment behind until the scenario is over
    if (name === 'stats' && !last) {
      continue;
    }
    note(name, withAcceptedLink(legacy.controls[name]), angular.controls[name]);
  }
  return differences;
}

/**
 * Reads whose newest answer is all that is shown: the legacy page asked once per change of a field, the Angular page
 * cancels or merges the ones a newer one replaces (only the newest answer counts, a bug fixed on purpose).
 */
const LATEST_ONLY = /^GET \/collection\//;
/** Sent on a timer after a change, so which step it falls in varies: compared over the whole scenario (compareStats). */
const STATS = 'POST /deck/stats';
/** The Angular page checks the login before opening (the owner's request, the legacy page has no such check). */
const LOGIN_CHECK = 'GET /player';

/** The requests of a step as one comparable list: in order within each address (separate addresses run in parallel). */
export function requestList(requests) {
  const live = requests.filter((request) => !request.aborted);
  const byRoute = new Map();
  for (const { method, route, data } of live) {
    const key = `${method} ${route}`;
    if (key === STATS || key === LOGIN_CHECK) continue;
    byRoute.set(key, LATEST_ONLY.test(key) ? [data] : [...(byRoute.get(key) ?? []), data]);
  }
  return [...byRoute.entries()].sort(([a], [b]) => a.localeCompare(b));
}

/** The deck stats asked for in a scenario: the same last one (the ones before depend on the timer they are sent on). */
export function compareStats(legacySteps, angularSteps) {
  const bodies = (steps) =>
    steps.flatMap((step) =>
      step.requests.filter((r) => !r.aborted && `${r.method} ${r.route}` === STATS).map((r) => r.data),
    );
  const legacy = bodies(legacySteps);
  const angular = bodies(angularSteps);
  const problems = [];
  if (legacy.at(-1) !== angular.at(-1)) {
    problems.push(`the last deck stats request differs: legacy ${legacy.at(-1)}, Angular ${angular.at(-1)}`);
  }
  return problems;
}

export function compareRequests(legacy, angular) {
  const a = requestList(legacy);
  const b = requestList(angular);
  if (JSON.stringify(a) === JSON.stringify(b)) {
    return [];
  }
  const only = (x, y) =>
    x.filter(([route, data]) => !y.some(([r, d]) => r === route && JSON.stringify(d) === JSON.stringify(data)));
  return [
    ...only(a, b).map(([route, data]) => `legacy only: ${route} ${JSON.stringify(data).slice(0, 220)}`),
    ...only(b, a).map(([route, data]) => `Angular only: ${route} ${JSON.stringify(data).slice(0, 220)}`),
  ];
}

function inside(x, y, area) {
  return x >= area.left && x < area.right && y >= area.top && y < area.bottom;
}

/**
 * Compares two screenshots. Pixels inside an accepted area count towards that area's limit, all others towards
 * `outside`. Returns a diff image only when asked, since most steps are identical.
 */
export function compareScreens(bufferA, bufferB, areas, withImage) {
  const a = PNG.sync.read(bufferA);
  const b = PNG.sync.read(bufferB);
  if (a.width !== b.width || a.height !== b.height) {
    return { outside: Infinity, areas: [], image: null };
  }
  const counts = areas.map(() => 0);
  const image = withImage ? new PNG({ width: a.width, height: a.height }) : null;
  let outside = 0;
  for (let i = 0; i < a.data.length; i += 4) {
    const d = Math.max(
      Math.abs(a.data[i] - b.data[i]),
      Math.abs(a.data[i + 1] - b.data[i + 1]),
      Math.abs(a.data[i + 2] - b.data[i + 2]),
    );
    if (d > 2) {
      const x = (i / 4) % a.width;
      const y = Math.floor(i / 4 / a.width);
      const area = areas.findIndex((candidate) => candidate && inside(x, y, candidate));
      if (area >= 0) {
        counts[area]++;
      } else {
        outside++;
      }
      image?.data.set([255, 0, 0, 255], i);
    } else {
      image?.data.set([a.data[i] / 3, a.data[i + 1] / 3, a.data[i + 2] / 3, 255], i);
    }
  }
  return { outside, areas: counts, image: image && PNG.sync.write(image) };
}

/**
 * Where each control is (run.mjs --geometry): the box of every visible match of its selector, top to bottom and left
 * to right, at most 40. Runs in the page; the same on both, so its results can be compared.
 */
export function readBoxes(controls) {
  const boxes = {};
  for (const [name, selector] of Object.entries(controls)) {
    const found = [];
    for (const el of document.querySelectorAll(selector.replace(/:visible/g, ''))) {
      const box = el.getBoundingClientRect();
      if (box.width > 0 && box.height > 0 && getComputedStyle(el).visibility !== 'hidden') {
        found.push([box.x, box.y, box.width, box.height].map((n) => Math.round(n * 10) / 10));
      }
    }
    if (found.length) {
      // In reading order: the pages may hold the same boxes in another order in their markup
      boxes[name] = found.sort((a, b) => a[1] - b[1] || a[0] - b[0]).slice(0, 40);
    }
  }
  return boxes;
}

/**
 * What differs in where the controls are: a control on one page only, a different number of matches, or a match whose
 * position or size differs by more than `margin` px (the room the themes' fonts need), one line per control.
 */
export function compareBoxes(legacy, angular, margin, skip = () => false) {
  const differences = [];
  const show = (box) => (box ? `${box[0]},${box[1]} ${box[2]}x${box[3]}` : 'none');
  // What GEOMETRY compares of a box
  const part = {
    centre: ([x, y, w, h]) => [x + w / 2, y + h / 2],
    start: ([x, y, , h]) => [x, y, h],
    last: (box) => box,
  };
  for (const name of new Set([...Object.keys(legacy), ...Object.keys(angular)])) {
    if (skip(name)) continue;
    const by = GEOMETRY[name]?.by;
    let a = legacy[name] ?? [];
    let b = angular[name] ?? [];
    if (by === 'last') {
      a = a.slice(-1);
      b = b.slice(-1);
    }
    const parts = (box) => (by ? part[by](box) : box);
    if (a.length !== b.length) {
      differences.push(`geometry ${name}: ${a.length} shown on the legacy page, ${b.length} on the Angular page`);
      continue;
    }
    const off = a.findIndex((box, i) => {
      const other = parts(b[i]);
      return parts(box).some((n, k) => Math.abs(n - other[k]) > margin);
    });
    if (off >= 0) {
      differences.push(
        `geometry ${name}${a.length > 1 ? ` #${off + 1}` : ''}: legacy ${show(a[off])}, Angular ${show(b[off])}`,
      );
    }
  }
  return differences;
}
