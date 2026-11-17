package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blow", "1_5");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no damage bonus
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only

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
	public void CleavingBlowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cleaving Blow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Dwarf strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blow");

		assertEquals("Cleaving Blow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfMadeStrengthPlus2AndDamagePlus1WinsHisSkirmishAndWoundsTheMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		assertInHand(blow);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInHand(blow);
		assertTrue(scn.FreepsPlayAvailable(blow));

		// The Guard is the only Dwarf: no choice is asked.
		scn.FreepsPlayCard(blow);
		assertInDiscard(blow);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();

		// 6 against 5: the Guard wins, where at 4 he would have lost. 1 wound for losing + 1 for damage +1.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(savage));

		// The skirmish is over and so are the bonuses.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
	}

	@Test
	public void CanBePlayedInASkirmishAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInHand(blow);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blow));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(blow));

		// Gimli, 6 with damage +1, beats the Savage, who survives with 2 wounds of 3.
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(blow);
		assertFalse(scn.FreepsPlayAvailable(blow));
	}

	@Test
	public void ADwarfWaitingForHisOwnSkirmishIsStrengthenedOnlyForThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsPlayCard(blow);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally in the support area;
		// not Aragorn or Frodo, companions who are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// Gimli's own damage +1 and the event's add up.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(grimir));
		assertFalse(scn.HasKeyword(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: Gimli is still strengthened.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The Guard, 4, loses to the Soldier, 7.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// A new skirmish phase: the bonuses went with the last one.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier1);

		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(blow));

		scn.FreepsPlayCard(blow);
		assertInDiscard(blow);
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
