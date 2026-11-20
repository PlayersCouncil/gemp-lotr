# core/chat

One chat core for every page that shows a chat room: the hall (`ChatService`, the "Game Hall" room) and the game page
(`ChatRoom`, `Game<id>`). The legacy pages had one `ChatBoxUI` (chat.js) for both, configured per page; so has this.
What the hall's chat does is described in `../hall/README.md`, section "The chat".

## Shared

| File                  | What                                                                                                                                                                                                                                                          |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `chat-models.ts`      | The models: `ChatMessage` (`from`, `html`, `date` or null, `system`), `ChatUser` (`role` admin / leagueAdmin or null, `self`, `incognito`), `ChatSnapshot` (one answer). The hall's own `ChatLine` and `TournamentAnnouncement` are views of these.           |
| `chat-parsers.ts`     | `parseChat`: the one reader of the `<chat>` answer (users written the old way, "\* name" / "+ name", get their role from the mark); the ignore list and incognito answers.                                                                                    |
| `chat-api.service.ts` | `ChatApi`: the calls (join GET, poll POST with the 20 s time-out of communication.js updateChat, send POST, ignores, incognito).                                                                                                                              |
| `chat-connection.ts`  | `ChatConnection`: join, then poll a set time after each answer until closed or a join or poll fails; messages sent one at a time in the order typed. Each service creates its own (`new ChatConnection()` in its constructor), which stops with that service. |
| `chat-messages.ts`    | The wording, and `chatWarning(status)`: chat.js chatErrorMap (0, 401, 403, 404, 410 stop the chat) and "Unknown chat problem occurred (error=N)" for any other status.                                                                                        |
| `chat-markup.ts`      | `parseServerMarkup`: a message's (or the MOTD's) server markup as nodes the view draws element by element (`ui/shared/chat/markup.component`).                                                                                                                |
| `chat-format.ts`      | The hall's timestamps, highlights, tournament senders, ignore matching, the user list's order.                                                                                                                                                                |

## What still differs, and why

Both services run on `ChatConnection`, `ChatApi` and `parseChat`; what they do with the answers is their page's:

|                                       | Hall (`ChatService`)                                                                                                                     | Game page (`ChatRoom`)                                                                                                                                  |
| ------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Poll delay after an answer            | 100 ms (chat.js chatUpdateInterval in the hall)                                                                                          | 3 s (the legacy game page's chatUpdateInterval)                                                                                                         |
| Lines                                 | `ChatLine`s: timestamps, the sender as shown (both tournament senders as "TournamentSystem"), highlights, warnings as lines, at most 500 | the `ChatMessage`s as they came; `GameLog` merges them with the game's messages and keeps them per game in the tab's session storage                    |
| User list                             | ordered (own entry first), roles, ignore list, incognito chip, status line                                                               | the names, on the Players tab                                                                                                                           |
| Discord                               | the switch and the embed                                                                                                                 | none                                                                                                                                                    |
| A failed send with an unmapped status | a warning; the chat goes on (as chat.js)                                                                                                 | the chat stops, as the game page's chat always did                                                                                                      |
| An answer that is not a `<chat>`      | a warning, the chat stops                                                                                                                | the chat stops quietly (as before the merge)                                                                                                            |
| Highlights                            | `highlightOf` (chat-format.ts)                                                                                                           | `mentionOf` (`ui/game/tabs/game-chat.component.ts`): tested on the whole line, as the legacy game page, and kept so the game page's look doesn't change |
| Drawing                               | `gd-chat` with `gd-markup`, never as HTML                                                                                                | `gd-game-chat`, which binds the server's HTML (Angular sanitizes it) so its card hints work; left for the game page's own redesign                      |

Changes the merge brought to the game page: its unmapped-status warning reads as the legacy chat's ("Unknown chat problem
occurred (error=N)", was "There was a problem communicating with the chat server (N)"), the 401 warning lost its
trailing space, and its polls get the legacy 20 s time-out (a poll with no answer by then is a lost connection).
