import { readLegacyWebFile } from '../../testing/files';
import * as messages from './login-messages';
import { LoginMessage, messageText } from './login-messages';

/** A message as the legacy page wrote it: "\n" was <br/>, links were <a href='url>text</a> (sic: no closing quote). */
function legacyHtml(message: LoginMessage): string {
  return message
    .map((part) => (typeof part === 'string' ? part.replace(/\n/g, '<br/>') : `<a href='${part.url}>${part.text}</a>`))
    .join('');
}

describe('login messages', () => {
  // Read as text only, to check the wording; the legacy script is not run.
  const loginJs = readLegacyWebFile('src/Login/login.js').replace(/"\s*\+\s*"/g, '');

  const htmlMessages: [string, LoginMessage][] = Object.entries(messages).filter(
    (entry): entry is [string, LoginMessage] => Array.isArray(entry[1]),
  );

  it.each(htmlMessages)('%s has the wording of login.js', (_, message) => {
    expect(loginJs).toContain(legacyHtml(message));
  });

  it.each([
    ['CANNOT_CONTACT', messages.CANNOT_CONTACT],
    ['CANNOT_CONNECT', messages.CANNOT_CONNECT],
    ['FORGOT_PASSWORD', messages.FORGOT_PASSWORD.text],
    ['UPDATE_PASSWORD_BUTTON', messages.UPDATE_PASSWORD_BUTTON],
  ])('%s has the wording of login.js', (_, text) => {
    expect(loginJs.replace(/<\/?span[^>]*>/g, '')).toContain(text);
  });

  it('flattens a message to text', () => {
    expect(messageText(messages.PERMANENTLY_BANNED)).toBe(
      'You have been permanently banned. If you think it was a mistake please appeal with dmaz or ketura on the PC ' +
        'Discord.',
    );
  });
});
