# core/login: what the legacy login page does

Ported from `gemp-lotr-async/src/main/web/index.html`, `src/Login/login.js`, `includes/registrationForm.html` and the
calls in `js/gemp-022/communication.js`. Wording is in `login-messages.ts`; the UI shows it as given.

- On load: GET `/gemp-lotr-server/` (StatusRequestHandler) gives one line of plain text ("Tables count: …, players in
  hall: …, …"), shown above the form (`serverStatus`). If it fails, the legacy page alerts "Unable to contact the server".
- Login form: "Login below, or [Register]", Login (text), Password (password; Enter submits), [Login], and the link
  "Forgot your password? Contact ketura on the PC Discord." (FORGOT_PASSWORD). No client-side checks.
- POST `/gemp-lotr-server/login` with `login`, `password`, `participantId` (form-encoded; the body is `<result>OK</result>`).
  - 200: go to the hall. 202: the password was reset by an admin: show the register form with the login filled in, the
    button "Update Password" and PASSWORD_RESET_PROMPT; registering then sets the new password.
  - 401: INVALID_LOGIN, and the login form is shown again, empty. 403: PERMANENTLY_BANNED, 409: TEMPORARILY_BANNED,
    and the form is hidden (`formShown`). 503: MAINTENANCE.
  - 0: alert CANNOT_CONNECT. Anything else: alert CANNOT_CONTACT (the communication object's failure handler).
- Register form (fetched in legacy from `includes/registrationForm.html`; here the UI draws it): Login, Password,
  Password repeated, [Register]. Only check: the two passwords must match (PASSWORDS_DIFFER, nothing sent).
  The server checks the login (2-30 chars, letters/digits/_/-, no admin/guest/system/bye prefix); the 400 message
  still says 2-10, kept as is.
- POST `/gemp-lotr-server/register` with `login`, `password`, `participantId`.
  - 200: go to the hall. 400: INVALID_NEW_LOGIN. 409: LOGIN_TAKEN. 503: MAINTENANCE. 0/other: alerts as for login.
  - 202: PASSWORD_UPDATED. The legacy check compares jQuery's text status, so it never matched; the server answers
    200 for a reset too, so today this branch is unused.
- After 200 the browser goes to `/gemp-lotr/hall.html` plus the page's #fragment when it looks like a hall link
  (`/^#[A-Za-z0-9_\-\/?=&%.+]{1,200}$/`, e.g. `#events/leagues`), else nothing (`returnLink`).
- Cookies: the page reads and writes none itself; the server's answer sets `loggedUser`. No browser checks.
- Differences: switching to Register clears the old error (the legacy `$(".error").html()` was meant to and didn't);
  `showLogin()` goes back (the legacy page had no way back); the shell's player name is refreshed before leaving.
- The info text below the form (Decipher, Player's Council, the Star Wars CCG GEMP shout-out) is static markup for the UI.
- The page (`ui/login`) is one `<form>`: Enter submits from any field, in both forms (the legacy page listened for Enter
  in the login form's password field only). While a call is under way its buttons are disabled (the legacy login call
  was synchronous, so the page froze until the answer).
