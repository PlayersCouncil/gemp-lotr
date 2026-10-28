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

public class Card_07_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("preparations", "7_12");
					put("preparations2", "7_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability, not used here
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, vitality 2; a play requirement only
					// Thrarin, Dwarven Smith: Dwarf ally, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, strength 8; a maneuver ability, not used here
					// Here Is Good Rock: [dwarven] condition in the support area; "Skirmish: Stack a Free Peoples card
					// from hand here to make a Dwarf damage +1" is how a card comes to lie on it.  Not used here.
					put("rock", "4_50");
					// Cards to stack or to lie in the discard pile, none with text that acts from either place
					put("guard2", "1_7");       // Dwarf Guard: a [dwarven] companion card
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("axe2", "1_9");
					put("armory", "4_47");      // From the Armory: a [dwarven] condition
					put("toby", "1_305");       // Old Toby: a [shire] possession
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" only
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli in a skirmish against two Uruk Savages, with two [dwarven] cards stacked on the condition.
	protected VirtualTableScenario GetTwoMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("preparations", "7_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] card
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] card
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PreparationsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Preparations
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Regroup:</b> Exert a Dwarf companion to stack a card from your discard pile here.<br><b>Skirmish:</b> Wound a minion skirmishing a Dwarf once for each [dwarven] card stacked here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("preparations");

		assertEquals("Preparations", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Wound a minion skirmishing a Dwarf once for each [dwarven] card stacked here": with "for each" the wounds
	// may go to several minions or to one more than once (CR 4.2 "for each"; the shape of F-0224).  The
	// definition chose one minion and wounded it once for each card.
	// F-0459.
	@Test
	public void TheWoundsForEachStackedDwarvenCardMayGoToDifferentMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoMinionsScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, scn.GetFreepsCard("axe"), scn.GetFreepsCard("armor"));

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, preparations);
		assertEquals(2, scn.GetStackedCards(preparations).size());
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.ShadowAssignToMinions(gimli, savage2);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(preparations));
		scn.FreepsUseCardAction(preparations);

		// The first card's wound: either minion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// The second card's wound: either minion again, and it goes to the other.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage2);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		assertInZone(Zone.DISCARD, preparations);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsADwarfCompanionToStackACardOfYourChoiceFromYourDiscardPileHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var preparations2 = scn.GetFreepsCard("preparations2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(preparations, preparations2);
		// A Free Peoples companion, a Free Peoples possession and a Shadow minion in the discard pile.
		scn.MoveCardsToDiscard(guard2, axe, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInDiscard(guard2, axe, troop);
		assertEquals(0, scn.GetStackedCards(preparations).size());
		assertEquals(0, scn.GetStackedCards(preparations2).size());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(preparations));
		scn.FreepsUseCardAction(preparations);

		// The cost: a Dwarf companion, and not a Dwarf ally or a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// "A card": every card of the discard pile, a Shadow card of your own among them; one of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, axe, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		// Stacked here, on the copy used, and not on the other copy.
		assertInZone(Zone.STACKED, troop);
		assertEquals(1, scn.GetStackedCards(preparations).size());
		assertTrue(scn.GetStackedCards(preparations).contains(troop));
		assertEquals(0, scn.GetStackedCards(preparations2).size());
		assertInDiscard(guard2, axe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheDiscardPileEmptyTheRegroupAbilityIsOfferedAndTheExertionPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(preparations);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(preparations));
		scn.FreepsUseCardAction(preparations);

		// Gimli, the only Dwarf companion, exerts unasked; there is nothing to stack.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetStackedCards(preparations).size());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithItsOnlyDwarfCompanionExhaustedTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(preparations);
		scn.MoveCardsToDiscard(axe);
		// Vitality 3 with 2 wounds: exhausted.  The Dwarf ally and the companion who is no Dwarf could exert.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInDiscard(axe);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(preparations));
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(preparations);
		scn.MoveCardsToDiscard(axe);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(preparations));
	}

	@Test
	public void SkirmishAbilityWoundsAMinionSkirmishingADwarfOnceForEachDwarvenCardStackedHereAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var preparations2 = scn.GetFreepsCard("preparations2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var armory = scn.GetFreepsCard("armory");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(preparations, preparations2, rock);
		// Here: three [dwarven] cards of three types and a [shire] card.  On the other copy, one [dwarven] card;
		// on another [dwarven] condition, one more.
		scn.StackCardsOn(preparations, guard, axe, armory, toby);
		scn.StackCardsOn(preparations2, guard2);
		scn.StackCardsOn(rock, axe2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();

		assertInZone(Zone.STACKED, guard, axe, armory, toby, guard2, axe2);
		scn.MoveMinionsToTable(savage, runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, runner2});
		// The Shadow player puts the minion left over on Gimli too.
		scn.ShadowAssignToMinions(gimli, runner);
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage));
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner));

		// Aragorn's skirmish first, while Gimli's two minions wait for theirs.  The other copy is used here:
		// the minion skirmishing Aragorn is skirmishing no Dwarf, and the two assigned to Gimli are not yet
		// skirmishing, so nobody is wounded.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(preparations2));
		scn.FreepsUseCardAction(preparations2);

		assertEquals(0, scn.GetWoundsOn(runner2));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(preparations2, guard2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn (8) beats the Runner (5), who dies of the wound.
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner2);

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStackedCards(preparations).size());
		scn.FreepsUseCardAction(preparations);

		// Three [dwarven] cards here: three wounds, each to a minion skirmishing Gimli, and one minion may
		// take more than one (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// The Runner, exhausted at vitality 1, is offered for a wound and is killed by it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);
		assertEquals(2, scn.GetWoundsOn(savage));

		// No fourth wound: the [shire] card here and the [dwarven] card on the Rock are not counted.  Then the
		// condition is discarded, and what was stacked on it with it (CR 4.2 "leaving play").
		assertInDiscard(preparations, guard, axe, armory, toby);
		assertInZone(Zone.STACKED, axe2);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWoundsAMinionSkirmishingADwarfAllyOnceForTheOneDwarvenCardStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var thrarin = scn.GetFreepsCard("thrarin");
		var axe = scn.GetFreepsCard("axe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, axe, toby);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, axe, toby);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(preparations));
		scn.FreepsUseCardAction(preparations);

		// One [dwarven] card here: one wound, to the only minion skirmishing a Dwarf, with nothing asked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(preparations, axe, toby);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, axe);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, axe);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(preparations));

		// In the skirmish that follows it is offered.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(preparations));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
