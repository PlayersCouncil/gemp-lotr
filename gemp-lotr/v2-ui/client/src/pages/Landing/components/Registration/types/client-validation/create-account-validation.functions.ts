import {
  ActionResult,
  createErrorActionResult,
  createSuccessActionResult,
} from '../../../../../../common/action-result';
import * as AccountCreationErrorMessages from './account-validation-errors.constants';
import {
  defaultRegisrationErrorState,
  RegistrationErrorState,
} from './registration-error-state.interface';

const loginRegex = /[@,^,&,*,(,),#,$,!]/;

export const isLoginValid = (
  login: string
): ActionResult<boolean, RegistrationErrorState> => {
  const containsInvalidCharacters = loginRegex.test(login);

  if (containsInvalidCharacters) {
    return createErrorActionResult<RegistrationErrorState>({
      login: AccountCreationErrorMessages.invalidLoginCharacters,
    });
  }

  if (login.length < 3 || login.length > 10) {
    return createErrorActionResult<RegistrationErrorState>({
      login: AccountCreationErrorMessages.invalidLength,
    });
  }
  return createSuccessActionResult(true) as ActionResult<
    boolean,
    RegistrationErrorState
  >;
};

export const isValidAccount = (
  login: string,
  password: string,
  passwordConfimation: string
): RegistrationErrorState => {
  const errors = defaultRegisrationErrorState();
  if (login === '' || password === '' || passwordConfimation === '') {
    errors.global = 'Please provide all required fields';
    return errors;
  }
  const isValidLogin = isLoginValid(login);
  if (!isValidLogin.success && isValidLogin.error) {
    // this is what bums me out, it should be able to infer that no success means an error is there
    return isValidLogin.error;
  }

  return errors;
};
