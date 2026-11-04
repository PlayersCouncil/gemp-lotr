package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dread", "11_118");
					put("goblin", "11_137");    // Scurrying Goblin: an [orc] Orc; its only text answers its own play, and it is cheated in
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is a Half-troll; its trigger is at the start of the assignment phase
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("beckoning", "12_83");  // The Beckoning Shadow: an [orc] condition, no minion; its text needs 5 burdens
					put("breath", "1_207");     // Black Breath: a Shadow condition in its owner's support area; a skirmish ability only
					put("weary", "4_212");      // Weary: a Shadow condition on a companion; it answers an assignment to an [isengard] tracker only
					put("shoulder", "1_59");    // Shoulder to Shoulder: a Free Peoples condition in its owner's support area; a maneuver ability only
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow POSSESSION on a [moria] Orc; its text answers its own play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to bear Weary; a maneuver ability only
					put("filler1", "1_299");    // Hobbit Sword, seven times: cards for the Free Peoples draw deck
					put("filler2", "1_299");
					put("filler3", "1_299");
					put("filler4", "1_299");
					put("filler5", "1_299");
					put("filler6", "1_299");
					put("filler7", "1_299");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DreadandDespairStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dread and Despair
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot 2 [orc] minions to discard a condition from play. The Free Peoples player may discard the top 6 cards of his or her draw deck to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dread");

		assertEquals("Dread and Despair", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoOrcMinionsItDiscardsAConditionOfEitherSideFromWhereverItLiesWhenTheFreePeoplesPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shoulder = scn.GetFreepsCard("shoulder");
		var dread = scn.GetShadowCard("dread");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var breath = scn.GetShadowCard("breath");
		var weary = scn.GetShadowCard("weary");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCardsToHand(dread);

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"), scn.GetFreepsCard("filler5"), scn.GetFreepsCard("filler6"), scn.GetFreepsCard("filler7"));
		scn.MoveMinionsToTable(goblin, halftroll, troop);
		scn.AttachCardsTo(aragorn, weary);
		scn.AttachCardsTo(troop, scimitar);
		assertAttachedTo(weary, aragorn);
		assertAttachedTo(scimitar, troop);
		assertInZone(Zone.SUPPORT, shoulder, breath);
		assertInHand(dread);
		assertEquals(7, scn.GetFreepsDeckCount());

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(dread));
		scn.ShadowPlayCard(dread);

		// Any condition, either side's, in a support area or borne; not a possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shoulder, breath, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(shoulder);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInDiscard(shoulder, dread);
		assertInZone(Zone.SUPPORT, breath);
		assertAttachedTo(weary, aragorn);
		assertAttachedTo(scimitar, troop);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardsTheTopSixCardsOfHisDrawDeckToKeepTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shoulder = scn.GetFreepsCard("shoulder");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var filler6 = scn.GetFreepsCard("filler6");
		var filler7 = scn.GetFreepsCard("filler7");
		var dread = scn.GetShadowCard("dread");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(dread);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3, filler4, filler5, filler6, filler7);
		scn.MoveMinionsToTable(goblin, halftroll);
		assertInZone(Zone.SUPPORT, shoulder);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPass();

		// The one condition in play is chosen unasked.
		scn.ShadowPlayCard(dread);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The top six of his own deck, and not the seventh; the condition stays.
		assertInDiscard(filler1, filler2, filler3, filler4, filler5, filler6);
		assertInZone(Zone.DECK, filler7);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.SUPPORT, shoulder);
		assertInDiscard(dread);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveCardsInHisDrawDeckTheFreePeoplesPlayerCannotPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shoulder = scn.GetFreepsCard("shoulder");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var dread = scn.GetShadowCard("dread");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(dread);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3, filler4, filler5);
		scn.MoveMinionsToTable(goblin, halftroll);
		assertInZone(Zone.SUPPORT, shoulder);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass();

		// The one condition in play is chosen unasked.
		scn.ShadowPlayCard(dread);

		// Six cards cannot be discarded from five: the cost cannot be paid in full, so it is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInDiscard(shoulder, dread);
		assertEquals(5, scn.GetFreepsDeckCount());
		// Shoulder to Shoulder alone: no card of the deck.
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithOneOrcMinionThoughAMoriaOrcAndAnOrcConditionAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shoulder = scn.GetFreepsCard("shoulder");
		var dread = scn.GetShadowCard("dread");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(dread);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, troop);
		assertInZone(Zone.SUPPORT, shoulder, beckoning);
		assertInHand(dread);

		scn.FreepsPass();

		// One [orc] minion, beside an Orc of another culture and an [orc] card that is no minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dread));

		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(dread));
	}

	@Test
	public void WithNoConditionInPlayItIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var filler6 = scn.GetFreepsCard("filler6");
		var filler7 = scn.GetFreepsCard("filler7");
		var dread = scn.GetShadowCard("dread");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.MoveCardsToHand(dread);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3, filler4, filler5, filler6, filler7);
		scn.MoveMinionsToTable(goblin, halftroll);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertInHand(dread);

		scn.FreepsPass();

		// A target is no requirement: the event is played and does nothing.
		assertTrue(scn.ShadowPlayAvailable(dread));
		scn.ShadowPlayCard(dread);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInDiscard(dread);
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
