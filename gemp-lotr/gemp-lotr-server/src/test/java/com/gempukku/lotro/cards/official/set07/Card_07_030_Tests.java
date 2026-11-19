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

public class Card_07_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "7_30");
					put("elf", "1_53");         // Lórien Elf: an [elven] Elf companion, strength 4; no text once in play
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
	public void UncertainPathsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Uncertain Paths
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +2 and you may shuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paths");

		assertEquals("Uncertain Paths", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlusTwoForTheSkirmishAndShufflesYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var frodo = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(elf, arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(paths);

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
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(paths);

		// Any Elf: the one skirmishing, a companion in no skirmish, an ally; no companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(elf);

		assertEquals(6, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));

		assertTrue(scn.FreepsDecisionAvailable("shuffle your draw deck"));
		scn.FreepsChooseYes();
		// "Your" draw deck: the Free Peoples player's, once; the Shadow player's is left alone.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(paths);
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
	public void MakesAnElfInNoSkirmishStrongerAndTheShuffleMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(elf, arwen);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(4, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(paths);
		scn.FreepsChooseCard(arwen);

		// Arwen, in no skirmish, is made stronger all the same; the Elf skirmishing is not.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(elf));

		assertTrue(scn.FreepsDecisionAvailable("shuffle your draw deck"));
		scn.FreepsChooseNo();
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(8, scn.GetStrength(arwen));
		assertInDiscard(paths);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoElfInPlayItIsPlayedForNothingButTheShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(paths));
		scn.FreepsPlayCard(paths);

		// Nobody is made stronger; the shuffle is still the player's to take.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("shuffle your draw deck"));
		scn.FreepsChooseYes();
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(paths);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(paths));
	}
}
