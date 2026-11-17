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

public class Card_04_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steel", "4_70");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					// Legolas, Companion of the Ring: Elf companion, strength 6, vitality 3; stronger only at a
					// battleground or forest site, which no site of this path is
					put("legolas", "11_21");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, vitality 3; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlashingSteelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Flashing Steel
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert an Elf to make a minion skirmishing that Elf strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("steel");

		assertEquals("Flashing Steel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToMakeAMinionSkirmishingThatElfStrengthMinus3ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steel = scn.GetFreepsCard("steel");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, legolas, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(steel);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage},
				new PhysicalCardImpl[]{gimli, runner});
		// The Troop is left over: the Shadow player puts it on Arwen too, so that two minions skirmish her.
		scn.ShadowAssignToMinions(arwen, troop);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsPlayAvailable(steel));

		scn.FreepsPlayCard(steel);
		// Any Elf may pay: a companion in the skirmish, one out of it, and an ally; not a Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		// Either minion skirmishing Arwen; not the Runner, assigned to Gimli, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(steel);

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetStrength(savage));

		// 2 + 13 against Arwen's 6 overwhelms her; in the next skirmish, Gimli's, the Savage is 5 again.
		scn.FreepsPass();
		assertInZone(Zone.DEAD, arwen);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage));
	}

	@Test
	public void ExertingAnElfWhoIsInNoSkirmishPlaysItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steel = scn.GetFreepsCard("steel");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToHand(steel);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPlayCard(steel);
		scn.FreepsChooseCard(legolas);

		// The Savage skirmishes an Elf, but not the Elf who exerted: the event is played, the exertion paid, and
		// nothing more happens.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertInDiscard(steel);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayedInTheAssignmentPhaseButIsInTheSkirmishThatFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steel = scn.GetFreepsCard("steel");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(steel);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsPlayAvailable(steel));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(steel));
	}

	@Test
	public void IsNotPlayableWhenTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steel = scn.GetFreepsCard("steel");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveCardsToHand(steel);
		// Vitality 3 with 2 wounds: Arwen cannot exert.  Gimli, beside her, can, but is no Elf.
		scn.AddWoundsToChar(arwen, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(steel);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(steel));
		assertInHand(steel);
	}
}
