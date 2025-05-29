package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "13_137");
					put("eowyn", "5_122");
					put("eomer", "4_267");
					put("theodred", "13_138");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Théoden, The Renowned
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot Éowyn, Théoden is <b>defender +1</b>.<br>While you can spot Éomer, Théoden is <b>damage +1</b>.<br>While you can spot Théodred, the move limit is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("The Renowned", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void TheodenIsDefenderPlus1IfYouCanSpotEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(eowyn);
		scn.MoveCompanionToTable(theoden);

		scn.StartGame();

		assertFalse(scn.HasKeyword(theoden, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));

		scn.FreepsPlayCard(eowyn);

		assertTrue(scn.HasKeyword(theoden, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
	}

	@Test
	public void TheodenIsDamagePlus1IfYouCanSpotEomer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCardsToHand(eomer);
		scn.MoveCompanionToTable(theoden);

		scn.StartGame();

		assertFalse(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));

		scn.FreepsPlayCard(eomer);

		assertTrue(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
	}

	@Test
	public void TheodenAdds1ToMoveLimitIfYouCanSpotTheodred() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCardsToHand(theodred);
		scn.MoveCompanionToTable(theoden);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(theodred);

		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void TheodenMoveLimitIsNotAvailableToShadowPlayerIfPlayedInStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//We are specifically getting player 2's versions of the cards
		var theoden = scn.GetShadowCard("theoden");
		var theodred = scn.GetShadowCard("theodred");

		//Since we are specifically testing the starting fellowship, we won't use
		// the normal StartGame call (since that skips starting fellowships)
		//scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(theoden);
		scn.ShadowChooseCardBPFromSelection(theodred);

		scn.SkipMulligans();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		//Move limit should have been reset
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheodenMoveLimitIsNotAvailableToShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//We are specifically getting player 2's versions of the cards
		var eviltheoden = scn.GetShadowCard("theoden");
		var eviltheodred = scn.GetShadowCard("theodred");

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");

		//Since we are specifically testing the starting fellowship, we won't use
		// the normal StartGame call (since that skips starting fellowships)
		//scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowChooseCardBPFromSelection(eviltheoden);
		scn.ShadowChooseCardBPFromSelection(eviltheodred);

		scn.SkipMulligans();
		scn.SkipToSite(2);

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		//Move limit should have been reset
		assertEquals(2, scn.GetMoveLimit());
	}
}
