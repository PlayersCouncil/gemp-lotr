package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "7_139");
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul, vitality 3;
					                            // his bonuses want 3 and 5 burdens, and these tests lay none
					put("infantry", "4_227");   // Easterling Infantry: an Easterling Man; fierce and damage +1 only at 3 burdens
					put("veteran", "7_147");    // Easterling Veteran: an Easterling Man of vitality 3; his ability removes 2 burdens
					put("sentry", "4_253");     // Southron Sentry: a [raider] Man who is no Easterling; a maneuver ability only
					put("scout", "15_77");      // Easterling Scout: a [men] minion with "Easterling" in his title only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] card with no text, for a hand
					put("sscout", "4_252");     // Southron Scout: a [raider] card for a hand
					put("sscout2", "4_252");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.
		 * <b>Skirmish:</b> If you have initiative or you spot 3 Easterlings, discard 2 cards from hand to heal another Easterling.
		*/

		var scn = GetScenario();

		var aggressor = scn.GetFreepsCard("aggressor");

		assertEquals("Easterling Aggressor", aggressor.getBlueprint().getTitle());
		assertNull(aggressor.getBlueprint().getSubtitle());
		assertFalse(aggressor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, aggressor.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, aggressor.getBlueprint().getCulture());
		assertEquals(CardType.MINION, aggressor.getBlueprint().getCardType());
		assertEquals(Race.MAN, aggressor.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(aggressor, Keyword.EASTERLING));
		assertEquals(3, aggressor.getBlueprint().getTwilightCost());
		assertEquals(8, aggressor.getBlueprint().getStrength());
		assertEquals(2, aggressor.getBlueprint().getVitality());
		assertEquals(4, aggressor.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeAndTwoEasterlingsItDiscardsTwoCardsFromHandToHealTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("sscout"), scn.GetFreepsCard("sscout2"));

		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var sscout = scn.GetShadowCard("sscout");
		var sscout2 = scn.GetShadowCard("sscout2");
		scn.MoveCardsToHand(troop, sscout, sscout2);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, veteran, sentry);
		scn.AddWoundsToChar(veteran, 2);
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(troop);
		assertInHand(sscout);
		assertInHand(sscout2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two Easterlings and a Southron: initiative alone opens it.
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(aggressor);

		// Any card of the hand may go, [raider] or not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, sscout, sscout2));
		scn.ShadowChooseCards(troop, sscout);

		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.DISCARD, sscout);
		assertInHand(sscout2);
		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithoutInitiativeAndThreeEasterlingsItHealsAnotherEasterlingOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("sscout"), scn.GetFreepsCard("sscout2"),
				scn.GetFreepsCard("veteran"));

		var aggressor = scn.GetShadowCard("aggressor");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		var sscout = scn.GetShadowCard("sscout");
		scn.MoveCardsToHand(troop, sscout);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, khamul, infantry, sentry, scout);
		scn.AddWoundsToChar(aggressor, 1);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(infantry, 1);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(troop);
		assertInHand(sscout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Exactly 3 Easterlings, the Aggressor himself among them, a Man and a Nazgul.
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(aggressor);
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.DISCARD, sscout);

		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, infantry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor, sentry, scout));
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithoutInitiativeAndWithTwoEasterlingsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Exactly four cards in the Free Peoples hand: one more than initiative allows.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("sscout"), scn.GetFreepsCard("sscout2"),
				scn.GetFreepsCard("khamul"));

		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(scn.GetShadowCard("troop"), scn.GetShadowCard("sscout"));

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, veteran, sentry, scout);
		scn.AddWoundsToChar(veteran, 2);
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two Easterlings; the Southron and the Easterling by title do not make a third.
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(aggressor));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetWoundsOn(veteran));
	}

	@Test
	public void WithOneCardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var aggressor = scn.GetShadowCard("aggressor");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(scn.GetShadowCard("troop"));

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, khamul, infantry);
		scn.AddWoundsToChar(khamul, 2);
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Initiative and 3 Easterlings both, but only one card to discard.
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(aggressor));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetWoundsOn(khamul));
	}

	@Test
	public void WithNoOtherEasterlingWoundedTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var aggressor = scn.GetShadowCard("aggressor");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var sscout = scn.GetShadowCard("sscout");
		scn.MoveCardsToHand(troop, sscout);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, infantry, sentry);
		scn.AddWoundsToChar(aggressor, 1);
		scn.AddWoundsToChar(sentry, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(troop);
		assertInHand(sscout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(aggressor);

		// The cost is paid; the Aggressor himself and the Southron are no other Easterling, and nobody is healed.
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.DISCARD, sscout);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
