import { ActionResult } from './action-result.interface';

export const createSuccessActionResult = <T = boolean>(
  result: T
): ActionResult<T> => ({
  success: true,
  result,
});

export const createErrorActionResult = <TError>(
  errorMessage: TError
): ActionResult<boolean, TError> => ({
  success: false,
  error: errorMessage,
});
