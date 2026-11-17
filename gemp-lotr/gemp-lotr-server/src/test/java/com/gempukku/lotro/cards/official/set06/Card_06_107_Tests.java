package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chain", "6_107");
					put("chain2", "6_107");
					put("gatetroll", "6_103");  // Gate Troll: unique [sauron] Troll, strength 13; a cost and a strength modifier only
					put("hilltroll", "8_102");  // Great Hill Troll: [sauron] Troll, besieger, strength 16; its play trigger is not met when cheated in
					put("olog", "11_108");      // Beastly Olog-hai: [orc] Troll; an assignment ability used only on purpose
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollsChainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Troll's Chain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [sauron] Troll.<br>When you play this possession, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chain");

		assertEquals("Troll's Chain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASauronTrollAndMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetShadowCard("chain");
		var gatetroll = scn.GetShadowCard("gatetroll");
		var hilltroll = scn.GetShadowCard("hilltroll");
		var olog = scn.GetShadowCard("olog");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.MoveMinionsToTable(gatetroll, hilltroll, olog, soldier);

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(chain));

		scn.ShadowPlayCard(chain);

		// Two [sauron] Trolls are offered; an [orc] Troll and a [sauron] Orc are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gatetroll, hilltroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(olog, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(gatetroll);

		assertAttachedTo(chain, gatetroll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain));
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

		var chain = scn.GetShadowCard("chain");
		var gatetroll = scn.GetShadowCard("gatetroll");
		var hilltroll = scn.GetShadowCard("hilltroll");
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.MoveMinionsToTable(gatetroll, hilltroll);

		scn.FreepsPass();
		scn.ShadowPlayCard(chain);
		scn.ShadowChooseCard(hilltroll);

		assertAttachedTo(chain, hilltroll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain));
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

		var chain = scn.GetShadowCard("chain");
		var chain2 = scn.GetShadowCard("chain2");
		var gatetroll = scn.GetShadowCard("gatetroll");
		var hilltroll = scn.GetShadowCard("hilltroll");
		scn.MoveCardsToHand(chain2);

		scn.StartGame();
		scn.MoveMinionsToTable(gatetroll, hilltroll);
		scn.AttachCardsTo(hilltroll, chain);

		scn.FreepsPass();
		// The Great Hill Troll already bears a hand weapon, so the Gate Troll is the one bearer left and nothing is asked.
		scn.ShadowPlayCard(chain2);

		assertAttachedTo(chain2, gatetroll);

		// Only the copy just played offers its draw.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chain2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chain));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
