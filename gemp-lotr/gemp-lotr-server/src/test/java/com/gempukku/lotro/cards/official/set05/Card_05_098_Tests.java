package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "5_98");
					put("trooper2", "5_98");
					put("orctrooper", "3_99");  // Orc Trooper: [sauron] Orc minion; strength +1 for each card in the Shadow hand, no trigger
					put("blood", "2_31");       // Blood of Númenor: each [sauron] Orc comes into play exhausted (and skip the archery phase)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // resistance 10: five burdens do not corrupt him
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gate Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: When you play this minion, you may exert him and spot 5 burdens to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Gate Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedWithFiveBurdensHeMayExertToDrawACardAndACopyInPlayMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
		scn.MoveCardsToHand(trooper2);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(trooper);
		scn.SetTwilight(5);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(trooper2);
		// Only the copy just played answers its own play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trooper2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(trooper));
		assertEquals(0, scn.GetWoundsOn(trooper2));
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(trooper2));
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// Spotted, not removed.
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndAnotherSauronOrcPlayedAfterHimIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var orctrooper = scn.GetShadowCard("orctrooper");
		scn.MoveCardsToHand(trooper, orctrooper);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(5, scn.GetBurdens());
		assertEquals(2, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(trooper);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trooper));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(orctrooper);
		assertInPlay(orctrooper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(trooper));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertEquals(0, scn.GetShadowHandCount());
	}

	@Test
	public void WhenPlayedWithFourBurdensAndAThreatNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToHand(trooper);

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		// A threat beside the four burdens nearly counts, and is no burden.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(5);

		scn.FreepsPass();
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());

		scn.ShadowPlayCard(trooper);
		assertInPlay(trooper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(trooper));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(trooper));
	}

	@Test
	public void PlayedExhaustedUnderBloodOfNumenorHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var blood = scn.GetFreepsCard("blood");
		scn.MoveCardsToHand(trooper);
		scn.MoveCardsToSupportArea(blood);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blood);
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.SetTwilight(5);

		scn.FreepsPass();
		assertEquals(5, scn.GetBurdens());

		scn.ShadowPlayCard(trooper);
		// Vitality 2 with 1 wound as he comes into play: exhausted, so he cannot exert.
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(trooper));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
