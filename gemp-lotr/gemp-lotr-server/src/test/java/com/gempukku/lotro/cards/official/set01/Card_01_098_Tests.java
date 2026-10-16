package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "1_98");

					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion who is not Boromir; a maneuver ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: a Man ally; a fellowship ability used only on purpose

					// Weather and other conditions.
					put("chill", "1_134");      // Saruman's Chill: [isengard] weather condition that plays on a site
					put("urgency", "19_17");    // Urgency: [isengard] weather condition in the support area; a Shadow skirmish ability
					put("spies", "1_140");      // Spies of Saruman: [isengard] condition on a site, not a weather
					put("stairs", "1_24");      // Stairs of Khazad-dum: a Free Peoples condition in the support area, not a weather

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
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
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Cloak
		 * Game Text: Bearer must be Boromir.
		 * <b>Maneuver:</b> Exert Boromir to discard a weather condition.
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
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnceBoromirIsInPlayAndGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(cloak, boromir);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		// A [gondor] Man companion, a Man ally and Frodo, but no Boromir.
		assertInHand(cloak);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cloak));

		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsPlayAvailable(cloak));

		// Boromir is the one bearer there can be, so he is not asked for.
		scn.FreepsPlayCard(cloak);
		assertAttachedTo(cloak, boromir);
		assertNotAttachedTo(cloak, aragorn);
		assertNotAttachedTo(cloak, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionExertsBoromirToDiscardAWeatherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var stairs = scn.GetFreepsCard("stairs");
		var chill = scn.GetShadowCard("chill");
		var urgency = scn.GetShadowCard("urgency");
		var spies = scn.GetShadowCard("spies");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(urgency);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AttachCardsTo(scn.GetCurrentSite(), chill, spies);
		scn.FreepsRefreshDecision();

		assertAttachedTo(chill, scn.GetCurrentSite());
		assertAttachedTo(spies, scn.GetCurrentSite());
		assertInPlay(urgency, stairs);
		assertAttachedTo(cloak, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(cloak));
		scn.FreepsUseCardAction(cloak);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// A weather on the site and one in the support area; not a Shadow condition or a Free Peoples
		// condition that is no weather.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chill, urgency));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spies, stairs));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(urgency);

		assertInDiscard(urgency);
		assertInPlay(chill, spies, stairs);
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWhileBoromirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var urgency = scn.GetShadowCard("urgency");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(urgency);
		// Vitality 3 with 2 wounds: exhausted. Aragorn, unwounded, cannot exert in his place.
		scn.AddWoundsToChar(boromir, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInPlay(urgency);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cloak));

		// One wound off, and he can pay again.
		scn.RemoveWoundsFromChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cloak));
	}

	@Test
	public void ManeuverActionIsOfferedAndPaidForNothingWithNoWeatherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var stairs = scn.GetFreepsCard("stairs");
		var spies = scn.GetShadowCard("spies");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AttachCardsTo(scn.GetCurrentSite(), spies);
		scn.FreepsRefreshDecision();

		// Conditions in play, but no weather. The exertion is a cost and can be paid, so the action is offered.
		assertAttachedTo(spies, scn.GetCurrentSite());
		assertInPlay(stairs);
		assertAttachedTo(cloak, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInPlay(spies, stairs);
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
