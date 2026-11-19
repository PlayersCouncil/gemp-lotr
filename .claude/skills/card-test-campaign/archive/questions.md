# Questions for Ketura

Each question says which card or rule it is about, the readings on offer, and what each would mean for the tests.
An answer is copied into the rulings log in the campaign skill, and the question is marked answered here.

## Open

## Answered

### Q-0411 Keep or strike: Danger Wrapped in Shadows 32_37's unprinted requires standing in for removedTwilight inside a choice

"Shadow: Remove (2) to play a [sauron] card from your draw deck or discard pile (limit once per phase)." (Danger Wrapped in Shadows 32_37)

Built now (F-0761, fixed at landing): the ability carries a `requires` that the text does not print, `or( playable fromZone DISCARD, filter culture(sauron), discount: 2 ; hasCardInDrawDeck culture(sauron) )`. The `discount: 2` of `playable` is a cost modifier, so it stands for the (2) the cost will take. With it the ability is withheld at 5 in the pool when the only [sauron] card is Orkish Veteran 32_43 (2, +2 roaming) in the discard pile, and offered and played at 6. The usual correction for this shape (F-0046: `removedTwilight: 2` on the discard play) cannot be used here. Inside a `choice` the engine asks each branch again after the (2) is paid and subtracts it a second time, so at exactly enough twilight the (2) is taken and nothing is played.

**A, keep.** The `requires` stays. The card plays right, and nothing outside 32_37 changes. The cost is a requirement whose numbers do not read as the text does (a "discount" of 2 that is really +2), and the same trick would be needed on any later card that puts a twilight cost beside a choice of piles. 32_37 is the only Hobbit card of Q-0406's list that has one.

**B, strike, and change the engine.** PlayCardFromDiscard stops counting removedTwilight once the costs are paid. 32_37 then takes `removedTwilight: 2` on its discard branch, as F-0046's other cards do, and the `requires` comes off. The tests stay as they are: all 12 assert the same outcomes either way. The change touches the engine for every card, not only Hobbit cards.

Parked on it: nothing. All 12 of 32_37's tests are live and pass under A, and would pass under B.

Recommendation: A for now. B is the cleaner fix and is filed with the orchestrator as an engine proposal. If it is built, 32_37's `requires` is swapped for `removedTwilight: 2`.

**Ketura (2026-11-05):** Keep.


### Q-0417 Dol Guldur 32_46: who is 'you' in 'At the start of the Shadow phase, you may discard Gandalf'?

"At the start of the Shadow phase, you may discard Gandalf. If you do, Wise allies participate in skirmishes until the regroup phase." (Dol Guldur 32_46, site 5.)

Built now: an optional trigger with `requires: isSide shadow`. At the start of the Shadow phase the Shadow player of the turn is offered it; taken, the Free Peoples player's Gandalf is discarded and that player's Wise allies (Elrond 30_21, Saruman 32_24, all with homes at 3H) may then be assigned to skirmishes until the regroup phase. The Free Peoples player is not offered it.

**A** (as built): "you" is the Shadow player. The Shadow player trades a Gandalf off the table for letting the Free Peoples player's Wise allies fight. This matches Old Forest Road 30_62, the other Hobbit site 5 ("At the start of the Shadow phase, you may discard Gandalf to play a minion from your discard pile", built `isSide shadow`), and Mirkwood 30_53, which discards Gandalf outright. The active tests assert this.

**B**: "you" is the Free Peoples player, who discards his own Gandalf so that his Wise allies may fight at site 5: the one who pays is the one who gains. The requires becomes `isSide free people`, and in each offer test the Shadow and Free Peoples offer reads swap (the accept and decline calls move to the Free Peoples player); nothing else in the tests changes.

No test is parked: the tests of reading A pass and stay active. No ruling settles a bare "you" on a site's Shadow-phase text outside the meta-sites (Q-0183, Q-0188).

Recommendation: B is the likelier design (the reward goes only to the Free Peoples player, which makes no sense as a Shadow player's choice), but A matches how the sister site Old Forest Road is built; Ketura's knowledge of the HDG's intent should decide.

**Ketura (2026-11-05):** I'd say the Shadow player as it is now. The second effect is I think a compensation for the powerful effect of discarding Gandalf, not a purchased effect per se.


### Q-0419 Beorn's House 31_44: 'spot a [gandalf] staff or play it from your draw deck' with no staff in play or deck, offered (as built) or withheld?

"Fellowship: Spot a [gandalf] staff or play it from your draw deck to prevent Gandalf from being discarded until the end of turn."

Built now: the cost is a choice of a spot and a play from the draw deck, and the play branch has no `mustExist`, so the ability is always offered. With no [gandalf] staff in play and none playable from the deck, a use shows and shuffles the deck, plays nothing and protects nothing (on a throwaway board Gandalf was then discarded at Mirkwood). With a staff in play, the choice of ways is put even when the deck holds no playable staff.

**A** (as built): offered always. Q-0019's "no penalty for failing to play from the draw deck" read plainly. The parked test is dropped.

**B**: withheld. `mustExist: true` on the play branch, as Q-0406 did for the Hobbit cards' "from your draw deck or discard pile" (Q-0019: "a pretty shit UX to let the player play when it's not in the draw deck"). With nothing in play or playable in the deck, the ability is not offered; with a staff in play and none playable in the deck, the spot is taken without asking. mustExist also respects uniqueness and item class: a unique Wizard Staff 31_18 in the deck does not count while a 'Wizard Staff' is in play, nor while the only Wizard bears a staff. Shown with mutate.py: the parked test passes and all 7 active tests still pass.

Parked: Card_31_044_Tests.IsNotOfferedWithNoGandalfStaffInPlayOrInTheDrawDeck.

Recommendation: B, the same UX reasoning as Q-0406; the wording is "spot X or play it from your draw deck", which neither Q-0019 nor Q-0406 names.

**The orchestrator (2026-11-05), settled on Q-0019 and Q-0406/Q-0418:** B. A play from the draw deck with nothing to find is withheld. `mustExist: true` on the play branch in the next engine pass; the parked test goes live. It stands unless Ketura strikes it.


### Q-0418 Blunt The Knives! 33_4: is 'play Balin from your draw deck' offered inside the 'choose one' when no Balin can be played?

"Choose one: draw 2 cards; play Balin from your draw deck; or place 2 cards from your discard pile beneath your draw deck."

**As built:** the Balin branch is a `playCardFromDrawDeck` with no `mustExist`, so the game counts it as doable in full on every board, Balin in play or not, Balin in the deck or not (as Q-0019 has it for a play from a draw deck: no penalty for failing to find the card). With Balin already in play and one card in the discard pile, the game asks draw-or-play-Balin instead of drawing unasked. With a one-card deck it asks play-Balin-or-place instead of placing unasked. With a one-card deck and one card in the discard pile it carries out the Balin branch unasked: a look through the deck that plays nothing, the draw and the place never offered.

**A (as built):** the branch is always on offer. Choosing it with no Balin to play is a legal use that looks at the deck and shuffles it. The three parked tests go, and the active tests stand as they are.

**B:** the branch is withheld when no Balin can be played (CR 4.2 "effect": a choice must be one the player is fully capable of performing; F-0310). This is Q-0406's correction carried to a draw-deck-only play: `mustExist: true` on the branch's `playCardFromDrawDeck`. Then the other branches are taken unasked or offered as F-0310 says, and with no branch doable in full all three are offered and the one chosen does what it can. Q-0406's list (cards worded "from your draw deck or discard pile") does not take this card in, which is why it is asked. Under the correction all 9 tests pass; against the card as it is the three parked tests fail at "Choose action to perform".

**Parked on it:** Card_33_004_Tests WithBalinInPlayAndOneCardInTheDiscardPileTwoCardsAreDrawnUnasked, WithBalinInPlayAndOneCardInTheDeckTwoCardsArePlacedUnasked, WithNoBranchPossibleInFullEachIsOfferedAndTheDrawTakesTheOneCardThere.

**Recommendation:** B. Balin in play is no hidden-zone case, and the deck is the player's own to search; offering a branch that can play nothing against two that can be done in full is the same poor UX Q-0019 named. (The Bridge of Khazad-dum 1_349, "Play The Balrog from your draw deck or hand", is built as A and landed so; an answer of B here leaves it alone unless Ketura says otherwise.)

**The orchestrator (2026-11-05), settled on Q-0019 and F-0310:** B. Q-0019 withholds a play from the draw deck with nothing to find ("a pretty shit UX"); inside a 'choose one' that branch is then not fully doable (F-0310). `mustExist: true` on the Balin branch in the next engine pass; the three parked tests go live. The Bridge of Khazad-dum 1_349 is left as it landed. It stands unless Ketura strikes it.


### Q-0416 Terrifying Legend 32_53: with Smaug spotted and no weapon in play, do the allies still exert?

"Each time a companion is killed, you may discard a weapon (and exert each ally if you spot Smaug)."

Built now: the optional trigger discards a chosen weapon, then, if Smaug is spotted, exerts each ally. With Smaug spotted and no weapon in play, the trigger is offered and, taken, exerts each ally though nothing is discarded.

**A** (as built): the parenthesis is a second effect of the same "may", done as far as it can be (CR 4.2 "effect": do as much as you can; as with an unconditional parenthesis, F-0554, and Q-0412 A). With Smaug in play and no weapon, the Shadow player may still take the trigger to exert each ally. The active test WithSmaugSpottedAndNoWeaponTheAlliesStillExert asserts this.

**B**: the exertion goes only with a weapon actually discarded ("and" ties it to the discard). With no weapon, the trigger would do nothing and is not offered. WithSmaugSpottedAndNoWeaponTheAlliesStillExert flips to "not offered", and the card's requirement (F-0788) becomes `canSpot weapon` alone.

No test is parked: the test of reading A passes and stays active.

Recommendation: A, as built; it is CR 4.2's "as much as you can".

**The orchestrator (2026-11-05), settled on F-0702 and Q-0412:** A. Ketura on F-0702: "effects should do as much as they can in that case where the other half of the choice isn't doable." With Smaug spotted and no weapon, the allies still exert. Nothing changes. It stands unless Ketura strikes it.


### Q-0415 Caught In A Sack 31_57: may it be transferred again from the Dwarf bearing it?

"Discard this condition if Bilbo wins a skirmish. **Regroup:** Discard a minion (or exert a Troll) to transfer this condition to a [dwarven] companion. Bearer cannot be assigned to a skirmish by any player." (Caught In A Sack 31_57)

Built now: the transfer has no `fromSupport`, so once a Dwarf bears the Sack the Shadow player's Regroup ability is still offered, its price (a minion discarded, or a Troll exerted) is paid again, and the Sack moves to another [dwarven] companion. The card prints no "from your support area" (Q-0022: asked card by card).

- **A (the letter, as built).** It may be moved on from the Dwarf bearing it to another, once per price paid. Nothing changes. `Card_31_057_Tests.ItMayBeMovedOnFromOneDwarvenCompanionToAnother` reads it moved from Balin to Dwalin for the Goblin Runner's discard; it is live and passes.
- **B (as Fifth Level 10_32 under Q-0349: B).** Only a Sack in the support area may be transferred: `requires: canSpot self,zone(SUPPORT)` beside `fromSupport: true` on the transfer, and that test turns round to read the ability not offered for the Sack on Balin.

Nothing is parked: the game passes reading A and its test is live.

Recommendation: A, lightly, as Soldier's Cache (Q-0369: A): each move costs the Shadow player a minion or a Troll's exertion and takes a second Dwarf out of the skirmishes, so moving it on does something.

**The orchestrator (2026-11-05), settled on Q-0369:** A. Ketura allowed Soldier's Cache to re-transfer because each move does something; each move of the Sack costs the Shadow player a minion or a Troll's exertion and takes another Dwarf out of the skirmishes. Nothing changes. It stands unless Ketura strikes it.


### Q-0414 Crown of Erebor 32_1: is the heal its own 'may' beside the doubt, or the whole trigger's offer?

"Each time you play Roäc, you may heal Thorin or Dain Ironfoot. Then, you may remove a doubt."

Built now: one optional trigger. Taking it heals Thorin or Dain Ironfoot (a required choice of one, wherever either is wounded), then asks a yes/no to remove a doubt (asked only while there is a doubt, F-0782). Declining it does neither.

**A (as built):** the opening "you may" is the trigger's. A player who wants only the doubt removed must also heal a wounded Thorin or Dain; one who wants neither declines. The active tests take this reading and the game passes them.

**B (Q-0314 read across):** "Then" begins a separate effect, as Ketura ruled on Home and Hearth 12_122, and here that effect has a "may" of its own. The trigger becomes required, the heal a 0-to-1 choice, and the doubt removal its own 0-to-1 after it, each taken or left independently. Tests would decline the heal and still remove the doubt; the decline test would become two zero answers. shapes.md sets apart a "Then" that has its own "may" (Great Gate 103_23), so Q-0314 does not settle it outright.

No test is parked: the game passes reading A, and no test of B is written until the answer.

Recommendation: B, the nearer reading of Q-0314; the only outcome it adds is removing a doubt without healing.

**The orchestrator (2026-11-05), settled on Q-0314:** B. "Then" begins a separate effect (Ketura, Q-0314), and each "you may" is its own choice: the trigger is required, the heal 0 to 1, the doubt removal 0 to 1. The correction to 32_1 goes in the next engine pass. It stands unless Ketura strikes it.


### Q-0413 The Elvenking 33_34: does its Response answer an opponent's discard of the Elvenking itself?

"Response: If [gundabad] and/or [mirkwood] conditions are about to be discarded by an opponent, discard this condition to prevent that." (The Elvenking 33_34)

Built now: the Response's trigger filter is `another,condition,or(culture(gundabad),culture(mirkwood))`. The Elvenking is itself a [mirkwood] condition, but the "another" leaves it out. So when an opponent discards the Elvenking alone (Ancestral Knowledge 30_1 choosing it), no Response is offered and the Elvenking goes to the discard pile. When the discard also holds another [gundabad] or [mirkwood] condition, the Response is offered as usual. Siege Engine 5_60 ("If one or more machines are about to be discarded by an opponent, discard this condition to prevent that") is a machine too, and its definition has no "another".

**A, keep "another".** The Elvenking's own discard alone is not answered. Paying for the Response means discarding the Elvenking, which is the very card the opponent is about to discard, so the offer would cost nothing new and save nothing (and Q-0038 says a prevented discard that was a cost counts as unpaid). The test NotOfferedWhenTheElvenkingAloneIsDiscarded asserts this and is live.

**B, strike "another", as Siege Engine is built.** The Response is offered when the Elvenking alone is about to be discarded. That means a prompt whose cost is the discard being prevented: a circular case the engine does not settle, which would need its own ruling and a test of what is left in play. NotOfferedWhenTheElvenkingAloneIsDiscarded would be turned round to assert the offer and what it leaves.

Parked on it: nothing. The test of A passes as built and stays live.

Recommendation: A. The text's words reach the card itself, but the only thing the offer could do is answer its own discard with its own discard.

**The orchestrator (2026-11-05), settled on Q-0038:** A. Answering its own discard with its own discard saves nothing, and Q-0038 counts a prevented discard paid as a cost as unpaid. Nothing changes. It stands unless Ketura strikes it.


### F-0772 Spear of the Guard 33_28: 'Bearer must a [men] Man.' printed with no 'be'

"Bearer must a [men] Man." (Spear of the Guard 33_28, its first sentence as the picture prints it, with no "be".)

Built now: the definition's gametext copies the picture, "Bearer must a [men] Man.", and the card plays as "Bearer must be a [men] Man." (`target: culture(men),man`); the tests pass on that.

**A.** Write the corrected words: the gametext reads "Bearer must be a [men] Man." Players see the sentence the card means; nothing in play changes; no test changes (no test asserts the gametext). This is how The Elvenking 33_34 (F-0738) and The Arkenstone 31_7's colon (F-0739) went.

**B.** Keep the printed slip. The client shows what the picture shows; nothing in play or the tests changes.

Parked: nothing.

Recommendation: A, as with F-0738 and F-0739.

**The orchestrator (2026-11-05), settled on F-0738 and F-0739:** A. Ketura corrected the picture's misprints in both; this is the same. The gametext gets "be" in the next engine pass. It stands unless Ketura strikes it.


### Q-0412 Hidden Attack 32_38: a character in the dead pile and only one [sauron] Orc that can be played: the event is played for the one, or not at all?

"Spot an Orc to play a [sauron] Orc (or 2 [sauron] Orcs if you spot a character in the dead pile) from your discard pile." (Hidden Attack 32_38)

Built now: the event requires an Orc to spot; it plays one [sauron] Orc from the discard pile, then an `If` on `HasCardInDeadPile character` plays a second. The playability check looks at the first play only. So with a character in the Free Peoples dead pile and only one [sauron] Orc in the discard pile that can be paid for, the event is played and that one Orc comes into play. With two that can be played, the second is played as well, and the player cannot decline it.

**A (as built).** With a character in the dead pile and only one playable [sauron] Orc, the event is played for that one Orc. This is Q-0062's "we should at least confirm that the first play is possible". When a second Orc can be played, it must be played (Q-0035, mandatory when available). F-0491's "up to" does not apply here, because every Orc offered comes from the Shadow player's own discard pile. Nothing changes. PlaysTheOneSauronOrcThereIsWhileACompanionIsInTheDeadPile and PlaysTwoSauronOrcsWhileACompanionIsInTheDeadPile (second choice minimum 1) stay as they are.

**B (Q-0389 read strictly).** Once the condition is met, the parenthesis is the only option, and the plays from the pile are its cost (Q-0062). With only one playable [sauron] Orc, the event cannot be played at all. The definition would take a `requires` asking for two playable [sauron] Orcs whenever a character is in the dead pile. The one-Orc test would then read the event as not playable, with a further test showing it playable once a second Orc can be paid for.

(A third reading, the second Orc optional at 0 to 1 on F-0491's "you spot", would change only the second choice's minimum, to 0.)

Parked on it: nothing. The game passes reading A, and the tests of A are live.

Recommendation: A. Q-0062 already speaks to a card played several times, and Q-0389 dealt with a price that could not be paid, not with an effect that can do only part of what it says.

**The orchestrator (2026-11-05), settled on F-0702 and Q-0062:** A. Ketura on F-0702: "effects should do as much as they can in that case where the other half of the choice isn't doable"; Q-0062 checks only that the first play is possible. With one playable [sauron] Orc the event plays that one. Nothing changes. It stands unless Ketura strikes it.


### Q-0410 William, Troll of Ettenmoors 31_61: does a follower's own transfer to the support area count as 'the Free Peoples player transfers a follower'?

"Each time the Free Peoples player transfers a follower, you may discard a hand weapon." (William, Troll of Ettenmoors 31_61)

Built now: a `transferred` trigger on a Free Peoples follower moved onto a character (`to: character`, side corrected by F-0756). A follower's Aid and Dwarven Warhammer 33_7's attach (Q-0407: B) make the offer. A follower's own "transfer <it> to your support area" (Gwaihir 31_15's Skirmish ability, Bofur 30_6's Response) does not: the engine's move to the support area raises no `transferred` result at all.

**A:** yes. The printed verb is "transfer" (No Visitors 15_150 and Q-0401 call the move to the support area a transfer too), so Gwaihir or Bofur going back to the support area offers William's discard. This wants an engine change, not just the definition: the transfer to a support area would have to raise a `transferred` result, and William's `to: character` widened to any destination. That result would reach every other card with a `transferred` trigger (Óin 30_16, Ori 30_18 and the like), whose own definitions would then need `to: character` where they mean a bearer. The parked test GwaihirTransferredBackToTheSupportAreaOffersTheDiscard goes live.

**B:** no. Only a transfer onto a character counts (an Aid, or the Warhammer's attach). Nothing changes; the parked test is turned round to read no offer, and goes live.

Parked: GwaihirTransferredBackToTheSupportAreaOffersTheDiscard (Card_31_061_Tests), which fails at William's offer.

Recommendation: B, unless the move back was meant to feed William: A costs an engine change that reaches every transfer trigger, and an HDG game that sends a follower home mid-skirmish rarely has a hand weapon in reach anyway.

**Ketura (2026-11-05):** B, we dont' need to trigger off of follower returns.


### Q-0409 Smaug, The Golden 30_66: is a Hobbit-set follower whose race is Dwarf, Man or Elf spotted for the discount?

"For each Dwarf, Man and Elf you spot, Smaug's twilight cost is -1." (Smaug, The Golden 30_66)

Built now: a standing discount over `or(dwarf,man,elf)`, which takes any card of those races on either side. A Hobbit-set follower prints a race (Ketura, 2026-11-04: "Followers have a race (Old Thrush is a Bird)"), so Bifur, Inarticulate 30_5, a [dwarven] follower of race Dwarf in the support area, takes Smaug from 16 to 15; Beorn, Skin-Changer 31_13 (a Man follower) would do the same.

**A** (as built): it counts. The race is printed on the card, and CR 4.2 "follower" refuses a race to followers only because a follower has none on its type line ("can't be spotted as such because a follower doesn't have a race on its card type line"). Nothing changes; the active test AFollowerWhoseRaceIsDwarfIsSpottedAsADwarf reads 15.

**B**: it does not count. CR 4.2's sentence is read as "a follower is never spotted as a race", and a follower is no character (F-0023). Smaug's filter would take a `character` (or `not(follower)`) word, the test would be turned round to read 16, and the same doubt reaches every Hobbit-set text that spots or counts a bare race word while a raced follower is in play.

Parked: nothing. The test of A passes as built and stays active.

Recommendation: A. The HDG gave followers a race on purpose, and a printed race is what "spot a Dwarf" looks for.

**The orchestrator (2026-11-05), settled on Ketura's HDG point of 2026-11-04 ("Followers have a Race, which normally is disallowed by the rules"):** A. The HDG gives its followers a race on purpose; a printed race is what spotting that race looks for. Nothing changes. It stands unless Ketura strikes it.


### Q-0405 Dark Sorcery 33_61 on the Hobbit Gandalfs 30_25 to 30_28: is the sentence after the keyword 'Wise.' their first sentence?

Dark Sorcery 33_61: "The first sentence of Gandalf's game text does not apply."

Each of the four Hobbit-set Gandalfs opens with the keyword line "**Wise**." and then a sentence:
- Gandalf, Leader of Dwarves 30_25: "Each time you reconcile, you may draw 2 cards."
- Gandalf, Friend of Thorin 30_26: "While you can spot 2 [dwarven] followers, Gandalf cannot be discarded."
- Gandalf, The Grey 30_27: "Each time you play a [gandalf] event, you may wound a minion (except Smaug)."
- Gandalf, Leader of the Company 30_28: "Gandalf cannot be discarded."

**Built now:** Dark Sorcery switches off the sentence AFTER "Wise." on each of them (each of those abilities carries `sarumanFirstSentenceActive`; that the flag is Saruman's is F-0037, confirmed, and is a separate matter). The keyword stays, and the rest of each Gandalf's text still works.

**A (as built):** the first sentence is the first one after the keyword line. Under Dark Sorcery 30_25 is not offered the reconcile draw, 30_26 and 30_28 can be discarded (Mirkwood 30_53 discards them), and 30_27 is not offered the wound. The active R tests of all four cards (R1 in 30_26, R2 in the other three) assert this, and nothing changes.

**B (LR 6 / Q-0016 read across):** LR 6 rules that Saruman's Staff has no effect on a Saruman whose text opens with keywords (12_54, 12_144). Read the same way, "Wise." is Gandalf's first sentence, and since every Gandalf of the Hobbit sets opens with it, Dark Sorcery's second sentence does nothing at all. The four Gandalfs would lose their gating, Dark Sorcery its modifier, and the four R tests would turn round (each ability still works with Dark Sorcery in play).

No test is parked on this: the active R tests take reading A.

Recommendation: A. Dark Sorcery exists only in the Hobbit Draft Game, and every Gandalf it can meet opens with "Wise.", so under B its sentence was printed for nothing; Q-0374 already declined to stretch LR 6 past the cards it names.

**Ketura (2026-11-05):** A


### Q-0407 Bilbo, Collector of Treasures 30_44: is Dwarven Warhammer 33_7's discard to attach a follower a discard 'to transfer a follower'?

"When you discard a card from hand to transfer a follower, you may draw 2 cards." (Bilbo, Collector of Treasures 30_44)

Dwarven Warhammer 33_7: "Maneuver: Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer (without paying the aid cost)."

Built now: an optional trigger on a discard from hand whose source is a follower still in the support area (as corrected by F-0741). A follower's own Aid paid by a discard from hand (Óin 30_16, Gwaihir 31_15) makes the offer; the Warhammer's discard, whose source is the Warhammer, does not.

**A** (as built): no. Bilbo's sentence follows the Aid's own wording ("discard a card from hand to transfer this"), and the Warhammer attaches the follower without the Aid, by a discard of its own. No offer; nothing changes. The active test CardsDiscardedFromHandToAttachAFollowerByDwarvenWarhammerDrawNothing reads it.

**B**: yes. Cards are discarded from hand in order to put a follower on a character, whatever card asks it, so the Warhammer's discard of 2 makes Bilbo's offer (once for the use, or once for each card discarded). The trigger would need to reach a discard by the Warhammer's ability as well (a second source, the Warhammer's discard of 2 marked as one or two offers), and the active test would be turned round to read the offer and the draw of 2.

Parked: nothing. The test of A passes as built and stays active.

Recommendation: A. "Transfer" is the Aid's word for the follower's own move; the Warhammer's text says "attach" and sets the Aid aside.

**Ketura (2026-11-05):** "attach" was bad wording.  Given that internally it uses the "transfer" action, then we should treat Bilbo as responding to the warhammer and oin should also trigger on it.  I would have worded that "attach" is not the same word as "transfer" but I'm not going to add an attach action for this.


### Q-0408 Óin, Knowledgeable Healer 30_16: is Óin attached by Dwarven Warhammer 33_7 'transferred' for 'When you transfer Óin, heal bearer'?

"Aid – Discard a [dwarven] card from hand. (At the start of the maneuver phase, you may discard a [dwarven] card from hand to transfer this to a companion.) When you transfer Óin, heal bearer." (Óin, Knowledgeable Healer 30_16)

Dwarven Warhammer 33_7: "Maneuver: Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer (without paying the aid cost)."

Built now: Óin's heal is a trigger on any transfer of Óin. The Warhammer's definition moves the follower with a `transfer` effect, so Óin attached by the Warhammer heals his new bearer.

**A** (as built): yes. The Warhammer's attach is a transfer (it moves a follower from the support area to a character, which is what the Aid's reminder calls a transfer), so Óin heals the bearer however he got there. Nothing changes. The active test AttachedByAnotherCardWithoutItsAidItStillHealsTheBearer reads it.

**B**: no. "Transfer" means the follower's own Aid; the Warhammer's "attach ... without paying the aid cost" is not a transfer of Óin, and the bearer is not healed. Óin's trigger would need to answer only his own Aid (or the Warhammer's move would need to be told apart from a transfer), and the active test would be turned round to read no heal.

This is the same word as Q-0407 (Bilbo 30_44, "discard a card from hand to transfer a follower"), seen from the other side: Q-0407's recommendation A (the Warhammer's attach is no "transfer" in Bilbo's sense) read across to Óin would be B here. One answer of what "transfer" covers should settle both, or the two be told apart on purpose (Bilbo's sentence is about the discard, Óin's about the move).

Parked: nothing. The test of A passes as built and stays active.

Recommendation: A. Óin's sentence names only the move, and the Warhammer moves him; Bilbo's turns on whose discard paid for it, which is why Q-0407 can be A there without B here.

**Ketura (2026-11-05):** "attach" was bad wording.  Given that internally it uses the "transfer" action, then we should treat Bilbo as responding to the warhammer and oin should also trigger on it.  I would have worded that "attach" is not the same word as "transfer" but I'm not going to add an attach action for this.


### Q-0406 Gandalf, The Grey 30_27: 'from your draw deck or discard pile' with nothing to play in either, offered (as built) or withheld as Q-0019's eight cards are?

Gandalf, The Grey 30_27: "**Fellowship:** Add 2 doubts to play an artifact or a Dwarf companion from your draw deck or discard pile."

**Built now:** the draw-deck branch has no `mustExist`, so with nothing playable in either pile the action is still offered; used, it adds 2 doubts (burdens), shows and shuffles the draw deck, and plays nothing. No Hobbit-set card that plays or takes a card "from your draw deck or discard pile" has `mustExist` (16 others: 30_23, 30_56, 31_14, 31_19, 31_42, 31_54, 32_10, 32_34, 32_37, 32_52, 32_54, 32_56, 33_13, 33_15, 33_21, 33_34), so one answer covers them.

**A (withheld, as Q-0019 has it for 5_30, 7_53, 8_92, 8_118, 11_106, 17_17, 101_28, 101_37):** `mustExist: true` on the draw-deck branch; with nothing to play in either pile the action is not offered and no doubts can be paid for nothing. The Card Test Standard's "Deliberate departures" paragraph states this for such cards generally. The parked test `FellowshipActionIsNotOfferedWithNothingToPlayInEitherPile` (Card_30_027_Tests) asserts it and goes live; every other test passes under the change.

**B (as built, and by the rules: Q-0019 says a failed play from the draw deck carries no penalty):** the action is offered with both piles empty of targets, and its 2 doubts are paid for a look at the deck. The parked test is struck, and the Standard's paragraph is narrowed to the eight cards Q-0019 names.

Recommendation: A, for the reason Q-0019 gave ("a pretty shit UX to let the player play when it's not in the draw deck"); two doubts are a heavy price to lose to a misremembered deck. Under A the other Hobbit cards above would be corrected the same way as their batches reach them.

**The orchestrator (2026-11-04), settled on Q-0019 and the Card Test Standard's "Deliberate departures":** A. Q-0019 withheld such a play with nothing to find ("a pretty shit UX to let the player play when it's not in the draw deck"), and the Standard states it for such cards generally; the 16 other Hobbit cards are corrected the same way as their batches reach them. It stands unless Ketura strikes it.


### Q-0400 Engrossed Hillman 15_78: does Q-0398's B reach it (may the other [men] Man be one of the 8 characters)?

"At the start of the maneuver phase, if you can spot another [men] Man and 8 characters (or if you can spot 3 Free Peoples cultures), you may take control of a site."

Built now: two separate spots, `another,culture(men),man` and 8 `character`, so the [men] Man spotted is also one of the 8 (and so is the Hillman himself). Batch dec-b15_75 folded this card into Q-0398 (One Last Surprise 15_33) as the same shape, "Spot X and a Y", but Q-0398 as you answered it named only One Last Surprise, and your words ("I think the two go hand in hand the way it's currently formulated") were about that card's wording. Engine pass 74 built B for One Last Surprise only and left this card as it is.

**A** (as built): the Man may be one of the 8 characters. 8 characters in all (the Hillman and the other [men] Man among them) open the site-control offer. Nothing changes; the active test at exactly 8 reads it.

**B** (Q-0398's reading carried across): the other [men] Man and the 8 characters are different cards, so 9 characters in all are wanted (the other Man plus 8, the Hillman among the 8). The 8-character requirement would count characters other than the Man spotted, and the + test at 8 would turn round to read no offer there, with a new + test at 9. The second branch (3 Free Peoples cultures) is untouched either way.

Recommendation: A, weakly. "If you can spot another [men] Man and 8 characters" reads as two conditions on the board (a [men] partner, and a crowded table) more than as two named cards, unlike "Spot Gandalf and an unbound companion ... to use Gandalf's resistance ... instead of that companion's strength", where the effect itself tells the two apart.

**Ketura (2026-11-04):** This one I think is A.


### Q-0401 No Visitors 15_150: may the Shadow player's follower on his own Hobbit pay 'transfer a follower from a Hobbit to your support area'?

"Transfer a follower from a Hobbit to your support area to cancel a skirmish involving that Hobbit." (No Visitors 15_150, a [shire] skirmish event)

Built now: the cost is `transferToSupport` of `choose(follower,attachedTo(hobbit))`, with no side named. The Shadow player's follower on the Shadow player's own Hobbit (Saruman, Servant of Sauron 17_38 on Ted Sandyman 18_75) is offered beside the Free Peoples followers, and once chosen goes to its OWNER's support area (the Shadow player's), not "your" support area. Taken in another companion's skirmish it cancels nothing; taken in Ted's own skirmish it cancels that skirmish and strips Saruman off Ted.

**A** (as built): a bare type word in a cost names no side (shapes.md, Erkenbrand's Horn 18_96 is paid with Saruman). The Shadow follower pays, lands in the Shadow player's support area, and the skirmish of the Hobbit who bore him is cancelled. Nothing changes; the parked test is dropped and one reading him offered takes its place.

**B**: "to your support area" is a destination a Shadow card cannot reach (Q-0364: no stealing, a card goes only to its owner's zone; CR 4.2 says a Shadow player cannot play a card to another player's support area), so only the player's own followers can pay. Correction: `choose(your,follower,attachedTo(hobbit))`. The parked test goes live (shown with mutate.py: it fails as built, and all 7 tests pass with the correction).

Parked: Card_15_150_Tests.TheShadowPlayersFollowerOnHisOwnHobbitCannotBeTransferredToYourSupportArea (Aragorn skirmishing Ted; the Worker and The Gaffer offered, Saruman not).

Recommendation: B. Erkenbrand's Horn's discard has no destination; here the text names "your support area", which, as Q-0364 reads a hand, cannot hold the other player's card.

**Ketura (2026-11-04):** Had it said "return...to the support area" I think it would work on follower Saruman, but given that it specifies your own support area, I think that makes this one B.


### Q-0402 Defensive Rush 15_159: under '(or heal an [uruk-hai] minion twice if you control a site)', is a 1-wound minion a candidate beside a 2-wound one

"At the start of each skirmish, you may remove (3) to heal an [uruk-hai] minion (or heal an [uruk-hai] minion twice if you control a site)."

Built now: while the Shadow player controls a site, the heal is a choice of any [uruk-hai] minion, healed twice (`times: 2` under `controlsSite`). An [uruk-hai] minion with one wound is chosen as freely as one with two, and is healed of its one wound.

**A**: as built. Any [uruk-hai] minion may be chosen for the twice-heal and is healed as far as it can be; with a 2-wound and a 1-wound minion in play, the Shadow player picks either. Q-0389 (a conditional parenthesis's only price is its condition, here a controlled site) reads this way. No change; the parked test is dropped.

**B**: read as Q-0392 reads "(or heal Sam twice)" and Q-0035 reads a parenthesis that can be taken: the twice-heal goes only to a minion that can be healed twice, so while one has 2 or more wounds it is the only candidate (chosen unasked if alone), and a 1-wound minion is healed only where no minion can take both heals. Q-0392 names a card; this text names a kind, so it does not settle it by its own words. B needs an engine change: the definition language has no filter for "has 2 or more wounds" (only wounded/unwounded and vitality bounds), and an `If` on `canSpotWounds` can switch the branch but cannot narrow the choice. Where no minion has 2 wounds the two readings play alike.

Parked: Card_15_159_Tests.WhileTheShadowPlayerControlsASiteOnlyAMinionThatCanBeHealedTwiceIsChosen (reading B: Tracking Uruk at 2 wounds beside Saruman, Master of the White Hand 17_116 at 1, a site controlled; the game asks for a choice between both).

Recommendation: B, for consistency with Q-0392's reasoning ("neither are fully capable of performing it"), if a wounds-count filter is worth building; otherwise A.

**Ketura (2026-11-04):** Let's go with A for this one; unlike New Chapter which does not specify a condition, this one does, and there's nothing wrong with choosing to heal an uruk twice that only has one wound.


### Q-0403 Uruk Cavern Striker 15_174: may the spotted Free Peoples site be one no marker has passed, such as the fellowship's current site?

"Skirmish: Spot a Free Peoples player's site on the adventure path and exert this minion twice to exchange the spotted site with a site you control. You now control the spotted site."

As built, the spot is `choose(not(your),site,zone(adventure path))`: any Free Peoples site on the path. That includes sites no marker has passed yet. In a probe the Free Peoples player's own site 6, the fellowship's current site, was offered and could be exchanged out from under the fellowship.

**A** (likelier): CR 4.2 "control" governs the control the text gives: "A Shadow player may only take control of a site on the adventure path if all player markers are on sites with higher site numbers." Only Free Peoples sites behind every marker can be spotted, so the fellowship's current site and the sites ahead of it are not offered. Correction: limit the spot filter to sites behind every marker. Adding `not(currentSite)` alone is enough to pass the parked test (the reviewer checked this with mutate.py). A full limit would also keep out the sites ahead.

**B** (as built): the card's own "spot" replaces the rule's choice of site entirely (the card already sets aside the rule's "lowest site number must be selected"), so any Free Peoples site on the path may be taken, the current one and those ahead included. The parked test is taken out and the probe's board becomes an active test showing site 6 offered.

Parked: Card_15_174_Tests.TheFellowshipsCurrentSiteCannotBeSpottedThoughItIsTheFreePeoplesPlayers.

Recommendation: A. The card sets aside which site is chosen, but nothing in its words lifts the marker rule, and taking the site the fellowship stands on is a much bigger effect than the card's cost suggests.

**Ketura (2026-11-04):** B.  The rules text is about the standard "take control of a site" action, which this is not.  Since Shadows sites are unnumbered, there's no issue with swapping position of two sites, which is all this is doing (the last sentence there is just confirming the state after the swap).


### Q-0404 Keep or strike: cards on the site Uruk Cavern Striker gives in exchange stay on it

Uruk Cavern Striker 15_174 exchanges a Free Peoples site on the path with a site the Shadow player controls. Since engine pass 75 (F-0720) the site handed over goes back onto the adventure path, nobody's, in the spotted site's place; before, it wrongly stayed controlled in the support area.

Not settled: what happens to cards stacked on, or played on, the site handed over (Uruk Besieger 4_180 "stack this minion on a site you control", No Retreat 4_30 "plays on a site you control"). As built now they STAY on the given site, which is back on the path and uncontrolled. The rules give two models:
- A: they stay on the given site (as built; nothing to change).
- B: they move to the site taken, as when a controlled site is replaced (CR 4.2 "site": cards on the old site move to the new site).
- C: they are discarded, as when a site is liberated (CR 4.2 "liberate": any cards on that site are discarded).

Only 15_174 is affected, and only when the site it gives up carries a stacked or attached card. Answer A to keep what is built; B or C would be a small change in the same place (ExchangeSite), with a test.

**Ketura (2026-11-04):** Let's go with B.  The circumstance of having minions etc stacked on a site on the adventure path would leave them permanently there with no way to be retrieved or used, and this circumstance is analogous enough to the replacement for it to apply, I think.


### Q-0399 Destructive Orc 15_108: after 'you may discard a fortification for each ... you can spot', exactly the count or 0 to the count?

"When you play Destructive Orc, you may discard a fortification for each [orc] possession and each [orc] artifact you can spot." (Destructive Orc 15_108)

Built now: an optional trigger (offered only while an [orc] possession or artifact can be spotted, F-0416) whose effect is a discard of `choose(fortification)` with the count `forEachYouCanSpot culture(orc),or(possession,artifact)`. Once the player says yes, the choice is exactly the count (a probe read min 2 and max 2 at a count of 2), or every fortification there is when there are fewer, and one fortification is discarded with nothing asked.

The repo builds this sentence shape both ways. Uruk-hai Healer 14_14 ("you may heal an Uruk-hai for each companion you can spot") is a repeat of 0-1 choices, kept as built under F-0385; Aragorn, Wingfoot 4_364 ("you may wound a minion for each unbound Hobbit you spot") is 0-N by Q-0248. The Witch-king, Conqueror of Arthedain 17_144 ("you may exert a companion for each companion you can spot over 4") is a plain repeat of exactly the count, as Destructive Orc is. One answer would cover all of them.

**A** (as built): the "may" is the trigger's yes/no; after the yes, exactly the count is discarded. Nothing changes; the lone test TheDiscardIsOfferedWithAnOrcPossession (one Threatening Warg, one Parapet discarded with nothing asked) stays as it is. The Witch-king 17_144 stays too, and Uruk-hai Healer would be the one out of line (left as kept under F-0385 unless struck).

**B**: as Q-0339 and Q-0397 read a "may", the yes leads to a choice of 0 up to the count (each fortification 0-1, as Wingfoot's wounds are). The discard's count becomes a range from 0 to the count; TheDiscardIsOfferedWithAnOrcPossession then answers a choice of the one Parapet, and a test reads the choice's minimum of 0. The Witch-king 17_144 would be corrected the same way (each exertion 0-1).

Parked: nothing. The lone test of A passes as built and stays active; the new test WithTwoOrcPossessionsTwoFortificationsOfAnyKindMayBeDiscarded reads only the choice's maximum, which both readings share.

Recommendation: B, for consistency with Q-0248, Q-0339 and Q-0397, which all read a "may" before a counted effect as 0 up to the count.

**Orchestrator (2026-11-04):** B, on Ketura's rulings Q-0248, Q-0339 and Q-0397 (a "may" before a counted effect is 0 up to the count); built by engine pass 74 (15_108, 17_144). Ketura may strike it.


### Q-0398 One Last Surprise 15_33: may Gandalf himself be the unbound companion skirmishing a minion?

"Spell. Spot Gandalf and an unbound companion skirmishing a minion to use Gandalf's resistance to resolve that skirmish instead of that companion's strength."

Built now: two spot requirements, `name(Gandalf)` and `unbound,companion,inSkirmishAgainst(minion)`, with nothing that keeps them apart, so Gandalf alone in a skirmish meets both and the event plays there: his resistance replaces his own strength. With 2 burdens Gandalf, Returned 17_17 has resistance 5 and strength 7; played in his own skirmish against a Goblin Pursuer 2_62 (strength 6), he loses.

**A** (as built): Gandalf may be the unbound companion. The event can be played in Gandalf's own skirmish (rarely good for the player, but a choice he has: after burdens Gandalf's resistance can only be lower than his printed 7, though a strength penalty could make it the better number). Nothing changes; the active test GandalfHimselfMayBeTheUnboundCompanionSkirmishingAMinion reads it.

**B**: Gandalf and the companion are two different cards ("Spot Gandalf and an unbound companion" names two spots). The second requirement becomes `unbound,companion,not(name(Gandalf)),inSkirmishAgainst(minion)`, the event is not playable in Gandalf's own skirmish, and the active test is turned round to read the play not available there.

Parked: nothing (the test of A passes as built and stays active; no test of B is written).

Recommendation: B, weakly. "Spot X and a Y" in the card language usually counts two cards, and the card's sense (lending a companion the Wizard's resistance) assumes another companion; but the text does not say "another", and by Q-0353's read-across a bare kind word with no "another" may take the card itself.

**Ketura (2026-11-04):** oh, let's do B I guess.  If it said "Spot Gandalf to spot an unbound companion..." I would take that to mean they aren't related, but I think the two go hand in hand the way it's currently formulated.


### Q-0397 Black Shapes Crawling 5_48: may the Free Peoples player place fewer than X of the X wounds?

"Maneuver: Spot X Uruk-hai. The Free Peoples player may assign X wounds to your minions. Play X [isengard] weapons from your discard pile."

Built now: each of the X wounds is its own choice for the Free Peoples player of 0 to 1 of the Shadow player's minions, so he may place none, some or all of them (one minion may take several, as Q-0345 has it).

**A** (as built): the "may" is per wound. He places any number from 0 to X. Nothing changes; the active test AnExhaustedMinionWoundedIsKilledAndAWoundMayBeLeftUnplaced places one of two wounds and declines the second (`FreepsGetChoiceMin()` 0). This matches Q-0248's 0-N choice for "wound a minion for each ...".

**B**: the "may" is all or nothing. He declines the whole, or he places exactly X. The definition would want a yes/no (or a 0-or-X choice) ahead of X required wound choices of minimum 1, and the active test above would change to read the second wound's minimum as 1; a test of the decline would read no wound choice at all.

Parked: nothing (the test of A passes as built and stays active; no test of B is written).

Recommendation: A. The wounds hurt the Shadow player's own minions, so letting the Free Peoples player stop partway costs the Shadow player nothing the card did not already risk, and it matches the other "for each" wound choices.

**Ketura (2026-11-03):** I think that since the assigning of X wounds is very carefully worded to not be a cost, it is simply an action available to the free peoples player.  Thus each wound should be 0-1 up to X, and then if they click "done/pass" (whatever it's worded), they are forfeiting the rest of X, but it doesn't change how many of X weapons Shadow gets to play from discard.


### Q-0396 Horse-country 4_326: does a [rohan] mount borne by an ally away from home count as 'at Horse-country'?

"While you can spot a [rohan] mount at Horse-country, the move limit is +1 for this turn." (Horse-country 4_326, site 1T)

Built now: a standing check, `canSpot culture(rohan),mount` with no place named, so any [rohan] mount in play counts, wherever its bearer is. Rider's Mount 4_287 borne by Guma 4_277 (a [rohan] Man ally whose home is 4T) while the fellowship is at Horse-country makes the move limit 3.

**A.** "At Horse-country" says where the mount must be. By F-0518 / Q-0318 an ally is at his home site, so a mount an ally bears is not at Horse-country and the limit stays 2. No ally of any culture has home site 1T, so on this reading an ally's mount never counts; the correction is a place on the spot (`culture(rohan),mount,attachedTo(companion)`, or an `atSite(name(Horse-country))` on the bearer, the engine-pass-48 form). The parked test goes live, and the reviewer ran it against that correction: all 7 tests pass.
**B.** "At Horse-country" only says where the site's text applies (the fellowship is there), so any [rohan] mount the player can spot counts, as built. The parked test is rewritten to read 3, and nothing in the definition changes.

Parked: Card_04_326_Tests.ARohanMountBorneByAnAllyAwayFromHorseCountryDoesNotRaiseTheMoveLimit (reading A; it reads 3 against 2 as built). Rivendell Waterfall 1_342 ("a ranger at") and Derndingle 4_330 ("an unbound Hobbit at") have the same words, but no board raises it there: no ranger ally is printed, and allies are never unbound.

Recommendation: A, which is how Ketura answered F-0503 (Ranger's Cloak 15_68: "if the ally is at his home site and the fellowship is at the *same* site, he should get the bonus"); the place of the card spotted counts.

**Ketura (2026-11-03):** Yeah, A.


### Q-0395 Southron Bow 4_247: "another [raider] Man" on a possession, other than the Bow or other than its bearer

"Bearer must be a [raider] Man. Bearer is an archer. While you can spot another [raider] Man, the minion archery total is +1."

Built now: the +1 is a `modifyArcheryTotal` (Shadow side) under `canSpot` of `not(bearer),culture(raider),man`: "another" is read against the bearer, so a second [raider] Man must be in play.

- **A: "another" than the Bow itself** (the literal reach of Q-0079: "a card other than THAT card"). The Bow is no Man, and its bearer must be a [raider] Man, so the bearer always satisfies the condition: the +1 holds wherever the Bow is borne, and the "while" can never fail on a reachable board. The definition would become `canSpot culture(raider),man` (or the condition dropped), and the test TheBearerIsNoOtherRaiderManAndTheTotalIsRaisedOnceAnotherIsPlayed would read 2 with the bearer as the only [raider] Man, not 1.
- **B: "another" than the bearer** (as built). The Bow on Southron Explorer 4_250 beside Úlairë Attëa 103_61 and Dunlending Brigand 4_10 (no other [raider] Man) gives a minion archery total of 1 (the bearer's archer keyword alone); Easterling Axeman 4_224 played from hand makes it 2. Nothing changes.

Other cards built the same way, by their definitions (not worked here): the [isengard] condition at 4_184 ("another ally or companion", `not(bearer)`) and a [men] count in set 13 (13_xx Men, `not(bearer),culture(men),minion`). One answer covers the shape: "another <kind>" on a card a character of that kind bears.

Parked: nothing. The test of B is active, because the game passes it; a test of A is written once this is ruled.

Recommendation: B. Under A the condition is dead text, and the definitions of this shape already read it so.

**Ketura (2026-11-03):** Yeah, "another" in such cases should be interpreted as "besides my bearer".  B.


### F-0691 Rest While You Can 4_171: "if bearer is killed" built as about-to-be-killed answers a kill The Dead City turns into a discard

"If bearer is killed, reveal the top 10 cards of opponent's draw deck and discard 1 Shadow card and 1 Free Peoples card. Your opponent reshuffles that deck." (Rest While You Can 4_171, an [isengard] condition played on a companion)

Built now: a required `aboutToBeKilled` trigger on `bearer`. In an ordinary kill this plays as printed (shown in the card's tests). But when another card turns the kill into something else, the trigger has already been raised: Sméagol, Poor Creature 6_45 bearing The Dead City 7_56 ("If Sméagol is about to be killed in a skirmish, he is discarded instead") and this card loses a skirmish, and the Free Peoples player is asked to order the two "is about to" triggers. With The Dead City first, nothing is revealed. With this card first, the top 10 Free Peoples cards are revealed, two are discarded, the deck is reshuffled, and then Sméagol is discarded, not killed (CR 4.2 "killed": a discard is no kill). The plain correction, `type: killed`, does not work: once the bearer has left play, `bearer` no longer names him, so the trigger never fires on a real kill. No other card's text says "if bearer is killed".

- **A (as printed, engine work):** the `killed` trigger is taught to resolve `filter: bearer` against what the condition was attached to as both left play, and to collect the condition's own trigger though it went to the discard pile with its bearer (the shape of Q-0020: a card's own required trigger still happens when the same loss removes it). The definition becomes `type: killed, filter: bearer`. Under The Dead City nothing is asked and nothing is revealed; an ordinary kill plays as now. The parked test goes live as written.
- **B (keep as built):** the aboutToBeKilled stays. Under a kill-to-discard replacement the Free Peoples player picks the order, and taking The Dead City first gives the text's result, so the only harm is a Free Peoples player choosing to hurt himself. The parked test is turned round to read the order asked and both outcomes, and the ledger's Notes say why.

Parked: Card_04_171_Tests.ABearerAboutToBeKilledAndDiscardedInsteadRevealsNothing, on reading A.

Recommendation: A when an engine pass next touches kill triggers (it is the text, and the same engine change would serve any future "if bearer is killed"); the board needing it is rare, so B is harmless meanwhile.

**Ketura (2026-11-03):** B, let it be a foot-gun.


### Q-0394 Keep or strike: declining Enquëa's response after the event was offered on its account loses the event (Q-0381 follow-on)

Engine pass 70 built Q-0381 (A): Úlairë Enquëa, Black Threat 12_175's response ("exert Úlairë Enquëa to reduce that event's twilight cost by X") now counts toward whether a [wraith] event with toil can be played. The game offers the event when her responses and the toil exertions together could bring its cost within the pool.

What this opens up: the response is still optional when the event is played, and nothing stops the player declining it. If the event was offered only because the response was counted, and the player then declines it, the cost can no longer be paid. The game then handles it the way it already handles any cost that fails partway through a play: the pool pays what it holds and the event goes to the discard pile with no effect. An example board: a pool of 2, Dark Powers Strengthen 11_207 (cost 6, Toil 2), an unwounded Enquëa. Declining her response leaves the pool at 0, the event discarded, nothing drawn and Enquëa unwounded (shown by a probe, not by a kept test).

Cards that behave differently: only Enquëa and the five [wraith] events with toil she can answer (Echo of Hooves 12_164, Minas Morgul Answers 12_167, Shadow in the East 13_180, Dark Powers Strengthen 11_207, Keening Wail 11_211). The misplay can happen only on a board where the event is offered because of her and toil alone would not cover it.

- **A. Keep (the simple form):** declining a response the play depended on is the player's misplay. The game behaves as it does for any other cost that fails.
- **B. Guard it:** in that case the play is called off and the event goes back to hand, with no twilight paid. This means changing how the three play actions (event, permanent, attached permanent) deal with a failed twilight payment, or else stopping the response from being declined when the play depends on it. Either touches more of the playability code than A did.

Striking A (choosing B) undoes nothing that pass 70 built. It adds the guard on top.

Recommendation: A. The board is narrow, the player has just been offered the response, and Ketura asked for the playability code to be kept simple.

**Ketura (2026-11-02):** Yeah let's stick with A, the scope is too tiny to bother altering the engine for this.


### Q-0362 Mordor Assassin 7_284: is an Assassin already assigned to another companion taken from that skirmish?

Mordor Assassin 7_284: "<b>Response:</b> If a companion is overwhelmed during a skirmish involving a [sauron] Orc and threat wounds are about to be placed, remove a threat to assign this minion to the Ring-bearer (even if the Ring-bearer is already assigned)."

**Built now:** the assignment writes `ignoreExistingAssignments: true`, and the engine applies that flag to both sides of the pair, the Ring-bearer and the minion (`AssignFpCharacterToSkirmish` passes one flag to both). So an Assassin already assigned to another companion is taken out of that skirmish and put in the Ring-bearer's. Probed: the Assassin is assigned to Pippin 1_306, whose skirmish is still waiting, and Merry 1_303 is overwhelmed by Orc Chieftain 1_266 with 2 threats out. The response is taken, the threat is paid, and the Assassin is now assigned against Frodo and no longer against Pippin. Pippin is left with no minion. No other card uses this flag.

**A:** the Assassin stays Pippin's. CR 4.2 "assignment phase": an action assigns a minion to a companion only when "both of them are unassigned", and the card's parenthesis lifts that for the Ring-bearer only. The threat is still paid, so the response is offered and buys nothing. The definition can do this with no engine change: wrap the assignment in an `If` that checks `canSpot` of `self,not(assignedToSkirmish(any))`. The reviewer tried this, and all 10 tests pass, the parked one included. Another way would be a requirement, so that the response is not offered at all while this Assassin is assigned.

**B (as built):** the Assassin leaves the other companion's skirmish and joins the Ring-bearer's. Nothing changes. The parked test's assertions are flipped and it goes live.

Parked: `Card_07_284_Tests.AnAssassinAlreadyAssignedToAnotherCompanionIsNotTakenFromHim` (it takes A, and fails at the reading that the Assassin is still assigned against Pippin).

Recommendation: A. The parenthesis names the Ring-bearer alone, and the CR's general rule covers the minion's side.

**Ketura (2026-10-28):** B.  The CR section you note is about "assignment actions" which is referring to **Assignment:** special abilities, not "actions which deal with assignments".  As a response, Mordor Assassin is not covered by this restriction.


### Q-0363 Troop Tower 7_316: does 'for each besieger stacked on a site' count a site the other player controls? (LR 7, with Olog-hai of Mordor 8_105)

"All [sauron] Orcs are strength +1 for each besieger stacked on a site." (Troop Tower 7_316). Olog-hai of Mordor 8_105 has the same shape ("The twilight cost of this minion is -2 for each [sauron] minion stacked on a site"), and the Legacy Rules council page LR 7 names it. That page (Discussion, 2025-04-25) has no decision yet.

Built now: `forEachStacked`, filter `besieger`, `on: site`. Every site counts, whoever controls it. A besieger the Free Peoples player stacked on his own controlled site (on an earlier turn, when he was the Shadow player) raises the Shadow player's [sauron] Orcs too.

- **A (the historical reading, LR 7's option A).** Only "a site you control" counts. `on: site` becomes the Shadow player's controlled sites, and 8_105's count changes the same way. `Card_07_316_Tests.ABesiegerStackedOnASiteTheOtherPlayerControlsIsCountedToo` would change its readings of Orc Chieftain 1_266 from 7, 8, 9 to 7, 7, 8: the other player's Gorgoroth Pillager on his own site would count nothing.
- **B (the printed text, as built; LR 7's option B).** Every site counts. Nothing changes.

Nothing is parked. The game passes reading B, so its test stays active with this number on the ledger's Findings line. If A is chosen, 8_105's ledger owes the same board.

Recommendation: none from the campaign. Use whatever the council decides for both cards.

**Ketura (2026-10-28):** For now we will do B, which will impact a small cluster of cards.  If the Rules Council issues a different ruling I will get back to you.


### Q-0364 The Art of Gandalf 11_28: whose hand a Shadow card taken from its stack goes to

"Fellowship: Discard a [gandalf] card from hand to take a card stacked here into hand." (The Art of Gandalf 11_28)

A Shadow card can be stacked here: Ted Sandyman 18_75's "Regroup: Exert Ted Sandyman to stack a [men] Man in play on a possession" may choose The Art of Gandalf, so a Shadow player's Footman of Dunland 11_82 can lie on it. What is built now: the Free Peoples player is offered that Shadow card among the cards stacked here, and if he takes it, it goes into its OWNER's hand, the Shadow player's (PutCardFromStackedIntoHandEffect moves every card to its owner's hand). The Free Peoples player has then discarded a [gandalf] card to hand his opponent a minion.

**A** (as built): the card goes to its owner's hand. A card never sits in another player's hand, as a stacked card shuffled in goes to its own owner's deck (F-0172). Nothing changes; a test would assert the Footman in the Shadow hand.

**B**: "take ... into hand" is the Free Peoples player's hand, whoever owns the card. The engine would need a way to put a card into a hand not its owner's, with all that follows (it is later discarded to whose pile?). A test would assert the Footman in the Free Peoples hand.

**C**: a card not the Free Peoples player's own cannot be chosen: only his own cards stacked here are offered. The definition's `putStackedCardsIntoHand` would get a `your` filter; a test would assert the Footman not offered.

No test is parked on it: Card_11_028_Tests.FellowshipAbilityDiscardsAGandalfCardFromHandToTakeACardStackedHereIntoHand reads the Footman as offered and takes another card.

Recommendation: C or A; C keeps the card from being a gift to the opponent, A is what the engine does for every such take.

**Ketura (2026-10-28):** There are no "stealing" concepts in the LOTR-TCG; if a card is returned to a hand it is to its *owner's* hand, regardless of side.  C.


### Q-0365 Orkish Worker 11_133: a reveal bought by a spot, with the draw deck empty (Q-0163 or Q-0348?)

"When you play this minion, you may spot another [orc] minion to reveal the top card of the Free Peoples player's deck. If it is a Shadow card, add (X), where X is its twilight cost."

Built now: an optional `played` trigger with `requires: canSpot another,culture(orc),minion` and no `HasCardInDrawDeck`. With the Free Peoples draw deck empty and another [orc] minion in play, the Shadow player is offered the trigger; taken, nothing is revealed and nothing added.

Two of your rulings meet here. Q-0163 (B): a reveal that a cost buys is offered with that deck empty, and shapes.md puts "<spot or cost> to reveal" under it (Wealth of Moria 1_28, an event). Q-0348 (B, folded into every no-cost optional reveal): withheld with the deck empty. But a spot pays nothing (Q-0224, F-0167: "Spot X to <effect>" is no cost for Q-0034), so this trigger is also a no-cost optional reveal.

**A**: Q-0348 reaches it. A spot is no cost, so the offer is withheld with the Free Peoples draw deck empty. The definition gets `requires: [ {type: canSpot, filter: another,culture(orc),minion} {type: HasCardInDrawDeck, player: free people} ]`, and the test WithTheFreePeoplesDrawDeckEmptyTheOfferIsStillMadeAndNothingHappens turns into a not-offered test. Úlairë Otsëa 11_224 ("you may spot a companion who has X wounds to reveal the top X cards of your draw deck", spot built as a cost) would get the same requirement when its batch comes, and shapes.md would say that only a real cost, or an event, is Q-0163's.

**B**: Q-0163 reaches it, as built. A reveal that a spot buys is offered with the deck empty. Nothing changes, and shapes.md stays as it is.

Parked: nothing. The active test WithTheFreePeoplesDrawDeckEmptyTheOfferIsStillMadeAndNothingHappens asserts B.

Recommendation: A. Q-0348's reason ("I see no reason to pester the user when they can't actually reveal anything") holds just as well when the spot costs nothing, and Q-0163 was ruled on actions with a real cost.

**Ketura (2026-10-28):** I agree with your recommendation for A.


### Q-0366 Rally Cry 11_150: is the +2 per wound counted once as the event is played, or kept up to date?

"Make a [rohan] Man who has resistance 4 or more strength +2 for each wound on each minion he or she is skirmishing."

Built now: the count is taken once, as the event is carried out. With the Rohirrim Guard 7_246 skirmishing an Isengard Worker 3_62 that has 1 wound, Rally Cry makes him 6 + 2 = 8. When his own skirmish ability then exerts the Worker (a second wound token), he stays at 8.

A (as built): the count is taken once, as the event is carried out. A wound placed on a skirmishing minion afterwards adds nothing. This matches CR 4.2 "event" (an event takes a "snapshot" of the game state) and Q-0270 (Madril 15_64: a trigger's "for each" count is taken once). Nothing changes.

B: the count is kept up to date for the rest of the skirmish. The Guard would read 10 after the exertion, the definition would need a live count in place of the evaluated one, and other events with "strength +N for each <count>" might need the same review.

Test: Card_11_150_Tests.TheBonusIsCountedAsTheEventIsPlayedAndAWoundAfterwardAddsNothing asserts A and is active, because the game passes it. Under B it would be parked and corrected.

Recommendation: A. It is the snapshot reading of CR 4.2 "event", and it would settle the same doubt for every event that counts.

**Ketura (2026-10-28):** This is snapshot rule territory.  A.


### Q-0367 Riders in Black 11_215: is 'Reveal your hand' paid with an empty hand?

"Reveal your hand to make a [wraith] minion strength +1 for each [wraith] minion in your hand." (Riders in Black 11_215, a Skirmish event costing 0)

*As built:* the reveal stands before "to", so it is the cost, and the game takes a reveal of an empty hand as paid. With no other card in the Shadow hand the event is played, nobody is shown anything (Q-0144), the [wraith] minion is still chosen and gains +0. No ruling covers a WHOLE-hand reveal as a cost with the hand empty: Q-0255 lets "discard your hand" be chosen with no card in hand, and Q-0336 makes a reveal of "a" card at random from an empty hand a cost that cannot be paid.

- **A. Keep as built:** "reveal your hand" with none in it is a reveal of none, and is paid, as Q-0255's discard of none. The event is playable with an empty hand and does nothing. The active test `WithNoOtherCardInHandItIsPlayedAndTheMinionGainsNothing` stays as it is.
- **B. Unplayable:** like Q-0336, a reveal that has nothing to show cannot be paid, so the event is not offered with no other card in hand. The definition would get a requirement of a card in its player's hand; the test above would assert the event not offered, with one card in hand as its control.

Parked on it: nothing. The test of A is active, since the game already does A.

Recommendation: A. The text reveals no "a" card, so Q-0336's reason does not reach it. Q-0255 is the nearer ruling.

**Ketura (2026-10-28):** A, yeah.


### Q-0368 Deceit 18_29: is the Shadow player's own site, used by the Free Peoples player, "a Free Peoples player's card"?

Deceit 18_29: "Response: If a Free Peoples player's card is about to discard your other [gollum] condition, remove (1) to prevent that."

Built now: `aboutToDiscard` with `opponent: true`, which asks who DISCARDS (the erratum 68_29's wording, "If the Free Peoples player is about to discard", F-0296), not whose CARD does the discarding. So at Heights of Isengard 11_244 ("Each time a [gandalf] companion wins a skirmish, the Free Peoples player may discard a condition from play"), when that site was laid by the Shadow player and the Free Peoples player uses it to discard the Shadow player's [gollum] condition, Deceit is offered, though the card discarding is the Shadow player's own site.

**A** (as built): the original means what the erratum says, so any discard the Free Peoples player makes counts, by whatever card. Deceit is offered at the Shadow player's Heights of Isengard. The parked test is dropped and nothing changes.

**B** (the printed words): the discarding card must be a Free Peoples player's card. The Shadow player's own site is not one, so no offer. The correction is `source: side(free people)` beside `opponent: true` in the trigger (the Introspection 12_29 case stays without offer, by Q-0171). Under it all 9 of the card's tests pass, the parked one included; against the card as built that test fails.

Parked: Card_18_029_Tests.WhenTheFreePeoplesPlayerDiscardsAGollumConditionByTheShadowPlayersSiteNothingIsOffered.

Recommendation: B. The original's printed wording is about the card, and the erratum 68_29 was what changed it to the player (F-0296).

**Ketura (2026-10-28):** Oh man, this gave me a good laugh.  This is very much the sort of thing that was the result of sets 17/18 being the work of a single overworked guy.  Let's be generous and interpret it as A and not bring sites into this lol.  A.


### Q-0369 Soldier's Cache 18_58 (and 17_35): may it be transferred again from the minion bearing it?

"At the start of your maneuver phase, you may spot 3 rangers or exert a [gondor] Man to transfer this condition to a minion. Wound that minion."

Built now: the trigger has no requirement and the transfer has no `fromSupport`, so the copy a minion bears is offered the trigger again at a later maneuver phase of the same turn (a second move). The cost is paid, the Cache moves to another minion (its present bearer is left out of the choice), and that minion is wounded too.

**A** (as printed and built): it may be moved on from minion to minion at each later start of your maneuver phase, a wound each time. Nothing changes; the live test stands.

**B** (as Fifth Level 10_32 under Q-0349: B): only a copy in the support area may be transferred. The definition gets `requires: CanSpot self,Zone(SUPPORT)` and `fromSupport: true` on the transfer, as First Level 7_92 and Disarmed 18_46 (F-0619) have it, and the live test is turned round to "not offered again".

Nothing is parked: the game passes reading A and Card_18_058_Tests.BorneByAMinionItIsOfferedAgainAtTheNextManeuverPhaseAndMovesToAnotherMinion is live. The same answer covers Soldier's Cache 17_35, word for word the same text and definition.

Recommendation: B, for the reason given on Q-0349. A second maneuver phase in one turn is rare, and the Cache is a fortification meant to be put on a minion once. Unlike Fifth Level, though, each move here wounds a minion, so moving it on does something.

**Ketura (2026-10-28):** A.  Unlike the prior fortification stuff, there's a good reason to want to re-transfer (you get another wound), and while the case is rare as it requires a double and the first minion to survive, I can imagine it occurring.


### Q-0370 Ted Sandyman, Chief's Men's Ally (18_75): which of two sites he controls is liberated

"**Maneuver:** Liberate a site you control to make each [men] minion strength +2 until the regroup phase."

**Built now:** `liberateSite` with `controller: shadow`, as the cost. With two sites of his own, the game liberates the higher-numbered one and asks him nothing. CR 4.2 "liberate" gives a highest-first order only for an opponent's sites. It also says a player cannot liberate a site he controls, and the card's text overrides that, so no rule orders his own sites.

- **A: "a site you control" is his choice.** With two or more sites of his own he picks which one goes back on the path. The definition needs a choice of `controlledSite` (his own) in the cost, and the test reads both sites offered and the chosen one liberated.
- **B: the rule's highest-first order carries over (as built).** The highest site he controls is liberated and nothing is asked. Nothing changes.

**Test:** `Card_18_075_Tests.WithTwoSitesOfHisOwnTheHigherIsLiberatedAndNothingIsAsked` asserts B and is active, because the game passes it. Under A it would be rewritten to read the choice. Nothing is parked.

The answer reaches three more Shadow cards that print a bare "liberate a site" with no "you control": Land Had Changed 17_114 ("You may then liberate a site to make that Uruk-hai strength +2 and damage +1"), White Hand Enforcer 17_125 and You Do Not Know Pain 17_138 ("Skirmish: Liberate a site to make an [uruk-hai] minion damage +1"). Their definitions write `liberateSite` with no `controller`. On the Free Peoples player's turn the engine then takes the Shadow player's own highest site, unasked, as Ted's `controller: shadow` does, and with only the Free Peoples player controlling a site the yes/no is not asked.

- **C (17_114 and 17_125 only): CR 4.2 "liberate" read literally.** Only a site another player controls may be liberated, so a bare "liberate a site" takes the Free Peoples player's site and never the Shadow player's own. Ted is not reached (his text says "you control"). Under C, `Card_17_114_Tests.WithOnlyTheOpponentControllingASiteItIsPlus2AndNoLiberationIsAsked` turns round: the yes/no is asked and the Free Peoples player's site is liberated.

**Tests of 17_114 (dec-b17_113), both active as built:** `WithTwoSitesOfHisOwnTheHigherIsLiberatedAndNothingIsAsked` (rewritten to read the choice under A) and `WithOnlyTheOpponentControllingASiteItIsPlus2AndNoLiberationIsAsked` (turned round under C). Nothing is parked.

**Tests of White Hand Enforcer 17_125 (dec-b17_125), all active as built** ("When you play this minion, you may liberate a site to add (X), where X is the Shadow number of the liberated site"): `Card_17_125_Tests.PlayingItLiberatesTheHigherOfHisTwoSitesToAddItsShadowNumber` (rewritten to read the choice under A), `WithOnlyTheFreePeoplesPlayerControllingASiteNothingIsOffered` and `WithAHigherSiteTheFreePeoplesPlayerControlsHisOwnHighestIsLiberated` (both turned round under C: the offer is made and the Free Peoples player's site is liberated). Nothing is parked.

**Tests of You Do Not Know Pain 17_138 (dec-b17_138), both active as built** (the liberation is the cost of its Skirmish ability): `Card_17_138_Tests.SkirmishAbilityLiberatesHisHigherSiteToMakeAnUrukHaiMinionDamagePlusOneForThatSkirmish` (with sites 1 and 2 his, site 2 is liberated unasked; rewritten to read the choice under A) and `WithOnlyTheFreePeoplesPlayerControllingASiteTheSkirmishAbilityIsNotOffered` (turned round under C: the ability is offered and the Free Peoples player's site is liberated). Nothing is parked.

**Recommendation:** B. It matches the rule's order for every other liberation, and a site lower on the path is usually worth more to keep controlled. A is the plainer reading of "a site", though. Against C: liberating the opponent's site would be no price for a Shadow pump, and "then liberate a site to make ..." reads as a cost.

**Ketura (2026-10-28):** Decipher had a habit of over-fitting their rules like this.  To make things trickier, Shadows-era sites don't have a number printed on them and since they can go in any position, tracking the "canonical" number assigned to them is more of an academic question than one that practically affects the game.  I think this should be A for each of these cards; they should not be able to reach over and liberate their opponent's shadow's controlled sites, but if they themselves control one or more it doesn't really matter if they liberate site "3" before site "2" because of the way the Shadows site path works.


### Q-0371 Thorin III, Stonehelm 17_6: two minions lose one skirmish to Thorin; one discard each, or once as ruled for Dwarven Axe 1_9?

"Each time a minion loses a skirmish involving Thorin III, its owner discards the top card from his or her draw deck."

What is built now: a required `losesSkirmish` trigger on each minion that loses to Thorin, with no limit. When two minions lose Thorin's one skirmish, the Shadow player discards two cards, one for each minion.

Decipher ruled on Dwarven Axe 1_9, whose text is nearly the same ("Each time a player's minion loses a skirmish to bearer, that player discards the top card from his or her draw deck"), in CR 4.2 Section Three: "This card can trigger only once for each Shadow player with a minion in that skirmish, regardless of how many minions." The ruling names only the Axe. The Axe is built with `limitPerPhase: 1`. Ketura's Q-0113 (Ancient Enmity 1_29, "If a minion loses this skirmish to that Elf") read that "If" as one effect, "essentially the way that Decipher *wanted* Dwarven Axe to work". Thorin says "Each time", not "If", so that answer does not settle this text either. No other printed card says "each time a minion loses a skirmish" except the Axe and Thorin.

**A**, one discard for each minion, as the plain text reads: no change. The active test TwoMinionsLosingOneSkirmishToThorinEachMakeTheirOwnerDiscard asserts this and passes.

**B**, the Axe ruling read across: once for the Shadow player in each skirmish, however many of his minions lose. Thorin's trigger would get `limitPerPhase: 1`, as the Axe has (one skirmish per Thorin each skirmish phase; a fierce skirmish is a second phase). The active test would be turned round to assert one card discarded.

Parked test: none. The test of A is active because the game passes it.

Recommendation: A. "Each time a minion" counts minions, and Decipher gave the Axe ruling for that card only.

**Ketura (2026-10-28):** Bah, that stupid ruling.  Let's do A.


### Q-0372 Barliman Butterbur 17_16: "each attached [gandalf] follower", attached to Barliman or to anyone

"Barliman Butterbur is strength +1 for each attached [gandalf] follower."

Built now: the count is `culture(gandalf),follower,attachedTo(any)`, so every [gandalf] follower attached to any character counts, wherever it is; one in the support area does not. With Alatar 13_28 on Arwen 1_30, Barliman is strength 5.

A (as built, the plain words): every [gandalf] follower attached to anyone counts. Nothing changes; AGandalfFollowerAttachedToAnotherCompanionCountsAsWell stays as it is (Barliman 5 with Alatar on Arwen).

B: only the [gandalf] followers Barliman himself bears count. The definition becomes `attachedTo(self)` in place of `attachedTo(any)`; with Alatar on Arwen, Barliman stays 4, and that test turns round to read 4. The other tests put the followers on Barliman and read the same either way.

Parked: nothing. AGandalfFollowerAttachedToAnotherCompanionCountsAsWell is active on reading A, which the game passes.

Recommendation: B looks likelier meant. Where set 17 means any bearer it says so (Orkish Assassin 17_72, "a follower attached to a companion"), and a companion's own bonus "for each attached" card usually means the cards he bears.

**Ketura (2026-10-28):** lmao.  Let's do B for now.


### Q-0373 Scintillating Bird 17_23: does "take those cards into hand" take all three revealed, or only those of the named side?

"Fellowship: Exert your Wizard and choose an opponent to name either Free Peoples or Shadow. Reveal the top three cards of your deck. If you revealed at least two of the chosen card type, take those cards into hand, otherwise, discard those cards."

Built now: when at least two of the three revealed cards are of the named side, all three revealed cards are taken into hand (`putCardsFromDeckIntoHand` of `memory(revealedCards)`); otherwise all three are discarded.

**A** (as built): "those cards" in the take branch means all three revealed cards, as it can only mean in the discard branch. Nothing changes. Card_17_023_Tests reads it so (ShadowNamedWithTwoShadowCardsRevealedTakesAllThreeIntoHand, and the take half of TwoOfTheNamedSideTakeTheCardsAndOneDiscardsThemWhicheverSideIsNamed): the third card, of the other side, goes to hand too.

**B**: only the revealed cards of the named side are taken. The text then does not say where the third card goes (left on top, or discarded); the definition would want a side filter on the take and a home for the rest, and the two tests above would read the other-side card not in hand.

Parked on it: nothing. The tests at A pass and stay active.

Recommendation: A. It is the only reading the same phrase allows in the otherwise-branch, and B leaves a card with nowhere to go.

**Ketura (2026-10-28):** Hmm, let's do A, yeah.


### Q-0374 Saruman, Coldly Still 17_48: does Saruman's Staff 4_174 (or The White Council 101_18) switch off his '[men] minion bearing a possession is strength +2'?

"Each [men] minion bearing a possession is strength +2." (Saruman, Coldly Still 17_48, his first sentence; the second is "Skirmish: Exert Saruman to play a [men] possession from your discard pile.")

Built now: the +2 modifier carries `requires: { type: sarumanFirstSentenceActive }`, so it is his "first sentence" for Saruman's Staff 4_174 ("Maneuver: Spot Saruman to make the first sentence of his game text not apply until the regroup phase") and The White Council 101_18. While he bears the Staff, its Maneuver ability is offered, and once used, every [men] minion bearing a possession loses the +2 until the regroup phase.

Legacy Ruling 6 (Q-0016) names only 12_54 and 12_144, whose texts open with keywords; 17_38 was added by reading the ruling's reasoning as "the sentence meant is the one about not skirmishing". LR 6's Clarifying Opinion says copies of Saruman "whose first sentence does not involve such a restriction are likely not really intended to interact with Staff at all". Coldly Still's first sentence is a real sentence, but has nothing to do with skirmishing or archery.

**A** (the Q-0016 / LR 6 reasoning carried over): the Staff and the Council have no effect on him. The `requires` block comes off his modifier; the Staff's ability is then not offered on him (the Q-0246 mechanism, withheld by the engine since engine pass 32) and the +2 always stands. The parked test SarumansStaffHasNoEffectOnHimAndEachMenMinionBearingAPossessionStaysStrengthPlus2 goes live (mutate.py with the block removed: it passes, and so do the other 6).

**B** (literal, as built): his first sentence is the +2, and the Staff or the Council switches it off. Nothing in the definition changes; the parked test is turned round into the Q-0236 "answers the switch" test: a [men] minion with a possession read at +2 before the Staff is used, without it after, and with it again at the regroup phase.

Parked: SarumansStaffHasNoEffectOnHimAndEachMenMinionBearingAPossessionStaysStrengthPlus2 (Card_17_048_Tests), on reading A.

Recommendation: A, on LR 6's Clarifying Opinion; the Staff's switch was printed for the Sarumans who cannot be assigned, and this one can be.

**Ketura (2026-10-28):** B is the way to go.  Shadow can choose to *not* shoot himself in the foot, and there is no ambiguity as to which is the first sentence in this case.


### Q-0375 Stampeding Savage 17_53: are the X stacked minions from one [men] possession or from any

"Maneuver: Discard this minion and X minions stacked on a [men] possession to play a [men] minion from your discard pile. That minion is twilight cost -X."

As built, X may be taken from any of your [men] possessions together (`discardStackedCards` on `culture(men),possession`, count any): one minion off Henchman's Dagger and one off Fleet of Corsair Ships make X = 2.

- **A:** all X come off ONE [men] possession. The definition would choose the possession first, then 0 to all of the minions on it; the tests DiscardsItselfAndTwoMinionsStackedOnMenPossessionsToPlayAMenMinionFromTheDiscardPileTwoCheaper (X = 2 from two possessions) would be rewritten to take both from one, and a test that a second possession's minions are not offered with the first's would be added.
- **B (as built):** X may come off any number of [men] possessions. Nothing changes.

No test is parked: the test of B passes and stays active. Set 17's own wording points both ways: "for each [men] minion stacked on a [men] possession" (17_46) plainly means all possessions, while texts that go on to say "that possession" mean one. Sunland Guard 17_55 has the same doubt about its "(or 2 ...)" and is asked beside this one. Recommendation: B, as built; "a" reads as the kind of card the minions are stacked on.

**Ketura (2026-10-28):** B


### Q-0376 Sunland Guard 17_55: must the two minions of '(or 2 ...)' come off the same [men] possession

"Maneuver: Exert this minion to play a [men] minion (or 2 if you spot 6 companions) from a [men] possession as if from hand."

As built, the play is `playCardFromStacked` on `culture(men),possession`, done once or (with 6 companions) twice, and each play may take its minion off any [men] possession: the second may come off a different possession from the first.

- **A:** both minions come off the SAME [men] possession. The definition would fix the possession at the first play; WithSixCompanionsPlaysTwoMenMinionsFromMenPossessions (which takes the second off another possession) would be rewritten to take both off one, and a test that the other possession's minion is not offered for the second play would be added.
- **B (as built):** each minion may come off any [men] possession. Nothing changes.

No test is parked: the test of B passes and stays active. This is the same kind of doubt as Stampeding Savage 17_53's "X minions stacked on a [men] possession", asked beside this one; one answer may well settle both, but the sentences differ ("from a [men] possession" against "stacked on a [men] possession"), so each has its own entry. Recommendation: B, as built.

**Ketura (2026-10-28):** B


### F-0640 Blade of the White Hand 17_112: does "you can spot 2 (4) controlled sites" count the Free Peoples player's sites too?

"While you can spot 2 controlled sites, this minion is damage +1. While you can spot 4 controlled sites, this minion is damage +3." (Blade of the White Hand 17_112, a Shadow possession borne by an [uruk-hai] Uruk-hai)

As built, each sentence is `requires: { type: ControlsSite, count: N }`, whose player defaults to "you": only the sites the Blade's owner (the Shadow player) controls are counted. A site the Free Peoples player took control of on one of his own Shadow turns is not counted. This is the only definition text in the game that says "controlled sites"; every other card of set 17 says "sites you control".

- **A (literal):** every controlled site counts, either player's (CR 4.2 "control": a controlled site is in a player's support area; "you can spot" is everything that can be spotted). The two `ControlsSite` blocks become a count of the sites both players control (`isGreaterThanOrEqual` over `sum` of `forEachSiteYouControl` and `forEachSiteOpponentControls`, or `canSpot` of `or(controlledSite,controlledByOtherPlayer)`). With site 1 the Free Peoples player's and site 2 the Shadow player's the bearer is damage +1; with sites 2 to 4 the Shadow player's as well, damage +4. The parked test goes live; mutate.py shows all 5 tests pass under the change.
- **B (as built):** "controlled sites" means the sites you control, as the rest of the set words it. Nothing in the definition changes; the parked test is turned round to read the Free Peoples player's site NOT counted (damage 0, then 1).

Parked: ASiteTheFreePeoplesPlayerControlsIsAControlledSiteToo (Card_17_112_Tests), on reading A.

Recommendation: A, as printed (Q-0012, cards are code); the wording reads as deliberate in a set that otherwise says "you control" every time, though it may be an editing slip.

**Ketura (2026-10-28):** lol the designer probably meant for it to be "your", but the rules are particular that controlled sites are still spottable in your opponent's support area.  Let's do A.


### F-0643 Argument Ready to Hand 12_1: may the Shadow player spot a companion's race, or only a minion's?

"Spot a Dwarf to choose a Shadow player who must spot a race. Discard from play all minions of all other races."

Built now: the Shadow player chooses from `choose(minion,hasRace)`, so only a minion's race can be spotted. Gimli or Aragorn on the table are never offered. The definition's prompt reads "Choose a race to spare, all other will be discarded". The second CR 4.2 "Gollum/Sméagol" ruling on this card says Gollum is not "of all other minion races".

- **A (correct it):** "spot a race" is a choice of any character with a race, as shapes.md "Spot a race" has it for Relentless Warg 17_89: companions, allies and minions, not Gollum. The definition becomes `select: choose(character,hasRace)`. The Shadow player may then spot, say, Gimli's Dwarf and discard every minion with a race. The parked test TheShadowPlayerMaySpotTheRaceOfACompanionAsWellAsOfAMinion goes live. It passes with the correction, and so does every other test.
- **B (keep it):** this card's "spot a race" is a race among the minions, the "race to spare". Nothing changes. The parked test is dropped and the ledger reads companions not offered.

Parked: Card_12_001_Tests.TheShadowPlayerMaySpotTheRaceOfACompanionAsWellAsOfAMinion.

Recommendation: A. Spotting goes by what is in play, and the text names no side. The only thing that changes in practice is that the Shadow player may now choose to give up every minion that has a race.

**Ketura (2026-10-28):** Let's do B.  There's no point doubling the number of races in the group when Shadow is almost never going to *want* to discard all their own minions.


### Q-0377 Belt of Erebor 12_2: can an item be transferred to an ally away from his home site?

"Skirmish: Discard this possession to transfer one of bearer's possessions to another eligible bearer."

Built now: an ally away from his home site is offered as the new bearer. At site 2, Grimir 1_17 (home 3F) is offered for the bearer's Hand Axe 2_10, beside two Dwarf Guards with the fellowship. CR 4.2 "transfer" says "Both characters involved in the transfer must be at the same site. (See ally.)". Ketura on Q-0355 (a CONDITION moved to an ally away from home): "Normal transfer rules about being at the same site are for *item* transfer." Ketura on Q-0252: an ally is "explicitly *not* at the same site as the fellowship unless they are at their home site".

- **A:** for an item's transfer, an ally away from his home site is no eligible bearer. At site 2 only the Dwarf Guards are offered. This needs an engine change: transfer targets need a same-site check, and the definition language has no word for it. It would reach every "transfer ... to another eligible bearer" of a possession or artifact, and every item transfer an ally could receive.
- **B:** an effect's transfer obeys only the item's own bearer and timing text, so Grimir is offered at site 2. Nothing changes. The parked test is rewritten to read him offered.

Parked: Card_12_002_Tests.ADwarfAllyAwayFromHisHomeSiteIsNoEligibleBearer. It fails at FreepsHasCardChoiceNotAvailable(grimir). At Grimir's home site both readings agree, and the active test has him offered there. Also parked on it: Card_12_070_Tests.AnAllyAwayFromHisHomeSiteIsNoEligibleBearer (Hemmed In 12_70, dec-b12_68).

Recommendation: A, in line with Ketura's words on Q-0355. It is an engine item.

**Ketura (2026-10-28):** Ooh, tough.  So I think the relevant section in the `transfer` section is talking about the Fellowship phase default transfer action, that is the only time there is an ally home site restriction.  Any card effect that instigates a "transfer" is a different operation with a confusingly identical name, as noted by the last clause in that rules section, where Strange-looking Men may transfer Flaming Brand to a Shadow Man; there is no circumstance where this could happen normally on play, and the Fellowship action would presumably not be able to do this either.  So I would say B.


### Q-0378 Taunt 12_104: is 'reveal the top 5 cards' paid by a draw deck of fewer than 5?

"Reveal the top 5 cards of your draw deck (or, if the fellowship is at a battleground site, the top 7 cards) to make an [orc] minion strength +1 for each [orc] card revealed." (Taunt 12_104, a Skirmish event costing 1)

*As built:* the reveal stands before "to", so it is the cost (F-0022), written as `revealTopCardsOfDrawDeck` with a count of 5 (7 at a battleground). The game judges it payable only in full (F-0425), so the event is not offered while the Shadow draw deck holds fewer than 5 cards (fewer than 7 at a battleground). An empty deck is settled (F-0022, Q-0148: not playable); a deck holding 1 to N-1 cards is not covered by any ruling. Q-0336 is about a reveal of "a" card at random from a hand, and Q-0163 about a reveal that a cost buys.

- **A. Reveal what is there:** a deck of 1 to 4 cards (1 to 6 at a battleground) pays the cost with the cards it has, and the [orc] cards among them are counted. The event's cost check would change to "at least one card in the draw deck"; the active test `WithFewerThanFiveCardsInTheDrawDeckItCannotBePlayed` (3 cards) would turn into a play that reads +N for the [orc] cards among the three.
- **B. Keep as built:** "the top 5" cannot be paid by fewer than 5, as a cost is judged payable in full before it begins (F-0425). Nothing changes.

Parked on it: nothing. The test of B is active, since the game already does B (`Card_12_104_Tests.WithFewerThanFiveCardsInTheDrawDeckItCannotBePlayed`; `CannotBePlayedWithAnEmptyDrawDeck` shows it offered once 5 cards are in the deck). The answer reaches any other "reveal the top N ... to" cost.

Recommendation: B, by F-0425.

**Ketura (2026-10-28):** ooh, interesting.  I would say that in this particular case, the reveal is only being done to count the random number of [orc] cards and so accidentally only having 4 to reveal cards is fine.  However if it were a formulation like "reveal the top 5 cards to add a burden", that would require there to be 5 cards.  A.


### Q-0379 Troll's Keyward 12_105: is 'reveal your hand' paid with an empty hand? (rider to Q-0367)

"Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed." (Troll's Keyward, Keeper of the Beast 12_105)

*As built:* both halves before "to" are the cost, and the game takes a reveal of an empty hand as paid. With no card in the Shadow hand the ability is offered, the Keyward exerts, nobody is shown anything (Q-0144), and (0) is added. This is the doubt of Q-0367 (Riders in Black 11_215, "Reveal your hand to ..."): its answer settles this card too.

- **A. Keep as built** (Q-0367's recommendation): a reveal of no cards is paid, as Q-0255's discard of none. The active test `Card_12_105_Tests.WithAnEmptyHandItIsStillOfferedAndHeExertsToAddNothing` stays; so does `mechanics/deck/RevealingNoCardsFromAHandTests.APlayersOwnEmptyHandIsShownToNobodyEither`, which lays this card on the same board.
- **B. Unplayable:** a reveal with nothing to show cannot be paid, so the ability is not offered with an empty hand. The definition would get a requirement of a card in its player's hand; the test above would assert the ability not offered, with one card in hand as its control, and the mechanics test would need another card.

Parked on it: nothing. The test of A is active, since the game already does A.

Recommendation: A, with Q-0367.

**Ketura (2026-10-28):** A.


### Q-0380 The Mouth of Sauron 12_118: 'the companion who has the highest strength' already assigned

"Assignment: Assign The Mouth of Sauron to the companion who has the highest strength. (If two or more are tied for highest, choose one.)" (The Mouth of Sauron, Lieutenant of Barad-dûr 12_118)

*As built:* the companion is `choose(companion,highestStrength(companion))`, so the highest-strength companion is picked whether or not he is assigned. CR 4.2 "assignment phase" says "You cannot do this unless both of them are unassigned". So when the companion of highest strength is already assigned, the ability can do nothing, and the engine does not offer it (Q-0224). The test board: Unexpected Visitor 7_126 is on Aragorn 1_89 (strength 8), and a Goblin Runner 1_178 has taken him by the ability the Visitor gives every minion. Boromir 1_97 (strength 7) is free, and the Mouth is not offered his ability.

- **A. Keep as built:** "the companion who has the highest strength" is Aragorn, assigned or not. With him taken, the ability does nothing and is not offered. Nothing changes. The active test `Card_12_118_Tests.WithTheCompanionOfHighestStrengthAlreadyAssignedTheAbilityIsNotOffered` stays.
- **B. The highest of those who can still be assigned:** the text reaches the highest-strength companion the Mouth can still be assigned to, which is Boromir on that board. The ability is offered and assigns the Mouth to Boromir. The definition would get `assignableToSkirmishAgainst(self)` inside `highestStrength(...)` and in the choice, and the test would flip to read the Mouth assigned to Boromir.

Parked on it: nothing. The test of A is active, since the game already does A. No other card's text says "the companion who has the highest strength".

Recommendation: A. The text names one companion, and CR 4.2 makes the assignment impossible when he is taken. B would let the Mouth pick a weaker companion, and the card is meant to fight the strongest one.

**Ketura (2026-10-28):** A


### Q-0381 Úlairë Enquëa, Black Threat 12_175: does the toil response count toward whether the event can be played

"Response: If you are playing a [wraith] event that has toil X, exert Úlairë Enquëa to reduce that event's twilight cost by X." (Úlairë Enquëa, Black Threat 12_175)

*As built:* a Response on `beforeToil` of a `culture(wraith),event,toil`, cost: exert her, effect: `incrementToil` (each toil exertion then takes 2X off). It is offered while the event is being played, and works when the pool covers the event after toil alone. But the game offers the play itself only when the pool covers the cost after toil alone, so the response cannot make an event playable that toil alone leaves short. The board: a Shadow pool of 2, Dark Powers Strengthen 11_207 (cost 6, toil 2) in hand, and an unwounded Enquëa as the only [wraith] character. Her toil exertion plus her response would bring the cost to 2. Toil alone brings it only to 4, so the event is not offered (`ShadowPlayAvailable` false). CR 4.2 "playing a card" step 2 checks that all costs can be paid before any is paid. CR 4.2 "cost" speaks of a response changing costs while they are being paid, but its example only makes them larger. Q-0211, F-0350 and F-0482 count a card's OWN modifiers ahead; none of them is about another card's response.

- **A. The response counts toward playability:** the event is offered from a pool of 2. Enquëa exerts for the response and again for toil (2 wounds), and the pool ends at 0. This is an engine change: the playability check for toil cards would have to count a Response that reduces toil (`beforeToil`/`incrementToil`) and could be paid on that board. No definition change can do it. The parked test goes live.
- **B. Keep as built:** the play is judged on toil alone, and the response only helps an event that toil already makes affordable. Nothing changes. The parked test is turned round to assert the event not offered from 2.

Parked on it: Card_12_175_Tests.ResponseAndToilTogetherLetAWraithEventBePlayedFromAPoolThatToilAloneCannotCover. It fails at `assertTrue(ShadowPlayAvailable(darkpowers))`. The active tests (and the two lone tests with Minas Morgul Answers 12_167 and Sword Rack 11_158) use pools that toil alone covers, so both readings agree on them. No other definition uses `beforeToil` or `incrementToil`.

Recommendation: A. The card's only use is to make [wraith] events cheaper, and a player would expect to count it when deciding whether the event can be played.

**Ketura (2026-10-28):** Hoooo boy.  So A is the *ideal*, it just wasn't included because it's so niche.  Yeah, go ahead and do A, but let me know the impact to the playability code; I would prefer it to be kept relatively simple.


### Q-0382 The Witch-king, Black Lord 12_183: a Shadow card's 'reveal this card from hand' trigger on its owner's own turn

"Each time a companion is played, you may reveal this card from hand to exert the Ring-bearer." (The Witch-king, Black Lord 12_183)

*As built:* the trigger is offered to the Witch-king's owner only while he is the Shadow player. The engine looks for "from hand" triggers in each player's hand among the cards of the side he plays that turn (`OptionalTriggersFromHandRule`, `Filters.filter(game, hand, side)`), so on his own turn, when he is the Free Peoples player, his own companion's play offers him nothing. The text names no side ("a companion"), and CR 4.2 "active" speaks only of cards in play, not of a card's text used from hand. No ruling covers a Shadow card's hand trigger on its owner's own turn.

- **A. Keep as built:** a Shadow card's hand trigger is its owner's only while he is the Shadow player; his own companion played on his own turn sets nothing off. Nothing changes. The active test `Card_12_183_Tests.ItsOwnersOwnCompanionPlayedOnHisOwnTurnDoesNotSetItOff` stays.
- **B. Any companion, his own included:** on his own turn he may reveal the Witch-king to exert his own Ring-bearer. This is strategically pointless, but it is an offer the game would then make. It is an engine change to `OptionalTriggersFromHandRule`, and it reaches every "reveal this card from hand" trigger (Orc Crusher 13_112, Corsair Champion 13_84 and others), not only this card. The active test would flip to read the offer made and Frodo exerted after acceptance.

Parked on it: nothing (the game passes reading A, which the active test takes).

Recommendation: A. A Shadow card does its work while its owner is a Shadow player, and B gives the Free Peoples player nothing but a way to hurt himself.

**Ketura (2026-10-28):** ha!  For QOL reasons, let's not have it reveal on its owner's own freeps turn, that would be a pester without a point.  A.


### Q-0383 Subterranean Homestead 13_8: a site that is both a mountain and underground adds one token or two (and 13_22, 13_139, 13_152)

"When you play this, add a [dwarven] token here for each mountain site and each underground site on the adventure path." (Subterranean Homestead 13_8)

*As built:* one count, `forEachYouCanSpot` of `or(mountain,underground),zone(adventure path)`, so a site that is both a mountain and underground adds 1 token. Mount Doom 15_193 (Battleground, Mountain, Underground) is the only printed site that is both. The board: at site 5 of the Fellowship path, Balin's Tomb 1_343 and The Bridge of Khazad-dum 1_349 (underground) on the path, and Mount Doom laid as site 6. The game adds 3 tokens. The three sister cards print the same sentence with other terrain words and are built the same way, with `or(...)`: Secluded Homestead 13_22 (forest, river), Wind-swept Homestead 13_139 (plains, battleground), Humble Homestead 13_152 (forest, dwelling). No ruling or rule speaks to the overlap.

- **A. Two counts, added together:** a token for each mountain site, and another for each underground site, so Mount Doom adds 2 (4 in all on that board). The definition's count becomes a `sum` of `forEachYouCanSpot` `mountain,zone(adventure path)` and `forEachYouCanSpot` `underground,zone(adventure path)` (checked with mutate.py: every test passes, the parked one included). The sister cards would be corrected the same way.
- **B. One count of the sites that are either:** Mount Doom adds 1 (3 in all). Nothing changes; the parked test is rewritten to read 3.

Parked on it: Card_13_008_Tests.ASiteThatIsBothAMountainAndUndergroundAddsATokenForEach. It fails at `assertEquals(4, GetCultureTokensOn(homestead))` (was 3). Also parked on it: Card_13_022_Tests.ASiteThatIsBothAForestAndARiverAddsATokenForEach (Secluded Homestead 13_22, dec-b13_21: at site 4 of the Fellowship path, Breeland Forest 1_329 as site 2 and Ford of Bruinen 1_338 as site 3, Anduin Wilderland 1_354 (Forest, River; the only printed site outside the Hobbit sets that is both, beside Forest River 31_46) laid as site 5). The game adds 3 tokens and A reads 4; the same `sum` correction on 13_22 makes it pass with every other test of the card. Also parked on it: Card_13_139_Tests.ASiteThatIsBothAPlainsAndABattlegroundAddsATokenForEach (Wind-swept Homestead 13_139, "for each plains site and each battleground site", dec-b13_139: at site 2 of the Fellowship path, Uruk Camp 4_335 (Battleground, Plains) laid as site 2). The game adds 1 token and A reads 2; the `sum` correction (`plains,site,zone(adventure path)` and `battleground,site,zone(adventure path)`) makes it pass with every other test of the card. Humble Homestead 13_152 ("for each forest site and each dwelling site", dec-b13_151): no printed site is both a forest and a dwelling (`packet.py --find "keywords:.*(Forest.*Dwelling|Dwelling.*Forest)"`, the Hobbit sets included, finds none), so no board can raise the overlap and nothing is parked for it; it is built with the same one count, and under A takes the same `sum` correction (`forest,site,zone(adventure path)` and `dwelling,site,zone(adventure path)`), which no test can tell apart.

Recommendation: A. "Each mountain site and each underground site" reads as two separate counts, and a site that is both is a mountain site and also an underground site.

**Ketura (2026-10-28):** Hmm, A.


### Q-0384 Galadriel, Sorceress of the Hidden Land 13_15: is 'reveal a card for each forest' offered with fewer cards in the draw deck than forests? (rider to Q-0378)

"Each time the fellowship moves during the regroup phase, you may reveal a card from the top of your draw deck for each forest on the adventure path to heal an Elf for each [elven] card revealed." (Galadriel, Sorceress of the Hidden Land 13_15)

*As built:* the reveal stands before "to", so by F-0022 it is the price, but the definition writes it as the trigger's first effect, revealing as many cards as there are forests on the path. The trigger is withheld with the draw deck empty (Q-0348, folded into this card) and with no forest on the path (F-0651). With two forests on the path and one card in the draw deck it is offered, and taken it reveals that one card and heals an Elf if the card is [elven]. This is the doubt of Q-0378 (Taunt 12_104, "Reveal the top 5 cards ... to"), but the two cards are built opposite ways: Taunt is built as B, Galadriel as A, so one answer changes one of them.

- **A. Reveal what is there:** with fewer cards than forests the trigger is still offered and reveals what the deck holds. Nothing changes on Galadriel; the parked test would be rewritten to assert the offer and the one card revealed.
- **B. The price is paid in full (F-0425):** not offered while the draw deck holds fewer cards than there are forests on the path. Galadriel needs a change: either the engine lets `HasCardInDrawDeck`'s `count` take a value (the forest count), a one-line requirement, or, with no engine change, nine requirements of the form "fewer than k forests, or at least k cards in the draw deck" for k = 1 to 9 (shown by mutate.py to pass every test). The parked test goes live.

Parked on it: `Card_13_015_Tests.WithTwoForestsAndOneCardInTheDrawDeckItIsNotOffered` (B; fails as built, passes under either correction).

Recommendation: answer it with Q-0378, the same way. B is the reading F-0425 gives a price, and the engine change is the tidy way to build it.

**Ketura (2026-10-28):** A I guess, to be consistent with the other question.  It's reducing the total number of chances freeps has, so it shouldn't be abusive.


### Q-0385 Let Fly 13_19: may 'spot a Shadow race' be met by a Man companion, or only a minion?

"Spot 3 Elves, spot a Shadow race, and choose a Shadow player to wound each of that player's minions which is not of that race." (Let Fly 13_19, an [elven] Archery event)

*As built:* the race is spotted by a choice of a minion with a race (`choose(minion,hasRace)`, minimum 1), and every minion of the chosen player not of that race is wounded. A companion or ally is never offered, though some of them (a Man such as Gondorian Knight 5_35) are of a race Shadow cards have too. No rule defines "Shadow race". The shapes entry on "Spot a race" offers every character with a race; F-0643 (Argument Ready to Hand 12_1, "choose a Shadow player who must spot a race", built minions only) is open on that point. That card names no "Shadow race", so its answer may not settle this one, and this entry takes a number of its own.

- **A. Keep as built:** a "Shadow race" is spotted on a Shadow character, a minion. Naming Man needs a Man minion on the table. The active test `WithThreeElvesSpottedEachMinionNotOfTheRaceSpottedIsWounded` reads the Knight not offered and stays as it is.
- **B. Any character of a race some Shadow card has:** Man (or another such race) could be spotted on a companion or ally. With no Man minion in play, every minion would then be wounded. The definition would choose `character,hasRace` with some filter that keeps out races no Shadow card has (Elf, Hobbit, Dwarf, Ent); no filter for that exists, and none was written. The test above would offer the Knight.

Parked on it: nothing. The test of A is active, since the game already does A.

Recommendation: A. "Shadow" reads most simply as the side of the card spotted, and B lets a fellowship name a race no minion has in order to wound all of them.

**Ketura (2026-10-28):** A


### F-0655 The Palantír of Orthanc 13_36: is uniqueness counted per player (CR 4.2), so that it can be played beside the Shadow player's Palantír and discard it?

"Bearer must be Gandalf. To play this, if you can spot The Palantír of Orthanc, discard it." (The Palantír of Orthanc, Recovered Seeing Stone 13_36, a unique Free Peoples [gandalf] artifact.)

CR 4.2 "unique": "You may have only one card with the card title of •Gandalf in play at one time. Other players may also have a card with the title of •Gandalf in play, but only one is allowed per player."

Built now: the game's uniqueness check (`PlayUtils.checkUniqueness`) counts every active card of the title, both players' cards included, and runs before the "discard it" cost. The only other Palantírs of Orthanc in print are the Shadow player's [isengard] artifacts 3_67 and 4_166 (Seventh Seeing-stone). So with either of them in play, 13_36 is not playable at all, and the discard it prints never happens. With neither in play it is played on Gandalf as usual. No definition change can mend this. A search of the text sheet finds no other unique title printed on both sides, so in practice the change reaches this card alone.

- **A (per player, as CR 4.2 says):** the uniqueness check counts only the cards of the player who plays the card. 13_36 can then be played on Gandalf beside the Shadow player's 3_67 or 4_166, and that Palantír goes to its owner's discard pile. This is an engine change, in `PlayUtils.checkUniqueness`. The parked tests go live.
- **B (both players, as built):** no unique card may be played while the other player has an active card of its title. 13_36's "discard it" can then never happen, and the card can be played only while no Palantír of Orthanc is in play. Nothing changes. The two parked tests are dropped, and the ledger notes this as a known departure.

Parked: Card_13_036_Tests.TheShadowPlayersPalantirOfOrthancIsDiscardedAsItIsPlayed and APalantirOfOrthancWithAnotherSubtitleIsDiscardedToo, on reading A.

Recommendation: A. It is what CR 4.2 says, and it is the only reading under which the card's own discard text does anything.

**Ketura (2026-10-28):** Gotta be A, yeah.  I would want the implementation for this to look for a flag or other kind of switch that is present on the playing card rather than something that is always on everywhere.  For instance, if playing an Aragorn, the code should not be checking for a Shadow Aragorn by default, but when playing this card it should detect that the blueprint has a "special uniqueness check" flag (or whatever) and evaluates differently based on that.


### Q-0386 Dunlending Patriarch 13_88: a "you may spot" discount on its own play, built as never asked (Uruk Aggressor 13_168 the same)

Dunlending Patriarch 13_88: "When you play this, you may spot another [men] minion to make this minion twilight cost -1 for each wound on the Ring-bearer."

**Built now:** a standing `modifyOwnCost` under a `canSpot another,culture(men),minion` requirement. Whenever another [men] minion can be spotted, the discount is applied and the Shadow player is asked nothing. Uruk Aggressor 13_168 ("When you play this, you may spot a companion who has resistance 0 to make this minion twilight cost -2") is built the same way; a search of every `modifyOwnCost` definition found no other card of this shape. The nearest ruled cases are a "you may add (N)" on a play built required (Goblin Runner 1_178, F-0009, kept as a shortcut; Mordor Fiend 60_91, Q-0165, where the "you may" was taken out of the text instead), and the discounts that have a price of their own and are asked (Mountain-troll 15_112, Sauron 13_140). A spot costs nothing, so no price makes this one an offer.

**A (as built):** the discount is always taken when the spot can be made: a lower cost is never worse for the Shadow player, and the definition language has no optional discount for a spot. Nothing changes. The active tests read the discount taken with nothing asked, and the clause has no O check.

**B (the text, as Q-0021):** the "you may" is asked. Each play of the Patriarch (and the Aggressor) with a spot available would put a yes/no first, and declining would pay the full 6 (+2 roaming). Both definitions would need an optional discount type the language does not now have (an engine change), the tests would gain an accept, and a test of declining would be added for each card.

No test is parked: the game does A and the tests of A are active. Recommendation: A, as Goblin Runner's shortcut; if the wording should follow (as Mordor Fiend's did under Q-0165), that is a line for the wording list, not a change to the game.

**Ketura (2026-10-28):** Let's do A.  It's not worth it refactoring the engine for an operation nobody is ever going to use.


### Q-0387 Harmless 13_93: does 'cannot take wounds during skirmishes involving bearer' protect a minion outside that skirmish?

"Minions cannot take wounds during skirmishes involving bearer." (Harmless 13_93, a [men] condition that a companion who loses to a [men] minion comes to bear)

*As built:* `cantTakeWounds` over `minion,inSkirmishAgainst(bearer)`: only the minions in the bearer's own skirmish are protected. A minion assigned elsewhere and waiting for its skirmish, or one left unassigned, can be wounded while the bearer's skirmish goes on. No rule or ruling names the card. Other texts that mean the narrow thing say so: Fortification 7_92 "Characters skirmishing bearer cannot take wounds", and the [rohan] event of set 15 "minions skirmishing that companion cannot take wounds during a skirmish involving that companion". Elsewhere "during a skirmish involving X" is read as a time window (Uruk-hai of sets 4 and 15: a companion "killed during a skirmish involving an Uruk-hai", whatever killed him, F-0546).

- **A. A time window:** while a skirmish involving bearer lasts, no minion anywhere can take a wound. The definition becomes `cantTakeWounds` over `minion` with `requires: canSpot bearer,inSkirmish` (shown by mutate.py: every test passes under it, the parked one included). Éowyn 10_72 bearing Harmless and using her skirmish ability ("wound a minion for each wound on each minion skirmishing Éowyn") then wounds nobody.
- **B. Keep as built:** only the minions skirmishing bearer are protected; Éowyn's ability lands its wound on a minion waiting for another skirmish. Nothing changes, and the parked test is dropped.

Parked on it: `Card_13_093_Tests.DuringBearersSkirmishAMinionInNoSkirmishTakesNoWoundEither` (A; fails as built, the Savage waiting against Gimli takes Éowyn's wound).

Recommendation: A. It is what the words say, and the cards that mean only the skirmishing minions say so.

**Ketura (2026-10-28):** Yeah this should have been A for sure.


### Q-0388 Expendable Servants 13_107: is the event's 'you may' the play itself, or a yes/no asked after playing it?

"If you can spot more [orc] conditions than [orc] minions, you may discard your [orc] condition from play to play an [orc] minion from your discard pile." (Expendable Servants 13_107, a Shadow [orc] Maneuver event; the "you may" is the whole of its text after the requirement.)

**Built now:** the threshold is the event's requirement, and the discard of the [orc] condition and the play of the [orc] minion from the discard pile are both costs (Q-0062: a play from a pile is a cost). So the event can be played only when an [orc] condition can be discarded AND an [orc] minion in the discard pile can be paid for, and nothing is asked between playing the event and choosing the condition: playing the event is the "may".

**A (as built):** playing the event is the choice; declining means not playing it. Nothing changes. The active tests read it so: not playable with the pool one short of the only [orc] minion in the pile, and no yes/no ahead of the condition choice. The ledger carries a `no O:` line, as For the Mark 12_114's maneuver "you may exert ... to play this event" does.

**B (the "may" asked inside):** the event can be played whenever the threshold holds; a yes/no is then asked whenever the discard and the play can be paid, and a no (or nothing payable) plays the event for nothing, as Whistling Death 11_104's inner "you may remove (3) to wound that character again" is asked (F-0379's shape). The definition's cost would move into an `optional` effect, the not-playable test would turn round (playable, the yes/no withheld), and a test that declines would be added. The event could then be played for nothing to feed "each time you play an event" cards.

Parked on it: nothing (the game does A, and the tests of A are active).

Recommendation: A. With no other sentence in the text, the "you may" reads as the usual cost-to-effect wording of a Shadow event, and B would only let it be played for nothing.

**Ketura (2026-10-28):** Yeah let's do A.


### Q-0389 Orc Plains Runner 13_114: next site underground and the Runner exhausted: discard it, or not offered?

"Regroup: Discard this minion from play (or exert this minion if the fellowship's next site is an underground site) and spot another [orc] minion to return that minion to your hand."

Built now (since F-0663): with the next site underground and the Runner able to exert, it exerts, unasked; otherwise it discards itself. So with an underground next site and the Runner exhausted (vitality 2, 1 wound), the ability is offered and the Runner discards itself.

**A.** Q-0035's "mandatory to choose if it's available": the exertion is not available, so the discard outside the parenthesis is the price. The ability stays on offer. As built; the test WhenTheNextSiteIsUndergroundAndTheRunnerIsExhaustedItDiscardsItself asserts it and is active.

**B.** CR 4.2 "effect" read strictly ("You have to use the conditional effect if the condition is met"): the condition is the next site being underground, which is met, so the exertion is the only price. An exhausted Runner cannot pay it, and the ability is not offered. The definition's If would lose its `canSpot self,canExert` check and the price become `exert self` alone under the underground condition; the active test would turn to read the ability absent.

Parked: nothing (the game passes A).

Recommendation: A, since Q-0035 words the rule on availability, and the Runner's text gives the discard as the plain price.

**Ketura (2026-10-28):** lol sucks to suck, Decipher.  If they wanted that dumbass mandatory parenthetical rule, this is the sort of thing it leads to.  RAW is B.


### Q-0390 Ferthu Théoden Hal 13_125: is 'Unless you can spot a companion who has resistance 3 or less' a requirement to play, or a condition inside the effect?

"To play, spot 2 [rohan] Men. Unless you can spot a companion who has resistance 3 or less, discard a possession from play." (Ferthu Théoden Hal 13_125, a Free Peoples [rohan] Maneuver event)

**Built now:** the event's requirement is the spot of 2 [rohan] Men alone. "Unless" is an `If` inside the effect (`cantSpot companion,maxResistance(3)`), so with a companion at resistance 3 or less the event can still be played, and it discards nothing. No other card opens an event's effect with "Unless you can spot".

**A (requirement to play):** the opening "unless" is checked before the event is played, as an opening "If <a state of the game>," on an event is (shapes.md, "If there are N or more <cards> in your discard pile": CR 4.2 "cost"), and as CR 4.2 "spot" says a spot "sets up a requirement for playing a card". The event is not playable while a companion at 3 or less can be spotted. The correction moves the `cantSpot` into the event's `requires` beside the spot of 2 [rohan] Men; the two parked tests go live and nothing active changes.

**B (as built, a condition of the effect):** the event can be played with such a companion in play, and does nothing but leave the hand. That lets it be played for nothing to feed "each time you play an event" cards. Nothing changes; the parked tests are deleted and a test of the empty play at exactly resistance 3 is written.

Parked on it: Card_13_125_Tests.IsNotPlayableWhileACompanionHasResistance3OrLess (Household Guard 5_83 at exactly 3 after three burdens) and IsNotPlayableWhileOnlyAShireHobbitCompanionHasResistance3OrLess (Merry 1_303 the only companion at 3, beside Éomer 13_123 and Gúma 4_277). Both fail today at the play read not available.

Recommendation: A. The card already prints a "To play" sentence, but an opening condition on an event that leaves the effect empty reads as part of whether it can be played at all.

**Ketura (2026-10-28):** Huh, interesting.  I'm torn on this one, because I could see wanting to play-for-no-effect to cycle it (and if you have a companion with res 3 or less, you're probably not ever going to not be able to spot it), but you're right that a good number of events (usually older events) have this sort of thing baked into its wording.  Let's go with B for now.


### Q-0391 Assault Commander 13_158: does 'for each card that has the same card title' count copies outside play?

"Shadow: Exert this minion and spot a Free Peoples condition to make an unbound companion resistance -1 for each card that has the same card title as that condition." (Assault Commander 13_158)

*As built:* the count is `forEachYouCanSpot` of `nameFromMemory(spottedCondition)`: only cards of that title in play are counted, the spotted condition itself included (Q-0092's shape). A copy in a hand, a discard pile, the dead pile or a draw deck is not counted. The text names no zone: its sister cards each name one (Pirate Cutthroat 13_97 "for each card you spot", Orc Raid Commander 13_115 "discard from play each other card"), and CR 4.2 "for each" and "spot" do not settle a count with no "spot" in it.

- **A. Cards in play only (as built):** two Lords of Moria 1_21 in play, a third in the Free Peoples hand and a fourth in his discard pile, read -2. Nothing changes.
- **B. Every card of that title, wherever it is:** the same board reads -4, and copies in hands, piles and decks lower the resistance too. The definition would need a count over every zone (the definition language has no single count for it), and the active test's reading of 2 becomes 4.

No test is parked: the game does A, and `Card_13_158_Tests.ShadowAbilityExertsThisMinionToMakeAnUnboundCompanionResistanceMinusOneForEachCardOfTheSpottedTitleUntilTheEndOfThePhase` asserts it, with the copies in hand and in the discard pile as its controls.

Recommendation: A. "Spot a Free Peoples condition" sets the table as the place the card looks, and a count of hidden zones would let a hand the Shadow player cannot see change the number.

**Ketura (2026-10-28):** Ha, definitely missing 'you can spot'.  Man....I guess in this case let's interpret it with the missing 'you can spot', because otherwise you could count your *own* freeps cards, and yeah do even more unintuitive things like copies removed from the game, or in discard, or hell in the deck.  A.


### Q-0392 New Chapter 13_154: 'heal a companion (or heal Sam twice)' with Sam unwounded or with one wound

"Each time you play a follower, you may heal a companion (or heal Sam twice)." (New Chapter 13_154)

*As built (since F-0666, at this landing):* an `If` on `canSpot` of `name(Sam),wounded`. While Sam has a wound, Sam is healed twice, unasked, and no other companion can be chosen. While Sam has no wound (or is not in play), any companion is chosen and healed once. Under Q-0035 the parenthesis is mandatory when it is available; the doubt is what makes healing Sam twice "available". Healing an unwounded Sam does nothing, and healing a Sam with one wound twice heals only one.

- **A. Sam with any wound (as built):** with one wound on Sam, Sam is healed of it and nobody else is; with Sam unwounded, another wounded companion is chosen and healed once. Nothing changes.
- **A2. Sam with 2 or more wounds:** with one wound on Sam the player chooses any companion and heals once (Sam may still be chosen). The check becomes a count of 2 wounds on Sam; WithSamWoundedOnceSamIsHealedAndNoOtherCompanionIsChosen is rewritten to the choice.
- **B. Sam in play at all:** with Sam unwounded, the heal goes to Sam and does nothing, and no other companion can be healed. The check becomes `canSpot name(Sam)`; WithSamInPlayAndUnwoundedAnotherCompanionIsHealed is rewritten to read no heal.

Tests: Card_13_154_Tests.WithSamInPlayAndUnwoundedAnotherCompanionIsHealed (A against B) and WithSamWoundedOnceSamIsHealedAndNoOtherCompanionIsChosen (A against A2), both active and passing as built. Nothing is parked.

Recommendation: A. A heal that can heal nothing is not "available", and one wound healed is still Sam healed.

**Ketura (2026-10-28):** haha, I guess A2.  If A1 were possible, then it would naturally bring into question why it doesn't also apply to a board state with no Sam at all, since neither are fully capable of performing it.


### Q-0393 Keep or strike: a counted draw ('draw a card for each ...') can now be a branch of a choice without failing the game; no printed card uses it (F-0629)

**What changed.** Engine pass 69 mended F-0629: `DrawCards.isPlayableInFull` (the check "can this draw be done in full", asked of each branch of a choice, of a cost, of an `If` inside a choice) read a draw's count with no action context. A count that is not a plain number (`forEachYouCanSpot`, a count read from memory, a `Conditional`) then threw, and the game failed as the card was played. It now reads the count in the action's context, as the draw itself already did when carried out. One line in `DrawCards.java`.

**Which cards behave differently.** None today. Hearth and Hall 17_8 was the one card with that shape ("choose one: draw a card for each hunter you can spot or ..."), and F-0628 had already rewritten its definition to draw one card at a time inside a repeat, which never asked the faulty line. A search of every definition finds no other draw with a non-constant count in a place where this check is asked. Tried by hand: with 17_8's old shape put back for a run (not committed), its 6 tests threw the NullPointerException before the change and all pass after it.

**No test.** No printed card reaches the line, and under Q-0241 (and Q-0271's "no precedent") no card or definition text is invented to show it. The code comment says the change is exercised by no card.

**Keep or strike.** Keep: future cards (and 17_8, if anyone writes it back in the plain shape) can use a counted draw as a branch of a choice. Strike: the line goes back to `count.getEvaluator(null)` and any such definition fails the game again. The change stays in unless struck.

**Ketura (2026-10-28):** Regarding Hearth and Hall and F-0629: we don't need cost-handling support for this.  HAH is clearly a templating issue, which should have been written "Spot an [Elven] hunter *to* choose one...".  Go ahead and strike that change.


### Q-0359 Sneaking! 7_73: the Ring-bearer Sméagol's skirmish cannot be cancelled in the format; is he healed, is the event played for nothing, or can it not be played?

"Heal Sméagol (or cancel Sméagol's skirmish if he has more vitality than the minion or minions he is skirmishing)." CR 4.2 "Ring-bearer" names the card: "If Sméagol is the Ring-bearer, you cannot play ... Sneaking to cancel his skirmish".

Built now: an `if` on the vitality comparison, with `cancelSkirmish` as its true branch and the heal as its false. With Sméagol, Bearer of Great Secrets 9_30 as the Ring-bearer, 1 wound, against a Mordor Fighter 7_286 (vitality 2) under multipath, the event is offered and played, the engine refuses the cancel (the format bars cancelling the Ring-bearer's skirmish), and nothing else happens: he keeps his wound and the skirmish goes on. Under fotr_block the skirmish is cancelled, as the text says.

**A**: the cancel cannot be carried out, so the parenthesis is not available and the heal is done: he is healed and the skirmish goes on. The parked test asserts this. No definition-only correction exists (the `if` has no check that reads the format's bar; `CancelSkirmish.isPlayableInFull` reads it inside the engine), so A is an engine item.

**B**: the condition in the parenthesis is met, so its branch is mandatory (CR 4.2 on parentheses, Q-0035); the cancel is refused and the event is played for nothing. As built; the parked test would be rewritten to read his wound kept.

**C**: the CR's plain words, "you cannot play ... Sneaking to cancel his skirmish": on that board the event cannot be played at all. Then the active test InMultipathTheRingBearerSmeagolsSkirmishIsNotCancelled, which asserts the event offered, and the ledger's R2 multipath `-` change with it, and the engine would have to withhold the play.

Unlike Slunk Out of Sight 10_115 (Q-0354: A), the heal here is not a separate effect but the other branch of the parenthesis.

Parked: Card_07_073_Tests.InMultipathTheRingBearerSmeagolIsHealedSinceHisSkirmishCannotBeCancelled.

Recommendation: C reads the CR example most closely and matches how the engine already withholds other offers that can do nothing; B is how the game plays now.

**Ketura (2026-10-27):** This is simplified somewhat by the fact that there is no format which has both a Smeagol Ring-bearer and permits for Ring-bearer skirmish canceling (indeed, it was with set 9 that the rules were changed).  Therefore, we can simplify this: treat the text as if the parenthetical said "(or cancel Smeagol's skirmish if he is not the Ring-bearer and he has more vitality than the minion or minions he is skirmishing)".  I think this is closest to A, but it should not require engine-work, only a check for him being the Ring-bearer.


### Q-0360 Sneaking! 7_73: "more vitality than the minion or minions he is skirmishing": against their total, or each one?

"Heal Sméagol (or cancel Sméagol's skirmish if he has more vitality than the minion or minions he is skirmishing)."

Built now: the minions' vitality is added together (`forEachVitality` over `minion,inSkirmishAgainst(name(Sméagol))`), and Sméagol's vitality left is compared with the total.

**A**, the total: Sméagol, Poor Creature 6_45 with 3 vitality left against two Mordor Fighters 7_286 of 2 each is not more than 4, so he is healed and the skirmish goes on. As built; the active test AgainstTwoMinionsTheirVitalityIsAddedTogether asserts this.

**B**, each one: 3 is more than either 2, so his skirmish is cancelled and he is not healed. The definition would compare with each minion (no minion in his skirmish with as much vitality as he has left), and the active test would turn round.

No rule or ruling speaks to "more vitality than the minion or minions", and no other card prints the shape. Nothing is parked; the active test holds reading A.

Recommendation: A, totals by side as strength is counted in a skirmish.

**Ketura (2026-10-27):** A


### Q-0361 Easterling Assailant 7_140: do the parenthesis's "2 Easterlings" take in the Assailant himself?

"**Skirmish:** Remove a burden and exert this minion to heal another Easterling (or 2 Easterlings if you have initiative)."

What is built now: one heal of `choose(another,easterling)`, count 2 when the Shadow player has initiative and 1 otherwise. The Assailant, wounded by his own exertion, is never one of the Easterlings healed.

**A**, as printed: the parenthesis says "2 Easterlings" with no "other", so under initiative the Assailant may be one of the two. With initiative and one other Easterling wounded, both he and that Easterling are healed. The definition would become an `If` on `haveInitiative`: heal `choose(easterling)` count 2 when true, heal `choose(another,easterling)` when false. A reviewer's mutation run shows the change breaks no active test, and the parked test then passes.

**B**, "another" carried into the parenthesis: only other Easterlings are healed, as the game does today. Nothing changes, and the parked test is deleted.

Parked: Card_07_140_Tests.WithInitiativeTheTwoEasterlingsHealedMayBeHimselfAndAnother (with initiative, Khamûl 103_61 the only other wounded Easterling: both he and the Assailant healed). Against the game today it fails at the Assailant's wounds (1 where 0 is read).

Q-0339 (Dunlending Pillager 4_13) reads a parenthesis's noun as printed, but that was a "may" trigger, and this is a required effect after costs, so it does not settle this. No other card prints "another X (or N Xs ...)".

Recommendation: A. The parenthesis's noun is read as printed elsewhere (Q-0339, F-0554), and nothing in the text carries "another" into it.

**Ketura (2026-10-27):** Hmm, I think this is B. There's sort of an implied "another 1 Easterling" and it's only the number being affected, not the kind.  I think otherwise it leads to abuse with the Assailant exerting as many times as there are burdens.


### Q-0358 The Dead City 7_56: is "about to be killed in a skirmish" his own skirmish, or any kill in the skirmish phase?

"If Sméagol is about to be killed in a skirmish, he is discarded instead." (The Dead City 7_56)

Built now: an `aboutToBeKilled` trigger on `name(Sméagol)` with `requires: canSpot name(Sméagol),inSkirmish`, so only a kill while Sméagol is himself in a skirmish is turned into a discard.

**A** (as built): "in a skirmish" means Sméagol is himself in a skirmish. A kill by the wound of his own lost skirmish or an overwhelm is a discard; a wound another card deals him during the skirmish phase while he waits for (or has finished) his own skirmish kills him as usual, as does an archery wound. Nothing changes.

**B**: "in a skirmish" means while a skirmish phase is going on, the reading shapes.md already settles for the sister sentence "If <a character> is about to take a wound in a skirmish" (The Ruling Ring 1_2, King of the Dead 8_38 and six more: `requires: phase skirmish`, a wound dealt to a character waiting for his skirmish is answered). Under B the `requires` becomes `type: phase / phase: skirmish`, and a kill during the skirmish phase outside his own skirmish (Orkish Skirmisher 18_90's wound after it beats Aragorn) is a discard too. Archery stays a kill under either reading.

Test turning on it: Card_07_056_Tests.KilledDuringASkirmishPhaseWhileInNoSkirmishSmeagolIsKilled takes A and passes, so it is active; under B it flips to expect the discard. A mutation run showed no other test turns on it. The CR 4.2 "instead" example (Sméagol 9_30 as Ring-bearer, killed not discarded) is unaffected. Uruk Reserve 13_173 ("about to be killed in a skirmish", built `inSkirmish` in its trigger filter) is built the same way as 7_56 and one answer covers both.

Recommendation: B, for the same words to play alike with "about to take a wound in a skirmish"; A only if "in a skirmish" on a kill is meant more narrowly than on a wound.

**Ketura (2026-10-26):** B, yeah, based on the Ring text.


### Q-0348 Celeborn 10_7, Elrond 10_9 (and Legolas 13_18): is a no-cost optional 'you may reveal the top card of your draw deck' trigger offered with that deck empty?

"At the start of each skirmish involving Celeborn, you may reveal the top card of your draw deck. If it is an [elven] card, you may discard it to wound each minion in that skirmish." (Celeborn, Lord of the Galadhrim 10_7). Elrond, Venerable Lord 10_9 ("... you may reveal the top card of your draw deck. If it is an [elven] card, you may heal another Elf.") and Legolas, Of the Grey Company 13_18 ("At the start of each archery phase, you may reveal the top card of your draw deck. If it is an [elven] card, wound a minion.") print the same optional reveal with no cost, so one answer covers all three.

Built now: the trigger requires only that the card is in that skirmish (for Legolas, nothing). With the draw deck empty it is offered; taken, nothing is shown and nothing follows.

- **A (as built).** Offered, and does nothing, as the optional look through an empty draw deck is (Q-0104: "having that underlined by the ask still happening and showing nothing would help them realize it") and as an optional draw is (Q-0029). Nothing changes. `Card_10_007_Tests.WithAnEmptyDrawDeckTheRevealIsStillOfferedAndDoesNothing` and `Card_10_009_Tests.WithAnEmptyDrawDeckTheRevealIsStillOfferedAndDoesNothing` read this and pass.
- **B.** Withheld, as Glorfindel 9_16's reveal is (Q-0148: "it has to happen for the rest to occur, so an empty deck should disallow the ability"): the rest of each text reads the card revealed. `requires: {type: HasCardInDrawDeck}` would go on the trigger of 10_7, 10_9 and 13_18, and the two tests above would turn round to read the offer absent with the deck emptied (and offered with one card left).

Nothing is parked: the game passes reading A and both tests of it are live.

Recommendation: A, lightly. The "you may" makes this a choice the player is offered, the case Q-0104 and Q-0029 keep on offer; Q-0148 was a mandatory reveal at the head of an ability.

**Ketura (2026-10-26):** B, I see no reason to pester the user when they can't actually reveal anything.


### Q-0349 Fifth Level 10_32: may it be transferred again from the minion bearing it?

"Bearer is strength -1 for each fortification you can spot. Maneuver: Exert 2 [gondor] Men or spot 3 knights to transfer this condition to a minion." (Fifth Level 10_32)

Built now: the transfer has no `fromSupport`, so once a minion bears it the Maneuver ability is offered again (in the same phase too), its price is paid again, and it moves to another minion; the minion it leaves gets its strength back. Every other Level of sets 5 to 8 prints "from your support area" (First Level 7_92 has `fromSupport: true`); Fifth Level does not. Q-0022 asks for a card saying only "transfer this" to be put to you card by card; the nearest answer is Sticky Strands 103_14 (Q-0064: "the intent is that it can be pulled from other companions").

- **A (the letter, as built).** It may be moved on from the minion bearing it. Nothing changes. `Card_10_032_Tests.BorneByAMinionItCanBeTransferredAgainToAnotherMinion` reads it moved from Goblin Patrol Troop to Uruk Savage, the Troop's strength restored; it is live and passes. With 3 knights the price is a spot only, so the move can be repeated as often as the player likes in one maneuver phase (a voluntary loop, Q-0321).
- **B (as the other Levels).** Only a copy in the support area may be transferred: `fromSupport: true` goes on the transfer, and that test turns round to read the ability not offered for the copy on the Troop.

Nothing is parked: the game passes reading A and its test is live. Soldier's Cache 17_35 and 18_58 are the other fortifications that print only "transfer this condition to a minion"; one answer could be taken to cover them when their sets come up.

Recommendation: A, lightly, by the letter and by Q-0064; B if the dropped words are taken for an oversight of set 10's printing.

**Ketura (2026-10-26):** Let's do B for now.  There's little use for strength in Maneuver and I don't think there's any time the exerts would matter, so I don't think there's any legit use for this.


### Q-0350 Drawing His Eye 10_29: with no Shadow card revealed, does 'its owner' still draw?

"For each [gondor] companion you spot, reveal 1 card at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner draws a card." (Drawing His Eye 10_29, Fellowship event)

What is built now. The draw is `drawCards`, `player: shadow`, after the discard and with no condition on it. So when every revealed card is a Free Peoples card of the Shadow player's own (his hand holds only those, or the random reveal shows only those), nothing is discarded and the Shadow player still draws a card.

- **A.** "Its owner" is the owner of the card discarded: with no Shadow card revealed, nothing is discarded and nobody draws. The definition would remember the discarded card and draw only if one was discarded. `Card_10_029_Tests.WithNoShadowCardRevealedNothingIsDiscardedAndNoCardIsDrawn` goes live; nothing else changes (checked with mutate.py: every other test passes under the correction).
- **B.** The draw is done whatever was discarded, as built: the Shadow player draws a card even when none of his cards was discarded. That test is turned round to read the draw.

Parked on this: `Card_10_029_Tests.WithNoShadowCardRevealedNothingIsDiscardedAndNoCardIsDrawn` (fails at the Shadow hand count: 3 where A expects 2).

Recommendation: A. "Its owner" has nothing to refer to when no card is chosen, and the draw reads as making up for the card lost; unlike Q-0314's "Then," the draw is in the same sentence as the discard and names that card.

**Ketura (2026-10-26):** Hmm.  I guess let's do A.


### Q-0351 Field of the Fallen 10_43: two dead characters of one culture, -1 or -2 to that culture's companions?

"While there is a character in the dead pile, each companion of the same culture as that character is strength -1."

Built now: one `modifyStrength` -1 over `companion,cultureInDeadPile`. A companion whose culture matches any character in the Free Peoples dead pile is -1, once, however many characters of that culture are there.

**A** (as built): the sentence is a condition. Each companion of a culture found among the dead-pile characters is -1 once. With Gondorian Merchant 7_97 and Gondorian Knight 5_35 both in the dead pile, Aragorn 1_89 (strength 8) reads 7. Nothing changes; the active test AnAllyAndADwarfInTheDeadPileLowerTheCompanionsOfBothCulturesByOne already reads 7.

**B**: "that character" is taken for each character in turn, so a companion is -1 for each dead character of its culture. Aragorn would read 6 on that board. The definition would need a strength amount counting the dead pile's characters of the companion's culture, and the test's reading would flip from 7 to 6.

Parked: nothing. The test of A is active and passes, so it notices a mistake there meanwhile.

Recommendation: A. "While there is a character" reads as a condition, and the -1 is a single modifier.

**Ketura (2026-10-26):** What an oddly worded card. Let's do A.


### Q-0352 High Vantage 10_44: is 'each time a Southron is about to take a wound' on an event a snapshot of the Southrons in play when it is played?

"Until the end of the assignment phase, each time a Southron is about to take a wound, prevent that." (High Vantage 10_44, a Shadow event)

Built now: the event lays an `aboutToTakeWound` trigger over `southron` until the end of the assignment phase, so every Southron in play at the moment of the wound is protected, including one played after High Vantage in the same Shadow phase.

The doubt: CR 4.2 "event" says some events take a "snapshot" and affect only the cards in play when played (its examples are "make each ... until"), and Depart Silently 3_31's bar on Hobbits for a time was made a snapshot (F-0558). High Vantage instead lays an "each time ... is about to" trigger for a time; no ruling, and no other printed event of this shape, settles which way it reads.

- **A (snapshot).** Only the Southrons in play when High Vantage is played are protected; a Southron played afterwards takes its wounds. The definition would get a `memorizeActive` of `southron` ahead of the trigger, with the trigger's filter and the `preventWound` over the memory, and `Card_10_044_Tests.ASouthronPlayedAfterHighVantageIsProtectedToo` would turn round to read the later Southron wounded.
- **B (as built).** Any Southron about to take a wound before the assignment phase ends is protected, since the "each time" is asked at each wound. Nothing changes. `ASouthronPlayedAfterHighVantageIsProtectedToo` reads this and passes.

Nothing is parked: the game passes reading B and its test is live.

Recommendation: B. The text names a trigger checked each time, not a change made to each Southron, and CR 4.2's snapshot examples are all of the "make each" kind.

**Ketura (2026-10-26):** Oof.  Let's do B.


### Q-0353 Gorbag's Sword 10_60: may its "discard a possession" choose the Sword itself?

"When you play this possession, you may discard a possession. Its owner may place it on top of his or her draw deck instead." (Gorbag's Sword 10_60)

Built now: an optional trigger on the Sword's play whose choice is `choose(possession,canBeDiscarded)`. The Sword is in play on its bearer when the trigger resolves, so it is among the candidates, beside every other possession of either side.

**A** (the plain words, as built): the Sword may choose itself. The offer is made on every play, including with no other possession in play (choosing the Sword then discards it, or its owner tops his deck with it). Nothing changes; the active tests already assert the Sword offered and never choose it.

**B** ("another possession" was meant): the Sword is not a candidate. `another` goes into the choice, and with no other possession in play the engine withholds the offer (Q-0224). The test's reading of the Sword as offered becomes `ShadowHasCardChoiceNotAvailable(sword)`, and a test of the withheld offer on a board with no other possession is owed.

Parked: nothing. The game passes reading A, so the tests of it stay active (WhenPlayedItMayDiscardAnyPossessionAndAFreePeoplesOwnerMayPutItOnTopOfHisDrawDeckInstead, IsPlayedOnlyOnAWraithOrc), with this number on the ledger's Findings line.

Recommendation: A. The text says "a possession", not "another", and choosing the Sword only costs its own player the card.

**Ketura (2026-10-26):** "Another" was definitely meant, but thems the breaks when one authors it wrong.  A, but make it a 0-1 choice so they don't accidentally nuke themselves upon selecting it with no other possessions on the field.


### Q-0354 Slunk Out of Sight 10_115: under a format that refuses the Ring-bearer's cancel, are his skirmish's minions still made fierce?

"If there are 6 or more [shire] cards in your discard pile, cancel a skirmish involving your Hobbit and a minion who is not fierce. Each minion in that skirmish is **fierce** until the regroup phase." (Slunk Out of Sight 10_115)

The doubt arises only under a format that refuses to cancel the Ring-bearer's skirmish (multipath, movie: Q-0001). There the event can be played in the Ring-bearer's skirmish against a minion who is not fierce, and the cancel does not happen.

Built now: the event's effect is an `If` on the skirmish's make-up (your Hobbit, and a minion who is not fierce), and inside it the skirmish's minions are memorized, then `cancelSkirmish`, then `addKeyword fierce` over the memory `until: regroup`. When the format refuses the cancel, the minions are still made fierce.

**A** (as built): the second sentence names the skirmish the text describes, and that skirmish exists. It does not hang on the cancel being carried out (CR 4.2 "effect", do as much as you can). Under multipath, Frodo's skirmish goes on and its minion is fierce until the regroup phase. Nothing changes. The active test InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelledThoughItsMinionIsMadeFierce reads this, and the game passes it.

**B**: "that skirmish" is the one cancelled. With nothing cancelled, nothing follows, as Return to Its Master 1_224's second half now lays nothing when its assignment fails (F-0386). The `addKeyword` would stand in its own `If` that the skirmish is gone, and the multipath test would read the minion not fierce.

Parked: nothing. The game passes reading A, so its test stays active, with this number on the ledger's Findings line.

Recommendation: A. The text puts no "if you do" between the two sentences. The only cost of A is a Free Peoples event that hands the Shadow a fierce minion for nothing.

**Ketura (2026-10-26):** A is fine.


### Q-0355 Brought Down from Inside 19_24: may the transfer go to a [rohan] ally away from his home site?

"At the start of each skirmish, you may transfer this condition to a [rohan] Man." (Brought Down from Inside 19_24; the erratum 69_24 prints the same sentence)

Built now: the transfer offers every [rohan] Man in play but the bearer, allies included, wherever their home sites are. On the Fellowship path, Guma 4_277 and Hlafwine 4_281 (home 4T) are at no site, and they are still offered; the condition can start on one of them too.

CR 4.2 "transfer" says "Both characters involved in the transfer must be at the same site. (See ally.)". That sentence stands in the paragraph on the player's own fellowship-phase transfer; the paragraph on an effect's transfer to an "eligible bearer" names only the card's bearer and timing requirements. Q-0318 (an ally is at his home site) and Q-0042 (Breaching Shot may reach an ally away from home) are the nearest answers.

- **A (the same-site sentence binds an effect's transfer too).** An ally is offered only while the fellowship is at his home site (or, for a card already on an ally away from home, it could not be moved off him either). The transfer's target filter gains an `atSite`/same-site word (none exists for "the bearer's site"; engine work), and `AtTheStartOfEachSkirmishItMayBeTransferredToAnotherRohanMan` in Card_19_024_Tests and Card_19_024_ErrataTests turns round for Guma and Hlafwine. It would reach every "transfer this ... to a <race>" card that can name an ally.
- **B (as built).** An effect's transfer obeys the bearer text and timing only; an ally away from home is offered. Nothing changes.

Nothing is parked: the game passes B, and both files' tests of it are live.

Recommendation: B, lightly: the sentence belongs to the fellowship-phase transfer of possessions, and Q-0042 lets Shadow effects reach allies away from home.

**Ketura (2026-10-26):** So.  Normal transfer rules about being at the same site are for *item* transfer.  Transferring conditions is a whole nother kettle of fish, and has (IIRC) has no restriction rules associated with it.  B.


### Q-0356 Éowyn, Lady of the Mark 19_26: is 'a twilight cost of 3 or more' the printed cost or the cost to play now?

"Fellowship: Play a [rohan] companion with a twilight cost of 3 or more; his or her twilight cost is -1." (Éowyn, Lady of the Mark 19_26)

Built now: the filter reads the PRINTED cost (`minTwilight(3)`). Rohirrim Soldier 15_138 is printed at 3 but costs 2 to play while a [rohan] Man can be spotted (his own text); beside Éowyn he is offered, and played for 1 (3 - 1 - 1).

- **A (printed cost, as built).** CR 4.2 "twilight cost": cost modifiers act only as the card comes into play, and the card is picked before its play begins (F-0109, Q-0073 read the printed number for a card in play). Nothing changes; `ACompanionPrintedAtThreeWhoseOwnTextLowersHisCostIsOffered` stays live.
- **B (the cost he would be played for now).** The Soldier, and any [rohan] companion printed at 3 whose own or another card's discount brings him under 3, is not offered. The definition's filter changes to read the cost-to-play, and that test turns round to read him not offered. The same question then reaches every "play a <kind> with a twilight cost of N or more/less" card.

Nothing is parked: the game passes A, and its test is live.

Recommendation: A, by CR 4.2 "twilight cost".

**Ketura (2026-10-26):** A, yeah.  Dumb rule, but this is one of the reasons why it's dumb.


### Q-0357 Spirit of Dread 16_5: does "(except by Wraiths)" reach the heal half as well as the burden half?

"While you can spot another [wraith] Wraith, wounds cannot be healed and burdens can not be removed (except by Wraiths)."

Built now: two bars under the one spot. `cantHeal` over `any` (no exception: every heal is refused), and `cantRemoveBurdens` over `not(wraith)` (a Wraith's own text may still remove a burden). So the parenthesis is read as belonging to the burden half only.

**A**: the parenthesis closes the whole sentence: a Wraith's own text may still heal under the bar, as it may still remove a burden. No Decipher Wraith heals (Spectral Sword 8_46, Ring of Rancor 9_44 and Úlairë Lemenya 10_69 are not Wraiths' own text), so the only card that tells the readings apart is the V-set King of the Dead, Dead Keeper 103_27 ("Assignment: Exert King of the Dead and restore a Wraith to heal that Wraith"): under A he heals the restored Dead Man of Dunharrow 10_27. The definition cannot say this today: the heal bar (`CantHealModifier.canBeHealed(game, performingPlayer, card)`) carries no source card, so A needs an engine word (a source filter on `cantHeal`) and then the definition's `cantHeal` gets the same `not(wraith)` source filter. A filter on the HEALED card instead would wrongly free wounded Wraith minions, which the tests show barred.

**B**: the parenthesis belongs to the burden half only (as built): every heal is refused while the bar holds, a Wraith's included. Nothing changes; the parked test is dropped.

Parked: Card_16_005_Tests.WithAnotherWraithWraithAWraithsOwnTextMayStillHeal (reading A). With its @Ignore taken out it fails only at the Dead Man's wound (expected 0, was 1); the same board without Spirit of Dread heals him to 0.

Recommendation: B. The parenthesis sits against "burdens can not be removed", and the only card that can tell the readings apart is a V-set card printed years later; A costs an engine word for that one interaction.

**Ketura (2026-10-26):** Yes, it should affect both halves.  A.


### Q-0346 Reckless We Rode 8_12 (and A Grey Ship 8_9): is X held to the fellowship archery total, or may it run past it to the '(to a minimum of 0)'?

"Exert an Elf companion and make the fellowship archery total -X (to a minimum of 0) to wound a minion X times. You cannot use archery special abilities." (Reckless We Rode 8_12)

Built now: after the Elf exerts, the Free Peoples player names X by a number choice from 0 to the fellowship archery total as it stands (`chooseANumber` up to `archeryTotal`), the total falls by X, and the chosen minion is wounded X times. With a total of 2 he may name at most 2. A Grey Ship 8_9 prints the same price ("... -X (to a minimum of 0) to heal X unbound companions") and is built the same way.

The doubt: Q-0191: A (Race Text 94_10) held an X taken off a value as a price to the value there, but that text ("damage -X") had no floor written. Here the card prints "(to a minimum of 0)", which does no work if X can never pass the total. Neither rules document has the phrase, and the rulings log has nothing on it.

- **A (as built).** X is held to the current fellowship archery total; the parenthesis is reminder text that a total never goes below 0. Nothing changes. `Card_08_012_Tests.ExertsAnElfCompanionAndTakesXOffTheArcheryTotalToWoundOneMinionXTimes` reads the choice as 0 to 2 at a total of 2; it is live and passes.
- **B.** X may be named above the total, and the total simply stops at 0: with a total of 2 he could name 5 and wound the minion 5 times (A Grey Ship: heal 5 unbound companions) for the same price as 2, and at a total of 0 the card still wounds. The maximum of the number choice would become unbounded or some larger cap, the definitions of 8_12 and 8_9 would change, and that test would read the new maximum and a wound count above the total.

Nothing is parked: the test reads A, is live, and passes.

Recommendation: A. Under B the price stops being a price once X passes the total, and a cost is paid in full or not at all (CR 4.2 "cost"); the parenthesis reads as Decipher's reminder that the total cannot go negative.

**Ketura (2026-10-21):** Oh yes, X is bounded to the current archery total; the intent is to prevent you from saying -100 and immediately wounding everything, to no penalty because a negative archery total doesn't mean anything.  94_10 also has a similar limit built into it. A.


### Q-0347 A Grey Ship 8_9: may X in "archery total -X (to a minimum of 0)" exceed the total?

"Spot an Elf companion and make the fellowship archery total -X (to a minimum of 0) to heal X unbound companions."

**Built now:** X is a number prompt from 0 to the current fellowship archery total, so X can never be more than the total; the total then drops by X and X wounded unbound companions are healed.

**A:** X may be higher than the total (up to the number of wounded unbound companions), and "(to a minimum of 0)" stops the total going below 0. With a total of 1 the player could heal 3 companions for 1 point of archery, or with a total of 0 heal companions for nothing. The prompt's maximum would be the count of wounded unbound companions; both tests that read the maximum (2 at a total of 2) would change, and a new test would heal more companions than the total.

**B (as built):** X is the price, paid in full, so it cannot be more than the total. The parenthesis only says the total is never driven below 0. Nothing in the tests changes.

**Tests:** none parked. TheXChosenComesOffTheFellowshipArcheryTotalAndThatManyUnboundCompanionsAreHealed and WithXOf2TheTotalLosesBothForTheArcheryPhaseAndTwoWoundedUnboundCompanionsAreHealed (Card_08_009_Tests) read B and pass. Reckless We Rode 8_12 prints the same cost and would follow the same answer.

**Recommendation:** B. It matches Q-0191: A (Race Text 94_10's "damage -X" held to the bonus there). Against that, the parenthesis adds nothing under B, and that may be why it was printed.

**Orchestrator (2026-10-21): the same question as Q-0346** (filed a moment earlier by dec-b8_12 on Reckless We Rode 8_12, with A Grey Ship 8_9 beside it). Closed here; Ketura's answer to Q-0346 reaches both cards.

### Q-0345 Battle in Earnest 8_2: may one minion take more than one of the X wounds?

"Spot a Dwarf who is damage +X to place X wounds on minions." (Battle in Earnest 8_2, a Regroup event)

Built now: a repeat, X times, of a choice of one minion to wound, so the same minion may be chosen for more than
one of the X wounds.

- **A:** the X wounds go on minions any way the player wishes, so one minion may take several. This is the
  "assign wounds ... in any way he wishes" of archery and threats (CR 4.2), and how "wound a minion ... once for
  each" is read (A Host Avails Little 1_251, Behold the White Rider 4_88). It is what the game does: nothing
  changes. The active test BothWoundsMayBePlacedOnOneMinion (Gimli at damage +2, Uruk Savage takes both wounds,
  Goblin Patrol Troop none) stays.
- **B:** the X wounds go on X different minions, as "wound X minions" would (CR 4.2 "wound": several characters
  wounded are different characters). With 2 minions and X of 2 each takes one; the minion wounded first is not
  offered for the second wound. The definition would become one choice of X different minions (and with fewer
  minions than X, the extra wounds are lost), and BothWoundsMayBePlacedOnOneMinion would be reversed.

Nothing is parked on it: the game passes reading A, and the test of A is active.

Recommendation: A. "Place X wounds on minions" names wounds, not minions, as the thing counted, which is how
archery's wounds are spread.

**Ketura (2026-10-21):** Yes, this is A; "place X wounds on Y" is the same structure as "wound a Y for each X"


### Q-0344 Keep or strike: a character replaced in a skirmish is no longer involved in it when it is about to end

Engine pass 57 fixed F-0563 ("yeah that's a bug"): a character who replaces another in a skirmish (Brave and Loyal 10_105, Counts But One 8_4, Mighty Steed 8_17, Pippin 11_170, Gandalf 12_27, Blade of Gondor 12_42, Guardian 12_50, Frodo 13_149, Controlled By The Ring 15_41, and the PC cards 101_65, 102_56, 103_15, 103_16) is now "involved" in it when it is about to end, so Sting 8_113 on Sam is offered after Sam comes in for Merry.

The item went one step further than your words, and that step changes play: the character who was REPLACED is no longer involved. Before, the engine counted everyone the skirmish began with. Now:
- Sting on Sam, when Merry replaces Sam in a fierce skirmish by Brave and Loyal, is not offered (it was).
- Loathsome 7_182 ("If a skirmish that involved a Nazgûl is about to end"), when Gollum replaces the only Nazgûl by Controlled By The Ring 15_41, is not offered (it was). Final Cry 2_57 likewise if Gollum replaces The Balrog. Loathsome and Final Cry say "involved", in the past tense.
- A character removed from the skirmish in nobody's place (Sméagol 8_27 discarding himself) or killed in it is still involved, as you ruled for Loathsome (Q-0332). Only a replacement takes a character out of the record.

Keep: the replaced character is no longer involved (as built). Strike: the replaced character stays involved as well, and the one who came in is added beside him (Sting on Sam offered whether Sam came in or went out; Loathsome offered after Gollum replaced the Nazgûl). Striking it is a one-line change in Skirmish.noteReplacement; the change is in place until you strike it.

**Ketura (2026-10-21):** Keep.  Strictly speaking I can see where you're coming from, but I don't think the replacement case needs tracked for these.


### Q-0341 Should three more 'spot X' number prompts start at all of them, as F-0561's do (Orc Insurgent 6_104, Wind That Sped Ships 8_66, Sunland Trooper 17_59)?

Your F-0561 word ("make sure that the default answer presented to the player is all of them so they don't have to click three things") is already how the game works for every "choose how many to spot" prompt: the box comes filled in with all that can be spotted. That covers Death to the Strawheads 4_8, Denizens Enraged 1_167, Alliance Reforged 4_58 and the 45 other cards built the same way. Nothing changed in the engine or the definitions. Tests now read that default on 4_8, 1_167 and 4_58.

Three cards ask a "spot X" number another way, through a plain number prompt, and that prompt comes filled in with 0:
- Orc Insurgent 6_104, "draw X cards and add (X), where X is the number of Free Peoples cultures you spot over 2"
- Wind That Sped Ships 8_66, "Make a corsair strength +1 for each [raider] token you spot (limit +6)"
- Sunland Trooper 17_59, "Spot X [men] minions stacked on a [men] possession and discard this minion to add (X)"

They were not built for Q-0248, so I left them alone. A player who accepts the prompt without typing gets X = 0 on these three, and all of them everywhere else.

A: give the three a default of all (the most the prompt allows, which on 8_66 is capped at 6), each with a test that reads it.
B: leave them filled in with 0.

**Ketura (2026-10-20):** A


### Q-0340 Keep or strike: Drawing His Eye 10_29 not playable with the Shadow hand empty (Q-0336 carried over)

"For each [gondor] companion you spot, reveal 1 card at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner draws a card." (Drawing His Eye 10_29, Fellowship event)

What changed. Under your Q-0336: B ("given that it has to reveal 'a' card, and the way it is configured I would read that as a cost. If there are no cards to reveal, the card should not be playable"), engine pass 53 gave Friends of Old 3_16, Storm of Argument 3_6 and Coming for the Ring 3_51 a requirement that the opponent's hand holds a card. Asked to find every other card whose effect begins with a random reveal from an opponent's hand with no cost before it, the pass found one more, Drawing His Eye, and gave it the same requirement: with the Shadow hand empty it can no longer be played.

Why it is asked. It differs from the three in two ways. Its reveal is "1 card for each [gondor] companion you spot", not "a card"; and the spot in front of it is not a cost that needs a card in that hand. With the Shadow hand empty, the event as it was built revealed nothing, discarded nothing, and still had the Shadow player draw a card.

Which cards play differently: Drawing His Eye 10_29 alone. With one or more cards in the Shadow hand it plays as before.

- **Keep.** The requirement stays. Drawing His Eye cannot be played while the Shadow hand is empty. `Card_10_029_Tests.IsNotPlayableWithTheShadowHandEmpty` reads that.
- **Strike.** The requirement comes off 10_29. With the Shadow hand empty the event can be played again: nothing is revealed or discarded, and the Shadow player draws a card. That test is turned round, and 10_29 comes out of the patch-note line and the shapes.md entry.

The change is made, and stays unless you strike it.

**Ketura (2026-10-20):** Keep


### Q-0343 Keep or strike: Plundered Armories 1_193's CR 4.2 ruling (it comes before optional actions of the win) gives way to your skirmish order

Plundered Armories 1_193: "Response: If your [moria] weapon is discarded, play it from your discard pile (that weapon's twilight cost is -1)."

CR 4.2 Section Three has a ruling on this card: "If a minion bearing a [Moria] weapon is discarded due to losing a skirmish, Plundered Armories takes effect before optional actions triggered by winning/losing that skirmish occur." The card's ledger held this as R1, and its test showed it: Gimli beats a Goblin Runner bearing a Goblin Spear. The Runner dies of its wound with the Spear, and the Armories play the Spear on a Goblin Patrol Troop. Only after that is Gimli's Battle Axe's "you may wound an Orc" offered.

Your skirmish order (2026-10-19) has the winners win at step 4, with everything that answers the win, before the losers lose at step 5. Engine pass 54 built that. Gimli's Axe is now offered first, while the Runner is alive and still bears the Spear. The Runner dies at step 5, and the Armories come then. Against optional actions triggered by WINNING, the ruling is reversed. Against those triggered by LOSING it still holds, because the Armories answer the discard inside step 5, ahead of the loss's optional answers. The test now shows the new order (Card_01_193_Tests, TheWinnersOptionalTriggerComesBeforeTheLoserDiesAndHisWeaponIsPlayedBack).

This is the second CR 4.2 ruling found that clashes with the new order. The first is Úlairë Otsëa 3_86, Q-0342. No others were found in the ledgers.

The change is made and stays unless you strike it.
- Keep: your order rules, and the ruling holds only for what answers the loss. The ledger's R1 is rewritten to say so.
- Strike: the Armories come before what answers the win as well. That would mean playing the loser's discard before step 4, or holding step 4's optional answers until the Armories are resolved, for this card alone. Nothing like that exists now.

**Orchestrator (2026-10-20): moot.** Engine pass 54 was reverted on her word ("we need to completely revert those two commits"), and her amended draft (answers/2026-10-20-a.md, step 4D) puts responses to the loser dying before optional responses to wins or losses, which is the CR 4.2 ruling this asked about. The card plays as it did before pass 54.

### Q-0342 Keep or strike: Úlairë Otsëa 3_86's CR 4.2 ruling (the Ring goes to Sam before Blade Tip moves) gives way to your skirmish order

Úlairë Otsëa, Ringwraith in Twilight 3_86: "Each time Úlairë Otsëa wins a skirmish, you may exert him to transfer Blade Tip from your support area to the Ring-bearer."

CR 4.2 Section Three has a ruling on this card: "If Frodo dies in a skirmish involving Otsëa with Sam also in play, The One Ring is transferred to Sam before Blade Tip may be transferred with Otsëa's game text." The card's ledger held this as R1, and its test showed it: Frodo is overwhelmed by Otsëa, Sam's response makes Sam the Ring-bearer, and only then is Otsëa's trigger offered, so the Blade Tip goes on Sam.

Your skirmish order (2026-10-19) has the winners win at step 4, with everything that answers the win, before the losers lose at step 5. Engine pass 54 built that. Otsëa's trigger now comes before Frodo is overwhelmed: the Blade Tip goes on Frodo, who is still the Ring-bearer, and is discarded when he dies. Sam then takes the Ring with no Blade Tip on him. That is the opposite of the CR 4.2 ruling. The test now shows the new order (Card_03_086_Tests, WhenFrodoIsOverwhelmedBladeTipIsTransferredToHimBeforeHeDiesAndSamTakesTheRing).

No other card's Decipher ruling in the ledgers was found to clash with the new order. Other cards whose win text acts on the Ring-bearer now act on him before he dies too.

The change is made and stays unless you strike it.
- Keep: your order rules, and the CR 4.2 ruling on Otsëa no longer holds. The ledger's R1 is rewritten to say so.
- Strike: Otsëa alone is answered after the loss, as CR 4.2 rules. That would need an exception to step 4 for this one card, keyed to it, or a new definition word for a win trigger that waits for the loss. Nothing like that exists now.

**Orchestrator (2026-10-20): moot.** Engine pass 54 was reverted on her word ("we need to completely revert those two commits"), and her amended draft (answers/2026-10-20-a.md, step 4D) puts responses to the loser dying before optional responses to wins or losses, which is the CR 4.2 ruling this asked about. The card plays as it did before pass 54.

### Q-0332 Final Cry 2_57: is a skirmish still one that 'involved The Balrog bearing Whip of Many Thongs' after he is killed in it?

"Response: If a skirmish that involved The Balrog bearing Whip of Many Thongs is about to end, wound a companion in that skirmish twice." (Final Cry 2_57)

Built now: a `skirmishAboutToEnd` trigger with `involving: name(The Balrog),hasAttached(name(Whip of Many Thongs))`. The engine remembers who was in the skirmish when the skirmish phase began, but it checks the filter against those cards as they are when the skirmish is about to end. In this engine that is after resolution. So when the loser's wound kills The Balrog (his fifth wound, against Aragorn at +15 strength) and the Whip is discarded with him, Final Cry is not offered: a Balrog in the discard pile bears nothing.

What the rules say. CR 4.2 Section Three, on Loathsome 7_182, whose trigger has the same words ("If a skirmish that involved a Nazgûl is about to end"): "This card is played as a response to the end of a skirmish that involved a Nazgûl, even if that skirmish ends before resolution." The glossary entry "skirmish phase(s)" uses the present tense: a character is in a skirmish "involving" him "only while the skirmish phase that character is assigned to is happening". LR 8 (preliminary) is about Sting's own trigger being lost when its bearer is overwhelmed, not about this.

- **A.** "Involved" is in the past tense. The skirmish did involve The Balrog bearing the Whip, so Final Cry may be played and wounds the companion twice. No definition can do this. A filter wide enough to let a dead Balrog through (`involving: name(The Balrog)`) also lets through a dead Balrog who bore The Balrog's Sword instead. The reviewer's mutate run showed this: the parked test passes and WithTheBalrogBearingAnotherWeaponAndNoWhipItIsNotOffered fails. The engine would have to record what each involved character bears as the skirmish begins and check the filter against that record. Or the definition language would need a way to say "bore X while in the skirmish". The parked test goes live unchanged.
- **B.** As built: The Balrog must still be in the skirmish, bearing the Whip, when it is about to end. Nothing changes, and the parked test is turned round to read the event not offered.

Parked on it: `Card_02_057_Tests.TheBalrogKilledInTheSkirmishItStillInvolvedHimAndTheEventIsOffered`. It reads A and fails today at `assertTrue(scn.ShadowPlayAvailable(cry))`.

Loathsome 7_182 uses the same trigger with `involving: nazgul` alone. A Nazgûl in the discard pile still matches that filter by race, so it may already play as A for a Nazgûl killed in the skirmish. This was not run.

Recommendation: A. The past tense and Decipher's clarification on a trigger worded the same way point the same way.

**Ketura (2026-10-16):** Attached is a preliminary draft of one of the Rules Council's alterations to the rules, which would be inserted into the `skirmish phase` section, which outlines the specifics of skirmish resolution in a manner akin to `playing a card` for card plays. You may consider this binding moving forward.  In this we can clearly see that step 6 is where Loathsome and Final Cry (and Sting, Bane of the Eight Legs) respond. Given that the Balrog will have died from step 5b, the Whip would have been discarded and so would not be in the skirmish at the time that Final Cry responds in step 6.  We do need to confirm that Final Cry responds to all other kinds of skirmish endings (death of the loser, canceled skirmish, removing the loser from the skirmish), but I think death of the Balrog is one case that legitimately cannot trigger Final Cry.  B.


### Q-0333 Keep or strike: They Will Find the Ring 2_81, the companions exerted run from 1, not 0

"Maneuver: Exert 2 Nazgûl and spot X burdens to exert X companions."

Built now (fixed at this landing, F-0553): after the 2 Nazgûl exert, the Shadow player chooses 1 to N companions to exert, N being the burdens in play (before, he had to take exactly N, unasked where the companions were as many). With no burden nobody is exerted.

**A** (as landed): the choice runs from 1. Q-0076 makes any X of exertions at least 1, and here X is the number of companions exerted. The test ThePlayerMaySpotFewerBurdensThanThereAre reads the minimum as 1.

**B**: the choice runs from 0. Q-0076's rider says an X of zero by spotting is still allowed, and X here is also the number of burdens spotted, so the Shadow player may spot none and exert nobody (the 2 Nazgûl exerted for nothing). The correction becomes `from: 0` and the test reads the minimum as 0 (one line each).

No test is parked: the card landed ready on A.

Recommendation: keep A. "Exert X companions" is the shape Q-0076 ruled, and the rider was about cards whose X is only spotted.

**Ketura (2026-10-16):** A


### Q-0334 Verily I Come 2_94: is a draw a Free Peoples card makes the Shadow player take, with no choice in it, the Free Peoples player's draw (Q-0251)?

Verily I Come 2_94: "To play, exert a [sauron] Orc. Plays on the Ring-bearer. Each time the Free Peoples player draws a card (or takes a card into hand) during the fellowship phase, add a burden."

Drawing His Eye 10_29 ([gondor] Fellowship event): "For each [gondor] companion you spot, reveal 1 card at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner draws a card."

What Are We Waiting For? 2_15 ([dwarven] Fellowship event): "Exert a Dwarf to make an opponent draw 2 cards. That player then chooses to either discard 2 Shadow cards from hand or skip his or her next Shadow phase."

**Built now.** A draw belongs to the player whose deck it comes from and whose hand it goes to. Verily I Come's trigger is `playerDrawsCard`, `player: fp`, `orTakesIntoHand: true`, and the engine compares that with the player id of the hand the card reached (`PlayerDrawsCard`, `DrawCardOrPutIntoHandResult`). Drawing His Eye's draw is `drawCards`, `player: shadow`, and 2_15's is `drawCards`, `player: shadowPlayer`. So when the Free Peoples player plays either one in the fellowship phase with Verily I Come on Frodo, the Shadow player draws and no burden is added.

**The doubt.** On Q-0251 you ruled that where a card makes an opponent do a thing and he has no choice in it, the card's player is the one doing it ("Make an opponent X" sometimes seems like redundant, unnecessary verbiage when X doesn't have any wiggle room). The Shadow player has no choice in either of these draws. So read literally, that rule makes the Free Peoples player the one who "draws a card" here. Q-0251 was about a reveal and F-0513 about an assignment. In both, the act is done to cards someone else could have done it to. A draw only ever moves a card from the drawer's own deck to the drawer's own hand.

- **A. Only the player whose hand the card goes to has drawn (as built).** Drawing His Eye and 2_15 add no burden under Verily I Come. Nothing changes in the game. The same goes the other way round: Gorgoroth Stormer 8_101 ("exert this minion to add a burden and make the Free Peoples player draw a card") is the Free Peoples player's draw, and Morgul on the March 7_195 (`playerDrawsCard`, `player: fp`, a threat for each draw outside the regroup phase) answers it as it does now.
- **B. Q-0251 reaches draws too: a draw a card forces on the other player, with no choice in it, is the card's player's draw.** Drawing His Eye and 2_15 each add a burden under Verily I Come (2_15 adds two). Gorgoroth Stormer's draw becomes the Shadow player's, so Morgul on the March no longer answers it. That is an engine change and no definition alone can make it: the draw result would have to carry the performing player as well as the hand's owner, and `playerDrawsCard` would read the performer, as engine pass 45 did for assignment (`AssignFpCharacterToSkirmish`). The definitions of Verily I Come and 7_195 stay as they are.

No test is parked on this. `Card_02_094_Tests.TheShadowPlayerDrawingInTheFellowshipPhaseAddsNoBurden` takes reading A and is active, because the game passes it. Under B it would be turned round to read one burden. No older test takes either reading.

Recommendation: A. "Draws a card" names whose deck and hand the card moves between, not who chose to act. Under B, "make an opponent draw" would count as the card's player's own draw, and that player never touches a card of his own deck.

**Ketura (2026-10-16):** A


### F-0555 Deft in Their Movements 2_99: only the sites in play when it is played (CR 4.2 names it), or every site until the end of the turn?

Deft in Their Movements 2_99: "**Regroup:** Spot 2 Hobbits to make each site's Shadow number -2 until the end of the turn."

**Built now:** a standing `ModifyCost` over `site`, -2, until the end of the turn. Every site is -2 while it lasts, a site laid after the event was played among them: played in the regroup phase at site 4, the move to site 5 lays the Bridge of Khazad-dûm, which reads 4 in place of 6, and the move adds 4 + 2 companions = 6. The authors' notes say this is right (line 3 of Q-0220 J, which you approved as part of "A-J are all yes": "a snapshot would do nothing (F-0217)").

**What disagrees:** CR 4.2 "event" names this very card as its example of the snapshot: "Deft in Their Movements (Regroup: "Spot 2 Hobbits to make each site's Shadow number -2 until the end of the turn.") affects only sites that are in play when it is played." Your F-0217 ruling ("As a non-event, it's not beholden to the snapshot rule RAW") was on an ability, and a snapshot does not leave Deft doing nothing: where the other player's fellowship is ahead, the site the fellowship moves to next is already on the path and is helped.

**A (CR 4.2): a snapshot of the sites in play when it is played.** A site laid by the second move keeps its printed number: site 5 reads 6 and the move adds 8. The fix is a `MemorizeActive` of `site` ahead of the `AddModifier`, with the modifier over `memory(...)`; the reviewer tried it and all five tests pass with it, the parked one included. The note in the authors' file would then be corrected; We Must Go Warily 3_48 ("each minion's twilight cost +1") sits under the same note line and would be read again when it is worked.

**B (as built, and the note): every site until the end of the turn.** Nothing changes; the parked test is dropped, and the card's ledger keeps a line that CR 4.2's example is not followed.

**Parked on this:** `Card_02_099_Tests.ASiteLaidByTheSecondMoveKeepsItsPrintedShadowNumber` (reading A). The test of D, `EverySiteInPlayStaysTwoLessThroughTheSecondMoveAndIsBackOnTheNextTurn`, reads only sites that were in play and is active under either reading.

Recommendation: A. The rules document uses this card by name, and the note that approved B rests on a premise ("a snapshot would do nothing") that does not hold for it.

**Ketura (2026-10-16):** Yup, A.  I didn't realize this one was broken, although it's possible it's only since the HJSON conversion.


### Q-0335 Erestor, Chief Advisor to Elrond 3_14: is an archery wound, while a [sauron] archer is in play, 'a wound from a [sauron] minion'?

"<b>Response:</b> If an Elf is about to take a wound from a [sauron] minion or [sauron] event, exert Erestor to prevent that wound." (Erestor, Chief Advisor to Elrond 3_14)

Built now: the Response is `aboutToTakeWound` with `source: culture(sauron),or(minion,event)`. Archery wounds are placed by the game's own archery process (`FellowshipPlayerAssignsArcheryDamageGameProcess`), whose source is "Archery Fire" and no card, so no archery wound matches. Played out: with Orc Archer Troop 7_294 (the only [sauron] archer minion in print) in play, archery fire wounds Arwen twice, and Erestor is not offered at either wound. Wounds from a [sauron] event (Hate 1_250), a [sauron] minion's ability (Tower Assassin 2_93) and a [sauron] minion winning a skirmish (Orc Soldier 1_271) are all offered.

No rules document or ruling says what source an archery wound has. CR 4.2 "archery phase" counts the wounds from the minion archery total, and cards other than minions add to that total too.

- **A (as built).** An archery wound comes from no single minion, so it is not "a wound from a [sauron] minion" and Erestor is not offered. Nothing changes.
- **B.** A [sauron] archer adds to the total, so archery wounds are "from" it, and Erestor is offered against archery wounds while a [sauron] archer is in play (say too whether that holds when other cards add to the total). This needs an engine change, since archery wounds have no source card today, or a second trigger on the definition. `AnArcheryWoundIsNotPreventedThoughTheArcherIsASauronMinion` would be turned round.

The erratum 53_14 ("a wound from a [sauron] or [ringwraith] card") is built the same way and does not answer archery wounds either. One answer covers both cards.

Nothing is parked: `Card_03_014_Tests.AnArcheryWoundIsNotPreventedThoughTheArcherIsASauronMinion` reads A, is live, and passes.

Recommendation: A. Archery wounds are given by the fellowship's archery fire as a whole, not by any one minion.

**Ketura (2026-10-16):** A.  As you say, archery total wounds are unsourced.


### Q-0336 Friends of Old 3_16 (and Storm of Argument 3_6, 3_51): does Q-0148 reach a random reveal from an empty HAND, so that the event is not playable?

"<b>Fellowship:</b> Reveal a card at random from an opponent's hand. Heal X [elven] allies, where X is the twilight cost of the card revealed." (Friends of Old 3_16)

Built now: the event has no requirement. With the Shadow hand empty it is played: nothing is revealed (no pop-up, the log line of your Q-0144), X is 0 and nobody is healed. Two landed cards print the same frame with no cost before the reveal and are built and tested alike: Storm of Argument 3_6 ("Reveal a card at random from an opponent's hand. Shuffle up to X [dwarven] cards ...") and, not yet worked, Isengard's 3_51 ("Regroup: Reveal a card at random from the Free Peoples player's hand. Heal X [isengard] minions ..."). The cards that print "<spot or cost> to reveal" (Might of Númenor 3_43 and others) are Q-0163's and are not asked here.

Your Q-0148 (Glorfindel 9_16 and 59_16) was about a draw deck: "Either way, it has to happen for the rest to occur, so an empty deck should disallow the ability." These cards reveal from a hand, and the rest of the text reads the card revealed in the same way.

- **A (as built).** Q-0148 is for draw decks. A random reveal from an empty hand is a reveal of nothing: the event may be played, and X is 0. Nothing changes. shapes.md says Q-0148 does not reach a hand.
- **B.** Q-0148 reaches a hand too: with the opponent's hand empty the event is not playable. The correction on 3_6, 3_16 and 3_51 is a requirement that the opponent's hand holds a card. `Card_03_016_Tests.WithAnEmptyHandNothingIsRevealedAndNobodyIsHealed` and `Card_03_006_Tests.WithTheShadowHandEmptyTheEventIsPlayedAndNothingIsShuffledIn` would be turned round to read the event not playable, and `mechanics/deck/RevealingNoCardsFromAHandTests.CardsAtRandomFromAnEmptyHandAreShownToNobodyAndTheLogSaysThereWasNothingToReveal`, which plays Friends of Old on an empty hand, would need a card with a cost before the reveal.

Nothing is parked: the two card tests read A, are live, and pass.

Recommendation: A. The size of a hand is open to both players, and you ruled under Q-0144 how an empty hand's reveal should look, which takes it as a thing that happens.

**Ketura (2026-10-16):** I'm concerned about a NRE. Regardless, given that it has to reveal 'a' card, and the way it is configured I would read that as a cost. If there are no cards to reveal, the card should not be playable.  B.


### Q-0338 Dunlending Looter 4_11: is 'When this minion wins a skirmish, you may' offered again at a later win after a decline?

"When this minion wins a skirmish, you may stack him on a site you control." (Dunlending Looter 4_11)

Built now: an optional `winsSkirmish` trigger on the Looter himself, with no limit. It is offered at every skirmish he wins while his player controls a site. A Looter made fierce (Anger 4_3) whose player declines at his first win, and who wins again in the fierce round, is offered the stack a second time.

What the rules say. CR 4.2 "when": the word is used "if an effect can happen only once"; "each time" is used if it can happen more than once. Stacking him takes him out of play, so once taken the effect cannot happen again. But declining leaves him in play, and a fierce minion can win twice in one turn. Hillman Rabble 4_24 prints the same effect with "Each time this minion wins a skirmish, you may stack it on a site you control". Only two printed cards say "When ... wins a skirmish".

- **A (as built).** "When" only describes how the words were chosen: the stack can happen once, so "when" was printed. The trigger answers every win, and an offer declined is made again at a later win. Nothing changes. `Card_04_011_Tests.DeclinedAtHisFirstWinHeIsOfferedAgainAtAWinInTheFierceRound` reads the second offer, is live and passes.
- **B.** "When" limits the trigger to one offer: once declined, no later win offers it (that turn, or ever while he stays in play). The definition would want a limit on the trigger, and that test would be turned round to read no offer in the fierce round.

Nothing is parked: the test reads A, is live, and passes.

Recommendation: A. The glossary entry explains why a card says "when" or "each time"; it does not read as a rule that a declined "when" trigger is spent, and the game plays the Looter and the Rabble alike.

**Ketura (2026-10-16):** "When" and "Each time" are for our purposes identical in execution.  They are meant to aid understanding, but they aren't actually binding.  In this case, the "When" just indicates that upon activating, the Looter is removed from play, meaning that no further executions could possibly occur to warrant an "Each time".  So this is A.


### Q-0339 Dunlending Pillager 4_13: are the '2 possessions' of the parenthesis 2 Free Peoples possessions, or any 2 possessions as under F-0491?

"Each time this minion wins a skirmish, you may discard a Free Peoples possession (or 2 possessions if you spot 6 companions)." (Dunlending Pillager 4_13)

Built now: one choice from one filter, `side(free),possession`, of exactly 2 when 6 companions can be spotted and exactly 1 otherwise (Q-0035: a parenthesis branch is mandatory when it is available). With 6 companions the Shadow player discards 2 Free Peoples possessions, and his own possessions (Hides 4_19) are never offered.

The doubt: the parenthesis says "2 possessions", with no side word. Your F-0491 on Secret Sentinels 2_20 ("discard a condition (or 2 conditions if you spot an Orc)") made such a parenthesis "1 mandatory and then a 0-1", because "condition" with no side could make a player discard his own card. There the first noun had no side either; here it has one ("a Free Peoples possession"). Q-0028 ("one" stands for the noun before it) is the nearest ruling on carrying a noun over, and does not reach this.

- **A (as built).** The parenthesis carries the sentence's noun: "2 Free Peoples possessions". With 6 companions exactly 2 Free Peoples possessions are discarded, nothing of the Shadow player's is offered, and nothing is asked about the spot. Nothing changes. `Card_04_013_Tests.WithSixCompanionsItDiscardsTwoFreePeoplesPossessions` reads a choice of exactly 2 from 3 Free Peoples possessions, Hides not offered; it is live and passes.
- **B.** Word for word, as F-0491: with 6 companions, 1 Free Peoples possession is discarded and then 0 to 1 more possession of either side, so the Shadow player's own Hides 4_19 is offered for the second, and the choice runs 1 to 2. The definition would become a discard of one `side(free),possession` followed by an optional discard of 0 to 1 `possession` under the 6-companion condition, and that test would read the second choice with Hides among it.

Nothing is parked: the test reads A, is live, and passes.

Recommendation: A. The side word on the first noun is the difference F-0491 turned on, and a Shadow player discarding his own possession off a Free Peoples-discard minion reads as no one's intent.

**Ketura (2026-10-16):** Hrrm.  I suppose as written it could discard Shadow possessions.  However since it is under a "may" clause, I would interpret this as a branch of 0-1 Free Peoples possessions, or 0-2 possessions of any side if the requirement is met. Since it is not an event the play execution is a little different from Secret Sentinels.


### Q-0337 They Will Never Stop Hunting You 3_84: at X 0 the game still asks which companion exerts no times

"<b>Maneuver:</b> Spot a Nazgûl to reveal a card at random from the Free Peoples player's hand. Exert a companion bearing a ranged weapon X times, where X is the twilight cost of the card revealed." (They Will Never Stop Hunting You 3_84)

Built now: the exert effect (`exert`, `select: choose(companion,hasAttached(ranged weapon))`, `times:` the cost of the card revealed) asks for its companion whatever X comes to. When the card revealed costs 0 (Wreathed in Shadow 1_238), or the hand was empty and nothing was revealed, and two or more companions bear a ranged weapon, the Shadow player is still asked "Choose cards to exert", and the one he picks exerts no times. With one such companion he is chosen unasked; with none nothing is asked. The outcome is the same either way: nobody exerts. Heal X of Might of Númenor 3_43 ("Heal X companions") asks nothing at X 0, because there X is the number of cards chosen, not the number of times.

Your F-0116 ("the prompt should be smarter") and F-0379 (a "you may" inside an event is not asked when yes would do nothing) were about prompts that change nothing; neither was about a choice of WHO, inside an event, whose outcome is nothing.

- **A (as built).** The text names a companion to exert whatever X is, so the choice is made and he exerts 0 times. Nothing changes. `Card_03_084_Tests.ACardOfCostZeroExertsNobody` reads the choice asked, then no wound.
- **B.** A choice whose outcome is nothing is not asked: with X at 0 the event reveals the card and stops. The proper place is the engine (`Exert.java`: treat `times` of 0 or less as certain to do nothing and choose no card), which reaches every "exert ... X times" in the definitions at once, 48 `exert` effects with a counted `times` (a memory or a count; the 624 with a fixed number are not touched). A definition-only mend for this card alone is an `If` with an `IsGreaterThan` on the revealed card's cost around the exert. The test would read no "Choose cards to exert" and no wound.

Nothing is parked: the test reads A, is live, and passes.

Recommendation: B, done in the engine, if you want the prompts trimmed as F-0116 trimmed them; otherwise A costs the player one meaningless click and no game outcome.

**Orchestrator (2026-10-16), settled by your F-0116 words** ("Yeah the prompt should be smarter": a prompt that changes nothing is a finding) and F-0379: B, in the engine. Nothing plays differently: a companion exerted 0 times takes no wound and sets off no "each time ... exerts". The count above was "about 160" as filed; a search finds 48. Queued for the next engine pass, effects only (an exert of X times paid as a COST keeps today's payability, so no card becomes playable or unplayable by it). Strike it if you want the choice kept.

### Q-0330 Keep or strike: Unferth 4_178 makes a defender assigned to two [isengard] Men exert twice, as Weary now does

Your B on Q-0323 (Weary 4_212) was built in the engine, and it reaches one other card: Unferth, Gríma's Bodyguard 4_178, "Each time the Free Peoples player assigns a companion or ally to an [isengard] Man, that companion or ally must exert."

**What changed.** The engine used to report the Free Peoples player's assignment of a defender to two minions as ONE assignment of that character, carrying both minions. It now reports one assignment for each minion, as CR 4.2 "defender +X" words it ("you may assign that character at this time to one additional unassigned minion"). Card-made assignments were already reported one pair at a time. So Weary's bearer with defender +1, assigned to two [isengard] trackers in one answer, exerts twice, as you ruled.

**What plays differently beyond Weary.** Unferth reads the same kind of assignment. A companion or ally with defender +1 (Aragorn, Heir of Elendil 4_109, for one) that the Free Peoples player assigns to Unferth and another [isengard] Man (Gríma, Wormtongue 4_154) in one answer now exerts TWICE. Before this pass it exerted once. A companion assigned to one [isengard] Man still exerts once. Q-0323 said no other card had these words. Unferth's wording differs slightly ("to an [isengard] Man", not "to skirmish an [isengard] tracker"), but it counts the minion the same way.

**Not changed.** Wold of Rohan 4_336 ("assigns a companion or ally to a skirmish") is a different trigger and still counts a defender's two minions once, as F-0513 has it. Every other card that answers an assignment names a minion that has only one pairing (its own: "this minion is assigned to ..."; Southron Fanatic 10_49's "a Man is assigned to skirmish this minion"), so it answers exactly as before. That includes ambush.

**Keep:** Unferth exerts once for each [isengard] Man the defender is assigned to. When Unferth's batch comes, its test will read 2.
**Strike:** Unferth goes back to exerting once per answer. That would need a new definition word on Unferth's trigger to count one answer once. Weary and the engine change stay either way.

**Ketura (2026-10-16):** Keep, that's fine.


### Q-0331 Great Works Begun There 2_9: may the [dwarven] event taken be one the cost just discarded off the top of the draw deck (Q-0192, Q-0320)?

"Each time you play a Dwarf, you may discard the top 2 cards from your draw deck to take a [dwarven] event into hand from your discard pile." (Great Works Begun There 2_9)

Built now: the take is `choose(culture(dwarven),event)` over the whole discard pile once the cost is paid, so a [dwarven] event that the cost has just discarded off the top of the draw deck may be the one taken. Played out: with Cleaving Blow 1_5 on top of the deck and Axe Strike 1_3 and Delving 1_6 already in the pile, Gimli is played, the trigger accepted, and Cleaving Blow is offered beside the other two.

Your Q-0192 and Q-0320 (the They Are Coming precedent, CR 4.2 "effect") speak of a card discarded from HAND for the cost. This cost discards off the top of the draw deck: cards the player does not choose and may not know.

- **A.** The precedent reaches a deck-top cost too: the cards just discarded are left out of the take. The correction: `memorize: milledCards` on the `discardTopCardsFromDeck` cost and `select: choose(culture(dwarven),event,not(memory(milledCards)))` on the take. With it all 7 tests pass, the parked one among them. shapes.md's Q-0192 entry says it reaches a mill cost.
- **B.** A deck-top cost is different: the player chose nothing, and the cards are in the discard pile when the effect is carried out. Nothing changes; the parked test is turned round to read Cleaving Blow offered, and shapes.md says Q-0192 does not reach a mill cost.

Parked on it: `Card_02_009_Tests.ADwarvenEventTheCostDiscardsIsNotTheOneTaken` (reads A; fails today at `FreepsHasCardChoiceNotAvailable(cleaving)`).

Beneath the Mountains 2_1 and 52_1 play a [dwarven] weapon from the pile after the same kind of cost, and their landed tests read B; they are asked separately under their own number (filed with this one), because CR 4.2 "effect" speaks to a card PLAYED from the pile in so many words, and this card only takes.

Recommendation: A. "Must be in your discard pile before you can begin to perform that action" is the principle you have applied to takes as well as plays, and nothing in it turns on who chose the cost's cards.

**Ketura (2026-10-16):** A


### F-0548 Beneath the Mountains 2_1 and 52_1: may the [dwarven] weapon played be one the cost just discarded off the deck (CR 4.2 'effect')?

"Fellowship: Discard the top 3 cards from your draw deck to play a [dwarven] weapon from your discard pile." (Beneath the Mountains 2_1; its erratum 52_1 prints the same sentence)

Built now: `discardTopCardsFromDeck` 3 as the cost, then `playCardFromDiscard` with `choose(culture(dwarven),weapon)`, so a weapon the cost has just put in the pile is offered. The landed tests of both read it so: `Card_02_001_Tests` and `Card_02_001_ErrataTests`, `WeaponAbilityOffersOnlyDwarvenWeaponsInYourDiscardPileIncludingOneTheCostDiscarded` (Gimli's Battle Axe second from the top, discarded by the cost, then offered). Their ledgers took it from CR 4.2 "playing a card" (costs paid before the effect chooses). The game is not of one mind: with no [dwarven] weapon in the pile before the action, the action is not offered at all (`WeaponAbilityIsNotOfferedWithoutADwarvenWeaponInDiscardPile`, Q-0062), even if one lies in the top 3.

CR 4.2 "effect": "If an action plays a card from your hand (or discard pile) as part of its effect, then that card must be in your hand (or discard pile) before you can begin to perform that action." Its example is They Are Coming, a hand discard; the sentence itself says nothing of where the cost's cards come from.

- **A.** CR 4.2 reaches this card by its own words: the weapon played must be in the pile before the action begins. The correction on 2_1 and 52_1: `memorize: milledCards` on the cost and `select: choose(culture(dwarven),weapon,not(memory(milledCards)))`. The two landed tests are turned round (the Battle Axe just discarded is not offered; the weapons already there are).
- **B.** As built: a deck-top cost is not what the rule had in mind, and the weapon milled may be played. Nothing changes.

Nothing is parked: both cards are landed, and the tests stand reading B until you answer.

Beside Q-0331 (Great Works Begun There 2_9, the same cost before a TAKE): A here does not settle Q-0331, which turns on Q-0192's reach and not on the CR's words; A on Q-0331 would settle this.

Recommendation: A. The rule's sentence is plain and the card plays from the pile.

**Ketura (2026-10-16):** A


### Q-0329 Voluntary loops (Q-0321, CR 4.2 "loops"): build the loop step as proposed, imposed or offered?

CR 4.2 "loops", on Sméagol, Slinker 5_29 ("Skirmish: Add a burden to make Sméagol strength +2 and take no wounds") against Anduin Banks 7_341 ("Skirmish: Spot your minion and remove a burden to make that minion strength +2"): the Free Peoples player names a number of iterations. The Shadow player then either names a lower number, and the Free Peoples player takes the last action, or agrees, and takes the last action himself. Gemp has no such step today. Each player can only pass, and whoever passes first gives the other the last action, so two stubborn players can go on for ever (your Q-0321 answer). Nothing is built yet. This asks whether to build it, and in which of two forms.

**What would be built** (engine pass 49's loop-proposal report has the detail):

- **Where.** The two places where the game has the players take phase actions in turn: the maneuver, archery, assignment and regroup phases, and each skirmish. The Fellowship and Shadow phases are left out, because there one player acts alone and can always stop. Response windows are left out too.
- **What counts as a loop.** Your first idea, the running tally, sets it off: the same two players use the same ability of the same card, in the same order, with no pass in between. The game then checks that the board has come back exactly as it was each time round: the cards and where they are, wounds, burdens, threats, culture tokens, twilight, hindered cards and the order of each draw deck. Strength bonuses and other effects that last until the end of the phase are left out of that check, since they are what the loop piles up. The check is what tells a loop from a pump war that spends something (exertions, cards, twilight). A pump war cannot go on for ever, so it is not a loop and is never stopped. Your second idea (the cost of one action is the effect of the other) is dropped. In the CR's own example both halves are costs (add a burden; remove a burden), so it would miss it. It would also need every kind of cost and effect taught what undoes it.
- **When.** The step begins when the player who opened the loop is about to be offered it a third time. By then each player has used his action twice.
- **The two prompts.** Both use prompts the client already has: a number, and a two-way choice. Nothing changes in the client. To the Free Peoples player: "Sméagol, Slinker and Anduin Banks can be used against each other for ever (a loop). How many more times should the loop run? Your opponent may agree and take the last action, or name a lower number, and then you take the last action." He answers 1 to 99. To the Shadow player: "Your opponent says the loop of Sméagol, Slinker and Anduin Banks runs 6 more times." The two choices are "Agree: 6 times, and I take the last action" and "Name a lower number: the Free Peoples player takes the last action". Choosing the second asks for a number from 0 to 5.
- **How it is carried out.** The game then plays the loop that many times itself. It answers each choice inside the loop as it was answered the last time round, and gives the final action to the player the answers named, adding one action at the end where needed (as the CR says). The game log gets one line for the whole loop. If anything in the loop can no longer be done the same way, the run stops and play goes on as it does now. When the loop has run, its two actions are not offered again while the board is as the loop left it, so that it cannot simply be started over. Anything else that changes the board lifts that bar.
- **Size and what changes for players.** About four new engine classes and four changed ones, each with tests. Nothing changes in a game where nobody loops: the board is only checked once an action is repeated. Card_05_029_Tests.AtAnduinBanksTheBurdenLoopRunsForAsLongAsBothPlayersKeepIt would be laid again to read the two answers.

**The choice:**

- **A. Build it, imposed** (recommended). The game stops the third time round and puts the number to the Free Peoples player. This is the CR as written: the procedure is not optional, and it is what the platform's players have learned to expect of Gemp.
- **B. Build it, offered.** Nothing stops. From the third time round, each player is offered "Call the loop" on the loop's own card, beside his action and Pass, and the two prompts follow when either of them takes it. Players who are happy to keep clicking are left alone, and either player can end a stubborn opponent's loop.
- **C. Not now.** The alternation stands, and 5_29's test stays as it reads.

If any part should be different under A or B, say so in the answer: the third time round, the cap of 99, the replaying of choices, or the bar after the loop (the bar is not in the CR's words).

Separately, and on your page as F-0536: the involuntary loop you recalled (Frodo, Weary From the Journey 15_144 against Black Land Observer 15_101) is NOT handled as the CR says, and no test of it exists, old or new. The game gives both cards their bonus where the CR gives neither.

**Ketura (2026-10-15):** I approve of the overall broad strokes here.  We're going to want some extensive tests to ensure that merely large yet finite repeats do not trigger the loop detection. For the Free Peoples message, let's do: "LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops) [[Smeagol, Slinker]] and [[Anduin Banks]] can be used forever. Choose how many more times this loop will execute." After choosing, the Shadow should get: "LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops) The Free Peoples player has decided that the loop between [[Smeagol, Slinker]] and [[Anduin Banks]] will be performed **6** more times." With choices: "Agree: the loop goes **6** more times, and Free Peoples takes the next action." "Choose a lower number: Shadow will take the next action." with the number prompt being: "Choose a number lower than **6**.  The loop between [[Smeagol, Slinker]] and [[Anduin Banks]] will be performed that many times, ending so that you will take the next action." After the loop has resolved, I think both cards involved need to have their actions barred from use for the rest of the phase.  This should involve sending a "loop-banned" flag on the action, similar to the "noeffect" tag, which will draw a dark grey border and disallow selection (I'll have another instance handle the front end for this).  I would not re-enable the actions once the game state changes, as it would just lead to doing do-nothing actions and then attempting to restart the loop again; nobody gets into a loop situation because they had other actions they could have meaningfully done instead. We need to be careful that X events are not sent or recorded in the replay file; we don't want trolls to pick 99 and then the clients have to sit there and wait for 99 flashing iterations of each card being activated.  I agree that the server should still perform that many iterations, but it should disable transmission/recording of the game events while doing so, only sending the final 1-2 actions with updated stats. We'll also need to have tests checking the way both decisions fall out, so that it alters whether 1 additional action is added on the end or not depending on the decisions made; whether it ends on the detected cycle or one action out of sync.


### Q-0316 Keep or strike: a forced single-candidate assignment is the Shadow player's for every reader, ambush included (engine pass 45, F-0513)

Engine pass 45 (F-0513, Wold of Rohan 4_336) built what you approved in Q-0251/Q-0171 ("where a card makes an opponent do a thing and he has a choice in it, he is the one doing it; where he has none, the card's player is") for assignments: an assignment a Shadow card forces on the Free Peoples player, with a single candidate left, is now the Shadow player's. The engine credits all three things an assignment emits, so it reaches beyond Wold:

- `assignedToSkirmish` readers with `side: free people`: Wold of Rohan 4_336, Ranks Without Number 4_170, Rohan Is Mine 4_172, Unferth 4_178, Weary 4_212, Invading Haradrim 11_86, Rosie Cotton 12_129.
- `assignedAgainst` readers with `side: free people`: Desert Runner/Scout/Sneak 7_133-7_135, Southron Chieftain 7_163, Morgul Tormentor 12_90, Orc Tormentor 12_98, Candle Corpses 16_2, Covetous Wisp 16_3, Dead Faces 16_4, A Defiled Charge 17_67.
- AMBUSH: ambush is offered only on the Free Peoples player's assignment, so a lone ambush minion forced onto the Ring-bearer by 1_169 now brings no ambush twilight.

The pairs that can actually meet: 12_135, 12_171 or V1_38 with Wold, Rohan Is Mine or Ranks Without Number; 1_169 with any minion of the second list, or with an ambush minion; 1_262 with Wold.

**Keep** (as built): the rule holds for every reader, ambush included: a forced assignment with no choice is not the Free Peoples player's.
**Strike ambush**: ambush still fires on a forced single-candidate assignment (CR 4.2 ambush: "when the Free Peoples player assigns this minion"); the other readers keep the rule. One condition in AmbushRule.

Nothing is parked; the suite passes either way today (no test reads the ambush board).
Recommendation: Keep, as the rule's wording; but ambush is a keyword players know, so you may want it to stay put.

**Ketura (2026-10-15):** keep


### Q-0317 Deeping Wall 4_348: keep the unprinted guard that withholds 'Play Saruman from your draw deck' while a Saruman is in play?

"Shadow: Play Saruman from your draw deck."

Built now: the definition has a requirement the text does not print, `requires: cantSpot name(Saruman)`. While any Saruman is in play (a minion, or the follower Saruman, Servant of Sauron 17_38), the ability is not offered at all. With no Saruman in play it is offered whatever the deck holds, as every "play ... from your draw deck" with no cost is (no `mustExist`; Q-0104): the deck is shown, a Saruman may be chosen, and the deck is shuffled.

By the text the ability could still be used while a Saruman is in play. No second Saruman could be played (CR 4.2 "unique"), but the deck would be looked through and shuffled, the same as a use with no Saruman in the deck, which the game allows.

- **A (as built, and likelier).** Keep the guard. A play that can never succeed is not offered, in the spirit of Q-0019 ("threads the needle of UX and rules") and Q-0034. Nothing changes. The active test `Card_04_348_Tests.WhileASarumanIsInPlayTheActionIsNotOffered` stays as it is.
- **B.** Drop the guard. The look and shuffle stay usable while a Saruman is in play, as with an empty or Saruman-less deck (Q-0104). Correction: remove the `requires` block from 4_348. The test is rewritten to read the action offered, the deck shown with nothing selectable, and one shuffle.

The same unprinted guard is on three other cards that play a unique title from the draw deck with no cost, and one answer covers them all: the site 4_332 (Treebeard), the site 4_341 (Théoden) and Sam 4_315 (Sam's Pack). Orthanc Balcony 4_361 has it too, but there no board can tell it from the rule, because a play from the discard pile is a cost (Q-0062). Captured by the Ring 7_53 has it beside `mustExist` (a Q-0019 card), so it is not the same case.

Nothing is parked. The as-built reading is the active test named above.

Recommendation: A. It matches how you ruled Q-0019, and the look it withholds can play nothing.

**Ketura (2026-10-15):** A


### Q-0318 Keep or strike: an ally with two home sites (Aldor 4_262) is at each one laid, wherever the fellowship is

Engine pass 48, item ally-site-terrain (F-0518). **Built now.** A text that names a character's place ("while bearer is at a plains site", "heal that Man if at a plains") reads an ally at his home site (CR 4.2 "ally"), as you approved. One case your words and the CR do not settle: **an ally with two home sites**. The only one any of these cards reaches is Aldor, Soldier of Edoras 4_262 (homes 3T and 6T), a [rohan] Man who can be mounted.

As built: an ally is at **each** of his home sites that has been laid on the path, wherever the fellowship is. CR 4.2: "Each ally in your support area is considered to be at his home site. When your fellowship is at your ally's home site, that ally is considered to be at the same site as the fellowship." Nothing in it says the second sentence takes him away from his other home.

Where it changes play: a Towers path whose site 3 is Westemnet Plains 4_342 (plains, sanctuary), with the fellowship at site 6 (Caves of Aglarond, Hornburg Hall, Great Hall, Hornburg Armory: none a plains). As built, Aldor is still at Westemnet Plains, so on him Rohirrim Mount 11_156 makes him defender +1, Swift Steed 7_253 lets him take no more than 1 wound in each skirmish, and Thundering Host 5_94 heals him. At sites 4 and 5 every reading agrees (he is at site 3). The test is mechanics/sites/WhereACharacterIsTests.AnAllyOfTwoHomesIsAtEachThatHasBeenLaid.

- **Keep.** Nothing to do.
- **Strike** (with the fellowship at one of his homes he is at that site alone; otherwise at each laid home). One branch in Filters.atSite: when the fellowship's current site is one of his homes, read that site only. Aldor at site 6 then gets none of the three. That test is turned round; no other test changes.

The change stays unless you strike it. Recommendation: keep (the rule's own words, and the simpler reading).

**Ketura (2026-10-15):** keep


### Q-0319 Keep or strike: Release the Angry Flood 2_19 and Hospitality of the Dwarves 101_1, whose 'if at a <terrain>' names no one, still read the fellowship's site

Engine pass 48, item ally-site-terrain (F-0518). Your item had every "while at a <terrain>" / "if at a <terrain>" that names a character an ally may be read where the character is (an ally at his home site), and cards that plainly name the fellowship's place left alone. **Two cards name no one** and were left as built, reading the **fellowship's** site:

- Release the Angry Flood 2_19: "Maneuver: Exert an Elf to wound each Nazgûl once (or twice if at a river)."
- Hospitality of the Dwarves 101_1: "Maneuver: Spot an unbound Dwarf bearing 2 or more items and hinder this condition to choose one: ... If at an underground site, do both."

Read as the character's place instead, they play differently with an ally: an Elf ally exerted for 2_19 with his home at a river (Ford of Bruinen 1_338 as site 3, Valley of the Silverlode 2_120 as site 6) would wound each Nazgûl twice wherever the fellowship is, and one whose home is not would wound once with the fellowship at a river; for 101_1 a Dwarf ally (every one is home 3F, none underground) would get one choice, not both, with the fellowship in Moria. As built they read the fellowship's site, and no test of theirs lays an ally.

- **Keep.** Nothing to do.
- **Strike** (read them as the character's place). 2_19's check becomes `canSpot memory(<the exerted Elf>),atSite(river)` (a `memorize` on the exert) and 101_1's `memoryMatches <the Dwarf> atSite(underground)`, with an ally test each. Nothing else changes.

Recommendation: keep. 2_19's parenthesis follows "each Nazgûl", who are with the fellowship, and 101_1's sentence has no subject; Thundering Host's "heal that Man if at a plains" names its Man, these do not.

**Ketura (2026-10-15):** Oooh, interesting.  That's going to need a ruling.  We'll keep this for now, but that's another thing to bring to the Rules Council.


### Q-0320 Final Account 11_31 and 61_31: may a card discarded from hand for the cost be the card taken back (Q-0192)?

"Discard a card from hand to take a Shadow card from your discard pile into hand." (the PC erratum 61_31; the original 11_31 reads "Discard 2 cards from hand to take a Free Peoples card and a Shadow card from your discard pile into hand.")

Built now: neither definition leaves the cards discarded for the cost out of the take. The approved test of 61_31, `Card_11_031_ErrataTests.AShadowCardDiscardedToPayForItMayBeTheCardTaken` (landed 2026-10-09, a few hours before you answered Q-0192), discards a Shadow minion for the cost and reads it offered and taken back. 11_31 has no tests yet.

Your Q-0192 (Race Text 94_12): "discarding a card from hand to retrieve a card is not allowed to pull the card just discarded." shapes.md applies it to every card of the shape, and What Did You Discover? 4_213, a condition's trigger, was corrected to it at this landing (F-0521). Final Account is the one case where an old test reads the other way.

- **A.** Q-0192 reaches Final Account. The same correction on 11_31 and 61_31: `memorize: discardedCards` on the `discardFromHand` cost, and `not(memory(discardedCards))` in each take's `select`. 61_31's test is turned round (the minion just discarded is not offered; the one already in the pile is taken), and 11_31's batch tests to it.
- **B.** An event is different: its cost is paid at step 3 and its choices made at step 7 (CR 4.2 "playing a card"), so the card discarded is in the pile like any other. Nothing changes; 61_31's test stays, and shapes.md's Q-0192 entry says it does not reach events.

Nothing is parked. New Power Rising 4_162 ("discard all Free Peoples cards revealed to take an [isengard] card into hand") is not reached: a card it discards can never be the [isengard] card it takes.

Recommendation: A. Your ruling's words cover it, and the 61_31 test came before the ruling.

**Ketura (2026-10-15):** The precedence of the They Are Coming ruling applies here: discarding cards from hand as a cost to play from the discard pile cannot then retrieve those discarded cards.  A.


### Q-0321 Sméagol, Slinker 5_29 with Anduin Banks 7_341: is CR 4.2's loop-naming step (iterations named, Shadow lowers or agrees) wanted, or is the plain alternation of actions enough?

"Skirmish: Add a burden to make Sméagol strength +2 and take no wounds." (Sméagol, Slinker 5_29), against Anduin Banks 7_341: "Skirmish: Spot your minion and remove a burden to make that minion strength +2." CR 4.2 "loops" names the pair: "the Free Peoples player names a number of iterations for these actions. The Shadow player then has two choices": a lower number, and the Free Peoples player takes the final action; or agree, and the Shadow player takes it.

Built now: there is no naming step. The skirmish phase runs as any other: each player in turn takes an action or passes, and the skirmish resolves once both pass in a row. So either player may stop the loop at any point by passing, and the other then answers or passes. Played out (Sméagol 3 against Uruk Pursuer 4_190 at 5, no burdens): his use (1 burden, he is 5), the site's (0, the Pursuer 7), his (1, 7), the site's (0, 9); he is offered it again, passes, and loses 7 against 9 with no wound.

- **A.** The alternation is how the online game does it, and nothing more is owed. Each player can end the loop whenever he likes by passing; what the game does not do is let the Shadow player decide who has the last action, and two players who both refuse to pass can run it as long as they please. The test `Card_05_029_Tests.AtAnduinBanksTheBurdenLoopRunsForAsLongAsBothPlayersKeepIt` reads it so, and stands.
- **B.** The rules' step is wanted: when such a loop starts, the Free Peoples player is asked for a number of iterations and the Shadow player to lower it or agree, and the loop is carried out to that number with the final action his answer gives. An engine feature (loop detection, two prompts); the R1 check would then be relaid to read the two answers, and the present test would be parked on it until built.

Nothing is parked: the active test reads A.

Recommendation: A. A general loop step reaches every two-player loop in the card pool for a case that the pass already ends.

**Ketura (2026-10-15):** So.  As of today there is no handling of infinite voluntary loops on Gemp; and in fact this comes up with some regularity, where two stubborn players keep playing the same actions, unwilling to voluntarily follow the rule in that instance (given that the platform has trained people to defer to Gemp's enforcement).


### Q-0322 The Shire Countryside 3_113: two burdens removed by one effect, one offer or two

"Each time you remove a burden (except by a Hobbit's game text), you may heal a companion." (The Shire Countryside 3_113)

**Built now:** one optional `removesBurden` trigger. The engine answers each burden of a removal as a removal of its own (RemoveBurdenEffect emits one result per burden), so where ONE effect removes two burdens at once (Jarnsmid, Barding Emissary 12_30 with another [gandalf] companion: "you may remove a burden (or 2 burdens ...)"; The More, The Merrier 11_169) the Countryside is offered twice, and may heal twice.

**A (as built): two burdens removed are two removals.** "Each time you remove a burden" counts burdens: two offers, two heals. Like "Exert X twice", which sets an "each time ... exerts" trigger off twice. Nothing changes; a test is written showing two offers (Jarnsmid 12_30 beside Gandalf 1_72, the Countryside in the support area, a companion with two wounds, both offers read and taken).

**B: one effect is one removal.** A single effect that removes two burdens sets the trigger off once: one offer, one heal. Like the Cirith Ungol Watcher ruling (F-0145), where two companions exerting at once draw one card. That would be an engine change (one result for a removal of several, or a per-effect count on the trigger), and it would reach every "each time you remove a burden" card, not only this one; a test is written showing one offer on the same board.

**Parked:** nothing. No definition word reads the count, so no test is written until you rule; the ledger's Notes name the board.

**Recommendation:** A. The card counts "a burden", and the engine's reading matches how "exert twice" was settled.

**Ketura (2026-10-15):** A


### F-0527 Vengeance 4_208: may an [isengard] tracker in no skirmish be chosen, as printed, or only a skirmishing one (as built, Q-0095's shape on a Decipher card)?

Vengeance 4_208: "Skirmish: Make an [isengard] tracker strength +2 for each card borne by the character it is skirmishing."

**Built now.** The choice is `choose(culture(isengard),tracker,InSkirmish)`: only an [isengard] tracker in the skirmish being resolved is offered. An [isengard] tracker in no skirmish (Uruk Pursuer 4_190) is not offered, and with no [isengard] tracker in the skirmish the event cannot be played at all. Under the printed words any [isengard] tracker in play could be chosen, and one in no skirmish would gain +0. This is the shape of Q-0095 ("a minion" on a Skirmish event that reaches only the skirmish: you changed the TEXT to "a skirmishing minion"), but that was settled on PC cards whose text you update yourself. This is a Decipher card: the print cannot change, and the game shows the Decipher scan. Of the 37 Decipher skirmish events that read "Make a/an <kind> strength +N", Vengeance is the only one built with `InSkirmish`. Its neighbour Kill Them Now 4_156 ("Make an [isengard] tracker strength +2 (or +4 if skirmishing ...)") is `choose(culture(isengard),tracker)`, as printed.

- **A. Keep the behaviour (Q-0095 carried to Decipher cards).** Only a skirmishing [isengard] tracker may be chosen. The definition's `gametext` becomes "Make a skirmishing [isengard] tracker strength +2 ...", so the definition no longer matches the printed card or the sheet. No test changes: `OnlyAnIsengardTrackerInTheSkirmishMayBeChosen` already reads Uruk Pursuer 4_190 not offered.
- **B. As printed (Q-0012, "cards are code").** `InSkirmish` is taken out of the choice, and the text stays as it is. Any [isengard] tracker may be chosen, and one in no skirmish gains nothing. The event can then be played with no [isengard] tracker skirmishing, if one is in play elsewhere (Q-0034 keeps events on offer whatever they do). That matters only to cards that answer the play of a skirmish event. The same test turns round: Uruk Pursuer is offered, and once chosen he gains nothing and the skirmishing trackers are unchanged.

Parked: nothing. The active test reads A, which is what the game does. It turns round under B.

Recommendation: B. The definition's InSkirmish is a quality-of-life shortcut that departs from a printed text nobody can update, and every other card of the shape on a Decipher card is built as printed. A also leaves the definition's text and the card's scan saying different things.

**Ketura (2026-10-15):** I think sadly we need to go B.


### Q-0323 Weary 4_212: a defender bearer assigned to two [isengard] trackers in one answer, one exertion or two?

Weary 4_212: "Each time the Free Peoples player assigns bearer to skirmish an [isengard] tracker, bearer must exert."

**What is built.** One `assignedAgainst` trigger on bearer, against an [isengard] tracker. The engine reports an assignment as one character with the set of minions he is assigned to, so a bearer with defender +1 that the Free Peoples player assigns to two [isengard] trackers in one answer exerts ONCE. Aragorn, Heir of Elendil 4_109 (defender +1) bearing Weary, assigned to Uruk Pursuer 4_190 and Uruk Foot Soldier 4_187 together, takes 1 wound.

No ruling settles it. Wold of Rohan 4_336 (F-0513) counts a defender on two minions once, but its words are "assigns a companion or ally to a skirmish", and two minions make one skirmish. Weary's words count the minion: "to skirmish an [isengard] tracker". CR 4.2 "defender +X" describes each extra minion as a further assignment ("you may assign that character at this time to one additional unassigned minion"). That leans towards B.

- **A: once (as built).** He is assigned once, to one skirmish, which holds both trackers. Nothing changes.
- **B: once for each tracker.** Each pairing of bearer with an [isengard] tracker is an assignment, so two trackers make him exert twice, and an Aragorn at 2 vitality left would be killed by the second exertion. The definition would need its trigger to fire once for each [isengard] tracker assigned against bearer (an engine or definition change to `assignedAgainst`, or a per-minion form of it). The test changes from 1 wound to 2.

Test: Card_04_212_Tests.ADefenderBearerAssignedToTwoIsengardTrackersInOneAnswerExertsOnce reads A and is active, because the game passes it. On B it is turned round and parked until the definition can count per minion.

No other card has these words with a character who can take two minions. The minion-side wording "assigns this minion to skirmish an unwounded companion" cannot pair one minion with two companions.

Recommendation: B follows the wording and CR 4.2's account of defender more closely. A is what the game does now, and costs nothing.

**Ketura (2026-10-15):** I think B.


### Q-0324 Tom Bombadil's Hat 0_60: two threats added at once by one Shadow card, one token or two

"Each time a Shadow card adds a burden or a threat, add a [shire] token here." (Tom Bombadil's Hat 0_60)

**Built now:** one required trigger on a burden or threat added by a Shadow card. The engine reports each threat (and each burden) of an addition as an addition of its own (AddThreatsEffect and AddBurdenEffect emit one result per unit), so where ONE Shadow card adds two at once (Morgul Destroyer 7_190: "you may spot a Nazgûl to add 2 threats"; Eye of Barad-dûr 5_96 adds 2 burdens) the Hat gets two tokens.

This is the adding side of Q-0322 (The Shire Countryside 3_113, two burdens removed by one effect). That question's answer would not settle this one by its own words (a removal by you against an addition by a Shadow card), so it has a number of its own; one answer can cover both.

**A: one effect is one addition.** Two threats added together by one card set the trigger off once: one token. Like the Cirith Ungol Watcher ruling (F-0145, two companions exerting at once draw one card). An engine change (one result for an addition of several, or a per-effect count on the trigger), reaching every "each time ... adds a burden/threat" card. Denethor, Steward of the City 7_85 has the same triggers ("Each time a Shadow card adds a threat, add a burden. Each time a Shadow card adds a burden, add a threat.") and would add one burden for Morgul Destroyer's 2 threats instead of two. The test would read 1 token.

**B (as built): two threats added are two additions.** "A threat" is counted one by one: two tokens. Like "Exert X twice", which sets an "each time ... exerts" trigger off twice. Nothing changes.

**Test:** Card_00_060_Tests.TwoThreatsAddedAtOnceByAShadowCardAddTwoTokens reads B (2 tokens) and is active, because the game passes it. On A it is turned round and parked until the engine counts per effect.

**Recommendation:** B, and the same answer as Q-0322: the card counts "a burden or a threat", and that is how "exert twice" was settled.

**Ketura (2026-10-15):** B


### Q-0326 Mithril Shaft 1_22: may the player stop before any card is discarded?

"Maneuver: Exert a Dwarf to discard cards from the top of your draw deck until you choose to stop (limit 5). Add (1) for each card discarded in this way. Take the last card discarded into hand."

Built now: once the Dwarf exerts, the top card is discarded without asking and (1) is added; only then is the player asked "Would you like to continue discarding?", and again after each card up to the fifth. Stopping at once leaves one card discarded, (1) added, and that card taken into hand.

A (as built): at least one card is always discarded; the choice to stop comes after each card. The event always gives a card into hand and at least (1). The active test TheFirstCardIsDiscardedUnaskedAndStoppingThereAddsOneAndTakesItBack asserts this.

B: the player is asked before the first card as well, and may stop with none discarded: Gimli exerts, nothing is added and nothing taken into hand. The definition would ask before the first discard, and that test would change to read the question first and a "no" doing nothing but the exertion.

No test is parked on it: the game passes reading A, so the test of A stays active.

Recommendation: A, as built; "discard cards ... until you choose to stop" reads as a discard that then stops, and B would let the event be played for the exertion alone, which nothing in the text points to.

**Ketura (2026-10-15):** A


### Q-0327 Paths Seldom Trodden 1_222: which tables does the ruling 'can replace sites only in the Fellowship block format' cover?

"Shadow: Remove (3) to replace the fellowship's site with your version of the same site." CR 4.2 Section Three has a ruling on the card: "This card can replace sites only in the Fellowship block format. See Section Two, adventure deck." The same ruling is printed for Úlairë Nelya 1_233 and Dark Places 2_55. Grimbold 7_233 has it with "King block" in its place.

Built now: the definition lays the Shadow player's FELLOWSHIP-block site with the fellowship's site number (`playSite`, `block: fellowship`), and does this in any format. In fotr_block, and on a Fellowship path in the default multipath format, it replaces the site. On a Shadows-block path it is offered, the (3) is paid, and nothing is replaced. On a Towers or King path it would find no Fellowship site of that number in the Shadow player's adventure deck, so again nothing is replaced. 1_233 and 2_55 are built the same way, and 7_233 the same way with `block: king`.

- **A (as built).** The ruling means the Fellowship block's sites, at any table. The card replaces wherever the path is made of Fellowship sites and does nothing on any other path. Nothing changes.
- **B.** The ruling only says the card needs numbered sites (its pointer to "adventure deck"), so it works on any numbered path. On a Towers or King path it would lay the Shadow player's Towers or King site of that number. Correction: drop `block: fellowship` from 1_222, 1_233 and 2_55, and `block: king` from 7_233. A test on a Towers path would be added.
- **C.** It works only in the fotr_block format itself. On the Fellowship path in multipath, nothing would be replaced. This needs a format check the definition language does not have.

Nothing is parked. The active test of the reading as built is Card_01_222_Tests.InTheMultipathFormatOnTheFellowshipPathItReplacesTheSite, and OnAShadowsBlockPathNothingIsReplaced passes under A, B and C alike. One answer covers 1_222, 1_233, 2_55 and 7_233.

Recommendation: A. Your 2026-10-04 ruling makes a format a matter of which decks may sit at the table, and a set 1 card that names a site by number means its own block's site (shapes.md, "A site NUMBER in a text").

**Orchestrator's note:** whichever reading stands, the shape "offered, (3) paid, nothing replaced" on a path where the card can do nothing is F-0379's (an action offered that does nothing). Under A or C the action would also want a requirement that a site of its block and that number can be laid; that goes with your answer, in the next engine pass.

**Ketura (2026-10-15):** So long as the player's site in their adventure deck is a Fellowship block site, we'll let it work.  This means a default FOTR block path (of course), and Multipath if the owner has chosen Fellowship sites (which means it could replace a Towers or King site with a Fellowship site).  This would continue to be barred from replacing a Towers site with a King site or vice versa, or replacing a Fellowship site with a Towers/King site.


### Q-0328 Orc Butchery 1_265: is a kill in a [sauron] Orc's skirmish by another minion's wound 'killed by a [sauron] Orc', now that CR 4.2 defines the phrase?

"Response: If a companion is killed by a [sauron] Orc, the Free Peoples player must discard 3 cards from the top of his draw deck for each card in the dead pile."

Q-0123 asked what "killed by a [sauron] Orc" means, and you kept the loose reading (A) "without an official definition of 'killed by'". There is one. CR 4.2 Section Three, filed under The Ring's Oppression 1_273: "A [Sauron] Orc kills a companion when the game text that kills that companion comes from a [Sauron] Orc minion card, or that companion is killed by losing a skirmish involving a [Sauron] Orc minion." Built now (Q-0123 A, and F-0534 at this landing): any companion killed while a [sauron] Orc is skirmishing counts, whatever dealt the last wound, and so does one killed outside a skirmish by a [sauron] Orc's own wound.

The board that tells them apart: Sam, at 3 of his 4, in a skirmish with Morgul Slayer 3_93 ([sauron] Orc) and Scouting Orc 15_116 ([orc] Orc). Before the skirmish resolves, the Scouting Orc exerts to wound Sam, and he dies.

- **A (keep Q-0123, as built):** Orc Butchery is offered. A kill in the [sauron] Orc's skirmish counts, whatever card dealt the wound. Nothing changes.
- **B (the CR 4.2 entry):** not offered. Sam was killed by an [orc] card's text, not by losing the skirmish. Only a kill by a [sauron] Orc's own text, or a kill by losing a skirmish that a [sauron] Orc is in, counts. 1_265 becomes one `killed` trigger with `killer: culture(sauron),orc` in place of its two branches. This was tried: every test passes but the A test, which flips. Goblin Spear 1_182 ("kills a companion in a skirmish") and The Ring's Oppression 1_273 (the entry's own card) use the same words and are built the A way, so they would be changed and their tests turned round too. The shapes.md entry for "If <X> kills a companion in a skirmish" would be rewritten.

Parked on this: Card_01_265_Tests.ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundDoesNotLetItBePlayed (B). The active ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundLetsItBePlayed asserts A.

Recommendation: B. It is Decipher's own definition of these words, and your answer to Q-0123 rested on there being none.

**Ketura (2026-10-15):** B, good find.


### Q-0325 Dwarven Heart 1_10: heal up to 2 times asked as two yes/no questions or one number choice?

Dwarven Heart 1_10: "When you play this condition, heal bearer up to 2 times."

**Built now:** a required trigger with two yes/no questions in a row, each "Would you like to heal bearer?", each heal one wound. Since F-0528 (fixed at this landing) a question is asked only while the bearer has a wound, so a bearer with 1 wound is asked once and an unwounded bearer is not asked. A bearer with 2 or more wounds is asked twice. Elrond, Herald to Gil-galad 3_13, the other "heal up to 2 times", is built as one number choice from 0 to 2, defaulting to 2.

**A: as built.** Two yes/no questions are fine: "up to 2" is 0 to 2 heals (F-0033), and the player gets every number. Nothing changes. The active tests PlaysOnADwarfAndHealsBearerTwiceButNoMore and HealsBearerOnlyAsOftenAsChosenAndNotWhenAnotherHeartIsPlayed read the two yes/no questions.

**B: one number choice, 0 to 2, as Elrond has it.** The player is asked once, and a default of 2 means one click to heal twice (your wish for Shelob's "up to N", F-0419, and against "do you want to heal" pop-ups, Q-0010 and Q-0143). With 1 wound the choice is still asked (any answer above 0 heals the one wound), or the cap could follow the wounds. The definition is rebuilt on Elrond's pattern, and the four heal tests are rewritten to read the number choice in place of "heal bearer" yes/no. The heals players get are the same either way: only the number of clicks changes.

No test is parked on this: the game passes all its tests as built, and they are rewritten only if B is chosen.

Recommendation: B, for one prompt in place of two, as on Elrond.

**Orchestrator (2026-10-15), settled by your F-0419 words** ("as written, it should offer a choice, 0-N, ideally with a prompt defaulting to N") and Q-0143 (no yes/no pop-up before a choice): B. Queued for the next engine pass (a definition rebuilt on Elrond 3_13's pattern; 1_10's heal tests rewritten). Strike it if you want the two questions kept.


### Q-0315 The Standard's Trigger row: is a wound owed as the nearest miss of 'each time X exerts' (Stewards' Legacy 13_75), the reverse of Q-0265?

The Card Test Standard's `Trigger` row owes as `-` "the nearest event that should not set it off". For "is about to take a wound" or "takes a wound" you ruled that an exertion is NOT owed card by card: it is shown once, in `mechanics/wounds/AWoundThatIsNotTakenTests` (Q-0265: A). The reverse was not asked: is a WOUND owed as the nearest miss of an "each time X exerts" trigger? The Standard's own sentence ("that exerting is not taking a wound and a wound is not an exertion" are mechanics, not retested card by card) suggests not, but no ruling says it and no mechanics class shows it. Stewards' Legacy 13_75's author wrote a wound board for each of its three exert triggers (Card_13_075_Tests.AWoundOnBoromirOrFaramirOrAnotherGondorCompanionExertingOffersNothing); about a dozen printed cards say "each time ... exerts" (Demands of the Sackville-Bagginses 2_40, Too Late 7_209, Demoralized 11_114, Determined Uruk 11_181, Merry 103_85, the Cirith Ungol Watchers 103_63 and 103_92, and errata). Nothing plays differently either way: this is only what a test must lay.

A: not owed, as Q-0265: A has it the other way: a wound does not set off an exertion trigger, shown once in a mechanics class (on 13_75's Boromir sentence, a card that exists), and the Standard says so beside Q-0265. 13_75's file keeps the boards it has.
B: owed: each "each time X exerts" trigger lays a wound on X as its `-`, as 13_75 does. The Standard's Trigger row says so; landed cards with such a trigger and no wound board owe one in a later pass.

**Ketura (2026-10-14):** So long as the relevant triggers are tested in the mechanics tests, I'd say this also doesn't need to repeat it on all relevant cards. A.


### Q-0314 Home and Hearth 12_122: is the heal after 'Then' inside the 'you may'?

"Each time you play a Hobbit, you may remove a burden. Then, if the fellowship is at dwelling site, heal the Ring-bearer."

Built now: one optional trigger whose effect is the burden removal and then the conditional heal. Declined, nothing happens. Taken at a dwelling with no burden left, the Ring-bearer is still healed. With no burden and no dwelling it is not offered.

**A** (as built): the "you may" covers the whole trigger, and the heal comes only if you take it. Declining at a dwelling heals nobody. Nothing changes; the active test DecliningRemovesNoBurdenAndHealsNobody already asserts this.

**B**: only the burden removal is optional, and every Hobbit you play at a dwelling heals the Ring-bearer whether or not you remove a burden. The definition would become a required trigger with a 0-to-1 burden choice. DecliningRemovesNoBurdenAndHealsNobody would then be rewritten to expect the heal.

Parked: nothing. The game passes reading A, so its test stays active.

Recommendation: A. "Then" ties the heal to the removal you chose to make, and this is the only card that prints "you may X. Then, if ..., Y".

**Ketura (2026-10-14):** I would expect the "then" to signify a separate effect, so that it is a required effect that is evaluated after the burden's 0-1 choice is resolved.


### Q-0313 Scouring of the Shire 18_112: does "add a [shire] token here to prevent that" save one [shire] card, or the whole discard as Q-0250?

"**Response:** If a [shire] condition or [shire] possession is about to be discarded from play by a Shadow card, add a [shire] token here to prevent that." (Scouring of the Shire 18_112; the erratum 68_112 has the same sentence word for word. Gimli, Feared Axeman 7_7, "If a [dwarven] condition is about to be discarded by an opponent, exert a Dwarf or discard a [dwarven] card from hand to prevent that", and Deceit 18_29, "If a Free Peoples player's card is about to discard your other [gollum] condition, remove (1) to prevent that", are built the same way, and one answer would cover all four.)

Built now: the response saves ONE card, the [shire] card chosen (`preventDiscard` with `select: choose(culture(shire),or(condition,possession))`). Against Saruman's Power 1_136 ("discard all conditions") the response is offered again for each [shire] card, one token each, and the [dwarven] condition and the Shadow player's own condition in the same discard are discarded. The landed and approved erratum 68_112's tests read it so.

Your Q-0250 (Siege Engine 5_60, "If one or more machines are about to be discarded by an opponent, discard this condition to prevent that") ruled the other way for that card: "prevent that" prevents the whole single simultaneous action, every card in it saved, the opponent's own included. The Standard's line on what a response saves generalises that to any response "that saves with no choice", while shapes.md leaves alone one "that names what it saves". This sentence has no choice in its words, but its "If a [shire] condition" names one card where Siege Engine's "one or more machines" named a kind, so it is not clear that Q-0250 reaches it.

**A (as built): one card.** "A [shire] condition" is one card (F-0136, "a" is one), and "prevent that" is that card's discard. Each [shire] card in a discard costs a token of its own; cards of other kinds go. Nothing changes; the three active tests of 18_112 that read this (WhenAShadowEventDiscardsAllConditionsEachShireConditionMayBeSavedForATokenOfItsOwn, OfEverythingACompanionBearsThatAShadowEventDiscardsOnlyTheShirePossessionAndConditionMayBeSaved, AShadowPossessionsDiscardOfAShireAndAnElvenPossessionIsAnsweredForTheShireOneOnly) and 68_112's stay. The Standard and shapes.md would say that a singular "If a <kind> ..." saves the one card.

**B: the whole discard, as Q-0250.** One token stops all of Saruman's Power: every condition stays, the Shadow player's own included, and the response is not asked again for that discard. The definitions of 18_112 and 68_112 become `select: all(any)` (mutate.py shows this passes the parked test and fails the three tests above, which would be rewritten), and 7_7 and 18_29 would be looked at the same way.

Parked: Card_18_112_Tests.OneResponseToADiscardOfEveryConditionPreventsTheWholeDiscard (Saruman's Power with this card the one [shire] card in the discard; Lord of Moria 1_21 and Strength Born of Fear 1_278 read still in play). It fails as built.

Recommendation: A. The singular "a [shire] condition" reads as one card, and under B one token would let the Free Peoples player cancel a Shadow sweep of every condition, both sides', which seems more than the card's price was meant to buy.

**Ketura (2026-10-14):** Yeah, keep it at A. Scouring is OP as it is (Thus the errata nerfing it), and it canceling all nukes would be even crazier.


### Q-0312 Keep or strike: Stern Words 19_9, a Hobbit-set card (Saruman, The White 32_24) as the only [gandalf] Wizard ally

"To play, spot a [gandalf] Wizard." (Stern Words 19_9; the same words on Traveled Leader 12_34, and "a [gandalf] Wizard" on 67_15 and 67_24.)

Built now: the definition spots `culture(gandalf),wizard`, which reaches an ally as well as a companion. The only printed [gandalf] Wizard who is an ally is Saruman, The White 32_24 (a Hobbit-set card; Radagast, The Brown 31_17 is the other, also Hobbit-set). helpers.md names him for this need, and Card_19_009_Tests.CanBePlayedWhenTheOnlyGandalfWizardIsAnAlly lays him alone and plays Stern Words. Without him, a definition narrowed to `culture(gandalf),wizard,companion` passes every test of 19_9, 12_34, 67_15 and 67_24.

Q-0303 took the Hobbit sets out of a bare TITLE's candidates ("HDG isn't played in constructed and so doesn't overlap with other sets"). It does not say whether that reaches a Hobbit-set card that is the only printed card of its TYPE in a culture-and-race spread.

**A (keep):** a Hobbit-set card is still laid where it is the only printed card that tells two definitions apart (here, the [gandalf] Wizard ally). The test on 32_24 stays in 19_9, and 67_15, 67_24 and 12_34 keep theirs; helpers.md stays as it is.

**B (strike):** Q-0303 reaches every candidate, so no Hobbit-set card is owed in a test of a card outside sets 30 to 33. CanBePlayedWhenTheOnlyGandalfWizardIsAnAlly is dropped from 19_9 (its ledger's F check for the ally Wizard goes to Notes as not shown), and helpers.md says the Saruman, The White board is not owed.

Nothing is parked on it: the test passes as built.

Recommendation: A. It is one small test, and it is the only thing that notices "companion" slipped into the spot.

**Ketura (2026-10-14):** Keep I guess; as you say it's a small test, it's just not exemplary to involve the HDG that way.


### Q-0304 Restated in plain words: a Skirmish card's "not offered outside skirmishes" test, read in which phase?

(Asked first on 2026-10-14; you said "I have no idea what this is asking". Restated.)

Every card usable only in one phase gets a test showing it is NOT offered in another phase. For a Skirmish event or
ability, the Standard says to show that in the regroup phase (the next phase after skirmishes where the player is
asked for actions). Eighteen landed cards (Hobbit Stealth 1_298, Hobbit Intuition 1_296, Saruman's Ambition 1_133
and others) show it in the assignment phase instead (the one just before skirmishes), and their reviewers passed
them. Both prove the same thing: that the card is not offered outside a skirmish. Nothing plays differently.

A: only the regroup phase counts; the eighteen are redone in a later pass.
B: either neighbouring phase (assignment or regroup) is fine; the Standard says so and nothing is redone.

Recommendation: B.

**Ketura (2026-10-14):** Ah yeah, B is fine


### Q-0310 Sting, Weapon of Heritage 11_173: is the Sam sentence a standing trigger, or part of the Maneuver action by Q-0164?

Sting, Weapon of Heritage 11_173 (possession, hand weapon): "Bearer must be Bilbo, Frodo, or Sam.<br>**Maneuver:** If bearer is Bilbo or Frodo, exert him to make another companion resistance +2 until the regroup phase. If bearer is Sam, each time he wins a skirmish, you may remove a burden."

The Sam sentence is printed straight after the Maneuver action, in the same paragraph. Your Q-0164 (Under Foot 60_52, Flaming Brand 52_32) says: "On standard templating, phase actions go last on a card, with ongoing or each time triggers above them. If you see such text at the end after a phase action, it's tied to the prior phase action." Read as written, that reaches this sentence. But it was given for burn-out clauses ("At the start of the regroup phase, discard this"), and here the sentence could never act if it were part of the action.

**Built now:** the Sam sentence is a standing optional trigger, `winsSkirmish` on `bearer,title(Sam)`, active whenever Sam bears Sting. The Maneuver action is offered only to a Bilbo or Frodo bearer.

**A (as built): a standing trigger.** Whenever Sam bears Sting and wins a skirmish, the Free Peoples player may remove a burden. Nothing changes. Five active tests read it and pass: EachTimeSamWinsASkirmishYouMayRemoveABurden, AnotherCompanionWinningOffersNothingAndSamsOfferMayBeDeclined, SamLosingASkirmishOffersNothing, SamWinningWithNoBurdenToRemoveOffersNothing and BorneByFrodoAWinOffersNothingAndNorDoesSamsWin.

**B: part of the Maneuver action, as Q-0164 reads.** With Sam as bearer the action would be offered (it has no cost for him) and would set the trigger up. With no duration printed, that would last only to the end of the maneuver phase (Q-0018), and no skirmish is won in the maneuver phase. So Sam's half of the card would never do anything unless the set-up were given a duration the text does not print, such as the rest of the turn. The definition would be rebuilt as an action open to Sam, BorneBySamTheManeuverActionIsNotOffered would be reversed, and the five tests above would first have to use the action.

**Parked:** nothing. The tests of A are active and pass. No test of B is written.

**Recommendation:** A. Under B, Sam's text would be dead, so Q-0164's template rule should be read as covering only sentences that can act after the action ends, like the burn-out clauses it was given for.

**Ketura (2026-10-14):** This was a poorly-templated card; we will treat the ability as if it were in the proper place above the special ability; they at least had the decency to use proper linebreaks to make that readably distinct. A


### Q-0311 Gwaihir, The Windlord 17_20: may an exhausted Wizard minion be chosen for 'exert a minion (if that minion is a Wizard, wound it instead)'?

"If bearer is a [gandalf] Wizard, each time bearer wins a skirmish, you may exert a minion (if that minion is a Wizard, wound it instead)."

Built now: the choice is of a minion that can be exerted, or a Wizard minion that can be wounded (`or(and(not(wizard),canExert),and(wizard,canWound))`). So an exhausted Wizard minion (the Sarumans of the [men] culture, such as Saruman, Coldly Still 17_48, are the only Wizard minions) may be chosen and is wounded: at his last vitality the wound kills him.

A: yes, as built. The exertion is replaced by the wound, and the replaced effect is what is done (your ruling on "instead" as a conversion, Merry 4_311 with Treebeard 4_103; Q-0209, a choice that can be done in full is carried out in full). Gwaihir can finish off an exhausted Saruman. Nothing changes; the test stays as it is.
B: no. The text chooses "a minion" to exert, and an exhausted minion cannot exert, so he is not offered; the wound only takes the place of an exertion that could have happened. The Wizard half of the choice becomes `and(wizard,canExert)`, and the test flips to assert Saruman not offered (with only him on the table, the trigger is not offered at all).

Test: Card_17_020_Tests.AnExhaustedWizardMinionMayBeChosenAndIsWoundedInstead takes A and is active (the game passes it); nothing is parked.

Recommendation: A, as built; your "instead converts" ruling reads that way.

**Ketura (2026-10-14):** I'm squinting a bit at "the Sarumans of the [men] culture, such as Saruman, Coldly Still 17_48, are the only Wizard minions", which isn't true; besides all but one version of Saruman being a minion, there are also the Deceived wizards that emerge from conditions (Pallando Deceived, Alatar Deceived, etc). Oh, I see what you're asking.  A is fine, the "instead" is superfluous and perhaps should have been worded "exert a minion (or wound it if it is a Wizard)".  A.


### Q-0303 Keep or strike: are the Hobbit-set cards counted among a bare title's owed candidates (Legolas, Prince of Mirkwood 31_9)?

The Card Test Standard, "Cards picked out by title": where a text names a title with no type word ("spot Legolas"), the valid candidates are "cards of that title of each type and each culture there is". It does not say whether the Hobbit sets (30 to 33) count. Those sets are left until very last (your note of 2026-10-04), and lint's `title-kinds` warning does not count them.

Landed cards read it both ways for Legolas: Legolas, Prince of Mirkwood 31_9 is the one Legolas who is an ally. 65_15 leaves him out ("the campaign does not count the Hobbit sets"); 102_7 and 3_21 lay him as a valid candidate. Nothing plays differently either way: this is only what a test must lay.

A: the Hobbit sets are not counted for "each type there is" until their own turn comes. 102_7 and 3_21 keep their boards (more than is owed); nothing is added anywhere.
B: they are counted. Every landed card that names a bare title held by a Hobbit-set card of a type or culture no other card of that title has (31_9 for Legolas, and the like) owes that card as a candidate; 65_15 and any others are reopened, and lint's `title-kinds` learns the Hobbit sets.

**Ketura (2026-10-14):** We don't need to consider the HDG for this, no.  (their own tests can include those, but we don't need to make all Legolas tests everywhere more bloated for no reason; HDG isn't played in constructed and so doesn't overlap with other sets.  So even within its own tests it might not make sense.)


### Q-0305 CR 4.2 'starting fellowship' and 'When you play': owed by every such companion, or one mechanics test?

CR 4.2 "starting fellowship" says "You may use 'When you play' game text on a starting companion". So every Decipher companion with a "When you play" trigger (about 19) has a rules sentence about playing him in the starting fellowship, and neither the Standard nor shapes.md says whether each such card's tests owe it. One landed card lays it: Card_01_307_Tests.ChosenIntoTheStartingFellowshipPippinRemovesTheBidBurden (the bid burden is the cheapest thing such a trigger can act on). mechanics/game/StartingFellowshipTests reads no "When you play" trigger today. Nothing plays differently either way: this is only where the rule is tested.

A: one mechanics test (a companion's "When you play" trigger answered as he is played into the starting fellowship) shows it for every card, as Q-0237 keeps such classes to the mechanics; no card owes it, and 1_307's test stays as it is.
B: every such companion owes it as an `R` clause with a test in his own file; the landed ones without it are reopened.

**Ketura (2026-10-14):** Hummm.  I think this one is valid to check on everyone (where possible), given that the context there is different enough (given the phase it's in) for situations to crop up.


### Q-0306 'Spot Saruman' (Uruk Ravager 3_75): is every culture of a bare title owed, or one second culture?

The Card Test Standard, "Cards picked out by title": where a text names a title with no type word, the valid candidates are "cards of that title of each type and each culture there is". For "spot Saruman" (Uruk Ravager 3_75) that is a Saruman minion of each of four cultures ([isengard], [dunland] 4_33, [uruk-hai] 12_144, [men] 17_48), the follower 17_38 and the ally 32_24, each on a table of its own because the title is unique. The author laid four (two minion cultures, 17_38 and 32_24), as 103_40 did before, and the reviewer passed it. Nothing plays differently either way: this is only what a test must lay.

A: every culture is owed, as written. 3_75 and 103_40 owe the two missing minion cultures, a table each, in a later pass.
B: once each TYPE of the title is laid, a second culture of one type is enough: a culture written beside the name in a definition is then caught. The Standard's line says so, and nothing landed changes.

**Ketura (2026-10-14):** Frankly I think "each type and culture" is a lot, especially for the Sarumen who are the most diverse card bar none. I'd say just cross type boundaries (companion/ally) and culture boundaries (isengard/dunland) and call it a day.


### Q-0307 'Read before and after': is a cheated zone read before an action that alone could move the card?

The Card Test Standard, "Read before and after": "A state the test laid by a cheat (wounds added, an item attached, a card put in a zone) is read too, before the action that changes it". A reviewer read that as not owed where the after-reading could not come out the same without the action: an item attached by a cheat and then discarded by its own ability, or a Nazgul cheated into the discard pile and then played from there (the card cannot be in play, or gone, unless the action moved it). The lander added the before-readings to 9_12, 9_47 and 7_358 so that they pass either way. Nothing plays differently either way: this is only what a test must read.

A: owed always, as written: every state a cheat laid is read before the action that changes it.
B: not owed where the after-reading cannot come about without the action (a zone the card could only reach by it); still owed for a number (wounds, tokens, burdens, the pool), where "unchanged" or "one more" means nothing without the reading before. The Standard's sentence says so; the readings already added stay.

**Ketura (2026-10-14):** Hmm.  I think the Nazgul example seems like it should be checked, just put it in the discard pile *sooner*.  But I can see where the first one is trickier; if it's discarded as soon as it's attached, then it can't be seen on the proper spot without shenanigans.  I'd say A except for the case of instant removal.


### Q-0308 The Standard's B row: does an 'X or Y' cost owe a board where both can be paid and the player is asked which (Citadel of the Stars 5_32)?

The Card Test Standard's `B` row: "Each alternative of an 'or' or 'choose one' is taken in some test", and where a board makes one alternative impossible, a test shows the other is then what happens. It does not say whether an "X or Y" COST owes a board where BOTH alternatives can be paid and the player is read being asked which. Citadel of the Stars 5_32 ("Skirmish: Exert a [gondor] Man or spot 3 knights to transfer this condition ...") takes each alternative on a board where only that one can be paid, so the game never asks; the reviewer passed it, as the row allows. A game that took the first payable alternative without asking would pass those tests. Nothing plays differently either way: this is only what a test must lay.

A: each alternative taken alone is enough, as written; nothing landed changes.
B: an "X or Y" cost also owes one board where both can be paid, with the choice read as asked (both offered) and one of them taken. The `B` row says so; 5_32 (and the landed cards with such a cost) owe that board in a later pass.

**Ketura (2026-10-14):** B, but I don't think older tests need relitigated.


### Q-0309 The Standard's Trigger row: does 'each time <this card> wins a skirmish' owe another card of its kind winning, beside its own loss (Úlairë Toldëa 6_88)?

The Card Test Standard's `Trigger` row names the neighbouring event for "wins a skirmish": "the same character LOSING a skirmish" (a trigger on the end of any skirmish he is in passed every other test of 11_35 and 12_35). For a trigger on the card's OWN win ("Each time Úlairë Toldëa wins a skirmish", 6_88), there is a second miss: another card of its kind winning (another Nazgûl beating a companion), which is what catches `self` written wider (`nazgul`, `minion`). The Modifier and Restriction rows owe that "other card of its kind" for a text on the card itself; the Trigger row does not say. 6_88's author laid the other Nazgûl's win and its reviewer asked which is owed: its loss, the other's win, or both. Nothing plays differently either way: this is only what a test must lay.

A: its own loss alone, as the row has it; another card of its kind winning is laid where the author's "one word wider" reading calls for it, and nothing landed changes.
B: both: a trigger on the card's own win owes its own loss AND another card of its kind winning, as the Modifier and Restriction rows owe for a text on the card itself. The row says so; landed cards with such a trigger and only one of the two owe the other in a later pass.

**Ketura (2026-10-14):** I think A is sufficient here.


### Q-0302 Keep or strike: a character that comes into play exhausted does not exert

Engine pass 37 (F-0500) now places a character that "comes into play exhausted" with its wounds already on it, before anything answers its play. That is how Nertëa 103_77 stops finding it able to exert. The wounds are put on the card directly. The character does not exert, so no "each time ... exerts" card answers it.

Before this pass, the five cards (Gothmog 18_80, Spied From Above 6_84, Peril 6_105, Mountain-troll 65_112, Race Text 93_9) played the character and then exhausted it, after its play had been answered. CR 4.2 says "to exhaust a character means to exert that character as many times as you can", so that exhaust was a run of exertions, and cards that answer an exertion did answer it. I read "comes into play exhausted" as a state the card arrives in, not an exhaust it receives, so the new build lays no exertion.

What plays differently now:
- Unheeded 8_115 ("If a minion exerts, exert an unbound Hobbit to wound that minion") is no longer offered for a minion that Gothmog, Spied From Above, Peril or Mountain-troll 65_112 plays.
- Demoralized 11_114 / 61_114, Determined Uruk 11_181, Cirith Ungol Watcher 103_63 / 103_92 and Lorien Throne Room 101_61 ("each time a companion exerts ...") no longer answer a companion that Race Text 93_9 plays.

Keep: as built. Strike: the placing of those wounds would count as that many exertions, answered by those cards along with the play. The character would still be exhausted before its play is answered, so F-0500's fix and Gothmog's test stay either way. No test reads this yet; one would be added to whichever reading you keep.

**Ketura (2026-10-14):** Hmm, yeah I'd say that "coming into play exhausted" pre-empts the actual play trigger, so it should occur before any exertion responses or whatnot can occur.  Keep.


### Q-0301 Osgiliath Channel 8_120: does "choose to move again this turn" force one move, or every move left in the turn?

Osgiliath Channel 8_120 (King site 7): "**Regroup:** Spot your minion and 6 companions to make the Free Peoples player choose to move again this turn."

**Built now:** the ability adds a `hasToMoveIfAble` modifier with no `until`, so it lasts to the end of the regroup phase it was used in (Q-0018). The Free Peoples player makes that one move without being asked. If a later regroup phase comes in the same turn (move limit 3 or more), he is asked "another move?" as usual.

**A (as built): it forces one move.** "This turn" only says the forced move is this turn's. This matches It Betrayed Isildur 101_47 ("make the Free Peoples player choose to move again", with no "this turn"), whose landed test takes the same reading. Nothing changes. TheFreePeoplesPlayerIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase (move limit 3 by an ad hoc +1) is active and passes.

**B: it forces every move for the rest of the turn.** "This turn" makes the compulsion last the turn, so with a move limit of 3 he must also make the third move. The modifier would get `until: endOfTurn`, and the active test above would be rewritten to assert the third move made unasked. The readings differ only when the move limit is above 2 (Race Texts 91_8 and 91_12, or any +1 to the limit).

**Parked:** nothing. The test of A is active and passes. No test of B is written.

**Same words elsewhere:** No Retreat 4_30 and Forced March 13_108 print "choose to move again this turn (if the move limit allows)", and Hornburg Wall 5_118 prints "make the fellowship move again this turn (if the move limit allows)". All are built the same way, and one answer covers them.

**Recommendation:** A. One "move again" is one move, and "this turn" only rules out a move on a later turn.

**Ketura (2026-10-13):** A, I think.


### Q-0298 Keep or strike: Saruman's Staff 4_174 withheld on 12_54 and 12_144 by the engine, not by naming their subtitles (one Hobbit-set board plays differently)

Saruman's Staff 4_174, "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
phase", was kept from being offered on Saruman, Of Many Colours 12_54 and Saruman, Agent of the Dark Lord 12_144
(Legacy Ruling 6: it "does not have any effect" there) by a requirement in its definition naming the two
subtitles (F-0426; you kept it on Q-0246). Engine pass 32, as queued, made the game's own rule (Q-0224: no offer
that costs nothing and would do nothing) say it instead: thrown with no card in play but the Staff whose
definition reads the switch, it does nothing, so it is not offered; and the subtitle requirement came out. On
every board the campaign plays, nothing changes: both of the Staff's tests pass as they were.

One board plays differently. The four Gandalfs of the Hobbit sets (30_25 to 30_28) read the same switch (F-0037,
known: the Gandalfs want a flag of their own). With one of them in the other player's fellowship and the Staff on
12_54 or 12_144, the Staff is now OFFERED (before: not offered), and using it switches that Gandalf's first
ability off until the regroup phase. Tried on a probe. Only a format that has both the Hobbit sets and set 12
can reach it (Anything Goes). Fixing F-0037 would end the difference.

Keep (as built): the engine decides, and the board above follows F-0037 until that is fixed.
Strike: the subtitle requirement goes back into 4_174's definition beside the engine's rule, and the Staff is
not offered on the two Sarumans whatever else is in play.

**Ketura (2026-10-13):** keep


### Q-0299 Athelas 1_94: does "remove a Shadow condition" discard it, or take it out of the game?

"Fellowship: Discard this possession to heal a companion or to remove a Shadow condition from a companion."

Built now: the "remove" branch is a discard (`type: discard`), so the chosen Shadow condition goes to its owner's discard pile. Neither rules document says where a removed condition goes (no glossary entry for "remove", no ruling naming Athelas), and no other Decipher card removes a condition, so nothing elsewhere settles the word.

**A**: "remove" means discard. As built. The condition goes to the Shadow player's discard pile, where his cards that take from or count his discard pile can reach it. The tests stay as they are.

**B**: "remove" means take it out of the game, not into the discard pile (the game's other out-of-play place, as "remove from the game" is). The definition would change to remove the condition from the game, and in WithOnlyAShadowConditionOnACompanionItDiscardsItWithoutAskingWhich and WithAWoundedCompanionAndAShadowConditionThePlayerAskedWhichMayRemoveTheCondition, the assertion that the condition is in the Shadow discard pile would change to an assertion that it was removed from the game.

Parked: nothing. Both tests read A and pass, and they stay active until this is answered.

Recommendation: A. Where Decipher meant out of the game, the text says "remove from the game"; the plain "remove" of a card in play is the early sets' word for discarding it.

**Ketura (2026-10-13):** A, this is early installment weirdness. Although actually it occurs to me, we should keep track of weird wording for a potential spring cleaning update in the future that fixes these sorts of things.


### Q-0300 Troop of Uruk-hai 1_143: is a strength bonus a weapon gives by its own game text also a bonus "from weapons"?

"A character skirmishing this minion does not gain strength bonuses from weapons." (Troop of Uruk-hai 1_143)

Built now (A): the definition cancels every strength bonus whose source is a weapon (`cancelStrengthBonus`, `from: weapon`), whatever gave it. So the weapon's printed bonus is lost, and so is a bonus the weapon gives through its own game text. On the board tested, Arwen 1_30 bearing Gwemegil 1_47 ("Skirmish: Exert Arwen or discard 2 cards from hand to make her strength +1") reads 6 against a Troop, and still 6 after she exerts to use Gwemegil's ability. Against a minion that is no Troop she reads 8, and 9 after the same ability. Neither rules document speaks of "bonuses from weapons", and no ruling names the card.

**A**: any strength bonus a weapon gives is not gained, the printed +X and a "make bearer strength +1" in its own text alike. As built. Gwemegil's ability can still be used against a Troop: the exertion is paid and the +1 is not gained. The tests stay as they are.

**B**: only the weapon's printed strength bonus is lost; a bonus that its game text gives is gained. Arwen would read 7 after using the ability against a Troop. The engine's `cancelStrengthBonus` would need to tell a card's printed bonus from a modifier its text lays. AStrengthBonusAWeaponGivesByItsOwnAbilityIsNotGainedEither would change its last reading from 6 to 7.

The same words, built the same way (`from: weapon`), are on Mithril-coat 2_105, Shield of Boromir 2_38 and its erratum 52_38, The Witch-king 8_84 and Uruk-hai Troop 12_157 ("loses all strength bonuses from weapons"), so one answer covers them all. The texts that say "from possessions" (built `from: possession`) would presumably follow the same answer.

Parked: nothing. AStrengthBonusAWeaponGivesByItsOwnAbilityIsNotGainedEither reads A and passes, so it stays active until this is answered. AStrengthBonusAWeaponGivesByItsOwnAbilityIsGainedAgainstAMinionThatIsNoTroop is the control.

Recommendation: A. The +1 comes from a weapon, and the text does not say "printed".

**Ketura (2026-10-13):** A


### Q-0295 Keep or strike: the minions with a cost of assigning are offered ahead of his assigning only where he could then assign them, and a payment that fails is not offered again (engine pass 30, beside Q-0283)

Morgul Regiment 7_197: "For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish." Morgul Spawn 7_200 and Morgul Spearman 7_201: "While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish." Úlairë Nertëa 69_38: "The Free Peoples player must add a burden to assign Úlairë Nertëa to skirmish a companion of the named race."

**What was asked and is built (engine pass 30, `regiment`, your words on Q-0283).** As the Free Peoples player starts his own assigning, each of these minions is offered to him as an optional trigger on the minion itself ("Pay to assign Morgul Regiment"): the client highlights the minion for him to click, as it highlights Keep Him Blind to All Else That Moves 103_26 for the Shadow player. He clicks one, pays (choosing who exerts), and is offered the rest again, each judged afresh, until he passes. A minion he paid for is his to assign; one he did not is not (Nertëa: not to a companion of the named race, anyone else as before), and is left for the Shadow player, who pays nothing. One mark on the minion, "its cost of assigning is paid", is set by this payment or by the payment a CARD makes when it has him assign (Éowyn 17_96 and the others, which act before his own assigning and still pay then), and is asked by both, so neither charges twice. Nothing is paid once he has answered (engine pass 26's paying after his answer, in card-id order, is gone). A payment he does not then use is lost, and a fierce assignment is paid for afresh.

**What follows from it that your words did not say, and is now so.**
1. A minion is offered only where he could then assign it to a character its cost charges for, as the game stands. Your words: "presented again with the pending second for pass or selection (if vitality permits)". Beside "can be paid in full" (F-0425), the offer also wants a character to assign the minion to: no offer for Nertëa with no companion of the named race he could assign her to (Hobbit named and the only Hobbit companion hindered, say), none for a minion already assigned by a card, none for a non-fierce minion at the fierce assignment. This is the rule of Q-0224 (an offer that would do nothing is not made): paying would buy nothing.
2. A payment that fails is not offered again in the same assigning. Taken and prevented (Melilot Brandybuck 3_110 stops Nertëa's burden), the minion's offer has been used for that moment, as any optional trigger's is, and she is not his to assign to the named race. Shown by `Card_19_038_ErrataTests.ABurdenPreventedAsHePaysAheadLeavesHerNotHisToAssignToTheNamedRaceAndTheShadowPlayersToAssign`.

Cards that behave this way: the four above, and no other (they are the only definitions with `extraCostToAssign`).

**Keep** (as built): both, as above.

**Strike 1:** the minion is offered whenever its cost can be paid, though nobody could then be assigned to it; a payment then buys nothing and is lost. Undoing it is one condition in `AssignmentCostRule.findCharacterToPayFor` (the costs are still those of a pair, so it would pay Nertëa's burden with no companion of the race to take her).

**Strike 2:** a prevented payment may be tried again while he has not passed (another burden, another Melilot). Undoing it is giving the offer a new identity after a failed payment in `AssignmentCostRule`; nothing else.

Recommendation: keep both.

**Ketura (2026-10-12):** keep both


### Q-0297 Keep or strike: Into the Caves 17_100's bar keeps the minion to what it had when the bar was laid: no more of a keyword it has, and a keyword lost by its own text may come back up to that (engine pass 30, beside Q-0282)

Into the Caves 17_100: "Assignment: Assign a minion to Frodo to make that minion lose all game text keywords of your choice and unable to gain game text keywords until the regroup phase."

**What you ruled (Q-0282: A) and is built (engine pass 30, `nogain`).** The player names which of the minion's keywords it loses; whichever he names, it can gain no game text keyword until the regroup phase. The game now asks "Choose a keyword for the minion to lose, or None" of the minion's OWN game text keywords and None, asks again of what it still has until he answers None or it has none left, and asks nothing of a minion with no keyword. "Would you like to choose more keywords?" is gone: None ends it. Then the bar is laid.

**What follows that your words did not say, and is now so.** The bar is built as "the minion keeps the game text keywords it has at that moment, and has no more than that until the regroup phase":
1. No more of a keyword it already has. An Uruk Savage 1_151 (damage +1) under the bar, made damage +1 by Lurtz's Battle Cry 1_128, gets the strength -1 and stays damage +1; the other Savage beside it, with no bar, is damage +2 (`Card_17_100_Tests.AMinionWithADamageBonusGainsNoMoreOfIt`).
2. A keyword it had when the bar was laid and then lost by its own text may come back, up to what it had. A Desert Nomad 7_132 (damage +1 while 2 threats can be spotted) left damage +1 by the choice, whose threats drop to 1 and come back to 2 before the regroup phase, is damage +1 again. No test shows this; no board was asked for.
Roaming is not a game text keyword and is not held (a minion's roaming follows the site as ever).

Which cards behave differently: Into the Caves alone (no other definition uses the bar). Éowyn, Northwoman 17_96 is unchanged.

- **A. Keep** both, as built.
- **B. Strike 1:** more of a keyword it has may be gained (damage +1 to +2). One line in `ModifiersLogic.getKeywordCount` comes out; the test above would read damage +2.
- **C. Strike 2:** a keyword lost after the bar was laid does not come back before the regroup phase. Not a strike of a line: the bar would have to follow the minion at every step and remember what it lost (as Sentry Uruk's check does); say so and it is built as a change of its own.

Recommendation: A. "Unable to gain" read against what it had leaves the card as close to the plain words as one moment's snapshot allows; 2 is a narrow corner.

**Ketura (2026-10-12):** A


### Q-0296 Making the 3K faster: what the measurements say, and four changes to adopt

You asked for reductions to speed the pipeline up. One reading agent measured the 114 agent runs of the 38 Decipher batches. The full report is `orchestrator/trials/slimdown.md`.

**Where it stands now.** A card costs 6.05M cache-read tokens and 12.5 minutes. That is more than the 4M to 5M we estimated, because the lander was added and the documents grew. At that rate the remaining 2,900 cards take about 300 hours on two pipelines, or about 200 on three.

**A. Adopt (recommended, on the new instance, measured on its first batch):**
1. Card agents carry only the tools they use. About 48K of the 50K tokens each agent starts with are this session's tool schemas (Artifact, remote devices, Docs, scheduled tasks, memory), and they are re-read on every step. A custom agent type in the repo with a short tool list (Bash, Read, Write, Edit, Grep, Glob) saves about 18% of cache-read. Nothing a card agent uses is lost. I still have to check that the workflow's `agent()` can be given an agent type. If it can't, this one waits.
2. Cap what agents read in full at the start, and stop it growing. The author's start went from 127K to 148K tokens over the Decipher run. Cutting about 25K saves about 9%. The risk is a rule an agent no longer reads through, so it would be tried by replaying one past batch (dec-b9_1) first.
3. Launch the next batch before landing the last, which removes the 3-minute gap between batches (about 6%). The cost is that a fix to a brief reaches agents one batch later.
4. The lander drops its first gate run, since its finish step runs the same gate anyway (1.3 minutes a batch).

Together these come to about −27% cache-read and −9% time a card. On three pipelines that is about 185 hours for the 2,900.

**B. Try, don't adopt yet:** the author at medium effort instead of high. 75% of the author's time is writing, so it might save 14% of the time. But the author found 31 of the 35 game faults in these batches, so it would only be adopted if two replayed batches (dec-b15_86, dec-b4_4) still find what they found.

**C. Three pipelines on the new instance** (you raised it). Throughput rises almost in step, since Java now runs about 11 minutes of a 47-minute batch. Tokens per card don't change, but your usage goes 1.5 times as fast an hour. The one risk is memory on the machine (8 GB here).

**Not doing** (unless you want one):
- Reviewing only the cards whose author raised something: it would have missed all three faults the reviewers found (F-0391, F-0475, F-0478).
- Skipping review for short cards: 7.7%, but it misses Q-0248 and three test gaps.
- Changing the batch size: four is about right.
- Cheaper mutation: 7% of tokens, and already the cheap selection.
- Scripting the easy cards: about 25 cards, under 1%.
- Moving doc triage out of the lander: lessons would reach agents later.

The token weights assume output costs 5 times an input token and a cache read 0.1. That is not checked against how your usage meter counts.

Answer A, B and C each, or one word for the lot.

**Ketura (2026-10-12):** I think your A batch is solid, but I wouldn't eliminate the gap; that one I think is likely more responsible for the solid stability than any other.  I'm also not convinced about B; on a previous instance we did an extensive rerun trial with Sonnet vs Opus and Sonnet did in fact straight miss several errors, tho Opus also didn't re-find all the same errors either, which made downgrading a bit of a tradeoff.


### Q-0289 Keep or strike: The Witch-king's Beast 12_184's additional phase and a relentless round are now one round, with The Witch-king and the relentless minions in it (engine pass 30, beside Q-0267)

The Witch-king's Beast, Fell Creature 12_184: "If bearer is The Witch-king, after all skirmishes and fierce skirmishes have been resolved, you may exert him twice to make him participate in one additional assignment and skirmish phase." The V3 keyword relentless (Úlairë Enquëa 103_74: "This minion participates in 1 extra round of skirmishes after fierce"; Savage Fury 103_39 and three other V3 cards give it) brings a round at the same moment. Both are legal together in Expanded (PC).

**What was so.** On your answer to Q-0267 (A) the Beast's effect went back to `setupExtraAssignmentAndSkirmishes, filter: bearer`, which bars every other minion from assignment until the end of the next assignment phase. Tried with a relentless minion in play (The Witch-king bearing the Beast, Enquëa beside him, nobody assigned in the first or fierce round, the Beast's offer taken): the game ran the round as the relentless one, which admits only relentless minions, and the Beast's bar shut out Enquëa as well. Nobody could be assigned: The Witch-king had exerted twice for nothing, and Enquëa lost his relentless round too. Under the keyword build you reverted, the two were one round with both in it.

**What I changed (engine pass 30, `definitions`).** Three classes, a few lines each:
- `EndSkirmishesGameProcess`: when a relentless minion is in play AND a card set up an extra phase, the round is marked both relentless and extra (it used to be relentless only). `AfterAllSkirmishesResult.isExtraPhaseSetUpByACard()` is the new reading of the second.
- `SetupExtraAssignmentAndSkirmishes`: its bar on the other minions leaves out relentless ones.
So with a relentless minion in play the Beast's phase and the relentless round are ONE round, in which The Witch-king and the relentless minions may be assigned, and nobody else. Without one, nothing differs from the revert: the phase is The Witch-king's alone (the thing your commit 85031d5e78 fixed stays fixed: a relentless round still admits only relentless minions).

**Which cards behave differently.** Only 12_184 with a relentless minion in play (Enquëa, or a minion made relentless by one of the four V3 cards that give it). Shown by `Card_12_184_Tests.WithARelentlessMinionInPlayHisAdditionalPhaseIsTheRelentlessRoundAndBothTakePart` (The Witch-king and Enquëa offered for assignment, a fierce Black Rider 12_161 not). `mechanics/RelentlessTests` pass unchanged.

**A. Keep:** one round, both take part (what the keyword build did, now with no keyword).

**B. Two rounds:** the relentless round and then his additional phase, each with its own minions. A larger change to how the rounds follow one another; I would write it up first.

**Strike** (with no B): the three classes go back, and with a relentless minion in play the Beast's offer is still made, he exerts twice, and neither he nor any relentless minion can be assigned in the round that follows.

Recommendation: A. The text says "participate in one additional assignment and skirmish phase", not "a phase of his own", and it is how the game played this since your consolidation.

**Ketura (2026-10-12):** Well done, I approve. A.


### Q-0290 Ravaging Wild Man 15_91: "While you can spot a hunter, this minion gains hunter 1", the Sentry's words: does it, too, keep hunter 1 once gained (Q-0276's A)?

Ravaging Wild Man 15_91 ([men] Man minion, cost 4, strength 10, vitality 2, not unique): "While you can spot a hunter, this minion gains **hunter 1**. Each time this minion wins a skirmish, you may exert 2 [men] Men to discard a follower."

**Why it is put to you.** Engine pass 30 (`sentry`) built your A on Q-0276 for Sentry Uruk 15_170: a keyword the card gives itself while it can spot that keyword, with no "another", is kept once gained, because the card is then itself what it spots. It was built as a word a definition opts into (`spotsItself: true` on `addKeyword`), so no other "while you can spot" changed. The item asked for a search of the definitions for the same shape (a card that gives ITSELF a keyword or kind while it can spot that keyword or kind, with no "another"/"other"). Every definition was read: this card is the only other one. Its first sentence is the Sentry's third, word for word but for the keyword ("gains hunter 1"). Nothing has been changed on it.

**As built now (B).** It does not count itself. Beside another hunter (a hunter companion such as Faramir 18_48, or a hunter minion) it is hunter 1; when the last other hunter leaves play it loses hunter 1. Alone it has none.

- **A. As the Sentry.** `spotsItself: true` on its `addKeyword`. Once it is hunter 1 it keeps hunter 1 while it is in play though the other hunter has gone; alone from the start it still has none. Two copies alone still have none (neither has held it). In play: hunter 1 makes it strength +1 in a skirmish against a companion or ally who is no hunter (CR 4.2 "hunter"), so on A it stays 11 there after, say, the hunter companion it fought earlier in the turn is killed. One line in the definition and a test in its file (the card is unworked: its file has only the stats check), when its chunk comes.
- **B. Leave it as built.** It loses hunter 1 with the last other hunter, as today. The Sentry and the Wild Man then read the same words two ways.

Recommendation: A. The words are the Sentry's, and your reasoning on Q-0276 ("without 'another' in the spot filter text, once it has bootstrapped itself it must continue spotting itself") reaches them as it stands.

**Ketura (2026-10-12):** A, yeah, same deal


### Q-0291 Keep or strike: Sentry Uruk 15_170 keeps a keyword only while it has it: once a card takes it away or bars it, or its text stops, it is not regained alone (engine pass 30, beside Q-0276)

Sentry Uruk 15_170: "While you can spot a fierce minion, this minion is fierce. While you can spot a hunter, this minion gains hunter 1. While you can spot a character that is damage +1, this minion is damage +1."

**What was asked and is built (engine pass 30, `sentry`, your A on Q-0276).** Once the Sentry has a keyword by its own sentence it is itself a card of that kind you can spot, and keeps the keyword while it is in play though the other card has gone; alone it starts with none. Shown by `Card_15_170_Tests.ItSpotsItselfAndKeepsEachKeywordWhenTheOtherCardThatHasItLeavesPlay` and `AFierceMinionKilledInTheFirstRoundLeavesItFierceAndItFightsTheFierceRound` (Crazed Hillman 12_60 killed by Aragorn in the first round; there is a fierce round and the Sentry fights it).

**What follows from it that your words did not say, and is now so.** The Sentry keeps the keyword only while it HAS it. So the chain breaks, and is not made again, whenever the Sentry stops being fierce (hunter, damage +1) by its own sentence for a moment:
- a card makes it lose fierce, or bars it from gaining keywords (Éowyn, Northwoman 17_96; Into the Caves 17_100; a "loses fierce until the regroup phase" card): while that lasts it is not fierce, so there is no fierce minion it can spot, and when that ends it is NOT fierce again unless another fierce minion is in play;
- its text is stopped: it is hindered and restored, or it leaves play and comes back. It starts again from nothing.
This is the same reading as yours turned the other way: "a fierce minion you can spot" is the Sentry only while it is fierce.

How it is built: the condition of each of its three modifiers remembers whether it held, and while it is being worked out counts the Sentry's own grant if it did. The game works it out at every step (`TurnProcedure`, beside the initiative and the deaths of characters) as well as whenever anything asks, so the answer does not depend on whether anything looked at the Sentry in between. That one line in `TurnProcedure` runs in every game; it does nothing unless a card with `spotsItself` is in play.

**Keep** (as built): lost and not regained, as above.

**Strike:** say what the Sentry should do instead. The one other reading I can see is "once gained, kept until it leaves play, whatever happens to it between" (it would be fierce again when the loss or the bar ends). That is a change to how the remembering is cleared (a few lines in `ModifiersLogic`); nothing else.

No test shows the loss: no card's board for it was asked. A probe (an ad hoc "loses fierce" laid and lifted, with a pass between) reads it so.

Recommendation: keep.

**Ketura (2026-10-12):** Yup, that makes sense.  If he bootstraps himself, and then loses other supporting minions, and then his bootstraps are cut, he can't get back up again by himself.  Keep.


### Q-0292 Keep or strike: gen_play.py no longer counts a twilight cost the text gives ANOTHER card as the card's own (8 cards leave pass 31's keep list)

**What changed (engine pass 28, `tools`, from a line a card author left in the proposals list).** `gen_play.py` is the script that writes a card's plain Play test. Since engine pass 29 it is also what lint's `plain-play` warning reads, to decide whether a card's own text bears on how it is played. It used to treat ANY mention of "twilight cost" in a card's text as being about the card's own cost. Now it tells the two apart. A cost the text plainly gives to another card no longer counts:
- "play an [uruk-hai] hunter at twilight cost -2" (Orkish Breeder 18_87);
- "that minion's twilight cost is -2";
- "a Free Peoples card with a twilight cost of X" (Grond 58_103);
- "Elladan is twilight cost -2" (on Elrohir 14_3);
- "its twilight cost is -1" after "play an Uruk-hai" (Uruk Regular 54_192).

Anything it cannot tell is still counted, which is the cautious side. One example: "If stacked on a site you control, play this minion. Its twilight cost is -1" (Uruk Trooper 4_199).

Over every card, 93 cards lose that one reason and nothing else changes. Of those, 51 are now ones the generator writes a Play test for, where before it left them to the author.

**What it changes for you.** Pass 31 strips Play-only tests except on the cards in the keep list (`engine-pass-29/plainplay-keep.tsv`). Under the new reading that list goes from 17 cards to 9. These 8 would now have their Play-only test stripped like any other plain play:
- Orkish Breeder 18_87
- Galadriel 51_45
- Come Down 54_146
- Uruk Regular 54_192
- Grond 58_103
- Glorfindel 59_16
- Ancient Evil 102_37
- From Deepest Dungeon 102_39

Engine pass 28's `trust` report had already read these by eye as plain plays. Pass 29 kept them only because the generator's reading was too broad, and said narrowing it was not that pass's change to make. The new list is `engine-pass-28/tools-files/plainplay-keep.tsv`.

**Also outside the line's words (small):** `wf.py` now finds `workflows.json` under `GEMP_WORK`, as `close.py` already did. I needed that to try `integrate.sh` on throwaway trees. Nothing changes for a normal run.

**Keep**: the narrower reading stays, and pass 31 strips by the 9-card list.

**Strike**: the "twilight cost" check goes back to matching any mention (`gen_play.py`'s `speaks_of_own_cost` and `_own_or_not` come out), and pass 31 strips by pass 29's 17-card list. The `GEMP_WORK` line can be struck on its own; that undoes nothing else.

Until you answer, the change stays. But pass 31 still keeps the tests of all 17 cards: stripping the 8 waits for your Keep.

**Ketura (2026-10-12):** keep


### Q-0293 Keep or strike: Treebeard 15_38 measures a removed loser as it left the skirmish, by a word of its own (memorizeLoser left as it was)

Engine pass 30, item `loser` (F-0480: A). Treebeard, Enraged Shepherd 15_38: "the first Shadow player must exert X minions, where X is the difference between Treebeard's strength and the losing character's strength." As you answered, X now takes the strength the losing character had as the skirmish resolved, wherever it has gone by the time the trigger is carried out. Two choices were made in building it that your words did not settle:

1. **A character removed from the skirmish before it resolved** (Legacy Ruling 2's final removed character, the losing character of an emptied side) is measured at the moment it LEFT the skirmish, with its own text and everything it bore still in force. Example: Goblin Flankers (11), Treebeard's only opponent, are killed mid-skirmish by Long I Fell's wound: X is 12 - 11 = 1. The engine notes each character's strength as it leaves a skirmish (killed, discarded, hindered or removed by a card) for this alone. The other reading would be its strength at the moment of resolution, but by then it may be out of play, where a strength means little (printed 11 here either way).

2. **`memorizeLoser` is left as it was.** It still holds the characters in the skirmish at resolution, less the winner, and not the removed character. Treebeard now reads a new trigger word of his own, `memorizeLoserStrength`. The four other cards that use `memorizeLoser` (Faramir 7_90, Ravage the Defeated 4_32, Savage Fury 103_39, This is My Hour 103_71) play as before. No board was found where they would differ: the removed character is out of play, or hindered (and so not "a companion" to their filters), or `against` is never met.

What changed: `Skirmish` and `GameState` (the note of a strength as a character leaves the skirmish), `ResolveSkirmishEffect` and `CharacterWonSkirmishResult` (the winner is handed each loser's strength as the skirmish resolved, the removed final loser included), and `WinsSkirmish` (the word `memorizeLoserStrength`). Only Treebeard uses it. No other card reads a loser's strength, or any other stat of a loser, after a skirmish. Shown in Card_15_038_Tests by the parked test (now live) and two new ones: the loser killed by its wound for losing taken first, and an enduring loser whose wound raises its strength (X from 11, not 13).

Striking 1 would measure a removed loser at resolution instead. Striking 2 (asking for `memorizeLoser` to hold the removed loser too) would add Legacy Ruling 2's final removed character to that memory for the five cards, with no visible change on any board found. Both are kept unless you strike them.

**Ketura (2026-10-12):** keep both


### Q-0294 Patch notes: the whole draft, and seven doubts

The whole draft, made the Q-0288 way: 455 findings labelled (415 a player can see, 40 not), 21 buckets, of which 13 are big enough for a tally line. It is the file `patchnotes/draft.md` (on your machine with this sync) and is printed in full at the end of this entry; the `###` headings are indented here only so that this page does not split the entry. From now on rows are written as findings are fixed, so the next notes need no agent of their own.

The agent's doubts, for you to settle (each is about whether a player sees it, or where it belongs):

1. The game section has 25 lines. You asked for "a handful of concise sentences". Keep the 25, or should some merge or move down to the card fixes?
2. F-0018: "[[Legolas, Keen-eyed]]: its collector's number now reads V1R9, not V1R10." Do players see collector's numbers (deck builder, card info)? Now in the text corrections.
3. F-0030: "[[53_106|Bill the Pony]] (PC Errata) is now marked a Pony, as the original is." Is the item class shown anywhere? Now in the text corrections.
4. F-0207 and F-0279: the PC errata's link to its original card (171 definitions fixed under F-0279). If the PC Errata page pairs each erratum with its original by that link, these are seen. F-0207 is in as a text correction; F-0279 is out.
5. F-0239, F-0246, F-0247: three errata (55_51, 54_154, 54_73) rebuilt to your newer text. In the card fixes now; they may belong in a PC errata announcement instead.
6. F-0370 and F-0363: "now marked as doing nothing" (O Elbereth! Gilthoniel! with the Ring off; the Aid at Streets of Bree). Only seen if the client shows the no-effect flag. In now.
7. F-0137: This is My Hour offered twice. The fix may reach every response to a won skirmish; if so it is a game line, not a card line.

Small things I mended in the script after the agent: a card with sub-items now keeps "(PC Errata)" on its header line, and names with accented capitals (Úlairë) sort where they should.

The draft:

---
title: Card fixes
tags: Card Fixes, User Interface
---

    ### Game changes

- Revealing a random card from a player's hand no longer shows that player a pop-up of their own card to dismiss.
- Maps and Race to Mount Doom meta-sites can no longer be hindered.
- Playing a minion from your discard pile by its own text (such as Gollum, Plotting Deceiver) no longer counts as a special ability, so cards like [[Gandalf, Lathspell]] can no longer answer it.
- The deck builder now lists each site that is not from a legal set on a line of its own.
- Choosing a card to restore now says "Choose cards to restore", not "Choose cards to hinder".
- A card hindered while its ability is being carried out now finishes that ability, even when it names its bearer (such as [[Gift of the Evenstar]] hindered by [[Pass of the Spider]]).
- A card already in play now counts its printed twilight cost when another card reads it (such as [[Tormented Revenant]]); cost changes apply only while a card is being played.
- When another player's card hinders or discards one of your cards, that card's own "when this is hindered" or "discarded" text is now yours to carry out.
- When a card must exert a character twice to pay its cost (such as [[Spied From Above]]), only characters who can exert twice are offered.
- A card discarded as it comes into play (such as [[Reaching Tentacle]] away from a marsh) no longer sets off its own text or "each time you play" texts on other cards.
- A card that hinders itself to pay for its text is still bound by "cannot" effects (such as [[Cover of Darkness, Omen of Dread]] adding a burden while the Ring-bearer is hindered).
- Cards that play an event from your discard pile, a stack or your draw deck no longer offer response events, which could stop the game.
- Cards dealt with together by one effect (such as "wound each minion") are now always taken in the order they came into play, not a different order each game.
- A choice of cards from a discard pile now shows the card's own instructions where it has them, and the game log names the player who chose.
- An event printed with several phases (such as [[Sword-wall]]) now counts only as an event of the phase it is played in, so it no longer sets off "each time you play a skirmish event" in the regroup phase.
- In the regroup phase, a fellowship with no unhindered companions can no longer move again.
- A game no longer runs an end of turn before the first turn, so nobody is asked questions before play begins and cards in a starting fellowship no longer leave play then.
- An ally that a card bars from archery fire or skirmishes (such as a [rohan] ally at [[Streets of Edoras]]) can no longer be made to take part by another card.
- A possession that cannot be transferred (such as one borne by [[Warrior of Rohan]]) is no longer offered by cards that transfer, only to go nowhere.
- An action that has a minion assign itself to a character is no longer offered when that character already has a minion, whatever his defender bonus.
- A minion that is killed no longer counts as discarded for cards that answer a discard (such as [[Wildman's Oath]]), as the rules have it.
- A Free Peoples event that needs initiative to be played no longer counts itself among the cards in your hand.
- When a site is replaced, cards stacked or played on it now move to the new site, and the old site no longer stays under a player's control.
- You are no longer asked a question whose answer cannot matter: a number when only one number can be chosen, a choice between options none of which could do anything, or whether to pay to prevent something that would not happen (such as a draw from an empty draw deck or past the Rule of 4).
- A hindered Ring-bearer can no longer be discarded or returned to hand, and The One Ring can no longer be discarded, even while hindered (such as at [[Doom Causeway]]).

    ### Card fixes

- Fixed 151 cards offering an ability or trigger, or asking a question, that would do nothing. Example: [[Aragorn, King in Exile]] no longer asks you to heal if there are no wounded companions with the Aragorn signet.
- Fixed 19 cards making you take a card once you looked through your draw deck; you may now come up empty. Example: [[To Honour That Allegiance]] no longer makes you take a card once you look through your draw deck.
- Fixed 14 cards offering to play a card from outside your hand when you could not pay for everything. Example: [[Horsemen Took Your Lands]] can no longer be played when you cannot pay for both the event and the minion.
- Fixed 11 cards offering an action or a choice when its cost could not be paid in full. Example: [[To Highest Peak]] no longer asks the Free Peoples player to prevent it when fewer than X companions can exert.
- Fixed 11 cards not shuffling a draw deck after it was looked through or shuffled into. Example: [[Flotsam]] now shuffles your draw deck even when you shuffle no cards into it.
- Fixed 11 cards not reaching or counting hindered cards. Example: [[Lying Counsel]] now counts and returns hindered cards borne by the companion.
- Fixed 10 cards that did not let you decline a "you may". Example: [[Stewards' Legacy]] now asks whether you want to exert a minion.
- Fixed 9 cards that could not be played or used when they would do nothing; you may now play them anyway, as with any card. Example: [[Breaching Shot]] can now be played when no Uruk-hai can be assigned.
- Fixed 8 cards giving more than their limit when used more than once. Example: [[Sting, Sharp Elven-blade]] no longer gives more than +6 strength in a phase.
- Fixed 7 cards making you choose an exact number where the card says "up to" or "any number". Example: [[Erkenbrand, Kinsman of the House of Eorl]] now lets you remove fewer than 3 companions from the game.
- Fixed 7 cards offering their choice or action to the wrong player. Example: [[Race Text 91_10]] is no longer offered to the player who does not hold it.
- Fixed 7 cards reaching an ally where the card says a companion. Example: [[Savage Fury]] no longer hinders cards borne by an ally.
- Fixed 6 cards giving their "first ... played" bonus to a later card as well. Example: [[Morannon]] no longer discounts a second [sauron] Orc played at site 4 in a later phase of the same turn.
- Fixed [[Ancient Blade]] giving minions an action that named Reckless Pride and assigned to that card's bearer, or to nobody.
- Fixed [[1_29|Ancient Enmity]] and [[51_29|Ancient Enmity]] (PC Errata) going off once for each minion that lost the skirmish, not once.
- Fixed [[Ancient Evil]] not counting its own discount when checking whether you could pay for the stacked minion.
- Fixed [[Andúril, Flame of the West]] and [[4_100|Shadowfax]] not discarding a weapon taken up while they were hindered, once they were restored.
- Fixed [[67_15|A New Light]] (PC Errata) revealing and discarding from the Shadow player's hand when only one minion was returned.
- Fixed [[12_128|A Promise]] giving its bonus while Frodo or Sam was exhausted.
- Fixed [[Arrow and Blade]] being playable without exerting an Elf companion.
- Fixed [[Asfaloth]] making an ally bearer strength +2 when the fellowship was at a plains site.
- Fixed [[Back to the Light]] and [[The Mirror of Galadriel, Dangerous Guide]] naming in the game log the cards put back on your draw deck.
- Fixed [[Beast of War]] being playable from your discard pile with 3 threats, not 5.
- Fixed [[Behold the White Rider]] putting all its wounds on one minion instead of letting you spread them.
- Fixed [[Berserker Torch]] being offered from a pool too small to pay for it even with toil, and being lost when played there.
- Fixed [[Bilbo, Of Bag End]] being playable without spotting 2 companions with the Frodo signet.
- Fixed [[52_75|Bill Ferny, Swarthy Sneering Fellow]] (PC Errata) being listed for the Free Peoples player to assign, only to be refused.
- Fixed [[3_106|Bill the Pony]] and [[53_106|Bill the Pony]] (PC Errata) being discarded at an underground site before lowering its Shadow number.
- Fixed [[Boromir, Proud and Noble Man]] offering only [gondor] possessions from your draw deck, not any possession he can bear.
- Fixed [[Breached Wall]]'s extra site being ignored by [[Gondor Still Stands]], [[Vorondil]], [[Horseman of the North]], [[Sturdy Shield]] and [[White Hand Intruder]].
- Fixed [[Burn and Pillage]] putting every token on one card; you may now spread them out.
- Fixed [[Cirith Ungol Watcher, Spirit of Vigilance]], [[Cirith Ungol Watcher, Hideous Warden]] and [[They Are Not All Accounted For]] counting "limit once per site" wrongly, so a draw could be lost or taken twice.
- [[54_146|Come Down]] (PC Errata):
    - Fixed it being usable without an [isengard] minion
    - Fixed archer allies adding to the fellowship archery total
    - Fixed it and the original losing archery wounds no ally could take; they now go to companions
    - Fixed it and the original sending archery wounds to a [rohan] ally at Streets of Edoras, and being offered when he was the only ally
- Fixed [[Corsair Quartermaster]] gaining damage from items in a skirmish it was not in.
- Fixed [[Council Courtyard]] not removing (2) for Silinde's copy of its text.
- Fixed [[Counsel of the Wise]] not showing the revealed [elven] ally to your opponent.
- Fixed [[Cover of Darkness, Omen of Fear]] hindering every other card in play, not only characters.
- Fixed [[Deadly Contest]] reinforcing a token without the companion it had to spot.
- Fixed [[68_29|Deceit]] (PC Errata) not being offered when the Free Peoples player discarded your [gollum] condition by a site's text.
- Fixed [[Deepest Delvings]] letting you take only a [dwarven] card stacked on it, not any card.
- Fixed [[Desert Wind Scout]] going off before it was hindered, so it could be used though never hindered, and two Scouts could hinder each other back and forth.
- Fixed [[Desolation of Saruman]] letting the Shadow player discard a site he controlled.
- Fixed [[Discoveries]] stopping the game when you spotted no other companions.
- Fixed [[Driven By Her Own Cruel Will]] not hindering a minion when the Hobbit died in the skirmish.
- Fixed [[Dunlending Renegade]] naming Dunlending Rampager in its question to the Free Peoples player.
- Fixed [[1_9|Dwarven Axe]] going off a second time, for nothing, when two minions lost one skirmish to its bearer.
- Fixed [[Elessar's Edict]] letting you spot another [gondor] Wraith who was not exhausted, paying nothing more.
- Fixed [[Ethereal March]] counting the Shadow player's Wraiths toward the 2 [gondor] Wraiths it needs.
- Fixed [[Feel His Blade]] being usable in any skirmish, giving the Shadow initiative for the turn without being transferred.
- Fixed [[Fires Raged Unchecked]] also making roaming [sauron] Orcs strength +1.
- Fixed [[Flung Into the Fray]] playing a [wraith] minion stacked on another card, such as Web.
- Fixed [[Ford of Bruinen]]'s text on Silinde discounting the first Nazgûl played at any site, not only at the Ford.
- [[56_16|Forearmed]] (PC Errata):
    - Fixed it being playable with an empty draw deck
    - Fixed the revealed card being discarded when no minion was made weaker
- Fixed [[For My Old Gaffer]] adding a burden when a Hobbit minion lost the skirmish.
- Fixed [[Furious Hillman]] being usable beside any other [men] card, not only another [men] Man.
- Fixed [[Gamling, Dutiful Marshal]] offering [rohan] possessions he cannot bear or already bears.
- Fixed [[Gandalf, Olorin]] counting a follower named Saruman toward his strength bonus.
- Fixed [[Gathering Wind]] naming its number only after the Shadow player could cancel the event.
- [[53_88|Get Off the Road!]] (PC Errata):
    - Fixed it being playable without spotting a [sauron] card and a Nazgul
    - Fixed one companion exerting for each burden instead of the exertions being spread
- Fixed [[Get Up, Mister Frodo]] asking whether to heal 2 companions where the card says up to 2.
- [[Gollum, Half a Wraith Himself]]:
    - Fixed an X of 0 that did nothing being offered again and again
    - Fixed X being capped by companions' costs; it may now be up to the threats you can remove
- Fixed [[Gollum, Her Sneak]] not counting a Déagol that the other player had removed from the game.
- Fixed [[Great Gate of Minas Tirith]] being offered again and again for one use of a Shadow card's ability.
- [[55_51|Grima, Chief Counselor]] (PC Errata):
    - Now has you spot an unbound companion first, then hinder a condition they bear (or them and their cards)
    - Fixed being able to hinder a condition borne by a hindered companion
- [[54_154|Gríma, Wormtongue]] (PC Errata):
    - Fixed being able to hinder a weapon borne by a hindered companion
    - Now has you spot an unbound companion first, then hinder their weapon (or all their cards)
- Fixed [[Gwaihir, Lord of Eagles]] asking the Shadow player a yes or no before the choice of up to 2 cards, and asking with no Shadow card in the discard pile.
- Fixed [[Hall of Our Fathers]] not taking the stacked cards into an empty hand.
- Fixed [[Halls of My Home]] not letting you discard a revealed Shadow card or order the rest when it had to reveal from your opponent's draw deck.
- Fixed [[53_90|Hand of Sauron]] (PC Errata) offering an exhausted companion who bore no possession.
- Fixed [[Held]] corrupting the Ring-bearer when he was not the Frodo bearing it.
- Fixed [[53_91|His Cruelty and Malice]] (PC Errata) discarding another [sauron] minion unasked.
- Fixed [[House of Elrond]] and [[2_120|Valley of the Silverlode]] not making their offer for an ally who has their text, such as Silinde or Uruviel.
- Fixed [[In the Wild Men's Wake]] letting you spot a [men] possession borne by a minion, not only one in your support area.
- Fixed [[Into the Caves]] making later minions lose keywords named in earlier uses, even in other games.
- [[54_73|Legolas, Dauntless Hunter]] (PC Errata) no longer has a limit on the twilight he removes.
- Fixed [[Leithio i Phillin!]] asking which 2 Elves to spot and then ignoring the answer.
- Fixed [[Let Folly Be Our Cloak]] being playable without exerting a companion.
- Fixed [[Library of Rivendell]] counting the culture of a card stacked on it only when a card of that culture was also in play.
- Fixed [[Long Prepared]] wounding a minion that was not roaming.
- Fixed [[Lorien Throne Room]] not working through an ally who has its text, such as Uruviel.
- Fixed [[52_11|Make Light of Burdens]] (PC Errata) discarding a Shadow condition borne by a hindered Dwarf.
- Fixed [[Merry, Master Holbytla]] resolving any skirmish, not only one involving a [rohan] Man.
- Fixed [[Merry's Pipe]] not going off for a pipeweed discarded along with its killed bearer.
- Fixed [[57_242|Merry's Sword]] (PC Errata) exerting X minions however many [rohan] companions you could spot.
- Fixed [[More Yet to Come]] also taking away the damage bonus wound another losing minion was still to take.
- Fixed [[Morgai Foothills]] discarding no cards from hand.
- Fixed [[Morgul Legions]] and [[Ill Wind]] going off for the second card played in a regroup phase, not only the Shadow phase.
- Fixed [[Morgul on the March]] adding a threat when the Free Peoples player took a card into hand, not only when he drew one.
- Fixed [[Mountain Orc]] asking whether to exert the companion; it now happens, as the card says.
- [[Mountain-troll]]:
    - Fixed it not being playable for its discounted cost beside 5 [orc] minions
    - Fixed it asking whether to discard the 5 minions when it could not be paid for otherwise
- Fixed [[Northern Signal-fire, Flame of Calenhad]] also giving the keywords to Men who came into play after it was used.
- [[O Elbereth! Gilthoniel!]] is now marked as doing nothing when the Ring is off and no skirmish can be cancelled.
- Fixed [[Old Differences]] being able to target any Dwarf or Elf, not just one in the skirmish.
- Fixed [[Ominous Sky]] letting you play a different condition from the one you chose, twilight or not.
- Fixed [[One Dwarf in Moria]] wounding a minion for every item the Dwarf lost when he died, not only for each item returned.
- Fixed [[Orc of Lugburz]] protecting every [sauron] minion with a site number of 1 or less, not only Orcs.
- Fixed [[Orc Searcher]] having another card's text; it now lowers the site number of [sauron] Orcs and can discard itself to cancel a skirmish involving an [isengard] tracker.
- Fixed [[Pale Crown]] also taking away strength bonuses its bearer got from cards borne by the character it was skirmishing.
- Fixed [[Path of the Fellowship]], [[Road to the Towers]] and [[Journey of the King]] showing a garbled message in the deck builder.
- Fixed [[Phial of Galadriel, Bane of Darkness]] hindering every card with the title you spotted, not only Shadow support cards.
- Fixed [[Preparations]] putting all its wounds on one minion instead of letting you spread them.
- Fixed [[Pressing On]] not making a Hobbit companion exert as part of its cost.
- Fixed [[Questions That Need Answering]] and [[Risk a Little Light]] naming in the game log the cards you took into hand.
- Fixed [[Race Text 91_12]] dropping its move limit +1 when the Dwarf, Elf or Gandalf left play; it now lasts the turn.
- Fixed [[Race Text 91_2]] naming in the game log the card you took from your draw deck.
- Fixed [[Race Text 91_8]] dropping its move limit -1 when a fifth companion died; it now lasts the turn.
- Fixed the deck builder not showing sites from other blocks to a player whose Race to Mount Doom path gives him [[Race Text 92_19]].
- [[Race Text 92_29]]:
    - Fixed the opened booster pack being shown only to the Shadow player
    - Fixed response events in the pack being playable, which could stop the game
    - Fixed an event played from the pack paying the cost in its own text
- Fixed [[Race Text 92_35]] letting an ally or minion you healed at a sanctuary be healed again.
- Fixed [[Race Text 93_3]] losing its move limit +1 at the next move; it now lasts the turn.
- Fixed [[Race Text 93_8]] giving a Wraith not in the skirmish strength for the wounds in it.
- Fixed [[Race Text 94_3]] offering Shadow cards for your starting fellowship.
- Fixed [[Race Text 94_4]] freezing the game when a killed companion bore a possession nobody else could bear.
- Fixed [[59_26|Radagast, The Brown]] (PC Errata) taking away the move limit bonus twice when two copies left play in one turn.
- Fixed [[Ravage the Defeated]] also discarding Free Peoples cards borne by a minion who won beside the [dunland] Man.
- Fixed [[Return to Its Master]] switching off The One Ring's text for the next skirmish when no Nazgûl was assigned.
- Fixed [[58_91|Rohirrim Army]] (PC Errata) having resistance 0, not 6.
- [[Royal Guard]]:
    - Fixed valiant lasting only until the end of the skirmish, not until the regroup phase
    - Fixed it not being able to add his strength to a Hobbit ally
- Fixed [[Rush of Steeds]] being offered when a minion exerted as the effect of its own ability, not only as its cost.
- Fixed [[51_139|Savagery to Match Their Numbers]] (PC Errata) letting you take only the plain +2 when you could spot 5 companions.
- Fixed [[17_24|Shadowfax, Greatest of the Mearas]] discarding an artifact hand weapon Gandalf already bore.
- Fixed [[52_38|Shield of Boromir]] (PC Errata) being usable when its bearer was not Boromir.
- Fixed [[Show Them the Meaning of Haste]] being playable with no other Free Peoples event in hand that could be played.
- Fixed [[Shrouded Elf]] and [[63_23|Shrouded Elf]] (PC Errata) putting the event in your discard pile instead of on top of your draw deck.
- Fixed [[Sirannon Dam]] not letting the Shadow player choose which tentacle Watcher in the Water replaces.
- Fixed [[Slow-kindled Courage]] naming Reckless Pride on the assignment action it gives each minion.
- Fixed [[53_5|Song of Durin]] (PC Errata) letting you hinder a Shadow condition instead of discarding [moria] conditions when there were some to discard.
- Fixed [[Steadfast Champion]] labelling the action it gives each minion "Assign this minion to bearer of Steadfast Champion"; it now reads "Assign this minion to Gandalf".
- [[Storm of Argument]]:
    - Fixed it revealing a card despite No Business of Ours
    - Fixed the revealed card being shown to its owner instead of to you
- Fixed a follower's Aid at [[Streets of Bree]] asking for a character it could not go to; it is now offered as doing nothing.
- Fixed [[Supplies of the Mark]] healing any ally, not only a [rohan] ally.
- Fixed [[Take it Down!]] exerting the mounted minion twice instead of wounding it twice.
- Fixed [[Tempest of War]], [[Great Peril of Fire]], [[Guard of the White Tree]], [[Bilbo, Well-spoken Gentlehobbit]] and [[Desolation of Saruman]] not reaching cards borne by cards in a support area.
- Fixed [[53_103|Terrible as the Dawn]] (PC Errata) offering the wounded companion himself as the other companion to discard to prevent it.
- [[53_87|The Dark Lord Advances]] (PC Errata):
    - Fixed the Free Peoples player being able to prevent it by discarding no cards
    - Fixed it letting you take any minion, not only one costing up to X
- Fixed [[The Highest Quality]] and [[An End Worthy of Remembrance]] paying their costs only after the Shadow player could cancel the event, so a cancelled event cost nothing.
- Fixed [[53_67|The Palantir of Orthanc]] (PC Errata) asking a yes or no before the choice of cards; you now just choose any number of cards, none included.
- Fixed [[The Way is Shut]] letting you hinder the same condition whose discard was prevented.
- [[57_256|They Sang as They Slew]] (PC Errata):
    - Fixed X being capped; you may now exert any number of mounted companions
    - Fixed it being playable while exerting no one
- Fixed [[7_256|They Sang as They Slew]] being playable without exerting a mounted companion.
- [[This is My Hour]]:
    - Fixed it wounding every other character in the skirmish, not one companion
    - Fixed two identical offers when two Nazgul won one skirmish
- Fixed [[52_45|Too Much Attention]] (PC Errata) making the Free Peoples player discard the top 6 cards to prevent it, not 5.
- Fixed [[53_104|Tower of Barad-dur]] (PC Errata) asking whether to draw 2 cards when the fellowship moved; it now draws them, as the card says.
- Fixed [[51_142|Traitor's Voice]] (PC Errata) lowering the move limit by itself; it is now a Regroup action you choose to use.
- Fixed [[53_72|Trapped and Alone]] (PC Errata) being playable without another [isengard] Orc to shuffle in.
- [[Trapped in its Gaze]]:
    - Fixed exerting the winning minion and adding a burden without asking the Shadow player
    - Fixed it going off when the winning minion could not exert
- Fixed [[Treebeard, Enraged Shepherd]] counting a losing minion killed in the skirmish as strength 0, so the Shadow player exerted too many minions.
- [[Two-faced]]:
    - Fixed it stopping the game when nothing in the discard pile could replace the minion
    - Fixed the new minion being counted twice
- Fixed [[Úlairë Attëa, Bestowed with Dominion]] letting you play again, as the other minion, a minion discarded as it was played.
- [[69_38|Ulaire Nertea, Dark Horseman]] (PC Errata):
    - Fixed her being assignable to the named race without a burden while the Ring-bearer was hindered
    - Fixed its question naming "a NONE"; the named race is now shown on the card
    - Fixed no burden being added when she was assigned to the named race by a card
- Fixed [[1_234|Úlairë Nertëa, Messenger of Dol Guldur]] asking a yes or no before each minion played from your discard pile.
- Fixed [[Úlairë Nertëa, Sanctified for Cruelty]] offering to play a card from your discard pile when none could go on the minion just played.
- Fixed [[51_154|Uruk Soldier]] (PC Errata) offering exhausted companions to exert, and asking even when every companion was exhausted.
- Fixed [[3_27|Vilya]] being usable with no Shadow condition to return, exerting Elrond for nothing.
- [[War Howdah]]:
    - Fixed keywords being gained without their numbers, such as ambush (2)
    - Fixed the text gained being lost if the Man left the stack
- Fixed [[War Machine]] letting any lurker minion pay its cost, not only an [uruk-hai] one.
- Fixed [[We Hates Them]] naming Reckless Pride on the assignment action it gives each minion.
- Fixed [[We Must Go Warily]] exerting two [gondor] companions once each, not one companion twice.
- Fixed [[White Hand Vanquisher]] counting the Free Peoples player's hunters toward its damage bonus.
- Fixed [[White Mountains]] letting Théoden be one of the 2 other companions who exert.
- Fixed [[Worth Fighting For]] shuffling Free Peoples cards stacked on a Shadow card into the Shadow player's draw deck.

    ### Card text corrections

- [[53_106|Bill the Pony]] (PC Errata) is now marked a Pony, as the original is.
- [[Blood Runs Chill]]: its text now says "exert that Dwarf twice", as it has always played.
- [[69_24|Brought Down from Inside]] (PC Errata): its title now writes "from" in lower case, as on the original.
- [[58_57|Corsair Marauder]] (PC Errata): its text now shows its current errata, as it has always played.
- [[51_167|Denizens Enraged]] (PC Errata) is now shown beside its own original card, not Goblin Archer.
- [[51_245|Desperate Measures]] (PC Errata): its text now says the top 8 cards, as it has always played.
- [[68_134|Doorway to Doom]] (PC Errata): its text now writes "2" as a numeral, as card text does.
- [[60_11|Galadriel, Lady Redeemed]] (PC Errata): its text again says her twilight cost is -3 in your starting fellowship, as it has always played.
- [[53_88|Get Off the Road!]] (PC Errata): its text now shows "Search." on a line of its own, with the rest no longer in bold.
- [[Glorfindel, Revealed in Wrath]] and [[59_16|Glorfindel, Revealed in Wrath]] (PC Errata): a typo in its question ("the the") is corrected, and the errata's text no longer breaks mid-sentence.
- [[Isengard Foreman]]: its text now writes "Orc" with a capital.
- [[Legolas, Keen-eyed]]: its collector's number now reads V1R9, not V1R10.
- [[Leithio i Phillin!]]: its text now writes "Elves" with a capital.
- [[60_91|Mordor Fiend]] (PC Errata): its text no longer says "you may"; the twilight has always been added without asking.
- [[57_190|Morgul Destroyer]] (PC Errata): its text now says strength +3, as it has always played.
- [[68_110|Prized Lagan]] (PC Errata): its text now puts the Maneuver ability on a line of its own, in bold.
- [[Race Text 94_22]]: its text now says "draw deck", as it has always played.
- [[68_111|Robin Smallburrow, Shirriff Cock-Robin]] (PC Errata): its text now shows the Aid reminder as other cards do, on a line of its own.
- [[51_139|Savagery to Match Their Numbers]] (PC Errata): its title now writes "to" in lower case, as on the original.
- [[67_24|Shadowfax, Greatest of the Mearas]] (PC Errata): its text now shows its current errata, as it has always played.
- [[Watching and Waiting]]: its text now says the second ability removes (3), as it has always played.
- [[Writhing Tentacle]]: its text now says it may not bear items, as the card does.

**Ketura (2026-10-12):** Alright, let's dig in: 1- Conciseness was the more important part, that each sentence did not drone on and on, and I think these do that just fine. 2- The collinfo isn't visible anywhere except the image, no; you can nix that. 3- Technically it's visible in the right-click card readout.  I would say "Added missing 'Pony' item class." 4- This is bookkeeping the user won't see, you can drop this. 5- Yeah they'll be listed as proper errata, but they'll still be here too. 6- The no-effect feature will need a bit of explanation in its own feature bullet, and then after that all such affected cards can reference it by saying "now marked with no-effect border" 7- Agreed. As for feedback, I think most of the changes I would make are very specific to the entry at hand, and so will just need a pass from me at the end, whenever these get posted.  But I will say: capitalization entries can all get put into one bucket, those don't need their own individual card lines.


### Q-0285 Keep or strike: `attachedTo(not(<kind>))` is now asked of a hindered bearer too (one class of the engine), because Asfaloth 1_31's `attachedTo(not(ally))` changed nothing without it

Engine pass 28, item `definitions` (your answer to Q-0252, part 2). **Built now.**

**What you asked.** Asfaloth 1_31's standing discard to read `attachedTo(not(ally))` in place of `attachedTo(companion)`, so that hinder does not interfere: a hindered companion bearing it at an underground site loses it.

**What I found.** Written so, nothing changed. The test (Arwen hindered at site 3 with Asfaloth on her, the move to Balin's Tomb) failed under your filter exactly as under the old one. `Filters.attachedTo`, as engine pass 26 built it (Q-0220: E), leaves a hindered bearer out of everything but five bare words (`self`, `bearer`, `memory(...)`, `ring-bearer`, `any`); a `not(...)` was "taken to name a kind" with all the rest, because the filter could not be seen into.

**What I changed, beyond the one word in the definition.** One class, `Filters`. A `not(...)` given to `attachedTo` by itself is now asked of a hindered bearer too, and read against him as he is printed: it finds the bearer by no kind of card, it only rules one out. (`Filters.not` returns a marker type, `RulesAKindOut`, and `attachedTo` lets a hindered bearer through for it as it does for the five words.) A `not(...)` joined to another word, as in `attachedTo(companion,not(name(Frodo)))`, arrives inside an `and` and leaves a hindered bearer out as before.

**Which cards behave differently.**

- Asfaloth 1_31. On a hindered COMPANION it is discarded when the fellowship is at an underground site (your answer; `Card_01_031_Tests.BorneByAHinderedCompanionItIsDiscardedAtAnUndergroundSiteAllTheSame`). On a hindered ALLY it stays, as on any ally: he is read as the ally he is printed as. That second half is my reading of your words ("the intent is to wiggle around auto-discarding from an Ally ... rather than anything that needs the ability to spot bearer in a way that hinder should legitimately interfere with"); it is also what the game did before. Both halves are in `mechanics/BorneByAHinderedBearerTests.ACardOnAHinderedBearerIsStillBorneByAnAllyOrByOneWhoIsNone`.
- The Arkenstone 31_48 is the one other definition with a bare `not(...)` there (of 147 `attachedTo` in the definitions): at the end of the maneuver phase at sites 7 to 9, "if it is on someone who is not Bilbo, wound bearer". Its check now passes for a hindered bearer who is not Bilbo; the wound that follows still finds no hindered bearer, so nothing changes in play.
- Nothing else.

**The choices.**

- **Keep.** Nothing to do.
- **Strike A** (a hindered bearer is no kind of card at all, so "not an ally" is true of every hindered bearer). The marker comes out of `Filters`, and Asfaloth's check is written `self,not(attachedTo(ally))`, which the engine as it stood already reads that way. A hindered companion still loses Asfaloth underground; a hindered ALLY then loses it too, where today and before he keeps it. The ally half of the mechanics test is turned round.
- **Strike B** (no change to `Filters`, and the play stays as it is now). The marker comes out and Asfaloth's check becomes a requirement that no ally, hindered or not, bears it (a negated `canSpot` of `ally,hasAttached(self)` with `hindered: true`; not yet tried). The definition then no longer reads as your filter, and the next `attachedTo(not(...))` anyone writes leaves a hindered bearer out again.

It is in and stays unless you strike it. Recommendation: keep.

**Ketura (2026-10-12):** Keep, good work.


### Q-0286 Keep or strike: two tests on Race Text 94_30's bar on drawing, beside the Rule of 4 boards of Q-0249 and Q-0262 (Barrows of Edoras 4_337, Old Toby 1_305)

**What you asked for, and what was built (engine pass 28, `candraw`; no answer needed for this part).** On Q-0249 you asked for "a requirement that checks to see if anyone is able to draw". It is the requirement `CanDraw`: false once the Rule of 4 is spent in the player's own fellowship phase, and while a card bars him from drawing (Race Text 94_30 is the only card in the definitions that does), true otherwise; whether his draw deck holds a card is not asked (Q-0029). Barrows of Edoras 4_337 ("Fellowship: Place your hand beneath your draw deck and draw 4 cards") now requires a card in hand OR `CanDraw`, in place of the "not yet used this phase" stand-in. On Q-0262 (B) the same reading tells the game's no-idle-offer rule (Q-0224) that a draw by such a player does nothing, so the fifth Old Toby 1_305 of one fellowship phase offers no draw, and neither does an Old Toby played by a player under 94_30.

**What I did beyond the item's words, and why.** Two tests more than the item named, both on printed cards, both showing the half of the change that is the bar on drawing (your "other restrictions"), which the item's named boards do not reach:
1. In Barrows of Edoras 4_337's file, `WithNoCardInHandAndABarOnDrawingTheActionIsNotOffered`: a Towers-sites table where the player who uses the Barrows has Race Text 94_30; at site 3 in his fellowship phase, his hand empty, cards in his deck, nothing drawn yet: the action is not offered.
2. In `mechanics/AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests`, `AnOptionalDrawIsNotOfferedToAPlayerBarredFromDrawing`: Old Toby played from hand by a player who has 94_30, with a card in his deck: its "you may draw a card" is not offered.

One reading of the item's words: it said the requirement's player defaults to "the card's own". It defaults to "you", the player using the card, as every other requirement with a player does. For the Barrows that is the only reading that works: the site the fellowship stands on is often the opponent's copy, and it is the mover who draws.

**Which cards behave differently.** None by these two tests: they are tests only. (By the change itself: Barrows of Edoras, and the optional, cost-free draws of 48 definitions, each only where its player may draw no more; the report lists them.)

**Keep** (the default; both tests stay unless you strike them). **Strike** deletes the two tests and the Barrows ledger line that claims the first; the bar half of `CanDraw` and of the withheld draw is then shown by no test, though it is the same one line of the engine that the Rule of 4 half goes through.

Recommendation: keep.

**Ketura (2026-10-12):** Keep


### Q-0287 Wanted? Replay summaries after the F-0469 fix marked as MetadataVersion 4

**Wanted? Mark replay summaries made after the F-0469 fix with a new MetadataVersion (4), so that stats can tell them from the old ones.**

What was done (engine pass 28, item replaystats): `ReplayMetadata.ParseReplay` now counts a card in a deck's `StartingFellowship` only if it was put into play before the game started, attached cards as much as characters. Before, every card a player attached during the whole game (each possession and condition played on a character, The One Ring again when it changes bearer) was added to the starting fellowship. No card behaves differently in play: this is the replay summary only.

What was NOT done: `MetadataVersion` is still 3. Every summary already stored has the miscounted starting fellowship and nothing in it says so; summaries written from now on are right but carry the same version number. A stats reader can therefore not tell a right summary from a wrong one except by the game's date. The item said to touch nothing else in that server code, so the bump was left for you.

- **A: bump it.** `MetadataVersion = 4`, with a line under the version list ("Version 4: StartingFellowship no longer counts cards attached after the game started"). One line of code and one of comment; nothing reads the number inside the server today (the stats readers outside it would filter on it).
- **B: leave it at 3.** The fix stands as it is; old summaries are told apart by date if ever needed.

Recommendation: A, since the reason for the fix is your stats, and the number is what that field exists for.

**Ketura (2026-10-12):** Hmm, yeah, go ahead and bump it.


### Q-0288 Patch notes for players: a sample, and how the rest would be made

You were right that I should have shown you a sample before writing the whole thing. Here is one, made the new way, from 24 findings I labelled by hand. Nothing else has been run.

How it is made: an agent reads each fixed finding once and writes one row: can a player see it (y/n), card or game, a bucket if it has one, the cards it reaches, the player's sentence, and a short form for when it ends up as a sub-item. A script (patchnotes.py) does the rest: checks every link names one card (the hall's own lookup rules, so a PC errata is linked by id: [[52_91|Southern Spies]]), refuses our numbers and engine words in a line, counts each bucket's cards, turns a bucket of more than 5 cards into one tally line with its example, puts a card with two or more lines outside buckets under its name as sub-items, puts game changes first, and writes the file for the patchnotes folder. It also remembers what went out in which notes, so the next one starts after it.

The sample, as it would sit in the patchnotes folder:

```
    ---
    title: Card fixes (sample)
    tags: Card Fixes, User Interface
    ---

    ### Game changes

    - Revealing a random card from a player's hand no longer shows that player a pop-up of their own card to dismiss.
    - Maps and Race to Mount Doom meta-sites can no longer be hindered.

    ### Card fixes

    - Fixed 17 cards making you take a card once you looked through your draw deck; you may now come up empty. Example: [[To Honour That Allegiance]] no longer makes you take a card once you look through your draw deck.
    - Fixed 6 cards offering an ability or trigger that would do nothing. Example: [[Out of Dark Waters]] no longer offers to play a tentacle when none stacked on it can be played.
    - Fixed [[Gamling, Dutiful Marshal]] offering [rohan] possessions he cannot bear or already bears.
    - Fixed [[Gothmog, Morgul Leader]] offering to play a minion from your discard pile when you could not pay for both the ability and the minion.
    - Fixed the PC errata of [[54_154|Gríma, Wormtongue]] being able to hinder a weapon borne by a hindered companion.
    - Fixed [[Horsemen Took Your Lands]] being playable when you could not pay for both the event and the minion.
    - Fixed the PC errata of [[53_92|Massing in the East]] not letting you return fewer than X [sauron] minions.
    - Fixed [[Shrouded Elf]] and its PC errata putting the event in your discard pile instead of on top of your draw deck.
    - Fixed [[Sirannon Dam]] not letting the Shadow player choose which tentacle Watcher in the Water replaces.
    - Fixed the PC errata of [[53_72|Trapped and Alone]] being playable without another [isengard] Orc to shuffle in.
    - [[Trapped in its Gaze]]:
        - Fixed exerting the winning minion and adding a burden without asking the Shadow player
        - Fixed the Free Peoples player being offered the Shadow player's choice
        - Fixed it going off when the winning minion could not exert
```

The 17 in the first bucket are mostly cards no batch has worked yet: the fix was to the game, and it reaches them. The tally counts every card a fix reaches, not only those a finding names.

Questions:

1. Is this the shape you want? Anything to change in the wording, before an agent writes 400 more like it?

2. "A handful" is set at more than 5 cards. Keep or change?

3. A PC errata is written "the PC errata of [[id|Name]]", linked by id so the card shown is the errata. Keep or change?

4. 20 findings correct only a card's displayed text. For a PC errata that text is shown on the PC Errata page (before and after), so I would count those as seen. For a Decipher or V card I don't know where, if anywhere, a player reads it. Are those patch notes?

5. One file for the whole branch when it is deployed, or one per batch of fixes as they go out?

6. Keep or strike: from now on the lander of each batch writes the patch-note row when it files a finding, so the 3K cards never need a separate pass for this. The rows for the 428 findings already fixed would be written once by one agent, when a pipeline is free.

**Ketura (2026-10-12):** (note that the markdown somehow got eaten by the parser in the Waiting on Ketura page; I went and fished out the full thing from questions.md) Fantastic, this is a thousand percent better.  Very well done :) 1- It's just about perfect, thank you. 2- 5 is good 3- Hmm, let's do "[[id|Name]] (PC Errata)" 4- Put all of those into a separate section; I'll be introducing those changes as "non-errata errata" that emphasize they're just visual in nature without changing much. 5- I don't know when things will get deployed, but there's a chance none of this will go out until December, so I just want the pipeline/workflow streamlined. 6- Brilliant idea, that way the agent review happens as we go and makes it more scriptable.  Very good.


### Q-0262 A "you may draw a card" once the Rule of 4 is spent (Old Toby 1_305): still offered, for nothing (as built), or withheld (Q-0034)?

Old Toby 1_305: "<b>Pipeweed</b>. Plays to your support area. When you play this possession, you may draw a card."

The Rule of 4 (CR 4.2): "You cannot draw (or take into hand) more than 4 cards during your fellowship phase."

**Built now.** A fifth Old Toby played in one fellowship phase, four cards already drawn, still asks "you may draw a card", and a yes draws nothing (tried on a throwaway table: the hand goes from 5 to 4 as he is played and the deck stays at 2). The offer costs nothing and does nothing, which by Q-0034 is an offer not made; the engine's rule of Q-0224 does not withhold it, because the draw says it will do nothing only as it is about to be made (that is what a prevention asks, Q-0114) and not while the action is on offer.

**Why it is asked and not just built.** You ruled on the draw that fails for an EMPTY DECK: "while it is technically a pester to ask them when they have 0 cards in their deck, that is both rare and a bit of a chance for them to *realize* they have no cards in their deck ... I'd say let it ask with 0 cards in deck" (Q-0029). The Rule of 4 was not part of that answer. The same argument may hold (a chance to realize the four are spent), or may not (the player has just drawn four and knows).

- **A. Still offered, as built.** The Rule of 4 is treated as the empty deck is. Nothing changes. Barrows of Edoras 4_337 keeps the hand-written stand-in of Q-0249.
- **B. Not offered once the four are drawn.** The draw tells the rule of Q-0224 that it will do nothing while the player may draw no more this phase (the Rule of 4 spent in his own fellowship phase, or a card that bars him from drawing: Race Text 94_30). An optional trigger or a no-cost ability whose whole effect is a draw is then withheld there; one with a cost, an event, and a "you may" inside another action are offered as now. Some 48 definitions have such an offer (Old Toby 1_305, Red Book of Westmarch 2_113, Book of Mazarbul 3_1, Woodhall Elf 18_17 among them); only those that can be reached a fifth time in one fellowship phase would ever show it. One line in the engine, a `mechanics/` test on Old Toby both ways.

Nothing was changed and no test is parked on this: it comes from the list of things a batch passed up (dec-b4_201, the author of Barrows of Edoras 4_337). Recommendation: A, on the strength of your words on Q-0029; B if the pester is the greater nuisance.

**Ketura (2026-10-12):** A is more likely to cause questions; lots of people get confused over Rule of 4 stuff (even with the addition of UI explaining it).  Let's do B for this one.


### Q-0263 Keep or strike: one mechanics test added on Slippery as Fishes 9_29 (an empty hand revealed by revealCardsFromHand), in place of the Storm of Argument 3_6 test that F-0391's fix deleted

**What you ruled, and what was done (no answer needed for this part).** F-0391: Storm of Argument 3_6 ("Fellowship: Reveal a card at random from an opponent's hand. Shuffle up to X [dwarven] cards from your discard pile into your draw deck, where X is the twilight cost of the card revealed") showed a Shadow card past No Business of Ours 2_44. You: "Seems pretty clear that it should be rewritten to use revealRandomCardsFromHand. If the other test can't then be tested, then it seems that edge case isn't something we really need to worry about, if there are no other candidates in the entire card base." Engine pass 26 did that: 3_6's reveal is written as its three siblings write the same sentence (Friends of Old 3_16, His First Serious Check 3_33, Might of Númenor 3_43), X is still the cost of the card revealed, and 2_44's parked test is live. No engine code changed. I then searched every definition (428 files; the script and its print are in the report): `revealCardsFromHand` is written 13 more times, and not one gives it the hand of a player other than the one revealing (twelve name no hand at all; Porter Troll 11_135 names the Free Peoples player's hand inside a prevention that is the Free Peoples player's own to make). So there is no other candidate, and the two tests of `mechanics/RevealingNoCardsFromAHandTests` that stood on 3_6 are deleted, as you said: `ARevealWrittenAsAChoiceOfCardsFromAnotherPlayersHandIsShownToThePlayerWhoDoesNotOwnTheHand` and `ARevealWrittenAsAChoiceOfCardsFromAnEmptyHandIsShownToNobodyAndTheLogNamesTheHand`. The class comment and F-0274's outcome say so in your words.

**What I did beyond that, and why.** The second of those two was also the class's only showing of your Q-0144 ruling (an empty hand revealed: no pop-up, and a line in the log) for the word `revealCardsFromHand`. Q-0260, still open, told you an empty hand "is still shown for all three reveal words", and named 3_6 for this one; with 3_6 rewritten that stopped being true. A printed card still comes to that line of the engine: Slippery as Fishes 9_29 ("Regroup: Spot Gollum to reveal your hand. Place all Shadow cards revealed beneath your draw deck. Discard this condition."), whose reveal is written with that word, used while its owner's hand is empty. So I added one test on it in the same class, `TheWholeOfAPlayersOwnEmptyHandRevealedByTheWordForChosenCardsIsShownToNobodyEither`: Slippery as Fishes in the Shadow support area, Gollum 5_24 on the table, the Shadow hand empty, the regroup phase. The ability is used; the Free Peoples player is shown nothing; the log says "There were no cards in Shadow Player's hand for Slippery as Fishes to reveal"; the condition is discarded. It shows what a card that exists does. No definition and no engine code was changed for it.

**Which cards behave differently.** None by this: it is a test only. (By F-0391's fix: Storm of Argument 3_6 alone, and only beside No Business of Ours 2_44 or 52_44. There it now reveals nothing, X is 0, and the draw deck is shuffled with nothing shuffled in, as it is when the Shadow hand is empty. The pop-up and the log line of its reveal now read as its siblings' do: "revealed card(s) from hand at random".)

**Keep** (the default; it stays unless you strike it): the class has 7 tests, this one among them, and each of the three reveal words is still shown against an empty hand.

**Strike** would mean: that one test is deleted, with the clause of the class comment and of shapes.md that names it. `revealCardsFromHand` against an empty hand then has no test until Slippery as Fishes 9_29 is worked in its own batch (set 9), if its author lays that board. Nothing else changes; F-0391's fix and the two deletions stand either way.

**Ketura (2026-10-12):** this is keep.  Thank you for the explanation.


### Q-0264 A card that bars an act (No Business of Ours 2_44, "may not look at or reveal"): one card that is stopped, as Q-0236 has it, or a card of each definition word that performs the act?

No Business of Ours 2_44 (and its erratum 52_44): "The Free Peoples player may not look at or reveal cards in any Shadow player's hand." The same holds for any card that bars an act many other cards perform ("may not heal", "cannot be discarded").

**Built now.** Card_02_044_Tests stops four cards: a reveal of the whole hand, a look at it, a card revealed at random, and a look by a site's text; and reads two that are still allowed. F-0391 (Storm of Argument 3_6 showing a Shadow card past No Business) was found only because the reviewer read the engine: the definition language has five words that look at or reveal a hand, four asked the bar and one did not. That is fixed. The batch then proposed a line for the reviewer's checklist: for a card that bars an act, find every definition word that performs the act and lay a card of each word in the barring card's own tests. I did not write that line (engine pass 26, docs): it goes past your answer on Q-0236, so it is yours.

**A (Q-0236 as it stands, carried to a bar):** the barring card is tested with ONE card that is stopped, and its control. That each definition word asks the bar is a mechanic's, shown once in a mechanics class and not card by card (mechanics/RevealingNoCardsFromAHandTests is where this one would go). No line is added to the checklist. Card_02_044_Tests keeps the tests it has.

**B:** a card that bars an act lays a card of each definition word that performs it. That would have caught F-0391 in 2_44's own file. It also makes each such file as wide as the definition language, which you said on 2026-10-11 the campaign is not there to test.

Nothing is parked on this and nothing was changed. Recommendation: A.

**Ketura (2026-10-12):** Hmm. I don't think that a card should necessarily take upon itself the burden of checking that every possible engine route follows its own bar, but I *do* think that mechanic tests should exist that do such a thing.  I would say B2: one of each is a great idea, but they should be in mechanics tests, which will then ensure that future cards using the bar only need to act as A.  Make sense?


### Q-0265 An "is about to take a wound" trigger (The One Ring 1_1, 7_2): is an exertion owed as its nearest miss on every such card, or is "exerting is not taking a wound" the mechanics' alone?

The One Ring, Isildur's Bane 1_1: "Response: If bearer is about to take a wound, he wears The One Ring until the regroup phase. While wearing The One Ring, each time the Ring-bearer is about to take a wound, add 2 burdens instead." The One Ring 7_2 has the same second sentence. 99 cards have "take a wound" or "takes a wound" in a trigger or a response.

**Built now.** The Standard says two things. Its Trigger row: the `-` is "the nearest event that should not set it off". Its last rule: "What every card of a kind obeys is a mechanic and is not retested card by card: that exerting is not taking a wound and a wound is not an exertion". The tests of 1_1 and 7_2 each read an EXERTION of the worn Ring-bearer as the nearest miss (no burden is added). In this game an exertion places a wound token, so it is the event a wrongly written trigger would answer. The reviewer of 7_2 asked for a sentence in the Trigger row making that reading owed. I did not write it (engine pass 26, docs): it adds to what a test must cover, so it is yours.

**A:** not owed card by card. That an exertion does not set off "is about to take a wound" or "takes a wound" is shown once, in a mechanics class (mechanics/AWoundThatIsNotTakenTests is the nearest there is; it would gain one test). The Standard's last rule stands as written; the two Ring tests that already read it stay.

**B:** owed on every card whose trigger or response answers a wound taken or about to be taken: one test exerts the same character and reads nothing happening. That is one more reading on some 99 cards.

Nothing is parked and nothing was changed. Recommendation: A, by your words on Q-0237 ("such classes of mechanical interaction can stand to keep to mechanical tests instead of incessantly checking them on all cards").

**Ketura (2026-10-12):** A


### Q-0266 Sting, Bane of the Eight Legs 8_113: does a choice carried out with no question, the invalid card on the table, show that the card is no candidate, or is a read "not offered" always owed?

Sting, Bane of the Eight Legs 8_113: "Response: If a fierce skirmish involving bearer is about to end, add a threat to discard a minion involved in that skirmish." The doubt is about every text that chooses "a <kind>" where the board holds one card of the kind.

**Built now.** The Standard ("Give choices something to choose from") asks for one more valid candidate than the choice needs, an invalid one beside them, and the offer read on each ("offered" and "not offered") before choosing. In 8_113's test Sam bears Sting against Shelob alone. The game discards Shelob with nothing asked, because she is the only candidate. The invalid candidate is Sam himself, who is also "involved in that skirmish". With the definition's `minion` taken out the game would have two candidates and would ask, and that test fails. So the mistake is noticed, but no "not offered" was ever read. The reviewer asked whether that is enough. I did not write a sentence either way (engine pass 26, docs): allowing it loosens what a test must read, so it is yours.

**A:** it is enough, where the test says so. With exactly one valid candidate and the invalid card on the table, the effect carried out with no question shows the invalid card was no candidate, provided the test reads that nothing was asked, the valid card affected and the invalid one untouched. The Standard gains one sentence saying so. A second valid candidate is still owed wherever the text's own words make a choice ("a minion" where two can be in the skirmish is such a case only if a second minion changes a reading).

**B:** not enough. A choice always gets a second valid candidate and a read "not offered" on the invalid one. 8_113 gains a second minion in the fierce skirmish and reads Sam as not offered.

One thing holds under both, and would be written with the answer: where the invalid candidate is the Ring-bearer and the effect is a discard, the reading shows nothing, since the Ring-bearer is never discarded (Q-0109); the bearer in such a test is Sam, not Frodo.

Nothing is parked and nothing was changed. Recommendation: B. It is one more card on the board, and it keeps one rule for every choice.

**Ketura (2026-10-12):** Yeah I think B is the way to go.


### Q-0267 The Witch-king's Beast 12_184: is its additional assignment and skirmish phase lost when another card has barred The Witch-king from gaining keywords (as built, on the keyword relentless; Éowyn 17_96), or is it his all the same?

The Witch-king's Beast, Fell Creature 12_184 ([wraith] mount, cost 2, strength +2): "Bearer must be a Nazgûl. If bearer is The Witch-king, after all skirmishes and fierce skirmishes have been resolved, you may exert him twice to make him participate in one additional assignment and skirmish phase."

Éowyn, Northwoman 17_96: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that minion lose all game text keywords and unable to gain game text keywords until the regroup phase."

**Built now.** Since your commit 85031d5e78 (2025-11-26, "Fixed Relentless skirmishes including all minions and not just relentless ones") the Beast's effect is `AddKeyword Relentless` on bearer until the start of the regroup phase, where it was `setupExtraAssignmentAndSkirmishes, filter: bearer`; the additional phase is the relentless round of CR 5.0, which the game runs only while a minion has that keyword (`Filters.java` about line 328 and `AfterAllSkirmishesResult.isRelentlessPhase` both ask `hasKeyword RELENTLESS`). So where Éowyn has assigned The Witch-king to the Ring-bearer that turn, he can gain no keyword: after the skirmishes the Beast's offer is still made, he exerts twice (1 wound to 3 on the test's board), and no additional phase follows. The game goes straight to the regroup phase. Éowyn is the only Decipher card that bars a minion from gaining keywords; Pippin, Trollslayer 103_31 ("lose all keywords (except race) until the regroup phase") is on the same `removeAllKeywords` and does the same to a Witch-king he skirmishes. The board comes up in ordinary play whoever wins the Ring-bearer's skirmish: he has lost fierce with the rest, so the Beast's offer comes straight after the first round.

- **A. The additional phase is his all the same.** The printed card gives no keyword: "participate in one additional assignment and skirmish phase" is not a "game text keyword" for Éowyn's bar to reach, and relentless did not exist when either card was printed. He exerts twice and is assigned and fought once more. Two ways to build it. In the definition alone: back to `setupExtraAssignmentAndSkirmishes, filter: bearer` (tried by the author and again by the reviewer: the parked test passes, and every active test of the card passes but one line, `assertTrue(scn.InRelentlessRound())` in the lone test `TheExtraAssignmentPhaseIsOfferedThoughNobodyWasAssignedInEitherRound`, since the game then calls the round "Extra skirmish"; that line would go). Or in the engine, keeping the round relentless: the relentless round asked of something a keyword bar cannot take away, with the Beast marking its bearer for it. The parked test goes live either way.
- **B. As built: the Beast in effect makes him relentless, and a card that bars keywords bars that.** Under Éowyn's bar (or Pippin's) the Beast buys nothing; the offer is still made, as an offer with a cost is (Q-0034), and a Shadow player who takes it has exerted twice for nothing. Nothing changes in the game; the parked test is deleted.

Parked on this, in `Card_12_184_Tests`: `TheAdditionalPhaseIsHisThoughEowynHasBarredHimFromGainingKeywords` (A). The game fails it at `assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase())`: REGROUP is read, after the three wounds have been read. (Frodo is given strength by hand there only so that he lives through the skirmish Éowyn assigns him.) Ten tests of the card are active and pass, under either reading.

Recommendation: **A**, lightly. The commit reads as a tidying of how the extra round is run, not as a decision that keyword bars should reach the Beast, and the text names no keyword. B if making him relentless was the point.

**Ketura (2026-10-12):** Ah shit, I didn't think about keyword effects when consolidating the beast's effect.  Go ahead and revert it to the old effect; A.


### Q-0268 Keep or strike: the engine's return-to-hand effect now refuses the Ring-bearer for itself (before, only the definition word's choice did), so that the guard can be laid with a stapled effect; no card behaves differently

Engine pass 27, `guards`: the guards for things no card should be able to do (discard The One Ring, discard or return to hand the Ring-bearer, hinder a Map or a meta-site) are now laid as you said on Q-0241, with the engine's own effect aimed at the protected card and stapled as a maneuver ability onto Goblin Patrol Troop 1_177, and the invented cards are gone. For that to show anything, the effect itself has to refuse the card. Two of the three did. One did not, and I changed it.

**What was so.** `DiscardCardsFromPlayEffect` and `HinderCardsInPlayEffect` each ask their bar as they are carried out, so a discard aimed straight at the Ring or the Ring-bearer, or a hinder aimed straight at a Map, does nothing to it. `ReturnCardsToHandEffect` asked `canBeReturnedToHand` only when asked whether it could be carried out in full; carried out, it returned whatever it had been aimed at. Aimed straight at the Ring-bearer, hindered or not, it put Frodo in his owner's hand and the game ended (probed before the change). No card could reach that: the definition word `ReturnToHand` asks the same bar of every card it selects (`choose`, `all`, `self`, `memory`) before the effect is made, so the effect was never handed him.

**What changed.** One class, `ReturnCardsToHandEffect`: the cards it returns, the cards its text names and the cards `isPlayableInFull` counts are now those `canBeReturnedToHand` allows (one private method, `getCardsThatMayBeReturned`), the way the discard effect uses `canBeDiscarded`.

**Which cards behave differently.** None. The only bar on returning a card to hand in the whole game is the Ring-bearer's (`CantReturnToHandModifier` is laid by `RingBearerRule` and by nothing else; no definition word lays one), and all 49 `returnToHand` effects in the definitions already left him out at the selection. The suite is unchanged by it (10,989 tests, OK).

**What shows it.** `mechanics/HinderedRingBearerTests.AReturnToHandThatReachesHinderedCardsCannotTakeAHinderedRingBearer`: the Troop's stapled ability returns the Ring-bearer, Sam and Aragorn, with Frodo and Sam hindered; Sam and Aragorn go to hand and Frodo stays, hindered, still the Ring-bearer. With the change taken out that test fails (the hand reads 3 more, not 2). It is the only thing that exercises the change: by your rule that is the question, whether it is wanted at all.

**Keep** (as built; it stays unless you strike it): the rule holds in the effect as well as in the definition word, and the guard has a test.

**Strike**: `ReturnCardsToHandEffect` goes back to returning whatever it is given, and that one test is deleted, since a stapled return to hand would then take him. "A hindered Ring-bearer is not returned to hand" would be shown by no test: no printed card can try it (the two returns to hand that reach hindered cards, Gríma, Wormtongue 4_154 and Lying Counsel 15_85, return the Free Peoples cards a chosen companion bears). The discard, Ring and Map guards are untouched either way.

Recommendation: keep. It is four lines, it changes nothing for any card, and it makes the three effects alike.

**Ketura (2026-10-12):** Keep


### F-0480 Treebeard, Enraged Shepherd 15_38: the losing character has left play when X is worked out; is X taken from the strength it had, or from nothing (as built: X is 12 and every minion exerts)?

Treebeard, Enraged Shepherd 15_38: "Each time Treebeard wins a skirmish, the first Shadow player must exert X minions, where X is the difference between Treebeard's strength and the losing character's strength."

**Built now.** X is worked out as the trigger is carried out, from the minions that are then still in the skirmish and in play. A loser who is gone counts for nothing, so X is the whole of Treebeard's strength. There are two ways to it:

1. His only opponent is removed during the skirmish. Goblin Flankers (strength 11, one wound from death) are killed in the skirmish by the wound of Long I Fell 4_97. The skirmish ends with Treebeard its winner and the Flankers the losing character (CR 4.2 "skirmish phase(s)", LR 2). X is 12: the three other minions on the table all exert and the Shadow player is asked nothing. Had the Flankers lived and lost 11 against 12, he would have exerted one minion of his choice.
2. The loser dies of the skirmish's own wound or of being overwhelmed, and the Free Peoples player, who orders the two required actions (G-0005), takes the death first. Against an overwhelmed Uruk Savage (5) with eight other Savages on the table: Treebeard's text taken first asks the Shadow player to exert 7 of the 9; the death taken first exerts all eight with nothing asked.

- **A. The strength the losing character had when the skirmish resolved.** X is 12 - 11 = 1 against the Flankers, and 7 against the Savage whichever action is taken first. This is an engine change; no definition does it. The winner's trigger would be handed the final removed loser (the engine already names it a loser for LR 2, but `memorizeLoser` remembers only the characters still in the skirmish), and each loser's strength as the skirmish resolved would be kept for the trigger to read. Two definitions were tried: the remembered loser's PRINTED strength still gives 12 against the Flankers, since nothing is remembered, and breaks the test of a loser whose strength was raised in the skirmish. The parked test goes live.
- **B. As built: a card that has left play has no strength, and X is Treebeard's own.** Killing an 11-strength minion outright exerts every minion the Shadow player has, where beating it exerts one; and where the skirmish itself kills the loser, the Free Peoples player picks the larger X by taking the death first. Nothing changes in the game; the parked test is rewritten to read the three minions exerted unasked.
- **C. Nothing is left to measure, and nobody exerts.** The trigger would ask for a loser still in play. The parked test is rewritten to read no exertion.

Parked on this, in `Card_15_038_Tests`: `WhenTheOnlyMinionIsKilledInTheSkirmishTheDifferenceIsFromTheStrengthItHad` (A). The game fails it at the Shadow player's choice ("Choose cards to exert") not being asked. No test is parked for the second way: that order is the player's own doing (F-0389), and this answer settles what X is there too. No old test takes any reading: both of them have the loser alive when X is worked out. Nine tests of the card are active and pass under every reading.

The other cards that remember a skirmish's losers (Ravage the Defeated 4_32, Faramir, Stout Captain 7_90, Savage Fury 103_39, This is My Hour 103_71) act on the loser or on cards it bears; with the loser gone there is nothing for them to act on, so none of them plays differently under A.

Recommendation: A, lightly. "The losing character's strength" reads as a fact about the skirmish he has just won, and the rules give an emptied side a strength of 0 only for deciding who wins (CR 4.2 "overwhelmed"). B is the answer that costs nothing; it makes a kill worth eleven more exertions than a win, which the card does not look meant to do.

**Ketura (2026-10-12):** Oof.  let's do A I suppose.


### Q-0269 Treebeard, Enraged Shepherd 15_38: he beats two minions in one skirmish; is "the losing character's strength" their strength together (as built), or one loser's own?

Treebeard, Enraged Shepherd 15_38: "Each time Treebeard wins a skirmish, the first Shadow player must exert X minions, where X is the difference between Treebeard's strength and the losing character's strength."

**Built now.** X is Treebeard's strength less the strength of every minion in the skirmish, added together. At 12 against an Uruk Savage (5) and an Orc Assassin (6) in one skirmish, X is 12 - 11 = 1 and the Shadow player exerts one minion of his choice. The text speaks of one losing character; here there are two.

- **A. The losing side's strength together (as built).** X is the margin he won by, the number the skirmish itself was resolved on (CR 4.2 "skirmish phase(s)" totals a side). Nothing changes; `BeatingTwoMinionsAtOnceTheDifferenceIsFromTheirStrengthTogether` stays as it is.
- **B. One losing character's own strength.** X is 12 - 5 = 7 or 12 - 6 = 6, so beating two weak minions exerts far more than beating one strong one, and somebody has to say which loser is measured (the Free Peoples player, whose card it is, would be my guess). The trigger would choose one loser and that test would be rewritten to read 7 or 6 minions exerted.

Nothing is parked: the active test reads A, which the game passes. No old test laid two minions against him. (What X is when the loser has left play is asked apart, under F-0480.)

Recommendation: A. It is the plain sense of "the difference" between the two sides of a skirmish he won, and B needs a choice the text does not give anybody.

**Ketura (2026-10-12):** A


### Q-0270 Madril, Defender of Osgiliath 15_64: are the threats counted once, at the start of the maneuver phase (as built), or does the count go on until the regroup phase?

Madril, Defender of Osgiliath 15_64: "At the start of the maneuver phase, each minion is site number +1 for each threat you can spot until the start of the regroup phase."

**Built now.** The threats are counted once, as the trigger is carried out, and the number stands until the regroup phase. With 1 threat every minion on the table is site number +1; a second threat added in the maneuver phase leaves them at +1, there and in the assignment phase. (WHICH minions is not asked: those on the table at the start of the maneuver phase, as Q-0011 has it for a "make each ... until". A minion played later keeps its printed number, and a test reads that.)

- **A. Counted once, at the start of the maneuver phase (as built).** Nothing changes; `OneThreatIsPlusOneAndAThreatAddedAfterTheStartOfTheManeuverPhaseAddsNothing` stays as it is.
- **B. A standing count until the regroup phase.** Each minion's site number follows the threats: +2 as soon as the second threat lies, and down again when a companion's death takes threats away. A Free Peoples player who adds threats after the maneuver phase has begun (to pay for a card, say) would send more minions roaming for his rangers. The trigger's amount would be made a count that goes on being read, and that test rewritten to read +2 after the second threat.

Nothing is parked: the active test reads A, which the game passes. No old test changed the number of threats after the trigger.

Recommendation: A. The sentence opens with a moment, "At the start of the maneuver phase", and which minions it reaches is already fixed at that moment.

**Ketura (2026-10-12):** A


### Q-0271 The check of a player's account name (PlayerIs; Race Text 94_5) has no game test now that the invented cards are gone, and Q-0190 asked for a card-agnostic one: what is to stand in its place?

Engine pass 27, `nocards`: all 23 invented cards that were left are deleted, with the tests that laid them, as you said on 2026-10-11. One of the classes that went is the test you asked for on Q-0190, and the two rulings pull apart there, so I am asking rather than choosing.

**What Q-0190 settled (2026-10-09).** Race Text 94_5 knows the Dark Lord and a World Champion by account name (`PlayerIs`, `player: freeps`, `names: [bign19]` and `[basmelis]`). You said its own tests are not to seat a player under those names, since the names will change, "but with the mechanic test coverage": "What's more important is that the player-name-checking code has card-agnostic tests somewhere."

**What that coverage was.** `mechanics/PlayerIsRequirementTests`, eight tests on three invented cards (Test Roster, Test Summons, Test Census: "Shadow: If <you are / the Free Peoples player is / any player is> the Warden of the Test Table, add (1)"), at a table that seats a player under a made-up name (G-0039, built for it). It read: a listed name is met; any name of the list will do; capitals do not matter; a name that only begins with a listed name is not met; asked of "you" it is the player using the card; asked of the Free Peoples player it is the player whose turn it is; asked of "any" it is either seat.

**As the tree stands now.** That class and the three cards are gone. No test anywhere seats a player under a listed name: 94_5's own tests show only that the usual two players, who are on no list, are not barred. So a `PlayerIs` that never matched, or one on 94_5 asked of the owner instead of the opponent, would pass every test. Still there: the loader's guard that a `PlayerIs` with no names is not loaded (`definitions/PlayerIsRequirementDefinitionTests`), and the rig's own test that a table can seat a player under a name (`virtualtable/NamedSeatTests`).

**A:** leave it so. The name check is tested as far as 94_5's own file goes, which is the negative only. Nothing more is written.

**B:** test it on 94_5 itself, with the names in its definition: an opponent seated as `bign19` cannot choose to move in the regroup phase in region 3, the same for `basmelis`, and the owner seated under either name bars nobody. Three short tests on a printed card and a real board; this is the A you turned down on Q-0190, and they are edited whenever a title changes hands.

**C:** one card-agnostic test of the check with made-up names and no card at all: the requirement is built in the test from its definition text and asked directly, at a table seated under made-up names (four readings: a listed name is met, whatever its capitals; a longer name is not; asked of the Free Peoples player it follows the turn). That is how `at/RequirementsAtTest` tested requirements before the campaign. It is definition text made up for a test, which your rule of 2026-10-11 otherwise shuts out; it has no id and reaches no card pool, and it would be an exception for this one check on the strength of Q-0190.

Recommendation: C, as the only one that keeps both of your answers; B if no definition text is to be written in a test for anything but a guard.

Either way `player: any` and a `PlayerIs` with no `player` ("you") stay untested: no printed card writes them (they are on the list of unused words, asked separately).

**Ketura (2026-10-12):** Yeah do C


### Q-0272 Keep or strike: Test Banner's two readings were moved onto Úlairë Nelya 103_76, the one printed card with its shape (a limit that is a value), not deleted with it: one test added to her own file, one mechanic test relaid on her

Engine pass 27, `nocards`. The item listed Test Banner 0_8001 ("Fellowship: Add (1) to make a companion strength +1 (limit +2, or +4 if you can spot a Dwarf)") among the invented cards whose shape no printed card has, to be deleted with its tests and not replaced. Its own comment said so. It is no longer so: Úlairë Nelya, Glorified to Conquer 103_76 has exactly that shape since F-0135 was fixed ("limit +2, or limit +5 if you can spot another Nazgul": a `CardPhaseLimit` whose limit is a `Conditional`), and she is the only card that has it. So I treated the Banner as your rule treats a word one card uses: tested as far as that card goes.

**What the Banner's two tests read, and where each reading is now.**

1. A limit that falls below what has already been given leaves nothing more to give and takes nothing back (`mechanics/LimitTests`). Now in her own file: `Card_V3_076_Tests.WhenTheOtherNazgulHasLeftPlayASecondUseAddsNothingToTheThreeAlreadyGivenAndTakesNoneBack`. At site 4 with Attëa beside her the first use gives +3 (limit +5); Attëa is taken off the table by the rig; the second use exerts her and she stays at 14. 103_76 was approved and goes back to `ready` for this one test.

2. A bonus whose cap is a value is flagged "limit" once the cap as it stands is spent, and the flag goes when the cap rises (`mechanics/NoEffectFlagTests`). Her ledger said in 2026-10-07 that the game did not flag her spent ability, "the engine's to settle"; the engine has flagged it since, and only the Banner showed it. The Standard keeps the flag of a cap written inside an effect out of a card's own file ("the mechanic's to show"), so this one stays in `mechanics/NoEffectFlagTests` under its old name, relaid on her: alone at site 4 her first use gives +2 and the ability then reads "limit"; Attëa arrives and it reads "false"; the next use gives 3 more.

Each was seen to fail with her limit changed in the definition.

**Keep** (as built; it stays unless you strike it): both readings are of a printed card on a board a game can reach, and the Banner and its tests are gone.

**Strike**: the new test comes out of 103_76's file (which returns to `approved` as it was) and the flag test is deleted from `mechanics/NoEffectFlagTests`. Nothing would then show what happens when her limit drops in mid-skirmish, or that her spent ability is flagged.

Recommendation: keep.

**Ketura (2026-10-12):** Keep


### Q-0273 Keep or strike: of the five other test classes that build a definition in the test, the two that read shapes no card has are deleted and the three guards of the loader are kept

Engine pass 27, `nocards`, the search you asked for on Q-0241: any other test that invents a card. `testCards.hjson` is gone with all 30 of its cards, and the loader for it (`TestCardLibrary`): a test table's card library is now the printed cards and nothing else. Beyond that file I found five test classes that build a definition in the test, all of the campaign's making. None of them puts a card on a table or into any library; each hands definition text to the builder and reads what comes back. I judged them by your three rules and acted, and this is the account, for you to strike either way. Nothing that predates the campaign invents a card: the only other stand-ins are Mockito mocks of the card library in the hall, tournament and handler tests (one gives three real meta-sites a name), which play no card, and `library/LotroCardBlueprintLibrary2` (2024), a second loader of the real definitions.

**Deleted (a shape no printed card has):**

- `game/formats/DeckBuildingRestrictionTest`, 4 tests (engine pass 5). It built a Map "of the Test" with id 0_1 to read the `min` of a `DeckBuildingRestriction`, which engine pass 5 mended (it was read from the `max` key). The three Maps that have the effect all write `max: 0` and no `min`, so no card can show `min`, nor a `max` above 0.
- `definitions/AidTwilightCostDefinitionTests`, 3 tests (engine pass 26). It built 17 followers to read what a blueprint keeps of an Aid cost: numbers written as text, a cost in a list of one, twilight beside another cost, twilight worked out in play. The Aid costs printed followers have (a plain number, the default 1, a Shadow follower's removal; a burden, a threat, an exertion, which have no number) are read on the printed followers in the rig's own `virtualtable/AidCostReadingTests`, which stays.

**Kept, as guards (5 tests in 3 classes):** a definition the engine cannot read must be refused when it is loaded. No printed card can show that, since a card so written would not load at all, and it cannot be stapled onto Frodo: there is no game. So each writes its definitions in the test itself, with throwaway ids (0_91xx, 0_92xx) that only name the entries of the text handed to the loader.

- `definitions/UnknownPhaseInADefinitionTests`: `until: end(turn)`, a misspelt phase and the like are refused; they used to load and end the effect a phase or a turn early with nothing to say why.
- `definitions/PlayerIsRequirementDefinitionTests`: a `PlayerIs` that lists no name is refused.
- `definitions/PlacingTheEventPlayedOnADrawDeckDefinitionTests`: the `filter` of the two effects is `self` or `played` and any other word is refused.

These are the "nuts-and-bolts testing of the HJSON layer ... sequestered into a separate place" of Q-0233, and I read them as your guards "for things that shouldn't happen", though they are not laid with the ad hoc staplers and do carry ids of a kind.

**Keep** (as built): the two classes stay deleted and the three guards stay.

**Strike, either half:** say "restore the two" and they come back from the commit before; say "delete the three" and the loader's refusals have no test.

Recommendation: keep.

**Ketura (2026-10-12):** Keep


### Q-0274 Definition words and shapes no printed card uses (the list after the invented cards went): which, if any, are to be deleted from the engine?

Engine pass 27, `nocards`. You said of definitions that have no cards: "they might be obsolete due to refactors, they might be preemptive, or they might be intentionally abandoned. In any of those situations, we don't want to *keep testing* that they work." Their tests are deleted. The words themselves are all still in the engine: nothing was removed, and this is the list for you to say which, if any, should go. Each was checked against every definition under `cards/` (3,997 cards).

**Words no card definition uses, whose only test laid an invented card (test deleted this pass):**

1. `cantAddBurdens` (modifier; `modifier/CantAddBurdens.java`). "Burdens cannot be added." Never used by a card; F-0159 mended it (it built "cannot remove"). The modifier class it builds is also what the rules lay while the Ring-bearer is hindered, and that stays whatever becomes of the word.
2. `canAddBurdens` (requirement; `requirement/CanAddBurdens.java`). Built in engine pass 20 for Úlairë Nertëa 69_38 (F-0308), who stopped using it in engine pass 26 when her burden became a cost of assigning (Q-0221).
3. `fpStartedAssigning` (trigger; `trigger/FPStartedAssigning.java`). Made in 2021 for Úlairë Nertëa 19_38 / 69_38; no card has had it since F-0308.
4. `PerSiteLimit` (requirement) and `IncrementPerSiteLimit` (effect): "limit once per site" written by hand. The five cards with such a limit use the field `limitPerSite`, which does both.

**Words no card definition uses, which had no test (engine pass 26 left them so):**

5. `allyCanParticipateInArcheryFire` (modifier; `modifier/AllyCanParticipateInArcheryFire.java`). The two cards of the kind say `allyCanParticipateInArcheryFireAndSkirmishes` (Great Hall 4_353, Percy 32_19).
6. `restored` (trigger; `trigger/Restored.java`). Andúril 7_79 and Shadowfax 4_100 were its only users until their discards became standing ones (Q-0229). Its twin `hindered` is in use.

**Shapes of a word that IS in use, which no card writes (their tests deleted this pass; nothing to remove unless you want the engine narrowed):**

7. `PlayerIs` with `player: any`, or with no `player` ("you"): Race Text 94_5 writes `player: freeps` only.
8. `PutPlayedEventOnBottomOfDrawDeck` with `filter: played` (another card's response placing the event beneath a deck): Shrouded Elf places on top; the three cards that place beneath place themselves.
9. `DiscardCardsFromDrawDeck` / `RemoveCardsInDeckFromGame` with `deck:` naming the other player and `shuffle: true` (F-0369): the Gronds write `shuffle: false` and a `ShuffleDeck`.
10. A `choice` one of whose branches is a list of effects, the first filling a memory for the second: no definition's choice has a list for a branch at all.
11. `{stored}` in a QUESTION put by a later action than the one that stored the value (F-0307's shape): 69_38 now has it in a log line, which her own tests read.
12. `DeckBuildingRestriction` with a `min`, or a `max` above 0: the three Maps write `max: 0`.
13. A cost that exerts a character the player chooses, on an ability that then plays an EVENT from a discard pile (F-0184's third case, "judged on the board as it stands"): the printed cards of that cost play possessions, minions and conditions.
14. `DuplicateGameText` with `snapshot: true` on a card that outlives the time named (the copied text gone when the time is up): War Howdah's Southron is discarded before its turn ends. And for a keyword snapshot, that what the source gains AFTERWARDS is not passed on: nothing printed gives Northern Signal-fire's Man an unloaded keyword in mid-turn.

**Found by a mechanical sweep of the four factories, not looked into one by one** (registered words that no definition under `cards/` uses; none has a test): modifiers `addNoTwilightForCompanionMove`, `canBearExtraItems`, `modifyHandSize`; requirements `isNotEqual`, `canMove`, `canSpotDifferentCultures`; triggers `aboutToRestore`, `addsTwilight`, `cancelledSkirmish`, `fpDecidedIfMoving`, `fpDecidedToMove`, `removesCultureToken`, `siteLiberated`; effects `resetWhileInZoneData`, `revealCards`, `setupExtraAssignmentAndSkirmishes`, `shadowCantHaveInitiative`. Four of these are used by the archived second-edition set 40 only (`addNoTwilightForCompanionMove`, `cancelledSkirmish`, `resetWhileInZoneData`, `shadowCantHaveInitiative`), `modifyHandSize` by a tutorial scenario (so it is in use, though by no card), and `isNotEqual` by a card that is commented out in the V3 [ringwraith] file.

**A:** leave every word where it is (as the tree stands). Untested, each costs nothing until a card wants it.

**B:** name the numbers to delete from the engine, and an engine item does it (1 to 6 are whole words and come out cleanly; 7 to 14 would mean refusing the shape when a definition is loaded).

No recommendation on which: they are yours to know as abandoned, preemptive or wanted. If it helps, 2, 3 and 6 lost their last card to changes made in this campaign, and 4 is a second way of writing what a field already does.

**Ketura (2026-10-12):** A, we'll keep all these.  Many of them are counterparts to existing mechanics, and since they swim in the same waters, future cards playing in the same design space are likely to run into them.  And some of them are for scenarios, as you say.


### Q-0275 Mountain-troll 15_112: "to make it twilight cost -10 and fierce" names no time; is the troll fierce while it is in play (as built, and as your old test read), or only to the end of the Shadow phase it was played in (Q-0018's default)?

Mountain-troll 15_112: "When you play this minion, you may discard 5 [orc] minions from play to make it twilight cost -10 and fierce."

**Built now.** Once the five were discarded the troll is fierce for as long as it is in play, which in a game is until the turn ends and the minions are discarded. Your old test (`OnPlayTrollOptionallDiscards5OrcMinionsForMinus10TwilightAndFierceUntilEndOfTurn`) read it fierce in the assignment phase, in the regroup phase and after a second move, and the erratum 65_112 writes "until the end of the turn" outright. The text of 15_112 names no time, and Q-0018 says an effect with no stated duration lasts to the end of the current phase, "for a trigger's effect as for a phase action's".

- **A. Fierce while it is in play (as built, and as your old test read).** The sentence says how the card is played, as its cost is paid, and is no effect begun in a phase: Q-0018's default does not reach it. Nothing changes; `FierceLastsThroughTheRegroupPhaseAndASecondMoveUntilTheTurnEnds` stays, the parked test is deleted, and the clause owes no check of an ending.
- **B. Q-0018's default: fierce to the end of the Shadow phase it was played in.** The word then does nothing in any game, since no skirmish is fought in a Shadow phase. The definition's standing modifier would be held to the Shadow phase, the parked test would go live, and the old test's readings after the Shadow phase would be turned round.

Parked on it: `FierceEndsWithTheShadowPhaseTheTrollWasPlayedIn` (reading B; the game fails it at the start of the maneuver phase, where the troll is still fierce). No other card has the shape: every other "When you play this ..., ... to make it ..." names a time or speaks of cost alone (Host of Udûn 7_282).

Recommendation: A. On B the card's last two words would be empty.

**Ketura (2026-10-12):** lmao, of course it doesn't say.  The intent is obvious in this case, as Fierce does very little in the Shadow phase; Decipher had a habit of accidentally leaving it off.  We'll be gracious this once and let it be until the end of the turn like the errata is; A.


### Q-0276 Sentry Uruk 15_170: once it is fierce (a hunter, damage +1) by its own text, does it spot itself and keep the keyword after the other card has left play, or lose it (as built)?

Sentry Uruk 15_170: "While you can spot a fierce minion, this minion is fierce. While you can spot a hunter, this minion gains hunter 1. While you can spot a character that is damage +1, this minion is damage +1."

**Built now.** The Sentry is not counted toward its own sentences. Alone it has none of the three keywords. Beside a Black Rider (fierce), a Tracking Uruk (hunter 1) and an Uruk Savage (damage +1) it has all three; when the Black Rider leaves play it is no longer fierce, and so for the other two. None of the three sentences says "another", and the definition's filters (`fierce,minion`; `hunter`; `character,damage`) do not leave the Sentry out: the game does, by how it works out a modifier a card lays on itself.

- **A. It spots itself (the words as written).** Once the Sentry is fierce it is a fierce minion you can spot, so it stays fierce for as long as it is in play though the other minion has gone; the same for hunter 1 and damage +1. It still starts with nothing when alone. In play: the fierce minion beside it is killed in the first round of skirmishes, and the Sentry still fights the fierce round. The engine would have to keep each keyword once gained (no change to the definition does it), and `ItDoesNotSpotItselfAndLosesEachKeywordWhenTheOtherCardThatHasItLeavesPlay` would be turned round.
- **B. It does not spot itself (as built).** Each keyword goes when the last OTHER card that has it leaves play. Nothing changes.

Nothing is parked: the active test reads B, which the game passes. No old test took a reading (the old tests laid the other minions and never took one away).

Recommendation: B. A keyword the Sentry has only by a sentence cannot be what keeps that sentence true; on A the same board would read two ways by what had been in play before.

**Ketura (2026-10-12):** This one actually came up on mlotro a while ago and I thought through it then: it has to be A, because without 'another' in the spot filter text, once it has bootstrapped itself it must continue spotting itself.  Undoing that would mean altering the text from what was written (which i just said to do on Mountain-troll lmao, but in this case it's not quite as clear what was meant in any case).  A.


### Q-0277 Keep or strike: an exhaustion paid as a cost says so in its exertions' results, as an exertion now does (F-0478, Rush of Steeds 11_157); no card shows it

Engine pass 27, `exertcost` (F-0478, commit f81c44cac). Rush of Steeds 11_157: "Response: If a minion exerts as a cost of its special ability, discard this condition to prevent that and return that minion to its owner's hand."

**What the item asked for, and is done:** the result of an exertion now says whether it was paid as a cost (`ExertResult.isPaidAsCost`, the `cost` flag the definition's `exert` was run with), and the trigger `exertsForSpecialAbility` answers only a cost. Lavastone Giant 33_44's second exertion, the effect of its own ability, is no longer answered.

**What I did beyond its words:** the definition word `exhaust` hands the same flag on (`Exhaust` to `ExhaustCharacterEffect.setPaidAsCost`, 10 lines in two classes). An exhaustion is a run of exertions, each with its own `ExertResult`. Without this, an exhaustion paid as a cost would say "not a cost", and a minion with "Exhaust this minion to ..." as a special ability would stop being answered by Rush of Steeds, where before this pass it was answered.

**No card shows it, and no test holds it.** No minion's special ability exhausts that minion as a cost. Five definitions have an exhaustion in a cost of any kind, and none is a minion's own special ability: Elessar's Edict 8_33 and Words of Power 103_8 are events; Úlairë Toldëa 103_79 exhausts herself as the cost of an optional trigger, and a trigger is no special ability; Merry 101_52 and Snowmane 103_88 have the Shadow player exhaust a minion, the one to prevent a companion's ability and the other in a possession's trigger. Rush of Steeds answers none of them, before or after. So no card behaves differently for these lines, either way.

**Keep** (as built): `exert` and `exhaust` say the same thing of themselves, and a future "Exhaust this minion to ..." is answered as Rush of Steeds' text says. The code's comment says no card exercises it.

**Strike:** say "strike" and the 10 lines go (`setPaidAsCost` in `ExhaustCharacterEffect`, its call in `Exhaust`). Nothing printed changes; an exhaustion's results would then read as effects, with a comment saying so.

Recommendation: keep.

**Ketura (2026-10-12):** Exhaust is defined as "exert that character as many times as you can", so the two are linked.  Keep.


### Q-0278 Keep or strike: A New Light 17_15 now asks before it reveals the Shadow player's hand ("you may reveal"), and the reading of your 2022-12-13 test that showed the hand at once is dropped (F-0485)

Batch dec-b17_15. Already done; it stays unless you strike it.

A New Light 17_15 (Free Peoples [gandalf] Fellowship event, cost 1): "**Spell**. Spot a [gandalf] Wizard to search a Shadow player's discard pile and choose a minion, then return that minion to its owner's hand. If you do, you may reveal that Shadow player's hand and discard a minion found there." Comprehensive Rules 4.2, Section Three, prints the same words.

**What was built.** As soon as the minion was back in the Shadow player's hand, that hand was shown to the Free Peoples player and he had to discard one minion from it. He was never asked, and could not decline. The Java card before your conversion of 2022-12-13 (80f1cdbf63) did the same.

**What is built now (F-0485).** After the return he is asked "Would you like to reveal the Shadow player's hand and discard a minion found there?". Yes shows the hand and has him discard exactly one minion of it (one "may" governs both, so after a yes the discard is not optional). No leaves the hand unshown and the returned minion in it. With no minion returned nothing is asked, as before. In the definition the reveal and the discard under the `If` stand inside one `optional`; nothing else changed. The erratum 67_15 is not touched: its own text has no "may" ("If you do, reveal that hand and discard a revealed minion").

**Why it is put to you.** The rule it rests on is yours ("You may" is asked, Q-0021), but your own test of 2022-12-13, `ANewLightSearchesRevealsAndDiscards`, read the hand as shown at once with no question. Its header gave the text as "Reveal that Shadow player's hand and discard a minion from his or her hand", which has no "may" and is not the printed card. That reading is dropped. Four tests of other files that play this card as a helper now answer yes before the hand is dismissed (two in Card_94_030_Tests, two in mechanics/BarOnTakingCardsIntoHandTests).

- **A. Keep.** The player is asked, as the card says. F-0485 stands as fixed.
- **B. Strike.** The hand is shown and a minion discarded unasked, as Gemp has always played it: the `optional` comes out, the three tests that answer the question lose that answer and the test of a no (`DecliningToRevealLeavesTheReturnedMinionInHandAndShowsNothing`) goes, the four helper lines come back out, and F-0485 is closed not-a-bug. The card then departs from its text for the sake of one click fewer.

Recommendation: A. The case for B is that a player nearly always says yes; the case against is that the no is real (he may want the returned minion to stay in that hand, or not want to learn the hand in a game he is being watched in), and the text gives it to him. No test is parked on this.

**Ketura (2026-10-12):** A, but it should be a 0-1 choice so they could decide to not discard (I think the two "mays" are separate, plus there are cases where not discarding is preferred strategically).


### Q-0279 What a card's tests may take as proven (your note on Q-0261): five parts, each yes or no. Write the twilight and stats mechanics classes, strip the 179 landed tests that only play a card, and have lint guard the play

Engine pass 28, `trust`: the "good look" you asked for on Q-0261. Nothing is changed yet; each part below is done only on your yes. The whole of it, with the tables and the card-by-card lists, is the report `orchestrator/trials/trust.md` (Reports tab). Counts are over the 764 cards landed when the pass was queued.

**What the look found.**

- The stats tests read, by card: title and type (764); subtitle and twilight cost (670); uniqueness (651, and two or three dots on 23); side and culture (618); strength (279); vitality (256); race (227); site number (150); resistance (104); signet (59); item class (55); a site's block (52) and arrow (50); an ally's home (26); and each keyword with its number.
- **Only the Play clause is re-litigated as whole tests.** 181 landed tests claim the Play clause and nothing else. 128 read the play and nothing more. 53 read something more: the hand a second way (17), a printed strength or vitality in play (8), other characters as not bearing the card (7), the card's own text at rest, such as no token yet or not hindered yet (24), a third copy of a two-dot card (1), nothing (2). Of the 24, 19 have the same reading again in the test of the clause it belongs to; 3 have a reading that would have to move there first (Vile Tentacle 101_36, Bladetusk Charger 103_41, Bladetusk Rearguard 103_43); and 2 are not Play tests at all but the nearest miss of the card's own discount (Uruk Trooper 4_199, Uruk Veteran 4_201) and stay.
- **No test re-litigates a keyword, and none a character being assignable.** All 262 keyword lines in the ledgers are claimed by the stats test alone (but Robin Smallburrow 68_111's Aid, which the Standard asks for). Beyond the Play clause the whole of it is: the two-dot limit re-read on Savage Fury 103_39 and Inner Gate 103_25, and the item class rule on Naith Armaments 102_10.
- **The proof is what is missing.** Every keyword with a rule has its mechanics class (fierce is thin: 2 tests). But no class shows the plain twilight rule (only its edges: roaming, toil, a cost changed, a transfer), none shows where each type of card is played, none strength deciding a skirmish (only overwhelming and damage bonuses), none resistance during play (only the bid), none item classes or the bonus an item prints, none an ally at home in a skirmish.
- **One thing the Play test notices that the stats test cannot:** a definition that carries something about being played that the text does not say (a stray requirement, cost, discount, or trigger on its own play). `gen_play.py` already works that out from the definition, which is how it knows to "leave" a card.

**The parts. Each is yes or no.**

- **A. Write `TwilightPoolTests` (about 32 tests) and `WhereACardIsPlayedTests` (about 10).** The first to your bar, as far as printed cards go: a Free Peoples card of each type adds its cost (9 cases: companion, ally, possession on a bearer and to the support area, artifact, condition to the support area and on a bearer, follower, event); a Shadow card of each type removes it (7; there is no Shadow follower, ally or companion); cost 0 on each side; a Shadow card withheld one token short; a cost lowered and raised by another card, never below 0; a play from a discard pile, a draw deck, a stack and the dead pile; no roaming penalty for a card that is no minion; the pool emptied at the start of a turn; a move adding the Shadow number and 1 a companion. `RoamingTests` (5 tests, complete) stays as it is and is named there. The second class: each type goes to its place, a Support Area card lies on nobody though a bearer is there, each side's cards are offered only in their own phase, an event is discarded.
- **B. Then strip the Play clause.** 179 of the 181 tests go (3 after one reading is moved; the 2 Uruks keep theirs under their discount). `P1` comes off all 467 landed ledgers; the 330 tests that claim it together with a clause of the card's text stay and lose only that claim line. A card's play is from then on tested only where its own text bears on it, by the test of that clause, as now. `gen_play.py` stops writing tests: of the cards not yet worked it would have written one for 1,118, and none of those is written. Saved: 1,297 tests and ledger clauses nobody has to read. Lost: one failing test per card when the twilight rule breaks; about 600 other tests still read the pool in passing and would still fail.
- **C. lint asks what the Play test was the only thing to notice (`plain-play`).** A card that no test plays from hand must be one whose definition and text have nothing that bears on its play, by the generator's own reading; otherwise lint says what does and asks for the test. No test is written for it and it reads every card. I would not take B without C.
- **D. Write the classes for the other stats (about 30 tests).** `SkirmishStrengthTests` (8: more strength wins, a tie is the Shadow's, minions are totalled, zero, a borne bonus counts, an ally at home), `BorneItemsTests` (8: one of each class, any number with none, Naith Armaments as two classes, a mount, a printed strength, vitality and resistance bonus reaching the bearer), `ResistanceTests` (6: burdens against every companion, corruption during play, the unprinted 6, an ally's none), `AllyHomeTests` (4), and one test each for three dots (War-beacon 103_35 and Ominous Sky 103_97 are the only such cards), a fierce minion killed in the first round, and where the killed go. This strips almost nothing (half of Inner Gate's test; Naith Armaments' moves into the class). It is the proof for stats that are already trusted from the stats line with nothing behind them.
- **E. The readings inside tests that stay:** 326 shared Play tests and 273 tests on 105 events read the pool by a printed cost on the way to something else, and 47 tests on 38 items read the printed bonus on the bearer. **E1:** leave them, and new tests stop adding such readings. **E2:** take them out as well.

**Recommendation: A yes, B yes, C yes, D yes, E1.** The smallest thing that does what you asked is A, B and C together: 42 tests written, 179 removed, 1,118 never written. D saves nothing and stands on whether you want the proof; I think you do, since strength and resistance are the stats most cards lean on and nothing shows their rule once. E2 would edit 600 passing tests for no gain and take away the net under B.

Not proposed, and why: the timeword (the phase word is the event's own text, and each event shows it); "may start with the Ring" (deck building); the label keywords, culture, race and signet (the rules attach nothing to them; each card that reads one shows its own filter); the 194 `Rule` clauses that show a rule card by card because each definition writes it for itself.

Until this is answered, batches go on generating Play tests for the cards they land. The list of 179 is `orchestrator/engine-pass-28/trust-files/strip-list.tsv` and is run again on the day.

**Ketura (2026-10-12):** Excellent work. Yes on A-D (tho on A: there is a Shadow follower, only one; a Saruman.  On D: also site bonus reaching the bearer) I think I want E2.  Were this a new initiative or a new app I would think differently, but the sorts of things that these tests are hitting are among the most frequently tested areas of the game, so I'm not worried that it removes a safety net we need.  The thing I do want to improve in this case is how tightly packed our tests are; 600 tests which are doing more than they should (or even existing when they shouldn't) is some bloat I would like to trim; we're already likely to hit 20,000 tests by the end with perfectly legitimate use cases, and I don't want to inflate that number with pointless tests.


### Q-0280 Keep or strike: the F-0468 revert reaches the [gollum] culture as well as Morannon Plains: Trusted Promise 18_37, Fishing Boat 13_48, Out of All Knowledge 13_54 and Enemy in Your Midst 18_30 may again reinforce the other player's [gollum] card

Engine pass 28, `reverts`. Already done; it stays unless you strike it.

**What you asked for, and it is done.** On F-0468 you said "for now let's revert this and permit Morannon Plains to let either player choose either player's cards". The owner filter is out of `ReinforceTokens` again, and `mechanics/CultureTokensTests.AtMorannonPlainsEachPlayerMayReinforceATokenOnACardOfEitherPlayers` shows it on Morannon Plains 18_137: each player is offered all four cards that have a culture token, the Free Peoples player puts his on the Shadow player's Ships of Great Draught and the Shadow player his on the Free Peoples player's Heirs of Gondor.

**What the revert does beside that.** F-0468's write-up said Morannon Plains was the only card the filter had changed: every other card names a culture, and the only active cards with that culture's tokens are the reinforcing player's own. That is wrong for one culture. [gollum] is the one culture with cards on both sides, and it has token cards on both:

- Larder 8_23 (Shadow possession): "Regroup: Discard an Orc from hand to add a [gollum] token here. Skirmish: Remove a [gollum] token here to make Shelob strength +3 (limit +6)."
- Gladden Homestead 13_49 (Free Peoples condition): "When you play this, add a [gollum] token here for each [gollum] card you can spot. Maneuver: Discard this from play or remove 2 tokens from here to ..."
- Nice Fish 15_46 (Free Peoples condition): "Each time Sméagol wins a skirmish, place a [gollum] token here. Regroup: Heal a Ring-bound companion for each token here. Discard this condition."

So with the filter out, four cards again choose among both players' [gollum] cards, as they did before engine pass 26:

- Trusted Promise 18_37 (Free Peoples event): "Reinforce a [gollum] token. Each Shadow player may draw a card."
- Fishing Boat 13_48 (Free Peoples possession): "When you play Fishing Boat, you may reinforce a [gollum] token."
- Out of All Knowledge 13_54 (Free Peoples event): "Spot Sméagol or exert Gollum to make Sméagol strength +2 and you may reinforce a [gollum] token."
- Enemy in Your Midst 18_30 (Shadow event): "Heal a Ring-bound companion to reinforce a [gollum] token."

Probed on two of them: with the Free Peoples player's Gladden Homestead and the Shadow player's Larder both holding tokens, Trusted Promise offers its player both cards, and Enemy in Your Midst offers the Shadow player both. The other 44 uses of the effect name a one-sided culture (or "a Free Peoples token", Gandalf 17_17) and cannot reach another player's card in a two-player game.

**Why it is put to you.** Your words named Morannon Plains, and the write-up you were answering told you it was the only one. These four are also the plainest case for the council: a reinforcement that names a culture, where "a card that already has a token of the same culture" and "any of your cards" give different answers in a real game (a Free Peoples player feeding the Larder is unlikely; a Shadow player adding a heal to Nice Fish, or a Free Peoples player choosing between his Homestead and nothing else, is the same question).

- **A. Keep.** Either player's card, for every reinforcement, until the council answers. Nothing more to do.
- **B. Strike for a named culture.** A reinforcement that names a culture is held to the reinforcing player's own cards (the owner filter back in `ReinforceTokens` only where the definition gives a `culture`), and Morannon Plains, which names none, stays open to either player's. One line in the engine, and a test on Trusted Promise with a Larder across the table.

Recommendation: A. Your reason for the revert (a player may want a token on an opponent's card) is not about Morannon Plains in particular, and one rule everywhere is easier to turn round when the council answers than two. None of the seven cards has been worked by the campaign yet, and no test is parked on this.

**Ketura (2026-10-12):** A, yeah.  Gollum is just weird like that, and it's a weirdness that many of its cards lean into, so I'm fine with it having such unorthodox interactions.


### Q-0281 Keep or strike: Light the Beacons 7_43 played from exactly four cards, itself among them, now exerts Gandalf three times and not twice: the event is not counted while its cost is judged or paid (beyond the words of F-0464: A)

Engine pass 28, item "initiative" (F-0464). Already done; it stays unless you strike it.

Light the Beacons 7_43 (Free Peoples Maneuver event, [gandalf], cost 3): "Exert Gandalf three times (or twice if you have initiative) to make all unbound companions of one culture (except [gandalf]) strength +3 until the regroup phase."

Comprehensive Rules 4.2, "playing a card": "Reveal The Card from your hand, and it enters the void (not in your hand, not in your discard pile, not in play). You cannot count The Card for initiative. ... Meet requirements to play The Card. If you are the Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your hand not counting The Card. Checking to see that all costs can be paid is a requirement of playing a card. ... Pay costs to play The Card."

**What you agreed to (F-0464: A).** The `haveInitiative` requirement, asked of a card in the Free Peoples player's hand, leaves that card out of the count. That is built, and the five events (Guarded Fastness 7_234, Stand to Arms 7_120, Where Shall We Go 7_78, Charged Headlong 8_85, So Fair, So Desperate 8_111) now want four cards beside themselves (8_111: three or fewer).

**What that alone did to a sixth card.** Light the Beacons has the same requirement inside its cost, not on its play. It is asked twice: once while the event lies in hand, to judge whether the cost can be paid, and again as the cost is paid, when the event lies in the void from hand, where the engine still counts it toward initiative on purpose (so that the loss of initiative is answered with the play, F-0139). With the event left out in the hand only, Light the Beacons played from exactly four cards, itself one of them, was judged at three exertions and then exerted Gandalf twice: a Gandalf with two exertions left could not play it, and a Gandalf with three paid two. Before this pass it was judged at two and paid two there.

**What is built.** The same reading leaves the card out while it lies in the void from hand as well, for its own requirement only: one more zone in the one new method (`ModifiersLogic.hasInitiativeNotCounting`). Light the Beacons played from exactly four cards, itself among them, now wants three exertions and takes three; from five cards (four beside it) it wants and takes two, as it always did. What reads the loss of initiative (`hasInitiative`, the rule that publishes the change of side) is untouched: the event is still counted there until its play is announced.

**Which cards behave differently.** Light the Beacons 7_43 alone. It is the only definition in which an initiative condition stands inside the cost of a card played from hand (searched: 80 uses of the requirement in 75 definitions). `mechanics/InitiativeTests.ACostThatIsSmallerWithInitiativeIsReadWithoutTheEventThatIsBeingPlayed` shows it: played from five cards Gandalf exerts twice; the second copy, from four, is not offered while he has two wounds, and unwounded he exerts three times.

- **A. Keep.** From exactly four cards with itself among them, Light the Beacons costs three exertions, judged and paid. "You cannot count The Card for initiative" stands in the first step, before requirements and costs.
- **B. Strike the void part only.** The zone test comes out. Light the Beacons from exactly four cards is then offered only where Gandalf can exert three times, and exerts him twice. The last reading of the mechanics test becomes 2 wounds and its comment says so. I would not: it is offered by one count and paid by another.
- **C. The cost counts the event, as Gemp played it (two exertions from exactly four cards), while the five events keep answer A.** Not a strike: the requirement would have to be told whether it stands on the play or inside a cost, which no requirement knows today (a change to how requirements are built, for one card). Say so and it is written up as a proposal.

Recommendation: A. No test is parked on this; nothing else waits on the answer.

**Ketura (2026-10-12):** A is correct


### Q-0282 Into the Caves 17_100: is the minion unable to gain ANY game text keyword until the regroup phase, or only the keywords the player named?

Into the Caves 17_100 ([rohan] condition, support area): "To play, spot 3 [rohan] companions. Assignment: Assign a minion to Frodo to make that minion lose all game text keywords of your choice and unable to gain game text keywords until the regroup phase."

Éowyn, Northwoman 17_96 has the same sentence without "of your choice": "... to make that minion lose all game text keywords and unable to gain game text keywords until the regroup phase."

**Built now.** The game asks "Choose a keyword" from a list of every keyword of the game (fifty, whether the minion has it or not; a first one must always be named), takes that keyword from the minion until the regroup phase, and asks "Would you like to choose more keywords?". A keyword taken so is also kept out until then (Q-0011, as on Harrowdale 11_243), so the player bars a keyword the minion does not have by naming it from the list. A keyword he did not name is neither lost nor barred. The board that tells: an Easterling Axeman 4_224 ("While you can spot 2 burdens, this minion is strength +3 and fierce") with no burden is assigned to Frodo and Easterling alone is named; 2 burdens are added later in that phase; it is strength 9 and fierce.

- **A. "Of your choice" belongs to the first half only.** The player chooses which of its keywords the minion loses; whichever he chooses, it can gain no game text keyword until the regroup phase, as under Éowyn. The Axeman above stays not fierce. No definition word does it: the only bar on gaining is the removal of a named keyword, and `removeAllKeywords` (Éowyn's) would take the unnamed keywords with the rest. So it is engine work, a bar on every gain that leaves the minion the keywords it has and was not made to lose. The choice would then more naturally list the minion's own keywords, and could be a choice of none. The parked test goes live; every active test of the card passes under it.
- **B. As built: "of your choice" governs both halves.** The minion loses, and cannot gain, the keywords named and no others, and naming a keyword it lacks is how a gain is barred. Nothing changes; the parked test is deleted.

Parked on this, in `Card_17_100_Tests`: `AMinionCannotGainAKeywordThatWasNotNamed` (A). The game fails it at its last reading, that the Axeman is not fierce after the 2 burdens. It must be run alone until F-0487 is mended (the keywords named with this card carry over from one use to the next). The card's old test, `IntotheCavesAssignsFrodoToMinionToRemoveKeywords` (Sauron 9_48 loses damage +2 and enduring, which are named, and keeps fierce, which is not), holds under both readings.

Recommendation: A, lightly. It is the plain parse, and the same words on Éowyn bar every keyword. B if the list of all fifty keywords was meant as the way to bar a gain.

**Ketura (2026-10-12):** lol.  Sets 17 and 18 were built in a CAVE with a box of SCRAPS by a single lone employee as Decipher imploded around his ears.  The templating is inconsistent at the best of times.  Let's do A.


### Q-0283 Keep or strike: a Morgul Regiment 7_197 that can no longer be paid for in full when its turn comes (another payment of the same answer having used the exertions) pays nothing and is left over, where it used to take what exertions were left and still not be assigned (beyond the words of the item on F-0425: B)

Engine pass 28, item "fullcost" (F-0425). Already done; it stays unless you strike it.

Morgul Regiment 7_197 ([wraith] Orc minion, twilight 7, strength 15, vitality 4): "For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish."

**What you ruled (F-0425: B), and it is built.** "If you're unable to pay the cost by default, you're not supposed to be able to perform the action at all ... Regiment should be altered such that it does not permit the costs to even begin if they are not fully capable of paying them at the start." A repeated cost is now judged for every repetition: with fewer exertions left among the companions than Nazgûl, the Regiment is not the Free Peoples player's to assign (it is off his list), nobody exerts, and the Shadow player assigns it for nothing as a leftover minion. Two companions with one exertion each do pay for two Nazgûl.

**The one place that judging does not reach.** At his own assigning the Free Peoples player answers once for all his minions, and the costs are then paid one charged minion after another, in the order of the minions' card ids (engine pass 26; you kept this on Q-0247). Each minion is judged by itself when he answers. So he may put two charged minions in one answer where each could be paid for alone and both together cannot: two Regiments beside two Nazgûl with three exertions left, or a Morgul Spawn and a Regiment with two left. The first is paid in full. When the second one's turn came it could no longer be paid in full, and before this pass it began all the same: a companion exerted once for it, the second exertion could not be made, and it was not assigned. That is the exertion paid for nothing of F-0425, on a rarer board.

**What is built.** A repeated cost that cannot be paid in full when its turn to be paid comes is not begun: nothing is exerted for it, the log says "<player> did not pay to assign Morgul Regiment", and it is a leftover minion for the Shadow player, who assigns it for nothing. This is the same judging asked once more as the payment starts (`Repeat`, in the definition language's code), so it holds wherever a repeated cost is paid, not only at his own assigning.

**Which cards behave differently.** Morgul Regiment 7_197 alone: it is the only definition with a `repeat` among its costs (searched: 84 uses of `repeat`, 83 of them among effects). Shown by `Card_07_197_Tests.ARegimentWhoseTwoExertionsCanNoLongerBothBePaidWhenItsTurnComesIsNotBegunAndIsLeftOver`: two Regiments, two Nazgûl, Frodo exhausted and Aragorn unwounded (three exertions). He assigns both; Aragorn exerts twice for the first; the second, wanting two with one left, pays nothing (Aragorn has 2 wounds, not 3) and goes to the Shadow player.

- **A. Keep.** A Regiment that can no longer be paid for in full pays nothing and is left over.
- **B. Strike.** The four lines in `Repeat` come out. In that one-answer corner the second Regiment again takes whatever exertions are left and is still not assigned; the test above would read 3 wounds on Aragorn. Everything else of F-0425 stands (what he may assign is still judged for every repetition). I would not: it is the fault you ruled on.
- **C. Judge the whole answer at once** (refuse an answer whose costs cannot all be paid together). Not a strike: it changes what Q-0247 kept, the payment of each cost as its turn comes, and reaches Morgul Spawn 7_200, Morgul Spearman 7_201 and Úlairë Nertëa 69_38 as well. Say so and it is written up as a proposal.

Recommendation: A. No test is parked on this; nothing else waits on the answer.

**Ketura (2026-10-12):** Can we make this be an optional trigger that happens when Freeps starts assignments?  Ideally something that makes Regiment highlight itself for Freeps to click on or pass (so that 2 Regiments in play are both highlighted and both clickable in any order, where I might click on the first, then select my exerters, then be presented again with the pending second for pass or selection (if vitality permits)).  Then if any Regiments aren't selected, they are barred from normal assignments.  This solves the UX of not knowing which one is asking and also manages the timing a little better rather than waiting for a cost-inspection pileup when all assignments are submitted. Keep Him Blind To All Else That Moves does something similar but with special abilities assigned to the other player.

**Ketura (2026-10-12):** 1- Yeah they call all get it, I think it's better UX. 2- I suppose it...god, so twisty.  I guess maybe extraCostToAssign and this trigger would need to be able to affect the same self-flag, indicating that it has been paid for? So that assignment actions trip the extra cost, set the flag, and then the at-assignment trigger also asks and potentially sets the flag, without the two tripping on each other.  A pair of results hitting the same inner fields.  Thoughts?


### Q-0284 The Faithful Stone 18_50: is the Shadow player asked whether to remove (2) BEFORE the minion is named (as built), or after, knowing which (as the erratum 68_50 asks)?

The Faithful Stone 18_50 ([gondor] condition, support area, not unique): "<b>Maneuver:</b> Remove 3 [gondor] tokens from here to spot a minion. That minion cannot be assigned to a skirmish until the regroup phase. Any Shadow player may remove (2) to prevent this."

**Built now.** The three tokens are removed; the Shadow player is then asked "Would you like to remove 2 twilight to prevent effect of The Faithful Stone?", and only on a no does the Free Peoples player choose the minion. On a yes no minion is ever named. The PC erratum 68_50, whose two sentences here are these word for word, is built the other way round: the Free Peoples player chooses the minion ("Choose a minion to prevent from skirmishing until the Regroup phase"), and the Shadow player is then asked "Would you like to remove (2) to allow <that minion> to skirmish?". Prized Lagan 68_110 (the erratum: "Add a threat to spot a minion. That minion loses fierce and cannot gain fierce until the regroup phase. Any Shadow player may remove (2) to prevent this") asks after the choice as well. So the two versions of this one card ask in opposite orders. I searched every definition: no other card has a text of this shape (the minion named by a sentence BEFORE the one that is prevented), so the answer touches 18_50 alone.

- **A. As built: the Shadow player is asked before the minion is named.** "This" is everything the tokens bought, the spot with it, and he removes (2) or not without knowing which minion it would have been. Wielder of the Flame 2_28 ("Make a companion defender +1 until the regroup phase. Any Shadow player may remove (3) to prevent this") is built so and landed as built, its one sentence giving no order. Nothing changes; the parked test is deleted.
- **B. The text's order, and the erratum's: the minion is spotted first.** The spot is an effect of its own (Q-0085) and stands before the sentence that is prevented, and effects go in the order printed (Q-0080); "this" is "that minion cannot be assigned". The Shadow player answers knowing which minion, as on 68_50, which is worth something to him with several minions out: he pays only when the one that matters is named. The definition takes the erratum's lines (a choice of the minion ahead of the preventable, the bar laid on the minion remembered); no engine work. The parked test goes live, and three active tests that answer the prevention first are driven the other way round and read what they read now (`ManeuverActionRemovesThreeTokensFromHereAndTheChosenMinionCannotBeAssignedByAnyone`, `TheMinionCannotBeAssignedInTheFierceAssignmentsEitherAndCanBeOnceTheRegroupPhaseHasBegun`, `TheShadowPlayerMayRemoveTwoToPreventItAndTheTokensStaySpent`).

Parked on this, in `Card_18_050_Tests`: `TheMinionIsSpottedBeforeTheShadowPlayerIsAskedToPrevent` (B). The game fails it at "the Shadow player is not yet asked", read before any minion is chosen; against the erratum's lines it passes (run by the author and again by the reviewer). The two boards that do not turn on the order pass either way: a Shadow player with (1) is not asked, and with no minion left to spot the tokens are spent and he is not asked (Q-0114). None of the card's seven old tests turns on it: each had one minion on the table, so the choice was never put.

Recommendation: B. The text names the minion in the sentence before, the erratum built on the same two sentences asks in that order, and A has the Shadow player buy off a bar on a minion nobody has named.

**Ketura (2026-10-12):** B


### F-0430 Old Differences 2_69: you asked for F-0430 to be reverted; it did not change the "-3 if you spot an Elf and a Dwarf" half, only who may be chosen. Keep it, or revert all the same?

Old Differences 2_69: "**Skirmish:** Make an Elf or Dwarf skirmishing a [moria] Orc strength -1 (or -3 if you spot an Elf and a Dwarf)."

You asked for F-0430 to be reverted: "read the text again: "(or -3 if you spot an Elf and a Dwarf)". That is purely about seeing both on the table". I have NOT reverted it yet, because I do not think the correction is what you took it for, and I would rather you said so again knowing what it is.

**The parenthesis was not touched.** Before and after, -3 is a plain spot of an Elf and of a Dwarf anywhere on the table (`canSpot Elf`, `canSpot Dwarf`); neither has to be in the skirmish. The test of the correction shows exactly your reading: Arwen is skirmishing a Goblin Runner, Gimli is on the table fighting somebody else, and she is strength -3.

**What changed is the first half: who may be chosen.** The definition's choice was `choose(or(Elf,Dwarf))`: ANY Elf or Dwarf in play, whoever he was skirmishing and whether he was in a skirmish at all (Gimli fighting an Uruk Savage could be made strength -3 by a [moria] event). It is now `choose(or(Elf,Dwarf),inSkirmishAgainst(culture(moria),orc))`: "an Elf or Dwarf skirmishing a [moria] Orc", as the text's first words have it.

- **Keep** the correction (my reading of the text). F-0430 is then approved with the rest.
- **Revert** all the same: the choice goes back to any Elf or Dwarf in play, the lone test is deleted, and the finding is not a bug. Engine pass 28's `reverts` item has the words for it and does it if you say so.

**Ketura (2026-10-11):** oh!  My apologies, I completely misread what F-0430 was about.  Wasn't quite fully awake yet I suppose; good show catching me.  Yes, that is a good solid bug fix, keep the solution.


### Q-0244 Keep or strike: a follower's Aid is flagged no-effect whenever nobody can be given the follower, which takes in a follower already borne by its owner's only character in a second maneuver phase (a step past F-0363's Streets of Bree 18_140)

Engine pass 26, item `aidflag` (F-0363, your B with its rider). The rule, CR 4.2 "aid": "At the start of the maneuver phase, you may pay the aid cost to transfer that follower to your character. ... The follower is borne by the character for the rest of the turn, no matter how often the fellowship moves. You don't have to pay the aid cost again for a follower borne by a character in a subsequent maneuver phase."

**What the item asked for, and is built.** At Streets of Bree 18_140 ("followers cannot be transferred to a character") a follower's Aid is still offered and its cost paid; the offer carries the no-effect flag (`"effect"`); nobody is asked for a character; the follower stays put.

**What went one step past the item's words.** `FollowerRule` now has one reading, "is there a character of the owner's this follower could be given to now", and both the flag and the question of a character go by it. That reading is also false in one case that has nothing to do with a bar on transfers: a second maneuver phase in the same turn, when the follower is already borne by its owner's ONLY character. Seen with Pallando 13_37 on a lone Frodo: the Aid is offered again (as it always was), and there is nobody else to give him to. Before this pass that offer looked like any other, and a yes paid (2) and moved nothing, with nobody asked. Now the same offer is flagged `"effect"`; a yes still pays (2) and moves nothing. With a second character in play (Frodo and Gandalf, Pallando on Gandalf) nothing is flagged, as before.

So the only thing that differs from before is the flag on that one offer; what a yes does is unchanged. I took your words on F-0363 ("marked as noeffect when their aid is offered, so that the UI can indicate it won't do anything") to hold for any Aid that will move nothing, whatever the reason. The test is `mechanics/AidTests.AFollowerBorneByItsOwnersOnlyCharacterHasNobodyToGoToAndItsAidIsFlaggedAndMovesNothing`.

- **Keep.** Nothing to do.
- **Strike** (if the flag is meant for a follower that something says cannot be transferred, and for nothing else). The flag's judge in `FollowerRule` asks `canBeTransferred` alone, one line; that test's second flag reading becomes `"false"`; the line on it in pitfalls ("A follower's Aid") goes. Streets of Bree is untouched either way.

The change is in and stays unless you strike it.

**Ketura (2026-10-11):** keep


### Q-0245 Band of Wild Men 4_4: is "you may make it fierce until the regroup phase" still offered at a second win, when the Band is fierce already until that regroup phase? (Q-0034)

Band of Wild Men 4_4: "Each time this minion wins a skirmish, you may make it **fierce** until the regroup phase."

**Built now.** The offer is made after every win. A Band that took it after its first win and then wins its fierce skirmish as well is offered it again: it is fierce already, until the same regroup phase, so a yes changes nothing. The trigger has no cost. The engine's rule of Q-0224 withholds a keyword "given to nobody"; it was not taught a keyword given to a card that has it.

- **A. Offered at every win (as built).** "Each time" is each time, and the game does not ask how long the fierce it already has will last. Nothing changes: the active test `WinningASkirmishOffersToMakeItFierceAndItFightsAgainAndIsFierceNoLongerInTheRegroupPhase` reads the second offer and declines it.
- **B. Not offered when a yes would change nothing (Q-0034 to its letter).** No requirement can say it: "not fierce already" is wrong under Hillman Horde 4_22 ("While you control 3 sites, each of your [dunland] Men is fierce"), where the Band is fierce when it wins and its own "until the regroup phase" still matters if a site is liberated before the fierce round. So B is one more kind for the engine's rule: a keyword that does not add up, given until a time, to a card that has it from something lasting at least as long. That one line of the active test then reads the offer absent. Of the cards built alike only this one is touched: Dunlending Madman 4_12, Dunlending Robber 4_16 and Living Off Rock 4_27 also give strength +2, so their second offer always does something, and an ability with a cost (Uruk Fighter 1_146) is offered whatever it does.

No test is parked: the game passes the test of A.

Recommendation: A. The case is a second win, in the fierce round, by a Band already made fierce; it costs one click, and B's rule would be written for this one card.

**Ketura (2026-10-11):** The fact that the fierce skirmish is already over is a bit beyond what we should consider for whether or not it's offered; various effects care about whether a minion is fierce or not, and thus the effect still has potential to alter that calculus, even if the primary loaded behavior won't.  A.


### F-0424 Axe of Erebor 4_41 and More to My Liking 5_9: may "a card stacked on a [dwarven] condition" be a card stacked on the OTHER player's own [dwarven] condition, which is not active on your turn?

Axe of Erebor 4_41: "**Skirmish:** Discard a [dwarven] condition or a card stacked on a [dwarven] condition to make Gimli strength +1." More to My Liking 5_9 pays the same cost by the same words: "Spot a Dwarf and discard a card stacked on a [dwarven] condition to take a Free Peoples card stacked here into hand." Neither says "your".

**The doubt.** In a mirror match the other player's own [dwarven] condition (his More to My Liking, his Here Is Good Rock) lies in his support area on your turn with cards stacked on it, and is not active. May your Axe discard a card stacked on it?

**Built now: neither yes nor no.** When the game judges whether the cost can be paid it counts a card stacked on ANY [dwarven] condition, his included (`CardResolver.resolveStackedCards` reads every card in the game). When the card is to be chosen it looks only under active conditions (`ChooseStackedCardsEffect`). So with his the only stacked card the Axe's ability is offered, and used it discards nothing and Gimli gains nothing; with a [dwarven] condition of your own beside it you are asked "Choose action to perform" between the two alternatives, and "stacked" does nothing. His stacked card is never offered to be chosen.

Your own test of this, `SkirmishAbilityCanDiscardACardStackedOnEitherPlayersDwarvenCondition` ("Regression for #1001: cards stacked on the opponent's [dwarven] condition were never offered, because the stacked-card chooser only looked at hosts that are currently active. Stacked cards are inactive regardless of their host, so the host's activity is not the right gate"; commit "Myriad card fixes", 2026-09-14), asserts A. You switched it off the same day ("Ignoring semi-obsolete test"); it still fails, at the choice of the stacked card.

- **A. A card stacked on either player's [dwarven] condition.** A stacked card is inactive wherever it lies (CR 4.2 "stack"), so whether its host is active does not decide. The engine's change is in the chooser: it looks under hosts active or not. Your old test goes live as written and the two tests of B are deleted. In a mirror match the Axe and More to My Liking then eat the other player's stacks.
- **B. Only a card stacked on a [dwarven] condition of your own.** CR 4.2 "active": "Inactive cards are not affected by the game and do not affect the game", so his condition is no "[dwarven] condition" for your card to find a stack on (as his culture tokens are not yours to remove). The engine's change is in what is judged payable: stacked cards under active hosts only. Then the ability is not offered when his is the only stacked card, and no choice between the alternatives is asked when only a condition can be discarded. The two tests of B go live and your old test is deleted. (Until the engine is changed, `on: your,culture(dwarven),condition` in the two definitions gives B for these two cards; "your" is no word of either text, so it was not written.)

Parked on this number, in `Card_04_041_Tests`: the old test (A); `IsNotOfferedWhenTheOnlyStackedCardIsOnTheOtherPlayersDwarvenCondition` and `WithADwarvenConditionOfHisOwnTheStackedAlternativeIsNotAskedWhenTheOnlyStackedCardIsOnTheOtherPlayersCondition` (B). The game fails all three today, so the fault stands whichever you choose; the answer says which way the engine is brought into line.

Recommendation: B, by CR 4.2 "active". Nothing was changed, because your old test says you once meant A.

**Ketura (2026-10-11):** Bleh.  Decipher really needed to tighten up their definitions of when stacked cards could be interacted with and they never did.  Let's do B for now.


### Q-0246 Keep or strike: Saruman's Staff 4_174's Maneuver ability is no longer offered on the two Sarumans Legacy Ruling 6 names, where it has no effect (F-0426; Q-0034)

Saruman's Staff 4_174: "<b>Maneuver:</b> Make the first sentence of Saruman's game text not apply until the regroup phase." Legacy Ruling 6: "The ability on Saruman's Staff does not have any effect when used on Saruman, Of Many Colours or Saruman, Agent of the Dark Lord"; its opinion adds that "the ability is an optional one that does not need to be activated if it is not beneficial to the Shadow player".

**Built until this landing.** With the Staff on 12_54 or 12_144 the ability was offered once in every maneuver phase (its one requirement was that the sentence is not already off), and used it changed nothing on him: 12_54 kept damage +1, fierce and lurker.

**Built now (F-0426, fixed at the landing of dec-b4_78).** It is not offered while the Staff is on either of those two. The ability has no cost, and by Q-0034 an offer that would do nothing is not made. The engine's own rule (Q-0224) cannot say it, the effect being a table-wide flag that only other cards' definitions read, so the Staff's definition names the two by subtitle: `cantSpot bearer,or(subtitle(Of Many Colours),subtitle(Agent of the Dark Lord))`. On every other Saruman the ability is as it was; a Saruman made later whose text opens with keywords would be offered it until his subtitle is added.

- **Keep.** Q-0034 to its letter. Nothing to do. `IsNotOfferedOnSarumanOfManyColoursWhereItWouldHaveNoEffect` and `IsNotOfferedOnSarumanAgentOfTheDarkLordWhereItWouldHaveNoEffect` in Card_04_174_Tests hold it, one for each Saruman.
- **Strike** (if LR 6's "when used on" means the ability may be used there, for nothing; or if a definition should not carry two cards' names for one click a phase). The second requirement comes out, the two tests become one that takes the action on 12_54 and reads his keywords unchanged, and F-0426 is not a bug.

One thing changes besides the click. The flag is the one the Hobbit-set Gandalfs 30_25 to 30_28 read (F-0037, which you have for the Hobbit sets' turn), so from those two Sarumans the Staff used to switch off those Gandalfs' first ability; it no longer can. From any other Saruman it still does, and a test in this file is parked on F-0037 for it.

No test is parked on this. The change is in and stays unless you strike it. Recommendation: keep.

**Ketura (2026-10-11):** Keep.  This is as good as any mechanism for communicating the results of that ruling.


### Q-0247 Keep or strike: three things built beyond the approved design of the cost of assigning (Q-0221: A; Nertëa 69_38, Morgul Regiment 7_197, Morgul Spawn 7_200, Morgul Spearman 7_201)

Engine pass 26, item `assigncost` (Q-0221: A, with Q-0199). What you approved is built: nothing is asked ahead; the Free Peoples player pays as he makes the assignment (at his own assigning, or once a card has him choose the pair); a cost not paid in full means that one assignment is not made; while the cost cannot be paid the assignment is not his to make; the Shadow player's assignments are never charged. Úlairë Nertëa 69_38 and the three Morgul Orcs (7_197, 7_200, 7_201) are on that one build. The report is `orchestrator/engine-pass-26/assigncost.md`.

**Three things the agent decided beyond the approved design** (it judged them part of building it and filed nothing; I am putting them to you because they are choices). None changes what any card did before this item.

1. **The payment's source is the card that charges** (Morgul Spawn, Nertëa), not the card or action that is making the assignment. So a companion exerted for Morgul Spawn under Éowyn 17_96's action is "exerted by a Shadow card", and Nertëa's burden is "added by a Shadow card", which is what lets Melilot Brandybuck 3_110 prevent it. The design had it the other way (part of Éowyn's action).
2. **An assignment already paid for is not asked to pay again** when the game re-checks the pair as it makes it (a companion's last exertion pays, and the paid-for assignment stands). This touched `AssignmentEffect`, which the design said would be left alone. Held by `CostOfAssigningTests.ACompanionsLastExertionPaysAndTheAssignmentStands`.
3. **A card that leaves play or is hindered while a payment is being answered takes no part** in the assignments then made (the minion of a character so lost is a leftover minion for the Shadow player). NO TEST: it wants a card that removes or hinders a character in answer to an exertion or a burden, and none was looked for.

One more thing changes for players besides what you approved: with a Morgul Orc the choice of who exerts has no "none", so a Free Peoples player who drags one by mistake has no way back (before, the yes/no ahead could be answered no).

- **Keep** all three. Nothing to do. (By your notes of 2026-10-11 the third then stays without a test unless a real card can make the board.)
- **Strike** one by its number: 1 goes back to the design (the payment is part of the assigning action; Melilot then cannot prevent Nertëa's burden under Éowyn); 2 cannot simply be struck (without it the paid-for assignment is refused), so striking it means another way to the same end; 3 means the assignments are made as answered, whatever left play meanwhile.

Recommendation: keep. The first is the one with a rules reading in it, and it is the reading the old required trigger had.

**Ketura (2026-10-11):** Keep.  Well done on maneuvering that one.


### F-0425 Morgul Regiment 7_197: is a cost repeated "for each Nazgûl" payable when only one of its exertions can be made?

Morgul Regiment 7_197: "For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish."

**Built now** (and before engine pass 26 as well; found there, item `assigncost`, and left alone). The Regiment is the Free Peoples player's to assign while ONE companion can exert, however many Nazgûl there are. With two Nazgûl and one exertion left among the companions (the Ring-bearer alone, with 2 wounds) he assigns it, the Ring-bearer exerts once, the second exertion cannot be made, and the Regiment is not assigned: he has paid an exertion for nothing and it goes to the Shadow player as a leftover minion.

**Why.** The cost is written as a repeat of "exert a companion", and the game judges a repeated cost payable when it can be done ONCE, not as many times as it will be repeated.

- **A. Leave it.** The player can count his own exertions; a cost he starts and cannot finish is his mistake. Nothing changes.
- **B. A repeated cost is payable only if every repetition can be paid.** With two Nazgûl and one exertion left the Regiment is then not his to assign at all (the Shadow player assigns it), and nothing is exerted. The change is in the engine's judging of a repeat among costs, so it reaches every definition that has one: an engine item would first list them and say which behave differently, and I have not counted them.

Not parked: 7_197 goes back into the cards' queue either way, and its author tests the game as built.

Recommendation: B, on the general rule that a cost is paid in full or the action is not taken; but its reach is unknown until the definitions are listed, so the item's first step would be that list, shown to you before anything is changed.

**Ketura (2026-10-11):** Interruptions by the opponent are one thing, but if you're unable to pay the cost by default, you're not supposed to be able to perform the action at all.  "If a card or special ability has a cost, you must pay that cost or you cannot use that card or special ability."  Also, `playing a card`, step 2: "Checking to see that all costs can be paid is a requirement of playing a card. If you cannot meet all requirements, The Card returns to your hand. If The Card returns to hand, then you do not lose initiative and skip the remaining steps."  (Obviously Gemp doesn't have such mechanisms because we look *forward* at whether actions can be performed, not *backward* and undoing unplayable actions, but the principle is the same.) Thus, Regiment should be altered such that it does not permit the costs to even begin if they are not fully capable of paying them at the start.  B.


### Q-0248 Aragorn, Wingfoot 4_364, and every "for each X you spot": is the player asked how many to spot (as built; CR 4.2 "spot"), or is every one spotted unasked (your words on Q-0141)?

Aragorn, Wingfoot 4_364: "Each time the fellowship moves, you may wound a minion for each unbound Hobbit you spot."

**Built now.** On a yes the player is asked for a number, from 0 to the unbound Hobbits there are, and then wounds that many times, choosing a minion for each wound. Every Decipher card that counts what "you spot" is built so (`chooseHowManyToSpot`, 46 definitions: the pipes, Forces of Mordor 1_248, the original Merry's Sword 7_242), and `mechanics/ChoosingHowManyToSpotTests` holds it as the rule. CR 4.2 "spot": "If a card says, 'for each Elf you spot' and there are 2 Elves in play (and active), you may choose to spot 2 Elves, 1 Elf, or none."

**The doubt.** On Q-0141 you kept the erratum Merry's Sword 57_242 as built, counting every [rohan] companion unasked: "I can't imagine why one would want some exertions but not the max." The campaign's log wrote that down as a rule for the shape ("a 'for each X you spot' count spots all without asking"), and on this card the game, and your own old tests of it, go the other way. Were your words for that one card, or for every "for each X you spot"?

- **A. The player names the number (as built, and the rule's own example).** Nothing changes in the game. The log's line on Q-0141 is narrowed to Merry's Sword 57_242 and the parked test is deleted. On this card a smaller number has a use: with two unbound Hobbits, a minion on its last vitality and a minion he would rather not wound, he spots one.
- **B. Every one is spotted, unasked.** One prompt fewer at each use. Here the number prompt comes out and the wounds are counted from the table; `EveryUnboundHobbitIsSpottedWithoutBeingAskedHowMany` goes live and the five active tests that answer the number lose that line. He must then place every wound (each is a choice of exactly one minion) unless he declines the whole offer. The other definitions that say "for each X you spot" follow in one sweep (those that say "spot X to ...", where the number is the point, stay), and the mechanics class is rewritten to what is left.

Parked on this, in `Card_04_364_Tests`: `EveryUnboundHobbitIsSpottedWithoutBeingAskedHowMany` (B). The tests of A are active and pass.

Recommendation: A. The rule gives this very wording as its example, and you were told on Q-0141 that the original Merry's Sword asks and left it.

**Ketura (2026-10-11):** Ask, but instead of as a popup, as a 0-N choice of cards to wound.  Wingfoot still has to be clicked to indicate the optional trigger is being used, but then it skips straight to the wounding.


### Q-0249 Keep or strike: Barrows of Edoras 4_337 is now offered to a player with no card in hand, who draws 4; "already used this phase" stands in for the Rule of 4 to keep the use that does nothing off the menu (F-0429)

Barrows of Edoras 4_337: "<b>Sanctuary</b>. <b>Fellowship:</b> Place your hand beneath your draw deck and draw 4 cards."

**Built until this landing.** The action required a card in hand. A player at the Barrows with an empty hand was not offered it and could not draw 4. Your old test used it twice in one fellowship phase and read it gone after the second use ("Psyche! Rule of 4 means you can only really do this once"): no hand left, and nothing more that may be drawn.

**Built now (F-0429, fixed at the landing of dec-b4_201).** The text has no requirement and no "to", so placing the hand is no cost, and CR 4.2 "effect" says "you must perform as much as you can and ignore the rest": with no hand he places nothing and draws 4. The action is now offered with a card in hand, as before, OR while it has not yet been used this phase.

The second half is a stand-in. What should stay off the menu is the use that does nothing (no hand to place, no card that may be drawn), and no requirement in the definition language reads the Rule of 4. One use draws as far as the rule allows, so "used already this phase" says the same thing in every game but two:

- a player with no hand who has already drawn his four from other cards this phase is still offered it, once, and it does nothing (by the requirement as written; not laid as a test);
- a player whose use emptied a short draw deck, who then plays out that hand and has cards put back into his deck in the same phase, is not offered it though he could draw.

The definition carries a comment saying what the limit stands for. Simply taking the old requirement out was tried and does not do: the action is then offered with an empty hand after the Rule of 4 is spent, which your old test's last line says it is not.

- **Keep.** Nothing to do. `WithNoCardInHandTheActionStillDrawsFourCards` holds the new offer, and `ASecondUseInOneFellowshipPhasePlacesTheHandBeneathAndDrawsNothingAndThenTheActionIsGone` still ends on your old last line.
- **Strike** (if "place your hand" was meant to need a hand to place). The requirement goes back to a card in hand, the new test is deleted, and F-0429 is not a bug.

A requirement in the engine that reads whether the player may still draw this phase would replace the stand-in and take both corners away ("a card in hand, or a card may still be drawn"). It is on the orchestrator's list for the next engine pass and wants no answer from you unless you would rather it were not built.

No test is parked on this. The change is in and stays unless you strike it. Recommendation: keep.

**Ketura (2026-10-11):** Keep.  I agree that there should be a requirement that checks to see if anyone is able to draw; this is relevant for Rule of 4 shenanigans but there's also other restrictions.


### Q-0250 Siege Engine 5_60: when an opponent discards machines and other cards at once (Sleep, Caradhras; Fortress Never Fallen), does its response save the cards that are no machines as well?

Siege Engine 5_60: "<b>Response:</b> If one or more machines are about to be discarded by an opponent, discard this condition to prevent that."

CR 4.2, the ruling under Fortress Never Fallen: "The effect of this condition's special ability when the card has more than one token is simultaneous. Several conditions are discarded at the same time. Siege Engine responds to these discards by preventing all of them. *You have three tokens on Fortress Never Fallen when you use its special ability. You select three Shadow conditions to be discarded (including my Siege Engine), and discard Fortress Never Fallen. I use the response special ability on Siege Engine, which technically saves all three conditions, but then I discard Siege Engine to pay its own cost.*"

**Built now.** The response is offered when at least one machine is among the cards an opponent is about to discard, and it saves every machine in that discard and nothing else (`preventDiscard`, `select: all(machine)`). Gandalf plays Sleep, Caradhras against Siege Engine, Devilry of Orthanc, Battering Ram, Scaling Ladder and Machinery of War 102_28 (an [isengard] condition that is no machine): the three other machines stay, Siege Engine goes to pay for it, and Machinery of War is discarded.

**The doubt.** The ruling says "all of them" and "all three conditions", and Fortress Never Fallen discards "a Shadow condition for each [rohan] token": nothing in the example says that the three chosen are machines.

- **A. Only the machines are saved (the card's words, and as built).** "That" is the discard of the machines. The ruling is about the discard being simultaneous: one response answers for every machine in it, and its three conditions are taken to be machines. Nothing changes.
- **B. The whole discard is prevented once one machine is in it.** "All of them" is every card of the one simultaneous discard. The definition's `all(machine)` becomes every card in that discard, the trigger staying as it is. Under Sleep, Caradhras ("discard every condition") one Siege Engine would then keep every other condition on the table, the Free Peoples player's own included. `OneResponseSavesEveryMachineAFreePeoplesEventDiscardsAtOnceAndNoOtherCondition` would read Machinery of War still in the support area; the test of the ruling in Fortress Never Fallen 54_276's file (`SiegeEngineSavesEveryMachineDiscardedWithOneResponse`, which chooses two machines and leaves the Uruk-hai Armory unchosen) and the original 4_276's file would each choose a condition that is no machine and read it saved; and the Card Test Standard's `F` for a response (a card it does not answer for, laid among those it does) would be reworded for a prevention.

No test is parked: the test of A is active and the game passes it. No old test takes either reading (yours laid four machines and nothing else). Recommendation: A. The card says "prevent that" of machines, and under B a Shadow condition would save the Free Peoples player's own conditions from his own Sleep, Caradhras.

**Ketura (2026-10-11):** Hrrm, this is indeed unclear.  Given that the FNF ruling steers clear of specifying "machines" being discarded, I think we are meant to infer that they are non-machines except for Siege Engine itself.  This makes me think that the text is thus meant to be read as: "If one or more machines are about to be discarded by an opponent['s card's action], discard this condition to prevent that [action]." Given that it emphasizes that this is a single simultaneous action, I think it's trying to say that the action is all or nothing.  This would indeed rescue the free people's own conditions, by preventing the blanket simultaneous action rather than negating it for some and not others, which requires splitting up the action into several linked but not simultaneous effects. Let's go with B for this one.


### Q-0251 Eye of Barad-dûr 5_96: when It Burns Us 6_110 has the Shadow player "reveal his or her hand", has a Free Peoples player revealed the Eye?

Eye of Barad-dûr 5_96: "<b>Response:</b> If a Free Peoples player reveals this card from your hand, discard this card to add 2 burdens."

It Burns Us 6_110: "<b>Regroup:</b> Spot Sam and Sméagol (or Gollum) to choose an opponent who must reveal his or her hand. Wound a minion X times, where X is the number of different cultures revealed."

**Built now.** The Eye's response asks whose CARD caused the reveal: it is offered whenever the card that revealed the hand is the Free Peoples player's (`revealedCardFromHand`, `player: fp`, compared with the owner of the card that revealed). The game keeps no record of who is made to reveal: Glamdring's "reveal an opponent's hand" and It Burns Us's "an opponent who must reveal his or her hand" are one and the same effect to it. So after It Burns Us the Shadow player is offered the Eye and may add 2 burdens. It Burns Us is the only printed Free Peoples card worded this way, and the Eye is the only card that answers a reveal from hand, so this pair is the whole of the question.

**The doubt.** On Q-0171 (Deceit 68_29 under Introspection, "choose an opponent who must discard one of his or her conditions") you ruled that the Shadow player is the one discarding: "if the player has a choice, they're not forced". It Burns Us has the same words, "choose an opponent who must ...", but the opponent chooses nothing here: he shows his whole hand. Does that answer carry over to a reveal with no choice in it?

- **A. The Shadow player is the one revealing, and the Eye is not offered.** The text makes the opponent the one who reveals "his or her hand", where Glamdring, Treachery Deeper Than You Know and Meant to Be Alone have the Free Peoples player reveal "an opponent's hand". The Free Peoples player can then play It Burns Us into a hand that may hold an Eye at no risk. This is an engine change and no definition alone does it: the reveal effect would have to say who reveals and the trigger read it, with It Burns Us naming the Shadow player. `WhenAFreePeoplesEventMakesTheShadowPlayerRevealHisHandNothingIsOffered` goes live.
- **B. A reveal that the Free Peoples player's card forces is his reveal, and the Eye is offered (as built).** The Shadow player had no choice in it, which is what your answer on Q-0171 turned on. Nothing changes in the game; the parked test is rewritten to read the offer and the 2 burdens.

Parked on this, in `Card_05_096_Tests`: `WhenAFreePeoplesEventMakesTheShadowPlayerRevealHisHandNothingIsOffered` (A). The game fails it at the offer. No old test takes either reading: the one old test of the response reveals with Glamdring.

Recommendation: A on the words alone (the opponent is the subject of "reveal", as Introspection makes him the subject of "discard"). But your reason on Q-0171 was the choice, and there is none here, so the words and that reason point different ways.

**Ketura (2026-10-11):** Since there's no optionality and no choice, I'd say this is Free Peoples doing the revealing. "Make an opponent X" sometimes seems like redundant, unnecessary verbiage when X doesn't have any wiggle room.  B.


### Q-0252 Keep or strike: with "borne by a <kind>" now leaving a hindered bearer out in the engine (Q-0220: E), the Ring-bearer is still found as the Ring-bearer, and Asfaloth 1_31 waits on a hindered companion underground

Engine pass 26, item `attachedto` (Q-0220: E). **Built now.** One place in the engine (`Filters.attachedTo`) reads "borne by <such a card>". A hindered bearer is no longer found by its kind: card type, race, culture, title, keyword or number, alone or with anything beside it (`attachedTo(Hobbit)`, `attachedTo(companion,inSkirmish)`, `attachedTo(or(ally,and(unbound,companion)))`, `attachedTo(memory(loser),companion)`). A bearer the text holds as a card is found as before: `self`, `bearer`, `memory(...)`, `any` ("each attached follower"). `mechanics/BorneByAHinderedBearerTests` shows both sides on printed cards. That much is the item as you approved it. Two things in it are my reading and not your words:

**1. "The Ring-bearer" is not a kind: `attachedTo(ring-bearer)` still finds a hindered Ring-bearer, as it did before.** He is the Ring-bearer by the rules, not by anything printed on him (CR 5.0 "hinder" itself says "If the Ring-bearer is hindered, burdens may be spotted and counted"; your Q-0186: burdens are "on the Ring-bearer" whatever his state). Three definitions write it:

- Precise Attack 18_101, "discard a Shadow card the Ring-bearer is bearing": with Frodo hindered and Spider Poison on him, the Poison is still offered and discarded (the new test `AShadowCardOnAHinderedRingBearerIsStillACardTheRingBearerIsBearing`).
- Black Breath 1_207, "Burdens and wounds may not be removed from bearer", and Phial of Galadriel 103_1, "play this on the new Ring-bearer": each asks whether it is on the Ring-bearer. Nothing in play turns on his being hindered there (burdens cannot be removed from a hindered Ring-bearer in any case).

- **Keep.** Nothing to do.
- **Strike 1** (a hindered Ring-bearer is not found as "the Ring-bearer" either, as a hindered Frodo is not "Frodo", Q-0103). One word comes off `Filters.ringBearer`; Precise Attack then no longer offers its third choice for a card on a hindered Ring-bearer, and the test above is turned round. The rules' own protections of him (F-0174) do not go through this and are untouched.

**2. Asfaloth 1_31 now stays on a hindered companion at an underground site until he is restored.** "Discard Asfaloth when at an underground site" is built as "while the fellowship is underground and this is `attachedTo(companion)`": the `companion` is there to leave out an ally bearer, who is at his home site (F-0380), and not because the text says "borne by a companion". A hindered Arwen is no companion to find, so with her hindered and the fellowship in Moria the discard waits; it is carried out once she is restored while the fellowship is still at an underground site (probed: the regroup reconcile restores her and Asfaloth goes); if the fellowship moves on to a site that is not underground while she is still hindered, Asfaloth is not discarded at all. Before this change Asfaloth went at once. No test of Asfaloth's lays this board; nothing else about the card changes.

- **Keep.** Nothing to do: it is what "a hindered bearer is not found by its card type" gives.
- **Strike 2** (Asfaloth is at the fellowship's site whatever state its bearer is in, and goes at once). No word in the definition language says "the site this card's bearer is at"; shapes.md already lists that word as the engine's to build for eight other possessions an ally may bear (F-0380). Asfaloth would be the ninth and would be put right with them; until then it stays as built.

For your eye only, no answer wanted: a TITLE is treated as a kind too, by CR 5.0's list ("title, culture, card type ... do not apply") and your Q-0103. It is written in Staff Asunder 2_27 ("a staff Gandalf is bearing"), Blade of Gondor 12_42 ("a [gondor] card borne by him") and Goblin Song 31_33; none of the three plays differently, because each already needed its bearer spotted or exerted.

Both are in and stay unless you strike them. Recommendation: keep both.

**Ketura (2026-10-11):** Hrrm.  1 is fine to keep.  2...let's change the filter to `attachedTo(not(ally))`.  The intent is to wiggle around auto-discarding from an Ally (who is explicitly *not* at the same site as the fellowship unless they are at their home site), rather than anything that needs the ability to spot bearer in a way that hinder should legitimately interfere with.


### Q-0253 Very Nice Friends 7_76: is the hand of "If you do not move again this turn, discard your hand" discarded before the reconcile (as built) or after it, at the end of the turn?

Batch dec-b7_2. Very Nice Friends 7_76 (Free Peoples [gollum] event, Regroup): "Play Sméagol from your discard pile to play the fellowship's next site (replacing an opponent's site if necessary). If you do not move again this turn, discard your hand."

**The doubt.** The text does not say WHEN the hand goes: as the player chooses not to move, before he reconciles, or once the turn is over, after it. CR 4.2 "regroup phase" makes the choice "moving ... or reconciling your hand" and says nothing of the order here; neither rules document names the card. Raised by the author before the definition or the old test was read.

**What is built.** The event adds a trigger of its own kind, `FPDecidedToStay` (this card is its only user), that lasts to the end of that regroup phase. The hand is discarded the moment the Free Peoples player answers no to "another move", and also when the turn ends at the move limit with no question asked. He then reconciles from an empty hand and draws eight. Your old test says so in its name, `StayingDuringRegroupDiscardsHandBeforeReconcile`, and it is kept active with what it read.

- **A. Before the reconcile** (as built). The cost of staying is the cards he was holding; he starts the other player's turn with a fresh hand of eight. Nothing changes: 7_76 stays as landed, 8 tests.
- **B. After the reconcile**, at the end of the turn. He reconciles as usual and then loses the lot, and plays the other player's turn as Shadow with no hand. The definition's trigger moves to the end of the turn, and `StayingDuringRegroupDiscardsHandBeforeReconcile` (renamed) and `AtTheMoveLimitTheHandIsDiscardedThoughNoMoveIsOffered` read a hand of 0 where they read 8, with his draw deck untouched at 9.

No test is parked: the old test takes A and the game passes it, so no test of B was written.

My reading is A: the old test's name states the order on purpose, and the trigger was built for this card alone.

**Ketura (2026-10-11):** Yeah I think it's as soon as Freeps chooses not to stay, so before reconcile as it is.  A.


### F-0464 An event that needs initiative to be played counts itself toward it: mend it to the rules' word, or leave it as Gemp has always played it? (Guarded Fastness 7_234, Stand to Arms 7_120, Where Shall We Go 7_78, Charged Headlong 8_85, So Fair, So Desperate 8_111)

Guarded Fastness 7_234 (Free Peoples Maneuver event): "If you have initiative, discard 2 cards from hand to heal all [rohan] allies." The same shape: Stand to Arms 7_120, Where Shall We Go 7_78, Charged Headlong 8_85; and, asking for the other side, So Fair, So Desperate 8_111 ("If the Shadow has initiative, add a threat to ...").

Comprehensive Rules 4.2, "playing a card": "Reveal The Card from your hand, and it enters the void (not in your hand, not in your discard pile, not in play). You cannot count The Card for initiative. ... Meet requirements to play The Card. If you are the Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your hand not counting The Card."

What the game does: with exactly four cards in hand, the event one of them, Guarded Fastness is offered and can be played (it then discards 2 of the other 3). So Fair, So Desperate held the same way, with three cards beside it, is NOT offered, though by the rule the Shadow has initiative for it. The requirement is asked while the event still lies in the hand, and the hand is counted as it lies.

Why I am asking and have not called it plainly a fault: the engine counts a card played from hand toward initiative on purpose while its costs are paid (`Zone.VOID_FROM_HAND`; the 2013 commit that made the zone says "if a card is played from hand, it still counts for initiative purposes"), which is how the loss of initiative comes to be answered with the play, as you ruled for F-0139 ("Loss of initiative is step 5"). That part is left alone under either answer. The play requirement was never given the same thought that I can find: it is the one place the rule's "not counting The Card" is not kept. An initiative that the event's EFFECT asks for is already read without the event (All Save One 7_31 played from four cards gives +2, not +4), and an ability's requirement is checked before its cost (Madril 7_110), both as the rules say.

- **A (the rules' word):** such an event wants four cards beside itself; "If the Shadow has initiative" wants three or fewer beside itself. Built in an engine pass: the `haveInitiative` requirement, asked of a card in the Free Peoples player's hand, leaves that card out of the count (a new reading beside `ModifiersLogic.hasInitiative`; a handful of lines in three or four classes). The five events above change: the four are no longer offered from exactly four cards, 8_111 is offered from exactly four. The parked test `mechanics/InitiativeTests.AnEventThatNeedsInitiativeToBePlayedIsNotCountedTowardIt` goes live.
- **B (as Gemp has always played it):** the event counts itself. Nothing changes; the parked test is rewritten to assert it, F-0464 is closed not-a-bug, and the pitfall entry says so.

Recommendation: A. The sentence is explicit and written for exactly this case, and the game already keeps it for effects. Nothing has been changed: the test is parked and the finding is open.

**Ketura (2026-10-11):** I agree, A is the right choice.  Not sure why this was altered way back when, but I suspect it was someone reporting a bug that seemed reasonable rather than a rules-based intervention.


### Q-0254 Keep or strike: Above the Battlement 7_262 is no longer offered when neither half can be paid for, and the reading of your 2026-09-14 test that played it there is dropped (F-0465)

Batch dec-b7_80. Already done; it stays unless you strike it.

Above the Battlement 7_262 (Shadow event, cost 0): "Play a besieger stacked on a site you control or remove a burden to play a [sauron] Orc from your discard pile." Comprehensive Rules 4.2, Section Three, names the card: "When you play this card you must choose one of the following actions: (1) play a besieger stacked on a site you control; or (2) remove a burden to play a [Sauron] Orc from your discard pile."

**What was built.** The event had no requirement, so it was offered in every Shadow phase and could be played for nothing: with no besieger stacked on a site of yours and no burden (nothing asked, nothing done); with a burden and no [sauron] Orc in your discard pile (by the notes of engine pass 26 the burden is removed and nothing played); and with the pool one short of the only minion it could play.

**What is built now (F-0465).** A requirement on the event: a besieger stacked on a site you control that the pool can pay for (cost and roaming), OR a burden to remove together with a [sauron] Orc in your discard pile that the pool can pay for. Where neither holds the event is not offered. In the definition: `requires: Or [ Playable fromZone STACKED, besieger,stackedOn(controlledSite) ; And [ CanSpotBurdens ; Playable fromZone DISCARD, culture(sauron),orc ] ]`. Nothing else in the card changed, and no other card's definition or test was touched.

**Why it is put to you.** The rulings it rests on are yours: a play as an effect, from any pile, is a cost ("it should enforce a card being there and playable before playing", Q-0062; "It should block until both can be paid for", F-0046; Q-0086), and a choice of pay-to-do branches with no payable branch is not offered (F-0222, News of Mordor 53_82, approved 2026-10-08). But your own test of 2026-09-14 (af53ef51a8, "Fixed Above the Battlement permitting the discard ability to be used when there were 0 burdens"), `AbovetheBattlementDoesNotPlayOrcFromDiscardWhenThereAreNoBurdensToRemove`, stood on the first of those boards and said so in its comment: "No burdens added, and no besieger stacked on a controlled site: neither branch can actually be paid for." It read `assertTrue(scn.ShadowPlayAvailable(battlements))`, played the event, took the burden branch, and read the Orc still in the discard pile. Its subject was the Orc played for no burden, which your change to RemoveBurdens ended; that the event could be played there at all it read in passing, a month before F-0222. That reading is dropped.

**What stands for your test.** Its board is now `CannotBePlayedWithNoBesiegerOnTheSiteAndNoBurdenToRemove`: the event is NOT offered with no burden, a [sauron] Orc who is no besieger on your site and a [sauron] Orc in your discard pile, and is offered once a burden is added. What it was written for, that no Orc comes out of the discard pile without a burden, is read in `WithNoBurdenToRemoveItPlaysABesiegerStackedOnASiteYouControl` (two [sauron] Orcs lie in the discard pile, are not offered, and stay). Two more tests hold the other two boards: `CannotBePlayedWithABurdenAndNoSauronOrcInTheDiscardPile` (a [sauron] Uruk-hai and a [wraith] Orc lie there) and `CannotBePlayedWhileThePoolIsShortOfTheMinionItWouldPlay` (5 where Gorgoroth Pillager on the site wants 4 + 2; 3 where Orc Chieftain in the pile wants 2 + 2).

- **A. Keep.** The event is withheld when neither half can be paid for; the three tests above assert it. F-0465 stands as fixed.
- **B. Strike.** The event may be played for nothing, as Gemp played it: the requirement comes out, the three tests are turned to read it offered (and, on the second board, the burden spent for no Orc), your old test comes back, and F-0465 is closed not-a-bug. That would make this card an exception to F-0222 and Q-0062, and would say the same for Arwen's Fate 1_93, the other card still playable with neither half possible (not yet worked).

Recommendation: A. A Shadow player gains nothing by playing it there, and on the second board loses a burden to a misclick. No test is parked on this.

**Ketura (2026-10-11):** Ah, Above the Battlement.  This just needs an official rules council reading; the damn text is ambiguous as to what the hell it *means*: is it a choice between two actions? [play a besieger from a site] or [remove a burden to play an orc from discard]? or is it [either play from site, or remove a burden] to [play an orc from discard]?  Without knowing which reading is intended, it's impossible to know which way it should behave.  Keep it as it is now, but I'll ask the Council.


### Q-0255 Pelennor Flat 7_345: with no card in hand, may the Free Peoples player still take "discard his or her hand", for nothing (as built), or must he add 2 burdens?

Pelennor Flat 7_345: "<b>Plains</b>. At the start of the maneuver phase, the Free Peoples player must discard his or her hand or add 2 burdens."

**Built now.** The trigger is a choice between discarding the whole hand and adding 2 burdens. With a card in hand he picks one, and the other is not carried out. With NO card in hand he is still asked "Choose action to perform", and "Discard your hand" taken there discards nothing and adds no burden: a player who reaches the Flat with an empty hand passes it for free. (The game counts a discard of the whole hand as something that can be done in full whatever the hand holds. The Q-0224 teaching, that a discard from an empty hand "will do nothing", is read for offers only and not by a choice.)

**The doubt.** CR 4.2 "effect": "If the effect of a card or special ability requires you to choose one of two different actions, you must choose an action that you are fully capable of performing (if possible)." On F-0310 you held that a branch that cannot be done in full is not there to be chosen, and on F-0116 that a branch with nothing to act on is no branch. Neither says which of the two a hand of no cards is: discarded in full (all none of it), or nothing to discard. No ruling of Decipher's names the card, and no other printed card sets "discard your hand" against a second thing to do.

- **A. A hand of no cards cannot be discarded: nothing is asked and 2 burdens are added.** Reaching the Flat empty-handed costs 2 burdens, and the way to keep them off is to hold a card back to discard. In the definition this is an `If` on a card in the Free Peoples player's hand around the first branch (tried: all four tests pass with it); the engine's own way would be for a whole-hand discard to tell a choice, as it already tells an offer, that it will do nothing. The parked test goes live.
- **B. A hand of no cards is discarded in full, for nothing (as built).** A player who has played out his hand before the Flat is asked, takes the discard and pays nothing: he has already given up what the site would take. Nothing changes in the game; the parked test is rewritten to read the question, the first option taken, no burden added and the hand still empty.

Parked on this, in `Card_07_345_Tests`: `WithNoCardInHandTheFreePeoplesPlayerIsNotAskedAndTwoBurdensAreAdded` (A). The game fails it at the question being asked. No old test takes either reading: both had eight cards in hand.

Recommendation: A, only because it is how the game already treats every other branch with nothing to act on (nobody to wound, exert or discard: F-0116); B if "all of an empty hand" counts as done in full, which is how the site has played until now.

**Ketura (2026-10-11):** I'd say this is a difference between zero and null; is discarding an empty hand an action of "discarding 0 cards" or a NPE when trying to locate a hand that doesn't exist? I think the player always has a hand, even if empty.  I'm reminded of the ruling on What Are We Waiting For, which permits Shadow to re-pick skipping the phase even when earlier copies had them picking that already.  I think discarding 0 cards is kosher.  B.


### Q-0256 Blood Runs Chill 8_3: is X the damage bonus the Dwarf had when spotted (as the erratum 58_3 reads it) or the one he has after exerting twice (as built; Gimli, Counter of Foes 8_5 makes it 3)?

Blood Runs Chill 8_3: "Spot a Dwarf who is damage +X and exert that Dwarf twice to make an opponent discard X Shadow cards."

Gimli, Counter of Foes 8_5 (the same set): "<b>Damage +1</b>. For each wound on Gimli, he is strength +1 and <b>damage +1</b>."

**Built now.** X is counted when the discard is carried out, after the Dwarf has exerted twice. For a Dwarf whose bonus does not move as he exerts that is the number he was spotted with. For Gimli, Counter of Foes it is not: unwounded he is damage +1 when spotted and damage +3 once he has paid, and the Shadow player discards 3.

**The doubt.** The erratum 58_3 ("If the fellowship moves, spot a Dwarf who is damage +X and exert that Dwarf to make an opponent hinder X Shadow cards") has the same spot-and-exert words, and since your commit of 2026-09-14 (3898e999aa, #1026) it fixes X before the exertion, with your comment "X is part of the cost, so it is locked in before the exertion happens; anything responding to the exertion (e.g. Cirith Ungol Watcher hindering the Dwarf) must not change it". The original was not changed with it. Was it left as it is on purpose?

- **A. X is what the Dwarf was when he was spotted (as the erratum now reads it).** Unwounded Gimli 8_5 makes the Shadow player discard 1 and not 3; wounded once beforehand he could not pay at all (vitality 3). The definition takes the erratum's shape: a spot of a Dwarf able to exert twice, the bonus remembered, then the two exertions, and the discard counted from what was remembered (tried: the parked test passes with it, and so does every active test but the one below). The parked test goes live, and the 3 in `HinderedShadowCardsMayBeAmongThoseDiscarded` becomes 1, with one hindered card discarded in place of two.
- **B. X is what the Dwarf is once he has exerted (as built).** Exerting Gimli 8_5 twice for three discards stays the play the set offers, and the original and its erratum read X at different moments on purpose. Nothing changes in the game; the parked test is deleted, since `HinderedShadowCardsMayBeAmongThoseDiscarded` already reads the 3.

Parked on this, in `Card_08_003_Tests`: `XIsTheDamageBonusTheDwarfHadWhenHeWasSpotted` (A). The game fails it at the Shadow player's choice: a minimum of 3 where the test reads 1. For B there is `HinderedShadowCardsMayBeAmongThoseDiscarded`, which engine pass 8 wrote for Q-0098 with this Gimli and which reads the 3 ("damage +1 more for each of the two wounds"); it stays active, but it was written to show the hindered cards and may only describe the game as it stood. The card's older test used this Gimli too and read only his two exertions and the twilight.

The answer will also be read for Great Day, Great Hour 10_1 ("If an opponent plays a minion, exert a Dwarf who is damage +X to exert that minion X times"), whose definition counts the bonus after the exertion in the same way (its batch, dec-b10_1, has since come: the card's OWN old test, `GreatDayGreatHourExertsMinionTwiceWithDamagePlus2`, now `GimliCounterOfFoesExertsTheMinionOnceMoreForTheWoundHisOwnExertionGivesHim` (Gimli, Counter of Foes 8_5 and a Great Hill Troll exerted twice), asserts reading B and stays active; `XIsTheDamageBonusTheDwarfHadBeforeHeExerted` (A) is parked on this question and fails today, 1 expected and 2 read; under A the definition takes 58_3's shape and the old test's 2 becomes 1). A Dwarf hindered by a Cirith Ungol Watcher in answer to the first exertion is no board for the original: he cannot pay the second, and the event does nothing under either reading.

Recommendation: A, on the text (X is named where the Dwarf is spotted, ahead of the exertions) and on your own comment on the erratum; B only if the Gimli play was meant to stay on the original.

**Ketura (2026-10-11):** A is more consistent with the wording, yeah. (The errata was actually because a response to that exert from Cirith Ungol Watcher could cause the dwarf to become hindered, and therefore damage +0, so this memorized that before the exert.  But I think that's a more faithful reading regardless, it just didn't matter much before.)


### Q-0257 Crashed Gate 8_119: with no site that can be taken, may the Free Peoples player still choose the opponent, for nothing (as built), or must he add 3 threats?

Crashed Gate 8_119: "At the start of the regroup phase, the Free Peoples player must add 3 threats or choose an opponent who may take control of a site."

**Built now.** The trigger is a choice between adding 3 threats and choosing the opponent, and the opponent chosen is then asked "Would you like to take control of a site?" only if a site can be taken. With room for 3 threats the Free Peoples player is asked, whatever the sites; with room for fewer the opponent is chosen with no question. So with NO site that can be taken (the Shadow player already holds every site behind both markers) and room for 3 threats, he is still asked "Choose action to perform", and "Choose an opponent to take control of a site" taken there asks the Shadow player nothing, takes no site and adds no threat: the Gate is passed for free. With no site and room for only 2 threats nobody is asked and nothing happens.

**The doubt.** CR 4.2 "effect": "If the effect of a card or special ability requires you to choose one of two different actions, you must choose an action that you are fully capable of performing (if possible)." On F-0310 you held that a branch that cannot be done in full is not there to be chosen, and on F-0116 that a branch with nothing to act on is no branch. Neither says which "choose an opponent who may take control of a site" is when there is no site: done in full (an opponent can always be chosen, and his "may" is his own, empty here as it is when he says no), or nothing to do. No ruling of Decipher's names the card, and no other printed card sets "choose an opponent who may ..." against a second thing to do (Denethor 10_28 and Race Text 94_17 have the words with no choice). It is the same family as Q-0255 (Pelennor Flat 7_345, a hand of no cards "discarded") and could be answered with it, though the two need not go the same way: there the empty thing is the Free Peoples player's own act, here it is the opponent's leave.

- **A. An opponent can always be chosen (as built).** With no site to take and room for 3 threats the player may pick the opponent, who is not asked, and nothing happens; the Gate costs 3 threats only to a player who would rather pay them than give up a site, and nothing to one whose opponent holds every site behind the markers. Nothing changes in the game; the parked test is deleted.
- **B. An opponent with no site to take cannot be chosen.** With no site and room for 3 threats the 3 threats are added with no question. With no site and room for fewer than 3, neither branch can be done in full, so by F-0310 both are offered: "add 3 threats" adds the ones that fit, and the opponent may still be picked there for nothing. With no site and no room at all, nothing is asked. The correction is in the definition alone, the second branch inside an `If` on `SiteAvailableToControl` (tried by the author and again by the reviewer: the parked test passes with it). The parked test goes live, and the two active tests of reading A are rewritten: `WithNoSiteToTakeTheOpponentMayStillBeChosenAndNothingHappens` (it reads 0 threats where there would be 3) and `WithRoomForOnlyTwoThreatsAndNoSiteToTakeNobodyIsAskedAndNothingHappens` (the question would be asked there).

Parked on this, in `Card_08_119_Tests`: `WithNoSiteToTakeTheThreeThreatsAreAddedWithNoQuestion` (B). The game fails it at its first reading, the question being asked. The card's old tests took reading A twice, and one was named for it (`CrashedGateOffersOpponentChoiceWhenSiteControlNotAvailable`, and `CrashedGateChoosesOpponentChoiceAutomaticallyWhen3ThreatsCantBeAddedAndNoSiteAvailableToControl`); both are kept active under new names.

Recommendation: A, since what the text has the Free Peoples player do on that branch is choose an opponent, which he can do in full, and an old test was named for that outcome; B if a "may" with nothing to take is no branch, as a discard, wound or exertion with nothing to act on is none (F-0116).

**Ketura (2026-10-11):** Hrrrm.  The question is over whether the Freeps choice is "choose an opponent" or "choose an opponent and make them control a site".  The "may" part is what tips this over for me in the direction of the first; if the opponent chooses not to take it is the card retroactively made an invalid choice?  So I think A is the right answer.


### Q-0258 The One Ring when its bearer is killed while ASSIGNED to a skirmish not yet fought: among the companions with the assignment dropped (as built), or drawn in his assignment too?

Built for Q-0232 (engine pass 26, item ringrest): when the Ring-bearer is killed, The One Ring is no longer discarded with his other cards. It stays in play in his zone, on nobody, and if he was killed as the character of the skirmish being fought it is drawn in his place there. Sam's response takes it up from there; with no new Ring-bearer the game ends with it lying there.

One case your words may or may not reach. The Ring-bearer can be killed while he is ASSIGNED to a skirmish that has not begun: by a threat wound when another companion dies in an earlier skirmish, or by a card that wounds outside its own skirmish (Orkish Skirmisher 18_90). On the table he is then in an assignment group opposite his minion, not in the skirmish box.

**Built now.** His assignment is dissolved as it is for any character who dies (the clients are sent its removal, and his minion goes back among the unassigned), and the Ring lies among the companions: in no assignment, and not in the skirmish on the table, which was somebody else's. Then the game ends; or Sam takes the Ring from the companions' row and play goes on with that minion unassigned, which is what happened before this change too. `mechanics/TheOneRingWhenItsBearerIsKilledTests`, `KilledDuringAnothersSkirmishTheRingIsInNeitherThatSkirmishNorTheOneHeWasAssignedTo`, shows it (a Dwarf Guard overwhelmed, the threat wound killing an exhausted Frodo who was assigned to a second minion).

- **A: keep.** "The same position, including in skirmishes" is his zone, and the skirmish box when he died in it. Nothing changes.
- **B: the Ring stands in his assignment as well.** A finished game shows the Ring opposite the minion he was assigned to. For the table only, as the skirmish place is: the game's own assignment is still dissolved (an assignment whose fellowship character is the Ring would be offered as a skirmish to fight once Sam has taken the Ring and play goes on), and the clients are sent an assignment of that minion to the Ring, taken away again when a new Ring-bearer takes it up. That is a second table-only place in `GameState` and one more test.

Recommendation: **A**. It is the rarer death, and B adds table-only state to the assignments for the sake of it.

For your information, how the Ring is told to the clients, in case any of it is not what you meant by "the proper PCIP etc events". None of it needs an answer:

- The Ring never leaves play. After the `RCFP` of the Ring-bearer and the cards discarded with him it is MOVED: one `MCIP` with zone FREE_CHARACTERS and no target card, under the card id it had. Where he died in the skirmish an `ATS` for the Ring follows. A client that joins or refreshes afterwards is sent the Ring as a card in FREE_CHARACTERS and, after the `SS`, the same `ATS`. There is no `PCIP` at the kill; the `RCFP` and the `PCIP` attached to Sam at the hand-over are `MakeRingBearerEffect`'s, as they were.
- The skirmish place is the table's alone. `Skirmish.getFellowshipCharacter()` is null, as it is today once the character has died, so no filter, strength or rule sees the Ring as skirmishing; `Skirmish.getCardLeftInFellowshipPlace()` holds it for the events.
- The client as it stands should draw it with no change: a card in FREE_CHARACTERS that is marked as in the skirmish goes in the fellowship half of the skirmish box (`skirmishFellowshipGroup` in gameUi.js), and otherwise in the companions' row. That is from reading the client's code; I did not run a browser.

**Ketura (2026-10-11):** A.  In the client today, if Frodo dies in an unresolved assignment it deletes the assignment box and rearranges the board before Sam's response can kick in, so there's no need for us to change anything for that brief instant that doesn't even show on the client before the Ring is back in the characters zone.


### Q-0259 Many Miles 13_21: is it +2 at a site a Shadow player replaced this turn BEFORE the fellowship came to it (as built: +1)?

Many Miles 13_21 ([elven] Archery event, cost 2): "Spot 2 Elf archers to make the fellowship archery total +1 (or +2 if a Shadow player has replaced the fellowship's current site this turn)."

Q-0234 (A) is built (engine pass 26, item manymiles): the mark a Shadow player's replacement leaves is now that site's, in force only while the fellowship is still there. Once the fellowship has moved on, the event is +1 again. Nothing else was changed, and one board beside it is left as it was, which your words on Q-0234 may or may not reach.

**The board.** A Shadow player replaces a site AHEAD of the fellowship, and the fellowship moves to it later the same turn. Úlairë Nelya, Lieutenant of Morgul 1_233 does it ("Shadow: Exert Úlairë Nelya and spot an opponent's site to replace it with your site of the same number"): the fellowship is at site 2, the other player's marker is two sites ahead, so site 3 already lies there and is the mover's own; in the Shadow phase Nelya replaces site 3; in the regroup phase the fellowship moves to site 3; in that archery phase Many Miles is played. Her definition lays a Fellowship-block site, so this wants a numbered path, which with set 13 is the Anything Goes format. No Shadow card on a Shadows-block path replaces any site but the one the fellowship is at.

**Built now.** +1 (the total goes 1 to 2). The mark is laid only when the site replaced is the one the fellowship is at as it is replaced. Probed after the change, with site 3 laid by a cheat.

- **A. As built: +1.** "Has replaced the fellowship's current site" is the act that Riddermark Tactician 13_133 answers ("Each time a Shadow player replaces the fellowship's current site"): something done to the site under the fellowship. A site replaced before the fellowship came to it was not that. Nothing changes.
- **B. +2.** The site the fellowship is at when the event is played was replaced by a Shadow player this turn, wherever the fellowship stood at the time. One line of `PlaySiteEffect` (the mark is laid for any site a Shadow player replaces; it is still read only at that site) and a test in the card's file.

I recommend **B**, lightly: it is the reading of "current site" you gave on Q-0234 (the site the fellowship is at now, asked what has happened to it this turn). It is the rarer board by far.

For your information, and needing no answer unless it is wrong: one more neighbour is as built. A Shadow player replaces the site under the fellowship (Úlairë Nelya 11_222 in the Shadow phase), and the Free Peoples player replaces it again before the archery phase (Traveled Leader 12_34 in the maneuver phase: "Spot a [gandalf] Wizard to replace a site in the fellowship's current region with a site from your adventure deck"). Many Miles is +2 there (probed: 1 to 3): a Shadow player has replaced the fellowship's current site this turn, whatever card lies there now.

**Ketura (2026-10-11):** B I think, yeah.  Your reading seems sound. For the Traveled Leader case, yeah, I concur that the replacement still happened, whatever happened in between.


### Q-0260 Keep or strike: a test of a reveal that finds no card of the kind was removed from mechanics/RevealingNoCardsFromAHandTests, and one shortened, now that Mallorn-trees 1_54 is not offered with no [elven] card in hand (F-0420)

**What changed, and why.** Mallorn-trees 1_54 ("Fellowship: Reveal an [elven] card from hand and place it beneath your draw deck") now requires an [elven] card in hand (F-0420, which you confirmed: "Yeah that looks like a reasonable fix"). The mechanics class `mechanics/RevealingNoCardsFromAHandTests` (your ruling on Q-0144: a reveal with nothing to show gives no pop-up and a line in the log) had used that same ability with NOTHING to reveal, as the one printed card whose chosen reveal could come to nothing. With the requirement the ability is not offered on those boards, so two of its tests failed:

- `WithNoCardOfTheKindInAHandThatHoldsCardsNobodyIsShownAnythingAndTheLogDoesNotCallTheHandEmpty` (Mallorn-trees used with only Aragorn in hand; the log says "revealed no cards from <player>'s hand"): **removed**. Its whole board is the one F-0420 takes away.
- `AChosenCardFromAPlayersOwnHandIsShownToTheOtherPlayerAsBeforeAndFromAnEmptyHandNothingIs`: **its last four lines removed** (Mallorn-trees used a second time with the hand empty), and renamed `AChosenCardFromAPlayersOwnHandIsShownToTheOtherPlayerAsBefore`. What it shows of a card revealed from its owner's hand stays.

I searched every definition that reveals from a hand (14): no other printed card can come to a CHOSEN reveal that finds no card. The others are costs (not offered when they cannot be paid), take the whole hand, or take a card at random. So no card that exists reaches the log line "<card> revealed no cards from <player>'s hand" any more, and by your notes of 2026-10-11 ("a test shows what a card that exists does") I did not rebuild the test on an invented ability. The engine's code for that line is untouched; an empty hand is still shown for all three reveal words by the class's other tests (Treachery Deeper Than You Know 1_86, Troll's Keyward 12_105, Friends of Old 3_16, Storm of Argument 3_6). The class comment, shapes.md and the pitfall say what is no longer laid.

**Which cards behave differently.** None beyond Mallorn-trees itself (F-0420). This is about tests only.

**Keep** (the default; it stays unless you strike it): the class has 8 tests, and the "holds cards but none of the kind" line of the log has no test.

**Strike** would mean: the removed test and the removed lines come back with the reveal stapled by the rig's ad-hoc effects onto a card with no text of its own (Q-0241's way), so the log line is still shown though no printed card reaches it. Say so and it is done in the next engine pass.

**Ketura (2026-10-11):** Keep, well done.  Yeah sometimes we're just not going to have some branches tested, and that's okay (for now; we have so many extent branches that are higher priority right now)


### Q-0261 Keep or strike: the generated Play test (Q-0220: I) reads the play and nothing more, where 32 of 149 hand-written ones read something further; it reads a Shadow card's pool as a difference; and it names the card and removes the placeholder in a skeleton file

**What was built, and why.** Your answer to Q-0220 was "I is "generate"": the Play clause's tests have never had a finding beside them and should be written by a generator. `scripts/gen_play.py <id> --write` now writes that test from the card's sheet row (cost, type, a minion's site number) into the card's file, and the `P1` clause and its claim into the ledger. It takes a companion, an ally, a minion, a follower, and a condition, possession or artifact played to the support area, where nothing in the card's text or definition is about its being played: 1,130 of the 2,187 Decipher cards that are played from hand (1,105 of the 2,120 not yet worked). It leaves the rest to the author and says why: a card played on a bearer (392 cards), a requirement or an extra cost to play (339), a card that answers its own play or its being in play (293), a cost its own text changes or speaks of (190), toil (17), a card of the Ring-bearer's own title (14); a card may have several of these. Tried in a scratch copy of the repository on every card it takes, landed and unworked: 1,289 generated tests, all pass. Nothing generated is committed; no landed card's file was touched.

**What the generated test reads.** On the file's own `GetScenario()`, with nothing in play but the Ring-bearer: the card in hand and offered; the pool before and after (a Free Peoples card: 0, then its cost, in the fellowship phase of turn 1; a Shadow card: the pool as the Shadow phase at site 2 finds it, then that less the cost and, for a minion whose site number is above 2, 2 more for roaming); the zone it ends in (among the companions, among the minions, in the support area and not on the Ring-bearer); and what the game then waits for.

**Where that differs from what authors wrote.** I laid it beside all 196 hand-written tests of the 195 landed cards it takes (the list asked for was ten; `orchestrator/engine-pass-26/playgen.md` has ten in full). The cost and the zone are the same reading in every one (Southron Archer 54_245's author played it at site 5, where it pays no roaming penalty; the generated test plays it at site 2 and reads the penalty too). Three differences, each of which you may strike on its own:

1. **Nothing beyond the play is read.** Of the 149 hand-written tests that claim `P1` and nothing else, 32 read something more: the character's printed strength or vitality once in play (12: Grimir 1_17 reads 3 and 3, Úlairë Lemenya 69_37 reads 9); the hand count going from 1 to 0 (9: the same reading as the zone); a companion or a minion laid on the table and read as not bearing the card (5: Shoulder to Shoulder 1_59, Sticky Strands 103_14); and something of the card's own (12: no token on it yet, not hindered, "is no minion there" on Bladetusk Charger 103_41, not discarded away from a marsh on Vile Tentacle 101_36). The Standard asks none of these of the Play clause, and each is the `-` or the "before" of another clause of the same card, whose own tests read it. As built the generated test does not read them, and the Standard now says that anything more the play would show is read in the test of the clause it belongs to.
2. **A Shadow card's pool is read as "what it was, less N"**, with the fellowship read at site 2, where authors mostly wrote the sum out ("10 set + 1 companion + 2 for Ettenmoors" is 13, then 9). The generated form does not depend on which site path the file's scenario lays; the hand-written form also pins what the move added, which is no part of the card.
3. **In a file that is still the skeleton, `--write` gives the card a name in place of "card"** (made from its title: "troop" for Goblin Patrol Troop; `--alias` chooses another) **and takes out the commented-out placeholder test**, which is this same test unwritten. The conformance check refuses both leftovers anyway.

**Which cards behave differently.** None. This is tests and tools only.

**Keep** (the default; it stays unless you strike it): the generator as described; an author runs it before writing any test and writes `P1`'s test by hand only for a card it leaves (the Standard and `roles/author.md` now say so).

**Strike 1** would mean: the generated test also reads a character's printed strength and vitality in play. It would then leave every character whose own text changes either while it stands alone (Black Rider 12_161 is strength +1 for each companion), which I have not counted. **Strike 2** would mean: the generated Shadow test asserts the pool as a spelled-out sum, and the generator leaves any file whose scenario lays a path it has no shadow number for. **Strike 3** would mean: the generator writes only into a file its author has already named the card in, and leaves a skeleton alone. Say which and it is done in the next engine pass; say "strike the generator" and the two documents go back to "the author writes P1's test".

**Ketura (2026-10-11):** Keep.  I might even want to go one further; I do not think it is necessary to, for example, test that every single minion removes twilight from the pool based on its twilight cost when played.  I would prefer we have mechanical "TwilightPool" tests that go over the nuances of the mechanic, which exhaustively confirms that if a Shadow card of any type has a twilight cost of X, X is going to be removed (roaming handled here too), and establishes that essentially proven function. That way, it is acceptable on all cards to just check that their twilight cost is set to the appropriate amount (as the StatsAndKeywords check already does), and further testing does not need to re-litigate proven functionality (that the number translates to twilight tokens). I would also say that this should be done for things like stats and such.  We aren't, for example, testing that every character is "able to be assigned to a skirmish", because that is proven basic functionality and we don't need to get obsessive that a character with no assignment-affecting text has interfered with this. Perhaps this warrants another pass, something that takes a good look at the StatsAndKeywords tests and extrapolates what mechanical tests need to exist for us to *trust* that StatsAndKeywords are all we need to check for the associated elements.  We can then strip out tests that do nothing except re-litigate what StatsAndKeywords already checked for us.


### Q-0243 Hatred Stirred 11_210: "you may shuffle a [wraith] card from your discard pile into your draw deck" with no [wraith] card there: still offered (it shuffles the deck, Q-0102), or not (Q-0112)?

Engine pass 26, item `q34sweep` (Q-0220: A). Hatred Stirred 11_210: "Each time a companion loses a fierce skirmish involving a [wraith] minion, you may shuffle a [wraith] card from your discard pile into your draw deck."

**The doubt.** With no [wraith] card in the Shadow player's discard pile the offer is made today, and a yes shuffles his draw deck with nothing shuffled in. The trigger has no cost, so Q-0034 asks whether that yes "does nothing". Two of your rulings point different ways:

- Q-0102, on "you may shuffle UP TO 2 ... from your discard pile into your draw deck" (Flotsam 103_106, Morgul Skulker 1_258): the deck is shuffled even when nothing is shuffled in, "so such an offer always does something and is always made".
- Q-0112, on Mûmakil Commander 18_71: "on an engine level, the shuffle can target 0 cards, but on a card level we'll set up Mumakil Commander to require there be a mount in play first."

Hatred Stirred says "a [wraith] card", one, with no "up to": choosing none is not a way of doing what it says, as it is on the Skulker. It is the only unworked Decipher card I found with this wording in a no-cost offer; Morgul Skulker 1_258 and Sméagol, Pitiable Guide 19_11 say "up to 2" and are left offered under Q-0102.

**What was done.** Nothing: the card is as it was, offered and shuffling. It stays so unless you say otherwise.

- **A. Leave it** (Q-0102 holds for "shuffle a card" as for "up to N"): the offer is always made and a yes with no [wraith] card there shuffles the deck. Nothing to do; an author tests it so when the card is worked.
- **B. Require the card** (as Q-0112 did for the Commander): the trigger gains `requires: HasCardInDiscard` with `filter: culture(wraith)`, and with no [wraith] card in the pile the offer is not made. One definition, one test in its file; Grimir 1_17 ("Exert Grimir to shuffle a [dwarven] event from your discard pile into your draw deck") is not touched either way, since it has a cost.

My reading is B, for the word "a"; I did not make it because the engine's shuffle is yours by Q-0102 and I could not tell which ruling you would want to reach this card.

**Ketura (2026-10-11):** Go for B.


### Q-0242 Keep or strike: Frodo, Courteous Halfling 4_301's bar on Shadow discards from hand is now shown stopping Hand of Sauron 3_90, since Orc Inquisitor's offer is no longer made under it (Q-0231: B)

Engine pass 26, item `adopt` (Q-0231: B). Frodo, Courteous Halfling 4_301: "While you can spot 3 unbound companions, Shadow cards may not discard cards from your hand or from the top of your draw deck." Orc Inquisitor 1_268: "When you play this minion, you may make the Free Peoples player discard a card at random from hand." Hand of Sauron 3_90: "Maneuver: Exert a [sauron] minion to make the Free Peoples player discard a card at random from his or her hand."

**What changed.** With your B the Inquisitor's offer is no longer made under Frodo's bar, and `Card_04_301_Tests.WithThreeUnboundCompanionsAShadowCardsOptionalDiscardFromHandIsNotOffered` is live and says so. The test beside it, `WithThreeUnboundCompanionsAShadowCardCannotMakeYouDiscardFromHand` (the old `at` test courteousFrodoPreventsDiscard, converted), TOOK that offer and read the hand untouched; it could not stand as it was. The item's words covered only the first test.

**What was built.** Rather than delete it, the second test was laid again on a discard that is still attempted: with three unbound companions and two cards in the Free Peoples hand, the Shadow player plays Hand of Sauron in the maneuver phase. It is an event with a cost, so it is played whatever it will do (Q-0034): the Inquisitor exerts to pay for it, and the hand is as it was. That keeps what the old test showed, the bar stopping a discard a Shadow card really makes. `WithTwoUnboundCompanionsAndARingBoundOneAShadowCardStillMakesYouDiscardFromHand` is unchanged (the Inquisitor's offer made and taken with the bar off).

No definition changed for this. 4_301 has not been worked by an author yet (no ledger); when it is, these three tests are what he finds.

- **Keep.** Nothing to do.
- **Strike** (if the old test should simply go, or Hand of Sauron is the wrong card for it). The test is deleted, or laid on the card you name; until 4_301 is worked nothing would then show the bar stopping a discard that is made, only the offer withheld and the discard with the bar off.

One thing this does NOT cover, seen on a probe and left alone because the item names the random discard only: the other half of Frodo's sentence. Uruk Soldier 1_154 ("When you play this minion, you may make the Free Peoples player discard the top card of his draw deck") played under the bar is still offered, and a yes discards nothing. Your B, as Q-0231 put it, "would then hold for every no-cost 'you may make ... discard' under Frodo 4_301 and Frodo, Little Master 19_28"; it is three lines where the top-of-deck discard is taught, and is left for the sweep that follows this item.

The test is in and stays unless you strike it.

**Ketura (2026-10-11):** keep


### Q-0241 Keep or strike: four of the fourteen kinds of effect the engine was taught for Q-0224 are tested with the tests' own cards (Test Muster, Test Salve, Test Almanac, Test Refrain), because every printed card that has them carries a hand-written requirement

Engine pass 26, item `adopt` (Q-0224: A2). Your words on the item: "A `mechanics/` test for EACH taught kind of effect, both ways (offered where it can do something, withheld where it cannot)". And on Q-0166: "Always prefer using real mechanics or cards; if we have to invent cards we need to scrutize whether or not we have gotten lost in the weeds."

**What was built.** `mechanics/AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests` has every one of the fourteen kinds both ways. Ten are on printed Decipher cards whose definitions have no requirement that covers the board: Bill the Pony 13_143 (reinforce), Too Late 7_209 (transfer), Úlairë Toldëa 1_236 (assignment), Dunlending Looter 4_11 (stack from play), Vengeful Savage 17_62 (stack from a discard pile), Narchost 7_362 (put on the Ring), Uruk Soldier 1_154 (discard from the top of a deck), Gwaihir 17_20 (a card chosen for a later effect), Ever My Heart Rises 4_46 (a number named), Orc Inquisitor 1_268 under Frodo, Courteous Halfling 4_301 (random discard from hand).

For the other four no printed card will do. The only cards that have the kind in an offer that costs nothing are the ones the campaign corrected by hand before the rule existed, and their hand-written requirement answers before the engine is asked (those 53 requirements stay, by the item's words):

- making an ally take part in skirmishes: Dunlending Warrior 4_18 and Ravager 4_15 (F-0283);
- a restore: Desert Wind Scout 103_48 (F-0122);
- a card chosen from a discard pile: Greatest Kingdom of My People 51_16 (F-0182);
- a repeated effect (`DoWhile`): Race Text 94_4 (F-0323).

So four cards were added to the tests' own file (`testCards.hjson`, set 0, never in a real game), each one ability with no cost and no requirement:

- Test Muster 0_8029: "Maneuver: Make an ally participate in skirmishes until the regroup phase."
- Test Salve 0_8030: "Fellowship: Restore a Free Peoples card."
- Test Almanac 0_8031: "Fellowship: Choose a Free Peoples card in your discard pile and take it into hand."
- Test Refrain 0_8032: "Fellowship: Heal a companion, and again for as long as a companion was healed."

Each of its tests fails with the rule off and with the rule on but the kind untaught, and passes as built: not offered with no ally / nothing hindered / an empty discard pile / nobody wounded, offered and used once there is one.

- **Keep.** Nothing to do.
- **Strike** (if four invented cards are too many for this). The four cards and their four tests are deleted. Those four kinds are then taught in the engine and held by no test of their own: only by the card tests of 4_18, 103_48, 51_16 and 94_4, which pass with the rule off as well. The other way to strike is to take the hand-written requirement off one of those real cards for each kind and let the engine carry it; that changes the item's "the 53 requirements STAY" and is yours to say.

The cards are in and stay unless you strike them.

**Ketura (2026-10-11):** This needs reworked.  I'm not opposed to inventing new cards when testing mechanics or engine code, but I do object to adding them as cards with IDs.  The original Hinder at tests established the way to do this: staple the ability you want to see onto a card with no usual text (frodo or a minion depending on side) and invoke it that way.  This establishes the artificial test nature of the card while also including its creation right there, plus does not expose us to potential issues surrounding test cards infecting the ecosystem. If there are any other tests which have invented their own cards, please set up a task to convert them to test frankensteins.


### Q-0240 Resistance Becomes Unbearable 2_79: does a Ring-bearer who was already exhausted put on the Ring?

"**Maneuver:** Exert a twilight Nazgûl to exert the Ring-bearer. If the Ring-bearer is then exhausted, he puts on The One Ring until the regroup phase."

The Ring-bearer is ALREADY exhausted when the event is played. Its cost (a twilight Nazgûl exerts) can be paid, so it may be played; he cannot exert (CR 4.2 "exhausted": "must exert" on an exhausted character, "nothing happens").

**Built now:** the Nazgûl exerts, the Ring-bearer does not, and he puts on the Ring: the definition exerts him and then asks only whether an exhausted Ring-bearer can be spotted. The card's test from before the campaign, `PutsOnRingWhenRBWasAlreadyExhausted` (Frodo with 3 wounds, Úlairë Attëa 2_82), asserts exactly this; it is kept, active, as `PutsOnTheRingWhenTheRingBearerWasAlreadyExhaustedAndCannotExert`.

- **A (as built, and the plain words):** "is then exhausted" reads his state once the event has done what it can. He is exhausted, so he puts on the Ring. Nothing changes.
- **B:** the second sentence follows from an exertion that was made; none was, so nothing follows and the Ring stays off. The definition would remember the exertion and ask the `If` of it, and that test's last assertion would turn to the Ring off.

Nothing is parked: the test of A passes and stays active. Neither rules document names the card, and no other card's text says "is then".

Recommendation: A. It is what the words say and what the old test was written to show.

**Ketura (2026-10-11):** A


### Q-0239 Keep or strike: no third copy of a two-dot title in play in a card test: Raiding Schooner 103_55, Winged Nazgul 103_81 and Endless Night 103_96 relaid (CR 5.0 "non-unique"; a step past Q-0213, row G)

**What was done (engine pass 25, item twodot, commit 43e9b1b47; already made, and it stays unless you strike
it).** Your ruling on Q-0213, row G, was about two cards of one UNIQUE title in play. It did not speak of a title
with two dots. CR 5.0 "non-unique": "If a card has 2 dots it is restricted to 2 copies in play". The game counts
them by title, subtitles regardless and hindered copies included (`PlayUtils.checkUniqueness`). Three landed test
files cheated a third copy in, which is a board no game reaches, and I relaid them as row G says (the extra copy
goes). Tests only: no definition or engine change, so no card plays differently. The whole suite passes (10356).

What each test was, and what it shows now:

1. **Raiding Schooner 103_55** ("Maneuver: Spot a Corsair, hinder another Raiding Schooner, and remove 3 [raider]
   tokens from your cards to hinder a Free Peoples artifact"). `TheSchoonerToHinderIsChosenFromTheOthersAndIsNeverItself`
   had three Schooners in play to read the choice between the two others. No game puts that choice. With the third
   gone the test would have repeated the main test, so I gave the same board one thing no other test had: neither
   Schooner holds a token and all 3 are on a Corsair War Galley. It is now
   `ManeuverAbilityIsPaidForWithThreeTokensOnAnotherOfYourCardsAndWithOneOtherSchoonerNothingIsAsked`: the ability
   is offered, the other Schooner is hindered, the Galley's three tokens go and the one artifact is hindered, with
   nothing asked. Tried and put back: the ability's requirement narrowed to tokens on the Schooner used passed
   every test before the change and fails this one. That reading is new, and is the part of this that goes beyond
   taking a copy out.
2. **Winged Nazgul 103_81** ("Assignment: Return another Winged Nazgul to hand to assign this minion to an unbound
   companion"). `WithTwoOtherCopiesInPlayTheOneReturnedIsChosenFromThem` had three copies in play for the same
   kind of choice. With the third gone it is `EitherCopyMayBeTheOneUsedAndItIsTheOneAssignedWhileTheOtherGoesHome`:
   the second copy is used where the first behaviour test uses the first, the first goes home with nothing asked,
   Úlairë Lemenya stays, and the copy used is assigned to Aragorn. Plainly: this is the first behaviour test with
   the two copies' parts exchanged. I found no mistake in the definition that it alone notices. I kept it because
   row G says the copy goes and the test stays; I would delete it (answer C).
3. **Endless Night 103_96** ("To play, hinder 4 twilight conditions").
   `IsPlayedForFiveByHinderingFourTwilightConditionsOfThePlayersChoice` had three Covers of Darkness in play (Omen
   of Fear, Omen of Despair, and Omen of Dread hindered beforehand as the candidate not offered) beside three
   Ominous Skies. Dread is out of that test and nothing else in it changed: five twilight conditions are offered,
   four are chosen, a condition that is not twilight and a twilight minion are not offered. The hindered twilight
   condition is still read in `CannotBePlayedWithFewerThanFourTwilightConditionsToHinder`, by the count (three
   that can be hindered and a hindered Dread: not playable), where only two Covers are in play. It cannot be read
   in the test of the choice as well: every twilight condition in print is Endless Night, Ominous Sky (three dots)
   or a Cover of Darkness (two dots), so five is all a game has beside Endless Night, and a choice of 4 wants all
   five able to be hindered.

The three ledgers say this in their Notes. 103_81 and 103_96 were approved and are `ready` again; 103_55 was
already `ready` from the relay.

**These were the only three.** A search of every test file for a test that cheats more copies of a two-dot or
three-dot title into play than its dots allow found exactly these three before the change and none after.

**If you keep it,** the Standard and the rig's skill get one line beside "no two cards of one unique title in
play": no more copies of a title in play than its dots allow. That line is not written yet; it waits on your
answer.

- **A. Keep** (as built): the three tests as described.
- **B. Strike:** the three tests go back to their boards of three (a third Schooner, a third Winged Nazgul, a
  hindered third Cover of Darkness), cheated in to read a choice the dots keep out of play. The Schooner test's
  new reading (tokens all on another card) would be lost with it unless you say to keep that as a test of its own.
- **C. Keep, and delete the Winged Nazgul test** of item 2: it repeats the first behaviour test. Its ledger line
  goes and nothing is left unclaimed.

**Ketura (2026-10-11):** A.  Yeah, the standing idea is "no cheating in more copies of a card than their uniqueness level allows" for that.


### Q-0238 Speak "Friend" and Enter 2_26, and the nine cards worded "play the fellowship's next site (replacing opponent's site if necessary)": on a Shadows-block path, may one replace the player's OWN site? (rider to Q-0226)

Speak "Friend" and Enter 2_26: "Fellowship or Regroup: Spot Gandalf to play the fellowship's next site (replacing opponent's site if necessary). Draw a card if you play an underground site." The same parenthesis stands on Pathfinder 1_110 and 4_129, Thrór's Map 1_318, Galadriel 3_17, Follow Sméagol 5_23, Very Nice Friends 7_76, Andúril 7_79, Pressing On 7_325 and Still Far Ahead 8_29. The cards of sets 11 and after say only "play the fellowship's next site" (One Good Turn Deserves Another 11_49, Sméagol 11_51, Madril 11_62, Ranger of Westernesse 11_65, Úlairë Toldëa 11_225, Mere of Dead Faces 11_246) and carry no parenthesis. CR 4.2 "site": "Some cards allow a player to play the next site on the adventure path at times when the fellowship is not moving. These may be used even when the next site is already there. In such cases, the new site replaces the old one; put the old site back in its owner's adventure deck."

**Built now.** One effect (`PlaySiteEffect`) is behind every one of these cards and looks only at what the player's adventure deck holds for the place, never at whose site lies there. On a numbered path (sets 1 to 10) the player's own site lying next cannot be replaced, because his deck has no other site of that number: the event is played, nothing is laid, no card is drawn (tested: Card_02_026_Tests.WithHisOwnSiteAlreadyNextNoSiteIsPlayedAndNoCardIsDrawn). On a Shadows-block path his deck does hold other sites, and there a second copy of the event REPLACES HIS OWN site: a probe played two copies in one fellowship phase, and after his Abandoned Mine Shaft was laid as site 2 the second copy asked "Choose site to play" among his seven other sites (one must be chosen) and sent the Mine Shaft back to his adventure deck. It can only arise where a card of sets 1 to 8 meets a Shadows-block path.

- **A (as built): the parenthesis is a reminder, and the rule decides.** A card that plays the next site replaces whatever lies there, the player's own site included, as the rules entry says with no word of whose it is. The nine older cards and the Shadows-era cards then do the same thing. Nothing changes; a test of it on the Shadows path could be added to this file.
- **B: the parenthesis is the card's own limit (cards are code, Q-0012): only an opponent's site is replaced.** With his own site lying next, one of the nine older cards plays nothing, on any path, as it does on a numbered path today (and Speak "Friend" draws no card). `PlaySiteEffect` then wants a switch, set on those nine definitions, that offers nothing when the site lying there is the player's own; the Shadows-era cards, which have no parenthesis, go on replacing it. A test on the Shadows path goes into this file and into each of the nine as its batch comes.

Nothing is parked: no test of either reading was written. The file's SpeakFriendandEnterReplacesOpponentSite reads the replacement of the other player's site as built (by its twin, which is Q-0226 itself).

Recommendation: A, lightly. The parenthesis was written when a player could not hold a second site for a place, so it could not have been meant to forbid anything; against it, B is what the words say, and it may be one more for the pile of era rulings you are gathering under Q-0226.

**Ketura (2026-10-11):** A


### Q-0237 Keep or strike: no hindered candidate, and no second block's path, is owed on a Decipher card whose text names neither

From the orchestrator (2026-10-10). Two batches of Decipher cards have now had author and reviewer disagree on the same point, so I have settled it for the phase and put it to you. **It stands unless you strike it.**

**1. A hindered candidate on a Decipher card that spots.** CR 5.0 says a hindered card cannot be spotted. The Standard's check F asks for a hindered candidate where a text "spots"; the reviewers of 1_53 and of a card in the batch dec-b1_87 held that every spot requirement therefore owes one, the authors that it does not. No Decipher text speaks of hinder, the rule is shown once in `mechanics/HinderedCardCannotBeSpottedTests`, and a hindered candidate on every card that spots (many hundreds of the 3,000) is, as I read it, the kind of test you ruled out under Q-0213: "a waste of time to continue searching for *theoretical* malformed targets in places and arrangements where they have been historically sparse or even outright nonexistent." The campaign's ten findings about hindered cards (Q-0108) were all one filter word, `attachedTo`, which Q-0220's E mends once in the engine.

*As it stands:* no hindered candidate on a Decipher card; the ledger's Notes say so in a line. PC cards, whose texts are written with hinder in mind, keep theirs.

**2. A site's block.** A text that says "a site" or "each site" with no block named: does it owe a second scenario on another block's path, to show the definition did not narrow it to one block? One card so far (Aragorn's... 1_108) had `siteBlock(fellowship)` written where the text names none, and no test noticed. *As it stands:* no second scenario on every such card; that one definition is the finding, and Q-0220's census check (B) is the place to catch a block named in a definition and not in its text.

- **Keep** both.
- **Strike 1**, **strike 2**, or both: the candidate, or the second scenario, is owed on every such Decipher card.

**Ketura (2026-10-11):** Keep both for sure. That is already covered by the generic hinder tests, and such classes of mechanical interaction can stand to keep to mechanical tests instead of incessantly checking them on all cards.


### Q-0236 Keep or strike: two tests of The White Council 101_18 each lay nine tables in a loop, one Saruman to a table (Q-0213, row G)

**What was done (engine pass 25, relay-14; already made, and it stays unless you strike it).** The White Council
101_18 says "the first sentence of Saruman's game text does not apply". The game keeps that switch in each
Saruman's own definition, so this card's tests put every Saruman whose text opens with a sentence under it: nine
of them (3_68, 3_69, 4_173, 5_56, 53_68, 101_30 "may not take wounds during the archery phase and may not be
assigned to a skirmish"; 4_33, 17_37, 102_29 "may not be assigned to a skirmish"). Two tests had all nine on one
board (Q-0213, row G: nine cards of one unique title). The relay list said "each on a board of its own".

They are now still two tests, `WithGandalfAndTwoElvenAlliesEverySarumanCanBeWoundedByArcheryAndAssignedToASkirmish`
and `WithGandalfAndOnlyOneElvenAllyNoSarumanCanBeWoundedByArcheryOrAssignedToASkirmish`, and each lays nine tables
in turn:

    for (var name : new String[] { "keeper", "servant", "traitor", "master", "keeperPC", "fellVoice", "rabble", "instigator", "mind" }) {
        var scn = GetScenario();
        var saruman = scn.GetShadowCard(name);
        ...                                   // the Council, Gandalf, the allies; Saruman, a Troop and a Savage
        assertTrue(name, scn.FreepsHasAssignmentTargetAvailable(saruman));
        ...
    }

One Saruman to a table, the same board around each, the same readings as before (the archery wound may or may not
be put on him; each player is or is not offered him for assignment; the Shadow player assigns him), and every
assertion carries the alias, so a failure names the Saruman ("AssertionError: master expected:<3> but was:<2>").

**Why it wants your eye.** The Standard says "A test's first statement gets a table" and allows no helper methods.
No other card test loops over tables. I chose the loop over the Standard's letter because the other way is
eighteen near-identical tests of about 35 lines each, and I think that is worse to read and to keep; but it is a
new form and you may not want it in card tests.

**What it catches.** Tried, one at a time, and put back: the switch taken off 5_56's archery sentence, off its
assignment sentence, off 102_29's sentence and off 17_37's. Each fails the first test and names that Saruman. No
Saruman's own test file reads the switch (none names the Council or Saruman's Staff 4_174), so these two tests are
the only place it is read on each of the nine.

**Nothing else behaves differently:** tests only, no definition or engine change.

- **A. Keep** (as built): two tests, nine tables each.
- **B. Strike for eighteen tests:** one table to a test, as the Standard has it; the same readings, nine times
  two. The two tests above are replaced; the ledger's two claim lines become eighteen.
- **C. Strike for one Saruman of each wording:** each test keeps one table and two Sarumans go under it in two
  tests of their own (3_68 and 4_33, say). The other seven definitions' switches are then read by nothing until
  their own cards are tested; the ledger's Notes would say so.

**Ketura (2026-10-11):** Are we testing White Council or are we testing nine Sarumen? In the case of a modifier, we only need to ensure that the *outgoing* mechanism has been enabled, and it's up to each of those individual cards to check their side responds to the *incoming* mechanism.  For White Council, that means we check *one* of them, which then ensures that White Council has done its due diligence.  So a C with just one Saruman.


### F-0385 Valley of the Silverlode 2_120: an optional trigger, then a choice that may be empty: may the player say no twice?

"When the fellowship moves to Valley of the Silverlode, each Hobbit companion may heal."

**Built now.** An optional trigger: the Free Peoples player is offered it among his optional responses and may pass. Once he takes it he is asked to choose Hobbit companions to heal, and that choice has a minimum of 0 (read with Sam alone wounded: 0 to 1). So he can say no twice, and the two noes end the same way. Uruviel 1_67, who has her owner's site 6's text, makes the same offer with the same choice beside the site's.

On Q-0143 (The Palantír of Orthanc 53_67) you took the yes/no off a 0-to-N choice: "let's get rid of the pop-up". There the yes/no was a pop-up inside an ability; here it is the trigger's own offer. On F-0041 (Stewards' Legacy 13_75) you had "you may" on a standing trigger made an optional trigger, and its choice became exactly 1. The two point different ways for this card, so it is not corrected.

**A. Leave it.** The offer may be declined, and the choice after it may still be answered with nobody. No finding; nothing changes. The Shire Countryside 53_113 ("you may heal up to 2 companions"), landed, is built and tested this way.

**B. One no, the offer's.** The trigger stays optional and the choice becomes 1 or more (`count: anyNonZero`), as 13_75 and Robin Smallburrow 68_111 (F-0300) were built. With one Hobbit wounded, taking the offer leaves only him to choose. Uruviel's test of the Valley gains a reading of the minimum, under the site's offer and under hers; nothing else changes.

**C. One no, the choice's** (the reviewer's correction; Q-0143 to the letter). The trigger becomes required (`optional: false`) and the choice of 0 to N is asked alone. With Uruviel beside the site the player is then asked to order two required triggers before he is asked either choice; what is asked with no Hobbit wounded has not been probed. Uruviel's test of the Valley is rewritten.

**Built the same way**, by a search of the definitions for an optional trigger with no cost and a choice whose minimum is 0, and the answer would be carried to them: Longbottom Leaf 1_300 ("When you play this possession, you may discard up to 2 cards from hand") and Uruk-hai Healer 14_14 ("At the start of each assignment phase, you may heal an Uruk-hai for each companion you can spot"). Not these: the "you may shuffle up to N ... into your draw deck" triggers (Q-0102: their two noes differ) and the takes from a draw deck (F-0049).

**Parked on it:** nothing. `Card_01_067_Tests.MovingToHerOwnersValleyOfTheSilverlodeOffersTheHealOnceForTheSiteAndOnceForHerCopyAndMovingOnOffersNothing` reads the game as built (both Hobbits chosen under each offer), stays active, and passes under A and B.

**Recommendation:** B. It keeps the "may" an offer, as you had 13_75 made, and takes away only the empty answer behind it.

**Ketura (2026-10-11):** Hrrrrm.  There are rules surrounding optional/required actions as you point out, so I think we are required to keep this an optional trigger.  In this case there is no downside to healing, but if we're following a general rule I would rather we click on the card to trigger it (such actions don't use popups) and then have the option to select 0-N, if one were to change one's mind (for example, the Longbottom Leaf we may click the card for the trigger, and only after thinking about it decide that none of the cards are good discard candidates and choose 0).  So this one is A.


### Q-0224 The trial of Q-0034 in the engine is back: it fails your test as the engine stands (42%) and passes it with thirteen kinds of effect taught (89%). Adopt?

From the orchestrator: the trial you agreed to under Q-0215 has run (one agent, six and a half hours, on the branch `trial/no-effect-withhold`, which is NOT in your bundle). Its report is first on the Reports tab, `trials/no-effect-withhold.md`, with its tables beside it. Nothing of it is on the campaign's branch. This entry is the decision; entry A of Q-0220 waits on it.

**Your test** was: "if it requires the engine fix and still a bunch of card edits then we're right back to where we started."

**What was measured.** The rule: an optional trigger, or an ability of a card in play, that pays nothing and whose every effect says it will do nothing, is not offered. Then each of the 53 cards the campaign had corrected by hand for Q-0034 had its hand-written requirement taken out again, and its own tests were run.

- **With the engine as it stands, the rule fails your test.** It covers 22 of the 53 (42%); taking the no-effect flag exactly as it is sent, 14 (26%). The flag was made to warn about events and costed abilities, and most effects cannot say beforehand that they will do nothing.
- **With thirteen kinds of effect taught to say so, it passes.** 47 of the 53 (89%). The teaching is built on the trial branch, about 240 lines, 4 to 25 a kind; the 47 is measured, not estimated.
- **Wrongly withheld: none that the suite shows.** 16 of 10,149 tests change, and all 16 assert an offer that does nothing: 13 cards, 10 of them empty offers nobody had filed (three on helper cards in landed tests: Brego 4_263, Bill the Pony 13_143, Shadowy Mount 12_171).
- **The Decipher sets.** About 190 to 200 unworked cards have the fault (30 were put on a board; 22 had it). The rule taught puts right 8 or 9 in 10 of them with no edit, leaving roughly 25 to 35 cards to correct by hand; untaught, roughly 75. Those figures rest on a sample of 30: sure of the direction, within about 20 points on the numbers.

**What I would have you weigh against it.** (1) The thirteen taught forecasts have no tests of their own yet; they are held only by the suite and by 32 boards where the offer can do something. (2) The rule is only as right as each effect's own check: a wrong "I can do nothing" takes a legal action away, where a wrong requirement on one card harms one card. None was found; that is not the same as none exists. (3) The rule as built also reaches a "you may" INSIDE another action, an event's text included (not the event's own offer, which is untouched). That goes past what you agreed to and is yours to strike.

- **A. Adopt it, taught.** One engine item brings the three commits onto the campaign's branch with the rule on: a `mechanics/` test for each taught kind, the 16 tests turned to read "not offered", the 10 unfiled empty offers filed and closed by it. The 53 requirements already written stay (they do no harm). The cards it does not reach are corrected by hand as they are met. Say with A whether the "you may" inside another action stays in (**A1**) or comes out (**A2**).
- **B. Leave it.** Card by card, as now; the branch is kept for reference.

I recommend **A2**: it passes your test by a wide margin and found ten faults on the way, and the inner "you may" is the one part nobody asked for. I would not adopt it untaught.

**Ketura (2026-10-10):** I had to spend some time going over this, but if I am understanding things correctly: you would like to "teach" a variety of game effects how to report noeffect, to 80/20 the work so that it's majority not work held only in the card definitions?  If that's an accurate read of the report, then yes, I approve it.  I think I agree with your rec for A2.


### Q-0225 Keep or strike: a Free Peoples player's own assignment action (Éowyn 17_96, Into the Caves 17_100, Alcarin 5_31, Sméagol 7_71) is not offered for a companion who already has a minion, whatever his defender bonus (F-0355)

CR 4.2, "assignment phase": "Many assignment actions assign a minion to a companion. You cannot do this unless both of them are unassigned." And "defender +X": "if your assigned companion has the keyword defender +1, you may assign that character to one additional unassigned minion."

Engine pass 24 (item `defender`, F-0355) made the definition word `assignableToSkirmishAgainst` stop counting defender, through the flag the engine's check already had (`ignoreDefender`, which the `AssignFpCharacterToSkirmish` effect has always had on by default). The finding and the lander's line spoke of the Shadow player's side: a minion's "Assign this minion to bearer of Reckless Pride" offered for a bearer with defender +1 who already has a minion. This entry is the one thing built wider than those words.

**What was built.** The word leaves defender out whichever player asks, so the Free Peoples player's own assignment ACTIONS are held to "both unassigned" as well. Four cards have such an action written with the word:

- Éowyn, Northwoman 17_96: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that minion lose all game text keywords and unable to gain game text keywords until the regroup phase."
- Into the Caves 17_100: "Assignment: Assign a minion to Frodo to make that minion lose all game text keywords of your choice ..."
- Alcarin, Warrior of Lamedon 5_31: "Assignment: Assign Alcarin to a minion bearing a [gondor] fortification to heal Alcarin."
- Sméagol, Always Helps 7_71: "Assignment: Assign a minion to Sméagol and add 2 threats to exhaust that minion."

**What changes in play.** Only one board: the companion the action assigns to (the Ring-bearer, Frodo, Alcarin, Sméagol) has defender +1 or more AND already has a minion, put on him by a Shadow assignment action. Before, the action was still offered there (seen with Éowyn: with the word left counting defender for the Free Peoples player, the test below fails at her offer). The effect itself has always ignored defender, so by the definitions a use there assigned nobody and still took what the action takes before it: Éowyn's exertion, Sméagol's 2 threats. Now it is not offered on that board. Nothing changes for a companion without defender, or one who is unassigned.

The Free Peoples player's ordinary assigning (after both players pass) is untouched: there defender counts, and he may still give that companion one more minion. `mechanics/DefenderTests.TheFreePeoplesPlayersOwnAssignmentActionGivesACompanionWithDefenderNoSecondMinionEither` shows both halves with Éowyn, Gimli as a Ring-bearer made defender +1 by Disquiet of Our People 2_2, and Reckless Pride.

- **Keep.** Nothing to do.
- **Strike** (if a Free Peoples assignment action should be able to use a defender bonus, or should stay on offer there). One argument in `FilterFactory`, at `assignabletoskirmishagainst`: the `true` for `ignoreDefender` becomes `side == Side.SHADOW`. The four actions are then offered again on that board and still assign nobody, unless the effect is changed as well (`ignoreDefender: false` on each of the four definitions), which would let the action add a second minion to a defender +1 companion; and the DefenderTests test above is turned round. The Shadow side of F-0355 stays as built either way.

The change is made and stays unless you strike it.

**Ketura (2026-10-10):** Very good, keep.  That's one of the more common mistakes people make, that assignment actions can't actually be used like normal assignments due to the rules restricting their use.


### Q-0226 Pathfinder 1_110, and every card that plays a site: may a site be replaced by another player's copy of the same site? (CR 4.2 "site": "You can't replace a site card with the same site card")

Pathfinder 1_110: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary)." CR 4.2 "site", on replacing: "To replace a site, choose a new site from your adventure deck and place it on top of the site you are replacing on the adventure path. Take the old site from beneath the new site and place it in your adventure deck. You can't replace a site card with the same site card." Two paragraphs above it: "You may play a copy of a site on the adventure path even if an opponent's copy was already played as an earlier site and is still active. The copies are treated as different sites, with each given a different site number."

**Built now.** Nothing looks at the title. The engine's site-playing effect (`PlaySiteEffect`, behind all 40 site-playing texts in 28 definition files) offers every site of the wanted number in the player's own adventure deck, and the old site goes back to its owner's. So with Ettenmoors lying as site 2 from my opponent's deck, my Pathfinder may put my own Ettenmoors there in its place. The converter saw it on a probe (player 1's Pathfinder lays his Ettenmoors at site 2; on player 2's turn, player 2's Pathfinder replaces it with player 2's Ettenmoors), and an old test of yours already does it: Silinde 1_60's `ReplacingOpponentSiteWithYourSiteStartsCopy` swaps the opponent's site 3 for the player's own copy of the same site, and Silinde ("While you can spot your site 3, Silinde has the game text of that site") then has its text.

- **A (as built): "the same site card" is the very card.** An opponent's copy is another site ("the copies are treated as different sites"), so one copy may replace the other, and whose site it is changes though the title does not. The sentence then forbids only what cannot happen (the card on the path is not in an adventure deck). Nothing changes; Pathfinder's parenthesis can be tested on the usual table.
- **B: "the same site card" is a site of the same title.** Ettenmoors may not replace Ettenmoors, whoever owns each. An engine change: a site of the title that lies there is not offered, and a site-playing card with nothing else to offer for that number replaces nothing (its cost still paid, as with a site that cannot be replaced). The tests that replace a site with its twin are turned round or moved to a table whose two adventure decks differ: on the test table both players hold the same nine sites, so in a numbered block every replacement there is by a twin (Silinde 1_60's two replacement tests; those of Thrór's Map 1_318, Get On and Get Away 4_304 and its erratum, One Good Turn Deserves Another 11_49, There's Another Way 12_40, Mount Doom 15_193 and Doorway to Doom 68_134 want looking at). That table is a rig request, filed only if you answer B.

Nothing is parked. Card_01_110_Tests has one test, the play onto an empty place (site 2 comes from the player's own adventure deck, the opponent's stays in his); the survey made the parenthesis optional and the converter left it for the card's own batch because of this doubt.

Recommendation: A, lightly. It is what the game has always done and the paragraph on copies reads that way; against it, the last sentence says nothing at all under A.

**Ketura (2026-10-10):** I had to go do a deep dive on the rules to see when this was introduced (and now I have another instance running some analysis on rules eras, stay tuned).  I think this was added in 2005, i.e. around set 12-13.  We may need to add a new format rule switch for this as a result.  For now park this while the analysis continues and while I inquire at the PC; we may have a whole slew of such rulings to add to the pile.


### Q-0227 Keep or strike: Held 6_109 no longer corrupts Frodo at site 9 while he is hindered (it follows from the filter you named, F-0356)

From engine pass 24, item "cards" (F-0356). The change is made and stays unless you strike it.

**The card.** Held 6_109: "Bearer must be Frodo. Each time Frodo is about to be killed by a wound, add a burden instead. When the fellowship moves to site 9, Frodo is corrupted. Regroup: If you can spot no minions, discard this condition."

**What you ruled.** The site 9 sentence corrupted the Ring-bearer whoever he was; "it should be 'Ring-bearer, title(Frodo)'". Built as you said: the trigger now requires `canSpot` with `ring-bearer,title(Frodo)`, and two lone tests in Card_06_109_Tests hold it (borne by Frodo the Ring-bearer, the move to site 9 ends the game by corruption; borne by Frodo, Frenzied Fighter 13_149 beside Gimli as Ring-bearer, nobody is corrupted and the Shadow phase at site 9 follows).

**What comes with it, which you did not say.** A filter does not find a hindered card. So with Frodo the Ring-bearer HINDERED as the fellowship moves to site 9 (For the Shire 103_108, One Ill Turn Deserves Another 103_38 and Ill Wind 103_67 can hinder him), Held no longer corrupts him: probed, the game goes on into the Shadow phase at site 9. Before the change the effect had no filter and he was corrupted hindered or not. This is the reading of Q-0103 ("while Frodo is hindered, he himself can't be [spotted]": burdens are not "on Frodo" then), applied to "Frodo is corrupted". No test reads it either way. Only Held behaves differently; no other card has this sentence.

- **A. Keep.** A hindered Frodo is not "Frodo" for this card, as for For Frodo 103_107. Nothing more to do.
- **B. Strike that part.** Held corrupts a Frodo who is the Ring-bearer hindered or not: the requirement gets `hindered: true` (the word Q-0098 built for CanSpot), and a third lone test reads it. The rest of F-0356 stays.

I recommend **A**: it is what the filter you named does, and it agrees with Q-0103.

**Ketura (2026-10-10):** Hrrm.  I think that corruption is basically this game's way of saying "you lose the game".  On the other hand, it specifically applies to Frodo.  Tricky, tricky.  I think for now we'll go with A until someone complains, lol.  Held has always been a card about utilizing it until you counter it at the end, and hinder is one potential counter.


### Q-0228 Keep or strike: a minion may be cheated onto the table in the fellowship phase so that a standing text can be read on it there (the Card Test Standard, "A board a game could reach")

From engine pass 24, item "triage" (the lines landers passed up about the Card Test Standard). The sentence is written and stays unless you strike it. No test, definition or engine class changed.

**What was unclear.** Since your answer to Q-0213 the Standard has a rule, "A board a game could reach", which begins: "The cheats shorten a set-up; they do not make a board no game arrives at." Its check F already said, of the opponent's Man as a candidate for a bare "a Man" chosen in a fellowship phase: "no minion is in a fellowship phase: then Notes says none can be, and no board is made for it". Read together, the two could forbid something the letters A to L of the audit never named and the tests do often: minions cheated onto the table straight after `StartGame()`, in the fellowship phase, where no game has a minion, so that a standing text can be read on them before and after a Free Peoples card is played there. A Blended Race 2_16's converted test is the case that raised it: the Uruk Savage reads damage +1 and strength 5, the condition is played from hand, he reads no damage bonus and strength 3, and the Cave Troll beside him reads as it did. A rough search finds 49 tests in 41 landed card files that make a Free Peoples play or use an ability in the first fellowship phase with a minion already on the table; 20 of them, in 18 files, go on to read that minion there (`orchestrator/engine-pass-24/triage-fellowship-minions.tsv`). All but one of the 18 are PC cards' files, most of them approved.

**What the Standard now says**, as one more sentence of that rule: "When a card is laid is another matter: cards that stand in play together later in the turn may be laid earlier in it, so a minion cheated onto the table in the fellowship phase, to read a standing text on it before and after a Free Peoples card is played there, is a shortening (Ketura, 2026-10-08: the cheats "short-circuit more-or-less legitimate setups"). It is still no candidate for anything chosen or done in that phase (check `F`)."

**Why.** The same cards stand together lawfully from the Shadow phase of that very turn, and a standing text reads the same on them there; all the cheat skips is the move and the play of the minion. Nothing about any card is made up, which is what A to L were about. What stays out is the minion as a CANDIDATE: something a fellowship-phase action or event chooses or acts on could never find a minion, so check F's sentence stands as it was.

- **A. Keep.** Nothing more to do.
- **B. Strike.** A card is on the table only in a phase in which a game could have it there. The sentence is replaced by its opposite. A new test plays the Free Peoples card, lays the minions once the Shadow phase has begun and reads them against their printed numbers or against a second table without the card; and the landed tests that read a minion in a fellowship phase (the 20 found, and any the search missed) join the relay of Q-0213's row K.

I recommend **A**.

**Ketura (2026-10-10):** A for sure.  The pre-game cheating is meant to keep setup concise and specific to the scenario so that we're not wasting a bunch of time going through setup that doesn't matter for the test.  There's definitely tension between that and "keeping it real", but it's a matter of striking the right balance.


### Q-0229 Hearken to Me 4_94: is "Discard all Shadow conditions on Théoden" done once when it is played, again when it is restored, or all the time? (rider to Q-0223)

Hearken to Me 4_94 (a [gandalf] condition on Théoden): "Bearer must be Théoden. Discard all Shadow conditions on Théoden. Shadow conditions may not be played on Théoden."

Council page LR 1 (Shadowfax 17_24) calls this "the first card to use that Andúril wording": a "discard X" sentence followed by a bar on X. On Andúril 7_79 and Shadowfax 4_100 the bar is "cannot bear", so nobody ever sees when the first sentence acts (LR 1 says so). Here the bar is only "may not be played on", so a Shadow condition can come to be on Théoden beside Hearken to Me in two ways, and then it shows.

**Built now.** The discard happens once, as Hearken to Me is played; the bar stands while it is in force. Probed at this landing:

- Hearken to Me is hindered on Théoden (Shifting Battle-lines 103_58, Gríma 55_51 and The Witch-king 103_80 can do it). Its bar is off, as CR 5.0 "hinder" says, and Weary 4_212 is played on Théoden. Hearken to Me is restored: Weary stays on him.
- A Shadow condition that reaches Théoden without being played stays on him (a transfer is not a play, Q-0053: Blade Tip 1_209, "transfer this condition from your support area to the losing character").

- **A: once, when it is played (as built).** Both conditions stay. Nothing changes.
- **B: again whenever its text comes back into force.** What F-0360 did for Andúril 7_79 and Shadowfax 4_100, after CR 5.0's "Upon being restored, Andúril's game text resumes being applied, upon which Aragorn's Bow would be discarded": the definition gets the same second trigger (restored, itself, the same discard) and one lone test (Weary played while it is hindered, gone when it is restored). Blade Tip transferred to him still stays.
- **C: all the time.** What the council ruled for Shadowfax 17_24 ("continuously discards ... they will immediately be discarded once attached"): a standing discard. Weary goes at the restore, and Blade Tip is discarded as it lands on him. One lone test for each.

Why Q-0223 does not settle it by itself: its answer A ("the bar itself discards") is about "cannot bear", which a card already borne breaks; "may not be played on" is not broken by a condition that is already there. Its answer B ("only a 'discard' sentence discards") gives B here.

Parked on it: nothing. The card's one lone test (from the old `at` test of the bar) reads only that Shadow conditions cannot be played on him.

Recommendation: B. It is what the two cards with the same pair of sentences now do, and it asks nothing of the engine. C is the council's reading of one card whose two sentences name different things; they noted that ruling the wording in general would touch other cards.

**Ketura (2026-10-10):** Hearken to Me is the proto-Anduril, which codified its sort of effect. You'll note that later cards use "cannot bear" on the second clause, because it famously became apparent that Blade Tip and Black Breath could still go on Theoden, because they weren't *played* but *transferred*. In this case we will apply that same GOTM ruling: the discard will be continuous and the poor-mans-cannot-bear poorly keeps them off. Oh, I do want to make sure: on GOTM you can see that there is a ConstantlyCheck trigger, but it has to specify the CanSpot requirement so that it doesn't constantly blip in the client on every action; that same structure and requirement will need to be used on all of these related cards.  ConstantlyCheck in general should be carefully restricted like this lest it be a nuisance to play with. So, C, and I would like C to be applied to all the related cards.


### Q-0230 Keep or strike: Dunlending Looter 4_11 is read stacked on a controlled site with the fellowship at site 2, a moment no game reaches, to keep the old test's "3 + 2 roaming - 2"

Dunlending Looter 4_11 ([dunland] Man, cost 3, site number 3): "When this minion wins a skirmish, you may stack him on a site you control. Shadow: If stacked on a site you control, play this minion. His twilight cost is -2."

The old `at` test (playFromControlledSite) put him on a controlled site 1 with the fellowship at site 2 and gave the pool exactly "3 (card cost) + 2 (roaming penalty) - 2 (discount from stacked)" = 3: he is played and the pool is 0. Its replacement, Card_04_011_Tests.StackedOnASiteYouControlHeIsPlayedInTheShadowPhaseForTwoLessAndStillPaysRoaming, keeps that sum (not offered at 2, offered at 3, the pool 3 to 0) and is active. It is laid on player 2's first turn, the only turn on which a fellowship is at site 2 and site 1 can be controlled.

No game reaches that board. He is stacked only by winning a skirmish; the first Shadow phase his owner has after that finds the fellowship at site 3 or later, where his site number is 3 and he does not roam; and no card raises a minion's site number in a Shadow phase. Every card on the board is real and is what it is printed as; what the cheat makes up is when he was stacked. "A board a game could reach" (your answer to Q-0213) says the cheats do not make a board no game arrives at; its list A to L has no row for this.

- **A. Keep.** The test stays as it is: the old test's own arithmetic, and the only reading of his discount and a roaming penalty together.
- **B. Strike the roaming half.** The test is relaid at site 4: not offered with the pool at 0, offered at 1, the pool 1 to 0 (3 - 2). That a minion played from a stack still pays to roam is then read where a game does reach it, as one more test of mechanics/RoamingTests: Goblin Swarms 1_183 ("Shadow: Play an Orc stacked here as if played from hand") with a [moria] Orc of site number 4 stacked at one site and played at site 3.

I recommend **A**: nothing on the board is invented but the moment, and the old test asked for exactly this sum.

**Ketura (2026-10-10):** So the thing is, this card has two halves.  *By itself* the card cannot get stacked on a site you control, but there are myriad ways of stacking other minions around, and it's reasonable to treat these two abilities entirely separately: one set of tests that is about the stacking, and another that is about the Shadow ability.  The two do not have to be tested in tandem. I think that A is the sane way to handle it.  The test doesn't teleport a site into the Shadow control area and try to do a bunch of weird shit around that, it just arranges the board state a little early.


### Q-0231 Frodo, Courteous Halfling 4_301 against Orc Inquisitor 1_268: is a no-cost "you may" still offered when another card's bar means it will do nothing? (rider to Q-0224)

Frodo, Courteous Halfling 4_301: "While you can spot 3 unbound companions, Shadow cards may not discard cards from your hand or from the top of your draw deck." Orc Inquisitor 1_268: "When you play this minion, you may make the Free Peoples player discard a card at random from hand."

**Built now.** With Frodo's bar in force and cards in the Free Peoples hand, the Shadow player who plays the Inquisitor is still given "Optional responses" with the Inquisitor's trigger, and a yes discards nothing. The old `at` test (courteousFrodoPreventsDiscard) took that offer and read the card still in hand; its replacement, Card_04_301_Tests.WithThreeUnboundCompanionsAShadowCardCannotMakeYouDiscardFromHand, does the same and is active. (With that hand EMPTY the offer is withheld since this landing: F-0368, a requirement that counts the hand.)

- **A: offered, and it comes to nothing (as built, and as the old test played it).** Nothing changes.
- **B: not offered (Q-0034 to its letter: a "you may" with no cost whose whole effect would do nothing).** The Shadow player is asked nothing and goes on with his Shadow phase. No definition word can say it: a requirement can count the hand, but none asks whether this card may discard from it. The engine can: the random discard's own check already reads both the empty hand and Frodo's bar. So B is one more kind of effect for the rule of Q-0224 to be taught (the trial's table lists the Inquisitor among the cards its thirteen kinds do not reach), and it would then hold for every no-cost "you may make ... discard" under Frodo 4_301 and Frodo, Little Master 19_28. If Q-0224 is left (its B), B here wants a requirement word made for it.

Parked on it: Card_04_301_Tests.WithThreeUnboundCompanionsAShadowCardsOptionalDiscardFromHandIsNotOffered, which asserts B on the same board. The active test asserts A.

Recommendation: B if the rule of Q-0224 is adopted (a few lines there, and the two tests change places); A otherwise. A word made for this one pair of cards is the card-by-card work Q-0215 set out to avoid, and the offer hides nothing from the Shadow player, who can count the three companions.

**Ketura (2026-10-10):** I agree with B.


### Q-0232 The One Ring when its bearer is killed: does "never discarded, for any reason" (Q-0109) reach the Ring that goes to the discard pile with a dead Ring-bearer?

You ruled on Q-0109 (Doom Causeway 103_127, 2026-10-07): "The Ring-bearer and One Ring should never be discarded for any reason." The game holds that against every card: no discard offers or takes the Ring, hindered or not (mechanics/TheOneRingIsNeverDiscardedTests). There is one place where the Ring still goes to a discard pile, and it is the rules' own doing, not a card's. Comprehensive Rules 4.2, "leaving play": "When a card (other than a site) leaves play for any reason, any cards played on that card (or borne by or stacked on that card) are discarded."

**Built now** (probed in engine pass 24; the lander of rtmd-b94_1 saw the first of these for Frodo and for Galadriel 9_14):

- The Ring-bearer is killed and nobody takes the Ring. He goes to the dead pile, The One Ring goes to its owner's discard pile with the other cards he bore, and the game ends. The table of the finished game shows the Ring in the discard pile.
- Frodo is killed with Sam, Son of Hamfast 1_311 beside him ("Response: If Frodo dies, make Sam the Ring-bearer (resistance 5)"). While Sam's response is on offer, in a game that is still going, the Ring lies in the discard pile (the pile counts one more card) and the game has announced it as a card discarded from play. When the response is taken the Ring is fetched from the pile onto Sam. Declined, the game ends as above.

**What can tell.** Nothing in play today. Every trigger in the definitions that answers a card discarded or removed from play names a side, a culture or a kind of card, and the Ring has none of them. I found no card that can do anything to it in the pile during Sam's window, but I did not go through every effect that reads a discard pile. A player sees the Ring drop into the pile and come back. A later card that answers "a card" discarded from play, or one borne by a companion, would answer the Ring here, as Doom Causeway did when it was hindered.

- **A: leave it as built.** "Never discarded" is read as: no card or effect discards the Ring. The Ring going with its dead bearer is the rules' sentence above, and no game comes out differently. Nothing changes; I would add that reading to the rulings log under Q-0109 in your words.
- **B: never means never.** The Ring is left out of the cards discarded with a dead Ring-bearer and is held until a new Ring-bearer takes it or the game ends, with no discard announced for it. That is an engine job in the kill and in the hand-over to a new Ring-bearer, and it needs one thing settled first: where the Ring rests meanwhile (on the dead Ring-bearer in the dead pile, or out of play). One mechanics test would show it: at Sam's offer the Ring is not in the discard pile.

Parked on it: nothing. No test reads where the Ring is at those two moments.

Recommendation: **A**. No outcome differs, the rules say the borne cards are discarded, and B changes the kill of a Ring-bearer for the sake of what the table shows for one response.

**Ketura (2026-10-10):** So while I was reading this, I first thought "why would it be anything but A", but the more I thought about it, the cooler it seemed like it would be if the game somehow kept The One Ring in the spot where the Ring-bearer was before being killed, sort of underlining "the Ring is now undefended, right there for the taking", a visual mark of successfully winning the game. So let's see if we can have some fun with it.  If the Ring-bearer is killed, let's ensure the One Ring is moved to the same position the Ring-bearer previously occupied, including in skirmishes (as the skirmish UI will very frequently leave a finished game in the state of the last skirmish with a blank spot where the One Ring used to be).  Don't worry about the front-end for this, just take a stab at ensuring the server issues the proper PCIP etc events down the chain.  When Frodo dies and Sam is in play with his ability, there should thus be a brief moment (until the response is accepted by the player) where the Ring is where Frodo once was, before Sam's ability kicks in and transfers, with gameplay resuming as normal. So I guess this is B+, haha.


### Q-0233 Keep or strike: the five tests of whether a definition is loaded (a time that names no phase, a filter on PutPlayedEventOnTopOfDrawDeck, a playerIs with no name) moved from mechanics/ to plain JUnit in the logic module, where cardtest.py suite does not run them

Engine pass 24, item fixes-c (bringing the older classes under `mechanics/` to `lint.py --form`), commit 59d507e4c. Already done; it stays unless you strike it.

Five tests in three mechanics classes play no game. Each hands `LotroCardBlueprintLibrary.loadCardsFromFile` a few definitions written in the test and reads which of them the builder kept: a time that names no phase ("end(turn)") and a `DuplicateActionFromPhase` phase that is none (the three tests of `UnknownPhaseInADefinitionTests`, engine pass 16); a `filter` other than "self" or "played" on `PutPlayedEventOnTopOfDrawDeck` / `...OnBottomOfDrawDeck` (one test of `PlacingTheEventPlayedOnADrawDeckTests`, pass 19); a `playerIs` requirement that lists no name (one test of `PlayerIsRequirementTests`, pass 21). The form check fails them for having no table from a `Get...Scenario()` method, and the first two for the helper methods that write their definitions. Giving them a table they do not use would be for show.

The Standard, "Tests that are not about a card", second paragraph: a test of a server class with no game is "plain JUnit beside the class it tests, in that class's module (`RTMDGameInfoTest`, `MultipleChoiceAwaitingDecisionTest`)", and the rules of form are for tests of the game. So the five now stand beside the builder, in the logic module: `gemp-lotr-logic/src/test/java/com/gempukku/lotro/cards/build/UnknownPhaseInADefinitionTests.java` (moved whole), `PlacingTheEventPlayedOnADrawDeckDefinitionTests.java` and `PlayerIsRequirementDefinitionTests.java` (one test each, taken out of the mechanics class, which keeps its tests of the game). No definition, no assertion and no test name changed; their helper methods went with them.

What it changes: `mvn test` runs them as before (the logic module's tests ran green: 5 tests in the 3 classes). `cardtest.py suite all`, the campaign's own gate, runs the server module's tests only, so those five are no longer in it: a change to the builder that broke one would be seen by a build and not by an agent's suite run. `pitfalls.md` ("Definitions of the tests' own") and `hjson-reference` say where they are.

- **A. Keep.** They stay in the logic module.
- **B. Strike.** The three files go back under `gemp-lotr-server/src/test/java/com/gempukku/lotro/mechanics/` as they were (one class and two tests), `suite all` runs them again, and those three classes fail `lint.py --form` again (`scenario`, `helper`) until the check is taught to pass over a test that plays no game.
- **C. Keep them plain JUnit, but in the server module** (under `gemp-lotr-server/src/test/java/com/gempukku/lotro/` in a package of their own, say `definitions/`), so that `suite all` runs them and `mechanics/` holds tests of the game only.

I recommend **A**: it is what the Standard says for a test with no game, and the build runs them. C is the one to take if you want every agent's suite run to cover the builder.

**Ketura (2026-10-10):** Hummm.  You know what, I had never considered that, but it actually makes a lot of sense to have nuts-and-bolts testing of the HJSON layer (when performed) sequestered into a separate place.  I think I lean towards C; gemp-lotr-server is the only module with real tests, and it has begun to accumulate many end-to-end tests, not just of cards but also of the interface and other aspects of the system, never mind existing tournament/league tests and the like.  But yes, feel free to alter the scripts you use during testing to only run relevant sections (I doubt you need the league/tournament tests either).


### Q-0234 Many Miles 13_21: is it +2 once the fellowship has moved on from the site a Shadow player replaced this turn?

Many Miles 13_21 ([elven] Archery event, cost 2): "Spot 2 Elf archers to make the fellowship archery total +1 (or +2 if a Shadow player has replaced the fellowship's current site this turn)."

**Built now.** When a player who is not the Free Peoples player replaces the site the fellowship is standing on, the game sets a mark that lasts until the end of the turn, and Many Miles reads only that mark. Nothing takes the mark away when the fellowship moves on. A game reaches it: Úlairë Nelya 11_222 ("Shadow: Exert Úlairë Nelya to replace the fellowship's current site with a site from your adventure deck") replaces site 2 in the Shadow phase; in the regroup phase the fellowship moves again to site 3, which the Shadow player lays in the ordinary way and nobody replaces; in that second archery phase Many Miles takes the fellowship archery total from 1 to 3.

- **A. The site the fellowship is at when the event is played.** "The fellowship's current site" is site 3, and nobody has replaced it: +1 (1 to 2). The engine would have to change (remember which site was replaced, or drop the mark when the fellowship moves); it goes to an engine pass, and the parked test goes live as written.
- **B. As built: the act is what counts.** A Shadow player did, this turn, do the thing Nelya's text calls "replace the fellowship's current site", so the event is +2 for the rest of the turn wherever the fellowship now stands. Nothing changes; the parked test is turned to read 3 and goes live.

No rule or ruling settles it (CR 4.2 says how a site is replaced and nothing of this). The two active tests hold under either reading: nobody replaces, +1; Nelya replaces the site the fellowship is still at, +2.

Parked on it: Card_13_021_Tests.AddsOneWhenTheSiteAShadowPlayerReplacedThisTurnIsNoLongerTheCurrentSite (fails at the reading itself: expected 2, was 3).

I recommend **A**, lightly: "current" reads most plainly as the site under the fellowship now, and the card rewards archers for ground the Shadow player changed under them. It only shows on a second move in a turn in which the first site was replaced.

**Ketura (2026-10-10):** Ohhh, good catch, interesting.  I can see why it was implemented the way it was, but if "current site" is meant to literally be the site the fellowship is currently at (which seems beyond reasonable), then I think A is it.


### Q-0235 Keep or strike: the three old `at` tests you kept in 2022 as 'Legacy' classes inside Asfaloth 1_31's and Dwarven Axe 1_9's test files (under your note "these will simply have to stay") are gone with at/

The batch `sweep` (the last of the `at` conversion), already done; it stays unless you strike it.

Three old `at` tests were not in `at/` at all, and the survey missed them. On 2022-12-31 (efa6547367, "I'm reluctant to delete the older ones even tho they overlap quite a bit, partially because they poke at parts of the system I'm mostly unaware of") you moved them into their cards' files as a nested class, `public static class LegacyAtTest extends AbstractAtTest`, called from `Legacy_...` tests: `dwarvenAxeDoesNotFreeze` in Card_01_009_Tests (Dwarven Axe; the first `at` test, 2011), and `playDiscountAsfalothOnArwen` and `playDiscountAsfalothOnOtherElf` in Card_01_031_Tests (Asfaloth). Above the Asfaloth class stood your note: "These particular tests are pretty redundant with the new ones that check for twilight results above (not to mention somewhat less rigorous about it), but I am hesitant to delete these entirely only because they check for certain things around the guts that I do not--for instance, the specific existence of arguments instead of just card id counts, or the existence of the specific kind of card action choice. As I do not understand enough about the system to know what can and cannot be safely discarded, these will simply have to stay."

They were the last users of `AbstractAtTest`'s game-driving methods, so the base class could not go while they stood. Both nested classes, their three callers and the note are gone.

**What stands for each reading they made.**

- The two Asfaloth tests read: the pool 0 before the play; 0 after on Arwen 1_30, 2 after on Legolas 1_51; Asfaloth attached. The file's own `TwilightCost0OnArwen` and `TwilightCost2OnNonArwen` read the same pools (on Arwen, and on the Elf ally Erestor, chosen from three Elves), and at landing each was given the reading that Asfaloth is attached to the Elf chosen. To get Arwen or Legolas into play the old tests played her from the draw deck as the starting fellowship: that offer, its bounds and the empty pool as turn 1 begins are read in mechanics/StartingFellowshipTests.
- The Axe test read: Goblin Runner 1_178 played roaming at site 2 ("Twilight is -1-2+2 (cost, roaming, effect)"); Gimli with the Axe beats it; the two required actions (the Axe's discard and the wound for losing) are put to be ordered; the Axe first; the regroup phase is reached, the Runner dead and the card on top of its owner's deck discarded. Now: the Runner's play is its own test in Card_01_178_Tests (3 - 1 - 2 + 2 = 2, nothing asked: F-0009); the Axe-first order with the minion dead was already `AxeCausesDeckDiscardOnSkirmishVictory`; and two new tests in Card_01_009_Tests read the two orders the old name was about, a game that does not stall: the wound carried out first, so that the Runner is dead before the Axe's trigger is (its owner still discards the top card of his own deck, the Free Peoples deck untouched, regroup reached), and the same loss with no card in the Runner's owner's draw deck (nothing discarded from either deck, the Runner dies, regroup reached).

**What nothing reads by name any more: the "guts" of your note.** The old tests asserted the KIND of each decision and its raw argument lists: the starting fellowship is `ARBITRARY_CARDS` with a `blueprintId` list holding exactly "1_30"; a phase's actions are `CARD_ACTION_CHOICE` with a `cardId` list holding exactly Asfaloth's id; the ordering of required responses is `ACTION_CHOICE` with `blueprintId` "1_9" and "rules". No test of the campaign asserts a decision's type or parameters by name, and the Standard forbids it in a card test ("Only the rig": no decision parameters). They are held all the same, by every test at once: the rig answers each decision through exactly those lists (`cardId`, `blueprintId`) and refuses a card choice put to a decision of another kind, so a decision that changed its kind or lost a list fails every test that answers it.

- **A. Keep.** The legacy classes stay gone; the readings above are what stands for them.
- **B. Strike in part: one test that names the guts.** A small class beside the rig (not a card's file) that asserts by name the kind and the argument lists of those three decisions, so that the contract the web client reads is written down in one place. It would reach past the rig on purpose (a `// lint-ok` on each line, or three small rig readers by a rig request).

I recommend **A**: the rig is itself a client of those argument lists, and every test that plays a game answers through them.

**Ketura (2026-10-10):** I concur that those transferred tests were good to delete with A; they are now covered better elsewhere.


### Q-0216 Keep or strike: Race Text 94_30 stops a draw that an opponent's card has its owner make (Change of Plans 1_99)

"You may not play or take into hand cards from your discard pile or draw deck (except when reconciling or starting fellowship)."

**Built now (engine pass 23, with Q-0206 and Q-0207). It stays unless you strike it.** The bar is read for the player a text has draw or take, whoever's card that text is on. So the owner of 94_30 draws nothing outside a reconcile even when it is his OPPONENT's card that tells him to draw:

- Change of Plans 1_99 ("Regroup: Exert a ranger to make an opponent shuffle his hand into his draw deck and draw 8 cards") and Shield of the White Tree 18_57, against an owner who is the Shadow player: his hand goes into his deck and he draws none. Both are regroup cards, so his reconcile at the end of that phase then draws his eight.
- What Are We Waiting For? 2_15 ("make an opponent draw 2 cards. That player then chooses to either discard 2 Shadow cards from hand or skip his or her next Shadow phase"): he draws none, and then chooses.
- Gorgoroth Stormer 8_101 ("add a burden and make the Free Peoples player draw a card") and Force of Uruk-hai 11_184 ("make the Free Peoples player draw a card instead" of its wound), against an owner who is the Free Peoples player: no card is drawn; the burden is added and the wound is not taken all the same.
- Mellon! 15_31 (each player discards his hand and draws as many): the owner discards and draws none.
- The draws an opponent's card offers him ("each Shadow player may draw", "choose an opponent who may draw"): News From the Mark 6_96, Radagast 9_26 and 59_26, Denethor 10_28 and 60_28, Trusted Promise 18_37, Drawing His Eye 10_29. He draws none.

The same reading holds for a take: Stern People 7_251 and Safe Paths 6_44 are Free Peoples cards that say "each Shadow player may take up to N cards into hand from his or her discard pile", and a Shadow owner takes none. That was so before this pass as well.

**Why it is built so:** Q-0206's answer is that the owner draws only when reconciling, and in each of these the text has the owner himself do the drawing ("make an opponent draw", "each Shadow player may draw"). A New Light 17_15 is different in its words: there the Free Peoples player returns the minion, and the owner takes nothing (Q-0207, built: it returns the minion).

**The doubt:** your words on Q-0207, "such restrictions almost always get ruled to be treated differently when the opponent is acting (see discard prevention and the "forced" stuff)". A draw that the opponent's card forces on the owner could be read as the opponent acting.

**Striking it** would mean: a draw is barred only when the owner's own card, or the rules (muster), has him draw; an opponent's card that makes or lets him draw gives him the cards. It is one condition where a card is drawn (the player whose action it is against the player who draws), and the mechanic test `BarOnTakingCardsIntoHandTests.ADrawThatAnotherPlayersCardHasTheBarredPlayerMakeDrawsNothing` is turned round. Say with it whether Stern People and Safe Paths go the same way (the Shadow owner then takes his cards), or stay barred as they have been.

**Ketura (2026-10-10):** I approve of your reasoning; Keep.


### Q-0217 Race Text 94_30: is a companion exchanged out of your discard pile into your hand (Borne Far Away 10_14) "taken into hand"?

"You may not play or take into hand cards from your discard pile or draw deck (except when reconciling or starting fellowship)."

Borne Far Away 10_14: "Spot your Wizard to exchange a companion in hand with a companion in your dead pile or discard pile." It is the one card whose definition moves a card from a discard pile to a hand by an exchange (the dead pile is no part of 94_30's text).

**Built now (untouched by engine pass 23, which met it):** the exchange is not read as a taking. The owner of 94_30 plays Borne Far Away with Gimli in hand and Aragorn in his discard pile: Aragorn comes into his hand and Gimli goes to the discard pile (probed).

**A:** a card that comes into your hand from your discard pile by your own card is taken into hand, whatever the card calls it. The discard-pile half of the exchange is barred for the owner: with only the discard pile to exchange with, the event is played for nothing, and the dead-pile half works as ever. A small engine change (the exchange reads the same bar as a take), with a test in 94_30's file.

**B (as built):** an exchange is not "taking into hand"; the hand is no larger for it. Nothing changes.

Unanswered, B stays. One card reaches this board.

**Ketura (2026-10-10):** Woof.  I'm going to say that we treat this like the persona replacement of the V3 gollum cards, where a swap isn't a play/discard, so here it's not taken into hand.  I may come to regret this, but we gotta start somewhere.  B.


### Q-0218 Keep or strike: Race Text 92_19 in the deck builder: a Block or Set the player picks still narrows his site list

Race Text 92_19: "Your adventure deck may contain sites from any block. (You may either mix any Movie block sites together, or use any Shadow sites, but not both.)"

Engine pass 23 (item `deckbuilder`, F-0342) mended the deck builder's site list for a player who has this modifier, as you asked: with his league chosen and Sites asked for, the list holds sites of every block, less the format's X-list and the first printings of sites it plays as errata'd. This entry is one choice inside that which your words did not settle.

**What was built.** The override opens the FORMAT's test of a site and nothing else. The Block and Set dropdowns still narrow his list as they narrow everyone's. In PC Movie (probed over the whole library, and in `game/SortAndFilterCardsTest`):

- Block "Any", which is where the builder starts in PC Movie: every site he may use, 291 where 175 show without the modifier.
- Block "Towers Block (PC)": the Towers sites and no others, as without the modifier.
- No Block or Set entry of PC Movie covers the Shadows sets, so the 79 Shadows sites are found with both at "Any" (sorted by site number they come first, having none) or by name.

In Expanded, picking "Fellowship Block" lists the Fellowship sites alone; without the modifier it lists none.

The code as it stood handed the flag to the Block and Set filters as well as to the format's, and all three then hid every site (the fault of F-0342). Read as it was meant ("a site passes"), a block picked would not have narrowed sites at all for such a player: "Towers Block (PC)" would have listed all 291.

**What it costs.** A format with one block (Fellowship Block, Towers Block, King Block, the Hobbit format) has that block picked by the builder itself, so there he sees only that block's sites until he sets Block to "Any". PC Movie, PC Expanded, Movie and Expanded have several blocks and start at "Any".

- **Keep.** Nothing to do.
- **Strike.** One argument in `SortAndFilterCards.process`: the `null` handed to the block filter becomes `siteOverrides`. A block picked then lets every site of every block through for such a player (the Set dropdown still narrows), the one-block formats show everything at once, and `ABlockThePlayerPicksStillNarrowsTheSitesWithTheOverride` is turned round.

The change is made and stays unless you strike it.

Report: engine-pass-23/deckbuilder.md.

**Ketura (2026-10-10):** Keep.


### Q-0219 Race Text 92_19: with it 29 Hobbit sites and both printings of seven Shadows sites are legal in PC Movie, and are now on the deck builder's list: hold them out?

Race Text 92_19: "Your adventure deck may contain sites from any block. (You may either mix any Movie block sites together, or use any Shadow sites, but not both.)"

Met while mending the deck builder's site list (engine pass 23, item `deckbuilder`, F-0342). The list now shows a player who has this modifier the sites deck validation takes for him. Validation lets his sites off the format's block and off its sets (Q-0185: that is what lets a Shadows site into PC Movie), and holds them to its X-list and errata. Being let off the sets lets in more than Movie and Shadows sites. In PC Movie, counted over the whole library (298 sites; 175 are listed without the modifier, 291 with it):

- **The 29 Hobbit sites** (sets 30 to 33). On Q-0185 you said "As for the hobbit sites, they don't need brought in I think." Engine pass 21 read that as no work wanted and left them validating (Dol Guldur 32_46 validates among eight Fellowship sites). They validate still, and are now on his list.
- **Both printings of seven Shadows sites**: Anduin Banks 11_227 and 61_227, Cavern Entrance 11_232 and 61_232, Neekerbreekers' Bog 11_249 and 61_249, Courtyard Parapet 13_188 and 63_188, Doorway to Doom 18_134 and 68_134, Mithlond 18_136 and 68_136, Steward's Tomb 18_139 and 68_139. PC Movie has errata for sets 1 to 10 and so no word on these: either printing validates, and both are listed.
- **Rath Dínen 100_1**, of set V0, which PC Movie does not play.

In a format with no errata at all (Movie, Expanded, Fellowship Block) every PC errata printing of a site and every V-set site validates and is listed beside the first printing: 14 sites twice. Race to Mount Doom is not run there.

Nothing was changed for any of these. The list and validation ask one method (`DefaultLotroFormat.validateSite(site, overrides)`), so whichever of these you choose is made in one place and reaches both.

- **A (as built).** Nothing to do. In PC Movie he sees 29 Hobbit sites and seven Shadows sites twice among 291, and may play any of them.
- **B (hold out the Hobbit sites).** Under the modifier a site must be of the Fellowship, Towers, King or Shadows block, the blocks the parenthesis names, or of the format's own. One condition in that method: a Hobbit site in his deck draws "Site does not use supported block", and the list loses the 29. One test added beside `mechanics/SitesFromAnyBlockTests` and one in `game/SortAndFilterCardsTest`.
- **C (B, and one printing of each of the seven).** This needs your word on which printing a PC format takes for a site from a set it has no errata for. C1, the first printing only (a site from an errata set the format does not play is refused): one more condition, no format data, and he plays Anduin Banks as Decipher printed it. C2, the errata only, as PC formats play PC errata: PC Movie's definition would need a second list of errata sets (61 to 69) read for sites alone under the modifier, since adding them to its ordinary errata list would make every card of sets 61 to 69 legal in PC Movie.

Recommendation: B, which is how I read your words on Q-0185. On C I have no view: which printing is right is a matter of how the PC formats are meant to treat a block they do not play.

No test is parked. `game/SortAndFilterCardsTest` and `mechanics/SitesFromAnyBlockTests` leave Hobbit sites and these printings out of every list they read, so neither asserts A.

Report: engine-pass-23/deckbuilder.md.

**Ketura (2026-10-10):** Let's do B.


### Q-0220 Before the Decipher sets: which of these to pre-empt?

The brainstorm you asked for (Q-0215) is `orchestrator/trials/patterns.md`, with its searches beside it in
`trials/patterns-searches/`. It read all 357 findings into 25 shapes, searched the 3,083 unworked Decipher
definitions for each, and read the first ten matches of every search against the cards' texts. Nothing was run
in the game and nothing in the repository was changed. For most shapes the Decipher definitions hold few or no
more. What is worth doing first, lettered; answer by letter (yes, no, or a word):

- **A. Q-0034 once, before the sets.** 48 findings so far; the search finds 256 more definitions, 7 of the
  first 10 the fault when read (about 180 by that rate, one card in twelve). In the engine as far as the trial
  says it reaches, the rest in one sweep by kind of effect. **Recommended: do it**, once the trial has its number.
- **B. A "census" check in lint, and a sweep of what it found.** It compares what a text names (a culture with
  its kind, a kind of card, a thing done) with what the definition names. It found 17 faulty Decipher
  definitions nobody had pointed at (Old Differences 2_69 has no "skirmishing a [moria] Orc"; Pressing On 7_325
  never exerts its Hobbit; A Promise 12_128 has no "neither is exhausted"; the list is section 5), and in
  hindsight flags 16 the campaign has already corrected. **Recommended: do it.**
- **C. `mechanics/` classes for the rules only Decipher cards use, then a first batch of 59 cards.** Hunter,
  lurker, initiative, threats, enduring, damage +N, sanctuary healing, wearing the Ring, control of sites,
  stacking, tokens, playing sites and reconciling have engine rules and no class. 59 chosen cards meet every
  word of the definition language that 5 or more unworked cards use and the PC cards barely touched; in set
  order the last of those words is first met in set 19. **Recommended: do it.**
- **D. A choice's branches, once in the engine.** An `If`, a cost-to-effect or a play inside a `choice` cannot
  say it will do nothing, so the question is put with a dead answer: 18 findings, 35 more definitions, 6 of the
  first 10. **Recommended: do it.**
- **E. `attachedTo(<a kind>)` and a hindered bearer (Q-0108), once in the engine.** 27 Decipher definitions
  have it and no Decipher author will meet it. A bearer named by kind would not be found while hindered; one
  named as a card still would. **Recommended: do it; say "sweep" if you would sooner keep it in the definitions.**
- **F. One fixer's list of 21 definitions in shapes you have already ruled** (an X of exertions from zero, a
  missing `removedTwilight`, "limit 1 per site" from a phase limit, "up to 2" built as 2, "for each" built as
  `times`, and the four of G). **Recommended: do it.**
- **G. A ruling, once.** Where a trigger's own sentence says "you may" and the definition cannot be declined,
  or says no "may" and the definition asks: corrected where found with no question each time, Goblin Runner
  1_178 the one exception? Four are waiting: Final Count 4_69, Isengard Ruined 15_192 and White Hand Warrior
  17_136 are built required; Mountain Orc 11_129 asks with no "may". **Recommended: yes.**
- **H. Twins in one batch.** 153 cards in 65 groups share a text but for names and cultures; the queue would
  keep a group together so the second card starts from the first's ledger. Small (3% of the cards).
  **Recommended: do it; it is a rule in the queue.**
- **I. The Play clause's test by a generator.** 396 Play clauses and 226 stats claims have no finding beside
  any of their tests (half the findings could not be laid beside a test at all, so this is a floor).
  **Recommended: ask first; it is your Standard.** Say "generate", "keep" or "drop".
- **J. Seven lines for the authors' notes** on things that will otherwise be raised and are no fault (section
  2, e). **Recommended: do it.**

Not proposed, because the searches came back empty: wrong numbers, wrong phase words, reveals and shuffles,
durations, copied blocks, memories nothing writes. Those were PC faults or are already swept.

**Ketura (2026-10-10):** A-J are all yes.  I is "generate"


### Q-0221 Ulaire Nertea 69_38: the design for a cost of assigning (Q-0199: "look before we leap"), and its size. Build it for her and the three Morgul Orcs of set 7, for her alone, or not at all?

Ulaire Nertea, Dark Horseman 69_38: "The Free Peoples player must add a burden to assign Úlairë Nertëa to skirmish a companion of the named race." On Q-0199 you said the answer is closer to B (a cost in full: a prevented burden means no assignment), by "what is essentially an 'add additional cost' modifier" aimed at "any assignment action that lets free peoples assign to Nertea", and to look before leaping. This is the look. Nothing is built and no file is changed.

**What exists.** An extra cost exists for PLAYING a card and nowhere else: the modifier `extraCostToPlay` (Wrath of Harad 4_261, Balin Avenged 17_2, Better Than Nothing 31_19) and a card's own `extraCost` ("To play, ...", written 136 times). The game asks such a modifier two things: can it be paid (if not, the play is not offered), and add your cost to this play. Nothing adds a cost to using an ability, and a cost hung on the action would not do here anyway: with Éowyn 17_96 the minion is chosen after her action has begun, and with Shadowy Mount 12_171 the action is the Shadow player's. The cost has to sit on the assignment. The three Morgul Orcs of set 7 (7_197, 7_200, 7_201: "the Free Peoples player must exert a companion to assign this minion to a skirmish") use a third thing, `assignmentCost`: a yes/no as he starts his own assigning, paid at the yes whether or not he then assigns, and a bar until then.

**The design.** A new modifier, `extraCostToAssign`, the twin of `extraCostToPlay`: which minion, against which characters, an optional condition, and a cost the Free Peoples player pays. The game asks it the same two things.

- *Can it be paid?* Asked inside the one check every assignment already passes, so a pair he cannot pay for is refused to him everywhere (his own assigning, a card's choice, the bots, the rig). This takes the place of the bar Nertea has now for a hindered Ring-bearer.
- *Pay it.* At his own assigning: after he submits and before anything is assigned, each charged assignment is paid for in turn; then every assignment that was paid for or cost nothing is made at once, as now. At a card's assignment: once the pair is chosen he pays, then it is made. One effect is behind all 12 cards that have him assign (his own Alcarin 5_31, Sméagol 7_71, Crashing Cavalry 13_13, Éowyn 17_96, Into the Caves 17_100; the Shadow's The End Comes 1_169, Orc Assassin 1_262, Reclaim the Precious 10_22, Barbaric Uruk 12_135, Shadowy Mount 12_171, Borders of the Forest 33_30, Harry Goatleaf 101_38), so it is done once.

The Shadow player's assignments (leftovers; a minion's own "Assignment: Assign this minion to ...") are never charged.

**A cost not paid** (prevented, as by Melilot 3_110, or no longer payable when its turn comes): that one assignment is not made.

- At his own assigning the minion was never assigned, so it is a leftover and the Shadow player assigns it as he likes, for nothing (CR 4.2 "assignment phase": leftovers are made when the Free Peoples player is done). He is not asked again. Melilot there only hurts him: she is exerted, and Nertea goes where the Shadow player puts her.
- Éowyn: she is exerted, nobody is assigned, the minion keeps its keywords (the assignment is part of her cost; Q-0038).
- Shadowy Mount: nobody is assigned. There Melilot is worth it.

**Nertea, as a player sees her.** As now: nothing asked, a burden as he assigns her to the named race, again at the fierce assignment, and by Éowyn or the Mount. What changes: the burden comes before the assignment, preventing it stops the assignment, and it is no longer a trigger to be ordered against others (Spider Poison 8_28).

**The three Morgul Orcs, if moved to it.** No yes/no ahead. The Orc is offered whenever a companion can exert; he assigns it, is asked which companion exerts, and it is assigned. He pays only if he assigns it (today a yes is paid even if he then leaves it). His own cards and The End Comes can take such an Orc, exerting then; today they cannot take one at all, which is F-0308's hole the other way round. He loses the question, and so the way back from a mistaken drag. `assignmentCost` would have no card left and comes out.

**Size.** One ordinary engine item, not a rewrite: two new classes and five touched, about 200 lines. The Free Peoples player's assigning gains about 30 (pay between his answer and the batch; an unpaid minion goes back among the leftovers), the builder of a card's assignment about 12; the rest is the modifier and its queries. `AssignmentEffect`, `AssignmentPhaseEffect` and the Shadow player's assigning are not touched, and the phase keeps its steps. No rig change, no client change. Tests: 69_38 should keep 12 of its 13 and gains three for a prevented burden; the Orcs' five old tests are re-laid; one new mechanics class on Decipher cards.

**Two details I would build this way unless you say otherwise.** (1) Several charged minions assigned at once are paid for in card order, unasked; with two Morgul Orcs and one exertion left, the second is unpaid and goes to the Shadow player. (2) A Shadow card that makes him assign her to the named race charges him unasked, as you ruled on F-0308: he may prevent the burden but not decline it.

- **A. Build it for all four cards** and retire `assignmentCost`.
- **B. Build it for Nertea alone.** The Orcs keep their question; one printed sentence then has two builds.
- **C. Do not build.** A prevented burden leaves her assigned, as today.

Recommendation: A. The engine work is the same size as B, and the three Orcs are in the queue of Decipher cards with existing tests, about to be worked and asserted as built; deciding now saves doing them twice. B if you would rather not change how three Decipher cards have always asked.

Nothing is parked. Report: engine-pass-23/nertea.md.

**Ketura (2026-10-10):** Excellent work.  A.


### Q-0222 Hinder and restore (mechanic; shown on Aragorn 1_89): is a bonus given "until the regroup phase" back in force when its card is hindered and restored before then?

CR 5.0 "hinder": "The entire game text of hindered cards does not apply. This includes triggers, special abilities, modifiers, keywords, and other effects." CR 5.0 "restore": "When a hindered card is restored ... it immediately returns to activity." Neither says what becomes of a bonus that was laid on a card for a time ("until the regroup phase") when that card is hindered and then restored before the time is up.

**Built now.** Aragorn, Ranger of the North 1_89, "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase": he uses it, is hindered (no defender while hindered), and is restored in the same maneuver phase: he is defender +1 again, and loses it at the regroup phase as he would have (probed at landing). Nothing in the engine ends an "until ..." effect when the card it is on is hindered; it is only out of force meanwhile.

- **A (as built): suspended.** Hindering switches the card off and takes nothing from it; what was laid on it for a time is in force again on restoring, until its own time runs out. It is how the rule treats tokens, borne cards and a hindered item's bonuses ("temporarily revoked"). The test stays as it is.
- **B: ended.** A restored card has only what it prints and what standing texts give it afresh; the defender +1 is gone for good once he is hindered. That is an engine item (drop the timed effects on a card as it is hindered), and the test's last reading of Aragorn turns to "no defender".

Test: mechanics/HinderedCardHasNoGameTextTests.AHinderedCompanionHasNoKeywordPrintedOrGiven reads A after the restore and is active; nothing is parked. The old at/ method (HinderedCompanionsLoseKeywordsFromModifiers) stopped at "no defender while hindered" and read neither.

Recommendation: A.

**Ketura (2026-10-10):** A was intended, yeah.


### Q-0223 Hinder and restore (mechanic; shown on Andúril 7_79): a text that says only "cannot bear X" is restored on a bearer who took up an X while it was hindered. Is the X discarded?

CR 5.0 "hinder", on a hindered item: "the game text of such items does not apply, so any restrictions that come from the game text of a hindered card are ignored. Example: If Aragorn bears Andúril, Flame of the West, he cannot bear Aragorn's Bow due to Andúril's game text. If Andúril is hindered ... Aragorn's Bow can now be played. Upon being restored, Andúril's game text resumes being applied, upon which Aragorn's Bow would be discarded."

Andúril 7_79 has two sentences that could do that discarding: "Discard other weapons he bears" and "[He] cannot bear other weapons". Shadowfax 4_100 has the same two ("Discard any hand weapon he bears. Gandalf may not bear a hand weapon."). Three cards that can be hindered on a bearer who is not have only the second kind:

- Elven Armaments 18_8: "To play, bearer must not be bearing any possessions. ... Bearer cannot bear any other possessions."
- Black Dart 8_69 (a Shadow condition on a companion): "... transfer this condition from your support area to an unbound companion. Discard a mount borne by that companion. Bearer cannot heal or bear mounts."
- Disarmed 18_46 (a Free Peoples condition on a minion): "Bearer cannot bear any Shadow cards. When this condition is transferred to bearer, discard all Shadow cards it bears."

**Built now.** Andúril 7_79 and Shadowfax 4_100 discard when a card restores them (F-0360, fixed at this landing); when a reconcile restores them they do not yet (F-0361, an engine fault, open). Shadowfax 17_24 and its erratum 67_24 are built with a standing discard and do it at any restore. The three above have no discard but the one their text ties to being played or transferred: while one of them is hindered its bearer can be given what it forbids, as the rule says, and when it is restored nothing is discarded, so the bearer goes on bearing a card the restored text says he cannot bear. (Read from the three definitions, which are built as Andúril's bar is; not run.)

- **A: the bar itself discards.** Whenever a "cannot bear X" comes back into force on a bearer who bears an X (a restore by a card or by a reconcile), that X is discarded, on every such card, whether or not its text has a "discard" sentence. One engine rule for all of them; it would take the place of F-0360's two triggers and of the mending F-0361 asks for, and gets a mechanics test for each kind (an item, a Shadow condition on a companion, a Free Peoples condition on a minion).
- **B: only a "discard" sentence discards.** Andúril and Shadowfax 4_100 discard because their text says "Discard ..."; a card that says only "cannot bear" refuses further plays and transfers and leaves what is already there. Nothing more is built for the three: Elven Armaments then sits beside a second possession, Black Dart's bearer keeps his mount, and Disarmed's minion keeps its Shadow cards. F-0361 is still mended, so that a reconcile's restore is answered.

Nothing is parked on this question. mechanics/HinderedBorneCardTests.AWeaponTakenUpWhileAnItemThatBarsItWasHinderedIsDiscardedWhenThatItemIsRestored (Andúril restored by its player's reconcile) is parked on F-0361 and goes live under either answer.

Recommendation: A. The rule's own general sentence wants no state on the table that a restored card forbids, and B leaves three.

(Orchestrator, 2026-10-10, after engine pass 24: F-0361 is mended, so Andúril 7_79 and Shadowfax 4_100 now discard on a reconcile's restore too; where "Built now" above says otherwise it is out of date. The question stands as asked.)

**Ketura (2026-10-10):** So.  Anduril's interpretation was altered with the Legacy Ruling on GOTM: we see in GOTM a situation where both clauses exist but they *don't* match the same target.  We therefore decided that both are in force as ongoing effects: the discard removes any cards matching the description that end up on the bearer, while the "cannot bear" prevents them from getting there in the first place (makes the targeting invalid).  In GOTM's case, an artifact hand weapon that was on bearer before GOTM was played would therefore not be discarded: the discard effect only hits possessions, and "cannot bear" only prevents new bearings. In Anduril's case, both clauses target the same kind, so it's a little more moot, but we still interpret them as two separate, unrelated effects that just so happen to share a target. So.  When restoring, Anduril in particular has a "discard" clause that immediately kicks in and banishes other cards (say, an Aragorn's Bow) that were played on him while Anduril was hindered, but in the case of something like Elven Armaments, because it only has the cannot-bear clause and lacks a discard clause, it will have to begrudgingly co-exist with other possessions played while it was hindered.  The other examples you list (including Black Dart), I would interpret as matching Anduril, where they all contain an ongoing discard effect that makes the cannot-bear moot for the purposes of hinder/restore. Disarmed however also lacks an ongoing discard (it explicitly makes it an on-transfer trigger), so it would also permit the shadow cards to stay. So all that to say, B.


### Q-0215 The Q-0034 corrections: card by card as now, or once in the engine, from the game's own no-effect flag?

From the orchestrator, after the first `at` conversion batch (2026-10-10). Not about any one card, and not the Q-0034 shapes themselves (those are settled): about WHERE the correction is made, before the Decipher sets multiply it.

**What prompts it.** Q-0034 says an optional trigger or ability with no cost, whose whole effect would do nothing, is not offered. Every such card is corrected by hand, with a requirement written into its definition. Of the 640 cards worked so far, 57 findings cite Q-0034, most of them this correction. The first conversion batch replaced 16 old methods and turned up three more on Decipher cards (F-0347 Gimli 5_7, F-0348 Dervorin 7_88, F-0349 Aragorn 1_365), because the old tests asserted the empty offer. At that rate the Decipher sets (some 3,000 cards) hold a few hundred, each a finding, a definition change and one or two tests. And a card not yet worked goes on pestering players until its turn comes.

**What could be done once.** The game already works out the no-effect flag for the decision it sends. In all three findings above it had flagged the offer no-effect and offered it anyway. The engine could withhold by itself an OPTIONAL trigger or special ability that has NO cost and whose flag says the effect does nothing; events and anything with a cost are untouched, as Q-0034 has it.

**What I do not know.** (1) How many of the 57 the flag actually catches: I have the game's flag on record for the last few only. (2) How reliable the flag is. As a highlight a wrong "no effect" is harmless; as a bar it takes a legal action away, which is worse than a pester prompt, and you made it a highlight "which will permit players to make such actions should they deem it strategically necessary." (3) The exceptions it must leave alone: an optional draw from an empty deck stays on offer (Q-0029), and any other "let it ask" you have ruled.

- **A. Try it.** One engine item: make the rule, run the whole suite, and report which tests change (every offer the suite asserts that the rule would withhold, and every Q-0034 requirement in a definition the rule makes redundant). Nothing is kept until you have seen that list. The requirements already written stay either way.
- **B. Leave it.** Card by card, as now: the definition says in so many words when the card is offered, and the engine keeps no opinion.

I lean A as a trial only, because the list it returns answers (1) to (3) at the cost of one item; I would not adopt the rule on the strength of the three cards above.

**Ketura (2026-10-10):** I'm down for a trial on a separate branch.  I would want it specifically aiming to try to evaluate how widespread of a fix it would be; if it requires the engine fix and still a bunch of card edits then we're right back to where we started. Honestly I'm more concerned about trying to ensure that such ideas like this have been ferreted out before we do the big 3K.  The PC cards cover a large swathe of functionality, between the ~256 V-cards and hundreds of errata, so it seems like problem patterns we've found here should probably generalize, tho it seems apparent that more are likely lurking. In addition to the trial, let's pencil in a brainstorm task to see if there are more patterns we should try and pre-empt before the big chunk.


### Q-0214 Keep or strike: on the errata, keyword: and timeword: are now keywords: and timewords:, and five values capitalised another way are the Decipher spelling (Q-0158, 2A)

From engine pass 21, item "cardinfo" (your Q-0158: 1A and 2A), commit 6befeb255. Both halves are done as you answered: every erratum's `//id` is its own id (143 changed, and `cardinfo.py` takes that form only), and the errata have `unique:`, `side:` and the capitals written out. Two things in that commit go a step past the three you were asked about (no `side:`, no `unique:`, `type` and the like in lowercase). **Both are made and stay unless you strike them.** Nothing plays differently either way: the library loads 3,997 cards before and after, and every card's title, uniqueness, side, culture, type, race, signet, keywords, timewords, numbers, item classes and site fields read the same.

1. **The keys `keyword:` and `timeword:` are now `keywords:` and `timewords:` on 94 errata** (71 and 25 lines; two cards have both). Why: it is the other mark of the older style. Every Hobbit-set definition writes the singular, every V-set definition the plural, and 2,093 of the 2,101 such lines in the Decipher definitions are the plural; engine pass 17 already made it `timewords:` on 54_304 when it wrote that card out. `KeywordFieldProcessor` and `TimewordFieldProcessor` are registered under both names, so the game cannot tell. On 75 of the 96 lines the value was being given its capitals anyway (`keyword: support area` to `keywords: Support Area`); on 21 the key is the only change to the line (`keyword: Corsair` on 58_57, say), and four definitions are touched for nothing else: 58_51, 58_57, 58_59, 68_29. *Strike:* the 96 keys go back to the singular (the capitals stay), and the errata are again half one and half the other, as before (71 `keyword:` to 61 `keywords:`).
2. **Five values that were not lowercase, but capitalised another way than the Decipher definitions, are the Decipher spelling:** `itemclass: Hand Weapon` is `Hand weapon` on Dwarven Axe 51_9, Flaming Brand 52_32, Lurtz's Sword 52_43 and Ithilien Blade 65_62 (the other nine hand weapons among the errata, and all 135 Decipher ones, say `Hand weapon`), and `race: Uruk-Hai` is `Uruk-hai` on Uruk Regular 54_192. Why: so that a search for `itemclass: Hand weapon` or `race: Uruk-hai` finds every one. *Strike:* those five lines go back.

Not done, for you to know: The One Ring 69_1 has no `side:` (it has no culture, and no Decipher Ring has a side), so 92 errata gained one, not the 94 the count gave; `unique: 1` on Still Draws Breath 51_25 and `Hunter` with no number on 65_64 are as they were; the Hobbit sets, eight Decipher definitions with a singular key (3_67, 8_29, 11_49, 11_66, 12_11, 12_34, 12_75, 17_32) and four V2 keywords in lowercase (102_8, 102_16, 102_55, 102_64) were outside the item and are untouched. Report: engine-pass-21/cardinfo.md, with every changed line in cardinfo-written-out.tsv beside it.

**Ketura (2026-10-10):** I approve of all of those changes.  The keyword/keywords thing is because many cards have just the one keyword, and with array support on those fields it felt strange to me to have a singular field hold multiple values.  But I appreciate the standarization.


### Q-0213 Keep, replace or drop: the boards no game could reach, with what each has caught; and the count of name(Frodo) for the Ring-bearer, which is none (Q-0159, second half)

From engine pass 21, item "audit": the second half of your answer to Q-0159. Nothing is changed. Answer by letter, or "all as recommended".

**The count you asked for.** Definitions that say `name(Frodo)` or `title(Frodo)` where the text says "the Ring-bearer": **none.** 216 texts name the Ring-bearer; 41 definitions name Frodo in a filter, and in all 41 the text names Frodo. None in the tree before the campaign either, none ever put right in the definitions' history, no finding of the kind. (The Hobbit sets have no text to compare and are not counted.) The reverse, `ring bearer` where the text says "Frodo": two. For Frodo 103_107 means it (burdens are on Frodo only while he bears the Ring, Q-0103). Held 6_109 is the other: see L.

**What the made-up boards have caught.** 233 of the 640 landed test files lay at least one ad hoc modifier. Of 344 findings they produced 3, all the same slip (a race written where the text names the culture: F-0019, F-0052, F-0291), all on PC cards, none changing anything a game could show. Reading each definition's filters against its own text finds those same three with no board at all, and finds none in the tree today.

"Files" below is landed test files. "Slips" is definitions making the mistake today, of 3,720 with text, and in brackets when the campaign began.

**A. A Ring-bearer who is not Frodo, alone at the table** (the Standard asks for one such test on every card that says "the Ring-bearer"). 28 files. Guards against `name(Frodo)` for "the Ring-bearer". Slips 0 (0). Findings 0. The board is legal; it is the shadow. **Recommend: drop the requirement.** Tests written stay.

**B. A card given a culture or race it does not have** (a Man made [elven], Gimli made an Elf; lint asks for it on every definition with elf, dwarf or hobbit in a filter). 124 files. Guards against a culture written for a race or a race for a culture. Slips: culture for race 0 (0); race for culture 0 (3). Findings 3, as above. A board in play: for Hobbit and [shire], [rohan] and the Shadow cultures, printed cards exist (Pippin 103_31, Merry 103_85, Sméagol 103_116, Khamûl 103_61), in most of 48 files. For Elf and [elven], Dwarf and [dwarven] none exists: all 141 Elves are [elven] and all 53 Dwarves [dwarven], and the other way about (76 files, with the Free Peoples Wizards and Wraiths). **Recommend: replace with the printed card where there is one; drop it for Elf, Dwarf, Wizard and Wraith, and have lint report the definition** (it already spots the one slip ever made, and then asks for the board).

**C. A keyword given to a card no printed card with that keyword resembles** (an Orc made a Southron, an ally made valiant, a [rohan] Man made a knight). About 50 files. Guards against a culture, race or type in the filter that the text does not name. Slips 0 (0), bar one type word left out with no difference in play (Defiled 13_105). Findings 0. No board in play, by its nature. **Recommend: drop.** No new ones.

**D. A keyword given only to set a board up** (fierce in 41 files, archer, damage +1). 56 files. Guards nothing. Printed cards do the same (the helper table names them). **Recommend: keep**; new tests take the printed card.

**E. A number given by hand** (strength in 46 files; vitality, archery total, twilight cost and the like). 72 files. Guards nothing; each stands in for what some card does. **Recommend: keep.**

**F. Sites given a terrain** (every site a marsh, a forest, a sanctuary). 6 files. Guards nothing. Real sites of the terrain exist for five. **Recommend: replace with real sites.**

**G. Two cards of one unique title in play.** You struck this on 2026-10-08. It still stands in 13 landed files: 13_76, 51_15, 51_55, 51_282, 101_18 (nine Sarumans), 101_43, 102_77, 103_5, 103_11, 103_14, 103_15, 103_18, 103_75. Findings 0. **Recommend: relay them**, as the Frodo files were.

**H. Items where they could not be** (two hand weapons on one bearer; Narsil on Gandalf; The Pale Blade on a Goblin Runner). At least 7 files. Guards nothing; it is set-up for "bears 3 cards" or "each weapon". Items of different classes on lawful bearers do the same. **Recommend: replace.**

**I. Culture tokens put on a card nothing gives tokens to.** 1 file known. Finding 1 (F-0073), moot in play. **Recommend: no new ones.**

**J. A site of another block put into a path** (Redhorn Pass on a Fellowship path). At least 6 files. No deck holds that path outside Race Text 92_19, but the test reads the site's own text and bends no rule. **Recommend: keep.**

**K. For B and C, what becomes of the 163 files already written?** They pass and harm nothing. **Recommend: leave them, and take the modifier out when a file is next relaid for another reason.** Say "relay" if you want them gone now, as with the Frodo board.

**L. Held 6_109**, "Bearer must be Frodo ... When the fellowship moves to site 9, Frodo is corrupted", is built to corrupt the Ring-bearer. On Frodo, Frenzied Fighter 13_149 beside another Ring-bearer (the board of Q-0212) that corrupts the other one. Fault, or as meant?

Found on the way and filed as candidates: F-0345 (Boromir 18_40 fetches only a [gondor] possession where the text says "a possession") and F-0346 (Wish For Our Kinfolk 8_8 lets a Dwarf ally pay "Exert a Dwarf companion"). Both show on boards in play.

Report, with the searches and every file list: engine-pass-21/audit.md.

**Ketura (2026-10-10):** So in general I approve of the approaches recommended for each item.  In particular I think it's a waste of time to continue searching for *theoretical* malformed targets in places and arrangements where they have been historically sparse or even outright nonexistent. For item K, I would like a pass to alter the tests that have utilized those methodologies: if the case is legitimate (i.e. can be represented in a real card, such as the V3 Attea who is also an Easterling), then use such an example, but in the case where it's only theoretical and can't actually exist in the current card base...?  See if there's a nearby concept, but if not, delete the test. Item L looks like a bad implementation; it should be "Ring-bearer, title(Frodo)"


### Q-0212 Keep or strike: six tests keep a Frodo who is not the Ring-bearer, as Frodo, Frenzied Fighter 13_149, the one Frodo who can be in play beside another Ring-bearer (Q-0159)

From engine pass 21, item "standards" (your B on Q-0159). The change is made and stays unless you strike it.

**Done as you ruled.** Frodo, Son of Drogo 1_290 cheated in beside a Ring-bearer who is not Frodo was laid in fourteen landed files, not one. In nine of them he was only one more candidate for a text about "the Ring-bearer", and he is out; each ledger now says a Frodo who may bear the Ring cannot be on the table beside another Ring-bearer (CR 4.2 "Ring-bearer"). They are The Witch-king 52_85, O Elbereth! Gilthoniel! 52_108, Arwen 53_7, Phial of Galadriel 53_24, Orthanc Berserker 53_66, Sméagol 59_30, The One Ring, The Great Ring 69_1, Race Text 92_8 and 92_36. Nothing else those tests show was lost: where he was the card chosen or wounded, a companion already in the test (Sam, Aragorn, Arwen, Legolas) or Gimli, Son of Glóin stands in.

**What I did that you did not rule.** Q-0159 told you "no table in play has Frodo beside another Ring-bearer". That is true of every Frodo but one. CR 4.2 "Ring-bearer" bars "any version of Frodo with the Ring-bearer keyword", and Frodo, Frenzied Fighter 13_149 has none (CR 4.2, individual ruling: "This version of Frodo cannot become the Ring-bearer"). The game agrees: the rule code bars only Frodos that may start with the Ring, and a landed test (Two Eyes, as Often as I Can Spare Them 101_17) plays Frenzied Fighter from hand beside Gimli the Ring-bearer; For Frodo 103_107, For the Shire 103_108 and Get Up, Mister Frodo 103_111 lay him beside Isildur and Sam as Ring-bearers. So a Frodo who is not the Ring-bearer is a board in play, with that one printed card and no cheat against the rules.

In five of the fourteen files that Frodo is what the test is about, not an extra candidate, and taking him out would have deleted the test. There I changed the card and nothing else: 1_290 became 13_149, and the only assertion that moved is his resistance (8 where it read 10). The six tests:

- Sam, Faithful Companion 51_310, Sam, Son of Hamfast 51_311 and Sam, Of Bagshot Row 101_54: `ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled` (`...Dies` on 51_311). These test CR 4.2's glossary entry "Sam": "Any version of Sam that can become the Ring-bearer if Frodo dies can only become the Ring-bearer if Frodo was the Ring-bearer when he was killed", a rule that only this board can ever call on.
- Sting 51_313 ("Bearer must be Frodo", "Exert Frodo"): `PlaysOnAFrodoWhoIsNotTheRingBearerAndNotOnTheRingBearer` and `AbilityExertsTheFrodoWhoBearsItAndNotTheRingBearer`.
- Sam, Undaunted 103_115 ("Frodo is resistance +2"): `AFrodoWhoIsNotTheRingBearerIsResistancePlusTwoAndARingBearerWhoIsNotFrodoIsNot` (Frenzied Fighter 8 to 10, Gimli the Ring-bearer unchanged).

The Standard and the helper table now say: a Ring-bearer who is not Frodo has no Frodo beside him; only where the text under test names Frodo, and a Frodo who is not the Ring-bearer is what the test is about, is that Frodo laid, and he is Frenzied Fighter; never as one more candidate for "the Ring-bearer".

**Keep:** nothing more to do.

**Strike:** those six tests are deleted (Sting keeps a test that it cannot be played on Galadriel the Ring-bearer); the glossary rule "Sam" is left with no test on three cards, under a line saying you ruled the board out; and the sentence about Frenzied Fighter comes out of the Standard and the helper table. Say also whether the four V-set files that already lay him beside another Ring-bearer (101_17, 103_107, 103_108, 103_111; untouched by this pass) should lose him too.

Report: engine-pass-21/standards.md.

**Ketura (2026-10-10):** well done.  I had forgotten about Frenzied Fighter, and he is a valid use case in scenarios where we legitimately need to use Frodo contrasting with the Ring-bearer.  The principle is that I objected to exhaustively testing and re-testing every instance of "Ring-bearer" (which is a lot!) using cases that can't arise naturally, and which so far have found no actual bugs.  But you have found the right balance of adherence to the principle and practical application.  Keep.


### Q-0211 Keep or strike: the Playable requirement counts an event's own twilight cost (made for Little Snuffler 13_52, F-0058)

From engine pass 21, item "cards" (F-0058, Little Snuffler 13_52, which you ruled "good to fix"). The finding called it a definition fix; it needed three lines of engine code as well, which is why this is here. The change is made and stands unless you strike it.

**The card.** Little Snuffler, Shadow event, cost 2: "... If the fellowship is in region 3, play a [gollum] minion from your discard pile." Its play sits inside an `If` inside a `choice`, where the engine does not ask beforehand whether the play can happen (it asks only of a play that is an action's top-level effect). So in region 3 it was offered with any pool of 2 or more, took its (2), and played nothing when the pool was short of the minion or the discard pile held no [gollum] minion.

**The definition** now has a requirement of its own: not in region 3, or `Playable` with `fromZone: DISCARD`, `filter: culture(gollum),minion`. Cost and effect are as they were.

**The engine piece.** The `Playable` requirement asked "could this card be played with the pool as it is", which for an event is the pool BEFORE the event's own cost comes out: with Gollum (2) in the discard pile and a pool of 3 it said yes, and the Snuffler was still offered. `Playable` with `fromZone: DISCARD` now counts the twilight cost of the event that is asking as already gone, exactly as `PlayCardFromDiscard` does when the engine asks it beforehand (your ruling on F-0046: "It should block until both can be paid for"). Three lines in `Playable.java`, and an overload of `PlayConditions.canPlayFromDiscard` that takes the twilight to count as gone.

**What plays differently.** Little Snuffler only: in region 3 it is not offered with the pool one short of event and minion together, nor with no [gollum] minion in the discard pile (so it can no longer be played as a dud there, as no event whose whole effect is a play from the discard pile can). Regions 1 and 2 are as they were. No other card changes: the seven other definitions that use this requirement (Úlairë Nertëa 1_234 and 51_234, Black Sails of Umbar 8_50, Isengard Foreman 102_25, Uneasy Alliance 102_32, From Deepest Dungeon 102_39, Endless Night 103_96) are none of them events, and for anything but an event being asked whether it can be played the number counted is zero. The filter word `Playable`, `HasCardInDiscard`, and `Playable` from a draw deck, hand or stack are untouched (no event uses those three zones).

**Shown by** `mechanics/EventThatPlaysFromADiscardPileNeedsBothCostsTests` (4 tests: pool 3 not offered and pool 4 offered; played at 4, Gollum comes into play and the pool is empty; no [gollum] minion in the discard pile, not offered; region 1 needs only its own cost and Gollum spotted).

- **Keep.** Nothing to do.
- **Strike.** `Playable.java` and `PlayConditions.java` go back, and Little Snuffler needs another way to say "the pool pays for both": either its requirement is written with a fixed `discount: -2` (which is wrong whenever something changes the event's cost), or the engine is taught to ask beforehand about a play inside an `If` that reads only where the fellowship is, which reaches every card built that way. Until one of those is done the Snuffler is offered again at a pool of 2 or 3 with Gollum in the discard pile, and F-0058 is open again.

Report: engine-pass-21/cards.md.

**Ketura (2026-10-10):** keep, no notes.


### F-0342 Race Text 92_19: the deck builder lists no sites while a league with this modifier on its path is chosen (met in Q-0185's build)

Race Text 92_19: "Your adventure deck may contain sites from any block. (You may either mix any Movie block sites together, or use any Shadow sites, but not both.)"

Met while building Q-0185 (engine pass 21, item `sites`), and left alone: it is the deck builder's card list, not deck validation.

**What the game does.** `SortAndFilterCards.isInSetOrFormat`, in its loop over the format filter, has this for a site when the filter carries `siteOverride:true`:

    if (card.getCardType() == CardType.SITE) {
        if(siteOverride)
            continue;
        invalid = format.validateSite(blueprintId);
    }

`continue` goes to the next entry of the filter. With one format in the filter there is none, the loop ends, and the method answers false: the site is filtered out. So with the override every site is hidden, where the intent reads as "show sites whatever their block".

Probed on the server class at this pass's commit (`/tmp/ep21-sites/probe/ProbeSiteFilter.java`), over seven sites (1_331, 51_331, 1_327, 4_337, 11_239, 1_360, 32_46):

- `format:pc_movie cardType:SITE` returns 4_337, 1_327, 1_360, 51_331
- `format:pc_movie cardType:SITE siteOverride:true` returns nothing
- `format:multipath cardType:SITE` returns 4_337, 1_327; with `siteOverride:true`, nothing

The client builds that string (read in the JavaScript, not run in a browser): `deckBuildingUi.setRaceVisibility` sets `cardFilter.siteOverride` when ANY meta-site on the chosen league's path has the override, whatever place the player himself holds, and `CardFilter.calculateDeckFilter` adds ` siteOverride:true` beside the league's format whenever the card type filter is SITE. If that reading is right, every member of a Race league whose path holds 92_19 sees an empty list when he goes to pick a site with that league chosen in the deck builder. You will know at once whether that has been seen on the live server; if sites do show there, the client is not doing what I read and this is not a bug.

**A (mend it):** under the override the list shows the sites deck validation now takes: a site of any block, less the format's X-list and the first printings of sites it plays as errata'd (so in PC Movie Ettenmoors shows as 51_331 and not 1_331, as it does without the override). One line in `isInSetOrFormat` and a public method on the format for "this site under the any-block override", beside `validateSite`. Whether the flag should also follow the player's own place on the path, and not the whole path, is a second choice inside this one: say if it should.

**B (leave it):** nothing changes.

Nothing was changed. No test is parked: no card test reaches the deck builder's filter.

Recommendation: A, if the empty list is what players see.

**Ketura (2026-10-10):** yeah, let's amend this. I think it will warrant a few different sortandfilter tests emulating different formats and contexts, and yeah I also think that if possible it should only actually open up the site filter if the player is currently using that meta-site, which I *thought* was the case.


### Q-0197 Keep or strike: Rath Dínen 100_1's arrow is now Right as you ruled (Q-0181), but its released picture shows it pointing Left

Rath Dínen 100_1. You answered Q-0181 ("is the arrow Left (as defined) or Right (as Steward's Tomb)?") with "Ha, let's make it match Tomb I suppose." That question said nobody had looked at the card's picture. Engine pass 22 made the change and then looked.

**What the picture shows.** The released picture of Rath Dínen (https://i.lotrtcgpc.net/sets/vset0/LOTR-ENV0S001.0_card.jpg, stamped 2023.01.11) has the arrow pointing LEFT. The Tomb's erratum (https://i.lotrtcgpc.net/errata/LOTR-EN18E139.1_card.jpg, same date) has it pointing RIGHT. So the old `direction: Left` was what the card shows, and the definition now disagrees with its own picture.

**What I did (engine pass 22, F-0331).** As you ruled: `direction: Right` in V0-shadows-sites.hjson, and the stats test of Card_V0_001_ErrataTests asserts RIGHT. Nothing plays differently at a two-player table (the arrow says which neighbour lays the next site). The change stays unless you strike it.

**Keep**: the definition stays Right, to match the Tomb, and the picture is the thing that is wrong: it wants its arrow turned when you next touch the V0 images (as you are adjusting Mountain-troll's sheet row for Q-0168).

**Strike**: the picture is right and the card was meant to point Left. `direction: Left` goes back, the stats test's one line and its header go back to Left, F-0331 becomes not-a-bug, and the ledger's Notes say the arrow was checked against the picture. Nothing else in the pass is touched.

**Ketura (2026-10-10):** lol piss, I'll change the image.


### Q-0198 Keep or strike: a cost modifier written with a phase word (Orc Skulker 12_95) prices a two-phase event by the phase it is played in (with F-0292)

F-0292 is fixed as you said: an event printed with several phases is, while it is being played, an event of the phase it is played in and of no other, so Sword-wall 4_84 played in the regroup phase no longer brings the Ewer's offer. The fix is where `timeword(...)` is read, so it reaches every text written with that word about a card being played. The queue named the "played" triggers. Two definitions use the same word in a cost modifier, and I made them follow too. A change already made stays unless you strike it.
Orc Skulker 12_95: "Each skirmish event you play is twilight cost -1." Some Who Resisted 3_45: "Maneuver: Spot a [gondor] companion to make the twilight cost of each maneuver event +2."
**What changed:** the Skulker takes nothing off a Shadow event printed "Maneuver or Skirmish" (or "Shadow or Skirmish") when it is played outside a skirmish; played in a skirmish it still takes 1 off. Before, it took 1 off in every phase. In numbers: Two-faced 103_15 (cost 2) costs 2 in the Shadow and maneuver phases and 1 in a skirmish; No Man May Hinder Me 103_69 (cost 3) costs 3 in the maneuver phase and 2 in a skirmish. Flanking Attack 4_234 and We Must Have It 5_30 cost 0 and are unchanged on their own; after Some Who Resisted, Flanking Attack in the maneuver phase costs 2 where it cost 1. Some Who Resisted by itself plays as before. No Free Peoples event is touched (the Skulker says "you").
**Why:** the price is read twice, when the event is offered and when it is paid for. With the fix in the one reader, the second reading would have followed the rule and the first not: the Skulker's owner would be offered Two-faced for 1 and charged 2. So the cost had to go one way or the other, and by CR 4.2 "phase action" an event played in the maneuver phase is no skirmish event.
Shown by `mechanics/EventPrintedWithSeveralPhasesTests` (two cost tests among its seven).
**Keep** (what happens if unanswered): as above. **Strike:** the cost reads the phases printed on the event again, offered and paid alike, so the Skulker takes 1 off those events in every phase; the triggers and responses stay fixed. That is one wrapper out of `ModifiersLogic.getTwilightCostToPlay`, one line moved in `PlayEventAction`, and the two cost tests out.
Report: engine-pass-20/phase.md.

**Ketura (2026-10-10):** Keep, well done.


### Q-0199 Ulaire Nertea 69_38: when her burden is prevented (Melilot Brandybuck 3_110), does the assignment stand? (with F-0308)

Ulaire Nertea, Dark Horseman 69_38: "The Free Peoples player must add a burden to assign Úlairë Nertëa to skirmish a companion of the named race." Melilot Brandybuck 3_110: "Response: If a burden is about to be added by a Shadow card, exert Melilot Brandybuck to prevent that burden." (Tolman Cotton 12_133, Sam, Samwise the Brave 4_316 and Relaxation 15_152 prevent a Shadow card's burden the same way; Relaxation also discards the Shadow card.)

**Built now (engine pass 20, F-0308, as you ruled: "so long as the freeps player is the one doing the assignment, it should add a burden automatically"):** the assignment is made first, and the burden is then added by a required trigger of hers. So when the Free Peoples player assigns her to a Hobbit and answers the burden with Melilot, the burden is prevented and the assignment stands: burdens 0, Melilot exerted, she skirmishes the Hobbit (probed). Before this pass the burden was asked for ahead of the assigning, and a burden Melilot prevented left him unable to assign her to the named race (your rule on Q-0038: preventing a cost "is permitted, but it will cause the cost to no longer be considered paid").

**A (as built):** the burden is something that happens when he assigns her, and preventing it is what those four cards are for. Melilot's exertion (or Tolman's, or Sam's) buys the assignment in place of the burden. Nothing changes.

**B (a cost in full):** the burden is paid before the assignment, and an assignment whose burden was prevented is not made: at his own assigning she is left over for the Shadow player to assign; Éowyn's action and Shadowy Mount assign nobody. This is not a change to the card: the two effects that make assignments (`AssignmentEffect` for a card's, `AssignmentPhaseEffect` for a player's own) would have to collect a cost before assigning, and the Free Peoples player's assigning would have to work out the leftover minions afterwards. I would write that design up for you before building it.

No test asserts either: the pair of cards is left for Melilot's own tests, and nothing is parked. Recommendation: A. It is what your ruling says in plain words, it needs nothing more, and the four cards that can do this are all Free Peoples cards spending something of their own to stop a burden.

Report: engine-pass-20/nertea.md.

**Ketura (2026-10-10):** Ffffuck. Okay. So. The solution is closer to B, but I think needs what is essentially an "add additional cost" modifier; I'm pretty sure that already exists in some form, and it will need applied to this somehow. So that it targets "any assignment action that lets free peoples assign to Nertea". Dunno how involved this might be, let's look before we leap.


### Q-0200 Keep or strike: the original Úlairë Nertëa 19_38 shows its named race too, and the test of the fpStartedAssigning trigger is now a mechanic test on a test card (with F-0307, F-0308)

F-0308 and F-0307 are built for Ulaire Nertea, Dark Horseman 69_38: a burden is added, unasked, each time the Free Peoples player assigns her to a companion of the named race (his own assigning, Éowyn 17_96, Shadowy Mount 12_171); he cannot assign her to that race while no burden can be added; the Shadow player's assignments cost nothing; and her card info shows "Named race is Hobbit" under "Extra information", the line the client already shows for four older cards (`displayableInformation`). Two things in the commit are beyond the item's words. A change already made stays unless you strike it.

**(a) The original, Úlairë Nertëa 19_38, shows its named race too.** Its definition gained the same line (`displayableInformation`, "Named race is {stored}") and stores the race with the new `storeRace` in place of `storeWhileInZone`, so that the line reads "Hobbit" and not "HOBBIT". The race named for the original is buried in the log exactly as the erratum's was. Nothing plays differently: its bar ("The Free Peoples player cannot assign ...") is untouched and reads the stored race as before.

**(b) The test of the `fpstartedassigning` trigger is a mechanic test on a test card, and the trigger is kept.** You made that trigger for 69_38 in 2021, and 69_38 was the only card that used it; she no longer does (her burden is added at the assignment, not asked for ahead of it). `at/TriggersAtTest.fpStartedAssigning` put her on the table and read the "Yes" of her question, so it could only fail, and no real card is left to show the trigger with. I removed that test and wrote `mechanics/FreePeoplesPlayerStartsAssigningTriggerTests` (2 tests) on a card of the tests' own, Test Drum 0_8022 ("Each time the Free Peoples player starts to assign minions, add (1)"): the trigger comes when the assignment actions are passed and again in the fierce assignment phase, and not when the Shadow player assigns what is left over. The trigger word itself, `FPStartedAssigning.java` and the game's "Free Peoples player starts assigning" moment are untouched; `assignmentCost` (Morgul Spawn 7_200 and its two kin) still answers that moment.

**Cards that behave differently because of (a) or (b):** none.

**Keep** (what happens if unanswered): both as built. **Strike a:** 19_38's definition goes back to `storeWhileInZone` with no displayed line (three lines); the original's named race is then found only in the log. **Strike b:** say which you want instead: the trigger word taken out of the definition language as dead (`FPStartedAssigning.java`, its line in `TriggerCheckerFactory`, the test card and the mechanic test go), or the word left with no test at all (the test card and the mechanic test go). The old at test cannot come back either way: it asserted the question you ruled out.

Report: engine-pass-20/nertea.md.

**Ketura (2026-10-10):** keep


### Q-0201 "Ignoring all costs" (Race Text 92_29): what is X when the cost that names it is ignored? (with F-0316)

F-0316 is fixed as you approved: an event played from the pack of Race Text 92_29 ("You may play one shadow card revealed, ignoring all costs") no longer pays the cost in its own text. Threat of the Unknown 1_197 ("Exert your [moria] Orc to draw 2 cards") draws 2 with no Orc exerted, and with no Orc on the table. The same goes for every other Shadow-phase event in the sixteen boosters whose cost is a plain price (the list is at the end).
One kind of cost I did not pass over, because the event's effect reads it back and would have nothing to read. These play from a pack exactly as they did before the fix, and this question is what they should do:
- "Spot X ... to <do X>": The Master's Will 1_215 ("Spot X Nazgûl to reveal the top X cards of your draw deck"), Forces of Mordor 1_248 ("Spot X [sauron] minions to add (X)"), Evil Afoot 2_41 and The Eye of Sauron 2_87 ("... and spot X burdens to ..."), From Deep in Shadow 12_38 ("Spot X [gollum] cards to reveal the top X cards").
- Shadow in the East 13_180: "Spot a companion to draw a card for each wound on that companion."
- Power and Terror 2_70: "Reveal any number of [moria] Orcs from your hand to play The Balrog. Its twilight cost is -2 for each Orc revealed."
- All Life Flees 18_132: "Remove (X) to choose one: discard a follower from play that has a twilight cost of X; or play from deck a [wraith] minion that has a twilight cost of X."
**A (as built, and what happens if unanswered):** a cost the text reads back is carried out as written. The player names X from what there is to spot (a spot pays nothing: CR 4.2 "spot" calls it a requirement), Shadow in the East has a companion chosen, Power and Terror reveals Orcs for its discount, and All Life Flees still removes (X) from the pool. Where an event has a plain cost beside such a one, the plain cost alone is passed over: The Eye of Sauron exerts no Orc and still asks how many burdens to spot.
**B:** as A for spotting and revealing, but nothing is ever paid: All Life Flees asks for X and removes nothing. Then X needs a ceiling of some kind (the pool, as if it were paid; or none, and any follower or [wraith] minion can be named).
**C:** X is 0 wherever its cost is ignored. The Master's Will and From Deep in Shadow reveal nothing, Forces of Mordor adds (0), The Eye of Sauron draws nothing, All Life Flees looks for a follower or minion of cost 0, Shadow in the East draws only its card for a Nazgûl. These become dead cards in a pack.
A is the only one built; B or C is a further change to `DefaultActionSource.createAction` and the memory the effects read (C would otherwise stop the game: an effect that reads an X nobody named throws).
One more event is left as it was, for a reason of its definition and not of the rules: We Must Have It 5_30 ("Shadow: Play Gollum from your draw deck or discard pile. Skirmish: Discard a card at random from hand to make Gollum strength +3") is written with both texts inside its `cost`, so passing that over would have made it do nothing. From a pack its Shadow text plays Gollum, who is paid for as before (he is played by the event, not by the pack).
**What now behaves differently, from a pack only** (played from hand all of these are unchanged): Saruman's Power 1_136, Threat of the Unknown 1_197, Shadow's Reach 1_277, Troubled Mountains 2_72, Speak No More to Me 10_100, Ill News is an Ill Guest 18_70 (no minion exerts); Evil Afoot 2_41 and The Eye of Sauron 2_87 (no exertion; X still spotted); Captured by the Ring 7_53 and So Polite 7_74 (the threat or the twilight is added with no Gollum played); Horribly Strong 11_43 (wounds with no Gollum discarded, and with none in play); Little Snuffler 13_52 (no Gollum spotted); Later Than You Think 15_181 (no threats removed); Enemy in Your Midst 18_30 (no companion healed); War Preparations 18_92 (no threat removed); Sauron's Might 19_27 (no Free Peoples token removed). Before, each of these paid, and where it could not pay it was chosen, discarded and did nothing.
Shown by `mechanics/IgnoringAllCostsTests` (five tests: the Threat with an Orc and with none, the same event from hand paying, The Eye of Sauron, We Must Have It) and two new tests of 92_29. Report: engine-pass-20/costs.md.

**Ketura (2026-10-10):** hoooo boy what a mess. Let's stick with A; this isn't a mechanic that's going to be reused, and it's just for fun on the RTMD card.


### Q-0202 Race Text 94_22: is "your deck" of at least 100 cards the draw deck, or everything a player brings (a draw deck of 89)?

"Your deck must have at least 100 cards." The rule it bends, CR 4.2 "building your deck", uses "deck" both ways: "Each player brings to the game at least 71 cards: a Ring-bearer, bearing The One Ring (2 cards), a draw deck of at least 60 cards, and a 9-card adventure deck. You and your opponents must each have a deck built for the same format. ... Your draw deck must have at least 60 cards". The other modifiers of deck building name theirs (92_1: "Your draw deck may contain up to 6 copies of any card"; 92_19: "Your adventure deck may contain sites from any block"); this one says "deck" alone.

**Built now:** the definition's `minimumDeckSize: 100` takes the place of the format's 60, and the format counts the draw deck only. A player with this modifier is refused at 99 cards in the draw deck ("Deck contains below minimum number of cards: 99<100.") and accepted at 100; his Ring-bearer, Ring and nine sites are not counted.

- **A (as built):** "deck" is the draw deck: the rule's 60 made 100. Nothing changes. The old tests assert it (`NinetyNineCardsFailsWithContext`, `HundredCardsPassesWithContext`) and are active.
- **B:** "deck" is everything the player brings: the rule's 71 made 100, so a draw deck of 89 beside the Ring-bearer, the Ring and nine sites is enough. The definition's number becomes 89, and the two tests move to 88 refused and 89 accepted.

Nothing is parked: the game passes A. I recommend A: the game's own message calls the draw deck "Deck", and a 100-card draw deck is the plain parallel of the rule's 60. Say only if it should be B.

**Ketura (2026-10-10):** Piss, that should say "draw deck". I'll adjust the wording. A.


### Q-0203 Boromir's Cloak 51_98: 1A needs no engine change after all. 1A, or the card's words changed? (with F-0201, Q-0119)

"Response: If a skirmish event or special ability is used during a skirmish involving Boromir, discard this to prevent that."

On Q-0119 you chose 1B (only Skirmish: abilities) because 1A (Response abilities too) "requires an engine-level alteration", and on F-0201 you said "good to fix". Engine pass 20 went to make that alteration and found there is none to make: **F-0201 was a misreading of the source, not a fault.** A Response special ability is the same kind of action as a Skirmish: or Maneuver: one and has always reported itself as "a special ability used"; only its word is Response. In the game as it stands (and before this pass):

- Storied Homestead 13_76 ("If a minion's special ability is used ...") is offered against Isengard Shaman 3_59's Response and cancels it. Terrible as the Dawn 53_103 ("If a companion or ally's special ability is used ...") is offered against Éowyn 4_270's. Every card whose text names no phase reaches Response abilities, as its text says.
- Sharku's Warg 5_59 and War-warg 5_64 ("a skirmish special ability"), and Rare Good Ballast 5_114 ("a regroup special ability"), do not, though the Response is used in that phase. That is your own reading on Q-0119 ("skirmish special ability" does not permit Response actions), so I left them.
- Boromir's Cloak is 1B only because its definition says `phase: skirmish`. Nothing else stands between it and 1A.

**No engine code changed, and no card behaves differently.** F-0201 is recorded as not a bug. What the commit holds: `mechanics/ResponseAbilityIsASpecialAbilityTests` (3 tests: a Response ability is answered; one that is cancelled keeps its cost paid and may be used again; one used in a skirmish is no "skirmish special ability"), one new test on 13_76 (it answers the Shaman's Response) and one on 51_98 (1B: the Cloak is not offered against the Shaman's Response in Boromir's skirmish), and the pitfall that told authors a Response ability is never "used", put right.

So the question you settled for the engine's sake is open again on its merits:

- **A (1A, what the words say):** the Cloak also answers a Response special ability used during a skirmish involving Boromir (the Shadow player's only, as you ruled on F-0200). One line of the definition goes; the new test of 51_98 turns round. It would then reach, while Boromir is fighting: a wound prevented (Isengard Shaman 3_59, Saruman 3_68, Goblin Armory 1_173, His Terrible Servants 2_77) and, when a minion beats him, its "wins a skirmish" Responses (Uruk Bloodlust 1_144, Uruk-hai Rampage 1_159, Goblin Swarms 1_183, Blade Tip 1_209, The Pale Blade 1_221, Band of the Eye 1_240); 61 Shadow cards in all have a Response ability. The Cloak stops one use: by CR 4.2 "response" ("You may respond more than once to the same situation") the Shaman's owner may pay again once the Cloak is gone.
- **B (keep 1B, and the card's words change):** as built and as you meant, "skirmish event or skirmish special ability". Nothing changes in the game; the wording is yours to update, with the Shadow restriction you already mean to write in.

**If unanswered: B.** It is your last word and what the tests assert. Say A and it is a one-line change.

Also for your eye, not mine to touch in this item: the ledger of Terrible as the Dawn 53_103 says no test answers a companion's Response ability "because the engine does not report one as used (F-0201)". The game does offer it there; a test of it on that card is wanting, and the orchestrator has the line.

Report: engine-pass-20/special.md.

**Ketura (2026-10-10):** Ha, in that case let's 1A. I'm nothing if not consistently inconsistent. And yeah, address TATD's gap with it.


### Q-0204 Race Text 94_28: do the words reach the opening hand and the mulligan?

"Each time you are about to draw a card (except when reconciling), instead your opponent looks at the 2 top cards from your draw deck, chooses 1 for you to take into hand, and the other is placed beneath your draw deck."

Built now: the opening hand of eight is dealt from the top of the deck as it lies and the opponent is asked nothing (`TheOpeningHandIsDrawnAsItLiesWithNoChoice`, new, passes). The mulligan's six are dealt the same way (read in the engine by the reviewer, not tested: both steps of set-up put cards into hand directly and never pass through the draw the modifier replaces). The doubt: the modifier is in play from set-up, the rules call both of these draws (CR 4.2 "setting up the game": "draw eight cards to form your starting hand"; "mulligan": "draw 6 cards"), and the text's one exception is reconciling. No old test takes a reading.
**A:** the letter: the modifier bites from the first card. The opponent is shown two cards and picks one, eight times for the owner's opening hand and six more if he mulligans. The engine's set-up would have to deal those hands through the draw that can be replaced (an engine change), and the new test is turned round.
**B:** as built: set-up is not a draw in the course of play, and the opening hand and the mulligan are dealt as the deck lies. Nothing changes.
Parked on it: nothing. `TheOpeningHandIsDrawnAsItLiesWithNoChoice` asserts B and is active.
I would keep B unless the modifier was meant to shape the opening hand: A is up to fourteen picks before the first turn.

**Ketura (2026-10-10):** Nah, let's leave it out of the game setup for the same reason that reconcile is left out (8+ choices would slow eeeeverything down). B.


### Q-0205 Race Text 94_28: is the card taken in place of a draw a card drawn, for "each time ... draws a card"?

"Each time you are about to draw a card (except when reconciling), instead your opponent looks at the 2 top cards from your draw deck, chooses 1 for you to take into hand, and the other is placed beneath your draw deck."

Built now: the card taken counts as a card drawn. Probed at landing: the owner as Free Peoples player with Frodo and Gimli, Morgul on the March 7_195 in the Shadow player's support area ("Each time the Free Peoples player draws a card (except during the regroup phase), add a threat"), and Delving 1_6 played ("draw 3 cards"): three looks, three cards taken, and 2 threats (all that two companions allow), the same as at a table without the modifier. No test of 94_28 reads this.
The doubt: CR 4.2 "instead" has the stated effect "replaced with a different effect", and the game's own words tell a draw from a take. The Rule of 4 says "draw (or take into hand)"; Betrayal of Isengard 3_29 and Verily I Come 2_94 say "draws a card (or takes a card into hand)", where Morgul on the March says only "draws a card".
**A:** the letter: nothing was drawn. Morgul on the March adds no threat for a card the opponent hands over; 3_29 and 2_94, which name a take, still answer it; the Rule of 4 still counts it (`TheFifthCardOfAFellowshipPhaseIsNotTaken`). This needs the engine: the definition word behind all three of those cards, `playerDrawsCard`, answers a card drawn and a card taken into hand alike (read in the engine, not probed), so it would have to tell the two apart, and Morgul on the March would then stop answering a card taken from a discard pile or a draw deck as well. That is what its own text says, and is a matter for set 7 when the campaign reaches it. An Interaction test of 94_28 with 7_195 would be written.
**B:** as built: the modifier changes which card is drawn and who picks it, and for every other card it is still a draw. Nothing changes (the same Interaction test, asserting the threat, if you want one).
Parked on it: nothing.
No recommendation: the letter is A, and the modifier was built as a filter on draws (its engine word is `OpponentFiltersYourDraws`, and the opponent is asked to "Choose the card <player> draws").

**Ketura (2026-10-10):** I intended the "instead" use to pay card draw costs...but then I suppose if I take a burden instead of a wound, I don't trigger a wound trigger. Hmm. Viewing it as a draw filter is helpful; after all that card *did* come from the top of your draw deck. Let's say B for this one.


### Q-0206 Race Text 94_30: is a card drawn a card "taken into hand from your draw deck"?

"You may not play or take into hand cards from your discard pile or draw deck (except when reconciling)."

**Built now:** the bar reaches a card played from either pile and a card an effect puts into hand from either pile (Risk a Little Light 1_82 takes nothing from the top of the deck; Ring of Guile 9_8 takes nothing from the discard pile). No draw passes through it. The owner plays Delving 1_6 ("exert a Dwarf companion to draw 3 cards"): Gimli exerts and 3 cards are drawn. The old test `OrdinaryDrawEffectIsNotBlocked` asserts exactly that, with the comment "an ordinary top-of-deck draw, unrelated to 94_30's discard-pile/draw-deck play-and-take restriction", and is active.

The doubt: the one exception the text names is a draw (CR 4.2 "reconcile": "he must draw cards until he has eight"), and a reconcile moves a card from a draw deck to a hand in no other way. So as built the parenthesis excepts nothing. The rules speak both ways. The Rule of 4 tells the two apart ("You cannot draw (or take into hand) more than 4 cards") and in its next lines counts a draw among them ("This applies to cards taken into hand by any means. This does not apply to cards drawn 'at the start of each of your turns.'"). The modifier two before it, 94_28, has the same parenthesis on a text that is about draws ("Each time you are about to draw a card (except when reconciling), instead ...").

**A (the parenthesis means something):** a card drawn is a card taken into hand from the draw deck, and the owner draws only when reconciling. Delving exerts Gimli and draws nothing; so does every other "draw N cards" of the owner's, on either side of the table. This needs the engine: the modifier's flag is read where an effect puts a card into hand from a pile and nowhere a card is drawn, and the reconcile's draws would have to be let through. The old test is turned round (the parked test takes its place), and `ReconciliationDrawsAreUnaffected`, which today no mistake can fail, becomes the test of the exception.

**B (as built, the old test):** only a play, and an effect worded "take into hand", are barred; a draw is neither, and the parenthesis is reassurance. Nothing changes; the parked test is deleted.

Parked on it: `ADrawOutsideAReconcileTakesNoCardFromTheDrawDeck` (the old test's board read the other way: Gimli exerted, the deck's count unchanged; today it fails at the deck's count, 3 short).

No recommendation: the words lean A, the old test says B on purpose, and A makes the modifier a good deal harsher (no card drawn by any effect, all game). Unanswered, B stays.

**Ketura (2026-10-10):** I intended for it to include standard card draw; the reconcile exception was so that the player didn't just sit down to play with their starting fellowship and nothing else. The instance who coded it must have missed it, and so did I in the review. A.


### Q-0207 Race Text 94_30: does the bar reach an opponent's card that returns the owner's minion to his hand (A New Light 17_15)?

"You may not play or take into hand cards from your discard pile or draw deck (except when reconciling)."

A New Light 17_15: "Spell. Spot a [gandalf] Wizard to search a Shadow player's discard pile and choose a minion, then return that minion to its owner's hand. If you do, you may reveal that Shadow player's hand and discard a minion found there." (The erratum 67_15 is the same with 2 minions.) It is the one printed card the reviewer's search of the game texts found that moves a card out of ANOTHER player's pile; none plays one from there.

**Built now:** the bar on taking follows the owner of the card taken, whoever's card moves it (the bar on playing follows the player who plays). With the modifier in the Shadow player's seat, the Free Peoples player plays A New Light and chooses a minion in the Shadow player's discard pile: the minion stays in the discard pile. His hand is then revealed and a minion discarded from it all the same (probed at landing: Goblin Backstabber and Goblin Patrol Troop in his pile, Goblin Runner in his hand; the Backstabber chosen and not returned, the Runner discarded, his hand empty). With the modifier in the Free Peoples player's seat the same play returns the minion (`TakingACardFromTheOpponentsDiscardPileIsUnaffected`, new, active). No old test lays either board.

**A (the words, as the reviewer reads them):** "you" is the player doing the taking, and here that is the opponent. The Free Peoples player's card returns the minion to its owner's hand whatever modifier the owner holds, and A New Light plays as at any table. This needs the engine: the two effects that put a card into hand from a pile would read the flag for the player carrying the effect out, as the two play effects already do. The parked test goes live.

**B (as built):** a card that comes into your hand from your discard pile is taken into hand by you, whoever made it come (94_28 speaks so: "your opponent ... chooses 1 for you to take into hand"). Nothing is returned. A New Light's "If you do" would then want putting right on 17_15 and 67_15 when their sets are reached: as built the owner loses a minion from hand for one that never came back. The parked test is deleted.

Parked on it: `AnOpponentsCardCanReturnTheOwnersMinionFromHisDiscardPileToHisHand` (today it fails where the chosen minion should be in his hand).

A small matter: one card and its erratum reach this board. The reviewer thinks A the likelier; I have no strong view. Unanswered, B stays as built.

**Ketura (2026-10-10):** I agree with the reviewer, A. Besides restricting your opponent when it's not meant to, such restrictions almost always get ruled to be treated differently when the opponent is acting (see discard prevention and the "forced" stuff).


### Q-0208 Race Text 94_30: do the words reach the starting fellowship, played "from your draw deck"? (rider to Q-0204)

"You may not play or take into hand cards from your discard pile or draw deck (except when reconciling)."

CR 4.2 "starting fellowship": "You may then play other companions ... one at a time from your draw deck, as long as the total twilight cost of these companions is 4 or less."

**Built now:** the modifier is in play from set-up, and set-up is untouched: its owner is offered companions from his draw deck for his starting fellowship and plays them, and his opening hand of eight is dealt. `TheStartingFellowshipIsPlayedAndTheOpeningHandDealtAsEver` (new) asserts that and is active. No old test took a reading.

This is Q-0204's doubt (do a modifier's words reach set-up?) on a second card, and it is new in one half: the rules have a starting fellowship played from the draw deck, which is the very thing this text forbids.

**A (the letter, as Q-0204's A):** the owner starts with his Ring-bearer alone. (His opening hand and mulligan are draws: they are barred as well only if Q-0206 is answered A, and then as Q-0204 itself.) This needs the engine; the new test is turned round.

**B (as built, as Q-0204's B):** set-up is not play in the modifier's sense, and the fellowship and the hand are made as at any table. Nothing changes.

Parked on it: nothing.

Recommendation: B, with one answer for both cards. Under A this modifier would also mean a start with the Ring-bearer alone, which nothing in its words points at.

**Ketura (2026-10-10):** No, it shouldn't affect the starting fellowship. I will add "or starting fellowship" to the text, go ahead and do the same.


### Q-0209 Stand Against Darkness 51_63, Here Lies Balin 1_19: once "wound X twice" may be chosen, may it be aimed at an X with 1 vitality left? (with F-0310)

Stand Against Darkness 51_63 (landed): "Maneuver: Exert an Elf to wound a [sauron] minion twice or discard a [sauron] condition or possession."
Here Lies Balin 1_19: "Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc twice."

Your ruling on F-0310: "when you make a choice you must make one you are 'fully capable' of performing, which means that a 1-vitality Orc cannot be hit by either branch. However, since you then must 'do what you can and ignore the rest', the player should be given a choice and attempt to perform either branch."

**Built now** (the engine as it stood; engine pass 21 changed nothing here): "wound X twice" is a branch the player may choose while some X in play can take two wounds. Once it is chosen, every X that can take a wound is offered, and one with 1 vitality left may be picked: it takes the one wound and dies. Probed on 51_63, with a [sauron] condition in play each time. A lone [sauron] minion with 1 vitality left: nothing is asked, the condition is discarded and the minion cannot be touched, which is your ruling. Put a second [sauron] minion of 3 vitality beside it: "Wound a [sauron] minion twice" is now offered, the weak minion may be picked, and it is killed. So whether the weak minion can be killed turns on a bystander.

**A (as built):** "fully capable" is asked of the branch; whom it is then aimed at is "do what you can and ignore the rest", as for a "wound X twice" that is no part of a choice. Nothing changes.

**B:** a branch chosen because the player is fully capable of it has to be carried out in full: only an X able to take both wounds may be picked. This needs the engine: inside a choice the word `wound` would offer only the candidates it counted when it said the branch could be done in full (as "exert X twice" does everywhere since F-0131). A "wound X twice" outside a choice is not touched, and where no branch can be done in full and both are offered, any X may still be picked.

The cards with such a branch: Stand Against Darkness 51_63, Here Lies Balin 1_19, Deep Hatred 13_3, The Witch-king 6_122 ("wound an ally twice or exert a companion"), Battle of Azanulbizar 30_4, Dawn Take You All 30_24. On Here Lies Balin and Deep Hatred no player gains by it: with a second Orc in play "wound 2 Orcs" kills the weak one and wounds the other as well. On 51_63 and 6_122 it decides whether a 1-vitality minion or ally can be killed.

Parked on it: nothing. No landed test reads whether a character with 1 vitality left may be picked (51_63's lays candidates of 2 and 3). `mechanics/ChoiceOfWhatCanBeDoneInFullTests` lays an Orc of 1 beside an Orc of 3, picks the Orc of 3, and asserts nothing about the other.

No strong recommendation: A is how the game has stood, B is the stricter reading of your words. Unanswered, A stays.

**Ketura (2026-10-10):** B is correct, with one caveat: while the choice is valid (i.e. both sides are "fully capable" of happening), the eventual secondary choices (who to wound etc) are an extension of that choice and thus must be restricted to only "fully executed" choices; picking "wound one Orc twice" means you can't pick a 1-vitality Orc. However once a choice is broken by having no "fully capable" branches, then we are free of that restriction and instead fall back on "do what you can and ignore the rest", which *does* permit us to wound a 1-vitality Orc twice. Having the Choice structure somehow encode this strictness would be nice, but if we have to contort half a dozen cards instead of making the engine significantly twistier, that might be preferred.


### Q-0210 Keep or strike: what else the rules' order of a move changes (Q-0184 as built): site texts, Hillman Band 4_21, limits per site

Q-0184 is built the way you said: the engine's order was wrong and is put right, and Race Text 92_18 keeps `type: Moves`. A move now goes: the next site is placed if needed, "moves from", "the fellowship moves", the player marker, "moves to", the move's twilight (CR 4.2 "moving your fellowship", in that order). Before, the marker was moved ahead of "the fellowship moves". One effect moved up in `MovementGameProcess`; nothing else in the engine and no definition changed. 92_18's parked test is live (from site 6, one of two companions) and the card is landed.

This entry is the list of what else now plays differently, since it reaches cards the question did not name. Everything that answers "the fellowship moves" (88 definitions) is carried out with the marker still on the site being left. For nearly all of them a player sees no difference: they add twilight, exert, wound, draw or add a threat, and the move's own twilight was and is added last. Where it shows:

1. **Site texts.** The text of the site being left still binds what such a trigger does, and the text of the site ahead does not yet. Legolas' Sword 5_12 ("Each time the fellowship moves, you may spot a wounded minion to heal Legolas") heals him on the move TO Steward's Tomb ("Wounds cannot be prevented or healed") and does not on the move FROM it; it was the other way round. Tested. The same goes for any such pair, for one Merchant of Westfold 13_132 or Éowyn 13_124 (reinforce a [rohan] token as the fellowship moves) against Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced"): by reasoning, not tested.
2. **Hillman Band 4_21** ("Each time the fellowship moves, you may spot another [dunland] Man to take control of a site"), the only such text that takes a site. A site can be controlled only when every marker is past it (CR 4.2 "control"), and the moving marker is now still on the site being left. It differs from before on one board: the moving fellowship is behind the other player's and every site below its own is already controlled. The Band then takes nothing on that move, where it used to take the site just left; on the next move it takes it. Probed, not in a test: the Band's offer with nothing to take is itself against Q-0034 and is filed as F-0341 for the card's author.
3. **"Limit once per site"** (Cirith Ungol Watcher 103_63 and 103_92, "Each time a companion exerts, draw a card (limit once per site)"; They Are Not All Accounted For 103_103). An exertion made by a "the fellowship moves" text (Under the Watching Eye 1_281, Saruman 4_33, Rout 7_158, Dark Fire 2_54) now counts for the site left, where it counted for the site arrived at. An exertion made by a "moves to" text (Midgewater Marshes, Silverlode Banks) counts for the site arrived at, as before. By reasoning from where the count is cleared (with the marker); not tested.
4. The move is not yet counted while such a text is answered (the first move reads as none made). No definition reads the count there.
5. The Concealed rule (`ConcealedRule`; no card has the keyword) answers "the fellowship moves" and so now reads "exposed" on the site left. Left alone.

In other cards' tests, eight lines that read the site number or the move count at a decision inside such a trigger asserted the old order and now read the site left: Radagast 59_26 (2), Their Eyes Fell Upon Him 101_41 (3), Savage Harpoon 103_57 (3). Nothing those cards do changed. The suite is green (10043 tests), with `mechanics/StepsOfAMoveTests` added.

**Keep** (what happens if unanswered): the order stays as the rules list it, with 1 to 5 as its consequences.
**Strike:** the marker goes back ahead of "the fellowship moves", 92_18 discards two again on the move from site 6 and would need `MovesFrom` to read A (which you ruled against), and the eight lines, the pitfall and the mechanics test go back.
Or say if any one of 1 to 3 should be held to its old behaviour by a rule of its own.

Report: engine-pass-21/moves.md.

**Ketura (2026-10-10):** yeah this is a big keep. Those situations you mention are *different*, but now they are in line with what the rules dictate.


### Q-0196 Keep or strike: the CanMove requirement reads the no-unhindered-companion rule too, and the game log says why the move was not offered (F-0315)

F-0315 is built: in the regroup phase a player whose fellowship has no unhindered companion is not asked "Do you want to make another move?"; the fellowship stays and its player reconciles, and a card that makes the Free Peoples player move again (`HasToMoveIfAble`) gives way. Two things in the commit are beyond the finding's words. A change already made stays unless you strike it.
**(a) The `CanMove` requirement reads the rule too.** `CanMove` ("they can voluntarily choose to move again if they so desire", in your HJSON document) held its own copy of the test the regroup phase made: no `CantMove` in force and the move count under the move limit. Both now call one method, `RuleUtils.canChooseToMoveAgain(game)`, which adds "the fellowship has an unhindered companion". No definition uses `CanMove` today, so no card behaves differently and nothing tests the requirement itself. It reads the board whenever it is asked, in any phase: in a fellowship phase with every companion hindered it now answers false, though the move of that phase is no choice and is still made.
**(b) A line in the game log.** When the question is withheld because no companion is unhindered, the log says "<player> has no unhindered companion and cannot choose to move again." before the usual "<player> decides to stay and reconcile." Without it a player with a move left under the limit is not asked and is not told why.
**Cards that behave differently because of (a) or (b):** none. (The finding itself reaches the five cards that can force a move on that board: Hornburg Wall 5_118, No Retreat 4_30, Forced March 13_108, It Betrayed Isildur 101_47, The Last Lights of Durin's Day 33_60. Hornburg Wall says "make the fellowship move again this turn (if the move limit allows)" where the others say "make the Free Peoples player choose to move again"; all five are one flag and all five give way.)
Shown by `mechanics/NoUnhinderedCompanionNoRegroupMoveTests` (8 tests; the log line is read in the first).
**Keep** (what happens if unanswered): both as built. **Strike a:** `CanMove` gets its own three lines back and answers as before, true for a fellowship with every companion hindered; the line in requirements.md goes back to your wording. **Strike b:** two lines out of `FellowshipPlayerChoosesToMoveOrStayGameProcess` and one assertion out of the test; the log then says only "decides to stay and reconcile".
Report: engine-pass-20/hinder.md.

**Ketura (2026-10-09):** Keep, looks good.


### Q-0195 Race Text 94_17: is the opponent asked "Would you like to take control of a site?" when no site can be controlled?

"At the end of each of your turns, choose an opponent who may take control of a site."

**Built now:** a required trigger at the end of each of the owner's turns: the opponent is asked "Would you like to take control of a site?" every time, whether a site can be controlled or not. At the end of the owner's first turn none can (the other marker is still on site 1): he is asked, and his yes does nothing. The old tests assert it: `FiresAtEndOfTurnOneButFizzlesSinceNoSiteIsYetAvailable` (asked, yes, neither player's site count changes), and `OpponentMayTakeControlOfASiteOnceOneIsAvailable` answers the same turn-1 prompt on its way to turn 3.

The doubt: Q-0034 (an optional action with no cost that would do nothing is not offered) and Q-0106 ("asked only when X can be done") reach this "may" by their own scope, and the old tests assert the departure.

**A:** he is not asked while no site can be controlled; the turn simply ends. `{ type: SiteAvailableToControl }` goes beside `OwnerIsFreeps` in the trigger's `requires`. The parked test goes live; the first old test is turned round (nothing asked at the end of turn 1), and the turn-1 answer comes out of the second old test and of the new `DecliningLeavesNoSiteControlledAndTheOfferComesAgainAtTheEndOfTheOwnersNextTurn`. Shown: with that requirement the parked test passes and those three are the active tests that fail. It also ends the prompt before the first turn on this card (F-0327), there being no site to take then either; the engine's own fault stays the engine's.
**B (as built):** he is always asked, and a yes with no site does nothing. Nothing changes and the parked test is deleted.

Parked on it: `TheOpponentIsNotAskedWhileNoSiteCanBeControlled`.

Recommendation: A. It is a prompt that can do nothing, in every game at the end of the owner's first turn and again whenever the other marker has not moved on.

**Ketura (2026-10-09):** A, yeah


### Q-0194 Race Text 94_13: is the Archery action of "your archers" the modifier's owner's alone, when its old test has the other player use it?

"Archery: Make each of your archers strength +3 until the regroup phase. Your archers do not contribute to your archery total and you cannot make any more Archery actions."

**Built now:** the action has no owner requirement. In the archery phase either player is offered it, whoever holds the modifier, and "your archers" and "you" are the player who uses it; both players may use the one modifier in the same archery phase. The old test `ShadowsActivationOfTheSameMetaSiteAffectsOnlyShadowsOwnArchersNotFreepsSince` asserts it on purpose: with the modifier in player 1's seat the Shadow player is offered the action and uses it, and its comment says "'your archers' scopes to the activator, exactly as with a real Site".

The doubt: this is the only modifier left whose phase action says "your" and is open to both players. The others are held to their owner (F-0312): 91_10 "Skirmish: Discard 3 cards from hand to make your companion or minion strength +1" (`IsOwner`, corrected at landing), and by `OwnerIsFreeps` or `OwnerIsShadow` 92_23 "Skirmish: Exert your companion ...", 93_1 "Skirmish: Play a possession on your minion", 94_1 "Maneuver: Exert your ally ...", 94_8 "Maneuver: ... place your unique companion ...", 94_9 "Regroup: Discard your minion ...".

**A:** the owner's alone. `requires: { type: IsOwner }` goes on the action; the player across the table is never offered it, and his archers fire as usual. The parked test goes live and the old test is turned round (the Shadow player read as not offered). Shown: with that requirement the parked test passes and that old test is the only active one that fails.
**B (as built):** either player may use it for his own archers. Nothing changes; the parked test is deleted and F-0312's shape gets a line that this card is open on purpose.

Parked on it: `TheOtherPlayerIsNotOfferedTheOwnersArcheryAction` (player 1 holds the modifier, the Shadow player has Goblin Marksman and Southron Archer on the table and is read as not offered).

Recommendation: A. "Your" has meant the owner on every other modifier, and the old test's comment reads as an account of how the engine offers a meta-site's action (the fault F-0312 found on 91_10) more than as a decision about this card.

**Ketura (2026-10-09):** Yup, A.


### Q-0193 Keep or strike: PlayPermanentAction shuffles a draw deck only when it was looked through (F-0313), not on every play from one

F-0313 is built: a card played from a draw deck to the table or the support area now shuffles the deck as it leaves, before it is paid for, so Old Toby's "you may draw a card" comes off a shuffled deck. One thing is not as the finding put it. A change already made stays unless you strike it.
The finding said PlayPermanentAction should shuffle "as the other two play actions do", and those two (AttachPermanentAction, PlayEventAction) shuffle whenever the card comes from a draw deck. But PlayPermanentAction is the one that plays cards from a deck nobody looked through:
- From Deep in Shadow 12_38: "Spot X [gollum] cards to reveal the top X cards of your draw deck. You may play a revealed minion. Its twilight cost is -2." No look and no reshuffle; the other revealed cards stay on top.
- Shepherd of the Trees 15_36: "Maneuver: Exert this companion to reveal the bottom card of your draw deck. If it is a [gandalf] character, you may play it."
- every companion of a starting fellowship, whose deck is shuffled once, before the opening hands.
An unconditional shuffle would have shuffled the deck under all three (tried: From Deep in Shadow and the starting fellowship fail the new mechanic test with it).
**Built:** PlayPermanentAction shuffles as the card leaves the deck only when it has been told the deck was looked through (`setDeckLookedThrough()`), and PlayCardFromDrawDeck tells it so where the definition writes `showAll: true`, which is the look itself. Of the 137 uses of PlayCardFromDrawDeck, 133 write it; the four that do not are the plays of a revealed card: 12_38, 15_36, and Éomer 8_87 and Faramir 8_34 (top 10 revealed, and their own "Shuffle your draw deck" afterwards, as before). Nothing changes for those four or for a starting fellowship. The other two play actions are untouched.
**Cards that behave differently:** every "play ... from your draw deck" that takes a companion, minion, ally or support-area card: the deck is shuffled as the card leaves, and again by the definition's `shuffle: true` after the play. Two shuffles where there was one, and the game log says "shuffles their deck" twice, as it always has for a card played onto a bearer and for an event. Morgul Squealer 8_77 ("play up to 2 [wraith] minions from your draw deck"), whose definition looks twice and shuffles once at the end, now shuffles after each minion too: three. A scenario file that lists `rng:` shuffles around such a play meets one more shuffle than it did.
Shown by `mechanics/DrawDeckShuffledWhenTheLookIsOverTests` (Southfarthing Leaf 17_110 fetching Old Toby; Morgul Squealer; From Deep in Shadow; a starting fellowship) and by 92_4's parked test, now live. Sixteen lines in nine other cards' tests, and one in the rig's own, read an exact count of one shuffle and now read two.
**Keep** (what happens if unanswered): as built. **Strike:** one condition out of PlayPermanentAction and two lines out of PlayCardFromDrawDeck; every card PlayPermanentAction plays from a draw deck then shuffles it, as the finding's words had it: From Deep in Shadow and Shepherd of the Trees shuffle a deck that was only revealed from, each starting-fellowship companion shuffles the deck with a log line of its own, and two of the four mechanic tests go.
**Not done, yours to say:** "once". The second shuffle, the definition's own after the play, is now redundant whenever a card was played (for all three kinds of play; it is still the only one when the look plays nothing). Leaving it out there would make the log say it once; it changes the counts that landed tests of cards played onto a bearer and of events read, and was not asked.
Report: engine-pass-20/shuffle.md.

**Ketura (2026-10-09):** Keep, looks good.


### Q-0174 Ulaire Nertea, Dark Horseman 69_38: is the burden rightly paid ahead, at a yes/no put before anything is assigned and whoever is on the table?

Ulaire Nertea, Dark Horseman 69_38: "The Free Peoples player must add a burden to assign Úlairë Nertëa to skirmish a companion of the named race."

**Built now:** once the assignment actions are passed and before the Free Peoples player assigns, he is asked "Would you like to add a burden to be able to assign Ulaire Nertea to a companion of the named race?". Yes adds the burden there and then, and leaves him free to assign her to that race or not; no bars the named race from her for that assignment phase. He is asked again in the fierce assignment phase. The question is put whatever the table holds, so a yes can buy nothing on three boards: no companion of the named race in play; she is already assigned by an assignment action; and a fierce assignment phase in which she has lost fierce while another fierce minion keeps the round going. (Since this landing it is not put when no burden can be added, F-0306.) Your old test `FreepsAddsABurdenToSkirmishNamedRace` asserted the question and the burden at the yes, before anything was assigned. The engine's `assignmentCost` presents "must exert a companion to assign this minion" (Morgul Spawn 7_200) in the same way, except that it does not ask in a fierce round about a minion that is not fierce. The Standard's list of deliberate departures does not name this one, which is why it is asked.

**A (as built):** a yes/no ahead of the assigning, paid at the yes, is how Gemp presents a cost of assigning, and it is put whenever the burden can be added, like any offer with a cost (Q-0034). Nothing changes; every test stands. I would then write it into the Standard's deliberate departures so that the set-7 cards do not raise it again.

**B (asked only where it can buy something):** the same yes/no, but withheld in a phase where he could not assign her to a companion of the named race anyway (none in play, she already assigned, she not fierce in a fierce round). The burden is still paid at the yes. A requirement on the trigger should do the first of the three (not tried); the other two may want the engine. One test is added that reads the question withheld with no companion of the named race in play; the rest stand.

Nothing is parked: the tests of A pass and are active. Recommendation: A. Charging the burden only when she is actually assigned to the named race, with no question ahead, is neither of these: it is reading A of F-0308 carried to his own assigning, and needs the engine.

**Ketura (2026-10-09):** since this is settled, let's get it removed from my list. [Settled by her answer to F-0308, as the orchestrator read it and she confirmed ("Those three readings look good"): no yes/no ahead of the assigning; the burden is added as the Free Peoples player assigns her to a companion of the named race, at his own assigning as by a card.]


### Q-0167 Demoralized 61_114: is "you may add (1)" on each exertion of a companion meant to be added without asking? (rider to Q-0165)

Demoralized 61_114: "To play, spot an [orc] minion. Each time a companion exerts, you may add (1) (or (2) if you can spot 6 companions)." The original 11_114 has the same two sentences; the erratum only made the card unique.

**Built now:** a required trigger (`optional: false`). Each exertion of a companion adds the twilight and the Shadow player is asked nothing. The original 11_114 is built `optional: true` and asks every time. Nothing in the file's history shows the difference was chosen: the erratum was written in 2022 as `type: trigger` with no `optional` line, which then meant required, and your mass edit of 2024-11-08 (7994c9ba71, "Fixing missing trigger alterations") wrote that default out as `optional: false`; the original got its `optional: true` when set 11's Orc cards were migrated from Java in 2024-05. Of the Shadow cards whose text is a bare "you may add (N)" on a trigger, three add it unasked: Goblin Runner 1_178 (ruled deliberate, F-0009), Mordor Fiend 60_91 (Q-0165, open) and this one. Eight ask: 11_114 itself, Goblin Flankers 2_61, Isengard Scimitar 5_54, Mûmak 5_73, War Mûmak 5_78, Banner of the Eye 6_98, Easterling Regiment 7_145 and Orc Artisan 12_91.

**A (as built):** the Goblin Runner shortcut reaches this card: every exertion of a companion adds 1 (or 2 at six companions) with no click. Nothing changes in the erratum. Four active tests read the tokens in the pool and no offer pending (`EachTimeACompanionExertsOneTwilightIsAddedWithoutAsking`, `ACompanionExertedTwiceByAMinionAddsOneForEachExertionAndTheMinionsOwnExertionsAddNothing`, `AnAllyExertingAMinionExertingAndACompanionTakingAWoundAddNothing`, `AddsOneWithFiveCompanionsAndAnAllyAndTwoOnceASixthCompanionIsPlayed`). If A, say whether the original 11_114 should lose its click as well, so that the two play alike.

**B (the text, as Q-0021):** the Shadow player is asked on each exertion and may decline. One word in the definition (`optional: true`, as on 11_114); those four tests each gain an accept, one test of declining is added (the pool unchanged, the exertion stands), and the clause's ledger line takes the O check.

No test is parked: the game does A and the tests of A are active. Recommendation: answer it as you answer Q-0165. The case for the shortcut is, if anything, stronger here than on Goblin Runner (the trigger comes on every exertion of every companion, not once a play), but it is the only card of its "Each time ..." wording that does not ask, and no one appears to have decided that.

**Ketura (2026-10-09):** B for this one, I suspect it's a victim of the mass edit.


### Q-0168 Mountain-troll 65_112: may the troll just played be one of the 5 [orc] minions it discards?

Mountain-troll 65_112: "When you play this minion, you may discard 5 [orc] minions from play to make it strength +10 and <b>fierce</b> until the end of the turn." The original 15_112 asks the same discard for another reward ("... to make it twilight cost -10 and <b>fierce</b>"), and there the five go as a discount while the troll is still being played, so it can never be one of them. The erratum made the sentence a trigger on the play: when it is answered the troll is in play (CR 4.2 "playing a card") and is itself an [orc] minion.

**Built now:** five OTHER [orc] minions (`choose(culture(orc), minion, another)`). The `another` is in your first build of the erratum (c5a84e0338, 2022-08-19), and so is the old test OnPlayTrollDoesNothingIfLessThan5OrcMinionsInPlay, which plays the troll beside four and asserts no offer. The nearest sibling is built the other way: Vengeful Primitive 17_65 ("When you play this minion in the maneuver phase, you may discard X [men] minions from play to add (X)") has `choose(culture(men),minion)` and may discard itself.

**A (the words, Q-0012):** the troll counts. Beside four other [orc] minions the offer is made, and taking it discards all five, the troll among them, for nothing; beside five or more the troll is one of the candidates. One word comes out of the definition (`another`). The parked test goes live and three active tests change what they read: `PlayedForEightAndRoamingAndWithFourOtherOrcMinionsInPlayMakesNoOffer` (an offer beside four), `WhenPlayedFiveOrcMinionsMayBeDiscardedToMakeItStrengthPlus10AndFierce` (the troll among the candidates) and `StrengthAndFierceLastThroughTheRegroupPhaseAndASecondMoveUntilTheTurnEnds` (beside exactly five others the game would ask which five of six).

**B (as built):** five other [orc] minions; beside four there is no offer and the troll is never a candidate. Nothing changes in the game, and the parked test is deleted. If B, say whether the text should gain the word ("discard 5 other [orc] minions"): as it stands it reads as A.

Parked on it: `WithFourOtherOrcMinionsTheTrollJustPlayedMayBeTheFifthDiscarded` (fails as built, at the offer; passes with `another` dropped). Recommendation: B. All that A adds is a way to throw the troll away for no gain, and the exclusion has been in the card since you built it.

**Ketura (2026-10-09):** Let's do B and edit the text to add "other", I will adjust the errata


### Q-0169 A New Light 67_15: with one minion in the Shadow player's discard pile, is "If you do" met by returning that one?

A New Light 67_15: "<b>Spell.</b> Spot a [gandalf] Wizard to search a Shadow player's discard pile and choose 2 minions, then return those minions to their owner's hand. If you do, reveal that hand and discard a revealed minion." The original 17_15 asks for one minion ("... choose a minion, then return that minion to its owner's hand. If you do, you may reveal that Shadow player's hand and discard a minion found there.").

**Built now:** the check after the return is "at least one minion was returned" (`IsGreaterThan`, `secondNumber: 0`). With exactly one minion in that discard pile it goes back to its owner's hand (CR 4.2 "effect": as much as can be done is done), the hand is revealed, and a minion is discarded from it: the one just returned, or another. You wrote the check into both cards in one commit (7c2f28d45d, 2022-12-25, "Restoring the "if you do" clause to A New Light and its errata"), with the erratum's `count: 2` on the line above it. The test added then has an empty pile only, and no test has ever had one minion in the pile. Neither rules document says what "if you do" asks for when the sentence before it could only be carried out in part.

**A (the words, Q-0012):** "If you do" asks for what the sentence says: 2 minions chosen and returned. With one minion in the pile it is returned and nothing more happens; the Free Peoples player has handed a minion back for nothing. One number changes in the definition (`secondNumber: 0` becomes `1`). The parked test goes live as it is, and no active test changes.

**B (as built):** returning what there was is enough. With one minion in the pile the erratum plays as the original does: one minion back, the hand shown, one minion discarded. Nothing changes in the game; the parked test is rewritten to read the reveal (the 3 cards of that hand) and the discard.

Parked on it: `WithOneMinionInTheShadowPlayersDiscardPileItIsReturnedAndHisHandIsNotRevealed`, which asserts A (as built it fails at the reveal, 3 cards shown where it expects none; it passes with the 1). Recommendation: A, lightly. It is what the card says, and under B the erratum's higher price (two minions back for one discarded) falls away whenever the pile holds exactly one.

**Ketura (2026-10-09):** A, that's an oversight.


### Q-0170 Keep or strike: an erratum whose change is to "you can spot" owes one hindered candidate (errata notes, after Anduin Banks 61_227)

The errata notes said: "an errata card's tests need no hindered card unless its text speaks of one." On Anduin Banks 61_227 (err-b61_132) the erratum's one change of words is "in the fellowship" to "you can spot"; the author left a hindered companion off by that line, and the reviewer added `AHinderedCompanionCannotBeSpottedAndSoAddsNothing`, which is the only test that notices the mutant `hindered: true` on the count.

**What I did (orchestrator, 2026-10-09).** Added to the notes: where the erratum's own change is to the words "you can spot" (from "in play", "in the fellowship" and the like), one hindered candidate that adds nothing is owed, because it is what shows the change. It is owed nowhere else, and no landed card is reopened for it.

**Keep**: the notes stay as amended; errata still to come whose change is that wording get the one test.

**Strike**: the notes go back to "no hindered card unless the text speaks of one"; 61_227 keeps its test (it passes and rests on CR 5.0 "hinder"), and nobody else owes one.

**Ketura (2026-10-09):** hmm, I question how often this will come up, but I think it's fine to keep.


### Q-0171 Deceit 68_29: when a Free Peoples card makes the Shadow player discard his own [gollum] condition (Introspection 12_29), is that the Free Peoples player discarding?

Deceit 68_29: "<b>Response:</b> If the Free Peoples player is about to discard your other [gollum] condition, remove (2) to prevent that." The original 18_29 reads "If a Free Peoples player's card is about to discard your other [gollum] condition, remove (1) to prevent that." Two Free Peoples cards have the SHADOW player do the discarding: Introspection 12_29 ("Spot a [gandalf] Wizard to choose an opponent who must discard one of his or her conditions from play") and Brooding on Tomorrow 10_14 ("... to choose an opponent who must discard one of his or her conditions"). There the Shadow player chooses which of his conditions goes, and may choose a [gollum] one.

**Built now (changed at this landing, F-0296):** the response asks whether the player discarding is Deceit's owner's opponent (`opponent: true`). So Sleep, Caradhras, Galadriel 10_11 and the Free Peoples player's discard at Heights of Isengard 11_244 are answered, and Introspection is not: the Shadow player picks Chasm's Edge, it is discarded, nothing is offered. Until this landing the erratum asked whether the CARD causing the discard was a Free Peoples card (`source: side(free people)`, your build of 2021-12-17, a copy of the original's Java of the day, when the trigger had no word for who discards). That gave the opposite on both boards: no offer at Heights of Isengard (a site has no side), which is F-0296 and was fixed as plainly against the text; and an offer on Introspection, which is this question. One word made both changes. The original 18_29 has been `opponent: true` since it was migrated from Java in 2024-05, so the two cards now play alike here.

**A (the words, Q-0012):** the Shadow player is the one discarding, so it is not a discard "the Free Peoples player is about to" make, and Deceit says nothing. Nothing more changes; `WhenAFreePeoplesEventMakesTheShadowPlayerDiscardHisOwnGollumConditionNothingIsOffered` stays as it is.

**B (whose card it is):** a discard a Free Peoples card forces is the Free Peoples player's, whoever picks the card. Deceit is offered on Introspection and Brooding on Tomorrow, and its owner can answer either by choosing a [gollum] condition and paying (2) to lose nothing. The definition then needs both tests (the player discarding is the opponent, OR the source is a Free Peoples card): a second Response block with the old `source: side(free people)`, or one engine word for the pair. That test is rewritten to assert the offer and the condition saved for (2).

Nothing is parked: the test of A passes as the card now stands and is active. Recommendation: A. It is what the erratum's sentence says, and the change of wording from "a Free Peoples player's card" reads as meant to say who discards. If you answer B, say whether the original 18_29 ("a Free Peoples player's card") should offer on Introspection too; since 2024-05 it does not.

**Ketura (2026-10-09):** A. There are a couple official card rulings for Old Noakes and Courteous Halfling which are relevant: if the player has a choice, they're not forced and therefore don't get the benefit of protection.


### Q-0172 Grond, Forged With Black Steel 68_82 (and 18_82): must the Shadow player discard 2 Free Peoples cards from the deck he searches, or may he take fewer?

Grond, Forged With Black Steel 68_82: "Each time the fellowship moves during the regroup phase, you may discard this possession to make the Free Peoples player shuffle 2 cards from their hand into their draw deck.  Search that deck and discard 2 Free Peoples cards from it." The original 18_82 ends "... to search the Free Peoples player's draw deck and choose 2 Free Peoples cards found there. Remove those cards from the game."

**Built now:** the Shadow player is shown the whole Free Peoples deck and must then choose exactly 2 Free Peoples cards from it (minimum 2, maximum 2); with one Free Peoples card in the deck that one goes without a question. The original 18_82 asks for exactly 2 in the same way. Your ruling on F-0049 and F-0051 is headed "Looking through a draw deck never forces a card to be taken" ("Those should all be an effective 0-N choice"), but the cards it was given on take into hand or play from the player's OWN deck, and the rule under it speaks of that deck (CR 4.2 "draw deck": "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck"). Here the deck is the opponent's and the cards are taken from him, so the ruling does not plainly reach it; the old tests read the maximum only.

**A (the words, and as built):** exactly 2 where the deck holds 2. Nothing changes; `ARegroupMoveLetsGrondBeDiscardedToShuffleTwoCardsFromHandInAndDiscardTwoFreePeoplesCardsFromTheDeck` reads minimum 2 and maximum 2 and stays as it is.

**B (F-0049 carried to a search of the opponent's deck):** 0 to 2. The Shadow player could look through the deck and discard one card or none, which matters only where the Free Peoples player wants cards in his discard pile (a deck that plays from it). The count in the definition becomes 0-2 on 68_82, and on 18_82 if you want the two alike; that test reads a minimum of 0, and one test of taking none is added (nothing discarded, the deck still shuffled).

Nothing is parked: the test of A passes as the card stands and is active. Recommendation: A. The card says "discard 2", any Free Peoples card will do so there is nothing to fail to find, and the Free Peoples player knows what his own deck holds.

**Ketura (2026-10-09):** I concur with your reasoning on A


### Q-0173 Doorway to Doom 68_134: does "This site cannot be replaced" hold while no fellowship is at it?

Doorway to Doom 68_134: "<b>Mountain.</b> When the fellowship moves to this site, each player wounds 2 of their characters.  This site cannot be replaced." The last sentence is the erratum's; the original 18_134 has none.

**Built now:** a `PermanentSiteModifier` (CantReplaceSite, filter self), the build of Mount Doom 15_193. Doorway cannot be replaced wherever it lies on the path: under the fellowship, ahead of it as a next site nobody has reached, or ahead of it with the other player's fellowship standing there. A card that would replace it pays its cost and replaces nothing (There's Another Way 12_40 is discarded, Úlairë Nelya 11_222 exerts, One Good Turn Deserves Another 11_49 is played and goes straight on to its offer of a burden). Your commit 81323cc833 of 2023-11-06 made it so ("Fixing Doorway to Doom PC Errata not being permanently in effect as intended"), and the old test `DoorwaytoDoomCannotBeReplacedWhenItIsNextSite` asserted it. Against that stands CR 4.2 "active" as Legacy Ruling 3 rewrote it, enacted 2025-04-18 and so later than the erratum: "A site's non-keyword game text does not apply unless the fellowship is at that site", with a card ruling for Mount Doom alone ("The game text on this card goes into effect the moment it enters play, even if no player has yet moved to it"). No rules document gives Doorway to Doom the same.

**A (the rule as it now stands):** the sentence is in force only while the fellowship is at Doorway to Doom. Lying ahead as the next site it could be replaced by One Good Turn Deserves Another. The definition becomes a plain `modifier`, as Steward's Tomb 68_139 has, and two tests are turned round to read the site replaced: `DoorwayPlayedAsTheNextSiteWoundsNobodyAndIsNotReplacedByAnotherPlayOfTheNextSite` and `DoorwayWhereTheOtherPlayersFellowshipStandsIsNotReplacedByAPlayOfTheNextSite`.

**B (as built, and as your commit says was intended):** the sentence holds from the moment the site is on the path. Nothing changes in the game or the tests. The rule in CR 4.2 "active" then has a second exception that is written nowhere: Doorway to Doom would want a card ruling like Mount Doom's (LR 3 is the Legacy team's and names Decipher's card only), or words on the card that say it.

Nothing is parked: both tests of B pass as the card stands and are active. Recommendation: B. The history says it was meant, and LR 3 chose the same for Mount Doom's "cannot be replaced". If B, say whether you want the ruling written down somewhere the campaign can cite; the ledger cites this number meanwhile.

**Ketura (2026-10-09):** the Mount Doom legacy ruling was meant to establish a precedent on site replacement, which this errata follows. B.


### Q-0175 Keep or strike: with no site to liberate, the Shadow player is not asked to prevent the liberation (Snowmane 7_250, Rapt Hillman 15_90; beyond F-0293)

**What changed (engine pass 19, the fix for F-0293).** To stop Sword-wall's pointless "liberate a site or exert a minion" question, the definition word `liberateSite` now says, at the moment it is about to be carried out, that it will do nothing when there is no site to liberate. `takeControlOfSite` has said the same of itself since engine pass 14. A prevention reads that answer too (your ruling on Q-0114: "prevention clauses should only trigger an ask if the thing they are preventing actually occurred"), so two cards F-0293 does not name behave differently:

- **Snowmane 7_250**: "Regroup: If bearer is Théoden, exert him to liberate a site. Any Shadow player may remove (2) to prevent this." With no site that can be liberated the ability is still offered and Théoden still exerts, as before; the Shadow player is no longer asked whether to remove (2).
- **Rapt Hillman 15_90**: "At the start of the regroup phase, the Free People's player may liberate a site. You may spot 3 Men to prevent this." With no site that can be liberated the Free Peoples player is still asked "Would you like to liberate a site?", as before; on a yes, the Shadow player is no longer asked whether to spot 3 Men.

With a site that can be liberated both ask exactly as they did. Shown by `WithNoSiteToLiberateTheShadowPlayerIsNotAsked` in mechanics/PreventionIsAskedOnlyWhenItsEffectWillHappenTests (Snowmane: asked with a site, not asked without). Neither card has campaign tests yet.

The change is made and stays unless you strike it.

**Keep**: as above.

**Strike**: on those two cards the Shadow player is again asked to pay to prevent a liberation that cannot happen. `liberateSite` would keep its answer for a choice and withhold it from a prevention, and that one test would go. Sword-wall's fix, and the rest of F-0293, are not touched by a strike.

**Ketura (2026-10-09):** Keep


### Q-0176 Race Text 91_2: is the card taken named in the game log, when the text says "any card" and no "reveal"?

Race Text 91_2: "At the start of each of your fellowship phases, you may take any card into hand from your draw deck."

**Built now:** the owner is asked yes or no; on yes he looks through his draw deck and takes one card or none, the other player is shown nothing, the deck is shuffled, and the game log says "Free Peoples Player puts Uruk Savage from deck into their hand", with the link any player can click (`reveal: true`). Your ruling on Q-0025 has a card taken "by type, where the text does not say 'reveal'" revealed by the log line alone: there the log is what shows the card was of the type. Here any card will do, so there is nothing to show. This is the only take from a draw deck in the card pool that names no type and no side; every other use of the effect has `reveal: true` as well, so the value here may be habit.

**A (the words):** nothing is revealed. The log says "Free Peoples Player puts a card from deck into their hand" and the other player learns only that a card was taken. One word changes in 91_2 (`reveal: false`), and the parked test goes live.

**B (as built):** the log names the card, as it does for every other take from a draw deck. Nothing changes in the game; the parked test is deleted and one active test reads the log naming the card.

Parked on it: `TheCardTakenIsNotMadeKnownToTheOtherPlayer` (fails as built, at the log; passes with `reveal: false`, every other test of the card still passing). Recommendation: A. At a table the card goes from deck to hand unseen, and the modifier is worth less if the opponent reads what was fetched each turn.

**Ketura (2026-10-09):** Yeah there's a rule about this, A is the correct way.


### Q-0177 Race Text 91_4: does "Your weapons lose all damage bonuses" reach a bonus a weapon's ability gives (Gondorian Sword 7_98)?

Race Text 91_4: "Your weapons lose all damage bonuses." Gondorian Sword 7_98: "<b>Skirmish:</b> Discard 3 cards from hand to make bearer <b>damage +1</b>."

**Built now:** every damage bonus that comes from a weapon you own is cancelled on its bearer (`CancelKeywordBonus`, `from: your,weapon`), whether the weapon gives it as standing text ("Bearer is damage +1") or by an ability that was used. With the modifier the Sword's ability is still offered, the three cards are discarded, and the bearer gains nothing. The bearer's own damage +1 (Gimli, Lurtz) stays, and so does a bonus from a card that is no weapon (Ring of Guile 9_8).

**A (as built, "all damage bonuses"):** a bonus bought from the weapon's ability is the weapon's bonus and is lost with the rest. Nothing changes; `ADamageBonusBoughtFromAWeaponsAbilityIsLostToo` stays as it is. Under A, say whether the Sword's ability should be marked no-effect (Q-0034) while its owner has this modifier: today it is offered unmarked and its cost is paid for nothing.

**B:** the weapon loses the bonuses it gives as standing text; a bonus an ability gives has been given to the character and stays. The cancel would have to tell a standing bonus from one an ability added, which the modifier cannot do today (an engine change), and that test's last reading becomes damage +1.

Nothing is parked: the test of A passes and is active, and its twin with the modifier across the table (`ADamageBonusBoughtFromAWeaponsAbilityIsKeptWhenTheOpponentHasTheModifier`) reads the +1 either way. Recommendation: A.

**Ketura (2026-10-09):** hrrm. I think A.


### Q-0179 Race Text 91_8 (and 91_12): "While ..., your move limit is -1" with no "for this turn": does the change end with its condition, or hold for the whole turn?

Race Text 91_8: "While you can spot 5 or more companions, your move limit is -1." Race Text 91_12 has the same shape: "While you can spot a Dwarf companion, an Elf companion, and Gandalf, your move limit is +1."

**Built now:** both are a plain standing modifier (`ModifyMoveLimit` with a `CanSpot` requirement), read afresh each time the limit is used. With five companions the limit is 1; when one dies in the regroup phase (Morgul Slayer 3_93 wounding a Dwarf Guard) the limit reads 2 at once and the fellowship is asked about another move.

CR 4.2 "move limit": "If the move limit is modified for a turn, then that modification is in effect for the whole turn, even if the conditions for the modification change." Neither text says "for this turn". The game applies that rule to Decipher's cards whether they say it or not: Théoden 13_137 ("While you can spot Théodred, the move limit is +1") and Radagast 9_26 ("The move limit is +1") are each built as a once-a-turn trigger that adds the change until the end of the turn, as The Number Must Be Few 1_260 and Derndingle 4_330 are, which do say "for this turn". The only plain "while" builds in the card pool are these two modifiers and The Number Must Be Few 51_260, whose erratum says so in its own second sentence (Q-0122). On Q-0122 you called the turn-lock a Decipher ruling the PC's cards work around; these two texts have no sentence that sets it aside.

**A (as built, a plain "while"):** the change goes when its condition does, and comes back if the condition does. Nothing changes; `WhenAFifthCompanionDiesTheMoveLimitIsTwoAgainAndTheFellowshipMayMoveAgain` stays as it is, and 91_12 is tested the same way when its batch comes.

**B (CR 4.2 "move limit", as Decipher's cards are built):** once five companions have been spotted in a turn the limit is -1 for the rest of that turn, whoever dies; and 91_12's +1, once given, lasts the turn after Gandalf has gone. Both definitions become the once-a-turn trigger 1_260 has, and that test's last two readings turn round: the limit stays 1 and the turn ends with no question of another move.

Nothing is parked: the test of A passes and is active. No old test took a reading (the old files read the limit only in the fellowship phase). By the rules as the game applies them to Decipher's cards it is B; it is A if these modifiers are to behave as the PC's errata do.

**Ketura (2026-10-09):** These ones should follow the basic rules around move limits, which is that they are permanent. B.


### Q-0180 Deepest Delvings 101_2: was the sheet's revision 1 ("add (X) ... where X is the number of cards stacked here") ever released?

The master errata sheet has a revision 1 row for Deepest Delvings (2023.05.17, batch "06: 2023 Summer"):

"Each time a [dwarven] card is discarded from the top of your draw deck, you may add (X) to stack that card here, where X is the number of cards stacked here. Maneuver: Exert a Dwarf to take a card stacked here into hand."

**What is built and tested now.** The definition says `revision: 0` and "you may add (1) to stack that card here"; the V1 sheet and the picture the client shows say "add (1)" too. 101_2 is landed with its tests asserting (1).

**A (the row was never released):** nothing changes in the game or the tests. The sheet's row is marked as unreleased if you want it marked; the packet goes on printing it as "a later row" until then.

**B (revision 1 is the card):** the definition is behind: text, `revision: 1` and the cost become (X), the number of cards stacked there, and 101_2's tests are re-worked for it (the first stack is free, the second costs 1, and so on).

Nothing is parked on this. Found by engine pass 19's tools item, which made the packet print a sheet row of a higher revision than the definition's (after F-0294, Shadowfax 67_24, where the later row was the released one). It is the only card with such a row today. Recommendation: A, since three witnesses say (1); but only you know what was released.

**Ketura (2026-10-09):** Oh. No, I don't think it was released, good catch. The current one is too braindead and probably should have been altered, but we didn't muster up the willpower to do it.


### Q-0181 Rath Dínen 100_1: is the arrow Left (as defined) or Right (as Steward's Tomb)?

Rath Dínen 100_1 is defined with `direction: Left`. Steward's Tomb, which you say it is a partial version of, has `direction: Right` (18_139 and the erratum 68_139 alike). The master sheet's row for Rath Dínen gives no direction, and the batch that landed the card did not read its picture (https://i.lotrtcgpc.net/sets/vset0/LOTR-ENV0S001.0_card.jpg), so the stats test asserts Left on the definition's word alone.

**A (Left is what the card shows):** nothing changes.

**B (it should be Right, as the Tomb):** the definition and the stats test's one line are corrected.

Nothing is parked. Its twilight, 0 against the Tomb's 1, is in the sheet's row and is not in doubt. (Doorway to Doom 68_134, F-0302, is the same kind of question.)

**Ketura (2026-10-09):** Ha, let's make it match Tomb I suppose.


### Q-0182 Race Text 92_1: under "up to 6 copies of any card", how many copies of the Ring-bearer may the draw deck hold beside him?

"Your draw deck may contain up to 6 copies of any card (instead of 4)." The rule it bends, CR 4.2 "building your deck": "You may have up to four copies of each card title (ignoring subtitles) in your draw deck. ... Exception: Since one copy of your Ring-bearer is always part of your starting fellowship, you may have only three copies of that character in your draw deck." The modifier says nothing of the Ring-bearer.

**Built now:** deck validation has no separate Ring-bearer rule. It counts the Ring-bearer (and the Ring, and the sites) together with the draw deck, by title, against the one copy limit, and the modifier's `maximumSameName: 6` raises that limit. So a player with 92_1 and Frodo as his Ring-bearer may have five more Frodos (of any subtitle) in his draw deck; six are refused.

- **A (as built):** the Ring-bearer is one of the six copies, as he is one of the four without the modifier: five in the draw deck. Nothing changes. `TheRingBearerIsOneOfTheSixCopies` asserts it (5 pass with the modifier and fail without; 6 fail with it) and is active.
- **B (the modifier's words to the letter):** "your draw deck may contain up to 6 copies of any card", and the Ring-bearer is not in the draw deck: six Frodos in the deck beside the Frodo who bears the Ring. The format's count would leave the Ring-bearer out for a player under this modifier (an engine change), and the test's refusal of 6 becomes a pass.
- **C (the rule's exception to the letter):** the modifier changes the general limit only, and "only three copies of that character" stands as written: three in the draw deck, with or without the modifier. An engine change (a Ring-bearer limit of its own), and the test's pass at 5 becomes a refusal at 4.

No old test put the Ring-bearer's card in the draw deck, and no test is parked: the game passes A. I recommend A, since the exception gives its own reason (he is one copy, always in the starting fellowship) and that reason carries over to a limit of 6. Say only if it should be B or C.

**Ketura (2026-10-09):** I think A is the reasonable reading; the 3 is an illustrative demonstration and not a set point, imo.


### Q-0183 Race Text 92_9: does "companions cannot be played" bar the other player too, while the owner's fellowship is at site 6 or higher?

"While your fellowship is at site 6 or higher, companions cannot be played." "Companions" has no "your".

**Built now:** `CantPlayCards` with `filter: your,companion`, in force while the current site is 6 to 9. The owner plays no companion at site 6 or beyond, from hand or by another card's effect (Théoden 8_92's "you may play a [rohan] companion" is withheld), and the other player plays his companions as ever. The old test `OpponentNotAffected` asserts exactly that, on the board that tells the two readings apart: the modifier is player 2's, both fellowships stand at site 6, and player 1 plays Éomer.

- **A (as built, and the old test):** the restriction is its owner's alone, a handicap on him like the rest of an intensity +4 text. Nothing changes.
- **B (the words to the letter):** a word with no "your" names no side, as you ruled of race words (Q-0058, Q-0068), so nobody plays a companion while the owner's fellowship is at site 6 or higher, wherever the other player's own fellowship stands. The filter loses `your`, the requirement has to read the owner's position and not the site of whoever's turn it is, and `OpponentNotAffected` is turned round.

The same set builds its other bare prohibition without "your": Race Text 92_16, "Gollum and Smeagol cannot take wounds", is `filter: or(name(Gollum),name(Sméagol))`, either player's.

Parked on this: `TheOtherPlayerCannotPlayCompanionsEither_WhileTheOwnersFellowshipIsAtSite6OrHigher` (reading B: it fails against the game as it is and passes with `your` out of the filter). `OpponentNotAffected` stays active. Recommendation: A, since the old test was written to say so and the text is a handicap on its owner.

**Ketura (2026-10-09):** A. The rule we've been following is that any meta-site with "you" or "your" anywhere in the text causes the entire card to only affect the owner (modulo context like "your opponent" etc).


### Q-0184 Race Text 92_18: on the move from site 6 to site 7, does "(or 2 if at site 7 or higher)" discard one companion or two?

Race Text 92_18: "Each time your fellowship moves, discard 1 non-Hobbit companion (or 2 if at site 7 or higher)."

**Built now:** a required trigger on `type: Moves`, its count a `Conditional` on `Location siteNumber(7-9)`. The game raises "fellowship moves" after the marker has been moved (MovementGameProcess: "moved from", the marker, "fellowship moves", "moved to", the twilight), so the count reads the site ARRIVED at: one companion on every move up to 5 to 6, two on the move from 6 to 7 and on every move after.

CR 4.2 "moving your fellowship" has the other order: 'Perform "When the fellowship moves..." actions' stands before "Move your player marker to the next site", and "After you have completed any moving actions and have added twilight tokens ... the fellowship has moved to the new site". Since engine pass 16 (F-0233) the game's own standing checks agree that the fellowship is not yet "at" the new site while the move is being carried out; a trigger's `Location` requirement is not held back in the same way. The old tests were laid out for the site left: `DiscardsOneAtSite6` is headed "Moving from site 6 (at site 6, below 7 threshold), should discard 1" and `DiscardsTwoAtSite7OrHigher` moves from site 7 ("site 7 >= 7 so discard 2"). But the site-6 test has Aragorn alone, who goes whether the count is 1 or 2, so it passes either way and settles nothing.

**A (the site left; the rules' order and the old tests' comments):** on the move from site 6 the fellowship is still at site 6 and ONE companion is discarded; two on the moves from 7 and from 8. The definition's trigger becomes `type: MovesFrom`, which is raised before the marker moves; nothing else changes, and with it every test of the card passes, the parked one included. No other card is touched: this is the only definition in which a `Moves` trigger reads a site number, region or terrain.

**B (the site arrived at; as built):** "if at site 7" is where the fellowship has got to, so the move from 6 to 7 costs TWO. Nothing changes in the definition; the parked test is rewritten to expect two of two chosen, and `DiscardsOneAtSite6` has its heading corrected.

Parked on this: `MovingFromSite6DiscardsOneOfTwo` (Aragorn and Gimli at site 6, the fellowship moves, one of the two is chosen; the game asks for two). The active tests hold under both readings: from site 5 one of two, from site 7 both of two, from site 8 two of three.

Recommendation: A. The rules, the old tests' own comments and the game's treatment of standing checks during a move all put the fellowship at site 6 when this is carried out, and the correction is one word on this card alone.

**Ketura (2026-10-09):** I think A is the correct reading as per that movement breakdown you cited. However, if the engine is doing that in the wrong order, that needs adjusting, rather than switching the trigger to "moves from".


### Q-0185 Race Text 92_19: does "sites from any block" also let in a site the format has X-listed, or the first printing of a site it plays as errata'd?

Race Text 92_19: "Your adventure deck may contain sites from any block. (You may either mix any Movie block sites together, or use any Shadow sites, but not both.)"

**Built now:** the modifier sets `skipSiteBlockValidation`, and for the player who has it the format then asks two things of his nine sites and no more: that each card is a site, and that Fellowship, Towers or King sites are not beside Shadows sites. (The format's counting rules stand: nine sites, one of each number where sites have numbers, no more than three of one Shadow number.) The check every other card of the deck gets is skipped whole for his sites, so with the block and set rules go two things the text does not speak of:

- the format's X-list. Pre-Shadows Multipath bans eight sites (1_331, 1_338, 1_354, 1_355, 1_360, 4_338, 4_355, 4_357). Nine Fellowship sites with Emyn Muil 1_360 at 9 are refused without the modifier ("Card is X-listed: Emyn Muil") and validate with no error with it, though all nine are of one block;
- the format's errata. In PC Movie the nine Fellowship sites with Ettenmoors 1_331 as first printed are refused without the modifier ("Non-errata version of an errata'd card: Ettenmoors") and validate with no error with it.

**A (the text lifts the block rule and nothing else):** a site the format has X-listed stays refused, and so does the first printing of a site the format plays as errata'd, with the modifier as without. No change to the definition can do it: the engine's branch for the override (`DefaultLotroFormat.validateSites`) would apply to each site the ban and errata tests of `validateCard`, leaving out only "Card not from valid set", which a site of another block must be let past. The parked test then goes live as written. The old tests' own mixed Movie deck holds Emyn Muil 1_360 and would need another site 9 in Multipath; they read the block objection alone, so they pass either way.

**B (as built: any site card at all):** under this modifier a site is legal whatever the format says of it. Nothing changes; the parked test is turned round to assert that the X-listed site passes.

Parked on this: `MovieBlock_ASiteTheFormatHasXListedIsStillRefusedWithContext` (Multipath; fails at its last line only, where the deck with Emyn Muil validates with the modifier). No old test takes a reading.

Rider, the same doubt and no test laid (the Hobbit sets come last): the override branch counts Fellowship, Towers and King sites as Movie, Shadows as Shadows, and a Hobbit-block site as neither. Dol Guldur 32_46 validates with no error among eight Fellowship sites in Multipath, PC Movie and Expanded, and among eight Shadows sites in Expanded. The parenthesis names Movie block and Shadow sites only. Say if Hobbit sites are meant to be let in.

Recommendation: A. "From any block" is about which path a site belongs to; an X-list and an erratum are the format's word on one card, and nothing in the text touches them.

**Ketura (2026-10-09):** ha! Good question. Let's see...I think we can safely ignore the X-list issue, as nobody is going to run RTMD on anything but a common format, and none of those have banned sites. So I think the errata question is the only one that can crop up, as we do run PC-Movie RTMD. I think A is preferred. As for the hobbit sites, they don't need brought in I think.


### Q-0186 Race Text 92_25: with the Ring-bearer hindered, are the burdens still "on the Ring-bearer" (Q-0103 was "on Frodo")?

Race Text 92_25: "Each time your fellowship moves, add (1) for each burden on the Ring-bearer."

**Built now:** a required trigger on the owner's fellowship moving, its amount a bare `ForEachBurden` with no spot of the Ring-bearer, so every burden is counted whether he is hindered or not. For Frodo 103_107 ("strength +1 for each burden on Frodo") spots Frodo before it counts, and you ruled there (Q-0103) that the burdens are not "on Frodo" while he is hindered: "the burdens can still be spotted by themselves, but ... while Frodo is hindered, he himself can't be, so they're 'on' 'a Free Peoples card' that can't otherwise be distinguished." That was said of his name; nothing ruled speaks of "the Ring-bearer". The board arises: a Ring-bearer hindered during the turn (Was He Dear to Thee? 103_104, in the maneuver phase; For the Shire 103_108) is still hindered when the fellowship moves again in the regroup phase, since the Free Peoples player has not reconciled.

**A (as built): the burdens are counted.** "The Ring-bearer" is not a title, culture, type or race, which are what a hindered card loses, and CR 5.0 "hinder" goes on calling him by it ("If the Ring-bearer is hindered, burdens may be spotted and counted"; "While the Ring-bearer is hindered, they cannot be corrupted"). CR 4.2 "burden" puts every burden on the Ring-bearer, so "each burden on the Ring-bearer" is each burden. A second move with Frodo hindered, a Dwarf Guard beside him and 2 burdens adds 3: 1 for the Guard and 2 for the burdens (a hindered companion adds nothing for the move). Nothing changes.

**B (as you ruled for "on Frodo"): a hindered Ring-bearer cannot be made out, so no burden is "on the Ring-bearer".** The same move adds 1, and a player whose own Ring-bearer is hindered moves free of this modifier. The definition's count goes inside a `Conditional` on spotting the Ring-bearer, as 103_107 has it for Frodo (tried: the test below notices it and every other test of the card still passes), and that test's last reading becomes +1.

**Parked on this:** nothing. `TheBurdensOnAHinderedRingBearerAreStillCounted` asserts A and is active, since the game passes it. The one other card with these words is Mysterious Wizard 1_78 ("+4 if there are 4 or fewer burdens on the Ring-bearer"): the same answer settles it when set 1 is worked, where under B a hindered Ring-bearer would read as no burdens and give the +4.

Recommendation: A. Q-0103 turned on a name the hindered card no longer has; "the Ring-bearer" is a role the rules keep giving him while he is hindered, and Decipher's "burdens on the Ring-bearer" is only the long way of saying "burdens". It is not a sure call: your reason for Q-0103 stretches to B if a hindered Ring-bearer is to be no more than "a Free Peoples card" for every card that names him.

**Ketura (2026-10-09):** Burdens can't go on anything *but* the Ring-bearer, so I call this a case of redundant verbiage. A. The Frodo case was one where there could be someone else they are on, such as Sam or Galadriel or whoever, but there is no alternate here.


### Q-0187 Race Text 92_35: is "no more than one wound from each character" counted by the turn, or over the whole stay at the sanctuary?

"While at a sanctuary, you may heal no more than one wound from each character."

Built now: the count is by the turn. A character healed at the sanctuary cannot be healed there again until the end of that turn's regroup phase, and the next turn starts a fresh count. So Aragorn, healed by Master of Healing 3_22 in the regroup phase of the turn the fellowship arrives at site 3, is offered the sanctuary healing when the next turn begins at the same site, and takes a second wound off.
**A:** one wound a turn, as built. A fellowship that arrives, heals by a card that turn and begins its next turn there has one wound from each character in each of the two turns. Nothing changes; `AWoundHealedInTheRegroupPhaseDoesNotCountAgainstTheNextTurnsSanctuaryHealing` stays as it is.
**B:** one wound for the whole stay. A heal on the turn of arrival uses up the character's one wound, and the sanctuary healing at the start of the next turn passes him over. The bar would last until the fellowship leaves the site and not to the end of the regroup phase, and that test is turned round.
No old test takes a side: each heals twice within one fellowship phase, which both readings refuse. Nothing is parked; the test of A is active. (One wound in each phase, as you ruled for Hospitality of the Dwarves 101_1, is not on the table here: this card's old Athelas test refuses a fellowship-phase heal after the start-of-turn one.)
The words lean to B ("while at a sanctuary" names the stay, and a stay runs over two turns); A is what was built, and the two differ only when a card heals on the turn of arrival.

**Ketura (2026-10-09):** I would do by the turn. Tracking stuff across multiple turns is a negative play experience that I try to avoid. A.


### Q-0188 Race Text 92_36: is "Fellowship: Exert the Ring-bearer to discard another companion" the owner's alone, when the sentence has no "your"?

"Fellowship: Exert the Ring-bearer to discard another companion." (The sentence before it: "You cannot have more than 5 companions in your fellowship.")

Built now: only the modifier's owner is offered the action, in his own fellowship phase. The old test `OpponentCantUseFellowshipAction` asserts it: with the modifier across the table, player 1, with Frodo able to exert and Gimli there to discard, is not offered it.
The doubt is that the sentence has no "your". The five other modifiers whose phase action has no "your" are built open to whichever player may act in the phase, and their old tests assert that the other player may use them: 92_5 ("Fellowship: Exert X companions to remove (X)"), 92_13 ("Shadow: Play a possession to make the Free Peoples player exert a companion"), 92_14 ("Shadow: If 3 or more [raider] minions have been played this phase, discard Sam"), 92_21 ("Fellowship: Play a companion; their twilight cost is -4"), 92_22 ("Shadow: Play a weather condition to draw a card; its twilight cost is -2"). This is the one such action held to its owner.
**A:** the owner's alone, as built: the action is the relief for the first sentence, which speaks to "you". Nothing changes.
**B:** open to the Free Peoples player of the turn whoever owns the modifier, as the other five are. The owner requirement comes off the action, the other player may exert his own Ring-bearer to discard one of his own companions, and `OpponentCantUseFellowshipAction` is turned round.
Nothing is parked; the old test stays active. I would keep A: the old test asserts it, and the action has no use to a player the limit does not bind.

**Ketura (2026-10-09):** A, yeah, for the same reasoning as mentioned above.


### Q-0189 Race Text 94_3: may a Shadow condition or item be included in "your starting fellowship"?

"You may include items, conditions, allies, and followers in your starting fellowship. You cannot play more than 1 card with 0 twilight cost of each card type this way."

Built now: the owner's starting-fellowship list is every companion, possession, artifact, condition, ally and follower in his draw deck that he could play, of either side. Goblin Armory 1_173 and Saruman's Ambition 1_133 (Shadow conditions of cost 0 that play to the support area) are offered beside his Free Peoples cards, go to his support area and are in play when the game begins. The type list is the engine's (`PlayerPlaysStartingFellowshipGameProcess.getPossibleCharacters`); the definition is a bare flag and cannot change it. The old tests' deck held Free Peoples cards only, so no old test takes a side.
The doubt is that "items, conditions, allies, and followers" names no side, while the thing they are included in is "your starting fellowship". The rule the sentence lifts is CR 4.2 "starting fellowship": "You may then play other companions (not allies, possessions, artifacts, or conditions)".
**A:** a starting fellowship is Free Peoples cards; the sentence takes away the rule's parenthesis and nothing more. Shadow conditions and Shadow items are not offered. The engine's list gets `Side.FREE_PEOPLE` on the five extra types (an engine change, filed as a finding once you say A), and the parked test goes live.
**B:** as built: a card type with no side word is either side's ("a bare word names no side"), so the owner may start with his Shadow support-area conditions already in play, at their cost against the 4. The parked test is replaced by one that asserts the offer, the play and the count. There is then still a fault to file: a Shadow card's cost is handled two ways at once. Goblin Swarms 1_183 (a Shadow condition of cost 1) is not offered while the running total is 0, as if the twilight pool had to pay for it, is offered once a cost-1 Free Peoples card has gone down, and then adds 1 to the total (1 to 2) as a Free Peoples card does. Under B it should be offered at 0 and counted once.
Parked on it: `NoShadowCardIsOfferedForTheStartingFellowship` (Three Monstrous Trolls offered, Goblin Armory not), which asserts A and which the game fails. I would say A: the way a Shadow card's cost is treated suggests Shadow cards were never thought of here.

**Ketura (2026-10-09):** ha, yes, A. No playing minions during the starting fellowship :)


### Q-0190 Race Text 94_5: are the Dark Lord and a World Champion to be tested by the account names in the definition?

"If your opponent is the Dark Lord or a World Champion, or if they are placed in the top 10% of the current league, they cannot choose to move during the regroup phase in region 3."

Built now: the two titles are account names written in the definition, one each: the Dark Lord is `bign19` and a World Champion is `basmelis`. Each is asked of the modifier's opponent, as the league place is. The league alternative is tested from both seats (an opponent tenth of a hundred is barred, eleventh is not, a place held by the owner bars nobody). The two titles have no test at all: the test table's players are always "Free Peoples Player" and "Shadow Player", so no table seats a title-holder. As things stand, a definition that asked a title of the OWNER (so that a Dark Lord who has this modifier bars whoever he plays) would pass every test.
**A:** each title gets its tests, the names taken from the definition since nothing else says who holds one: an opponent seated as `bign19` cannot choose to move in region 3, the same for `basmelis`, and the owner seated under either name bars nobody. That needs the test table to seat a player under a chosen name (G-0039), and the tests are edited whenever a title changes hands. 94_5 stays parked on G-0039 until it is built.
**B:** who holds a title is data and not the card's to test; the name check has its own mechanic test (at/RequirementsAtTest.playerIs) and that is enough. G-0039 is struck, the ledger says the titles are left to the mechanic, and 94_5 is ready as it stands.
No test is parked; the card is parked on G-0039. I would take A: three short tests, and the only place a title asked of the wrong player would show.

**Ketura (2026-10-09):** hmm. What's more important is that the player-name-checking code has card-agnostic tests somewhere. Given that these will change over time if past world champions reveal their account names (or if someone *finally* beats basmelis so he doesn't win a fourth time in a row), let's do B, but with the mechanic test coverage.


### Q-0191 Race Text 94_10: is X in "damage -X" held to the damage bonus the chosen minion has?

"Skirmish: Make your minion damage -X until the regroup phase to make another minion strength +X until the regroup phase."

Built now: only a minion of the owner's that has a damage bonus may pay, and X runs from 1 to that minion's own bonus. An Uruk Fighter (damage +1) gives +1 without a number being asked for; an Easterling Berserker (damage +2) is asked for 1 or 2, and an Uruk Fighter beside him adds nothing to that 2. With no minion that has a bonus the action is not offered. Two old tests assert it: `TradesDamageForStrengthUntilRegroup` reads a Goblin Backstabber as not offered for the cost, with the comment that X is bounded by the chosen minion's damage bonus, and `UnavailableWithoutADamagedMinion` reads the action as not there with the Backstabber alone.
The doubt is that the sentence gives X no bound, and a number may be modified below zero and is then read as zero (CR 4.2 "modifier": "Numbers can go below zero until the final check is made", the Enduring Evil example).
**A:** as built: "damage -X" is the price, and a minion can give up only the bonus it has. Nothing changes.
**B:** X is any number, on any minion of the owner's. A minion with no damage bonus may pay and X has no top, so the owner makes another minion as strong as he likes at no loss. The damage word comes off the requirement and the cost's choice, the number loses its maximum, and the two old tests above and the new tests' readings of the maximum (1 to 2) are turned round.
Nothing is parked: the tests of A are active and the game passes them; no test of B is written.
I would keep A: under B the price is no price, and both old tests say A was meant.

**Ketura (2026-10-09):** A, yeah


### Q-0192 Race Text 94_12: may the minion just discarded for the cost be the one taken back?

"Shadow: Discard a minion from hand to take a minion into hand from the discard pile."

Built now: the minion just discarded is left out of the choice (`choose(minion,not(memory(discardedMinion)))`). The old test `DiscardAMinionFromHandToTakeOneFromDiscard` asserts it, with the comment "the 'They Are Coming' ruling - the card just discarded as the cost cannot be the one retrieved". With no other minion in the discard pile the action is still offered (it has a cost: Q-0034); the minion goes to the discard pile and nothing comes back (probed at landing).
The doubt is that the rule the comment leans on speaks of a play: CR 4.2 "effect", "If an action plays a card from your hand (or discard pile) as part of its effect, then that card must be in your hand (or discard pile) before you can begin to perform that action." This action plays nothing, the sentence has no "another", and the discarded minion is in the discard pile when the effect is carried out. No other card has this shape with the overlap possible (Return to Us 13_39 discards Shadow cards to take a [gandalf] event).
**A:** as built: the minion taken is another one. Nothing changes and the parked test is deleted. (If A, say whether the action should also be withheld when no other minion lies in the discard pile, as a play from the discard pile would be: today it is offered there and the minion is simply lost.)
**B:** the sentence as written: the minion just discarded is among the choices, and with no other minion there it comes straight back. `not(memory(discardedMinion))` comes off the definition, the parked test goes live, the old test's reading of the discarded Backstabber as not offered is turned round, and `ActionIsAvailableToShadowEvenWhenFreepsOwnsTheMod` gets the choice it is now spared (the Brigand it discarded beside the Troop). Tried at landing: with that one change the parked test passes and those two are the only tests that fail.
Parked on it: `TheMinionJustDiscardedForTheCostMayBeTheOneTakenBack`, which asserts B; the game fails it where the Backstabber should be offered.
I would keep A: the exclusion was written on purpose, taking back the card just discarded does nothing, and you kept the like exclusion on Foul Things 52_59 (Q-0126).

**Ketura (2026-10-09):** the ruling for They Are Coming applies here: discarding a card from hand to retrieve a card is not allowed to pull the card just discarded. A.


### Q-0151 Keep or strike: Come Down is withheld when no ally can take an archery wound

Come Down 4_146 and 54_146: the archery ability is now withheld when it would do nothing. Engine pass 16 (F-0243) sent the archery wounds allies cannot take on to companions, and added a requirement to both definitions: an ally must be spotted. Engine pass 17 (F-0275, your Q-0147) narrowed it to an ally who is not barred from archery fire, with a new filter word, `barredFromArcheryFire`. The agents' reason: used with no ally able to take a wound the ability does nothing and has no cost, so by Q-0034 it is not offered, and option A of Q-0136 as you ruled it says as much. The Shadow player loses nothing: if an ally arrives later in the archery phase he is offered it then. Neither requirement was named in the queue.
**Keep** (what happens if this goes unanswered), or **strike**: the requirement comes out of both definitions, with its two card tests and two mechanic tests, and the ability is offered with no ally to wound.
Reports: engine-pass-16/archery.md, engine-pass-17/streets.md.

**Ketura (2026-10-09):** Keep


### Q-0152 Keep or strike: the fix of F-0245 touches nine engine classes

The fix of F-0245 (Uruk Regular 54_192: a printed "(limit -N)" on a play's discount is the card's for the whole phase) touches nine engine classes, where the brief tells an agent to stop at a handful and ask. Three carry the change. In the other six (the five `PlayCardFrom...` effects and the `Playable` requirement) the same two or three lines are swapped, because a discount is resolved in each of them and the item asked that a limit wrapper inside a discount either work or fail when the card is loaded. No rule's flow is rewritten, and no existing card but 54_192 plays differently. Commit 83596b32f. (F-0267, found on the way and fixed in the same commit, is among the findings below.)
**Keep** (what happens if unanswered), or **strike**: the commit is reverted and 54_192 goes back to parked on F-0245.
Report: engine-pass-16/limit.md.

**Ketura (2026-10-09):** Let's strike this one. I'm not convinced that a restriction on a discount is treated the same as a strength bonus limit, and in Regular's case it was moot anyway.


### Q-0153 Keep or strike: how the engine knows a discard that waits for the move's twilight, and the 18 cards that now wait (F-0233)

F-0233 (your Q-0133) is built: a card discarded for where the fellowship is now goes when the move's twilight has been added, not as the site marker lands. The order of a move's steps is unchanged and no definition changed; what changed is when one kind of standing check is looked at. Two things for your judgment.
How the engine tells that kind of check: a `ConstantlyCheck` whose `requires` holds a `location`, read from the definition's JSON. The agent chose that over (1) making every ConstantlyCheck wait during the arrival, which would let a minion played by a "when the fellowship moves to" text count as played before it is discarded (the F-0139 behaviour), and (2) a typed method on Requirement, which is eight or nine files for the same behaviour. A check on the site written as a filter (`canSpot` with `currentSite`) is not seen; no definition has one.
What else now waits the same way, 18 definitions: Bill the Pony 3_106 and 53_106; Asfaloth 1_31; Black Steed 1_208; Goblin Man 2_42; Bill Ferny 2_75 and 52_75; The Balrog 2_51 and 2_52; the tentacles 2_58, 2_66, 19_19, 19_20, 101_33, 101_66 and Watcher in the Water 2_73; Smaug 30_66; Rivendell Waterfall 1_342 (its move limit +1 is granted when the move there is finished). For all but Bill the only difference is that the card is still in play while "moves" and "moves to" texts are carried out.
**Keep** (what happens if unanswered), **strike**, or say which of the two other ways of recognising the check you would rather have.
Report: engine-pass-16/bill.md.

**Ketura (2026-10-09):** Keep


### Q-0154 Keep or strike: DuplicateActionFromPhase refuses an unknown phase when a card is loaded

The loader item made a time string that names no phase (`until: end(turn)`) fail when the card is loaded, as asked. One step beyond it: `DuplicateActionFromPhase` read its `oldPhase` and `newPhase` through the same lookup and kept a null in the same way, and it now refuses an unknown name too. One card uses it (103_7, `Fellowship` / `Regroup`) and loads as before. No definition turned out to have a bad time string.
**Keep** (what happens if unanswered) or **strike**.
Report: engine-pass-16/loader.md.

**Ketura (2026-10-09):** Keep


### Q-0155 Keep or strike: chosen cards and "discard each" are also in card id order (beyond Q-0142)

Q-0142 is built as F-0268: cards an effect takes by "each" or "every" are taken in card id order. Two steps go beyond those words.
1. Cards a player CHOSE together ("wound 2 Orcs") are also taken in id order, whichever he named first (`ChooseActiveCardsEffect`). The agent's reason: without it two rules would hold side by side, since some effects read the chosen cards again through the filter (now ordered) and others kept the order they were chosen in.
2. "Discard each" lays the cards on the discard pile in id order (`DiscardCardsFromPlayEffect`).
**Keep** both (what happens if unanswered), or **strike** either: for 1 the order the player chose in stands again; for 2 the pile order goes back to what it was, which is not the same from run to run.
Report: engine-pass-17/order.md.

**Ketura (2026-10-09):** I'm okay with keeping I think but the order really doesn't matter unless a specific counterexample is brought to my attention.


### Q-0156 Keep or strike: no pop-up when a reveal from a hand that holds cards reveals none (beyond Q-0144)

Q-0144 is built: a reveal from an empty hand asks nobody, and the log says so. One step beyond it: `revealCardsFromHand` (reveal cards of a kind, or any number the player chooses) can come to nothing in a hand that still holds cards, when there is no card of the kind or none is chosen. It used to show "revealed card(s) in hand" with a list of no cards; it now shows no pop-up in that case either, and writes a log line that does not call the hand empty: "<card> revealed no cards from <player>'s hand". Seen on Mallorn-trees 1_54.
**Keep** (what happens if unanswered) or **strike**: the empty list is shown again in that case.
(F-0274, Storm of Argument 3_6 showing its card to the wrong player, is among the findings below.)
Report: engine-pass-17/reveal.md.

**Ketura (2026-10-09):** Keep


### Q-0157 Keep or strike: the engine change behind Q-0143, in PutCardsFromHandOnTopOfDeck

The Palantír of Orthanc 53_67 was rebuilt for Q-0143 (F-0277), and that needed a change in one engine class the queue had not expected, `PutCardsFromHandOnTopOfDeck`. Two parts.
1. It takes an optional `text`, the words put to the player who chooses the cards. The default is the old fixed "Choose cards from hand", so the seven other definitions that use it read as before (`ChooseCardsFromDiscard` and `ChooseCardsFromDrawDeck` already have such a field). Without it the message could not say that none need be chosen.
2. With two or more cards chosen it asks their order on the deck, and it asked that of the player performing the action, not of its own `player`. On 53_67 the old Optional hid this, because it ran in a context whose performer was the Free Peoples player. It now asks its `player`.
No other definition behaves differently; the whole suite passed (9,289 tests). The prompt's wording ("Choose any number of cards from your hand to place on top of your draw deck (you may choose none). Your hand will then be revealed.") you have approved.
**Keep** (what happens if unanswered) or **strike** either part.
Report: engine-pass-17/cards.md.

**Ketura (2026-10-09):** Keep both


### Q-0158 cardInfo: which id an erratum's //id names, and whether to write out the older definitions

Your Q-0145 answer was carried out in engine pass 17 (F-0279), and the item found more than the three cards named. Done already, cardInfo lines only, nothing in play changes (commit 11c305c10, 171 definitions in 80 files): `variantPath: promos/pc` was on 117 errata and is now `errata/pc`; 49 errata of sets 10 to 19 had no `parent` / `variant` / `variantPath` and now have them; 54_192, 57_96 and 54_146 have their own ids. A new check, `cardinfo.py`, lists such departures, and lint raises them as warnings.
Two things it left for you.
1. Which id goes in `//id:` on an erratum of a Decipher card. Q-0145 as I put it to you said the neighbours of 54_192 had `54_x.1`; in fact 146 of the 211 errata of sets 1 to 9 name the ORIGINAL's id there (`4_192.1`). Only the three cards you ruled on were changed; the other 143 are as they were, and the check accepts both forms for now. **A:** the erratum's own id everywhere (`54_192.1`): 143 more definitions change. **B:** the original's id is the convention for those sets: the three are changed back. **C:** leave both forms.
2. The older definition style: 94 other non-site errata have no `side:` field, 43 no `unique:`, and 103 write `type` and the like in lowercase. The game reads them the same. **A:** write them all out in the newer style. **B:** leave them, and write them out only as each card is tested.
Report: engine-pass-17/cardinfo.md.

**Ketura (2026-10-09):** so the intent is that `id` is for the card's unique identifier, and parent is supposed to point to the original (in the case of an errata, absent if an original card). So 1A. 2A is also preferred.


### Q-0159 The Standard's board of Frodo beside a Ring-bearer who is not Frodo: does it stand?

The Card Test Standard, for "the Ring-bearer" in a card's text, has authors lay down a Ring-bearer who is not Frodo with Frodo beside him as a plain companion (Frodo 1_290 cheated in), so that a definition written for Frodo by name is told from one written for the Ring-bearer. The lander of err-b59_30 doubts the board: CR 4.2 "Ring-bearer" says "If a character other than Frodo is your Ring-bearer, you cannot play any version of Frodo with the Ring-bearer keyword during the game", which the game enforces (RingRelatedRule's CantPlayCardsModifier), so no table in play has Frodo beside another Ring-bearer. It is the kind of board your ruling on boards outside play may reach. Smeagol 59_30's test `WhileSmeagolIsTheRingBearerTheMoveTakesAnUnboundCompanionAndNeverFrodo` follows the Standard and was left as it is.
**A:** the board stands: the cheat stands in for a mistake a definition could make, as another culture does for race against culture (F-0019). **B:** it is a board outside play: it comes out of the Standard and the helper table, and the tests that use it lose that candidate, with a line in Notes that Frodo cannot be on the table.

**Ketura (2026-10-09):** Wow I didn't realize this was the case. Yeah it's illegitimate to put both ring bearers in play like that. B for sure. In addition please look for other standards which are twisting the game board state past the point of breaking for that, and in particular try to see if they're jumping at shadows; how many card definitions accidentally said "title(Frodo)" instead of "Ring-bearer"?


### Q-0160 Glimpse of Fate 60_12 (and the original 10_12): how long is "until the regroup phase" when initiative is lost during a regroup phase?

"When you lose initiative, you may make a minion strength –3 until the regroup phase (limit once per turn)." The original 10_12: "Each time you lose initiative, make a minion strength -4 until the regroup phase."

Initiative can be lost inside a regroup phase: Galadriel, Lady Redeemed 60_11's own "Regroup: Discard an [elven] event from hand to hinder ..." takes a hand from 4 cards to 3, and so does Ranger of Osgiliath 7_116's discard of 2. Neither rules document says what "until the regroup phase" means for an effect that begins in one; the rulings on those words (Eregion's Trails, Hospitality of the Dwarves, Harrowdale) are all on effects made before it.

**Built now:** `until: start(regroup)` (10_12 has `until: regroup`, which is the same): the minion is weakened until the next regroup phase begins. If the fellowship moves again, that is the whole of the second move (Shadow, maneuver, archery, assignment and skirmish phases); if it stays, the minion is discarded with the rest.

**A (as built):** "the regroup phase" is the next one to begin, so a use in the regroup phase weakens a minion for the second move. Nothing changes. The active test `LostInTheRegroupPhaseTheMinionStaysWeakenedThroughAnotherMoveUntilTheNextRegroupPhase` reads it: Goblin Patrol Troop 13, 10 at once, still 10 in the maneuver phase at site 3, 13 at the next regroup phase.
**B:** the regroup phase has already come, so the effect ends as it is made and does nothing. On 60_12 the offer would then be withheld in a regroup phase (Q-0034: no cost, nothing done) and the turn's limit kept; both definitions would get a requirement on the phase, and that test would read no offer and 13 throughout.

No test is parked: the game passes A and the card had no old test. The answer would hold for any other "until the regroup phase" that can begin in a regroup phase.

Recommendation: A. Every other "until the <phase>" is built to the next start of that phase, and B would make the card do nothing in the one phase where its own culture's Galadriel 60_11 loses initiative.

**Ketura (2026-10-09):** hmm, "until X phase" is definitely "until the start of X phase", but it's definitely a bit muddy when you're doing that *in* X phase. The One Ring I believe takes itself off at the end of regroup if it is put on during regroup, which establishes at least a little precedence. Let's just leave it at A for now.


### Q-0161 Keep or strike: Breaching Shot 102_21 does not reach a [rohan] ally at Streets of Edoras (Q-0150 read onto Q-0042)

Q-0150 is built (F-0281): at Streets of Edoras a [rohan] ally takes no part in archery fire or skirmishes, whatever card would have him take part. One card it reaches was not named in the question, and you ruled on that card before, so it is put to you by itself.
Breaching Shot 102_21: "Remove X tokens from machines to assign an Uruk-hai costing X or less to an ally or unbound companion." By Q-0042 its "an ally" reaches an ally away from home ("they are breaching the wall"). It is built with `ignoreAllyLocation`, which in the engine skipped the whole question of whether an ally may fight, the Streets' bar included: at Streets of Edoras it could assign an Uruk-hai to a [rohan] ally. The agent read Q-0042 as this card granting the "may" and Q-0150 as the bar beating every grant ("whatever card"), so the engine now reads the bar first: at the Streets Breaching Shot offers an ally away from home and unbound companions, and not a [rohan] ally. Away from the Streets nothing changes, and no other card uses `ignoreAllyLocation`.
The card is back to ready with one new test, `AnAllyACardBarsFromSkirmishesIsNotOfferedThoughAnAllyAwayFromHomeIs` (Aldor at his home, barred, is not offered; Grimir, away from home, is, and the Uruk Savage is assigned to him).
**Keep** (what happens if unanswered): the bar beats Breaching Shot as it beats One Whom Men Would Follow and the Dunlendings. **Strike**: Breaching Shot breaches the Streets too. That moves one line back in `Filters.assignableToSkirmish` (the `allowAllyToSkirmish` test above the bar), and takes that test and its `R3` clause out of 102_21; the rest of F-0281 stands.
Report: engine-pass-18/participate.md.

**Ketura (2026-10-09):** keep; can't > can, so Streets > Shot


### Q-0162 Keep or strike: the engine change behind F-0278 (Gwaihir): a choice from a discard pile is asked in a definition's own words, and the log names who chose

Gwaihir 101_15 is rebuilt for F-0278 as you said: the yes/no is gone and the Shadow player is asked the one choice of 0 to 2 cards. It reads: "Choose up to 2 Shadow cards from your discard pile to take into hand (you may choose none)." The wording is the agent's, on the pattern you passed for the Palantír. With no Shadow card in that discard pile the Shadow player is now asked nothing, where the yes/no used to be asked for nothing.
That took an engine change the queue had not expected, in four classes (F-0285, filed `fixed`). A change already made stays unless you strike it. Three parts.
1. `putCardsFromDiscardIntoHand` takes an optional `text`. Only Gwaihir uses it.
2. The text did not show. Every choice from a discard pile has always been asked as "Choose card from discard": `ChooseCardsFromDiscardEffect` sent those fixed words whatever it was given, so the `text` you gave `chooseCardsFromDiscard` in 2022 was never shown either. A definition's own `text` is now what the player is asked, on both words. Three other cards therefore read differently, in the words their definitions already had: Hugin 2_24 "Choose card to reveal and put beneath your deck"; Greatest Kingdom of My People 51_16 "Choose a Dwarven card from your discard pile to take into hand" (its count is 0-1 and these words do not say none may be chosen); Desperate Measures 51_245 "Choose a [Sauron] card to take into hand from your discard pile" (the "[Sauron]" shows as typed, with no icon). Their definitions were not touched. Every definition that gives no text is asked "Choose card from discard" as before.
3. The log line "<player> chooses <cards>" for such a choice named the player performing the action, not the player who chose. With Gwaihir's yes/no gone it would have said the Free Peoples player chose the Shadow player's cards. It now names the chooser, which also puts the line right on Stern People 7_251, Safe Paths 6_44 and Now for Ruin 102_52.
Shown by the mechanic test `ChoiceFromADiscardPileTests` (Hugin, Stern People) and Gwaihir's own tests.
**Keep** (what happens if unanswered): all three stay. **Strike 2 for the three older cards only**: one line back in `ChooseCardsFromDiscard.java` and the Hugin test out; they read "Choose card from discard" again. **Strike 1 and 2 altogether**: Gwaihir's choice reads "Choose card from discard", which does not say none may be chosen, so Q-0143 goes unmet on him or the yes/no comes back. **Strike 3**: one line back in `CardResolver`; the log names the wrong player on those four cards.
**Not done, yours to say:** the other eleven discard-pile effects carry words in the code that are likewise never shown ("Choose cards to shuffle in", "Choose card to transfer", "Choose cards to stack", "Choose card to play from discard" and the like). Say "all" and every choice from a discard pile is asked in its own words: some 370 uses in the definitions change their prompt, and the 21 lines of four older test files that read "Choose card from discard" (1_363, 17_15, 67_15, 102_52) are put right with it.
Report: engine-pass-18/cards.md.

**Ketura (2026-10-09):** definitely keep, excellent work. Not every card needs to add its own wording I think, many of them really are just fine as "choose a card from discard". But if there's any real twist to it I think it's fine to improve the messaging to have a custom entry.


### Q-0163 A reveal that a cost buys, with an empty draw deck: War-beacon 103_35 and His Cruelty and Malice 53_91 (after Q-0148)

Q-0148 is built (F-0284): Glorfindel's ability is not offered with an empty draw deck. You asked for the same shape in other landed cards. None has it as plainly as he does: Forearmed 56_16 already has its reveal as the cost (F-0249), and Counsel of the Wise 101_13 searches the deck. Two have a reveal that a cost buys, with what follows reading the cards revealed:
War-beacon 103_35: "Fellowship: Exert a Man and hinder a beacon to reveal the top 4 cards of your draw deck. You may take any [gondor] or [rohan] cards revealed into hand. Replace the rest in any order."
His Cruelty and Malice 53_91: "Regroup: Discard a [sauron] minion to reveal the top 2 cards of your opponent's draw deck. If at least 1 is a Free Peoples card, exert a companion (except the Ring-bearer). Your opponent may discard all revealed Free Peoples cards to prevent this."
**Built now** (probed, neither changed): with that draw deck empty each is offered, and not flagged no-effect; the cost is paid and nothing happens.
**A:** as Q-0148, the reveal must be able to happen: each is withheld while that deck is empty (a requirement of one card in it; a deck shorter than N still reveals what is there). Two definitions change.
**B:** as your rule for the no-effect flag has it for an action with a cost that would do nothing: offered, and flagged. That is engine work on what the reveal forecasts, and reaches every costed reveal from a draw deck.
**C:** leave them; an empty deck is rare.
The answer is also the rule for the Decipher cards of this shape when their turn comes (The Dark Lord's Summons 1_242, Into the West 7_23, Might of the Elf-lords 11_24 and some twenty more). Not asked here: Rúmil 12_22 ("Maneuver: Reveal the top card of your draw deck. If it is an [elven] card, ...") is Glorfindel's shape exactly and gets his requirement under Q-0148 when it is tested; the "you may reveal the top card" triggers (Celeborn 10_7, Elrond 10_9, Legolas 13_18) are for their own authors to ask.
No test is parked. Recommendation: B is what your flag ruling already says for a costed action; A is the cheaper to build and reads Q-0148 one step further than you spoke.
Report: engine-pass-18/cards.md.

**Ketura (2026-10-09):** b is fine


### Q-0164 Under Foot 60_52 (and Flaming Brand 52_32): is "At the start of the regroup phase, discard this condition" the rest of the ability, or a discard every turn?

Under Foot 60_52: "**Shadow:** If you have initiative, spot a [raider] Man to reconcile your hand (limit once per phase). At the start of the regroup phase, discard this condition." The Skirmish ability follows on a new line: "**Skirmish:** Discard this condition to make a [raider] Man strength +2." The original 10_52 had the discard as the reconcile's cost: "spot a [raider] Man and discard this condition to reconcile your hand."

**Built now:** the discard belongs to the Shadow ability. Using it reconciles the hand and sets up a discard of the condition at the start of that turn's regroup phase; in a turn in which it is not used, Under Foot stays in play. Flaming Brand 52_32, landed earlier, is built the same way and was read the same way without being asked: "Skirmish: Make bearer strength +2 and <b>damage +1</b> while bearing this item and skirmishing a Nazgul until the regroup phase (limit once per turn). Discard this possession at the start of the regroup phase." A brand that is never used stays on its bearer. Neither rules document speaks of either sentence. Decipher used the same words for a discard that happens every turn, after a modifier: Grieving the Fallen 15_81.

**A (as built):** the sentence is the rest of the ability's effect. Under Foot may wait in the support area turn after turn until the Shadow player has initiative, and goes at the regroup phase of the turn it reconciles. Nothing changes. Two active tests assert it: `NotUsedInTheShadowPhaseItStaysInPlayThroughTheRegroupPhase`, and `UsedWithNoHandAndNoDeckItReconcilesNothingAndIsStillDiscardedAtTheStartOfTheRegroupPhase` (the ability is offered with no hand and no draw deck, because using it still costs the condition).

**B:** the sentence is a trigger of its own. Under Foot is discarded at the start of every regroup phase, used or not, so it never outlives the turn it is played in. The definition changes (the reconcile alone in the ability, and a required trigger beside it). The first test above then reads the opposite, and the second becomes "not offered": the reconcile would be the ability's whole effect, and a spot is no cost (Q-0034). Flaming Brand becomes a one-turn weapon in the same way, and the unused brand in its test `SkirmishActionMakesBearerStrongerAndDamagePlus1AgainstANazgulUntilRegroupThenDiscardsTheBrand` is discarded.

No test is parked: the game does A and the tests of A are active. Recommendation: A. Both definitions were written with the discard inside the ability, and on Under Foot the sentence stands where the original's cost stood.

**Ketura (2026-10-09):** This is A. On standard templating, phase actions go last on a card, with ongoing or each time triggers above them. If you see such text at the end after a phase action, it's tied to the prior phase action. In the Flaming Brand errata's case, you can think of it as "lighting" the brand, which then "burns out" later. These burn-out clauses keep the effects from being on events without the clog issues of events.


### Q-0165 Mordor Fiend 60_91: is "you may add (3)" meant to be added without asking, as Goblin Runner's is?

Mordor Fiend 60_91: "When you play this minion, if you cannot spot another minion and there are 3 or fewer twilight tokens, you may add (3) (or (5) if this minion is roaming)." The original 10_91 was a Shadow-phase ability with the same conditions and amounts ("**Shadow:** If you cannot spot another minion and there are 3 or fewer twilight tokens, add (3) ..."), which its player used or left alone; the erratum made it a trigger on the play and wrote "you may".

**Built now:** a required trigger (`optional: false`). When the conditions hold the twilight is added and the Shadow player is asked nothing. Goblin Runner 1_178 ("When you play this minion, you may add (2).") is built the same way, and you ruled that one deliberate (F-0009: "Rule this kind of thing a quality-of-life implementation, not a bug"). The other two cards of the wording are built to ask: Goblin Flankers 2_61 ("When you play this minion, you may add (3) if the fellowship has moved more than once this turn.") and Isengard Scimitar 5_54 ("When you play this weapon, you may add (1) for each site you control."). Since F-0009 every other unasked "you may" put to you has come back as an oversight (Q-0021, Q-0032, Q-0037, Q-0106), which is why this is asked and not assumed.

**A (as built):** the Goblin Runner shortcut reaches this card: a lone Fiend played into a small pool adds its twilight with no click. Nothing changes. The two tests in which it fires assert that nothing is offered and the pool already holds the tokens (`ARoamingFiendWithThreeTokensLeftAddsFiveAndASecondFiendPlayedBesideItAddsNothing`, `AtSite6TheFiendIsNotRoamingAndAddsThree`).

**B (the text, as Q-0021):** the Shadow player is asked each time the conditions hold and may decline. One word in the definition (`optional: true`); those two tests each gain an accept, one test of declining is added (the pool stays at 3), and the clause's ledger line takes the O check.

No test is parked: the game does A and the tests of A are active. Recommendation: A. It is Goblin Runner's sentence with a condition in front of it, and a Shadow player has next to no reason to turn the twilight down. If A, say whether Goblin Flankers and Isengard Scimitar should be left asking as they are (they match their text, so their authors will otherwise assert the offer and raise nothing).

**Ketura (2026-10-09):** ah piss, we missed that. We'll alter the text, make it required in the text and alter the definition to match.


### Q-0166 Keep or strike: an "add a burden" cost that cannot be paid is shown with a hindered Ring-bearer, not with Test Seal 0_8004 (the tests' own card, in three landed files)

From engine pass 18, item "tools" (a line the lander of err-b59_30 passed up: "say which board shows an unpayable 'add a burden' cost"). Nothing was changed in the game or in any test; this is about what the Card Test Standard tells authors, and it stands unless you strike it.

**The gap.** A card whose cost is a burden ("To play, add a burden", "add 2 burdens to discard each minion") owes a test that with the cost unpayable it is not offered. No printed card stops a burden being added. Three landed files show it with Test Seal 0_8004, a card of the tests' own ("Burdens cannot be added.", written for mechanics/CantAddBurdensTests), against the pitfalls' line "a card's own tests use real cards":

- Get On and Get Away 54_304: CannotBePlayedWhileBurdensCannotBeAdded
- Sméagol, Bearer of Great Secrets 59_30: CannotBePlayedWhileBurdensCannotBeAdded and RegroupActionIsNotOfferedWhileBurdensCannotBeAdded
- Band of the Eye 51_240: WhenBurdensCannotBeAddedTheFreePeoplesPlayerIsNotAskedToPrevent

**A board in play exists.** While the Ring-bearer is hindered no burden can be added (CR 5.0 "hinder"; the rules code puts on the very modifier Test Seal carries), and the rig hinders a card directly (`HinderCard`). Sam 103_115's test does it that way, and a probe shows it stops Sméagol 59_30 being played. It cannot serve one of the four tests: Sméagol's regroup ability needs Sméagol himself as the Ring-bearer, and hindered his text is off, so no board the game can reach has that cost unpayable.

**What the Standard now says** (test-generation, under "anything before 'to' is a cost"; one line in the rig's pitfalls): the board for an unpayable "add a burden" is a hindered Ring-bearer; where the card must itself be the Ring-bearer to pay, the check is waived with a line in the ledger (your ruling of 2026-10-08 on boards outside play); Test Seal is the mechanics' card. It names the three files as standing until you answer here.

- **A. Keep.** The three files are brought into line the next time a fixer has them: three tests re-laid with a hindered Ring-bearer, and 59_30's RegroupActionIsNotOfferedWhileBurdensCannotBeAdded dropped for a line in its Notes.
- **B. Strike: Test Seal may stand in a card's own tests wherever burdens must be unaddable.** The three files stay as they are; the sentence in the Standard is turned round to say so, and "a card's own tests use real cards" gets this one exception.
- **C. A hindered Ring-bearer where one works, Test Seal only where the card is itself the Ring-bearer.** 59_30's regroup test stays as it is; the other three tests are re-laid.

**Ketura (2026-10-09):** A, yeah. Always prefer using real mechanics or cards; if we have to invent cards we need to scrutize whether or not we have gotten lost in the weeds.


### F-0283 Dunlending Warrior 4_18 and Dunlending Ravager 4_15: is the assignment ability withheld when it can assign the minion to nobody?

Both cards: "**Assignment:** Spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that ally." Built the same on both: it requires `canSpot ally`, has the player choose any ally, gives him "participates in skirmishes", then assigns this minion to him. It has no cost.
Since Q-0150 was built (F-0281, engine pass 18) the ability does nothing to a [rohan] ally at Streets of Edoras: the site's bar wins and nobody is assigned. But it is still offered there when such an ally is the only ally in play. He can be chosen, nothing happens, and the ability is on offer again at the Shadow player's next action, as often as he likes (seen in a probe on the committed engine, for both cards). The same happens, and did before this pass, when the only ally is already assigned to another minion, and when the Dunlending is himself already assigned.
By Q-0034 an optional action with no cost whose whole effect would do nothing is not offered, and a spot is no cost (the settled "Spot X to <effect>" shape), so as the rulings stand this is a fault in both definitions. It was not fixed in passing because the fix is more than a line, neither card has campaign tests yet, and you have not yet said whether you want the like done to Come Down (Q-0151). Nothing is parked on it.
**A:** withhold it whenever it can assign this minion to nobody: no ally who is unassigned and not barred from skirmishes, or this minion already assigned; and offer only such allies for the choice. Two definitions change (the requirement and the choice become "an ally this minion can be assigned to"), which needs either a filter word `barredFromSkirmishes`, the twin of pass 17's `barredFromArcheryFire`, or a flag that lets the existing `assignableToSkirmishAgainst` word pass over where an ally is. The mechanic test `AMinionWhoseOwnTextAssignsItToAnAllyIsNotAssignedToOneBarredFromSkirmishes` would then read that Aldor is not offered, in place of "chosen and not assigned".
**B:** withhold it only for the bar (an ally a card says may not skirmish is not "an ally" this text can do anything to), and leave the already-assigned cases as they are.
**C:** leave it: "spot an ally" is met by any ally, and the ability may be used for nothing. F-0283 closes as not a bug, and the set 4 author asserts the offer.
Recommendation: A. It is what Q-0034 says, and a no-cost action that can be clicked for nothing again and again is the griefing angle you named on Q-0015.
Report: engine-pass-18/participate.md.

**Ketura (2026-10-09):** A


### F-0292 Galadriel's Silver Ewer 68_11 (and every "each time you play a ... skirmish event"): is Sword-wall, printed "Skirmish or Regroup", a skirmish event when it is played in the regroup phase?

Galadriel's Silver Ewer 68_11: "Each time you play an [elven] skirmish event, you may exert Galadriel to reinforce an [elven] token." (The original 18_11 has the same trigger without the exertion.) Sword-wall 4_84 is the one [elven] event printed with two phases: "**Skirmish** *or* **Regroup:** Spot 3 Elf companions to liberate a site or exert a minion." CR 4.2, "phase action": "If a phase action can be played in multiple phases (for example, 'Maneuver or Skirmish:'), its action type is of the phase during which the action is taken."

**Built now:** the trigger's filter is `your,culture(elven),timeword(skirmish),event`, and `timeword(skirmish)` is true of every event that lists Skirmish among its phases, whatever phase it is played in. So Sword-wall played in the REGROUP phase brings the Ewer's offer; taken, Galadriel exerts and a token is added. Every trigger on the play of an event of a named phase is built this way, thirteen in all: Strength of Kings 1_115 and 51_115, Orc Ambusher 1_261 ("a Free Peoples regroup event"), Bow of the Galadhrim 7_18, Hope Comes 7_22, Aragorn 11_53, Gríma's Dagger 17_44, the Ewer 18_11 and 68_11, Time for Food 18_36, 30_27 (fellowship events), Boromir's Cloak 51_98, I Would Have Gone With You to the End 101_23. So are the two cost modifiers 3_45 (maneuver events) and 12_95 (your skirmish events) and 93_4's "played a skirmish event this phase". The events printed with Skirmish and another phase: 1_315, 4_84, 4_110, 4_234, 5_30, 5_96, 17_32, 103_6, 103_15, 103_16, 103_36, 103_69; with Regroup and another: 1_110, 2_26, 3_22, 4_84, 4_129, 8_29, 11_49, 11_66, 12_34, 12_75.

**A (the rule):** an event printed with several phases is an event of the phase it is played in, and of no other. Sword-wall played in the regroup phase is a regroup event: no offer from the Ewer, Bow of the Galadhrim or Hope Comes. Stout and Sturdy 1_315 played in the maneuver phase is no skirmish event for Strength of Kings or Aragorn 11_53. The other way about, Orc Ambusher could no longer cancel Sword-wall played in a skirmish, or Traveled Leader 12_34 played in the maneuver phase, as it can today. It is best done once in the engine (an event that lists more than one phase is read, while it is being played, by the phase it is played in), with one care: For the Mark 12_114 lists Maneuver only as the way its off-phase play was built (F-0003) and must stay a regroup event when it is played in the maneuver phase. For the Ewer and 18_11 alone, a `requires: { type: phase, phase: skirmish }` on the trigger does it, and can go in at the next landing if you would sooner have it card by card. The parked test goes live. A card that is not being played (lying in a discard pile or a hand: "take an [elven] skirmish event into hand", Gil-galad 68_12 and the cards like it) would still be read by its printed words, so Gil-galad takes Sword-wall under either reading; say so if you want that otherwise.

**B (the printed words):** an event printed "Skirmish or ..." is a skirmish event wherever it is played. Nothing changes in the game. The parked test is turned round to assert the offer in the regroup phase, F-0292 closes as not a bug, and the shape is written down so that no later card raises it.

Parked on it: `SwordWallPlayedInTheRegroupPhaseIsNoSkirmishEventAndBringsNoOffer` in the 68_11 tests (fails as built, at the offer; passes with the `requires`, with every other test). `SwordWallPlayedInASkirmishIsAnElvenSkirmishEvent` is active and holds under both readings. Recommendation: A, because the Comprehensive Rules say it in so many words; it reaches few boards (Sword-wall is the only [elven] event of the kind), so B costs little if the printed-words reading is how you have always played it.

**Ketura (2026-10-09):** Definitely a bug, go ahead and fix.


### F-0302 Doorway to Doom 68_134: the erratum's arrow points left, Decipher's points right

Doorway to Doom 68_134: "<b>Mountain.</b> When the fellowship moves to this site, each player wounds 2 of their characters.  This site cannot be replaced." The question is not about these words but about the arrow under them, which no sheet has a column for.

**Built now:** `direction: Left`, which is what the erratum's released image shows (errata/LOTR-EN18E134.1_card.jpg, dated 2023.01.11: the arrowhead is at the left, the ring at the right). Decipher's card 18U134 has the arrow pointing right (decipher/LOTR18134.jpg; the original's definition has `direction: Right`, and so did its Java class before the 2024 migration). The erratum's row in the master sheet changes the game text and nothing else. It is the only errata site whose direction differs from its original's: the other eleven agree, and the images of Mithlond 68_136 (left) and Steward's Tomb 68_139 (right), made in the same batch of errata, match Decipher's. The arrow on the site the fellowship leaves says whether the player to its left or its right lays the next site, so in a two-player game it changes nothing.

**A (the arrow was turned by mistake when the erratum's image was made):** the definition goes back to `direction: Right`, the stats test `DoorwayToDoomStatsAndKeywordsAreCorrect` reads RIGHT, and the erratum's image wants a new render with the arrow as Decipher printed it.

**B (Left is meant, or not worth another image):** nothing changes. The definition and the erratum's image agree with each other as they stand, and the stats test reads LEFT.

Nothing is parked: the stats test asserts the definition's value and passes. Nothing was changed at landing, because the definition matches the card players are shown. Recommendation: A, as a mistake in the image that the definition copied (or the other way round); but the image is yours to make, so the definition was left alone until it changes.

**Ketura (2026-10-09):** heh, go ahead and fix. You're right that the arrow never matters in normal gameplay, but that's no excuse for us to be sloppy on this. Eventually we do want to support multiplayer.


### F-0308 Ulaire Nertea, Dark Horseman 69_38: when a card has the Free Peoples player assign her to the named race (Éowyn 17_96, Shadowy Mount 12_171), is a burden owed or is the assignment barred?

Ulaire Nertea, Dark Horseman 69_38: "When you play Úlairë Nertëa, name a race. The Free Peoples player must add a burden to assign Úlairë Nertëa to skirmish a companion of the named race." The original 19_38 ends "The Free Peoples player cannot assign Úlairë Nertëa to skirmish a companion of the named race."

**Built now:** the burden is asked for at one moment only, when the Free Peoples player starts his own assigning, after the assignment actions are passed. Before that moment nothing stands between her and the named race, so an assignment that a card has him make during the assignment actions costs nothing. Two boards, Hobbit named, burdens 0 before and 0 after: Éowyn, Northwoman 17_96 ("Assignment: Exert Éowyn and assign a minion to the Ring-bearer ...") puts her on Frodo; Shadowy Mount 12_171 borne by her ("Assignment: Make the Free Peoples player assign bearer to an unbound companion bearing a [wraith] condition") puts her on Pippin. The original's bar is a standing one and stops both: with 19_38 in play Éowyn's action cannot take her and the Mount assigns nobody. So the erratum gives a free pass where the original gave none. It reads as an oversight of the trigger hand-made for this card in 2021 (`fpstartedassigning`, which no other card uses). The free pass is wrong under either reading; the doubt is which way it is closed.

**A (the words: a burden whenever he assigns her to that race):** he may assign her by a card, and adds a burden as he does. Éowyn's action takes her onto Frodo and the burdens go 0 to 1; where no burden can be added (the Ring-bearer hindered) she cannot be chosen. No definition can do this: the engine's assignment effect would have to offer and collect an assignment cost whenever the Free Peoples player is the one assigning. The two parked tests assert this reading as they stand (if the engine asks before it collects, each wants a `FreepsChooseYes()`). If A, say also whether the Mount's forced assignment owes the burden (the parked test says it does: he is the one assigning), or whether an assignment he is made to make and does not pay for is simply not made.

**B (the original's bar, with a price that lifts it):** the bar stands from the moment she is played, through the assignment actions, and the burden can be paid only at his own assigning, where it lifts the bar for that assignment phase. Éowyn's action then takes another minion and not her, and the Mount assigns nobody, as with 19_38. This is how the engine's `assignmentCost` already treats "The Free Peoples player must exert a companion to assign this minion" on the three set-7 [wraith] Orcs (7_197, Morgul Spawn 7_200 and 7_201): a standing bar on the Free Peoples player until the cost is paid. It wants either `assignmentCost` to take a filter for which companions (engine), or a standing bar in the definition that the yes lifts (not tried). The two parked tests are turned round: Éowyn's action offers Úlairë Nelya and not her, the Mount's action assigns nobody, burdens 0 throughout.

Parked on it: `TheFreePeoplesPlayerAssigningHerByHisOwnAssignmentActionAddsTheBurden` and `TheFreePeoplesPlayerMadeToAssignHerByAShadowCardAddsTheBurden` in the 69_38 tests (both fail as built at the burden count only: expected 1, was 0). Recommendation: B. It is the original with a price on it, it is what the engine does for every other "must ... to assign", and it needs nothing new in the assignment effect. A is nearer the words, and worth the engine work only if you want a burden to be able to buy Éowyn's action.

**Ketura (2026-10-09):** Humm. I think so long as the freeps player is the one doing the assignment, it should add a burden automatically.


### F-0316 Race Text 92_29: is the cost in an event's own text one of "all costs"?

Race Text 92_29: "Shadow: Open and reveal a sealed booster pack (limit once per turn). You may play one shadow card revealed, ignoring all costs."

**Built now:** the card chosen is played with nothing taken from the pool (its twilight cost, roaming), with no "To play, ..." cost paid (Alive and Unspoiled 1_120, "To play, exert an Uruk-hai") and no requirement met (Cave Troll of Moria 1_165, "To play, spot a [moria] Orc"; Hate and Anger 3_53, "Shadow: Spot an Uruk-hai and a [moria] minion to draw 3 cards", draws 3 with no minion on the table). The old tests `IgnoresTwilightCost`, `IgnoresExtraCosts` and `IgnoresToPlayRequirements` assert the first three, and F-0193 left card types other than events "as now". Uniqueness, a card that says cards cannot be played, and an event's phase (F-0193) still hold. The one thing still charged is the cost in an event's own text: Threat of the Unknown 1_197 ("Shadow: Exert your [moria] Orc to draw 2 cards") exerts a Moria Scout and then draws 2, and with no [moria] Orc on the table it can still be chosen and is discarded with nothing drawn. (Probed at landing. F-0193's outcome left this "open for the RTMD chunk".)

**A (the text): the cost in an event's own text is one of "all costs".** Threat of the Unknown draws 2 with no Orc exerted, and with no [moria] Orc on the table. The engine's play of an event from the pack passes over the event's costs as it now passes over its twilight cost (`PlayUtils.getPlayCardActionIgnoringCosts`). One thing goes with it: where an event's effect is done to what paid the cost ("Exert a minion to make it fierce"), the choice would still be made and only the payment left out.

**B (as built): "all costs" are the costs of playing the card (twilight, roaming, "To play ..."), and an event is then carried out as written, its own cost paid.** Nothing changes for an event whose cost can be paid. An event whose own cost cannot be paid would be shown and not choosable, as any card that cannot be played is; today it is chosen for nothing.

**Parked on this:** no test, since none can be written yet: the rig's test pack holds one Shadow-phase event, Foul Things 2_59, which has no cost of its own. G-0038 asks for a pack of named cards, and the card is parked on that. Either answer gets its test then (A: the Scout unexerted and 2 drawn; B: the Scout exerted, and with no Orc the event shown and not choosable).

Recommendation: A. The game already waives everything else such an event asks, its "Spot ... to" requirement included, so the cost before "to" is the odd one out, and it is what CR 4.2 most plainly calls a cost.

**Ketura (2026-10-09):** well-reasoned, I approve fixing this.


### Q-0178 Keep or strike: Race to Mount Doom cards get every missing check of the Standard, not only form (after the first batch)

You asked for Race to Mount Doom to be light: "These tests were done relatively recently, and so less effort should be required. I think that formatting and such may still be required, but these ... sets should overall require less attention."

**What the first batch did (rtmd-b91_1, four cards).** The notes I wrote tell the agents: put the existing file in form, write the ledger, and add a test only where a check of the Standard has no test at all. On 91_1 that meant 6 tests before and 6 after, four of them put in form. On the other three the old files had 3 tests each and came out with 9, 7 and 8, because the Standard's checks for a "you may" trigger (the owner on the other side, the decline, the next turn, the board where it does nothing, the rule it meets) had nothing. One of those added tests, the decline, found F-0311: on 91_2, 91_3 and 93_3 the other player was offered the owner's trigger when the owner declined. The batch cost about three quarters of what an errata card costs, per card; not a third.

**Keep**: the remaining 90 cards go on this way: form, ledger, and every check of the Standard that no existing test touches. About 23 more batches of this size.

**Strike**: form and ledger only. The existing tests are put in form and listed in the ledger; a check with no test is written down in the ledger as a gap and no test is added for it, unless what is missing is the card's main effect. Much cheaper, and bugs of F-0311's kind would not be found in this phase.

Until you answer, batches go on as in Keep.

**Ketura (2026-10-09):** Keep. "I suppose it can't be helped if they weren't up to the standard. Let em get re-worked I suppose." The Race to Mount Doom cards get every check of the Standard that no existing test touches, as the first batches did.


### Q-0150 Streets of Edoras 4_340 against cards that have an ally take part: which wins?

Streets of Edoras 4_340: "Each [rohan] ally may not participate in archery fire or skirmishes at Streets of
Edoras." Where another card has such an ally take part (One Whom Men Would Follow 1_109, for archery fire and
skirmishes; Dunlending Ravager 4_15 and Dunlending Warrior 4_18, which assign to him), the game lets the allowing
card win: he adds to the archery total, takes ordinary archery wounds, and skirmishes. Your answer to Q-0147
("can't > can") says the bar should win. Engine pass 17 made the bar hold for archery wounds sent by Come Down
(F-0275) and left the rest alone, because it reaches skirmishes and assignment; what it looked at is in
`orchestrator/engine-pass-17/streets.md`.
**A:** the bar wins everywhere: a [rohan] ally at the Streets neither adds to archery, nor takes its wounds, nor
is assigned or skirmishes, whatever card would have him do so. An engine item. **B:** as built: a card that names
allies taking part overrides the site. Recommended: A, by Q-0011 and Q-0147.

**Ketura (2026-10-08, night):** A. "Yeah pretty much everywhere that the "ally can participate" modifier is called should be confirmed to be going through Modifiers Querying, and then the appropriate functions can be set up to check for the "ally can't participate" modifier and defer to it."

### Q-0149 Unordered card collections in other effects: pile order and shuffles still vary from run to run

Engine pass 17 put "each / every" effects in card id order (F-0268, your Q-0142 B). Its agent found the same
unordered collections (hash sets) in about a dozen more effect classes that it left alone as too many to change
unasked: KillEffect, the four ShuffleCards...IntoDeck effects, RemoveCardsFromTheGame, DiscardCardsFromHand and
others, listed in `orchestrator/engine-pass-17/order.md`. There the order cards land on a pile, and the order
cards go into a shuffle, still vary from one run of the same game to the next. The second matters beyond tests:
a shuffle fed its cards in a different order gives a different deck from the same random numbers, so a game
replayed from its seed need not come out the same.
**A:** make them all ordered by card id, as one engine item with a mechanic test for each kind. **B:** leave them
until a card's test or the scenario system trips on one. Recommended: A.
Also for you to strike if unwanted, both already committed in 563869b5b: cards a player chose together are taken in
id order whichever he named first (ChooseActiveCardsEffect), and "discard each" lays cards on the discard pile in
that order.

**Ketura (2026-10-08, night):** B. "This is getting a bit into the weeds. If we have a specific reason, a test that won't work due to the stochastic ordering, we can address those as we go. But preemptively hitting it is unproductive I think."

### Q-0148 Glorfindel, Revealed in Wrath 59_16 (and 9_16): is the ability offered with an EMPTY draw deck?

"Skirmish: Reveal the top card of your draw deck. You may exert Glorfindel to make a Nazgûl he is skirmishing
strength –X, where X is the twilight cost of the card revealed." The reveal stands in a sentence of its own with
no "to", so it is an effect and the ability has no cost; the exertion is a price inside the effect. As built (59_16
and the original 9_16 alike), with an EMPTY draw deck the ability is offered and flagged as doing something; used,
it shows nobody anything and then asks "Would you like to exert Glorfindel ...", and an exertion paid buys nothing.
**A:** withheld with an empty deck, as a costless ability whose effect would do nothing (Q-0034; F-0211, Beneath
the Mountains' discard from an empty deck): `requires: {type: HasCardInDrawDeck}` on the ability in both
definitions, and the parked test goes live. **B:** offered, as the optional look and the optional draw stay on
offer with an empty deck (Q-0104, Q-0029), and the exertion can still be paid for its own sake (Q-0034's exception
for a cost): nothing changes in the game, and the parked test is dropped.
Parked on it: IsNotOfferedWithAnEmptyDrawDeck, which asserts A. Recommended: B: the reveal is an effect, like the
look of Q-0104, and with a card on the deck but no Nazgûl in his skirmish the game already offers the exertion for
nothing (an active test reads that).

**Ketura (2026-10-08, night):** A. "I would consider this either a requirement or a cost. Either way, it has to happen for the rest to occur, so an empty deck should disallow the ability."

### Q-0147 Streets of Edoras 4_340 with Come Down: does an ally barred from archery fire still take its wounds?

Streets of Edoras 4_340: "Each [rohan] ally may not participate in archery fire or skirmishes at Streets of
Edoras." Come Down 4_146 / 54_146 has allies take archery wounds in place of companions. With both in force the
game has a [rohan] ally still take archery wounds at the Streets, before engine pass 16 and after it. Your answer
to Q-0137 points the same way ("Come Down doesn't say 'participate in archery fire', it just says 'take wounds from
archery fire'"), so this is asked only to have it on the record.
**A:** as built: barred from participating, he still takes the wounds Come Down sends to allies. **B:** "may not
participate" also keeps the wounds off him, and they go to the next ally or to companions. Nothing is parked and
no test reads it yet; found by the archery item of engine pass 16.

**Ketura (2026-10-08, night):** B. "Ha! Well, if we follow can't > can, then Streets should I think disallow Come Down's effect from affecting [Rohan] allies. My intent with the ruling on Come Down was that "participation" is clearly made of a few sub-parts, such as "adding to archery totals" and "taking archery wounds when assigned", and Come Down is performing the second but not the first. However disallowing "participation" should disallow both sub-parts here, meaning Come Down is ineffectual."

### Q-0146 Bill Ferny, Swarthy Sneering Fellow 52_75: listed for assignment and then refused

"While you can spot a Hobbit, the Free Peoples player may not assign a character to skirmish Bill Ferny." The
erratum builds this as a restriction on each pair (`cantBeAssignedToSkirmishAgainst`), which the game checks only
when the assignment is answered: Bill stays in the list of minions to assign against, and choosing him is refused
and the decision sent again. The Decipher original 2_75 uses `CantBeAssignedToSkirmish` for the Free Peoples player,
which takes Bill off his list. The players end in the same place.
**A:** build the erratum as the original is, so that Bill is not offered: a definition change, and the test then
reads that he is not on the list, with no new rig method. **B:** keep it; the test reads the refusal once G-0029
(`FreepsCanAssignAgainst`) is built. Recommended: A. The card is parked on G-0029 meanwhile; ten other definitions
use the per-pair restriction, so G-0029 has a use either way.

**Ketura (2026-10-08, night):** "A sounds good." The erratum is to be built as the original is, so that Bill is not offered to the Free Peoples player.

### Q-0145 Uruk Regular 54_192 and 57_96: are they meant to be promos?

Their definitions have `//id: 4_192.1` / `//id: 7_96.1` and `variantPath: promos/pc` in cardInfo, where their
neighbours in the same errata files have `54_x.1` / `57_x.1` and `errata/pc`. Nothing in play reads these.
**A:** an oversight: I bring both into line at a landing. **B:** intended (issued as promos): leave them.
Needing no answer, and fixed at a landing unless you say not: 52_121, 54_304 and 57_96 have no `side:` field (the
blueprint still answers Free Peoples), 54_304 and 57_96 no `unique:`, 54_304 lowercase `culture`/`type`/`timeword`
keys, and 101_49 has `collInfo: V3_49` on a V1 card at revision 1.
(Found on 2026-10-08 in an earlier batch's notes, where it had been left as a remark. Tested as built; nothing is parked on it.)

**Ketura (2026-10-08, night):** A. "Ah no, this is almost certainly a copy-paste error. The cardInfo sections are pre-emptive for a future rework of the HJSON spec to make certain other aspects of the system obsolete, so there hasn't been a need to keep them accurate everywhere (tho of course we would prefer them to be accurate so when that eventual rework occurs there's not much that has to be updated). Tests don't have to be written against them but I would still prefer the information be checked as this is doing. Don't treat them as a source of truth, just update them to match the rest of the card definition."

### Q-0144 Foul Creation 51_44 (and 1_44): revealing an empty hand

"Fellowship: Exert an Elf to reveal an opponent's hand. If an [isengard] minion was revealed, you may draw 2 cards."
With the opponent's hand empty, the player of the card still gets a "Hand of <player>" pop-up listing no cards, which
he must dismiss before the event leaves the void. A random reveal from an empty hand (Curse Their Foul Feet! 51_36)
shows nothing at all. **A:** keep both as they are (the 51_44 test asserts the empty pop-up). **B:** no pop-up for an
empty hand on any reveal of a hand: one place in the engine, and one prompt fewer, as with Q-0090.
(Found on 2026-10-08 in an earlier batch's notes, where it had been left as a remark. Tested as built; nothing is parked on it.)

**Ketura (2026-10-08, night):** B, with a line in the log. "Both of these cards should behave the same....not sure which one is better UX. Let's go ahead and leave out the pop-up and just go to the log, but ensure that the log has some sort of message to the tune of "there were no cards in hand to reveal" or something so the user isn't confused."

### Q-0143 The Palantír of Orthanc 53_67: a yes/no before a choice that can itself be empty

"Shadow: Exert an [isengard] minion. The Free Peoples player may place any number of cards from hand on top of their
draw deck, then reveals their hand. ..." Built as "Would you like to put any cards in hand on top of your draw deck?"
followed by a choice of cards whose minimum is 0, so the Free Peoples player can say no twice. Q-0090 covers an offer
that leaves one thing to do, and Q-0010 a required trigger's choice; neither is this. The outcomes are the same.
**A:** keep. **B:** drop the yes/no and ask the choice alone (0 to N): a definition change at a landing, and the same
for any other card built so.
(Found on 2026-10-08 in an earlier batch's notes, where it had been left as a remark. Tested as built; nothing is parked on it.)

**Ketura (2026-10-08, night):** B. "The first no permits an opt-out, while giving leeway for the player to change their mind after....let's get rid of the pop-up and just make sure the message displayed to the user is sufficiently clear."

### Q-0142 "Wound each / every X": who decides the order of the wounds? (engine)

An effect that wounds several characters at once (Mithrandir, Mithrandir! 4_98, Pursuit Just Behind 1_111, Their Power
Is in Terror 1_226) is carried out one character at a time, each wound with its own 'Optional "is about to" responses'
window, and the order of the characters changes from one run of the same game to the next. Two authors met it
(v3-b59 and err-b53_14; one had a test pass and fail on consecutive runs); the orchestrator has not reproduced it.
Usually nothing hangs on the order, since every window comes round, and the tests answer every window or wound one
character only.
**A:** the Free Peoples player orders them, as your ruling on simultaneous actions (G-0005) has him order required
responses: an engine change and a new decision whenever more than one character is wounded. **B:** a fixed order with
no prompt (the order the cards came into play, say): an engine change, and a test may then name the order.
**C:** leave it; a pitfall line tells authors not to assume an order. Recommended: B. Under A or B the present random
order is recorded as an engine finding.
(Found on 2026-10-08 in an earlier batch's notes, where it had been left as a remark. Tested as built; nothing is parked on it.)

**Ketura (2026-10-08, night):** B. "In any situation where there is a timing tie of required actions, the Free Peoples player chooses the order, as you noted. However, I can see why these were coded that way, as the vast majority of the time, the order isn't going to matter, and so the game chooses for them. Let's go with B; I think the CardResolver should at some level be pulling the list of cards from GameState in a way that should be able to get them in ID order."

### Q-0141 Merry's Sword 57_242: "(limit X)", and how many [rohan] companions are spotted

"Maneuver: Exert Merry X times to exert a minion once for each [rohan] companion you spot (limit X)." Two parts, both
built the way that needs no answer from you unless you want the other:
1. **"(limit X)":** taken as a cap on each use at that use's X (two uses in one phase, X = 1 then X = 2, with three
   [rohan] companions: 1 + 2 exertions). The other reading is Q-0134's whole-phase limit (1 + 1). X has a value only
   within one use, so the per-use reading seemed likelier; a test asserts it.
2. **"you spot":** every [rohan] companion is counted without asking (as you prefer no pointless prompts). The
   original 7_242 asks how many to spot (CR 4.2 lets a player spot fewer).
(Fixed at landing on the same card: F-0255, the [rohan] companions were never counted at all.)

**Ketura (2026-10-08, evening):** Both as built: "the per-use limit was intended, given that the player can increase X to get more", and "I can't imagine why one would want some exertions but not the max." Nothing changes.

### F-0239 Gríma, Chief Counselor 55_51: may the bracket hinder a Ring-bound companion or the Ring-bearer?

"Exert Gríma to hinder a condition borne by an unbound companion (or hinder a companion and all Free Peoples cards
they bear if you can spot 4 Free Peoples cultures)." The bracket is built choose(unbound,companion), so Sam and the
Ring-bearer are never offered; the text (sheet and definition gametext) says "a companion", where the first branch
says "unbound". It may be deliberate: a hindered Ring-bearer cannot be corrupted until restored (CR 5.0), which would
help the Free Peoples player. **Fix** (choose(companion)) **or keep?** A test of the fix is parked on F-0239. (Fixed at
landing on the same card: F-0240, a condition on a hindered companion was offered.)

**Ketura (2026-10-08, evening):** New text to build: "Exert Gríma to spot an unbound companion. Hinder a condition borne by that companion (or hinder that companion and all Free Peoples cards they bear if you can spot 4 Free Peoples cultures)."

### Q-0140 Cavern Entrance 54_355: on a site, who is "you"?

"... you may exert your character in that skirmish to prevent special abilities being used." Taken as each player
(A, as built: at the start of each skirmish the Free Peoples player may exert his character in it, then the Shadow
player his minion), as Ettenmoors 51_331's "your character" and Slopes of Amon Hen 51_361 were read. B would be the
Free Peoples player only. Nothing parked; answer only if B. One answer covers site "you" generally.

**Ketura (2026-10-08, evening):** A.

### Q-0138 Gríma, Wormtongue 54_154: who is "that companion"?

"Hinder a weapon borne by an unbound companion (or all Free Peoples cards borne by that companion if they are bearing
3 or more cards)."
- **A (the plain text):** the Shadow player chooses a weapon on an unbound companion; if that companion bears 3 or
  more cards, all his Free Peoples cards are hindered instead. A companion bearing 3 or more cards and no weapon
  cannot be chosen, and a weapon on a companion with fewer cards can still be chosen while another bears 3. A test of
  A is parked on Q-0138; the correction is ready.
- **B (as built, copied from the original 4_154's branch; an engine-pass test of F-0188 asserts it):** whenever any
  unbound companion bears 3 or more cards, weapon or not, the Shadow player must choose such a companion (the
  parenthesis is mandatory, F-0047) and hinder all his Free Peoples cards; no lone weapon can then be chosen.
(Fixed at landing on the same card: F-0238, a weapon on a hindered companion was offered.)

**Ketura (2026-10-08, evening):** New text: "Exert Grima to spot an unbound companion. Hinder a weapon borne by that companion (or all Free Peoples cards borne by them if they bear 3 or more cards)", the spot restricted to companions bearing a weapon, or 3+ cards and at least one unhindered Free Peoples card (QOL). Rebuilt.

### Q-0139 Decipher ruling not carried over (default B of Q-0111): Uruk Regular (54_192)

CR 4.2 on 4_192: "...the twilight cost of an Uruk-hai -1 for each Uruk-hai you spot other than this card. Example:
If you have four Uruk Regulars in play and you use the special ability on one of them to play an Uruk Searcher
(twilight cost of 4), the Searcher's twilight cost is 1." The erratum keeps the sentence and adds "(limit -2)". The
main sentence is tested (one other Uruk-hai gives -1); the example's number cannot hold under the limit (the Searcher
costs 2). Reported only; no answer needed.

**Ketura (2026-10-08, evening):** "If no answer is needed, it should be an F not a Q." Filed as F-0242.

### Q-0136 Come Down 54_146: archery wounds the allies cannot take, lost or to companions?

After Come Down's archery ability ("allies take archery wounds instead of companions" in effect), one ally (Silinde,
vitality 2) faces a minion archery total of 3.
- **A (CR 4.2 "instead": "If the second effect cannot happen for any reason, then the original effect occurs"):**
  once the ally is killed, the third wound goes to a companion; with no ally at all the ability does nothing (and
  by Q-0034 is not offered). A test of A is parked on Q-0136; it needs an engine change, not a definition change.
- **B (as built, and the original 4_146's long-standing engine flag):** companions cannot take archery wounds that
  phase, so leftover wounds are lost, and with no ally in play the ability stops all archery wounds.

**Ketura (2026-10-08, evening):** "A is the RAW way to handle this." Engine finding F-0243.

### Q-0137 Come Down 54_146: does an archer ally away from home count for the fellowship archery total?

The erratum is built with AllyCanParticipateInArcheryFire.
- **A (the text and the Decipher ruling speak only of allies taking wounds):** no; Orophin (home 6) at site 2
  leaves the total at 0. A test of A is parked on Q-0137; the fix is the original's
  alliesTakeArcheryFireWoundsInsteadOfCompanions modifier in place of the two modifiers.
- **B (as built; CR 4.2 "ally": an ally allowed to "participate in archery fire" may be counted):** yes, the total
  goes from 0 to 1.

**Ketura (2026-10-08, evening):** A: allies take archery wounds but do not contribute. Finding F-0244 (a modifier may be needed).

### Q-0134 Legolas, Dauntless Hunter 54_73: "(limit (2))" for the whole phase, or a cap on each removal?

"Each time a Shadow event or Shadow condition is played, remove (1) for each unbound Hobbit you can spot (limit (2))."
- **A:** the card's limit for the whole phase, cumulative over its triggers (CR 4.2 "limit"; your F-0135 on "limit
  +N"; Sting 51_313's "remove (1) for each Orc revealed (limit (2))" is built per phase). With one unbound Hobbit:
  1, then 1, then nothing more that phase; it renews in the next. A test of A is parked on Q-0134; the fix wraps the
  amount in cardPhaseLimit 2.
- **B (as built, and as the old tests assert, active):** a cap on each removal; with one Hobbit every Shadow event or
  condition removes 1, however many are played.
The same "(limit (N))" on a count is built per use on Forces of Mordor 51_248, 54_92 and 4_229, so one answer covers
them. Uruk Regular 54_192's "its twilight cost is -1 for each other Uruk-hai you can spot (limit -2)" is the
same question on a cost reduction (a whole-phase test is parked there too); there, A would need an engine change, as
a phase limit does not reach a play's discount. If A: does a shortfall (a pool short of the amount) count toward the limit?

**Ketura (2026-10-08, evening):** The wording is the cumulative whole-phase limit; Legolas loses his limit altogether (new errata revision). Uruk Regular's "(limit -2)" is whole-phase: engine finding F-0245.

### Q-0135 Decipher ruling not carried over (default B of Q-0111): Legolas, Dauntless Hunter (54_73)

CR 4.2 on Eye of Barad-Dûr: "This card must be played in order to use its response text, which is affected by cards
like Legolas, Dauntless Hunter." It was about the original 4_73's cost increase on Shadow events and conditions. The
erratum rewrote that sentence to a removal of twilight when one is played; a response event played is still a Shadow
event played, so the erratum's text reaches the Eye by itself. No R clause, nothing parked. Reported only; no answer
needed.

**Ketura (2026-10-08, evening):** "good." Filed as F-0241.

### F-0235 Ever My Heart Rises 54_46: should "reveal the top 6 cards ... and stack them here" show them?

"When you play this condition, reveal the top 6 cards of your draw deck and stack them here." The definition stacks
them with stackTopCardsOfDrawDeck, so nobody is shown anything. By F-0021 / Q-0024 a reveal from a draw deck is
shown to both players. But stacked cards are face up (CR 4.2 "stack"), so the reveal would only add a click before
the stack, which anyone can look at. **Fix** (reveal, then stack) **or keep?** A test of the fix is parked on F-0235.
The original 4_46 is built the same way. (Fixed at landing on the same card: F-0234, the "how many times" discard
was offered with an empty draw deck.)

**Ketura (2026-10-08, evening):** Keep: "The window would be a useless click." Not a fault; the parked test is dropped.

### Q-0133 Bill the Pony 53_106: "Discard Bill the Pony when at an underground site": played at one, discarded at once?

A (as built, ConstantlyCheck; asserted by an active test): a state, so Bill played while the fellowship is already
at an underground site is discarded as he comes into play. B (CR 4.2 "when": a one-off trigger): only arriving at an
underground site discards him, so he can be played and kept at one. The same wording and build are on Asfaloth
1_31, Black Steed 1_208 and the original 3_106; one answer covers all four. Answer only if B.

**Ketura (2026-10-08):** "it is a continuous effect, same as the Watcher. A." Nothing changes. The official ruling on 3_106: the site's Shadow number is reduced before Bill is discarded; a test reads it.

### Q-0132 Hand of Sauron 53_90: does F-0206's canExert reach "exert a companion and discard a possession attached to him"?

Taken as settled, reported only: an exhausted companion who bears a possession is offered, loses the possession
and takes no wound (CR 4.2 "exhausted": a must-exert on an exhausted character does nothing; "effect": do as much
as you can). One who bears nothing is not offered (fixed at landing, F-0227). Reading B would apply F-0206
literally (canExert in the choice) and never offer an exhausted companion; two active tests would turn round.
Answer only if B.

**Ketura (2026-10-08):** "A is fine." Nothing changes.

### Q-0131 Uruk Raider 53_74: "While an ally is in the dead pile": whose dead pile?

"While an ally is in the dead pile, this minion is strength +3 and damage +1." Each player has a dead pile, and the
Shadow player's own ally killed on his own turn sits in his on the next Free Peoples turn, when the Raider can be in
play. The game reads only the Free Peoples player's (hasCardInDeadPile).
A. The Free Peoples player's only (as built; likelier; asserted by an active test).
B. Either player's.
The same words and build are on the original 3_74 and Uruk Stalker 4_197, and the requirement is used by a few cards
in sets 4, 7 and 12, so one answer covers them all. Nothing is parked; answer only if B.

**Ketura (2026-10-08):** ""The dead pile" refers to the current Free Peoples dead pile, same as how threats only count the current Free Peoples threats. A." Nothing changes.

### Q-0123 "Killed by a [sauron] Orc" (Orc Butchery 51_265, and every "killed by <minion>" card)

Orc Butchery's definition answers a kill in two ways: any companion killed in a skirmish that has a [sauron] Orc
in it, and a kill outside a skirmish by a wound whose source is a [sauron] Orc. So a companion killed in that Orc's
skirmish by some other card's wound (a Shadow skirmish event) counts as "killed by a [sauron] Orc" too. The
original 1_265 is built the same way.

- **A (as built):** a kill in the Orc's skirmish counts, whatever dealt the last wound.
- **B:** only a kill by the Orc's own wound (losing to it, overwhelm by it, or its ability's wound) counts.

Nothing is parked and no test reads that board. One answer covers every "killed by" card; no hurry.

**Ketura (2026-10-08):** "I think the current loose interpretation is fine, so let's stick with B." The loose interpretation is the as-built one, labelled A above; kept as built. Letter to confirm. (later) "Ah, my mistake on Q-0123 and Q-0122. A for both." Kept as built.

### Q-0122 The Number Must Be Few 51_260: does a lost penalty come back in the same turn?

"While you can spot 7 companions, the move limit for this turn is -1 (to a minimum of 1). When you can no longer
spot 7 companions or this card leaves play, this penalty is immediately lost."

- **A (the game, tested, active):** the "while" decides; the second sentence only sets aside CR 4.2's rule that a
  move-limit change holds for the whole turn. A 7th companion played later in the turn brings the -1 back.
- **B:** "lost" is for the rest of the turn: once 7 companions cannot be spotted, the limit stays at 2 even if a
  7th comes back.

Nothing in the rules or rulings settles it. The test asserts A; nothing is parked. Say B if you mean it.

**Ketura (2026-10-08):** "B. ... it should make this behave as if it were a less fraught effect, such as "men are strength +1", which obviously turns off and back on whenever the companion boundary is crossed." Off and back on is the as-built behaviour, labelled A above; kept as built. Letter to confirm. (later) "Ah, my mistake on Q-0123 and Q-0122. A for both." Kept as built.

### Q-0130 Galadriel, Lady of the Golden Wood 53_17: on a Shadows-block path, is the site replacement offered?

"Each time the fellowship moves to an opponent's site during the fellowship phase, you may exert Galadriel to
replace that site with a forest of the same site number from your adventure deck." The trigger filter is
`not(your),siteHasSiteNumber`, so a move to an opponent's Shadows-path site (no printed number, CR 4.2 "block
symbol") never offers it. The old test asserts that, so the departure looks meant.
A. Not offered (as built; the old test stays active).
B. Offered whenever Galadriel can exert (the text, and Q-0034 for an optional trigger with a cost). Sub-question
   under B: can an unnumbered forest in your deck count as "the same site number" (its position), or is the
   exertion paid for nothing?
A test of B (offer only) is parked on Q-0130. No other card uses siteHasSiteNumber.

**Ketura (2026-10-08):** "A was intended, yes." Nothing changes; the parked test of B is dropped.

### F-0220 Song of Durin 53_5: with a [moria] condition in play, must it discard (no choice to hinder)?

"Fellowship: Exert a Dwarf ... to hinder a Shadow condition (or discard up to 2 [moria] conditions)." Built as an
If: with no [moria] condition it hinders; with one it asks the player to choose hinder or discard. Your ruling on
F-0047 / Q-0035 makes a parenthesis branch mandatory when available, so the action would discard up to 2 [moria]
conditions and hinder nothing. Why it may be deliberate: the definition writes out the choice and both option texts
in full. Candidate, not fixed. A test of the mandatory reading is parked on F-0220; two active tests hold the
as-built choice (one takes Discard, one Hinder). If it is a fault, the If's true branch becomes the Discard alone
and those two go; if the choice is meant, the parked test goes.

**Ketura (2026-10-08):** "Yup, since parentheticals are required, the discard is mandatory." Fixed: with a [moria] condition in play the action discards up to 2 and hinders nothing.

### Q-0129 A Royal Welcome 53_4: "Add damage bonuses" offered for an ally with no damage bonus?

The skirmish action exerts a [dwarven] ally and offers its strength or its damage bonuses to an unbound [dwarven]
companion. With an ally that has no damage bonus, the game still offers the damage branch, which adds nothing.
Your F-0053 ruling ("pick one that you're 'fully capable' of performing") and CR 4.2 "effect" apply; the doubt is
whether adding zero damage bonuses counts as fully performed.
A. Not offered: the strength is added without a question (likelier; the parked test). Fix: a `requires` on the
   Choice (CanSpot memory(exertedAlly),damage for the damage option). Two active tests that answer the choice with
   Grimir would drop that step.
B. Offered as built: the parked test goes.

**Ketura (2026-10-08):** "If the ally has no damage, then that shouldn't be offered ... A." Fixed: the damage option is withheld for an ally with no damage bonus.

### Q-0127 Filibert Bolger 52_101: offered only in a fierce skirmish?

"Skirmish: Exert a Hobbit companion (except the Ring-bearer) twice to cancel a fierce skirmish involving that Hobbit."
The definition chooses the Hobbit with `inFierceSkirmish`, so the ability is withheld in a regular skirmish, and a
Hobbit companion outside the skirmish being fought is never offered. No other card is built this way; the
Decipher original 2_101 is built the text's way (any Hobbit companion pays, `fierceOnly` on the cancel).

- **A (as built, and as the old test asserts, active):** offered only in a fierce skirmish, paid by the Hobbit
  in it.
- **B (the text and Q-0034: a payable cost is offered whatever the effect will do):** offered in any skirmish
  while a Hobbit companion other than the Ring-bearer can exert twice; it cancels only a fierce skirmish of the
  Hobbit who paid. Your no-effect remark (a cancel that reaches nobody fighting is offered and flagged) leans
  this way. A test of B is parked on Q-0127.

**Ketura (2026-10-08):** "So long as it's marking it as noeffect, we'll go with B." Fixed: offered in any skirmish; it cancels only a fierce skirmish of the Hobbit who paid.

### Q-0128 Decipher ruling not carried over (default B of Q-0111): O Elbereth! Gilthoniel! (52_108)

CR 4.2: "As skirmishes involving the Ring-bearer cannot be cancelled, the skirmish action of this condition can
only be used to take off The One Ring." The erratum rewrote the cancel as "make the Ring-bearer strength +4 if
skirmishing a Nazgûl". Default B taken: the ruling was about the cancel, which is gone, so the +4 is usable (three
tests). The erratum looks written to work round the ruling. Reported because you asked to hear of each case; no
answer needed. (Fixed at landing on the same card: F-0218, a pointless two-way prompt.)

**Ketura (2026-10-08):** "Correctly surmised; the errata is about making the ruling unnecessary." Nothing changes.

### Q-0126 Foul Things 52_59: may it play another Foul Things from the discard pile?

"Play a [moria] card from your discard pile." The definition excludes `title(Foul Things)`, a workaround since
commit facbc783c ("Fixed infinite loop whenever a Foul Things was in hand and in the discard at the same time").

- **A (the text):** a second Foul Things is a [moria] Shadow event and may be played, then plays another [moria]
  card. A test of A is parked on Q-0126. Removing the exclusion alone is not enough: the engine's playability
  check then recurses without end, because the copy in the discard pile, when its own play is checked, counts
  itself among the cards it could play. A needs an engine change (a card checked for play from a pile does not
  count itself as a candidate for its own effect); then the exclusion can go.
- **B (as built):** the exclusion stays as a deliberate departure, recorded as such; the parked test is dropped.

**Ketura (2026-10-08):** "... we'll leave this exclusion on the card itself. B." The exclusion stays; the parked test of A is dropped.

### Q-0125 Realm of Dwarrowdelf 52_12: "discard 3 more?" asked with an empty draw deck?

"You may then discard up to 30 cards from your draw deck 3 at a time." The card asks "Would you like to discard 3
cards from the top of your deck to no effect?" after its cost, and again after each group, even when the deck is
already empty.

- **A (F-0116, a choice whose every branch does nothing is not put):** not asked once the deck is empty. Two
  tests of A are parked on Q-0125 (the deck emptied by the cost; and by a further group).
- **B (your Q-0029 and Q-0104: an ask at an empty deck helps the player notice it):** asked, as the game does.

Your two empty-deck rulings were about a draw and a look, not a discard; so I am asking rather than fixing. (Fixed
at landing on the same card: F-0213, its "damage +1 (limit +3)" counted per Dwarf instead of per card, F-0125's
shape; the Decipher original 2_12 still has it, for its own batch.)

**Ketura (2026-10-08):** "In this case we can go with A." Fixed: not asked once the deck is empty.

### Q-0124 Beneath the Mountains 52_1: "discard the top 3 cards" built as "how many times?"

"Fellowship: Spot a Dwarf to discard the top 3 cards from your draw deck." The definition asks a number N (from
0, no maximum) and discards 3 x N, with the action labelled "Discard 3 from deck to no effect." Ever My Heart Rises
4_46 and its erratum 54_46 are built the same way (N times 5). It looks like a shortcut for using an unlimited
ability several times at once.

- **A (the text):** each use discards the top 3, and nothing is asked. A test of A is parked on Q-0124.
- **B (as built, deliberate?):** a number is asked; the outcome is that of N separate uses.

One answer covers all three cards. (Fixed at landing on the same card: F-0211, the ability had no requirement at
all and was offered with no Dwarf to spot, or with an empty draw deck.)

**Ketura (2026-10-08):** "Definitely keep B. However, the reasoning behind it is unique to these three cards (ROD, BTM, EMHR)." Nothing changes; the parked test of A is dropped.

### Q-0121 Úlairë Nertëa 51_234: a yes/no after the trigger is accepted (Q-0090)?

"When you play Úlairë Nertëa, for each companion over 4, you may play a unique [Wraith] minion from your discard
pile." The definition offers the optional trigger, and once it is accepted asks "Would you like to play a unique
WRAITH minion from your discard?" before the first minion is chosen, and again before each later play. The
original 1_234 is built the same way, and the old tests, written with you, assert those questions.

- **A (as built, the old tests, active):** accept, yes/no, choose, for each play.
- **B (Q-0090, "where taking an offer leaves one thing to do, do it without asking again"):** accepting goes
  straight to choosing the first minion; each later play, being a "may" of its own, is a 0-to-1 choice
  (Q-0010) instead of a yes/no. Q-0029 ("one big batch") points the same way. A test of B is parked on Q-0121.

The settled shapes would call A a finding, but the old tests make it look deliberate, so I am asking first. One
answer covers 1_234 too when its turn comes. (Fixed at landing on the same card: F-0208, the trigger was offered
when no unique [wraith] minion in the discard pile could be played.)

**Ketura (2026-10-08):** "This is a poor implementation, yeah; B is definitely an improvement." Fixed.

### Q-0120 Decipher ruling not carried over (default B of Q-0111): Savagery to Match Their Numbers (51_139)

Decipher's ruling on 1_139: "The strength +4 bonus lasts until the regroup phase." The original: "Make an
Uruk-hai strength +2, or spot 5 companions to make an Uruk-hai strength +4 and fierce until the regroup phase."
The erratum: "Make an Uruk-hai strength +2. If you can spot 5 companions, also make it fierce and an additional
strength +2 until the regroup phase."

- **B (default, taken, tested):** the first +2 has no duration of its own, so it lasts its skirmish; only the
  extra +2 and fierce last until the regroup phase. In the fierce skirmish the Uruk-hai is +2, not +4.
- **A (ruling kept):** the whole +4 lasts until the regroup phase; the first +2 would gain `until:
  start(regroup)` when 5 companions are spotted, and the test's two readings would go from +2 to +4.

Unlike Q-0115 and Q-0116, the two readings differ here. No answer needed unless you want A.

(Fixed at landing on the same card: F-0202, the erratum was still built as the original's choice of two texts,
so with 5 companions the Shadow player could pick the plain +2; F-0203, the title's "To" for "to".)

**Ketura (2026-10-08):** "B." Nothing changes.

### F-0205 Uruk Rager 51_150: should its "discard the top 3 cards" be stopped by Frodo 4_301 / 19_28?

The Rager's win offers the Free Peoples player "exert a companion or discard the top 3 cards of your draw deck".
The discard is built `forced: false`, unlike 1_150 and every other Shadow card that makes him discard from his
deck, so "Shadow cards may not discard cards from your hand or from the top of your draw deck" (Frodo,
Courteous Halfling 4_301; Frodo 19_28) does not stop it. Since the choice is his own, this may be deliberate.
**Fix** (forced: the Frodos take the discard option away, he must exert) **or keep?** A test of the fix is
parked on F-0205.

The same build is on every PC erratum of this "must X or discard the top N" shape: Uruk Rager 51_150, Uruk-hai
Sword 51_160 (also parked on F-0205), Lurtz's Sword 52_43 ("must wound a companion or discard the top 3") and
51_265 ("must choose to add a burden or discard 4 cards"). Their Decipher originals are forced. One answer covers
all four. (Where the discard is a price the Free Peoples player MAY pay to prevent something, 51_154, 51_245,
52_45, Band of the Eye 51_240 (two cards at random from hand) and Decipher's 11_118, it is unforced too, and that looks right: it is his own choice to pay.)

**Ketura (2026-10-08):** Not a fault: "If a Shadow card gives you a choice, you may choose to discard" (Courteous Halfling ruling), so the Free Peoples player's chosen discard is not forced. The parked tests of the fix are dropped. A Shadow card that makes him discard with no choice is forced.

### Q-0118 "Bearer must be a Man or [gondor] companion": may a Man ally bear it? (Armor 51_92, Coat of Mail 51_101, Great Shield 51_107)

The three set-1 [gondor] armour errata share the sentence; the originals said "Bearer must be a Man."

- **A (the definitions and the game, taken by active tests):** "a Man, or a [gondor] companion". Any Man may bear
  it: a Man ally (Barliman Butterbur 1_70), and by Q-0058 (a race word with no "your" names no side) an
  opponent's Man minion too, which the game offers. Elsewhere "or" joins whole noun phrases in such sentences
  ("Bearer must be a companion or ally", "a [rohan] Man or Merry").
- **B:** "(Man or [gondor]) companion": only companions. The tests of the ally and the minion would turn round
  and the definitions would change.

Nothing is parked; the tests assert A. If you mean B, say so and it is fixed at once on all three.

**Ketura (2026-10-08):** "Yup, Man Allies count. ... A. As for the non-shadow restriction, yes, Strange-looking Men could move it to a Shadow Man." Nothing changes.

### Q-0119 Boromir's Cloak 51_98: which special abilities does it answer? (with F-0200)

"Response: If a skirmish event or special ability is used during a skirmish involving Boromir, discard this to
prevent that."

1. **Does "skirmish" govern "special ability" too?** **A:** any special ability used in his skirmish, a
   Response ability included (Isengard Shaman 3_59 preventing a wound in that skirmish). Decipher wrote
   "skirmish special ability" where it meant only those (Warg 5_65, Saruman's Snows 1_138). **B (the
   definition):** only Skirmish: abilities. A test of A is parked on Q-0119. Note that A cannot be built in the
   definition alone: Response abilities never count as "used" for any card that answers special abilities
   (F-0201, an engine candidate that also touches 5_59, 5_64, 5_114, 13_76).
2. **F-0200, whose abilities?** The special-ability half is built `player: shadow`, so the Free Peoples
   player's own skirmish ability (Ettenmoors' at site 2) is not answered, while the event half answers either
   player's events, his own Stout Resistance included. The text names no side. This may be deliberate (not to
   prompt him on every one of his own abilities), so I have not fixed it: say fix (both sides, as the text
   says) or keep. A test of the text is parked on F-0200.

**Ketura (2026-10-08):** "let's just go with 1B for now" and, on F-0200, "Let's keep the Shadow restriction and I'll update the wording." Nothing changes; the parked tests of 1A and of F-0200's fix are dropped.

### Q-0117 Decipher ruling not carried over (default B of Q-0111): Sleep, Caradhras (51_84)

Decipher's ruling on 1_84: "'Discard every condition' means to discard every active condition. Inactive
conditions are not discarded." The erratum rewrote that sentence to "discard a Shadow condition and then hinder
all other conditions".

The two readings come to the same thing: an inactive condition (the Shadow player's own Free Peoples condition in
his support area, on your turn) is neither discarded nor hindered, by CR 4.2 "active" alone. A test asserts it.
Reported only because you asked to hear of each case; no answer needed.

**Ketura (2026-10-08):** "One of the only actual Decipher "clarifications"." Nothing changes.

### Q-0116 Decipher ruling not carried over (default B of Q-0111): The Mirror of Galadriel (51_55)

Decipher's ruling on 1_55: "If you cannot look at a Shadow player's hand, the effect of The Mirror of
Galadriel's special ability is ignored." The original looked at 2 cards, discarded one and replaced the other;
the erratum says "reveal 3 cards at random from an opponent's hand. Place one on top of that player's draw
deck."

Here the two readings come to the same thing: with No Business of Ours 2_44 in play the ability is still
offered, Galadriel exerts, nothing is revealed and nothing is placed, which is what "the effect is ignored"
would give too. A test asserts it. Reported only because you asked to hear of each case; no answer needed.

**Ketura (2026-10-08):** "I approve." Nothing changes.

### Q-0115 Decipher ruling not carried over (default B of Q-0111): Galadriel, Lady of Light (51_45)

Decipher's ruling on 1_45: "The twilight cost of an Elf played using Galadriel's text is zero, and cannot be
raised or lowered." The original said "Exert Galadriel to play an Elf for free"; the erratum says "Exert
Galadriel to play an Elf; that Elf's twilight cost is -1."

- **B (default, taken, the game today):** the -1 is a discount like any other, so other cards' changes to the
  Elf's cost still apply. A test asserts it: beside The Misadventure of Mr. Underhill 1_129 (+2), Arwen played
  by Galadriel costs 2 + 2 - 1 = 3.
- **A (ruling kept):** the cost is printed less 1 whatever else is in play; Arwen would cost 1 there and the
  definition would change.

Nothing is parked; this is the report you asked for whenever a ruling is dropped. No answer needed unless you
want A.

**Ketura (2026-10-08): B.** "Decipher's ruling was on 'free', not Galadriel per se. Since the errata does not say 'free' it's irrelevant." Nothing changes: the active test asserts B.

### Q-0113 Ancient Enmity (51_29, and the original 1_29): two minions lose the one skirmish to that Elf. One heal or two?

"Skirmish: Make an Elf strength +1. If a minion loses this skirmish to that Elf, you may heal an Elf; any Shadow
player may discard a card from hand to prevent this." The game today makes two heals, one required trigger for
each losing minion, each with its own prevention.

- **A, one heal:** "if" is a condition, met or not; the text says neither "each time" nor "for each".
- **B, one per losing minion (the game today):** each minion's loss is "a minion loses this skirmish to that
  Elf". The original 1_29 has the same trigger sentence, and there "that minion's owner discards 2 cards" is
  plainly per minion.

The rules do not settle it: CR 4.2 speaks of "each time" and "when", not "if". Your Q-0111 ruling (Dwarven Axe,
per minion) was on a text that says "each time". The same answer decides the original 1_29 (2 cards discarded
or 4) when the campaign reaches it. One test is parked asserting A; if B, the same board becomes an active
test of two heals.

**Ketura (2026-10-08): A, for both errata and original.** "Errgh, what a mess. Let's interpret the If as evaluated once as a one-time trigger as if it said 'At the end of this skirmish, if at least 1 minion lost this skirmish to that Elf...', essentially the way that Decipher *wanted* Dwarven Axe to work." Built as F-0197 (engine pass 15).

### Q-0112 Shuffling "any number" of cards FROM PLAY into your draw deck, choosing none: is the deck shuffled?

You ruled on Q-0102 that "shuffle up to N cards from your discard pile into your draw deck" shuffles even when
nothing is shuffled in. Engine pass 12 built that for shuffles from a discard pile and from a hand, and left
shuffles from play alone, because two cards make it matter:

- Mûmakil Commander 18_71: "Regroup: Shuffle any number of your [men] mounts in play into your draw deck."
- Rest by Blind Night 4_54 (event): "Regroup: Shuffle any number of [dwarven] conditions (and all cards stacked
  on them) into your draw deck."

Today the Commander's ability is on offer with no mount in play, costs nothing, and used there does nothing.

- **A (as Q-0102):** choosing none still shuffles the deck. The Commander then gives his owner a free reshuffle
  in every regroup phase, and his offer with no mount in play is right as it stands.
- **B:** a shuffle from play of nothing is nothing. The Commander's ability with no mount in play is then a
  free optional action that does nothing, to be withheld (Q-0034), a finding on that card.

The pass's agent would choose A for the two "any number" texts, for consistency with Q-0102, and leave
fixed-count texts ("shuffle a [sauron] Orc from play", Rally the Host 7_308) doing nothing with no card. I lean
A too, weakly: the free reshuffle every regroup is real value the text does not obviously intend, which is
why I am asking and not building. Nothing is parked on this.

**Ketura (2026-10-08):** "Hrrm. Rest by Blind Night should clearly be playable with no effect, but I can see the issue with Mumakil Commander. Let's say that on an engine level, the shuffle can target 0 cards, but on a card level we'll set up Mumakil Commander to require there be a mount in play first." Built as F-0196 (engine pass 15).

### Q-0114 Far-seeing Eyes (51_43): is the Shadow player asked to pay to prevent a draw that cannot happen?

"Each time you play an Elf, draw a card. Any Shadow player may discard a card from hand to prevent this." The
game asks the Shadow player whenever he holds a card, including when the draw cannot happen: with the Free
Peoples draw deck empty, and at the fifth Elf of a fellowship phase (the Rule of 4; both probed). A yes costs
him a card for nothing.

- **A (likelier, author and reviewer):** he is not asked. This follows F-0116 and your approval of F-0051
  ("offered only when a minion was chosen"), and the fixes at landing F-0183, F-0185, F-0192. Those were all a
  choice that came up empty; here the effect itself cannot happen, which is why it is a question.
- **B (the game today):** he is asked as printed. Q-0034 keeps a costed "may" on offer whenever the cost can be
  paid, and he might want a card in his discard pile.

If A: does it cover the Rule of 4 board as well as the empty deck? The same answer reaches every costed
prevention of a draw with no guard: Tidings of Erebor 2_13, Leaping Blaze 5_3, Grishnakh 5_100, Through the
Misty Mountains 51_198, Orc Inquisitor 51_268, Tower of Barad-dur 53_104. One test is parked asserting A for
the empty deck.

**Ketura (2026-10-07): A.** "In general, prevention clauses should only trigger an ask if the thing they are preventing actually occurred, so both lack of cards to draw, prevented draws, and rule of 4 should prevent the asking of the prevention clause. So A, yeah." Recorded as F-0194, an engine item (the preventable effect itself), for engine pass 14.

### Q-0106 Worth Fighting For (103_120): "you may make the Shadow player shuffle ..." is never asked

"... If you cannot spot more than 4 companions, you may make the Shadow player shuffle one of their hindered
cards (and all cards stacked on it) into their owner's draw deck." The game goes straight to the Shadow
player's choice of card; the Free Peoples player is not asked.

- **A (the text, and your Q-0021):** the Free Peoples player gets a yes/no first; declining leaves the hindered
  cards in play. One wrapper in the definition; one test of it is parked, and two active tests gain a "yes".
- **B (the game today, and the old test):** never asked, as a shortcut, since a player would almost never
  decline (the Goblin Runner kind of shortcut).

I did not fix this one because I can't tell an oversight from a deliberate shortcut here.

**Ketura (2026-10-07): A,** "I think this is an oversight in hindsight. A, but have it be a 0-1 choice instead of a 'do you want to' prompt." **One thing I need before building it: whose choice is the 0-to-1?** The text has the Shadow player pick which hindered card ("make the Shadow player shuffle one of their hindered cards").
- **(i)** the Free Peoples player picks 0 or 1 of the Shadow player's hindered cards himself: one choice, but he now chooses WHICH card, which the text gives to the Shadow player;
- **(ii)** the Shadow player still picks the card, and the Free Peoples player's "may" is some other 0-to-1 choice before it (I do not see a natural one that is not a yes/no).

**Ketura (2026-10-07, later): (ii) with a yes/no.** "Oh I forgot shadow makes the choice for WFF. Never mind then, ask freeps and make shadow choose as it should." Built as F-0180: the Free Peoples player gets a yes/no (only when the Shadow player has a hindered card), then the Shadow player picks the card.

### Q-0111 Does a Decipher ruling on the original card reach an erratum that rewrote the sentence it rules on? (Dwarven Axe 51_9)

The first errata card to meet this, and many will. CR 4.2 rules of DWARVEN AXE as printed ("Each time a
player's minion loses a skirmish to bearer, that player discards the top card ..."): "This card can trigger
only once for each Shadow player with a minion in that skirmish, regardless of how many minions that player
had." The erratum's trigger is "Each time a minion loses a skirmish to bearer, you may make a minion strength -1
until the regroup phase ...". The original's definition carries the ruling as a per-phase limit; the erratum's
has none, and two minions losing one skirmish set it off twice.

- **A:** the ruling goes by the card's name and carries over: once per skirmish. One field in the definition;
  one test of it is parked.
- **B (the game today, and what I expect):** the ruling was about the old sentence and went with it; each minion
  that loses is a time. The earlier revision of this erratum has a test written for exactly that (two Orcs
  lose one skirmish and the Axe answers each).

Unless you say otherwise I will take B as the rule for every erratum: a Decipher ruling on the original binds
the erratum only where the sentence it rules on is still there.

**Answered by Ketura (2026-10-07): B.** "Given that our errata is our own set of rulings, I will rule that it is not affected by that ... ruling." And for the phase: "many errata happen to work around the ruling made by Decipher, but not all. I concur with B being your default, but do bring it up when it happens, because I sense that there are some where we agree with the ruling." The parked test and its clause are deleted.

### Q-0110 Pinnacle of Doom (103_129): may a Map be hindered?

"At the start of each phase, the Shadow player may remove (1) to hinder a card (except companions or The One
Ring)." The definition also leaves out Maps and Race to Mount Doom's meta-sites, in revision 0 as well, so it
looks meant.

- **A (the text as written):** a Map in a support area is "a card" and may be hindered. It would matter: cards
  that count hindered cards (Tormented Spectre, Red Eye Assassin, I Am No Man) gain from it, and PC-Movie
  always has two Maps, so the Shadow player would always have something to hinder for (1).
- **B (the game today):** Maps and meta-sites cannot be hindered. Then the card's text or the CR 5.0 entry
  could say so.

Nothing is parked: the test asserts B.

**Answered by Ketura (2026-10-07): B.** "Maps shouldn't be hindered, if only because they don't do anything and would be a distraction. B, and I agree that an explicit entry should be added for Maps and Meta-sites." For engine pass 12 as a rule of the game (F-0176); the CR 5.0 "hinder" entry is yours to amend.

### Q-0109 Doom Causeway (103_127): is The One Ring discarded when it is hindered there?

"Each time a card is hindered, discard it." The Sooner We're Rid of It 103_118, used for the Ring-bearer,
hinders each card he bears, the Ring among them.

- **A (the letter, and the game today):** yes. The Ring is "a card"; the rules protect only its bearer. It goes
  to the discard pile and the game goes on with a Ring-bearer who bears no Ring (he loses its bonuses, stays
  Ring-bearer, and can still win by surviving site 9).
- **B:** The One Ring never leaves its bearer: it stays, hindered. One filter in the definition.

Nothing is parked: the test asserts A.

**Answered by Ketura (2026-10-07): B.** "The Ring-bearer and One Ring should never be discarded for any reason, so B for sure." For engine pass 12 as a rule of the game, not a filter on this card (F-0175).

### Q-0108 Gorgoroth Wastes (103_124): does a Free Peoples card borne by a HINDERED companion count?

"When moving here, for each Free Peoples card borne by companions, the Free Peoples player must discard it or
add (1)." The Q-0098 wording put on this card lets the card itself be hindered; it also lets the BEARER be a
hindered companion, so armor on a hindered Gimli is asked about and costs (1) if kept.

- **A (my reading, and the reviewer's):** no. A hindered card has no card type to be found by (CR 5.0), so a
  hindered Gimli is not "a companion" for this text. Two filters gain not(hindered) on the bearer.
- **B (the game today):** yes.

One test of A is parked; the test of B is active. Related to Q-0103 but does not hang on it: the burdens
exception that could make Q-0103 a B has no counterpart here.

**Answered by Ketura (2026-10-07): A.** "Oh ho, very good catch. I concur with your and the reviewer's reading of A." Both filters now leave a hindered bearer out; the test of A is active and the other deleted.

### Q-0107 Desolation of Ash (103_122): keep the guard that withholds its offer when there is no event to take?

"At the start of each phase, the Shadow player may remove (1) to take a Shadow event for that phase into hand
from discard." The definition withholds the offer when no Shadow event for that phase is in the discard pile.
Your Q-0034 says an optional trigger with a cost is offered whenever the cost can be paid, and calls such a
guard a finding (Naith Armaments). But this one looks deliberate: without it the Shadow player is asked at the
start of every phase at this site while the pool is not empty, six or more yes/no prompts a turn.

- **A (Q-0034 as ruled):** offer it always, flagged as doing nothing when there is no event. One word in the
  definition; one test is parked on it (F-0173).
- **B (the game today):** keep the guard here for the sake of play.

**Answered by Ketura (2026-10-07): B, keep the guard.** "Shadow almost never wants to waste twilight like that and it would be a pester prompt showing 5 times or more per turn for no reason." F-0173 is not a bug; the test is turned round to assert the guard.

### Q-0105 Merry's Pipe (103_113): is a pipeweed discarded WITH its killed bearer "a pipeweed is discarded"?

"Each time a pipeweed is discarded, you may exert bearer to play that pipeweed from your discard pile." Pippin
dies bearing a pipeweed; it goes to the discard pile with him. Today Merry's Pipe makes no offer, because its
trigger carries `source: any` and a discard no card did has no source.

- **A:** yes, it is offered (Q-0081: any discard from play is a discard, one with its bearer included). The fix
  is one line deleted; one test is parked on it (F-0170).
- **B (the game today):** no. Your Q-0023 said "we won't respond to cards discarded on bearer death". Its
  reason there was a response that PREVENTS the discard and leaves the card nowhere to go, which is not this
  case (the Pipe plays the card from the discard pile onto a living bearer), but the words are general.

**Answered by Ketura (2026-10-07): A.** "Yes, I think Merry would kick in, but it has to be playable. Wizard's Stash without a Wizard should not proc, but Saruman's Hospitality might if there's a valid companion with a pipe not bearing a SH ... *replaying* can retarget and locate another eligible bearer, so it's kosher." F-0170 fixed: the `source: any` line is gone and the test is active.

### Q-0104 Hornblower Barrel (103_112): "you may discard a Free Peoples card from your draw deck" with an empty draw deck

The offer is made; taking it shows an empty deck, discards nothing and shuffles nothing. This one does not
hang on Q-0102: nothing is shuffled under either answer there.

- **A:** withhold it, as any no-cost optional trigger that would do nothing (Q-0034). One requirement on the
  trigger; the parked test goes live.
- **B (the game today):** keep it, as you kept the offer of a draw from an empty deck (Q-0029): a chance to
  notice the deck is empty.

Holds nothing: one test of A is parked, and the test of B is active.

**Answered by Ketura (2026-10-07): B, keep the offer.** "Normally I would avoid showing it for QOL reasons, but the draw deck emptying entirely is a rare event, and having that underlined by the ask still happening and showing nothing would help them realize it." The parked test is deleted.

### Q-0103 For Frodo (103_107): with Frodo hindered, are his burdens still "burdens on Frodo"?

"... strength +1 for each burden on Frodo." For the Shire 103_108 hinders the Ring-bearer in region 1, so the
two events meet in play.

- **A (what the game does):** no, +0. A hindered card cannot be spotted or targeted by its title (CR 5.0), so
  there is no "Frodo" to bear them; Q-0098 said nothing more specific than a side reaches a hindered card.
- **B:** yes. CR 5.0 also says "if the Ring-bearer is hindered, burdens may be spotted and counted", and he is
  Frodo still. The definition would then look for a hindered Frodo too.

Holds nothing: the test asserts A and is active.

**Answered by Ketura (2026-10-07): A.** "The burdens can still be spotted by themselves, but I think while Frodo is hindered, he himself can't be, so they're 'on' 'a Free Peoples card' that can't otherwise be distinguished." The game and the test already say so.

### Q-0102 "You may shuffle up to 2 ... from your discard pile into your draw deck": taken with no card chosen, is the deck shuffled?

Met on Flotsam (103_106); Morgul Skulker 1_258 has the same words for [wraith] cards.

- **A (what the game does):** no. Nothing is shuffled in and the draw deck is left as it was. Then the offer
  with no pipe or pipeweed in the discard pile does nothing at all and should be withheld (F-0161, the fix is
  one requirement, as on Hound of Isengard).
- **B:** yes, the deck is shuffled whatever is put in. Then the offer always does something (a free reshuffle
  of a deck whose order may be known) and stays as it is.

Holds one fix: F-0161 waits on this. The test of A (taking none shuffles nothing) is active.

**Answered by Ketura (2026-10-07): B, the deck is shuffled.** "Let's say yes, it gets reshuffled, I don't think there's a ruling on this but I think it's more consistent for the shuffle to always happen even if you're shuffling 'in' nothing." So the offer always does something and F-0161 is not a bug. For engine pass 12 (F-0178).

### Q-0101 Decipher's "in / from <a player's> support area": do borne cards count there too, and whose area is a Shadow card on an ally in?

Your answer to Q-0099 is built for the V3 words "support card": a card lying in a support area, or borne at any
depth by a card that is. The older cards say it another way and were left alone, because two things are not
settled by your words:

1. Do they take in borne cards too? Tempest of War 12_148 ("discard from play all conditions in all players'
   support areas") leaves a Weary on an ally and a Pillage of Rohan on a controlled site where they are today.
   The cards whose play would change: Bilbo 2_96, Great Peril of Fire 7_280, Tempest of War 12_148, Guard of
   the White Tree 13_67, Desolation of Saruman 102_72.
2. Whose support area is a card in when the other player's card bears it? The Shadow player's Black Breath on
   your ally: is it in YOUR support area (where it lies) or its owner's? "Spot a [wraith] card in your support
   area" (Shadow in the East 13_180 and its like) and "a card from their opponent's support area" (Desolation
   of Saruman) turn on it.

- **A:** yes to 1, and the area is that of the card at the bottom of the chain (a Black Breath on your ally is
  in your support area, not the Shadow player's). The agent's proposal; fourteen definitions change, five in
  play.
- **B:** leave the Decipher wording as the zone alone; only "support card" reaches borne cards.

Holds nothing. "Transfer this from your support area" and "if this is in your support area" (about 37 cards)
keep meaning the card itself lying there under either answer.

**Answered by Ketura (2026-10-07): A, borne cards count.** "The borne cards are physically placed under the cards which bear them, and so by transitive property I would expect that if their bearer is 'in the support area' then they must, too." 1: "Stacked cards are definitely not seen, but I think otherwise they should be fair game. Weary on an Ally and Pillage should both get hit." 2: "If my opponent's Black Breath is on my Elrond, it's in my support area, because that's where Elrond is." For engine pass 12 (F-0177).

### Q-0100 Driven By Her Own Cruel Will (103_105): played where no Hobbit can be strengthened, is a minion still hindered?

"Skirmish: Make a skirmishing Hobbit bearing a hand weapon strength +X ... At the end of that skirmish, hinder
a minion involved in that skirmish if you cannot spot more than 4 companions." The event is offered whatever it
will do, so it can be played while a Man is fighting, with 4 companions or fewer.

- **A (what the game does):** no. "That skirmish" is the strengthened Hobbit's; with no such Hobbit the second
  sentence has nothing to speak of.
- **B:** yes. The second sentence stands by itself ("do what you can"), and "that skirmish" is the one being
  fought.

Holds nothing: the test asserts A and is active.

**Answered by Ketura (2026-10-07): A.** No minion is hindered when no Hobbit was strengthened. The game and the test already say so.

### Q-0099 Is a card borne by a card in the support area a "support card"?

Met on Sauron's Reach (103_101), which hinders "support cards" on both sides. Neither rules document defines
the term. An ally plainly is one (CR 4.2 "ally": "Play them to your support area"); the doubt is what an ally
or another support-area card bears (Vilya on Elrond, a Black Breath on an ally).

- **A (Sauron's Reach as built, and its old test, named "...HindersAllFPSupportsAndAttachmentsToSupports"):**
  borne cards count. Elrond and Vilya are both hindered and both counted toward what the Shadow player owes.
  Grond 58_103 ("If that card is not in the support area ...") is built the same way. The six other V3
  definitions that say "support card" (103_1, 103_23, 103_82, 103_96, 103_115, 103_120) filter on the support
  zone alone and would gain the borne cards.
- **B:** only cards lying in the support area itself. CR 5.0 "hinder" leans this way ("Borne and stacked cards
  are not themselves hindered unless the hindering effect says so"), and it is what those six do. Sauron's
  Reach then loses the borne cards from both filters (its main test's 5 becomes 4).

Holds nothing: the tests assert A, as the game plays today, and nothing is parked.

**Answered by Ketura (2026-10-07): A.** "'Support' is meant to be shorthand for 'in the support area'. A card attached to another is on Gemp in the 'attached zone', but ultimately that's an implementation detail; it is still 'in the support area'." Sauron's Reach stands as built; the other definitions that say "support card" gain what support-area cards bear in the next engine pass.

### Q-0098 May "a Free Peoples card" (or "a Shadow card") be a hindered one, when the effect is not a hinder?

Met on Cover of Darkness, Omen of Despair (103_93): "... to discard a Free Peoples card borne by a companion
that Troll is skirmishing", with a hindered Athelas and an unhindered Last Alliance on Aragorn. CR 5.0 "hinder":
"The side/alignment of a hindered card continues to apply. This means that hindered cards can be spotted or
targeted as 'a Free Peoples card' or 'a Shadow card'". Your Q-0060 kept hindered cards out of HINDER choices
"for QOL purposes", since hindering a hindered card does nothing; discarding one does something, and the log
does not say whether that reaches other effects.

- **A (CR 5.0 as written):** both cards are offered and the hindered Athelas may be the one discarded. Every
  definition that picks by side alone then needs `override: hindered` (or the engine's default changes for
  side-only filters). In V3 besides this card: V3-Gandalf lines 38, 115, 523; V3-Rohan 835; V3-Shire 557;
  V3-Sites 373. Only two V3 definitions ask for hindered cards today, both with "hindered" in their text.
- **B (what the game does):** hindered cards are out of every choice unless the text says "hindered"; the Last
  Alliance is discarded without a choice. CR 5.0 then wants a line saying so.

Holds nothing: one test of reading A is parked (HinderedFreePeoplesCardOnThatCompanionMayBeTheOneDiscarded).
The mechanic test for this, at/HinderTests.HinderedCompanionsCanBeSpottedAsFreePeoplesCards, is an empty stub.

**Answered by Ketura (2026-10-07): A.** "Yes, as written that is valid. This should only apply to cards which target 'a card', 'a Shadow card', or 'a Free Peoples card', since anything more specific is unable to pierce hinder's veil." 103_93 now carries `override: hindered` and its test is active; the other V3 definitions that pick by side alone are for the next rulings pass.

### Q-0097 Flame of Calenhad (103_86): "(except race)" here too?

You ruled on Pippin, Trollslayer 103_31 (Q-0071) that race, though an unloaded keyword by the rules, is not
passed on, and had its text gain "(except race)". This card has the same words, and Merry 103_85 beside Eowyn
is a Hobbit who is also a Man, so the board exists: the game does not make the other Men Hobbits, and the
test asserts that. Shall I add "(except race)" to this card's text as well?

**Answered by Ketura (2026-10-07): yes.** "I had not dreamed of such shenanigans as making Theoden and Aragorn Hobbits. Yes, we will add 'except race'." The text now reads "each of the unloaded keywords (except race) on that Man".

### Q-0096 Northern Signal-fire, Flame of Calenhad (103_86): do the other Men keep the keywords when the Man exerted leaves?

"Maneuver: Hinder this beacon and exert an unbound Man. Until the regroup phase, each of your other Men gain
each of the unloaded keywords on that Man."

- **A:** they keep them until the regroup phase, whatever becomes of him.
- **B (what the game does):** the keywords are read from him as he is, so when he is killed (or hindered: Merry
  103_85 hinders himself in a skirmish) the others lose them at once.

Holds nothing: one test of reading A is parked. A would need an engine change.

**Answered by Ketura (2026-10-07): A.** "Huh, I hadn't realized killing him would alter the keywords granted. The intent was that a snapshot be made at the time of invocation, and whatever keywords were on the target would get memorized and applied to everyone else. Guess it needs an engine change." Engine pass 8.

### Q-0095 I Am No Man (103_84): "make a minion strength -1 ..." but only a minion in the skirmish is offered

The same shape as Trapped in its Gaze 102_66 (Q-0051), where you kept the game and changed the text to "a
skirmishing twilight minion".

- **A (what the game does):** only a minion in the skirmish being fought; the text then wants "a skirmishing
  minion", and I would change it as on 102_66.
- **B (the text):** any minion.

Holds nothing: one test of reading B is parked.

**Answered by Ketura (2026-10-07): A.** "Yes, we'll update the game text." The text now reads "a skirmishing minion"; the parked test is turned round.

### Q-0094 Gondor Calls For Aid! (103_83): may X be more than the items that can be played?

"Fellowship: Hinder X beacons to play X items from your discard pile on your Men." With three beacons to hinder
and one item a Man can bear in the discard pile:

- **A (what the game does, and what the active test asserts):** X runs 1 to 3; taking 3 hinders three beacons
  and plays the one item. There can be a reason: I Am No Man 103_84 weakens a minion for each hindered card,
  and you allowed a wasteful X on Gollum (Q-0087) and asked only that the first play be possible (Q-0062).
- **B:** X is capped at the items that can be played, as you ruled for From Deepest Dungeon (F-0067) and
  Northern Signal-fire (F-0111).

Holds nothing: one test of reading B is parked.

**Answered by Ketura (2026-10-07): B.** "Hmm. B, yeah." X is capped at the items that can be played. Engine pass 8 (it needs the "can be played on" filter).

### Q-0093 "Limit once" against "limit +N": should the abilities put on offer at their limit go back to being withheld?

Your answer on 103_103: "there's a nuance between 'limit +3' and 'limit once', with the former being a
cumulative addition of multiple invocations, and the latter being a limit on the number of times the ability
can invoke at all." The no-effect work (design note, section 5, built yesterday) did not draw that line: every
ability with a cost and a limit on its uses now stays on offer at the limit, flagged `limit`, its cost paid for
nothing. Your Q-0041 answer ("technically it should be offered") was given on Rivendell Gateway, which is a
"+N" card, and the rules entry for "limit" speaks only of "+N".

The 30 abilities that changed are of both kinds:
- **"+N" caps written as a count of uses** (stay on offer, flagged, by either reading): Rivendell Gateway 101_59
  "(limit +6)", Orc Scimitar 1_269 "(limit +3)", Shake Off the Shadow 8_13 "(limit +2)", and the like.
- **"Limit once / limit 1"** (the ones your nuance would take back off offer): Dark Tidings 7_130 "(limit 1)",
  Theoden 4_365 and Stables 4_339 "(limit once per turn)", Lorien Throne Room 101_61 and Pass of the Spider
  103_12 "(limit once per phase)", Shadowplay 10_114 "(limit once per turn)", and the like.

- **A (your nuance, applied everywhere):** a "limit once / N times" ability cannot be used again at its limit and
  is withheld, cost or no cost; only a "+N" cap stays on offer, flagged `limit`. I would sort the 30 by their
  texts and take the second kind back; the flag's wire format does not change.
- **B (as built):** leave all 30 on offer at their limit; 103_103 is the one exception.

I recommend A: it is what the rules entry describes, and it removes the Lorien Throne Room prompt at every
exertion. Nothing is changed until you say.

**Answered by Ketura (2026-10-07): A, with one correction.** "Dark Tidings is more like '+1' than 'once'; it is limiting the number of threats that can be added the way that Scimitar limits the strength bonus. Besides that nitpick, yes, I agree that the first kind should be offered but effectless, while the second should halt being offered once the invocation limit is reached." So a limit that counts what the effect gives ("+N", or "limit N" of things added) leaves the ability on offer, flagged; a limit on invocations ("limit once per ...") withholds it. Engine pass 8 sorts the 30.

### Q-0092 Death Take Us All (103_82): what is a "copy of that card"?

"...for each ... copies of that card you can spot." Two parts, both as the game has them today and as the tests
assert:

- **(a)** the card spotted counts itself, so the bonus is never less than +1;
- **(b)** a card of the same title under another subtitle counts (Cover of Darkness, Omen of Fear beside Omen
  of Dread counts 2). Three other V3 cards count by title the same way.

Holds nothing. Say only if either should be otherwise.

**Answered by Ketura (2026-10-07):** "Yup, that's correct." Both parts stay as the game has them.

### Q-0091 The Witch-king, Empowered by His Master (103_80): is the exertion the price of restoring?

"The Free Peoples player may restore any number of their cards, and must exert one of their characters for
each card restored."

- **A (a price):** no more cards may be restored than exertions can be made; with every character exhausted
  nothing may be restored.
- **B (what the game does, and what the tests assert):** any number is restored, then one exertion is made for
  each as far as someone can exert. Frodo alone with one exertion left restores two cards and exerts once.

Decipher cards of the same shape ("must exert a companion for each ...") are built as B. Holds nothing. Say
only if you want A.

**Answered by Ketura (2026-10-07): B, as the game has it.** "TWK here is worded carefully to permit such a result, that's working as intended."

### Q-0090 Ulaire Nertea (103_77): with only artifacts to play, is the player asked a second time?

Taking the offer exerts the minion played; the game then asks "Would you like to exert Ulaire Nertea to play
an artifact from discard?", and answering No leaves the minion exerted for nothing.

- **A (what the game does):** two questions.
- **B (the text has one "may"):** with only artifacts to play, taking the offer exerts both and plays the
  artifact, with no second question.

Holds nothing: one test of reading B is parked.

**Answered by Ketura (2026-10-07): B.** "Yeah if we can reduce the questions that's good." With only artifacts to play, taking the offer exerts both and asks nothing more (engine pass 7).

### Q-0089 Ulaire Nertea (103_77): does "a branch in parentheses is mandatory" reach a branch with its own price?

"Each time you play a minion, you may exert it to play a possession on it from your discard pile (or an
artifact if you also exert this minion)." You ruled (F-0047, Q-0035) that a branch in parentheses is taken
whenever it is available. Here the branch has a further cost the player chooses to pay.

- **A (what the game does, and what the tests assert):** the player chooses. With a possession and an artifact
  both playable, taking the possession leaves Nertea unexerted.
- **B (the ruling to the letter):** with an artifact playable and Nertea able to exert, Nertea must exert and
  only an artifact may be played.

One answer covers Whatever Means 13_121 "(or +6 if you remove 2 burdens)", built as A. Holds nothing: one test
of reading B is parked.

**Answered by Ketura (2026-10-07): keep A.** "That is a mandatory wording. I will adjust the wording; keep A." The player chooses; the parked test of the other reading is deleted (engine pass 7, with the card's other changes).

### Q-0088 Ill Wind (103_67): who chooses the companion that is exerted or wounded?

"...you may hinder a wounded companion. The Free Peoples player may make you exert another companion to prevent
this (or wound if you can spot The Witch-king)."

- **A (what the game does, and what the old tests assert):** the Free Peoples player says yes and chooses the
  companion.
- **B (the text):** "make you exert" has the Shadow player do it, so the Shadow player chooses; with The
  Witch-king that lets the Shadow player put the wound on an exhausted companion and kill him.

Every other card with this sentence is built as B (Southron Troop 4_256, Long I Fell 4_97 and 54_97, Morgul
Destroyer 57_190). Holds nothing: one test of reading B is parked.

**Answered by Ketura (2026-10-07): B.** "Oh, I meant for B; it was only supposed to change the exert to a wound, not alter who selects it." The Shadow player chooses the companion. Rulings pass 7.

### Q-0087 Gollum, Half a Wraith Himself (103_66): is X capped at what the dearest companion costs?

"Assignment: Remove X threats to assign Gollum to a companion costing X or less (except the Ring-bearer)."

- **A (a cap, as the game means to have it):** X is offered no higher than the dearest companion's cost, so
  the Shadow player cannot throw threats away for nothing. As built the cap is wrong anyway (F-0133: it adds up
  the companions tied for dearest; Sam and Gimli at 2 each give a ceiling of 4, where a cap would be 2).
- **B (the text):** X is the player's, up to the threats there are. With a threat and nobody within reach the
  ability is then offered and takes its threats for nothing (it has a cost).

Either way X of zero is on offer where it does nothing (F-0132); its correction takes its shape from this
answer. Holds nothing: four tests are parked.

**Answered by Ketura (2026-10-07): B.** "If the player has a reason to remove more threats (against Gondor Wraiths or something), then I think they may." X is the player's up to the threats; the cap goes (F-0133 moot), and X of zero is on offer only where it does something (F-0132). Rulings pass 7.

### Q-0086 "Play any number of / up to N ... from your discard pile" with nothing there to play: offered?

Morgul Legions 103_68: "Shadow: Spot a Nazgul to play any number of possessions on your [ringwraith] Orcs from
your discard pile. Discard this condition." With a Nazgul to spot and nothing in the discard pile that could
be played:

- **A (what the game does, and what the active test asserts):** offered. "Any number" includes none, so it
  plays nothing and discards the condition.
- **B:** withheld. A play from a pile as an effect is treated as a cost, and you asked that the first play be
  confirmed possible (Q-0062).

One answer covers every card of this shape, all built as A today: These Are My People 5_41, Dead Marshes 5_95,
Merry 7_321, Tol Brandir 1_363, Malice 53_79. Holds nothing.

**Answered by Ketura (2026-10-07): B.** "I think B is more consistent with the play-from-discard rules." Withheld when nothing there can be played (engine pass 7, with the filter F-0140 needs; the five Decipher cards of the shape with it).

### Q-0085 Cover of Darkness, Omen of Fear (103_64): with no unbound companion to spot, is the ability offered?

"Assignment: Spot 3 [ringwraith] minions, remove a burden, and hinder this condition to spot a [ringwraith]
minion and an unbound companion. Hinder all other characters."

- **A (the spots stand after "to", so they are effect; an ability with a cost is offered when the cost can be
  paid):** offered with Frodo alone, or Frodo and Sam. No companion is spotted, and every other character is
  hindered, the whole fellowship and the Ring-bearer included: nothing can be assigned and the fellowship
  cannot move again.
- **B (what the game does, and what the old test asserts):** the definition has both spots among the costs, so
  without an unbound companion the ability is not offered.

Holds nothing: one test of reading A is parked.

**Answered by Ketura (2026-10-07): A.** "As written I think it is A, tho B is the better UX...but then I can clearly see why one would want to do it without an unbound companion, to halt a ring-bound fellowship from moving. Let's say A." The spots become effects (rulings pass 7).

### Q-0084 "Limit once per site" (Cirith Ungol Watcher 103_63; also 103_92 and 103_103): renewed by a new turn?

The fellowship stands at one site at the end of a turn and through the fellowship phase of that player's next
turn. Is a once-per-site draw taken there on the first turn available again in the next turn's fellowship
phase, still at that site?

- **A (the words):** no; the next draw comes at the next site. This needs an engine limit that follows the site.
- **B (what the game does):** yes; the limit is once for each site in a turn. Under B the fault in F-0127 (a
  draw in the fellowship phase uses up the draw of the site then moved to) is corrected in the definitions.

Holds nothing: one test of reading A and one of the F-0127 correction are parked.

**Answered by Ketura (2026-10-07): B.** "This one is shakey, but the intent is not that you have to track it across turn boundaries." Once for each site in a turn; F-0127 is corrected in the definitions (rulings pass 7).

### Q-0083 War Howdah (103_62): may a Southron who already bears a Howdah be exerted for a second one?

"Maneuver: If this is in your support area, exert a mounted Southron to transfer this to that Southron. Limit 1
per bearer. Choose a Man stacked on that Southron. Until the end of the turn, that Southron gains the game text
of that stacked Man."

- **A (to the letter):** any mounted Southron may be exerted; the limit stops the transfer, the Howdah stays in
  the support area, and the stacked Man's text is still gained, so a second Howdah buys a second Man's text.
- **B (what the game does, and what the tests assert):** only a Southron who can bear it may be exerted; with
  no other mounted Southron the second Howdah's ability is not offered.

Holds nothing. Say only if you want A.

**Answered by Ketura (2026-10-07): B.** As the game has it. Nothing changed.

### Q-0082 They Are Not All Accounted For (103_103): should it stay on offer at its limit?

Its cost is exerting the OTHER player's companion or ally, to draw a card, with a limit of once per site. By
Q-0041 an ability with a cost stays on offer at its limit: the cost is paid and nothing else happens. Converted
that way, one Halls of My Home (reveal 3) gave the Shadow player three offers: Aragorn took three exertions
for one card drawn. Against a Dwarven mill deck that exhausts the fellowship at every site.

- **A:** convert it like the others (the rule as written); the card then wants new wording.
- **B (as it is now):** leave it withheld at its limit, the one exception, until its text is settled, for
  instance "Once per site, each time ..., you may exert ... to draw a card".

Holds nothing: the definition is unchanged and its existing tests pass. The agent that built the limit gate
recommends B, and so do I: the limit is the only thing capping a cost paid by the opponent.

**Answered by Ketura (2026-10-07): B, it stays withheld.** "Ah right, this should be a limit on *triggering* once per turn. I think there's a nuance between 'limit +3' and 'limit once', with the former being a cumulative addition of multiple invocations, and the latter being a limit on the number of times the ability can invoke at all." What this means for the other "limit once" abilities the no-effect work put on offer at their limit is asked back as Q-0093.

### Q-0081 "When this minion is discarded (from play)": does the end-of-turn discard of minions count?

A card's own discarded-from-play trigger is not set off by a removal that no card did: the end-of-turn discard
of all minions, and discards the engine reports with no source (a card lost with its bearer, among others).
Probed: Orc Footman 12_93 ("when this minion is discarded from play, you may shuffle it into your draw deck")
is not offered its shuffle at the end of the turn. Narzug 30_36 has a "not in regroup" requirement, which
suggests its author expected the end-of-turn discard to count. Also bears on Mordor Guard 7_287.

- **A (what the game does):** only a discard done by a card sets it off.
- **B:** any discard from play does, the end-of-turn discard of minions included.

Holds nothing now: none of the three cards has been reached by the campaign (sets 7, 12 and the Hobbit).

**Answered by Ketura (2026-10-07): B.** "I would expect that end-of-turn discard to produce a triggerable response." Any discard from play sets it off. Engine pass 7.

### Q-0080 Raiding Schooner (103_55): are the costs of one sentence paid in the order printed?

"Maneuver: Spot a Corsair, hinder another Raiding Schooner, and remove 3 [raider] tokens from your cards to
hinder a Free Peoples artifact." Each Schooner puts its token on itself when played, so the ordinary board has
tokens on the Schooner that gets hindered, and tokens on a hindered card cannot be removed. The rules give no
order for the costs of one sentence beyond twilight first.

- **A (printed order):** the other Schooner is hindered first and its tokens are out of reach; with fewer than
  3 tokens elsewhere the ability is not offered.
- **B (the player's order):** the tokens may be removed first, the other Schooner's among them, and then it is
  hindered; the ability is offered and works in full.

The game today does neither (F-0123): it offers the ability, hinders the Schooner, removes what tokens it can
still reach and hinders nothing. Two other V3 cards can only be paid for in printed order (Inner Gate 103_25,
Smeagol 103_116). Holds nothing: one test of reading A is parked, and one active test reads the printed order.

**Answered by Ketura (2026-10-07): A.** "Costs (and effects for that matter) should always be evaluated in the order in which they appear." The ability now requires 3 [raider] tokens on cards other than the Schooner it hinders (F-0123 fixed); the parked test is active.

### Q-0079 Corsair Helmsman (103_46): "by another card": other than what?

"Response: If your [raider] card is about to be hindered or discarded by another card, exert or hinder this
minion to prevent that." The earlier wording was "by a card effect".

- **A (the plainer grammar):** a card other than the [raider] card itself. Nothing a card does to itself is
  answered: a Desert Wind minion choosing "hinder this minion" when played is not saved by a Helmsman, and a
  Helmsman hindering itself as its payment is not answered by a second Helmsman.
- **B (what the game does):** a card other than this Helmsman. A Helmsman may then exert to prevent a Desert
  Wind minion's own hinder, which puts that minion into play unhindered with nothing removed from the pool.

Elsewhere "another X" is counted from the card whose text it is, which favours B; this sentence's grammar
favours A. Holds nothing: one test of reading A is parked, and the active tests hold either way.

**Answered by Ketura (2026-10-07): B.** "The wording is meant to prevent Helmsmen from responding to itself hindering itself." The parked test of reading A was turned round to assert B.

### Q-0078 Bladetusk Matriarch (103_42): is the archery sentence part of the Maneuver ability?

"Maneuver: Remove (6) to make this artifact a fierce mounted Southron minion until the end of the turn that is
strength +3 and ambush (1) for each Southron stacked on her. She adds 1 to the Shadow archery total for each
Southron stacked on her (limit 6)."

- **A (what the game does, and what the tests assert):** "she" is the minion the ability makes; as an artifact
  with Southrons stacked she adds nothing to archery.
- **B:** a standing sentence of its own: she adds to the archery total from the support area whenever Southrons
  are stacked on her and another minion makes an archery phase matter.

Holds nothing. Say only if you want B.

**Answered by Ketura (2026-10-06): A.** The archery sentence holds only while she is the minion, as the game has it. Nothing changed.

### Q-0077 Bladetusk Charger, Matriarch, Rearguard (103_41, 103_42, 103_43): may the Maneuver ability be used again once the card is a minion?

All three definitions require the card to be in the support area, which the texts do not say. By the Q-0034
rule an ability whose cost can be paid is offered even if it does nothing.

- **A:** offered again, and the bonuses are counted once ("that is strength +N ... for each Southron stacked
  here" describes the minion). For the Charger a second use is not idle: it stacks more Southron Men. For the
  Matriarch and the Rearguard it pays (6) or (5) and changes nothing, so it would carry the no-effect flag.
- **B (what the game does):** the ability is for the card in the support area only; never twice in a turn.
- **C:** offered again, and each use adds its own bonuses (a second use doubles strength and ambush).

Removing the requirement alone gives C, not A: the modifiers would be added a second time. Holds nothing: one
test of reading A is parked on the Charger and one on the Matriarch; the Rearguard's tests assert B.

**Answered by Ketura (2026-10-06): B.** The ability is for the card in the support area only, as the game has it. The two parked tests of reading A were deleted.

### Q-0076 A Wizard Knows Better (103_40): without Saruman, may X be zero?

"Then you may exert X [isengard] minions (or spot Saruman and spot X [isengard] minions) to hinder an unbound
companion costing up to X." The only unbound companion who costs 0 is Galadriel, Sorceress of the Hidden Land
13_15.

- **A:** "exert X minions" is an exertion cost like "exert X times", so at least one minion must exert.
- **B (what the game does, and what the active test asserts):** your ruling on 101_5 keeps X from zero only for
  "exerting X times"; this says "exert X minions", so the Shadow player may exert nobody and hinder Galadriel
  for nothing, with or without Saruman.

Holds nothing: one test of reading A is parked. Say only if you want A.

**Answered by Ketura (2026-10-06): A.** At least one minion must exert. The exert choice now has a minimum of 1, the parked test is active, and the test of the other reading was turned round.

### Q-0075 One Ill Turn Deserves Another (103_38): does it answer its own hindering or discard?

"Each time your condition is hindered or discarded by a Free Peoples card, hinder a Free Peoples condition."

- **A (what the game does, and what the tests assert):** it is itself "your condition", so when a Free Peoples
  card hinders or discards One Ill Turn itself, the Shadow player hinders one Free Peoples condition. The
  definition has a separate trigger for this, commented "we want both hinder and discard", and the Q-0020 ruling
  (a card's own required trigger still happens when the same loss removes the card) points the same way.
- **B:** once hindered its text does not apply, and once discarded it is out of play, so nothing happens.

Holds nothing: both self tests are active and assert A. Say only if you want B.

**Answered by Ketura (2026-10-06): A.** It answers its own hindering or discard, as the game has it. Nothing changed.

### Q-0074 Details the fix agents settled the game's way (2026-10-06); say only if one is wrong

None of these holds anything. Each is asserted by a test or noted in a ledger.

1. **Overwhelm by minion (Q-0044).** A minion spared by its protection has still lost: it takes 1 wound plus
   the winner's damage bonus. Its loss is an ordinary loss, so "when overwhelmed" texts do not fire for it; the
   winner's result is still an overwhelm.
2. **Breached Wall and winning by controlling sites.** The win check counts site cards, so four real sites plus
   the Wall's +1 is not a win. (Formats with winOnControlling5Sites.) And the Wall's extra site is not a
   battleground under Trail of Savagery 102_31 (the rider to Q-0054).
3. **Sticky Strands (Q-0065).** When the minion hindered is the one in the skirmish, the skirmish ends once the
   ability has been carried out, so the Strands are on the companion when it ends and he is weaker by 1 more
   (Aragorn 8 reads 6). And a companion who already bears a copy and was fighting Shelob gets no second copy
   that way: the limit is judged when the transfer is carried out, when Shelob is no longer fighting him.
4. **A transfer and a card's own cost text (Q-0053).** A plain statement of the card's own cost still applies
   on a transfer (Heavy Axe 15_7, "This is twilight cost -1 for each ..."); text about what it is played on
   does not (Asfaloth). Your answer to Q-0073 (printed cost in play) may mean Heavy Axe should cost its printed
   number on a transfer too.
5. **Maps and the bid (Q-0055).** The Map card reaches the support area after the bid; during the bid the
   client is told each player's Map through the pre-game information. The tests read the support area at the
   first decision after the bid.

**Answered 2026-10-06 (evening).** Ketura: "1- Very good. 2- winOnControlling5Sites is from the very obsolete 2nd edition; it's not worth considering in the future. 3- good 4- Based on a strict reading of the twilight cost rule, transfers should not take self-discounts into account. It's stupid and asinine, but it's RAW. 5- Right, that's just a detail about how it's presented in the client, since the support area doesn't render until after the bid. But the information is there all the same, which is the important part." So 1, 2, 3 and 5 stand as built; 4 changes: a transfer costs the printed number, the card's own cost text included (Heavy Axe 15_7), which goes into the next engine pass.

### Q-0073 The twilight cost of a card in play whose OWN text changes its cost (met on Northern Signal-fire, Flame of Amon Din 103_28)

"...an unbound Man costing X". Eomer, Third Marshal of Riddermark 4_267: "While you can spot a [rohan] Man,
Eomer's twilight cost is -1", and in play he is himself a [rohan] Man. Other Men are written the same way
(Eomer 6_92, 7_227, 7_365; Theoden 7_254, 8_92; Rohirrim Soldier 15_138).

- **A (the printed 3; what this card does today):** a card's cost in play is the number printed on it.
- **B (his text is a standing "While", so in play he costs 2):** CR 4.2 limits only cards that change ANOTHER
  card's cost to the moment it comes into play, and says modifiers are reapplied each time a value is used.

The game is of two minds (this card reads the printed number; the engine's own current cost for Eomer reads
2). The answer decides what F-0109's fix does with a card's own cost text, so it is wanted before that fix.
The test takes A and is active.

**Answered 2026-10-06 (evening).** Ketura: "Gah, this is why the Decipher rule is so *stupid*, because of course the logical and common-sense way to handle this is to let the game text have an ongoing effect on the twilight cost! For now, we will follow the rules as written, but I'm intending to change that written rule." A: in play a card costs the number printed on it, its own cost text included.

### Q-0071 Pippin, Trollslayer (103_31): does "lose all keywords" reach the minion's race?

"...make a minion he is skirmishing lose all keywords until the regroup phase." CR 4.2 calls race an "unloaded
keyword" (and card type and class "loaded keywords"); Decipher's card of this kind says "lose all game text
keywords", which keeps race out. Pippin says "all keywords".

- **A (the game, and the active test):** the keywords of the text box and those other cards give; the minion
  keeps its race.
- **B (the glossary's letter):** race goes too (a stripped Orc is no Orc until the regroup phase), and to the
  same letter so would the card type, which nobody can have meant.

Say only if B is meant, or if the text should read "game text keywords".

**Answered 2026-10-06 (evening).** Ketura: "wow, race *is* an unloaded keyword. It's almost always explicitly called out as an exception in these situations...treat it as if the text says '(except Race)' at the appropriate spot, updating the text as well. I'll alter the image." A; the game text gains "(except race)".

### Q-0072 Tormented Revenant (103_32): "To play, spot or add 2 threats" when both can be done

With 2 threats to spot and room for 2 more, may the player add 2 instead of spotting? Adding has uses (Ethereal
March, Ethereal Banner).

- **A (the text):** the player chooses. Every Decipher card with this shape of sentence is built as a choice
  (Elendil 13_65, Desolation Orc 15_107, Back to the Light 18_6); the "or" is not in parentheses, so your
  mandatory-branch ruling does not reach it.
- **B (the game):** the threats are spotted whenever 2 are there and the player is never asked.

One test is active on B and one parked on A (it passes with the cost rebuilt as a choice).

**Answered 2026-10-06 (evening).** Ketura: "Yeah, go ahead and set it up as a choice." A.

### Q-0041 An ability with a cost that has reached its limit: offered, or withheld?

Your cost exception to Q-0034 says an optional action with a cost is offered even when its effect will do
nothing. Rivendell Gateway (101_59): "Fellowship: Exert an unbound companion to make one of another culture
strength +1 until the end of the turn (limit +6)." Once +6 has been given the game no longer offers the action
at all; your old test asserts that, and the new one kept it. CR 4.2 "limit" has the action still usable, the
excess ignored ("no more may be added, and the last +1 is ignored").

The game is not consistent about it: 42 definitions withhold the ability at its limit (a `perTurnLimit` or
`perPhaseLimit` requirement), and the rest cap the effect and leave the ability on offer.

- **A:** a limit is its own case; withholding at the limit is fine and stays (the endless-pointless-click worry
  of Q-0015).
- **B:** the cost exception applies: an ability with a cost stays on offer at its limit, and the 42 are findings
  to fix as their sets come up.

Nothing is changed meanwhile.

**Answered 2026-10-06 (evening).** Ketura: "So. I think that this is a prime candidate for ensuring the 'noeffect' flag is set when the limit is reached. Technically speaking it should be offered, and some cards (Beneath the Mountains) are famous for using the cost over and over in spite of the effect limit being reached." B with the new no-effect flag: an ability with a cost stays on offer at its limit and is flagged as having no effect. The definitions that withhold at the limit are converted with the flag's arrival.

### Q-0042 Breaching Shot (102_21): may it assign an Uruk-hai to an ally who is away from home?

"... to assign an Uruk-hai costing X or less to an ally or unbound companion." The game lets any ally be chosen,
and the ally then fights that skirmish; the definition has a flag (`ignoreAllyLocation`) that only this card
sets, so it looks deliberate. The test asserts it. Say only if an ally must already be able to skirmish.

**Answered 2026-10-06 (evening).** Ketura: "Yes, the intent is that Shadow may target down a pesky ally regardless of skirmish eligibility. (They are breaching the wall past where people thought they were safe.)" As the game has it; nothing changes.

### Q-0043 Storied Homestead (13_76): is a response to "a special ability is used" still offered once that ability is cancelled?

Its printed "Response: If a minion's special ability is used, discard this condition or remove 2 [gondor]
tokens from here to cancel its effect" is built as an optional trigger. Rebuilt as a real Response it is
offered a second time against the ability it has just cancelled (discarding itself is still payable); as a
trigger it is offered once. It was left a trigger. For set 13: should a cancelled ability stop drawing
responses (an engine change), or should Homestead stay a trigger? Nothing changes meanwhile.

**Answered 2026-10-06 (evening).** Ketura: "It should not be; normally when responses react to a trigger, they should stop reacting if that trigger is prevented by a prior trigger. This is likely why it was built the way it was, but strictly speaking it should be a response that then fizzles after another copy (or itself) cancels it." Engine: a cancelled ability stops drawing responses; Storied Homestead is then rebuilt as a Response.

### Q-0044 Isengard Grunt (102_26): "prevent an [isengard] Orc from being overwhelmed unless its strength is tripled", with other minions in that skirmish

Every other card with this wording is a Free Peoples card, where a side is one character. This is the only
minion that has it, and with two minions on the Shadow side the text does not say what happens.

- **A (the game):** by side. One protected minion raises the whole side's threshold to triple the side's total.
  A Grunt (5) protecting itself beside an Uruk Savage (5) against Aragorn at 20 (exactly double their 10): both
  live. So a card naming "an [isengard] Orc" keeps an Uruk-hai from being overwhelmed too.
- **B (word for word):** only the Orc is spared, and only while the other side is under three times that Orc's
  own strength; every other minion is overwhelmed at double. On that board both would die.
- **C:** the threshold is triple the side's total, but only the protected Orc is spared.

B and C need the engine to resolve an overwhelm minion by minion. Every test that resolves an overwhelm keeps
one minion on the Shadow side, where all three agree; nothing is parked.

**Answered 2026-10-06 (evening).** Ketura: "Hoo boy. I think the right call is B, which is going to require some alterations to the overwhelm code and some mechanic tests besides." B: only the protected Orc is spared, by its own strength; overwhelm is resolved minion by minion.

### Q-0045 Wolf Pit (102_36): how is "you may stack two [isengard] mounts here from your draw deck or discard pile" carried out?

The game takes the mounts one at a time, asking for a pile each time, and each is optional: one may come from
the deck and one from the discard pile, and a player may stack just one from a discard pile that holds two.
Your old tests assert this ("We now optionally do it all again a second time"), and the new ones keep it. The
other reading is one pile for both mounts, or "two" meaning two when two are there. Say only if that is wanted.

**Answered 2026-10-06 (evening).** Ketura: "I implemented it the way it should work, with both cards being independent choices." As the game has it.

### Q-0046 Royal Guard (102_54): does "until the regroup phase" govern the strength as well as valiant?

"Skirmish: Exert this ally to add his strength to a skirmishing Hobbit and make that Hobbit gain valiant until
the regroup phase."

- **A (the game, and the tests):** only the keyword. The added strength is for that skirmish; in a later
  skirmish the Hobbit is back at his own strength.
- **B:** both last until the regroup phase, which matters when the Hobbit skirmishes again (a fierce minion).

The six cards worded "make X strength +N and <keyword> until the regroup phase" give both the same duration,
but each has one verb with two complements; this card has two verbs, with the phrase inside the second.

**Answered 2026-10-06 (evening).** Ketura: "A." Only the keyword lasts until the regroup phase.

### Q-0047 Now for Wrath (102_51): does a [rohan] item or ally discarded from the draw deck add a token?

Text (revision 1): "Each time a [rohan] item or [rohan] ally is killed or discarded from play or from hand
(except during the fellowship phase), add a [rohan] token here." The game also counts a discard from the top of
the draw deck. You added that on purpose (408b0e724, "not catching cards discarded from hand or deck") when the
text said only "is discarded or killed"; the rewording that brought in "from play or from hand" (a9b499a58) says
"no change to functionality".

- **A (the game):** the deck counts, and the text wants "or from your draw deck".
- **B (the text):** play and hand only, and the deck trigger goes.

Both readings have a test; the game's is the active one.

**Answered 2026-10-06 (evening).** Ketura: "Ah piss. The change was meant to prevent deck discards specifically. 'From play or from hand' is an exact listing of all sources which should be honored; from deck should not trigger this card. B." B: the deck trigger goes.

### Q-0048 Now for Wrath (102_51): only your own [rohan] items and allies?

The text names no owner; the game counts only the card owner's (you added "your" in 2ed6651f6, "triggering on
opponent's discarded cards"). The one reachable case of the other reading is the Shadow player discarding a
[rohan] item or ally from his own hand on your turn.

- **A (the game):** yours only, and the text wants "your".
- **B (the text, cards are code):** anyone's.

Both readings have a test; the game's is the active one.

**Answered 2026-10-06 (evening).** Ketura: "The text should have been updated to 'your'. Nonetheless in this case let's keep it as if it were written as 'your'; update the text accordingly and I'll update the image used." A: yours only; the game text gains "your".

### Q-0049 Now for Ruin (102_52): what does "Each time there are 3 [rohan] tokens here" mean once the third token has come and there was nothing to play?

The tokens rightly stay at 3 (playing from the discard pile is a cost). The game then looks again each time a
token is added here, 3 or more counting: a 4th token plays a card that has reached the discard pile meanwhile,
and all 4 are removed. A card reaching the discard pile, or a token added elsewhere, sets nothing off. The
other readings: exactly 3 (a 4th token kills the card for good), or a standing condition (a [rohan] event
reaching the discard pile triggers it at once). The tests take the game's reading. Say only if it is wrong.

**Answered 2026-10-06 (evening).** Ketura: "The current implementation is correct."

### Q-0050 A required "spot X to Y" trigger: may the player decline to spot?

Now for Ruin (102_52): "Each time a minion is killed, spot 2 valiant companions to add a [rohan] token here."
CR 4.2 "spot": "Normally, you don't have to spot all the cards in play that meet the requirement if you don't
want to." The game spots unasked, so the token always arrives; on this card that matters, because the third
token forces a play from the discard pile and lets the Shadow player play a minion from his.

- **A (the game):** not a choice; a required trigger's spot is made whenever it can be.
- **B (the rule):** the player is asked and may leave it.

One answer would cover every required "spot X to Y" trigger (Fires of Industry 102_23 is another).

**Answered 2026-10-06 (evening).** Ketura: "A" A required trigger's spot is made whenever it can be; not a choice.

### Q-0051 Trapped in its Gaze (102_66): "If that minion wins its skirmish" — this skirmish, or that minion's own?

"Exert a Wraith to make a twilight minion strength +2. If that minion wins its skirmish you may exert it to add
a burden." The event can be played on a twilight minion that is not in the current skirmish. Every Decipher
card of this shape says "wins this skirmish" (five of them); this is the only card that says "its".

- **A (the game, and the active test):** "its" means "this". The offer ends with the skirmish the event is
  played in; a minion pumped during another's skirmish that later wins its own is offered nothing.
- **B:** the offer waits for that minion's own skirmish later in the turn (the +2 still ends). The added
  trigger would need a longer "until" and one test's last stage would change.

Tests take A; nothing is parked. Say only if B is meant (or if the text should read "this skirmish").

**Answered 2026-10-06 (evening).** Ketura: "A. It should have been worded 'to make a skirmishing twilight minion'." A. Asked back whether the text and the choice change to a skirmishing twilight minion: "Yeah go ahead and change the text + implementation, and I'll update the physical text."

### Q-0052 Eastemnet Hills (102_67): "each player may discard 2 cards to draw 2 cards" — from hand?

CR 4.2 "discard": "The default meaning of the word 'discard' is 'discard from play.' Discarding from other
locations (such as from your hand...) is always specified." The text specifies nothing; the game discards from
hand, and every other card of this shape says "from hand". The tests take from hand and pass.

- **A:** from hand is meant; the displayed text should gain "from hand" (a gametext correction to the card).
- **B:** from play is meant (2 of your cards on the table to draw 2).

**Answered 2026-10-06 (evening).** Ketura: "Oh shit, how did we miss that. Yes, it's intended to be 'from hand'; let's keep that implementation and I'll update the wording." A: from hand; Ketura updates the wording.

### Q-0053 Does a twilight cost modifier reach a transfer? (mechanic; met on Westfold Plains 102_69)

Westfold Plains: "Each mount is twilight cost -1." A Swift Steed (printed 2) transferred between two Riders in
the fellowship phase costs 1 at the Plains and 2 elsewhere. The game prices a transfer exactly as it prices a
play, so this covers every cost modifier on possessions and artifacts (Rohirrim Road's +2, Strong and Old
7_122, Ordulus 54_80, Taurnil 55_13 and so on).

- **A (the rules read literally):** CR 4.2 "twilight cost": "Cards already in play that affect another card's
  twilight cost have that effect only when the card to be modified is coming into play"; CR 4.2 "transfer":
  a transfer pays the twilight cost again and "is not playing that card". So a transfer costs the printed
  number, and the game's transfer pricing is an engine bug.
- **B (the game):** "paying the twilight cost again" means what the card would cost to play now, modifiers
  included.

One test is parked on A in 102_69's file (AMountTransferredAtThePlainsCostsItsPrintedNumber); under B its 3
becomes 2 and it goes live. Either way the answer belongs in a mechanic test.

**Answered 2026-10-06 (evening).** Ketura: "...well shit, I hadn't ever quite put two and two together on that. That 'twilight cost isn't reduced except on play' rule is bullshit; why would twilight be treated differently from any other ongoing numeric stat modifier? But, it is on the books. I think it's clear that transferring 'is not playing' and so specifically does not count as 'coming into play' for the purposes of having the twilight cost altered. So option A. We're going to have to alter Asfaloth as well, as niche as that interaction is." A: a transfer is not priced by other cards' cost modifiers. Engine change.

### Q-0054 Breached Wall (102_70): what does "The count of sites each Shadow player controls is +1" reach? (F-0085)

With no site really controlled:

- **A (the old test's reading, and the tests as written):** every answer about controlled sites goes by the
  count. At the Wall "While you control a site" holds (Grousing Hillman +3 and fierce), "Spot a site you
  control" can be done (Hill Chief), "While no opponent controls a site" fails (Haldir loses +2), and "for
  each site" reads one more.
- **B:** only numbers change ("for each site you control", "while you control 2 sites"); a card that asks
  whether there is a site at all, or spots one, still needs a real site.

Under both, a card that needs the site itself (stack on it, play on it, liberate it) needs a real one.
The game is inconsistent today (F-0085): Haldir 4_71 / 54_71 and Last Throw 10_34 go by the count; Vorondil
7_127, Horseman of the North 15_130, Sturdy Shield 15_141, Gondor Still Stands 7_95 and White Hand Intruder
17_127 look for a site card and never see the +1. Under A those five change; under B the count-built ones
do. Rider: with Trail of Savagery 102_31 ("Each site you control gains battleground"), is the extra one a
battleground for Uruk Occupier 102_34 and Legion of Isengard 102_27? The game says no.

**Answered 2026-10-06 (evening).** Ketura: "So. The original design intent was a little convoluted I think, where it wanted to permit cards like Ready to Fall to increment but not affect Grousing Hillman or Gondor Still Stands or whatnot. But I'm going to make an executive decision: all the cards you mention and those using the same language should be retrieving the count of controlled sites, going from 0 -> 1 and enabling/disabling their text accordingly. So A." A: every "control(s) a site" reading goes by the count. The filter-built definitions are converted (F-0085).

### Q-0055 The V2 Maps (102_74, 102_75, 102_76): does a Map do anything in play that its text does not say?

The skills' own format notes (test-generation/references/format-rules.md) say "Maps choose site 1"; neither
rules document has an entry for the Map card type. In the game a Map is put in its owner's support area before
site 1 is played and restricts the adventure deck at validation; site 1 is then the first player's, as in any
game. If that is all a Map does, the note gets reworded and there is nothing more to test. If a Map is meant
to decide something about site 1 (which path, or whose site), the game is missing a rule. Nothing is parked.

**Answered 2026-10-06 (evening).** Ketura: "Ah damn, we really should have a CR 5.0 entry for this. So PC-Movie operates under Multipath rules, which is a community-built concept where you can bring any legal sitepath, but only include cards from that particular path, no mix-and-matching. All the Map does is announce which path the player is using, and it is revealed in the player's support area before the game begins, so it is information available during bidding. Maps therefore have no ongoing in-game effect, they are purely information repositories." A Map does nothing in play; the format note is reworded.

### Q-0056 Desolation of Saruman (102_72): may it discard a controlled site? (F-0093)

"When the fellowship moves to this site, each player may discard a card from their opponent's support area."
A controlled site lies in its controller's support area and stays its owner's card (CR 4.2 "control").

- **A (the text as written):** a controlled site is a card in the opponent's support area, so the Free Peoples
  player may discard any site the Shadow player controls. Then where does it go? Today a discarded site goes
  to its owner's discard pile and leaves no site of that number on the path.
- **B:** a site is not a thing that is discarded (the rules only liberate or replace one); nobody is offered
  one.

The game does neither: it goes by owner, so the Free Peoples player is offered controlled sites that came from
the Shadow player's adventure deck and not his own, and the Shadow player is offered a site he himself controls
if it came from the Free Peoples player's deck (site 1 always does), which discards his own controlled site.
That last part is wrong under both readings (F-0093, one test parked); your answer picks the filter. No other
card discards "a card" of no stated type from a support area.

**Answered 2026-10-06 (evening).** Ketura: "Excellent catch; controlled sites should not be discardable and should be excluded." B: no site is offered to either player (F-0093).

### Q-0057 Phial of Galadriel (103_1), Sting (103_117): "play this on the new Ring-bearer" when it is already in play

"Response: If The One Ring is transferred, play this on the new Ring-bearer. You may use this ability from your
discard pile." The Phial can be in play on another Ring-bound Hobbit when Frodo dies and Sam takes the Ring
(Bilbo 12_119 or 13_142).

- **A (the game, and the active test):** it moves to Sam as a transfer by an effect: no twilight, not a play.
- **B:** "play" means play: its (1) is paid and anything that answers a card being played is set off.
- **C:** a card in play cannot be played, so from play the ability does nothing and is not offered.

The rules do not cover playing a card that is in play. Tests take A; nothing is parked.

**Answered 2026-10-06 (evening).** Ketura: "This one is hampered by the lack of text space. Let's do this: treat it as a transfer if the card is in play, and a play if the card is in the discard pile." A, as the game has it.

### Q-0058 Wizard's Stash (103_7): are "Exert bearer and discard this" costs?

"Regroup: Exert bearer and discard this. This phase you may use special abilities on pipes as if it were the
Fellowship phase." The first sentence has no "to".

- **A (the game, and the active test):** costs. With the bearer exhausted the ability is not offered.
- **B (by the letter; costs come before "to"):** effects. The ability is always offered in the regroup phase;
  an exhausted bearer does not exert, the Stash is still discarded and the pipes are still opened.

The game's own practice is split on this shape: Fortress Never Fallen 4_276 / 54_276 is built as effects only,
From Deepest Dungeon 102_39 as cost-to-effect. Also on this card, by the letter and asserted in a test: "Bearer
must be a Wizard" has no "your" or "companion", so the opponent's Saruman minion may bear it (reachable through
Merry's Pipe 103_113 in the regroup phase). Say if that is not meant.

**Answered 2026-10-06 (evening).** Ketura: "Correct, they are costs; A. The standard 'to' formulation did not really work ('Exert bearer and discard this to....' what? 'Make it so...?'). As for the bearer restriction, ha, that's just how it works, then. I would love it if something could steal the stash for Saruman's sake..." A; the opponent's Saruman may bear it.

### Q-0059 Reconcile: are a player's hindered cards restored before the discard, or after? (mechanic; met on Words of Power 103_8)

CR 5.0 "reconcile": "When any player reconciles (before they discard any cards from hand), restore all cards
owned by that player which would be active upon restoration." The game restores after the discard and the
draw, and at/RestoreTests asserts that (OnlyPlayerReconcileRestoresPlayerHinderedCards,
ArtificialReconcileTriggersRestore: still hindered while the discard is asked, restored once it is answered).
Text and existing test disagree; it matters for a restored card that changes hand size or answers a draw or a
discard from hand. One test is parked on the text's reading in 103_8's file; the old tests stay as they are.

**Answered 2026-10-06 (evening).** Ketura: "Hrrm. Take a stab at fixing this one; if I implemented it as following the discard/draw, it was because I couldn't fit in a mechanism for doing it before the discard." The text's reading: restore before the discard. Engine change to be tried.

### Q-0060 Hinder: may a card that is already hindered be chosen to be hindered? (mechanic; met on You Cannot Enter Here 103_9)

The game leaves hindered cards out of every hinder choice, on purpose (GameState.canBeHindered: "Cannot hinder
if it is already hindered"). CR 5.0 "hinder" does not say so, where "restore" does say a card that is not
hindered cannot be restored. By the letter a hindered card could be chosen and nothing would happen (so an
event could be spent on its cost alone). The tests take the game's reading. If the game is right, CR 5.0 wants
a line; say only if the letter is meant.

**Answered 2026-10-06 (evening).** Ketura: "Ha. Strictly speaking, it should be legal for YCEH to target a hindered Shadow card, since its targeting profile works on a hindered card. However, nobody is going to play the card and pay the exertion for that result, so for QOL purposes let's continue to leave hindered cards out of the filter." As the game has it.

### Q-0061 Shadowfax, Swiftest of All (103_5): "Shadow cards they bear" — each minion's own, or all of theirs together?

"...minions skirmishing bearer gain no strength or keyword bonuses from Shadow cards they bear." Two Southrons
are both skirmishing Gandalf; one bears Horn of Far Harad (103_54) and the Shadow player removes a threat to
make the *other* Southron strength +2 (or a [men] minion is made fierce by another minion's Poleaxe 11_95).

- **A (each minion and what it bears itself):** the other Southron does not bear the Horn, so it gets +2.
- **B (the game, and the active test):** the Horn is a Shadow card borne by one of the minions skirmishing
  bearer, so no minion in that skirmish gains from it; the threat is still removed.

Not in doubt and tested: a bonus crossing between a minion in Gandalf's skirmish and one outside it is gained
both ways. Under A the change is in the engine's cancel-bonus modifiers, not only the card.

**Answered 2026-10-06 (evening).** Ketura: "Excellent find, this is exactly the sort of edge cases I wanted you to find. When I try to unwrap 'they' into a fuller sentence in my head, I get 'Minions skirmishing bearer gain no strength or keyword bonuses from Shadow cards borne by minions skirmishing bearer', so I think B is the right way of it. This is complicated by the fact that 'they' is both plural in this case *and* singular gender-neutral, but I don't think that changes it." B, as the game has it.

### Q-0062 Is a play from hand as an effect treated as a cost, as one from the discard pile is? (F-0098; Show Them the Meaning of Haste 103_6)

"...to reveal another Free Peoples event from your hand. Play that event 3 times, then remove it from the game."
Haste is offered, and can be played, with no other Free Peoples event in hand that can be played, or with no
other card in hand at all: (3) is added, Gandalf exerts three times, the Shadow player is shown an empty reveal,
and nothing happens. CR 4.2 "effect": "If an action plays a card from your hand (or discard pile) as part of
its effect, then that card must be in your hand (or discard pile) before you can begin to perform that action."
Your ruling on F-0046 named the discard pile; your ruling on Q-0034 has events offered whenever their own
requirements and costs are met, duds included.

- **A (the CR sentence reaches the hand too):** Haste is withheld unless another Free Peoples event that can
  be played is in hand. A definition requirement does it (tried: one test parked on this passes).
- **B (the dud-event exception wins):** Haste stays as it is; the parked test goes.

One answer covers every event that plays a card from hand as its effect.

Rider, the dead pile (I Call on You 103_24: "...add X threats to play a Wraith from your dead pile"): the
definition withholds the event with no Wraith in the dead pile and your old test asserts that
(ICallOnYouCannotPlayWithoutWraithInDeadPile). Does the dead pile go with the discard pile? If it does, the
event should also be withheld when its own threats leave no room for the threat the Wraith adds to be played
(F-0107; your comment in the old test says as much; needs a "with threats added" playability check in the
engine). If it does not, the event is offered regardless and the requirement comes out.

**Answered 2026-10-06 (evening).** Ketura: "RAW, yes, since it plays a card from hand it should enforce a card being there and playable before playing STTMOH. This however is complicated by the fact that we are playing it *three* times, and I have been unable to adequately get Gemp to be able to process 'theoreticals' for pre-calculating circumstances like this; I don't think the architecture as it stands is able to. But yes, we should at least confirm that the first play is possible. For ICOY, yes, I took the dead pile to be covered for the same reasons that the discard pile is by inference. I also agree that it should properly withhold itself if the threat cost of the wraith to be played cannot be paid." A: a play from hand or from the dead pile as an effect is treated as a cost; Haste requires that the first play is possible (F-0098); I Call on You is withheld when the Wraith's threat cannot be added (F-0107).

### Q-0063 Show Them the Meaning of Haste (103_6): is a second copy "another Free Peoples event"?

By the text "another" leaves out only the card being played, so a second copy in hand could be revealed and
played three times, each play revealing and playing a further event three times. The definition filters copies
out by title, deliberately by the look of it. One test is parked on the text's reading. If copies are meant to
be barred, the text wants a word; if they are allowed (and Q-0062 is answered A), two copies would each be what
makes the other playable, which the engine would have to guard.

**Answered 2026-10-06 (evening).** Ketura: "Yeah, the 'another' reading is definitely a bit strained, but I wanted to avoid double-comboing itself to play 3 events a total of 9 times (or even worse). Leave that title restriction in place." As the game has it; the parked test goes.

### Q-0064 Sticky Strands of Spider-silk (103_14): may it be moved on from a companion who bears it?

"...transfer this to that companion", with no "from your support area" (you asked on Q-0022 that "transfer
this" cards be put to you one by one). The game lets a copy on one companion be moved to another companion who
is skirmishing a [gollum] minion, for a hindered [gollum] card and no twilight; the active test asserts it.
Say if only a copy in the support area may be transferred.

**Answered 2026-10-06 (evening).** Ketura: "Yes, the intent is that it can be pulled from other companions if so desired." As the game has it.

### Q-0065 Sticky Strands (103_14): the [gollum] card hindered as the cost is the very minion in the skirmish

- **A (the game, and the active test):** hindered, the minion leaves the skirmish, which ends before the
  transfer is carried out; the companion is no longer skirmishing a [gollum] minion and nothing moves.
- **B (the letter):** the companion was spotted before the cost was paid and is still "that companion", so
  the Shadow player can end a skirmish he is losing and stick the Strands in one action.

**Answered 2026-10-06 (evening).** Ketura: "I would argue that since the companion was spotted as part of the cost *before* the minion was hindered, and then the effect is to transfer it to 'that companion', i.e. the one that was spotted, the timing works out. B." B: the Strands still go to that companion. The game is to be changed.

### Q-0066 Two-faced (103_15), Two Minds of It (103_16): is the replacement offered when nothing in the discard pile could replace the minion?

"Then you may spot a [gollum] minion and remove a threat to replace it with another minion from your discard
pile with the same title." With a [gollum] minion in play and a threat, but no minion of that title in the
discard pile:

- **A (the game, your old test TwofacedReplacementNotOfferedWithoutValidDiscard by name, and the active test):**
  not asked; taking a card from the discard pile is treated like a cost, as playing from it is (F-0046).
- **B (Q-0034, anything with a cost is offered when the cost can be paid):** asked; the threat is removed and
  nothing is replaced.

One test is parked on B in each card's file. On Two Minds of It your old tests DoesNotExchangeIfNoSmeagolInDiscard
and DoesNotExchangeIfOnlyDuplicateSmeagolInDiscard take A as well; under B a definition-only correction works
there (tried). The answer also decides how the crash F-0100 is mended (under A the minion nothing
can replace is kept out of the spot; under B an empty choice does nothing).

**Answered 2026-10-06 (evening).** Ketura: "Let's treat this as if playing from discard for rules purposes; if it's not in the discard don't offer it. A." A; the parked tests go, and F-0100 is mended by keeping a minion nothing can replace out of the spot.

### Q-0067 Two-faced (103_15): what does the new minion take over, beyond "all tokens and borne cards"?

- **A (the game, your old test of the +3, and the active test):** the old minion's place entire: its
  assignment to a companion whose skirmish has not begun, and effects applied to the old minion (Gollum, Mad
  Thing's +3 until the regroup phase carries over). The engine gives the new card the old card's id.
- **B (the letter):** only tokens and borne cards; effects on the old card end with it.

Rider: may the replacement be a second copy of the very card replaced? The text says only "another minion ...
with the same title"; the definition also asks for a different subtitle, and the tests assert that. It matters
only under B.

**Answered 2026-10-06 (evening).** Ketura: "'Another' here is meant to cover that it is a different subtitle; no swapping the same persona for no real reason. But yes, A is the intended result, that for all intents and purposes, the minion simply 'becomes' another persona, without skipping a beat or interrupting any prior tracked effects." A, as the game has it; another subtitle is required.

### Q-0068 Ethereal Banner (103_21): which Wraiths does "exert a Wraith and a minion it is assigned to skirmish" reach?

"Each time bearer wins a skirmish, you may exert a Wraith and a minion it is assigned to skirmish." The text
has no "your" and nothing tying the Wraith to an assignment; the definition chooses one of your Wraiths, then
a minion assigned to it if there is one.

- **A (a pair):** the Wraith must be one assigned to a minion. Then the game is too wide (it exerts an
  unassigned Wraith alone), and one active test turns round.
- **B (do what you can):** any Wraith, and a minion assigned to it if there is one. Then the game is right
  for your own Wraiths but too narrow by the letter: "a Wraith" names no side, so a Shadow minion who is a
  Wraith (Corpse Lights, Marsh Wight, the [wraith] Wraiths) could be exerted alone. One test is parked on
  that; it passes with "your" taken out. If only your own Wraiths are meant, the text wants "your".

The tests take B for your own Wraiths.

**Answered 2026-10-06 (evening).** Ketura: "Wraith is Wraith; it was intended to be able to hit a Shadow wraith if they were there; since this is an effect, we do as much as we can, and if there isn't a minion assigned to skirmish that wraith, we shrug. This can be used strategically on unassigned Wraiths of your own to increase their enduring bonus, too. B." B, and a Shadow Wraith may be exerted: "your" comes out of the definition.

### Q-0069 Great Gate of Minas Tirith (103_23): is X capped at the number of other copies? (F-0106)

"Then you may exert X knights to hinder X other copies of that card with the same title." The game withholds
the offer when there is no other copy and caps X at the copies there are. By your ruling on Q-0034 the exertion
is a cost and should be offered whenever a knight can exert (Pippin, Trollslayer 103_31 is an enduring knight),
so the withheld offer is a finding. Is X then also free above the number of copies (extra exertions hinder
nothing), as Breaching Shot's X was? If yes, three active assertions turn round with the fix; if X stays
capped at the copies, only the no-copy case changes.

Also on this card, for your word (F-0105): it is built as a Response, so for one use of a Shadow ability it is
offered again and again while an unhindered fortification remains; making it an optional trigger gives one
offer per use (tried, one test parked).

**Answered 2026-10-06 (evening).** Ketura: "Hmm. I suppose that any number should be permitted; this should follow the UX pattern where the game permits you to select and deselect any number of knights before hitting 'done', that way mistakes or second thoughts can be rectified. It shouldn't be a response; I probably built it like that due to some restriction or other on a trigger, but it should be a trigger as the text uses." X is free up to the knights able to exert (F-0106); the card becomes an optional trigger (F-0105).

### Q-0070 Hinder: a card hindered in answer to its own ability that adds a token to itself (met on Great Gate 103_23)

Ships of Great Draught 8_65: "Shadow: Remove a threat ... to add a [raider] token here." The Gate hinders it in
answer.

- **A (the game, and the active test):** the ability is carried out to the end and the hindered Ships has its
  token (CR 5.0: "the effects of that text continue to be executed until fulfilled").
- **B:** CR 5.0 also says "Tokens on hindered cards cannot be spotted, counted, reinforced, removed, or added
  to", so no token. The engine would need a guard.

The two sentences pull apart on the kind of card the Gate is most used against. Say only if B is meant.

**Answered 2026-10-06 (evening).** Ketura: "The token should fizzle. The same logic is what makes Anduril, Legend Remade successfully block a wound; hindering before the token arrives blocks the token. B, as in this case can't > can; tokens cannot be added > continue to execute." B: no token. Engine guard.

### Q-0002 Do 7_303 "Orc Pursuer(2)" and 18_124 "White Hand Attacker(2)" carry the "(2)" on purpose? (F-0004)

The printed titles are "Orc Pursuer" and "White Hand Attacker", shared with 5_108 and 17_120, which are different
cards. The definitions add "(2)" to the later ones.

- Deliberate: keeps the two apart for the four-copy limit and for anything that looks cards up by name. Then the
  stat tests assert the title with "(2)" and the spreadsheet difference is expected.
- Not deliberate: the titles are wrong and name-based effects treat them as different cards when they should not.

**Answered 2026-10-04.** Deliberate. Decipher ruled the two Orc Pursuers separate titles; White Hand Attacker is the same case without a formal ruling. F-0004 closed as not a bug.

### Q-0003 Is 12_114 For the Mark a maneuver event? (F-0003)

The card is a Regroup event whose text lets it be played in the maneuver phase by exerting a [rohan] Man. Its
definition lists both Maneuver and Regroup as timewords.

- If it counts as a maneuver event for cards that care about maneuver events, the definition is right.
- If the second timeword is only how off-phase play was implemented, cards that look for maneuver events will
  wrongly find it.

**Answered 2026-10-04.** Timewords are an implementation detail, not a game mechanic; the extra timeword is acceptable. F-0003 closed as not a bug.

### Q-0004 Should the 26 PC errata test classes with no active test be switched on? (F-0006)

Every `@Test` in them is commented out. If these are current errata, they are untested; if they are earlier
iterations kept for the historical mode, they belong with the dormant files and the tracker should count them
there.

**Answered 2026-10-04.** On some errata the tests were commented out under time pressure. They are to be written, in the errata phase. (The finding first named 51_15, which was my mistake; that card is fine.)

### Q-0005 Are the Hobbit sets (30 to 33) and the RTMD set 90 meta-sites in scope?

255 Hobbit cards and 18 set-90 cards have definitions and no test files. The tracker leaves the Hobbit cards out
and lists set 90 at the end.

**Answered 2026-10-04.** Hobbit sets last of all, if ever: there is no text for them yet. Sets 91 to 94 are in; set 90 is purely visual and needs no tests.

### Q-0006 Is there a rules text for the PC's own mechanics (hinder, restore, and whatever V-sets add)?

Comprehensive Rules 4.2 predates them, and the only description in the repository is in the hjson-reference
skill, which documents the implementation. Every V3 card that hinders runs into this. Card 103_50's tests assume:
a hindered card stays on the table; its game text is switched off (a hindered Desert Wind Whisper gives no ambush
bonus); and hindering the just-played minion can always be fully performed, so "remove (4) or hinder" with less
than (4) in the pool hinders with no choice.

- If there is a document, it goes into the campaign's references and `rules.py` learns to search it.
- If not, a short statement from you of what hinder and restore do is enough to write the mechanic tests from.

One assertion in 103_50 is parked on this.

**Answered 2026-10-04.** The Comprehensive Rules 5.0 excerpts cover hinder, restore, relentless and the rest; `rules.py` searches them. The parked assertion in 103_50 is switched on.

### Q-0009 Does "a card stacked here" on Deepest Delvings include Shadow cards? (F-0008)

If the finding is confirmed and the filter is widened, should it be any card, or any Free Peoples card? Decipher's
own cards say "a Free Peoples card stacked here" when they mean the latter.

**Answered 2026-10-04.** The text is right and the implementation was not, so "a card" is any card. Fixed (F-0008).

### Q-0001 Which formats let a Ring-bearer's skirmish be cancelled?

You said the rule that the Ring-bearer's skirmish cannot be cancelled came in with the King era, so Fellowship and
Towers formats do not have it. `lotrFormats.hjson` sets `cancelRingBearerSkirmish: true` on `king_block`,
`king1_block`, `king2_block`, `rotk_sta` and `pc_king_block` as well, and sets it false on `ts_reflections`.

- If the file is right, the rule arrived with Reflections and the King *block* formats are deliberately exempt;
  the format-aware tests use `king_block` as an allows-it format.
- If your statement is right, those five King formats are wrong in the file and it is a data finding.

Until answered, format-aware cancel tests use `fotr_block` (allows) and `multipath` (refuses), which both readings
agree on.

**Answered 2026-10-05.** The rule came in with set 9, Reflections, whose non-Frodo Ring-bearers warp the format if
their skirmishes can be cancelled. King block is sets 7, 8 and 10, and there is no good reason for it to allow
the cancel; nobody noticed because King block is hardly played, and Movie block (1 to 10) has the rule properly.
Filed as F-0014; whether to change the five King formats is Ketura's call.

### Q-0007 Does Desert Wind Whisper count separate instances of ambush together?

A minion with printed ambush (2) that gains ambush (1) from Desert Wind Stalker: is Whisper's bonus +3, or +2?
The rules call them separate instances and do not say how a card reading "ambush (X)" treats two. The existing
test (yours, December 2025) asserts they add, the two cards look designed as a pair, and the test stays active
on that reading. Low doubt; confirming it closes the question.

**Answered 2026-10-05.** It does not matter whether they are combined: a minion with ambush (2) and ambush (1)
would get +2 and +1, so offering the instances as one trigger does not change the result. The existing test's
reading (they add) stands.

### Q-0008 When Sam becomes the Ring-bearer, what carries over?

Two things no rule states outright, found while testing 101_54:

- "(resistance 5)": is it only stating Sam's resistance, or setting it to 5 on succession?
- Burdens: a probe shows they go from 2 to 0 when the Ring passes to Sam, and his resistance from 3 to 5. Is
  that right? Rules entries "burden" and "leaving play" lean that way but do not say so for succession.

No test asserts either yet. Eight official sets have a Sam with this response, so the answer becomes a mechanic
test.

**Answered 2026-10-05.** Resistance is a stat a companion always has, like strength or vitality. On the early
Sams it was dormant until he became the Ring-bearer; set 11 widened it so every companion has a resistance, always
reduced by the number of burdens, which matters at 0 only for the Ring-bearer. So "(resistance 5)" states Sam's
resistance and sets nothing. Burdens are tokens on the Ring-bearer, and when a character dies "any tokens on that
card are removed" (leaving play): Sam takes up the Ring with no burdens. The engine already does both. To be
written as a mechanic test.

### Q-0010 Three for the Elven-Kings (101_12): is the heal part of the "may", or its own required action?

"Each time the fellowship moves, you may spot an [elven] or [gandalf] ring to place a card from hand beneath your
draw deck and draw a card (and heal a companion if you can spot 3 [elven] or [gandalf] rings)."

- **As the sentence reads:** the heal is part of what using the trigger does. Decline the trigger and nobody is
  healed; accept it with 3 rings and you place, draw, then heal. Then the definition is wrong (it should be one
  trigger with a conditional heal at its end) and this is a finding.
- **What the game does, and what your existing tests assert:** the heal is a required action of its own on every
  move with 3 rings. It happens before the optional trigger is offered and whether or not that is used, so it
  cannot be declined and does not need a card cycled.

Why it is a question and not a finding: the old tests assert the game's order on purpose (their accept lines are
commented out, and the heal trigger has `optional: false`). Why it may be half an edit: an earlier wording said
"If you can spot 3 ... rings you may also heal a companion", the heal trigger still carries a "Would you like
to..." prompt that a required trigger never shows, and every other card in the pool shaped "you may ... (or/and
... if ...)" has its parenthesis inside the "may"; this is the only one built as a trigger of its own.

The existing reading stays active. A test of the other reading is written and parked
(`WithThreeRingsTheHealIsPartOfTheTriggerAndIsDeclinedWithIt`), ready to go live if you rule that way.

**Answered 2026-10-05.** A separate may: its own choice of none or one companion, not a yes-or-no prompt. The definition was changed to ask for none or one, and the tests assert it.

### Q-0011 Let Fly the Darts of Lindon (101_10): two things about "lose archer ... until the regroup phase"

"Archery: Exert bearer to make all Free Peoples archers lose archer and gain damage +1 until the regroup phase."

1. **Is the set of archers fixed when the ability is used?** CR 4.2 "event" says some *event cards* take a
   snapshot of what they affect. This is a special ability with the same wording. The game takes a snapshot (a
   character who becomes an archer afterwards keeps archer and gains nothing), and an active test asserts that,
   reading the rule across from events. Confirm, or say that a special ability keeps applying.
2. **Can someone who lost archer this way be given it again before regroup?** The game says no: an Elven Bow put
   on Legolas after the use leaves him not an archer. CR 4.2 says "cannot gain" only of losing *all damage
   bonuses*, and where Decipher meant it elsewhere the text says "and unable to gain". No test asserts either.

The first matters beyond this card: every "make each/all X ... until Y" ability raises it, and in Gemp the
snapshot is written into each definition, not the engine, so each such card needs its own test of it.

**Answered 2026-10-05.** (1) The snapshot holds for this ability. (2) "Can't" beats "can", so "lose" beats "gain": he cannot be given archer again before regroup. Both are asserted by active tests.

### Q-0012 Library of Rivendell (101_11): may a Shadow artifact in your own hand be stacked?

"Fellowship: Stack your tale or artifact from play or from hand here..." names no side, and there are Shadow
artifacts (The Palantir of Orthanc, Ithil Stone). The game lets one be stacked from hand, which turns a dead card
into a draw and adds nothing to X. A test asserts that and is active. If you meant Free Peoples cards only, the
from-hand filter needs a side and the test is inverted.

**Answered 2026-10-05.** As written it permits a Shadow artifact, so it stays: cards are code. An erratum if anyone objects.

### Q-0013 Is a draw deck reshuffled after a card has looked through it, when the card does not say "shuffle"?

Raised by Counsel of the Wise (101_13), "reveal an [elven] ally from your draw deck"; the same shape is in 53_87
The Dark Lord Advances and 52_45 Too Much Attention. Their definitions show the whole deck to the player in order
and leave the order as it was (`shuffle: false`). In the official definitions `shuffle: false` appears only on
top-of-deck reveals, never on a search of the whole deck.

- CR 4.2 "draw deck" says "When you finish looking through it, reshuffle it". If that holds for any look through
  the deck, these three definitions are wrong and the player is learning their deck's order.
- If that sentence is only about playing a card from the draw deck, the definitions are right.

No test asserts either: the rig cannot yet see a shuffle (rig request G-0011).

**Answered 2026-10-05.** Always reshuffled: "When you finish looking through it" is the timing. The three definitions were corrected (F-0034).

### Q-0014 Out of Dark Waters (101_34): may a tentacle killed by the wound be stacked?

"Each time a tentacle takes a wound, you may stack that tentacle here." A tentacle on its last point of vitality
takes a wound.

- **A.** The wound was taken, so it may be stacked, and ends on the condition and not in the discard pile. This is
  what the game does, on purpose: the tentacle is killed and put in the discard pile, the offer is then made, and
  accepting stacks it from the discard pile. The definition has a branch for exactly this, and the old test
  asserted it. The vitality-1 tentacles (Foul, Writhing) can only ever be stacked this way.
- **B.** A character at zero vitality is killed at once, ahead of anything optional (CR 4.2 "killed"; the Cave
  Troll's Chain ruling), so by the time the "may" comes round the tentacle is not in play and "that tentacle" is
  nothing that can be stacked.

The test takes A and is active (`TentacleKilledByTheWoundMayStillBeStackedHere`). A rider, settled by the rules
unless you say otherwise: with two copies in play, both offer; once one copy's offer is taken the other's does
nothing, since a stacked card is not in play (CR 4.2 "stack", "active").

**Answered 2026-10-05.** A: being killed does not preclude being stacked afterwards by another card's text. The test is active and stands.

### Q-0015 I Would Have Gone With You to the End (101_23): may its skirmish action be used for X = 0?

"Skirmish: Spot Aragorn and discard X cards here to make a skirmishing companion with the Aragorn signet
strength +X (limit +3)." The definition requires a card stacked here and its range starts at 1, so with nothing
stacked the action is not offered. Your old test asserted exactly that ("no stacks yet").

Your ruling on 101_5 was that X may be zero everywhere but "exert X times": "every other case has no such guard
rail". This definition has one. A free do-nothing skirmish action is not quite nothing: it lets the Free Peoples
player make the Shadow player act or pass first.

- **A.** The ruling governs: drop the requirement, range from 0. A finding on this card.
- **B.** Not offering a do-nothing use is deliberate (a quality-of-life departure, like Goblin Runner's).

Other definitions of the same shape go both ways. Zero allowed: The Trees Are Strong 3_73, Fool's Hope 57_35,
Black Sails of Umbar 8_50, Grond 8_103, From Deepest Dungeon 102_39. Floor of 1: Breaching Shot 102_21 ("Remove
X tokens from machines"), Northern Signal-fire 103_30 ("Hinder X beacons and discard X ..."). One answer would
settle all three floor-of-1 cards.

The old reading is kept active; `SkirmishActionCanBeUsedWithNothingStackedHereForXOfZero` is parked on this.

**Answered 2026-10-05.** B: a do-nothing X of zero is deliberately not offered (quality of life, and a griefing angle). The parked test goes; the three floor-of-1 cards are right.

### Q-0016 The White Council (101_18): what is "the first sentence of Saruman's game text" when it opens with keywords?

"While you can spot Gandalf and 2 [elven] allies, the first sentence of Saruman's game text does not apply."
Saruman, Of Many Colours (12_54) opens "Damage +1. Fierce. Lurker."; Agent of the Dark Lord (12_144) opens
"Lurker."; the follower Servant of Sauron (17_38) opens "Aid - (1)." The same phrase is Decipher's on Saruman's
Staff (4_174), so the answer covers that card too. Neither rules document says anything of "first sentence".

- **A.** To the letter: the first keyword is the first sentence (12_54 loses damage +1 and keeps the rest).
- **B.** Keyword lines are not sentences; the first full sentence after them stops applying.
- **C.** What the game does: nothing happens to a Saruman whose text does not open with a sentence.

No test asserts any of the three. A rider for when the Hobbit sets are done: Saruman, The White (32_24) is a Free
Peoples ally, so the Council's owner could switch off his own Saruman; the game leaves him alone.

**Answered 2026-10-05.** Already ruled: Legacy Ruling 6. The Staff, and so the Council, has no effect on 12_54 or 12_144 (reading C).

### Q-0017 Boromir, Redeemed (101_20): is he made defender +1 without asking?

"At the start of each assignment phase, make Boromir defender +1 if you can spot an unbound Hobbit or another
companion with the Aragorn signet." There is no "may", but the game offers it as an optional trigger, and the
old test accepts the offer. Revision 0 read "you may exert another companion ... to make Boromir defender +1",
and the definition still has `optional: true` from then, so this looks like a leftover. The old reading stays
active; `DefenderPlusOneIsMadeWithoutAsking` is parked and passes with `optional: false`.

**Answered 2026-10-05.** Automatic and required; the definition's `optional: true` was a leftover and is now false.

### Q-0018 Boromir, Redeemed (101_20): how long does the defender +1 last?

The text names no duration (revision 0 said "until the regroup phase"; the erratum dropped those words and
changed "the assignment phase" to "each assignment phase"). The definition still says until the regroup phase.

- **A.** Each assignment phase stands alone: it ends with the assignment phase it was made in, and the fierce
  assignment phase makes it again. Boromir is exactly defender +1 in every assignment phase.
- **B.** As the game does it: until the regroup phase. Then the offer comes again at the fierce assignment phase
  with the first +1 still on him and stacks to defender +2, and a Boromir whose only Hobbit died in the first
  round is still defender +1 in the fierce assignment phase.

Every other card of this shape names its duration. The old test takes B as far as it goes and stays active;
`DefenderPlusOneLastsOnlyForTheAssignmentPhaseItWasMadeIn` is parked and passes with `until: end(assignment)`.

**Answered 2026-10-05.** With no duration stated it lasts to the end of the current phase (A). The definition now says so.

### Q-0019 Just a Wisp of Cloud (101_28): with no Saruman that can be played, may the event be played for its cost alone?

"Discard a Free Peoples card from hand to play Saruman from your draw deck or discard pile" (his cost reduced
for each Crow). With no Saruman in either pile, or one there that the pool cannot pay for, the game does not
offer the event.

- **A.** As the game has it: no. The definition asks for it (`mustExist: true`), and so do exactly the eight
  definitions that play "from your draw deck or discard pile" (5_30, 7_53, 8_92, 8_118, 11_106, 17_17, 101_28,
  101_37), and none of the roughly 130 that play from the draw deck alone. It looks deliberate. CR 4.2 "effect":
  a card played from hand or discard pile as part of an effect must be there before the action can begin.
- **B.** Yes. The same entry leaves "playing a card directly from your draw deck" out of that exception, "draw
  deck" says there is no penalty for not finding a card, and your ruling on F-0022 (what follows "to" is an
  effect) leans the same way. The Shadow player could then spend the event just to discard a Free Peoples card
  from hand. B would reach the six Decipher cards too, and is more than deleting `mustExist`: the draw deck
  would then be offered with no Saruman in it.

Two active tests assert A; `CanBePlayedForItsCostAloneWithNoSarumanInTheDrawDeckOrDiscardPile` is parked.

**Answered 2026-10-05.** As the game has it (A), knowingly: by the rules it could be played at any time, but the current implementation "threads the needle of UX and rules". The parked test goes.

### Q-0020 Thrashing Tentacle (101_33): is the burden still added when the lost skirmish kills it?

Its "each time this minion loses a skirmish" trigger adds a burden. With vitality 2 and fierce it ordinarily dies
of its second loss. The wound for losing and the trigger are simultaneous required actions, which the Free
Peoples player orders; ordered wound first, the tentacle is in the discard pile when its trigger resolves.

- **A.** Yes, the burden is added: it was a losing character whatever became of it (CR 4.2 "skirmish phase(s)").
  This is what the game does, in either order.
- **B.** No: once it has left play its text is not active. The Plundered Armories and Cave Troll's Chain rulings
  put a death from losing ahead of *optional* actions triggered by the loss and say nothing of required ones.

Shagrat, Isildur and Shelob have the same shape of sentence and no ruling. The test asserts A with the wound
ordered first, and is active (`LosingTheSkirmishThatKillsItStillAddsABurden`).

**Answered 2026-10-05.** A: the burden is added.

### Q-0021 Their Eyes Fell Upon Him (101_41): is the "you may" draw meant to happen without asking?

"Each time the fellowship moves, you may spot a wound on the Ring-bearer to draw a card (or 2 cards if the
Ring-bearer is exhausted)." The definition has `optional: false`: the Shadow player is never asked and draws on
every move with the Ring-bearer wounded. The old tests assert the unasked draw.

- **A.** A deliberate shortcut, like Goblin Runner's: leave it.
- **B.** The text: the Shadow player is asked and may decline (to spare a thin draw deck, or keep his hand down).

Of 35 cards worded "Each time ..., you may spot X to Y", 32 are built optional, among them the two nearest in
shape (2_113 "Each time you play a tale, you may spot Bilbo to draw a card"; 3_71 "Each time the fellowship
moves, you may spot an exhausted companion to add (3)"). The other two built required are 68_50 (an erratum of
18_50, whose original is optional) and 13_75 (three triggers, two of them "you may"): one answer can cover them.

The old reading is active; `TheDrawIsOfferedAndMayBeDeclined` is parked and passes with `optional: true`.

**Answered 2026-10-05.** B: an oversight; the draw is offered. The definition is now optional.

### Q-0022 Pale Crown (101_39) and six others: may a "transfer this from your support area" ability be used once the card has left the support area?

"Skirmish: Remove (1) to transfer this artifact from your support area to a Nazgul." With the Crown already on
a Nazgul the game still offers the action: using it removes (1), asks for a Nazgul and moves nothing.

- **A.** As this definition has it: removing (1) is the whole cost, the transfer is the effect and simply
  cannot be done (your ruling on F-0022). Seven definitions behave so: 101_39, Feel His Blade 7_177, Too Late
  7_209, Lingering Shadow 12_166, Sauron's Gaze 12_169, Disarmed 18_46, Cleaved 18_114.
- **B.** Being in the support area is a requirement (CR 4.2 "transfer" speaks of a card's requirements on when
  it may be transferred). 21 definitions enforce it (Black Breath 1_207, Blade Tip 1_209, Lost in the Woods
  11_212, Black Dart 8_69 ...), and the PC's later cards write it out ("If this is in your support area ...":
  Inner Gate 103_25, War Howdah 103_62). Then the seven are findings.

By your ruling on Q-0015 a do-nothing use that can be clicked over and over is a griefing angle, which leans to
B; here it does cost (1) each time. The test is active on A
(`OnceOnANazgulTheCrownsSkirmishActionStillTakesItsTwilightTokenAndTransfersNothing`).

A small rider from Fell Voices Call (101_37): once a "play from your draw deck" effect is looking through the
deck and finds an eligible card, the game makes the player play one (the choice has a minimum of 1), where CR
4.2 "draw deck" speaks of a player who may "choose not to play" the card. No test asserts either.

**Answered 2026-10-05.** Where the card says "from your support area" it cannot be used from elsewhere: Pale Crown's ability now requires it. Cards that say only "transfer this" are case by case. The rider (being made to play the card found in the deck) "looks like a bug": F-0042.

### Q-0023 Gaze of the Eye (101_45): is a condition discarded because its bearer left play "about to be discarded"?

"Response: If your [sauron] condition is about to be discarded, remove a burden to prevent that." When the
character bearing a [sauron] condition is killed, or a minion bearing one is discarded by a card or at the end
of the turn, the condition goes with it and the response is not offered.

- **A.** As the game has it: that discard follows from the bearer leaving play and cannot be prevented (there
  would be nothing left to bear the condition).
- **B.** By the bare words it is about to be discarded, so the response is offered; a saved condition would then
  have no bearer, and the response would be offered at the end of every turn for each [sauron] condition on a
  minion.

Neither rules document settles it. The test asserts A and is active
(`IsNotOfferedForAConditionDiscardedBecauseItsBearerWasKilled`). Low stakes: say only if A is wrong.

**Answered 2026-10-05.** A: a discard that follows from the bearer leaving play is not responded to; there is nowhere for the saved card to go.

### Q-0024 When cards are revealed from a player's hand, is that player shown which they were?

Mirror of Galadriel (1_55) reveals 2 cards at random from the Shadow player's hand; A New Light (17_15, and its
erratum 67_15) reveals the Shadow player's hand after a search. The Free Peoples player is shown the cards. The
hand's owner is shown nothing: he is not told which two were picked. The old tests of all three had the Shadow
player dismiss a showing as well, which did nothing; the rig's new check (G-0013) caught it, and those three
stray lines are removed.

- **A.** As the game has it: the owner knows his own hand and need not be shown anything.
- **B.** A revealed card is shown to all players (CR 4.2 "effect"; your ruling on F-0021), and for a random
  reveal the owner does not otherwise know which cards the other player has seen.

Low stakes; nothing is parked on it.

**Answered 2026-10-05.** No pop-up for the hand's owner, but the game log must say which cards were revealed. Checked: it does. `RevealRandomCardsFromHandEffect` and `RevealHandEffect` each write a log line naming the cards, as links, to every player.

### Q-0025 Is a card taken from a draw deck "by type" to be shown to the other player?

CR 4.2 "effect" has a card that is moved between areas because of what it is (an [isengard] item taken into
hand from the draw deck, say) revealed, so that its type can be verified. On Counsel of the Wise the printed
word "reveal" was ruled to mean shown (F-0021). Where the text does not say "reveal" (Crows of Isengard 101_26,
Murder of Crows 101_29, and every "take a ... into hand from your draw deck"), the definitions set
`reveal: true`, which only writes a line in the game log; the other player is shown nothing.

- **A.** The log line is the reveal; leave it.
- **B.** The other player is to be shown the card, as on Counsel of the Wise. Then it is an engine change (what
  `reveal: true` does on a move), not card by card.

Nothing is parked on it and no test asserts either.

**Answered 2026-10-05.** A: the log line, with its link to the card, is the reveal.

### Q-0026 Bilbo, Of Bag End (101_50): may both items go on the same [shire] companion?

"... play 2 items from your draw deck on [shire] companions". The game lets each item go on any [shire]
companion, the same one twice included; your old test put Sting and the Phial both on Frodo. The other reading
is two different companions. The only other card of this shape, Gondor Calls For Aid! (103_83, "play X items
from your discard pile on your Men"), is built the same way as Bilbo. The tests assert the game's reading and
are active. Low stakes: say only if it should be two different companions.

**Answered 2026-10-05.** "Can go on multiple": each item goes on any [shire] companion, the same or another. The tests stand.

### Q-0027 Bag End Study (101_57) with a Hobbit Farmer in play: is its trigger offered once or twice?

Hobbit Farmer (1_295): "While you can spot your site 1, this ally has the game text of that site." With the
Free Peoples player's own Bag End Study as site 1 and a Farmer in play, the fellowship moving from the Study
gets two offers: the Study's and the Farmer's copy. Two [shire] companions may exert (or one twice) for two
[shire] conditions from the deck.

- **A.** Once: on an ally, "here" names no site the fellowship can move from. Your old test
  `HobbitFarmerDoesntCopyStudy` says so by its name, but its body never put a Farmer in play (its "farmer" was
  the Ring-bearer), so it has passed all along against a game that does B. On A the fault is one word: the
  Study's trigger is `movesFrom, filter: name(Bag End Study)` where every other "moves from here" site has
  `filter: self`; with `self` there is one offer.
- **B.** Twice, as the game has it: the Farmer has the site's text, literally (cards are code), as he does for
  every other site 1 (Green Hill Country's discount is counted twice with a Farmer in play).

The test of B is active; the test of A (`WithAHobbitFarmerInPlayTheStudyIsStillOfferedOnlyOnce`) is parked.

The same question on Lorien Throne Room (101_61) with Uruviel (1_67, who has the text of your site 6): "Each
time a companion exerts here, you may exert an [elven] ally to heal that companion (limit once per phase)."
There the game gives ONE offer, the site's: the definition asks `Location: self`, so Uruviel's copy never does
anything. So the two V1 sites that say "here" are built opposite ways. The engine's own stated convention
(the comment in Rivendell Waterfall 1_342, issue #1045) is that a site's name is something a copying ally can
satisfy and a self-reference is not, which makes Bag End Study's name filter the odd one out. One ruling covers
both: if "here" is the site alone, the Study changes to `filter: self`; if a copy counts, the Throne Room
changes and gives two heals a phase with Uruviel. The Throne Room's test of one offer is active.

**Answered 2026-10-05.** "Here" is a synonym for the site's title: an ally with the site's text doubles the benefit, at that site only. Bag End Study stands; Lorien Throne Room is corrected (F-0043).

### Q-0028 Rivendell Gateway (101_59): does "one of another culture" mean an unbound companion?

"Fellowship: Exert an unbound companion to make one of another culture strength +1 until the end of the turn
(limit +6)." The game offers only unbound companions of another culture as the one strengthened (Merry, not Sam
or Frodo). Revision 0 said "a companion" in both places and the erratum changed both filters to unbound
together, and your old test's comment says "not frodo". The test asserts that and is active. Low stakes: say
only if a Ring-bound companion of another culture should be offered.

(That the action is not offered at all once +6 has been given, I have taken as covered by your ruling on
Q-0015: a use that would do nothing is deliberately not offered.)

**Answered 2026-10-05.** "One" stands for "an unbound companion". The test stands.

### Q-0029 Nen Hithoel (101_63): may the Shadow player draw fewer cards than one for each companion over 4?

"When the fellowship moves to Nen Hithoel, the Shadow player may draw a card for each companion over 4." The
game asks once and draws all or none; your old test accepts once and reads three drawn. The other reading is
that each draw is its own "may", so he could stop anywhere from 0 to the full number. Every card of this shape
is built all-or-none (Orthanc Library 4_362, Summit of Amon Hen 1_362, Book of Mazarbul 3_1 and others). The
tests assert all-or-none and are active. Low stakes: say only if a number should be chosen.

An observation with it, not a question: these three sites ask the Shadow player whether to draw even when there
is nothing to draw (four companions or fewer; no burdens), on every move there. By your ruling on Q-0015 that
is not wrong, but it is the kind of prompt you called a pester on Q-0010. A test asserts the offer as it is.

**Answered 2026-10-05.** One offer for the whole draw; the offer stays when the draw deck is empty. The
four-companions-or-fewer case was not answered and is Q-0033.

### Q-0033 Nen Hithoel (101_63), Orthanc Library (4_362), Summit of Amon Hen (1_362): is the draw offered when the count is zero?

Your answer to Q-0029 covered an empty draw deck (rare, and a useful nudge). This is the other draw of nothing:
a fellowship of four companions or fewer (or no burdens, for the other two sites), where "a card for each
companion over 4" is no cards. The game asks the Shadow player anyway, on every move there.

- **A:** leave it; the offer stays whenever nothing would be drawn.
- **B:** not offered when the count is zero (the pointless prompt of Q-0010 and Q-0015). The definitions gain a
  requirement and three assertions on 101_63 change to "no offer".

The tests assert the offer as the game makes it.

**Answered 2026-10-05: B.** "If there's 4 or fewer companions, there's no reason to nag the player with 0 draws,
yeah. Let's make it smarter." The three sites now ask only when there is something to count.

### Q-0030 Sirannon Dam (101_65): when Watcher in the Water replaces a tentacle, what becomes of his own assignment?

"Skirmish: Remove 2 burdens to have Watcher in the Water replace a tentacle skirmishing an unbound companion."
Watcher may himself be assigned to another companion whose skirmish has not been fought. The game takes him out
of that assignment: he fights in the tentacle's place and the companion he was assigned to has no skirmish. Your
old test asserts this, and the new one follows it through to the regroup phase. The other reading is that he
keeps his assignment and fights again in his own skirmish afterwards. CR 4.2 "replace" speaks only of the
character replaced. The tests take the game's reading and are active. Say only if he should fight twice.

**Answered 2026-10-06.** "It's being removed from its assignment, which may cancel it if it's the only minion in its skirmish." The game is right.

### Q-0031 Sirannon Dam (101_65): does "with no other terrain keywords" limit "Rivers" as well as "sites"?

"Rivers and sites with no other terrain keywords gain marsh."

- **A (the game, and the tests):** it belongs to "sites" alone. Every river gains marsh whatever else it is, so
  Anduin Wilderland (forest and river) becomes a marsh.
- **B:** it governs both, and a river with another terrain keyword gains nothing. Three assertions turn round.

A is the likelier: under B the words "Rivers and" add nothing. No other card has a sentence of this shape.

**Answered 2026-10-06.** A. "This is meant to parse as '[rivers] and [sites with no other terrain keywords] gain marsh'."

### Q-0032 Horsemen Took Your Lands (102_4): is the "you may play" possession played without asking?

"If you have initiative, you may play a [dunland] possession on that minion from your discard pile."

- **A (the game, and your old test):** with initiative, a [dunland] possession in the discard pile that can go
  on the minion is always played: unasked when there is one, a choice with a minimum of 1 when there are two.
- **B (the text):** the Shadow player may decline (a 0 to 1 choice). It matters a little: Iron Axe costs (1), and
  a possession left in the discard pile can be fetched again. The definition gets `optional: true` on the inner
  play, the parked test goes live and the old-reading test is rewritten.

Of the 20 cards with "may play ... from your discard pile" this is the only one where the "may" sits inside an
event's effect. Your ruling on Q-0021 (a trigger's "you may" is asked) points to B.

**Answered 2026-10-06.** B. "Should be optional, an implementation oversight." Fixed.

### Q-0034 May I treat "an optional action that would do nothing is not offered" as a general rule?

You have ruled this way three times, each for its own shape: Q-0010, Q-0015 (X of zero), Q-0033 (a draw of
zero). It has come up again on Naith Armaments (102_10): "Each time bearer wins a skirmish, you may exert bearer
to heal another valiant Elf." The definition adds a requirement the text does not have (another wounded valiant
Elf), so nothing is offered when the exertion would heal nobody. By the rules a cost may be paid for nothing.

- **A:** general rule. Where the game withholds an optional action or trigger whose whole effect would do
  nothing, that is right and is not asked about again; where it offers one, that is a finding to fix.
- **B:** case by case, as now.

The 102_10 test asserts the game's reading (not offered). A would save you a question on most sets.

**Answered 2026-10-06.** A, with two exceptions. "Sure, start treating it as a rule, except in these cases: Events. People often want to play dud events to get them out of their hand. Situations with a cost. Oftentimes the cost may incidentally be beneficial even if the effect fizzles, such as exerting an enduring character." Naith Armaments has a cost, so its offer is now made even when nobody would be healed.

### Q-0035 To Honour That Allegiance (102_11): "Exert a valiant companion (or spot 2 valiant Elves)": may the player exert when the spot is open?

The game spots unasked whenever two valiant Elves can be spotted, and offers the exertion only when they cannot.

- **A (the game, and your old tests):** the free alternative is taken unasked.
- **B (the text):** the player says which. A reason to want the exertion: Erkenbrand (102_47) makes exhausted
  companions valiant.

Four V3 cards are built the same way (103_23, 103_24, 103_84, 103_101), so one answer covers them. Low stakes.

**Answered 2026-10-06.** A. "Nope, Decipher ruled that a branch in parenthesis is mandatory to choose if it's available."

### Q-0036 A card revealed at random from a hand: is the hand's owner given the pop-up? (rider to Q-0024)

On Q-0024 you ruled no pop-up for the hand's owner, the log being enough. That is what the game does for
"look at" and "reveal your hand". The effect behind "reveal a card at random from a hand" is different: it
sends the blocking pop-up to every player, the owner included. Thirteen cards use it (1_120, 3_16, 3_33, 3_43,
3_67, 3_76, 3_84, 3_87, 3_109, 10_29, 51_55, 51_313, 53_87), and your own tests for Sting (51_313), The Mirror
of Galadriel (51_55) and 3_67 have both players dismiss.

- **A (your ruling as written):** the owner is shown nothing. An engine change; those tests lose a dismissal.
- **B:** a random reveal is the exception, since the owner did not choose the card.

I will do A unless you say B. The test of A on 53_87 is parked until then.

**Answered 2026-10-06.** A. "Nah, doesn't need the popup, just the event log." Now finding F-0056, an engine fix.

### Q-0037 Deadly Contest (102_7): is "you may reinforce both ... an additional time" asked?

"If you can spot both Legolas and Gimli you may reinforce both an [elven] and [dwarven] token an additional
time." The game places both additional tokens unasked; your two old tests read it so.

- **A:** leave it (nobody declines a free token).
- **B:** the player may decline. If B, say whether it is one yes/no for the pair, or two picks of 0 to 1, which
  would let a player take one token and leave the other.

**Answered 2026-10-06.** B. "The card probably shouldn't have had may on it; there's zero circumstances where more tokens is bad. Let's go ahead and fix it to match the bad wording." Built as one yes/no for the pair.

### Q-0038 Haldir, Naith Commander (102_8): is a discard paid as a cost "by a card effect"?

"Each time an [elven] possession is about to be discarded by a card effect, you may exert Haldir to prevent
that." Lembas: "Regroup: Discard this possession to reconcile your hand." The game offers Haldir, and
preventing leaves the cost unpaid, so the ability does nothing. The other reading is that a cost is not an
effect and nothing is offered. The test asserts the game's reading. Low stakes.

**Answered 2026-10-06.** "That's permitted, but it will cause the cost to no longer be considered paid." The game is right.

### Q-0039 Gandalf, Lathspell (102_13): is what a minion card does from the discard pile "a minion's special ability"?

"Each time a minion's special ability is used (except during a skirmish), you may ... prevent that and exert
that minion." The game offers Lathspell for anything a minion card does by its own text, wherever the card is.
New Authority (11_37) and Storied Homestead (13_76) are built the same way.

1. **A permission to play with no phase word.** Gollum, Plotting Deceiver (7_58): "If you have initiative during
   the Shadow phase, you may play this minion from your discard pile." The game offers Lathspell; accepted,
   Gandalf exerts twice, Gollum stays in the discard pile, and the offer comes straight back on the next
   attempt. By CR 4.2 that sentence is not a special ability (no boldfaced phase word). The test of "not
   offered" is parked. Is the game wrong here?
2. **A labelled ability used from the discard pile or while stacked.** Gollum, Her Sneak (13_50): "Shadow: Remove
   ... to play this minion from your discard pile"; Uruk Occupier (102_34); the stacked minions. The game
   offers Lathspell, the ability is prevented, and "exert that minion" does nothing. The active test asserts
   this (cards are code). Say only if Lathspell should answer minions in play alone.

**Answered 2026-10-06.** "It would count, if it's got the bold text and a colon. Obviously the exert would fizzle, and we should check that it doesn't cause problems ... 2 counts, 1 does not." Part 1 is now finding F-0055, an engine fix.

### Q-0040 Gandalf's Staff, Old Man's Crutch (102_77): is "Discard any weapon he bears" a standing effect?

The game treats it as continuous: a weapon played on Gandalf after the Staff is paid for and discarded as it
arrives. That is LR 1's reasoning for Shadowfax and what your old test asserts, so the new test asserts it too.
LR 1 names Shadowfax only, which is why this is written down at all. Say only if it should be once, on play.

**Answered 2026-10-06.** "Yup, we ruled that with the Shadowfax, GOTM ruling and applied it to Anduril as well. It's continuous."
