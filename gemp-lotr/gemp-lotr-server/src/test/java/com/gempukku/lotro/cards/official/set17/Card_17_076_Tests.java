package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_76");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path: each site is laid from an adventure deck as the game goes.
	protected VirtualTableScenario GetSitesLaidFromAdventureDecksScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend", "17_76");
					// Sméagol, Old Noser: the companion the two cards below spot; a regroup ability that exerts
					// him twice, used only on purpose
					put("smeagol", "5_28");
					// One Good Turn Deserves Another: Fellowship or Regroup event, cost 0. "Spot Sméagol to play
					// the fellowship's next site. Then you may add a burden to take this card back into hand."
					put("turn1", "11_49");
					put("turn2", "11_49");
					// There's Another Way: support-area condition; Sméagol is strength +1. "Regroup: Discard this
					// condition to replace the fellowship's current site with a site from your adventure deck."
					put("way1", "12_40");
					put("way2", "12_40");
				}},
				// site2 is Abandoned Mine Shaft (underground; its burden comes only on a regroup move from it
				// outside region 1), site5 Sirannon Ruins (river, no text), site4 Nurn, site8 The Angle
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void OrkishFiendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Fiend
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 18
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] Orc.<br>While at an underground site, this minion is <b>fierce</b> and the Free Peoples player may not replace sites or play a site from his or her adventure deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Orkish Fiend", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(18, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// For the card's own batch: this board does not tell "may not replace sites" from "may not play a site from
	// his or her adventure deck".  The game replaces a site only by playing one from an adventure deck, so the
	// second bar alone stops both cards below; the first alone would let the event play site 3.
	@Test
	public void AtAnUndergroundSiteTheFreePeoplesPlayerCanNeitherPlayNorReplaceASiteAndElsewhereHeCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSitesLaidFromAdventureDecksScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var turn1 = scn.GetFreepsCard("turn1");
		var turn2 = scn.GetFreepsCard("turn2");
		var way1 = scn.GetFreepsCard("way1");
		var way2 = scn.GetFreepsCard("way2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(way1, way2);
		scn.MoveCardsToHand(turn1, turn2);

		var fiend = scn.GetShadowCard("fiend");

		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");
		var nurn = scn.GetFreepsSite("site4");
		var angle = scn.GetFreepsSite("site8");

		// The fellowship moves to Abandoned Mine Shaft, an underground site, where the Fiend stands.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(fiend);
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(shaft, scn.GetCurrentSite());
		assertNull(scn.GetSite(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(turn1));

		// The event is played and plays no site: the Free Peoples player is not asked for one, and goes
		// straight on to its second sentence.
		scn.FreepsPlayCard(turn1);
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, nurn, angle);
		assertInDiscard(turn1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The condition is discarded, which is its cost, and the site under the fellowship is not replaced.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(way1));
		scn.FreepsUseCardAction(way1);
		assertInDiscard(way1);
		assertSame(shaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, nurn, angle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The fellowship moves on with the Fiend still in play.  The site it moves to is the Shadow player's
		// to lay, and he lays it: Sirannon Ruins, which is not underground.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(ruins, scn.GetCurrentSite());
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertNull(scn.GetSite(4));

		// Here the same event plays the fellowship's next site from his adventure deck...
		scn.FreepsPlayCard(turn2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(nurn, angle));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(nurn);
		assertSame(nurn, scn.GetSite(4));
		scn.FreepsChooseNo();
		assertInDiscard(turn2);

		// ...and the other copy of the condition replaces the site under the fellowship with one of his.
		scn.ShadowPass();
		scn.FreepsUseCardAction(way2);
		assertInDiscard(way2);
		scn.FreepsChooseCardBPFromSelection(angle);
		assertSame(angle, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, ruins);
		assertSame(nurn, scn.GetSite(4));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
