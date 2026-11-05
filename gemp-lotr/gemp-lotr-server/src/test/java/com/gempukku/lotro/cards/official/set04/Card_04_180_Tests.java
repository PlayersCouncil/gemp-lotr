package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("besieger", "4_180");
					put("besieger2", "4_180");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Pass of the Spider: a [gollum] possession in the Shadow support area.  "Skirmish: If Shelob
					// is skirmishing, stack another minion from play here ...": how a Besieger comes to be
					// stacked on a Shadow card that is no site.  Its trigger answers a Free Peoples condition's
					// special ability; none is in these tests.
					put("pass", "103_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukBesiegerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Besieger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Regroup:</b> Stack this minion on a site you control.
		 * <b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("besieger");

		assertEquals("Uruk Besieger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandForTwoAndTwoMoreWhileRoamingWithNoDiscountThoughASiteIsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveCardsToHand(besieger);

		// 0 set + 1 companion + 3 for Balin's Tomb
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(besieger));

		// 2 to play + 2 roaming (site number 5, at site 4).  The -1 belongs to the play from a controlled site.
		scn.ShadowPlayCard(besieger);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, besieger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionStacksThisBesiegerOnAChosenSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");
		var besieger2 = scn.GetShadowCard("besieger2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// Site 1 is the first player's, taken on a turn when he was the Shadow player; sites 2 and 3 are the
		// second player's, who is the Shadow player now.
		scn.FreepsTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.IsSiteControlled(site2));
		assertTrue(scn.IsSiteControlled(site3));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(2, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.MoveMinionsToTable(besieger, besieger2, troop);

		// A Regroup action: not offered in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(besieger));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(besieger));
		assertTrue(scn.ShadowActionAvailable(besieger2));
		assertFalse(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(besieger);
		// "A site you control": the Shadow player's two, and not the one the other player controls.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(site3);
		assertInZone(Zone.STACKED, besieger);
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertTrue(scn.GetStackedCards(site3).contains(besieger));
		assertEquals(0, scn.GetStackedCards(site2).size());
		assertEquals(0, scn.GetStackedCards(site1).size());
		// "This minion": the other Besieger and the Troop are still on the table.
		assertInZone(Zone.SHADOW_CHARACTERS, besieger2, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		// Taken by the first player on a turn when he was the Shadow player.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.MoveMinionsToTable(besieger);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A site is controlled, but not by the Besieger's owner: there is nowhere to stack him.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(besieger));
		assertInZone(Zone.SHADOW_CHARACTERS, besieger);
		assertEquals(0, scn.GetStackedCards(site1).size());
	}

	@Test
	public void ShadowActionPlaysThisBesiegerFromASiteItsOwnerControlsForOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");
		var besieger2 = scn.GetShadowCard("besieger2");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, besieger, besieger2);
		assertInZone(Zone.STACKED, besieger, besieger2);

		// 3 set + 1 companion + 6 for The Bridge of Khazad-dum
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, besieger, besieger2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(besieger));
		assertTrue(scn.ShadowActionAvailable(besieger2));

		// 2 - 1, and no roaming at site 5
		scn.ShadowUseCardAction(besieger);
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, besieger);
		// "This minion": the other Besieger is still on the site.
		assertInZone(Zone.STACKED, besieger2);
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(site1).contains(besieger2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPaysTheRoamingPenaltyAndIsNotOfferedOneTwilightShort() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, besieger);
		assertInZone(Zone.STACKED, besieger);

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One short of 2 - 1 + 2 roaming (site number 5, at site 4)
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(besieger));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(besieger));

		scn.ShadowUseCardAction(besieger);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, besieger);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedFromAShadowPossessionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");
		var besieger2 = scn.GetShadowCard("besieger2");
		var troop = scn.GetShadowCard("troop");
		var pass = scn.GetShadowCard("pass");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(site1, besieger);
		scn.StackCardsOn(pass, besieger2);
		assertInZone(Zone.STACKED, besieger, besieger2);

		// 13 set + 1 companion + 6 for The Bridge of Khazad-dum: enough for both Besiegers
		scn.SetTwilight(13);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.STACKED, besieger, besieger2);

		// "If stacked on a site you control": the one on the site, and not the one on the possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(besieger));
		assertFalse(scn.ShadowActionAvailable(besieger2));

		// A Shadow action: on the same site with the same pool, not offered in the maneuver phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(20, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(besieger));
		assertInZone(Zone.STACKED, besieger, besieger2);
	}
}
