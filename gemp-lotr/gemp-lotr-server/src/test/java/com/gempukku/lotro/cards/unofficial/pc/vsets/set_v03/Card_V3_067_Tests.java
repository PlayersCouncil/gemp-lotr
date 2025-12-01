package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "103_67");
					put("nazgul1", "1_230");
					put("nazgul2", "1_231");
					put("nazgul3", "1_232");

					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("legolas", "1_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IllWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ill Wind
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The second time you play a Nazgul each Shadow phase, you may hinder a wounded
		 * companion (except a companion with the highest strength).
		 * If you can spot The Witch-king, exert that companion first.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Ill Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IllWindPermitsHinderingOfMiddlingStrengthCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		var wind = scn.GetShadowCard("wind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var nazgul3 = scn.GetShadowCard("nazgul3");
		scn.MoveCardsToHand(nazgul1, nazgul2, nazgul3);
		scn.MoveCardsToSupportArea(wind);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(legolas, 1);
		
		scn.SetTwilight(50);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, wind);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(legolas));

		assertTrue(scn.ShadowPlayAvailable(nazgul1));
		assertTrue(scn.ShadowPlayAvailable(nazgul2));
		assertTrue(scn.ShadowPlayAvailable(nazgul3));

		scn.ShadowPlayCard(nazgul1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowPlayCard(nazgul2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoicesAvailable(gimli, legolas, frodo));
		//As Aragorn has the highest strength (8 vs 6/6/4), he is ineligible
		assertFalse(scn.ShadowHasCardChoicesAvailable(aragorn));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));

		scn.ShadowPlayCard(nazgul3);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
