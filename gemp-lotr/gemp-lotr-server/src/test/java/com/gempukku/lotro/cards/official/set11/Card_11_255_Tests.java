package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_255_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("gandalf", "1_72");
					put("radagast", "9_26");
					put("merry", "1_302");
					put("pippin", "1_306");
				}},
				new HashMap<>() {{
					put("site1", "11_239");
					put("site2", "11_255");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				GenericCardTestHelper.Shadows
		);
	}

	@Test
	public void PinnacleofZirakzigilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pinnacle of Zirakzigil
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Mountain</b>. At the start of your fellowship phase, you may exert 3 companions to play a Wizard from your dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Pinnacle of Zirakzigil");

		assertEquals("Pinnacle of Zirakzigil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PinnacleofZirakzigilExerts3CompanionsAtStartOfFellowshipPhaseToPlayWizardFromDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pinnacle = scn.GetFreepsSite("Pinnacle of Zirakzigil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.FreepsMoveCardToDeadPile(gandalf, radagast);
		scn.FreepsMoveCharToTable(merry, pippin);

		scn.StartGame(pinnacle);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(Zone.DEAD, gandalf.getZone());
		assertEquals(Zone.DEAD, radagast.getZone());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.FreepsGetSelectableCount());
		scn.FreepsChooseCardBPFromSelection(gandalf);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(Zone.FREE_CHARACTERS, gandalf.getZone());
		assertEquals(Zone.DEAD, radagast.getZone());

	}
}
