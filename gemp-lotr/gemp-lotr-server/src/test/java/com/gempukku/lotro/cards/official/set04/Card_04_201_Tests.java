package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "4_201");
					put("veteran2", "4_201");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Pass of the Spider: a [gollum] possession in the Shadow support area.  "Skirmish: If Shelob
					// is skirmishing, stack another minion from play here ...": how a Veteran comes to be
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
	public void UrukVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Regroup:</b> Stack this minion on a site you control.
		 * <b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Uruk Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandForThreeAndTwoMoreWhileRoamingWithNoDiscountThoughASiteIsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveCardsToHand(veteran);

		// 1 set + 1 companion + 3 for Balin's Tomb
		scn.SetTwilight(1);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(veteran));

		// 3 to play + 2 roaming (site number 5, at site 4).  The -1 belongs to the play from a controlled site.
		scn.ShadowPlayCard(veteran);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionStacksThisVeteranOnAChosenSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
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
		scn.MoveMinionsToTable(veteran, veteran2, troop);

		// A Regroup action: not offered in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veteran));
		assertTrue(scn.ShadowActionAvailable(veteran2));
		assertFalse(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(veteran);
		// "A site you control": the Shadow player's two, and not the one the other player controls.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(site3);
		assertInZone(Zone.STACKED, veteran);
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertTrue(scn.GetStackedCards(site3).contains(veteran));
		assertEquals(0, scn.GetStackedCards(site2).size());
		assertEquals(0, scn.GetStackedCards(site1).size());
		// "This minion": the other Veteran and the Troop are still on the table.
		assertInZone(Zone.SHADOW_CHARACTERS, veteran2, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		// Taken by the first player on a turn when he was the Shadow player.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A site is controlled, but not by the Veteran's owner: there is nowhere to stack him.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(0, scn.GetStackedCards(site1).size());
	}

	@Test
	public void ShadowActionPlaysThisVeteranFromASiteItsOwnerControlsForOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, veteran, veteran2);

		// 3 set + 1 companion + 6 for The Bridge of Khazad-dum
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, veteran, veteran2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veteran));
		assertTrue(scn.ShadowActionAvailable(veteran2));

		// 3 - 1, and no roaming at site 5
		scn.ShadowUseCardAction(veteran);
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		// "This minion": the other Veteran is still on the site.
		assertInZone(Zone.STACKED, veteran2);
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(site1).contains(veteran2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPaysTheRoamingPenaltyAndIsNotOfferedOneTwilightShort() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, veteran);

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One short of 3 - 1 + 2 roaming (site number 5, at site 4)
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(veteran));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(veteran));

		scn.ShadowUseCardAction(veteran);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedFromAShadowPossessionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
		var troop = scn.GetShadowCard("troop");
		var pass = scn.GetShadowCard("pass");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(site1, veteran);
		scn.StackCardsOn(pass, veteran2);

		// 13 set + 1 companion + 6 for The Bridge of Khazad-dum: enough for both Veterans
		scn.SetTwilight(13);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.STACKED, veteran, veteran2);

		// "If stacked on a site you control": the one on the site, and not the one on the possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veteran));
		assertFalse(scn.ShadowActionAvailable(veteran2));

		// A Shadow action: on the same site with the same pool, not offered in the maneuver phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(20, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertInZone(Zone.STACKED, veteran, veteran2);
	}
}
