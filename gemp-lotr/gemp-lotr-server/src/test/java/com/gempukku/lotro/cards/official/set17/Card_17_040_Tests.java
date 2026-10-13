package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_40");
					put("man", "17_57");
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("legolas", "1_50");
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeastofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Beast of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Game Text: Bearer must be a [men] Man.<br>While you can spot a threat, bearer is <b>fierce</b>.<br>While you can spot 3 threats, bearer is <b>damage +1</b>.<br>While you can spot 5 threats, you may play this from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Beast of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	// F-0060 (Ketura, 2026-10-07: "clearly an implementation error"): the definition let it be played from the
	// discard pile at 3 threats; the text says 5.
	@Test
	public void IsPlayedFromTheDiscardPileOnlyWhileFiveThreatsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("card");
		var man = scn.GetShadowCard("man");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToDiscard(beast);

		scn.StartGame();
		scn.MoveMinionsToTable(man);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(10);

		// Four threats, with five companions to hold five: one short.
		scn.AddThreats(4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetThreats());
		assertEquals(Zone.DISCARD, beast.getZone());
		assertFalse(scn.ShadowActionAvailable(beast));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(beast));

		scn.ShadowUseCardAction(beast);

		assertEquals(Zone.ATTACHED, beast.getZone());
		assertSame(man, beast.getAttachedTo());
		assertEquals(7, scn.GetTwilight());
		assertEquals(5, scn.GetThreats());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BeastofWarTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
