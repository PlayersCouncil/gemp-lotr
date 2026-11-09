// Differences from the legacy page that the owner has accepted (trymarz/gemp-lotr-private#26). Only the owner adds to
// this list: anything not in it fails the check. An entry allows at most `maxPixels` differing pixels inside the area of
// `selector` (the Angular page's, grown by `margin` px); every other pixel still counts towards the normal tolerance.
//
//   scenarios  which scenarios it applies to
//   sizes      which window sizes (default: all)
//   reason     what differs and why it is accepted

const SLIDER_RING = 'Slider handle focus ring: Chrome drew the legacy ring itself, CSS can only approximate its shape';

export const ACCEPTED = [
  {
    scenarios: ['card-info', 'card-info-zoom-flip', 'card-info-close-drag'],
    selector: '.gd-card-info__zoom',
    margin: 8,
    maxPixels: 180,
    reason: SLIDER_RING,
  },
  { scenarios: ['state-slider-focus'], selector: '.gd-pager__size', margin: 8, maxPixels: 180, reason: SLIDER_RING },
  {
    scenarios: ['page-size-slider'],
    selector: '.gd-pager',
    margin: 8,
    maxPixels: 480,
    reason: 'Requested by the owner: the page size handle stays inside its bar and never covers the arrow buttons',
  },
  {
    scenarios: ['state-button-focus'],
    selector: '.gd-toolbar [data-action="notes"]',
    margin: 4,
    maxPixels: 250,
    reason: 'Toolbar button keyboard focus ring, 1px shorter',
  },
  {
    scenarios: ['state-dialog-close-hover', 'state-dialog-close-focus'],
    selector: 'dialog[open] .gd-dialog__close',
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
    sizes: ['1920x1080'],
    selector: '.gd-slot[data-slot="sites"] .gd-card:last-child',
    margin: 3,
    maxPixels: 32000,
    reason: 'The last site 1px off at some window sizes (legacy floating-point rounding)',
  },
];

export function acceptedFor(scenario, sizeLabel) {
  return ACCEPTED.filter(
    (entry) => entry.scenarios.includes(scenario) && (!entry.sizes || entry.sizes.includes(sizeLabel)),
  );
}
