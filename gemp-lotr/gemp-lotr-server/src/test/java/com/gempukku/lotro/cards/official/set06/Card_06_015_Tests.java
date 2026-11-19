package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "6_15");
					// Celeborn, Lord of Lorien: [elven] Elf ally, home site 6, vitality 3; a fellowship ability used only on purpose
					put("celeborn", "1_34");
					// Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; +3 only against a Nazgul
					put("arwen", "1_30");
					// Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability used only on purpose
					put("legolas", "1_50");
					// Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");

					// Cards laid on top of the draw deck; only their culture is read.
					put("elfcard", "1_53");     // Lorien Elf: an [elven] companion
					put("count", "4_69");       // Final Count: an [elven] condition
					put("bow", "1_41");         // Elven Bow: an [elven] possession
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Elrond, Keeper of Vilya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: At the start of each turn, heal Elrond.
		 * <b>Fellowship:</b> Exert Elrond to discard the top card of your draw deck. If it is an [elven] card, you
		 * may take it into hand and heal an Elf companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Keeper of Vilya", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void HealsElrondAtTheStartOfEachOfHisOwnersTurnsAndNoOtherAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(elrond, celeborn);

		scn.StartGame();
		// Wounded during the owner's first turn, after its start.
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(celeborn, 1);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		// Player 2's turn: Elrond is not active on it.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		// The owner's next turn: Elrond is healed once, and the other wounded Elf ally is not.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetWoundsOn(elrond));

		// His next turn begins at site 3, a sanctuary, whose start-of-turn heal is ordered with Elrond's: Elrond's is
		// taken first, and the sanctuary's then has no wounded companion to ask about.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Required trigger from");
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsElrondToDiscardElvenCardsThatAreTakenWithAHealOfAnElfCompanionUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfcard = scn.GetFreepsCard("elfcard");
		var count = scn.GetFreepsCard("count");
		var bow = scn.GetFreepsCard("bow");
		var shadowArmor = scn.GetShadowCard("armor");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(aragorn, 1);
		// From the top: the Lorien Elf, Final Count, the Elven Bow.  The Shadow player's deck has a card on top too.
		scn.MoveCardsToTopOfDeck(bow, count, elfcard, shadowArmor);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertSame(shadowArmor, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, elfcard, count, bow);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elrond));
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);

		// The top card, an [elven] companion, is discarded, and the question is asked.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInDiscard(elfcard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(shadowArmor, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseYes();

		assertInHand(elfcard);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		// The two wounded Elf companions, and not Elrond (an Elf ally, wounded by his exertion) or Aragorn (no Elf).
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Final Count, an [elven] condition.
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseYes();
		assertInHand(count);
		scn.FreepsChooseCard(legolas);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The Elven Bow, an [elven] possession; Arwen is now the one wounded Elf companion, and is healed unasked.
		scn.FreepsUseCardAction(elrond);
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseYes();
		assertInHand(bow);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertSame(shadowArmor, scn.GetShadowTopOfDeck());

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elrond));
	}

	@Test
	public void ACardThatIsNotElvenStaysInTheDiscardPileAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var armor = scn.GetFreepsCard("armor");
		var count = scn.GetFreepsCard("count");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 1);
		// The [dwarven] Armor on top, an [elven] card under it.
		scn.MoveCardsToTopOfDeck(count, armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor, count);
		assertEquals(1, scn.GetWoundsOn(arwen));
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInDiscard(armor);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to put"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesTheElvenCardInTheDiscardPileAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var count = scn.GetFreepsCard("count");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToTopOfDeck(count);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, count);
		assertEquals(1, scn.GetWoundsOn(arwen));
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInDiscard(count);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseNo();

		assertInDiscard(count);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElfCompanionWoundedTheElvenCardIsStillTakenAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var count = scn.GetFreepsCard("count");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Only Aragorn, who is no Elf, is wounded.
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToTopOfDeck(count);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, count);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to put"));
		scn.FreepsChooseYes();

		assertInHand(count);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheAbilityExertsElrondForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 1);
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		int discard = scn.GetFreepsDiscardCount();
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
