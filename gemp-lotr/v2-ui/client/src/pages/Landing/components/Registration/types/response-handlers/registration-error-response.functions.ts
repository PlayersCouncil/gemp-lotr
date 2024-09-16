import {
  convertHttpStatusCodeToFriendlyName,
  HttpErrorMessages,
} from '../../../../../../common/types/http-errors';
import { registrationErrors } from './registration-error-response.constants';

let registrationErrorValues: HttpErrorMessages | null = null;

export const getRegistrationErrorMessages = (statusCode: number): string => {
  if (registrationErrorValues === null) {
    registrationErrorValues = { ...registrationErrors };
  }
  return (
    registrationErrorValues[convertHttpStatusCodeToFriendlyName(statusCode)] ??
    ''
  );
};
