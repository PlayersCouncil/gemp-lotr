package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_033_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grumbler", "102_33");
					put("grumbler2", "102_33");

					// [sauron] trackers, both Orcs.  Their texts turn on a site number of 1 or less, which
					// nothing here brings about.
					put("spotter", "102_60");    // Lugburz Spotter: strength 6, vitality 2
					put("lugburz", "102_63");    // Orc of Lugburz: strength 9, vitality 3
					// Orc Chieftain: a [sauron] Orc and no tracker, strength 7, vitality 2.  Adds (2) when a
					// weapon is played on him.
					put("chieftain", "1_266");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					// Gimli, Son of Gloin: strength 6, vitality 3, so he loses to the Grumbler's 7 without being
					// overwhelmed.  "Skirmish: Exert Gimli to make him strength +2", never used here.
					put("gimli", "1_13");
					// Bounder: a Hobbit ally whose home is site 2, where every test here is fought.  Strength 2,
					// vitality 2, and a skirmish ability that is never used.
					put("bounder", "1_286");
					// Unexpected Visitor: a condition on a [gondor] companion.  "Each minion gains this ability:
					// 'Assignment: Assign this minion to bearer of Unexpected Visitor.'"  Its regroup ability is
					// never used.
					put("visitor", "7_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukGrumblerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Grumbler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: Tracker. Fierce.
		 * Each time this minion is assigned to skirmish a companion, you may spot a [sauron] tracker and discard this minion to wound that companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grumbler");

		assertEquals("Uruk Grumbler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToACompanionByTheFreePeoplesPlayerItMayBeDiscardedToWoundThatCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var spotter = scn.GetShadowCard("spotter");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, spotter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, grumbler}, new PhysicalCardImpl[]{gimli, spotter});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, grumbler));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// "That companion": not the one another minion was assigned to, and not the Ring-bearer.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The [sauron] tracker is only spotted.
		assertInZone(Zone.SHADOW_CHARACTERS, spotter);
		assertEquals(0, scn.GetWoundsOn(spotter));

		// Gimli's skirmish with the Spotter is still to be fought.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, spotter));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedToTheRingBearerByTheShadowPlayerItMakesTheSameOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var lugburz = scn.GetShadowCard("lugburz");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, lugburz);
		scn.SkipToAssignments();
		// The Free Peoples player assigns nobody, and both minions are the Shadow player's to assign.
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{frodo, grumbler}, new PhysicalCardImpl[]{aragorn, lugburz});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, lugburz);
		assertEquals(0, scn.GetWoundsOn(lugburz));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lugburz));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedByAnAssignmentActionItMakesTheSameOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var spotter = scn.GetShadowCard("spotter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, visitor);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, spotter);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grumbler));
		assertFalse(scn.IsCharAssigned(grumbler));

		scn.ShadowUseCardAction(grumbler);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, grumbler));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, spotter);
		// The assignment phase goes on.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedToAnAllyItMakesNoOfferWhileACopyAssignedToACompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var grumbler2 = scn.GetShadowCard("grumbler2");
		var spotter = scn.GetShadowCard("spotter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, grumbler2, spotter);
		scn.SkipToAssignments();
		// Site 2 is the Bounder's home, so he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, grumbler},
				new PhysicalCardImpl[]{aragorn, grumbler2},
				new PhysicalCardImpl[]{gimli, spotter});

		assertTrue(scn.IsCharAssignedAgainst(bounder, grumbler));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, grumbler2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grumbler));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// Nothing more is offered: neither for the copy on the ally nor for the [sauron] tracker on Gimli.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, grumbler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedItFightsItsSkirmishAndMakesTheOfferAgainWhenAssignedInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var spotter = scn.GetShadowCard("spotter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, spotter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, grumbler}, new PhysicalCardImpl[]{aragorn, spotter});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertTrue(scn.IsCharAssignedAgainst(gimli, grumbler));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn's 8 against the Spotter's 6: the Spotter takes a wound and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(spotter));
		assertInZone(Zone.SHADOW_CHARACTERS, spotter);

		// Gimli's 6 against the Grumbler's 7: Gimli takes a wound, and the Grumbler, who is fierce, comes again.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, grumbler));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, grumbler);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler);
		// 1 from the skirmish + 1
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// No minion is left to fight a fierce skirmish.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutASauronTrackerAndIsOnceOneIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var spotter = scn.GetShadowCard("spotter");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		// A [sauron] Orc who is no tracker, and the Grumbler himself, a tracker who is not [sauron].
		scn.MoveMinionsToTable(grumbler, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, grumbler}, new PhysicalCardImpl[]{aragorn, chieftain});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.IsCharAssignedAgainst(gimli, grumbler));

		// Aragorn's 8 against the Chieftain's 7, and Gimli's 6 against the Grumbler's 7.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler, chieftain);

		// A [sauron] tracker joins them before the fierce assignment.
		scn.MoveMinionsToTable(spotter);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, grumbler);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, grumbler);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoCopiesEachMakeTheirOwnOfferAndTheCopyChosenWoundsTheCompanionItWasAssignedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var grumbler2 = scn.GetShadowCard("grumbler2");
		var spotter = scn.GetShadowCard("spotter");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(grumbler, grumbler2, spotter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, grumbler},
				new PhysicalCardImpl[]{gimli, grumbler2},
				new PhysicalCardImpl[]{frodo, spotter});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler2));

		scn.ShadowChooseOptionalTrigger(grumbler2);

		assertInZone(Zone.DISCARD, grumbler2);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The other copy's offer still stands.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grumbler));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grumbler2));

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, grumbler);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, grumbler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
