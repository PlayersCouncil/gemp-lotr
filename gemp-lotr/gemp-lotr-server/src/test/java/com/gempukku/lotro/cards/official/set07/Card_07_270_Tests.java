package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_270_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attacker", "7_270");
					put("attacker2", "7_270");
					put("battlement", "7_262");  // Above the Battlement: plays a besieger stacked on a site you control (or removes a burden to play a [sauron] Orc from the discard pile)
					put("battlement2", "7_262");
					put("teeth", "5_110");       // Teeth of Mordor: Shadow: play a [sauron] Orc stacked on this condition as if played from hand
					put("pillager", "7_275");    // Gorgoroth Pillager: another besieger Orc, cost 4, site 5; his text works only while he is stacked
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothAttackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Attacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.
		 * When you play this minion from a site you control, add a burden for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attacker");

		assertEquals("Gorgoroth Attacker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromASiteYouControlItAddsABurdenForEachSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attacker = scn.GetShadowCard("attacker");
		var attacker2 = scn.GetShadowCard("attacker2");
		var battlement = scn.GetShadowCard("battlement");
		var battlement2 = scn.GetShadowCard("battlement2");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, site 2 to the Shadow player; both Attackers stacked on site 2.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.StackCardsOn(site2, attacker, attacker2);
		assertTrue(scn.GetStackedCards(site2).contains(attacker));
		assertTrue(scn.GetStackedCards(site2).contains(attacker2));
		assertInZone(Zone.STACKED, attacker, attacker2);
		scn.SetShadowHand(battlement, battlement2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(battlement);
		assertTrue(scn.ShadowHasCardChoiceAvailable(attacker, attacker2));
		scn.ShadowChooseCard(attacker);

		// One site of his own; the Free Peoples player's site is not counted.
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertEquals(1, scn.FreepsGetBurdens());

		scn.ShadowTakeControlOfSite();
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());

		// No [sauron] Orc in the discard pile, so the burden is no price to choose: the Attacker on the site is played.
		scn.ShadowPlayCard(battlement2);

		// 1 + 2 for two sites of his own: the Attacker already in play does not answer this one's play.
		assertInZone(Zone.SHADOW_CHARACTERS, attacker2);
		assertEquals(3, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromHandOrFromAConditionItAddsNoBurdenNorDoesAnotherBesiegerPlayedFromTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attacker = scn.GetShadowCard("attacker");
		var attacker2 = scn.GetShadowCard("attacker2");
		var battlement = scn.GetShadowCard("battlement");
		var teeth = scn.GetShadowCard("teeth");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// One Attacker in hand, one stacked on Teeth of Mordor (a condition, not a site); a Pillager on the site.
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, attacker2);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertTrue(scn.GetStackedCards(teeth).contains(attacker2));
		assertInZone(Zone.STACKED, pillager, attacker2);
		scn.SetShadowHand(attacker, battlement);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPlayCard(attacker);
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowUseCardAction(teeth);
		assertInZone(Zone.SHADOW_CHARACTERS, attacker2);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPlayCard(battlement);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
