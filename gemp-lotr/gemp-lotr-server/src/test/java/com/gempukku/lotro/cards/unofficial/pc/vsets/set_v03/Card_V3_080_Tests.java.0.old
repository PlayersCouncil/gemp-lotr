package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "103_80");

					put("aragorn", "1_89");
					put("boromir", "1_96");
					put("athelas1", "1_94");   // Possession
					put("athelas2", "1_94");   // Possession
					put("lastalliance", "1_49"); // Condition

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
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Fierce. Enduring. Damage +1.
		* 	When you play this minion, hinder all Free Peoples cards of one type (except companion). The Free Peoples player may restore any number of their cards, and must exert one of their characters for each card restored.
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
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}



// ======== BASIC HINDER FUNCTIONALITY ========

	@Test
	public void WitchKingHindersAllCardsOfChosenType() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var lastalliance = scn.GetFreepsCard("lastalliance");

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas1, lastalliance);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.IsHindered(athelas1));
		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(lastalliance));

		// Play Witch-king - required trigger fires
		scn.ShadowPlayCard(witchking);

		// Shadow chooses a card to determine type - pick a possession
		assertTrue(scn.ShadowHasCardChoicesAvailable(athelas1, athelas2, lastalliance));
		scn.ShadowChooseCard(athelas1);

		// All possessions hindered, condition not hindered
		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(lastalliance));

		// FP gets restore prompt - decline by choosing none
		scn.FreepsDeclineChoosing();

		// Cards remain hindered
		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(athelas2));
	}

	@Test
	public void FreepsCanRestoreByExertingCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas1);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		// Shadow picks possession type
		scn.ShadowChooseCard(athelas1);

		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(athelas2));

		// FP chooses to restore one card
		scn.FreepsChooseCard(athelas1);

		// Athelas1 restored
		assertFalse(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(athelas2));  // Still hindered

		// FP must exert a character
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void FreepsCanRestoreMultipleAndSpreadExertions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas1);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);
		scn.ShadowChooseCard(athelas1);

		// FP chooses to restore both cards
		scn.FreepsChooseCards(athelas1, athelas2);

		// Both restored
		assertFalse(scn.IsHindered(athelas1));
		assertFalse(scn.IsHindered(athelas2));

		// FP must exert twice - can spread across characters
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
	}

	@Test
	public void FreepsCanDeclineToRestore() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas1 = scn.GetFreepsCard("athelas1");

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas1);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		assertTrue(scn.IsHindered(athelas1));

		// FP declines to restore (chooses 0 cards)
		scn.FreepsDeclineChoosing();

		// Athelas still hindered, no exertions
		assertTrue(scn.IsHindered(athelas1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void ShadowCanChooseBetweenCardTypes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var lastalliance = scn.GetFreepsCard("lastalliance");

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas1, lastalliance);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		// Shadow can choose between possession and condition
		assertTrue(scn.ShadowHasCardChoicesAvailable(athelas1, lastalliance));

		// Choose condition this time
		scn.ShadowChooseCard(lastalliance);

		// Condition hindered, possession not
		assertTrue(scn.IsHindered(lastalliance));
		assertFalse(scn.IsHindered(athelas1));

		scn.FreepsDeclineChoosing();
	}

	@Test
	public void WitchKingTriggerNoOpsWithNoNonCompanionFPCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		// No possessions or conditions attached

		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		// Trigger fires but nothing to choose from - should skip straight through
		// and return to Shadow phase actions
		assertTrue(scn.AwaitingShadowPhaseActions());
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

		var twk = scn.GetShadowCard("witchking");
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
