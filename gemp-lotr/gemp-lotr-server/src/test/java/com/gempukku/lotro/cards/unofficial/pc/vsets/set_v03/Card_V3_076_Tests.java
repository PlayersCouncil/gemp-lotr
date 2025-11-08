package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twk", "103_76");

					put("aragorn", "1_89");
					put("sting", "1_313");
					put("anduril", "7_79");
					put("coat", "2_105");
					put("bow", "1_90");
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
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Fierce. Enduring. Damage +1.
		* 	When you play this minion, hinder all Free Peoples cards of one type (except companion). The Free Peoples
		 * 	player may restore any number of their cards, and must exert one of their characters for each card restored.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twk");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Empowered by His Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheWitchkingTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var coat = scn.GetFreepsCard("coat");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting, coat);
		scn.AttachCardsTo(aragorn, anduril, bow);

		var twk = scn.GetShadowCard("twk");
		scn.MoveCardsToHand(twk);

		scn.StartGame();

		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(twk));
		scn.ShadowPlayCard(twk);

		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(bow));

		assertTrue(scn.ShadowDecisionAvailable("Choose a card type to hinder"));

		//Companions are not valid targets
		assertFalse(scn.ShadowHasCardChoicesAvailable(frodo, aragorn));
		//Possessions and Artifacts are
		assertTrue(scn.ShadowHasCardChoicesAvailable(sting, anduril, coat, bow));

		scn.ShadowChooseCard(anduril);

		//Artifacts are hindered, not possessions
		assertTrue(scn.IsHindered(coat));
		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(bow));

		//Freeps now has a chance to restore any cards they want
		assertTrue(scn.FreepsDecisionAvailable("Choose any number of cards to restore"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(anduril, coat));
		assertFalse(scn.FreepsHasCardChoicesAvailable(sting, bow));

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCards(anduril);

		assertTrue(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(anduril));

		scn.FreepsChooseCards(aragorn);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

}
