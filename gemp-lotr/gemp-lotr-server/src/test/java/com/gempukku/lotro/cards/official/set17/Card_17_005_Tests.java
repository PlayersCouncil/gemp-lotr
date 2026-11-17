package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axework", "17_5");

					// Gimli, Son of Gloin: Dwarf companion, strength 6; "Skirmish: Exert Gimli to make him strength +2",
					// used only on purpose.
					put("gimli", "1_13");
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, strength 5; +2 only against an Orc
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, no Orc, strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeworkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Axe-work
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2 (or if you cannot spot a threat, make that Dwarf strength +1 for each
		 * Dwarf you can spot).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axework");

		assertEquals("Axe-work", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAThreatToSpotTheDwarfIsStrengthPlus2ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axework = scn.GetFreepsCard("axework");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(axework);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveMinionsToTable(savage, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(axework));

		scn.FreepsPlayCard(axework);
		// Any Dwarf: the one skirmishing, a companion in no skirmish and an ally.  Not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		// A threat can be spotted: +2, and not the +3 that the three Dwarves would give.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(3, scn.GetStrength(grimir));
		assertInZone(Zone.DISCARD, axework);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts the skirmish it was played in: gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatTheDwarfIsStrengthPlus1ForEachDwarfYouCanSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axework = scn.GetFreepsCard("axework");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(axework);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveMinionsToTable(savage, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(axework);
		scn.FreepsChooseCard(gimli);
		// No threat: +1 for each of Gimli, Farin and the ally Grimir, and none for Aragorn or Frodo.  Nothing
		// was asked about which Dwarves are spotted.
		assertEquals(9, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, axework);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts the skirmish it was played in: gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatAndOneDwarfTheBonusIsOnlyPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axework = scn.GetFreepsCard("axework");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axework);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(axework);
		// Gimli is the only Dwarf.  The branch in parentheses is mandatory when it is available (Ketura, F-0047,
		// Q-0035), so it is +1, though +2 would be the larger.
		assertEquals(7, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, axework);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axework = scn.GetFreepsCard("axework");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(axework);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.HAND, axework);
		assertFalse(scn.FreepsPlayAvailable(axework));
	}
}
