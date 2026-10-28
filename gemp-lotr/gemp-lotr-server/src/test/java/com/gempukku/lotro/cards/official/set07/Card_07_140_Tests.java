package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assailant", "7_140");
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul, vitality 3;
					                            // his bonuses want 3 and 5 burdens, and these tests lay 2 at most
					put("infantry", "4_227");   // Easterling Infantry: an Easterling Man; fierce and damage +1 only at 3 burdens
					put("veteran", "7_147");    // Easterling Veteran: an Easterling Man of vitality 3; his ability removes 2 burdens
					put("sentry", "4_253");     // Southron Sentry: a [raider] Man who is no Easterling; a maneuver ability only
					put("scout", "15_77");      // Easterling Scout: a [men] minion with "Easterling" in his title only
					put("troop", "1_177");      // Goblin Patrol Troop: a card with no text, for the Free Peoples hand
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingAssailantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Assailant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.
		 * <b>Skirmish:</b> Remove a burden and exert this minion to heal another Easterling (or 2 Easterlings if you have initiative).
		*/

		var scn = GetScenario();

		var assailant = scn.GetFreepsCard("assailant");

		assertEquals("Easterling Assailant", assailant.getBlueprint().getTitle());
		assertNull(assailant.getBlueprint().getSubtitle());
		assertFalse(assailant.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, assailant.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, assailant.getBlueprint().getCulture());
		assertEquals(CardType.MINION, assailant.getBlueprint().getCardType());
		assertEquals(Race.MAN, assailant.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(assailant, Keyword.EASTERLING));
		assertEquals(4, assailant.getBlueprint().getTwilightCost());
		assertEquals(9, assailant.getBlueprint().getStrength());
		assertEquals(2, assailant.getBlueprint().getVitality());
		assertEquals(4, assailant.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithoutInitiativeItRemovesABurdenAndExertsToHealOneOtherEasterling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Exactly four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"));

		var assailant = scn.GetShadowCard("assailant");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(assailant, khamul, infantry, sentry, scout);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(infantry, 1);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(scout, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(assailant));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(assailant));
		scn.ShadowUseCardAction(assailant);

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(assailant));
		// The Assailant, wounded now by his own exertion, is no other Easterling.
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, infantry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(assailant, sentry, scout));
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(assailant));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithInitiativeItHealsTwoOtherEasterlings() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Exactly three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"));

		var assailant = scn.GetShadowCard("assailant");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var veteran = scn.GetShadowCard("veteran");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(assailant, khamul, infantry, veteran, sentry, scout);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(infantry, 1);
		scn.AddWoundsToChar(veteran, 1);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(assailant));
		scn.ShadowUseCardAction(assailant);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(assailant));
		// Two, and no fewer, while two can be healed.
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, infantry, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(khamul, infantry);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithInitiativeTheAssailantIsNotAmongTheEasterlingsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		// Ketura's ruling (Q-0361): the parenthesis changes only the number healed, not the kind; "2 Easterlings" are
		// two other Easterlings, so the Assailant, wounded by his own exertion, is not offered.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var assailant = scn.GetShadowCard("assailant");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(assailant, khamul, infantry, veteran);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(infantry, 1);
		scn.AddWoundsToChar(veteran, 1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(assailant));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(assailant));
		scn.ShadowUseCardAction(assailant);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(assailant));
		// Three other Easterlings wounded, so the two are chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, infantry, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(assailant));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(khamul, infantry);

		assertEquals(1, scn.GetWoundsOn(assailant));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var assailant = scn.GetShadowCard("assailant");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(assailant, khamul);
		scn.AddWoundsToChar(khamul, 2);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(assailant));
		assertEquals(2, scn.GetWoundsOn(khamul));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Assailant can exert; only the burden is missing.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assailant));
		assertEquals(0, scn.GetWoundsOn(assailant));
		assertEquals(2, scn.GetWoundsOn(khamul));
	}

	@Test
	public void ExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var assailant = scn.GetShadowCard("assailant");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(assailant, khamul);
		scn.AddWoundsToChar(assailant, 1);
		scn.AddWoundsToChar(khamul, 2);
		assertEquals(1, scn.GetBurdens());
		// Vitality 2 with 1 wound: exhausted, so he cannot exert.
		assertEquals(1, scn.GetWoundsOn(assailant));
		assertEquals(2, scn.GetWoundsOn(khamul));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assailant));
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(khamul));
	}

	@Test
	public void WithNoOtherEasterlingWoundedTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"));

		var assailant = scn.GetShadowCard("assailant");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(assailant, infantry, sentry);
		scn.AddWoundsToChar(sentry, 1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assailant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(assailant));
		scn.ShadowUseCardAction(assailant);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(assailant));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
