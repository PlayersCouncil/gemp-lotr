package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("companies", "8_107");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, site 6; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, site 6, damage +1
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc; his text needs 2 wounds on the Ring-bearer
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] condition that play stacks a [sauron] Orc on
					put("garrison", "7_273");   // Gorgoroth Garrison: besieger [sauron] Orc; regroup actions used only on purpose
					put("pillager", "7_275");   // Gorgoroth Pillager: besieger [sauron] Orc; its text works only while stacked

					put("gimli", "1_13");       // Gimli, Son of Gloin: a second companion, so that two threats can be laid
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirMarchingCompaniesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Their Marching Companies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Engine</b>. To play, spot a [sauron] minion.
		 * <b>Regroup:</b> Remove a threat to stack your [sauron] minion on a site you control.
		 * <b>Shadow:</b> Remove a threat to play a [sauron] minion stacked on a site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("companies");

		assertEquals("Their Marching Companies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.ENGINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");

		scn.StartGame();
		scn.SetShadowHand(companies, chieftain);
		scn.MoveCardsToSupportArea(teeth);
		scn.SetTwilight(10);
		assertInHand(companies, chieftain);
		assertInZone(Zone.SUPPORT, teeth);
		scn.FreepsPass();

		// A [wraith] Orc and a [sauron] condition: neither is a [sauron] minion.
		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(companies));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(companies));

		scn.ShadowPlayCard(companies);
		assertInZone(Zone.SUPPORT, companies);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingASauronUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SetShadowHand(companies);
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		assertInHand(companies);
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(companies));
		scn.ShadowPlayCard(companies);
		assertInZone(Zone.SUPPORT, companies);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesAThreatToStackYourSauronMinionOnASiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		// Player 1's fourth turn: sites 1 to 3 are behind every marker.
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(companies);
		scn.MoveMinionsToTable(chieftain, guard, hound);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.SUPPORT, companies);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, guard, hound);
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		assertEquals(1, scn.GetThreats());

		// The site is asked first: either site the Shadow player controls; not the one the other player controls,
		// nor one on the path.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3, site4));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		// A [sauron] Orc and a [sauron] Uruk-hai; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.STACKED, guard);
		assertTrue(scn.GetStackedCards(site2).contains(guard));
		assertFalse(scn.GetStackedCards(site1).contains(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(companies);
		scn.MoveMinionsToTable(chieftain);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, companies);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(companies));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(companies));
	}

	@Test
	public void RegroupAbilityWithNoSauronMinionOfHisRemovesTheThreatForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(companies);
		scn.MoveMinionsToTable(hound);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, companies);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityWithNoSiteHeControlsRemovesTheThreatForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		// The player whose fellowship is moving controls site 1; the Shadow player controls none.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(companies);
		scn.MoveMinionsToTable(chieftain);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SUPPORT, companies);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesAThreatToPlayASauronMinionStackedOnASiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var garrison = scn.GetShadowCard("garrison");
		var pillager = scn.GetShadowCard("pillager");
		var theirChieftain = scn.GetFreepsCard("chieftain");

		scn.StartGame();
		// Player 1's third turn: sites 1 and 2 are behind every marker.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// On his site: a [sauron] Orc, a [sauron] Uruk-hai and a [wraith] Orc.  A [sauron] Orc stacked on a
		// condition, one in the discard pile, and the other player's [sauron] Orc on the site he controls.
		scn.StackCardsOn(site1, chieftain, guard, hound);
		scn.MoveCardsToSupportArea(companies, teeth);
		scn.StackCardsOn(teeth, garrison);
		scn.MoveCardsToDiscard(pillager);
		scn.StackCardsOn(site2, theirChieftain);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(20);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertInZone(Zone.STACKED, chieftain, guard, hound, garrison, theirChieftain);
		assertInDiscard(pillager);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, garrison, pillager, theirChieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertInZone(Zone.STACKED, chieftain, hound, garrison, theirChieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, chieftain);
		scn.MoveCardsToSupportArea(companies);
		scn.SetTwilight(20);
		assertInZone(Zone.STACKED, chieftain);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(companies));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(companies));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoSauronMinionItCouldPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var garrison = scn.GetShadowCard("garrison");
		var pillager = scn.GetShadowCard("pillager");
		var theirChieftain = scn.GetFreepsCard("chieftain");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, hound);
		scn.MoveCardsToSupportArea(companies, teeth);
		scn.StackCardsOn(teeth, garrison);
		scn.MoveCardsToDiscard(pillager);
		scn.StackCardsOn(site2, theirChieftain);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(20);
		assertInZone(Zone.STACKED, hound, garrison, theirChieftain);
		assertInDiscard(pillager);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(companies));

		// A [sauron] Orc put on his site, and it is offered.
		scn.StackCardsOn(site1, chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(companies));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileThePoolIsShortOfTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, chieftain);
		scn.MoveCardsToSupportArea(companies);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.STACKED, chieftain);
		scn.FreepsPass();

		// The Chieftain costs 2 + 2 for roaming: site number 6 at site 3.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(companies));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
