import {
  HttpErrorMessages,
  defaultHttpErrorMessages,
} from '../../../../../../common/types/http-errors';

export const registrationErrors: HttpErrorMessages = {
  ...defaultHttpErrorMessages(),
  conflict:
    'User with this login already exists in the system. Try a different one.',
  badRequest:
    'Login is invalid. Login must be between 2-10 characters long, and contain only<br/> english letters, numbers or _ (underscore) and - (dash) characters.',
};
