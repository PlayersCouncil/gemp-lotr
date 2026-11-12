import {
  BothPages,
  CARD_PLACES,
  CULTURES,
  FILTER_INPUTS,
  FILTER_SELECTS,
  openBothPages,
} from '../testing/parity/both-pages';
import { Observation } from '../testing/parity/both-pages';
import { Choices, choices } from '../testing/parity/choices';
import { DialogAnswer } from '../testing/parity/legacy-page';
import { CONTROLS } from '../../visual-check/controls.mjs';

const TEXTS = ['', 'gandalf', 'the one', 'a+b & c', "frodo's", '  spaced  ', '12', '-3', 'ring bearer'];
/** No markup: the legacy page showed a deck's name as HTML, which the Angular page deliberately doesn't. */
const DECK_NAMES = ['Mine', 'Test + deck & co', 'Fellowship', 'x'.repeat(101), '', 'Rock & "roll" 100%'];

const DECKLISTS = [
  '',
  '2x 7\n12\n3 5',
  'Ring-bearer: 2\nRing: 1\n* 4x 9 (my note)\nstart 319\n320',
  'nothing the server knows\n\n\n8',
];
// Left out: a line that is only "(" (the legacy page fails on it, see differences.parity.spec.ts)
const NOTES = ['', 'Plain notes', 'With + plus and a "quote"', 'Two\nlines'];

/** What a session has done that later choices depend on. */
interface Session {
  /** The deck may have notes: written, or opened or imported with a deck. */
  notes: boolean;
}

interface Step {
  name: string;
  run(pages: BothPages): Promise<void>;
}

/**
 * The next gesture of a session, chosen from what the legacy page shows now. Left out, because the Angular page
 * differs there (differences.parity.spec.ts shows each): game text with more than one space, choosing a product
 * while a slot's own search (which sets the product) is on, converting or leaving a deck that has notes, notes with
 * "&" or "<", and removing a site while "Special in Deck" is chosen.
 */
function nextStep(pages: BothPages, shown: Observation, pick: Choices, session: Session): Step | null {
  const { legacy } = pages;
  if (shown.dialogParts['dialogClose']) {
    return dialogStep(shown, pick, session);
  }
  const kinds: (() => Step | null)[] = [
    () => {
      const button = pick.of(['deckList', 'library', 'importDeck', 'notes'] as const);
      const answers = [pick.of([true, false]), pick.of(DECK_NAMES)];
      return {
        name: `press ${button}, answering ${JSON.stringify(answers)}`,
        run: (p) => pressAnswering(p, button, 0, answers),
      };
    },
    () => {
      const control = pick.of(FILTER_SELECTS);
      const options = legacy.options(control);
      const index = pick.below(options.length);
      if (control === 'product' && shown.collection?.includes(' product:card&')) {
        return null;
      }
      return {
        name: `choose ${control} = "${options[index]?.value}"`,
        run: (p) => p.both((page) => page.choose(control, index)),
      };
    },
    () => {
      const control = pick.of(FILTER_INPUTS);
      const text = pick.of(TEXTS);
      // A value field shows only once its comparison is chosen
      if (legacy.count(control) === 0 || (control === 'gametext' && text.split(' ').length > 2)) {
        return null;
      }
      return { name: `type "${text}" into ${control}`, run: (p) => p.both((page) => page.type(control, text)) };
    },
    () => {
      const code = pick.of(CULTURES);
      // A hidden button can't be pressed
      if (shown.hiddenCultures.includes(code)) {
        return null;
      }
      return { name: `press culture ${code}`, run: (p) => p.both((page) => page.press(CONTROLS.culture(code))) };
    },
    () => {
      const control = pick.of(['nextPage', 'previousPage', 'resetAdvanced', 'resetAll'] as const);
      return { name: `press ${control}`, run: (p) => p.both((page) => page.press(control)) };
    },
    () => {
      const control = pick.of(['format', 'collection'] as const);
      const options = legacy.options(control);
      const index = pick.below(options.length);
      if (options[index]?.disabled) {
        return null;
      }
      return {
        name: `choose ${control} #${index} ("${options[index]?.label}")`,
        run: (p) => p.both((page) => page.choose(control, index)),
      };
    },
    () => {
      const place = pick.of([...CARD_PLACES, 'collectionCards', 'collectionCards', 'collectionCards'] as const);
      const count = legacy.count(place);
      if (count === 0) {
        return null;
      }
      const index = pick.below(count);
      if (place === 'siteCards' && shown.filter['product'] === 'specialForDeck') {
        return null;
      }
      if (place === 'drawCard') {
        // The two pages keep the piles' cards in a different order: the same card is found by its picture
        const image = legacy.image(place, index);
        return {
          name: `press the deck's ${image}`,
          run: (p) => p.both((page) => page.press(CONTROLS.deckCard(image))),
        };
      }
      return { name: `press ${place} #${index}`, run: (p) => p.both((page) => page.press(place, index)) };
    },
    () => {
      const slot = pick.of(['ringBearerSlot', 'ringSlot', 'sitesSlot'] as const);
      if (legacy.count(slot) === 0) {
        return null;
      }
      return { name: `press ${slot}`, run: (p) => p.both((page) => page.press(slot)) };
    },
    () => {
      const button = pick.of(['newDeck', 'saveDeck', 'renameDeck', 'copyDeck', 'convertErrata'] as const);
      if ((button === 'convertErrata' || button === 'newDeck') && session.notes) {
        return null;
      }
      const answers = [pick.of([true, false]), pick.of(DECK_NAMES), pick.of([true, false])];
      return {
        name: `press ${button}, answering ${JSON.stringify(answers)}`,
        run: (p) => pressAnswering(p, button, 0, answers),
      };
    },
  ];
  return pick.of(kinds)();
}

/** The same press on both pages, with these answers to what it asks; answers it didn't ask for are dropped. */
async function pressAnswering(
  pages: BothPages,
  control: string,
  index: number,
  answers: DialogAnswer[],
): Promise<void> {
  pages.answer(...answers);
  await pages.both((page) => page.press(control, index));
  pages.legacy.answers.length = 0;
  pages.angular.answers.length = 0;
}

/** A dialog is open: nothing behind it can be used, only what it holds. */
function dialogStep(shown: Observation, pick: Choices, session: Session): Step | null {
  const parts = shown.dialogParts;
  const kinds: (() => Step | null)[] = [
    () => ({ name: 'close the dialog', run: (p) => p.both((page) => page.press('dialogClose')) }),
    () => {
      const control = pick.of(['openDeck', 'deckRename', 'deckDelete', 'deckShare', 'libraryShow'] as const);
      const count = parts[control] ?? 0;
      if (count === 0) {
        return null;
      }
      const index = pick.below(count);
      const answers = [pick.of([true, false, ...DECK_NAMES]), pick.of([true, false]), pick.of(DECK_NAMES)];
      session.notes ||= control === 'openDeck';
      return {
        name: `press ${control} #${index}, answering ${JSON.stringify(answers)}`,
        run: (p) => pressAnswering(p, control, index, answers),
      };
    },
    () => {
      if (!parts['importText']) {
        return null;
      }
      const decklist = pick.of(DECKLISTS);
      return {
        name: `import ${JSON.stringify(decklist)}`,
        run: async (p) => {
          await p.both((page) => page.type('importText', decklist));
          await p.both((page) => page.press('importButton'));
        },
      };
    },
    () => {
      if (!parts['notesText']) {
        return null;
      }
      const notes = pick.of(NOTES);
      session.notes = true;
      return {
        name: `write the notes ${JSON.stringify(notes)}`,
        run: (p) => p.both((page) => page.type('notesText', notes)),
      };
    },
  ];
  return pick.of(kinds)();
}

/**
 * How many sessions, of how many gestures, starting from which seed. A few run with the unit tests; `npm run
 * test:parity` (part of check:all in visual-check) runs many. A failure names its seed: PARITY_SEED=<seed>
 * PARITY_SESSIONS=1 runs that session alone.
 */
const SESSIONS = Number(process.env['PARITY_SESSIONS'] ?? 10);
const STEPS = Number(process.env['PARITY_STEPS'] ?? 25);
const FIRST_SEED = Number(process.env['PARITY_SEED'] ?? 1);

describe('parity with the legacy page: generated sessions', () => {
  let pages: BothPages;

  beforeEach(async () => (pages = await openBothPages()));
  afterEach(() => pages.close());

  it.each(Array.from({ length: SESSIONS }, (_, i) => FIRST_SEED + i))(
    'session %i: both pages ask, show and save the same after every gesture',
    async (seed) => {
      const pick = choices(seed);
      const done: string[] = [];
      const session: Session = { notes: false };
      const start = pages.observe();
      expect(start.angular, 'as the pages open').toEqual(start.legacy);
      let shown = start.legacy;
      while (done.length < STEPS) {
        const step = nextStep(pages, shown, pick, session);
        if (!step) {
          continue;
        }
        done.push(step.name);
        await step.run(pages);
        const seen = pages.observe();
        expect(seen.angular, `seed ${seed}, after:\n  ${done.join('\n  ')}\n`).toEqual(seen.legacy);
        shown = seen.legacy;
      }
    },
    60_000,
  );
});
