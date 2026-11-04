package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merchant", "13_132");
					// Doom Drove Them: a [rohan] support-area condition that holds [rohan] tokens; its two abilities
					// are used only on purpose
					put("doom1", "8_86");
					put("doom2", "8_86");
					// Garrison of Osgiliath: a [gondor] support-area condition that holds [gondor] tokens; a skirmish
					// ability only
					put("garrison", "6_52");
					// Ships of Great Draught: a [raider] support-area possession that holds [raider] tokens; its
					// abilities are used only on purpose
					put("ships", "8_65");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerchantofWestfoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Merchant of Westfold
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Each time the fellowship moves, you may add (1) and remove a Free Peoples culture token to reinforce a [rohan] token.
		*/

		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");

		assertEquals("Merchant of Westfold", merchant.getBlueprint().getTitle());
		assertNull(merchant.getBlueprint().getSubtitle());
		assertFalse(merchant.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, merchant.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, merchant.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, merchant.getBlueprint().getCardType());
		assertEquals(Race.MAN, merchant.getBlueprint().getRace());
		assertEquals(2, merchant.getBlueprint().getTwilightCost());
		assertEquals(6, merchant.getBlueprint().getStrength());
		assertEquals(3, merchant.getBlueprint().getVitality());
		assertEquals(5, merchant.getBlueprint().getResistance());
	}

	@Test
	public void OnTheMoveAddsOneAndRemovesAFreePeoplesTokenToReinforceARohanToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(merchant);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(ships, 2);

		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merchant));
		scn.FreepsAcceptOptionalTrigger();

		// The token removed: one on either Free Peoples culture's cards, not the Shadow player's [raider] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(doom1);
		assertEquals(1, scn.GetCultureTokensOn(doom1));

		// The token reinforced: either [rohan] card, not the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(doom2);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		// 2 for Ettenmoors + 2 companions + 1 added
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCultureTokenToRemoveItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var doom1 = scn.GetFreepsCard("doom1");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(merchant);
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		// Only the Shadow player's [raider] tokens: nothing can pay, and the move goes on unasked.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(merchant));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(ships));
		// 2 for Ettenmoors + 2 companions
		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void WithNoRohanTokenToReinforceTheGondorTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(merchant);
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(garrison, 1);

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		// A cost that can be paid: offered, though no [rohan] token is there to reinforce.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merchant));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		// 2 for Ettenmoors + 2 companions + 1 added
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedOnTheFirstMoveItIsOfferedAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(merchant);
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(garrison, 1);

		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merchant));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		// 2 for Ettenmoors + 2 companions, and nothing added
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		int pool = scn.GetTwilight();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merchant));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(garrison);
		// The one [rohan] token left to reinforce is chosen unasked.
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(3, scn.GetCultureTokensOn(doom1));
		// 0 for Rivendell Valley + 2 companions + 1 added
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(merchant);
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(garrison, 1);

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		// The fellowship stays: no offer, and the other player's turn begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(merchant));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
