package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_247_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foe", "1_247");
					put("soldier", "1_271");
					put("band", "1_272");

					put("sam", "1_310");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnheartenedFoeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Enheartened Foe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a [sauron] Orc wins a skirmish, make that Orc <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foe");

		assertEquals("Enheartened Foe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EnheartenedFoePermitsSelectionOfWinningOrcInDefenderScenarios() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		var foe = scn.GetShadowCard("foe");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(soldier, band);
		scn.MoveCardsToHand(foe);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsPass();
		scn.ShadowAssignToMinions(sam, soldier, band);
		scn.FreepsResolveSkirmish(sam);

		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowPlayAvailable(foe));
		scn.ShadowPlayCard(foe);

		assertTrue(scn.ShadowHasCardChoicesAvailable(soldier, band));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		
		scn.ShadowChooseCard(band);
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
	}
}
