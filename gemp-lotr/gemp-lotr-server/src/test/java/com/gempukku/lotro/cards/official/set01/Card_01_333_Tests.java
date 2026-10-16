package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_333_Tests
{

	// Midgewater Moors is site 2; the rest of the path is that of FellowshipSites.  Each test moves there from site 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards of each type for the Free Peoples hand
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, cost 4; a maneuver ability
					put("sword", "1_299");        // Hobbit Sword: [shire] possession; "Bearer must be a Hobbit" only
					put("appetite", "1_294");     // Hobbit Appetite: [shire] Fellowship event
					put("backstabber", "1_174");  // Goblin Backstabber: [moria] Orc minion, cost 1, site 4; damage +2 beside another [moria] Orc in its skirmish

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai minion, cost 2, site 5; damage +1 only
					put("spear", "1_182");        // Goblin Spear: [moria] possession, cost 1, on a [moria] minion; acts only when its bearer kills
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_333");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MidgewaterMoorsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Midgewater Moors
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Plains</b>. Each time a minion is played, the Free Peoples player discards a card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Midgewater Moors", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachMinionPlayedMakesTheFreePeoplesPlayerDiscardACardOfHisChoiceFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		// A Shadow card in the Free Peoples hand
		var freepsBackstabber = scn.GetFreepsCard("backstabber");
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(aragorn, sword, appetite, freepsBackstabber);
		scn.MoveCardsToHand(backstabber, savage, spear);

		scn.StartGame();
		assertInHand(aragorn, sword, appetite, freepsBackstabber);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 2 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("1_333", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());

		// A [moria] Orc: the Free Peoples player chooses one card from his hand, of whatever type or side.
		scn.ShadowPlayCard(backstabber);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sword, appetite, freepsBackstabber));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, backstabber));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sword);

		assertInZone(Zone.DISCARD, sword);
		assertInHand(aragorn, appetite, freepsBackstabber);
		assertEquals(3, scn.GetFreepsHandCount());
		// The Shadow player's hand is short only the minion he played.
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai: a second discard.
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, appetite, freepsBackstabber));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(appetite);

		assertInZone(Zone.DISCARD, appetite);
		assertInHand(aragorn, freepsBackstabber);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow card that is not a minion sets nothing off.
		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, backstabber);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionPlayedHereSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		// The first player's next fellowship phase, with his fellowship still at Midgewater Moors.
		scn.SkipToSite(2);
		assertEquals("1_333", scn.GetCurrentSite().getBlueprintId());

		assertInHand(aragorn);
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsPlayAvailable(aragorn));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// Short only the companion played.
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
