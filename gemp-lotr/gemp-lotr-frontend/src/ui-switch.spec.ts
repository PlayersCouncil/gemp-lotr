import { leaveLegacyStyles, wantsNextUi } from './ui-switch';

describe('ui switch (temporary)', () => {
  it('starts the new UI only with ?ui=next', () => {
    expect(wantsNextUi('?ui=next')).toBe(true);
    expect(wantsNextUi('?participantId=bob&ui=next&theme=modern')).toBe(true);
    expect(wantsNextUi('')).toBe(false);
    expect(wantsNextUi('?ui=old')).toBe(false);
  });

  it('removes the legacy stylesheets and body class, and nothing else', () => {
    const doc = document.implementation.createHTMLDocument();
    doc.head.innerHTML = `
      <link rel="stylesheet" data-legacy-stylesheet href="/gemp-lotr/css/gemp-001/deckBuild.css">
      <link rel="stylesheet" data-legacy-stylesheet href="/gemp-lotr/css/gemp-001/hall.css">
      <link rel="stylesheet" href="styles.css">
      <link rel="icon" href="/favicon.png">`;
    doc.body.className = 'ui-layout-container';
    leaveLegacyStyles(doc);
    expect(Array.from(doc.head.querySelectorAll('link')).map((link) => link.getAttribute('href'))).toEqual([
      'styles.css',
      '/favicon.png',
    ]);
    expect(doc.body.className).toBe('');
  });
});
