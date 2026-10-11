package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "11_170");
					// Gimli, Lively Combatant: unbound Dwarf, resistance 7, damage +1; he may draw a card when he
					// wins a skirmish
					put("gimli", "11_8");
					// Legolas, Companion of the Ring: unbound Elf, resistance 7, and strength +2 and resistance +2
					// at a forest: at site 2 his resistance is Pippin's
					put("legolas", "11_21");
					// Aragorn, Strider: unbound Man, strength 8, resistance 8, one less than Pippin's; he adds (1)
					// each time the fellowship moves
					put("aragorn", "11_54");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, resistance 5
					put("bounder", "1_286");    // Bounder: Hobbit ally, strength 2, home site 2; an ally's resistance is 0

					// Goblin Runner: strength 5, vitality 1.  It beats Pippin (3) without overwhelming him and
					// loses to Gimli, Legolas and Aragorn, who would each kill it.
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_329"); // Breeland Forest: a forest; stealth events may not be played
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pippin, Brave Decoy
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Game Text: <b>Skirmish:</b> If Pippin is not assigned to a skirmish, spot an unbound companion who has less resistance than Pippin to have Pippin replace him or her in a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Brave Decoy", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(9, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedFromHandForOneAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		assertInZone(Zone.HAND, pippin);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReplacesAnUnboundCompanionOfLessResistanceInTheSkirmishBeingFoughtAndFightsItOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(pippin, aragorn, gimli);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, runner1 },
				new PhysicalCardImpl[] {   gimli, runner2 }
		);

		// Each resistance is one lower for the burden: 9 - 1, 8 - 1 (one less than Pippin's) and 7 - 1.
		assertEquals(8, scn.GetResistance(pippin));
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner1));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		// Gimli is only assigned: his skirmish has not begun, so he is in no skirmish to be replaced in, and
		// nothing is asked.  Pippin takes Aragorn's place against the same Runner.
		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner2));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		// Pippin is in the skirmish now, and there is nobody else in it to replace.
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();

		// 3 against 5: Pippin loses the skirmish Aragorn (8) would have won, and the Runner lives.
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		// His skirmish is over, so he is assigned to none, and he takes a Dwarf's place as he took a Man's.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner2));
		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner2));
		assertFalse(scn.IsCharSkirmishing(gimli));

		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionOfEqualResistanceARingBoundCompanionAndAnAllyAreNotReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin, legolas, sam, gimli);
		scn.MoveAlliesToTable(bounder);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner1, runner2, runner3);

		// Site 2 is the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { legolas, runner1 },
				new PhysicalCardImpl[] {     sam, runner2 },
				new PhysicalCardImpl[] { bounder, runner3 }
		);

		assertEquals(8, scn.GetResistance(pippin));
		// 7 - 1 for the burden + 2 at a forest: Pippin's own resistance, and not less.
		assertEquals(8, scn.GetResistance(legolas));
		assertEquals(4, scn.GetResistance(sam));
		// Gimli is an unbound companion of less resistance, and is in no skirmish: there is one to spot and
		// nobody for Pippin to replace.
		assertEquals(6, scn.GetResistance(gimli));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(pippin));

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishing(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		scn.PassCurrentPhaseActions();

		// Sam's resistance is the lowest at the table, and he is Ring-bound.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		scn.PassCurrentPhaseActions();

		// The Bounder is an ally.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishing(bounder));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhilePippinIsAssignedToASkirmishOfHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(pippin, gimli);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {  gimli, runner1 },
				new PhysicalCardImpl[] { pippin, runner2 }
		);

		assertEquals(8, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(gimli));

		// Gimli, whom Pippin could replace, is fighting; Pippin's own skirmish is still to come.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner1));
		assertTrue(scn.IsCharAssignedAgainst(pippin, runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));

		// Gimli wins, and may draw a card for it.
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		// Nor in his own skirmish, where he is the one fighting.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner2));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
