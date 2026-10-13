package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_352_Tests
{

	// Lothlorien Woods is site 6 of the Fellowship path.  Site 7 is Anduin Wilderland (the archery phase is
	// skipped there) and not Anduin Confluence, which discards every ally.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Galadriel, Lady of Light: [elven] Elf ally, home 6, strength 3; heals the allies of home 6 at
					// the start of each of her owner's turns (nobody is wounded) and has a fellowship ability
					// used only on purpose
					put("galadriel", "1_45");
					// Orophin, Lorien Bowman: [elven] Elf ally, home 6, strength 3; an archery ability used only
					// on purpose
					put("orophin", "1_56");
					// Silinde, Elf of Mirkwood: [elven] Elf ally, home 3, strength 5; she copies her owner's site 3,
					// which is not on the path
					put("silinde", "1_60");
					// Aldor, Soldier of Edoras: [rohan] Man ally, homes 3 and 6 of the Towers block, strength 3;
					// an archery ability that needs a villager
					put("aldor", "4_262");
					// Legolas, Greenleaf: an Elf companion, strength 6; an archery ability used only on purpose
					put("legolas", "1_50");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_352");
					put("site7", "1_354");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LothlorienWoodsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lothlórien Woods
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Forest</b>. <b>Sanctuary</b>. Each ally whose home is site 6 is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Lothlórien Woods", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void EachAllyWhoseHomeIsSite6IsStrengthPlus3OnlyWhileTheFellowshipIsAtTheWoods() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var aldor = scn.GetFreepsCard("aldor");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveAlliesToTable(galadriel, orophin, silinde, aldor);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(5);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(orophin));
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPass();

		// At the Woods: the two allies whose home is site 6 are +3.  Not the ally of site 3, nor the ally whose
		// home is site 6 of the Towers block (CR 4.2 "block symbol"), nor an Elf companion.
		assertSame(scn.GetShadowSite(6), scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetStrength(galadriel));
		assertEquals(6, scn.GetStrength(orophin));
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(6, scn.GetStrength(legolas));

		// With no minion in play the game goes from the Shadow phase straight to regroup, and the fellowship
		// moves on.  The Woods' text does not apply once the fellowship is elsewhere (CR 4.2 "active").
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(orophin));
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(6, scn.GetStrength(legolas));
	}
}
