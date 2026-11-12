import { siteImage, SERVER_URL } from './app-paths';

describe('app paths', () => {
  it('builds the address of an image of the site', () => {
    expect(siteImage('holo.jpg')).toBe('/gemp-lotr/images/holo.jpg');
    expect(siteImage('cultures/elven.svg')).toBe('/gemp-lotr/images/cultures/elven.svg');
  });

  it('knows where the server is', () => {
    expect(SERVER_URL).toBe('/gemp-lotr-server');
  });
});
