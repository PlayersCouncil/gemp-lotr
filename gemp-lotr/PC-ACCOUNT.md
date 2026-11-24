# The PC Account In Gemp

Signing in to Gemp with a Player's Council account, beside the old username and password.
The account service is Argonauth (`X:\Projects\Argonauth`, `auth.lotrtcgpc.net`); nobody who
plays sees that name. On screen it is "PC Account".

Decided with Ketura on 2026-11-23, starting from `migration/mellon-plan/05-gemp.md`, which
this replaces where they differ. What Gemp asks of Argonauth is in
`migration/mellon-plan/08-gemp-integration-notes.md`; the wire contract is Argonauth's
`docs/INTEGRATING.md`.

## Words

| Word | Means |
|---|---|
| PC account | The person: an Argonauth account, known to Gemp by its permanent `sub` |
| Alias | A Gemp account (`player` row). A PC account may have any number |
| Link | Argonauth's record that an alias belongs to a PC account. Argonauth is the authority; Gemp keeps a copy (`player_argonauth`) |
| Session | Gemp's own, in memory: which PC account is signed in (if any) and which alias is being played (if any). The two are separate |

## Modes

`argonauth.mode` in `gemp-lotr.properties`. Moving between modes is a setting and a restart.

| Mode | PC account | Password login | Registration | Where |
|---|---|---|---|---|
| `test` (default) | Nowhere to be seen; the `argonauth` addresses answer 404, but for `session` and `logout` | Yes | Local, as it always was | Local development, test servers |
| `optional` | "Sign in with PC Account" beside the form | Yes, for accounts that have a password | Through a PC account only | Production, from Yuletide 2026 |
| `pester`, `required`, `argonauth_only` | | | | Named in `05`; not built. The server refuses to start in them |

| Setting | |
|---|---|
| `argonauth.mode` | Above |
| `argonauth.issuer` | The address in tokens and the one browsers are sent to. `https` unless the host is `localhost` |
| `argonauth.client.id`, `argonauth.client.secret` | This Gemp's registration at that Argonauth |
| `argonauth.redirect.uri` | This Gemp's public callback address, exactly as registered: `<site>/gemp-lotr-server/argonauth/callback` |
| `argonauth.postlogout.uri` | Where Argonauth sends the browser after a sign-out: `<site>/gemp-lotr/ng/login`, as registered |
| `argonauth.backchannel.url` | Optional. Where Gemp's server reaches Argonauth, when that differs from the issuer (both in containers on one machine). Defaults to the issuer |

A test Gemp is never registered at production Argonauth: links are keyed by `player.id`, and
a test server's ids are production's ids for other people. Test servers that try the
integration use the dev Argonauth, which has its own database.

### Turning It On

1. **The table.** Run `db/2026-11-23_player-argonauth.sql` on the database. It is empty until someone signs in
   with a PC account and never read in `test` mode, so it can go in ahead of time. (A new database gets it from
   `database_script.sql`.)
2. **The registration at Argonauth.** `python manage.py client save ...` there, with this Gemp's two addresses
   exactly as they will be set here (Argonauth's `docs/INTEGRATING.md`). A local Argonauth already has `gemp-dev`
   for a Gemp on port 17001.
3. **The settings.** Each `argonauth.x.y` can be given as the environment variable `ARGONAUTH_X_Y`, which wins
   over the properties file; `docker/.env` reaches the app's container, so the secret goes there and not in the
   repository. For a local Gemp against a local Argonauth, both in Docker:

       ARGONAUTH_MODE=optional
       ARGONAUTH_ISSUER=http://localhost:1630
       ARGONAUTH_CLIENT_ID=gemp-dev
       ARGONAUTH_CLIENT_SECRET=gemp-dev-secret
       ARGONAUTH_REDIRECT_URI=http://localhost:17001/gemp-lotr-server/argonauth/callback
       ARGONAUTH_POSTLOGOUT_URI=http://localhost:17001/gemp-lotr/ng/login
       ARGONAUTH_BACKCHANNEL_URL=http://host.docker.internal:1630

   On Linux Docker the app's service also needs `extra_hosts: ["host.docker.internal:host-gateway"]`. On the
   droplets there is no back channel: leave it unset and the server uses the public https address.
4. **Restart.** The log says `PC accounts: argonauth.mode is optional, at <issuer> as <client id>`. A setting
   that is missing or malformed stops the server and names itself. Argonauth is first reached for when someone
   signs in, not at start: a Gemp can start while Argonauth is down.

Going back is `ARGONAUTH_MODE=test` and a restart. Aliases created through a PC account have no password, so in
`test` mode nobody can log in to them; they are there again when the mode is.

## What A Person Does

**Signing in.** "Sign in with PC Account" on the login page goes to Argonauth and comes
back. With one alias the person lands in the hall. With several, or none, they land on the
login page, which now shows their aliases:

- each alias with **Enter Gemp**;
- **Create New Gemp Alias**: asks for a name, creates the account, enters the hall as it;
- **Link Existing Gemp Account**: asks for an old username and password, and adds that account;
- **Log Out**.

The list is paged at 10. There is no limit on aliases.

**The same list** shows whenever someone with a PC account signed in opens the login page,
and on the profile page (My Account), which also has a link to manage the PC account.

**Someone logged in with a password** sees their one account on the login page with Enter
Gemp and Log Out, and on the profile page a **Connect PC Account** button, which asks for the
account's password once more, goes through Argonauth, and links the account they are logged in as.

**Switching is gated, per browser.** A browser does not leave the alias it is playing for another one, entered
or newly created, while that alias sits at a table, in a queue, in a running tournament or in a game. Leagues do
not count. The busy alias itself can always be entered again, and Log Out is always allowed. The gate is that
browser's alone: what the PC account's other aliases are doing in other browsers does not come into it, because
people do play themselves with two of their own accounts (Ketura, 2026-11-23). So it keeps someone from walking
off a table by mistake; it is not a limit of one alias at a time.

**Two log-outs.**

| From | Ends | Lands on |
|---|---|---|
| The hall | The alias. The PC account stays signed in | The alias list on the login page |
| The alias list | Everything, the PC session at Argonauth included | Argonauth's sign-out page, then the login page |

For a password login both simply log out.

**Registration** in `optional` is "Sign in with PC Account", then Create New Gemp Alias. The
old registration form is gone there. An alias made this way has no Gemp password.

**A lost Gemp password.** Admins no longer reset passwords. An admin checks the player is
who they say (the deck-name check, as before), then links the Gemp account to the PC
account the player names, in Argonauth's admin. The player signs in with the PC account and
finds the alias in their list. The Gemp admin page shows the player's id for this.

## Rules The Code Keeps

- **Gemp never trusts a PC account's username.** It enters an alias only because Argonauth
  says that alias is linked to the `sub` that just signed in.
- **Only a PC-account session can switch.** A password login for one alias opens that alias
  and nothing else, whatever it is linked beside.
- **Every enter is checked with Argonauth** (`GET /api/v1/links/{id}`), because an admin
  unlink there is not pushed here. Gemp's own copy decides only what the list shows. (The one
  alias of a sign-in is entered on that sign-in's own userinfo, seconds old.)
- **An alias being played is looked at again every five minutes** in every PC-account session
  used since the last look: one Argonauth no longer says is theirs is put down, and the browser
  is back at its list. An Argonauth that cannot be asked puts nobody down.
- **The player comes from the session cookie and nothing else.** The `participantId`
  parameter no longer names the acting player for anyone.
- **Linking and connecting need the account's password**, typed then, with guesses counted as
  at the login form. Being logged in is not enough: a browser left logged in, or a borrowed
  cookie, must not make an account somebody's for good. An account with a blank password (a
  reset that was never completed, an alias made through a PC account, a bot) cannot be linked
  with one, and cannot log in with one.
- **Only a password login connects.** A PC account's own aliases are linked already, and one
  it has lost to an admin's unlink is not taken back that way.
- **Nothing that changes anything answers a GET.** The two pages below only start or finish a
  trip to Argonauth.
- **Creating and linking need a fresh PC sign-in** (Argonauth's token lasts ten minutes).
  Started later in a session, they pass through Argonauth first; with a live PC session
  that shows no page.
- **Nothing in the sign-in blocks the server's IO threads.** Calls to Argonauth run on
  virtual threads with timeouts.

## The Server's Addresses

All under `/gemp-lotr-server/argonauth`. In `test` mode all but `session` and `logout` answer 404: logging out
is for every mode.

### Pages (the browser is sent to these)

| | |
|---|---|
| `GET /login` | Starts a PC sign-in: 302 to Argonauth. `then` = `link` or `create`: land on the list with that form open. `return` = a place in the app to go to after entering (as the login page's `?return=`) |
| `GET /callback` | Argonauth's answer. Always ends in a 302 into the app (below) |

Where the callback lands:

| Outcome | Lands on |
|---|---|
| One alias, nothing asked by `then` | `return`, or `/gemp-lotr/ng/hall` |
| Several aliases, or none | `/gemp-lotr/ng/login` |
| `then=link` or `then=create` | `/gemp-lotr/ng/login?open=link` or `?open=create` |
| Started by `POST /connect`, linked | `/gemp-lotr/ng/account?pc=connected` |
| Started by `POST /connect`, the alias belongs to another PC account | `/gemp-lotr/ng/account?pc=linked-elsewhere`. The browser stays a password login |
| The person cancelled at Argonauth | `/gemp-lotr/ng/login?pc=cancelled`; for a connect, `/gemp-lotr/ng/account?pc=cancelled` |
| Anything else went wrong | `/gemp-lotr/ng/login?pc=failed`; for a connect, `/gemp-lotr/ng/account?pc=failed` |

### Data (JSON)

`GET /session`: who this browser is. Public, always 200, in every mode.

```json
{
  "mode": "optional",
  "player": "Underhill",
  "pcAccount": { "name": "Frodo Baggins", "manageUrl": "https://auth.lotrtcgpc.net/", "fresh": true },
  "linked": true,
  "aliases": [
    { "id": 900001, "name": "Frodo", "current": false, "busy": false },
    { "id": 900002, "name": "Underhill", "current": true, "busy": true }
  ],
  "blockedBy": "Underhill"
}
```

| Field | |
|---|---|
| `mode` | `test` or `optional`. In `test` every other field is as for a password login |
| `player` | The alias this session plays as, or null |
| `pcAccount` | Null for a password login and for nobody. `name` is the display name. `fresh` is false once Argonauth's token has lapsed: create and link then start with `GET /login?then=...` |
| `linked` | Whether `player` is linked to a PC account, by Gemp's copy. Null with no player. For a password login this decides whether "Connect PC Account" is offered |
| `aliases` | Every alias of the signed-in PC account, by name. Empty without one. `busy`: sits at a table, in a queue, in a running tournament or in a game, in whatever browser. Shown, and gates nothing by itself |
| `blockedBy` | `player`, when a PC account is signed in and that alias is busy; otherwise null. While set, this browser can enter only that alias, and can create none |

The actions answer 200 with the session as above, or an error as `{"error": "..."}`:

| | Form fields | Errors |
|---|---|---|
| `POST /enter` | `playerId` | `401 no-pc-account`; `403 not-linked` (the list has been corrected; ask for the session again); `403 banned`; `409 busy`; `503 unavailable` (Argonauth did not answer) |
| `POST /create` | `name` | `401 no-pc-account`; `401 reauth`; `400 invalid-name`; `409 name-taken`; `409 busy`; `429 slow-down`; `503 unavailable` |
| `POST /link` | `login`, `password` | `401 no-pc-account`; `401 reauth`; `401 bad-credentials` (also for an account that cannot be linked with a password); `409 linked-elsewhere`; `429 slow-down`; `503 unavailable` |
| `POST /connect` | `password` | For a password login: checks the password of the account this browser is logged in to, and answers `{"next": "<address>"}`, Argonauth's sign-in address, in place of the session. `403 not-allowed` (nobody is logged in, or a PC account is); `401 bad-credentials`; `429 slow-down`; `503 unavailable` |
| `POST /logout` | `scope` = `alias` or `all` | None. Answers `{"next": "<address>"}`: where to send the browser, which for `all` with a PC account is Argonauth's own address. Sets no cookie: the session is ended here, and its cookie names nothing |

`create` enters the new alias. `link` does not enter the linked one. A PC account creates at most 10 aliases an
hour (Argonauth takes 20 links an hour from one person, creating and linking together); an alias that could not
be recorded at Argonauth is taken back, and its name with it.

`POST /gemp-lotr-server/register` answers 403 in any mode but `test`.

**Password guesses** (the login form, `link` and `connect` alike) are counted over ten minutes: 10 wrong for one
name from one address, 30 for one name from everywhere, 50 from one address whatever the names. At a limit the
answer is 429 and the password is not looked at. The limit for a name from everywhere is the one that can be
turned on a player (wrong passwords typed for a rival's name on purpose), which is why it is the higher one and
why one address alone cannot reach it. A request's address is the one nginx saw: the last entry of
`X-Forwarded-For`.

## Sessions And The Cookie

- One cookie, `loggedUser`, as before: now `HttpOnly`, `SameSite=Lax`, and `Secure` when the
  request came over https. Its value is 32 characters from `SecureRandom`.
- A session lasts a month without use, and is lost on a restart. One that is nobody's (made to carry a sign-in to
  Argonauth and back, which anyone can ask for) lasts ten minutes. The code used to say ten
  minutes, but its sweep looked at one session a minute, so in practice sessions already
  lasted until a restart. That is now what the code says.
- A sign-in in progress (its `state`, `nonce` and PKCE verifier) is kept in the session that
  started it, for five minutes, and can be answered once. The callback must come back with
  the same cookie. A completed sign-in gets a new session id, and the session under the old
  one is left with nothing. A browser has one sign-in in progress at a time: starting a
  second (another tab) drops the first.
- There are at most 50,000 sessions. Past that a PC sign-in from a browser with no session is
  turned away, and says so in the log.
- `Secure` on the cookie goes by `X-Forwarded-Proto`, which the new nginx layout sends
  (`snippets/proxy.conf`) and the old production `nginx.conf` did not.

## Later

- **Turning a password off.** With resets gone, nothing changes a Gemp password. One that has leaked or been
  shared stays good for ever: its holder logs in after the owner has the account back through a PC account, and
  can link the account to a PC account of their own first. Since a blank password now logs nobody in, "turn off
  password login for this account" would be safe as an admin action (blank it, end its sessions). Ketura's to
  decide: it is what was removed, in a form that cannot be used to take an account.
- **A PC account disabled at Argonauth** cannot sign in again, but a Gemp session it already has goes on for
  its month: the lookup still says its aliases are linked.
- **The new SQL has no test in the repository.** The tests stand a map in for the tables. It was run once by
  hand (2026-11-23) against an embedded MariaDB 10.11 (production is 10.5), with the tables as
  `database_script.sql` makes them and the DAOs as they are: `player_argonauth` (the upsert keeping `linked_at`
  for the same PC account and renewing it for another, `replaceLinks`, the foreign key, 320 concurrent
  `replaceLinks` without a deadlock), `registerAlias`, `removeUnusedAlias` (leaves alone an account with a
  password or one that has been entered), and that `'Frodo '` matches the row `Frodo` in the column but no longer
  logs in. A test that does this on every build needs a database in the build (MariaDB4j is an 80 MB test
  dependency): Ketura's call.
- **Bans that follow the PC account.** A ban is per alias today, and a new alias is one
  click. Ketura is settling with the moderators how it should behave; until then dodgers are
  dealt with by IP.
- **`pester`, `required`, `argonauth_only`**, and with them the `local_only` marker for the
  Librarian (the one account that is not a person's).
- **The promo pack** for connecting: once per PC account, grantable to everyone already
  linked when it arrives.
- **Stored passwords**: still one unsalted SHA-256. `05` proposes wrapping them in PBKDF2 in
  place.
- **`origin.allowed.pattern=^.*$`** in the default properties.
- **The legacy Admin tab** still runs in `hall-legacy.html` (see `gemp-lotr-frontend/HALL-PORT.md`).
- **`login-legacy.html`**, kept until it is removed, still offers Register. In `optional` the server refuses it
  (403) and the page says nothing. Nothing links to that page.
