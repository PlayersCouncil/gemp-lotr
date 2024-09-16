export type HttpGlobalErrorMessages = {
  noConnection: string;
  internalServerError: string;
  serverDown: string;
};

export type HttpErrorMessages = HttpGlobalErrorMessages & {
  unauthorized?: string;
  conflict?: string;
  forbidden?: string;
  badRequest?: string;
};
