package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInPlay;
import static org.junit.Assert.*;

public class Card_91_011_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Shadow support area condition: Saruman's Ambition (shadow condition, support area)
		put("condition", "1_130");
		// blade tip, another condition
		put("condition2", "1_209");
		// Shadow possession: Morgul Blade (shadow possession)
		put("possession", "1_216");
		// Shadow artifact: The Palantir of Orthanc (shadow artifact)
		put("artifact", "1_69");
		// Filler cards to discard from hand
		put("filler1", "1_3");
		put("filler2", "1_3");
		put("filler3", "1_3");
		put("filler4", "1_3");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_11"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_11", null
		);
	}

	@Test
	public void PlayConditionFromDiscardStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_11
		 * Type: MetaSite
		 * Game Text: Shadow: Discard 3 cards from hand to play a condition from your discard pile.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 91_11", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowCanPlayConditionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: "Shadow: Discard 3 cards from hand to play a condition from your discard pile."
		// Shadow should be able to use the ability to play a condition from discard.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var condition = scn.GetShadowCard("condition");
		var condition2 = scn.GetShadowCard("condition2");
		var possession = scn.GetShadowCard("possession");
		var artifact = scn.GetShadowCard("artifact");

		scn.MoveCardsToDiscard(condition, condition2, possession, artifact);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// Shadow phase — should be able to use the mod's ability
		assertTrue(scn.ShadowActionAvailable(shadowMod));
		scn.ShadowUseCardAction(shadowMod);

		// Should only be able to choose a condition, not possession or artifact
		assertTrue(scn.ShadowHasCardChoiceAvailable(condition));
		assertFalse(scn.ShadowHasCardChoiceAvailable(possession));
		assertFalse(scn.ShadowHasCardChoiceAvailable(artifact));

		scn.ShadowChooseCard(condition);

		// Condition should now be in play
		assertInPlay(condition);
		// Should have discarded 3 cards from hand
		assertEquals(0, scn.GetShadowHandCount());
	}

	@Test
	public void CannotUseWithoutThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: Should not be usable without 3 cards in hand.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var condition = scn.GetShadowCard("condition");

		scn.MoveCardsToDiscard(condition);
		scn.MoveCardsToShadowHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move

		// Should NOT be able to use the ability with only 2 cards in hand
		assertFalse(scn.ShadowActionAvailable(shadowMod));
	}

	@Test
	public void FreepsCopyDoesNotWorkForShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_11: "your discard pile" — Freeps' copy should not let Shadow use it.

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var condition = scn.GetShadowCard("condition");

		scn.MoveCardsToDiscard(condition);
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();
		scn.FreepsPass(); // move

		// Shadow should NOT be able to use Freeps' copy
		assertFalse(scn.ShadowActionAvailable(freepsMod));
	}
}
