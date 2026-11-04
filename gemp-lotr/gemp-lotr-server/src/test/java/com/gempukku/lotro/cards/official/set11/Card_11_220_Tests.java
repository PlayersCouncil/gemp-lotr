package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_220_Tests
{

	// Site 2 of the Fellowship path is Ettenmoors 1_331, a plains; the Valley is laid after it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "11_220");
					// Ulaire Lemenya, Lieutenant of Morgul: another [wraith] Nazgul, strength 9; her text reaches only a
					// companion or ally bearing a [wraith] condition.
					put("lemenya", "1_232");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Valley of the Silverlode: a Shadows-block dwelling and river, laid on the path by
					// MoveCardToAdventurePath as site 3; "When your fellowship moves to this site, you may heal each
					// Hobbit companion" (nobody is wounded).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A forest at site 1, then a dwelling laid after it.
	protected VirtualTableScenario GetWoodsAndBreeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "11_220");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Hobbiton Woods: a forest with no other keyword; a fellowship ability used only on purpose.
					put("site1", "2_116");
					// Streets of Bree: a Shadows-block dwelling with no other keyword, laid on the path by
					// MoveCardToAdventurePath as site 2; a bar on transferring followers.
					put("bree", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A forest that is also a sanctuary at site 3.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "11_220");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Rivendell Waterfall: a forest and a sanctuary; a move-limit text for a ranger (there is none).
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Cantëa, Fourth of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: While Úlairë Cantëa is at a forest or dwelling site, he is strength +2 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Fourth of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAPlainsHeIsAsPrintedAndAtADwellingThatIsARiverHeIsStrength12AndFierceAndAnotherNazgulIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");
		var valley = scn.GetShadowSite("valley");

		scn.StartGame();
		scn.SkipToSite(2);
		// Laid early, in the fellowship phase at Ettenmoors (a plains), to be read there and after the move.
		scn.MoveMinionsToTable(cantea, lemenya);
		scn.MoveCardToAdventurePath(valley);

		assertEquals(10, scn.GetStrength(cantea));
		assertFalse(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.FreepsPass();
		assertSame(valley, scn.GetCurrentSite());

		// 10 + 2
		assertEquals(12, scn.GetStrength(cantea));
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAForestAndThenAtADwellingWithNoOtherKeywordHeIsStrength12AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsAndBreeScenario();

		var cantea = scn.GetShadowCard("cantea");
		var bree = scn.GetShadowSite("bree");

		scn.StartGame();
		// At Hobbiton Woods, a forest.
		scn.MoveMinionsToTable(cantea);
		scn.MoveCardToAdventurePath(bree);

		assertEquals(12, scn.GetStrength(cantea));
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));

		scn.FreepsPass();
		assertSame(bree, scn.GetCurrentSite());

		assertEquals(12, scn.GetStrength(cantea));
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAForestThatIsASanctuaryHeIsStrength12AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(cantea);

		assertEquals(10, scn.GetStrength(cantea));
		assertFalse(scn.HasKeyword(cantea, Keyword.FIERCE));

		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());

		assertEquals(12, scn.GetStrength(cantea));
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
