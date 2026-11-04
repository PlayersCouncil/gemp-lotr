package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defiled", "13_105");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose

					put("prowler", "11_136");   // Prowling Orc: [orc] Orc lurker, strength 6; a skirmish ability used only on purpose
					put("horror", "14_13");     // Horror of Harad: [orc] Half-troll lurker, strength 9; keywords only
					put("column", "11_72");     // Column of Easterlings: [men] Man lurker, strength 8; a skirmish ability used only on purpose
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, no lurker, strength 5; adds (2) only when played underground
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defiled", "13_105");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion in the skirmish, strength 8
					// Howling Orc: an [orc] lurker minion, strength 8; his trigger answers an [orc] minion killed
					// in a skirmish
					put("howler", "13_109");

					// [orc] cards of four types for the discard pile, where none of them does anything
					put("underling", "11_125");     // Isengard Underling: minion
					put("demoralized", "11_114");   // Demoralized: condition
					put("spear", "12_96");          // Orc Spear: possession
					put("strength", "11_140");      // Strength in Shadows: event
					put("hordes", "11_123");        // Goblin Hordes: condition
					put("runner", "1_178");         // Goblin Runner: an Orc of the [moria] culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefiledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Defiled
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [orc] cards in your discard pile instead of paying the twilight cost for this card.<br>Make your [orc] lurker minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("defiled");

		assertEquals("Defiled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedByRemovingFourOrcCardsInTheDiscardPileFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var strength = scn.GetShadowCard("strength");
		var hordes = scn.GetShadowCard("hordes");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(defiled);
		scn.MoveCardsToDiscard(underling, demoralized, spear, strength, hordes, runner);

		scn.StartGame();
		assertInDiscard(underling, demoralized, spear, strength, hordes, runner);
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		// Nothing in the pool for an event of cost 2, and five [orc] cards in the discard pile.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(defiled));
		assertEquals(8, scn.GetStrength(howler));

		// Exactly four of the five are to be removed; the Orc of another culture is no candidate.
		scn.ShadowPlayCard(defiled);
		assertTrue(scn.ShadowHasCardChoiceAvailable(underling, demoralized, spear, strength, hordes));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(underling, demoralized, spear, strength);

		assertInZone(Zone.REMOVED, underling, demoralized, spear, strength);
		assertInDiscard(hordes, runner);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(defiled);
		assertEquals(10, scn.GetStrength(howler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolAndThreeOrcCardsInTheDiscardPileItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(defiled);
		// Four cards, three of them [orc].
		scn.MoveCardsToDiscard(underling, demoralized, spear, runner);

		scn.StartGame();
		assertInDiscard(underling, demoralized, spear, runner);
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(defiled));

		// With its cost in the pool it is offered, and paid for there: nothing is asked.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(defiled));
		assertEquals(8, scn.GetStrength(howler));

		scn.ShadowPlayCard(defiled);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(defiled, underling, demoralized, spear, runner);
		assertEquals(10, scn.GetStrength(howler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheTwilightAndFourOrcCardsBothWaysToPayAreAskedAndTheCardsAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsDefiled = scn.GetFreepsCard("defiled");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var strength = scn.GetShadowCard("strength");
		var hordes = scn.GetShadowCard("hordes");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(defiled);
		scn.MoveCardsToDiscard(underling, demoralized, spear, strength, hordes, runner);
		// The other player's [orc] card, in his own discard pile: a title the Shadow player's pile does not hold,
		// since a choice from a pile is read by blueprint.
		scn.MoveCardsToDiscard(freepsDefiled);

		scn.StartGame();
		assertInDiscard(underling, demoralized, spear, strength, hordes, runner, freepsDefiled);
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(howler));
		scn.ShadowPlayCard(defiled);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseYes();

		// Exactly four [orc] cards of any type; not the [moria] Orc, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(underling, demoralized, spear, strength, hordes));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, freepsDefiled));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(underling, demoralized, spear, hordes);

		assertInZone(Zone.REMOVED, underling, demoralized, spear, hordes);
		assertInDiscard(strength, runner, freepsDefiled);
		assertEquals(2, scn.GetTwilight());

		// The lone [orc] lurker is chosen unasked.
		assertEquals(10, scn.GetStrength(howler));
		assertInDiscard(defiled);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheRemovalPaysTheTwilightCostAndLeavesThePileAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToHand(defiled);
		scn.MoveCardsToDiscard(underling, demoralized, spear, strength);

		scn.StartGame();
		assertInDiscard(underling, demoralized, spear, strength);
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(defiled));
		assertEquals(8, scn.GetStrength(howler));
		scn.ShadowPlayCard(defiled);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseNo();

		assertEquals(0, scn.GetTwilight());
		assertInDiscard(underling, demoralized, spear, strength, defiled);
		assertEquals(10, scn.GetStrength(howler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesOneOfYourOrcLurkersStrengthPlus2ForThisSkirmishWhereverItIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var defiled = scn.GetShadowCard("defiled");
		var prowler = scn.GetShadowCard("prowler");
		var horror = scn.GetShadowCard("horror");
		var column = scn.GetShadowCard("column");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(defiled);

		scn.StartGame();
		scn.MoveMinionsToTable(prowler, horror, column, goblin);
		scn.SkipToAssignments();
		// The Goblin in the first skirmish, the Prowling Orc (a lurker) in a later one; the other two assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, goblin}, new PhysicalCardImpl[]{sam, prowler});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// No [orc] card in the discard pile: the twilight cost is paid, with nothing asked.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(defiled));
		scn.ShadowPlayCard(defiled);
		assertEquals(0, scn.GetTwilight());

		// An [orc] lurker of either race, in a later skirmish or in none; not the [orc] minion who is no lurker,
		// nor the [men] lurker, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowler, horror));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblin, column, aragorn, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.GetStrength(prowler));
		scn.ShadowChooseCard(prowler);

		assertEquals(8, scn.GetStrength(prowler));
		assertEquals(9, scn.GetStrength(horror));
		assertInDiscard(defiled);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In the Prowling Orc's own skirmish, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);
		assertEquals(6, scn.GetStrength(prowler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOrcLurkerInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var goblin = scn.GetShadowCard("goblin");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToHand(defiled);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, column);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(defiled));
		scn.ShadowPlayCard(defiled);

		assertEquals(0, scn.GetTwilight());
		assertInDiscard(defiled);
		assertEquals(5, scn.GetStrength(goblin));
		assertEquals(8, scn.GetStrength(column));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var prowler = scn.GetShadowCard("prowler");
		scn.MoveCardsToHand(defiled);

		scn.StartGame();
		scn.MoveMinionsToTable(prowler);
		scn.SetTwilight(4);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(defiled));
	}
}
