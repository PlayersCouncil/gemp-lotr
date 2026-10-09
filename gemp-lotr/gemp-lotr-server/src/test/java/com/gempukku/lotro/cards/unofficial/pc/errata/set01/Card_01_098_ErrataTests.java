package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_098_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "51_98");

					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion who is not Boromir

					// Weather and other conditions.
					put("chill", "1_134");      // Saruman's Chill: [isengard] weather condition that plays on a site
					put("urgency", "19_17");    // Urgency: [isengard] weather condition in the support area; a Shadow skirmish ability
					put("spies", "1_140");      // Spies of Saruman: [isengard] condition on a site, not a weather

					// Skirmish events and special abilities.
					put("bred1", "1_121");      // Bred for Battle: Shadow skirmish event, "exert an Uruk-hai to make it strength +3"
					put("bred2", "1_121");
					put("stout", "7_121");      // Stout Resistance: Free Peoples skirmish event, "add a threat to make a [gondor] Man strength +3"
					put("kings", "1_115");      // Strength of Kings: Free Peoples Response event that cancels a skirmish event
					put("numenor", "5_37");     // Men of Numenor: Free Peoples skirmish event, "wound a minion bearing a [gondor] fortification"
					put("wall", "5_33");        // City Wall: [gondor] fortification, cheated onto a minion (strength -3 there)

					put("savage1", "1_151");    // Uruk Savage: Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("soldier", "1_271");    // Orc Soldier: "Skirmish: exert this minion to wound a character he is skirmishing"
					put("soldier2", "1_271");
					put("shaman", "3_59");      // Isengard Shaman: [isengard] Orc; "Response: if an [isengard] Orc is about to take a wound, remove (2) to prevent that wound"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirsCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Boromir's Cloak
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Cloak
		 * Game Text: Bearer must be Boromir.
		 * Maneuver: Exert Boromir to discard a weather condition.
		 * Response: If a skirmish event or special ability is used during a skirmish involving Boromir, discard this to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloak");

		assertEquals("Boromir's Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.CLOAK));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedForOneOnlyOnceBoromirIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(cloak, boromir);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// A [gondor] Man, but not Boromir.
		assertFalse(scn.FreepsPlayAvailable(cloak));

		scn.FreepsPlayCard(boromir);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(cloak));

		// Boromir is the one bearer there can be, so he is not asked for.
		scn.FreepsPlayCard(cloak);
		assertAttachedTo(cloak, boromir);
		assertNotAttachedTo(cloak, aragorn);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionExertsBoromirToDiscardAWeatherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var chill = scn.GetShadowCard("chill");
		var urgency = scn.GetShadowCard("urgency");
		var spies = scn.GetShadowCard("spies");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(urgency);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AttachCardsTo(scn.GetCurrentSite(), chill, spies);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(cloak));
		scn.FreepsUseCardAction(cloak);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// A weather on the site and one in the support area; not a condition that is no weather.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chill, urgency));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spies));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chill);

		assertInDiscard(chill);
		assertInPlay(urgency, spies);
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceBoromirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var urgency = scn.GetShadowCard("urgency");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(urgency);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(cloak));
		assertInPlay(urgency);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var urgency = scn.GetShadowCard("urgency");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(urgency);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cloak));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInPlay(urgency);
	}

	@Test
	public void PreventsAShadowSkirmishEventInBoromirsSkirmishAndNotInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var bred1 = scn.GetShadowCard("bred1");
		var bred2 = scn.GetShadowCard("bred2");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.MoveCardsToHand(bred1, bred2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, savage1}, new PhysicalCardImpl[]{aragorn, savage2});

		// Aragorn's skirmish: Boromir is not in it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPlayCard(bred1);
		scn.ShadowChooseCard(savage2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cloak));
		assertEquals(8, scn.GetStrength(savage2));
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Boromir's skirmish.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(savage1));
		scn.ShadowPlayCard(bred2);
		scn.ShadowChooseCard(savage1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsChooseOptionalTrigger(cloak);

		assertInDiscard(cloak, bred2);
		// The exertion was the event's cost and stays paid; the +3 does not happen.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PreventsAShadowSpecialAbilityInBoromirsSkirmishAndNotInAnotherAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetShadowCard("soldier");
		var otherSoldier = scn.GetShadowCard("soldier2");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, otherSoldier);
		scn.MoveCardsToSupportArea(urgency);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, soldier}, new PhysicalCardImpl[]{aragorn, otherSoldier});

		// Aragorn's skirmish: Boromir is not in it, so the same ability used there is not answered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(otherSoldier));
		scn.ShadowUseCardAction(otherSoldier);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cloak));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(otherSoldier));
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Boromir's skirmish: a condition's skirmish ability is offered, and declined it happens.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(soldier));
		scn.ShadowUseCardAction(urgency);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(8, scn.GetStrength(soldier));
		assertInDiscard(urgency);
		assertAttachedTo(cloak, boromir);

		// A minion's skirmish ability: offered, and taken it does not happen.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.ShadowUseCardAction(soldier);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsChooseOptionalTrigger(cloak);

		assertInDiscard(cloak);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAgainstAFreePeoplesSkirmishEventInBoromirsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var stout = scn.GetFreepsCard("stout");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.MoveCardsToHand(stout);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(stout);
		// The text names no side: the Free Peoples player's own skirmish event is answered too.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(1, scn.GetThreats());
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedAgainstAResponseEventInBoromirsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var kings = scn.GetFreepsCard("kings");
		var savage = scn.GetShadowCard("savage1");
		var bred = scn.GetShadowCard("bred1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.MoveCardsToHand(kings);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(bred);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsPass();
		scn.ShadowPlayCard(bred);
		// Both answer the skirmish event; Strength of Kings, a Response event, is played instead of the Cloak.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsChooseOptionalTrigger(kings);

		assertInDiscard(kings, bred);
		assertEquals(5, scn.GetStrength(savage));
		// Strength of Kings is no skirmish event: the Cloak is not offered against it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cloak));
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedAgainstAResponseAbilityUsedInBoromirsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.AttachCardsTo(shaman, wall);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, shaman);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(2);

		// Men of Numenor is a skirmish event played in Boromir's skirmish: the Cloak is offered, and kept.
		scn.FreepsPlayCard(numenor);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cloak));
		scn.FreepsDeclineOptionalTrigger();

		// The Shaman, about to take its wound, uses his Response: the Shadow player's special ability, used
		// during a skirmish involving Boromir, and no Skirmish: ability.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);
		assertEquals(0, scn.GetTwilight());

		// Only skirmish special abilities are answered (Ketura on Q-0119, 1B): the Cloak is not offered, and
		// the wound is prevented.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cloak));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertAttachedTo(cloak, boromir);
		assertInDiscard(numenor);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

}
