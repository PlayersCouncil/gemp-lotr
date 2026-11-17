package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_220_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyed", "1_220");
					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: a [wraith] Nazgûl of vitality 4; his maneuver ability needs 6 companions or 5 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl of vitality 3; his text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl; his text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotEasilyDestroyedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Not Easily Destroyed
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Maneuver</b> <i>or</i> <b>Skirmish:</b> Remove (3) to heal a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("destroyed");

		assertEquals("Not Easily Destroyed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverActionRemoves3ToHealANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyed = scn.GetShadowCard("destroyed");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveCardsToSupportArea(destroyed);
		scn.MoveMinionsToTable(enquea, khamul, hound);
		scn.AddWoundsToChar(enquea, 2);
		scn.AddWoundsToChar(khamul, 1);
		scn.AddWoundsToChar(hound, 1);
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(destroyed));
		scn.ShadowUseCardAction(destroyed);

		// A [wraith] Nazgûl and a [raider] one; not a [wraith] minion who is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(enquea);

		assertEquals(2, scn.GetTwilight());
		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishActionHealsANazgulInTheSkirmishOrInNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyed = scn.GetShadowCard("destroyed");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveCardsToSupportArea(destroyed);
		scn.MoveMinionsToTable(enquea, khamul, hound);
		scn.AddWoundsToChar(enquea, 1);
		scn.AddWoundsToChar(khamul, 1);
		scn.AddWoundsToChar(hound, 1);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToAssignments();
		scn.SetTwilight(5);
		scn.FreepsAssignToMinions(aragorn, enquea);
		// Khamûl and the Hound are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, enquea));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(destroyed));
		scn.ShadowUseCardAction(destroyed);

		// The Nazgûl in the skirmish and the one in none are both offered; the Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		scn.ShadowChooseCard(khamul);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThan3Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyed = scn.GetShadowCard("destroyed");
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveCardsToSupportArea(destroyed);
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 2);
		assertEquals(2, scn.GetWoundsOn(enquea));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(destroyed));
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(enquea));
	}

	@Test
	public void NotOfferedInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyed = scn.GetShadowCard("destroyed");
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveCardsToSupportArea(destroyed);
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 2);
		scn.SetTwilight(5);
		assertEquals(2, scn.GetWoundsOn(enquea));

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 5 + 2 for Ettenmoors + 2 companions: enough to pay.
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(destroyed));
		assertEquals(2, scn.GetWoundsOn(enquea));
	}

	@Test
	public void WithNoNazgulToHealTheTwilightIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyed = scn.GetShadowCard("destroyed");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveCardsToSupportArea(destroyed);
		scn.MoveMinionsToTable(hound);
		scn.AddWoundsToChar(hound, 1);
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// An ability with a cost is offered whether or not its effect can act on anything.
		assertTrue(scn.ShadowActionAvailable(destroyed));
		scn.ShadowUseCardAction(destroyed);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
