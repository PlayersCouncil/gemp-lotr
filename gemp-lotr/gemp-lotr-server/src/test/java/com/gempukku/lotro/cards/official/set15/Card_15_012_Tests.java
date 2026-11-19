package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dinendal", "15_12");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, vitality 4, no archer; a Maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Glóin: [dwarven] Dwarf, vitality 3, no archer; a Skirmish ability used only on purpose
					put("legolas", "1_50");       // Legolas, Greenleaf: an [elven] Elf companion; an Archery ability only
					// Rohan Stable Master: another follower, Aid (1); "Bearer is considered to be mounted."
					put("stablemaster", "15_134");
					put("troop", "1_177");        // Goblin Patrol Troop: no text; only there so that a maneuver phase is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DinendalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Dínendal, Mirkwood Archer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype:
		 * Game Text: <b>Aid</b> – (3). <helper>(At the start of the maneuver phase, you may add (3) to transfer this to a companion.)</helper><br>Each time you transfer this to a companion, except an [elven] companion, exert bearer.<br>Bearer is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dinendal");

		assertEquals("Dínendal", card.getBlueprint().getTitle());
		assertEquals("Mirkwood Archer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, scn.GetAidTwilightCost(card));
	}

	@Test
	public void TransferredByItsAidToACompanionWhoIsNotElvenItExertsHimAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(dinendal);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, dinendal);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dinendal));
		scn.FreepsAcceptOptionalTrigger();

		// Any companion may be given him, an [elven] one included.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas));
		scn.FreepsChooseCard(aragorn);

		// Aid - (3), and Aragorn, a [gondor] Man, exerts once.
		assertEquals(pool + 3, scn.GetTwilight());
		assertAttachedTo(dinendal, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// His bearer is an archer; another companion is not.
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToAnElvenCompanionHeExertsNobodyNorDoesAnotherFollowerTransferredToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var dinendal = scn.GetFreepsCard("dinendal");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(dinendal, stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, dinendal, stablemaster);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dinendal));
		scn.FreepsChooseOptionalTrigger(dinendal);
		scn.FreepsChooseCard(legolas);

		assertAttachedTo(dinendal, legolas);
		assertEquals(pool + 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(legolas));

		// The other follower goes to Aragorn, who would be exerted if Dínendal, borne elsewhere, answered it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stablemaster));
		scn.FreepsChooseOptionalTrigger(stablemaster);
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(stablemaster, aragorn);
		assertEquals(pool + 4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredAgainInASecondManeuverPhaseHeExertsTheNewBearerAndTheArcherKeywordGoesWithHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(dinendal);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, dinendal);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(dinendal, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(gimli, Keyword.ARCHER));

		// Nobody is assigned to the Troop. The fellowship moves again, and a second maneuver phase is played.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dinendal));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);

		// Gimli, a [dwarven] Dwarf, exerts once; Aragorn is not exerted again.
		assertEquals(pool + 3, scn.GetTwilight());
		assertAttachedTo(dinendal, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(gimli, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
