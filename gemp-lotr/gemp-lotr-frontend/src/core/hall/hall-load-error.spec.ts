import { loadErrorMessage } from './hall-load-error';

describe('loadErrorMessage (neutralErrorMap)', () => {
  it("words each status as the legacy map, naming what wasn't loaded", () => {
    const subject = 'the format definitions';
    expect(loadErrorMessage(0, subject)).toBe(
      'Could not reach the server to load the format definitions. Check your connection and reload the page.',
    );
    expect(loadErrorMessage(400, subject)).toBe(
      'Could not load the format definitions (the server did not understand the request).',
    );
    expect(loadErrorMessage(401, subject)).toBe('You are not logged in. Log in again to see the format definitions.');
    expect(loadErrorMessage(403, subject)).toBe('You do not have access to the format definitions.');
    expect(loadErrorMessage(404, subject)).toBe('The format definitions could not be found.');
    for (const status of [500, 502, 503, 504]) {
      expect(loadErrorMessage(status, subject)).toBe(
        'Could not load the format definitions right now. Please try again in a few minutes.',
      );
    }
  });

  it('reads any other status as the server being unavailable, and has a default subject', () => {
    expect(loadErrorMessage(429)).toBe('Could not load this page right now. Please try again in a few minutes.');
    expect(loadErrorMessage(-1)).toBe('Could not load this page right now. Please try again in a few minutes.');
  });
});
