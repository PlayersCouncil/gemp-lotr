package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_030_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nardol", "103_30");
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");

					put("aragorn", "1_89");

					// Valid discard targets (Gondor/Rohan conditions or possessions)
					put("citadel", "5_32");          // Gondor support Condition
					put("arrowslits", "5_80");       // Rohan support Condition

					put("runner", "1_178");          // Generic minion for archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Nardol
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	Archery: Hinder X beacons and discard X [Gondor] or [Rohan] conditions or possessions to make the fellowship archery total +X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nardol");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Nardol", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NardolArcheryHindersXBeaconsAndDiscardsXCardsForPlusX() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		// Errata: cost changed from "hinder self + discard any" to "hinder X beacons + discard X cards"
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var citadel = scn.GetFreepsCard("citadel");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(nardol, beacon1, beacon2, citadel, arrowslits);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		int baseArchery = scn.GetFreepsArcheryTotal();
		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));

		assertTrue(scn.FreepsActionAvailable(nardol));
		scn.FreepsUseCardAction(nardol);
		// Errata: choose X beacons to hinder (range-based cost)
		scn.FreepsChooseCards(beacon1, beacon2);
		// Then discard X (2) Gondor/Rohan conditions/possessions
		scn.FreepsChooseCards(citadel, arrowslits);

		// +2 archery for hindering 2 beacons and discarding 2 cards
		assertEquals(baseArchery + 2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
		assertInDiscard(citadel);
		assertInDiscard(arrowslits);
	}
}
