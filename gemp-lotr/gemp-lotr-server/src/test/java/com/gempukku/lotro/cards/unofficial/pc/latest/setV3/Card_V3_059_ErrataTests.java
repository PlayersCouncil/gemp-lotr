package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_059_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_59");
					put("initiate1", "103_47");  // Desert Wind Initiate - Raider minion (tracker tax)
					put("initiate2", "103_47");  // Another one
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Strength of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot 2 [raider] cards to restore 2 [raider] cards (or reconcile your hand if you can spot 5 burdens or 6 [raider] cards).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Strength of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StrengthofMenRestores2HinderedRaiderCards() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata: complete rewrite as Maneuver event
		// Spot 2 [raider] to restore 2 [raider] cards
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var initiate1 = scn.GetShadowCard("initiate1");
		var initiate2 = scn.GetShadowCard("initiate2");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(card, initiate1, initiate2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play both initiates, choosing to hinder each
		scn.ShadowPlayCard(initiate1);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(initiate1));

		scn.ShadowPlayCard(initiate2);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(initiate2));

		// Now skip to Maneuver phase to play the event
		scn.ShadowPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Can spot 2 [raider] cards (initiate1, initiate2) - meets spot 2 requirement
		assertTrue(scn.ShadowActionAvailable(card));
		scn.ShadowPlayCard(card);

		// Choose both hindered initiates as restore targets
		// With only 2 [raider] cards in play and count:2, they should be auto-selected
		assertFalse(scn.IsHindered(initiate1));
		assertFalse(scn.IsHindered(initiate2));
	}
}
