package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_066_Tests
{

	// The usual Fellowship sites.  Site 2, Ettenmoors, is plains and not a marsh.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("writhing", "101_66");

					// Out of Dark Waters: a [moria] condition that tentacles are stacked on.  "Each time a tentacle
					// takes a wound, you may stack that tentacle here.  Shadow: Play a tentacle from here as if
					// from hand."  Nothing is wounded here and its own action is never used.
					put("waters", "101_34");

					// Tentacles of three other titles, for the discard pile.
					put("foul", "2_58");
					put("huge", "2_66");
					put("reaching", "19_19");
					// Vile Tentacle: the one tentacle that is not discarded away from a marsh.
					put("vile", "101_36");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, so that the first Shadow phase is at a marsh and a second
	// move in the regroup phase leaves it for Rivendell Valley, which is not one.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("writhing", "101_66");
					put("writhing2", "101_66");
					put("writhing3", "101_66");
					put("writhing4", "101_66");

					// Out of Dark Waters: a [moria] condition that tentacles are stacked on.  "Each time a tentacle
					// takes a wound, you may stack that tentacle here.  Shadow: Play a tentacle from here as if
					// from hand."  Nothing is wounded here and its own action is never used.
					put("waters", "101_34");
					put("waters2", "101_34");
					// Uruk Savage: an [isengard] minion whose whole text is damage +1.
					put("savage", "1_151");
					// Gandalf's Cart: a Free Peoples possession in the support area that cards are stacked on.
					// "Fellowship: Stack a Free Peoples artifact (or possession) from hand on this card, or play
					// a card stacked here as if played from hand."  Never used.
					put("cart", "1_73");

					// Tentacles of other titles.  Foul and Huge Tentacle may fetch a card when played and are
					// never played here; Reaching and Strong Tentacle are stronger for each tentacle.
					put("foul", "2_58");
					put("huge", "2_66");
					put("reaching", "19_19");
					put("strong", "19_20");
					// Vile Tentacle: the one tentacle that is not discarded away from a marsh.  Its Shadow
					// ability is never used.
					put("vile", "101_36");

					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc.  "When you play this minion, you may add (2)"; never played.
					put("runner", "1_178");
					// Goblin Spear: "Bearer must be a [moria] minion."  Its other text is about killing a companion.
					put("spear", "1_182");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					// Midgewater Marshes: "Marsh. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must
					// exert."  Frodo arrives with a wound, which nothing here turns on.
					put("site2", "1_332");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WrithingTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Writhing Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Tentacle. This minion may not bear items and is discarded if not at a marsh.
		 * 	Shadow: If this minion is stacked on a Shadow card, play it to stack 2 tentacles (except Writhing
		 * 	Tentacle) from your discard pile on that card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("writhing");

		assertEquals("Writhing Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandAtAMarshForTwoTwilightAndTwoMoreForRoamingAndStaysInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		scn.MoveCardsToHand(writhing);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 3 set + 1 companion + 1 for Midgewater Marshes
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, writhing);
		assertTrue(scn.ShadowPlayAvailable(writhing));

		scn.ShadowPlayCard(writhing);

		// 2 to play + 2 roaming: its site number is 4 and this is site 2
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, writhing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedAsSoonAsItIsPlayedAtASiteThatIsNotAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var writhing = scn.GetShadowCard("writhing");
		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(writhing);

		scn.StartGame();
		scn.MoveMinionsToTable(vile, troop);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(writhing);

		// The cost is paid all the same: 2 to play + 2 roaming.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, writhing);
		assertEquals(1, scn.GetShadowDiscardCount());
		// The tentacle and the Orc beside it are no more at a marsh than it was.
		assertInZone(Zone.SHADOW_CHARACTERS, vile, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyIsDiscardedWhenTheFellowshipMovesOnFromAMarshToASiteThatIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var writhing2 = scn.GetShadowCard("writhing2");
		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(writhing, writhing2, vile, troop);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, writhing, writhing2, vile, troop);

		// Nobody is assigned, so every minion is still there when the Free Peoples player decides whether to move.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, writhing, writhing2, vile, troop);

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.DISCARD, writhing, writhing2);
		// Vile Tentacle is a tentacle that may leave a marsh, and the Orc never needed one.
		assertInZone(Zone.SHADOW_CHARACTERS, vile, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var writhing2 = scn.GetShadowCard("writhing2");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(writhing, writhing2, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// The tentacles would be discarded at Bag End, so they are played here, at the marsh.
		scn.ShadowPlayCard(writhing);
		scn.ShadowPlayCard(writhing2);
		assertInZone(Zone.SHADOW_CHARACTERS, writhing, writhing2, troop, runner);
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);

		// All four are [moria] minions, which is all the Spear asks of its bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(writhing, writhing2));

		scn.ShadowChooseCard(troop);

		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromAStackOnAShadowCardItStacksTwoChosenTentaclesFromItsOwnersDiscardPileOnThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var writhing2 = scn.GetShadowCard("writhing2");
		var writhing3 = scn.GetShadowCard("writhing3");
		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		var vile = scn.GetShadowCard("vile");
		var runner = scn.GetShadowCard("runner");
		var otherPlayersStrong = scn.GetFreepsCard("strong");
		// Two copies, each stacked on its own copy of the condition, and a third in the discard pile.
		scn.MoveCardsToSupportArea(waters, waters2);
		scn.StackCardsOn(waters, writhing);
		scn.StackCardsOn(waters2, writhing2);
		scn.MoveCardsToDiscard(writhing3, foul, huge, reaching, runner, otherPlayersStrong);
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile);

		// 2 set + 1 companion + 1 for Midgewater Marshes: exactly what it costs to play here.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.STACKED, writhing, writhing2);
		assertInZone(Zone.DISCARD, writhing3, foul, huge, reaching, runner);
		assertTrue(scn.ShadowActionAvailable(writhing));
		assertTrue(scn.ShadowActionAvailable(writhing2));
		assertFalse(scn.ShadowActionAvailable(writhing3));

		scn.ShadowUseCardAction(writhing);

		// 2 to play + 2 roaming: its site number is 4 and this is site 2
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, writhing);
		assertTrue(scn.ShadowHasCardChoiceAvailable(foul, huge, reaching));
		// Another Writhing Tentacle and an Orc in the discard pile; a tentacle in hand, with the other player's
		// copy of it in his discard pile; a tentacle in play.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(writhing3, runner, strong, otherPlayersStrong, vile));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(foul, huge);

		assertEquals(2, scn.GetStackedCards(waters).size());
		assertTrue(scn.GetStackedCards(waters).contains(foul));
		assertTrue(scn.GetStackedCards(waters).contains(huge));
		assertInZone(Zone.DISCARD, reaching, writhing3, runner);
		// The other copy and the condition it is stacked on are as they were.
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.GetStackedCards(waters2).contains(writhing2));
		// Stacking is not playing: nothing more was paid.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithThePoolOneShortOfItsCostAndRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var waters = scn.GetShadowCard("waters");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var reaching = scn.GetShadowCard("reaching");
		scn.MoveCardsToSupportArea(waters);
		scn.StackCardsOn(waters, writhing);
		scn.MoveCardsToDiscard(foul, huge, reaching);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 companion + 1 for Midgewater Marshes, against 2 to play + 2 roaming
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.STACKED, writhing);
		assertFalse(scn.ShadowActionAvailable(writhing));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedStackedOnAFreePeoplesCardOrASiteNorFromTheDiscardPileNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var writhing2 = scn.GetShadowCard("writhing2");
		var writhing3 = scn.GetShadowCard("writhing3");
		var writhing4 = scn.GetShadowCard("writhing4");
		var waters = scn.GetShadowCard("waters");
		var cart = scn.GetFreepsCard("cart");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var reaching = scn.GetShadowCard("reaching");
		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToSupportArea(waters, cart);
		// One copy on the Free Peoples player's Cart, one in the discard pile, and one on a Shadow card.
		scn.StackCardsOn(cart, writhing);
		scn.MoveCardsToDiscard(writhing2, foul, huge, reaching);
		scn.StackCardsOn(waters, writhing3);

		scn.StartGame();
		// And one on Bag End: a site is neither a Shadow card nor a Free Peoples card.
		var bagEnd = scn.GetSite(1);
		scn.StackCardsOn(bagEnd, writhing4);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(vile);

		// 10 set + 1 companion + 1 for Midgewater Marshes: three times what it costs.
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(cart).contains(writhing));
		assertTrue(scn.GetStackedCards(bagEnd).contains(writhing4));
		assertInZone(Zone.DISCARD, writhing2);
		assertTrue(scn.ShadowActionAvailable(writhing3));
		assertFalse(scn.ShadowActionAvailable(writhing));
		assertFalse(scn.ShadowActionAvailable(writhing4));
		assertFalse(scn.ShadowActionAvailable(writhing2));

		// The maneuver phase: the copy on the Shadow card is still there and the pool still covers it.
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(waters).contains(writhing3));
		assertFalse(scn.ShadowActionAvailable(writhing3));
	}

	@Test
	public void StackedOnAShadowMinionOfAnotherCultureItStacksTheOnlyOtherTentacleInTheDiscardPileThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var writhing = scn.GetShadowCard("writhing");
		var writhing3 = scn.GetShadowCard("writhing3");
		var savage = scn.GetShadowCard("savage");
		var foul = scn.GetShadowCard("foul");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(writhing3, foul, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.StackCardsOn(savage, writhing);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 1 for Midgewater Marshes
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(savage).contains(writhing));
		assertInZone(Zone.DISCARD, writhing3, foul, runner);
		assertTrue(scn.ShadowActionAvailable(writhing));

		scn.ShadowUseCardAction(writhing);

		// 2 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, writhing);
		// There is one tentacle to stack where the text asks for two, so that one is stacked: the other Writhing
		// Tentacle and the Orc do not make up the number.
		assertEquals(1, scn.GetStackedCards(savage).size());
		assertTrue(scn.GetStackedCards(savage).contains(foul));
		assertInZone(Zone.DISCARD, writhing3, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromAStackAwayFromAMarshItIsDiscardedAndNoTentacleIsStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var writhing = scn.GetShadowCard("writhing");
		var waters = scn.GetShadowCard("waters");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var reaching = scn.GetShadowCard("reaching");
		scn.MoveCardsToSupportArea(waters);
		scn.StackCardsOn(waters, writhing);
		scn.MoveCardsToDiscard(foul, huge, reaching);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 1 set + 1 companion + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.STACKED, writhing);
		assertEquals(1, scn.GetStackedCards(waters).size());
		assertTrue(scn.ShadowActionAvailable(writhing));

		scn.ShadowUseCardAction(writhing);

		// What was paid to play it stays paid: 2 to play + 2 roaming.
		assertEquals(0, scn.GetTwilight());
		// Ettenmoors is no marsh, so the tentacle is discarded as it comes into play.  Playing it was the cost of
		// the ability, and a card that has left play before its play is complete was not played: no tentacle is
		// chosen and none is stacked.
		assertInZone(Zone.DISCARD, writhing, foul, huge, reaching);
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
