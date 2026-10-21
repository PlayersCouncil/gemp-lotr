package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nlm", "8_90");
					// Théoden, Leader of Spears: [rohan] Man companion, Théoden signet, vitality 3; an optional trigger
					// on a move (declined by the skips) and a cost of his own lowered
					put("theoden", "7_254");
					// Éowyn, Lady of Rohan: [rohan] Man companion, Théoden signet, vitality 3; a response to a wound on
					// an unbound companion, which no test sets off
					put("eowyn", "4_270");
					put("gimli", "7_6");        // Gimli, Faithful Companion: [dwarven] Dwarf, Théoden signet; +2 only with initiative
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Gandalf signet; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play trigger does not fire when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoLivingManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: No Living Man
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Théoden to exert a minion for each Théoden signet you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nlm");

		assertEquals("No Living Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheodenToExertAMinionForEachOfThreeTheodenSignets() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nlm = scn.GetFreepsCard("nlm");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, eowyn, gimli, aragorn);
		scn.MoveCardsToHand(nlm);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, soldier, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, savage);
		// The Orc Soldier and the Goblin Runner are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.IsCharSkirmishingAgainst(theoden, savage));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(nlm));
		scn.FreepsPlayCard(nlm);
		assertEquals(1, scn.GetWoundsOn(theoden));

		// Théoden signets on Théoden, Éowyn and Gimli (a [dwarven] Dwarf); Aragorn's Gandalf signet and Frodo's
		// Frodo signet are not counted: three exertions.  Any minion, in the skirmish or not; not the exhausted
		// Runner.  Each exertion may be passed over, as fewer signets may be spotted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));

		// One minion may be exerted more than once (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertInDiscard(nlm);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneTheodenSignetItExertsOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nlm = scn.GetFreepsCard("nlm");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, aragorn);
		scn.MoveCardsToHand(nlm);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsPlayCard(nlm);
		assertEquals(1, scn.GetWoundsOn(theoden));

		// Théoden's own signet alone: one exertion, and no second choice.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, soldier));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInDiscard(nlm);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nlm = scn.GetFreepsCard("nlm");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eowyn);
		scn.MoveCardsToHand(nlm);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Vitality 3, 2 wounds: exhausted.  Éowyn beside him can exert, but she is no Théoden.
		scn.AddWoundsToChar(theoden, 2);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertInHand(nlm);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, savage);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(nlm);
		assertFalse(scn.FreepsPlayAvailable(nlm));
		assertEquals(0, scn.GetWoundsOn(eowyn));
	}
}
