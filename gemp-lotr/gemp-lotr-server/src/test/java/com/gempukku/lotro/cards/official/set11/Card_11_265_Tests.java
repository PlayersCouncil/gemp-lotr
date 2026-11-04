package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_265_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man companion; a Skirmish ability only
					put("merchant", "7_97");      // Gondorian Merchant: [gondor] Man ALLY; a Maneuver ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, laid wounded; his Maneuver ability is never used
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, no Man; Skirmish ability only
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, valiant only
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");       // Fangorn Glade: its text answers a move to it; the game starts there
					put("window", "11_265");
					put("ruins", "18_138");       // Sirannon Ruins: no text
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "17_148");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void WindowontheWestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Window on the West
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Underground</b>. Each unwounded [gondor] Man is <b>defender +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("window");

		assertEquals("Window on the West", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void EachUnwoundedGondorManIsDefenderPlusOneThereAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// At Fangorn Glade nobody is a defender.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.FreepsPass();
		var window = scn.GetShadowSite("window");
		scn.ShadowChooseCardBPFromSelection(window);
		assertSame(window, scn.GetCurrentSite());

		// An unwounded [gondor] Man companion and a [gondor] Man ally.
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		// A wounded [gondor] Man, a [gondor] Hobbit, a [rohan] Man and the Ring-bearer are not.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheWindowNobodyIsADefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var window = scn.GetShadowSite("window");
		scn.ShadowChooseCardBPFromSelection(window);

		scn.SkipToMovementDecision();
		assertSame(window, scn.GetCurrentSite());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(merchant, Keyword.DEFENDER));

		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
