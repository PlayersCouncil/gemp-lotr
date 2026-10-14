package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_186_Tests
{
	// The card's own block: the Shadow player lays each next site, The Bridge of Khazad-dum among them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Balrog, The Terror of Khazad-dum: [moria] Balrog, cost 12, site 4. Its text acts only at an
					// underground site (fierce, cannot take wounds or be exerted), and nothing discards it.
					put("terror", "12_79");
					// The Balrog, Demon of Might: [moria] Balrog, cost 14, site 4. Nothing needed to play it; its text
					// skips phases while it is at an underground site, and it is only read in hand here.
					put("demon", "19_18");
					// Goblin Patrol Troop: [moria] Orc, cost 6, no text. A [moria] minion of another title.
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("bridge", "12_186");
					put("ruins", "18_138");     // Sirannon Ruins: River and no text
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "13_185");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "11_237");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TheBridgeofKhazaddumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Bridge of Khazad-dûm
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Underground</b>. The Balrog is twilight cost -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("bridge");

		assertEquals("The Bridge of Khazad-dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void AtTheBridgeEachBalrogIsThreeCheaperAndAMoriaMinionOfAnotherTitleIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetShadowHand(terror, demon, troop);

		// At Fangorn Glade each is its printed cost.
		assertEquals(12, scn.GetTwilightCost(terror));
		assertEquals(14, scn.GetTwilightCost(demon));
		assertEquals(6, scn.GetTwilightCost(troop));

		// The Bridge is laid as site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bridge"));
		assertEquals("The Bridge of Khazad-dûm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 12 - 3 and 14 - 3; the Troop is a [moria] minion but no Balrog.
		assertEquals(9, scn.GetTwilightCost(terror));
		assertEquals(11, scn.GetTwilightCost(demon));
		assertEquals(6, scn.GetTwilightCost(troop));

		// A Balrog has site number 4: at site 2 he is roaming, +2 on top of the cost.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(terror));
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(terror));

		scn.ShadowPlayCard(terror);
		assertInZone(Zone.SHADOW_CHARACTERS, terror);
		// 11 - (12 - 3) - 2 roaming
		assertEquals(0, scn.GetTwilight());
		// The other Balrog in hand is still 3 cheaper.
		assertEquals(11, scn.GetTwilightCost(demon));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheBridgeTheBalrogCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetShadowHand(terror);

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("bridge"));
		var bridge = scn.GetCurrentSite();
		assertEquals("The Bridge of Khazad-dûm", bridge.getBlueprint().getTitle());
		// 12 - 3
		assertEquals(9, scn.GetTwilightCost(terror));

		// No minion is played at the Bridge. The fellowship moves on in the regroup phase to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, bridge);
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(12, scn.GetTwilightCost(terror));
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(terror));
		scn.SetTwilight(14);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(terror));

		scn.ShadowPlayCard(terror);
		assertInZone(Zone.SHADOW_CHARACTERS, terror);
		// 14 - 12 - 2 roaming at site 3
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
