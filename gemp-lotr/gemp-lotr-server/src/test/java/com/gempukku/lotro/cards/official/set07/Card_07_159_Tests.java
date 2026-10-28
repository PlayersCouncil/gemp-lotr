package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "7_159");
					put("hope2", "7_159");
					// Easterling Axeman: a [raider] Easterling Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Easterling Infantry: a [raider] Easterling Man, strength 8, vitality 2; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul and no Man, strength 12, vitality 3;
					// fierce; his text needs 3 burdens
					put("khamul", "103_61");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Man who is no Easterling, strength 5, vitality 3; a regroup ability used only on purpose
					// Easterling Scout: a [men] Man with Easterling in his title only, strength 9, vitality 2; a skirmish ability used only on purpose
					put("scout", "15_77");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmallHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Small Hope
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Maneuver:</b> If you cannot spot 2 burdens, exert an Easterling to add a burden.<br><b>Skirmish:</b> Discard this condition to make an Easterling strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Small Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverAbilityExertsAnEasterlingToAddABurdenUntilTwoBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hope);
		scn.MoveMinionsToTable(axeman, khamul, explorer, scout);
		assertInZone(Zone.SUPPORT, hope);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(hope));

		scn.ShadowUseCardAction(hope);
		// An Easterling Man and the Easterling who is a Nazgul; not a [raider] Man who is no Easterling, nor a Man
		// with Easterling in his title only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.SUPPORT, hope);

		// With one burden it cannot spot 2, and is offered again.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hope));
		scn.ShadowUseCardAction(hope);
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(2, scn.FreepsGetBurdens());

		// At 2 burdens it is not offered, with Khamul (1 wound of 3) still able to exert.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hope));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheOnlyEasterlingIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var axeman = scn.GetShadowCard("axeman");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hope);
		scn.MoveMinionsToTable(axeman, explorer, scout);
		// Vitality 2 with 1 wound: the Axeman is exhausted.  The Southron and the Scout could exert, but neither is
		// an Easterling.
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SUPPORT, hope);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hope));
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeAnEasterlingStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var hope = scn.GetShadowCard("hope");
		var hope2 = scn.GetShadowCard("hope2");
		var axeman = scn.GetShadowCard("axeman");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hope, hope2);
		scn.MoveMinionsToTable(axeman, infantry, khamul, explorer, scout);
		assertInZone(Zone.SUPPORT, hope, hope2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hope));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, axeman }, new PhysicalCardImpl[] { gimli, infantry });
		// The Southron and the Scout each join a skirmish; Khamul is left to nobody.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[] { aragorn, explorer }, new PhysicalCardImpl[] { gimli, scout });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(khamul));
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(9, scn.GetStrength(scout));
		assertTrue(scn.ShadowActionAvailable(hope));

		scn.ShadowUseCardAction(hope);
		assertInZone(Zone.DISCARD, hope);
		assertInZone(Zone.SUPPORT, hope2);
		// The Easterling in this skirmish, the one waiting for the next and the one assigned to nobody; not the
		// Southron, the Scout or the other player's Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, infantry, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(infantry);
		assertEquals(10, scn.GetStrength(infantry));
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(9, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In the Infantry's own skirmish the bonus is gone: each skirmish is a phase of its own.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(infantry));
		assertInZone(Zone.SUPPORT, hope2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoEasterlingInPlayDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var hope = scn.GetShadowCard("hope");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hope);
		scn.MoveMinionsToTable(explorer, scout);
		assertInZone(Zone.SUPPORT, hope);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.ShadowAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Its cost is the condition itself, so it is offered with no Easterling to make stronger.
		assertTrue(scn.ShadowActionAvailable(hope));
		scn.ShadowUseCardAction(hope);

		assertInZone(Zone.DISCARD, hope);
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(9, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
