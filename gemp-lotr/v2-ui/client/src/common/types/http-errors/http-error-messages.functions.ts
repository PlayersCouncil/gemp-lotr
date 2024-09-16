import { HttpGlobalErrorMessages } from './http-error-messages.interface';

export const defaultHttpErrorMessages = (): HttpGlobalErrorMessages => ({
  noConnection:
    'Unable to connect to server, either server is down or there is a problem' +
    ' with your internet connection',
  serverDown: 'Server is down for maintenance. Please come at a later time.',
  internalServerError: 'Unknown Internal Server Error',
});
