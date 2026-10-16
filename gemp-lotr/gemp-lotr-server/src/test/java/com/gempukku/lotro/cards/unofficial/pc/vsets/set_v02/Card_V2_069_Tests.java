package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

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

public class Card_V2_069_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Rider of Rohan: [rohan] Man, cost 2; a skirmish ability that is never used
					put("rider", "4_286");
					put("rider2", "4_286");
					put("knight", "5_35");          // Gondorian Knight: [gondor] Man, cost 2; Knight is his whole text
					// Swift Steed: [rohan] mount, cost 2, for a [rohan] Man; its text is about skirmishes
					put("steed", "7_253");
					// Knight's Mount: [gondor] mount, cost 1, for a knight; its text is about winning a skirmish
					put("knightsmount", "8_40");
					put("spear", "4_288");          // Rider's Spear: [rohan] hand weapon, cost 1, for a [rohan] Man

					put("wargrider", "5_67");       // Warg-rider: [isengard] Orc, a warg-rider
					put("lemenya", "1_232");        // Ulaire Lemenya: a Nazgul; his text is about [wraith] conditions
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai
					// Warg: [isengard] mount, cost 3, for a warg-rider; a response to skirmish abilities
					put("warg", "5_65");
					// Black Steed: [wraith] mount, cost 1, for a Nazgul; discarded only at an underground site
					put("blacksteed", "1_208");
					// Uruk-hai Sword: [isengard] hand weapon, cost 1, for an Uruk-hai; its text is about winning a skirmish
					put("sword", "1_160");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "102_69");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestfoldPlainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Westfold Plains
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: Plains. Each mount is twilight cost -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Westfold Plains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachFreePeoplesMountPlayedAtThePlainsCostsOneLessAndACompanionOrAHandWeaponItsPrintedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var knight = scn.GetFreepsCard("knight");
		var steed = scn.GetFreepsCard("steed");
		var knightsMount = scn.GetFreepsCard("knightsmount");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(knight);
		// The Rider is in hand from the start, so that he can be played when the fellowship phase at the Plains
		// begins; the cards put into hand after that are offered once he has been.
		scn.MoveCardsToHand(rider);

		scn.StartGame();
		scn.SkipToSite(4);

		assertEquals("Westfold Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCardsToHand(steed, knightsMount, spear);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetTwilight());

		// The Rider, who is to bear a mount and is not one: his printed 2.
		scn.FreepsPlayCard(rider);
		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertEquals(2, scn.GetTwilight());

		// Swift Steed, a [rohan] mount: 2 - 1.
		scn.FreepsPlayCard(steed);
		assertAttachedTo(steed, rider);
		assertEquals(3, scn.GetTwilight());

		// Knight's Mount, a [gondor] mount: 1 - 1.
		scn.FreepsPlayCard(knightsMount);
		assertAttachedTo(knightsMount, knight);
		assertEquals(3, scn.GetTwilight());

		// Rider's Spear, a [rohan] possession that is no mount: its printed 1.
		scn.FreepsPlayCard(spear);
		assertAttachedTo(spear, rider);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShadowMountPlayedAtThePlainsCostsOneLessAndAHandWeaponItsPrintedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wargRider = scn.GetShadowCard("wargrider");
		var lemenya = scn.GetShadowCard("lemenya");
		var savage = scn.GetShadowCard("savage");
		var warg = scn.GetShadowCard("warg");
		var blackSteed = scn.GetShadowCard("blacksteed");
		var sword = scn.GetShadowCard("sword");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveMinionsToTable(wargRider, lemenya, savage);
		scn.MoveCardsToHand(warg, blackSteed, sword);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Westfold Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 3 for the site
		assertEquals(14, scn.GetTwilight());

		// Warg, an [isengard] mount: 3 - 1.
		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, wargRider);
		assertEquals(12, scn.GetTwilight());

		// Black Steed, a [wraith] mount: 1 - 1.
		scn.ShadowPlayCard(blackSteed);
		assertAttachedTo(blackSteed, lemenya);
		assertEquals(12, scn.GetTwilight());

		// Uruk-hai Sword, an [isengard] possession that is no mount: its printed 1.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, savage);
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMountPlayedAtTheNextSiteWithThePlainsBehindTheFellowshipCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var steed = scn.GetFreepsCard("steed");
		scn.MoveCardsToHand(rider);

		scn.StartGame();
		scn.SkipToSite(5);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Westfold Plains", scn.GetSite(4).getBlueprint().getTitle());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetSite(4));
		scn.MoveCardsToHand(steed);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(rider);
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(steed);
		assertAttachedTo(steed, rider);
		assertEquals(4, scn.GetTwilight());     // 2 + the Steed's printed 2
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountTransferredAtThePlainsCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var steed = scn.GetFreepsCard("steed");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(rider, rider2);
		// The Spear is in hand from the start, to be the legal play after which the game asks again.
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.SkipToSite(4);

		assertEquals("Westfold Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.AttachCardsTo(rider, steed);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(spear);
		scn.FreepsChooseCard(rider);
		assertEquals(1, scn.GetTwilight());     // the Spear

		// A transfer pays the twilight cost again and is not a play: the Steed is not coming into play, so nothing
		// in play changes what it costs.
		assertTrue(scn.FreepsTransferAvailable(steed));
		scn.FreepsTransferCard(steed);
		assertAttachedTo(steed, rider2);
		assertEquals(3, scn.GetTwilight());     // 1 + the Steed's printed 2
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
