import { AxiosRequestConfig } from 'axios';
import { LoginRequestInterface } from './login-request-body.interface';

export default (
  login: string,
  password: string
): AxiosRequestConfig<LoginRequestInterface> => ({
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  data: {
    login,
    password,
  },
});
