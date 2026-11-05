package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_062_Tests
{

	// The Hobbit table, with Old Forest Road as site 5 in place of Mirkwood (which would discard Gandalf as the
	// fellowship moves there): the turn begins at Goblin Town (site 4) and the Shadow phase after the move is at
	// Old Forest Road.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gandalf, The Grey: [gandalf] Wizard companion.  A trigger on his player's [gandalf] events and
					// a fellowship ability, neither reached.
					put("gandalf", "30_27");
					// Gandalf, Friend of the Shirefolk: a Gandalf of another set; strength +1 for each race he
					// spots in the fellowship.
					put("gandalf72", "1_72");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 while 3 Ents are spotted.
					put("avenger", "6_27");
					// Goblin Runner: [moria] Orc minion, cost 1.  "When you play this minion, you may spot another
					// [moria] card to add (2)": no other [moria] card is in play.
					put("runner", "31_30");
					// Gangsquad Spies: [men] Man minion, cost 3, fierce.  The site number of each [men] Man is -3;
					// a maneuver ability never used.
					put("spies", "33_26");
					// Jail: [sauron] condition, cost 2, support area; a regroup ability never reached.
					put("jail", "32_39");
					// Orkish Veteran: [sauron] Orc minion, cost 2, laid in the FIRST player's discard pile.  At
					// site 9 each Orc is an archer; a regroup ability never reached.
					put("veteran", "32_43");
					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase.
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site5", "30_62");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OldForestRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Old Forest Road
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: none
		 * Game Text: <b>Forest</b>. At the start of the Shadow phase, you may discard Gandalf to play a minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Old Forest Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void TheShadowPlayerMayDiscardGandalfToPlayAMinionFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var myVeteran = scn.GetFreepsCard("veteran");
		var runner = scn.GetShadowCard("runner");
		var spies = scn.GetShadowCard("spies");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToDiscard(runner, spies, jail, myVeteran);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, avenger);
		assertInZone(Zone.DISCARD, runner, spies, jail, myVeteran);
		scn.FreepsPass();

		var road = scn.GetCurrentSite();
		assertEquals("30_62", road.getBlueprintId());
		// The Free Peoples player is not offered it; the Shadow player is.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(road));
		scn.ShadowAcceptOptionalTrigger();

		// The one Gandalf is discarded unasked; Ent Avenger, a [gandalf] companion of another title, stays.
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.FREE_CHARACTERS, avenger);

		// Minions of two cultures and races; Jail is no minion, and the other player's Veteran lies in his own
		// discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, spies));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, myVeteran));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(spies);

		assertInZone(Zone.SHADOW_CHARACTERS, spies);
		assertInZone(Zone.DISCARD, runner, jail, myVeteran);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesGandalfAndTheMinionAndTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(runner);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsPass();

		var road = scn.GetCurrentSite();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(road));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The start of the maneuver phase, at the same site, Gandalf and the Runner where they were: nothing.
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DISCARD, runner);
	}

	@Test
	public void WithNoMinionInTheDiscardPileThatCanBePlayedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var myVeteran = scn.GetFreepsCard("veteran");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(jail, myVeteran);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DISCARD, jail, myVeteran);
		scn.FreepsPass();

		// Gandalf could be discarded, but the play from the discard pile is part of the price and has nothing to
		// play: Jail is no minion, and the Veteran is the other player's.
		assertEquals("30_62", scn.GetCurrentSite().getBlueprintId());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
	}

	@Test
	public void WithNoGandalfItIsNotOfferedAndOnTheNextTurnTheFirstPlayerDiscardsTheOthersGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var runner = scn.GetShadowCard("runner");
		var myRunner = scn.GetFreepsCard("runner");
		var hisGandalf = scn.GetShadowCard("gandalf72");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToDiscard(runner);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsPass();

		var road = scn.GetCurrentSite();
		assertEquals("30_62", road.getBlueprintId());
		// No Gandalf: Ent Avenger, a [gandalf] companion, does not pay.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, runner);

		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship, at site 4, with his Gandalf, Friend of the Shirefolk, moves to the same
		// Old Forest Road; the first player, Shadow there, has his own Runner in his discard pile.
		scn.MoveCompanionsToTable(hisGandalf);
		scn.MoveCardsToDiscard(myRunner);
		assertInZone(Zone.FREE_CHARACTERS, hisGandalf);
		assertInZone(Zone.DISCARD, myRunner);
		scn.ShadowPass();

		assertSame(road, scn.GetCurrentSite());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(road));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, hisGandalf);
		assertInZone(Zone.SHADOW_CHARACTERS, myRunner);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
