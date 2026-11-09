// The same control on each page, as { legacy, angular } selectors: the legacy page's element IDs and classes, and the
// Angular page's DOM contract (the `.gd-*` markup and its data-* state, see the frontend README). Shared by run.mjs
// (through scenarios.mjs). `which` is 'legacy' or 'angular'.

/** A field of the collection filter, by its `data-field`: its select or text input (numeric: its comparison select). */
const field = (legacy, name, control = 'select') => ({
  legacy,
  angular: `.gd-field[data-field="${name}"] ${control}`,
});
/** The value input of a numeric filter field. */
const numericValue = (legacy, name) => ({
  legacy,
  angular: `.gd-field[data-field="${name}"] input[type=number]`,
});
const toolbar = (legacy, action) => ({ legacy, angular: `.gd-toolbar [data-action="${action}"]` });
const slot = (name) => `.gd-slot[data-slot="${name}"]`;

export const CONTROLS = {
  // --- Collection pane ---
  nextPage: { legacy: '#nextPage', angular: '.gd-pager__next' },
  previousPage: { legacy: '#previousPage', angular: '.gd-pager__previous' },
  /** The page size: the legacy jQuery UI slider, the Angular range input. */
  pageSize: { legacy: '#countSlider', angular: '.gd-pager__size' },
  side: field('#sideSelect', 'side'),
  product: field('#productSelect', 'product'),
  rarity: field('#raritySelect', 'rarity'),
  block: field('#blockSelect', 'block'),
  set: field('#setSelect', 'set'),
  sort: field('#sortSelect', 'sort'),
  cardType: field('#cardTypeSelect', 'cardType'),
  name: field('#nameInput', 'name', 'input'),
  twilightCompare: field('#twilightCompareSelect', 'twilight'),
  twilightValue: numericValue('#twilightValueInput', 'twilight'),
  strengthCompare: field('#strengthCompareSelect', 'strength'),
  strengthValue: numericValue('#strengthValueInput', 'strength'),
  resetAdvanced: {
    legacy: '#resetAdvancedFiltersButton',
    angular: '.gd-filter__resets [data-action="reset-advanced"]',
  },
  resetAll: {
    legacy: '#resetAllFiltersButton',
    angular: '.gd-filter__resets [data-action="reset-all"]',
  },
  /** A culture button, by its code (ELVEN, SHIRE, ...). */
  culture: (code) => ({ legacy: `#label${code}`, angular: `.gd-culture[data-culture="${code}"]` }),
  /** The checkbox behind a culture button (it takes the keyboard focus). */
  cultureInput: (code) => ({
    legacy: `#${code}`,
    angular: `.gd-culture[data-culture="${code}"] .gd-culture__input`,
  }),
  /** The pane that scrolls the filter and the collection. */
  collectionPane: { legacy: '#collectionDiv', angular: '.gd-page__collection' },
  /** Every item of the collection (packs first). */
  firstCard: {
    legacy: '#collection-display .card .actionArea',
    angular: '.gd-collection .gd-card',
  },
  /** The collection's cards only (no packs or selection packs). */
  collectionCards: {
    legacy: '.cardInCollection .actionArea',
    angular: '.gd-collection .gd-card[data-role="card"]',
  },
  /** Selection packs only. */
  selectionPacks: {
    legacy: '.selectionInCollection .actionArea',
    angular: '.gd-collection .gd-card[data-role="selection"]',
  },
  /** The bar between the deck and the collection (drag to resize), and its toggle. */
  separator: { legacy: '.ui-layout-resizer-east', angular: '.gd-page__separator' },
  toggleCollection: { legacy: '.ui-layout-toggler-east', angular: '.gd-page__toggle' },

  // --- Toolbar ---
  deckList: toolbar('#deckListBut', 'deck-list'),
  library: toolbar('#libraryListBut', 'library'),
  importDeck: toolbar('#importDeckBut', 'import'),
  notes: toolbar('#notesBut', 'notes'),
  saveDeck: toolbar('#saveDeckBut', 'save'),
  renameDeck: toolbar('#renameDeckBut', 'rename'),
  copyDeck: toolbar('#copyDeckBut', 'copy'),
  autoZoom: toolbar('#auto-zoom-toggle', 'auto-zoom'),
  format: { legacy: '#formatSelect', angular: 'select.gd-toolbar__format' },
  collection: { legacy: '#collectionSelect', angular: 'select.gd-toolbar__collection' },

  // --- Deck ---
  ringBearerSlot: { legacy: '#rb-content', angular: `${slot('ring-bearer')} .gd-slot__label` },
  ringSlot: { legacy: '#ring-content', angular: `${slot('ring')} .gd-slot__label` },
  sitesSlot: { legacy: '#sites-content', angular: `${slot('sites')} .gd-slot__label` },
  mapLink: { legacy: '#map-content a', angular: `${slot('map')} .gd-slot__link` },
  ringBearerCard: {
    legacy: '#ringBearerDiv .cardInDeck .actionArea',
    angular: `${slot('ring-bearer')} .gd-card`,
  },
  siteCards: { legacy: '#sitesDiv .cardInDeck .actionArea', angular: `${slot('sites')} .gd-card` },
  /** A card by its image, in the deck's piles or in the collection. */
  deckCard: (image) => ({
    legacy: `#decksRegion .card:has(img[src*="${image}"]) .actionArea`,
    angular: `.gd-pile .gd-card:not([data-role="copy"]):has(img[src*="${image}"])`,
  }),

  // --- Dialogs ---
  /** The close button of the open dialog (legacy dialogs stay in the page while closed, hence :visible). */
  dialogClose: { legacy: '.ui-dialog-titlebar-close:visible', angular: 'dialog[open] .gd-dialog__close' },
  deckListItem: { legacy: '.deckItem', angular: '.gd-deck-list__item' },
  openDeck: { legacy: 'button[title="Open deck"]', angular: '[data-action="open"]' },
  importText: { legacy: '#deckImport', angular: '.gd-import__text' },
  importButton: {
    legacy: '.ui-dialog button[title="Import"]',
    angular: '.gd-import [data-action="import"]',
  },
  cardInfoZoom: { legacy: '#card-info-slider', angular: '.gd-card-info__zoom' },
  cardInfoCard: { legacy: '#card-image-content .actionArea', angular: '.gd-card-info__card' },
};
