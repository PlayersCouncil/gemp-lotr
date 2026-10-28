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

public class Card_07_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("needed", "7_29");
					put("elf", "1_53");         // Lórien Elf: an [elven] Elf companion, strength 4, no archer; no text once in play
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, strength 6 (+3 only against a Nazgûl)
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally, strength 5, home site 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, Damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillNeededStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Still Needed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +2. You may place this event on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("needed");

		assertEquals("Still Needed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlusTwoForTheSkirmishAndMayGoOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var needed = scn.GetFreepsCard("needed");
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elf, arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(needed);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elf, savage}, new PhysicalCardImpl[]{aragorn, runner});
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));
		int deckBefore = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(needed);

		// Any Elf: the one skirmishing, a companion in no skirmish, an ally; no companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(elf);

		assertEquals(6, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseYes();
		assertEquals(needed, scn.GetFreepsTopOfDeck());
		assertEquals(deckBefore + 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +2 at the next decision; the Elf (6) beats the Savage (5).
		assertEquals(6, scn.GetStrength(elf));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(elf));

		// In the next skirmish the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAnElfInNoSkirmishStrongerAndDeclinedGoesToTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var needed = scn.GetFreepsCard("needed");
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(elf, arwen);
		scn.MoveCardsToHand(needed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(4, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		int deckBefore = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(needed);
		scn.FreepsChooseCard(arwen);

		// Arwen, in no skirmish, is made stronger all the same; the Elf skirmishing is not.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(elf));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseNo();
		assertInDiscard(needed);
		assertEquals(deckBefore, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var needed = scn.GetFreepsCard("needed");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(needed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(needed));
	}
}
