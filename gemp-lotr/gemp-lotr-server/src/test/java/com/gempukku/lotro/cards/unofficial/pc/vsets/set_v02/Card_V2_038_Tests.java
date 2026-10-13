package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_038_Tests
{
	// Every skirmish here is fought at site 2 of the Fellowship path, Ettenmoors: "Skirmish: Exert your companion or
	// minion to make that character strength +2", a skirmish special ability either player may use.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "102_38");
					put("savage", "1_151");      // Uruk Savage: [isengard], strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");      // Goblin Runner: [moria], strength 5, vitality 1

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, never used
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3, Damage +1.  "Skirmish: Exert Gimli to make him strength +2."
					put("boromir", "1_97");      // Boromir, Son of Denethor: vitality 3.  "Skirmish: Exert Boromir to make a Hobbit strength +3."
					put("bounder", "1_286");     // Bounder: Hobbit ally, home site 2, vitality 2.  "Skirmish: Exert this ally to prevent a Hobbit from being overwhelmed unless that Hobbit's strength is tripled."
					put("saga", "1_114");        // The Saga of Elendil: condition on a [gondor] companion, vitality +1.  "Skirmish: Discard this condition to make bearer strength +2."
					put("swordarm", "1_116");    // Swordarm of the White Tower: skirmish event for 0.  "Make a [gondor] companion strength +2."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: The Balrog, Lieutenant of Morgoth
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: Fierce. Damage +1.
		 * Each time a minion takes a wound, you may remove (1) to add a threat.
		 * Each time the Free Peoples player uses a skirmish special ability during a skirmish involving the Balrog, you may wound a companion it is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgoth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachMinionTakingAWoundLetsTheShadowPlayerRemoveOneTwilightToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		// Four companions, so that three threats can be added.
		scn.MoveCompanionsToTable("aragorn", "gimli", "boromir");

		// Three fellowship archery wounds, for the Shadow player to place one at a time.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, null, 3));

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage, runner);
		scn.SkipToArcheryWounds();
		scn.SetTwilight(3);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage));

		// A minion of another culture
		scn.ShadowChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// The Balrog himself
		assertEquals(0, scn.GetWoundsOn(balrog));
		scn.ShadowChooseCard(balrog);

		assertEquals(1, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());

		// A minion the wound kills
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.ShadowChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoWoundsOnOneMinionAreTwoOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage);
		scn.SkipToAssignments();
		scn.SetTwilight(2);
		scn.FreepsAssignAndResolve(gimli, savage);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetThreats());

		// Gimli's 6 beats the Savage's 5, and Damage +1 makes it two wounds on the one minion.
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(savage));
		// The Balrog was assigned to nobody and is fierce: the fierce assignment phase is next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedItCostsNothingAndWithAnEmptyPoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable("aragorn");

		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, null, 3));

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage);
		scn.SkipToArcheryWounds();
		scn.SetTwilight(1);

		scn.ShadowChooseCard(savage);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		// The second wound makes the offer again, and paying for it empties the pool.
		scn.ShadowChooseCard(savage);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// The third wound, on the Balrog, finds nothing to pay with.
		scn.ShadowChooseCard(balrog);

		assertEquals(1, scn.GetWoundsOn(balrog));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAsManyThreatsAsCompanionsItIsStillOfferedAndPaidForWithNoThreatAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable("aragorn");

		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, null, 1));

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage);
		scn.SkipToArcheryWounds();
		scn.SetTwilight(2);
		// Frodo and Aragorn: no more than 2 threats.
		scn.AddThreats(2);

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());

		scn.ShadowChooseCard(savage);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachFreePeoplesSkirmishAbilityUsedInTheBalrogsSkirmishLetsHimWoundTheCompanionHeFights() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage);
		scn.SkipToAssignments();
		// Twilight to spare, so that a companion's wound could be answered as a minion's is if it counted as one.
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, balrog}, new PhysicalCardImpl[]{gimli, savage});
		scn.FreepsResolveSkirmish(aragorn);
		var ettenmoors = scn.GetCurrentSite();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, balrog));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// An ability of a card the companion in the skirmish bears.  Declined, nobody is wounded.
		scn.FreepsUseCardAction(saga);

		assertInZone(Zone.DISCARD, saga);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// An ability of a companion who is in no skirmish
		scn.FreepsUseCardAction(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Boromir's one wound is his own exertion.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// The site's ability, paid for by Gimli, who is assigned to the Savage in a skirmish yet to be fought
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// A companion's wounds are not a minion's: nothing was removed to add a threat.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASkirmishAbilityUsedInAnotherSkirmishAndASkirmishEventInTheBalrogsSetNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(swordarm);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog, savage);
		scn.SkipToAssignments();
		// An empty pool, so that the Savage's wounds bring no offer of their own.
		scn.SetTwilight(0);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, balrog}, new PhysicalCardImpl[]{gimli, savage});

		// Gimli's skirmish with the Savage: the Balrog is assigned, and his skirmish has not begun.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();

		// The Balrog's skirmish.  An event is no special ability.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, balrog));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(swordarm);

		assertEquals(10, scn.GetStrength(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// The ability that set nothing off in the Savage's skirmish does in the Balrog's.
		scn.FreepsUseCardAction(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Gimli's two wounds are his two exertions.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayerUsingASkirmishAbilityToExertTheBalrogSetsNeitherTriggerOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog);
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignAndResolve(aragorn, balrog);
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(balrog));
		assertEquals(17, scn.GetStrength(balrog));

		// The same site ability that sets the trigger off when the Free Peoples player uses it.  The Balrog is
		// the Shadow player's only minion and so the one who exerts: a wound token, and no wound taken.
		scn.ShadowUseCardAction(ettenmoors);

		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(19, scn.GetStrength(balrog));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheRingsResponseUsedInTheBalrogsSkirmishIsNotASkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, balrog);
		var ettenmoors = scn.GetCurrentSite();

		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(balrog));
		scn.ShadowAcceptOptionalTrigger();

		// The Ruling Ring: "Response: If bearer is about to take a wound in a skirmish, he wears The One Ring
		// until the regroup phase."  A special ability the Free Peoples player uses in the Balrog's skirmish,
		// and not a skirmish special ability.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void FightingAnAllyTheBalrogHasNoCompanionToWoundAndMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		scn.MoveMinionsToTable(balrog);
		scn.SkipToAssignments();
		// At site 2 the Bounder is at home and may be assigned.
		scn.FreepsAssignAndResolve(bounder, balrog);

		assertTrue(scn.IsCharSkirmishingAgainst(bounder, balrog));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsUseCardAction(bounder);

		// An ally is no companion: with nobody the Balrog may wound, and nothing to pay, there is nothing to offer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// The Bounder's ability goes on to its choice of a Hobbit, himself or Frodo.
		scn.FreepsChooseCard(bounder);

		// The one wound is the Bounder's exertion.
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
