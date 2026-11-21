# The hall and login port

The game hall (`hall.html` and its tabs) and the login page (`index.html`) are being ported to the Angular app, on
`feature/hall-angular`. This file holds the plan, where the port stands, and what it leaves for later.

## What was agreed (Ketura, 2026-11-20)

- **In place.** The Angular hall and login page replace the legacy ones, with no choice between them. The legacy
  pages stay in the repository (as `hall-legacy.html` and `login-legacy.html`) until they are removed: the Admin tab
  still lives in the legacy hall, and the checks compare against them.
- **The same as today.** Behaviour and look are those of the hall as it is, which is the `classic` theme. For now
  `modern` uses the same values for the hall; Ketura directs its own look once the port works.
- **Real addresses.** Each tab and sub-tab is a route (`/gemp-lotr/ng/help/formats/<code>`, ...), not a `#fragment`
  of `hall.html`. `hall.html` and `index.html` become pages that send an old link to its new address (a fragment
  never reaches the server, so the page itself has to do it); they keep their `og:` tags for link previews.
- **Data from the server, presentation from the page.** Where the server sent HTML that laid something out, the
  page asks for data instead. New calls are added beside the old ones, which the legacy pages still use.
  - Already data, used as it is: the hall's XML, leagues, tournaments, the calendar, event history, stats, game
    history, deliveries, scenarios, `/hall/formats/json`, `/hall/errata/json`.
  - To add: the server status as JSON (login page), and the tournament prizes as a label and lines
    (`DailyTournamentPrizes` and `WinForAwardTournamentPrizes` send a `<div class='prizeHint' value='<ul>...'>`).
  - The exception is rich text the server renders from what someone wrote: patch notes, chat, the MOTD, league and
    event descriptions. It stays rendered by the server, and the page draws it element by element from a list of
    allowed elements (`core/chat/chat-markup.ts`), never as HTML.
- **Admin is left out** (see Later).

## kermit's earlier port

`kermit/feature/frontend-themes` (2026-11-08 to 11-10, never merged) ported the login page, the hall's frame, chat
and long-poll, and the tables' logic, before themes became values only. Its `core/` code came over nearly as it was
(`core/login`, `core/hall`, the hall chat in `core/chat`, a few `core/platform` helpers), with its notes on what the
legacy pages do: `core/hall/README.md`, `core/hall/tables/README.md`, `core/login/README.md`. Its "parts 4.1 to 4.5"
are the hall's pieces as those notes number them. Its stylesheets were one per theme and page; here each component
has its own, drawn with the theme's values.

## Order of work

1. The core from kermit's branch, compiling and tested here. **Done.**
2. The shell: routes, the tab bar, the connection readout, the chat, the Deck-builder and Play buttons. **Done.**
3. The login page. **Done.**
4. The Game Hall tab: tables, queues, tournaments, ready checks, the MOTD, announcements. **Done.**
5. The Play and Join flows: casual, bots, league, tournament, scenario. **Done.**
6. Help and Server Info. **Done** (PC Errata last).
7. Events. **Done.**
8. My Account. **Done.**
9. The switch. **Done:** `hall.html` and `index.html` are now pages that send the browser on to the app's `go` and
   `start` routes with the link's query and fragment (`core/hall/legacy-links.guard.ts` decides where each leads); the
   legacy pages are `hall-legacy.html` and `login-legacy.html`; the share pages lead to the app's routes; the Admin
   tab is a link to `hall-legacy.html?admin=1#admin`.
10. `visual-check/hall` (`npm run check:hall` in `visual-check`): 36 screens of the hall and the login page against
    their legacy pages, and 58 old addresses against where the legacy hall led. **Done**; the addresses all agree.
    After a pixel pass the frame is identical and most screens are within a few thousand pixels of 1,049,088 (see
    "What looks different" for what is left); the check still fails on 34 of 36 until those are brought to nothing
    or Ketura accepts them (`HALL_ACCEPTED` in `visual-check/accepted.mjs`, empty). The deck builder's
    `check:stubbed` gives the same 14 differences on this branch as on the commit it started from.

## For Ketura to review

Where the Angular hall knowingly does something other than the legacy hall did. Each part's `README.md` under
`src/core/hall/` has the detail.

### Legacy bugs fixed

- **Tournaments, the "Round:" line**: the legacy code compared the stage with "Playing games" while the server sends
  "Playing Games", so the line never showed. It shows now.
- **Calendar**: an answer for a month the viewer had already left was drawn under the newer month's title; it is
  dropped. A failure other than 400/401 went to the hall's "Server error" dialog; it shows in the calendar.
- **Leagues**: a join that failed with anything but 409 said nothing; it shows the error.
- **Announcements**: Dismiss also snoozed the announcement; it only dismisses. Several Race to Mount Doom moves were
  drawn one over another, so only the last was seen; they show one after another.
- **Row buttons** (Leave, Start, Ready, Abandon): a failure over HTTP shows an "Error" dialog in the tables' wording,
  not the hall's "Server error" dialog.
- **Loading errors** on Help, Server Info, My Game History and My Stats: a status the legacy code didn't name left
  "Loading..." on screen (and popped "Server error"); the page says it is unavailable.
- **My Account's pocket** was drawn once, when the tab loaded; it follows the hall's latest answer.
- **PC Errata**: a link to an original with both a PC and a playtest errata always showed the first; it shows the one
  the link's filters list. `&nbsp;` and the like in game text read as characters.
- **Hall connection readout**: its popup could reopen after Escape; it stays closed.
- **Login page**: the two ban messages' Discord links had a broken quote and linked nowhere; they are links.
- **Login page, the picture**: it fills the window. The legacy stylesheet says `shtml { height: 100% }` (a typo for
  `html`), so the picture was sized to the page's content, 829 px tall whatever the window: a different crop from
  what was meant, and on a window taller than that (1920 x 1080) a black band below it. This is why the whole login
  screen differs in `visual-check/hall`.
- **Server text shown as HTML** (player names, deck and format names, standings): all drawn as text now.
- **Static pages**: the invalid markup in What Are Leagues? and About is put right; the wording is unchanged.

### Behaviour that changed on purpose

- **The start page** sends a logged-in player straight to the hall; the legacy one always showed the login form.
- **After logging in** the player comes back to the page that sent them (the deck builder or a game too), not always
  the hall.
- **Unknown addresses under `/gemp-lotr/ng/`** open the hall; they used to open the deck builder.
- **Share** copies with the Clipboard API only. On a page served over plain http the link is shown to copy by hand;
  over https it copies as before.
- **Delivery headers** are read on every server answer, and the leagues are fetched when Play > League opens, not on
  every Play.
- **Game page chat** (it shares the hall's chat code now): an unmapped status reads "Unknown chat problem occurred
  (error=N)" as on the legacy page, its polls time out after the legacy 20 s, and an unreadable answer stops the chat
  quietly.
- **Help > PC Errata** opened bare comes back as it was left; the legacy bare link cleared the filters.
- **The Admin tab** opens the legacy hall's Admin in a tab of its own, and "< Back to the Game Hall" there closes
  that tab. It can't share the hall's tab as it did: the Angular hall has to stay open and polling, or the server
  takes the admin out of their tables and queues after 20 s.
- **Links into the hall in server text** (a patch note's, an announcement's, the MOTD's) are real addresses, so they
  open in a new tab too; the legacy `#fragment` ones did as well.
- **The game page's "Go to main page"** leads to the start page, so a logged-in player lands in the hall.
- **Leave Queue, Start, Ready and Abandon** that fail with a 400 show the server's own words; the legacy hall showed
  "Server error ... (error 400)". A lost request and a 401 are handled as before.
- **A blocked game tab** always reads "... your browser blocked the new window."; the legacy scenario shelf alone
  said "new tab".

### Wording that is new

- "Could not load your stats. Please try again later." (My Stats; the legacy page showed nothing.)
- "Could not load your game history. Please try again later." (My Game History, for a failure the legacy page left
  on "Loading...".)
- The alt texts "gold" and "silver" on the pocket's coins.
- "< Back to the Game Hall" on the legacy hall when it is open for its Admin tab alone, and "The Game Hall is open
  in another tab." beside it when the browser won't let the page close itself.

### What looks different

- Sections, drawers and the Play overlay's screens open and close at once; the legacy hall animated them (200 to
  250 ms). Updated rows don't flash their border.
- The card dialog is the app's own. It is titled as the legacy preview was in Format Definitions and PC Errata (the
  card's name); a card named in a patch note is still "Card Information".
- Four old patch notes (2021-12-24, 2022-07-07, 2022-08-19, 2022-12-21) lose their inline colours.
- In "New items" and Race to Mount Doom dialogs the cards are plain pictures, with no zoom or details.
- What `visual-check/hall` still shows at 1366 x 768, after the pixel pass (the frame itself is identical, and so
  are the tabs at 1100, 950 and 700 px wide):
  - the login and register pages, whole: the picture (see "Legacy bugs fixed");
  - a tournament's own link (about 117,000 px): where the page ends up scrolled. The legacy smooth scroll is
    clamped to the page's height at the moment it starts, while the drawer is still loading, so where it stops
    depends on timing; ours always lands on the row;
  - Play > League (42,000): the "Join Leagues" list starts about 10 px higher than the legacy one;
  - Show Prizes (30,000) and the player picker's menu (26,000): each sits 2 px from the legacy one;
  - Play > Bot (17,000), the "Not logged in" dialog's button bar and the queues' rows (12,000 each), a league opened
    in Events (10,000), My Game History's rows (8,000): a pixel here and there, not yet measured;
  - everything else is under 7,000 px, mostly sub-pixel text positions.

### Suggestions not taken, for a decision

- Contribute and About links open in the same tab, as today; a new tab would match the hall's other outside links.
- The register form has no way back to the login form, as today (the button exists and is hidden).

## Later

Left out of the port on purpose. Each stays as it is until it is taken up.

### To be looked at together

- **The Admin tab** (general, league, tournament, prizes, scenarios, users): stays on the legacy hall, which the
  Angular hall's Admin tab links to. With it stay `addItemsForm.js`, `prizeTierEditor.js`, `manage.js` and the admin
  calls that answer with HTML. The server allows a player one hall at a time (a second one gives the first a
  "Concurrent access" error), so the link is `hall-legacy.html?admin=1#admin`: with `?admin=1` the legacy hall asks
  who the player is, draws only its Admin tab and a way back, and starts neither its hall poll nor its chat
  (`GempHallSession.adminOnly`, `startForAdmin`). Without it the legacy hall is as it always was.
- **Rich text the server renders** (above). To be decided again when the Admin tab is ported: whether the server
  should send the source and the page render it.
- **The MOTD as markdown**: today an admin types HTML (links, `i`, `b`, `br`). The form is on the Admin tab.

### Legacy pages that stay

- `soloDraft.html`, `tableDraft.html` and `merchant.html`. kermit's branch has Angular ports of all three (logic with
  specs, classic and modern stylesheets in the old layout), so none starts from nothing. The merchant's whole concept
  needs an overhaul first.
- `rtmd-feedback.html`: no longer needed.
- `deckBuild2.html`, `layoutest.html` and the `admin.html`, `ban.html`, `leagueAdmin.html` stubs: left over from
  when changes were tried out on the live server.

### Pages the server draws

Opened in a tab of their own from the hall, and not part of the Angular app:

- the deck readouts (`DeckRenderer`), a finished tournament's decks among them;
- the tournament report;
- the draft format page (`/deck/draftHtml`).

The share pages (`SharePages`) stay drawn by the server for good: the sites that preview a link don't run scripts.

### After the port

- **The modern theme** for the hall.
- **Logging in with a PC account**: a button next to the login form, which stays for the months both logins are
  accepted.
- **The Users tab**: never finished, and hidden today.
- **The old calls**: `/hall/formats/html` and `/hall/format/{code}` go when no legacy page asks for them, and
  `communication.js` with the last legacy page.
- **Removing the legacy pages** the port replaces, once the Angular ones are signed off.
