# Ketura's rulings

What Ketura has said about how the game is to be read and how the campaign is to be run, as she said it. This is
the first thing an expectation may rest on, ahead of the Comprehensive Rules and the card's text. Add to it
whenever she answers a question; do not paraphrase an answer into something broader than what she said.

Each entry: the date (at its start, or in brackets at its end), what was asked or what it was about, what she said, and which cards it bears on.

The entries are grouped by subject. `rulings-index.md` beside this file lists every entry by its first words and the numbers it settles; `scripts/lookup.py "<words or a number>"` prints whole entries. A new ruling goes under its subject, with its date; then run `scripts/lookup.py --index` to rebuild the index.

## How the campaign is run

**2026-10-04. Gaps in what a test can do.** They are added to the test rig, never hidden in one card's suite as
ad hoc test functions.

**2026-10-04. Candidate bugs.** Park them for her to rule on rather than fixing them. "Most HJSON bugs have
historically been correctly identified by you, but sometimes you take a correct implementation with an obscure
ruling as an incorrect implementation." Once it is clear how often that happens she will have them fixed without
parking.

**2026-10-04. Questions.** Ask whenever there is one; do not guess at niche rulings or historical minutiae.

**2026-10-04. Earlier iterations of PC cards.** Every iteration of a PC card is kept, with a view to a future mode
that shows how a card used to be handled. Their tests are kept in step with the live ones but stay switched off
until that mode exists.

**2026-10-04. Errata with no tests (F-0006).** On some errata, repeat errata especially, the tests were
commented out rather than written, under time pressure. They are to be written.

**2026-10-04. Race to Mount Doom.** Sets 91 to 94 are within the audit. Set 90's cards are purely visual, with
no text, and need no tests.

**2026-10-04. The Hobbit sets (30 to 33).** Left until very last: there is no text for them, their definitions
are more abbreviated and less structured because the PC does not manage them, and they would have to be
transcribed from the card images first.

**2026-10-04. The ledger.** She does not want the ledger block in each card's test file: "It's encoded and
esoteric; presumably this is used by the author/reviewer, but it should be elsewhere and not in the final
product." It is kept in a file of its own outside the tests.

**How the campaign is run from here (Ketura, 2026-10-06, replacing the earlier rules they touch).**
- *Rig additions need no approval.* "If you're adding a feature to the test rig ... you can add it. The only
  consideration I want you to do is to only do so in a case where there is actual use; no adding features if you
  don't have an existing practical use case for them. Also inform me after the fact in rig-requests, but they no
  longer need to be proposals, just informationals." An author still does not edit the rig: a rig need is
  reported and the orchestrator has it built.
- *The stock mutation run is cut.* "Let's go ahead and cut the remaining mutation step." Authors no longer run
  `mutate.py <id>`. What stays: one `mutate.py --replace ... --with-parked` to show that a parked test passes
  under the correction its finding proposes.
- *One commit for each batch, after its review.* "Let's reduce the frequency of commits to once per agent per
  batch, happening after the reviews have completed."
- *Reports to Ketura are short:* the Q-, F- and G- items that need her for the work to go on, and lists of the
  cards finished and in progress. *[2026-10-06, evening]*

**Findings are fixed without waiting.** "Each of the F- issues that have been brought up have overwhelmingly
been actual issues. Continue to give me a breakdown of fixes being made so I can intervene, but you don't need
to wait for my approval to fix cards; the text should do what it says it does, and I think you only need to
ask if you find an obvious intentional break from the text rather than what appears to be an error or typo.
Or if you are uncertain, of course." Findings are tracked as `fixed` (not yet looked at by her) and
`fixed-approved` (she has said so). *[2026-10-07, later afternoon]*

**Every fix through F-0155 is approved** (Ketura, 2026-10-07): the findings fixed under a like ruling or
under the new rule up to that number are `fixed-approved`. *[2026-10-07, later afternoon]*

**Approvals of 2026-10-08.** Every finding fixed without her word up to F-0195 is fixed with approval (2026-10-08), and every card whose
tests have nothing parked is approved. *[2026-10-08]*

**Code review:** "On RingBearerRule, the private filter is not the way to go. Instead add it to be a sibling of
Filters.ringBearer." Done: Filters.theOneRing (f7dfee394). Card-type filters a rule needs go in Filters, not in
the rule. *[2026-10-08]*

**Landing is a stage of the pipeline, and reports name numbers only (Ketura, 2026-10-08, evening).** A lander
agent follows the reviewer: it files and numbers what the batch found, applies the corrections, works the
documentation and standard issues into the skills at once, and commits. "Ideally it's wrapping and handling all
the updates itself, and then you only have a very few things to put in here so your context doesn't bloat.
Ideally you don't transcribe things that are in files I can read; just listing the Q- or F- or G- id's for me to
go look up is sufficient." The tracker is refreshed at every landing.

**After the errata: Race to Mount Doom lightly, then the `at` tests, then the Decipher sets (Ketura, 2026-10-08,
night).** On RTMD: "These tests were done relatively recently, and so less effort should be required. I think that
formatting and such may still be required, but these three sets should overall require less attention." (Four sets, 91 to 94: "oh right, it is 4 sets. I forgot
we added community entries to 94.") On the
`at` tests, which come next and before the Decipher sets: "we're going to tackle the `at` tests and get those all
converted and in place, either in the appropriate mechanical folders or within the cards they are most associated
with. These tests are going to be a bit gnarly and opaque, so it may make more sense to just note what bug or
behavior or interaction the test is handling, and then take that as a design directive when instantiating a
wholesale new test." So an `at` test is not ported line by line: what it guards is written down, and a new test is
written from that, in `mechanics/` or in the file of the card it is most about.

**Race to Mount Doom is re-worked to the Standard after all (Q-0178, Ketura, 2026-10-09).** The first batch showed
the old files had about three tests a card and that the Standard's missing checks find real faults (F-0311). Asked
whether to keep adding every check no existing test touches or to cut to form and ledger only: "I suppose it can't
be helped if they weren't up to the standard. Let em get re-worked I suppose." Existing tests are still kept and put
in form, not rewritten. And of set V0: "make sure the one card in V0 gets in somewhere" (Rath Dínen 100_1, a batch
of its own beside the RTMD batches).

**Rath Dínen 100_1 and Steward's Tomb (Ketura, 2026-10-09).** "Rath Dinen is actually *sort of* an errata of
Steward's Tomb. It was too strong on sanctuaries, so we altered Tomb to no longer work at sanctuaries and added Rath
Dinen as a partial Tomb that does work at sanctuaries." So 18_139, 68_139 ("If this site is not a sanctuary, ...")
and 100_1 ("When sanctuary healing at this site, ... heals only 3 wounds instead of 5") are one design in three
cards; 100_1 is still tested from its own text, with no original's definition to read.

**Her notes of 2026-10-09 (the whole of them: `orchestrator/answers/2026-10-09-b.md`).** What they rule, by number:

**A non-event's "make each X ..." need not be a snapshot (F-0217, Not Feared in Sunlight 52_107).** "This one
actually doesn't work without the prior functionality. As a non-event, it's not beholden to the snapshot rule RAW,
but there are no Nazgul on the table during the Fellowship phase so it can only function with the non-snapshot
behavior. Revert this please." The snapshot of Q-0011 / F-0144 is the rule for EVENTS; an ability of a card in
play whose text could do nothing as a snapshot keeps its standing filter. *[2026-10-09]*

**A phase word in a "played" trigger is the phase the event is played in (F-0292).** "Definitely a bug, go ahead
and fix." CR 4.2 "phase action": an action that can be played in several phases is of the phase it is taken in; a
"Skirmish or Regroup" event played in the regroup phase is no skirmish event. *[2026-10-09]*

**A site's arrow is tested and kept right (F-0302, Doorway to Doom 68_134).** "Go ahead and fix. You're right that
the arrow never matters in normal gameplay, but that's no excuse for us to be sloppy on this. Eventually we do want
to support multiplayer." *[2026-10-09]*

**Úlairë Nertëa, Dark Horseman 69_38 (F-0307, F-0308).** On the burden: "so long as the freeps player is the one
doing the assignment, it should add a burden automatically": whenever the Free Peoples player assigns her to a
companion of the named race, by his own assigning or by a card (Éowyn 17_96; Shadowy Mount 12_171 has HIM assign),
a burden is added without asking. On the named race: "Let's queue up the engine work that would enable the ability
to reference the memory (one way or another); the mentioned race might be buried in the log and it's tedious to
make the player scroll through it." *[2026-10-09]*

**A choice between two things to do (F-0310, Here Lies Balin 1_19).** "Here Lies Balin is an infamous rules
issue. To wit: when you make a choice you must make one you are "fully capable" of performing, which means that a
1-vitality Orc cannot be hit by either branch. However, since you then must "do what you can and ignore the rest",
the player should be given a choice and attempt to perform either branch. In the zero Orc case, I guess we just
fizzle and offer neither option." So: a branch that can be performed in full is what may be chosen; where no branch
can be performed in full but something can be done, both are offered and what can be done is done; where nothing
can be done on any branch, nothing is asked (F-0116). *[2026-10-09]*

**"Ignoring all costs" waives the cost in an event's own text (F-0316, Race Text 92_29).** "Well-reasoned, I
approve fixing this." *[2026-10-09]*

**Approved as found, to be fixed:** F-0041 (13_75: "Oversight, please fix"), F-0058, F-0201 (Response special
abilities are special abilities to "a special ability is used"; engine pass 20 then found they are already
reported as used, so no engine change was made and F-0201 is closed not-a-bug: the tests hold it, Q-0203), F-0313
("PlayPermanentAction should be altered to shuffle at the appropriate time"), F-0315 (no unhindered companion, no
move: "it will definitely need enforcing in the appropriate places"), F-0318 with F-0319 ("each character" at a
sanctuary counts allies and minions: "Make sure to check minions too"), G-0037. *[2026-10-09]*

**A limit on a discount is not a limit on a strength bonus (Q-0152: struck).** "I'm not convinced that a
restriction on a discount is treated the same as a strength bonus limit, and in Regular's case it was moot anyway."
The engine change of F-0245 (83596b32f) is reverted; Uruk Regular 54_192 is tested as it is built. *[2026-10-09]*

**cardInfo (Q-0158).** "The intent is that `id` is for the card's unique identifier, and parent is supposed to
point to the original (in the case of an errata, absent if an original card). So 1A. 2A is also preferred." An
erratum's `//id:` is its own id everywhere (`54_192.1`); the older definitions are written out in the newer style
(`side:`, `unique:`, the capitals of `type` and the like). *[2026-10-09]*

**No Frodo beside a Ring-bearer who is not Frodo (Q-0159: B).** "Wow I didn't realize this was the case. Yeah it's
illegitimate to put both ring bearers in play like that. B for sure. In addition please look for other standards
which are twisting the game board state past the point of breaking for that, and in particular try to see if
they're jumping at shadows; how many card definitions accidentally said "title(Frodo)" instead of "Ring-bearer"?"
The board comes out of the Standard and the helper table, and tests that use it lose that candidate. Where the
Frodo who is not the Ring-bearer is what a test is ABOUT, he is Frodo, Frenzied Fighter 13_149 (Q-0212, below).
*[2026-10-09]*

**"Until the X phase", begun in the X phase (Q-0160: A, as built).** "'Until X phase' is definitely 'until the
start of X phase', but it's definitely a bit muddy when you're doing that *in* X phase. The One Ring I believe
takes itself off at the end of regroup if it is put on during regroup, which establishes at least a little
precedence. Let's just leave it at A for now." *[2026-10-09]*

**Can't beats can (Q-0161: keep).** "can't > can, so Streets > Shot": Streets of Edoras bars the ally Breaching
Shot 102_21 would have take part. *[2026-10-09]*

**The words of a choice from a discard pile (Q-0162: keep).** "Definitely keep, excellent work. Not every card
needs to add its own wording I think, many of them really are just fine as "choose a card from discard". But if
there's any real twist to it I think it's fine to improve the messaging to have a custom entry." *[2026-10-09]*

**A reveal that a cost buys, from an empty draw deck (Q-0163: B).** "b is fine": offered, and flagged no-effect
(the rule for a costed action that would do nothing); it is the rule for the Decipher cards of the shape too. *[2026-10-09]*

**A sentence after a phase action belongs to that action (Q-0164: A, as built).** "On standard templating, phase
actions go last on a card, with ongoing or each time triggers above them. If you see such text at the end after a
phase action, it's tied to the prior phase action. In the Flaming Brand errata's case, you can think of it as
"lighting" the brand, which then "burns out" later. These burn-out clauses keep the effects from being on events
without the clog issues of events." *[2026-10-09]*

**Mordor Fiend 60_91 loses its "you may" (Q-0165).** "Ah piss, we missed that. We'll alter the text, make it
required in the text and alter the definition to match." *[2026-10-09]*

**Real cards and real mechanics before invented ones (Q-0166: A).** "Always prefer using real mechanics or cards;
if we have to invent cards we need to scrutize whether or not we have gotten lost in the weeds." An unpayable "add
a burden" is shown with a hindered Ring-bearer. (The invented card the mechanics tests then kept for it went with
the rest in engine pass 27: Q-0241.) *[2026-10-09]*

**Kept as built:** Q-0151, Q-0153, Q-0154, Q-0156, Q-0157 ("Keep both"), and Q-0155 ("I'm okay with keeping I
think but the order really doesn't matter unless a specific counterexample is brought to my attention").
**F-0283: A** (Dunlending Warrior 4_18 and Ravager 4_15 are withheld when they can assign the minion to nobody). *[2026-10-09]*

**Her third notes of 2026-10-09 (the whole of them: `orchestrator/answers/2026-10-09-c.md`).** By number:

**A meta-site that says "you" or "your" anywhere binds its owner alone (Q-0183, Race Text 92_9; Q-0188).** "The
rule we've been following is that any meta-site with "you" or "your" anywhere in the text causes the entire card to
only affect the owner (modulo context like "your opponent" etc)." A sentence of such a card with no "your" in it is
still the owner's alone; it is not a question. *[2026-10-09, later]*

**A player with a choice is not forced (Q-0171, Deceit 68_29).** "There are a couple official card rulings for Old
Noakes and Courteous Halfling which are relevant: if the player has a choice, they're not forced and therefore don't
get the benefit of protection." When a Free Peoples card has the Shadow player discard his own card, the Shadow
player is the one discarding. *[2026-10-09, later]*

**"Cannot be replaced" holds wherever the site lies (Q-0173, Doorway to Doom 68_134: B).** "The Mount Doom legacy
ruling was meant to establish a precedent on site replacement, which this errata follows." *[2026-10-09, later]*

**A card taken from a draw deck with no "reveal" in the text is not named (Q-0176, Race Text 91_2: A).** "Yeah
there's a rule about this, A is the correct way." The log says a card was taken and no more. *[2026-10-09, later]*

**A change to the move limit lasts the turn (Q-0179, Race Text 91_8 and 91_12: B).** "These ones should follow the
basic rules around move limits, which is that they are permanent." "While ..., your move limit is -1" is given
once and holds for the rest of the turn, as Decipher's cards are built (1_260). *[2026-10-09, later]*

**"The fellowship moves" is read at the site left (Q-0184, Race Text 92_18: A).** "I think A is the correct reading
as per that movement breakdown you cited. However, if the engine is doing that in the wrong order, that needs
adjusting, rather than switching the trigger to "moves from"." The fix is in the order the engine raises the move,
not in the card. *[2026-10-09, later]*

**"Sites from any block" lifts the block rule and nothing else (Q-0185, Race Text 92_19: A).** "I think we can
safely ignore the X-list issue, as nobody is going to run RTMD on anything but a common format, and none of those
have banned sites. So I think the errata question is the only one that can crop up, as we do run PC-Movie RTMD. I
think A is preferred. As for the hobbit sites, they don't need brought in I think." *[2026-10-09, later]*

**Burdens are on the Ring-bearer whatever his state (Q-0186, Race Text 92_25: A).** "Burdens can't go on anything
*but* the Ring-bearer, so I call this a case of redundant verbiage. A. The Frodo case was one where there could be
someone else they are on, such as Sam or Galadriel or whoever, but there is no alternate here." So Q-0103 ("on
Frodo") is about a NAMED bearer; "on the Ring-bearer" counts every burden. *[2026-10-09, later]*

**Counted by the turn, not across turns (Q-0187, Race Text 92_35: A).** "I would do by the turn. Tracking stuff
across multiple turns is a negative play experience that I try to avoid." *[2026-10-09, later]*

**A starting fellowship is Free Peoples cards (Q-0189, Race Text 94_3: A).** "No playing minions during the
starting fellowship :)" *[2026-10-09, later]*

**Who holds a title is data; the name check gets a mechanic test (Q-0190, Race Text 94_5: B).** "What's more
important is that the player-name-checking code has card-agnostic tests somewhere. Given that these will change
over time if past world champions reveal their account names (or if someone *finally* beats basmelis so he doesn't
win a fourth time in a row), let's do B, but with the mechanic test coverage." *[2026-10-09, later]*

**A card discarded to pay for a retrieval is not the card retrieved (Q-0192, Race Text 94_12: A).** "The ruling
for They Are Coming applies here: discarding a card from hand to retrieve a card is not allowed to pull the card
just discarded." *[2026-10-09, later]*

**Card by card:** Q-0167, Demoralized 61_114: B, asked each time ("I suspect it's a victim of the mass edit").
Q-0168, Mountain-troll 65_112: B, five OTHER [orc] minions ("edit the text to add "other", I will adjust the
errata"). Q-0169, A New Light 67_15: A, "If you do" wants both minions returned ("that's an oversight"). Q-0172,
Grond 68_82: A, exactly 2 ("I concur with your reasoning"). Q-0177, Race Text 91_4: A ("hrrm. I think A").
Q-0180, Deepest Delvings 101_2: the sheet's revision 1 was never released ("The current one is too braindead and
probably should have been altered, but we didn't muster up the willpower to do it"). Q-0181, Rath Dínen 100_1: the
arrow is Right ("let's make it match Tomb I suppose"). Q-0182, Race Text 92_1: A, the Ring-bearer is one of the six
("the 3 is an illustrative demonstration and not a set point"). Q-0191, Race Text 94_10: A. Kept: Q-0170 ("I
question how often this will come up, but I think it's fine to keep"), Q-0175. *[2026-10-09, later]*

**Every finding below F-0308 that was fixed without her word is approved** ("go ahead and also approve all the F-
entries lower than F-0308 which were marked as fixed without word"): 62 findings, now `fixed-approved`. *[2026-10-09, later]*

**One entry for one issue (Ketura, 2026-10-09, evening).** "If there is a Q for an F of the same issue, I only need
to see the Q (the F can be given the same approval status as the Q)." The tracker's waiting list leaves out a
finding that an open question is filed under or names; when the question is answered, the finding takes the
answer's status with her words (approved as fixed, to be fixed, or not a bug) without being put to her again. The
same day: every finding fixed without her word below F-0327 is approved; Q-0193 and Q-0196 kept; Q-0194 and Q-0195
A; Q-0174 closed as settled by F-0308; F-0324, "I think a 'transferable to' filter is perfectly kosher, go for it."

**An `at` test about a Decipher card becomes a lone test in that card's file (Ketura, 2026-10-09, late).** Asked
whether the replacement of an `at` test that is the plain case of a card whose set is not yet worked should be
(a) written now, to the Standard's form, as a lone test in the card's file, with no ledger and no landing, or
(b) the occasion for the card's whole treatment out of set order: "A is all I wanted, yeah. Just wanted to ensure
that we didn't forget certain tests which cover functionality that (so far) aren't covered elsewhere, and also to
ensure that the improved version of those tests was inserted as a kickstart for the associated cards." So nothing
an `at` test guards is lost, and its improved form waits in the card's file for the card's own batch.

**"Instead" converts a cost; it does not prevent or cancel (Ketura, 2026-10-09, late; Merry 4_311 with Treebeard,
Earthborn 4_103).** "The main thing that makes "instead" interesting is that it causes costs to be paid via an
alternate currency--not preventing or canceling the effect, just providing a conversion. This has not been fully
realized in the engine (nor with the rules interpretation gestalt)." A cost paid in the other currency is paid,
and what it buys happens; where the game does not do that it is an engine finding, not a question.

**Her notes of 2026-10-10 (the whole of them: `orchestrator/answers/2026-10-10-a.md`).** By number:

**A branch chosen because it can be done in full is carried out in full (Q-0209: B, with a caveat; Here Lies Balin
1_19, Stand Against Darkness 51_63).** "B is correct, with one caveat: while the choice is valid (i.e. both sides
are "fully capable" of happening), the eventual secondary choices (who to wound etc) are an extension of that
choice and thus must be restricted to only "fully executed" choices; picking "wound one Orc twice" means you can't
pick a 1-vitality Orc. However once a choice is broken by having no "fully capable" branches, then we are free of
that restriction and instead fall back on "do what you can and ignore the rest", which *does* permit us to wound a
1-vitality Orc twice. Having the Choice structure somehow encode this strictness would be nice, but if we have to
contort half a dozen cards instead of making the engine significantly twistier, that might be preferred." *[2026-10-10]*

**The rules' order of a move stands, with what it changes (Q-0210: keep).** "This is a big keep. Those situations you
mention are *different*, but now they are in line with what the rules dictate." *[2026-10-10]*

**Race Text 94_30, "You may not play or take into hand cards from your discard pile or draw deck (except when
reconciling)" (Q-0206: A; Q-0207: A; Q-0208).** A card DRAWN is a card taken into hand from the draw deck: "I
intended for it to include standard card draw; the reconcile exception was so that the player didn't just sit down
to play with their starting fellowship and nothing else." The bar is on the player doing the taking, not on an
opponent's card that returns his card to him: "Besides restricting your opponent when it's not meant to, such
restrictions almost always get ruled to be treated differently when the opponent is acting (see discard prevention
and the "forced" stuff)." And set-up is untouched: "No, it shouldn't affect the starting fellowship. I will add "or
starting fellowship" to the text, go ahead and do the same." *[2026-10-10]*

**Race Text 94_28: the card taken in place of a draw is still a card drawn (Q-0205: B), and set-up is left alone
(Q-0204: B).** "I intended the "instead" use to pay card draw costs...but then I suppose if I take a burden instead
of a wound, I don't trigger a wound trigger. Hmm. Viewing it as a draw filter is helpful; after all that card *did*
come from the top of your draw deck. Let's say B for this one." "Let's leave it out of the game setup for the same
reason that reconcile is left out (8+ choices would slow eeeeverything down)." *[2026-10-10]*

**Boromir's Cloak 51_98 reaches Response abilities after all (Q-0203: 1A; it replaces 1B of Q-0119).** "Ha, in that
case let's 1A. I'm nothing if not consistently inconsistent. And yeah, address TATD's gap with it." (Terrible as the
Dawn 53_103's test was added in engine pass 21.) *[2026-10-10]*

**Úlairë Nertëa 69_38 when her burden is prevented (Q-0199).** "The solution is closer to B, but I think needs what
is essentially an "add additional cost" modifier; I'm pretty sure that already exists in some form, and it will
need applied to this somehow. So that it targets "any assignment action that lets free peoples assign to Nertea".
Dunno how involved this might be, let's look before we leap." A design is written up for her before anything is
built. *[2026-10-10]*

**The deck builder under Race Text 92_19 (F-0342).** "Let's amend this. I think it will warrant a few different
sortandfilter tests emulating different formats and contexts, and yeah I also think that if possible it should only
actually open up the site filter if the player is currently using that meta-site, which I *thought* was the case." *[2026-10-10]*

**Card by card:** Q-0201, "ignoring all costs" where a cost names X: A, as built ("this isn't a mechanic that's
going to be reused, and it's just for fun on the RTMD card"). Q-0202, Race Text 94_22: A, the draw deck ("that
should say "draw deck". I'll adjust the wording"). Q-0197, Rath Dínen 100_1: the arrow stays Right ("I'll change the
image"). Kept: Q-0198 ("well done"), Q-0200. *[2026-10-10]*

**Concealed and exposed are no part of the game (Ketura, 2026-10-10).** "Let's go ahead and nix the
concealed/exposed and rip out those keywords and their uses. It was a mechanic proposal that never manifested."
Removed from the engine in engine pass 23; no card or test names them.

**The old hinder suite (Ketura, 2026-10-10).** "Really the entire Hinder suite was tdd for the hinder mechanic; it was
developed before any of the cards were implemented, thus the hacked Frodo. But yeah I do approve of altering them
to use real cards now." Its three tests of a hindered card's strength, vitality and resistance stay as they are in
kind, on a construct of the tests' own: "leave those stat tests, as there's engine code that this was used to
enforce." Hinder does not reach inactive cards: a stacked card is not hindered (the stub about Courtesy of My Hall
4_44 came from a draft that had it, "but later we altered it to not affect inactive cards, so that's obsolete").

**The bid reads resistance in force (Ketura, 2026-10-09, late; Galadriel 9_14 as Ring-bearer).** "The game text on
the ring and Ring-bearer are active enough for resistance calculations during the bidding process, otherwise
Galadriel would never get to bid much at all." CR 4.2 "bid" (a bid equal to or higher than the Ring-bearer's
resistance loses) is read against resistance with his own text and the Ring's counted.

**The `at` tests, what is kept (Ketura, 2026-10-09, late).** "Some tests will be inferior to our standard card
procedure. But it's the tricky ones, the ones about specific interactions between cards, that I know are important
and need keeping. (The rest are busy work)". "The set 40 stuff going into oblivion is acceptable." A Hobbit-set
test goes under `cards/unofficial/hobbit/` ("but not under 'pc', as the PC doesn't run that"), in the usual
directory structure; the Hobbit sets themselves are still worked last.

**Everything she must review is on the tracker, whole; the chat carries the code (Ketura, 2026-10-08, night).**
"Please ensure the details are in the Waiting on Ketura page of the report; I will be on my phone for an extended
period of time, so any details for every item I need to review will need to be there for the foreseeable future.
Summaries aren't necessary in here, just a reference to the code, and if I can look at it in there that would be
sufficient." So every item that wants her word has a number (Q-, F-, G-) and its full text on that page: open
questions, changes an agent made beyond its brief ("Keep or strike" questions; unanswered, the change stays),
findings mended or filed without her word, open rig requests; and the reports are on the page in full. Nothing for
her lives only in a report or in the chat.

**The boards no game could reach: what stays and what goes (Q-0213).** "So in general I approve of the approaches
recommended for each item. In particular I think it's a waste of time to continue searching for *theoretical*
malformed targets in places and arrangements where they have been historically sparse or even outright
nonexistent." By the letters of the audit (`orchestrator/engine-pass-21/audit.md`): **A**, a lone Ring-bearer who
is not Frodo on every card that says "the Ring-bearer": the requirement is dropped (no definition has ever said
Frodo for the Ring-bearer); tests written stay. **B**, a card given a culture or a race it does not have: a
printed card where one exists (Pippin 103_31, Merry 103_85, Sméagol 103_116, Khamûl 103_61 and their like);
dropped for Elf, Dwarf, Wizard and Wraith, where none can exist, and lint reports the definition in place of
asking for a board. **C**, a keyword given to a card no printed card with it resembles: dropped, no new ones.
**D**, a keyword given only to set a board up, and **E**, a number given by hand: kept; a new test takes the
printed card where there is one. **F**, sites given a terrain: real sites. **G**, two cards of one unique title
(struck 2026-10-08): the 13 files that still have it are relaid. **H**, items where they could not be: items of
different classes on lawful bearers. **I**, tokens on a card nothing gives tokens to: no new ones. **J**, a site
of another block put into a path: kept. **K**, the files already written with B and C, where the audit proposed
leaving them: "I would like a pass to alter the tests that have utilized those methodologies: if the case is
legitimate (i.e. can be represented in a real card, such as the V3 Attea who is also an Easterling), then use
such an example, but in the case where it's only theoretical and can't actually exist in the current card
base...? See if there's a nearby concept, but if not, delete the test." **L**, Held 6_109, built to corrupt the
Ring-bearer where the text names Frodo: "looks like a bad implementation; it should be "Ring-bearer,
title(Frodo)"". *[2026-10-10]*

**A Frodo who is not the Ring-bearer, where a test needs one, is Frodo, Frenzied Fighter 13_149 (Q-0212: keep).**
"I had forgotten about Frenzied Fighter, and he is a valid use case in scenarios where we legitimately need to use
Frodo contrasting with the Ring-bearer. The principle is that I objected to exhaustively testing and re-testing
every instance of "Ring-bearer" (which is a lot!) using cases that can't arise naturally, and which so far have
found no actual bugs. But you have found the right balance of adherence to the principle and practical
application." The tests of 51_310, 51_311, 101_54, 51_313 and 103_115 that are about that contrast stand.
*[2026-10-10]*

**Before the Decipher sets: a trial of Q-0034 in the engine, and a search for other patterns (Q-0215).** On
withholding by the engine, from its own no-effect flag, what Q-0034 says is not offered: "I'm down for a trial on
a separate branch. I would want it specifically aiming to try to evaluate how widespread of a fix it would be; if
it requires the engine fix and still a bunch of card edits then we're right back to where we started." And
beyond it: "I'm more concerned about trying to ensure that such ideas like this have been ferreted out before we
do the big 3K. The PC cards cover a large swathe of functionality, between the ~256 V-cards and hundreds of
errata, so it seems like problem patterns we've found here should probably generalize, tho it seems apparent
that more are likely lurking. In addition to the trial, let's pencil in a brainstorm task to see if there are
more patterns we should try and pre-empt before the big chunk." Briefs: `orchestrator/briefs/` in the state.
*[2026-10-10]*

**The `at` conversion is a one-time job.** On what of the running of the campaign belongs in the skill: "the at
pipeline can be mentioned, but also note that once complete it's unlikely to be revisited, since it is a one-time
lift-and-shift upgrade. The rest of this suite is likely to be used with future cards, however." *[2026-10-10]*

**After the `at` conversion: the cards with existing tests, then a stop (2026-10-10).** "After the at suite
conversion has been completed, resume upgrading the existing non-stat tests, which I believe now only has about
150ish remaining. At that point however, if you finish those, please wind down the card agents (you may continue
doing engine upgrades as needed). We will stop at that point to take stock and put together a massive set of patch
notes from the findings list, as well as cross our i's and dot our t's pending near-future deployment. And only
after that will we begin tackling the big 3K." The 152 are the Decipher cards whose files had behavioural tests
before the campaign (`orchestrator/queue-decipher-existing.json`). No card batch is launched after them without
her word. *[2026-10-10]*

**Before the Decipher sets, every proposal of the pattern search is taken (Q-0220).** "A-J are all yes. I is
"generate"". By its letters (`orchestrator/trials/patterns.md` has each in full): A, the Q-0034 offers once, in
the engine as far as the trial reaches and the rest in a sweep (how far the engine goes is Q-0224); B, a
"census" check in lint (what a text names against what its definition names) and a sweep of what it found; C,
`mechanics/` classes for the rules only Decipher cards use, then a first batch of 59 chosen cards; D, a choice's
branches able to say they will do nothing, once in the engine; E, `attachedTo(<a kind>)` and a hindered bearer
(Q-0108), once in the engine; F, one list of 21 definitions in shapes already ruled; G, a ruling (next entry);
H, cards that share a text kept in one batch; I, the Play clause's test written by a generator; J, seven lines
for the authors' notes. *[2026-10-10]*

**"You may" on a trigger against a definition that cannot be declined, or the reverse, is corrected where found
(Q-0220 G: yes).** Where a trigger's own sentence says "you may" and the definition cannot be declined, or says
no "may" and the definition asks, the definition is corrected with no question each time. Goblin Runner 1_178
is the one exception already ruled. *[2026-10-10]*

**The Q-0034 offers are withheld by the engine, with the effects taught to say they will do nothing (Q-0224: A2).**
"if I am understanding things correctly: you would like to "teach" a variety of game effects how to report
noeffect, to 80/20 the work so that it's majority not work held only in the card definitions? If that's an
accurate read of the report, then yes, I approve it. I think I agree with your rec for A2." (It is an accurate
read.) The trial's rule comes onto the campaign's branch with its thirteen taught kinds; the part that reached a
"you may" inside another action comes out. And Q-0231, B: "I agree with B": a no-cost "you may" that ANOTHER
card's bar makes empty (Orc Inquisitor 1_268 against Frodo, Courteous Halfling 4_301) is not offered either; the
random discard is one more kind taught. *[2026-10-10]*

**Setting a board up early is no board out of play (Q-0228: A; Q-0230: A).** On a minion laid on the table in a
fellowship phase so that a standing text can be read on it there: "A for sure. The pre-game cheating is meant to
keep setup concise and specific to the scenario so that we're not wasting a bunch of time going through setup
that doesn't matter for the test. There's definitely tension between that and "keeping it real", but it's a
matter of striking the right balance." On Dunlending Looter 4_11 read stacked on a controlled site with the
fellowship at site 2: "this card has two halves. *By itself* the card cannot get stacked on a site you control,
but there are myriad ways of stacking other minions around, and it's reasonable to treat these two abilities
entirely separately: one set of tests that is about the stacking, and another that is about the Shadow ability.
The two do not have to be tested in tandem. I think that A is the sane way to handle it. The test doesn't
teleport a site into the Shadow control area and try to do a bunch of weird shit around that, it just arranges
the board state a little early." *[2026-10-10]*

**Tests of the definition language go in a place of their own (Q-0233: C).** "it actually makes a lot of sense to
have nuts-and-bolts testing of the HJSON layer (when performed) sequestered into a separate place. I think I lean
towards C; gemp-lotr-server is the only module with real tests ... But yes, feel free to alter the scripts you
use during testing to only run relevant sections (I doubt you need the league/tournament tests either)." Plain
JUnit, in the server module, in a package of their own (`definitions/`), run by the suite; `mechanics/` holds
tests of the game only. *[2026-10-10]*

**Site text is not active while the starting fellowships are played.** "I believe there was an earlier item that
ensured that site text worked during starting fellowships, but this text is in the `active` section of the CR:
"Site text is not active when the starting fellowships are played." So we'll need to dig out that change and axe
it. I expect that I will be back with a more broken-down version of the game rules for us to do a pass on for
things like this." *[2026-10-10]*
Looked for by engine pass 26 (item `sitetext`, 2026-10-11; F-0470, not a bug): there is no such change to axe. The
game has never let a site's text act while the starting fellowships are played (guards of 2011 and 2019 in
`GameState.isCardInPlayActive`; no commit of the campaign near them; no test asserting the opposite; eleven probe
boards, in `orchestrator/engine-pass-26/sitetext.md`). At East Road 1_320 a companion in the draw deck reads its
printed cost while the fellowship is chosen, and 2 more in the first fellowship phase. The item she remembered
is most likely G-0036 (a site map that gives the game no site 1 to lay), which is about the test table and not
about site text.

**Patch notes are for players (2026-10-12, on the first draft, which was not).** "I wish I had been consulted
first lmao. These are *not* patch notes, they are a summary that is only just readable enough for me myself to
parse it, and nobody else. Patch notes are for the end user, who is not technical and has zero insight to the
Q-/F-/G- system, nor Gemp itself. What they want to know is what changed from *their* side." Her format: "Every
item should be a single statement about how a card or feature has changed in interaction. If it is not seeable on
the client, it is not a patch note. Example: "Fixed [[Aragorn, King in Exile]] prompting the user when no
companion could be healed."" Materially similar cards go into buckets: "If more than a handful of cards fit into
the bucket, the entry should change: "Fixed 123 cards prompting the user with an optional trigger that would do
nothing. Example: [[Aragorn, King in Exile]] no longer asks you to heal if there are no wounded companions with
the Aragorn signet."" "Straightforward implementation fixes should not belabor the fix." A card with several fixes
that are in no bucket gets them as subitems under its name. Changes to the game as a whole, not about one card,
are "described in a handful of concise sentences, and all of them should be bundled together at the top before
card-specific fixes" ("New Feature: After the Ring-bearer is killed, the One Ring now stays on the board marking
where they once were. ..."). "This may result in a large number of F- items not showing in the patch notes (or
not being discernable), and that's okay." And: "I recommend writing some scripts for this procedure ... bucketing
and such can be made procedural. And given that we have 3,000 more cards to go, this is likely to come up over
and over." Her model, for the gist only: the Dota 2 patch notes (dota2.com/patches/7.41). *[2026-10-12]*

**Patch notes, the sample (Q-0288, 2026-10-12, fifth notes).** "Fantastic, this is a thousand percent better."
The shape stands ("It's just about perfect"); a handful is 5 ("5 is good"). A PC errata is written "[[id|Name]]
(PC Errata)". Fixes to a card's displayed text alone: "Put all of those into a separate section; I'll be
introducing those changes as "non-errata errata" that emphasize they're just visual in nature without changing
much." When notes go out is open ("there's a chance none of this will go out until December, so I just want the
pipeline/workflow streamlined"). The lander writes each finding's patch-note row as it files it: "Brilliant idea,
that way the agent review happens as we go and makes it more scriptable." *[2026-10-12]*

**Patch notes, the first full draft (Q-0294, 2026-10-12, sixth notes).** "Conciseness was the more important part,
that each sentence did not drone on and on, and I think these do that just fine." Collector's numbers: "The collinfo
isn't visible anywhere except the image, no; you can nix that." Item class: "Technically it's visible in the
right-click card readout. I would say "Added missing 'Pony' item class."" An erratum's link to its original: "This
is bookkeeping the user won't see, you can drop this." Errata rebuilt to her newer text: "Yeah they'll be listed as
proper errata, but they'll still be here too." "The no-effect feature will need a bit of explanation in its own
feature bullet, and then after that all such affected cards can reference it by saying "now marked with no-effect
border"". A response offered twice for two winners is a game change: "Agreed." And: "most of the changes I would
make are very specific to the entry at hand, and so will just need a pass from me at the end, whenever these get
posted. But I will say: capitalization entries can all get put into one bucket, those don't need their own
individual card lines." (`references/patch-notes.md` has these as rules.) *[2026-10-12]*

**Only questions reach her; no double approval (2026-10-12).** "a good number of recent entries has just been my
rubber-stamp ... update how the "Waiting on ketura" tab shows items, perhaps only showing questions (leaving
Findings still in the two-stage fixed/fixed with approval, so that I can spot-check those). And questions that are
just asking for double-approval after I already approved the item are probably overkill, yeah." So: the Waiting tab
shows open questions, findings asked as questions and rig requests; fixes made without her word are spot-checked on
the Findings tab. A "Keep or strike" is filed only for a change beyond an item's words that changes play (roles/
engine.md step 9). Also: deployment is "days or possibly months" off ("I just want the workflow all ready to go");
after the engine pieces the campaign moves to a new instance; the usage window "hasn't come into play for a very
long time", and three pipelines may come "but not with this instance". *[2026-10-12]*

**Making the 3K faster (Q-0296, 2026-10-12).** Of the slim-down's four changes (`orchestrator/trials/slimdown.md`):
card agents carry only the tools they use, a cap on what agents read in full at the start, and the lander's first
gate run dropped are adopted; the gap between batches stays: "I wouldn't eliminate the gap; that one I think is
likely more responsible for the solid stability than any other." The author stays at high effort: "I'm also not
convinced about B; on a previous instance we did an extensive rerun trial with Sonnet vs Opus and Sonnet did in fact
straight miss several errors, tho Opus also didn't re-find all the same errors either, which made downgrading a bit
of a tradeoff." *[2026-10-12]*

**The trial batch, and the gap taken out (2026-10-12, thirteenth notes).** After dec-b1_349 (card-worker agents:
3.6M cache-read and 5.8 minutes a card, against 6.05M and 12.5): "if it's that big a tokens savings, let's remove the
gap and go ahead and go with a 2 pipeline setup." (The next batch is launched before the last is closed;
`<work>/pipelines` is 2.) *[2026-10-12]*

**The gap back, after a restart (2026-10-13).** The machine restarted with two batches at work and a landing
running beside them (no gap). Offered (1) two pipelines with the landing between batches, (2) no gap on one
pipeline, (3) as before: "Yeah go with 1." *[2026-10-13]*

**Every landed card approved; mechanics sorted by subject (2026-10-12, tenth notes).** "Go ahead and mark all current
card tests as approved." (633 cards from ready to approved; the 2 parked stay parked.) "Let's sort the mechanics test
folder into subfolders. Something like: keywords, hinder, choices, actions, skirmishes, as well as as many others as
you deem fit." (Engine pass 33: twelve folders.) *[2026-10-12]*

## Sources, sheets, revisions and errata

**2026-10-04. Comprehensive Rules 4.2** should contain all the official rulings.

**2026-10-04. The reviewed spreadsheet** (`gen_en_standard.tsv`) is the combination of at least two card
databases and has been combed for errors since. It is what the card images are regenerated from, and it was the
input from which the Decipher definitions and their stat tests were first generated.

**2026-10-04. The generator sheets.** `V1.tsv`, `V2.tsv` and `V3.tsv` are the V-sets; `master_errata.tsv` was put
together from the twenty or so sheets used over the years. "Many times there were errata *candidates* which did
not go out, or which were delayed between releases, and so the information here is likely only about 95%
accurate. If an agent comes across a discrepancy, let's flag it but it may not necessarily be an error." The
latest revision of a V-set card may be in the V-set sheet or may be counted as an erratum.

**2026-10-04. Comprehensive Rules 5.0 excerpts** have the entries for hinder and the other PC-specific mechanics.

**The Rules Councils' own rulings.** Ketura has supplied the eight Legacy Rules Team rulings (Decipher's cards)
and two Modern Rules Team rulings (the PC's) made since CR 4.2. `rules.py council` lists them and
`rules.py card` and `rules.py grep` search them. Look there before asking: Q-0016 had been ruled already (LR 6).
Their status lines are not all current (MR 1 and MR 2 say "Discussion" above a dated decision); a page with a
decision section is decided. *[2026-10-05, afternoon]*

**2026-10-05. What a card looks like today.** "The URL of the card can be pulled to inspect the actual text on
the card image used today as a further piece of information."

**Sheet discrepancies, card by card (2026-10-05).** Which side was right, card by card. Where the definition was wrong it has been corrected (findings F-0024 to
F-0030); where the sheet was, the difference is in `known-differences.tsv` or was a matter of revisions.

- **51_245 Desperate Measures:** "top 8" is correct.
- **52_45 Too Much Attention:** "top 5" is correct.
- **57_190 Morgul Destroyer:** +3 is correct.
- **60_11 Galadriel, Lady Redeemed:** the text should have the starting fellowship discount; it was not removed.
- **68_134 Doorway to Doom:** "2", not "two". "Game text should always use numerals where possible for space
  reasons, particularly on sites like this."
- **102_15 Watching and Waiting:** "remove (3)" is correct.
- **53_106 Bill the Pony:** should have the Pony item class; it was added to the engine and never put on the one
  card that has it.
- **54_245 Southron Archer:** no limit is correct; it was made unique instead of having its text altered.
- **102_75, 102_76 (the maps):** they have no culture; they are neutral.
- **The Bladetusk cards (103_41 to 103_43):** printed with a strength and vitality "rather than cram even more
  game text in", but "from a mechanical perspective it doesn't gain them until the text is triggered, as
  implemented."
- **Revisions.** Several differences were one revision set beside another: 53_120 ("heal a companion" is revision
  2, the most recent), 58_57 ("assigned to a skirmish" is revision 1; the definition's text looks like the base
  card's; revision 2 is different again), 60_17 ("twice to discard 3" is revision 1, "hinder" is revision 2),
  51_80 (revision 1 had vitality 2, revision 2 has 4), 53_103 (revision 3 is Maneuver, revision 4 Response),
  101_26 and 101_28 (revision 0 and revision 1; 101_28 was Maneuver for 1 and is Shadow for 0), 102_51
  ("remove 2 tokens" is revision 1). "Make sure this check is lining up revision numbers."
- **Rath Dinen** is in the errata sheet as V0U1.0. **Forth the Three Hunters!** was printed by Decipher three ways
  with the same collector's info, one in each culture, identical otherwise; "a perpetual headache in all systems
  that needed a way of handling their unique ID situation."
- The V2 errata were missing from the master sheet and have been added (most add Valiant to various Elves).

**The sheets (2026-10-05, evening).** 58_57 Corsair Marauder: the sheet's row is the revision 1 that was
released, and the definition "is an old candidate and needs altered to match the sheet." 67_7: helper text
"should be removed from the game text (so it isn't interpreted as a secondary cumulative effect). In print those
are italicized and ignored." Of the wording-only differences, 60_8 keeps its wording ("so players right-clicking
on it see the rounding-down ruling"), 53_17 is revision 2 with revision 1 missing from the file, and "all other
cards in this section should be altered to use the wording in the sheet." `PC_Cards.js` shows which revision's
image is in use and is one more source. On revision markers: a later revision was copied from the one before,
so a live block mislabelled with the earlier number is the common fault; look in the comments for the old
revision before concluding anything. *[2026-10-05, evening]*

**Corsair Marauder (58_57) and the history of a card's revisions.** "The current implementation is good, it just
needs to have its revision number updated." The earlier implementation "should be put into a comment above it in
the HJSON, as should any of the other missing revisions, so that the file has a one-stop-shop for history." So
an errata definition file carries every earlier revision of a card as a commented-out block above the live one,
oldest first, and the live block's `revision:` is the number of the revision released. *[2026-10-05, night]*

**2026-10-04. Reprints keep signets (F-0002).** Boromir, Defender of Minas Tirith (12_43) and Pippin, Hobbit of
Some Intelligence are exact reprints of earlier cards that had a signet, and were ruled to have those signets
too. The ruling is in Section Three. A difference between a sheet and a definition is to be checked against
Section Three before it is called an error.

**2026-10-04. Two cards, one printed title (F-0004).** Orc Pursuer (5_108 and 7_303) "was a fuckup on Decipher's
part, and they were ruled to be two separate titles so you could have 4x of each." White Hand Attacker (17_120
and 18_124) is "a directly equivalent circumstance", though it never got an official ruling: sets 17 and 18 were
the work of one of the last people not laid off, and rulings by then were nonexistent. The "(2)" on the later
card's title never appears to players.

**2026-10-05. Keys written twice (F-0017).** "Almost certainly a migration or automation error. In repeat cases,
reduce them to one, and if they do not match we'll have to investigate the text to see which is correct."

**2026-10-04. Timewords (F-0003).** "Timewords are an implementation detail and not a game mechanic." A
definition that lists a timeword the card does not print, as the way to let an event be played in another phase,
is acceptable. Bears on: 12_114 For the Mark, and any event playable outside its own phase.

**A Decipher ruling on the original card does not reach an erratum that rewrote the sentence, by default**
(Q-0111, Dwarven Axe 51_9): "our errata is our own set of rulings". But: "many errata happen to work around
the ruling made by Decipher, but not all. I concur with B being your default, but do bring it up when it
happens, because I sense that there are some where we agree with the ruling." So each such case is reported
to her, with the default taken. *[2026-10-07, night]*

**Q-0115, Galadriel 51_45:** "Decipher's ruling was on 'free', not Galadriel per se. Since the errata does not
say 'free' it's irrelevant. B." A Decipher ruling about a WORD does not reach an erratum that dropped the word. *[2026-10-08]*

**Northern Ithilien's errata 57_359** existed only to work around the engine fault now fixed and will be
reverted once F-0139 is deployed: its limit is not a concern. *[2026-10-07, later afternoon]*

**A backslash in a title, lore or game text is no fault.** "This is due to that being the shorthand used in our
nanDECK script for newlines. Anywhere that it's found in titles, lore, or game text, it can be replaced with a
space; it doesn't need its own finding report." *[2026-10-08, afternoon]*

## Formats

**2026-10-04. Formats and correctness.** Formats are by and large a table-validation concept: players may not
show up at a table with a deck that violates the format, but with a few exceptions format has very little effect
on whether play is correct. "A card from set 1 doesn't care that the site path has cards from set 18 in it by and
large." Tests need not hold a card to the format it was printed for.

**2026-10-04. The exceptions.** A card that explicitly mentions sites of a certain block has to be tested on that
block. And rules that differ by format have to be tested in a format-aware way where a card touches them. Her
example: the rule that the Ring-bearer's skirmish cannot be cancelled was introduced in the King era, so
Fellowship- and Towers-era formats do not have it, and cards that cancel a Ring-bearer's skirmish need tests under
both sets of rules. She expects the list of such rules to be short.

**2026-10-05. When the Ring-bearer's skirmish stopped being cancellable (Q-0001).** The rule came about from set
9, Reflections, "which has several non-Frodo ring-bearers who warp the format if they are able to have their
skirmishes canceled, so it was with the release of set 9 that the rule was added." King block is sets 7, 8 and
10; "there's not really a good reason for King Block itself to permit Ring-bearer skirmish canceling. I think
nobody noticed because King Block itself is a rounding error of usage; it's far more common to play Movie Block
(1-10), which does indeed have the proper RB canceling rule." Format-aware cancel tests use `fotr_block` (allows)
and `multipath` or `movie` (refuses), and leave the King block formats out until F-0014 is settled.

**Historical formats are left alone (F-0014).** "We won't touch these formats. There is one card that can cancel
a RB skirmish, and there's no reason to alter the historical format." King block's settings stand. *[2026-10-05, evening]*

## What gets tested, and the no-effect flag

**2026-10-04. Isolation and interactions.** Cards are by and large tested in isolation. But wherever a ruling's
explanation uses a pair of cards interacting as its example, there should be a test that the interaction goes as
the ruling says.

**2026-10-04. Mechanic tests.** A set of tests that a particular card happened to uncover, but which is not
*because* of that card, may deserve to be tests of the mechanic itself, agnostic of any card, in their own folder
structure as a sibling to the card tests.

**2026-10-04. The old `at` tests.** They were written before the rig (or occasionally stubbornly after it) and are
to be converted: to rig tests in the appropriate card's file, as some already have been, or to card-agnostic
feature or mechanic tests.

**The no-effect flag (new engine feature).** "On decisions sent to a player, let's add a flag of 'no effect',
meaning the action is determined to have no effect ahead of time. In addition, for skirmish actions only I want
that flag to be able to detect that it will have no effect on a currently-skirmishing character, for the
extremely common case of 'I used my hobbit pump on a gondor skirmisher'. ... such actions highlight with a
different color, which will permit players to make such actions should they deem it strategically necessary."
The campaign then checks the flag "in cases like Breaching Shot and Trail of Savagery, where we had for the
player's convenience disallowed the card use in some legal-but-pointless situations. I don't want us to test
literally every card for this flag, just some mechanic tests on common use cases and the occasional conversion
of convenience guards." It does not replace the Q-0034 rule: an optional action with no cost that would do
nothing is still withheld; an event, or an action with a cost, that would do nothing is offered and flagged. *[2026-10-06, evening]*

**The no-effect flag, what it covers (Q-0073 follow-up).** The no-effect
flag: abilities that play a card from a pile stay withheld when nothing can be played; a skirmish action is
flagged for missing the skirmish only when it is a pump or a cancel that reaches nobody fighting ("If it actually
results in something hitting the table, it's fine to leave noeffect off of it"); auto-pass is left alone for now. *[2026-10-06, evening]*

**What gets tested (2026-10-11, first notes).** Q-0241, the four cards `adopt` invented for the kinds of effect no
printed card shows without a hand-written requirement: "This needs reworked. I'm not opposed to inventing new
cards when testing mechanics or engine code, but I do object to adding them as cards with IDs. The original
Hinder at tests established the way to do this: staple the ability you want to see onto a card with no usual
text (frodo or a minion depending on side) and invoke it that way. This establishes the artificial test nature of
the card while also including its creation right there, plus does not expose us to potential issues surrounding
test cards infecting the ecosystem. If there are any other tests which have invented their own cards, please set
up a task to convert them to test frankensteins." So: no invented card with an id of its own (none in any
definition file, of the cards or of the tests); where a mechanics or engine test needs an ability no printed card
shows, the ability is stapled, in the test, onto a card with no usual text. Q-0239, no third copy of a two-dot
title in play in a card test: "A. Yeah, the standing idea is "no cheating in more copies of a card than their
uniqueness level allows" for that." (A: the three relaid tests are kept as built.) Q-0237, no hindered candidate
and no second block's path owed on a Decipher card whose text names neither: "Keep both for sure. That is already
covered by the generic hinder tests, and such classes of mechanical interaction can stand to keep to mechanical
tests instead of incessantly checking them on all cards." Q-0236, The White Council 101_18's two tests laying
nine tables, one Saruman to a table: "Are we testing White Council or are we testing nine Sarumen? In the case of
a modifier, we only need to ensure that the *outgoing* mechanism has been enabled, and it's up to each of those
individual cards to check their side responds to the *incoming* mechanism. For White Council, that means we check
*one* of them, which then ensures that White Council has done its due diligence. So a C with just one Saruman."
So a card that switches something on other cards is tested with ONE card that answers the switch; each card that
answers it shows, in its own tests, that it does. *[2026-10-11]*

**The campaign tests cards, not the definition language (2026-10-11, second and third notes; Q-0241 carried
further).** "I think the main thing we want to worry about with this campaign is the testing of *cards*. The
HJSON definition spec, while clearly related to cards, isn't really what we're trying to exhaustively test, and
it's in flux to serve the cards and the rules." Shown what the 30 invented cards of the tests' own definitions
file were for:
"the only ones that make sense to me are the ones which are testing guards for things that shouldn't happen--not
being able to discard the One Ring, not being able to hinder Maps, etc. Obviously no card *should* exist that
does those things, so we have to invent one, but that is the exact situation the ad-hoc actions can handle just
fine, and what they're built for. Definitions that have no cards have a real hard time justifying themselves;
they might be obsolete due to refactors, they might be preemptive, or they might be intentionally abandoned. In
any of those situations, we don't want to *keep testing* that they work. Definitions that have limited use case
are a little more understandable, we don't want to leave the implementation half-baked, but like....why is
Shrouded Elf also testing beneath the deck? Does any card do that? Then...why are we testing it? We've got 3,000
cards that exist, that's a big enough burden without adding cards that *don't* exist. Remember, we're trying to
make the cards robust, not *necessarily* the design language they're built in." So: a test shows what a card
that exists does. A guard for a thing no card should be able to do is laid with the rig's ad-hoc staplers
(`AdHocEffects`) on a text-less card. A definition word no card uses is not tested; a word one card uses is
tested as far as that card goes and no further. No new rig method for stapling definition text. Engine pass 27
(`guards`, `nocards`) carries it out. *[2026-10-11]*

**What a card's tests may take as proven (2026-10-11, fifth notes).** Q-0261, the generated Play test: "Keep. I
might even want to go one further; I do not think it is necessary to, for example, test that every single minion
removes twilight from the pool based on its twilight cost when played. I would prefer we have mechanical
"TwilightPool" tests that go over the nuances of the mechanic, which exhaustively confirms that if a Shadow card
of any type has a twilight cost of X, X is going to be removed (roaming handled here too), and establishes that
essentially proven function. That way, it is acceptable on all cards to just check that their twilight cost is
set to the appropriate amount (as the StatsAndKeywords check already does), and further testing does not need to
re-litigate proven functionality (that the number translates to twilight tokens). I would also say that this
should be done for things like stats and such. We aren't, for example, testing that every character is "able to
be assigned to a skirmish", because that is proven basic functionality and we don't need to get obsessive that a
character with no assignment-affecting text has interfered with this. Perhaps this warrants another pass,
something that takes a good look at the StatsAndKeywords tests and extrapolates what mechanical tests need to
exist for us to *trust* that StatsAndKeywords are all we need to check for the associated elements. We can then
strip out tests that do nothing except re-litigate what StatsAndKeywords already checked for us." The pass is
queued as a reading task first (what mechanics classes are owed, which tests would go, how many), put to her
before anything is stripped. Q-0260, a mechanics test removed because no printed card reaches its branch: "Keep,
well done. Yeah sometimes we're just not going to have some branches tested, and that's okay (for now; we have so
many extent branches that are higher priority right now)". *[2026-10-11]*

**The stats line is trusted, and the tests are trimmed to it (2026-10-12; Q-0279, engine pass 28's report
`orchestrator/trials/trust.md`).** "Excellent work. Yes on A-D (tho on A: there is a Shadow follower, only one; a
Saruman. On D: also site bonus reaching the bearer) I think I want E2. Were this a new initiative or a new app I
would think differently, but the sorts of things that these tests are hitting are among the most frequently
tested areas of the game, so I'm not worried that it removes a safety net we need. The thing I do want to improve
in this case is how tightly packed our tests are; 600 tests which are doing more than they should (or even
existing when they shouldn't) is some bloat I would like to trim; we're already likely to hit 20,000 tests by
the end with perfectly legitimate use cases, and I don't want to inflate that number with pointless tests." So:
A, `mechanics/costs/TwilightPoolTests` and `WhereACardIsPlayedTests` are written; B, the Play clause is stripped (a
card's play is tested only where its own text bears on it) and the generator stops; C, lint's `plain-play` asks
for a test where a definition carries something about its own play; D, the classes for strength, borne items
(and a site's bonus reaching the bearer), resistance and an ally's home; E2, the readings of a printed cost or
bonus on the way to something else come OUT of the tests that stay. Q-0264, a card that bars an act (No Business
of Ours 2_44): "I don't think that a card should necessarily take upon itself the burden of checking that every
possible engine route follows its own bar, but I *do* think that mechanic tests should exist that do such a
thing. I would say B2: one of each is a great idea, but they should be in mechanics tests, which will then ensure
that future cards using the bar only need to act as A." (the barring card is tested with ONE card that is
stopped; a mechanics class holds one card of each definition word that performs the act). Q-0265, an exertion as
the nearest miss of "is about to take a wound": "A" (shown once in a mechanics class, not card by card). Q-0266,
a choice carried out with no question because one card is valid: "Yeah I think B is the way to go." (a choice
always gets a second valid candidate and a read "not offered" on the invalid one). Q-0271, the account-name
check with no game test: "Yeah do C" (ONE card-agnostic test, the requirement built in the test from its
definition text and asked directly at a table seated under made-up names: an exception to "no definition text
made up for a test", for this check, on the strength of Q-0190). Q-0272 (Test Banner's readings moved onto
Úlairë Nelya 103_76): "Keep". Q-0273 (two classes that read shapes no card has deleted, three loader guards
kept): "Keep". Q-0274, the definition words no printed card uses: "A, we'll keep all these. Many of them are
counterparts to existing mechanics, and since they swim in the same waters, future cards playing in the same
design space are likely to run into them. And some of them are for scenarios, as you say." (kept in the engine,
untested). Q-0263 (a mechanics test on Slippery as Fishes 9_29 for an empty hand revealed): "this is keep."
Q-0268 (the return-to-hand effect refusing the Ring-bearer for itself): "Keep". Q-0277 (an exhaustion paid as a
cost says so, as an exertion does): "Exhaust is defined as "exert that character as many times as you can", so
the two are linked. Keep." F-0484, the rig's cheat for a site crashing on Mount Doom 15_193: "Good fix to
perform; the cheat functions should really be as legitimate as possible, timing aside." *[2026-10-12]*

## How card text is read

**2026-10-05. Cards are code (Q-0012).** "In general I hold to the philosophy that cards are code: the text should
do *what it says*, not *what the coder meant*; if the author meant something else they should have *written*
something else." Library of Rivendell (101_11) says "your tale or artifact" with no side, so a Shadow artifact in
hand may be stacked: "This was unintended, but it's legal 'code', so it ships as-is. If someone doesn't like it,
we'll issue a formal errata." She adds that this "does sometimes conflict with the QOL ruling, and I admit it's a
bit subjective where and when it gets applied." So: a literal reading the game already takes is not a finding,
however odd; a quality-of-life shortcut that departs from the text is still a question.

**2026-10-05. Followers are not characters (F-0023).** A filter that goes by name alone where the text says
"these characters" is to have "character" added.

**2026-10-05. Race and culture are different filters (F-0019).** "Bearer must be a unique [elven] companion"
filtered by the race Elf is "an implementation error ... moot in gameplay today, but someday a non-[Elven] Elf
could be printed or vice versa." To be fixed where found.

**2026-10-05. "Can't" beats "can" (Q-0011).** "This falls under our modern 'can't > can', and so 'lose > gain'."
A character who has lost a keyword until some time cannot be given it again by another card before then.

**An effect with no stated duration lasts to the end of the current phase (Q-0018).** On Boromir, Redeemed
(101_20): "Since it does not specify a duration, it defaults to 'the end of the current phase', which is long
enough to assign but not long enough to e.g. use Swordarm of the White Tower on him in the Skirmish phase."
This holds for a trigger's effect as for a phase action's. The definition's "until the regroup phase" was left
over from an earlier revision and has been changed. *[2026-10-05, afternoon]*

**"Here" on a site is the site's title (Q-0027).** "Yes, if the Farmer is out on site 1, you should get double
the benefit, but only on site 1. 'here' is supposed to be a synonym for '[title]'." An ally who has a site's
game text (Hobbit Farmer, Uruviel, Silinde) therefore makes the site's "here" offer too, while the fellowship
is at that site. Bag End Study (101_57) was right; Lorien Throne Room (101_61) required the site itself and is
corrected to go by its name (F-0043). *[2026-10-05, evening]*

**"One" stands for the noun before it (Q-0028).** On Rivendell Gateway (101_59), "the intent is (when game text
is at such a premium as it is for sites) 'one' is a stand-in for 'an unbound companion'." *[2026-10-05, evening]*

**"Comes into play exhausted" is a state it arrives in, not exertions (Q-0302).** "I'd say that "coming into play
exhausted" pre-empts the actual play trigger, so it should occur before any exertion responses or whatnot can occur.
Keep." The wounds are on the character as it is placed in play, before anything answers its play, and they are no
exertions: no "each time ... exerts" card answers them (engine pass 37, `exhausted: true` on the play effects).
*[2026-10-14]*

**A branch in parentheses is mandatory when it is available (F-0047, Q-0035).** "When there are parenthesis like
this, it's actually mandatory to use the parenthesis over the portion outside the parenthesis, so the player
doesn't have a choice", and "Decipher ruled that a branch in parenthesis is mandatory to choose if it's
available." "Exert a valiant companion (or spot 2 valiant Elves)": with two valiant Elves to spot the game spots
and never offers the exertion, and is right. "Wound a minion (or wound a minion twice if each of those Elves
bears a ranged weapon)": if two valiant Elves bearing ranged weapons can be spotted the minion is wounded twice;
the player is not asked which Elves and cannot choose the lesser result. *[2026-10-06]*

**A parenthesis that adds targets of either side is "up to" (F-0491, Secret Sentinels 2_20).** "Exert an Elf ally
to discard a condition (or 2 conditions if you spot an Orc)": "This one needs to be optional because A: it says
"you spot" not "you can spot", and B: it's keyed to "condition" and not "Shadow condition" meaning you can be
forced to discard your own condition. In effect, treat this one as 1 mandatory discard and then a 0-1 discard if
an Orc can be spotted. (We won't waste their time actually choosing one to spot)" So nothing is asked about the
spot; with an Orc to spot the player discards 1 or 2 conditions, his choice; without, exactly 1. Q-0035 stands
where the better branch can only help him (a minion wounded twice). *[2026-10-13]*

**"Spot X to Y" does nothing without X, even as one branch of a choice (F-0053).** "When you're given a choice,
you have to pick one that you're 'fully capable' of performing. But when an effect cannot be fully performed,
you 'do what you can and ignore the rest'. That can lead to some unexpected edge cases. That said, 'spot X to Y'
should clearly not resolve if X isn't around, even if it's one branch of a choice. This is clearly an engine bug." *[2026-10-06]*

**What a special ability is (Q-0039).** "It would count, if it's got the bold text and a colon." A labelled
ability ("Shadow: ...") used from the discard pile or while stacked is a minion's special ability and Gandalf,
Lathspell answers it; the exertion of a minion that is not in play does nothing. A permission with no phase word
("you may play this minion from your discard pile") is not a special ability and is not answered. *[2026-10-06]*

**Reading card text.** A race word with no "your" names no side: "Wraith is Wraith" (Q-0068), "Bearer must be a
Wizard" lets the opponent's Saruman bear Wizard's Stash (Q-0058). A sentence of costs with no "to" is still
costs where the next sentence is what they buy (Q-0058). An X of exertions is the player's, up to those able to
exert, whatever it can buy (Q-0069). A companion spotted as part of a cost is still "that companion" when the
effect is carried out, though paying the cost ended the skirmish (Q-0065). "Play this" on a card in play is a
transfer, from the discard pile a play (Q-0057). An exact list of sources ("from play or from hand") leaves the
others out (Q-0047). A required trigger's spot is made whenever it can be (Q-0050). "Prevent X from being
overwhelmed unless its strength is tripled" on a minion spares only that minion, by its own strength; overwhelm
is resolved minion by minion (Q-0044). *[2026-10-06, evening]*

**X of exertions is at least 1 (Q-0076).** "Exert X <cards>" is an exertion cost and X is at least 1, as with "exert X times" (Q-0076, A Wizard Knows
Better 103_40: without Saruman no companion is hindered for no exertion). X of zero by SPOTTING is still
allowed. This narrows "X may be zero" of 2026-10-05: any X of exertions is at least 1. *[late on 2026-10-06]*

**A card that makes itself a minion offers that from the support area only (Q-0077).** A card that turns itself into a minion from the support area offers that ability from the support area only
(Q-0077, the Bladetusks 103_41, 103_42, 103_43): never again while it is that minion. The requirement in
their definitions is right and is not a convenience guard to convert. *[late on 2026-10-06]*

**A sentence after such an ability that calls the card by the minion's pronoun is part of it (Q-0078).** A sentence after such an ability that calls the card by the minion's pronoun is part of the ability (Q-0078,
Bladetusk Matriarch: "She adds 1 to the Shadow archery total" holds only while she is the minion). *[late on 2026-10-06]*

**"Its skirmish" on a minion is the skirmish it is in (F-0114).** "Its skirmish" on a minion means the skirmish that minion is in: "While merely assigned to a skirmish, it is
not in an active skirmish and so that text should not be active" (F-0114, Corsair Quartermaster 103_45). A
modifier that counts over `InSkirmish` needs `self,InSkirmish` as its own filter. *[late on 2026-10-06]*

**"By another card" is a card other than THAT card (Q-0079).** "By another card" in a card's own text is a card other than THAT card, as "another X" is everywhere else
(Q-0079, Corsair Helmsman 103_46: "The wording is meant to prevent Helmsmen from responding to itself
hindering itself"). A [raider] minion that hinders itself when played is answered by a Helmsman. *[2026-10-07]*

**A spot after "to" is an effect even when nothing can be spotted** (Q-0085, Cover of Darkness, Omen of Fear
103_64): "As written I think it is A ... I can clearly see why one would want to do it without an unbound
companion, to halt a ring-bound fellowship from moving. Let's say A." *[2026-10-07, later]*

**"Remove X threats to ... a companion costing X or less" has no cap but the threats** (Q-0087, Gollum
103_66): "if the player has a reason to remove more threats (against Gondor Wraiths or something), then I
think they may." *[2026-10-07, later]*

**"The <other> player may make you X ... to prevent this": "you" chooses** (Q-0088, Ill Wind 103_67): "it
was only supposed to change the exert to a wound, not alter who selects it." *[2026-10-07, later]*

**A parenthesis with its own price the player elects to pay is the player's choice** (Q-0089, Ulaire Nertea
103_77): the wording is mandatory as printed and will be changed; "keep A". Do not ask about this shape. *[2026-10-07, later]*

**"A" is one** (F-0136, This is My Hour 103_71): "'a' in English is a synonym for 'one'; it should be
restricted to a single companion and not every companion ... change it to 'choose' instead of 'all'." *[2026-10-07, later]*

**"The second time ... each Shadow phase" means the Shadow phase** (F-0130): a phase requirement. *[2026-10-07, later]*

**"May X, and must Y for each X" is not a price** (Q-0091, The Witch-king 103_80): "worded carefully to
permit such a result, that's working as intended." *[2026-10-07, night]*

**"Copies of that card" counts the card itself and goes by title** (Q-0092, Death Take Us All 103_82). *[2026-10-07, night]*

**"A minion" on a Skirmish event that only reaches the skirmish becomes "a skirmishing minion" in the text**
(Q-0095, I Am No Man 103_84), as Q-0051 did for 102_66. *[2026-10-07, night]*

**"Is considered a [culture] <race>" adds the race** (Merry 103_85): "an addition and not a rewrite". *[2026-10-07, night]*

**Race is excepted from "unloaded keywords"** on Flame of Calenhad too (Q-0097): text gains "(except race)". *[2026-10-07, night]*

**Later still (Ketura, 2026-10-06).** Q-0071 Pippin, Trollslayer 103_31: "race *is* an unloaded keyword ... treat it
as if the text says '(except Race)' at the appropriate spot, updating the text as well." Q-0072 Tormented Revenant
103_32: "spot or add 2 threats" is the player's choice when both can be done. *[2026-10-06, evening]*

**"Resolve a skirmish involving a [rohan] Man" resolves only such a skirmish** (F-0143, Merry 103_85). *[2026-10-07, night]*

**The general shape:** "you may make <the other player> do X" is asked of "you" as a yes/no, only when X
can be done; the choices the text gives the other player stay his. A definition that never asks is an
error to fix, not a question. *[2026-10-07, late]*

**Q-0113 (F-0197), Ancient Enmity 51_29 and the original 1_29, two minions losing one skirmish to the Elf:**
"Let's interpret the If as evaluated once as a one-time trigger as if it said 'At the end of this skirmish, if
at least 1 minion lost this skirmish to that Elf...', essentially the way that Decipher *wanted* Dwarven Axe to
work. So A for both errata and original." "If a <character> loses/wins this skirmish ..." is one effect, once,
however many lose. *[2026-10-08]*

**2026-10-05. X may be zero (101_5 One Dwarf in Moria).** "X may indeed be zero. The only time that X cannot be
zero is in the case of 'exerting X times', which has an explicit ruling, but every other case has no such guard
rail." Bears on: every card that says "X" of anything but exertions.

**X counts what was actually returned (F-0016).** One Dwarf in Moria (101_5) "should only count cards
successfully returned to their owner's hands, not ones discarded after the fact, even if they were slated for
return." The definition now remembers the items chosen and wounds one minion for each. *[2026-10-05, evening]*

**"Wound X twice" is two wounds** (F-0148, Take it Down! 103_89): "that's a bug in the implementation, go
ahead and fix." An exertion in the definition where the text wounds is a definition fault, fixable at
landing. *[2026-10-07, afternoon]*

**"Choose X ... (or you may play it ...)" plays the card chosen** (F-0152, Ominous Sky 103_97): "That's a bug,
yeah, fix it." *[2026-10-07, later afternoon]*

**Texts (2026-10-11, fifth notes).** F-0424, Axe of Erebor 4_41 and More to My Liking 5_9, "a card stacked on a
[dwarven] condition": "Bleh. Decipher really needed to tighten up their definitions of when stacked cards could
be interacted with and they never did. Let's do B for now." (only a card stacked on a [dwarven] condition of your
own, which is active). F-0468, a reinforcement on another player's card: "The full `reinforce` entry has a
contradiction in it: "This verb, used in game text, means to add a culture token to a card that already has a
token of the same culture. You may choose any of your cards with the appropriate culture token if you have more
than one." The first sentence reads as if there is no restriction on what side you choose, the second sentence
implies you are restricted to only your own. I'm going to put a rules council issue in for this, but for now
let's revert this and permit Morannon Plains to let either player choose either player's cards; there are some
cards whose tokens "count up" and then self-discard upon accumulation (such as Scouring of the Shire), which is
why someone might want the option to reinforce an opponent's card." (reverted; not a bug until the council
says). F-0430, Old Differences 2_69: "read the text again: "(or -3 if you spot an Elf and a Dwarf)". That is
purely about seeing both on the table, i.e. both are "working together" and the event is therefore finding more
ways for minions to exploit racial and cultural tension. Revert this, please." The correction had left that half alone (it is still a spot of both anywhere on the table) and
changed only who may be CHOSEN ("an Elf or Dwarf skirmishing a [moria] Orc", where any Elf or Dwarf in play could
be); told so, she kept it: "My apologies, I completely misread what F-0430 was about ... Yes, that is a good solid
bug fix, keep the solution." (F-0430 stands, approved.) *[2026-10-11]*

**Texts (2026-10-12).** Q-0276, Sentry Uruk 15_170 ("While you can spot a fierce minion, this minion is fierce"):
"This one actually came up on mlotro a while ago and I thought through it then: it has to be A, because without
'another' in the spot filter text, once it has bootstrapped itself it must continue spotting itself. Undoing
that would mean altering the text from what was written (which i just said to do on Mountain-troll lmao, but in
this case it's not quite as clear what was meant in any case). A." (it spots itself and keeps each keyword once
gained). Q-0275, Mountain-troll 15_112's "to make it twilight cost -10 and fierce" naming no time: "of course it
doesn't say. The intent is obvious in this case, as Fierce does very little in the Shadow phase; Decipher had a
habit of accidentally leaving it off. We'll be gracious this once and let it be until the end of the turn like
the errata is; A." Q-0282, Into the Caves 17_100 ("lose all game text keywords of your choice and unable to gain
game text keywords"): "Sets 17 and 18 were built in a CAVE with a box of SCRAPS by a single lone employee as
Decipher imploded around his ears. The templating is inconsistent at the best of times. Let's do A." ("of your
choice" belongs to the keywords lost; the minion can gain NO game text keyword until the regroup phase).
Q-0267, The Witch-king's Beast 12_184 under a bar on gaining keywords: "Ah shit, I didn't think about keyword
effects when consolidating the beast's effect. Go ahead and revert it to the old effect; A." (the additional
assignment and skirmish phase is his whatever bars keywords). F-0480, Treebeard, Enraged Shepherd 15_38 when the
losing character has left play: "Oof. let's do A I suppose." (X is taken from the strength the loser had when
the skirmish resolved). Q-0269, the same card beating two minions: "A" (their strength together). Q-0270,
Madril 15_64: "A" (the threats are counted once, at the start of the maneuver phase). Q-0280, the F-0468 revert
reaching the four cards that reinforce a [gollum] token: "A, yeah. Gollum is just weird like that, and it's a
weirdness that many of its cards lean into, so I'm fine with it having such unorthodox interactions." *[2026-10-12]*

## What is offered and what is asked

**X of zero that would do nothing is not offered (Q-0015).** A deliberate departure for quality of life: "we
don't offer to 'discard 0 cards to make a user strength +0'. Besides being strategically useless and
distracting the player with options that don't actually do anything for them, this is a potential griefing
angle if the player can click on the same pointless action over and over and time out the game without getting
kicked on the decision timer." So I Would Have Gone With You to the End (101_23), Breaching Shot (102_21) and
Northern Signal-fire (103_30) are right to start X at 1. X may still be zero where the action does something
without it (One Dwarf in Moria, 101_5, heals whatever X is). A definition that offers a do-nothing X of zero is
not thereby wrong; one that withholds it is not a finding. *[2026-10-05, afternoon]*
Since Q-0034 (2026-10-06, below: "An optional action that would do nothing is not offered: now a rule") that
last sentence holds only for an event's X. On a special ability or optional trigger with nothing else to pay, an
X of zero that would do nothing is to be withheld, and a definition that offers it is a finding (F-0067, From
Deepest Dungeon 102_39). Read that entry before resting anything on this one.

**No "may", no asking (Q-0017).** Boromir, Redeemed's defender +1 "should definitely be an automatic required
trigger; the optional bit is indeed a holdover from the prior version." Changed to `optional: false`. *[2026-10-05, afternoon]*

**"You may" is asked (Q-0021).** Their Eyes Fell Upon Him (101_41) drawing without asking "wasn't intentional,
that's an oversight." Changed to an optional trigger. 13_75, built the same way, is for its own
phase (F-0041). (68_50 was counted with it by mistake: its released text has no "you may", F-0297.) *[2026-10-05, evening]*

**"May draw a card for each" is one offer (Q-0029).** "It's better UX for it to be one big batch or not. The
vast, vast majority of the time, the player will want to draw, and they will not be amused needing to hit yes
for every individual card draw." And with an empty draw deck: "while it is technically a pester to ask them when
they have 0 cards in their deck, that is both rare and a bit of a chance for them to *realize* they have no cards
in their deck ... I'd say let it ask with 0 cards in deck." She spoke of the empty deck only. Whether the offer
should be made when the count itself is zero (four companions or fewer) was Q-0033, ruled below. *[2026-10-05, evening]*

**A draw of nothing because the count is zero is not offered (Q-0033).** "If there's 4 or fewer companions,
there's no reason to nag the player with 0 draws, yeah. Let's make it smarter." Nen Hithoel (101_63), Orthanc
Library (4_362) and Summit of Amon Hen (1_362) now require something to count before the Shadow player is asked.
The empty-deck offer of Q-0029 stays. Other "may draw a card for each X" cards are to be read the same way as
they come up: an offer with X at zero is a finding, kind hjson-logic, already ruled. *[2026-10-05, night]*

**2026-10-05. A parenthesis that is a separate may (Q-0010).** On Three for the Elven-Kings (101_12) the heal in
parentheses "should be a separate may (definitely a bit convoluted)", and "implemented as its own 0-1 choice that
is either a click on a companion or clicking 'pass/done', not a 'do you want to heal' pester prompt."

**An optional action that would do nothing is not offered: now a rule, with two exceptions (Q-0034).** "Sure,
start treating it as a rule, except in these cases: Events. People often want to play dud events to get them
out of their hand. Situations with a cost. Oftentimes the cost may incidentally be beneficial even if the effect
fizzles, such as exerting an enduring character." So: an optional trigger or action with no cost whose whole
effect would do nothing (a draw of zero, an X of zero) is rightly withheld, and offering it is a finding. An
event is offered whenever its own requirements and costs are met, whatever its effect will do. An optional
action or trigger that has a cost ("you may exert X to ...") is offered whenever the cost can be paid, even if
the effect will do nothing; a definition that adds a requirement to withhold it is a finding (Naith Armaments
102_10 had one). Do not ask about these shapes again. *[2026-10-06]*

**Fewer questions** (Q-0090): where taking an offer leaves one thing to do, do it without asking again. *[2026-10-07, later]*

**A choice with nothing on any branch is not asked (F-0116).** A choice whose every branch has nothing left to act on is not put to the player: "the prompt should be
smarter" (F-0116, Savage Fury 103_39). *[late on 2026-10-06]*

**Desolation of Ash 103_122 keeps its guard** (Q-0107, against the general rule of Q-0034): "Shadow almost never
wants to waste twilight like that and it would be a pester prompt showing 5 times or more per turn for no
reason." A guard of this kind on a card that would otherwise ask at every phase may stay; ask about others. *[2026-10-07, night]*

**An optional look through an EMPTY draw deck is still offered** (Q-0104, Hornblower Barrel 103_112): "the draw
deck emptying entirely is a rare event, and having that underlined by the ask still happening and showing
nothing would help them realize it." *[2026-10-07, night]*

**Q-0114 (F-0194), prevention clauses in general:** "In general, prevention clauses should only trigger an
ask if the thing they are preventing actually occurred, so both lack of cards to draw, prevented draws, and
rule of 4 should prevent the asking of the prevention clause. So A, yeah." A "<player> may <pay> to prevent
this" is put to that player only when the effect it would prevent is going to happen: not for a draw from
an empty deck, a draw past the Rule of 4, or a draw something else already prevents. This is one rule for
every such clause, with the earlier "to prevent NONE" cases (F-0051, F-0183, F-0185, F-0192) under it. *[2026-10-07, late]*

**2026-10-04. Goblin Runner's "may" (F-0009).** Technically incorrect and deliberate: "it is *absolutely* a QOL
thing to not make everyone click on Runner every time they play it." The twilight is added without asking.
Rule this kind of thing a quality-of-life implementation, not a bug.

**2026-10-04. Ambush instances (F-0010).** Also technically incorrect and deliberate: making the player accept
each constituent ambush trigger one by one would be very poor to play, "nobody is ever going to want two of
them but not three", and a blind eye was turned. Separate instances of ambush are offered as one trigger for
their sum.

**2026-10-05. Ambush instances and Desert Wind Whisper (Q-0007).** "This is a case where I don't think it matters
if they're combined or not. A minion with 'ambush (2)' and 'ambush (1)' would get +2 strength and +1 strength, I
think. So the QOL combination doesn't affect the end result."

**An ability at its limit is offered, flagged no-effect (Q-0041).** "Technically speaking it should be offered,
and some cards (Beneath the Mountains) are famous for using the cost over and over in spite of the effect limit
being reached." The definitions that withhold an ability at its limit are converted when the flag exists. *[2026-10-06, evening]*

**"Exert X twice"** should offer only those able to (F-0131): "It would be nice if [a vitality filter on
each card] wasn't necessary all the time." Fixed in the engine, not card by card. *[2026-10-07, later]*

**Worth Fighting For 103_120's "you may make the Shadow player shuffle" is to be asked** (Q-0106): "an oversight
in hindsight. A, but have it be a 0-1 choice instead of a 'do you want to' prompt." Whose choice the 0-to-1
is has been asked back; not built yet. *[2026-10-07, night]*

**Q-0106, Worth Fighting For 103_120, "you may make the Shadow player shuffle one of their hindered cards":**
"Oh I forgot shadow makes the choice for WFF. Never mind then, ask freeps and make shadow choose as it
should." This withdraws the 0-to-1 choice she first asked for: the Free Peoples player gets a yes/no, and
the Shadow player then picks the card. The yes/no is asked only when the Shadow player has a hindered card
(no pester prompt, as with Q-0107). Built as F-0180. *[2026-10-07, late]*

**What is offered and what is asked (2026-10-11, first notes).** F-0385, Valley of the Silverlode 2_120 ("each
Hobbit companion may heal"), an optional trigger whose choice may then be answered with nobody: NOT A BUG, A:
"There are rules surrounding optional/required actions as you point out, so I think we are required to keep this
an optional trigger. In this case there is no downside to healing, but if we're following a general rule I would
rather we click on the card to trigger it (such actions don't use popups) and then have the option to select 0-N,
if one were to change one's mind (for example, the Longbottom Leaf we may click the card for the trigger, and
only after thinking about it decide that none of the cards are good discard candidates and choose 0). So this one
is A." So an optional trigger whose EFFECT is a choice of "up to N" or "each may" keeps both: the offer, and a
choice from 0 (Longbottom Leaf 1_300 and Uruk-hai Healer 14_14 stay as built). The orchestrator's reading, told
to her: this does not reach a choice that is a COST (Robin Smallburrow 68_111, F-0300: 1 to 3, by Q-0015 as
amended by Q-0034), nor a text that says "a" and not "up to" (Stewards' Legacy 13_75, F-0041: exactly one).
Q-0243, Hatred Stirred 11_210 ("you may shuffle a [wraith] card from your discard pile into your draw deck") with
no [wraith] card there: "Go for B." (the trigger requires the card, as Q-0112 did for Mûmakil Commander; Q-0102
stays for "up to N"). F-0419, Shelob, Menace 18_34 ("you may add threats up to the number of burdens you can
spot") adding one for each burden unasked: "as written, it should offer a choice, 0-N, ideally with a prompt
defaulting to N." F-0420, Mallorn-trees 1_54 offered with no [elven] card in hand: "Yeah that looks like a
reasonable fix". Q-0240, Resistance Becomes Unbearable 2_79 with a Ring-bearer already exhausted: "A" (as built:
"is then exhausted" reads his state once the event has done what it can; he puts on the Ring). *[2026-10-11]*

**What is offered and what is asked (2026-10-11, fifth notes).** Q-0245, Band of Wild Men 4_4's "make it fierce"
offered again at a second win: "The fact that the fierce skirmish is already over is a bit beyond what we should
consider for whether or not it's offered; various effects care about whether a minion is fierce or not, and thus
the effect still has potential to alter that calculus, even if the primary loaded behavior won't. A." (offered at
every win; a keyword given to a card that has it is not "nothing" for Q-0034). Q-0248, Aragorn, Wingfoot 4_364
("you may wound a minion for each unbound Hobbit you spot"): "Ask, but instead of as a popup, as a 0-N choice of
cards to wound. Wingfoot still has to be clicked to indicate the optional trigger is being used, but then it
skips straight to the wounding." So the player still decides how many, and the number is not asked for by a
number prompt where the effect's own choice of cards can carry it; Q-0141's "spots all without asking" is for the
erratum Merry's Sword 57_242 alone. Q-0249, Barrows of Edoras 4_337 offered to a player with no hand: "Keep. I
agree that there should be a requirement that checks to see if anyone is able to draw; this is relevant for Rule
of 4 shenanigans but there's also other restrictions." Q-0255, Pelennor Flat 7_345 with no card in hand: "I'd say
this is a difference between zero and null; is discarding an empty hand an action of "discarding 0 cards" or a
NPE when trying to locate a hand that doesn't exist? I think the player always has a hand, even if empty. I'm
reminded of the ruling on What Are We Waiting For, which permits Shadow to re-pick skipping the phase even when
earlier copies had them picking that already. I think discarding 0 cards is kosher. B." (as built: a hand of no
cards may be discarded, for nothing). Q-0257, Crashed Gate 8_119 with no site to take: "The question is over
whether the Freeps choice is "choose an opponent" or "choose an opponent and make them control a site". The "may"
part is what tips this over for me in the direction of the first; if the opponent chooses not to take it is the
card retroactively made an invalid choice? So I think A is the right answer." (as built). Q-0244, a follower's
Aid flagged no-effect whenever nobody can be given the follower: "keep". Q-0246, Saruman's Staff 4_174 not
offered on the two Sarumans of Legacy Ruling 6: "Keep. This is as good as any mechanism for communicating the
results of that ruling." *[2026-10-11]*

**What is offered and what is asked (2026-10-12).** Q-0262, "you may draw a card" once the Rule of 4 is spent
(Old Toby 1_305): "A is more likely to cause questions; lots of people get confused over Rule of 4 stuff (even
with the addition of UI explaining it). Let's do B for this one." (not offered; Q-0029's "let it ask" is for the
empty deck). Q-0278, A New Light 17_15 ("you may reveal that Shadow player's hand and discard a minion found
there"): "A, but it should be a 0-1 choice so they could decide to not discard (I think the two "mays" are
separate, plus there are cases where not discarding is preferred strategically)." (asked whether to reveal;
then a choice of no minion or one). Q-0284, The Faithful Stone 18_50: "B" (the minion is spotted first and the
Shadow player answers knowing which, as the erratum 68_50 has it). Q-0281, Light the Beacons 7_43 not counting
itself while its cost is judged or paid: "A is correct". Q-0283, Morgul Regiment 7_197 and how its cost is
asked for: "Can we make this be an optional trigger that happens when Freeps starts assignments? Ideally
something that makes Regiment highlight itself for Freeps to click on or pass (so that 2 Regiments in play are
both highlighted and both clickable in any order, where I might click on the first, then select my exerters,
then be presented again with the pending second for pass or selection (if vitality permits)). Then if any
Regiments aren't selected, they are barred from normal assignments. This solves the UX of not knowing which one
is asking and also manages the timing a little better rather than waiting for a cost-inspection pileup when all
assignments are submitted. Keep Him Blind To All Else That Moves does something similar but with special
abilities assigned to the other player." (a change to the build of Q-0221 for his OWN assigning.) Asked which cards and what a card that has
him assign does: "1- Yeah they call all get it, I think it's better UX. 2- ... I guess maybe extraCostToAssign and
this trigger would need to be able to affect the same self-flag, indicating that it has been paid for? So that
assignment actions trip the extra cost, set the flag, and then the at-assignment trigger also asks and
potentially sets the flag, without the two tripping on each other. A pair of results hitting the same inner
fields." (all four charging minions; one mark of "paid for" on the minion, set by either way and asked by both;
engine pass 30, `regiment`, has the shape.) *[2026-10-12]*

**What is offered and what is asked (2026-10-12, fifth notes).** Q-0286, the bar on drawing (Race Text 94_30)
shown by two tests of its own beside the Rule of 4 boards (`CanDraw`; Barrows of Edoras 4_337, Old Toby 1_305):
"Keep". *[2026-10-12]*

## Limits

**"Limit once" is not "limit +N"** (Q-0082, They Are Not All Accounted For 103_103): "this should be a limit
on *triggering* once per turn. I think there's a nuance between 'limit +3' and 'limit once', with the former
being a cumulative addition of multiple invocations, and the latter being a limit on the number of times the
ability can invoke at all." 103_103 stays withheld at its limit. Whether the other "limit once" abilities
that the no-effect work put on offer go back is asked as Q-0093; until answered they stay as built. *[2026-10-07, later]*

**A "+N" limit is the card's for the whole phase**: counted per source card, not per card affected (F-0125:
"supposed to be a limit on the source card, not per-target"), and cumulative over uses, not a cap on each
use (F-0135, Ulaire Nelya 103_76: "the limit should be cumulative"). *[2026-10-07, later]*

**"Limit once per site" is once for each site in a turn** (Q-0084): "the intent is not that you have to
track it across turn boundaries." A use in the fellowship phase must not spend the next site's use (F-0127). *[2026-10-07, later]*

**Limits, settled (Q-0093).** A limit that counts what the effect gives stays on offer at the limit, flagged
`limit`, its cost paid for nothing: "(limit +3)", and also "(limit 1)" where it counts things added ("Dark
Tidings is more like '+1' than 'once'; it is limiting the number of threats that can be added the way that
Scimitar limits the strength bonus"). A limit on invocations ("limit once per turn / per phase") is
withheld at the limit: "the second should halt being offered once the invocation limit is reached." *[2026-10-07, night]*

**Two companions exerting at once draw one card** for a Cirith Ungol Watcher (F-0145, 103_63, 103_92): "if we
have to, we'll set it up as a 'limit once' and 'limit 1 drawn card' haha. But that can be internal, doesn't
need to alter the text. At any rate, CUW should only draw 1 card in that case." *[2026-10-07, early morning]*

**"Limit once per site" gets an engine counter** (F-0146): "it makes sense to add an engine-level handling of
this instead of a hack. All the Decipher once-per-site limits are skirmish effects that have no issues
around boundaries." *[2026-10-07, early morning]*

**Sixth Level 8_44 "(limit 1)" is built as a limit on uses**: "Let's code Sixth Level as 'once' just for UX
sake, but I agree that it's likely a +1 situation." It is withheld at its limit. Scaling Ladder 55_57 and The
White Council 101_18 "(limit 2 per phase)" stay limits on what the effect gives. *[2026-10-07, evening]*

**Morannon 5_102 counts by the turn** (F-0157): "Per turn is the correct interpretation." *[2026-10-07, night]*

**A discard the Free Peoples player chooses is not forced (F-0205).** "So this might need to have those "forced"
fields scrutinized on the various topdeck discard effects. There is a ruling for Courteous Halfling in Section
Three: "This card only prevents Shadow cards that make you discard from hand or draw deck. If a Shadow card gives
you a choice, you may choose to discard", and then gives Dunlending Rampager as an example. Since Rager and many
other errata offer a choice, the discard is not forced; Freeps could instead choose the other bad effect, and so
CH doesn't help them in that case." So `forced: false` is right where the discard is one of the Free Peoples
player's options, and a Shadow card that makes him discard from hand or deck with no choice is `forced: true`. *[2026-10-08, afternoon]*

## Costs, and playing cards from piles and hands

**2026-10-05. Costs and effects around "to" (F-0022).** On Counsel of the Wise (101_13): "Since the text says
'spot Gandalf *to*...' that is the only actual cost of the card; everything following is an effect." A definition
that writes something after the "to" as a cost, so that the card cannot be played when that thing cannot be done,
is wrong.

**Playing a card from the draw deck or discard pile (Q-0019).** "Playing a card from discard as an effect is
always treated as a cost" (the ruling on They Are Coming, "canonized as the standard way of treating it"). There
must be "no penalty" for failing to play from the draw deck, so the same does not hold there, and by the rules
Just a Wisp of Cloud (101_28) could be played at any time. "That said, it's a pretty shit UX to let the player
play when it's not in the draw deck if they thought they had one and did not. I believe the current
implementation threads the needle of UX and rules." So `mustExist: true` on the eight "from your draw deck or
discard pile" definitions (5_30, 7_53, 8_92, 8_118, 11_106, 17_17, 101_28, 101_37) is a deliberate departure:
with nothing playable in either pile the card is not offered, and that is asserted as the game has it. *[2026-10-05, afternoon]*

**Being made to play the card found (Q-0022, rider).** That a "play ... from your draw deck or discard pile"
card with something playable in the deck makes the player play it (the choice has a minimum of 1) "looks like a
bug to me": CR 4.2 "draw deck" lets a player choose not to. It is the engine's `mustExist` that does it
(F-0042). *[2026-10-05, evening]*

**Looking through a draw deck never forces a card to be taken (F-0049, F-0051).** "Those should all be an
effective 0-N choice." Taking into hand, like playing (F-0042), has a minimum of 0 once the deck is being looked
through. *[2026-10-06]*

**Playing from the discard pile as an effect is a cost, the event's own twilight included (F-0046).** "It should
block until both can be paid for; rules state that effects playing from discard pile have to essentially be
treated like costs." *[2026-10-06]*

**Playing a card as an effect, from any pile, is a cost (Q-0062, Q-0066).** From hand: "it should enforce a card
being there and playable before playing"; where an effect plays a card several times, "we should at least
confirm that the first play is possible". The dead pile is "covered for the same reasons that the discard pile
is", and I Call on You "should properly withhold itself if the threat cost of the wraith to be played cannot be
paid". Replacing a character from the discard pile: "treat this as if playing from discard for rules purposes;
if it's not in the discard don't offer it." The replaced minion "simply 'becomes' another persona, without
skipping a beat or interrupting any prior tracked effects"; "another" means another subtitle (Q-0067). *[2026-10-06, evening]*

**"Play any number of / up to N ... from your discard pile" is withheld when nothing there can be played**
(Q-0086): "more consistent with the play-from-discard rules." *[2026-10-07, later]*

**X is capped at what can be played** in "hinder X ... to play X items from your discard pile" (Q-0094,
Gondor Calls For Aid! 103_83): "B, yeah." *[2026-10-07, night]*

**"From your support area" is a requirement where the card says it (Q-0022).** "If the card explicitly states
'from the support area', then it clearly can't bounce." Pale Crown (101_39) does, so its ability is not offered
once the Crown is on a Nazgul. Where a card says only "transfer this" (many fortifications), whether it may be
moved again from its bearer "is completely unknown if that was intended or not" and is case by case: ask, card
by card, and do not report it as a finding. *[2026-10-05, evening]*

**A transfer is not "coming into play" (Q-0053).** "Transferring 'is not playing' and so specifically does not
count as 'coming into play' for the purposes of having the twilight cost altered." Other cards' twilight cost
modifiers do not reach a transfer. "We're going to have to alter Asfaloth as well." *[2026-10-06, evening]*

**The twilight cost of a card in play is the number printed on it (F-0109, Q-0073).** F-0109 and Q-0073, the twilight cost
of a card in play: it is the number printed on the card; neither another card's text nor its own changes it once
it is in play ("For now, we will follow the rules as written, but I'm intending to change that written rule"; "I'll
put it on the docket for us to remove that rule in PC formats, but don't take that into account"). *[2026-10-06, evening]*

**A transfer does not take the card's own cost text into account either (Q-0074).** A transfer does not take the card's own cost text into account either: "Based
on a strict reading of the twilight cost rule, transfers should not take self-discounts into account. It's stupid
and asinine, but it's RAW." *[2026-10-06, evening]*

**Order of costs and effects (Q-0080).** "Costs (and effects for that matter) should always be evaluated in
the order in which they appear." So a cost that puts what a later cost needs out of reach (Raiding Schooner
103_55: hinder another Schooner, then remove 3 tokens, some of them on that Schooner) makes the ability
unpayable, and it is not offered (F-0123). The engine checks each cost against the board as it stands, so
such a card needs a requirement saying what must be left after the earlier cost. *[2026-10-07]*

**Q-0080, as the engine now stands (engine pass 13, F-0184):** exertions and wounds that an action's earlier
costs are certain to place are judged in order by the engine, for later costs and for the card the action
then plays; a definition needs no requirement for those. A cost that puts something else out of a later
cost's reach (a hinder, tokens) still needs the requirement, and a real choice of who exerts is judged on
the board as it stands. *[2026-10-07, late]*

**2026-10-04. Exhausting the exhausted (F-0005).** "Exhausted" is a state in the original game, but "exhaust" as
a verb on Decipher's cards never appears as a cost. The PC has ruled that you may not exhaust an exhausted card.
The ruling had not been implemented; it is to be treated as an engine bug and implemented.
(What was found when implementing it: the rule has been in the exhaust effect since 74b4a685d, 2025-06-28, where
a character who cannot exert is not a candidate for exhausting. The card that prompted the finding, Ulaire Toldea
103_79, got round it by nesting its cost inside an `If` in its effect, where a cost does not decide whether a
trigger is offered. The definition was fixed; the engine needed nothing. `mechanics/wounds/ExhaustTests` holds the rule.)

**A card discarded with its dying bearer IS "discarded" for a card that then plays it from the discard pile**
(Q-0105, Merry's Pipe 103_113), "but it has to be playable". The older rule against answering a discard on
bearer death (Q-0023) is about PREVENTING it: "where would the card go after being 'prevented' from
discarding? Its bearer is gone. However *replaying* can retarget and locate another eligible bearer, so
it's kosher." *[2026-10-07, night]*

**Preventing a discard that was a cost (Q-0038).** Q-0038 (102_8): preventing a discard that was a
cost "is permitted, but it will cause the cost to no longer be considered paid". *[2026-10-06]*

**"Remove N [culture] tokens" may take them from more than one card.** Corsair Marauder 58_57, "remove 2 [raider]
tokens", built with removeTokensCumulative: "58_57 is supposed to be able to take from multiple targets, yeah."
*[2026-10-08, evening]*

**Costs (2026-10-11, fifth notes).** F-0425, Morgul Regiment 7_197 assignable while only one of its repeated
exertions can be paid: "Interruptions by the opponent are one thing, but if you're unable to pay the cost by
default, you're not supposed to be able to perform the action at all. "If a card or special ability has a cost,
you must pay that cost or you cannot use that card or special ability." Also, `playing a card`, step 2: "Checking
to see that all costs can be paid is a requirement of playing a card. If you cannot meet all requirements, The
Card returns to your hand. If The Card returns to hand, then you do not lose initiative and skip the remaining
steps." (Obviously Gemp doesn't have such mechanisms because we look *forward* at whether actions can be
performed, not *backward* and undoing unplayable actions, but the principle is the same.) Thus, Regiment should
be altered such that it does not permit the costs to even begin if they are not fully capable of paying them at
the start. B." So a cost is judged payable in FULL before it begins; a response of the opponent's that spoils it
midway is another matter. Q-0256, Blood Runs Chill 8_3 and Great Day, Great Hour 10_1 ("a Dwarf who is damage
+X"): "A is more consistent with the wording, yeah. (The errata was actually because a response to that exert
from Cirith Ungol Watcher could cause the dwarf to become hindered, and therefore damage +0, so this memorized
that before the exert. But I think that's a more faithful reading regardless, it just didn't matter much
before.)" (X is what the Dwarf was when he was spotted). F-0464, an event that needs initiative counting itself
toward it: "I agree, A is the right choice. Not sure why this was altered way back when, but I suspect it was
someone reporting a bug that seemed reasonable rather than a rules-based intervention." (four cards beside the
event). Q-0247, the three things built beyond the design of the cost of assigning: "Keep. Well done on
maneuvering that one." Q-0254, Above the Battlement 7_262: "This just needs an official rules council reading;
the damn text is ambiguous as to what the hell it *means* ... Keep it as it is now, but I'll ask the Council."
*[2026-10-11]*

## Triggers, responses and the play of a card

**2026-10-05. Who orders simultaneous actions (G-0005).** "After all other procedures have been followed, the
Free Peoples player is the 'tiebreaker' on deciding timing of two simultaneous actions." So only the Free Peoples
player is ever asked to order required responses, and Shadow twins of `FreepsResolveRuleFirst` and
`FreepsResolveActionOrder` would be invalid.

**A card's own required trigger still happens when the same loss removes the card (Q-0020).** Thrashing
Tentacle (101_33) adds its burden when the skirmish it loses kills it. "Consider something like Freca, that
produces an optional trigger *and also* lets the original card it was attached to get killed. If the required
version didn't work, the optional definitely wouldn't." (LR 2 is the related ruling on who counts as a losing
character; LR 8, still preliminary, is about "skirmish about to end" triggers on a card that is discarded
first, which is another matter.) *[2026-10-05, afternoon]*

**A discard that follows from its bearer leaving play cannot be responded to (Q-0023).** "When we're responding
to cards about to be discarded with the intent of preventing their discard, we can't usefully respond in the
case of bearer death. Where would the prevented-discard card go afterwards? ... we won't respond to cards
discarded on bearer death because there's no meaningful place to put them, usually." (V2 Haldir's wording was
changed for this.) *[2026-10-05, evening]*

**Responses and triggers.** "Each time ..., you may" is a trigger, not a response (Q-0069 on Great Gate: "it
should be a trigger as the text uses"). A response stops being offered once what it answers has been cancelled
or prevented (Q-0043: "normally when responses react to a trigger, they should stop reacting if that trigger is
prevented by a prior trigger"). An optional trigger added by an event belongs to the player who played it
(F-0083). *[2026-10-06, evening]*

**"Each time your condition is hindered or discarded" answers its own (Q-0075).** "Each time your condition is hindered or discarded" answers the card's own hindering or discard (Q-0075, One
Ill Turn Deserves Another 103_38). *[late on 2026-10-06]*

**"Each time this is hindered" is answered after the hindering (F-0120).** "Each time this <card> is hindered" is answered after the hindering, not when it is about to happen
(F-0120, Desert Wind Scout 103_48): "If the after-hinder works, I'm fine with switching to that; I think I
did it as a before-trigger because I was concerned that the after-trigger would fail to connect due to its
source being inactive due to the hinder." *[2026-10-07]*

**A card's own removed-from-play trigger is carried out as that card and its owner (F-0121).** A card's own hindered-from-play (and discarded-from-play) trigger is carried out as that card and its owner,
not as the card and player that removed it: "fix it in the engine" (F-0121). *[2026-10-07]*

**Any discard from play sets off "when this is discarded (from play)"** (Q-0081): "I would expect that
end-of-turn discard to produce a triggerable response." *[2026-10-07, later]*

**A copied keyword keeps its number** (F-0126). **Two offers of one response name what each is for**
(F-0137). **A play that fits nothing is not offered** (F-0140). *[2026-10-07, later]*

**A card discarded as it comes into play** (F-0139; CR 4.2 "discard"): "a persistent thorn in our side ...
one of the only interactions we've never been able to address in the engine": Northern Ithilien 7_359 with
Watcher in the Water and Evil-smelling Fens in play, playing Gollum again and again, "draining all the
threats and turning them into burdens; the rules dictate that the auto-discard should cause Gollum's play
to no longer count as fulfilling the cost of Northern Ithilien, but the engine doesn't have a way for
after-triggers like that to reach back in time and decide it so." She wants a proposal with a breakdown
before anything is built. *[2026-10-07, later]*

**On a card that leaves play as it is played (F-0139; her answers to the proposal).** The frame is CR 4.2
"playing a card": "Step 5 is when Watcher would respond and discard the card, while it is not until Step 7
that 'The card is played', at which point we can interpret it to have fulfilled the cost in the case of
Northern Ithilien." Steps as she counts them: 4 the card is placed and is in play; 5 respond to the playing
of the card (and to losing initiative); 6 perform effects; 7 the card is played.
1. A play that is a cost is fulfilled only at step 7: "a natural reading of steps 5-7".
2. A card's own "when you play this ... or discard it" is step 5 too: "I would also expect it to cause step 7
   to not manifest, and therefore not count as executed."
3. "If anything in step 5 (a Shadowplay/Unheeded combo is the classic example) causes it to exit play before
   it has made it to step 7, I would count that as not fulfilling the cost." Any exit, by any card.
4. A play discarded on entry does not count as "played this phase": "now that one's thornier. I would think
   it would not."
5. The pointless use (Northern Ithilien beside the Watcher) "should still be offered".
6. The Watcher played away from a marsh beside another minion: "it would have to evaluate the effects on
   Watcher in the order in which they are printed, which would first discard the others and then discard
   itself".
7. "Loss of initiative is step 5": it stays with the responses to the play. *[2026-10-07, night]*

**"Played this phase" is judged at step 7 as well** (F-0139, follow-up): "the 'played this phase' should also
hold its counting until step 7, for consistency's sake." A play whose card is gone before the play
completes, by the entering step or by a response, is not in the play history. *[2026-10-07, night]*

**A play's extra effects are carried out though the card has gone** (F-0139; Ulaire Otsea 103_78's threat):
"the threat can stay, given that it is an additional effect, which puts it under 'do what you can'." *[2026-10-07, later afternoon]*

**A card discarded by its own "when you play this ..., or discard it" was not played for other cards either**
(F-0139; Dunlending Rampager and Ulaire Attea): "a self-discarding card should not trigger Attea or other
'each time you play' or 'when you play', that's pretty straightforwardly the same situation as Runner, the
only difference being where the reaction text is." This reverses item (g) of the F-0139 build. *[2026-10-07, later afternoon]*

**F-0181, Greatest Kingdom of My People 51_16, "the first time your opponent plays an Orc each turn":**
"You and the reviewer are correct on F-0181." Only the first Orc played in the turn brings the offer,
whatever was answered; a declined offer does not come again. *[2026-10-07, late]*

**F-0186, response events when a card plays "an event" from somewhere:** "Response events should not show
in such generic cases when asking for a phase event, yeah, and especially when playing an event from
discard. There is an existing bug on a RTMD card that causes a game crash if a response event is chosen
this way and played." A response event is never among the cards offered by "play a(n) ... event from
<discard pile / a stack / draw deck / hand>" unless it also has the current phase's timeword. *[2026-10-07, late]*

**F-0193, Race Text 92_29 (RTMD), "Open and reveal a sealed booster pack ... You may play one shadow card
revealed, ignoring all costs":** "Given the specifics of that card, we'll say we restrict it to shadow-phase
events only." Of the events revealed, only Shadow-phase events may be chosen; a response event, or an event
of another phase, is not offered (today any event is, and a response event played this way crashes the
game). Other card types as now: those four words are the campaign's, not hers, and say only that her answer
changed nothing for a card that is no event; what "ignoring all costs" waives is F-0316's entry ("How the
campaign is run") and its shape. *[2026-10-07, late]*

**2026-10-05. The snapshot (Q-0011).** "Make all X ... until Y" on a special ability affects only what was on
the table when it was used, "as per the Snapshot Rule", as on an event.

**Keywords copied "until ..." are a snapshot taken when the ability is used** (Q-0096, Flame of Calenhad
103_86): "whatever keywords were on the target would get memorized and applied to everyone else." *[2026-10-07, night]*

**War Howdah 103_62 keeps the stacked Man's game text and keywords** "even if the Man is later lost": a
snapshot of both, like Q-0096. *[2026-10-07, evening]*

**A tentacle killed by the wound may still be stacked (Q-0014).** "The tentacle being killed doesn't preclude
also subsequently being stacked due to another card's game text." Out of Dark Waters (101_34) stacks it from
the discard pile. *[2026-10-05, afternoon]*

**Reinforcing more than one token is one at a time (F-0113).** Reinforcing more than one token: "Should let multiple tokens be selected one at a time" (F-0113). Write it as
a `Repeat` round a `ReinforceTokens` with no `times`; `times` on ReinforceTokens is only for "reinforce a
token twice" (the same card). *[late on 2026-10-06]*

**Triggers (2026-10-11, first notes).** F-0389, Rivendell Waterfall 1_342 giving no +1 to the move limit where
the Free Peoples player takes the ranger's death ahead of the site's check: DROPPED, not a bug: "this is a
situation that is both so niche as to be a very diminishing-returns edge case, and also something that is 100% in
the hands of the Free Peoples player who foolishly decided to choose the wrong order to resolve required triggers
in. I'm not worried about the timing on this; that's part of the reason *why* freeps has the last tiebreaking say
in cases like this, so that it's their fault and not the rules. Go ahead and drop this one." So what follows from
the order in which a player takes his own required triggers is his, and no finding. *[2026-10-11]*

**Responses (2026-10-11, fifth notes).** Q-0250, Siege Engine 5_60 ("If one or more machines are about to be
discarded by an opponent, discard this condition to prevent that") against a discard of machines and other
cards at once: "Given that the FNF ruling steers clear of specifying "machines" being discarded, I think we are
meant to infer that they are non-machines except for Siege Engine itself. This makes me think that the text is
thus meant to be read as: "If one or more machines are about to be discarded by an opponent['s card's action],
discard this condition to prevent that [action]." Given that it emphasizes that this is a single simultaneous
action, I think it's trying to say that the action is all or nothing. This would indeed rescue the free people's
own conditions, by preventing the blanket simultaneous action rather than negating it for some and not others,
which requires splitting up the action into several linked but not simultaneous effects. Let's go with B for this
one." (the whole discard is prevented once one machine is in it). Q-0253, Very Nice Friends 7_76: "I think it's
as soon as Freeps chooses not to stay, so before reconcile as it is. A." *[2026-10-11]*

## Hinder, the support area, sites, Maps and the Ring

**2026-10-05. Sam as Ring-bearer (Q-0008).** Resistance was fleshed out in sets 9 and 11, codifying what had been
improvised with Frodo and Sam earlier. "Resistance is a numeric stat that they always have, similar to Strength
or Vitality, but originally on Sam it was dormant and unused unless and until he became the Ring-bearer. With set
11 they expanded the concept such that every companion has a resistance that is always reduced by the number of
burdens, but which does nothing on its own if reduced to 0 unless the 0 companion is the Ring-bearer." And: "When
any character dies, as the 'leaving play' section indicates, 'Any tokens on that card are removed', and as
burdens are tokens they are thus flushed away, meaning Sam inherits the mantle of Ring-bearer with no burdens to
his name." Bears on: every Sam with "make Sam the Ring-bearer (resistance 5)".

**2026-10-05. Hinder.** `at/HinderTests.java` has an exhaustive set of tests for the hinder mechanic; read it
where the 5.0 excerpts leave a detail of hinder open.
(Since 2026-10-10 that set is converted: the rule-by-rule tests are the `mechanics/hinder/Hindered*Tests` classes, with
CardsBorneByAHinderedCardTests, TokensOnAHinderedCardTests, ReconcilingRestoresHinderedCardsTests and
UniquenessTests. Read those; `at/HinderTests.java` is gone, with the whole of `at/`.)

**A "Discard X" beside a bar on X is a CONTINUOUS discard, on every such card (Q-0229: C).** "Hearken to Me is the
proto-Anduril, which codified its sort of effect. You'll note that later cards use "cannot bear" on the second
clause, because it famously became apparent that Blade Tip and Black Breath could still go on Theoden, because
they weren't *played* but *transferred*. In this case we will apply that same GOTM ruling: the discard will be
continuous and the poor-mans-cannot-bear poorly keeps them off. Oh, I do want to make sure: on GOTM you can see
that there is a ConstantlyCheck trigger, but it has to specify the CanSpot requirement so that it doesn't
constantly blip in the client on every action; that same structure and requirement will need to be used on all
of these related cards. ConstantlyCheck in general should be carefully restricted like this lest it be a
nuisance to play with. So, C, and I would like C to be applied to all the related cards." So Hearken to Me 4_94,
Andúril 7_79, Shadowfax 4_100 and their like discard a matching card whenever one is on the bearer (as it lands
by a transfer; at a restore), built as that card is: a `ConstantlyCheck` trigger with a `CanSpot` requirement.
*[2026-10-10]*

**"Discard X" and "cannot bear X" are two standing effects; only the first discards on a restore (Q-0223: B).**
"Anduril's interpretation was altered with the Legacy Ruling on GOTM: we see in GOTM a situation where both
clauses exist but they *don't* match the same target. We therefore decided that both are in force as ongoing
effects: the discard removes any cards matching the description that end up on the bearer, while the "cannot
bear" prevents them from getting there in the first place (makes the targeting invalid). In GOTM's case, an
artifact hand weapon that was on bearer before GOTM was played would therefore not be discarded: the discard
effect only hits possessions, and "cannot bear" only prevents new bearings. In Anduril's case, both clauses target
the same kind, so it's a little more moot, but we still interpret them as two separate, unrelated effects that
just so happen to share a target. So. When restoring, Anduril in particular has a "discard" clause that
immediately kicks in and banishes other cards (say, an Aragorn's Bow) that were played on him while Anduril was
hindered, but in the case of something like Elven Armaments, because it only has the cannot-bear clause and
lacks a discard clause, it will have to begrudgingly co-exist with other possessions played while it was
hindered. The other examples you list (including Black Dart), I would interpret as matching Anduril, where they
all contain an ongoing discard effect that makes the cannot-bear moot for the purposes of hinder/restore.
Disarmed however also lacks an ongoing discard (it explicitly makes it an on-transfer trigger), so it would also
permit the shadow cards to stay. So all that to say, B." So: a card whose text has a discard sentence of its own
(Andúril 7_79, Shadowfax 4_100, Black Dart and the like) discards what matches whenever it comes back into
force, by a card's restore or a reconcile's; a card that says only "cannot bear" (Elven Armaments), or whose
discard is tied to its transfer (Disarmed), refuses new bearings and leaves what is there. *[2026-10-10]*

**What was laid on a card for a time is back in force when the card is restored (Q-0222: A).** "A was intended,
yeah." Hindering switches a card off and takes nothing from it: Aragorn 1_89 made defender +1 until the regroup
phase, hindered and restored in the same phase, is defender +1 again until the regroup phase. *[2026-10-10]*

**Hinder and restore.** A card hindered in the middle of its own ability still carries the ability out, and an
effect that names its bearer still finds him (F-0099: "the -1 strength should still apply"; the old stub
HinderingACompanionMidAbilityAlsoCancelsTheAbility "was the old design but it was altered"). But a token is not
added to a card hindered before the token arrives (Q-0070: "can't > can; tokens cannot be added > continue to
execute"; "the same logic is what makes Anduril, Legend Remade successfully block a wound"). A reconcile
restores the player's hindered cards before the discard (Q-0059, CR 5.0; the engine is to be changed). Cards
already hindered stay out of hinder choices (Q-0060: "for QOL purposes let's continue to leave hindered cards
out of the filter"). Shadowfax 103_5: "Minions skirmishing bearer gain no strength or keyword bonuses from
Shadow cards borne by minions skirmishing bearer" (Q-0061). *[2026-10-06, evening]*

**A choice by side alone may take a hindered card** (Q-0098, Cover of Darkness, Omen of Despair 103_93): "Yes,
as written that is valid. This should only apply to cards which target 'a card', 'a Shadow card', or 'a Free
Peoples card', since anything more specific is unable to pierce hinder's veil." So: where the text names
nothing but "a card" or a side, the definition's effect carries `override: hindered`; where it names a card
type, culture, keyword or anything else, hindered cards stay out. Hinder choices themselves still leave
hindered cards out (Q-0060). Fixable at landing. *[2026-10-07, afternoon]*

**"All Shadow cards borne by ..." and "each Free Peoples card borne by ..." reach hindered cards**, as "a
Shadow card" does (Q-0098): "Good call on all/each." *[2026-10-07, evening]*

**A "support card" is a card in the support area, and a card borne by one is in the support area** (Q-0099):
"'Support' is meant to be shorthand for 'in the support area'. A card attached to another is on Gemp in the
'attached zone', but ultimately that's an implementation detail; it is still 'in the support area'." *[2026-10-07, later afternoon]*

**"In / from a support area" takes in borne cards, on every card** (Q-0101): "The borne cards are physically
placed under the cards which bear them, and so by transitive property I would expect that if their bearer is
'in the support area' then they must, too." Stacked cards are not seen ("Stacked cards are definitely not
seen, but I think otherwise they should be fair game. Weary on an Ally and Pillage should both get hit").
Whose area: the one the bearer lies in ("If my opponent's Black Breath is on my Elrond, it's in my support
area, because that's where Elrond is"). *[2026-10-07, night]*

**Burdens are not "on Frodo" while Frodo is hindered** (Q-0103, For Frodo 103_107): "the burdens can still be
spotted by themselves, but ... while Frodo is hindered, he himself can't be." *[2026-10-07, night]*

**A card borne by a hindered companion is not "borne by a companion"** (Q-0108, Gorgoroth Wastes 103_124): a
hindered bearer is not found by its card type. *[2026-10-07, night]*

**The Ring-bearer and The One Ring are never discarded, for any reason** (Q-0109). *[2026-10-07, night]*

**Maps and meta-sites cannot be hindered** (Q-0110): "they don't do anything and would be a distraction ... an
explicit entry should be added for Maps and Meta-sites." *[2026-10-07, night]*

**"Bearing 3 or more cards" counts hindered cards** (Lying Counsel 15_85). *[2026-10-07, night]*

**"Control(s) a site" goes by the count of controlled sites (Q-0054).** "All the cards you mention and those
using the same language should be retrieving the count of controlled sites, going from 0 -> 1 and
enabling/disabling their text accordingly." A card that needs the site itself (stack on it, liberate it) still
needs a real one. *[2026-10-06, evening]*

**Maps are information only (Q-0055).** PC-Movie runs under Multipath rules: any legal site path, cards of that
path only. "All the Map does is announce which path the player is using, and it is revealed in the player's
support area before the game begins, so it is information available during bidding. Maps therefore have no
ongoing in-game effect." *[2026-10-06, evening]*

**Controlled sites are not discardable (Q-0056).** A card that discards "a card" from a support area does not
reach a controlled site. *[2026-10-06, evening]*

**Sites (2026-10-11, first notes).** Q-0238, Speak "Friend" and Enter 2_26 and the nine cards worded "play the
fellowship's next site (replacing opponent's site if necessary)", on a Shadows-block path with the player's own
site lying next: "A" (as built: the parenthesis is a reminder and the rule decides; the player's own site is
replaced too). Q-0226, of which this was a rider, stays parked. *[2026-10-11]*

**Bearers, the Ring and sites (2026-10-11, fifth notes).** Q-0252, what the engine's "borne by a <kind>" does
with a hindered bearer: "1 is fine to keep. 2...let's change the filter to `attachedTo(not(ally))`. The intent is
to wiggle around auto-discarding from an Ally (who is explicitly *not* at the same site as the fellowship unless
they are at their home site), rather than anything that needs the ability to spot bearer in a way that hinder
should legitimately interfere with." (a hindered Ring-bearer is still found as "the Ring-bearer"; Asfaloth 1_31
is discarded underground from a hindered companion too). Q-0258, The One Ring when its bearer is killed while
assigned to a skirmish not yet fought: "A. In the client today, if Frodo dies in an unresolved assignment it
deletes the assignment box and rearranges the board before Sam's response can kick in, so there's no need for us
to change anything for that brief instant". Q-0259, Many Miles 13_21 at a site a Shadow player replaced this
turn before the fellowship came to it: "B I think, yeah. Your reading seems sound. For the Traveled Leader case,
yeah, I concur that the replacement still happened, whatever happened in between." (+2). *[2026-10-11]*

**Bearers, the Ring and sites (2026-10-12, fifth notes).** Q-0285, a bare `not(...)` given to `attachedTo` is
asked of a hindered bearer too (read against him as printed: "not an ally" rules an ally out and finds no kind),
so Asfaloth 1_31 under `attachedTo(not(ally))` is discarded from a hindered companion underground and stays on a
hindered ally: "Keep, good work." *[2026-10-12]*

**Keywords a card gives itself while it can spot them (2026-10-12, sixth notes).** Q-0291, Sentry Uruk 15_170 keeps
a self-given keyword only while it has it; once taken away, barred or its text stops, it is not regained alone:
"Yup, that makes sense. If he bootstraps himself, and then loses other supporting minions, and then his bootstraps
are cut, he can't get back up again by himself. Keep." Q-0290, Ravaging Wild Man 15_91 ("While you can spot a
hunter, this minion gains hunter 1"), the Sentry's words: "A, yeah, same deal" (`spotsItself: true`).
*[2026-10-12]*

## Reveals, draw decks and shuffles

**2026-10-05. Reveal means shown (F-0021).** A card "revealed" from the draw deck that only the player choosing it
sees is an oversight; the opponent is to be shown it.

**2026-10-05. Looking through a draw deck (Q-0013).** CR 4.2 "draw deck": "When you finish looking through it" is
the timing. "So as per rules, we always reshuffle." Other Decipher games lacked the rule and wrote the reshuffle
on every card, and later LOTR sets fall back on that habit, but here an instruction to reshuffle after revealing
the deck is almost always redundant. A definition that searches the whole deck and leaves it in order is a bug.

**Who is shown a reveal (Q-0024, Q-0025, F-0039).** A player who needs to see the card is shown it: the
opponent of a card revealed from a draw deck by the printed word "reveal" (F-0021), everyone for an opened
booster (F-0039: "it should reveal it to everyone just for fun"). The owner of a hand that cards are revealed
from gets no pop-up ("which blocks play until dismissed"), but the game log must say which cards were revealed:
"If the log isn't being populated that needs addressed." A card taken from a draw deck by type, where the text
does not say "reveal", is revealed by the log line alone: "The log has links any player can click to see what
card it's talking about." *[2026-10-05, evening]*

**A random reveal from a hand gives its owner no pop-up either (Q-0036).** "Doesn't need the popup, just the
event log." *[2026-10-06]*

**2026-10-05. Copies in hidden zones (G-0003).** That a choice listed by blueprint cannot tell two copies apart is
a limitation of the engine: it cannot distinguish two blueprint ids of non-physical cards.

**"Shuffle up to N ... into your draw deck" shuffles the deck even when nothing is shuffled in** (Q-0102):
"it's more consistent for the shuffle to always happen even if you're shuffling 'in' nothing." So such an
offer always does something and is always made (F-0161 is not a bug). *[2026-10-07, night]*

**Q-0112 (F-0196), shuffling "any number" of cards from play with none chosen:** "Rest by Blind Night should
clearly be playable with no effect, but I can see the issue with Mumakil Commander. Let's say that on an engine
level, the shuffle can target 0 cards, but on a card level we'll set up Mumakil Commander to require there be a
mount in play first." The engine shuffles the deck with none shuffled in, as from a discard pile (Q-0102);
Mumakil Commander 18_71's ability is withheld until a mount of his can be spotted. *[2026-10-08]*

**Reveals (2026-10-11, first notes).** F-0391, Storm of Argument 3_6 revealing a Shadow card past No Business of
Ours 2_44: "Seems pretty clear that it should be rewritten to use revealRandomCardsFromHand. If the other test
can't then be tested, then it seems that edge case isn't something we really need to worry about, if there are no
other candidates in the entire card base." So the definition is written as its three siblings are, and the two
tests of `mechanics/deck/RevealingNoCardsFromAHandTests` that 3_6 alone could lay go, unless another card in the card
base can lay them. *[2026-10-11]*

**Reveals (2026-10-11, fifth notes).** Q-0251, Eye of Barad-dûr 5_96 when It Burns Us 6_110 has the Shadow player
"reveal his or her hand": "Since there's no optionality and no choice, I'd say this is Free Peoples doing the
revealing. "Make an opponent X" sometimes seems like redundant, unnecessary verbiage when X doesn't have any
wiggle room. B." (as built: the Eye is offered). With Q-0171 that makes the rule: where a card makes an opponent
do a thing and he has a choice in it, he is the one doing it; where he has none, the card's player is. *[2026-10-11]*

## The test rig

**2026-10-05. A cheat for allies (G-0004).** "There's nothing wrong with a MoveAlliesToTable function. Go for it;
you can enforce the card type if you like."

**2026-10-05. Freeps logic in Shadow twins (G-0006).** Many of the rig's twins were produced by copy and paste,
"and it's therefore been a common occurrence for the freeps logic to be lurking inside the shadow twin." A Shadow
method that asks about the Free Peoples side is to be fixed when found.

**Rig requests agreed (2026-10-05).** G-0007 (asking whether a character is offered for assignment), G-0008 (the optional-trigger check by card),
G-0009 (the by-card availability check and wording of its own), G-0010 (one of several actions of a card) and
G-0011 (a shuffle counter: "I think it shouldn't harm anything") were all approved, and are in the rig.

**Rig requests agreed.** G-0012 (an optional trigger chosen by card is matched exactly), G-0013 (the methods
about revealed cards: all three parts) and G-0014 (`FreepsResolveRuleFirst()` checks that what is pending is
the required-responses ordering: "This will cause a great number of test changes downstream of this, but
approved nonetheless"). *[2026-10-05, afternoon]*

**Passes stay harmless (G-0015).** "Let's leave passes harmless for now." And the names: "we should really use
FreepsPass and ShadowPass rather than the (needlessly) wordier FreepsPassCurrentPhaseAction /
ShadowPassCurrentPhaseAction." Tests are written with `FreepsPass()` and `ShadowPass()`. *[2026-10-05, night]*

**No two cards of one unique title in play, whatever their types.** On a second Boromir cheated in beside the
first to tell "the bearer" from "a Boromir": "Go ahead and remove that malformed board state. Also, Elrond being
unique means you have to choose one type or the other, you still can't have both just because they both exist.
The cheating functions are for us to short-circuit more-or-less legitimate setups, but putting 2 unique cards on
the table is getting too far into the weeds." And on not building such boards only to tell apart two definitions
that differ nowhere else: "This is good." *[2026-10-08, afternoon]*

**A card test never relies on an exception; a check the rig cannot show parks the card.** On Bill Ferny 52_75,
whose tests read the game refusing an assignment through `assertThrows`: "Standard card functionality tests should
never rely on exceptions, and I'd like those sorts of requests to park the card while pending the engine upgrade,
rather than moving on blithely." So: no `assertThrows` and no `try` in a card test (lint refuses them); the check
is written in the ledger as `waits <check>: G-0000 ...`, a rig request is filed, and the card lands parked on it
(G-0029 for 52_75, G-0028 for Long I Fell 54_97). *[2026-10-08, evening]*

**Rig methods ahead of their use (Ketura, 2026-10-08, night).** On `FreepsCanAssignAgainst`, built for a card
whose definition then changed: "I don't object to the function being there; there's plenty of functions which have
obvious use which just haven't been used yet, and the occasional add is fine. I just don't want us to get
distracted adding so much to the test rig before it's needed, because as we go through the entire code base we
*will* encounter every kind of mechanic known to the game, so that will shake out as a matter of course."

**A card that must not be caught up in the rest of a board is put somewhere fixed at the start.** On a test that
failed one run in eight because the card under test could be in the hand a random discard took from (F-0264,
52_44): "the way I usually handle this is to put the card I care about somewhere else, like explicitly into the
discard pile at the start of the scenario, then it is guaranteed to not interfere with the rest of it."
*[2026-10-08, night]*

**G-0055 (2026-10-11, fifth notes).** A card of player 2's cheated into play before his turn has its standing
text started twice (`GameState.addCardToZone` starts a card's text without asking whether the card is active):
"Good catch. A for sure; the gamestate doesn't want to run that for inactive cards even in real play." (mended in
the engine; then the landed tests are searched for a doubled number). *[2026-10-11]*

## Card by card

**2026-10-04. "A card stacked here" (F-0008).** On Deepest Delvings (101_2) the text is right and the
implementation, which only took [dwarven] cards, was not: likely a copy-paste from a similar Dwarven condition.

**2026-10-05. Hospitality of the Dwarves, "no more than 1 wound until the regroup phase" (101_1).** "Each dwarf
has their own per-phase limit of 1 wound." The limit is one wound in each phase while the effect lasts, not one
in all.

**"The first sentence of Saruman's game text" (Q-0016; LR 6).** Saruman's Staff has no effect on Saruman, Of
Many Colours (12_54) or Saruman, Agent of the Dark Lord (12_144), whose text opens with keywords. The White
Council (101_18) "was essentially binding itself to how Saruman's Staff works", so it leaves those two alone as
well, which is what the game does. The follower Saruman, Servant of Sauron (17_38) is not named by the ruling;
the game leaves it alone too, and the ruling's reasoning (the sentence meant is the one about not skirmishing)
covers it. *[2026-10-05, afternoon]*

**Items on [shire] companions (Q-0026).** Bilbo's two items "can go on multiple": each goes on any [shire]
companion, the same one or another. *[2026-10-05, evening]*

**Fixes agreed.** F-0033 (Gwaihir: "up to 2" is 0 to 2, "clearly an oversight"), F-0035 (The Dark Lord Advances:
"a clearly broken implementation"), F-0036 (Out of Dark Waters' action "should be fixed to not be available all
the time"), F-0038 (Pale Crown: "very nice find ... definitely good to fix and test for it"), F-0039 (92_29).
F-0037 (the Hobbit Gandalfs sharing Saruman's flag) is confirmed and waits for the Hobbit sets: "Those hobbit
cards should have made their own flag." *[2026-10-05, evening]*

**Bilbo, Of Bag End (101_50) had no requirement to play (F-0040).** "Implementation is wrong, needs fixing."
Fixed. *[2026-10-05, night]*

**Other answers.** Q-0030: Watcher in the Water replacing a tentacle "is being removed from its assignment, which
may cancel it if it's the only minion in its skirmish". Q-0031: "[rivers] and [sites with no other terrain
keywords] gain marsh". Q-0032 (102_4): the "you may play" possession "should be optional, an implementation
oversight". Q-0037 (102_7): "The card probably shouldn't have had may on it ... Let's go ahead and fix it to
match the bad wording": the additional reinforcement is asked. Q-0040 (102_77): continuous;
"we ruled that with the Shadowfax, GOTM ruling and applied it to Anduril as well." F-0044: the Shadow player
picks which tentacle Watcher replaces. F-0054: the Response type is to take a per-turn limit and the Staff is to
be a Response. G-0016 declined: "There are no cards that put more than one kind of culture token on another,
and no plans to in the PC." *[2026-10-06]*

**Card by card.** Q-0042 Breaching Shot may reach an ally away from home ("they are breaching the wall"). Q-0045
Wolf Pit as built. Q-0046 Royal Guard: only the keyword lasts. Q-0048 Now for Wrath: "your", and the text gains
the word. Q-0049 Now for Ruin as built. Q-0051 Trapped in its Gaze: this skirmish ("it should have been worded
'to make a skirmishing twilight minion'"). Q-0052 Eastemnet Hills: from hand, Ketura updates the wording.
Q-0063 Haste: the title restriction stays. Q-0064 Sticky Strands may be pulled from another companion. *[2026-10-06, evening]*

**Later the same evening (Ketura, 2026-10-06).** The reviewer's single mistakes and the `--with-parked` run stay
"for now". Q-0051 Trapped in its Gaze 102_66: "go ahead and change the text + implementation" to "a skirmishing
twilight minion". F-0067 From Deepest Dungeon 102_39: "X should only be permitted where it makes the Balrog
playable, since we are playing from discard and thus must be able to play it." F-0087 Gorgoroth Keeper 10_86:
"Do a requirement for now; I'm not certain an engine-level addition is warranted." F-0090 (the deck restriction's
min read from its max field): "go ahead and fix." *[2026-10-06, evening]*

**On the details pass 4 settled (Ketura, 2026-10-06, Q-0074).** A minion spared from an overwhelm by its
protection takes the ordinary wounds for losing and is not "overwhelmed" for other texts: "Very good." Winning by
controlling five sites "is from the very obsolete 2nd edition; it's not worth considering in the future." Sticky
Strands' timing as built: "good". A Map reaching the support area after the bid is "just a detail about how it's
presented in the client ... the information is there all the same". *[2026-10-06, evening]*

**Writhing Tentacle 101_66 played from a stack away from a marsh stacks no tentacles**: "that's how I'd
expect it to play." *[2026-10-07, later afternoon]*

**Beast of War 17_40 is played from the discard pile at 5 threats, as its text says** (F-0060): "clearly an
implementation error." *[2026-10-07, later afternoon]*

**Driven By Her Own Cruel Will 103_105 with no Hobbit strengthened hinders no minion** (Q-0100): A. "That
skirmish" is the strengthened Hobbit's. *[2026-10-07, later afternoon]*

**Q-0116, The Mirror of Galadriel 51_55:** "I approve." *[2026-10-08]*

**Repeated deck-validation lines are fine (F-0112).** Repeated lines in deck validation errors are "fine as-is" (F-0112): not a finding. *[late on 2026-10-06]*

**Q-0130, Galadriel, Lady of the Golden Wood 53_17: A.** "A was intended, yes. This card was originally banned in
Expanded due to her ability to easily play forests on that path, and so the errata specifically is keeping its
FOTR/Movie usage while supressing its ability to spam the Shadows path (which uses unnumbered sites), which should
answer that sub-question." *[2026-10-08, afternoon]*

**F-0220, Song of Durin 53_5: fix.** "Yup, since parentheticals are required, the discard is mandatory (and you
would always prefer to permanently remove 2 conditions over temporarily hindering 1, so it's not strategically
limiting)." *[2026-10-08, afternoon]*

**Q-0129, A Royal Welcome 53_4: A.** "If the ally has no damage, then that shouldn't be offered, for the reasons
you point out. A." *[2026-10-08, afternoon]*

**Q-0127, Filibert Bolger 52_101: B.** "So long as it's marking it as noeffect, we'll go with B." *[2026-10-08, afternoon]*

**Q-0128, O Elbereth! Gilthoniel! 52_108:** "Correctly surmised; the errata is about making the ruling
unnecessary." *[2026-10-08, afternoon]*

**Q-0126, Foul Things 52_59: B.** "The Foul Things errata is unique in that it plays from discard in a way that can
target itself, which I don't think applies to any other event. The infinite loop isn't common enough to guard
against in the engine, and there's no strategic need to play the same card over and over like that, so we'll leave
this exclusion on the card itself. B." *[2026-10-08, afternoon]*

**Q-0125, Realm of Dwarrowdelf 52_12: A.** "Heh, I normally have ruled that it's useful to the player to inform them
that their deck is empty, but Realm of Dwarrowdelf is only used in a particular self-mill deck that purposefully
discards a hundred cards off the top, and so such players are very, very aware of how many cards they have left.
In this case we can go with A." *[2026-10-08, afternoon]*

**Q-0124, Beneath the Mountains 52_1 (and Ever My Heart Rises 4_46 / 54_46): B.** "Yeah, this "how many times" is an
intentional QOL addition so that their opponent is not sitting there for 10 minutes while they discard their deck
3 cards at a time, and what's worse, having to manually click pass in between every iteration. Definitely keep B.
However, the reasoning behind it is unique to these three cards (ROD, BTM, EMHR), due to the deck that they are
used in." *[2026-10-08, afternoon]*

**Q-0123, "killed by" (Orc Butchery 51_265):** "Yeah, this one is tricky without an official definition of "killed
by". I think the current loose interpretation is fine, so let's stick with B." Then: "Ah, my mistake on Q-0123 and
Q-0122. A for both." Kept as built. *[2026-10-08, afternoon]*

**Q-0122, The Number Must Be Few 51_260:** "B. The intent here is to work around Decipher's dumbass ruling that move
limit modifiers are permanent, even if the card is discarded (or heaven forbid, discarded and replayed multiple
times in the same turn). So in effect, it should make this behave as if it were a less fraught effect, such as
"men are strength +1", which obviously turns off and back on whenever the companion boundary is crossed." Then: "Ah, my mistake on Q-0123 and
Q-0122. A for both." Kept as built: the penalty comes back when a 7th companion does. *[2026-10-08, afternoon]*

**Q-0121, Úlairë Nertëa 51_234: B.** "This is a poor implementation, yeah; B is definitely an improvement." *[2026-10-08, afternoon]*

**Q-0120, Savagery to Match Their Numbers 51_139: B.** "So Savagery set the high bar for power level in FOTR block:
5 companions was a boundary almost no other cards had, and it won you not just *this* skirmish but the *next* one
as well. The intent of the errata was to make it so it was not 2 automatic wins by intentionally only getting +4
total the first skirmish and +2 on the second. B." *[2026-10-08, afternoon]*

**Q-0118, Armor 51_92, Coat of Mail 51_101, Great Shield 51_107: A.** "Yup, Man Allies count. The original was down
to Decipher's preferred templating which used race as a shorthand if the culture was mono-race--a decision that
came back to bite them as soon as [Rohan] was released and suddenly a bunch of [Gondor] cards were able to go on
that culture, most notably Flaming Brand. Our errata leans into this rather than away, permitting [Gondor] Wraiths
to use them (since they're from the time period depicted on the card). A. As for the non-shadow restriction, yes,
Strange-looking Men could move it to a Shadow Man." *[2026-10-08, afternoon]*

**Q-0119 and F-0200, Boromir's Cloak 51_98: 1B, and the Shadow restriction kept.** "As the author of the errata, I
*meant* "skirmish event or skirmish special ability". However what I *wrote* does permit for Response actions as
well. Given that it requires an engine-level alteration, let's just go with 1B for now. Heh, for question 2, it
was meant to go against Shadow abilities, and would be pointless to block one's own intentionally-used ability
that presumably helps Boromir. Let's keep the Shadow restriction and I'll update the wording." *[2026-10-08, afternoon]*
1B was replaced on 2026-10-10 by 1A (Q-0203, under "How the campaign is run": no engine change was needed after
all): the Cloak answers a Response ability used during a skirmish involving Boromir. The Shadow restriction
stands. *[2026-10-10]*

**Q-0117, Sleep, Caradhras 51_84:** "Yup, that's a more general ruling about activity in a nascent game that wasn't
clear yet that this wouldn't discard your own inactive Shadow conditions. One of the only actual Decipher
"clarifications" that were actually that instead of an errata in disguise." *[2026-10-08, afternoon]*

**Findings through F-0210 approved.** "All pending Findings through F-0210 have my approval." *[2026-10-08, afternoon]*

**Q-0131, Uruk Raider 53_74: A.** ""The dead pile" refers to the current Free Peoples dead pile, same as how threats only
count the current Free Peoples threats. A." So an unqualified "the dead pile" is the Free Peoples player's. *[2026-10-08, later afternoon]*

**Q-0132, Hand of Sauron 53_90: A.** "A is fine." An exhausted companion who bears a possession is offered, loses it
and takes no wound. *[2026-10-08, later afternoon]*

**Q-0133, Bill the Pony 53_106 (and Asfaloth 1_31, Black Steed 1_208, 3_106): A.** "But it is a continuous effect,
same as the Watcher. A." And the official ruling on 3_106 (now on the wiki): "When the fellowship moves with Bill the
Pony to an underground site, the Shadow number of that site is reduced before Bill the Pony is discarded." *[2026-10-08, later afternoon]*

**Q-0138, Gríma, Wormtongue 54_154: new text.** "Ah, good point, it should read "Exert Grima to spot an unbound
companion. Hinder a weapon borne by that companion (or all Free Peoples cards borne by them if they bear 3 or more
cards)". I will update the wording. When spotting a companion, restrict the spotting to companions either bearing a
weapon or 3+ cards and at least one unhindered Free Peoples card, just for QOL purposes." *[2026-10-08, evening]*

**Reports that need no answer are findings, not questions (Q-0139).** "If no answer is needed, it should be an F not a
Q. But I appreciate the report." A Decipher ruling not carried over (Q-0111 default B) and other report-only items are
filed as findings (kind ruling, not-a-bug), not in questions.md. *[2026-10-08, evening]*

**Q-0136, Come Down 54_146: A.** "Oh ho ho, very nice find. A is the RAW way to handle this." Archery wounds the allies
cannot take go to companions (CR 4.2 "instead"). *[2026-10-08, evening]*

**Q-0137, Come Down 54_146: A.** "Come Down doesn't say "participate in archery fire", it just says "take wounds from
archery fire", that implies to me that they don't get to contribute, just be on the receiving end. The exiting
modifiers may not cover this and may need an additional one added. A." *[2026-10-08, evening]*

**Q-0134, "(limit (N))" on a count: the card's limit for the whole phase; Legolas 54_73 loses his.** "The intent with
this one was to limit the per-invocation amount at (2), not a cumulative phase limit, but based on what we've
established, this wording is far more like the cumulative +X limit than the invocation limit. Let's remove the limit
altogether. I think the only reason for its adding was as a band-aid in Expanded, and our second iteration altered how
this works and doesn't really need the limit anymore. Plus a limit of (2) twilight removed is materially different
from a +2 cost increase, where a limit doesn't run afoul of the cumulative limit stuff. I will get a new errata
revision issued." So "(limit (N))" and "(limit -N)" read as the card's limit for the whole phase (as "(limit +N)",
F-0135), and Legolas, Dauntless Hunter has no limit. *[2026-10-08, evening]*

**Q-0135, Legolas 54_73:** "good." *[2026-10-08, evening]*

**F-0235, Ever My Heart Rises 54_46: keep.** "It doesn't need a formal window, no, because on the game board the 6
cards are going to be stacked visually, where they can be inspected, and also seen in the log. The window would be a
useless click, as you surmised." *[2026-10-08, evening]*

**Q-0140, Cavern Entrance 54_355: A** (a site's "you" is each player). *[2026-10-08, evening]*

**F-0239, Gríma, Chief Counselor 55_51: new text.** "I will issue a new text; change the text and evaluate it as:
"Exert Gríma to spot an unbound companion. Hinder a condition borne by that companion (or hinder that companion and
all Free Peoples cards they bear if you can spot 4 Free Peoples cultures)."" *[2026-10-08, evening]*

**Q-0141, Merry's Sword 57_242: "(limit X)" is per use; every [rohan] companion is counted.** "1. Yeah, the per-use limit
was intended, given that the player can increase X to get more. It prevented a 6-companion setup exerting Merry
twice to get 6 exertions. 2. Yeah I can't imagine why one would want some exertions but not the max. If it's an
enduring minion, they're just not going to use the ability on them." So a "(limit X)" where X is the use's own
paid amount caps that use only (unlike Q-0134's printed "(limit (N))"), and on Merry's Sword the count takes every
[rohan] companion without asking. Her second answer is about that card: whether every "for each X you spot"
spots all unasked was Q-0248, then open (CR 4.2 "spot" and the game as built ask how many); answered 2026-10-11
("What is offered and what is asked": the player still decides how many). *[2026-10-08, evening]*

**Shadow Host 58_43:** "The original Shadow Host requires them to be exhausted, not just Wraiths (the errata removes
that requirement as excessive)." *[2026-10-08, evening]*

**Q-0142, "wound each / every X": a fixed order, no prompt (B).** "In any situation where there is a timing tie
of required actions, the Free Peoples player chooses the order, as you noted. However, I can see why these were
coded that way, as the vast majority of the time, the order isn't going to matter, and so the game chooses for
them. Let's go with B; I think the CardResolver should at some level be pulling the list of cards from GameState
in a way that should be able to get them in ID order." So the characters an effect reaches at once are taken in
the order of their card ids, the same in every run; no decision is added. *[2026-10-08, night]*

**Q-0143, The Palantír of Orthanc 53_67: B.** A yes/no before a choice whose minimum is 0 goes: "The first no
permits an opt-out, while giving leeway for the player to change their mind after....let's get rid of the pop-up
and just make sure the message displayed to the user is sufficiently clear." The choice (0 to N) is asked alone,
and its text says plainly that choosing none is allowed. Built in engine pass 17 as F-0277; Gwaihir 101_15 has the
same shape (F-0278). *[2026-10-08, night]*

**Q-0144, revealing an empty hand: no pop-up, a line in the log.** Foul Creation 51_44 / 1_44 against Curse Their
Foul Feet! 51_36: "Both of these cards should behave the same....not sure which one is better UX. Let's go ahead
and leave out the pop-up and just go to the log, but ensure that the log has some sort of message to the tune of
"there were no cards in hand to reveal" or something so the user isn't confused." *[2026-10-08, night]*

**Q-0145, cardInfo is not a source of truth, and is kept true.** On errata whose cardInfo names the original's id
and `promos/pc`: "this is almost certainly a copy-paste error. The cardInfo sections are pre-emptive for a future
rework of the HJSON spec to make certain other aspects of the system obsolete, so there hasn't been a need to keep
them accurate everywhere (tho of course we would prefer them to be accurate so when that eventual rework occurs
there's not much that has to be updated). Tests don't have to be written against them but I would still prefer the
information be checked as this is doing. Don't treat them as a source of truth, just update them to match the rest
of the card definition." So: no test reads cardInfo; a mismatch with the rest of the definition is corrected
where it is met, as a `data` finding, and the tools check it. *[2026-10-08, night]*

**Q-0146, Bill Ferny 52_75: A.** "A sounds good." The erratum is built as the original 2_75 is
(`CantBeAssignedToSkirmish` for the Free Peoples player), so Bill is not on his list of minions to assign against,
where the per-pair restriction listed him and refused the answer. Built in engine pass 17 as F-0276.
*[2026-10-08, night]*

**Q-0147, Streets of Edoras 4_340 with Come Down: B. "Participating in archery fire" has parts, and a bar on it
bars them all.** "Well, if we follow can't > can, then Streets should I think disallow Come Down's effect from
affecting [Rohan] allies. My intent with the ruling on Come Down was that "participation" is clearly made of a few
sub-parts, such as "adding to archery totals" and "taking archery wounds when assigned", and Come Down is
performing the second but not the first. However disallowing "participation" should disallow both sub-parts here,
meaning Come Down is ineffectual." So an ally who "may not participate in archery fire" neither adds to the
archery total nor takes archery wounds, whatever card would have him take them; the wounds go to an ally who can
take them, or to companions as F-0243 has it. Built in engine pass 17 as F-0275. *[2026-10-08, night]*

**Q-0148, Glorfindel, Revealed in Wrath 59_16 and 9_16: A. A reveal the rest of an ability hangs on is a
requirement or a cost.** "Skirmish: Reveal the top card of your draw deck. You may exert Glorfindel to make a
Nazgûl he is skirmishing strength –X, where X is the twilight cost of the card revealed." With an empty draw deck:
"I would consider this either a requirement or a cost. Either way, it has to happen for the rest to occur, so an
empty deck should disallow the ability." So where what follows reads the card revealed, the reveal must be able to
happen for the ability to be offered or the event played, whether or not a "to" stands after it. This is not the
optional look of Q-0104 or the optional draw of Q-0029, which stay on offer with an empty deck. Built in engine pass 18 as F-0284. *[2026-10-08, night]*

**Q-0149, unordered card collections in other effects: left alone (B).** After Q-0142 put "each / every" in card
id order, a dozen more effect classes were found to keep cards in sets with no order (kills, shuffles into a
deck, discards from hand). "This is getting a bit into the weeds. If we have a specific reason, a test that won't
work due to the stochastic ordering, we can address those as we go. But preemptively hitting it is unproductive I
think." So: no sweep; a test that an unordered collection makes unreliable is a finding then, with that class
ordered as F-0268 did. *[2026-10-08, night]*

**Q-0150, Streets of Edoras 4_340 against cards that have an ally take part: the bar wins (A).** "Yeah pretty much
everywhere that the "ally can participate" modifier is called should be confirmed to be going through Modifiers
Querying, and then the appropriate functions can be set up to check for the "ally can't participate" modifier and
defer to it." So every reading of whether an ally may take part in archery fire or skirmishes goes through the
modifiers' querying, and there a "may not participate" beats a "may" (Q-0011), for the archery total, archery
wounds, assignment and skirmishes alike. *[2026-10-08, night]*

**F-0278, Gwaihir 101_15, and the Palantír's prompt.** On the yes/no before Gwaihir's 0-to-2 choice: "yes, go
ahead and skip the prompt and just do the 0-1 choices." On the wording written for The Palantír of Orthanc 53_67
("Choose any number of cards from your hand to place on top of your draw deck (you may choose none). Your hand will
then be revealed."): "looks good". Gwaihir built in engine pass 18 (F-0278, with F-0285). *[2026-10-08, night]*

**Card by card (2026-10-10, second notes).** Q-0211, the Playable requirement counts an event's own twilight cost
(Little Snuffler 13_52): "keep, no notes." Q-0214, the errata's `keywords:` and `timewords:` and five values in
the Decipher spelling: "I approve of all of those changes. The keyword/keywords thing is because many cards have
just the one keyword, and with array support on those fields it felt strange to me to have a singular field hold
multiple values. But I appreciate the standarization." F-0341, Hillman Band 4_21 offers to take control of a site
when none can be taken: "yeah this is correct" (taken as: the finding is right; the offer is withheld by a
requirement, Q-0034). F-0340, a Transfer effect offers a possession borne by Warrior of Rohan 17_104: "good to
fix". F-0350, a toil card played on a bearer is offered from a pool short by twice what toil covers: "good to
fix". F-0355, the definition word `assignableToSkirmishAgainst` counting a defender bonus an assignment action
cannot use: "I believe the internal functions used for assignment checks have additional fields for flagging
whether or not defender etc should be considered, for just this use case. Likely the HJSON or test rig needs
expanded to support the use of those flags." G-0041, the rig method for a refused assignment: "Looks good to me".
*[2026-10-10]*

**Card by card (2026-10-10, third notes).** Q-0216, Race Text 94_30 stops a draw an opponent's card has its owner
make (Change of Plans 1_99): "I approve of your reasoning; Keep." Q-0217, a companion EXCHANGED out of the
discard pile into hand (Borne Far Away 10_14) under 94_30: "we treat this like the persona replacement of the V3
gollum cards, where a swap isn't a play/discard, so here it's not taken into hand. I may come to regret this,
but we gotta start somewhere. B." Q-0218, a Block or Set the player picks still narrows his site list under
Race Text 92_19: "Keep." Q-0219, the 29 Hobbit sites under 92_19 in PC Movie: "Let's do B" (held out: a site
must be of the Fellowship, Towers, King or Shadows block, or of the format's own). Q-0221, Úlairë Nertëa 69_38
and the three Morgul Orcs of set 7, a cost of assigning paid as the assignment is made: "Excellent work. A."
(built for all four, `assignmentCost` retired). F-0345, Boromir 18_40 fetches only a [gondor] possession: "this
is an oversight, should have permitted any possession that can be played on Boromir." F-0346, Wish For Our
Kinfolk 8_8 lets a Dwarf ally pay "Exert a Dwarf companion": "Yup, clearly a bug." F-0364, Gamling 18_99 offers
possessions he cannot bear: "definitely laziness in the implementation, approved." F-0363, a follower's Aid at
Streets of Bree 18_140, where followers cannot be transferred: "B is the right call; such followers should be
marked as noeffect when their aid is offered, so that the UI can indicate it won't do anything, but if they
*want* the burden or whatever (and many such decks do), they can still attempt to pay it and get nothing out of
it, just like any other legal cost-with-no-effect action." F-0376, Morgul on the March 7_195 counting a card
taken into hand as a card drawn: "this pattern has happened with Decipher cards before, where two related
actions are bound by the hip in their templating and always go together, to the point that they're treated like
synonyms...until they aren't. I think I approve of the definition change that literally adds orTakesIntoHand"
(the word answers a draw alone unless the definition says `orTakesIntoHand`, which Verily I Come 2_94 and
Betrayal of Isengard 3_29 and 12_25 then say). G-0051, a follower's Aid cost read by a test: "I would make it
`GetAidTwilightCost` to emphasize that it's only managing the case of twilight; if a more generic
effect-returning function is needed I could also see `GetAidCost` as that more generic version." G-0053 and
G-0054: "Approved." *[2026-10-10]*

**Card by card (2026-10-10, fourth notes).** Q-0225, a Free Peoples player's own assignment action is not offered
for a companion who already has a minion, whatever his defender bonus: "Very good, keep. That's one of the more
common mistakes people make, that assignment actions can't actually be used like normal assignments due to the
rules restricting their use." Q-0226, may a site be replaced by another player's copy of the same site
(Pathfinder 1_110): PARKED by her: "I think this was added in 2005, i.e. around set 12-13. We may need to add a
new format rule switch for this as a result. For now park this while the analysis continues and while I inquire
at the PC; we may have a whole slew of such rulings to add to the pile." Q-0227, Held 6_109 does not corrupt a
hindered Frodo at site 9: "I think for now we'll go with A until someone complains, lol. Held has always been a
card about utilizing it until you counter it at the end, and hinder is one potential counter." Q-0232, The One
Ring when its bearer is killed: "If the Ring-bearer is killed, let's ensure the One Ring is moved to the same
position the Ring-bearer previously occupied, including in skirmishes (as the skirmish UI will very frequently
leave a finished game in the state of the last skirmish with a blank spot where the One Ring used to be). Don't
worry about the front-end for this, just take a stab at ensuring the server issues the proper PCIP etc events
down the chain. When Frodo dies and Sam is in play with his ability, there should thus be a brief moment (until
the response is accepted by the player) where the Ring is where Frodo once was, before Sam's ability kicks in
and transfers, with gameplay resuming as normal. So I guess this is B+, haha." Q-0234, Many Miles 13_21: "if
"current site" is meant to literally be the site the fellowship is currently at (which seems beyond reasonable),
then I think A is it." Q-0235, the three old tests kept in 2022 as Legacy classes in 1_31's and 1_9's files:
"those transferred tests were good to delete with A; they are now covered better elsewhere." *[2026-10-10]*

**Card by card (2026-10-11, first notes).** Q-0242, Frodo, Courteous Halfling 4_301's bar on Shadow discards from
hand shown stopping Hand of Sauron 3_90: "keep". F-0418, Morgai Foothills 8_110 discarding no card from hand
(`select: self` in its discardFromHand): "yeah clearly a bug". "Approve all F- items lower than F-0420."
*[2026-10-11]*

**Card by card (2026-10-11, fifth notes).** F-0469, the replay summary's starting fellowship
(`ReplayMetadata.ParseReplay`): "Aha! I knew there was something fucking up our stats which made attached cards
appear to not be in play, and this is why! Excellent catch, please tweak the statement." F-0478, Rush of Steeds
11_157 answering an exertion that is an effect: "Lavastone Giant reads: "Archery: Exert this minion to reveal a
random card from your opponent's hand. If it is a Shadow card, the minion archery total is +2. Otherwise, exert
this minion." In principle I'm fine with adding the cost flag to exert, but if the HDG is the only place this
happens, we will defer until then to address it." (it had been built by engine pass 27 half an hour before these
words arrived; she was told). "I approve of all F- items below F-0478 that are crurently fixed without word."
*[2026-10-11]*

**Card by card (2026-10-12, fifth notes).** Q-0287, marking replay summaries made after the F-0469 fix:
"Hmm, yeah, go ahead and bump it." (`ReplayMetadata.MetadataVersion` is 4, 78fdfae69.) *[2026-10-12]*

**Card by card (2026-10-12, sixth notes).** Q-0289, The Witch-king's Beast 12_184's additional phase and a
relentless round made one round, with The Witch-king and the relentless minions in it: "Well done, I approve. A."
Q-0292, `gen_play.py` no longer counts a twilight cost the text gives another card as the card's own (pass 31's keep
list is 9 cards): "keep". Q-0293, Treebeard 15_38 measures a removed loser as it left the skirmish, by
`memorizeLoserStrength`, `memorizeLoser` unchanged: "keep both". F-0488 (Flung Into the Fray 8_71, "stacked here")
and F-0489: "good". *[2026-10-12]*

**Card by card (2026-10-12, ninth notes).** Q-0295, the minions with a cost of assigning (Morgul Regiment 7_197,
Morgul Spawn 7_200, Morgul Spearman 7_201, Úlairë Nertëa 69_38) offered ahead of his assigning only where he could
then assign them, and a payment that fails not offered again in the same assigning: "keep both". Q-0297, Into the
Caves 17_100's bar keeps the minion to what it had when the bar was laid (no more of a keyword it has; one lost by
its own text may come back up to that): "A". F-0490, Uruk Guard 1_147's bar on assigning names no side, so it stops
the Shadow player too: "yeah looks like a bug to fix" (`side: free people`, as Uruk-hai Guard 12_156). *[2026-10-12]*

**Card by card (2026-10-12).** F-0482 (Mountain-troll 15_112's discount counted ahead): "Go for it". F-0483 (the
same card asking whether to discard where the pool cannot pay without it): "Also good". F-0486, Éowyn,
Northwoman 17_96's last exertion with a Morgul Orc the only minion: "I'm inclined to agree with the agent's
corner-of-a-corner decision. Let's leave this test parked for potential work in the future, but Northwoman and
these minions have gotten enough attention spent on them for now. :)" (engine pass 28 had already built it when
these words arrived; told so: "I think F-0486 got its status changed since I wrote that entry, so "parked" may
not make sense anymore now that it's addressed." It stands as built.) "Good, maybe we can run two engine agents for a bit". *[2026-10-12]*

**Card by card (2026-10-13).** Q-0298, Saruman's Staff 4_174 withheld by the engine's rule instead of a
requirement naming the two subtitles: "keep". Q-0299, Athelas 1_94's "remove a Shadow condition" is a discard: "A,
this is early installment weirdness. Although actually it occurs to me, we should keep track of weird wording for
a potential spring cleaning update in the future that fixes these sorts of things." (the list is
`<state>/wording.md`). Q-0300, Troop of Uruk-hai: a strength bonus a weapon gives by its own ability is lost too:
"A". F-0492, Horn of Boromir 3_42 not offered with no ally to spot: "Approved." F-0491: under "How card text is
read". Q-0301, "choose to move again this turn" forces one move, not every move left that turn (Osgiliath
Channel 8_120; the same words on No Retreat 4_30, Forced March 13_108, Hornburg Wall 5_118): "A, I think." *[2026-10-13]*

**Card by card (2026-10-14).** F-0503, Ranger's Cloak 15_68 borne by an ally (the forest half now a companion
bearer's alone): "if the ally is at his home site and the fellowship is at the *same* site, he should get the bonus
if the site is a forest. Assuming blocks are correct. However this is overwhelmingly unlikely to occur given that this
is an Expanded-era card and I don't think any format can permit such an interaction except Anything Goes, so it's not
worth tuning up; leave it as is." F-0502, Merry and Pippin, In the Bloom of Health (17_107, 17_109), "Add 1 to the
number of Ents you can spot": "the "you" in their text is referring to its owner and not to anyone else. That's
valid, I think; carry on." A spot bonus counts for its card's owner only (engine pass 39). *[2026-10-14]*

**What a test must lay (2026-10-14, the Standard's questions from engine pass 39).** Q-0303, the Hobbit-set cards
among a bare title's candidates: "We don't need to consider the HDG for this, no. (their own tests can include
those, but we don't need to make all Legolas tests everywhere more bloated for no reason; HDG isn't played in
constructed and so doesn't overlap with other sets. So even within its own tests it might not make sense.)" Q-0305,
"When you play" on a companion played into the starting fellowship: "I think this one is valid to check on everyone
(where possible), given that the context there is different enough (given the phase it's in) for situations to
crop up." Q-0306, a bare title's candidates: "Frankly I think "each type and culture" is a lot, especially for the
Sarumen who are the most diverse card bar none. I'd say just cross type boundaries (companion/ally) and culture
boundaries (isengard/dunland) and call it a day." Q-0307, reading a cheated state before the action: "I think the
Nazgul example seems like it should be checked, just put it in the discard pile *sooner*. But I can see where the
first one is trickier; if it's discarded as soon as it's attached, then it can't be seen on the proper spot without
shenanigans. I'd say A except for the case of instant removal." Q-0308, an "X or Y" cost owes a board where both can
be paid and the player is asked: "B, but I don't think older tests need relitigated." Q-0309, a trigger on the
card's own win owes its own loss only: "I think A is sufficient here." *[2026-10-14]*

**Card by card (2026-10-14, later).** Q-0304, a Skirmish card's "not offered" check may be read in either
neighbouring phase (assignment or regroup): "Ah yeah, B is fine". Q-0310, Sting, Weapon of Heritage 11_173, the Sam
sentence printed after the Maneuver action: "This was a poorly-templated card; we will treat the ability as if it
were in the proper place above the special ability; they at least had the decency to use proper linebreaks to make
that readably distinct. A" (a standing trigger; Q-0164 reaches only a sentence that can act after its action, as the
burn-out clauses). Q-0311, Gwaihir, The Windlord 17_20, an exhausted Wizard minion may be chosen and is wounded: "A is
fine, the "instead" is superfluous and perhaps should have been worded "exert a minion (or wound it if it is a
Wizard)". A." She also corrected the question: not every Wizard minion is a Saruman; "there are also the Deceived
wizards that emerge from conditions (Pallando Deceived, Alatar Deceived, etc)" (13_x [isengard]). *[2026-10-14]*

**Q-0312, a Hobbit-set card that is the only printed card of its kind (2026-10-14).** Stern Words 19_9 ("spot a
[gandalf] Wizard"), whose only [gandalf] Wizard ally is Saruman, The White 32_24: "Keep I guess; as you say it's a
small test, it's just not exemplary to involve the HDG that way." The test stays; Q-0303 still keeps the Hobbit sets
out of a bare title's candidates, and a Hobbit-set card is laid only where it is the one card that tells two
definitions apart. *[2026-10-14]*

**"If a <kind> ... is about to be discarded ... to prevent that" saves the one card (Q-0313, 2026-10-14).** Scouring
of the Shire 18_112 (and 68_112; Gimli, Feared Axeman 7_7 and Deceit 18_29 built the same): "Yeah, keep it at A.
Scouring is OP as it is (Thus the errata nerfing it), and it canceling all nukes would be even crazier." One token
saves one [shire] card; Q-0250's whole-discard reading is for a text that names "one or more" of a kind (Siege
Engine 5_60). *[2026-10-14]*

**"You may X. Then, Y": the "Then" sentence is a separate, required effect (Q-0314, 2026-10-14).** Home and Hearth
12_122, "Each time you play a Hobbit, you may remove a burden. Then, if the fellowship is at dwelling site, heal the
Ring-bearer": "I would expect the "then" to signify a separate effect, so that it is a required effect that is
evaluated after the burden's 0-1 choice is resolved." So the trigger is required, the burden removal a 0-1 choice,
and at a dwelling the Ring-bearer is healed whether or not a burden was removed. *[2026-10-14]*

**A wound is not owed against an "each time X exerts" trigger card by card (Q-0315, 2026-10-14).** The reverse of
Q-0265: "So long as the relevant triggers are tested in the mechanics tests, I'd say this also doesn't need to repeat
it on all relevant cards. A." It is shown once, in a mechanics test (a wound does not set off an exertion trigger).
*[2026-10-14]*


**A forced assignment with one candidate is the forcing player's, for every reader (Q-0316, 2026-10-15).** Engine pass
45 (F-0513) credits an assignment the Free Peoples player is made to make with no choice to the Shadow player, in the
log, `assignedToSkirmish`, `assignedAgainst` and ambush alike: "keep". F-0513 is fixed-approved. *[2026-10-15]*

**An unprinted guard on "Play Saruman from your draw deck" stays (Q-0317, 2026-10-15).** Deeping Wall 4_348 (and
4_332, 4_341, 4_315): the ability is not offered while a Saruman is in play: "A". *[2026-10-15]*

**An ally is at each of his home sites laid on the path (Q-0318, 2026-10-15).** Aldor 4_262 (3T and 6T) is at Westemnet
Plains as site 3 while the fellowship is at site 6 (engine pass 48, `atSite`): "keep". *[2026-10-15]*

**"If at a <terrain>" with no character named reads the fellowship's site, for now (Q-0319, 2026-10-15).** Release the
Angry Flood 2_19, Hospitality of the Dwarves 101_1: "Oooh, interesting. That's going to need a ruling. We'll keep
this for now, but that's another thing to bring to the Rules Council." Kept as built; for the Rules Council.
*[2026-10-15]*

**A card discarded from hand as a cost is not the card taken back (Q-0320, 2026-10-15).** Final Account 11_31 and 61_31:
"The precedence of the They Are Coming ruling applies here: discarding cards from hand as a cost to play from the
discard pile cannot then retrieve those discarded cards. A." So Q-0192 reaches events too: the cost's cards are
memorized and left out of the take; 61_31's landed test is turned round. *[2026-10-15]*

**Voluntary loops (Q-0321, 2026-10-15).** Sméagol, Slinker 5_29 against Anduin Banks 7_341, CR 4.2 "loops": "As of
today there is no handling of infinite voluntary loops on Gemp; and in fact this comes up with some regularity, where
two stubborn players keep playing the same actions ... I would like loop support to be added, I'm just uncertain what
form it would take. Keep a running tally of top-level actions performed, and intervene when both players choose the
same non-pass actions 3 times in a row? Attempt to detect when the cost of one action is the effect of the opponent's
action? If a subagent can investigate and propose how the server-side aspect would work, I'm willing to consider how
to handle this." She believes involuntary loops are handled (Frodo, Weary From the Journey against Black Land
Observer). Until a design is approved the alternation stands and 5_29's test reads it; the proposal is an engine-pass
item that builds nothing. *[2026-10-15]*

**Several burdens or threats moved by one effect are counted one by one (Q-0322, Q-0324, 2026-10-15).** The Shire
Countryside 3_113 ("each time you remove a burden"): "A"; Tom Bombadil's Hat 0_60 ("each time a Shadow card adds a
burden or a threat"): "B". Two removed or added at once set the trigger off twice, as built. *[2026-10-15]*

**A Decipher card is built as printed, not narrowed to the skirmish (F-0527, 2026-10-15).** Vengeance 4_208, "Make an
[isengard] tracker strength +2 for each card borne by the character it is skirmishing": "I think sadly we need to go
B." `InSkirmish` comes out of the choice; a tracker in no skirmish may be chosen and gains nothing. Q-0095's text
change stays for PC cards whose text she updates. *[2026-10-15]*

**A defender assigned to two minions counts each minion (Q-0323, 2026-10-15).** Weary 4_212, "Each time the Free Peoples
player assigns bearer to skirmish an [isengard] tracker, bearer must exert": "I think B." Two [isengard] trackers in
one answer are two exertions. *[2026-10-15]*

**"Discard ... until you choose to stop" discards one first (Q-0326, 2026-10-15).** Mithril Shaft 1_22: "A". *[2026-10-15]*

**A site-replacing card of a block lays its own block's site, on any path (Q-0327, 2026-10-15).** Paths Seldom Trodden
1_222 (and 1_233, 2_55; Grimbold 7_233 for the King block): "So long as the player's site in their adventure deck is a
Fellowship block site, we'll let it work. This means a default FOTR block path (of course), and Multipath if the owner
has chosen Fellowship sites (which means it could replace a Towers or King site with a Fellowship site). This would
continue to be barred from replacing a Towers site with a King site or vice versa, or replacing a Fellowship site
with a Towers/King site." *[2026-10-15]*

**"A [Sauron] Orc kills a companion" is CR 4.2's definition (Q-0328, 2026-10-15).** Orc Butchery 1_265, The Ring's
Oppression 1_273, Goblin Spear 1_182: "B, good find." A kill counts when the game text that kills comes from a
[sauron] Orc minion card, or the companion loses a skirmish involving a [sauron] Orc; a kill in that skirmish by
another minion's wound does not. Q-0123's loose reading gives way to the definition. *[2026-10-15]*
