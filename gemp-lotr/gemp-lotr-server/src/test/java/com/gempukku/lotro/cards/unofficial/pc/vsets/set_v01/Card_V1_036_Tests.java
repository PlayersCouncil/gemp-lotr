package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_036_Tests
{

	// The usual Fellowship sites.  None of the first three is a marsh: Bag End, Ettenmoors (plains) and
	// Rivendell Valley (a sanctuary).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("vile", "101_36");
					put("vile2", "101_36");

					// Goblin Patrol Troop: a [moria] Orc with no text, strength 13.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc.  "When you play this minion, you may add (2)"; it is never played here.
					put("runner", "1_178");
					// Goblin Spear: a [moria] possession.
					put("spear", "1_182");
					// Uruk Savage: an [isengard] minion whose whole text is damage +1.
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Midgewater Marshes as site 2, a marsh as printed.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("vile", "101_36");
					put("vile2", "101_36");

					// Goblin Patrol Troop: a [moria] Orc with no text, strength 13.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc, cost 1.  "When you play this minion, you may add (2)", which the
					// game does without asking.
					put("runner", "1_178");
					// Goblin Spear: cost 1.  "Bearer must be a [moria] minion."  Its other text is about killing a
					// companion.
					put("spear", "1_182");
				}},
				new HashMap<>() {{
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
	public void VileTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Vile Tentacle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Tentacle. This minion may not bear possessions and is strength -4 while not at a marsh.
		 * 	Shadow: Discard a [moria] card from hand to make the fellowship's current site gain <b>marsh</b> until
		 * 	the end of the turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vile");

		assertEquals("Vile Tentacle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TENTACLE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandForTwoTwilightAndTwoMoreForRoamingAndStaysInPlayAwayFromAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToHand(vile);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		// 2 set + 1 companion + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, vile);
		assertTrue(scn.ShadowPlayAvailable(vile));

		scn.ShadowPlayCard(vile);

		// 2 to play + 2 roaming: its site number is 4 and this is site 2
		assertEquals(1, scn.GetTwilight());
		// Unlike the other tentacles, it is not discarded for being away from a marsh.
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthSevenAtAMarshAndFourLessAtTheSitesBeforeAndAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var vile = scn.GetShadowCard("vile");
		var vile2 = scn.GetShadowCard("vile2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(vile, vile2, troop);

		// Bag End is no marsh.  Each copy's -4 is its own, "this minion": 7 - 4 for each, and not 7 - 4 - 4 with
		// two of them on the table.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(3, scn.GetStrength(vile));
		assertEquals(3, scn.GetStrength(vile2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));
		assertEquals(7, scn.GetStrength(vile2));
		assertEquals(13, scn.GetStrength(troop));

		// Nobody is assigned, so all three minions are still there when the Free Peoples player decides whether to
		// move.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetStrength(vile));
		assertEquals(7, scn.GetStrength(vile2));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, vile, vile2, troop);
		assertEquals(3, scn.GetStrength(vile));
		assertEquals(3, scn.GetStrength(vile2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAsBearerOfAPossessionThatOtherMoriaMinionsMayBearEvenAtAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(spear, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(vile);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// At a marsh, so that what follows is not "while not at a marsh".
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));
		// 2 set + 1 companion + 1 for Midgewater Marshes: the Spear's 1 can be paid.
		assertEquals(4, scn.GetTwilight());
		// The tentacle is the only [moria] minion, and the Spear has nobody to go on.
		assertFalse(scn.ShadowPlayAvailable(spear));

		// Two Orcs join it: one put there, one played so that the Shadow player is asked afresh.
		scn.MoveMinionsToTable(troop);
		scn.ShadowPlayCard(runner);
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);

		// All three are [moria] minions, which is all the Spear asks of its bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vile));

		scn.ShadowChooseCard(troop);

		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsAChosenMoriaCardFromHandToMakeTheFellowshipsSiteAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vile = scn.GetShadowCard("vile");
		var vile2 = scn.GetShadowCard("vile2");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(vile2, runner, spear, savage);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(vile);
		scn.FreepsPass();

		var bagEnd = scn.GetSite(1);
		var ettenmoors = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));
		// 7 - 4 away from a marsh
		assertEquals(3, scn.GetStrength(vile));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowActionAvailable(vile));

		scn.ShadowUseCardAction(vile);

		// From hand: a [moria] minion, a [moria] possession and another copy of this card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vile2, runner, spear));
		// Not the [isengard] minion in hand, nor the [moria] cards in the discard pile and in play.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop, vile));

		scn.ShadowChooseCard(spear);

		assertInZone(Zone.DISCARD, spear);
		assertInZone(Zone.HAND, vile2, runner, savage);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(vile));

		// It is a Shadow ability: with [moria] cards still in hand, it is not there in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(vile));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoMoriaCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vile = scn.GetShadowCard("vile");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		// A card in hand that is not [moria], and [moria] cards that are not in hand.
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(vile, runner);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(vile));
	}

	@Test
	public void TheSiteIsStillAMarshInTheRegroupPhaseAndNoLongerOneOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vile = scn.GetShadowCard("vile");
		var spear = scn.GetShadowCard("spear");
		// Player 1's own copy, for player 2's turn, when player 1 is the Shadow player.
		var nextTurnsVile = scn.GetFreepsCard("vile");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(vile);
		scn.FreepsPass();

		var ettenmoors = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertEquals(3, scn.GetStrength(vile));

		// The Spear is the only [moria] card in hand, so it is the one discarded.
		scn.ShadowUseCardAction(vile);

		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));

		// The fellowship stays, and the turn ends.
		scn.SkipToOtherPlayersTurn();

		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));

		// Player 2's fellowship now moves to the same Ettenmoors, where a Vile Tentacle is waiting.
		scn.MoveMinionsToTable(nextTurnsVile);
		scn.ShadowPass();

		assertSame(ettenmoors, scn.GetCurrentSite());
		assertEquals(3, scn.GetStrength(nextTurnsVile));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void TheMarshStaysWithTheSiteItWasGivenToWhenTheFellowshipMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vile = scn.GetShadowCard("vile");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(vile);
		scn.FreepsPass();

		var ettenmoors = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));

		scn.ShadowUseCardAction(vile);

		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertEquals(7, scn.GetStrength(vile));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Rivendell Valley is the fellowship's current site now, and it was not when the ability was used.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(3, scn.GetStrength(vile));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
