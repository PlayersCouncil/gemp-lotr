package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_153_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mumakil", "7_153");

					// Easterling Infantry: [raider] Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); his text needs 3 burdens
					put("khamul", "103_61");
					// Great Beasts: a [raider] card that is no minion (a support-area condition); a regroup ability only
					put("beasts", "7_149");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, of another culture; no text

					// Companions, so that the fellowship can hold 3 threats
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakiloftheHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mûmakil of the Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a [raider] minion to add (2) for each threat.
		*/

		var scn = GetScenario();

		var mumakil = scn.GetFreepsCard("mumakil");

		assertEquals("Mûmakil of the Harad", mumakil.getBlueprint().getTitle());
		assertNull(mumakil.getBlueprint().getSubtitle());
		assertFalse(mumakil.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mumakil.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, mumakil.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, mumakil.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(mumakil, Timeword.MANEUVER));
		assertEquals(1, mumakil.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingARaiderManWithOneThreatAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumakil = scn.GetShadowCard("mumakil");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(mumakil);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInHand(mumakil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetTwilight()); // 2 for Ettenmoors + 1 companion
		assertTrue(scn.ShadowPlayAvailable(mumakil));

		scn.ShadowPlayCard(mumakil);

		assertEquals(4, scn.GetTwilight()); // 3 - 1 to play + 2 for the one threat
		assertEquals(1, scn.GetThreats());
		assertInDiscard(mumakil);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SpottingARaiderMinionWhoIsNoManWithThreeThreatsAddsSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumakil = scn.GetShadowCard("mumakil");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(mumakil);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertInHand(mumakil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(5, scn.GetTwilight()); // 2 for Ettenmoors + 3 companions
		assertTrue(scn.ShadowPlayAvailable(mumakil));

		scn.ShadowPlayCard(mumakil);

		assertEquals(10, scn.GetTwilight()); // 5 - 1 to play + 2 for each of the 3 threats
		assertEquals(3, scn.GetThreats());
		assertInDiscard(mumakil);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutARaiderMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumakil = scn.GetShadowCard("mumakil");
		var beasts = scn.GetShadowCard("beasts");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(mumakil);
		scn.MoveCardsToSupportArea(beasts);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInHand(mumakil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [raider] card that is no minion, and a minion of another culture: neither is spotted.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(mumakil));
		assertInHand(mumakil);
	}
}
