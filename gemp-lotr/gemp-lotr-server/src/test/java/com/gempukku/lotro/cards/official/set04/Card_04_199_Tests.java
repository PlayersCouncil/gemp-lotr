package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_199_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "4_199");
					put("trooper2", "4_199");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Pass of the Spider: a [gollum] possession in the Shadow support area.  "Skirmish: If Shelob
					// is skirmishing, stack another minion from play here ...": how a Trooper comes to be
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
	public void UrukTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Regroup:</b> Stack this minion on a site you control.
		 * <b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Uruk Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandForFourAndTwoMoreWhileRoamingWithNoDiscountThoughASiteIsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveCardsToHand(trooper);

		// 2 set + 1 companion + 3 for Balin's Tomb
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(trooper));

		// 4 to play + 2 roaming (site number 5, at site 4).  The -1 belongs to the play from a controlled site.
		scn.ShadowPlayCard(trooper);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionStacksThisTrooperOnAChosenSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
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
		scn.MoveMinionsToTable(trooper, trooper2, troop);

		// A Regroup action: not offered in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trooper));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(trooper));
		assertTrue(scn.ShadowActionAvailable(trooper2));
		assertFalse(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(trooper);
		// "A site you control": the Shadow player's two, and not the one the other player controls.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(site3);
		assertInZone(Zone.STACKED, trooper);
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertTrue(scn.GetStackedCards(site3).contains(trooper));
		assertEquals(0, scn.GetStackedCards(site2).size());
		assertEquals(0, scn.GetStackedCards(site1).size());
		// "This minion": the other Trooper and the Troop are still on the table.
		assertInZone(Zone.SHADOW_CHARACTERS, trooper2, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		// Taken by the first player on a turn when he was the Shadow player.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.MoveMinionsToTable(trooper);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A site is controlled, but not by the Trooper's owner: there is nowhere to stack him.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trooper));
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertEquals(0, scn.GetStackedCards(site1).size());
	}

	@Test
	public void ShadowActionPlaysThisTrooperFromASiteItsOwnerControlsForOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, trooper, trooper2);

		// 3 set + 1 companion + 6 for The Bridge of Khazad-dum
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, trooper, trooper2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(trooper));
		assertTrue(scn.ShadowActionAvailable(trooper2));

		// 4 - 1, and no roaming at site 5
		scn.ShadowUseCardAction(trooper);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		// "This minion": the other Trooper is still on the site.
		assertInZone(Zone.STACKED, trooper2);
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(site1).contains(trooper2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPaysTheRoamingPenaltyAndIsNotOfferedOneTwilightShort() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, trooper);

		// 0 set + 1 companion + 3 for Balin's Tomb: one short of 4 - 1 + 2 roaming (site number 5, at site 4)
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trooper));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(trooper));

		scn.ShadowUseCardAction(trooper);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedFromAShadowPossessionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
		var troop = scn.GetShadowCard("troop");
		var pass = scn.GetShadowCard("pass");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(site1, trooper);
		scn.StackCardsOn(pass, trooper2);

		// 13 set + 1 companion + 6 for The Bridge of Khazad-dum: enough for both Troopers
		scn.SetTwilight(13);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.STACKED, trooper, trooper2);

		// "If stacked on a site you control": the one on the site, and not the one on the possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(trooper));
		assertFalse(scn.ShadowActionAvailable(trooper2));

		// A Shadow action: on the same site with the same pool, not offered in the maneuver phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(20, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(trooper));
		assertInZone(Zone.STACKED, trooper, trooper2);
	}
}
