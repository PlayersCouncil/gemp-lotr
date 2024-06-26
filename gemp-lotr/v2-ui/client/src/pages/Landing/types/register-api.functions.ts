import { AxiosRequestConfig } from 'axios';

export interface RegisterRequest {
  login: string;
  password: string;
}

export const getLoginConfiguration = (
  login: string,
  password: string
): AxiosRequestConfig<RegisterRequest> => ({
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  data: {
    login,
    password,
  },
});
