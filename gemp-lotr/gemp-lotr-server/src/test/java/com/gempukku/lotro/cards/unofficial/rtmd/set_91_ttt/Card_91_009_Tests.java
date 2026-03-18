package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_009_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_9");
					// Gimli, Dwarf of Erebor: 6 str, 3 vit, twilight 2
					put("gimli", "1_13");
					// Aragorn, Ranger of the North: 8 str, 4 vit, twilight 4
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExhaustedOnPlayStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_9
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Each time a companion is played, that companion comes into play exhausted (except at site 1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CompanionExhaustedWhenPlayedAfterSite1() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: "Each time a companion is played, that companion comes into play
		// exhausted (except at site 1)."
		// Playing a companion at site 2+ should exhaust them.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(freepsMod);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SkipToSite(2);

		// Now at site 2, play Gimli
		scn.FreepsPlayCard(gimli);

		// Gimli should be exhausted (wounds = vitality - 1)
		assertTrue(scn.IsExhausted(gimli));
	}

	@Test
	public void CompanionNotExhaustedWhenPlayedAtSite1() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: Exception — companions played at site 1 should NOT be exhausted.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(freepsMod);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		// At site 1, play Gimli
		assertEquals(1, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(gimli);

		// Gimli should NOT be exhausted at site 1
		assertFalse(scn.IsExhausted(gimli));
	}

	@Test
	public void ShadowCopyAffectsFreePeoples() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: "Each time a companion is played, that companion comes into play
		// exhausted (except at site 1)."
		// Playing a companion at site 2+ should exhaust them.

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(shadowMod);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SkipToSite(2);

		// Now at site 2, play Gimli
		scn.FreepsPlayCard(gimli);

		// Gimli should be exhausted (wounds = vitality - 1)
		assertTrue(scn.IsExhausted(gimli));
	}
}
