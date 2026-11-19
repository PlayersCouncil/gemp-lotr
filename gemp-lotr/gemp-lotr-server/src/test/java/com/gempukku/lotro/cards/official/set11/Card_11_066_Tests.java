package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traveled", "11_66");
					// Ranger of Osgiliath: [gondor] Man companion, Ranger.  Its regroup ability (discard 2 cards
					// from hand) is never used.
					put("osgiliath", "7_116");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf ranger companion
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion who is no ranger

					// Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples
					// event that spots or exerts a ranger gains the cost "wound a companion"
					put("wrath", "4_261");
					// Easterling Axeman: [raider] Man, strength 6; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WelltraveledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Well-traveled
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship or Regroup
		 * Game Text: To play, exert a  ranger.<br>If the fellowship is in region 1, play the fellowship's next 2 sites.<br>Otherwise, play the fellowship's next site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("traveled");

		assertEquals("Well-traveled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheLastSiteOfRegionOneItExertsARangerToPlayTheNextTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveled = scn.GetFreepsCard("traveled");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(osgiliath, arwen, knight);

		var freepsSite4 = scn.GetFreepsSite(4);
		var freepsSite5 = scn.GetFreepsSite(5);
		var shadowSite4 = scn.GetShadowSite(4);
		var shadowSite5 = scn.GetShadowSite(5);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(traveled);

		// Site 3 is the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(4));
		assertNull(scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4, freepsSite5, shadowSite4, shadowSite5);
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(traveled));

		scn.FreepsPlayCard(traveled);

		// A ranger of either culture and race; not a companion who is no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(osgiliath, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(knight));
		// His own sites 4 and 5 are laid; site 6 is not, and the fellowship has not moved.
		assertSame(freepsSite4, scn.GetSite(4));
		assertSame(freepsSite5, scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4, shadowSite5);
		assertNull(scn.GetSite(6));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, traveled);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegionTwoInTheRegroupPhaseItPlaysTheNextSiteOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveled = scn.GetFreepsCard("traveled");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(osgiliath);

		var freepsSite5 = scn.GetFreepsSite(5);
		var shadowSite5 = scn.GetShadowSite(5);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(traveled);
		// No minion is played, so the turn goes from the move to site 4 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		// The fellowship is at site 4, in region 2, while the other player's marker is still at site 3, in region 1.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertNull(scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite5, shadowSite5);
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(traveled));

		// The only ranger exerts, with nothing to choose.
		scn.FreepsPlayCard(traveled);

		assertEquals(1, scn.GetWoundsOn(osgiliath));
		assertSame(freepsSite5, scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite5);
		assertNull(scn.GetSite(6));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, traveled);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void InRegionThreeItPlaysTheNextSiteOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveled = scn.GetFreepsCard("traveled");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(osgiliath);

		var freepsSite8 = scn.GetFreepsSite(8);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetFreepsHand(traveled);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(8));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite8);
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(traveled);

		assertEquals(1, scn.GetWoundsOn(osgiliath));
		assertSame(freepsSite8, scn.GetSite(8));
		assertNull(scn.GetSite(9));
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, traveled);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyRangerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveled = scn.GetFreepsCard("traveled");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(osgiliath, knight);
		scn.MoveCardsToHand(traveled);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.  The Knight beside him could exert, but is no ranger.
		scn.AddWoundsToChar(osgiliath, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertNull(scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(traveled));
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveled = scn.GetFreepsCard("traveled");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(osgiliath, knight);
		scn.MoveCardsToHand(traveled);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);
		var freepsSite3 = scn.GetFreepsSite(3);
		var freepsSite4 = scn.GetFreepsSite(4);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertNull(scn.GetSite(4));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(traveled));

		scn.FreepsPlayCard(traveled);
		// The event exerts a ranger, so Wrath of Harad, with a [raider] Man spotted, adds "wound a companion" to its
		// cost: a cost of the Free Peoples player's event, which he pays and so chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(osgiliath, knight));
		scn.FreepsChooseCard(knight);

		// The Ranger is the only ranger, and exerts with nothing to choose.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(osgiliath));
		// Site 2 is in region 1: his own sites 3 and 4 are laid.
		assertSame(freepsSite3, scn.GetSite(3));
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.DISCARD, traveled);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
