package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
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

public class Card_V2_073_Tests
{
	// The turns that bring the fellowship to site 8 draw this small deck into its owners' hands, so each test lays
	// the Shadow player's hand, draw deck and discard pile out afresh in the fellowship phase there, before the move
	// to the Clefts and before the Shadow player is asked anything.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dunlending Footmen: [dunland] Man, vitality 2, so able to exert once
					put("footmen", "6_3");
					// Dunlending Savage: [dunland] Man with no text and vitality 1, so never able to exert
					put("savage", "4_17");
					// Goblin Patrol Troop: [moria] Orc with no text, vitality 3, so able to exert twice
					put("troop", "1_177");

					// Shadow possessions, of three cultures.
					// Iron Axe: [dunland] hand weapon, cost 1; "Bearer must be a [dunland] Man." is its whole text
					put("axe1", "4_26");
					put("axe2", "4_26");
					put("spear", "1_182");          // Goblin Spear: [moria] hand weapon, cost 1, for a [moria] minion
					put("scaffolding", "6_73");     // Scaffolding: [isengard], cost 1, plays to the support area
					put("club", "4_36");            // War Club: [dunland] hand weapon, cost 0, for a [dunland] Man
					// Hides: [dunland], cost 1, plays to the support area; never in the Shadow player's discard pile
					put("hides", "4_19");

					// Cards that are not Shadow possessions, each playable with nothing asked.
					// Library of Orthanc: [isengard] artifact, cost 2, plays to the support area
					put("library", "9_39");
					put("pinned", "1_192");         // Pinned Down: [moria] condition, cost 1, to the support area
					// Old Toby: Free Peoples possession, cost 1, plays to the support area
					put("toby", "1_305");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "102_73");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardCleftsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Isengard Clefts
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: Shadow: Exert a minion to play a Shadow possession from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Isengard Clefts", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowActionExertsAMinionToPlayAShadowPossessionFromTheDiscardPileUntilNoMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var footmen = scn.GetShadowCard("footmen");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");
		var spear = scn.GetShadowCard("spear");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var club = scn.GetShadowCard("club");
		var hides = scn.GetShadowCard("hides");
		var otherHides = scn.GetFreepsCard("hides");
		var library = scn.GetShadowCard("library");
		var pinned = scn.GetShadowCard("pinned");
		var toby = scn.GetShadowCard("toby");

		scn.StartGame();
		scn.SkipToSite(8);

		scn.MoveMinionsToTable(footmen, savage, troop);
		// Four Shadow possessions in the Shadow player's discard pile, and beside them an artifact, a condition and
		// a Free Peoples possession.  More Shadow possessions lie in his hand, in his draw deck and in the other
		// player's discard pile.
		scn.MoveCardsToDiscard(axe1, spear, scaffolding, club, library, pinned, toby);
		scn.MoveCardsToHand(axe2);
		scn.MoveCardsToTopOfDeck(hides);
		scn.MoveCardsToDiscard(otherHides);

		scn.FreepsPass();

		var clefts = scn.GetCurrentSite();
		assertEquals("Isengard Clefts", clefts.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(7, scn.GetShadowDiscardCount());

		assertTrue(scn.ShadowActionAvailable(clefts));
		scn.ShadowUseCardAction(clefts);

		// A minion of either culture who can exert; the Savage, with 1 vitality, cannot.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footmen, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(footmen));

		assertTrue(scn.ShadowHasCardChoiceAvailable(axe1, spear, scaffolding, club));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(library, pinned, toby, hides));
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(axe1);

		// The Axe is played as from hand: on a [dunland] Man of the Shadow player's choosing, for its twilight cost.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footmen, savage));
		scn.ShadowChooseCard(savage);
		assertAttachedTo(axe1, savage);
		assertNotAttachedTo(axe1, footmen);
		assertEquals(9, scn.GetTwilight());         // 10 - 1 for the Iron Axe
		assertEquals(6, scn.GetShadowDiscardCount());       // the seven laid there, less the Axe
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No limit.  The Axe has left the pile, and its copy in hand is not offered in its place.
		assertTrue(scn.ShadowActionAvailable(clefts));
		scn.ShadowUseCardAction(clefts);
		assertTrue(scn.ShadowHasCardChoiceAvailable(footmen, troop));
		scn.ShadowChooseCard(footmen);
		assertEquals(1, scn.GetWoundsOn(footmen));
		assertEquals(1, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, scaffolding, club));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe2, library, pinned, toby, hides));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(scaffolding);
		assertInZone(Zone.SUPPORT, scaffolding);
		assertEquals(8, scn.GetTwilight());         // 9 - 1 for Scaffolding

		// The Troop is the one minion left who can exert, and the one [moria] minion for the Spear.
		assertTrue(scn.ShadowActionAvailable(clefts));
		scn.ShadowUseCardAction(clefts);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, club));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(spear);
		assertAttachedTo(spear, troop);
		assertEquals(7, scn.GetTwilight());         // 8 - 1 for the Goblin Spear

		// Every minion is exhausted.  The Club is still in the discard pile, costs nothing and could go on the
		// Footmen, but there is nobody to exert for it.
		assertTrue(scn.IsExhausted(footmen));
		assertTrue(scn.IsExhausted(savage));
		assertTrue(scn.IsExhausted(troop));
		assertInDiscard(club);
		assertFalse(scn.ShadowActionAvailable(clefts));
		assertInDiscard(library, pinned, toby, otherHides);
		assertInZone(Zone.HAND, axe2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoShadowPossessionInTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footmen = scn.GetShadowCard("footmen");
		var troop = scn.GetShadowCard("troop");
		var axe1 = scn.GetShadowCard("axe1");
		var hides = scn.GetShadowCard("hides");
		var otherHides = scn.GetFreepsCard("hides");
		var library = scn.GetShadowCard("library");
		var pinned = scn.GetShadowCard("pinned");
		var toby = scn.GetShadowCard("toby");

		scn.StartGame();
		scn.SkipToSite(8);

		scn.MoveMinionsToTable(footmen, troop);
		// An artifact, a condition and a Free Peoples possession in the Shadow player's discard pile; Shadow
		// possessions in his hand, in his draw deck and in the other player's discard pile.
		scn.MoveCardsToDiscard(library, pinned, toby);
		scn.MoveCardsToHand(axe1);
		scn.MoveCardsToTopOfDeck(hides);
		scn.MoveCardsToDiscard(otherHides);

		scn.FreepsPass();

		var clefts = scn.GetCurrentSite();
		assertEquals("Isengard Clefts", clefts.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Both minions could exert, the pool is full and the Axe could be played from hand.
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(10, scn.GetTwilight());        // 1 companion + 9 for the site
		assertTrue(scn.ShadowPlayAvailable(axe1));
		assertFalse(scn.ShadowActionAvailable(clefts));
	}

	@Test
	public void IsNotOfferedOnceThePoolCannotPayForAPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footmen = scn.GetShadowCard("footmen");
		var troop = scn.GetShadowCard("troop");
		var axe1 = scn.GetShadowCard("axe1");
		var scaffolding = scn.GetShadowCard("scaffolding");

		scn.StartGame();
		scn.SkipToSite(8);

		scn.MoveMinionsToTable(footmen, troop);
		scn.MoveCardsToDiscard(axe1, scaffolding);

		scn.FreepsPass();

		var clefts = scn.GetCurrentSite();
		assertEquals("Isengard Clefts", clefts.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(clefts));

		// The pool is cut to exactly the cost of one of the two possessions, each of which costs 1.
		scn.SetTwilight(1);
		scn.ShadowUseCardAction(clefts);
		scn.ShadowChooseCard(troop);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe1, scaffolding));
		scn.ShadowChooseCardBPFromSelection(axe1);
		assertAttachedTo(axe1, footmen);
		assertEquals(0, scn.GetTwilight());

		// Scaffolding is still in the pile and the Footmen can still exert; there is nothing to pay for it with.
		assertInDiscard(scaffolding);
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.ShadowActionAvailable(clefts));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
