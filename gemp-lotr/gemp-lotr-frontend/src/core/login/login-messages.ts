/** A link inside a message (the legacy page put <a> tags in some of them). */
export interface MessageLink {
  readonly text: string;
  readonly url: string;
}

/** A message of the login page: text and links, in order. Line breaks are "\n" (the legacy <br/>). */
export type LoginMessage = readonly (string | MessageLink)[];

/** The message as plain text (links as their text). */
export function messageText(message: LoginMessage): string {
  return message.map((part) => (typeof part === 'string' ? part : part.text)).join('');
}

export const PC_DISCORD_URL = 'https://lotrtcgpc.net/discord';

const PC_DISCORD: MessageLink = { text: 'the PC Discord', url: PC_DISCORD_URL };

// The wording of src/Login/login.js, unchanged (double spaces included).
export const CANNOT_CONTACT = 'Unable to contact the server';
export const CANNOT_CONNECT =
  'Unable to connect to server, either server is down or there is a problem with your internet connection';
export const INVALID_LOGIN: LoginMessage = ['Invalid username or password. Try again.'];
export const PERMANENTLY_BANNED: LoginMessage = [
  'You have been permanently banned. If you think it was a mistake please appeal with dmaz or ketura on ',
  PC_DISCORD,
  '.',
];
export const TEMPORARILY_BANNED: LoginMessage = [
  'You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake please ' +
    'appeal with dmaz or ketura on ',
  PC_DISCORD,
  '.',
];
export const MAINTENANCE: LoginMessage = ['Server is down for maintenance. Please come at a later time.'];
export const PASSWORD_RESET_PROMPT: LoginMessage = ['Your password has been reset.  Please enter a new password.'];
export const PASSWORDS_DIFFER: LoginMessage = ['Password and Password repeated are different! Try again'];
export const PASSWORD_UPDATED: LoginMessage = [
  'Your password has successfully been reset!  Please refresh the page and log in.',
];
export const INVALID_NEW_LOGIN: LoginMessage = [
  'Login is invalid. Login must be between 2-10 characters long, and contain only\n' +
    ' english letters, numbers or _ (underscore) and - (dash) characters.',
];
export const LOGIN_TAKEN: LoginMessage = ['User with this login already exists in the system. Try a different one.'];

/** The link under the login form. */
export const FORGOT_PASSWORD: MessageLink = {
  text: 'Forgot your password?  Contact ketura on the PC Discord.',
  url: PC_DISCORD_URL,
};

/** Button labels: the register form's button reads "Update Password" after a password reset. */
export const LOGIN_BUTTON = 'Login';
export const REGISTER_BUTTON = 'Register';
export const UPDATE_PASSWORD_BUTTON = 'Update Password';
