import { gamePageUrl, legacyRelativeImages, siteImage, SERVER_URL } from './app-paths';

describe('app paths', () => {
  it('builds the address of an image of the site', () => {
    expect(siteImage('holo.jpg')).toBe('/gemp-lotr/images/holo.jpg');
    expect(siteImage('cultures/elven.svg')).toBe('/gemp-lotr/images/cultures/elven.svg');
  });

  it("shows the server HTML's relative pictures from the legacy pages' folder, and leaves other addresses", () => {
    expect(
      legacyRelativeImages(
        "Remove a <span class='cultureHint' value='GONDOR'><img src='images/cultures/gondor.png'/> Gondor</span> token",
      ),
    ).toBe(
      "Remove a <span class='cultureHint' value='GONDOR'><img src='/gemp-lotr/images/cultures/gondor.png'/> Gondor</span> token",
    );
    expect(legacyRelativeImages('<IMG class="x" SRC="images/a.png">')).toBe(
      '<IMG class="x" SRC="/gemp-lotr/images/a.png">',
    );
    for (const kept of [
      "<img src='/gemp-lotr/images/a.png'>",
      '<img src="https://example.org/a.png">',
      '<img src="data:image/png;base64,AA">',
      'images/a.png without a picture',
    ]) {
      expect(legacyRelativeImages(kept)).toBe(kept);
    }
  });

  it("gives the game page's address for a game", () => {
    expect(gamePageUrl('42')).toBe('/gemp-lotr/ng/game?gameId=42');
    expect(gamePageUrl('a&b')).toBe('/gemp-lotr/ng/game?gameId=a%26b');
  });

  it('knows where the server is', () => {
    expect(SERVER_URL).toBe('/gemp-lotr-server');
  });
});
