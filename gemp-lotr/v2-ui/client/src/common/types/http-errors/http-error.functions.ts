import { HttpErrorMessages } from './http-error-messages.interface';

export const convertHttpStatusCodeToFriendlyName = (
  statusCode: string | number
): keyof HttpErrorMessages => {
  const code = statusCode.toString();
  switch (code) {
    case '0':
      return 'noConnection';
    case '400':
      return 'badRequest';
    case '401':
      return 'unauthorized';
    case '403':
      return 'forbidden';
    case '409':
      return 'conflict';
    case '503':
      return 'serverDown';
    case '500':
    default:
      return 'internalServerError';
  }
};
