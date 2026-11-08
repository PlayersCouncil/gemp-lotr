/**
 * TEMPORARY (removed when the new UI replaces the old one): `?ui=next` starts the new UI (src/ui) instead of the
 * current one, with the legacy stylesheets switched off, so the themes are seen on their own.
 */
export function wantsNextUi(search: string): boolean {
  return new URLSearchParams(search).get('ui') === 'next';
}

/** Switches off the legacy stylesheets (index.html marks them) and the legacy page's body class. */
export function leaveLegacyStyles(document: Document): void {
  document.querySelectorAll<HTMLLinkElement>('link[data-legacy-stylesheet]').forEach((link) => link.remove());
  document.body.classList.remove('ui-layout-container');
}
