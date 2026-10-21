package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "9_48");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that 2 threats can be added
					// Gorgoroth Stormer: [sauron] Orc, vitality 2; "Shadow: If you have initiative, exert this minion to
					// add a burden and make the Free Peoples player draw a card."
					put("stormer", "8_101");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sauron, The Lord of the Rings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 18
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: <b>Damage +2</b>. <b>Enduring</b>. <b>Fierce</b>.
		 * For each burden you spot, threat you spot, and site you control, Sauron's twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("The Lord of the Rings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(18, card.getBlueprint().getTwilightCost());
		assertEquals(24, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsOneLessForEachBurdenThreatAndSiteHisPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		// Sites 1 to 3 are behind both markers, and can be taken under control.
		scn.SkipToSite(4);
		scn.MoveCardsToHand(sauron);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(18, scn.GetTwilightCost(sauron));

		scn.AddBurdens(1);
		assertEquals(17, scn.GetTwilightCost(sauron));
		scn.AddBurdens(1);
		assertEquals(16, scn.GetTwilightCost(sauron));

		// Two companions: two threats can be added.
		scn.AddThreats(1);
		assertEquals(15, scn.GetTwilightCost(sauron));
		scn.AddThreats(1);
		assertEquals(14, scn.GetTwilightCost(sauron));

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		assertEquals(13, scn.GetTwilightCost(sauron));
		scn.ShadowTakeControlOfSite(site2);
		assertEquals(12, scn.GetTwilightCost(sauron));

		// A site the OTHER player controls (taken on a turn of his as the Shadow player) is not one Sauron's
		// player controls.
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.FreepsControls(site3));
		// 18 - 2 burdens - 2 threats - 2 sites
		assertEquals(12, scn.GetTwilightCost(sauron));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersOwnBurdensAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");

		scn.StartGame();
		// Burdens on the second player's own Ring-bearer, laid on his turn.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(3);
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(sauron);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.ShadowGetBurdens());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(18, scn.GetTwilightCost(sauron));

		// One burden on the Ring-bearer of the fellowship he plays against.
		scn.AddBurdens(1);
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(17, scn.GetTwilightCost(sauron));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedForEighteenLessABurdenAddedByPlayInTheShadowPhaseAndTheOthersCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var stormer = scn.GetShadowCard("stormer");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.AddBurdens(1);
		scn.AddThreats(1);
		scn.MoveMinionsToTable(stormer);
		scn.MoveCardsToHand(sauron);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// The Free Peoples hand is empty: the Shadow player has initiative.
		assertTrue(scn.ShadowHasInitiative());
		// 18 - 1 burden - 1 threat - 1 site
		assertEquals(15, scn.GetTwilightCost(sauron));

		scn.ShadowUseCardAction(stormer);
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertEquals(2, scn.GetBurdens());
		assertEquals(14, scn.GetTwilightCost(sauron));

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(sauron));
		scn.ShadowPlayCard(sauron);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		// At site 3 Sauron (site 6) is roaming: 18 - 2 burdens - 1 threat - 1 site + 2 roaming
		assertEquals(pool - 16, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
