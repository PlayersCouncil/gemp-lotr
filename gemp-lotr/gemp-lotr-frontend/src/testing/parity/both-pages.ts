/**
 * Test-only: the legacy page and the Angular page side by side, each with a server of its own, and what a test compares
 * after each gesture: what each asked its server, what it shows and what it left saved.
 */
import { CONTROLS } from '../../../visual-check/controls.mjs';
import { AngularPage, openAngularPage } from './angular-page';
import { Failure, FakeGempServer, Writing } from './fake-gemp-server';
import { DialogAnswer, LegacyPage, openLegacyPage } from './legacy-page';
import { Control, driveAngular, driveLegacy, PageDriver } from './page-driver';

export const FILTER_SELECTS = [
  'side',
  'product',
  'rarity',
  'keyword',
  'phase',
  'race',
  'itemClass',
  'signet',
  'block',
  'set',
  'sort',
  'cardType',
  'siteNumberCompare',
  'vitalityCompare',
  'resistanceCompare',
  'twilightCompare',
  'strengthCompare',
] as const;
export const FILTER_INPUTS = [
  'name',
  'gametext',
  'siteNumberValue',
  'vitalityValue',
  'resistanceValue',
  'twilightValue',
  'strengthValue',
] as const;
export const TOOLBAR_BUTTONS = [
  'newDeck',
  'saveDeck',
  'renameDeck',
  'copyDeck',
  'importDeck',
  'library',
  'deckList',
  'convertErrata',
  'notes',
] as const;
/** What the dialogs show: the deck list and the library, the import and the notes. */
export const DIALOG_PARTS = [
  'dialogClose',
  'openDeck',
  'deckRename',
  'deckDelete',
  'deckShare',
  'libraryShow',
  'importText',
  'notesText',
] as const;
/** The collection's packs: boosters and selection packs. */
export const PACKS = {
  legacy: '.packInCollection .actionArea, .selectionInCollection .actionArea',
  angular: '.gd-collection .gd-card:is([data-role="pack"], [data-role="selection"])',
};
export const CARD_PLACES = ['collectionCards', 'drawCard', 'ringBearerCard', 'ringCard', 'siteCards'] as const;
export const CULTURES = [
  'DWARVEN',
  'ELVEN',
  'GANDALF',
  'GONDOR',
  'ROHAN',
  'SHIRE',
  'GOLLUM',
  'DUNLAND',
  'ISENGARD',
  'MEN',
  'MORIA',
  'ORC',
  'RAIDER',
  'SAURON',
  'URUK_HAI',
  'WRAITH',
] as const;

/** What a page shows and has asked its server, after a gesture. */
export interface Observation {
  /** The newest request for a page of the collection: what the collection shows. */
  collection: string | null;
  /** The newest request for the deck's stats: it carries the deck as the page holds it. */
  stats: string | null;
  /** Every other request since the previous observation, in order. */
  requests: string[];
  dialogs: string[];
  deckName: string;
  /** Which options of the format and collection lists are chosen. */
  format: number;
  collectionChoice: number;
  /** What the page's scripts threw (the legacy page only; an Angular error fails the test by itself). */
  errors: string[];
  filter: Record<string, string>;
  cards: Record<string, number>;
  /** The titles of the open dialogs, in alphabetical order. */
  dialogTitles: string[];
  /** How many of each part of a dialog are on screen (none while no dialog is open). */
  dialogParts: Record<string, number>;
  disabled: string[];
  hiddenCultures: string[];
  /** The decks on the server. */
  saved: string;
}

/**
 * How the legacy client wrote a missing value: the word "null". For the notes of a new deck the owner accepted the
 * Angular page not copying it (visual-check/accepted.mjs, LEGACY_BUGS); a missing league is the same on the server.
 */
const legacyWriting: Writing = (values) => {
  const { leagueCode, ...rest } = values;
  return {
    ...rest,
    ...(leagueCode === undefined || leagueCode === 'null' ? {} : { leagueCode }),
    ...(values['notes'] === 'null' ? { notes: '' } : {}),
  };
};

const isCollectionPage = (request: string) =>
  request.startsWith('GET /collection/') && !request.startsWith('GET /collection/import/');
const isStats = (request: string) => request.startsWith('POST /deck/stats');
const isDelivery = (request: string) => request.startsWith('GET /delivery/');

class Side {
  private seen = 0;

  constructor(
    readonly server: FakeGempServer,
    readonly driver: PageDriver,
    private readonly writing: Writing,
    private readonly cultureHidden: (code: string) => boolean,
    private readonly isDisabled: (control: Control) => boolean,
    private readonly errors: string[] = [],
    private readonly sharedListTitle = false,
  ) {}

  /**
   * A legacy bug the Angular page doesn't copy (accepted, visual-check/accepted.mjs): the deck list and the library
   * shared one dialog, so whichever opened first gave both its title. On the legacy page the title comes from what
   * the dialog holds instead, so the two pages go on being compared.
   */
  private titles(titles: string[]): string[] {
    if (!this.sharedListTitle) {
      return titles;
    }
    const list = this.driver.count('libraryShow') > 0 ? 'Library Decks' : 'Your Saved Decks';
    return titles.map((title) => (title === 'Library Decks' || title === 'Your Saved Decks' ? list : title));
  }

  observe(): Observation {
    const all = this.server.requests(this.writing);
    const fresh = all.slice(this.seen);
    this.seen = all.length;
    return {
      collection: all.filter(isCollectionPage).at(-1) ?? null,
      stats: all.filter(isStats).at(-1) ?? null,
      requests: fresh.filter((request) => !isCollectionPage(request) && !isStats(request) && !isDelivery(request)),
      // A new tab's address with or without the site in front is the same page
      dialogs: this.driver.dialogs.splice(0).map((dialog) => dialog.replace(/^(new tab: )https?:\/\/[^/]+/, '$1')),
      deckName: this.driver.text('deckName'),
      format: this.driver.chosen('format'),
      collectionChoice: this.driver.chosen('collection'),
      errors: this.errors.splice(0),
      filter: Object.fromEntries([...FILTER_SELECTS, ...FILTER_INPUTS].map((name) => [name, this.driver.value(name)])),
      cards: {
        ...Object.fromEntries(CARD_PLACES.map((place) => [place, this.driver.count(place)])),
        packs: this.driver.count(PACKS),
        selectionChoice: this.driver.count('selectionChoice'),
      },
      dialogTitles: this.titles(this.driver.texts('dialogTitles')).sort(),
      dialogParts: Object.fromEntries(DIALOG_PARTS.map((part) => [part, this.driver.count(part)])),
      disabled: TOOLBAR_BUTTONS.filter((button) => this.isDisabled(button)),
      hiddenCultures: CULTURES.filter((code) => this.cultureHidden(code)),
      saved: this.server.saved(this.writing),
    };
  }
}

export interface BothPages {
  readonly legacy: PageDriver;
  readonly angular: PageDriver;
  /** The same gesture on both pages, then both settle. */
  both(gesture: (page: PageDriver) => void): Promise<void>;
  /** The same answers to the dialogs the next gesture opens. */
  answer(...answers: DialogAnswer[]): void;
  /** From now on both servers answer this request with an error. */
  fail(failure: Failure): void;
  observe(): { legacy: Observation; angular: Observation };
  close(): void;
}

export interface BothPagesOptions {
  /** Changes each page's server before the page opens (e.g. another saved deck). */
  prepare?: (server: FakeGempServer) => void;
  /**
   * False leaves the legacy page with a culture that a format hid still pressed (see releaseHiddenCultures), for
   * showing that difference itself.
   */
  releaseHiddenCultures?: boolean;
}

/** Opens both pages. Needs `vi.useFakeTimers()` to be off before, and turns it on (close() turns it off). */
export async function openBothPages(options: BothPagesOptions = {}): Promise<BothPages> {
  const legacyServer = new FakeGempServer();
  options.prepare?.(legacyServer);
  const legacyPage: LegacyPage = await openLegacyPage(legacyServer);
  vi.useFakeTimers();
  const angularServer = new FakeGempServer();
  options.prepare?.(angularServer);
  const angularPage: AngularPage = await openAngularPage(angularServer);
  const legacy = driveLegacy(legacyPage);
  const angular = driveAngular(angularPage);

  const legacyElement = (selector: string) => legacyPage.$(selector).get(0);
  const angularElement = (selector: string) => angularPage.element.ownerDocument.querySelector<HTMLElement>(selector);
  const legacyHidden = (element: HTMLElement | undefined) => {
    for (let at = element; at; at = at.parentElement ?? undefined) {
      if (at.style.display === 'none') {
        return true;
      }
    }
    return false;
  };
  const legacySide = new Side(
    legacyServer,
    legacy,
    legacyWriting,
    (code) => legacyHidden(legacyElement(CONTROLS.culture(code).legacy)),
    (control) => {
      const button = legacyElement(CONTROLS[control as string]?.legacy ?? '');
      return !!button && ((button as HTMLButtonElement).disabled || button.classList.contains('ui-button-disabled'));
    },
    legacyPage.errors,
    true,
  );
  const angularSide = new Side(
    angularServer,
    angular,
    (values) => values,
    (code) => angularElement(CONTROLS.culture(code).angular)?.closest('[hidden]') != null,
    (control) =>
      (angularElement(CONTROLS[control as string]?.angular ?? '') as HTMLButtonElement | null)?.disabled === true,
  );

  /**
   * A legacy bug the Angular page doesn't copy: a culture pressed while its group showed stayed pressed, and kept
   * filtering, after a format hid the group. The legacy page tried to release it and failed; here it is released for
   * it, so the two pages go on from the same state.
   */
  const releaseHiddenCultures = async () => {
    const pressedAndHidden = CULTURES.filter((code) => {
      const label = legacyElement(CONTROLS.culture(code).legacy);
      return label?.classList.contains('ui-state-active') && legacyHidden(label);
    });
    for (const code of pressedAndHidden) {
      // Its failed attempt left the checkbox unticked under a button still shown pressed: ticked again, a click
      // releases both, as a click on a pressed button does
      const checkbox = legacyElement(CONTROLS.cultureInput(code).legacy) as HTMLInputElement | undefined;
      if (checkbox) {
        checkbox.checked = true;
      }
      // An open legacy dialog lets no click through to the page behind it: this one isn't a player's
      const overlay = (legacyPage.window as unknown as { $: { ui: { dialog: { overlay: { maxZ: number } } } } }).$.ui
        .dialog.overlay;
      const maxZ = overlay.maxZ;
      overlay.maxZ = -1;
      legacyElement(CONTROLS.culture(code).legacy)?.click();
      overlay.maxZ = maxZ;
    }
    if (pressedAndHidden.length) {
      await legacy.settle();
    }
  };

  return {
    legacy,
    angular,
    both: async (gesture) => {
      gesture(legacy);
      await legacy.settle();
      if (options.releaseHiddenCultures !== false) {
        await releaseHiddenCultures();
      }
      gesture(angular);
      await angular.settle();
    },
    fail: (failure) => {
      legacyServer.failures.push(failure);
      angularServer.failures.push(failure);
    },
    answer: (...answers) => {
      legacy.answers.push(...answers);
      angular.answers.push(...answers);
    },
    observe: () => ({ legacy: legacySide.observe(), angular: angularSide.observe() }),
    close: () => {
      angularPage.close();
      vi.useRealTimers();
      legacyPage.close();
    },
  };
}
