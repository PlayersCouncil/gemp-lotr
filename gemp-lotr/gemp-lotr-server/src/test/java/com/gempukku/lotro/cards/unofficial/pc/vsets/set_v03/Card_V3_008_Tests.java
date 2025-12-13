package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("words", "103_8");
					put("gandalf", "1_364");
					put("narya", "3_34");
					put("fodder1", "1_3");
					put("fodder2", "1_3");
					put("fodder3", "1_3");
					put("fodder4", "1_3");
					put("fodder5", "1_3");
					put("fodder6", "1_3");
					put("fodder7", "1_3");
					put("fodder8", "1_3");
					put("fodder9", "1_3");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WordsofPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Words of Power
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spell.
		* 	Exhaust your Wizard to reconcile your hand. You may spot a [gandalf] artifact to shuffle this into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("words");

		assertEquals("Words of Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WordsOfPowerCannotPlayWithExhaustedWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Exhaust Gandalf (vitality 4, so 3 wounds = exhausted)
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(1, scn.GetVitality(gandalf));

		scn.SkipToPhase(Phase.MANEUVER);

		// Words of Power should not be playable - cannot exhaust already exhausted wizard
		assertFalse(scn.FreepsPlayAvailable(words));
	}

	@Test
	public void WordsOfPowerExhaustsWizardAndReconcilesHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words, fodder1);
		scn.MoveCardsToTopOfFreepsDeck("fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8", "fodder9");

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount()); // words + fodder1

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(words));
		scn.FreepsPlayCard(words);

		// Gandalf should be exhausted
		assertEquals(1, scn.GetVitality(gandalf));

		// Should be prompted to choose a card to discard for reconciliation
		scn.FreepsChooseCard(fodder1);

		// Should have drawn up to 8 cards
		assertEquals(8, scn.GetFreepsHandCount());

		// Event goes to discard (no artifact to enable shuffle)
		assertEquals(Zone.DISCARD, words.getZone());
	}

	@Test
	public void WordsOfPowerCanShuffleIntoDeckWithArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");
		var narya = scn.GetFreepsCard("narya");
		var fodder1 = scn.GetFreepsCard("fodder1");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToHand(words, fodder1);
		scn.MoveCardsToTopOfFreepsDeck("fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8", "fodder9");

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(words);

		// Gandalf should be exhausted
		assertEquals(1, scn.GetVitality(gandalf));

		// Reconcile - choose card to discard
		scn.FreepsChooseCard(fodder1);

		// Should be prompted to shuffle into deck
		assertTrue(scn.FreepsDecisionAvailable("Shuffle"));
		scn.FreepsChooseYes();

		// Event should be in deck, not discard
		assertEquals(Zone.DECK, words.getZone());
	}

	@Test
	public void WordsOfPowerCanDeclineShuffleWithArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");
		var narya = scn.GetFreepsCard("narya");
		var fodder1 = scn.GetFreepsCard("fodder1");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToHand(words, fodder1);
		scn.MoveCardsToTopOfFreepsDeck("fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8", "fodder9");

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(words);

		// Gandalf should be exhausted
		assertEquals(1, scn.GetVitality(gandalf));

		// Reconcile - choose card to discard
		scn.FreepsChooseCard(fodder1);

		// Decline to shuffle into deck
		assertTrue(scn.FreepsDecisionAvailable("Shuffle"));
		scn.FreepsChooseNo();

		// Event should be in discard
		assertEquals(Zone.DISCARD, words.getZone());
	}
}
