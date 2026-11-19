package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("meneldor", "17_22");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("legolas", "1_50");      // Legolas, Greenleaf: strength 6; an archery ability used only on purpose
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: a [gandalf] card that is no follower
					put("alatar", "13_28");      // Alatar, Final Envoy: [gandalf] follower, no strength bonus, nothing to play him
					put("radagast", "13_38");    // Radagast, Tender of Beasts: [gandalf] follower, resistance +2 only
					put("worker", "15_135");     // Rohan Worker: a follower of another culture, no printed bonus
					put("troop", "1_177");       // Goblin Patrol Troop: no text; only there so that a maneuver phase is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MeneldorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Meneldor, Misty Mountain Eagle
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Follower
		 * Game Text: <b>Aid</b> – (1).
		 * 	To play, spot a [gandalf] follower.
		 * 	Bearer is strength +1 for each other [gandalf] follower you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("meneldor");

		assertEquals("Meneldor", card.getBlueprint().getTitle());
		assertEquals("Misty Mountain Eagle", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void CannotBePlayedUntilAGandalfFollowerIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meneldor = scn.GetFreepsCard("meneldor");
		var alatar = scn.GetFreepsCard("alatar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(meneldor, alatar);

		scn.StartGame();

		// Gandalf is a [gandalf] card and no follower; the Rohan Worker is a follower of another culture.
		assertFalse(scn.FreepsPlayAvailable(meneldor));

		scn.FreepsPlayCard(alatar);
		assertInZone(Zone.SUPPORT, alatar);
		assertTrue(scn.FreepsPlayAvailable(meneldor));

		scn.FreepsPlayCard(meneldor);
		assertInZone(Zone.SUPPORT, meneldor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBePlayedSpottingAGandalfFollowerThatACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meneldor = scn.GetFreepsCard("meneldor");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, radagast);
		scn.MoveCardsToHand(meneldor);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(meneldor));
		scn.FreepsPlayCard(meneldor);
		assertInZone(Zone.SUPPORT, meneldor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AidAddsOneAndGivesHimToACompanionWhoIsThenStrengthPlusOneForAnotherGandalfFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var meneldor = scn.GetFreepsCard("meneldor");
		var alatar = scn.GetFreepsCard("alatar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(meneldor, alatar);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(meneldor));
		scn.FreepsChooseOptionalTrigger(meneldor);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas));
		scn.FreepsChooseCard(aragorn);

		// Aid - (1); Alatar in the support area is the one other [gandalf] follower.
		assertEquals(pool + 1, scn.GetTwilight());
		assertAttachedTo(meneldor, aragorn);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));

		// Alatar's own Aid is still offered; he stays where he is.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, alatar);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneOnlyOnceAnotherGandalfFollowerIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meneldor = scn.GetFreepsCard("meneldor");
		var alatar = scn.GetFreepsCard("alatar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gandalf = scn.GetFreepsCard("gandalf");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn, legolas, gandalf);
		scn.AttachCardsTo(aragorn, meneldor);
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(alatar);

		scn.StartGame();

		// Meneldor himself, Gandalf (a [gandalf] companion) and the Rohan Worker (a [rohan] follower) count for nothing.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPlayCard(alatar);
		assertInZone(Zone.SUPPORT, alatar);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerCountsGandalfFollowersInTheSupportAreaAndOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meneldor = scn.GetFreepsCard("meneldor");
		var alatar = scn.GetFreepsCard("alatar");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, meneldor);
		scn.AttachCardsTo(legolas, radagast);
		scn.MoveCardsToSupportArea(alatar);

		scn.StartGame();

		// 8 + 1 for Alatar + 1 for Radagast; Legolas, who bears Radagast and not Meneldor, is unchanged.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
	}
}
