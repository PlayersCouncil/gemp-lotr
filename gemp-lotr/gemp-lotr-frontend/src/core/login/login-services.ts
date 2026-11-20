import { Provider } from '@angular/core';
import { LoginApi } from './login-api.service';
import { LoginService } from './login.service';

/**
 * The login page's own state and calls, provided by its route. PlayerInfo, the Notifier and the platform services it
 * uses are app-wide.
 */
export const LOGIN_SERVICES: Provider[] = [LoginApi, LoginService];
