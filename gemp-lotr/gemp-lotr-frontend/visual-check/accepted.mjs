// Differences from the legacy page that the owner has accepted (trymarz/gemp-lotr-private#26). Only the owner adds to
// this list: anything not in it fails the check. An entry allows at most `maxPixels` differing pixels inside the area of
// `selector` (the Angular page's, grown by `margin` px); every other pixel still counts towards the normal tolerance.
//
//   scenarios  which scenarios it applies to: names, or patterns for the generated ones of matrix.mjs
//   sizes      which window sizes (default: all)
//   reason     what differs and why it is accepted

const SLIDER_RING = 'Slider handle focus ring: Chrome drew the legacy ring itself, CSS can only approximate its shape';

export const ACCEPTED = [
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

export function acceptedFor(scenario, sizeLabel) {
  const applies = (pattern) => (pattern instanceof RegExp ? pattern.test(scenario) : pattern === scenario);
  return ACCEPTED.filter((entry) => entry.scenarios.some(applies) && (!entry.sizes || entry.sizes.includes(sizeLabel)));
}
