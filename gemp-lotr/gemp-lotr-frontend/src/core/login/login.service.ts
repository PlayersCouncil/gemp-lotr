import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../api/api-error';
import { Notifier } from '../notifier/notifier';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { PlayerInfo } from '../session/player-info.service';
import { LoginApi } from './login-api.service';
import {
  CANNOT_CONNECT,
  CANNOT_CONTACT,
  INVALID_LOGIN,
  INVALID_NEW_LOGIN,
  LOGIN_BUTTON,
  LOGIN_TAKEN,
  LoginMessage,
  MAINTENANCE,
  PASSWORD_RESET_PROMPT,
  PASSWORD_UPDATED,
  PASSWORDS_DIFFER,
  PERMANENTLY_BANNED,
  REGISTER_BUTTON,
  TEMPORARILY_BANNED,
  UPDATE_PASSWORD_BUTTON,
} from './login-messages';
import { loginTarget } from './return-link';

/** Which form is shown: log in, or register (also used to set a new password after a reset). */
export type LoginMode = 'login' | 'register';

/** The register form's check (the only one the legacy page makes): the passwords must match. */
export function registrationError(password: string, passwordRepeated: string): LoginMessage | null {
  return password === passwordRepeated ? null : PASSWORDS_DIFFER;
}

/**
 * The login page's state and what its buttons do, ported from src/Login/login.js (see README.md for the inventory).
 * The page's own (LOGIN_SERVICES). Messages from the server go to `error`; what the legacy page alerted goes to
 * the Notifier. Once logged in it refreshes the player and leaves for `redirectTarget`.
 */
@Injectable()
export class LoginService {
  private readonly api = inject(LoginApi);
  private readonly notifier = inject(Notifier);
  private readonly navigation = inject(PageNavigation);
  private readonly players = inject(PlayerInfo);
  private readonly target = loginTarget(inject(PAGE_QUERY_STRING));

  private readonly mode_ = signal<LoginMode>('login');
  private readonly passwordReset_ = signal(false);
  private readonly login_ = signal('');
  private readonly password_ = signal('');
  private readonly passwordRepeated_ = signal('');
  private readonly error_ = signal<LoginMessage | null>(null);
  private readonly formShown_ = signal(true);
  private readonly busy_ = signal(false);
  private readonly serverStatus_ = signal<string | null>(null);
  private readonly redirectTarget_ = signal<string | null>(null);

  readonly mode = this.mode_.asReadonly();
  /** The register form is setting a reset account's new password (its button reads "Update Password"). */
  readonly passwordReset = this.passwordReset_.asReadonly();
  readonly login = this.login_.asReadonly();
  readonly password = this.password_.asReadonly();
  readonly passwordRepeated = this.passwordRepeated_.asReadonly();
  /** The message shown above the form (the legacy ".error"), or null. */
  readonly error = this.error_.asReadonly();
  /** False once the account is banned: the legacy page then emptied the form. */
  readonly formShown = this.formShown_.asReadonly();
  /** A login or register call is under way; submitting again is ignored. */
  readonly busy = this.busy_.asReadonly();
  /** The server's status line (tables, players in the hall, ...), once loaded. */
  readonly serverStatus = this.serverStatus_.asReadonly();
  /** Where the page goes once logged in; set just before leaving. */
  readonly redirectTarget = this.redirectTarget_.asReadonly();

  readonly submitLabel = computed(() =>
    this.mode_() === 'login' ? LOGIN_BUTTON : this.passwordReset_() ? UPDATE_PASSWORD_BUTTON : REGISTER_BUTTON,
  );

  /** Loads the server's status line (once the page is shown). */
  async loadStatus(): Promise<void> {
    try {
      this.serverStatus_.set(await firstValueFrom(this.api.serverStatus()));
    } catch {
      await this.notifier.alert(CANNOT_CONTACT);
    }
  }

  setLogin(value: string): void {
    this.login_.set(value);
  }

  setPassword(value: string): void {
    this.password_.set(value);
  }

  setPasswordRepeated(value: string): void {
    this.passwordRepeated_.set(value);
  }

  /** The Register button: an empty register form. */
  showRegister(): void {
    this.showForm('register');
    this.error_.set(null);
  }

  /** Back to an empty login form. */
  showLogin(): void {
    this.showForm('login');
    this.error_.set(null);
  }

  /** The form's button (or Enter in the password field). */
  submit(): Promise<void> {
    return this.mode_() === 'login' ? this.logIn() : this.register();
  }

  async logIn(): Promise<void> {
    if (this.busy_()) {
      return;
    }
    const login = this.login_();
    this.busy_.set(true);
    try {
      const outcome = await firstValueFrom(this.api.login(login, this.password_()));
      if (outcome === 'passwordReset') {
        this.showForm('register');
        this.passwordReset_.set(true);
        this.login_.set(login);
        this.error_.set(PASSWORD_RESET_PROMPT);
      } else {
        await this.leave();
      }
    } catch (error) {
      await this.loginFailed(error);
    } finally {
      this.busy_.set(false);
    }
  }

  async register(): Promise<void> {
    if (this.busy_()) {
      return;
    }
    const mismatch = registrationError(this.password_(), this.passwordRepeated_());
    if (mismatch) {
      this.error_.set(mismatch);
      return;
    }
    this.busy_.set(true);
    try {
      const outcome = await firstValueFrom(this.api.register(this.login_(), this.password_()));
      if (outcome === 'passwordUpdated') {
        this.error_.set(PASSWORD_UPDATED);
      } else {
        await this.leave();
      }
    } catch (error) {
      await this.registerFailed(error);
    } finally {
      this.busy_.set(false);
    }
  }

  private showForm(mode: LoginMode): void {
    this.mode_.set(mode);
    this.passwordReset_.set(false);
    this.formShown_.set(true);
    this.login_.set('');
    this.password_.set('');
    this.passwordRepeated_.set('');
  }

  /** Logged in: the shell's player is asked again (a failure there doesn't stop us), then to the hall. */
  private async leave(): Promise<void> {
    try {
      await this.players.refresh();
    } catch {
      // The hall asks again anyway.
    }
    this.redirectTarget_.set(this.target);
    this.navigation.go(this.target);
  }

  private async loginFailed(error: unknown): Promise<void> {
    switch (statusOf(error)) {
      case 401:
        this.showForm('login');
        this.error_.set(INVALID_LOGIN);
        return;
      case 403:
        this.error_.set(PERMANENTLY_BANNED);
        this.formShown_.set(false);
        return;
      case 409:
        this.error_.set(TEMPORARILY_BANNED);
        this.formShown_.set(false);
        return;
      default:
        return this.commonFailure(error);
    }
  }

  private async registerFailed(error: unknown): Promise<void> {
    switch (statusOf(error)) {
      case 400:
        this.error_.set(INVALID_NEW_LOGIN);
        return;
      case 409:
        this.error_.set(LOGIN_TAKEN);
        return;
      default:
        return this.commonFailure(error);
    }
  }

  /** What login and register handle alike: maintenance, no connection, anything else. */
  private async commonFailure(error: unknown): Promise<void> {
    const status = statusOf(error);
    if (status === 503) {
      this.error_.set(MAINTENANCE);
    } else {
      await this.notifier.alert(status === 0 ? CANNOT_CONNECT : CANNOT_CONTACT);
    }
  }
}

function statusOf(error: unknown): number | null {
  return error instanceof ApiError ? error.status : null;
}
