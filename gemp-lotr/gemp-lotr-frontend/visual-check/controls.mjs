// The same control on each page, as { legacy, angular } selectors: the legacy page's element IDs and classes, and the
// Angular page's DOM contract (the `.gd-*` markup and its data-* state, see the frontend README). Shared by run.mjs
// (through scenarios.mjs) and stubbed.mjs. `which` is 'legacy' or 'angular'.

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
  /** The selection pack in the collection (the second item). */
  selectionPack: {
    legacy: '#collection-display .card .actionArea',
    angular: '.gd-collection .gd-card',
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
  /** The draw deck's cards (copies included). */
  drawDeckCards: { legacy: '#decksRegion .cardInDeck .actionArea', angular: '.gd-pile .gd-card' },
  ringBearerSlot: { legacy: '#rb-content', angular: `${slot('ring-bearer')} .gd-slot__label` },
  ringSlot: { legacy: '#ring-content', angular: `${slot('ring')} .gd-slot__label` },
  sitesSlot: { legacy: '#sites-content', angular: `${slot('sites')} .gd-slot__label` },
  ringBearerCard: {
    legacy: '#ringBearerDiv .cardInDeck .actionArea',
    angular: `${slot('ring-bearer')} .gd-card`,
  },
  siteCards: { legacy: '#sitesDiv .cardInDeck .actionArea', angular: `${slot('sites')} .gd-card` },
  /** A card by its image, in the deck's piles or in the collection. */
  deckCard: (image) => ({
    legacy: `#decksRegion .card:has(img[src*="${image}"]) .actionArea`,
    angular: `.gd-pile .gd-card:has(img[src*="${image}"])`,
  }),
  collectionCard: (image) => ({
    legacy: `#collection-display .card:has(img[src*="${image}"]) .actionArea`,
    angular: `.gd-collection .gd-card:has(img[src*="${image}"])`,
  }),

  // --- Dialogs ---
  deckListItem: { legacy: '.deckItem', angular: '.gd-deck-list__item' },
  openDeck: { legacy: 'button[title="Open deck"]', angular: '[data-action="open"]' },
  deleteDeck: {
    legacy: '.deckItem button[title="Delete deck"]',
    angular: '.gd-deck-list [data-action="delete"]',
  },
  /** Rename in the deck list (not the toolbar's renameDeck). */
  renameListedDeck: {
    legacy: '.deckItem button[title="Rename deck"]',
    angular: '.gd-deck-list [data-action="rename"]',
  },
  importText: { legacy: '#deckImport', angular: '.gd-import__text' },
  importButton: {
    legacy: '.ui-dialog button[title="Import"]',
    angular: '.gd-import [data-action="import"]',
  },
  cardInfoZoom: { legacy: '#card-info-slider', angular: '.gd-card-info__zoom' },
  cardInfoCard: { legacy: '#card-image-content .actionArea', angular: '.gd-card-info__card' },
};
