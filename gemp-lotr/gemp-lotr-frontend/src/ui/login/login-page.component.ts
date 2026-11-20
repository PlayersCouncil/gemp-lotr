import { Component, computed, inject } from '@angular/core';
import { FORGOT_PASSWORD, LoginMessage, MessageLink } from '../../core/login/login-messages';
import { LoginService } from '../../core/login/login.service';

/** "Forgot your password?  Contact ketura on the PC Discord.", around the name the legacy page drew in orange. */
const CONTACT = 'ketura';
const [FORGOT_BEFORE, FORGOT_AFTER] = FORGOT_PASSWORD.text.split(CONTACT);

/** A piece of a message as drawn: text, a line break (the legacy <br/>) or a link. */
type Segment = { kind: 'text'; text: string } | { kind: 'break' } | ({ kind: 'link' } & MessageLink);

function segments(message: LoginMessage | null): Segment[] {
  return (message ?? []).flatMap((part): Segment[] =>
    typeof part === 'string'
      ? part
          .split('\n')
          .flatMap((text, i): Segment[] => [...(i ? [{ kind: 'break' as const }] : []), { kind: 'text', text }])
      : [{ kind: 'link', ...part }],
  );
}

/**
 * The login page (the legacy index.html): the site's banner, the server's status line, the login form (or the register
 * form, which also sets a new password after a reset), the message of the last attempt, and the static information
 * about the game and GEMP. The state and what the buttons do are LoginService's (core/login, see its README.md); this
 * draws them and passes the player's input on. Enter in a field submits the form.
 *
 * DOM contract: `.gd-login` (`data-mode` = login, register) holding `div.gd-login__brand` (empty; for a theme that
 * draws a logo), `h1.gd-login__banner`, then `.gd-login__content` with `section.gd-login__panel` and
 * `section.gd-login__info`. The panel: `p.gd-login__status` (the server's status line, once loaded),
 * `div.gd-login__error` (`role=alert`; the message: `span`s of text, its line breaks as `br`, its links as `a`), and unless banned
 * `form.gd-login__form` (`data-mode`, `data-busy` and `aria-busy` while a call is under way): in login mode
 * `p.gd-login__intro` ("Login below, or" `button.gd-button[data-action=register]`), a `label.gd-login__field` per field
 * (`data-field` = login, password, password-repeated (register mode only); `span.gd-login__label` and
 * `input.gd-login__input` with `name` and `autocomplete`), `.gd-login__actions` with
 * `button.gd-button[data-action=submit]` (type submit; "Login", "Register" or "Update Password") and in register mode
 * `button.gd-button[data-action=login]` (back to the login form), and in login mode `p.gd-login__forgot` with its link
 * (`span.gd-login__contact` around the name). While busy the buttons are disabled. The info: `p.gd-login__about` and
 * `.gd-login__shoutout` (`img.gd-login__force` either side of `p.gd-login__shoutout-text`).
 */
@Component({
  selector: 'gd-login-page',
  styleUrl: './login-page.component.css',
  host: { class: 'gd-login', '[attr.data-mode]': 'login.mode()' },
  template: `
    <div class="gd-login__brand" aria-hidden="true"></div>
    <h1 class="gd-login__banner">GEMP: Play the Lord of the Rings TCG</h1>
    <div class="gd-login__content">
      <section class="gd-login__panel" aria-label="Log in">
        <p class="gd-login__status">{{ login.serverStatus() }}</p>
        <div class="gd-login__error" role="alert">
          @for (segment of errorSegments(); track $index) {
            @switch (segment.kind) {
              @case ('link') {
                <a [href]="segment.url">{{ segment.text }}</a>
              }
              @case ('break') {
                <br />
              }
              @default {
                <span>{{ segment.text }}</span>
              }
            }
          }
        </div>
        @if (login.formShown()) {
          <form
            class="gd-login__form"
            [attr.data-mode]="login.mode()"
            [attr.data-busy]="login.busy() ? '' : null"
            [attr.aria-busy]="login.busy()"
            (submit)="$event.preventDefault(); submit()"
          >
            @if (login.mode() === 'login') {
              <p class="gd-login__intro">
                Login below, or
                <button
                  type="button"
                  class="gd-button"
                  data-action="register"
                  [disabled]="login.busy()"
                  (click)="login.showRegister()"
                >
                  Register
                </button>
              </p>
            }
            <label class="gd-login__field" data-field="login">
              <span class="gd-login__label">Login: </span>
              <input
                class="gd-login__input"
                type="text"
                name="login"
                autocomplete="username"
                autocapitalize="none"
                spellcheck="false"
                [value]="login.login()"
                (input)="login.setLogin(value($event))"
              />
            </label>
            <label class="gd-login__field" data-field="password">
              <span class="gd-login__label">Password: </span>
              <input
                class="gd-login__input"
                type="password"
                name="password"
                [attr.autocomplete]="login.mode() === 'login' ? 'current-password' : 'new-password'"
                [value]="login.password()"
                (input)="login.setPassword(value($event))"
              />
            </label>
            @if (login.mode() === 'register') {
              <label class="gd-login__field" data-field="password-repeated">
                <span class="gd-login__label">Password repeated: </span>
                <input
                  class="gd-login__input"
                  type="password"
                  name="password-repeated"
                  autocomplete="new-password"
                  [value]="login.passwordRepeated()"
                  (input)="login.setPasswordRepeated(value($event))"
                />
              </label>
            }
            <div class="gd-login__actions">
              <button type="submit" class="gd-button" data-action="submit" [disabled]="login.busy()">
                {{ login.submitLabel() }}
              </button>
              @if (login.mode() === 'register') {
                <button
                  type="button"
                  class="gd-button"
                  data-action="login"
                  [disabled]="login.busy()"
                  (click)="login.showLogin()"
                >
                  Back to login
                </button>
              }
            </div>
            @if (login.mode() === 'login') {
              <p class="gd-login__forgot">
                <a [href]="forgot.url"
                  >{{ forgot.before }}<span class="gd-login__contact">{{ forgot.contact }}</span
                  >{{ forgot.after }}</a
                >
              </p>
            }
          </form>
        }
      </section>
      <section class="gd-login__info" aria-label="About GEMP">
        <p class="gd-login__about">
          <a href="https://wiki.lotrtcgpc.net/wiki/The_Lord_of_the_Rings_TCG">The Lord of the Rings TCG</a> was
          published by Decipher, Inc from 2001-2007.<br />GEMP is a platform for playing this excellent game for free in
          your browser.<br />Maintained by the unofficial <a href="https://lotrtcgpc.net/">Player's Council</a>.
        </p>
        <div class="gd-login__shoutout">
          <img class="gd-login__force" src="https://res.starwarsccg.org/gemp/lightForce-42.png" alt="" />
          <p class="gd-login__shoutout-text">
            (Also check out the <a href="https://gemp.starwarsccg.org/gemp-swccg/">Star Wars CCG GEMP</a> ran by the
            <a href="https://www.starwarsccg.org/">Star Wars Players Committee</a>.)
          </p>
          <img class="gd-login__force" src="https://res.starwarsccg.org/gemp/darkForce-42.png" alt="" />
        </div>
      </section>
    </div>
  `,
})
export class LoginPageComponent {
  protected readonly login = inject(LoginService);
  protected readonly errorSegments = computed(() => segments(this.login.error()));
  protected readonly forgot = {
    url: FORGOT_PASSWORD.url,
    before: FORGOT_BEFORE,
    contact: CONTACT,
    after: FORGOT_AFTER,
  };

  constructor() {
    void this.login.loadStatus();
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  protected submit(): void {
    void this.login.submit();
  }
}
