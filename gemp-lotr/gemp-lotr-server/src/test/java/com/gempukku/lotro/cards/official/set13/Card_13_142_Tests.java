package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_142");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer: a Ring-bearer who is no Hobbit.  His own text acts only at
	// the start of a skirmish involving him.
	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "13_142");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit; a Fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Bilbo, Aged Ring-bearer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ring-bound</b>.<br>While Bilbo is not assigned to a skirmish, each Ring-bound Hobbit is strength +1.<br>At the start of each skirmish involving Bilbo, you may remove a burden for each other Ring-bound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Aged Ring-bearer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A burden for each
	// other Ring-bound Hobbit you can spot" is no burden where Bilbo is the only Ring-bound Hobbit.
	@Test
	public void TheBurdenRemovalIsNotOfferedWithNoOtherRingBoundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		// A burden is there to remove; the Ring-bearer is a Dwarf.
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBurdenRemovalIsOfferedWithAnotherRingBoundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(bilbo, sam);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BilboTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
