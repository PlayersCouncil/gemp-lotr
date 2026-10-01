# Patch notes

Everything in this folder is shown on **Server Info › Patch Notes** in the hall, together with past server announcements (see [Announcements](#announcements)). The page loads the newest few updates. At the top, a filter shows only the updates with one tag (see [Tags](#tags)), and the month control from the Events tab jumps to a month: its arrows step to the previous or next month with updates, and picking a month in its dropdown shows that month's newest update, with the older ones below it. At the bottom, "Newer updates" and "Older updates" page through the rest. Each update can also be linked on its own as `hall.html#patch-notes/<name>`, and anyone can open that link without logging in. All dates show as `YYYY-MM-DD`.

## Adding an update

1. Create `YYYY-MM-DD.md` in this folder. For a second update on the same day, or for a name that reads better in links, use `YYYY-MM-DD-some-name.md`. Names use lowercase letters, digits and dashes only, for example `2026-09-26-patch-notes-overhaul.md`. The file name without `.md` is the update's permalink name.
2. Start the file with front matter, then write the notes in Markdown:

   ```markdown
   ---
   date: 2026-09-26
   title: Patch notes get an overhaul
   summary: Updates now live in the repo, with screenshots, and link like any other hall page.
   tags: User Interface, Card Fixes
   ---

   ### New
   - Server Info › Patch Notes replaces the old change log. [Format definitions](#format-pc_movie) link here too.

   ### Fixes
   - Fixed **Asfaloth** (1U31) not paying twilight when transferred.

   ![The new Patch Notes page](img/2026-09-26-patch-notes.png)
   ```

3. Commit the file together with any images. The update goes live with the next deploy. The server re-reads this folder within a few seconds of a change, so no restart is needed. The admin panel's **Clear Server Cache** forces an immediate re-read.

### Front matter

| key       | required | meaning |
|-----------|----------|---------|
| `date`    | no       | `YYYY-MM-DD`. If left out, the date comes from the file name. |
| `title`   | no       | The update's heading. Without one, the date is the heading. |
| `summary` | no       | A one-paragraph blurb shown under the title in larger text. It is plain text, not Markdown. `blurb` is accepted as another name for it. |
| `order`   | no       | A whole number that orders several updates on the same day. Higher numbers are newer, and the default is 0. |
| `tags`    | no       | The update's tags, for the page's filter. See [Tags](#tags). |

Each value goes on one line. It can be written bare or in quotes, for example `title: "Gemp's 15th birthday"`. Lines starting with `#` are comments.

Updates are listed newest first: by date, then by `order`, then by file name.

### Tags

Tags sort updates for the filter at the top of the page (**All | Card Fixes | PC Updates | User Interface | Announcement**) and show as small labels on the update. The filter is not remembered: a reload shows All again.

| tag              | use it for |
|------------------|------------|
| `Card Fixes`     | fixes to how cards, or the rules they use, work |
| `PC Updates`     | Player's Council changes: errata, formats and their lists, new PC cards, promos and RTMD modifiers |
| `User Interface` | changes to the hall, the game window, the deck builder and other screens |
| `Announcement`   | news rather than changes. Past server announcements get this tag by themselves. |

Write them in any of these ways. Case, spaces and dashes don't matter, so `card-fixes` is `Card Fixes`.

```yaml
tags: Card Fixes, User Interface
tags: [Card Fixes, "User Interface"]
tags:
  - Card Fixes
  - User Interface
```

An update can have several tags, or none (it then shows under All only). A tag outside this list still shows on the update, and the server log warns about it, but it has no filter button. To add a filter, add the tag to `PatchNote.TAGS` (Java) and `PatchNotes.TAGS` (patchNotesUi.js), and to the table above.

The historical entries (2011 to 2023) are tagged `Card Fixes` only where every item of the entry names a card it fixes. The rest are untagged.

### Folders

Updates can live in subfolders of this folder, at any depth (up to 8 levels), for example `2011/2011-09-21.md` or `old/2012/2012-03-05.md`, to keep the old ones out of the way. The folder makes no difference to the page:

- An update's name, and so its link, is still its file name without `.md`. Moving a file to another folder keeps its links working.
- Two files with the same name in different folders are one too many: the server log warns, and the one higher up (then the first by path) is shown.
- `README.md` files and `img/` folders, at any depth, are not updates. Folders starting with `.` are skipped.
- Adding, removing, moving or editing an update in any subfolder shows up within a few seconds, as it does in this folder.
- An image or link path in an update is looked up next to the update first, then from this folder. So an update moved into `2011/` still finds `img/...` here, and one that keeps its pictures in `2011/img/` can use `img/...` too.

## Images and screenshots

- Put images in `img/` next to this file, named after the update, for example `img/2026-09-26-deckbuilder.png`.
- In the notes, write the path relative to this folder: `![Deck builder filters](img/2026-09-26-deckbuilder.png)`. The server rewrites it so it resolves from the hall. Absolute `https://` image URLs also work. (For an update in a subfolder, see [Folders](#folders).)
- Screenshots show at a readable thumbnail size, and several images in one paragraph line up in a row. Clicking one opens it full size. Clicking outside it or pressing Esc closes it again.
- To give an image its own size, use HTML: `<img src="img/2026-09-26-banner.jpg" width="500" alt="...">` (a `width` or `height` you set is kept, up to the page width).
- Keep screenshots reasonably small (a PNG or JPG under about 500 KB). They ship with every deploy.

## Announcements

Server announcements (the pop-ups admins create in the admin panel) are shown here too once their start time has passed, even after they have ended. Each one is dated by its start, tagged `Announcement`, headed by its title, and rendered like an update, except that a single line break stays a line break, as in the pop-up. Its link is `hall.html#patch-notes/announcement-<id>`. The server re-reads them every two minutes, and **Clear Server Cache** re-reads them at once.

The announcements the server makes by itself for a new update are left out, because the update is already here. They are recognised by the `<!-- gemp-patchnote:<name> -->` marker in their content.

### Automatic announcements

When the server finds a new update, it announces it in the hall pop-up for 14 days from the update's `date` (00:00 UTC). The pop-up's title bar is the update's `title` (or "Patch notes YYYY-MM-DD"), and its text starts with the same title as a large heading, then its `summary` (or, if it has none, its first paragraph: everything up to the first blank line), its **first image**, and a bold **Read the full patch notes here** link to `#patch-notes/<name>`. Culture icons in the summary or paragraph show in the pop-up too. So write the `summary` for players who will only read the pop-up, and put the picture you want them to see first. An update whose 14 days have already passed when the server first sees it is never announced.

Editing an update after its announcement exists does not change the announcement. To stop one early, end it rather than deleting it (a deleted one is recreated at the next server start while its 14 days last), e.g. `UPDATE announcements SET until = NOW() WHERE content LIKE '<!-- gemp-patchnote:<name> -->%';`

## Sharing an update

Each update has a **Share** button at the right of its heading. It copies the update's share link, `https://<site>/gemp-lotr/share/patch-notes/<name>`. Pasted into Discord, a forum or a chat app, that link shows a preview card. The preview's title is the update's `title` (or its date, as `YYYY-MM-DD`). Its text is the `summary`, or else the first lines of the notes. Its picture is the update's **first image**, so put the screenshot you want in the preview first. Anyone who opens the link goes straight on to `hall.html#patch-notes/<name>`.

## Supported Markdown

This is [CommonMark](https://commonmark.org/help/), rendered on the server with the same library as the chat and event descriptions, plus these extras:

- headings (`###` suits sections inside an update), paragraphs, bullet and numbered lists, and `> quotes`
- `**bold**`, `*italic*`, `~~strikethrough~~` and `` `code` ``, plus fenced code blocks
- links such as `[text](https://...)`. Bare `https://...` URLs become links automatically. Links to other sites open in a new tab.
- links to other hall pages, which stay in the hall:
  - `[PC-Movie](#format-pc_movie)`
  - `[the errata](#pc-errata)`
  - `[Cleaving Blow](#errata-1_4)`
  - `[an earlier update](#patch-notes/2023-12-09)`
- card links, which open the card in the zoomable card display (see [Card links](#card-links) below): `[[Cleaving Blow]]`
- `---` for a horizontal rule
- culture icons: `:isengard:` or `[isengard]` (see [Culture icons](#culture-icons) below)
- twilight icons: `(1)`, `(X)` or `:1twilight:` (see [Twilight icons](#twilight-icons) below)
- inline HTML for what Markdown can't express, for example `<span style="color:red">new text</span>` for errata colours, `<br>`, `<u>`, `<sub>`/`<sup>`, `<table>` and `<details><summary>`
- `<details><summary>Show the list</summary> ... </details>` folds a long part away. Its summary shows as a button with a ▸ that turns when it opens. Leave a blank line after `<summary>...</summary>` so the Markdown inside is still Markdown.

## Culture icons

Write a culture's name between colons or single square brackets, and it shows as the culture's icon, at the size of the text, with the name as its tooltip: `:isengard:` or `[isengard]`. They are the icons of the Culture column in **Help › PC Errata**. Case doesn't matter.

| icon | names you can use |
|---|---|
| Dwarven | `dwarven`, `dwarf`, `dwarves`, `dwarvish` |
| Elven | `elven`, `elf`, `elves`, `elvish` |
| Gandalf | `gandalf` |
| Gollum | `gollum` |
| Gondor | `gondor` |
| Rohan | `rohan` |
| Shire | `shire` |
| Dunland | `dunland`, `dunlending`, `dunlendings` |
| Isengard | `isengard` |
| Men | `men` |
| Moria | `moria` |
| Orc | `orc`, `orcs` |
| Raider | `raider`, `raiders` |
| Sauron | `sauron` |
| Uruk-hai | `uruk-hai`, `uruk_hai`, `urukhai`, `uruk` |
| Ringwraith | `ringwraith`, `ringwraiths`, `wraith`, `wraiths` |
| Esgaroth, Gundabad, Mirkwood, Smaug (Hobbit draft) | `esgaroth`, `gundabad`, `mirkwood`, `smaug` |
| Spider, Troll (Hobbit draft) | `spider`, `spiders`, `troll`, `trolls` |

For example, `adds a [dunland] token` or `each :uruk-hai: minion`. They work in the notes, in a `summary`, in past announcements shown here, and in the automatic pop-up.

A name that isn't in the table stays as written. So does a token in `` `code` ``, inside a `[[card link]]`, in the text of a link (`[isengard](...)`, `[isengard][ref]`), in a URL, or right after a letter or digit (`word:men:`). To show the brackets or colons themselves, put them in a code span.

## Twilight icons

A twilight cost written the way the cards write it, a single digit or `X` in round brackets, shows as the twilight cost icon, at the size of the text, with "(1) twilight" (and so on) as its tooltip: `remove (1)`, `add (X)`. You can also write `:0twilight:` to `:9twilight:` and `:xtwilight:` (case doesn't matter there).

- `(X)` must be an upper-case X: `(x)` stays as written.
- One digit only: `(10)`, `(25)` or `(2023)` stay as written.
- Not right after a letter, digit or `]`, nor right before a letter or digit: `f(1)` and `(1)st` stay as written.
- The same places as culture icons: not in `` `code` ``, a `[[card link]]`, the text of a link or a URL. To show `(1)` itself, put it in a code span.

They work in the notes, in a `summary`, in past announcements shown here, and in the automatic pop-up.

## Card links

Put a card between double square brackets and it becomes a card link. Clicking it (or right-clicking it) opens the card in the zoomable card display.

| you write | shows | opens |
|---|---|---|
| `[[Cleaving Blow]]` | Cleaving Blow | 1_5, looked up by name |
| `[[Aragorn, Ranger of the North]]` | Aragorn, Ranger of the North | 1_89. For a card with a subtitle, give "Title, Subtitle". |
| `[[1C5]]` | 1C5 | 1_5, looked up by collector's info |
| `[[1_5]]` | Cleaving Blow | 1_5, by blueprint id. The text is the card's name. |
| `[[51_5\|the errata]]` | the errata | 51_5, the errata version, with your own text after the `\|` |

- **Names** ignore case, accents, punctuation and spacing, so `[[Erethon, Naith Lieutenant]]` and `[[neekerbreekers bog]]` both work.
- **Errata:** a name or collector's info always means the printed card. The errata versions (sets 50 to 89 and 150 to 199) and the reprints the card library maps to it count as that same card, so `[[Cleaving Blow]]` opens 1_5, not its errata 51_5. To show an errata, use its id: `[[51_5]]`.
- **Ambiguous names:** a name that several different cards share, like `[[Aragorn]]` or `[[Gandalf]]`, is not linked. So is a site whose name several site paths share, like `[[Anduin Confluence]]` (1U353 and 11S228). Use the full "Title, Subtitle", the collector's info or the id instead. A name that is one card's whole name wins over the cards that only share its title: `[[The One Ring]]` is the Hobbit set's 30_48, and `[[The One Ring, The Ruling Ring]]` is 1_2.
- **Mistakes** don't break the page. A card link that names no card, or names several, shows its text without the brackets and without a link. The server log names the note and the reason, including which cards an ambiguous name matched. It looks like this: `Patch note 2026-10-01: card link [[Aragorn]]: 'Aragorn' matches 20 different cards (1_89 Aragorn, Ranger of the North 1R89, 1_365 Aragorn, King in Exile 1P365, ...); use the one's id instead, e.g. [[1_89|Aragorn]]`.
- **Where they work:** card links work in paragraphs, lists, headings and bold or italic text. They don't work inside a `` `code span` `` or a code block, which is how to show the syntax itself, or inside the text of a `[link](...)`.
- The lookup happens on the server when a note is first shown, against the card library that is loaded. When the library is reloaded from the admin panel, the notes are rendered again.

## Cleaning

The notes are trusted content, but the HTML is still cleaned before it reaches the page:

- `<script>`, `<style>`, `<iframe>`, forms, event handlers (`onclick=` and so on) and `javascript:` links are removed.
- `style` is kept only on `span`, `div`, `p`, `li`, `td` and `th`, and only its `color`, `background-color`, `font-weight`, `font-style` and `text-decoration` parts.

## History

The dated entries from 2011-09-21 to 2023-12-09 came from the old **GEMP Change Log** page. They were converted by `gemp-lotr/scripts/migrate_changelog_to_patchnotes.py`, which kept every word and every coloured span and link. That is why those entries use `&emsp;` indentation and backslash-escapes.
