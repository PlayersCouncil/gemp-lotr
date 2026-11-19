package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_030_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the maneuver phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer. A doubt is the Hobbit game's
	// burden. The allies are laid after StartGame(), so that Elrond's and Galadriel's start-of-turn heals are never
	// offered.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("courage", "30_30");
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: vitality 4; a fellowship ability and texts
					                            // about [dwarven] followers and Thorin, none laid
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; its text needs 3 Ents
					// [elven] Wise allies of two titles
					put("elrond", "30_21");     // Elrond, Elven Lord: vitality 4; a start-of-turn heal and a regroup ability
					put("galadriel", "32_13");  // Galadriel, Elven Lady: vitality 3; a start-of-turn heal of Gandalf and a
					                            // skirmish ability
					put("radagast", "31_17");   // Radagast, The Brown: a Wise ally of the [gandalf] culture; a fellowship ability
					put("lindir", "33_14");     // Lindir, Elven Minstrel: an [elven] ally who is not Wise; a move trigger
					                            // that heals an [elven] ally, and a fellowship ability

					put("aggressor", "30_39");  // Orkish Aggressor: a minion, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HegivesmecourageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: He gives me courage
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot Gandalf to remove a doubt (or 2 doubts, if you exert Gandalf and a [elven] Wise ally).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("courage");

		assertEquals("He gives me courage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfWithNoElvenWiseAllyRemovesOneDoubtUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var lindir = scn.GetFreepsCard("lindir");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		// A Wise ally who is no [elven] card, and an [elven] ally who is not Wise.
		scn.MoveAlliesToTable(radagast, lindir);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(courage));
		scn.FreepsPlayCard(courage);

		// The paid way cannot be paid, so the one way left is taken with no question: one doubt.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughOtherGandalfCardsAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		var elrond = scn.GetFreepsCard("elrond");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		scn.MoveAlliesToTable(radagast, elrond);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(courage));
		assertInZone(Zone.HAND, courage);
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void ExertingGandalfAndAnElvenWiseAllyOfTheChoiceRemovesTwoDoubts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var radagast = scn.GetFreepsCard("radagast");
		var lindir = scn.GetFreepsCard("lindir");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		scn.MoveAlliesToTable(elrond, galadriel, radagast, lindir);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(courage);
		// Both ways can be paid: the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Remove a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Gandalf"));
		scn.FreepsChooseOption("Exert Gandalf");

		// Gandalf, the only one, is exerted unasked; the [elven] Wise allies of both titles are offered, the
		// [gandalf] Wise ally and the [elven] ally who is not Wise are not.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, galadriel));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(radagast, lindir, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(galadriel);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(lindir));
		// Two doubts of the three.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThePlainWayMayBeChosenWhereBothCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		scn.MoveAlliesToTable(elrond);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(courage);
		assertTrue(scn.FreepsChoiceAvailable("Exert Gandalf"));
		scn.FreepsChooseOption("Remove a burden");

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithGandalfExhaustedOnlyOneDoubtIsRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		scn.MoveAlliesToTable(elrond);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. The [gandalf] Ent Avenger beside him could.
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(courage);

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTheElvenWiseAllyExhaustedOnlyOneDoubtIsRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courage = scn.GetFreepsCard("courage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var galadriel = scn.GetFreepsCard("galadriel");
		var lindir = scn.GetFreepsCard("lindir");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		assertInZone(Zone.HAND, courage);
		scn.MoveAlliesToTable(galadriel, lindir);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		// Vitality 3 with 2 wounds: Galadriel cannot exert. Lindir, an [elven] ally who is not Wise, could.
		scn.AddWoundsToChar(galadriel, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(galadriel));

		scn.FreepsPlayCard(courage);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
