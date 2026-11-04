package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palaces", "6_10");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion; a Skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a Fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					// From the Armory: [dwarven] support-area condition, not unique, cost 0; a Skirmish ability
					// stacks Free Peoples cards on it
					put("armory", "4_47");
					put("armory2", "4_47");
					// Here Is Good Rock: unique [dwarven] tale in the support area; a Skirmish ability stacks
					// Free Peoples cards on it
					put("rock", "4_50");
					// Wish For Our Kinfolk: [dwarven] support-area condition that holds [dwarven] tokens of its own
					put("wish", "8_8");
					put("stacked1", "1_3");     // Axe Strike: a Free Peoples card, stacked on the conditions
					put("stacked2", "1_3");
					put("top", "1_53");         // Lórien Elf: the top card of the draw deck
					put("second", "1_9");       // Dwarven Axe: the card beneath it
					// Teeth of Mordor: [sauron] support-area condition of the Shadow player's, whose Regroup
					// ability stacks a [sauron] Orc on it
					put("teeth", "5_110");
					put("trooper", "3_99");     // Orc Trooper: the [sauron] Orc stacked on the Teeth
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuspendedPalacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Suspended Palaces
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area. When you play this condition, you may spot a Dwarf to place 2 [dwarven] tokens here.<br><b>Fellowship:</b> Discard this condition or remove a [dwarven] token from here to stack the top card of your draw deck on a [dwarven] condition that has a card stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palaces");

		assertEquals("Suspended Palaces", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedBesideADwarfCompanionItMayPlaceTwoDwarvenTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(palaces);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(armory));
		scn.FreepsPlayCard(palaces);

		assertInZone(Zone.SUPPORT, palaces);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsAcceptOptionalTrigger();

		// Two tokens, and on this condition alone.
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetCultureTokensOn(armory));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(palaces);

		scn.StartGame();

		scn.FreepsPlayCard(palaces);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarfToSpotNoTokensAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(palaces);

		scn.StartGame();

		scn.FreepsPlayCard(palaces);

		// Aragorn is a companion and no Dwarf.
		assertInZone(Zone.SUPPORT, palaces);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(palaces));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(palaces);

		scn.StartGame();

		scn.FreepsPlayCard(palaces);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, palaces);
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherDwarvenConditionPlayedAfterItPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces);
		scn.MoveCardsToHand(armory);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(palaces));
		scn.FreepsPlayCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(palaces));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetCultureTokensOn(armory));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityRemovesATokenToStackTheTopCardOnADwarvenConditionThatHasACardStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		var armory2 = scn.GetFreepsCard("armory2");
		var rock = scn.GetFreepsCard("rock");
		var stacked1 = scn.GetFreepsCard("stacked1");
		var stacked2 = scn.GetFreepsCard("stacked2");
		var top = scn.GetFreepsCard("top");
		var second = scn.GetFreepsCard("second");
		var teeth = scn.GetShadowCard("teeth");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces, armory, armory2, rock);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(armory, stacked1);
		scn.StackCardsOn(rock, stacked2);
		scn.StackCardsOn(teeth, trooper);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 2);
		scn.SetDrawDeck(scn.P1(), top, second);

		assertInZone(Zone.SUPPORT, palaces);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(List.of(stacked1), scn.GetStackedCards(armory));
		assertEquals(List.of(stacked2), scn.GetStackedCards(rock));
		assertEquals(List.of(trooper), scn.GetStackedCards(teeth));
		assertEquals(0, scn.GetStackedCards(armory2).size());
		assertEquals(top, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());

		assertTrue(scn.FreepsActionAvailable(palaces));
		scn.FreepsUseCardAction(palaces);

		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token"));
		scn.FreepsChooseOption("token");

		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertInZone(Zone.SUPPORT, palaces);

		// Each [dwarven] condition with a card stacked on it; not one with none (the second Armory, the Palaces
		// itself), nor a [sauron] condition with a card stacked on it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, rock));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory2, palaces, teeth));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rock);

		// The top card alone.
		assertEquals(List.of(stacked2, top), scn.GetStackedCards(rock));
		assertInZone(Zone.STACKED, top);
		assertEquals(List.of(stacked1), scn.GetStackedCards(armory));
		assertEquals(second, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheConditionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		var wish = scn.GetFreepsCard("wish");
		var stacked1 = scn.GetFreepsCard("stacked1");
		var top = scn.GetFreepsCard("top");
		var second = scn.GetFreepsCard("second");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces, armory, wish);
		scn.StackCardsOn(armory, stacked1);

		scn.StartGame();
		// A [dwarven] token on another card is no token "here".
		scn.AddTokensToCard(wish, 1);
		scn.SetDrawDeck(scn.P1(), top, second);

		assertInZone(Zone.SUPPORT, palaces);
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(List.of(stacked1), scn.GetStackedCards(armory));
		assertEquals(top, scn.GetFreepsTopOfDeck());

		assertTrue(scn.FreepsActionAvailable(palaces));
		scn.FreepsUseCardAction(palaces);

		// No choice of costs, and the one condition with a card stacked on it is taken unasked.
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertInDiscard(palaces);
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(List.of(stacked1, top), scn.GetStackedCards(armory));
		assertEquals(second, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenConditionThatHasACardStackedTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		var top = scn.GetFreepsCard("top");
		var second = scn.GetFreepsCard("second");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces, armory);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 2);
		scn.SetDrawDeck(scn.P1(), top, second);

		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetStackedCards(armory).size());
		assertEquals(top, scn.GetFreepsTopOfDeck());

		assertTrue(scn.FreepsActionAvailable(palaces));
		scn.FreepsUseCardAction(palaces);
		scn.FreepsChooseOption("token");

		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetStackedCards(armory).size());
		assertEquals(top, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palaces = scn.GetFreepsCard("palaces");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		var stacked1 = scn.GetFreepsCard("stacked1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces, armory);
		scn.StackCardsOn(armory, stacked1);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(List.of(stacked1), scn.GetStackedCards(armory));
		assertTrue(scn.FreepsActionAvailable(palaces));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(palaces));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
	}
}
