package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_003_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("emeralds", "31_3");
					put("arkenstone", "31_7");  // The Arkenstone: [dwarven] artifact, support area, with a stack of its own; its abilities not used
					put("advice", "33_18");     // Wise Advice: [elven] Fellowship event
					put("lore", "30_13");       // Lore of Imladris: a [dwarven] Maneuver event
					put("battle", "30_4");      // Battle of Azanulbizar: a [dwarven] Maneuver event of another title
					put("hadafang", "33_13");   // Hadafang: an [elven] artifact, no event
					put("letters", "33_15");    // Moon-Letters: an [elven] Fellowship event, laid in the Shadow player's discard pile
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally, vitality 4; a start-of-turn heal and a Regroup ability, not used
					put("legolas", "31_9");     // Legolas, Prince of Mirkwood: [elven] Elf ally, vitality 3; an Archery ability, not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a Regroup ability, not used
					put("bats", "32_30");       // Dark Bats: [moria] minion, strength 4, vitality 2; a Shadow ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EmeraldsofGirionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Emeralds of Girion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Add (1) to take a card stacked here into hand.<br><b>Regroup:</b> Exert an [elven] ally to stack an [elven] event (or Lore of Imladris) from your discard pile on Emeralds of Girion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("emeralds");

		assertEquals("Emeralds of Girion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityAddsOneToTakeACardStackedOnTheEmeraldsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var advice = scn.GetFreepsCard("advice");
		var lore = scn.GetFreepsCard("lore");
		var battle = scn.GetFreepsCard("battle");
		scn.MoveCardsToSupportArea(emeralds, arkenstone);
		scn.StackCardsOn(emeralds, advice, lore);
		scn.StackCardsOn(arkenstone, battle);

		scn.StartGame();
		assertTrue(scn.GetStackedCards(emeralds).contains(advice));
		assertInZone(Zone.STACKED, advice, lore, battle);
		assertEquals(2, scn.GetStackedCards(emeralds).size());
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsActionAvailable(emeralds));
		scn.FreepsUseCardAction(emeralds);
		assertEquals(1, scn.GetTwilight());

		// The cards stacked here, of two cultures; not the one stacked on the Arkenstone.
		assertTrue(scn.FreepsHasCardChoiceAvailable(advice, lore));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(battle));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lore);

		assertInHand(lore);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.GetStackedCards(emeralds).contains(advice));
		assertEquals(1, scn.GetStackedCards(emeralds).size());
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingStackedOnTheEmeraldsTheTakeIsPaidForAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		scn.MoveCardsToSupportArea(emeralds, arkenstone);
		scn.StackCardsOn(arkenstone, battle);

		scn.StartGame();
		assertEquals(0, scn.GetStackedCards(emeralds).size());
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());

		// The ability has a cost, and so is offered with nothing to take.
		assertTrue(scn.FreepsActionAvailable(emeralds));
		scn.FreepsUseCardAction(emeralds);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var advice = scn.GetFreepsCard("advice");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(emeralds);
		scn.StackCardsOn(emeralds, advice);

		scn.StartGame();
		assertTrue(scn.GetStackedCards(emeralds).contains(advice));
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(emeralds));
		assertTrue(scn.GetStackedCards(emeralds).contains(advice));
	}

	@Test
	public void RegroupAbilityExertsAnElvenAllyToStackAnElvenEventOrLoreOfImladrisFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var advice = scn.GetFreepsCard("advice");
		var lore = scn.GetFreepsCard("lore");
		var battle = scn.GetFreepsCard("battle");
		var hadafang = scn.GetFreepsCard("hadafang");
		var letters = scn.GetShadowCard("letters");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var bard = scn.GetFreepsCard("bard");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveAlliesToTable(elrond, legolas, bard);
		scn.MoveCardsToDiscard(advice, lore, battle, hadafang, letters);

		scn.StartGame();
		assertInDiscard(advice, lore, battle, hadafang, letters);
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetStackedCards(emeralds).size());
		assertTrue(scn.FreepsActionAvailable(emeralds));
		scn.FreepsUseCardAction(emeralds);

		// [elven] allies of two titles; not an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(bard));

		// An [elven] event and Lore of Imladris; not a [dwarven] event of another title, an [elven] card that is no
		// event, or an [elven] event in the Shadow player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(advice, lore));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(battle, hadafang, letters));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(advice);

		assertTrue(scn.GetStackedCards(emeralds).contains(advice));
		assertEquals(1, scn.GetStackedCards(emeralds).size());
		assertInDiscard(lore, battle, hadafang, letters);

		// Again, with Elrond: Lore of Imladris, the one candidate left, goes on as well.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(emeralds));
		scn.FreepsUseCardAction(emeralds);
		scn.FreepsChooseCard(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.GetStackedCards(emeralds).contains(lore));
		assertEquals(2, scn.GetStackedCards(emeralds).size());
		assertInDiscard(battle, hadafang, letters);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheOnlyElvenAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var advice = scn.GetFreepsCard("advice");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveAlliesToTable(elrond, bard);
		scn.MoveCardsToDiscard(advice);

		scn.StartGame();
		assertInDiscard(advice);
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.REGROUP);
		// Vitality 4 with 3 wounds: Elrond cannot exert.  Bard, an ally of another culture, can.
		scn.AddWoundsToChar(elrond, 3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(bard));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(emeralds));
	}

	@Test
	public void WithNoElvenEventOrLoreOfImladrisInTheDiscardPileTheStackIsPaidForAndStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emeralds = scn.GetFreepsCard("emeralds");
		var battle = scn.GetFreepsCard("battle");
		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToDiscard(battle, hadafang);

		scn.StartGame();
		assertInDiscard(battle, hadafang);
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsActionAvailable(emeralds));
		scn.FreepsUseCardAction(emeralds);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetStackedCards(emeralds).size());
		assertInDiscard(battle, hadafang);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
