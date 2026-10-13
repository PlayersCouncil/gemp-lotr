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

public class Card_11_210_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_210");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "11_210");
					// Black Rider: [wraith] Nazgul, fierce, strength 8 and +1 for each companion: 11 beside three
					put("rider1", "12_161");
					put("rider2", "12_161");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4: he loses twice and lives
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3: he loses twice (11 is not double 6) and lives
					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] minion, for the discard pile
					put("breath", "1_207");     // Black Breath: a [wraith] condition, for the discard pile
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, the card in the discard pile that is not [wraith]
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HatredStirredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hatred Stirred
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a companion loses a fierce skirmish involving a [wraith] minion, you may shuffle a [wraith] card from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hatred Stirred", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// F-0473.  Ketura on Q-0243 (2026-10-11), "Go for B": "you may shuffle a [wraith] card from your discard pile into your
	// draw deck" is not offered with no [wraith] card in that pile (Q-0034; as Q-0112 for Mumakil Commander).
	@Test
	public void TheShuffleIsNotOfferedWithNoWraithCardInTheDiscardPileAndWithOneShufflesTheCardChosenIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hatred = scn.GetShadowCard("hatred");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var lemenya = scn.GetShadowCard("lemenya");
		var breath = scn.GetShadowCard("breath");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(rider1, rider2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider1}, new PhysicalCardImpl[]{gimli, rider2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The fierce skirmishes.  Aragorn (8) loses to a Black Rider (8 + 3 companions) with only the Goblin
		// Runner in the Shadow player's discard pile.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider1}, new PhysicalCardImpl[]{gimli, rider2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Two [wraith] cards join the Runner there before Gimli (6) loses his.
		scn.MoveCardsToDiscard(lemenya, breath);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hatred));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(breath);

		assertInZone(Zone.DECK, breath);
		assertInDiscard(lemenya, runner);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void HatredStirredTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
