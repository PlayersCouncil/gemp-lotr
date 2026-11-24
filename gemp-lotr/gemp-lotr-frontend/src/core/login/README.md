# core/login: what the legacy login page does

Ported from `gemp-lotr-async/src/main/web/index.html`, `src/Login/login.js`, `includes/registrationForm.html` and the
calls in `js/gemp-022/communication.js`. Wording is in `login-messages.ts`; the UI shows it as given.

- On load: the legacy page got one line of plain text from GET `/gemp-lotr-server/` (StatusRequestHandler: "Tables
  count: …, players in hall: …, …") and showed it above the form. This page asks GET `/gemp-lotr-server/status` for
  the four counts and words the same line itself (`serverStatusLine`). If it fails, the legacy page alerts "Unable to
  contact the server".
- Login form: "Login below, or [Register]", Login (text), Password (password; Enter submits), [Login], and the link
  "Forgot your password? Contact ketura on the PC Discord." (FORGOT_PASSWORD). No client-side checks.
- POST `/gemp-lotr-server/login` with `login` and `password` (form-encoded, without the legacy `participantId`, which
  the server ignores; the body is `<result>OK</result>`).
  - 200: go to the hall. (The legacy page took a 202 as "the password was reset by an admin" and asked for a new one
    through the register form. Admins no longer reset passwords and the server no longer answers 202 (PC-ACCOUNT.md):
    that path is gone, and any 2xx is a login.)
  - 429 (new: too many attempts): TOO_MANY_ATTEMPTS, keeping the form.
  - 401: INVALID_LOGIN, and the login form is shown again, empty. 403: PERMANENTLY_BANNED, 409: TEMPORARILY_BANNED,
    and the form is hidden (`formShown`). 503: MAINTENANCE.
  - 0: alert CANNOT_CONNECT. Anything else: alert CANNOT_CONTACT (the communication object's failure handler).
- Register form (fetched in legacy from `includes/registrationForm.html`; here the UI draws it): Login, Password,
  Password repeated, [Register]. Only check: the two passwords must match (PASSWORDS_DIFFER, nothing sent).
  The server checks the login (2-30 chars, letters/digits/_/-, no admin/guest/system/bye prefix); the 400 message
  still says 2-10, kept as is.
- POST `/gemp-lotr-server/register` with `login` and `password`.
  - 200: go to the hall. 400: INVALID_NEW_LOGIN. 409: LOGIN_TAKEN. 503: MAINTENANCE. 0/other: alerts as for login.
  - (The legacy 202 PASSWORD_UPDATED branch is gone with the password reset.)
- After 200 the browser goes to `/gemp-lotr/hall.html` plus the page's #fragment when it looks like a hall link
  (`/^#[A-Za-z0-9_\-\/?=&%.+]{1,200}$/`, e.g. `#events/leagues`), else nothing (`returnLink`).
- Cookies: the page reads and writes none itself; the server's answer sets `loggedUser`. No browser checks.
- Differences: switching to Register clears the old error (the legacy `$(".error").html()` was meant to and didn't);
  `showLogin()` goes back (the legacy page had no way back); the shell's player name is refreshed before leaving.
- The info text below the form (Decipher, Player's Council, the Star Wars CCG GEMP shout-out) is static markup for the UI.
- The page (`ui/login`) is one `<form>`: Enter submits from any field, in both forms (the legacy page listened for Enter
  in the login form's password field only). While a call is under way its buttons are disabled (the legacy login call
  was synchronous, so the page froze until the answer).

## The PC account

The page asks GET `/gemp-lotr-server/argonauth/session` first (core/session `PcSessionService`, see PC-ACCOUNT.md) and
draws the form's place once it has answered. In `test` mode, or when the server gives no session, the page is as
above. In `optional` mode, for nobody, the page is two columns with a rule between them (Ketura's layout): "Legacy
Login", the login form without Register (registering is through a PC account there) with the forgotten-password line
under it, and "PC SSO", with "Sign in with PC Account" and "Register PC Account" stacked: links to
`/gemp-lotr-server/argonauth/login`, the second with `register=1`, both carrying the page's `return`. They stack on a
narrow window. A signed-in PC account, or a password login, gets the alias list (`gd-alias-list`, core/session `AliasList`)
in the form's place. `?pc=cancelled` and `?pc=failed` are a line above (`pcNotice`); `?open=create|link` opens that
form of the list.
