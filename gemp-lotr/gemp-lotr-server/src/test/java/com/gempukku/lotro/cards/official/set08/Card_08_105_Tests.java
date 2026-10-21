package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("olog", "8_105");
					put("olog2", "8_105");

					put("pillager", "7_275");   // Gorgoroth Pillager: besieger Orc, cost 4, site 5; besiegers are fierce only while it is stacked on a site its owner controls
					put("troll", "8_102");      // Great Hill Troll: besieger Troll, cost 9, site 5, fierce; cost -1 for each [sauron] engine (none here); its trigger is for sites 5K to 9K
					put("garrison", "7_273");   // Gorgoroth Garrison: besieger Orc, cost 3, strength 9, site 5; regroup actions that stack a [sauron] Orc on a controlled site
					put("garrison2", "7_273");
					put("troop", "8_106");      // Siege Troop: besieger Orc, cost 8; a regroup ability used only on purpose
					put("urukbesieger", "4_180"); // Uruk Besieger: [isengard] Uruk-hai with "Besieger" in the title and not as a keyword
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] condition that play stacks a [sauron] Orc on
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, damage +1
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc; his text needs 2 wounds on the Ring-bearer

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OloghaiofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Olog-hai of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 18
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. <b>Fierce</b>.<br>The twilight cost of this minion is -2 for each [sauron] minion stacked on a site.<br>Each time this minion wins a skirmish, you may play a besieger stacked on a site you control. That besieger is <b>fierce</b> and <b>damage +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("olog");

		assertEquals("Olog-hai of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(18, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsTwoLessForEachSauronMinionStackedOnASiteWhoeverItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var theirChieftain = scn.GetFreepsCard("chieftain");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var garrison = scn.GetShadowCard("garrison");
		var troop = scn.GetShadowCard("troop");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		// Player 1's third turn: sites 1 and 2 are behind every marker.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.SetShadowHand(olog);
		// On a site: an [isengard] Uruk-hai and a [wraith] Orc.  A [sauron] Orc stacked on a condition, a [sauron]
		// minion in play and one in the discard pile.
		scn.StackCardsOn(site1, urukbesieger, hound);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, garrison);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToDiscard(pillager);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertInZone(Zone.STACKED, urukbesieger, hound, garrison);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(pillager);
		assertInHand(olog);
		assertEquals(10, scn.GetTwilightCost(olog));

		scn.StackCardsOn(site1, chieftain);
		assertEquals(8, scn.GetTwilightCost(olog));

		// A [sauron] Uruk-hai on the same site, and the other player's [sauron] Orc on the site he controls.
		scn.StackCardsOn(site1, guard);
		scn.StackCardsOn(site2, theirChieftain);
		// 10 - 2 x 3
		assertEquals(4, scn.GetTwilightCost(olog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASauronOrcStackedOnASiteByLegalPlayMakesItCostTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.SetShadowHand(olog, chieftain);
		scn.MoveMinionsToTable(garrison);
		assertInHand(olog, chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, garrison);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(10, scn.GetTwilightCost(olog));

		// The Garrison discards a card from hand to stack itself, a besieger [sauron] Orc, on the site.
		scn.ShadowUseCardAction(garrison, "Stack a besieger");
		assertTrue(scn.ShadowHasCardChoiceAvailable(olog, chieftain));
		scn.ShadowChooseCard(chieftain);
		assertInDiscard(chieftain);
		assertInZone(Zone.STACKED, garrison);
		assertTrue(scn.GetStackedCards(site1).contains(garrison));

		assertEquals(8, scn.GetTwilightCost(olog));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsPlayedFromHandForItsCostLessTwoForEachSauronMinionStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, chieftain, guard);
		scn.SetShadowHand(olog);
		assertInZone(Zone.STACKED, chieftain, guard);
		assertInHand(olog);
		scn.FreepsPass();

		// 10 - 2 x 2 + 2 for roaming: site number 5 at site 3
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(olog));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(olog));

		scn.ShadowPlayCard(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWinPlaysABesiegerStackedOnASiteYouControlThatIsFierceAndDamagePlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var teeth = scn.GetShadowCard("teeth");
		var garrison = scn.GetShadowCard("garrison");
		var garrison2 = scn.GetShadowCard("garrison2");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// On the site: besiegers of two races, and an Uruk-hai with "Besieger" only in his title.  A besieger stacked
		// on a condition and one in the discard pile.
		scn.StackCardsOn(site1, pillager, troll, urukbesieger);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, garrison2);
		scn.MoveCardsToDiscard(troop);
		scn.MoveCompanionsToTable(gimli, boromir);
		// A besieger in play beside the one to be played.
		scn.MoveMinionsToTable(olog, garrison);
		scn.SetTwilight(30);
		assertInZone(Zone.STACKED, pillager, troll, urukbesieger, garrison2);
		assertInDiscard(troop);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, olog}, new PhysicalCardImpl[]{boromir, garrison});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// 18 against 6: Gimli is overwhelmed.
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(olog));
		scn.ShadowChooseOptionalTrigger(olog);

		assertTrue(scn.ShadowHasCardChoiceAvailable(pillager, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(urukbesieger, garrison2, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pillager);

		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertInZone(Zone.STACKED, troll, urukbesieger, garrison2);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(pillager, Keyword.DAMAGE));
		// The besieger already in play gains neither.
		assertFalse(scn.HasKeyword(garrison, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(garrison, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fierce round: the Pillager is still fierce and damage +1.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(pillager, Keyword.DAMAGE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, olog);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		// A second win: offered again, and the Troll, the one besieger left on the site, is played.
		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(olog));
		scn.ShadowChooseOptionalTrigger(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		// That was the last skirmish, so the regroup phase has begun: the Pillager's bonuses are gone (and the
		// Troll's, laid as the phase began, with them).
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(pillager, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACopyThatLosesItsSkirmishOffersNothingAndACopyThatWinsOffersItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var olog2 = scn.GetShadowCard("olog2");
		var pillager = scn.GetShadowCard("pillager");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.MoveMinionsToTable(olog, olog2);
		scn.SetTwilight(30);
		assertInZone(Zone.STACKED, pillager);
		scn.FreepsPass();

		scn.SkipToAssignments();
		// Gimli at 26 beats the first copy's 18 without overwhelming it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 20));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, olog}, new PhysicalCardImpl[]{boromir, olog2});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli's damage +1: two wounds on vitality 4.
		assertEquals(2, scn.GetWoundsOn(olog));
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(olog2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(olog));
		// 30 set + 3 companions + 0 for site 3
		assertEquals(33, scn.GetTwilight());
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.STACKED, pillager);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertEquals(33, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWinWithNoBesiegerItCouldPlayOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var theirPillager = scn.GetFreepsCard("pillager");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var teeth = scn.GetShadowCard("teeth");
		var garrison2 = scn.GetShadowCard("garrison2");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(3);
		// The player whose fellowship is moving controls site 1, with a besieger of his own stacked on it; the
		// Shadow player controls site 2, with an Uruk-hai with "Besieger" only in his title on it.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirPillager);
		scn.StackCardsOn(site2, urukbesieger);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, garrison2);
		scn.MoveCardsToDiscard(troop);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(olog);
		scn.SetTwilight(30);
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertInZone(Zone.STACKED, theirPillager, urukbesieger, garrison2);
		assertInDiscard(troop);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, olog);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.STACKED, theirPillager, urukbesieger, garrison2);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWinIsNotOfferedWhileThePoolIsShortOfTheBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var pillager = scn.GetShadowCard("pillager");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.MoveMinionsToTable(olog);
		assertInZone(Zone.STACKED, pillager);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, olog);
		scn.FreepsResolveSkirmish(gimli);
		// The Pillager costs 4 + 2 for roaming: site number 5 at site 3.
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, olog);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(6);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(olog));
		scn.ShadowChooseOptionalTrigger(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
