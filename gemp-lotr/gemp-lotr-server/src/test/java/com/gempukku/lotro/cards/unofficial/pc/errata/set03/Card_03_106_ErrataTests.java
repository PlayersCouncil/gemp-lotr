package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_106_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "53_106");
					put("sam", "1_311");    // Sam, Son of Hamfast: Fellowship ability used only on purpose; a response if Frodo dies
					put("merry", "1_302");  // Merry, Friend to Sam: another [shire] Hobbit companion
					put("pan", "3_108");    // Frying Pan: a possession a Hobbit bears, with a Skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillthePonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Bill the Pony
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Pony
		 * Game Text: Stealth. Bearer must be Sam.
		 * Each site's Shadow number is -1.
		 * Discard this possession when at an underground site.
		 */

		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");

		assertEquals("Bill the Pony", bill.getBlueprint().getTitle());
		assertNull(bill.getBlueprint().getSubtitle());
		assertTrue(bill.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bill.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, bill.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bill.getBlueprint().getCardType());
		assertTrue(bill.getBlueprint().getPossessionClasses().contains(PossessionClass.PONY));
		assertTrue(scn.HasKeyword(bill, Keyword.STEALTH));
		assertEquals(0, bill.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(bill, sam);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		// Frodo and Merry are Hobbit companions, but neither is Sam.
		assertFalse(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(bill);
		assertAttachedTo(bill, sam);
		assertEquals(2, scn.GetTwilight()); // Bill costs nothing
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LowersTheShadowNumberOfTheSiteMovedToAndStaysAwayFromUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(bill);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.FreepsPlayCard(bill);
		assertAttachedTo(bill, sam);
		assertEquals(0, scn.GetTwilight());

		// To Ettenmoors, Shadow number 2, which is not underground.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertEquals(3, scn.GetTwilight()); // 2 - 1 for the site + 2 companions
		assertAttachedTo(bill, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedOnArrivingAtAnUndergroundSiteAndEverySiteIsLoweredUntilThen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		var pan = scn.GetFreepsCard("pan");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, bill, pan);

		// Ettenmoors (2) is behind the fellowship and Rivendell Valley (0) is where it stands: both are sites.
		var ettenmoors = scn.GetSite(2);
		var rivendell = scn.GetSite(3);
		assertEquals(1, scn.GetTwilightCost(ettenmoors));
		assertEquals(0, scn.GetTwilightCost(rivendell));

		// To Balin's Tomb, Shadow number 3, underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInDiscard(bill);
		assertAttachedTo(pan, sam);
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAnUndergroundSiteItIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(bill);
		scn.FreepsRefreshDecision();

		// Balin's Tomb is underground: Bill is discarded as he comes into play.
		assertTrue(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(bill);
		assertInDiscard(bill);
		assertEquals(0, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("F-0233: Bill is discarded before the underground site's Shadow number is counted; Decipher's ruling on 3_106 has the number reduced first")
	public void TheUndergroundSitesShadowNumberIsReducedBeforeBillIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, bill);

		// Decipher's ruling on 3_106 (Q-0133): "When the fellowship moves with Bill the Pony to an underground site,
		// the Shadow number of that site is reduced before Bill the Pony is discarded."  To Balin's Tomb (3): the
		// move adds 3 - 1 for the site and 2 for Frodo and Sam.
		int pool = scn.GetTwilight();
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(pool + 2 + 2, scn.GetTwilight());
		assertInDiscard(bill);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
