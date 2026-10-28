package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_316_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "7_316");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, site 6; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("pillager1", "7_275");  // Gorgoroth Pillager: besieger Orc, strength 10; while stacked on a site its owner controls, besiegers are fierce
					put("pillager2", "7_275");
					// Gorgoroth Garrison: besieger Orc, strength 9; "Regroup: Discard 2 cards from hand to stack a
					// [sauron] Orc on a site you control (or discard 1 card from hand if that Orc is a besieger)"
					put("garrison", "7_273");
					// Great Hill Troll: [sauron] besieger Troll, strength 16, fierce; its trigger is for sites 5K to 9K
					put("troll", "8_102");
					put("uruk", "4_180");       // Uruk Besieger: [isengard] Uruk-hai with the word in his title only; strength 6
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1, no other text
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
					put("runner", "1_178");     // Goblin Runner: a card in hand to discard for the Garrison
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TroopTowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Troop Tower
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Engine</b>. To play, spot a [sauron] Orc.<br>All [sauron] Orcs are strength +1 for each besieger stacked on a site.<br><b>Regroup:</b> Discard a besieger to take control of a site. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Troop Tower", card.getBlueprint().getTitle());
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

		var tower = scn.GetShadowCard("tower");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(tower, chieftain);
		assertInHand(tower);
		assertInHand(chieftain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentry, hound);
		scn.ShadowRefreshDecision();

		// A [sauron] minion who is no Orc and an Orc who is not [sauron]: not playable.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(tower);
		assertFalse(scn.ShadowPlayAvailable(tower));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedWithABesiegerOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(tower);
		assertInHand(tower);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(garrison);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(tower);
		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AllSauronOrcsAreStrengthPlusOneForEachBesiegerStackedOnASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var teeth = scn.GetShadowCard("teeth");
		var chieftain = scn.GetShadowCard("chieftain");
		var garrison = scn.GetShadowCard("garrison");
		var pillager1 = scn.GetShadowCard("pillager1");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var pillager2 = scn.GetShadowCard("pillager2");
		var soldier = scn.GetShadowCard("soldier");
		var uruk = scn.GetShadowCard("uruk");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(tower, teeth);
		assertInZone(Zone.SUPPORT, tower);
		assertInZone(Zone.SUPPORT, teeth);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, garrison, pillager1, sentry, hound);

		// Nothing stacked: the printed strengths.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(garrison));
		assertEquals(10, scn.GetStrength(pillager1));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));

		// A besieger stacked on a condition, and a [sauron] Orc who is no besieger and an Uruk-hai who is one by his
		// title only stacked on the site: none of them counts.
		scn.StackCardsOn(teeth, pillager2);
		scn.StackCardsOn(site1, soldier, uruk);
		assertTrue(scn.GetStackedCards(teeth).contains(pillager2));
		assertTrue(scn.GetStackedCards(site1).contains(soldier));
		assertTrue(scn.GetStackedCards(site1).contains(uruk));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(garrison));

		// A besieger Troll stacked on the site: +1 to each [sauron] Orc, the besiegers in play among them.
		scn.StackCardsOn(site1, troll);
		assertTrue(scn.GetStackedCards(site1).contains(troll));
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(garrison));
		assertEquals(11, scn.GetStrength(pillager1));
		// A [sauron] Uruk-hai and a [wraith] Orc are not raised.
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));

		// A besieger Orc stacked there as well: +2.
		scn.StackCardsOn(site1, pillager1);
		assertTrue(scn.GetStackedCards(site1).contains(pillager1));
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(11, scn.GetStrength(garrison));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABesiegerStackedByLegalPlayRaisesEachSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var chieftain = scn.GetShadowCard("chieftain");
		var garrison = scn.GetShadowCard("garrison");
		var pillager = scn.GetShadowCard("pillager1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.SetShadowHand(runner);
		scn.MoveMinionsToTable(chieftain, garrison, pillager);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertInHand(runner);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(garrison));

		// The one card in hand is discarded, and one site is controlled: only the Orc to stack is chosen.
		scn.ShadowUseCardAction(garrison, "Stack a besieger");
		scn.ShadowChooseCard(pillager);

		assertInDiscard(runner);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(garrison));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABesiegerStackedOnASiteTheOtherPlayerControlsIsCountedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager1");
		var theirPillager = scn.GetFreepsCard("pillager1");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Each player controls a site; the Free Peoples player has stacked a besieger of his own on his.
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(7, scn.GetStrength(chieftain));

		// "Stacked on a site", not "on a site you control" (LR 7 asks which).
		scn.StackCardsOn(site2, theirPillager);
		assertTrue(scn.GetStackedCards(site2).contains(theirPillager));
		assertEquals(8, scn.GetStrength(chieftain));

		scn.StackCardsOn(site1, pillager);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertEquals(9, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsABesiegerToTakeControlOfASiteThenDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var pillager1 = scn.GetShadowCard("pillager1");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		var pillager2 = scn.GetShadowCard("pillager2");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.StackCardsOn(site1, pillager2);
		scn.MoveMinionsToTable(pillager1, troll, chieftain, uruk);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Regroup at site 4: sites 1 and 2 lie behind both markers, and site 1 is his already.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.ShadowControls(site2));
		assertTrue(scn.GetStackedCards(site1).contains(pillager2));
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		// A besieger Orc and a besieger Troll in play; not a [sauron] Orc who is no besieger, nor an Uruk-hai who is
		// one by his title only, nor the besieger stacked on the site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pillager1, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, uruk, pillager2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertInDiscard(troll);
		assertInPlay(pillager1);
		assertTrue(scn.GetStackedCards(site1).contains(pillager2));
		assertTrue(scn.ShadowControls(site2));
		assertInDiscard(tower);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoBesiegerInPlayTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		var pillager = scn.GetShadowCard("pillager1");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.MoveMinionsToTable(chieftain, uruk);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A site 2 lies behind both markers to be taken, but the only besieger is stacked, not in play.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertFalse(scn.ShadowActionAvailable(tower));
		assertInZone(Zone.SUPPORT, tower);
	}

	@Test
	public void WithNoSiteToTakeTheAbilityIsStillOfferedAndTheBesiegerIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var pillager = scn.GetShadowCard("pillager1");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Turn 1: the Shadow player's marker is still on site 1, so no site lies behind both markers.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		// The one besieger is discarded unasked.
		assertInDiscard(pillager);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertInDiscard(tower);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
