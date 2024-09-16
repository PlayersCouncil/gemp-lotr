import { HttpErrorMessages } from '../../../../../common/types/http-errors/http-error-messages.interface';
import { convertHttpStatusCodeToFriendlyName } from '../../../../../common/types/http-errors/http-error.functions';
import { loginErrors } from './login-error.constants';

let loginErrorValues: HttpErrorMessages | null = null;

export const getLoginErrorMessage = (statusCode: number): string => {
  if (loginErrorValues === null) {
    loginErrorValues = { ...loginErrors };
  }
  return (
    loginErrorValues[convertHttpStatusCodeToFriendlyName(statusCode)] ?? ''
  );
};
