package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "8_108");

					// [sauron] Orcs of two titles, for "spot a [sauron] Orc"
					put("chieftain", "1_266");   // Orc Chieftain: [sauron] Orc, cost 2, site 6; his text answers a weapon played on him
					put("pillager", "7_275");    // Gorgoroth Pillager: [sauron] Orc besieger; its text works only while stacked
					// and the minions who are no [sauron] Orc
					put("hilltroll", "8_102");   // Great Hill Troll: [sauron] Troll besieger; its trigger is for King sites 5 to 9
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1 and nothing else
					put("hound", "7_192");       // Morgul Hound: [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer
					put("urukbesieger", "4_180"); // Uruk Besieger: [isengard] Uruk-hai, "Besieger" in his title and not the keyword

					// [sauron] engines, one of each type
					put("grond", "8_103");       // Grond: unique [sauron] engine possession, support area; abilities used only on purpose
					put("towers", "7_281");      // Great Siege-towers: [sauron] engine condition, cost 1, no requirement to play
					// and cards that are no engine
					put("peril", "7_280");       // Great Peril of Fire: [sauron] support condition that is no engine
					put("siegeengine", "5_60");  // Siege Engine: [isengard] machine, "Engine" in its title and not the keyword
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollofGorgorothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Troll of Gorgoroth, Abomination of Sauron
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. <b>Fierce</b>. To play, spot a [sauron] Orc.<br>The twilight cost of Troll of Gorgoroth is -2 for each [sauron] engine you spot.<br><b>Regroup:</b> Exert Troll of Gorgoroth to stack your besieger on a site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Troll of Gorgoroth", card.getBlueprint().getTitle());
		assertEquals("Abomination of Sauron", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		var hilltroll = scn.GetShadowCard("hilltroll");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(troll, chieftain);
		scn.MoveMinionsToTable(hilltroll, guard, hound);
		assertInHand(troll, chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, hilltroll, guard, hound);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Shadow phase at site 5.  A [sauron] Troll, a [sauron] Uruk-hai and a [wraith] Orc: none is a [sauron] Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAGorgorothPillager() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(troll);
		scn.MoveMinionsToTable(pillager);
		assertInHand(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(troll));
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsTwoLessForEachSauronEngineAndIsPlayedForThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var peril = scn.GetShadowCard("peril");
		var siegeengine = scn.GetShadowCard("siegeengine");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(troll, chieftain, towers);
		scn.MoveMinionsToTable(pillager);
		scn.MoveCardsToSupportArea(peril, siegeengine);
		assertInHand(troll, chieftain, towers);
		assertInZone(Zone.SUPPORT, peril, siegeengine);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Shadow phase at site 5.  A [sauron] condition that is no engine, a machine with "Engine" in its title and
		// an engine still in hand do not count.  Orc Chieftain in the same hand keeps his own cost.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(chieftain));

		// One engine, a possession.
		scn.MoveCardsToSupportArea(grond);
		scn.ShadowRefreshDecision();
		assertEquals(8, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(chieftain));

		// Two engines, the second a condition played from hand.
		scn.ShadowPlayCard(towers);
		assertInZone(Zone.SUPPORT, towers);
		assertEquals(6, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(chieftain));

		// 10 - 2 - 2, and no roaming at site 5: one short, then exactly enough.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troll));
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsTheTrollToStackYourBesiegerOnASiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var pillager = scn.GetShadowCard("pillager");
		var hilltroll = scn.GetShadowCard("hilltroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var urukbesieger = scn.GetShadowCard("urukbesieger");

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
		scn.MoveMinionsToTable(troll, pillager, hilltroll, chieftain, urukbesieger);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager, hilltroll, chieftain, urukbesieger);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		assertEquals(1, scn.GetWoundsOn(troll));

		// The site is asked first: either site the Shadow player controls; not the one the other player controls,
		// nor one on the path.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3, site4));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		// His besiegers, an Orc and two Trolls, the Troll of Gorgoroth among them; not an [isengard] Uruk-hai with
		// the word in his title only, nor a [sauron] Orc who is no besieger.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, pillager, hilltroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, urukbesieger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hilltroll);

		assertInZone(Zone.STACKED, hilltroll);
		assertTrue(scn.GetStackedCards(site2).contains(hilltroll));
		assertFalse(scn.GetStackedCards(site1).contains(hilltroll));
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager, chieftain, urukbesieger);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheTrollIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(troll, pillager);
		// Vitality 4: three wounds leave him exhausted.
		scn.AddWoundsToChar(troll, 3);
		assertEquals(3, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(3, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(troll, pillager);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
	}

	@Test
	public void RegroupAbilityWithNoSiteHeControlsExertsTheTrollForNothingAndAsksForNoBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		// The player whose fellowship is moving controls site 1; the Shadow player controls none.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// Two besiegers of his, so that a choice of besieger would be a real one.
		scn.MoveMinionsToTable(troll, pillager);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// Offered, its cost being payable (Q-0034), and flagged as doing nothing: nowhere to stack.
		assertTrue(scn.ShadowActionAvailable(troll));
		assertEquals("effect", scn.ShadowActionNoEffect(troll));

		scn.ShadowUseCardAction(troll);
		// The exertion is paid; with no site to stack on, no besieger is asked for (F-0579) and none is stacked.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertFalse(scn.ShadowDecisionAvailable("Choose cards to stack"));
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pillager);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
