package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_4");

					put("sam", "1_311");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandofWildMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Band of Wild Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may make it <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("band");

		assertEquals("Band of Wild Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BandofWildMenBecomesFierceIfWinningSkirmishNormally() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionToTable(gimli);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, band);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
	}

	@Test
	public void BandofWildMenBecomesFierceIfWinningSkirmishWithOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, band);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
	}

	@Test
	public void BandofWildMenBecomesFierceIfWinningSkirmishKillsOpponentWithWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		scn.MoveMinionsToTable(band);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionToTable(gimli);

		scn.StartGame();

		scn.AddWoundsToChar(gimli, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, band);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
	}
}
