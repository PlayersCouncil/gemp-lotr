package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "1_180");
					put("scimitar2", "1_180");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1; no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] minion who is a Troll, not an Orc
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [moria] Orc.<br>When you play this possession, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Goblin Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMoriaOrcAndMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop, troll, soldier);

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(scimitar));

		scn.ShadowPlayCard(scimitar);

		// Two [moria] Orcs are offered; a [moria] Troll and a [sauron] Orc are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, soldier));

		scn.ShadowChooseCard(marksman);

		assertAttachedTo(scimitar, marksman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop);

		scn.FreepsPass();
		scn.ShadowPlayCard(scimitar);
		scn.ShadowChooseCard(troop);

		assertAttachedTo(scimitar, troop);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACopyAlreadyInPlayDoesNotAnswerTheSecondCopysPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(scimitar2);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop);
		scn.AttachCardsTo(troop, scimitar);

		scn.FreepsPass();
		// The Troop already bears a hand weapon, so the Marksman is the one bearer left and nothing is asked.
		scn.ShadowPlayCard(scimitar2);

		assertAttachedTo(scimitar2, marksman);

		// Only the copy just played offers its draw.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
