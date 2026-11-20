# core/hall and core/chat: what the legacy hall frame and its chat do

Ported from `gemp-lotr-async/src/main/web/hall.html`, `src/Hall/GameHall.js` (tabs, hints), `js/gemp-022/hallUi.js`
(the frame, the long-poll, the connection readout), `js/gemp-022/hallLinks.js` (tabs, deep links, GempHallSession),
`js/gemp-022/chat.js` (ChatBoxUI) and the calls in `communication.js`. Server side: `HallRequestHandler`,
`HallServer`, `HallCommunicationChannel`, `ChatRequestHandler`, `ChatRoomMediator`, `ChatRoom`, `MarkdownParser`,
`PlayerInfoRequestHandler`, `LongPollingSystem`. The mobile branch's `src/HallMobile/hallState.js` was read for the
table model (part 4.2). Wording is in `hall-messages.ts` and `chat/chat-messages.ts`.

The hall is split into five parts; 4.1a is the core of the frame and the chat, **4.1b** their UI (`ui/hall`,
`ui/shared/chat`).

| Part | What                                                                                       |
| ---- | ------------------------------------------------------------------------------------------ |
| 4.1  | tabs frame, long-poll, connection readout, MOTD, server clock, main bar buttons, chat      |
| 4.2  | Game Hall tab: tables, queues, tournaments, ready checks, "your game is ready", Play popup |
| 4.3  | Help and Server Info tabs                                                                  |
| 4.4  | Events and My Account tabs (pocket, ignore list page, history, stats), deliveries          |
| 4.5  | Admin tab                                                                                  |

## The frame (`hall.html`)

- Top-level tabs (jQuery UI tabs; `hall-tabs.ts` `HALL_TABS`, with each tab's part): Game Hall (`#gameHall`, local),
  Help, Events, Server Info, Users (hidden: "until that feature is ready"), My Account, Admin (shown only when the
  player's type has `a` or `l`: `HallSession.adminTabShown`). The remote tabs were loaded from `includes/*.html`
  once, on first visit.
- Which tab opens: a link's `#fragment` (deep links, `GempLinks`: `#game-hall`, `#help/formats`, `#format-<code>`,
  `#league-<code>`, ... see hallLinks.js), else the tab last opened (localStorage `gemp.hall.activeTab`, the tab's
  href: `HALL_TAB_STORAGE_KEY`), else the Game Hall. Switching tabs writes the tab's link into the address bar
  (replaceState). Deep-link routes of the sub-pages belong to their parts.
- Tabs that show live data refresh when the viewer comes back to them after 10 s or more (GempHallTabs.onReturn:
  tab switch, page hidden/shown, window blur/focus). Each part does this for its own data.
- Game Hall tab, top (4.1): link buttons Report a Bug (forms.gle), Report a Player (forum), Join the PC Discord, Browse
  the Wiki, Merchant (`merchant.html`, struck through), each `target=_blank`; the MOTD (`<b>MOTD:</b> ` + server markup).
  Below them the collapsible sections of 4.2 (Waiting / Playing / Finished Tables, Recurring Events, and when there
  are any, World Championship Queues and Scheduled Events; open/closed in the `hallSettings` cookie).
- Main bar (4.1): the connection readout at the left, Deck-builder (`ng/deckbuilder/`, new tab) and Play (4.2's popup;
  a visitor gets the "Not logged in" dialog instead), the server clock at the right (`serverTime` with the space as a
  line break).
- Chat (below the bar, resizable by a drag handle along its top edge, height in the `chatResize` cookie, 100 px to the
  window less 160 px, default 300; Up/Down keys 20 px): the built-in chat or the Discord embed, and the user list
  column with the "Switch to Discord / Legacy" button.
- Disclaimer line (Decipher copyright) at the bottom.
- Hints anywhere in the hall (`.cardHint`, `.prizeHint`, `.draftFormatInfo`): card dialog, prize breakdown, draft format
  page (`/gemp-lotr-server/deck/draftHtml?draftCode=`). Used by 4.2-4.4; the card dialog is the shared CardInfoService.
- A sound (`/gemp-lotr/src/assets/fanfare_x.mp3`) plays when a game starts (4.2, `HallComms.startedGames$`).

## The hall long-poll (`HallApi`, `HallComms`)

- GET `/gemp-lotr-server/hall` (getHall, `participantId`, not cached, 20 s time-out) signs up for a new channel and
  answers with a full snapshot. POST `/hall/update` with `channelNumber` (updateHall, same) long-polls that channel:
  the server answers when the hall changes, or after at most 5 s (`LongPollingSystem._pollingLength`) with no
  changes. Every answer is `<hall>`:
  - attributes `channelNumber`, `currency` (the player's silver, shown on My Account: 4.4), `serverTime`
    ("yyyy-MM-dd HH:mm:ss", GMT), `serverTimeMs`, `motd` (only when it changed; server markup), `shutdown="true"`
    (while the server waits to restart; absent otherwise);
  - children `<queue>`, `<tournament>`, `<table>` with `action` add | update | remove and `id` plus their own
    attributes (all kept as sent in `HallItemChange.attributes`; typed by 4.2), and `<newGame id>` for games that
    started for the player.
- A snapshot (`HallUpdate.full`) replaces everything drawn. A re-signup re-announces running games; `startedGames$`
  gives each game once per page (legacy `offeredGames`). 4.2 opens `/gemp-lotr/game.html?gameId=` (+ participantId).
- Timing: the next poll 100 ms after an answer, in a `finally` so a failing subscriber can't stop the loop.
- Failures (`pollFailed`):
  - 401: stops; readout grey "Not logged in", "You are not logged in (or your session has ended)." with a login link.
  - 409 (the hall was opened in another window): stops; red, with "Reload the page".
  - 410 (the server dropped the channel after 5 minutes without a poll, e.g. a sleeping laptop): signs up again (full
    snapshot); once connected the readout says it reconnected and that tables or queues may have been closed.
  - no answer (status 0), time-out, 5xx: retry after 1, 2, 4, 8, ... s, at most 30 s; "Reconnecting" (amber) with
    "Lost contact with the server (<reason>). Retrying in N s.", and from the 5th failure in a row "Disconnected"
    (red) "Still retrying every N s; if it does not come back, reload the page." Retrying goes on.
  - other 4xx: stops, red, "The Game Hall stopped updating: the server answered with error N."
- The server also drops the player's waiting tables and queues after 20 s without a poll (HallServer.cleanup).
- Readout (`HallComms.connection`, `hall-messages.ts`): states connecting, connected, shutdown (yellow; replaced by a
  connection problem, back with the next good answer), reconnecting, disconnected, loggedout; a word, a popup with the
  state's text, "Last update: <server time> (server time)" / "none yet", the detail message and its action (Reload /
  log in); a live region announces each change of state (`HallComms.spoken`). A message given while connected stays
  until the connection changes. The popup opens on hover, focus or click (view: 4.1b).
- Server clock offset (`serverClockOffset`): `serverTimeMs` minus our clock, for 4.2's waiting-table timers.
- Responses may carry `Delivery-Service-Package`, `-Announcement`, `-League` headers (deliveryCheck): the legacy
  page then fetched `/delivery/misc` and friends. Not handled here (4.4).

## Logged in or not (`HallSession`, GempHallSession)

- On a tab that needs logging in (Game Hall, Events, Users, My Account, Admin) the chat and the poll start at once.
- On a public tab (Help, Server Info) GET `/player` is asked first: a player starts the hall; 401 is a visitor: nothing
  starts, the readout says "Not logged in" (login link) and the chat shows its not-logged-in warning with "Click here
  to register or log in." and "Log in to chat" in the box; any other failure starts the hall anyway.
- Opening a tab that needs logging in starts the hall, or, once a request found the viewer not logged in, shows the
  "Not logged in" dialog (one at a time: `loginRequired`), with "Register or log in" and "Close".
- Login links go to `/gemp-lotr/` with the current hall link to come back to (GempLinks.loginUrl; 4.1b).
- Side requests' errors (requestFailed): 401 → that dialog; 0 / 504 → nothing (the readout reports connectivity);
  others → "Server error" dialog. Each part handles its own calls through the Notifier.

## The chat (`ChatApi`, `ChatService`)

- One room in the hall: "Game Hall" (`HALL_CHAT_ROOM`). The same ChatBoxUI served the game and draft rooms
  (`Game-<id>`, `Draft-<id>`); those are other pages and not ported here.
- GET `/chat/{room}` (startChat) joins: the answer holds the room's recent history (at most 500 messages, from
  ChatRoom) plus the welcome message, and the user list. POST `/chat/{room}` without a message (updateChat, 20 s
  time-out) long-polls: new messages (≤ 5 s on the server) and the whole user list; the next poll 100 ms after an
  answer. POST with `message` (sendChatMessage) sends; messages go one at a time in the order typed (each waits for
  the previous answer). Enter sends, Shift+Enter is a new line (view).
- `<chat roomName>` with `<message from date>` (text: server markup) and `<user>` (bare name; `role` admin |
  leagueAdmin; on the viewer's own entry, which comes first, `self="true"` and `incognito`). Older "* name" /
  "+ name" prefixes are understood.
- Message markup (as data, for the view): players' text is rendered by the server (`MarkdownParser.renderMarkdown`
  with links shredded): HTML escaped, markdown (bold, italics, strikethrough, quotes, code, lists), bare URLs
  autolinked with `target="_blank"`, `[text](url)` written out as "text: url", underscores literal, newlines as
  `<br/>`. Commands (starting with "/") are not rendered. System messages hold simple tags (`<br />`). The legacy chat
  inserted the markup as it came; the view should bind it as HTML (Angular sanitizes it).
- Lines (`ChatLine`): kind `system` (from "System": joins, leaves, welcome, command answers), `chat`, or `warning`
  (made by the page); sender shown before the text in bold, preceded in the hall by a timestamp
  `[Nov 9 08:05:03]` in the viewer's time zone (`formatChatTimestamp`). Senders "TournamentSystem" and
  "TournamentSystemTo:a;b" show as "TournamentSystem"; their messages also go out on `tournamentAnnouncements$`
  (4.2 shows the "Tournament Update" dialog: to everyone in a queue, or to the players named).
- Highlights: "@<name>" → ping; the name as a word, "@everyone" or "@anyone" → mention (case-sensitive).
- At most 500 lines; older ones go. Scrolling: stuck to the bottom when already there (or for a warning) — view.
- The hall shows system messages (its toggle button is hidden; game rooms hide them by default).
- Commands (server, hall room): `/help`, `/ignore name`, `/unignore name`, `/listIgnores`, `/incognito`,
  `/endIncognito`; admins `/ban name`, `/banIp ip`, `/banIpRange prefix`. Unknown commands get a system answer; a
  failed command answers 400.
- Errors (chatErrorMap): 0 "Chat server has been closed or there was a problem with your internet connection.",
  401 "You are not logged in." + login link, 403 "You have no permission to participate in this chat.",
  404 "Chat room is closed.", 410 (dropped after 5 minutes without a poll) "You have been inactive for too long and
  were removed from the chat room. Refresh the page if you wish to re-enter."; each warning shows once, and the chat
  stops with the box disabled (drawn red). Any other status: "Unknown chat problem occurred (error=N)".
- User list (hall only): own entry first with an online / incognito chip that switches it (POST
  `/player/incognito`, `incognito=true|false` → `{inRoom, incognito}`; 409 when not in the hall chat), roles as
  badges ("admin", "league admin"), ignored players struck through. Clicking another player opens a menu: "Invite to
  table" (4.2's Play popup) and "Ignore" / "Unignore" (POST `/player/ignores/add` | `/remove` with `name` →
  `{outcome, changed, error, name, message, ignored}`). The ignore list (GET `/player/ignores` → `{ignored}`) is asked
  once, when the list first shows; failures are silent. A status line under the list says what happened, for 6 s.
  The column keeps one width, measured on the own entry (layout math, not ported). The ignore list itself is edited on
  My Account (4.4). The server filters ignored and banned players' messages itself.
- Discord: "Switch to Discord / Legacy" swaps the built-in chat for a WidgetBot embed (`DISCORD_WIDGET`, the player's
  name as username), created on first show. The choice is kept in localStorage `gemp.hallChat` ("discord" /
  "builtin"; Discord until chosen).
- There are no private messages besides the server's own per-player system answers.

## Differences from the legacy hall

- A hall answer that can't be read (not XML, not `<hall>`) is retried like a lost one; the legacy page stopped (bad
  XML) or treated it as an empty answer (another root).
- Blank (whitespace-only) chat messages are not sent: the server would take them for a poll and swallow its
  messages.
- After any failed chat join or poll the chat is `stopped`; the legacy chat stopped updating on an unmapped status too,
  but left the box enabled.
- Room names are encoded in the path ("Game Hall" → `Game%20Hall`, which the browser did anyway).
- Mentions match the player's name literally (the legacy regex took it as a pattern), and only in the message, not in
  the sender in front of it (the legacy chat used a lookbehind on the whole line).
- The "Unknown chat problem" warning reads "occurred" (legacy: "occured").
- `HallComms.stop()` drops the request in flight and the pending poll (the legacy page never stopped).

## Part 4.2a

The tables core (sections, table data, Play popup's Casual and Bot flows, Join / Leave, started games) is in
`tables/` with its own inventory: `tables/README.md`. `HallTablesService` is in `HALL_SERVICES`.

## Known gaps

- The UI (4.1b) is `ui/hall/` (the page, the connection readout, the tabs' placeholders) and the shared
  `ui/shared/chat/` (`gd-chat`, drawn from `ChatLine`s and `ChatUser`s; message markup is read into nodes by
  `chat-markup.ts` `parseServerMarkup` and drawn element by element, never as HTML). Deep links open their tab only (the
  sub-pages' routes belong to their parts); the return-refresh is left to the parts with live data. The chat's height
  is kept in localStorage (`gemp.hall.chatHeight`), not the legacy `chatResize` cookie. The user list's width and the
  user menu's place are the theme's (CSS), not layout math.
- The route's injector outlives the page, so the page stops the poll and the chat when it goes (`HallSession.stop()`)
  and starts them afresh when shown again (`ChatService.start()` after `stop()` joins anew, its old lines cleared).
- Play does nothing for a logged-in player yet (its popup is 4.2's); "Invite to table" in a player's menu waits for
  4.2 too.
- Delivery headers on hall answers are ignored (4.4). The chat does not rejoin after 410 by itself, as the legacy
  chat (refresh the page).
