import { readLegacyWebFile } from '../../testing/files';
import { DECKBUILDER_HREF, HALL_DISCLAIMER, HALL_INFO_LINKS } from './hall-links';

describe('hall links', () => {
  const html = readLegacyWebFile('hall-legacy.html');

  it("are the legacy hall's link buttons, labels and addresses, in order", () => {
    const legacy = [
      ...html.matchAll(
        /<a id="\w+-button" class="toolbar-button" href="([^"]+)"[^>]*>[\s\S]*?<span[^>]*>([^<]+)<\/span>/g,
      ),
    ].map((m) => ({ href: m[1], label: m[2] }));
    expect(HALL_INFO_LINKS.map(({ label }) => label)).toEqual(legacy.map(({ label }) => label));
    expect(HALL_INFO_LINKS.slice(0, 4).map(({ href }) => href)).toEqual(legacy.slice(0, 4).map(({ href }) => href));
    expect(HALL_INFO_LINKS.at(-1)).toMatchObject({ href: '/gemp-lotr/merchant.html', struck: true });
  });

  it('opens the Angular deck builder, and has the legacy disclaimer', () => {
    expect(DECKBUILDER_HREF).toBe('/gemp-lotr/ng/deckbuilder/');
    const disclaimer = /<div id="disclaimer">([\s\S]*?)<\/div>/.exec(html)?.[1]?.replace(/\s+/g, ' ').trim();
    expect(HALL_DISCLAIMER).toBe(disclaimer);
  });
});
