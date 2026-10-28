package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaked", "7_14");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaked", "7_14");
					put("gimli", "1_13");       // Gimli, Son of Glóin: the Dwarf companion spotted
					// Here Is Good Rock: [dwarven] condition in the support area.  "Skirmish: Stack a Free Peoples
					// card from hand here to make a Dwarf damage +1" is how a card comes to be stacked on it.
					put("rock", "4_50");
					put("savage", "1_151");     // Uruk Savage: vitality 3, so it can exert twice
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3 as well, and no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlakedThirstsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Slaked Thirsts
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Choose one: Spot a Dwarf to draw a card; or, if this card is stacked on a [dwarven] condition, spot a Dwarf companion and discard this event to exert a minion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slaked");

		assertEquals("Slaked Thirsts", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StackedOnADwarvenConditionItIsDiscardedInTheManeuverPhaseToExertAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.STACKED, slaked);
		assertEquals(List.of(slaked), scn.GetStackedCards(rock));
		assertEquals(0, scn.GetWoundsOn(savage));
		// The event is not in hand and is not played: the stacked card itself has the action.
		assertTrue(scn.FreepsActionAvailable(slaked));
		scn.FreepsUseCardAction(slaked);

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(slaked);
		assertEquals(0, scn.GetStackedCards(rock).size());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaked", "7_14");
					put("slaked2", "7_14");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability, not used here
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion of another title; a play requirement only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally; a maneuver ability, not used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability, not used here
					// Here Is Good Rock: [dwarven] condition in the support area.  "Skirmish: Stack a Free Peoples
					// card from hand here to make a Dwarf damage +1" is how a card comes to be stacked on it.
					put("rock", "4_50");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("scout", "1_191");      // Moria Scout: vitality 2, so it can exert once only; a "when you play" only
					put("runner", "1_178");     // Goblin Runner: vitality 1; a "when you play" only
					// Cards for the draw deck, with no text
					put("brigand1", "4_10");    // Dunlending Brigand
					put("brigand2", "4_10");
					put("brigand3", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlayedFromHandWithADwarfToSpotItDrawsACardAndAsksNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var slaked2 = scn.GetFreepsCard("slaked2");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		// A second copy stacked on a [dwarven] condition: the copy in hand is not stacked, and so has only the
		// first alternative.
		scn.StackCardsOn(rock, slaked2);
		scn.MoveCardsToHand(slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked2);
		assertInHand(slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(slaked));
		scn.FreepsPlayCard(slaked);

		// No choice is asked: the event left the hand and one card was drawn.
		assertInDiscard(slaked);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.STACKED, slaked2);
		assertTrue(scn.GetStackedCards(rock).contains(slaked2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutADwarfThoughADwarvenCardIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToHand(slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInHand(slaked);
		assertInZone(Zone.SUPPORT, rock);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(slaked));
		assertInHand(slaked);
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInHand(slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(slaked));
		scn.FreepsPlayCard(slaked);

		assertInDiscard(slaked);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StackedItOffersOnlyAMinionThatCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		// Dwarf Guard is the only Dwarf companion to spot.
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked);
		scn.MoveMinionsToTable(savage, troop, scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(slaked));
		scn.FreepsUseCardAction(slaked);

		assertInDiscard(slaked);
		// Vitality 3 can exert twice; vitality 2 and 1 cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scout, runner, guard));
		scn.FreepsChooseCard(troop);

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SUPPORT, rock);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StackedWithNoMinionAbleToExertTwiceItIsOfferedAndTheOneThatCanExertExertsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked);
		scn.MoveMinionsToTable(scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(slaked));
		scn.FreepsUseCardAction(slaked);

		// As much as can be done is done (CR 4.2 "effect"): the Scout, the only minion able to exert at all,
		// exerts once, with nothing asked; the exhausted Runner does not.
		assertInDiscard(slaked);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheDiscardPileItHasNoAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToDiscard(slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInDiscard(slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slaked));
	}

	@Test
	public void StackedItIsNotOfferedWithADwarfAllyAndNoDwarfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slaked));
	}

	@Test
	public void StackedItIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slaked));
		assertInZone(Zone.STACKED, slaked);
	}

	// CR 5.0 "hinder": "If Slaked Thirsts is stacked on a [Dwarven] condition which later is hindered, Slaked
	// Thirsts' conditional ability is no longer able to be invoked".
	@Test
	public void StackedOnADwarvenConditionThatIsThenHinderedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.STACKED, slaked);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(slaked));

		scn.HinderCard(rock);
		scn.FreepsRefreshDecision();

		assertTrue(scn.IsHindered(rock));
		assertTrue(scn.GetStackedCards(rock).contains(slaked));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slaked));
	}
}
