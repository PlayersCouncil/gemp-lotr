package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "13_75");
					put("denethor", "7_86");    // Denethor, Wizened Steward: strength 5, +1 for each other [gondor] Man; vitality 3
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
					put("faramir", "13_66");    // Faramir, Prince of Ithilien: strength 7, vitality 3; no ability to use
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion of another title, strength 8
					// Dear Friends: Fellowship event, "Exert a companion to place a Free Peoples card from your discard
					// pile beneath your draw deck"; with an empty discard pile it places nothing.
					put("dear1", "2_98");
					put("dear2", "2_98");
					put("dear3", "2_98");

					// Uruk Slayer: [isengard] Uruk-hai, strength 9, damage +1, vitality 2; "Skirmish: Remove (1) to make
					// this minion strength +1 (limit +3)"
					put("slayer1", "1_153");
					put("slayer2", "1_153");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1, so it cannot exert
					// Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)", a
					// wound that comes in the regroup phase itself
					put("mslayer", "3_93");
					// Gollum, Dark as Darkness: [gollum] minion, vitality 4; "Skirmish: Remove a burden to add (2)" and
					// "Regroup: Remove a burden to add (3)", the special abilities read across the regroup boundary
					put("gollum", "9_28");
					// Mûmak Commander: "Maneuver: Exert Mûmak Commander twice to exert a companion twice (except the
					// Ring-bearer)": one effect that exerts one companion twice
					put("mumak", "15_86");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StewardsLegacyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Stewards' Legacy
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Denethor exerts, he gains <b>muster</b> until the end of the regroup phase.
		 * 		Each time Boromir exerts, you may exert a minion.
		 * 		Each time Faramir exerts, you may spot a minion to prevent it from using special abilities until the regroup phase.
		 */

		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");

		assertEquals("Stewards' Legacy", legacy.getBlueprint().getTitle());
		assertNull(legacy.getBlueprint().getSubtitle());
		assertTrue(legacy.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, legacy.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, legacy.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, legacy.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(legacy, Keyword.SUPPORT_AREA));
		assertEquals(2, legacy.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeDenethorExertsHeAloneGainsMusterAndWithNoMinionInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var denethor = scn.GetFreepsCard("denethor");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var dear1 = scn.GetFreepsCard("dear1");
		var dear2 = scn.GetFreepsCard("dear2");
		var dear3 = scn.GetFreepsCard("dear3");
		scn.MoveCompanionsToTable(denethor, boromir, faramir);
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCardsToHand(dear1, dear2, dear3);

		scn.StartGame();

		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(boromir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(faramir, Keyword.MUSTER));

		// The fellowship phase: no minion to exert, and none to spot, so Boromir's and Faramir's offers are not
		// made; and neither exertion gives anyone muster.
		scn.FreepsPlayCard(dear1);
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(boromir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));

		scn.FreepsPlayCard(dear2);
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(faramir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));

		// Denethor's has no "may": he gains muster with nothing asked, and nobody else does.
		scn.FreepsPlayCard(dear3);
		scn.FreepsChooseCard(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(boromir, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(faramir, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DenethorWoundedInALostSkirmishAndByAWoundInTheRegroupPhaseHasNoMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var denethor = scn.GetFreepsCard("denethor");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var dear1 = scn.GetFreepsCard("dear1");
		scn.MoveCompanionsToTable(denethor, boromir, faramir);
		scn.MoveCardsToSupportArea(legacy);

		var troop1 = scn.GetShadowCard("troop1");
		var mslayer = scn.GetShadowCard("mslayer");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, mslayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(denethor, troop1);
		scn.ShadowDeclineAssignments();
		// A card in hand, so that muster, had he gained it, would be offered.
		scn.MoveCardsToHand(dear1);

		// 5 + 2 for Boromir and Faramir: 7 against 13, a loss and not an overwhelming.
		assertEquals(7, scn.GetStrength(denethor));
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		scn.FreepsResolveSkirmish(denethor);
		scn.PassSkirmishActions();

		// A wound is no exertion.
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A wound taken in the regroup phase itself, where a muster gained from it would still be in force.
		scn.FreepsPass();
		scn.ShadowUseCardAction(mslayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(denethor, boromir, faramir));
		scn.ShadowChooseCard(denethor);
		assertEquals(2, scn.GetWoundsOn(denethor));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MusterGainedInASkirmishLastsThroughTheRegroupPhaseAndEndsWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dear1 = scn.GetFreepsCard("dear1");
		scn.MoveCompanionsToTable(denethor, aragorn);
		scn.MoveCardsToSupportArea(legacy);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// A card in hand, so that muster can be used.
		scn.MoveCardsToHand(dear1);
		scn.FreepsResolveSkirmish(aragorn);

		// Ettenmoors exerts Denethor, who is in no skirmish.
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));
		scn.PassSkirmishActions();

		// The regroup phase: he has muster still, and it is offered.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));

		// The regroup phase ends as the fellowship moves on: the Shadow phase at site 3.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DenethorExertingOnALaterTurnGainsMusterAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var denethor = scn.GetFreepsCard("denethor");
		var dear1 = scn.GetFreepsCard("dear1");
		var dear2 = scn.GetFreepsCard("dear2");
		scn.MoveCompanionsToTable(denethor);
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCardsToHand(dear1);

		scn.StartGame();
		scn.FreepsPlayCard(dear1);
		scn.FreepsChooseCard(denethor);
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));

		// The other player's turn, and then this player's next.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(dear2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));

		scn.FreepsPlayCard(dear2);
		scn.FreepsChooseCard(denethor);
		assertEquals(2, scn.GetWoundsOn(denethor));
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBoromirExertsTheOfferMayBeDeclinedAndTakenItExertsOneMinionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(legacy);
		var frodo = scn.GetRingBearer();

		var slayer1 = scn.GetShadowCard("slayer1");
		var troop1 = scn.GetShadowCard("troop1");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, troop1, runner);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(boromir, slayer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: no minion is exerted, and Ettenmoors' own effect follows.
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// His next exertion makes the offer again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();

		// Minions of two cultures and races, the one in his skirmish and one in none; not a minion that cannot
		// exert, nor a companion.  "A minion" is exactly one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, troop1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, aragorn, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop1);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BoromirExertedTwiceByOneEffectMakesTwoOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(legacy);

		var mumak = scn.GetShadowCard("mumak");
		var slayer1 = scn.GetShadowCard("slayer1");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(mumak, slayer1, troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(mumak);
		assertEquals(2, scn.GetWoundsOn(mumak));
		scn.ShadowChooseCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(slayer1);

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(2, scn.GetWoundsOn(mumak));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertNothingIsOfferedWhenBoromirExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);

		// The only minion has 1 vitality: a yes would exert nobody, so the question is not put.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AWoundOnBoromirOrFaramirOrAnotherGondorCompanionExertingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, faramir, aragorn);
		scn.MoveCardsToSupportArea(legacy);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, troop1}, new PhysicalCardImpl[]{faramir, troop2});
		scn.FreepsResolveSkirmish(boromir);

		// Aragorn, a [gondor] companion of another title, exerts: no offer, with minions able to exert and to be
		// spotted.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Boromir loses to 13 (he is not overwhelmed unless tripled) and takes a wound: no offer.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Faramir loses to 13 (not double his 7) and takes a wound: no offer.
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeFaramirExertsTheOfferMayBeDeclinedAndTakenTheMinionSpottedCannotUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToSupportArea(legacy);
		var frodo = scn.GetRingBearer();

		var slayer1 = scn.GetShadowCard("slayer1");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, gollum);
		// A burden for Gollum's ability, and (1) for the Slayer's.
		scn.AddBurdens(1);
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(faramir, slayer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(faramir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: both minions keep their special abilities.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(slayer1));

		// His next exertion makes the offer again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();

		// A minion of either culture, in his skirmish or in none; not a companion.  Exactly one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, gollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(faramir, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gollum);

		// A spot costs nothing; Gollum, the one spotted, loses his ability and the Slayer keeps its own.
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(slayer1));
	}

	@Test
	public void TheMinionSpottedCannotUseSpecialAbilitiesInALaterSkirmishAndCanInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToSupportArea(legacy);

		var runner = scn.GetShadowCard("runner");
		var slayer1 = scn.GetShadowCard("slayer1");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, slayer1, gollum);
		scn.AddBurdens(1);
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, runner}, new PhysicalCardImpl[]{aragorn, slayer1});
		scn.ShadowDeclineAssignments();

		// The first skirmish: Faramir exerts, and Gollum is spotted.
		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gollum);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(slayer1));
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);

		// The next skirmish: Gollum still cannot use his ability, the Slayer can.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(slayer1));

		// The Slayer uses its ability; Faramir then exerts a second time and the Slayer is spotted as well.
		scn.ShadowUseCardAction(slayer1);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(slayer1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer1));
		assertFalse(scn.ShadowActionAvailable(gollum));
		scn.ShadowPass();
		scn.FreepsPass();

		// The regroup phase: the bar is over, and Gollum's regroup abilities are offered.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void FaramirExertedTwiceByOneEffectMakesTwoOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToSupportArea(legacy);

		var mumak = scn.GetShadowCard("mumak");
		var gollum = scn.GetShadowCard("gollum");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(mumak, gollum, slayer1, slayer2);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(mumak);
		scn.ShadowChooseCard(faramir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gollum);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(slayer1);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// In the skirmish both minions spotted are barred, and the other Slayer is not.
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(aragorn, mumak);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertFalse(scn.ShadowActionAvailable(slayer1));
		assertTrue(scn.ShadowActionAvailable(slayer2));
	}
}
