export interface RegistrationErrorState {
  global?: string;
  login?: string;
  password?: string;
}

export const defaultRegisrationErrorState = (): RegistrationErrorState => ({
  global: '',
  login: '',
  password: '',
});
