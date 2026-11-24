# The PC account

Checks how the login page, My Account and the hall behave with a PC account (`gemp-lotr/PC-ACCOUNT.md`), in a real
browser and without a server. Not a comparison (there is no legacy page to compare with, and Ketura asked for proper
behaviour, not looks): each scenario does what a player does and checks what the page shows and what was sent.

```
npm run check:account
npm run check:account -- --only create-taken,link-good --out report-account
CHECK_BROWSER_EXECUTABLE=/usr/bin/google-chrome npm run check:account
```

It checks the Angular app as built in the server's web root (or `CHECK_NG_BUILD`), as `hall/` does: build it first.
`report-account/results.json` lists each scenario's problems (for `check:all --failed`); the command exits with 1 when
one fails.

| File        | What it is                                                                                                                                                                                                |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `run.mjs`   | The scenarios and their checks                                                                                                                                                                            |
| `stubs.mjs` | `FakePcServer`: the server's `/argonauth` addresses as the document gives them, starting from a saved session of `src/testing/server-responses`, and every POST it was sent; the rest is `hall/stubs.mjs` |

The pages the browser is sent to (a PC sign-in, the account service's sign-out) are not followed: a scenario checks
that the browser got there, and opens where the server would send it back to (`?open=create`) itself.

What it covers: `test` mode shows no PC account on the login page or My Account, and the hall's Log Out works there; in `optional` mode the Legacy Login and PC SSO
columns (at three widths), Sign in
with PC Account and Register PC Account, a claimed account's password login (423) (with the page's `return`) and no registration form, and the lines for a cancelled or failed sign-in;
the alias list with several aliases, Enter (and an alias unlinked meanwhile, its refusal kept), a PC account with no alias landing in Create
(`?open=create&return=`) with Link beside it, the gate (`blockedBy`: this browser's alias busy) and an alias busy elsewhere gating nothing, paging at eleven aliases; Create (made, a name
outside the rule, a name taken, `401 reauth`, a lapsed sign-in); Link (linked, wrong credentials, linked elsewhere);
the alias list's Log Out and the hall's; a password login's own page and its Log Out; My Account's three states, its four `?pc=` lines and Connect
PC Account (its password, accepted and refused); and a deck builder open in a second tab reloading when the first enters an alias.
