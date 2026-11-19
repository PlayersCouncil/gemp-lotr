package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aimenu", "4_51");
					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only
					// Legolas, Companion of the Ring: strength 6, no archer; his +2 is at a battleground or forest, which no
					// site of this path is.
					put("legolas", "11_21");
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: an [elven] Elf companion who is not Legolas; +3 only against a Nazgul

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KhazadAimenuStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Khazâd Ai-mênu
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Dwarf strength +2 (or strength +3 and <b>damage +1</b> if you spot Legolas).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aimenu");

		assertEquals("Khazâd Ai-mênu", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedInASkirmishAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aimenu = scn.GetFreepsCard("aimenu");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(aimenu);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInHand(aimenu);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(aimenu));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(aimenu));

		// Gimli, 6 with damage +1, beats the Savage, who survives with 2 wounds of 3.
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(aimenu);
		assertFalse(scn.FreepsPlayAvailable(aimenu));
	}

	@Test
	public void WithNoLegolasADwarfIsStrengthPlus2ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aimenu = scn.GetFreepsCard("aimenu");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(aimenu);
		// An Elf companion who is not Legolas stands in his place.
		scn.MoveCompanionsToTable(guard, gimli, aragorn, arwen);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInHand(aimenu);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsPlayCard(aimenu);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally in the support area;
		// not Aragorn, Arwen or Frodo, companions who are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// No Legolas: +2, and no damage bonus.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(grimir));
		assertInDiscard(aimenu);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: Gimli is still strengthened.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// The Guard, 4, loses to the Soldier, 7. A new skirmish phase: the bonus went with the last one.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void WithLegolasADwarfIsStrengthPlus3AndDamagePlus1ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aimenu = scn.GetFreepsCard("aimenu");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(aimenu);
		scn.MoveCompanionsToTable(guard, gimli, aragorn, legolas);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInHand(aimenu);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		scn.FreepsPlayCard(aimenu);
		// With Legolas spotted the Dwarf is still any Dwarf; Legolas is spotted, not chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// +3 in place of +2, and damage +1 on top of his printed damage +1: the larger figure, unasked.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertInDiscard(aimenu);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: both still stand.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The Guard loses. In Gimli's own skirmish, another skirmish phase, both are gone.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aimenu = scn.GetFreepsCard("aimenu");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(aimenu);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInHand(aimenu);
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier1);

		assertEquals(8, scn.GetStrength(aragorn));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(aimenu));

		scn.FreepsPlayCard(aimenu);
		assertInDiscard(aimenu);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
