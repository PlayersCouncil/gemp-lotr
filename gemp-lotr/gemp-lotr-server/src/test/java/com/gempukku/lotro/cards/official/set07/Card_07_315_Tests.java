package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_315_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("walkway", "7_315");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, site 6; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("pillager", "7_275");   // Gorgoroth Pillager: besieger Orc, strength 10; its text works only while stacked
					// Gorgoroth Garrison: besieger Orc, strength 9; regroup abilities that cost cards from hand (none is held)
					put("garrison", "7_273");
					// Great Hill Troll: [sauron] besieger Troll, strength 16, fierce; its trigger is for sites 5K to 9K
					put("troll", "8_102");
					put("uruk", "4_180");       // Uruk Besieger: [isengard] Uruk-hai with the word in his title only; strength 6
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1, no other text
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Southron Explorer: "Regroup: Exert this minion and remove (5) to take control of a site"
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerWalkwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Tower Walkway
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Engine</b>. To play, spot a [sauron] Orc.<br>Each besieger is strength +1 for each site you control.<br><b>Regroup:</b> Discard 3 [sauron] Orcs to take control of a site. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("walkway");

		assertEquals("Tower Walkway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
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

		var walkway = scn.GetShadowCard("walkway");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(walkway, chieftain);
		assertInHand(walkway);
		assertInHand(chieftain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentry, hound);
		scn.ShadowRefreshDecision();

		// A [sauron] minion who is no Orc and an Orc who is not [sauron]: not playable.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(walkway);
		assertFalse(scn.ShadowPlayAvailable(walkway));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(walkway));
		scn.ShadowPlayCard(walkway);

		assertInZone(Zone.SUPPORT, walkway);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedWithABesiegerOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCardsToHand(walkway);
		assertInHand(walkway);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pillager);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(walkway);
		assertTrue(scn.ShadowPlayAvailable(walkway));
		scn.ShadowPlayCard(walkway);

		assertInZone(Zone.SUPPORT, walkway);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachBesiegerIsStrengthPlusOneForEachSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToSupportArea(walkway);
		assertInZone(Zone.SUPPORT, walkway);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pillager, troll, chieftain, uruk);

		// No site controlled: the printed strengths.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(6, scn.GetStrength(uruk));

		// A site the Free Peoples player controls is no site "you" control.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));

		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(11, scn.GetStrength(pillager));
		assertEquals(17, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(6, scn.GetStrength(uruk));

		scn.ShadowTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site3));
		assertEquals(12, scn.GetStrength(pillager));
		assertEquals(18, scn.GetStrength(troll));
		// A [sauron] Orc who is no besieger, and an Uruk-hai who is one by his title only.
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(6, scn.GetStrength(uruk));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteTakenByLegalPlayRaisesEachBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var pillager = scn.GetShadowCard("pillager");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(walkway);
		assertInZone(Zone.SUPPORT, walkway);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(pillager, explorer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowControls(site1));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(5, scn.GetTwilight());

		scn.ShadowUseCardAction(explorer);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowControls(site1));
		assertEquals(11, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsThreeSauronOrcsToTakeControlOfASiteThenDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(walkway);
		assertInZone(Zone.SUPPORT, walkway);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(chieftain, soldier, pillager, garrison, sentry, hound);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowControls(site1));
		assertTrue(scn.ShadowActionAvailable(walkway));
		scn.ShadowUseCardAction(walkway);

		// Four [sauron] Orcs, besiegers and not, of four titles; not the [sauron] Uruk-hai nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier, pillager, garrison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(chieftain, soldier, pillager);

		assertInDiscard(chieftain);
		assertInDiscard(soldier);
		assertInDiscard(pillager);
		assertInPlay(garrison);
		assertInPlay(sentry);
		assertInPlay(hound);
		// Site 1 is the only site behind both markers.
		assertTrue(scn.ShadowControls(site1));
		assertInDiscard(walkway);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyTwoSauronOrcsTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(walkway);
		assertInZone(Zone.SUPPORT, walkway);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(walkway));
		assertInZone(Zone.SUPPORT, walkway);
	}

	@Test
	public void WithNoSiteToTakeTheAbilityIsStillOfferedAndTheOrcsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkway = scn.GetShadowCard("walkway");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCardsToSupportArea(walkway);
		assertInZone(Zone.SUPPORT, walkway);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, soldier, pillager);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Turn 1: the Shadow player's marker is still on site 1, so no site lies behind both markers.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(walkway));
		scn.ShadowUseCardAction(walkway);

		// Three [sauron] Orcs for three: discarded unasked.
		assertInDiscard(chieftain);
		assertInDiscard(soldier);
		assertInDiscard(pillager);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertInDiscard(walkway);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
