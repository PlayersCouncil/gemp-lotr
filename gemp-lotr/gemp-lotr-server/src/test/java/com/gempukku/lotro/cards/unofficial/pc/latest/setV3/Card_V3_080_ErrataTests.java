package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_080_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "103_80");
					put("nazgul2", "1_234"); // Ulaire Nertea
					put("runner", "1_178");
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Witch-king, Empowered by His Master
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Enduring. Damage +1.
		* 	When you play this minion, spot another Nazgul to hinder all Free Peoples cards of one type
		*   (except companion). The Free Peoples player may restore any number of their cards,
		*   and must exert one of their characters for each card restored.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Empowered by His Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		// Errata removed Fierce keyword
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WitchkingRequiresAnotherNazgulForOnPlayTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(witchking);

		// No other Nazgul on the table -- only a runner
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play Witch-king without another Nazgul -- the "spot another Nazgul" requirement should fail
		scn.ShadowPlayCard(witchking);
		// The trigger requires spotting another Nazgul (errata added this requirement)
		// Without another Nazgul, the trigger should not fire
		assertFalse(scn.ShadowDecisionAvailable("Choose a card type"));
	}

	@Test
	public void WitchkingTriggerFiresWithAnotherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToHand(witchking);
		scn.MoveMinionsToTable(nazgul2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play Witch-king with another Nazgul on the table
		scn.ShadowPlayCard(witchking);

		// The trigger should fire -- shadow gets a decision to choose a FP card to target
		// (ChooseActiveCards with text "Choose a card type to hinder")
		assertTrue(scn.ShadowDecisionAvailable("Choose a card type"));
	}
}
