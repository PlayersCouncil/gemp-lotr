package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("library", "9_39");
					// Uruk Savage: [isengard] Uruk-hai, cost 2, strength 5, vitality 3, site 5; Damage +1 is its whole text.
					put("savage", "1_151");
					put("savage2", "1_151");
					// Isengard Worker: [isengard] Orc, cost 2, strength 5, vitality 3, site 4; a regroup ability that is
					// never used here.
					put("worker", "3_62");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, strength 5, site 4; never played here
					// Uruk-hai Armory: [isengard] condition, cost 1, played to the support area with nothing to spot.
					put("armory", "1_157");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: [gondor] companion, strength 6, vitality 3, no text
					put("knight2", "5_35");
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("strike", "1_3");       // Axe Strike: a [dwarven] skirmish event
					// The Shards of Narsil: a Free Peoples artifact that [gondor] cards are stacked on by its own text.
					put("shards", "3_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}


	@Test
	public void LibraryOfOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Library of Orthanc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: <b>Shadow:</b> Play an [isengard] minion to stack the top card of your draw deck on this card.
		 * <b>Skirmish:</b> Remove (1) and discard a Free Peoples card stacked here to make an [isengard] minion strength +1.
		 * <b>Regroup:</b> Remove (1) to take an [isengard] card stacked here into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("library");

		assertEquals("Library of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityStacksTopCardOfDeckAfterPlayingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		var axe = scn.GetShadowCard("axe");
		var strike = scn.GetShadowCard("strike");
		scn.MoveCardsToHand(library, savage, worker, runner, armory);

		scn.StartGame();
		// The Dwarven Axe is the top card of the Shadow player's draw deck, and Axe Strike the one beneath it.
		scn.MoveCardsToTopOfDeck(strike, axe);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 2 for the site.
		assertEquals(23, scn.GetTwilight());
		scn.ShadowPlayCard(library);
		assertEquals(21, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, library);
		assertNull(library.getAttachedTo());

		// Each [isengard] minion in hand costs 2 + 2 roaming.  With 3 in the pool the Goblin Runner (1 + 2) and
		// the Uruk-hai Armory (1) can be played, and neither is an [isengard] minion.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertTrue(scn.ShadowPlayAvailable(armory));
		assertFalse(scn.ShadowActionAvailable(library));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(library));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(axe, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetStackedCards(library).size());

		scn.ShadowUseCardAction(library);
		assertTrue(scn.ShadowDecisionAvailable("Choose card to play from hand"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, armory));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// 10 - 2 - 2 roaming.
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// The one card that was on top of the Shadow player's deck, and no other.
		assertEquals(1, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, axe);
		assertEquals(library, axe.getStackedOn());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(strike, scn.GetShadowTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		// A second use in the same phase: the Worker is the only [isengard] minion left in hand.
		scn.ShadowUseCardAction(library);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, strike);
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.HAND, runner, armory);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheMinionIsStillPlayedAndNothingIsStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(library);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(scn.GetShadowCard("savage2"), scn.GetShadowCard("worker"), scn.GetShadowCard("runner"),
				scn.GetShadowCard("armory"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("knight"),
				scn.GetShadowCard("knight2"), scn.GetShadowCard("axe"), scn.GetShadowCard("strike"),
				scn.GetShadowCard("shards"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowDeckCount());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		// The play is a cost that can be paid, so the ability is offered.
		assertTrue(scn.ShadowActionAvailable(library));

		scn.ShadowUseCardAction(library);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(9, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(library).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemoves1AndDiscardsStackedFreepsCardToAdd1Strength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var worker = scn.GetShadowCard("worker");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		var axe = scn.GetShadowCard("axe");
		var strike = scn.GetShadowCard("strike");
		var stackedAragorn = scn.GetShadowCard("aragorn");

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(library, shards);
		// The Shadow player's own Free Peoples cards, of three types and two cultures, and one [isengard] card.
		scn.StackCardsOn(library, axe, stackedAragorn, strike, armory);
		// A Free Peoples card stacked on another card.
		scn.StackCardsOn(shards, knight2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, worker, runner);

		// Not a maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(library));
		scn.ShadowPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{knight, worker});
		// The second Savage is left unassigned.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// With nothing in the pool the cost cannot be paid.
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(library));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(library));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(4, scn.GetStackedCards(library).size());
		int shadowDiscard = scn.GetShadowDiscardCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();

		scn.ShadowUseCardAction(library);
		// The Free Peoples cards stacked here; not the [isengard] card stacked here, nor the card on the Shards.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, stackedAragorn, strike));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, knight2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stackedAragorn);

		// Each [isengard] minion, in this skirmish or not; not the [moria] minion fighting beside the Savage, nor the
		// Library, an [isengard] card in play that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, savage2, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, library));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertEquals(2, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(5, scn.GetStrength(runner));
		// The card goes to its owner's discard pile, and the rest stay where they are.
		assertInZone(Zone.DISCARD, stackedAragorn);
		assertEquals(shadowDiscard + 1, scn.GetShadowDiscardCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, axe, strike, armory, knight2);

		// A minion assigned to a skirmish that has not begun ...
		scn.FreepsPass();
		scn.ShadowUseCardAction(library);
		scn.ShadowChooseCard(axe);
		scn.ShadowChooseCard(worker);
		assertEquals(1, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(worker));

		// ... and one assigned to nobody.  Axe Strike is the last Free Peoples card on the stack.
		scn.FreepsPass();
		scn.ShadowUseCardAction(library);
		scn.ShadowChooseCard(savage2);
		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(savage2));
		assertInZone(Zone.DISCARD, axe, strike);
		assertEquals(1, scn.GetStackedCards(library).size());

		// Only the [isengard] card is left on the stack: with the pool refilled there is still nothing to discard.
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(library));

		// Still +1 as the skirmish is about to end.
		assertEquals(6, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(worker));
		assertEquals(6, scn.GetStrength(savage2));

		// Aragorn, 8, loses to the Savage and the Runner, 6 + 5.  That skirmish over, each is back to 5.
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(5, scn.GetStrength(savage2));

		scn.FreepsResolveSkirmish(knight);
		assertEquals(5, scn.GetStrength(worker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RegroupAbilityRemoves1AndTakesStackedIsengardCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetShadowCard("library");
		var worker = scn.GetShadowCard("worker");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		var axe = scn.GetShadowCard("axe");

		scn.MoveCardsToSupportArea(library);
		// An [isengard] minion and an [isengard] condition; a Free Peoples card and a [moria] minion.
		scn.StackCardsOn(library, axe, runner, worker, armory);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// With nothing in the pool the cost cannot be paid.
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(library));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(library));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetStackedCards(library).size());

		scn.ShadowUseCardAction(library);
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, armory));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armory);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.HAND, armory);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, axe, runner, worker);

		// The Worker is the only [isengard] card left on the stack, and is taken with no question.
		scn.FreepsPass();
		scn.ShadowUseCardAction(library);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.HAND, worker);
		assertEquals(2, scn.GetShadowHandCount());

		// With no [isengard] card stacked the ability still has a cost that can be paid: (1) for nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(library));
		scn.ShadowUseCardAction(library);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetStackedCards(library).size());
		assertInZone(Zone.STACKED, axe, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
