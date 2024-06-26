import { defaultHttpErrorMessages } from '../../../../../common/types/http-errors/http-error-messages.functions';
import { HttpErrorMessages } from '../../../../../common/types/http-errors/http-error-messages.interface';

export const loginErrors: HttpErrorMessages = {
  ...defaultHttpErrorMessages(),
  unauthorized: 'Invalid username or password. Try again.',
  forbidden:
    'You have been permanently banned. If you think it was a mistake you can try sending a private message to Merrick_H http://lotrtcgwiki.com/forums/',
  badRequest:
    'You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake you can try sending a private message to Merrick_H on http://lotrtcgwiki.com/forums/',
};
