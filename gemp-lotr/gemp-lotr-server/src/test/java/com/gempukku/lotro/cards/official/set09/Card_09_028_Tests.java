package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "9_28");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// Isildur's Bane answers a WOUND on the Ring-bearer in any phase and never an exertion: its response
				// tells the Shadow ability's exertion from the Regroup ability's wound.
				VirtualTableScenario.IsildursBaneRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Gollum, Dark as Darkness
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype:
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Shadow:</b> Exert Gollum twice to exert the Ring-bearer.
		 * <b>Shadow:</b> Remove a burden to add (1).
		 * <b>Skirmish:</b> Remove a burden to add (2).
		 * <b>Regroup:</b> Remove a burden to add (3).
		 * <b>Regroup:</b> Exert Gollum twice to wound the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Dark as Darkness", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsGollumTwiceToExertTheRingBearerUntilHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// A burden, so that his other Shadow ability is on offer beside this one.
		scn.AddBurdens(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(gollum, "Exert the Ring-bearer"));

		scn.ShadowUseCardAction(gollum, "Exert the Ring-bearer");

		// Gollum exerts twice; the Ring-bearer exerts once, and Sam, Ring-bound but not the Ring-bearer, not at
		// all.  Nobody is asked to choose, and the Ring has nothing to answer: an exertion is no wound.
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Vitality 4 with 2 wounds: one exertion left, not two.  The ability is withheld; the burden one is not.
		assertFalse(scn.ShadowActionAvailable(gollum, "Exert the Ring-bearer"));
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (1)"));
	}

	@Test
	public void ShadowAbilityRemovesABurdenToAddOneUntilThereIsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddBurdens(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (1)"));

		scn.ShadowUseCardAction(gollum, "Add (1)");
		assertEquals(1, scn.GetBurdens());
		assertEquals(pool + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(gollum, "Add (1)");
		assertEquals(0, scn.GetBurdens());
		assertEquals(pool + 2, scn.GetTwilight());

		// No burden left to remove: not offered, though Gollum can still exert twice.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum, "Add (1)"));
		assertTrue(scn.ShadowActionAvailable(gollum, "Exert the Ring-bearer"));
	}

	@Test
	public void SkirmishAbilityRemovesABurdenToAddTwoUntilThereIsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddBurdens(1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		int pool = scn.GetTwilight();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		assertEquals(0, scn.GetBurdens());
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Asked again in the same skirmish, with no burden left: not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void RegroupAbilityRemovesABurdenToAddThreeUntilThereIsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		int pool = scn.GetTwilight();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (3)"));

		scn.ShadowUseCardAction(gollum, "Add (3)");
		assertEquals(0, scn.GetBurdens());
		assertEquals(pool + 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Asked again in the same phase, with no burden left: not offered, while his other regroup ability is.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum, "Add (3)"));
		assertTrue(scn.ShadowActionAvailable(gollum, "Wound the Ring-bearer"));
	}

	@Test
	public void RegroupAbilityExertsGollumTwiceToWoundTheRingBearerUntilHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// A burden, so that his other regroup ability is on offer beside this one.
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(gollum, "Wound the Ring-bearer"));

		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertEquals(2, scn.GetWoundsOn(gollum));

		// A wound, not an exertion: the Ring-bearer's Ring answers it.  Declined, it lands on him and on nobody else.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());

		// Vitality 4 with 2 wounds: he cannot exert twice, and the ability is withheld.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum, "Wound the Ring-bearer"));
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (3)"));
	}
}
