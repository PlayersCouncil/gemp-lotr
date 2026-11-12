/**
 * Test-only: the same gestures on the legacy page and on the Angular page, by the names of visual-check/controls.mjs
 * (one list of where each control is on each page). src/parity drives both pages with these and compares.
 */
import { CONTROLS, ControlSelectors } from '../../../visual-check/controls.mjs';
import { AngularPage } from './angular-page';
import { DialogAnswer, LegacyJQuery, LegacyPage } from './legacy-page';

/** A control of controls.mjs by name, or its selectors (for the ones made by a function, e.g. CONTROLS.culture). */
export type Control = string | ControlSelectors;

/** After a gesture: long enough for the server's answers, the stats' refresh and a collection page's images. */
const SETTLE_MS = 5000;

function selectorsOf(control: Control): ControlSelectors {
  const selectors = typeof control === 'string' ? CONTROLS[control] : control;
  if (!selectors || typeof selectors === 'function') {
    throw new Error(`controls.mjs has no control "${String(control)}"`);
  }
  // jQuery's :visible asks for an element's size, which only a real browser knows: here isShown decides instead
  return { legacy: selectors.legacy.replace(/:visible/g, ''), angular: selectors.angular };
}

export interface SelectOption {
  value: string;
  label: string;
  disabled: boolean;
}

export interface PageDriver {
  readonly dialogs: string[];
  readonly answers: DialogAnswer[];
  /** How many elements of the control the page shows now (not the ones in a closed dialog or a hidden part). */
  count(control: Control): number;
  /** The text of each of them, with white space collapsed. */
  texts(control: Control): string[];
  /** The control's text (of its first element), with white space collapsed. */
  text(control: Control): string;
  /** The file name of the picture of the control's index-th card. */
  image(control: Control, index: number): string;
  /** A select's options: their values and labels, and whether they can be chosen. */
  options(control: Control): SelectOption[];
  /** The value of a select or an input. */
  value(control: Control): string;
  /** Which option of a select is chosen. */
  chosen(control: Control): number;
  /** Chooses an option of a select by its place in the list (two options can have the same value). */
  choose(control: Control, index: number): void;
  /** Replaces the text of an input and leaves it (which is when both pages react). The same text changes nothing. */
  type(control: Control, text: string): void;
  /** A click of the main mouse button on the control's index-th element. A disabled control can't be pressed. */
  press(control: Control, index?: number): void;
  /** Lets the page finish what the gesture started. */
  settle(): Promise<void>;
}

const collapse = (text: string | null | undefined) => (text ?? '').replace(/\s+/g, ' ').trim();

/** A mouse event as a browser makes it: jsdom leaves out `which` (1 = the main button), which the legacy page reads. */
function mouse(view: Window, target: Element, type: string): void {
  const Mouse = (view as unknown as { MouseEvent: typeof MouseEvent }).MouseEvent;
  const event = new Mouse(type, { bubbles: true, cancelable: true, button: 0, clientX: 5, clientY: 5 });
  if (!event.which) {
    Object.defineProperty(event, 'which', { value: 1 });
  }
  target.dispatchEvent(event);
}

/** Not hidden by its own or an enclosing element's style or `hidden`, and not in a closed dialog. */
function isShown(element: Element): boolean {
  for (let at: Element | null = element; at; at = at.parentElement) {
    const closedDialog = at.tagName === 'DIALOG' && !at.hasAttribute('open');
    if ((at as HTMLElement).style.display === 'none' || at.hasAttribute('hidden') || closedDialog) {
      return false;
    }
  }
  return true;
}

/** The picture of the card an element is, or is part of. */
function imageOf(element: Element): string {
  const card = element.closest('.card, .gd-card') ?? element;
  const address = card.querySelector('img')?.getAttribute('src') ?? '';
  return decodeURIComponent(address.split('/').at(-1) ?? '');
}

const optionOf = (o: HTMLOptionElement): SelectOption => ({
  value: o.value,
  label: collapse(o.text),
  disabled: o.disabled,
});

/** As in a browser, where a press on a disabled button or field does nothing. */
const pressable = (target: Element) => !target.matches(':disabled, :disabled *');

function required<T>(found: T | null | undefined, what: string): T {
  if (found === null || found === undefined) {
    throw new Error(`Not on the page: ${what}`);
  }
  return found;
}

export function driveLegacy(page: LegacyPage): PageDriver {
  const present = (control: Control) => {
    const found = page.$(selectorsOf(control).legacy);
    return Array.from({ length: found.length }, (_, i) => found.get(i) as HTMLElement);
  };
  const all = (control: Control) => present(control).filter(isShown);
  const element = (control: Control, index = 0) =>
    required(all(control)[index], `${selectorsOf(control).legacy} [${index}] on the legacy page`);
  /** A field, shown or not: its value can be read while it is hidden. */
  const field = (control: Control) =>
    required(present(control)[0], `${selectorsOf(control).legacy} on the legacy page`) as HTMLSelectElement;
  const jQuery = (target: HTMLElement) => (page.window as unknown as { $: (e: HTMLElement) => LegacyJQuery }).$(target);
  return {
    dialogs: page.dialogs,
    answers: page.answers,
    count: (control) => all(control).length,
    texts: (control) => all(control).map((found) => collapse(found.textContent)),
    text: (control) => collapse(all(control)[0]?.textContent),
    image: (control, index) => imageOf(element(control, index)),
    options: (control) => [...field(control).options].map(optionOf),
    value: (control) => field(control).value,
    chosen: (control) => field(control).selectedIndex,
    choose: (control, index) => {
      const select = element(control) as HTMLSelectElement;
      select.selectedIndex = index;
      jQuery(select).change();
    },
    type: (control, text) => {
      const input = element(control) as HTMLInputElement;
      if (input.value !== text) {
        jQuery(input).val(text).change();
      }
    },
    press: (control, index = 0) => {
      const target = element(control, index);
      for (const type of pressable(target) ? ['mousedown', 'mouseup', 'click'] : []) {
        mouse(page.window as unknown as Window, target, type);
      }
    },
    settle: () => {
      page.advance(SETTLE_MS);
      return Promise.resolve();
    },
  };
}

export function driveAngular(page: AngularPage): PageDriver {
  const present = (control: Control) => [
    ...page.element.ownerDocument.querySelectorAll<HTMLElement>(selectorsOf(control).angular),
  ];
  const all = (control: Control) => present(control).filter(isShown);
  const element = (control: Control, index = 0) =>
    required(all(control)[index], `${selectorsOf(control).angular} [${index}] on the Angular page`);
  /** A field, shown or not: its value can be read while it is hidden. */
  const field = (control: Control) =>
    required(present(control)[0], `${selectorsOf(control).angular} on the Angular page`) as HTMLSelectElement;
  const changed = (target: HTMLElement) => {
    target.dispatchEvent(new Event('input', { bubbles: true }));
    target.dispatchEvent(new Event('change', { bubbles: true }));
  };
  return {
    dialogs: page.dialogs,
    answers: page.answers,
    count: (control) => all(control).length,
    texts: (control) => all(control).map((found) => collapse(found.textContent)),
    text: (control) => collapse(all(control)[0]?.textContent),
    image: (control, index) => imageOf(element(control, index)),
    options: (control) => [...field(control).options].map(optionOf),
    value: (control) => field(control).value,
    chosen: (control) => field(control).selectedIndex,
    choose: (control, index) => {
      const select = element(control) as HTMLSelectElement;
      select.selectedIndex = index;
      changed(select);
    },
    type: (control, text) => {
      const input = element(control) as HTMLInputElement;
      if (input.value !== text) {
        input.value = text;
        changed(input);
      }
    },
    press: (control, index = 0) => {
      const target = element(control, index);
      const view = required(target.ownerDocument.defaultView, 'a window');
      for (const type of pressable(target) ? ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click'] : []) {
        mouse(view, target, type);
      }
    },
    settle: () => page.advance(SETTLE_MS),
  };
}
