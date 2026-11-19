package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_007_Tests
{

	// The Towers path with Uruk Camp 4_335 as site 2 (Battleground, Plains; its trigger answers an Uruk-hai played,
	// and none is).  Site 1, Eastemnet Gullies 4_325, has no terrain; site 3, Barrows of Edoras 4_337, is a
	// sanctuary; site 4, White Rocks 4_346, is a battleground and nothing else (its Shadow number counts mounted
	// companions).
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farin", "11_7");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion; "To play, spot a Dwarf" is its whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with The Dimholt 8_117 as site 3 (Underground, Sanctuary; its Fellowship action is used only
	// on purpose).  Site 2, Ettenmoors 1_331, is a plains; site 4, Balin's Tomb 1_343, is underground and nothing else.
	protected VirtualTableScenario GetUndergroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farin", "11_7");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion; "To play, spot a Dwarf" is its whole text
					put("handcard", "1_7");     // a second Dwarf Guard, a card in hand for the muster offer
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "8_117");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Farin, Emissary of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While Farin is at a battleground or underground site, he gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetBattlegroundScenario();

		var card = scn.GetFreepsCard("farin");

		assertEquals("Farin", card.getBlueprint().getTitle());
		assertEquals("Emissary of Erebor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void FarinGainsMusterAtEachBattlegroundSiteAndNotElsewhereAndNoOtherDwarfDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(farin, guard);

		scn.StartGame();

		// Eastemnet Gullies is neither a battleground nor underground.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));

		// The move to Uruk Camp, a battleground (and a plains).
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));

		// A second move this turn, to Barrows of Edoras, a sanctuary.  The hand is empty, so muster makes no offer
		// at the start of the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));

		// The next turn of his, the move to White Rocks, a battleground and nothing else.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FarinGainsMusterAtEachUndergroundSiteAndNotElsewhereAndNoOtherDwarfDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUndergroundScenario();

		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		var handcard = scn.GetFreepsCard("handcard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Laid after the skip, whose turns would otherwise stop on his muster offer.
		scn.MoveCompanionsToTable(farin, guard);
		scn.MoveCardsToHand(handcard);
		assertInHand(handcard);

		// Ettenmoors is a plains.
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));

		// The move to The Dimholt, underground (and a sanctuary).
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));

		// His muster is offered at the start of the regroup phase; declined.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(handcard);

		// A second move this turn, to Balin's Tomb, underground and nothing else.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(farin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(guard, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
