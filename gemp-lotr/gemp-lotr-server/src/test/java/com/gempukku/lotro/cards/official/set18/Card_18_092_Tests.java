package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("preparations", "18_92");

					// Always Threatening: [orc] support-area condition, the one card that takes [orc] tokens by legal play
					// (as it is played); its skirmish ability is used only on purpose.
					put("always1", "13_103");
					put("always2", "13_103");
					put("always3", "13_103");
					// Deathly Roar: [uruk-hai] support-area condition that holds [uruk-hai] tokens; it answers Free Peoples
					// possessions played and [uruk-hai] wins, neither of which comes.
					put("roar", "17_113");

					put("aragorn", "1_89");    // Aragorn, Ranger of the North: a second companion, so that there is room for 2 threats
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarPreparationsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: War Preparations
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Remove a threat to reinforce an [orc] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("preparations");

		assertEquals("War Preparations", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesAThreatToReinforceAnOrcTokenOnTheCardChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var preparations = scn.GetShadowCard("preparations");
		var always1 = scn.GetShadowCard("always1");
		var always2 = scn.GetShadowCard("always2");
		var always3 = scn.GetShadowCard("always3");
		var roar = scn.GetShadowCard("roar");
		scn.MoveCardsToHand(preparations);
		scn.MoveCardsToSupportArea(always1, always2, always3, roar);

		scn.StartGame();
		scn.AddTokensToCard(always1, 1);
		scn.AddTokensToCard(always2, 2);
		scn.AddTokensToCard(roar, 1);
		scn.AddThreats(2);
		assertEquals(1, scn.GetCultureTokensOn(always1));
		assertEquals(2, scn.GetCultureTokensOn(always2));
		assertEquals(0, scn.GetCultureTokensOn(always3));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(2, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(preparations));
		scn.ShadowPlayCard(preparations);

		// The threat is removed as the cost.
		assertEquals(1, scn.GetThreats());
		// Each card holding an [orc] token is offered; one with none, and one holding an [uruk-hai] token, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(always1, always2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(always3, roar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(always1);

		assertEquals(2, scn.GetCultureTokensOn(always1));
		assertEquals(2, scn.GetCultureTokensOn(always2));
		assertEquals(0, scn.GetCultureTokensOn(always3));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetThreats());
		assertInDiscard(preparations);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetShadowCard("preparations");
		var always1 = scn.GetShadowCard("always1");
		scn.MoveCardsToHand(preparations);
		scn.MoveCardsToSupportArea(always1);

		scn.StartGame();
		scn.AddTokensToCard(always1, 1);
		assertEquals(1, scn.GetCultureTokensOn(always1));
		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(preparations));

		// One threat, and it can be played.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(preparations));
	}

	@Test
	public void WithNoOrcTokenToReinforceTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var preparations = scn.GetShadowCard("preparations");
		var always3 = scn.GetShadowCard("always3");
		var roar = scn.GetShadowCard("roar");
		scn.MoveCardsToHand(preparations);
		scn.MoveCardsToSupportArea(always3, roar);

		scn.StartGame();
		scn.AddTokensToCard(roar, 1);
		scn.AddThreats(1);
		assertEquals(0, scn.GetCultureTokensOn(always3));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(preparations));
		scn.ShadowPlayCard(preparations);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(always3));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertInDiscard(preparations);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
