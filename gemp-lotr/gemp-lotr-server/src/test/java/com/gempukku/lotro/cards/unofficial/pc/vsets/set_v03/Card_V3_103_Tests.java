package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_103_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("accounted1", "103_103");
					put("accounted2", "103_103");
					put("slayer", "3_93");        // Morgul Slayer: a [sauron] Orc minion, cost 2, vitality 2; a regroup ability never used here
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] condition, cost 1, support area; a skirmish ability never used here
					put("bladetip", "1_209");     // Blade Tip: a [ringwraith] condition, cost 0, support area; it does nothing until a Nazgul wins a skirmish
					put("runner", "1_178");       // Goblin Runner: an Orc who is [moria]
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: a Nazgul who is [raider], not [ringwraith]
					put("summons", "1_242");      // The Dark Lord's Summons: a Shadow condition: "Shadow: Remove (3) to reveal the top card of your draw deck. If it is a [sauron] card, take it into hand. Otherwise, discard it and one other card from hand."
					put("troop1", "1_177");       // Goblin Patrol Troop: [moria] cards for the Shadow player's draw deck
					put("troop2", "1_177");

					put("halls1", "1_18");        // Halls of My Home: "Fellowship: Exert a Dwarf to reveal the top 3 cards of any draw deck. You may discard 1 Shadow card revealed. Return the rest in any order."
					put("halls2", "1_18");
					put("kingdom", "1_16");       // Greatest Kingdom of My People: a Free Peoples condition: "Each time your opponent plays an Orc, that player discards the top card of his or her draw deck."
					put("wizard", "1_87");        // A Wizard Is Never Late: "Fellowship: Play a [gandalf] character from your draw deck."
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: the [gandalf] character in the draw deck
					put("treachery", "1_86");     // Treachery Deeper Than You Know: "Fellowship: Spot Gandalf to reveal an opponent's hand."
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: an unbound companion, vitality 3; the Dwarf who exerts for Halls of My Home
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("bilbo", "1_284");        // Bilbo, Retired Adventurer: an ally, vitality 3
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: a Free Peoples card for the top of a deck
					put("realm", "2_12");         // Realm of Dwarrowdelf: a Free Peoples condition: "Skirmish: Discard the top 3 cards from your draw deck to make a Dwarf strength +1 (limit +3)" (and damage +1 if underground)
					put("ottar", "1_80");         // Ottar, Man of Laketown: an ally: "Fellowship: Exert Ottar to discard up to 3 cards from hand and draw an equal number of cards."
					put("athelas", "1_94");       // Athelas: a possession on a [gondor] Man: "Fellowship: Discard this possession to heal a companion or to remove a Shadow condition from a companion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyAreNotAllAccountedForStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: They Are Not All Accounted For
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [sauron] or [ringwraith] cards.
		 * 	Each time a Free Peoples card reveals or discards a card from any draw deck, you may exert an
		 * 	unbound companion or ally to draw a card (limit once per site).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("accounted1");

		assertEquals("They Are Not All Accounted For", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoSauronCardsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var fear = scn.GetShadowCard("fear");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(accounted, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, runner, khamul);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		// One [sauron] card, and beside it an Orc who is [moria] and a Nazgul who is [raider].
		assertFalse(scn.ShadowPlayAvailable(accounted));

		scn.ShadowPlayCard(fear);
		// 11 - 1 for the condition
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(accounted));

		scn.ShadowPlayCard(accounted);

		assertInZone(Zone.SUPPORT, accounted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronCardAndARingwraithCardTogetherAreEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var bladetip = scn.GetShadowCard("bladetip");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(accounted, bladetip);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, runner);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(accounted));

		// A [ringwraith] condition, cost 0, joins the [sauron] minion.
		scn.ShadowPlayCard(bladetip);
		assertTrue(scn.ShadowPlayAvailable(accounted));

		scn.ShadowPlayCard(accounted);

		// 8 set + 1 for Frodo + 2 for the site - 1 to play
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, accounted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARevealFromTheFreePeoplesPlayersDeckLetsTheShadowPlayerExertAnUnboundCompanionOrAllyToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var accounted = scn.GetShadowCard("accounted1");
		var slayer = scn.GetShadowCard("slayer");
		var halls = scn.GetFreepsCard("halls1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		// The Free Peoples player's own copies, for the top of his deck
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();

		// Gimli, the only Dwarf, exerts for the event.
		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);

		// Unbound companions (one of them already exerted once) and an ally; not the Ring-bearer, not Sam, who
		// is Ring-bound, and not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, bilbo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, slayer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.ShadowChooseCard(bilbo);

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// The event left the Free Peoples player's hand and nothing came into it.
		assertEquals(freepsHand - 1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));
	}

	@Test
	public void AnOfferDeclinedChangesNothingAndADiscardFromTheFreePeoplesPlayersDeckOffersAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var halls = scn.GetFreepsCard("halls1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowDeclineOptionalTrigger();

		// Nobody exerted for it and nothing was drawn; three cards were revealed and one answer is all that is
		// asked for.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));

		// The event now discards one of the Shadow cards it revealed from that deck.
		scn.FreepsChooseYes();
		assertInZone(Zone.DECK, runner);
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner);

		// A card discarded from a draw deck is another time, and declining did not use up this site's one use.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, bilbo));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The two cards left go back in an order of the Free Peoples player's choosing.
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealFromTheShadowPlayersOwnDeckIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var halls = scn.GetFreepsCard("halls1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		// The Shadow player's own copies, for the top of his deck
		var runner = scn.GetShadowCard("runner");
		var bladetip = scn.GetShadowCard("bladetip");
		var elrond = scn.GetShadowCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("opponent's deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, bilbo));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));
	}

	@Test
	public void ADiscardFromTheShadowPlayersOwnDeckIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var halls = scn.GetFreepsCard("halls1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var runner = scn.GetShadowCard("runner");
		var bladetip = scn.GetShadowCard("bladetip");
		var elrond = scn.GetShadowCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("opponent's deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowDeclineOptionalTrigger();

		scn.FreepsChooseYes();
		assertInZone(Zone.DECK, runner);
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner);
		// One card has left the Shadow player's deck by the discard.
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, bilbo));
		scn.ShadowChooseCard(bilbo);

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());

		// One revealed card is left in that deck: there is no order to choose.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceUsedAtASiteItIsNotOfferedThereAgainForADiscardOrAnotherReveal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var halls1 = scn.GetFreepsCard("halls1");
		var halls2 = scn.GetFreepsCard("halls2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(halls1, halls2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls1);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The same event discards a Shadow card it revealed: a second time at this site.
		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner);

		// Aragorn, Bilbo and Gimli could each still exert and the Shadow player has cards to draw: it is the
		// limit that keeps the offer back.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(deck - 1 > 0);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// And a third: another reveal, by another copy of the event.
		scn.FreepsPlayCard(halls2);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));
	}

	@Test
	public void AUseInTheFellowshipPhaseDoesNotUseUpTheSiteMovedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var slayer = scn.GetShadowCard("slayer");
		var halls = scn.GetFreepsCard("halls1");
		var kingdom = scn.GetFreepsCard("kingdom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted, kingdom);
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToHand(slayer);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();
		scn.SetTwilight(10);

		assertEquals(1, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();

		// Site 1, the fellowship phase: a reveal.
		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// Nothing is discarded; the three revealed cards go back in any order.
		scn.FreepsChooseNo();
		scn.FreepsChooseAny();
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Site 2: the Shadow player plays an Orc, and the Free Peoples condition makes him discard the top card
		// of his own deck. No card is revealed here: a discard alone.
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(slayer);
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		// One drawn at site 1, the Orc played from hand, one drawn at site 2
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheUseTakenAtASiteIsThereToTakeAgainOnTheNextTurnAtThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var slayer = scn.GetShadowCard("slayer");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var halls = scn.GetFreepsCard("halls1");
		var kingdom = scn.GetFreepsCard("kingdom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted, kingdom);
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToHand(slayer);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Turn 1, site 2: the use for this site, on a discard from the Shadow player's deck.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(slayer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The turn ends at site 2; the other player's turn passes; the fellowship phase of the next turn finds
		// the fellowship at site 2 still.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, accounted);
		assertInZone(Zone.HAND, halls);
		// The reconciles on the way have drawn from both decks.
		scn.MoveCardsToTopOfDeck(troop1, troop2);
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Once for each site in a turn: on a new turn the site's use is there again,
		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// and once only.
		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyMakesItsOwnOfferAndKeepsItsOwnCountForTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted1 = scn.GetShadowCard("accounted1");
		var accounted2 = scn.GetShadowCard("accounted2");
		var halls = scn.GetFreepsCard("halls1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted1, accounted2);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted2));
		scn.ShadowChooseOptionalTrigger(accounted1);
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The second copy's offer for the same reveal is still there; it is declined.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(accounted1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The discard: the copy already used at this site keeps quiet, the other offers.
		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner);
		assertInDiscard(runner);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(accounted1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted2));
		scn.ShadowChooseOptionalTrigger(accounted2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, bilbo));
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealAndADiscardByAShadowCardAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var summons = scn.GetShadowCard("summons");
		var slayer = scn.GetShadowCard("slayer");
		var bladetip = scn.GetShadowCard("bladetip");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var kingdom = scn.GetFreepsCard("kingdom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(accounted, summons, kingdom);
		scn.MoveCardsToHand(slayer, bladetip);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Neither card on top of the Shadow player's deck is a [sauron] card.
		scn.MoveCardsToTopOfDeck(troop1, troop2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int discard = scn.GetShadowDiscardCount();

		// The Shadow player's own condition reveals the top card of his deck and, as it is no [sauron] card,
		// discards it from there, with one card from his hand.
		scn.ShadowUseCardAction(summons);
		scn.DismissRevealedCards();
		scn.ShadowChooseCard(bladetip);

		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(discard + 2, scn.GetShadowDiscardCount());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Free Peoples card doing the like on the same board: the Orc played makes Greatest Kingdom of My
		// People discard the next card of that deck.
		scn.ShadowPlayCard(slayer);

		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		// Two cards left the hand (one discarded, the Orc played) and one was drawn.
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADiscardThatIsTheCostOfAFreePeoplesCardIsAnsweredOnceThoughThreeCardsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var slayer = scn.GetShadowCard("slayer");
		var realm = scn.GetFreepsCard("realm");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(accounted, realm);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(6, scn.GetStrength(gimli));

		// The skirmish phase: three cards are discarded from the Free Peoples player's deck to pay for the
		// condition. Gimli is the only Dwarf.
		scn.FreepsUseCardAction(realm);

		assertEquals(freepsDeck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		scn.ShadowChooseCard(aragorn);

		// One use for the site, whatever the number of cards discarded together.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AUseInTheShadowPhaseIsTheSitesUseInALaterPhaseThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var slayer = scn.GetShadowCard("slayer");
		var kingdom = scn.GetFreepsCard("kingdom");
		var realm = scn.GetFreepsCard("realm");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(accounted, kingdom, realm);
		scn.MoveCardsToHand(slayer);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 2, the Shadow phase: the Orc played has the top card of the Shadow player's deck discarded.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(slayer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		// Site 2 still, the skirmish phase: a discard from a draw deck by a Free Peoples card, in another phase.
		scn.FreepsUseCardAction(realm);

		// Aragorn and Gimli could each exert and the Shadow player has cards to draw: it is the site's limit
		// that keeps the offer back.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(freepsDeck - 3, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(deck > 0);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACardRevealedFromAHandOrDiscardedFromHandOrFromPlayOrDrawnOrPlayedFromADrawDeckIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted1");
		var halls = scn.GetFreepsCard("halls1");
		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var treachery = scn.GetFreepsCard("treachery");
		var ottar = scn.GetFreepsCard("ottar");
		var athelas = scn.GetFreepsCard("athelas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// The Free Peoples player's own copies: one to discard from hand, three for the top of his deck
		var troop = scn.GetFreepsCard("troop1");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");
		// The one card of the Shadow player's hand
		var shadowsCard = scn.GetShadowCard("troop2");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, athelas);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(halls, wizard, treachery, troop);
		scn.MoveCardsToHand(shadowsCard);

		scn.StartGame();
		// Gandalf is wanted in the draw deck further down: he must not be the card drawn here.
		scn.MoveCardsToBottomOfDeck(gandalf);
		scn.FreepsRefreshDecision();

		int freepsDeck = scn.GetFreepsDeckCount();

		// A Free Peoples card discards a card from hand and draws one: the card drawn leaves the draw deck
		// neither revealed nor discarded.
		scn.FreepsUseCardAction(ottar);
		scn.FreepsChooseCard(troop);

		assertInDiscard(troop);
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples card discards a card from play: itself, to heal the one wounded companion.
		scn.FreepsUseCardAction(athelas);

		assertInDiscard(athelas);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples card plays a card from the draw deck: its owner looks through his deck, and the card
		// is neither revealed nor discarded.
		assertInZone(Zone.DECK, gandalf);
		scn.FreepsPlayCard(wizard);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples card reveals a card, but from a hand: the Shadow player's, shown to the Free Peoples
		// player. Gandalf, just played, is the one spotted.
		assertInZone(Zone.HAND, shadowsCard);
		scn.FreepsPlayCard(treachery);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertInDiscard(treachery);
		assertInZone(Zone.HAND, shadowsCard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A reveal from a draw deck on the same board is answered.
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
	}

	@Test
	public void WithNoUnboundCompanionOrAllyAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var accounted = scn.GetShadowCard("accounted1");
		var halls1 = scn.GetFreepsCard("halls1");
		var halls2 = scn.GetFreepsCard("halls2");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveCardsToHand(halls1, halls2);
		// Vitality 3: the exertion for the event will leave Gimli exhausted.
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elrond, bladetip, runner);
		scn.FreepsRefreshDecision();

		int hand = scn.GetShadowHandCount();

		scn.FreepsPlayCard(halls1);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		// The one unbound companion cannot exert; the Ring-bearer and Sam could, and they are Ring-bound.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));

		scn.FreepsChooseNo();
		scn.FreepsChooseAny();
		scn.FreepsChooseAny();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With Gimli rested, the same reveal at the same site is offered, and he is the only one to exert.
		scn.RemoveWoundsFromChar(gimli, 2);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(halls2);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(accounted));
		scn.ShadowChooseOptionalTrigger(accounted);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));
	}
}
